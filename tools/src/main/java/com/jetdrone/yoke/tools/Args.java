package com.jetdrone.yoke.tools;

import java.util.List;

public class Args {

    public static String getArgument(String name, String[] args) {
        return getArgument(name, args, null);
    }

    public static String getArgument(String name, String[] args, String error) {
        for (int i = 0; i < args.length; i++) {
            // argument must start with --name
            if (args[i].equals("--" + name)) {
                // it is a argument --name value
                if (i+1 < args.length) {
                    // there is a value after this
                    String value = args[i + 1];
                    if (value.startsWith("--")) {
                        // value is empty
                        break;
                    } else {
                        return value;
                    }
                }
            }
        }

        if (error != null) {
            throw new RuntimeException("Error: '--" + name + " <value>' " + error);
        }

        return null;
    }

    public static String getArgumentFlag(List<String> names, String[] args) {
        for (String name : names) {
            String value = getArgumentFlag(name, args);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public static String getArgumentFlag(String name, String[] args) {
        return getArgumentFlag(name, args, null);
    }

    public static String getArgumentFlag(String name, String[] args, String error) {
        for (String arg : args) {
            // argument must start with --name
            if (arg.equals("--" + name)) {
                return name;
            }
        }

        if (error != null) {
            throw new RuntimeException("Error: '--" + name + " <value>' " + error);
        }

        return null;
    }
}
