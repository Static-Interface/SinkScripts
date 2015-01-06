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

import de.static_interface.sinklibrary.api.user.*;
import de.static_interface.sinkscripts.scriptengine.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.impl.*;
import org.apache.commons.cli.*;
import org.bukkit.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

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
        options = new Options();
        options.addOption(Option.builder("h")
                                  .desc("Shows this message")
                                  .longOpt("help")
                                  .build());

        options = buildOptions(options);
    }

    public final void onPreExecute(SinkUser user, String[] args, String label, String nl) throws Exception{
        cmdLine = parser.parse(options, args);

        ScriptContext context = ScriptHandler.getScriptContexts().get(ScriptHandler.userToKey(user));
        if(context instanceof DummyContext && ScriptHandler.getLanguage(context.getUser()) != null) {
            String code = context.getCode();
            context = ScriptHandler.getLanguage(context.getUser()).createNewShellInstance(context.getUser());
            context.setCode(code);
        }

        if(cmdLine.hasOption('h')) {
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

        ScriptHandler.getScriptContexts().put(ScriptHandler.userToKey(user), context);
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
