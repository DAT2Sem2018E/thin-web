package dk.kalhauge.thin;

import java.lang.reflect.Method;

interface Context {
  default boolean isHidden(Method method) { return false; }
  String pathOf(String url) throws Response.BadRequestException;
  Session provideSession(Request request, Response response);
  }
