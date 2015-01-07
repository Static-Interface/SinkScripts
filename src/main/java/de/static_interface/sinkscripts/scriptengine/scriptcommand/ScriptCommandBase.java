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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.bukkit.ChatColor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ScriptCommandBase {
    private static Map<String, ScriptCommandBase> scriptCommands = new HashMap<>();
    private final String name;
    private Options options;
    private CommandLineParser parser = new DefaultParser();
    protected CommandLine cmdLine;
    private HelpFormatter cliHelpFormatter;

    public static void registerCommand(ScriptCommandBase command) {
        scriptCommands.put(command.getName(), command);
    }

    public ScriptCommandBase(String name) {
        this.name = name;
        initOptions();
    }

    public ScriptCommandBase(String name, boolean useCli) {
        this.name = name;
        if(useCli) {
            initOptions();
        }
    }

    private void initOptions(){
        options = new Options();
        options.addOption(Option.builder("h")
                                  .desc("Shows this message")
                                  .longOpt("help")
                                  .build());

        options = buildOptions(options);
    }

    public final void onPreExecute(ScriptContext context, String[] args, String label, String nl) throws Exception {
        if(options != null) {
            cmdLine = parser.parse(options, args);
            args = cmdLine.getArgs();
        }

        if(options != null && options.hasOption("h") && cmdLine.hasOption('h')) {
            context.getUser().sendMessage(getUsage());
            return;
        }

        if(languageRequired() && context.getScriptLanguage() == null) {
            context.getUser().sendMessage(ChatColor.RED + "Language not set! Use .setlanguage <language>");
            return;
        }

        boolean result = onExecute(context, args, label, nl);

        if(!result) {
            context.getUser().sendMessage(getUsage());
            return;
        }

        ScriptHandler.getInstance().getScriptContexts().put(ScriptHandler.getInstance().userToKey(context.getUser()), context);
    }

    protected abstract boolean onExecute(ScriptContext context, String[] args, String label, String nl) throws Exception;

    public abstract boolean languageRequired();


    public String getUsage() {
        if (options != null) {
            StringWriter writer = new StringWriter();
            getCliHelpFormatter(writer);
            return writer.toString();
        }
        return "";
    }

    public HelpFormatter getCliHelpFormatter(Writer writer) {
        if (cliHelpFormatter == null) {
            cliHelpFormatter = new HelpFormatter();
            cliHelpFormatter.setNewLine(System.lineSeparator());
            cliHelpFormatter.printHelp(new PrintWriter(writer), HelpFormatter.DEFAULT_WIDTH,
                                       getSyntax()
                                               .replaceAll("\\{COMMAND\\}", getName()), null, options,
                                       HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
        }
        return cliHelpFormatter;
    }

    @Nonnull
    public abstract Options buildOptions(Options parentOptions);

    @Nonnull
    public final String getName() {
        return name;
    }

    @Nullable
    public static ScriptCommandBase get(String name) {
        return scriptCommands.get(name);
    }

    @Nonnull
    public String getSyntax() {
        return "{COMMAND} <Options>";
    }
}
