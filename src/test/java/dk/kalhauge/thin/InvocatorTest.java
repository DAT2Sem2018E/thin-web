package dk.kalhauge.thin;

import dk.kalhauge.util.Path;
import dk.kalhauge.util.LinkedPath;
import java.lang.reflect.Method;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;

public class InvocatorTest {
  class CUT {
    public void single() { }
    public void singleone(String firts) { }
    public void thisIsAnotherMethod() { }
    public void withOneArgument(String first) { }
    public void withOne(String first, String second) { }
    }
  Context hidingContext = new Context() {
      @Override
      public boolean isHidden(Method method) { return true; }
      @Override
      public String pathOf(String url) {
        throw new UnsupportedOperationException("No support for #path");
        }
      };
  Context showingContext = new Context() {
      @Override
      public boolean isHidden(Method method) { return false; }
      @Override
      public String pathOf(String url) {
        throw new UnsupportedOperationException("No support for #path");
        }
      };
  
  @Test
  public void testSingleNameNoArgumentsNotHidden() throws NoSuchMethodException {
    Method method = CUT.class.getMethod("single");
    Invocator invocator = new Invocator(showingContext, "single", Path.EMPTY).matched(method);
    assertThat(invocator.getName(), is("single"));
    }

  @Test
  public void testSingleNameOneArgumentNotHidden() throws NoSuchMethodException {
    Method method = CUT.class.getMethod("singleone", String.class);
    Invocator invocator = new Invocator(showingContext, "singleone", Path.EMPTY).matched(method);
    assertNull(invocator);
    invocator = new Invocator(showingContext, "singleone", LinkedPath.create("Kurt"));
    assertThat(invocator.getName(), is("singleone"));
    }

  @Test
  public void testComplexNameNoArgumentsNotHidden() throws NoSuchMethodException {
    Method method = CUT.class.getMethod("thisIsAnotherMethod");
    Invocator invocator = new Invocator(
        showingContext, "this", LinkedPath.create("is", "another", "method")
        ).matched(method);
    assertThat(invocator.getName(), is("thisIsAnotherMethod"));
    }

  }
