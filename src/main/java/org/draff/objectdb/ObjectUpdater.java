package org.draff.objectdb;

/**
 * Created by dave on 1/9/16.
 */
@FunctionalInterface
public interface ObjectUpdater<T> {
   void update(T object);
}

