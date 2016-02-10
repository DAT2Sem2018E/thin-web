package dk.kalhauge.spind;

import dk.kalhauge.thin.Request;
import dk.kalhauge.thin.Server;
import dk.kalhauge.thin.Session;
import java.io.IOException;
import java.util.Collection;

public class ExampleServer extends Server {

  public ExampleServer(int port) throws IOException {
    super(port);
    new Person("Kurt", 34);
    new Person("Sonja", 25);
    new Person("Ib", 67);
    }
  
  public static void main(String... args) throws IOException {
    Server server = new ExampleServer(4712).root("/Users/AKA/Sites").start();
    }

  @Override
  public void command(String line) {
    super.command(line);
    System.out.println("\n## "+line);
    }
    
  public Collection<Person> getPerson(Session session) {
    System.out.println("\n-- getPerson() called");
    session.set("message", "hello world");
    return Person.list();
    }
  
  public int getPersonCount(Session session) { 
    System.out.println("\n-- Message from session: "+session.get(String.class, "message"));
    return Person.size();
    }

  public Person getPerson(int id) {
    System.out.println("\n-- getPerson("+id+") called");
    return Person.find(id);
    }
  
  public Person postPerson(Person person) {
    System.out.println("\n-- postPerson(...) called");
    return Person.save(person);
    }
  
  public Person deletePerson(int id) {
    System.out.println("\n-- deletePerson("+id+") called");
    return Person.remove(id);
    }
  
  public String getHelloMessage(String greeting, Request request) {
    System.out.println("\n-- "+request.getCookie("SID"));
    return greeting+" "+request.getParameter("quote");
    }
  
  public void putInfo(String message) {
    System.out.print("\n-- putInfo('"+message+"') called");
    }
  
  }
