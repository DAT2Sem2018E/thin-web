package dk.kalhauge.thin;

import dk.kalhauge.thin.exceptions.ClientErrorException;
import dk.kalhauge.thin.exceptions.NotFoundException;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import static dk.kalhauge.util.Strings.*;
import java.io.File;

class Service implements Runnable {
  private final Server server;
  private final Socket socket;
  private final Gson json = new Gson();

  public Service(Server server, Socket socket) throws IOException {
    this.server = server;
    this.socket = socket;
    }

  private int match(Method method, String... parts) {
    if (parts.length == 0) return 0;
    String name = parts[0];
    for (int index = 1; index < parts.length; index++) {
      name += pascal(parts[index]);
      if (!method.getName().startsWith(name)) return 0;
      if (method.getName().equals(name)) return index + 1;
      }
    return 0;
    }
  
  private class InvocationContext {
    Method method;
    int offset;

    public InvocationContext(Method method, int offset) {
      this.method = method;
      this.offset = offset;
      }
    
    }
  
  private int freeParameters(Method method) {
    int count = 0;
    for (Class klass : method.getParameterTypes())
        if (klass != Request.class && klass != Response.class) count++;
    return count;
    }
  
  private InvocationContext find(Method[] methods, String[] parts) throws ClientErrorException {
    Method candidate = null;
    int bestOffset = 0;
    for (Method method : methods) {
      int offset = match(method, parts);
      if (offset == 0) continue;
      if (offset <= bestOffset) continue;
      if (offset + freeParameters(method) == parts.length) {
        bestOffset = offset;
        candidate = method;
        }
      }
    if (candidate == null) throw new NotFoundException("Method for "+camel(parts)+"  not found");
    return new InvocationContext(candidate, bestOffset);
    }
  
  private boolean invokeMethod(Request request, Response response) throws IOException {
    String path = request.getPath();
    path = path.substring(server.getName().length());
    String[] parts;
    if (request.getBody().length > 0) {
      parts = (path+"/#").split("/");
      parts[parts.length - 1] = new String(request.getBody(), "utf-8");
      }
    else parts = path.split("/");
    parts[0] = request.getMethod();
    
    try {
      InvocationContext context = find(server.getClass().getMethods(), parts);
      Class[] types = context.method.getParameterTypes();
      Object[] values = new Object[types.length];
      for (int index = 0; index < types.length; index++) {
        Class type = types[index];
        if (type == Request.class) values[index] = request;
        else if (type == Response.class) values[index] = response;
        else values[index] = json.fromJson(parts[index + context.offset++], type);
        }
      if (context.method.getReturnType().equals(Void.TYPE)) {
        context.method.invoke(server, values);
        response.status(204).send();
        }
      else {
        Object result = context.method.invoke(server, values);
        String body = json.toJson(result, context.method.getReturnType());
        response.send(body);
        }
      return true;
      }
    catch (ClientErrorException cee) {
      response.status(cee.getStatus()).send(cee.getMessage());
      }
    catch ( IllegalAccessException
        | IllegalArgumentException ex
        ) {
      response.status(400).send(ex.getMessage());
      }
    catch (InvocationTargetException ite) {
      if (ite.getCause() instanceof ClientErrorException) {
        ClientErrorException cee = (ClientErrorException)ite.getCause();
        response.status(cee.getStatus()).send(cee.getMessage());
        }
      else response.status(400).send(ite.getMessage());
      }
    catch (UnsupportedOperationException uoe) {
      response.status(501).send(uoe.getMessage());
      }
    catch (RuntimeException re) {
      response.status(500).send(re.getMessage());
      }
    return false;
    }
  
  private boolean readFile(Request request, Response response) throws IOException {
    File file = server.file(request.getPath());
    System.out.print("\nFile: "+file.getAbsolutePath());
    if (file.isFile()) response.send(file);
    else response.status(404).send(file.getName()+" not found");
    return true;
    }
  
  
  @Override
  public void run() {
    try {
      Request request = new Request(socket.getInputStream());
      Response response = new Response(socket.getOutputStream());
      if (request.getPath().startsWith(server.getName()+"/")) invokeMethod(request, response);
      else readFile(request, response);
      }
    catch (IOException ioe) {
      server.report(this, ioe.getMessage());
      }
    }

  }
