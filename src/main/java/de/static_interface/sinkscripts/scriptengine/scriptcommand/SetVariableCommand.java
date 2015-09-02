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

package de.static_interface.sinkscripts.scriptengine.scriptcommand;

import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import org.apache.commons.cli.Options;
import org.bukkit.ChatColor;

import java.util.Arrays;

import javax.annotation.Nonnull;

public class SetVariableCommand extends ScriptCommandBase {

    //Todo: Fix this

    public SetVariableCommand() {
        super("setvariable", false);
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl)
    throws Exception {
        String argsLine = "";
        for(String s : args){
            if(argsLine.equals("")) {
                argsLine = s;
                continue;
            }
            argsLine += " " + s;
        }

        args = argsLine.split("=");
        String variableName = args[0];

        String[] valueArgs = new String[args.length - 1];
        for (int i = 0; i < valueArgs.length; i++) {
            valueArgs[i] = args[i + 1];
        }

        Object value = context.getScriptLanguage().getValue(valueArgs);

        String shownValue = String.valueOf(value);
        if(value instanceof Object[]){
            shownValue = Arrays.toString((Object[])value);
        }
        context.getScriptLanguage().setVariable(context, variableName, value);
        context.getUser().sendMessage(
                ChatColor.BLUE + variableName + ChatColor.RESET + " has been set to " + ChatColor.RED + shownValue
                + ChatColor.RESET + " (" + ChatColor.BLUE + value.getClass().getSimpleName() + ChatColor.DARK_BLUE + ".class" + ChatColor.RESET + ")");

        return true;
    }

    @Override
    public boolean languageRequired() {
        return false;
    }

    @Nonnull
    @Override
    public Options buildOptions(Options parentOptions) {
        return null;
    }

    @Override
    @Nonnull
    public String getSyntax() {
        return "{COMMAND} <variablename>=<value>, example: .{COMMAND} mynumber=5";
    }
}
