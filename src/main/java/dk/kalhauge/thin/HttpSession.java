package dk.kalhauge.thin;

import java.util.HashMap;
import java.util.Map;

class HttpSession implements Session {
  private final long timeStamp = System.currentTimeMillis();
  private final Map<String, Object> attributes = new HashMap<>();
  
  @Override
  public <T> T get(Class<T> type, String key) {
    return (T)attributes.get(key);
    }

  @Override
  public <T> Session set(String key, T value) {
    attributes.put(key, value);
    return this;
    }

  long getTimeStamp() {
    return timeStamp;
    }

  }
