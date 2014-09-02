/*
 * Copyright (c) 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkscripts.scriptengine;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.irc.IrcCommandSender;
import de.static_interface.sinkscripts.scriptengine.shellinstances.DummyShellInstance;
import de.static_interface.sinkscripts.scriptengine.shellinstances.ShellInstance;
import de.static_interface.sinkscripts.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class ScriptHandler
{
    private static ArrayList<String> enabledUsers = new ArrayList<>();
    private static HashMap<String, ScriptLanguage> scriptLanguages = new HashMap<>();
    protected static  HashMap<String, ShellInstance> shellInstances = new HashMap<>(); // PlayerName - Shell Instance
    private static HashMap<String, ScriptLanguage> languageInstances = new HashMap<>();

    public static Collection<ScriptLanguage> getScriptLanguages()
    {
        return scriptLanguages.values();
    }

    public static ScriptLanguage getScriptLanguageByExtension(String ext)
    {
        return scriptLanguages.get(ext);
    }

    public static ScriptLanguage getScriptLanguageByName(String name)
    {
        for(ScriptLanguage language : getScriptLanguages())
        {
            if(language.getName().equalsIgnoreCase(name)) return language;
        }
        return null;
    }

    public static boolean isEnabled(CommandSender sender)
    {
        return enabledUsers.contains(getInternalName(sender));
    }

    public static void setEnabled(CommandSender sender, boolean enabled)
    {
        if(enabled)
            enabledUsers.add(getInternalName(sender));
        else
            enabledUsers.remove(getInternalName(sender));
    }

    public static String getInternalName(CommandSender sender)
    {
        return sender instanceof IrcCommandSender ? "IRC_" + sender.getName() : sender.getName();
    }

    public static void register(ScriptLanguage scriptLanguage)
    {
        scriptLanguages.put(scriptLanguage.getFileExtension(), scriptLanguage);
        scriptLanguage.preInit();
    }

    volatile static ShellInstance shellInstance;
    public static void handleLine(final CommandSender sender, final String line, final Plugin plugin)
    {
        final String name = ScriptHandler.getInternalName(sender);
        final List<String> availableParamters = new ArrayList<>();
        availableParamters.add("--async");
        availableParamters.add("--skipoutput");
        availableParamters.add("--clear");
        availableParamters.add("--noimports");
        boolean isExecute = line.startsWith(".execute");
        boolean async = isExecute && line.contains(" --async");
        final boolean skipOutput = isExecute && (line.contains(" --skipoutput"));
        final boolean clear = isExecute && (line.contains(" --clear"));
        final boolean noImports = isExecute && (line.contains(" --noimports"));

        boolean isSave = line.startsWith(".save");
        final boolean isAutostartSave = isSave && line.contains(" --autostart");

        final ScriptLanguage language = getLanguage(sender);
        if(language == null && (!line.startsWith(".setlanguage") && !line.startsWith(".help") && !line.startsWith(".listlanguages")))
        {
            sender.sendMessage("Language not set! Use .setlanguage <language>");
            return;
        }
        if(sender instanceof ConsoleCommandSender )
        {
            shellInstance = language.getConsoleShellInstance();
        }
        else
            shellInstance = shellInstances.get(ScriptHandler.getInternalName(sender));
        if(shellInstance == null)
        {
            SinkLibrary.getCustomLogger().log(Level.INFO, "Initializing ShellInstance for " + sender.getName());

            if(language == null)
                shellInstance = new DummyShellInstance(sender, null); // Used for .setLanguage
            else
                shellInstance = language.createNewShellInstance(sender);

            shellInstances.put(ScriptHandler.getInternalName(sender), shellInstance);
        }

        // Still null?!
        if(shellInstance == null) throw new IllegalStateException("Couldn't create shellInstance!");

        Runnable runnable = new Runnable()
        {
            String nl = Util.getNewLine();
            String code = "";

            @SuppressWarnings("ConstantConditions")
            public void run()
            {
                try
                {
                    String currentLine = line;
                    String[] args = currentLine.split(" ");
                    String mode = args[0].toLowerCase();

                    boolean codeSet = false;

                    if ( shellInstances.get(name).getCode() == null )
                    {
                        shellInstance.setCode(currentLine);
                        code = currentLine;
                        codeSet = true;
                    }

                    //dont use new line if line starts with "<" (e.g. if the line is too long or you want to add something to it)
                    boolean useNl = !currentLine.startsWith("<");
                    if ( !useNl )
                    {
                        currentLine = currentLine.replaceFirst("<", "");
                        nl = "";
                    }

                    if ( currentLine.startsWith(".") ) // command, don't add to code
                    {
                        code = code.replace(currentLine, "");
                        currentLine = "";
                    }

                    String prevCode = shellInstance.getCode();

                    if ( !codeSet )
                    {
                        boolean isImport = false;
                        if (language != null && language.getImportIdentifier() != null)
                        {
                            for ( String s : language.getImportIdentifier() )
                            {
                                if ( language != null && currentLine.startsWith(s) )
                                {
                                    code = currentLine + nl + prevCode;
                                    isImport = true;
                                    break;
                                }
                            }
                        }
                        if (!isImport) code = prevCode + nl + currentLine;
                        shellInstance.setCode(code);
                    }
                    SinkLibrary.getUser(sender).sendDebugMessage(ChatColor.GOLD + "Script mode: " + ChatColor.RED + mode);

                    /* Todo:
                     * add permissions for commands, e.g. sinkscripts.use.help, sinkscripts.use.executefile etc...
                     */
                    switch ( mode )
                    {
                        case ".help":
                            sender.sendMessage(ChatColor.GREEN + "[Help] " + ChatColor.GRAY + "Available Commands: .help, .load <file>, " +
                                    ".save <file>, .execute [file], .setvariable <name> <value>, .history, .clear, .setlanguage <language>");
                            break;

                        case ".setlanguage":
                            ScriptLanguage newLanguage = null;
                            for(ScriptLanguage lang : ScriptHandler.getScriptLanguages())
                            {
                                if( lang.getName().equals(args[1]) || lang.getFileExtension().equals(args[1]) )
                                {
                                    newLanguage = lang;
                                    break;
                                }
                            }
                            if ( newLanguage == null )
                            {
                                sender.sendMessage("Unknown language: " + args[1]);
                                break;
                            }
                            if ( sender instanceof ConsoleCommandSender )
                            {
                                shellInstance = language.getConsoleShellInstance();
                            }
                            else
                            {
                                shellInstance = newLanguage.createNewShellInstance(sender);
                            }
                            setLanguage(sender, newLanguage);
                            sender.sendMessage(ChatColor.GOLD + "Language has been set to: " + ChatColor.RED + newLanguage.getName());
                            break;

                        case ".clear":
                            shellInstance.setCode("");
                            sender.sendMessage(ChatColor.DARK_RED + "History cleared");
                            break;

                        case ".listlanguages":
                            String languages = "";

                            for(ScriptLanguage language : ScriptHandler.getScriptLanguages() )
                            {
                                if(languages.equals(""))
                                {
                                    languages = language.getName();
                                    continue;
                                }

                                languages += ", " + language.getName();
                            }

                            sender.sendMessage(ChatColor.GOLD + "Available script languages: " + ChatColor.RESET + languages);
                            break;

                        case ".setvariable":
                            //if ( args.length < 1 || !currentLine.contains("=") )
                            //{
                            //    sender.sendMessage(ChatColor.DARK_RED + "Usage: .setvariable name=value");
                            //    break;
                            //}
                            try
                            {
                                String[] commandArgs = line.split("=");
                                String variableName = commandArgs[0].split(" ")[1];
                                Object value = language.getValue(commandArgs);
                                language.setVariable(shellInstance, variableName, value);
                                sender.sendMessage(ChatColor.BLUE + variableName + ChatColor.RESET +" has been successfully set to " + ChatColor.RED + value + ChatColor.RESET +" (" + value.getClass().getSimpleName() + ")");
                            }
                            catch ( Exception e )
                            {
                                Util.reportException(sender, e);
                            }
                            break;

                        case ".load":
                        {
                            if ( args.length < 2 )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "Too few arguments! .load <File>");
                                break;
                            }
                            String scriptName = args[1];

                            try
                            {
                                shellInstance.setCode(Util.loadFile(scriptName, language) + code);
                            }
                            catch ( FileNotFoundException ignored )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "File doesn't exists!");
                                break;
                            }
                            catch ( Exception e )
                            {
                                Util.reportException(sender, e);
                                break;
                            }
                            sender.sendMessage(ChatColor.DARK_GREEN + "File loaded");
                            break;
                        }

                        case ".save":
                            if ( args.length < 2 )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "Too few arguments! .save <File>");
                                break;
                            }
                            String scriptName = args[1];
                            File scriptFile;

                            if(isAutostartSave)
                                scriptFile= new File(language.AUTOSTART_DIRECTORY, scriptName + "." + language.getFileExtension());
                            else
                                scriptFile= new File(language.SCRIPTLANGUAGE_DIRECTORY, scriptName + "." + language.getFileExtension());

                            if ( scriptFile.exists() )
                            {
                                if ( !scriptFile.delete() ) throw new RuntimeException("Couldn't override " + scriptFile + " (File.delete() returned false)!");
                                break;
                            }
                            PrintWriter writer;
                            try
                            {
                                writer = new PrintWriter(scriptFile, "UTF-8");
                            }
                            catch ( Exception e )
                            {
                                Util.reportException(sender, e);
                                break;
                            }
                            writer.write(code);
                            writer.close();
                            sender.sendMessage(ChatColor.DARK_GREEN + "Code saved!");
                            break;

                        case ".execute":
                            language.setVariable(shellInstance, "me", SinkLibrary.getUser(sender));
                            language.setVariable(shellInstance, "plugin", plugin);
                            language.setVariable(shellInstance, "server", Bukkit.getServer());
                            language.setVariable(shellInstance, "players", Bukkit.getOnlinePlayers());
                            language.setVariable(shellInstance, "users", SinkLibrary.getOnlineUsers());
                            language.setVariable(shellInstance, "sender", sender);
                            language.setVariable(shellInstance, "language", language);

                            if ( sender instanceof Player )
                            {
                                Player player = (Player) sender;
                                BlockIterator iterator = new BlockIterator(player);
                                language.setVariable(shellInstance, "player", player);
                                language.setVariable(shellInstance, "at", iterator.next());
                                language.setVariable(shellInstance, "x", player.getLocation().getX());
                                language.setVariable(shellInstance, "y", player.getLocation().getY());
                                language.setVariable(shellInstance, "z", player.getLocation().getZ());
                            }

                            try
                            {
                                boolean isParameter = false;
                                if(args.length > 1)
                                {
                                    for ( String s : availableParamters )
                                    {
                                        if ( s.equals(args[1]) )
                                        {
                                            isParameter = true;
                                            break;
                                        }
                                    }
                                }
                                if ( args.length >= 2 && !isParameter )
                                {
                                    code = Util.loadFile(args[1], language);
                                }

                                if(!noImports)
                                    code = language.onUpdateImports(code);

                                SinkLibrary.getCustomLogger().logToFile(Level.INFO, sender.getName() + " executed script: " + nl + code);
                                String result = String.valueOf(language.eval(shellInstance, code));

                                if ( !skipOutput ) sender.sendMessage(ChatColor.AQUA + "Output: " + ChatColor.GREEN + language.formatCode(result));

                                if (clear)
                                {
                                    shellInstance.setCode("");
                                }
                            }
                            catch ( Exception e )
                            {
                                Util.reportException(sender, e);
                            }
                            break;

                        case ".history":
                            sender.sendMessage(ChatColor.GOLD + "-------|History|-------");
                            sender.sendMessage(ChatColor.WHITE + language.formatCode(code));
                            sender.sendMessage(ChatColor.GOLD + "-----------------------");
                            break;

                        default:
                            if ( mode.startsWith(".") )
                            {
                                sender.sendMessage('"' + mode + "\" is not a valid command");
                                break;
                            }
                            sender.sendMessage(ChatColor.DARK_GREEN + "[Input] " + ChatColor.WHITE + language.formatCode(currentLine));
                            break;
                    }
                    shellInstances.put(name, shellInstance);
                }
                catch(Throwable e)
                {
                    Util.reportException(sender, e);
                }
            }
        };

        if(async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable); // use tasks instead of because bukkit can handle them
        else
            Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private static void setLanguage(CommandSender sender, ScriptLanguage lang)
    {
        languageInstances.put(ScriptHandler.getInternalName(sender), lang);
    }

    private static ScriptLanguage getLanguage(CommandSender sender)
    {
        return languageInstances.get(ScriptHandler.getInternalName(sender));
    }

    public static HashMap<String, ShellInstance> getShellInstances()
    {
        return shellInstances;
    }
}
