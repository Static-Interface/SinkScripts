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

import de.static_interface.sinkscripts.scriptengine.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.*;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.*;
import org.apache.commons.cli.*;
import org.bukkit.*;

import javax.annotation.*;

public class ListLanguageCommand extends ScriptCommandBase {

    public ListLanguageCommand() {
        super("listlanguages");
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl)
            throws Exception {
        String languages = "";

        for (ScriptLanguage lang : ScriptHandler.getScriptLanguages()) {
            if (languages.equals("")) {
                languages = lang.getName();
                continue;
            }

            languages += ", " + lang.getName();
        }

        context.getUser().sendMessage(ChatColor.GOLD + "Available script languages: " + ChatColor.RESET + languages);

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
        return "{COMMAND}";
    }
}
