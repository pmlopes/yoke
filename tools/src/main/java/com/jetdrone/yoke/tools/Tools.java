package com.jetdrone.yoke.tools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tools {

    private static String language;
    private static String owner;
    private static String module;
    private static String app;
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

    static String getArgument(String name, String[] args, String error) {
        for (int i = 0; i < args.length; i+=2) {
            if (args[i].startsWith("--")) {
                if (args[i].equals("--" + name)) {
                    String result = args[i + 1];
                    if (result.startsWith("--")) {
                        break;
                    } else {
                        return result;
                    }
                }
            }
        }

        if (error != null) {
            abort("Error: '--" + name + " <value>' " + error);
        }
        return null;
    }

    static void printUsage() {
        System.out.println("Usage: --owner com.jetdrone --module app [--app yoke-app] [--version 1.0.0]");
    }

    public static void main(String[] args) throws IOException {

        language = "java";
        owner = getArgument("owner", args, "Owner e.g.: 'com.jetdrone'");
        module = getArgument("module", args, "Module name e.g.: 'app'");
        app = getArgument("app", args, null);
        if (app == null) {
            app = module;
        }
        version = getArgument("version", args, null);
        if (version == null) {
            version = "1.0.0-SNAPSHOT";
        }

        String help = getArgument("help", args, null);
        if (help != null) {
            printUsage();
            System.exit(0);
        }

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
                throw new RuntimeException("Not implemented yet");
//                break;
            case "javascript":
                throw new RuntimeException("Not implemented yet");
//                break;
            default:
                abort("Unsupported language: '" + language + "'");
        }

        printDone();
    }

    static void check() {
        File f = new File(app);
        if (f.exists() && f.isDirectory()) {
            abort("Directory '" + app + "' already exists!");
        }
        // create the base app directory
        mkdir(app);
    }

    static void copyBaseTemplate() throws IOException {

        copy(app + "/README.md", "templates/gradle/README.md");

        String props =
                "# E.g. your domain name\n" +
                "modowner=" + owner + "\n\n" +
                "# Your module name\n" +
                "modname=" + module + "\n\n" +
                "# Your module version\n" +
                "version=" + version + "\n\n" + readResourceToString("templates/gradle/gradle.properties");

        write(app + "/gradle.properties", props);
        copy(app + "/conf.json", "templates/gradle/conf.json");
        copy(app + "/LICENSE.txt", "templates/gradle/LICENSE.txt");
        copy(app + "/gradlew.bat", "templates/gradle/gradlew.bat");
        copy(app + "/gradlew", "templates/gradle/gradlew");
        // need to set the *nix to executable
        new File(app + "/gradlew").setExecutable(true);
        copy(app + "/build.gradle", "templates/gradle/build.gradle");

        mkdir(app + "/gradle");
        copy(app + "/gradle/vertx.gradle", "templates/gradle/gradle/vertx.gradle");
        copy(app + "/gradle/setup.gradle", "templates/gradle/gradle/setup.gradle");
        copy(app + "/gradle/maven.gradle", "templates/gradle/gradle/maven.gradle");

        mkdir(app + "/gradle/wrapper");
        copy(app + "/gradle/wrapper/gradle-wrapper.properties", "templates/gradle/gradle/wrapper/gradle-wrapper.properties");
        copy(app + "/gradle/wrapper/gradle-wrapper.jar", "templates/gradle/gradle/wrapper/gradle-wrapper.jar");
    }

    static void createJava() {
        // base source code
        mkdir(app + "/src/main/java");
        String modulePath = owner.replace('.', '/') + "/" + module.replace('.', '/');
        // expand package
        mkdir(app + "/src/main/java/" + modulePath);

        write(app + "/src/main/java/" + modulePath + "/App.java",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/java/App.java"));

        mkdir(app + "/src/main/resources");
        // write resources
        mkdir(app + "/src/main/resources/public");
        mkdir(app + "/src/main/resources/public/stylesheets");
        copy(app + "/src/main/resources/public/stylesheets/style.css", "templates/java/style.css");
        mkdir(app + "/src/main/resources/views");
        copy(app + "/src/main/resources/views/index.html", "templates/java/index.html");
        // write mod.json
        write(app + "/src/main/resources/mod.json",
                "{\n" +
                "  \"main\": \"" + owner + "." + module + ".App\",\n" +
                "  \"includes\": \"com.jetdrone~yoke~1.0.2-SNAPSHOT\"\n" +
                "}\n");

        mkdir(app + "/src/test/java");
        // expand package
        mkdir(app + "/src/test/java/" + modulePath);

        mkdir(app + "/src/test/resources");
    }

    static void printDone() {
        System.out.println();
        System.out.println("Go to your new app:");
        System.out.println("  cd " + app);
        System.out.println();
        System.out.println("Compile your app:");
        System.out.println("  ./gradlew[.bat] check");
        System.out.println();
        System.out.println("Run your app:");
        System.out.println("  ./gradlew[.bat] runMod");
        System.out.println();
    }
}
