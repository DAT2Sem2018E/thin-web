package dk.kalhauge.thin;

public interface Parser {
  final String JSON = "application/json";
  String getMime();
  <T> T fromText(Class<T> type, String text);
  <T> T fromTexts(Class<T> type, Iterable<String> texts);
  <T> String toText(Class<T> type, Object object);
  }
