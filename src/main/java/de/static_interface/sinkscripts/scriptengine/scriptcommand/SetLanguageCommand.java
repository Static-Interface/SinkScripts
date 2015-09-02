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

import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import org.apache.commons.cli.Options;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;

public class SetLanguageCommand extends ScriptCommandBase {

    public SetLanguageCommand() {
        super("setlanguage");
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl)
            throws Exception {
        ScriptLanguage newLanguage = null;
        args = cmdLine.getArgs();

        for (ScriptLanguage lang : ScriptHandler.getInstance().getScriptLanguages()) {
            if (lang.getName().equals(args[0]) || lang.getFileExtension().equals(args[0])) {
                newLanguage = lang;
                break;
            }
        }
        if (newLanguage == null) {
            context.getUser().sendMessage(ChatColor.DARK_RED + "Unknown language: " + args[0]);
            return true;
        }


        if(newLanguage.createExecutor(null) == null) {
            context.getUser().sendMessage(ChatColor.RED + "Couldn't create executor!");
            return true;
        }

        context.setScriptLanguage(newLanguage);
        context.getUser().sendMessage(ChatColor.GOLD + "Language has been set to: " + ChatColor.RED + newLanguage.getName());
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
        return "{COMMAND} <Language>";
    }
}
