/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

public class GBasicAuth extends BasicAuth {		
		public GBasicAuth(final Closure handler) {				
				super(new AuthHandler() {
					@Override
					public void handle(String username, String password, Handler<JsonObject> result) {
                        // TODO: Groovy works with Map not JsonObject
						handler.call(username, password, result);
					}
				});
    }
		
		public GBasicAuth(String realm, final Closure handler) {				
				super(realm, new AuthHandler() {
					@Override
					public void handle(String username, String password, Handler<JsonObject> result) {
                        // TODO: Groovy works with Map not JsonObject
						handler.call(username, password, result);
					}
				});
    }
}
