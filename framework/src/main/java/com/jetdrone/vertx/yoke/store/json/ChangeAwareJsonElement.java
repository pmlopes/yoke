package com.jetdrone.vertx.yoke.store.json;


public abstract class ChangeAwareJsonElement implements ChangeAware {

	protected final ChangeAware notifier;
	protected boolean changed = false;

	protected ChangeAwareJsonElement(ChangeAware notifier) {
		this.notifier = notifier;
	}

	@Override
	public void notifyChanged(ChangeAwareJsonElement jsonElement) {
		changed = true;
		// bubble up
		if (notifier != null) notifier.notifyChanged(jsonElement);
	}

	public boolean isChanged() {
		return changed;
	}

	protected void setChanged() {
		changed = true;
		if (notifier != null) notifier.notifyChanged(this);
	}

	public boolean isArray() {
		return this instanceof ChangeAwareJsonArray;
	}

	public boolean isObject() {
		return this instanceof ChangeAwareJsonObject;
	}

	public ChangeAwareJsonArray asArray() {
		return (ChangeAwareJsonArray) this;
	}

	public ChangeAwareJsonObject asObject() {
		return (ChangeAwareJsonObject) this;
	}
}
