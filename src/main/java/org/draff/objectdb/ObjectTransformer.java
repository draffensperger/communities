package org.draff.objectdb;

/**
 * Created by dave on 1/9/16.
 */
@FunctionalInterface
public interface ObjectTransformer<T> {
   T transform(T object);
}

