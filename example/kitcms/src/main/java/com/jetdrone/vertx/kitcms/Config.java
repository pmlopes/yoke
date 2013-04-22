package com.jetdrone.vertx.kitcms;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {

    public static class Domain {
        public final String name;
        public final String namespace;
        public final String user;
        public final String password;
        public final Pattern pattern;

        Domain(String name, String namespace, String user, String password) {
            this.name = name;
            this.namespace = namespace;
            this.user = user;
            this.password = password;
            // Make "*" match expand match and limit range of match
            // Case insensitive matching
            pattern = Pattern.compile("^(" + name.replaceAll("\\*", ".*") + ")$", Pattern.CASE_INSENSITIVE);
        }
    }

    // Output errors to browser
    public final boolean debug;
    // enable session cookies
    public final boolean sessions;
    // secret used to discourage cookie tampering
    public final String cookieSecret;
    //enable socket.io, see lib/socketio/index.js and lib/socketio/example.js
    public final boolean socketio;
    // http server settings
    public final String serverAddress;
    public final int serverPort;
    // redis config
    public final String dbServer;
    public final int dbPort;
    public final int dbNumber;
    public final String dbPassword;
    // Domains that are resolved by server
    // checks against req.host
    public final Domain[] domains;

    public static final String REDIS_ADDRESS = "kitcms.redis";


    public Config(JsonObject config) {

        debug = config.getBoolean("debug", false);
        sessions = config.getBoolean("sessions", false);
        cookieSecret = config.getString("cookieSecret", "ZhtIEKq51-Md2HH--b0w");
        socketio = config.getBoolean("socketio", false);
        serverAddress = config.getString("serverAddress", "0.0.0.0");
        serverPort = (Integer) config.getNumber("serverPort", 8080);

        dbServer = config.getString("dbServer", "localhost");
        dbPort = (Integer) config.getNumber("dbPort", 6379);
        dbNumber = (Integer) config.getNumber("dbNumber", 0);
        dbPassword = config.getString("dbPassword");

        // Credentials for /admin if not set in the domain
        String adminUser = config.getString("adminUser");
        String adminPassword = config.getString("adminPassword");

        JsonArray domains = config.getArray("domains");
        if (domains != null) {
            this.domains = new Domain[domains.size()];
            for (int i = 0; i < domains.size(); i++) {
                JsonObject domain = (JsonObject) domains.get(i);
                String name = domain.getString("name");
                String namespace = domain.getString("namespace");
                String user = domain.getString("user", adminUser);
                String password = domain.getString("password", adminPassword);
                this.domains[i] = new Domain(name, namespace, user, password);
            }
        } else {
            this.domains = new Domain[1];

            // Example catch all
            // Has the same namespace as the previous domain, so they share data
            // AdminRouter login has been disabled
            this.domains[0] = new Domain("*", "default", "foo", "bar");
        }
    }

    public JsonObject getRedisConfig() {
        JsonObject config = new JsonObject();

        config.putString("address", REDIS_ADDRESS);
        config.putString("host", dbServer);
        config.putNumber("port", dbPort);
        config.putString("password", dbPassword);
        // TODO: select
        return config;
    }
}
