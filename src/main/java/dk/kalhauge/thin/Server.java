package dk.kalhauge.thin;

import dk.kalhauge.thin.protocol.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Server implements Runnable {
  private boolean running = false;
  private final Map<String, Parser> parsers = new HashMap<>();
  private final int port;
  private File root;
  private String name;
  private final BufferedReader in;
  
  public Server(int port) throws IOException {
    this.port = port;
    in = new BufferedReader(new InputStreamReader(System.in));
    root = new File(getClass().getResource("/").getPath());
    name = getClass().getSimpleName();
    if (name.endsWith("Server")) name = name.substring(0, name.length() - 6);
    parser(new JsonParser());
    }
  
  protected final Parser parser(String mime) {
    if (mime == null || !parsers.containsKey(mime)) mime = Parser.JSON;
    return parsers.get(mime);
    }
  
  protected final Server parser(Parser value) {
    parsers.put(value.getMime(), value);
    return this;
    }
  
  protected Server name(String value) {
    name = value;
    return this;
    }
  
  String name() {
    return name;
    }
  
  String path() {
    if (name.isEmpty()) return "/";
    return "/"+name+"/";
    }
  
  protected Server root(File root) {
    this.root = root;
    return this;
    }
  
  protected Server root(String path) {
    root = new File(path);
    return this;
    }
  
  protected void command(String line) {
    if ("stop".equals(line)) stop();
    }
  
  public Server start() {
    new Thread(this).start();
    return this;
    }
  
  public void stop() {
    System.out.print("\n$$ server stopping...");
    running = false;
    }
  
  @Override
  public void run() {
    running = true;
    try (ServerSocket server = new ServerSocket(port)) {
      new Thread(new Runnable() {
          @Override
          public void run() { 
            try { while (running) if (in.ready()) command(in.readLine()); }
            catch (IOException ioe) { System.err.println(ioe.getMessage()); }
            }
          }).start();
      server.setSoTimeout(10000);
      System.out.print("\n$$ Name: "+name+"\n$$ Root: "+root.getAbsolutePath());
      System.out.print("\n$$ Waiting for requests on "+port+"...");
      int i = 0;
      char[] ws = new char[] { '-', '\\', '|', '/' };
      System.out.print("\n$$ .");
      while (running) {
        try {
          Socket socket = server.accept();
          HttpService service = new HttpService(this, socket);
          new Thread(service).start();
          }
        catch (SocketTimeoutException sto) {
          System.out.print("\b$$ "+ws[i]);
          i = (i + 1)%4;
          } 
        }
      }
    catch (IOException ioe) {
      Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ioe);
      }
    finally {
      System.out.println("\n$$ Server stopped");
      }
    }
  
  File file(String path) {
    if ("/favicon.ico".equals(path)) return new File(root, path);
    return new File(root, path.substring(name.length() + 1));
    }
  
  void report(HttpService service, String message) {
    System.err.println(">>> "+message);
    }
  
  public void postCoffee() throws Response.ImATeapotException {
    if ("Teapot".equals(name)) throw new Response.ImATeapotException();
    }

  public void get(Request request, Response response, String... path) throws IOException {
    File file = file(request.getPath());
    System.out.print("\nPath: "+request.getPath());
    System.out.print("\nFile: "+file.getAbsolutePath());
    if (file.isFile()) response.send(file);
    else response.status(404).send("Unknown resource: "+request.getPath());
    }

  public void post(Request request, Response response, String... path) throws IOException {
    File file = file(request.getPath());
    System.out.print("\nPath: "+request.getPath());
    System.out.print("\nFile: "+file.getAbsolutePath());
    if (file.isFile()) response.send(file);
    else response.status(404).send("Unknown resource: "+request.getPath());
    }

  }
