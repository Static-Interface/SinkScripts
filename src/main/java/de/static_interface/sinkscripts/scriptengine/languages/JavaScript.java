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

package de.static_interface.sinkscripts.scriptengine.languages;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JavaScript extends ScriptEngineScript
{
    public JavaScript(Plugin plugin)
    {
        super(plugin, "javascript", "js", "JavaScript");
    }

    @Override
    public String formatCode(String code)
    {
        ChatColor defaultColor = ChatColor.DARK_BLUE;
        ChatColor codeColor = ChatColor.RESET;
        ChatColor classColor = ChatColor.BLUE;
        ChatColor stringColor = ChatColor.RED;

        HashMap<String, ChatColor> syntaxColors = new HashMap<>();
        syntaxColors.put("import", ChatColor.GOLD);
        syntaxColors.put("package", ChatColor.GOLD);

        syntaxColors.put("abstract", defaultColor);
        syntaxColors.put("case", defaultColor);
        syntaxColors.put("continue", defaultColor);
        syntaxColors.put("double", defaultColor);
        syntaxColors.put("extends", defaultColor);
        syntaxColors.put("for", defaultColor);
        syntaxColors.put("let", defaultColor);
        syntaxColors.put("short", defaultColor);
        syntaxColors.put("this", defaultColor);
        syntaxColors.put("try", defaultColor);
        syntaxColors.put("while", defaultColor);
        syntaxColors.put("arguments", defaultColor);
        syntaxColors.put("catch", defaultColor);
        syntaxColors.put("debugger", defaultColor);
        syntaxColors.put("else", defaultColor);
        syntaxColors.put("false", defaultColor);
        syntaxColors.put("function", defaultColor);
        syntaxColors.put("in", defaultColor);
        syntaxColors.put("long", defaultColor);
        syntaxColors.put("private", defaultColor);
        syntaxColors.put("static", defaultColor);
        syntaxColors.put("throw", defaultColor);
        syntaxColors.put("typeof", defaultColor);
        syntaxColors.put("with", defaultColor);
        syntaxColors.put("boolean", defaultColor);
        syntaxColors.put("char", defaultColor);
        syntaxColors.put("default", defaultColor);
        syntaxColors.put("enum", defaultColor);
        syntaxColors.put("final", defaultColor);
        syntaxColors.put("goto", defaultColor);
        syntaxColors.put("instanceof", defaultColor);
        syntaxColors.put("native", defaultColor);
        syntaxColors.put("protected", defaultColor);
        syntaxColors.put("super", defaultColor);
        syntaxColors.put("throws", defaultColor);
        syntaxColors.put("var", defaultColor);
        syntaxColors.put("yield", defaultColor);
        syntaxColors.put("break", defaultColor);
        syntaxColors.put("class", defaultColor);
        syntaxColors.put("delete", defaultColor);
        syntaxColors.put("eval", defaultColor);
        syntaxColors.put("finally", defaultColor);
        syntaxColors.put("if", defaultColor);
        syntaxColors.put("int", defaultColor);
        syntaxColors.put("new", defaultColor);
        syntaxColors.put("public", defaultColor);
        syntaxColors.put("switch", defaultColor);
        syntaxColors.put("transient", defaultColor);
        syntaxColors.put("void", defaultColor);
        syntaxColors.put("byte", defaultColor);
        syntaxColors.put("const", defaultColor);
        syntaxColors.put("do", defaultColor);
        syntaxColors.put("export", defaultColor);
        syntaxColors.put("float", defaultColor);
        syntaxColors.put("implements", defaultColor);
        syntaxColors.put("interface", defaultColor);
        syntaxColors.put("null", defaultColor);
        syntaxColors.put("return", defaultColor);
        syntaxColors.put("synchronized", defaultColor);
        syntaxColors.put("true", defaultColor);
        syntaxColors.put("volatile", defaultColor);
        syntaxColors.put("isFinite", defaultColor);
        syntaxColors.put("NaN", defaultColor);
        syntaxColors.put("name", defaultColor);
        syntaxColors.put("eval", defaultColor);
        syntaxColors.put("function", defaultColor);
        syntaxColors.put("length", defaultColor);
        syntaxColors.put("valueOf", defaultColor);
        syntaxColors.put("hasOwnProperty", defaultColor);
        syntaxColors.put("toString", defaultColor);
        syntaxColors.put("const", defaultColor);

        for(String keyWord : syntaxColors.keySet())
        {
            ChatColor color = syntaxColors.get(keyWord);
            code = code.replace(" " + keyWord + " ", color + " " + keyWord + " " + ChatColor.RESET);
            code = code.replace(" " + keyWord, color + " " + keyWord + ChatColor.RESET);
            code = code.replace(keyWord + " ", color + keyWord + " " + ChatColor.RESET);
        }

        //Set class color, its not the best solution, because variables may also start with an uppercase name
        boolean classStart = false;
        boolean isString = false;
        char lastChar = 0;
        String tmp = "";
        for(char Char : code.toCharArray())
        {
            if(Char == '"' && lastChar != '\\')
            {
                isString = !isString;
            }
            if(!isString && ( !Character.isAlphabetic(lastChar) || lastChar == 0) && Character.isUpperCase(Char) && !classStart)
            {
                classStart = true;
            }

            if(!classStart || isString)
            {
                tmp += Char;
                continue;
            }

            if(!Character.isAlphabetic(Char))//if(Char == '.' || Char == ' ' || Char == ';' || Char == '+' || Char == '-' || Char == '*' || Char == ':' || Char == '/')
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
        for ( char Char : code.toCharArray() )
        {
            if ( Char == '"' && lastChar != '\\')
            {
                if ( !stringStart )
                {
                    tmp += stringColor;
                }

                tmp += Char;

                if ( stringStart )
                {
                    tmp += codeColor;
                }

                stringStart = !stringStart;
            }
            else
            {
                tmp += Char;
            }
            lastChar = Char;
        }

        return tmp;
    }

    @Override
    protected String getDefaultImports()
    {
        return ""; // Todo!
    }

    @Override
    public List<String> getImportIdentifier()
    {
        return new ArrayList<>(); // Todo!
    }
}
