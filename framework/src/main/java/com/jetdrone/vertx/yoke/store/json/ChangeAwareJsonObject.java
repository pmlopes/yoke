package com.jetdrone.vertx.yoke.store.json;

import java.util.Map;
import java.util.Set;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonElement;
import io.vertx.core.json.JsonObject;

public class ChangeAwareJsonObject extends ChangeAwareJsonElement {

	private final JsonObject inner;

	protected ChangeAwareJsonObject(JsonObject jsonObject, ChangeAware notifier) {
	    super(notifier);
	    inner = jsonObject;
    }

	public ChangeAwareJsonObject(JsonObject jsonObject) {
		this(jsonObject, false);
	}

	public ChangeAwareJsonObject(JsonObject jsonObject, boolean initialChanged) {
		super(null);
		inner = jsonObject;
		changed = initialChanged;
	}

	public ChangeAwareJsonObject putString(String fieldName, String value) {
		setChanged();
	    inner.putString(fieldName, value);
	    return this;
    }

	public ChangeAwareJsonObject putObject(String fieldName, JsonObject value) {
		setChanged();
	    inner.putObject(fieldName, value);
	    return this;
    }

	public ChangeAwareJsonObject putArray(String fieldName, JsonArray value) {
		setChanged();
	    inner.putArray(fieldName, value);
	    return this;
    }

	public ChangeAwareJsonObject putElement(String fieldName, JsonElement value) {
		setChanged();
	    inner.putElement(fieldName, value);
	    return this;
    }

	public ChangeAwareJsonObject putNumber(String fieldName, Number value) {
		setChanged();
	    inner.putNumber(fieldName, value);
	    return this;
    }

	public ChangeAwareJsonObject putBoolean(String fieldName, Boolean value) {
		setChanged();
	    inner.putBoolean(fieldName, value);
	    return this;
    }

	public ChangeAwareJsonObject putBinary(String fieldName, byte[] binary) {
		setChanged();
	    inner.putBinary(fieldName, binary);
	    return this;
    }

	public ChangeAwareJsonObject putValue(String fieldName, Object value) {
		setChanged();
	    inner.putValue(fieldName, value);
	    return this;
    }

	public String getString(String fieldName) {
	    return inner.getString(fieldName);
    }

	public ChangeAwareJsonObject getObject(String fieldName) {
	    return convertJsonObject(inner.getObject(fieldName));
    }

	public ChangeAwareJsonArray getArray(String fieldName) {
	    return convertJsonArray(inner.getArray(fieldName));
    }

	public ChangeAwareJsonElement getElement(String fieldName) {
	    JsonElement value = inner.getElement(fieldName);
	    if (value.isArray()) {
	    	return convertJsonArray(value.asArray());
	    } else {
	    	return convertJsonObject(value.asObject());
	    }
    }

	public Number getNumber(String fieldName) {
	    return inner.getNumber(fieldName);
    }

	public Long getLong(String fieldName) {
	    return inner.getLong(fieldName);
    }

	public Integer getInteger(String fieldName) {
	    return inner.getInteger(fieldName);
    }

	public Boolean getBoolean(String fieldName) {
	    return inner.getBoolean(fieldName);
    }

	public byte[] getBinary(String fieldName) {
	    return inner.getBinary(fieldName);
    }

	public String getString(String fieldName, String def) {
	    return inner.getString(fieldName, def);
    }

	public ChangeAwareJsonObject getObject(String fieldName, JsonObject def) {
	    return convertJsonObject(inner.getObject(fieldName, def));
    }

	public ChangeAwareJsonArray getArray(String fieldName, JsonArray def) {
	    return convertJsonArray(inner.getArray(fieldName, def));
    }

	public ChangeAwareJsonElement getElement(String fieldName, JsonElement def) {
	    JsonElement value = inner.getElement(fieldName, def);
	    if (value.isArray()) {
	    	return convertJsonArray(value.asArray());
	    } else {
	    	return convertJsonObject(value.asObject());
	    }
    }

	public boolean getBoolean(String fieldName, boolean def) {
	    return inner.getBoolean(fieldName, def);
    }

	public Number getNumber(String fieldName, int def) {
	    return inner.getNumber(fieldName, def);
    }

	public Long getLong(String fieldName, long def) {
	    return inner.getLong(fieldName, def);
    }

	public Integer getInteger(String fieldName, int def) {
	    return inner.getInteger(fieldName, def);
    }

	public byte[] getBinary(String fieldName, byte[] def) {
	    return inner.getBinary(fieldName, def);
    }

	public Set<String> getFieldNames() {
	    return inner.getFieldNames();
    }

	public <T> T getValue(String fieldName) {
	    return getField(fieldName);
    }

	@SuppressWarnings("unchecked")
    public <T> T getField(String fieldName) {
	    Object value = inner.getField(fieldName);
	    if (value instanceof JsonArray) {
	    	value = convertJsonArray((JsonArray) value);
	    } else if (value instanceof JsonObject) {
	    	value = convertJsonObject((JsonObject) value);
	    }
	    return (T) value;
    }

	public Object removeField(String fieldName) {
		setChanged();
	    return inner.removeField(fieldName);
    }

	public boolean containsField(String fieldName) {
	    return inner.containsField(fieldName);
    }

	public int size() {
	    return inner.size();
    }

	public ChangeAwareJsonObject mergeIn(JsonObject other) {
		setChanged();
	    inner.mergeIn(other);
	    return this;
    }

	public String encode() {
	    return inner.encode();
    }

	public String encodePrettily() {
	    return inner.encodePrettily();
    }

	public ChangeAwareJsonObject copy() {
	    return new ChangeAwareJsonObject(inner.copy());
    }

	public String toString() {
	    return inner.toString();
    }

	public boolean equals(Object o) {
	    return inner.equals(o);
    }

	public Map<String, Object> toMap() {
	    return inner.toMap();
    }

	public JsonObject jsonObject() {
		return inner;
	}

	private ChangeAwareJsonArray convertJsonArray(JsonArray value) {
		if (value == null) return null;
    	return new ChangeAwareJsonArray(value, notifier != null ? notifier : this);
	}
	
	private ChangeAwareJsonObject convertJsonObject(JsonObject value) {
		if (value == null) return null;
    	return new ChangeAwareJsonObject(value, notifier != null ? notifier : this);
	}
}
