package dk.cph.nice;

import dk.kalhauge.thin.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NiceServer extends Server {
  private List<String> names = new ArrayList<String>() {{ add("kurt"); add("sonja"); }};
      

  public NiceServer(int port) throws IOException {
    super(port);
    }

  public static void main(String... args) throws IOException {
    new NiceServer(4711).start();
    }
  
  public Collection<String> getName() {
    return names;
    }
  
  public String getName(int index) {
    return names.get(index);
    }
  
  }
