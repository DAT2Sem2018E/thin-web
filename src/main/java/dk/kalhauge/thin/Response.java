package dk.kalhauge.thin;

import java.io.File;
import java.io.IOException;

public interface Response {
  Response status(int value);
  Response type(String value);
  void send(byte[] body) throws IOException;
  void send() throws IOException;
  void send(String message) throws IOException;
  void send(File file) throws IOException;
  }
