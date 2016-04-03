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

import de.static_interface.sinkscripts.SinkScripts;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.util.Util;
import org.apache.commons.cli.Options;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;

public class RunInjectionCommand extends ScriptCommandBase {

    public RunInjectionCommand() {
        super("inject");
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl) {
        args = cmdLine.getArgs();

        if (args.length < 1) {
            return false;
        }

        String injectName = args[0];
        try {
            SinkScripts.getInstance().runInjection(injectName);
        } catch (Exception e) {
            Util.reportException(context.getUser(), e);
            e.printStackTrace();
            return true;
        }

        context.getUser().sendMessage(ChatColor.DARK_GREEN + "Done");
        return true;
    }

    @Override
    public boolean languageRequired() {
        return false;
    }

    @Nonnull
    @Override
    public Options buildOptions(Options parentOptions) {
        return parentOptions;
    }

    @Override
    @Nonnull
    public String getSyntax() {
        return "{COMMAND} <File>";
    }
}
