package dk.kalhauge.spind;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Person {
  private static final Map<Integer, Person> items = new HashMap<>();
  private static int nextId = 1;
  
  public static Collection<Person> list() {
    return items.values();
    }
  
  public static Person find(int id) {
    return items.get(id);
    }

  public static Person save(Person person) {
    if (person.id == 0 || person.id >= nextId) return new Person(person.name, person.age);
    items.put(person.id, person);
    return person;
    }
  
  public static Person remove(int id) {
    return items.remove(id);
    }
  
  public static int size() { return items.size(); }
  
  private final int id;
  private String name;
  private int age;

  public Person(String name, int age) {
    this.id = nextId++;
    this.name = name;
    this.age = age;
    items.put(id, this);
    }

  public int getId() {
    return id;
    }
  
  public String getName() {
    return name;
    }

  public void setName(String value) {
    name = value;
    }

  public int getAge() {
    return age;
    }

  public void setAge(int value) {
    age = value;
    }
  
  }
