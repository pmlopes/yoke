package com.jetdrone.yoke.tools;

import java.io.*;

public class Tools {

    private static String language;
    private static String owner;
    private static String module;
    private static String version;

    static void write(String path, String value) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            out.write(value.getBytes());
            out.close();
        } catch (IOException e) {
            abort(e.getMessage());
        }
        System.out.println("   \u001b[36mcreate\u001b[0m : " + path);
    }

    static void copy(String path, String resource) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            copyResource(resource, out);
            out.close();
        } catch (IOException e) {
            abort(e.getMessage());
        }
        System.out.println("   \u001b[36mcreate\u001b[0m : " + path);
    }

    static void mkdir(String path) {
        File f = new File(path);
        if (!f.mkdirs()) {
            abort("Could not create directory: '" + path + "'");
        }
        System.out.println("   \033[36mcreate\033[0m : " + path);
    }

    static void abort(String message) {
        System.err.println(message);
        System.exit(1);
    }

    static String readResourceToString(String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(Tools.class.getClassLoader().getResourceAsStream(resource), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return writer.toString();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    static void copyResource(String resource, OutputStream out) {
        try {
            try (InputStream in = Tools.class.getClassLoader().getResourceAsStream(resource)) {

                byte[] buffer = new byte[1024];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }

                out.flush();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void main(String[] args) {

        language = "java";
        owner = "com.jetdrone";
        module = "yoke-app";
        version = "1.0.0-SNAPSHOT";

        // init check
        check();
        // setup gradle
        copyBaseTemplate();
        // create template based on language
        switch (language) {
            case "java":
                createJava();
                break;
            case "groovy":
                break;
            case "javascript":
                break;
            default:
                abort("Unsupported language: '" + language + "'");
        }
    }

    static void check() {
        File f = new File(module);
        if (f.exists() && f.isDirectory()) {
            abort("Directory '" + module + "' already exists!");
        }
        // create the base app directory
        mkdir(module);
    }

    static void copyBaseTemplate() {

        copy(module + "/README.md", "templates/gradle/README.md");

        String props =
                "# E.g. your domain name\n" +
                "modowner=" + owner + "\n\n" +
                "# Your module name\n" +
                "modname=" + module + "\n\n" +
                "# Your module version\n" +
                "version=" + version + "\n\n" + readResourceToString("templates/gradle/gradle.properties");

        write(module + "/gradle.properties", props);
        copy(module + "/conf.json", "templates/gradle/conf.json");
        copy(module + "/LICENSE.txt", "templates/gradle/LICENSE.txt");
        copy(module + "/gradlew.bat", "templates/gradle/gradlew.bat");
        copy(module + "/gradlew", "templates/gradle/gradlew");
        copy(module + "/build.gradle", "templates/gradle/build.gradle");

        mkdir(module + "/gradle");
        copy(module + "/gradle/vertx.gradle", "templates/gradle/gradle/vertx.gradle");
        copy(module + "/gradle/setup.gradle", "templates/gradle/gradle/setup.gradle");
        copy(module + "/gradle/maven.gradle", "templates/gradle/gradle/maven.gradle");

        mkdir(module + "/gradle/wrapper");
        copy(module + "/gradle/wrapper/gradle-wrapper.properties", "templates/gradle/gradle/wrapper/gradle-wrapper.properties");
        copy(module + "/gradle/wrapper/gradle-wrapper.jar", "templates/gradle/gradle/wrapper/gradle-wrapper.jar");
    }

    static void createJava() {
        // base source code
        mkdir(module + "/src/main/java");
        String modulePath = owner.replace('.', '/') + "/" + module.replace('.', '/');
        // expand package
        mkdir(module + "/src/main/java/" + modulePath);

        String app =
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/java/App.java");

        write(module + "/src/main/java/" + modulePath + "/App.java", app);

        mkdir(module + "/src/main/resources");

        mkdir(module + "/src/test/java");
        // expand package
        mkdir(module + "/src/test/java/" + modulePath);

        mkdir(module + "/src/test/resources");
    }
}
