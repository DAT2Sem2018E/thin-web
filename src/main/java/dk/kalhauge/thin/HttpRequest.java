package dk.kalhauge.thin;

import static dk.kalhauge.util.Strings.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class HttpRequest implements Request {
  private static final int NL = 10;
  private static final int CR = 13;
  private final String method;
  private final String path;
  private final String protocol;
  private int contentLength = 0;
  private final byte[] body;
  
  private final Map<String, String> parameters = new HashMap<>();
  private final Map<String, String> headers = new HashMap<>();
  
  private String readLine(InputStream in) throws IOException {
    StringBuilder builder = new StringBuilder();
    do {
      // all characters in the header is ASCII = single byte characters
      char c = (char)in.read();
      //System.out.print(c == 0 ? '#' : c);
      if (c == '\r') continue;
      if (c == '\n') break;
      builder.append(c);
      }
    while (true);
    System.out.print("\n<<"+builder);
    return builder.toString();
    }

  private byte[] read(InputStream in, int number) throws IOException {
    byte[] buffer = new byte[number];
    int count = in.read(buffer);
    System.out.print("\n  reading "+count+" raw bytes");
    return buffer;
    }
  
  HttpRequest(InputStream in) throws IOException {
    String[] parts = readLine(in).split(" ");
    
    method = parts[0].toLowerCase();
    path = left(parts[1], "?");
    if (parts[1].contains("?")) {
      String[] params = right(parts[1], "?").split("&");
      for (String param : params) {
        parameters.put(left(param, "="), right(param, "="));
        System.out.println("\n :"+left(param, "=")+"="+right(param, "="));
        }
      }    
    protocol = parts[2];
    
    do {
      String line = readLine(in).trim();
      if (line.isEmpty()) break;
      String[] pair = line.split(":");
      String key = pair[0].trim();
      String value = pair[1].trim();
      headers.put(key, value);
      if (key.equalsIgnoreCase("Content-Length")) contentLength = Integer.valueOf(value);
      }
    while(true);
    body = read(in, contentLength);
    }

  @Override
  public Map<String, String> getHeaders() {
    return headers;
    }

  @Override
  public Map<String, String> getParameters() {
    return parameters;
    }

  @Override
  public String getParameter(String key) {
    return parameters.get(key);
    }
  
  @Override
  public String getMethod() {
    return method;
    }

  @Override
  public String getPath() {
    return path;
    }

  @Override
  public byte[] getBody() {
    return body;
    }
  
  @Override
  public boolean hasBody() {
    return contentLength > 0;
    }

  @Override
  public int getContentLength() {
    return contentLength;
    }

  @Override
  public String getProtocol() {
    return protocol;
    }

  @Override
  public String getContentType() {
    String mime = headers.get("Content-Type");
    if (mime == null) mime = "text/plain";
    int pos = mime.indexOf(';');
    if (pos >= 0) mime = mime.substring(0, pos - 1);
    return mime;
    }
  
  }
