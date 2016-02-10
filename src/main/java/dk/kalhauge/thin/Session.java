package dk.kalhauge.thin;

public interface Session {
  <T> T get(Class<T> type, String key);
  <T> Session set(String key, T value);
  }
