package dk.kalhauge.thin.protocol;

import com.google.gson.Gson;
import dk.kalhauge.thin.Parser;
import static dk.kalhauge.util.Strings.*;

public class JsonParser implements Parser {
  private final Gson gson = new Gson();
  
  private static String stringify(Class type, String text) {
    if (type != String.class || text.charAt(0) == '"') return text;
    return "\""+text+"\"";
    }
  
  @Override
  public <T> T fromText(Class<T> type, String text) {
    text = stringify(type, text);
    return gson.fromJson(text, type);
    }

  @Override
  public <T> String toText(Class<T> type, Object object) {
    return gson.toJson(object, type);
    }

  @Override
  public String getMime() {
    return JSON;
    }

  @Override
  public String toString() {
    return "JSON protocol parser";
    }

  @Override
  public <T> T fromTexts(Class<T> type, Iterable<String> texts) {
    // TODO: check for single elements lists
    String text = "["+join(",", t -> stringify(type, t), texts)+"]";
    return fromText(type, text);
    }

  }
