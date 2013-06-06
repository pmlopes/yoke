package com.jetdrone.vertx.yoke.engine;

import java.util.Map;

public interface Function {

    String exec(Map<String, Object> context, Object... args);
}
