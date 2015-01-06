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

import de.static_interface.sinklibrary.util.*;
import de.static_interface.sinkscripts.scriptengine.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.impl.*;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.*;
import de.static_interface.sinkscripts.util.Util;
import org.apache.commons.cli.*;
import org.bukkit.*;

import javax.annotation.*;

public class ExecuteCommand extends ScriptCommandBase {

    public ExecuteCommand() {
        super("execute");
    }

    @Override
    @Nonnull
    public Options buildOptions(Options parentOptions) {
        Option async = Option.builder("a")
                .desc("Execute async")
                .longOpt("async")
                .build();

        Option file = Option.builder("f")
                .hasArgs()
                .longOpt("file")
                .desc("Execute file")
                .type(String.class)
                .argName("file")
                .build();

        Option skipoutput = Option.builder("s")
                .longOpt("skipoutput")
                .desc("Skip return output")
                .build();

        Option clear = Option.builder("c")
                .longOpt("clear")
                .desc("Clear history after execute")
                .build();

        Option noimports = Option.builder("n")
                .longOpt("noimports")
                .desc("No default imports")
                .build();

        parentOptions.addOption(async);
        parentOptions.addOption(file);
        parentOptions.addOption(skipoutput);
        parentOptions.addOption(clear);
        parentOptions.addOption(noimports);

        return parentOptions;
    }

    @Override
    protected boolean onExecute(ScriptContext context, String[] args, String label, String nl) throws Exception {

        boolean noImports = cmdLine.hasOption('n');
        boolean clear = cmdLine.hasOption('c');
        boolean skipOutput = cmdLine.hasOption('s');

        ScriptLanguage contextLanguage = context.getScriptLanguage();
        String scriptName = null;
        if(cmdLine.hasOption('f')) {
            scriptName = cmdLine.getOptionValue('f').trim();
        }

        String result;

        if(scriptName != null) {
            String tmp[] = scriptName.split("\\Q.\\E");

            if (tmp.length < 1 && contextLanguage == null) {
                throw new IllegalStateException("Couldn't find extension/language for script file: " + scriptName);
            }

            if(tmp.length > 1) {
                String extension = tmp[tmp.length-1].trim();
                scriptName = StringUtil.formatArrayToString(tmp, ".", 0, tmp.length-1).trim(); // remove extension
                ScriptLanguage
                        tmpLanguage =
                        ScriptHandler.getScriptLanguageByExtension(extension); // get language by extension
                if (tmpLanguage == null && contextLanguage == null) {
                    context.getUser().sendMessage(ChatColor.DARK_RED
                                                  + "Language not set and/or invalid file extension! Use .execute <file.extension> or .setlanguage <language>");
                    return true;
                }

                if (tmpLanguage != null) {
                    contextLanguage = tmpLanguage;
                }
            }
        }

        if(contextLanguage == null || ScriptHandler.getLanguage(context.getUser()) == null) {
            context.getUser().sendMessage(ChatColor.RED + "Language not set! Use .setlanguage <language>");
            return true;
        }

        if(context instanceof DummyContext) {
            String code = context.getCode();
            context = ScriptHandler.getLanguage(context.getUser()).createNewShellInstance(context.getUser());
            context.setCode(code);
        }

        ScriptHandler.setVariables(context);

        String code = context.getCode();
        if(scriptName != null) {
            code = Util.loadFile(scriptName, contextLanguage);
        }

        result =
                String.valueOf(contextLanguage
                                       .run(context, code, noImports,
                                            clear));


        if (!skipOutput) {
            context.getUser().sendMessage(ChatColor.AQUA + "Output: " + ChatColor.GREEN + contextLanguage.formatCode(result));
        }

        return true;
    }

    @Override
    public boolean languageRequired() {
        return false;
    }
}
