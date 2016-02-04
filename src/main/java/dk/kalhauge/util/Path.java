package dk.kalhauge.util;

import static java.util.Collections.emptyIterator;
import java.util.Iterator;

public interface Path<T> extends Iterable<T> {
  static final Path EMPTY = new Path(){};

  default T getFirst() {
    throw new UnsupportedOperationException("Empty path has no first");
    }

  default Path<T> getRest() {
    throw new UnsupportedOperationException("Empty path has no rest");
    }

  default boolean isEmpty() { return true; }
  
  default int size() { return 0; }

  @Override
  default Iterator<T> iterator() { return emptyIterator(); }
  
  }

