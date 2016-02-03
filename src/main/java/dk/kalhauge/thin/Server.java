package dk.kalhauge.thin;

import dk.kalhauge.thin.exceptions.ImATeapotException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Server implements Runnable {
  private boolean running = false;
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
    }
  
  protected Server name(String value) {
    name = value;
    return this;
    }
  
  String name() {
    return name;
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
    running = false;
    }
  
  @Override
  public void run() {
    running = true;
    try (ServerSocket server = new ServerSocket(port)) {
      server.setSoTimeout(10000);
      System.out.print("\nName: "+name+"\nRoot: "+root.getAbsolutePath());
      System.out.print("\nWaiting for requests on "+port+"...");
      while (running) {
        try {
          Socket socket = server.accept();
          HttpService service = new HttpService(this, socket);
          new Thread(service).start();
          }
        catch (SocketTimeoutException sto) {
          while (in.ready()) {
            command(in.readLine());
            } 
          System.out.print(".");
          } 
        }
      }
    catch (IOException ioe) {
      Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ioe);
      }
    finally {
      System.out.println("\nStopped");
      }
    }
  
  File file(String path) {
    return new File(root, path.substring(name.length() + 1));
    }
  
  void report(HttpService service, String message) {
    System.err.println(">>> "+message);
    }
  
  public void postCoffee() throws ImATeapotException {
    if ("Teapot".equals(name)) throw new ImATeapotException("This server will not brew coffee");
    }

  public void get(Request request, Response response, String... path) throws IOException {
    File file = file(request.getPath());
    System.out.print("\nPath: "+request.getPath());
    System.out.print("\nFile: "+file.getAbsolutePath());
    if (file.isFile()) response.send(file);
    else response.status(404).send("Unknown resource: "+request.getPath());
    }

  }
