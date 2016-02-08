package dk.kalhauge.thin;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class HttpService implements Runnable {
  private final Server server;
  private final Socket socket;

  public HttpService(Server server, Socket socket) throws IOException {
    this.server = server;
    this.socket = socket;
    }

  @Override
  public void run() {
    try {
      Request request = new HttpRequest(socket.getInputStream());
      Response response = new HttpResponse(socket.getOutputStream());
      Parser parser = server.parser(request.getContentType());
      try {
        Invocator invocator = new Invocator(server, request);
        List<Invocator> candidates = new ArrayList<>();
        for (Method method : server.getClass().getMethods()) {
          Invocator candidate = invocator.matched(method);
          if (candidate != null) candidates.add(candidate);
          }
        if (candidates.isEmpty()) throw new Response.NotFoundException();
        candidates.sort(null);
for (Invocator candidate : candidates) System.out.println("++   "+candidate);
        invocator = candidates.get(0);
        invocator.invoke(parser, request, response, server);
        }
      catch (Response.HttpException he) {
        response.send(he);
        }
      }
    catch (IOException ex) {
      server.report(this, ex.getMessage());
      }
    }
  
  }
