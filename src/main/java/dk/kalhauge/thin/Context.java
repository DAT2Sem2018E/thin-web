package dk.kalhauge.thin;

import java.lang.reflect.Method;

public interface Context {
  boolean isHidden(Method method);
  }
