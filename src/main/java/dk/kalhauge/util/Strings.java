package dk.kalhauge.util;

public class Strings {
  
  public static String pascal(String word) {
    return word.substring(0, 1).toUpperCase()+word.substring(1);
    }
  
  public static String camel(String... words) {
    String result = null;
    for (String word : words) {
      if (result == null) result = word;
      else result += pascal(word);
      }
    return result;
    }
  
  public static String left(String word, String delimiter) {
    int pos = word.indexOf(delimiter);
    if (pos < 0) pos = word.length();
    return word.substring(0, pos);
    }
  
  public static String right(String word, String delimiter) {
    int pos = word.indexOf(delimiter);
    if (pos < 0) return "";
    return word.substring(pos + 1);
    } 
    
  }
