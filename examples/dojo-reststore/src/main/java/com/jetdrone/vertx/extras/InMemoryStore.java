package com.jetdrone.vertx.extras;

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import com.jetdrone.vertx.yoke.middleware.rest.Store;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

public class InMemoryStore implements Store {

  final Map<String, Map<String, JsonObject>> store = new HashMap<>();

  private Map<String, JsonObject> getCollection(final String entity) {
    Map<String, JsonObject> collection = store.get(entity);

    if (collection == null) {
      collection = new HashMap<>();
      store.put(entity, collection);
    }

    return collection;
  }

  public void bulkLoad(String entity, String idField, JsonArray source) {
    Map<String, JsonObject> collection = getCollection(entity);

    for (Object _state : source) {
      JsonObject state = (JsonObject) _state;
      collection.put(state.getValue(idField).toString(), state);
    }
  }

  private Comparator<JsonObject> buildComparator(final String field, final int asc) {
    return new Comparator<JsonObject>() {
      @Override
      public int compare(JsonObject o1, JsonObject o2) {
        final Object f1 = o1.getValue(field);
        final Object f2 = o2.getValue(field);

        if (f1 == null && f2 == null) {
          return 0;
        }

        if (f1 == null) {
          return -1 * asc;
        }

        if (f2 == null) {
          return asc;
        }

        // at this point both values are not null
        return f1.toString().compareTo(f2.toString()) * asc;
      }
    };
  }

  @Override
  public void create(String entity, JsonObject object, AsyncResultHandler<String> response) {
    final Map<String, JsonObject> collection = getCollection(entity);

    final String _id = Integer.toString(collection.size());
    object.put("id", _id);

    collection.put(_id, object);

    response.handle(new YokeAsyncResult<>(_id));
  }

  @Override
  public void read(String entity, String id, AsyncResultHandler<JsonObject> response) {
    final Map<String, JsonObject> collection = getCollection(entity);

    response.handle(new YokeAsyncResult<>(collection.get(id)));
  }

  @Override
  public void update(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
    final Map<String, JsonObject> collection = getCollection(entity);

    response.handle(new YokeAsyncResult<Number>(collection.put(id, object) == null ? 0 : 1));
  }

  @Override
  public void delete(String entity, String id, AsyncResultHandler<Number> response) {
    final Map<String, JsonObject> collection = getCollection(entity);

    response.handle(new YokeAsyncResult<Number>(collection.remove(id) == null ? 0 : 1));
  }

  @Override
  public void query(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
    final Map<String, JsonObject> collection = getCollection(entity);

    final List<JsonObject> values = new ArrayList<>(collection.values());

    if (sort.size() > 0) {
      for (String field : sort.fieldNames()) {
        Collections.sort(values, buildComparator(field, sort.getInteger(field)));
      }
    }

    List<JsonObject> filteredValues;

    if (query.size() > 0) {
      filteredValues = new ArrayList<>();

      for (JsonObject item : values) {
        for (String field : query.fieldNames()) {
          Object f = item.getValue(field);
          if (f != null && f.toString().equals(query.getString(field))) {
            filteredValues.add(item);
            break;
          }
        }
      }
    } else {
      filteredValues = values;
    }

    final JsonArray array = new JsonArray();
    int i = 0;

    for (JsonObject json : filteredValues) {
      if (start != null && end != null) {
        if (i >= start.intValue() && i < end.intValue()) {
          array.add(json);
        }
      } else {
        array.add(json);
      }
      i++;
    }

    response.handle(new YokeAsyncResult<>(array));
  }

  @Override
  public void count(String entity, JsonObject query, AsyncResultHandler<Number> response) {

    final Map<String, JsonObject> collection = getCollection(entity);
    final List<JsonObject> values = new ArrayList<>(collection.values());

    List<JsonObject> filteredValues;

    if (query.size() > 0) {
      filteredValues = new ArrayList<>();

      for (JsonObject item : values) {
        for (String field : query.fieldNames()) {
          Object f = item.getValue(field);
          if (f != null && f.toString().equals(query.getString(field))) {
            filteredValues.add(item);
            break;
          }
        }
      }
    } else {
      filteredValues = values;
    }

    response.handle(new YokeAsyncResult<Number>(filteredValues.size()));
  }
}
