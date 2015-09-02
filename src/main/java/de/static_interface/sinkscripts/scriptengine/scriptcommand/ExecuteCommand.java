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

import de.static_interface.sinklibrary.util.StringUtil;
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import de.static_interface.sinkscripts.util.Util;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.bukkit.ChatColor;

import java.util.Arrays;

import javax.annotation.Nonnull;

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

        String scriptName = null;
        if(cmdLine.hasOption('f')) {
            scriptName = cmdLine.getOptionValue('f').trim();
        }

        Object result;

        if(scriptName != null) {
            String tmp[] = scriptName.split("\\Q.\\E");

            if (tmp.length < 1 && context.getScriptLanguage() == null) {
                throw new IllegalStateException("Couldn't find extension/language for script file: " + scriptName);
            }

            if(tmp.length > 1) {
                String extension = tmp[tmp.length-1].trim();
                scriptName = StringUtil.formatArrayToString(tmp, ".", 0, tmp.length-1).trim(); // remove extension
                ScriptLanguage
                        extensionLanguage =
                        ScriptHandler.getInstance().getScriptLanguageByExtension(extension); // get language by extension

                if (extensionLanguage == null) {
                    context.getUser().sendMessage(ChatColor.DARK_RED
                                                  + "Unknown file extension: " + extension);
                    return true;
                }

                // Use a new context when -f is defined
                context = new ScriptContext(context.getUser(), extensionLanguage, context.getPlugin());
            }
        }

        if(context.getScriptLanguage() == null ) {
            context.getUser().sendMessage(ChatColor.RED + "Couldn't execute code: Language not set! Use .setlanguage <language>");
            return true;
        }

        ScriptHandler.getInstance().setDefaultVariables(context);

        String code = context.getCode();
        if(scriptName != null) {
            code = Util.loadFile(scriptName, context.getScriptLanguage());
        }

        try {
            result =
                    context.getScriptLanguage().run(context, code, noImports,
                                 clear);
        } catch (Throwable throwable) {
            Util.reportException(context.getUser(), throwable);
            return true;
        }

        if(result instanceof Object[]) {
            result = Arrays.asList((Object[])result);
        }

        if (!skipOutput) {
            context.getUser().sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Return Output: " + ChatColor.RESET + "" + ChatColor.BLUE + context.getScriptLanguage().formatCode(String.valueOf(result)));
        }

        return true;
    }

    @Override
    public boolean languageRequired() {
        return false;
    }
}
