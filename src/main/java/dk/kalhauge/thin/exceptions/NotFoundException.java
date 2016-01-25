package dk.kalhauge.thin.exceptions;

public class NotFoundException extends ClientErrorException {

  public NotFoundException(String message) {
    super(message);
    }

  @Override
  public int getStatus() { return 404; }

  }
