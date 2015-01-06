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

public class SaveCommand extends ScriptCommandBase {

    public SaveCommand() {
        super("save");
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl) {

        if(cmdLine.getArgs().length < 1) {
            return false;
        }

        String scriptName = cmdLine.getArgs()[0];
        File scriptFile;

        if (cmdLine.hasOption('a')) {
            scriptFile = new File(context.getScriptLanguage().AUTOSTART_DIRECTORY, scriptName + "." + context.getScriptLanguage().getFileExtension());
        } else {
            scriptFile = new File(context.getScriptLanguage().SCRIPTLANGUAGE_DIRECTORY, scriptName + "." + context.getScriptLanguage().getFileExtension());
        }

        if (scriptFile.exists()) {
            if (!scriptFile.delete()) {
                throw new RuntimeException("Couldn't override " + scriptFile + " (File.delete() returned false)!");
            }
            return true;
        }
        PrintWriter writer;
        try {
            writer = new PrintWriter(scriptFile, "UTF-8");
        } catch (Exception e) {
            Util.reportException(context.getUser(), e);
            return true;
        }
        writer.write(context.getCode());
        writer.close();
        context.getUser().sendMessage(ChatColor.DARK_GREEN + "Code saved!");
        return true;
    }

    @Override
    public boolean languageRequired() {
        return true;
    }

    @Nonnull
    @Override
    public Options buildOptions(Options parentOptions) {
        parentOptions.addOption(Option.builder("a")
                                .longOpt("autostart")
                                .desc("Set as autostart script")
                                        .build());
        return parentOptions;
    }

    @Override
    @Nonnull
    public String getSyntax() {
        return "{COMMAND} [Options] <File>";
    }
}
