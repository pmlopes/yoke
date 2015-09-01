package com.jetdrone.vertx.kitcms;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.regex.Pattern;

public class Config {

    public static class Domain {
        public final String name;
        public final String namespace;
        public final Pattern pattern;

        Domain(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
            // Make "*" match expand match and limit range of match
            // Case insensitive matching
            pattern = Pattern.compile("^(" + name.replaceAll("\\.", "\\.").replaceAll("\\*", ".*") + ")$", Pattern.CASE_INSENSITIVE);
        }
    }

    // http server settings
    public final String serverAddress;
    public final int serverPort;
    // redis config
    public final String dbServer;
    public final int dbPort;
    // Domains that are resolved by server
    // checks against req.host
    public final Domain[] domains;
    // admin credentials
    public final String adminUsername;
    public final String adminPassword;

    public static final String REDIS_ADDRESS = "kitcms.redis";


    public Config(JsonObject config) {

        serverAddress = config.getString("serverAddress", "0.0.0.0");
        serverPort = (Integer) config.getNumber("serverPort", 8080);

        dbServer = config.getString("dbServer", "localhost");
        dbPort = (Integer) config.getNumber("dbPort", 6379);

        adminUsername = config.getString("adminUsername", "foo");
        adminPassword = config.getString("adminPassword", "bar");

        JsonArray domains = config.getArray("domains");
        if (domains != null) {
            this.domains = new Domain[domains.size()];
            for (int i = 0; i < domains.size(); i++) {
                JsonObject domain = domains.get(i);
                String name = domain.getString("name");
                String namespace = domain.getString("namespace");
                this.domains[i] = new Domain(name, namespace);
            }
        } else {
            this.domains = new Domain[1];
            // Example catch all
            // Has the same namespace as the previous domain, so they share data
            // AdminRouter login has been disabled
            this.domains[0] = new Domain("*", "default");
        }
    }

    public JsonObject getRedisConfig() {
        JsonObject config = new JsonObject();

        config.putString("address", REDIS_ADDRESS);
        config.putString("host", dbServer);
        config.putNumber("port", dbPort);
        return config;
    }
}
