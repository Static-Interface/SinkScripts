/*
 * Copyright (c) 2013 - 2014 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl;

import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GroovyScript extends ScriptEngineScript {

    public GroovyScript(Plugin plugin) {
        super(plugin, "groovy", "groovy", "groovy");
    }

    @Override
    public String formatCode(String code) {
        ChatColor defaultColor = ChatColor.DARK_BLUE;
        ChatColor codeColor = ChatColor.RESET;
        ChatColor classColor = ChatColor.BLUE;
        ChatColor stringColor = ChatColor.RED;

        HashMap<String, ChatColor> syntaxColors = new HashMap<>();
        syntaxColors.put("import", ChatColor.GOLD);
        syntaxColors.put("package", ChatColor.GOLD);

        syntaxColors.put("class", defaultColor);
        syntaxColors.put("implements", defaultColor);
        syntaxColors.put("extends", defaultColor);
        syntaxColors.put("enum", defaultColor);
        syntaxColors.put("interface", defaultColor);

        syntaxColors.put("public", defaultColor);
        syntaxColors.put("private", defaultColor);
        syntaxColors.put("protected", defaultColor);

        syntaxColors.put("final", defaultColor);
        syntaxColors.put("static", defaultColor);
        syntaxColors.put("native", defaultColor);
        syntaxColors.put("throws", defaultColor);
        syntaxColors.put("transient", defaultColor);
        syntaxColors.put("volatile", defaultColor);
        syntaxColors.put("synchronized", defaultColor);
        syntaxColors.put("strictfp", defaultColor);
        syntaxColors.put("const", defaultColor);

        syntaxColors.put("try", defaultColor);
        syntaxColors.put("catch", defaultColor);
        syntaxColors.put("finally", defaultColor);
        syntaxColors.put("throw", defaultColor);

        syntaxColors.put("for", defaultColor);
        syntaxColors.put("while", defaultColor);
        syntaxColors.put("continue", defaultColor);

        syntaxColors.put("void", defaultColor);
        syntaxColors.put("return", defaultColor);
        syntaxColors.put("switch", defaultColor);
        syntaxColors.put("case", defaultColor);
        syntaxColors.put("default", defaultColor);
        syntaxColors.put("super", defaultColor);
        syntaxColors.put("goto", defaultColor);

        syntaxColors.put("new", defaultColor);
        syntaxColors.put("this", defaultColor);
        syntaxColors.put("null", ChatColor.GOLD);

        syntaxColors.put("if", defaultColor);
        syntaxColors.put("else", defaultColor);
        syntaxColors.put("instanceof", defaultColor);
        syntaxColors.put("assert", defaultColor);

        syntaxColors.put("true", ChatColor.GOLD);
        syntaxColors.put("false", ChatColor.GOLD);

        syntaxColors.put("int", defaultColor);
        syntaxColors.put("boolean", defaultColor);
        syntaxColors.put("long", defaultColor);
        syntaxColors.put("short", defaultColor);
        syntaxColors.put("float", defaultColor);
        syntaxColors.put("byte", defaultColor);
        syntaxColors.put("char", defaultColor);

        for (String keyWord : syntaxColors.keySet()) {
            ChatColor color = syntaxColors.get(keyWord);
            code = code.replace(" " + keyWord + " ", color + " " + keyWord + " " + ChatColor.RESET);
            code = code.replace(" " + keyWord, color + " " + keyWord + ChatColor.RESET);
            code = code.replace(keyWord + " ", color + keyWord + " " + ChatColor.RESET);
        }

        //Set class color, its not the best solution, because variables may also start with an uppercase name
        boolean classStart = false;
        char lastChar = 0;
        String tmp = "";
        for (char Char : code.toCharArray()) {
            boolean t = false;
            if (!Character.isAlphabetic(lastChar) && Character.isUpperCase(Char) && !classStart) {
                classStart = true;
                t = true;
            }

            if (!classStart) {
                tmp += Char;
                continue;
            }

            if (!Character.isAlphabetic(Char)
                && !t)//if(Char == '.' || Char == ' ' || Char == ';' || Char == '+' || Char == '-' || Char == '*' || Char == ':' || Char == '/')
            {
                classStart = false;
                tmp += ChatColor.RESET + "" + Char;
                continue;
            }

            tmp += classColor + "" + Char;
            lastChar = Char;
        }

        code = tmp;
        tmp = "";
        lastChar = 0;

        boolean stringStart = false;

        //Set String color
        for (char Char : code.toCharArray()) {
            if (Char == '"' && lastChar != '\\') {
                if (!stringStart) {
                    tmp += stringColor;
                }

                tmp += Char;

                if (stringStart) {
                    tmp += codeColor;
                }

                stringStart = !stringStart;
            } else {
                tmp += Char;
            }
            lastChar = Char;
        }

        return tmp;
    }

    @Override
    public String getDefaultImports(ScriptContext context) {
        String nl = Util.getNewLine();
        return "import de.static_interface.sinklibrary.*;" + nl +
               "import de.static_interface.sinklibrary.user.*;" + nl +
               "import de.static_interface.sinklibrary.api.*;" + nl +
               "import de.static_interface.sinklibrary.api.user.*;" + nl +
               "import de.static_interface.sinkscripts.*;" + nl +
               "import org.bukkit.block.*;" + nl +
               "import org.bukkit.event.*;" + nl +
               "import org.bukkit.entity.*;" + nl +
               "import org.bukkit.inventory.*;" + nl +
               "import org.bukkit.material.*;" + nl +
               "import org.bukkit.potion.*; " + nl +
               "import org.bukkit.util.*" + nl +
               "import org.bukkit.*;" + nl +
               "import javax.script.*;" + nl + nl;
    }


    @Override
    public Collection<String> getImportIdentifiers() {
        List<String> importIdentifiers = new ArrayList<>();
        importIdentifiers.add("import");
        importIdentifiers.add("package");
        return importIdentifiers;
    }
}
