package dk.kalhauge.thin;

import static dk.kalhauge.util.Strings.*;
import dk.kalhauge.util.Path;
import java.lang.reflect.Method;

public class Invocator {
  private final Context context;
  private final String name;
  private final Path<String> texts;

  public Invocator(Context context, String name, Path<String> texts) {
    this.context = context;
    this.name = name;
    this.texts = texts;
    }

  public String getName() {
    return name;
    }

  public Path<String> getTexts() {
    return texts;
    }
  
  private boolean matches(Method method) {
    if (context.isHidden(method)) return false;
    if (!method.getName().equals(name)) return false;
    int free = 0;
    for (Class type : method.getParameterTypes()) {
      if (type == Request.class) continue;
      if (type == Response.class) continue;
      free++;
      }
    if (method.isVarArgs()) return texts.size() >= free - 1;
    return texts.size() == free;
    }
  
  public Invocator matched(Method method) {
    if (method.getName().startsWith(name)) return null;
    if (!matches(method)) return this; 
    return refined().matched(method);
    }
  
  public Invocator refined() {
    return new Invocator(context, name+pascal(texts.getFirst()), texts.getRest());
    }
  
  }
