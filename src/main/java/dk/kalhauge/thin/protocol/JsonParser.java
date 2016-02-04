package dk.kalhauge.thin.protocol;

import com.google.gson.Gson;
import dk.kalhauge.thin.Parser;

public class JsonParser implements Parser {
  private final Gson gson = new Gson();
  
  @Override
  public <T> T fromText(String text, Class<T> type) {
    if (type == String.class && text.charAt(0) != '"') text = "\""+text+"\"";
    return gson.fromJson(text, type);
    }

  @Override
  public <T> String toText(Object object, Class<T> type) {
    return gson.toJson(object, type);
    }

  @Override
  public String getMime() {
    return "application/json";
    }

  @Override
  public String toString() {
    return "JSON protocol parser";
    }

  
  
  }
