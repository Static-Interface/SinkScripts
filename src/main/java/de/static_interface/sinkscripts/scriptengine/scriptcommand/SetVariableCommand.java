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

import de.static_interface.sinkscripts.scriptengine.scriptcontext.*;
import org.apache.commons.cli.*;
import org.bukkit.*;

import javax.annotation.*;

public class SetVariableCommand extends ScriptCommandBase {

    //Todo: Fix this

    public SetVariableCommand() {
        super("setvariable", false);
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl)
    throws Exception {
        String[] rawArgs = args;
        args = label.trim().replaceFirst("\\Q" + getName() + "\\E", "").split("=");
        String variableName = args[0];

        String[] rawValue = new String[rawArgs.length - 1];
        for (int i = 0; i < rawValue.length; i++) {
            rawValue[i] = rawArgs[i + 1];
        }

        Object value = context.getScriptLanguage().getValue(rawValue);
        context.getScriptLanguage().setVariable(context, variableName, value);
        context.getUser().sendMessage(
                ChatColor.BLUE + variableName + ChatColor.RESET + " has been successfully set to " + ChatColor.RED + value
                + ChatColor.RESET + " (" + ChatColor.BLUE + value.getClass().getSimpleName() + ")");

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
