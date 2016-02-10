package dk.kalhauge.thin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

class HttpResponse implements Response {
  private static final Map<Integer, String> messages = new HashMap<Integer, String>() {{
    put(200, "OK");
    put(201, "Created");
    put(204, "No Content");
    put(400, "Bad Request");
    put(404, "Not Found");
    put(418, "I'm a teapot");
    put(500, "Internal Server Error");
    put(501, "Not Implemented");
    }};
  private static final Map<String, String> mimes = new HashMap<String, String>() {{
    put("html", "text/html; charset=utf-8");
    put("htm", "text/html; charset=utf-8");
    put("txt", "text/plain; charset=utf-8");
    put("gif", "image/gif");
    put("jpeg", "image/jpeg");
    put("png", "image/png");
    put("bmp", "image/bmp");
    put("pdf", "application/pdf");
    put("json", "application/json");
    put("ico", "image/x-icon");
    }};
  private static final int NL = 10;
  private static final int CR = 13;
  private final OutputStream out;
  private int status = 200;
  private String type = "json";
  private boolean open = true;
  private String sessionId = null;

  public HttpResponse(OutputStream out) {
    this.out = out;
    }
  
  private static void writeLine(OutputStreamWriter writer, String line) throws IOException {
    System.out.print("\n>>"+line);
    writer.write(line);
    writer.write(CR);
    writer.write(NL);
    }
  
  private void writeHeaders(long contentLenght) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8");
    writeLine(writer, "HTTP/1.1 "+status+" "+messages.get(status));
    writeLine(writer, "Content-Type: "+mimes.get(type));
    writeLine(writer, "Content-Length: "+contentLenght);
    if (sessionId != null) writeLine(writer, "Set-Cookie: SID="+sessionId);
    writeLine(writer, "");
    writer.flush();
    }
  
  @Override
  public Response status(int value) {
    status = value;
    if (status >= 300) type = "txt";
    return this;
    }

  @Override
  public Response type(String value) {
    type = value.toLowerCase();
    return this;
    }

  @Override
  public void setSessionId(String value) { sessionId = value; }
  
  @Override
  public void send(byte[] body) throws IOException {
    if (open) open = false;
    else return;
    try {
      writeHeaders(body.length);
      out.write(body);
      }
    finally { out.close(); }
    }

  @Override
  public void send() throws IOException {
    if (open) open = false;
    else return;
    try {
      writeHeaders(0);
      }
    finally { out.close(); }
    }

  @Override
  public void send(String message) throws IOException {
    send(message.getBytes("utf-8"));
    }
  
  @Override
  public void send(File file) throws IOException {
    if (open) open = false;
    else return;
    int dotPos = file.getName().lastIndexOf(".");
    type = dotPos < 0 ? "txt" : file.getName().substring(dotPos + 1);
    try {
      writeHeaders(file.length());
      try (FileInputStream in = new FileInputStream(file)) {
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) > 0) out.write(buffer, 0, count);
        }
      }
    finally { out.close(); }
    }

  @Override
  public void send(HttpException he) throws IOException {
    //TODO: use the exception message
    status(he.getStatus());
    send();
    // send(he.getMessage());
    }
    
  }
