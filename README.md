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
In the above example, some typical Restful webservice calls for Person are implemented.

If by any chance form parameters are needed, just add a method parameter with the type Request. The Response object
can be accessed the same way.

```java
  // GET: /Example/say/hello/Hans?greeting=Welcome
  // res: Welcome dear Hans
  public String getSayHello(String name, Request request) {
    String greeting = request.getParameter("greeting");
    return greeting+" dear "+name;
    }
```

The overridable get() method is implemented using Request and Response. Also Java's varArgs construct is used to allow
arbitrary deep paths.
```java
  public void get(Request request, Response response, String... path) throws IOException {
    File file = file(request.getPath());
    if (file.isFile()) response.send(file);
    else response.status(404).send("Unknown resource: "+request.getPath());
    }
```

This code still needs refactoring and tests
