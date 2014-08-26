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

package de.static_interface.sinkscripts.scriptengine;

import de.static_interface.sinkscripts.ScriptUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.List;

public class JavaScript extends ScriptLanguage
{
    public JavaScript(Plugin plugin)
    {
        super(plugin, "javascript", "js");
    }

    @Override
    public String formatCode(String code)
    {
        return code; // Todo
    }

    @Override
    public Object runCode(ShellInstance instance, String code)
    {
        JavaScriptShellInstance jsInstance = (JavaScriptShellInstance) instance;
        String tmp= "";
        String out;
        int i = 1;
        String[] lines = code.split(ScriptUtil.getNewLine());
        for(String line : lines)
        {
            if(lines.length == i -1)
            {
                if(line.startsWith("return")) line = line.replaceFirst("return", "");
            }
            tmp += line;
            i++;
        }

        out = ((Context)jsInstance.getExecutor()).evaluateString(jsInstance.getScope(), tmp, instance.getSender().getName()
                , i, null) + ScriptUtil.getNewLine();

        if(out.startsWith("org.mozilla.javascript.Undefined")) return null; // VERY BAD
        return out;
    }

    @Override
    protected String getDefaultImports()
    {
        return ""; // Todo!
    }

    @Override
    public ShellInstance createNewShellInstance(CommandSender sender)
    {
        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();
        return new JavaScriptShellInstance(sender, cx, scope);
    }

    @Override
    public void setVariable(ShellInstance instance, String name, Object value)
    {
        JavaScriptShellInstance jsInstance = (JavaScriptShellInstance) instance;
        Scriptable scope = jsInstance.getScope();
        value = Context.javaToJS(value, scope);
        ScriptableObject.putProperty(scope, name, value);
    }

    @Override
    public List<String> getImportIdentifier()
    {
        List<String> importIdentifiers = new ArrayList<>();
        importIdentifiers.add("importPackage");
        importIdentifiers.add("importClass");
        return importIdentifiers;
    }
}
