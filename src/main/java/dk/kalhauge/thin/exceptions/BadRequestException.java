package dk.kalhauge.thin.exceptions;

public class BadRequestException extends ClientErrorException {

  public BadRequestException(String message) {
    super(message);
    }

  @Override
  public int getStatus() { return 400; }

  }
