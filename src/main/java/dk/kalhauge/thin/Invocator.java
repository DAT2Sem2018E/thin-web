package dk.kalhauge.thin;

import dk.kalhauge.util.LinkedPath;
import static dk.kalhauge.util.Strings.*;
import dk.kalhauge.util.Path;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class Invocator implements Comparable<Invocator> {
  private final Context context;
  private final String name;
  private final Path<String> texts;
  private Method method = null;

  public Invocator(Context context, String name, Path<String> texts) {
    this.context = context;
    this.name = name;
    this.texts = texts;
    }

  @Override
  public String toString() {
    return name+"("+String.join(", ", texts)+")";
    }
  
  public Invocator(Context context, Request request) throws Response.BadRequestException {
    this(context, request.getMethod(), LinkedPath.create(split(context, request)));
    }
  
  public String getName() {
    return name;
    }

  public Path<String> getTexts() {
    return texts;
    }
  
  private static String[] split(Context context, Request request) throws Response.BadRequestException {
    String path = context.pathOf(request.getPath());
    if (!request.hasBody()) return path.split("/");
    String[] parts = (path+"/#").split("/");
    parts[parts.length - 1] = new String(request.getBody(), Charset.forName("utf-8"));
    return parts;
    }
  
  private boolean matches(Method method) {
    if (context.isHidden(method)) return false;
    if (!method.getName().equals(name)) return false;
    int free = 0;
    for (Class type : method.getParameterTypes()) {
      if (type == Request.class) continue;
      if (type == Response.class) continue;
      if (type == Session.class) continue;
      free++;
      }
    if (method.isVarArgs()) return texts.size() >= free - 1;
    return texts.size() == free;
    }
  
  public Invocator with(Method method) {
    this.method = method;
    return this;
    }
  
  public Invocator matched(Method method) {
    if (!method.getName().startsWith(name)) return null;
    if (matches(method)) return with(method); 
    return texts.isEmpty() ? null : refined().matched(method);
    }
  
  public void invoke(Parser parser, Request request, Response response, Context context) throws IOException {
    try {
      if (method == null) throw new IllegalStateException("Invocator invoked with no method");
      Class[] types = method.getParameterTypes();
      Object[] values = new Object[types.length];
      Path<String> arguments = texts;
      for (int index = 0; index < types.length; index++) {
        Class type = types[index];
        if (type == Request.class) values[index] = request;
        else if (type == Response.class) values[index] = response;
        else if (type == Session.class) values[index] = context.provideSession(request, response);
        else {
          if (method.isVarArgs() && index == types.length - 1)
              values[index] = parser.fromTexts(type, arguments);
          else values[index] = parser.fromText(type, arguments.getFirst());
          arguments = arguments.getRest();
          }
        }
      try {
        if (method.getReturnType() == Void.TYPE) {
          method.invoke(context, values);
          response.status(204).send();
          }
        else {
          Object result = method.invoke(context, values);
System.out.print("\n%% "+result);
          String body = parser.toText(method.getReturnType(), result);
System.out.print("\n%% "+body);
          response.send(body);
          }
        }
      catch (InvocationTargetException ite) { throw ite.getCause(); }
      }
    catch (UnsupportedOperationException uoe) {
      response.send(new Response.NotImplementedException());
      }
    catch (Response.HttpException he) {
      response.send(he);
      }
    catch (Throwable t) {
t.printStackTrace();
      response.send(new Response.InternalServerException());
      }
    }
  
  private Invocator refined() {
    return new Invocator(context, name+pascal(texts.getFirst()), texts.getRest());
    }

  @Override
  public int compareTo(Invocator other) {
    if (this.method == null || other.method == null)
        throw new IllegalStateException("Invocator compared with no method");
    if (this.method.isVarArgs() == other.method.isVarArgs())
        return other.name.compareTo(this.name);
    return this.method.isVarArgs() ? 1 : -1;
    }
  
  }
