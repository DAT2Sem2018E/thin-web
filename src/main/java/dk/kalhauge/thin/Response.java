package dk.kalhauge.thin;

import java.io.File;
import java.io.IOException;

public interface Response {
  Response status(int value);
  Response type(String value);
  void setSessionId(String value);
  void send(byte[] body) throws IOException;
  void send() throws IOException;
  void send(String message) throws IOException;
  void send(File file) throws IOException;
  void send(HttpException e) throws IOException;
  
  public abstract class HttpException extends Exception {
    protected HttpException(String message) { super(message); }
    public abstract int getStatus();
    }
  
  public abstract class ClientException extends HttpException {
    protected ClientException(String message) { super(message); }
    }
  
  public class BadRequestException extends ClientException {
    public BadRequestException() { super("Bad Request"); }
    @Override public int getStatus() { return 400; }
    }

  public class NotFoundException extends ClientException {
    public NotFoundException() { super("Not Found"); }
    @Override public int getStatus() { return 404; }
    }

  public class ImATeapotException extends ClientException {
    public ImATeapotException() { super("I'm a teapot"); }
    @Override public int getStatus() { return 418; }
    }
  
  public abstract class ServerException extends HttpException {
    protected ServerException(String message) { super(message); }
    }
  
  public class InternalServerException extends ServerException {
    public InternalServerException() { super("Internal Server Error"); }
    @Override public int getStatus() { return 500; }
    }
  
  public class NotImplementedException extends ServerException {
    public NotImplementedException() { super("Not Implemented"); }
    @Override public int getStatus() { return 501; }
    }

  }
