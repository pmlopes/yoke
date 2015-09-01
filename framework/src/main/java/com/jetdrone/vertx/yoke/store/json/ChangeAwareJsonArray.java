package com.jetdrone.vertx.yoke.store.json;

import java.util.Iterator;
import java.util.List;

import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonElement;
import io.vertx.core.json.JsonObject;

public class ChangeAwareJsonArray extends ChangeAwareJsonElement {

	private final JsonArray inner;

	protected ChangeAwareJsonArray(JsonArray jsonArray, ChangeAware notifier) {
	    super(notifier);
	    inner = jsonArray;
    }

	public ChangeAwareJsonArray(JsonArray jsonArray) {
		this(jsonArray, false);
	}

	public ChangeAwareJsonArray(JsonArray jsonArray, boolean initialChanged) {
		super(null);
		inner = jsonArray;
		changed = initialChanged;
	}

	public ChangeAwareJsonArray addString(String str) {
		setChanged();
	    inner.addString(str);
	    return this;
    }

	public ChangeAwareJsonArray addObject(JsonObject value) {
		setChanged();
	    inner.addObject(value);
	    return this;
    }

	public ChangeAwareJsonArray addArray(JsonArray value) {
		setChanged();
	    inner.addArray(value);
	    return this;
    }

	public ChangeAwareJsonArray addElement(JsonElement value) {
		setChanged();
	    inner.addElement(value);
	    return this;
    }

	public ChangeAwareJsonArray addNumber(Number value) {
		setChanged();
	    inner.addNumber(value);
	    return this;
    }

	public ChangeAwareJsonArray addBoolean(Boolean value) {
		setChanged();
	    inner.addBoolean(value);
	    return this;
    }

	public ChangeAwareJsonArray addBinary(byte[] value) {
		setChanged();
	    inner.addBinary(value);
	    return this;
    }

	public ChangeAwareJsonArray add(Object value) {
		setChanged();
	    inner.add(value);
	    return this;
    }

	public int size() {
	    return inner.size();
    }

	@SuppressWarnings("unchecked")
    public <T> T get(int index) {
	    Object value = inner.get(index);
	    if (value != null) {
		    if (value instanceof ChangeAwareJsonArray) {
		    	value = new ChangeAwareJsonArray((JsonArray) value, notifier != null ? notifier : this);
		    } else if (value instanceof JsonObject) {
		    	value = new ChangeAwareJsonObject((JsonObject) value, notifier != null ? notifier : this);
		    }
	    }
	    return (T) value;
    }

	public Iterator<Object> iterator() {
		final Iterator<Object> iter = inner.iterator();
		return new Iterator<Object>() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			
			@Override
			public Object next() {
				return iter.next();
			}
			
			@Override
			public void remove() {
				ChangeAwareJsonArray.this.setChanged();
				iter.remove();
			}
		};
    }

	public boolean contains(Object value) {
	    return inner.contains(value);
    }

	public String encode() throws EncodeException {
	    return inner.encode();
    }

	public String encodePrettily() throws EncodeException {
	    return inner.encodePrettily();
    }

	public ChangeAwareJsonArray copy() {
	    return new ChangeAwareJsonArray(inner.copy());
    }

	public String toString() {
	    return inner.toString();
    }

	public boolean equals(Object o) {
	    return inner.equals(o);
    }

	public Object[] toArray() {
	    return inner.toArray();
    }

	@SuppressWarnings("rawtypes")
    public List toList() {
	    return inner.toList();
    }

	public JsonArray unsafeJsonArray() {
		return inner;
	}
}
