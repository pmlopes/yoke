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

    static String template1 = "\t\n" +
            "\n" +
            "    <script type=\"text/html\" id=\"item_tmpl\">\n" +
            "      <div id=\"<%=id%>\" class=\"<%=(i % 2 == 1 ? \" even\" : \"\")%>\">\n" +
            "        <div class=\"grid_1 alpha right\">\n" +
            "          <img class=\"righted\" src=\"<%=profile_image_url%>\"/>\n" +
            "        </div>\n" +
            "        <div class=\"grid_6 omega contents\">\n" +
            "          <p><b><a href=\"/<%=from_user%>\"><%=from_user%></a>:</b> <%=text%></p>\n" +
            "        </div>\n" +
            "      </div>\n" +
            "    </script>\n" +
            "\n";

    static String template2 = "\t\n" +
            "\n" +
            "    <script type=\"text/html\" id=\"user_tmpl\">\n" +
            "      <% for ( var i = 0; i < users.length; i++ ) { %>\n" +
            "        <li><a href=\"<%=users[i].url%>\"><%=users[i].name%></a></li>\n" +
            "      <% } %>\n" +
            "    </script>\n" +
            "\n";

    public static void template(String template) {
        int open = 0;
        int close = 0;

        List<String> strings = new ArrayList<>();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < template.length() - 1; i++) {
            if (template.charAt(i) == '<' && template.charAt(i+1) == '%') {
                if (close < i) {
                    // slice
                    int id = strings.size();
                    strings.add(template.substring(close, i));
                    code.append("print(");
                    code.append(id);
                    code.append(");\n");
                }
                open = i;
            }
            if (template.charAt(i) == '%' && template.charAt(i+1) == '>') {
                close = i+2;
                if (open < i) {
                    // slice
                    code.append(template.substring(open, close));
                    code.append("\n");
                }
            }
        }

        System.out.println(code.toString());
    }

    public static void main(String[] args) {
        template(template2);
//        availableEngine();
//        jsEvalWithVariable();

    }
}
