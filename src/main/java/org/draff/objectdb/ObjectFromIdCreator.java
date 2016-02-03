package org.draff.objectdb;

/**
 * Created by dave on 1/25/16.
 */
@FunctionalInterface
public interface ObjectFromIdCreator<T extends Model> {
  T generateFromId(Object nameOrId);
}
