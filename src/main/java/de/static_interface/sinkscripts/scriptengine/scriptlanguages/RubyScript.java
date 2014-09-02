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

package de.static_interface.sinkscripts.scriptengine.scriptlanguages;

import de.static_interface.sinkscripts.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RubyScript extends ScriptEngineScript
{
    public RubyScript(Plugin plugin)
    {
        super(plugin, "ruby", "rb", "jruby");
    }

    @Override
    public String formatCode(String code)
    {
        ChatColor defaultColor = ChatColor.DARK_BLUE;
        ChatColor codeColor = ChatColor.RESET;
        ChatColor classColor = ChatColor.BLUE;
        ChatColor stringColor = ChatColor.RED;

        //Set class color, its not the best solution, because variables may also start with an uppercase name
        boolean classStart = false;
        char lastChar = 0;
        String tmp = "";
        for(char Char : code.toCharArray())
        {
            boolean t = false;
            if(!Character.isAlphabetic(lastChar) && Character.isUpperCase(Char) && !classStart)
            {
                classStart = true;
                t = true;
            }

            if(!classStart)
            {
                tmp += Char;
                continue;
            }

            if(!Character.isAlphabetic(Char) && !t)//if(Char == '.' || Char == ' ' || Char == ';' || Char == '+' || Char == '-' || Char == '*' || Char == ':' || Char == '/')
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
        syntaxColors.put("import", ChatColor.GOLD);
        syntaxColors.put("package", ChatColor.GOLD);
        syntaxColors.put("BEGIN", defaultColor);
        syntaxColors.put("END", defaultColor);
        syntaxColors.put("__ENCODING__", defaultColor);
        syntaxColors.put("__END__", defaultColor);
        syntaxColors.put("__FILE__", defaultColor);
        syntaxColors.put("__LINE__", defaultColor);
        syntaxColors.put("alias", defaultColor);
        syntaxColors.put("and", defaultColor);
        syntaxColors.put("begin", defaultColor);
        syntaxColors.put("break", defaultColor);
        syntaxColors.put("case", defaultColor);
        syntaxColors.put("class", defaultColor);
        syntaxColors.put("def", defaultColor);
        syntaxColors.put("defined?", defaultColor);
        syntaxColors.put("do", defaultColor);
        syntaxColors.put("else", defaultColor);
        syntaxColors.put("elsif", defaultColor);
        syntaxColors.put("end", defaultColor);
        syntaxColors.put("ensure", defaultColor);
        syntaxColors.put("false", ChatColor.GOLD);
        syntaxColors.put("for", defaultColor);
        syntaxColors.put("if", defaultColor);
        syntaxColors.put("in", defaultColor);
        syntaxColors.put("module", defaultColor);
        syntaxColors.put("next", defaultColor);
        syntaxColors.put("nil", ChatColor.GOLD);
        syntaxColors.put("not", defaultColor);
        syntaxColors.put("or", defaultColor);
        syntaxColors.put("redo", defaultColor);
        syntaxColors.put("rescue", defaultColor);
        syntaxColors.put("retry", defaultColor);
        syntaxColors.put("return", defaultColor);
        syntaxColors.put("self", defaultColor);
        syntaxColors.put("super", defaultColor);
        syntaxColors.put("then", defaultColor);
        syntaxColors.put("true", ChatColor.GOLD);
        syntaxColors.put("undef", defaultColor);
        syntaxColors.put("unless", defaultColor);
        syntaxColors.put("until", defaultColor);
        syntaxColors.put("when", defaultColor);
        syntaxColors.put("while", defaultColor);
        syntaxColors.put("yield", defaultColor);

        syntaxColors.put("java_import", ChatColor.GOLD);
        syntaxColors.put("import", ChatColor.GOLD);
        syntaxColors.put("include", ChatColor.GOLD);
        syntaxColors.put("include_class", ChatColor.GOLD);
        syntaxColors.put("include_package", ChatColor.GOLD);
        syntaxColors.put("require", ChatColor.GOLD);
        syntaxColors.put("package_name", ChatColor.GOLD);

        for(String keyWord : syntaxColors.keySet())
        {
            ChatColor color = syntaxColors.get(keyWord);
            code = code.replace(" " + keyWord + " ", color + " " + keyWord + " " + ChatColor.RESET);
            code = code.replace(" " + keyWord, color + " " + keyWord + ChatColor.RESET);
            code = code.replace(keyWord + " ", color + keyWord + " " + ChatColor.RESET);
        }

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
        String nl = Util.getNewLine();
        return  "require 'java'" + nl +

                "module SinkLibrary" + nl +
                "    include_package \"de.static_interface.sinklibrary\"" + nl +
                "end" + nl + nl +

                "module SinkScripts" + nl +
                "    include_package \"de.static_interface.sinkscripts\"" + nl +
                "end" + nl + nl +

                "module Bukkit" + nl +
                "    include_package \"org.bukkit.block\"" + nl +
                "    include_package \"org.bukkit.event\"" + nl +
                "    include_package \"org.bukkit.entity\"" + nl +
                "    include_package \"org.bukkit.inventory\"" + nl +
                "    include_package \"org.bukkit.material\"" + nl +
                "    include_package \"org.bukkit.potion\"" + nl +
                "    include_package \"org.bukkit.util\"" + nl +
                "    include_package \"org.bukkit\"" + nl +
                "end" + nl +nl;
    }

    @Override
    public List<String> getImportIdentifier()
    {
        List<String> importIdentifiers = new ArrayList<>();
        importIdentifiers.add("java_import");
        importIdentifiers.add("import");
        importIdentifiers.add("include");
        importIdentifiers.add("include_class");
        importIdentifiers.add("require");
        importIdentifiers.add("package_name");
        return importIdentifiers;
    }

    @Override
    public void onInit()
    {
        System.setProperty("org.jruby.embed.compilemode", "OFF");
        System.setProperty("jruby.compile.mode", "OFF");
    }
}
