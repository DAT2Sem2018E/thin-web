package dk.kalhauge.thin;

public interface Parser {
  final String JSON = "application/json";
  String getMime();
  <T> T fromText(String text, Class<T> type);
  <T> String toText(Object object, Class<T> type);
  }
