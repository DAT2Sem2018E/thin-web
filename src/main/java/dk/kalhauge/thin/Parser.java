package dk.kalhauge.thin;

public interface Parser {
  String getMime();
  <T> T fromText(String text, Class<T> type);
  <T> String toText(Object object, Class<T> type);
  }
