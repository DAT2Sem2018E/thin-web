package dk.kalhauge.thin;

import dk.kalhauge.thin.exceptions.ClientErrorException;
import dk.kalhauge.thin.exceptions.NotFoundException;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import static dk.kalhauge.util.Strings.*;

class HttpService implements Runnable {
  private final Server server;
  private final Socket socket;
  private final Gson gson = new Gson();

  public HttpService(Server server, Socket socket) throws IOException {
    this.server = server;
    this.socket = socket;
    }

  private int match(Method method, String... parts) {
    if (parts.length == 0) return 0;
    int index = 0;
    String name = parts[0];
    do {
      if (!method.getName().startsWith(name)) break;
      if (method.getName().equals(name)) return index + 1;
      if (++index >= parts.length) break;
      name += pascal(parts[index]);
      }
    while (true);
    return -1;
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
    int bestOffset = -1;
    for (Method method : methods) {
      int offset = match(method, parts);
      if (offset < 0) continue;
      if (offset <= bestOffset) continue;
      int count = offset + freeParameters(method);
      if (count == parts.length || method.isVarArgs() && count <= parts.length) {
        bestOffset = offset;
        candidate = method;
        }
      }
    if (candidate == null) throw new NotFoundException("Method for "+camel(parts)+"  not found");
    return new InvocationContext(candidate, bestOffset);
    }
  
  private <T> T fromJson(String text, Class<T> type) {
    if (type == String.class && text.charAt(0) != '"') text = "\""+text+"\"";
    System.out.println(">"+text+"<");
    return gson.fromJson(text, type);
    }
  
  private String pack(String[] parts, int position, boolean last, boolean varArgs) {
    if (!last || !varArgs) return parts[position];
    StringBuilder pack = new StringBuilder("[");
    for (int i = position; i < parts.length; i++) {
      if (i > position) pack.append(",");
      pack.append(parts[i]);
      }
    return pack.append("]").toString();
    }
  
  @Override
  public void run() {
    try {
      Request request = new HttpRequest(socket.getInputStream());
      Response response = new HttpResponse(socket.getOutputStream());
      String path = request.getPath();
      if (!path.startsWith("/"+server.name())) {
        response.status(400).send("Unknown application");
        return;
        }
      path = path.substring(server.name().length() + 1);
      if (!path.isEmpty() && !(path.charAt(0) == '/')) {
        response.status(400).send("Unknown application");
        return;
        }
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
          //if (type.isAssignableFrom(Request.class)) values[index] = request;
          else if (type == Response.class) values[index] = response;
          else {
            String text = pack(parts, context.offset++, index == types.length - 1, context.method.isVarArgs());
            System.out.println("\nvarArgs: "+context.method.isVarArgs()+"\ntext:    '"+text+"'\ntype:    "+type.getName());
            values[index] = gson.fromJson(text, type);
            }
          }
        if (context.method.getReturnType().equals(Void.TYPE)) {
          context.method.invoke(server, values);
          response.status(204).send();
          }
        else {
          Object result = context.method.invoke(server, values);
          String body = gson.toJson(result, context.method.getReturnType());
          response.send(body);
          }
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
        re.printStackTrace();
        response.status(500).send(re.getMessage());
        }
      }
    catch (IOException ioe) {
      server.report(this, ioe.getMessage());
      }
    }

  }
