/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package com.jetdrone.vertx.yoke.sockjs;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Represents an event that occurs on the event bus bridge.
 * <p>
 * Please consult the documentation for a full explanation.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface BridgeEvent extends Future<Boolean> {

  /**
   * @return  the type of the event
   */
  BridgeEventType type();

  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved.
   *
   * @return the raw JSON message for the event
   */
  JsonObject rawMessage();

  /**
   * Get the SockJSSocket instance corresponding to the event
   *
   * @return  the SockJSSocket instance
   */
  SockJSSocket socket();

}
