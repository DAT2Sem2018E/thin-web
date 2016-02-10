package dk.kalhauge.thin;

import java.util.Map;

public interface Request {
  Map<String, String> getCookies();
  Map<String, String> getHeaders();
  Map<String, String> getParameters();
  String getParameter(String key);
  String getCookie(String key);
  String getSessionId();
  String getMethod();
  String getPath();
  byte[] getBody();
  boolean hasBody();
  int getContentLength();
  String getContentType();
  String getProtocol();
  }
