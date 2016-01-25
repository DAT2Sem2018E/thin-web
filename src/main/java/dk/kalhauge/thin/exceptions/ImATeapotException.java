package dk.kalhauge.thin.exceptions;

public class ImATeapotException extends ClientErrorException {

  public ImATeapotException(String message) {
    super(message);
    }

  @Override
  public int getStatus() { return 418; }

  }
