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
import de.static_interface.sinkscripts.util.Util;
import org.apache.commons.cli.*;
import org.bukkit.*;

import java.io.*;

import javax.annotation.*;

public class LoadCommand extends ScriptCommandBase {

    public LoadCommand() {
        super("load");
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl) {
        args = cmdLine.getArgs();

        if (args.length < 1) {
            return false;
        }

        String scriptName = args[0];

        try {
            context.setCode(Util.loadFile(scriptName, context.getScriptLanguage()) + context.getCode());
        } catch (FileNotFoundException ignored) {
            context.getUser().sendMessage(ChatColor.DARK_RED + "File doesn't exists!");
            return true;
        } catch (Exception e) {
            Util.reportException(context.getUser(), e);
            return true;
        }
        context.getUser().sendMessage(ChatColor.DARK_GREEN + "File loaded");

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
