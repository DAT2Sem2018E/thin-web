package dk.kalhauge.thin;

import java.lang.reflect.Method;

public interface Context {
  default boolean isHidden(Method method) { return false; }
  String pathOf(String url) throws Response.BadRequestException;
  }
