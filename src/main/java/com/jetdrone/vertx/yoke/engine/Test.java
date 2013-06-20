package com.jetdrone.vertx.yoke.engine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    private static void availableEngine()
    {
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory : factories)
        {
            System.out.println("ScriptEngineFactory Info");
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
            List<String> engNames = factory.getNames();
            for (String name : engNames)
            {
                System.out.printf("\tEngine Alias: %s\n", name);
            }
            System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
        }
    }

    private static void jsEvalWithVariable()
    {
        List<String> namesList = new ArrayList<String>();
        namesList.add("Jill");
        namesList.add("Bob");
        namesList.add("Laureen");
        namesList.add("Ed");

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");

        jsEngine.put("namesListKey", namesList);
        System.out.println("Executing in script environment...");
        try
        {
            jsEngine.eval("var x;" +
                    "var names = namesListKey.toArray();" +
                    "for(x in names) {" +
                    "  println(names[x]);" +
                    "}" +
                    "namesListKey.add(\"Dana\");");
        }
        catch (ScriptException ex)
        {
            ex.printStackTrace();
        }
        System.out.println("--");

        for (String s : namesList) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        availableEngine();
        jsEvalWithVariable();

    }
}
