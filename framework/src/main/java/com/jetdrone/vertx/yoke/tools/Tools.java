package com.jetdrone.vertx.yoke.tools;

import java.io.*;
import java.util.Arrays;

public class Tools {

    private static String language;
    private static String owner;
    private static String module;
    private static String version;

    private static final String VERSION = "1.0.8";

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

    static void printUsage() {
        System.out.println("Usage: [--help] --[java|groovy|js] group:artifact:version");
        System.out.println("  Group:    the owner of the project usually your reverse domain name");
        System.out.println("            e.g.: com.mycompany");
        System.out.println("  Artifact: the module name, usually your app name without special chars");
        System.out.println("             e.g.: mymodule");
        System.out.println("  Version:  app version");
        System.out.println("            e.g.: 1.0.0-SNAPSHOT");
        System.out.println();
    }

    public static void main(String[] args) throws IOException {

        try {
            language = Args.getArgumentFlag(Arrays.asList(new String[] {"java", "groovy", "js"}), args);

            if (language == null) {
                throw new RuntimeException("ERROR: Language is required!");
            }

            if (args.length < 2) {
                throw new RuntimeException("ERROR: Invalid number of arguments");
            }

            String[] gav = args[1].split(":");

            owner = gav[0];
            module = gav[1];
            version = gav[2];

            String help = Args.getArgument("help", args);
            if (help != null) {
                printUsage();
                System.exit(0);
            }
        } catch (RuntimeException re) {
            printUsage();
            abort(re.getMessage());
        }

        // init check
        check();
        // create template based on language
        switch (language) {
            case "java":
                // setup gradle
                copyBaseTemplate();
                createJava();
                break;
            case "groovy":
                // setup gradle
                copyBaseTemplate();
                createGroovy();
                break;
            case "js":
                // setup gradle
                copyBaseTemplate();
                createJS();
                break;
            default:
                abort("Unsupported language: '" + language + "'");
        }

        printDone();
    }

    static void check() {
        File f = new File(module);
        if (f.exists() && f.isDirectory()) {
            abort("Directory '" + module + "' already exists!");
        }
        // create the base app directory
        mkdir(module);
    }

    static void copyBaseTemplate() throws IOException {

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
        // need to set the *nix to executable
        new File(module + "/gradlew").setExecutable(true);
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

        write(module + "/src/main/java/" + modulePath + "/App.java",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/java/App.java"));

        mkdir(module + "/src/main/resources");
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/java/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.html", "templates/java/index.html");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                "{\n" +
                "  \"main\": \"" + owner + "." + module + ".App\",\n" +
                "  \"includes\": \"com.jetdrone~yoke~" + VERSION + "\"\n" +
                "}\n");

        mkdir(module + "/src/test/java");
        // expand package
        mkdir(module + "/src/test/java/" + modulePath);
        write(module + "/src/test/java/" + modulePath + "/AppTest.java",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/java/AppTest.java"));

        mkdir(module + "/src/test/resources");
    }

    static void createGroovy() {
        // base source code
        mkdir(module + "/src/main/resources");
        write(module + "/src/main/resources/App.groovy", readResourceToString("templates/groovy/App.groovy"));
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/groovy/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.html", "templates/groovy/index.html");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                "{\n" +
                        "  \"main\": \"App.groovy\",\n" +
                        "  \"includes\": \"com.jetdrone~yoke~" + VERSION + "\"\n" +
                        "}\n");
    }

    static void createJS() {
        // base source code
        mkdir(module + "/src/main/resources");
        write(module + "/src/main/resources/App.js", readResourceToString("templates/javascript/App.js"));
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/javascript/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.html", "templates/javascript/index.html");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                "{\n" +
                        "  \"main\": \"App.js\",\n" +
                        "  \"includes\": \"com.jetdrone~yoke~" + VERSION + "\"\n" +
                        "}\n");
    }

    static void printDone() {

        String OS = System.getProperty("os.name").toLowerCase();

        System.out.println();
        System.out.println("Go to your new app:");
        System.out.println("  cd " + module);
        System.out.println();
        System.out.println("Compile your app:");
        if (OS.contains("win")) {
            System.out.println("  gradlew.bat check");
        } else {
            System.out.println("  ./gradlew check");
        }
        System.out.println();
        System.out.println("Run your app:");
        if (OS.contains("win")) {
            System.out.println("  gradlew.bat runMod");
        } else {
            System.out.println("  ./gradlew runMod");
        }
        System.out.println();
    }
}
