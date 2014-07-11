package com.jetdrone.vertx.yoke.store.json;

public interface ChangeAware {

	public void notifyChanged(ChangeAwareJsonElement jsonElement);

}
