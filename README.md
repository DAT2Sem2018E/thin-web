Thin Web Server
===============

The thin web server is meant primarily as an example of how Reflection can be
used in Java. It is also an attempt to show that Java frameworks don't have to
drown in annotations and boiler plate code.

``` java
import dk.kalhauge.this.Server;

public class ExampleServer extends Server {
  
  public static void main(String[] args) {
    new ExampleServer(4711).start();
    }
  
  }
```

