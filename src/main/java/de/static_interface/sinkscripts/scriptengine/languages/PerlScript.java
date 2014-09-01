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

public class PerlScript extends ScriptEngineScript
{
    public PerlScript(Plugin plugin)
    {
        super(plugin, "perl", "pl", "jerl");
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

        syntaxColors.put("package", ChatColor.GOLD);
        syntaxColors.put("import", ChatColor.GOLD);

        syntaxColors.put("__DATA__", defaultColor);
        syntaxColors.put("__END__", defaultColor);
        syntaxColors.put("__FILE__", defaultColor);
        syntaxColors.put("__LINE__", defaultColor);
        syntaxColors.put("__PACKAGE__", defaultColor);
        syntaxColors.put("and", defaultColor);
        syntaxColors.put("cmp", defaultColor);
        syntaxColors.put("continue", defaultColor);
        syntaxColors.put("CORE", defaultColor);
        syntaxColors.put("do", defaultColor);
        syntaxColors.put("else", defaultColor);
        syntaxColors.put("elsif", defaultColor);
        syntaxColors.put("eq", defaultColor);
        syntaxColors.put("exp", defaultColor);
        syntaxColors.put("for", defaultColor);
        syntaxColors.put("foreach", defaultColor);
        syntaxColors.put("ge", defaultColor);
        syntaxColors.put("gt", defaultColor);
        syntaxColors.put("if", defaultColor);
        syntaxColors.put("le", defaultColor);
        syntaxColors.put("lock", defaultColor);
        syntaxColors.put("lt", defaultColor);
        syntaxColors.put("m", defaultColor);
        syntaxColors.put("ne", defaultColor);
        syntaxColors.put("no", defaultColor);
        syntaxColors.put("or", defaultColor);
        syntaxColors.put("package", defaultColor);
        syntaxColors.put("q", defaultColor);
        syntaxColors.put("qq", defaultColor);
        syntaxColors.put("qr", defaultColor);
        syntaxColors.put("qw", defaultColor);
        syntaxColors.put("qx", defaultColor);
        syntaxColors.put("s", defaultColor);
        syntaxColors.put("sub", defaultColor);
        syntaxColors.put("tr", defaultColor);
        syntaxColors.put("unless", defaultColor);
        syntaxColors.put("until", defaultColor);
        syntaxColors.put("while", defaultColor);
        syntaxColors.put("xor", defaultColor);
        syntaxColors.put("y", defaultColor);

        for(String keyWord : syntaxColors.keySet())
        {
            ChatColor color = syntaxColors.get(keyWord);
            code = code.replace(" " + keyWord + " ", color + " " + keyWord + " " + ChatColor.RESET);
            code = code.replace(" " + keyWord, color + " " + keyWord + ChatColor.RESET);
            code = code.replace(keyWord + " ", color + keyWord + " " + ChatColor.RESET);
        }

        tmp = "";

        char startChar = 0;
        boolean resetColor = false;
        //Set String color
        for ( char Char : code.toCharArray() )
        {
            if(Char == startChar)
            {
                tmp += Char;
                startChar = 0;
                resetColor = true;
            }
            else if (Char == '\'' || Char == '"' || (Char == '^' && lastChar == 'q'))
            {
                if(startChar == 0 && Char != ']')
                {
                    startChar = Char;
                }

                tmp += stringColor;
                tmp += Char;
            }
            else if(resetColor)
            {
                tmp += codeColor;
                tmp+= Char;
                resetColor = false;
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
        return ""; //Todo
    }

    @Override
    public List<String> getImportIdentifier()
    {
        List<String> identifiers = new ArrayList<>();
        identifiers.add("import");
        return identifiers;
    }
}
