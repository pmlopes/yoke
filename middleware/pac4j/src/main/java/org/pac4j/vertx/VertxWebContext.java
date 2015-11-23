/*
  Copyright 2014 - 2014 Michael Remond

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.vertx;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import org.pac4j.core.context.WebContext;
import io.vertx.core.json.JsonObject;

/**
 * WebContext implementation for Vert.x.
 *
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class VertxWebContext implements WebContext {

    private final YokeRequest request;
    private final YokeResponse response;

    public VertxWebContext(YokeRequest request) {
        this.request = request;
        this.response = request.response();
    }

    @Override
    public String getRequestParameter(String name) {
        String param = request.params().get(name);
        if (param == null & request.params().get("pac4jFormAttributes") != null) {
            param = request.formAttributes().get(name);
            if (param != null) {
                // FIX for Vert.x
                param = param.replaceAll("\\s", "+");
            }
        }
        return param;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        final Map<String, String[]> map = new HashMap<>();
        if (request.params().get("pac4jFormAttributes") != null) {
            for (String name : request.formAttributes().names()) {
                map.put(name, request.formAttributes().getAll(name).toArray(new String[0]));
            }
        }
        for (String name : request.params().names()) {
            map.put(name, request.params().getAll(name).toArray(new String[0]));
        }
        return map;
    }

    @Override
    public String getRequestHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public void setSessionAttribute(String name, Object value) {
        JsonObject session = request.get("session");
        session.put(name, value);
    }

    @Override
    public Object getSessionAttribute(String name) {
        JsonObject session = request.get("session");
        return session.getValue(name);
    }

    @Override
    public String getRequestMethod() {
        return request.method().name();
    }

    @Override
    public void writeResponseContent(String content) {
        response.end(content);
    }

    @Override
    public void setResponseStatus(int code) {
        response.setStatusCode(code);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        response.headers().set(name, value);
    }

    @Override
    public String getServerName() {
        return getRequestHeader("Host").split(":")[0];
    }

    @Override
    public int getServerPort() {
        String[] tab = getRequestHeader("Host").split(":");
        if (tab.length > 1) {
            return Integer.parseInt(tab[1]);
        }
        if ("http".equals(getScheme())) {
            return 80;
        } else {
            return 443;
        }
    }

    @Override
    public String getScheme() {
        try {
            return new URL(request.absoluteURI()).getProtocol();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFullRequestURL() {
        return getScheme() + "://" + getRequestHeader("Host") + request.uri();
    }

}