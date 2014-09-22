/*
 * Copyright (c) 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl;

import de.static_interface.sinklibrary.SinkLibrary;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PythonScript extends ScriptEngineScript {

    public PythonScript(Plugin plugin) {
        super(plugin, "python", "py", "jython");
    }

    @Override
    public String formatCode(String code) {
        ChatColor defaultColor = ChatColor.DARK_BLUE;
        ChatColor codeColor = ChatColor.RESET;
        ChatColor classColor = ChatColor.BLUE;
        ChatColor stringColor = ChatColor.RED;

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

        HashMap<String, ChatColor> syntaxColors = new HashMap<>();

        syntaxColors.put("and", defaultColor);
        syntaxColors.put("del", defaultColor);
        syntaxColors.put("from", ChatColor.GOLD);
        syntaxColors.put("not", defaultColor);
        syntaxColors.put("while", defaultColor);
        syntaxColors.put("as", defaultColor);
        syntaxColors.put("elif", defaultColor);
        syntaxColors.put("global", defaultColor);
        syntaxColors.put("or", defaultColor);
        syntaxColors.put("with", defaultColor);
        syntaxColors.put("assert", defaultColor);
        syntaxColors.put("else", defaultColor);
        syntaxColors.put("if", defaultColor);
        syntaxColors.put("pass", defaultColor);
        syntaxColors.put("yiel", defaultColor);
        syntaxColors.put("break", defaultColor);
        syntaxColors.put("except", defaultColor);
        syntaxColors.put("import", ChatColor.GOLD);
        syntaxColors.put("print", defaultColor);
        syntaxColors.put("class", defaultColor);
        syntaxColors.put("exec", defaultColor);
        syntaxColors.put("in", defaultColor);
        syntaxColors.put("raise", defaultColor);
        syntaxColors.put("continue", defaultColor);
        syntaxColors.put("finally", defaultColor);
        syntaxColors.put("is", defaultColor);
        syntaxColors.put("return", defaultColor);
        syntaxColors.put("def", defaultColor);
        syntaxColors.put("for", defaultColor);
        syntaxColors.put("lambda", defaultColor);
        syntaxColors.put("try", defaultColor);

        syntaxColors.put("True", ChatColor.GOLD);
        syntaxColors.put("False", ChatColor.GOLD);
        for (String keyWord : syntaxColors.keySet()) {
            ChatColor color = syntaxColors.get(keyWord);
            code = code.replace(" " + keyWord + " ", color + " " + keyWord + " " + ChatColor.RESET);
            code = code.replace(" " + keyWord, color + " " + keyWord + ChatColor.RESET);
            code = code.replace(keyWord + " ", color + keyWord + " " + ChatColor.RESET);
        }

        tmp = "";

        char startChar = 0;
        //Set String color
        for (char Char : code.toCharArray()) {
            if (Char == startChar) {
                if ((Char == ']' && lastChar == ']') || Char != ']') {
                    tmp += codeColor;
                }
                tmp += Char;
                startChar = 0;
            } else if (Char == '\'' || Char == '"' || (Char == '[' && lastChar == '[')) {
                if (startChar == 0 && Char != ']') {
                    startChar = Char;
                }

                tmp += stringColor;
                tmp += Char;
            } else {
                tmp += Char;
            }
            lastChar = Char;
        }

        return tmp;
    }

    @Override
    protected String getDefaultImports() {
        return "";
    }

    @Override
    public List<String> getImportIdentifier() {
        List<String> importIdentifiers = new ArrayList<>();
        importIdentifiers.add("from");
        importIdentifiers.add("import");
        return importIdentifiers;
    }

    @Override
    public void onPreInit() {
        try {
            System.getenv().put("JYTHON_HOME", FRAMEWORK_FOLDER.getAbsolutePath());
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Override
    public void onInit() {
        File jynxDirectory = new File(FRAMEWORK_FOLDER, "jynx");
        if (jynxDirectory.exists()) {
            setupJynx(jynxDirectory);
        } else {
            SinkLibrary.getInstance().getCustomLogger().warning("Warning! Couldn't find jynx! Missing directory: " + jynxDirectory.getAbsolutePath());
        }
    }

    private void setupJynx(File jynx) {
        File setup = new File(jynx, "setup.py");
        if (setup.exists()) {
            run(getConsoleShellInstance(), setup, false, true);
        }
    }
}
