package dk.kalhauge.thin.exceptions;

public abstract class ClientErrorException extends Exception {

  public ClientErrorException(String message) {
    super(message);
    }
  
  public abstract int getStatus();
  
  }
