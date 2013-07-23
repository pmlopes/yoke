/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.middleware;

import groovy.lang.Closure;
import org.vertx.java.core.Handler;

public class GBasicAuth extends BasicAuth {		
		public GBasicAuth(final Closure handler) {				
				super(new BasicAuth.AuthHandler() {
					@Override
					public void handle(String username, String password, Handler<Boolean> result) {
						handler.call(username, password, result);
					}
				}, "Authentication required");
    }
		
		public GBasicAuth(String realm, final Closure handler) {				
				super(new BasicAuth.AuthHandler() {
					@Override
					public void handle(String username, String password, Handler<Boolean> result) {
						handler.call(username, password, result);
					}
				}, realm);
    }
}
