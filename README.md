Thin Web Server
===============

The thin web server is meant primarily as an example of how Reflection can be
used in Java. It is also an attempt to show that Java frameworks don't have to
drown in annotations and boiler plate code.

``` java
import dk.kalhauge.this.Server;

public class ExampleServer extends Server {
  
  public static void main(String[] args) {
    new ExampleServer(4711).root("/PathToStaticFiles").start();
    }
  
  }
```
will start a this webserver that will serve the static files beneath the path to static files.
```java
public class ExampleServer extends Server {
  
  public static void main(String[] args) { ... }

  // GET: /Example/person
  // res: [{ "id" : 1, "name" : "Ib", "age" : 17 },{ "id" : 2, "name" : "Sonja", "age" : 42 }]
  public Collection<Person> getPerson() {
    return db.listPersons();
    }
  
  // GET: /Example/person/2
  // res: { "id" : 2, "name" : "Sonja", "age" : 42 }
  public Person getPerson(int id) {
    return db.findPerson(id);
    }

  // POST: /Example/person
  // req: { "name" : "Kurt", "age" : 34 }
  // res: { "id" : 3, "name" : "Kurt", "age" : 34 }
  public Person postPerson(Person person) {
    return db.savePerson(person);
    }
    
  // DELETE: /Example/person/2
  // res: { "id" : 2, "name" : "Sonja", "age" : 42 }
  public Person deletePerson(int id) {
    return db.removePerson(2);
    }
  
  // GET: /Example/person/count
  // res: 2
  public int getPersonCount() {
    return db.listPersons().size();
    }  
    
  }
```
In the above example is some typical Restful webservice calls for Person implemented.
