package com.jetdrone.vertx.yoke.tools;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class Tools {

    private static String language;
    private static String owner;
    private static String module;
    private static String version;

    private static String yokeVersion;

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
        System.err.println("   \033[31merror\033[0m : " + message);
        System.exit(1);
    }

    static void warn(String message) {
        System.err.println("   \033[33mwarning\033[0m : " + message);
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
        System.out.println("Usage: [--help] --[java|groovy|groovyscript|javascript] group:artifact:version");
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
            Properties properties = new Properties();
            properties.load(Tools.class.getClassLoader().getResourceAsStream("build.properties"));

            yokeVersion = properties.getProperty("version");

            language = Args.getArgumentFlag(Arrays.asList(new String[]{"java", "groovy", "groovyscript", "javascript"}), args);

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
            case "groovyscript":
                // setup gradle
                copyBaseTemplate();
                createGroovyScript();
                break;
            case "javascript":
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

        copy(module + "/README.md", "templates/maven/README.md");

        String pom = readResourceToString("templates/maven/pom.xml");

        pom = pom.replace("<groupId>mygroup</groupId>", "<groupId>" + owner + "</groupId>");
        pom = pom.replace("<artifactId>myartifact</artifactId>", "<artifactId>" + module + "</artifactId>");
        pom = pom.replace("<version>1.0-SNAPSHOT</version>", "<version>" + version + "</version>");

        pom = pom.replace("<!-- YOKE -->",
                "<dependency>\n" +
                "            <groupId>com.jetdrone</groupId>\n" +
                "            <artifactId>yoke</artifactId>\n" +
                "            <version>2.0.12-SNAPSHOT</version>\n" +
                "        </dependency>");

        write(module + "/pom.xml", pom);

        mkdir(module + "/src/main/assembly");

        copy(module + "/src/main/assembly/mod.xml", "templates/maven/src/main/assembly/mod.xml");
    }

    static void createJava() {
        // base source code
        mkdir(module + "/src/main/java");
        String modulePath = owner.replace('.', '/') + "/" + module.replace('.', '/');
        // expand package
        mkdir(module + "/src/main/java/" + modulePath);

        write(module + "/src/main/java/" + modulePath + "/App.java",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/java/App.java")
        );

        mkdir(module + "/src/main/resources");
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/java/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.shtml", "templates/java/index.shtml");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                        "{\n" +
                        "  \"main\": \"" + owner + "." + module + ".App\",\n" +
                        "  \"includes\": \"com.jetdrone~yoke~" + yokeVersion + "\"\n" +
                        "}\n"
        );

        mkdir(module + "/src/test/java");
        // expand package
        mkdir(module + "/src/test/java/" + modulePath);
        write(module + "/src/test/java/" + modulePath + "/AppTest.java",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/java/AppTest.java")
        );

        mkdir(module + "/src/test/resources");
    }

    static void createGroovy() {
        // base source code
        mkdir(module + "/src/main/groovy");
        String modulePath = owner.replace('.', '/') + "/" + module.replace('.', '/');
        // expand package
        mkdir(module + "/src/main/groovy/" + modulePath);

        write(module + "/src/main/groovy/" + modulePath + "/App.groovy",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/groovy/App.groovy")
        );

        mkdir(module + "/src/main/resources");
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/groovy/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.gsp", "templates/groovy/index.gsp");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                "{\n" +
                        "  \"main\": \"groovy:" + owner + "." + module + ".App\",\n" +
                        "  \"includes\": \"com.jetdrone~yoke~" + yokeVersion + "\"\n" +
                        "}\n"
        );

        mkdir(module + "/src/test/groovy");
        // expand package
        mkdir(module + "/src/test/groovy/" + modulePath);
        write(module + "/src/test/groovy/" + modulePath + "/AppTest.groovy",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/groovy/AppTest.groovy")
        );

        mkdir(module + "/src/test/resources");
    }

    static void createGroovyScript() {
        // base source code
        mkdir(module + "/src/main/resources");
        write(module + "/src/main/resources/App.groovy", readResourceToString("templates/groovyscript/App.groovy"));
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/groovyscript/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.gsp", "templates/groovyscript/index.gsp");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                        "{\n" +
                        "  \"main\": \"App.groovy\",\n" +
                        "  \"includes\": \"com.jetdrone~yoke~" + yokeVersion + "\"\n" +
                        "}\n"
        );
    }

    static void createJS() {
        String modulePath = owner.replace('.', '/') + "/" + module.replace('.', '/');
        // base source code
        mkdir(module + "/src/main/resources");
        write(module + "/src/main/resources/App.js", readResourceToString("templates/javascript/App.js"));
        // write resources
        mkdir(module + "/src/main/resources/public");
        mkdir(module + "/src/main/resources/public/stylesheets");
        copy(module + "/src/main/resources/public/stylesheets/style.css", "templates/javascript/style.css");
        mkdir(module + "/src/main/resources/views");
        copy(module + "/src/main/resources/views/index.ejs", "templates/javascript/index.ejs");
        // write mod.json
        write(module + "/src/main/resources/mod.json",
                        "{\n" +
                        "  \"main\": \"App.js\",\n" +
                        "  \"includes\": \"com.jetdrone~yoke~" + yokeVersion + "\"\n" +
                        "}\n"
        );

        mkdir(module + "/src/test/java");
        // expand package
        mkdir(module + "/src/test/java/" + modulePath);
        // JUnit helper
        write(module + "/src/test/java/" + modulePath + "/JSTester.java",
                "package " + owner + "." + module + ";\n\n" +
                        readResourceToString("templates/javascript/JSTester.java")
        );

        mkdir(module + "/src/test/resources");
        write(module + "/src/test/resources/AppTest.js", readResourceToString("templates/javascript/AppTest.js"));
    }

    static void printDone() {
        System.out.println();
        System.out.println("Go to your new app:");
        System.out.println("  cd " + module);
        System.out.println();
        System.out.println("Compile your app:");
        System.out.println("  mvn clean install");
        System.out.println();
        System.out.println("Run your app:");
        System.out.println("  mvn vertx:runMod");
        System.out.println();
    }
}
