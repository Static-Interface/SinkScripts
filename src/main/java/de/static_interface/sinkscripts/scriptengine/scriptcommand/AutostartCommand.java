/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
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
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class AutostartCommand extends ScriptCommandBase {

    public AutostartCommand() {
        super("autostart");
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl) throws Exception {
        if(cmdLine.hasOption('l')) {
            String languageNames = (String) cmdLine.getParsedOptionValue("l");

            List<ScriptLanguage> languages = new ArrayList<>();
            for(String languageName : languageNames.split(",")) {
                ScriptLanguage language = ScriptHandler.getInstance().getScriptLanguageByName(languageName);
                if (language == null) {
                    context.getUser().sendMessage(ChatColor.RED + "Warning: Unknown language: " + languageName);
                    continue;
                }
                languages.add(language);
            }

            for(ScriptLanguage language : languages) {
                language.onAutoStart(context);
            }
            return true;
        }

        SinkScripts.getInstance().loadAutoStart(context);
        return true;
    }

    @Override
    public boolean languageRequired() {
        return false;
    }

    @Nonnull
    @Override
    public Options buildOptions(Options parentOptions) {
        Option language = Option.builder("l")
                .hasArgs()
                .longOpt("language")
                .desc("Autostart only a specified language")
                .type(String.class)
                .argName("language")
                .build();
        parentOptions.addOption(language);
        return parentOptions;
    }
}
