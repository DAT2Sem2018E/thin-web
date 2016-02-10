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

public abstract class Server implements Runnable, Context {
  private volatile boolean running = false;
  private final Map<String, Parser> parsers = new HashMap<>();
  private final Map<String, HttpSession> sessions = new HashMap<>();
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

  public Server() throws IOException {
    this(4711);
    }
    
  @Override
  public Session provideSession(Request request, Response response) {
    String id = request.getSessionId();
    if (id != null && sessions.containsKey(id)) return sessions.get(id);
    HttpSession session = new HttpSession();
    id = ""+session.hashCode();
    sessions.put(id, session);
    response.setSessionId(id);
    return session;
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
  
  /**
   * Override this method to change which files are special
   * @param url of a file or resource
   * @return the relative path
   */
  public boolean isSpecial(String url) {
    return "/favicon.ico".equals(url);
    }
  
  @Override
  public String pathOf(String url) throws Response.BadRequestException {
    //TODO: create generic rule for special files
    if (isSpecial(url)) return url.substring(1);
    if (!url.startsWith(path())) throw new Response.BadRequestException();
    return url.substring(path().length());
    }
  
  public String path() {
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
  
  File file(String path) throws Response.BadRequestException {
    return new File(root, pathOf(path));
    }
  
  void report(HttpService service, String message) {
    System.err.print("\n$$ REPORTING '"+message+"'");
    }
  
  public void postCoffee() throws Response.ImATeapotException {
    if ("Teapot".equals(name)) throw new Response.ImATeapotException();
    }

  public void get(Request request, Response response, String... path) throws IOException, Response.NotFoundException, Response.BadRequestException {
    System.out.println("Cookie SID: "+request.getCookie("SID"));
    File file = file(request.getPath());
    if (file.isFile()) response.send(file);
    throw new Response.NotFoundException();
    }

  public void post(Request request, Response response, String... path) throws IOException, Response.NotFoundException, Response.BadRequestException {
    File file = file(request.getPath());
    if (file.isFile()) response.send(file);
    throw new Response.NotFoundException();
    }

  }
