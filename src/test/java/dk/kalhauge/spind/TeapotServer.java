package dk.kalhauge.spind;

import dk.kalhauge.thin.Server;
import java.io.IOException;

public class TeapotServer extends Server {

  public TeapotServer(int port) throws IOException {
    super(port);
    }

  public static void main(String[] args) throws IOException { 
    new TeapotServer(9090).start(); 
    }
  
  }
