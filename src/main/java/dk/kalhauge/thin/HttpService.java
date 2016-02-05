package dk.kalhauge.thin;

import dk.kalhauge.util.LinkedPath;
import dk.kalhauge.util.Path;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import static dk.kalhauge.util.Strings.*;
import java.nio.charset.Charset;

class HttpService implements Runnable {
  private final Server server;
  private final Socket socket;

  public HttpService(Server server, Socket socket) throws IOException {
    this.server = server;
    this.socket = socket;
    }

  private String[] split(Server server, Request request) throws Response.BadRequestException {
    String path = request.getPath();
    if (!path.startsWith(server.path())) throw new Response.BadRequestException();
    path = path.substring(server.path().length());
    if (!request.hasBody()) return path.split("/");
    String[] parts = (path+"/#").split("/");
    parts[parts.length - 1] = new String(request.getBody(), Charset.forName("utf-8"));
    return parts;
    }
  
  private Path<String> eatNext(Path<String> parts) {
    Path<String> parameters = parts.getRest();
    if (parameters.isEmpty()) return Path.EMPTY;
    return new LinkedPath<>(parts.getFirst()+pascal(parameters.getFirst()), parameters.getRest());
    }
  
  private Path<String> match(Method method, Path<String> parts) {
    int free = 0;
    for (Class klass : method.getParameterTypes())
        if (klass != Request.class && klass != Response.class) free++;
    return match(method.getName(), free, method.isVarArgs(), parts);
    }
  
  private Path<String> match(String name, int free, boolean varArgs, Path<String> parts) {
    if (parts.isEmpty()) return Path.EMPTY;
    String partName = parts.getFirst();
    if (!name.startsWith(partName)) return Path.EMPTY;
    if (!name.equals(partName)) return match(name, free, varArgs, eatNext(parts));
    if (varArgs && parts.size() >= free) return parts;
    if (parts.size() == free + 1) return parts;
    return Path.EMPTY;
    }
  
  @Override
  public void run() {
    try {
      Request request = new HttpRequest(socket.getInputStream());
      Response response = new HttpResponse(socket.getOutputStream());
      Parser parser = server.parser(request.getContentType());
System.out.print("\n** content:"+request.getContentType());
System.out.print("\n** parser: "+parser);

      try {
        Path<String> parts = new LinkedPath<>(request.getMethod(), split(server, request));
System.out.print("\n** path:   "+String.join(", ", parts));
        Path<String> parameters = Path.EMPTY;
        Method method = null;
        for (Method m : server.getClass().getMethods()) {
          Path<String> p = match(m, parts);
          if (p.isEmpty()) continue;
          if (parameters.isEmpty() || p.getFirst().length() > parameters.getFirst().length()) {
            parameters = p;
            method = m;
            } 
          }
        if (method == null) throw new Response.NotFoundException();
        parameters = parameters.getRest();
System.out.print("\n** method: "+method.getName()+"("+String.join(", ", parameters)+")");
        Class[] types = method.getParameterTypes();
        Object[] values = new Object[types.length];
//        boolean autoResponse = true;
        for (int index = 0; index < types.length; index++) {
          Class type = types[index];
          if (type == Request.class) values[index] = request;
          else if (type == Response.class) {
            values[index] = response;
//            autoResponse = false;
            }
          else {
            String text =
                method.isVarArgs() && index == types.length - 1 
                ? "["+String.join(",", parameters)+"]"
                : parameters.getFirst();
System.out.print("\n** text:   "+text);
System.out.print("\n** parser: "+parser);
            values[index] = parser.fromText(text, type);
            }
          }
        if (method.getReturnType().equals(Void.TYPE)) {
          method.invoke(server, values);
//          if (autoResponse)
          response.status(204).send();
          }
        else {
          Object result = method.invoke(server, values);
          String body = parser.toText(result, method.getReturnType());
          response.send(body);
          }
        }
      catch (Response.HttpException he) {
        response.send(he);
        } 
      catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        response.send(new Response.BadRequestException());
        }
      }
    catch (Response.BadRequestException | IOException bre) {
      server.report(this, bre.getMessage());
      }
    }

  }
