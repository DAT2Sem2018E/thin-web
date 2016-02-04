package dk.kalhauge.thin;

import java.util.Map;

public interface Request {
  Map<String, String> getHeaders();
  Map<String, String> getParameters();
  String getParameter(String key);
  String getMethod();
  String getPath();
  byte[] getBody();
  boolean hasBody();
  int getContentLength();
  String getContentType();
  String getProtocol();
  }
