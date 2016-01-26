package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Value;

import java.util.Date;

import static com.google.api.services.datastore.client.DatastoreHelper.makeValue;

/**
 * Created by dave on 1/25/16.
 */
public class ValueMapper {
  public static Value toValue(Object object) {
    return toValueBuilder(object).build();
  }

  public static Object fromValue(Value value) {
    if (value == null) {
      return null;
    } else if (value.hasIntegerValue()) {
      return value.getIntegerValue();
    } else if (value.hasStringValue()) {
      return value.getStringValue();
    } else if (value.hasDoubleValue()) {
      return value.getDoubleValue();
    } else if (value.hasBooleanValue()) {
      return value.getBooleanValue();
    } else if (value.hasTimestampMicrosecondsValue()) {
      return new Date(value.getTimestampMicrosecondsValue() / 1000L);
    } else {
      throw new IllegalArgumentException(
          "Not configured to convert Datastore value " + value);
    }
  }

  private static Value.Builder toValueBuilder(Object object) {
    if (object instanceof Long) {
      return makeValue((Long)object);
    } else if (object instanceof String) {
      return makeValue((String)object);
    } else if (object instanceof Double) {
      return makeValue((Double)object);
    } else if (object instanceof Boolean) {
      return makeValue((Boolean)object);
    } else if (object instanceof Date) {
      return makeValue((Date)object);
    } else {
      throw new IllegalArgumentException(
          "Can't make Datastore value for " + object.getClass() + ": " + object);
    }
  }
}
