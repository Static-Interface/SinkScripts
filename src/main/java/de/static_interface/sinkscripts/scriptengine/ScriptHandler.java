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
import de.static_interface.sinklibrary.api.sender.IrcCommandSender;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.util.BukkitUtil;
import de.static_interface.sinklibrary.util.Debug;
import de.static_interface.sinklibrary.util.StringUtil;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import de.static_interface.sinkscripts.scriptengine.shellinstance.ShellInstance;
import de.static_interface.sinkscripts.scriptengine.shellinstance.impl.DummyShellInstance;
import de.static_interface.sinkscripts.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class ScriptHandler {

    protected static HashMap<String, ShellInstance> shellInstances = new HashMap<>(); // PlayerName - Shell Instance
    private static ArrayList<String> enabledUsers = new ArrayList<>();
    private static HashMap<String, ScriptLanguage> scriptLanguages = new HashMap<>();
    private static HashMap<String, ScriptLanguage> languageInstances = new HashMap<>();

    public static Collection<ScriptLanguage> getScriptLanguages() {
        return scriptLanguages.values();
    }

    public static ScriptLanguage getScriptLanguageByExtension(String ext) {
        return scriptLanguages.get(ext);
    }

    public static ScriptLanguage getScriptLanguageByName(String name) {
        for (ScriptLanguage language : getScriptLanguages()) {
            if (language.getName().equalsIgnoreCase(name)) {
                return language;
            }
        }
        return null;
    }

    public static boolean isEnabled(CommandSender sender) {
        return enabledUsers.contains(getInternalName(sender));
    }

    public static void setEnabled(CommandSender sender, boolean enabled) {
        if (enabled) {
            enabledUsers.add(getInternalName(sender));
        } else {
            enabledUsers.remove(getInternalName(sender));
        }
    }

    public static String getInternalName(CommandSender sender) {
        return sender instanceof IrcCommandSender ? sender.getName() + ((IrcCommandSender)sender).getUser().getProvider().getTabCompleterSuffix() : sender.getName();
    }

    public static void register(ScriptLanguage scriptLanguage) {
        scriptLanguages.put(scriptLanguage.getFileExtension(), scriptLanguage);
        scriptLanguage.preInit();
    }

    public static void handleLine(final CommandSender sender, final String line, final Plugin plugin) {
        ShellInstance shellInstance;
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
        if (language == null && (!line.startsWith(".setlanguage") && !line.startsWith(".execute") && !line.startsWith(".help") && !line
                .startsWith(".listlanguages"))) {
            sender.sendMessage("Language not set! Use .setlanguage <language>");
            return;
        }
        if (sender instanceof ConsoleCommandSender) {
            shellInstance = language.getConsoleShellInstance();
        } else {
            shellInstance = shellInstances.get(ScriptHandler.getInternalName(sender));
        }
        if (shellInstance == null) {
            SinkLibrary.getInstance().getCustomLogger().log(Level.INFO, "Initializing ShellInstance for " + sender.getName());

            if (language == null) {
                shellInstance = new DummyShellInstance(sender, null); // Used for .setLanguage
            } else {
                shellInstance = language.createNewShellInstance(sender);
            }

            shellInstances.put(ScriptHandler.getInternalName(sender), shellInstance);
        }

        // Still null?!
        if (shellInstance == null) {
            throw new IllegalStateException("Couldn't create shellInstance!");
        }

        final ShellInstance tmpInstance = shellInstance;

        Runnable runnable = new Runnable() {
            String nl = Util.getNewLine();
            String code = null;
            ShellInstance localShellInstance = tmpInstance;
            @SuppressWarnings("ConstantConditions")
            public void run() {
                try {
                    String currentLine = "";

                    if(line.toCharArray()[0] != '.') {
                        currentLine = line;
                    }

                    String[] args = line.split(" ");
                    String mode = args[0].toLowerCase();

                    //check if code was set before, to prevent NPE's
                    boolean codeSet = false;
                    boolean isReplace = false;
                    if (StringUtil.isStringEmptyOrNull(localShellInstance.getCode())) {
                        if (line.toCharArray()[0] != '.') { //command, don't add to code
                            code = currentLine;
                            codeSet = true;
                        }
                    }

                    if(code == null ) {
                        code = localShellInstance.getCode();
                    }

                    // remove last line and add the code after ^
                    if(currentLine.trim().startsWith("^")) {
                        List<String> lines = new ArrayList<>();
                        lines.addAll(Arrays.asList(code.split(nl)));

                        // remove last line
                        if(lines.size() > 0) lines.remove(lines.size()-1);

                        code = "";

                        // if there is more than just a "^", add everything after that
                        for(String s : lines) {
                            code += nl + s;
                        }

                        //bad :(
                        while(code.startsWith(nl)) {
                            code = code.replaceFirst(nl, "");
                        }

                        if(!currentLine.trim().equals("^"))  {
                            currentLine = currentLine.replaceFirst("\\^", "");
                            isReplace = true;
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "Removed last line");
                            localShellInstance.setCode(code);
                            return;
                        }
                    }

                    //dont use new line if line starts with "<" (e.g. if the line is too long or you want to add something to it)
                    boolean useNl = !currentLine.startsWith("<");
                    if (!useNl) {
                        currentLine = currentLine.replaceFirst("<", "");
                        nl = "";
                    }

                    boolean isImport = false;

                    if (language != null && language.getImportIdentifier() != null) {
                        for (String s : language.getImportIdentifier()) {
                            if (currentLine.startsWith(s)) {
                                code = currentLine + nl + code;
                                isImport = true;
                                break;
                            }
                        }
                    }

                    if (!isImport && !codeSet) {
                        code = code + nl + currentLine;
                    }
                    localShellInstance.setCode(code);

                    /* Todo:
                     * add permissions for commands, e.g. sinkscripts.use.help, sinkscripts.use.executefile etc...
                     */
                    switch (mode) {
                        case ".help":
                            sender.sendMessage(ChatColor.GREEN + "[Help] " + ChatColor.GRAY + "Available Commands: .help, .load <file>, " +
                                               ".save <file>, .execute [file], .setvariable <name> <value>, .history, .clear, .setlanguage <language>");
                            break;

                        case ".setl":
                        case ".setlanguage":
                            ScriptLanguage newLanguage = null;
                            for (ScriptLanguage lang : ScriptHandler.getScriptLanguages()) {
                                if (lang.getName().equals(args[1]) || lang.getFileExtension().equals(args[1])) {
                                    newLanguage = lang;
                                    break;
                                }
                            }
                            if (newLanguage == null) {
                                sender.sendMessage("Unknown language: " + args[1]);
                                break;
                            }
                            if (sender instanceof ConsoleCommandSender) {
                                localShellInstance = newLanguage.getConsoleShellInstance();
                            } else {
                                localShellInstance = newLanguage.createNewShellInstance(sender);
                            }
                            shellInstances.put(ScriptHandler.getInternalName(sender), localShellInstance);
                            setLanguage(sender, newLanguage);
                            sender.sendMessage(ChatColor.GOLD + "Language has been set to: " + ChatColor.RED + newLanguage.getName());
                            break;

                        case ".clear":
                            localShellInstance.setCode("");
                            sender.sendMessage(ChatColor.DARK_RED + "History cleared");
                            break;

                        case ".listlanguages":
                            String languages = "";

                            for (ScriptLanguage language : ScriptHandler.getScriptLanguages()) {
                                if (languages.equals("")) {
                                    languages = language.getName();
                                    continue;
                                }

                                languages += ", " + language.getName();
                            }

                            sender.sendMessage(ChatColor.GOLD + "Available script languages: " + ChatColor.RESET + languages);
                            break;

                        case ".sv":
                        case ".setvariable":
                            //if ( args.length < 1 || !currentLine.contains("=") )
                            //{
                            //    sender.sendMessage(ChatColor.DARK_RED + "Usage: .setvariable name=value");
                            //    break;
                            //}
                            try {
                                String[] commandArgs = line.split("=");
                                String variableName = commandArgs[0].split(" ")[1];
                                String[] rawValue = new String[commandArgs.length - 1];
                                for (int i = 0; i < rawValue.length; i++) {
                                    rawValue[i] = commandArgs[i - 1];
                                }

                                Object value = language.getValue(rawValue);
                                language.setVariable(localShellInstance, variableName, value);
                                sender.sendMessage(
                                        ChatColor.BLUE + variableName + ChatColor.RESET + " has been successfully set to " + ChatColor.RED + value
                                        + ChatColor.RESET + " (" + ChatColor.BLUE + value.getClass().getSimpleName() + ")");
                            } catch (Exception e) {
                                Util.reportException(sender, e);
                            }
                            break;

                        case ".load": {
                            if (args.length < 2) {
                                sender.sendMessage(ChatColor.DARK_RED + "Too few arguments! .load <File>");
                                break;
                            }
                            String scriptName = args[1];

                            try {
                                localShellInstance.setCode(Util.loadFile(scriptName, language) + code);
                            } catch (FileNotFoundException ignored) {
                                sender.sendMessage(ChatColor.DARK_RED + "File doesn't exists!");
                                break;
                            } catch (Exception e) {
                                Util.reportException(sender, e);
                                break;
                            }
                            sender.sendMessage(ChatColor.DARK_GREEN + "File loaded");
                            break;
                        }

                        case ".save": {
                            if (args.length < 2) {
                                sender.sendMessage(ChatColor.DARK_RED + "Too few arguments! .save <File>");
                                break;
                            }
                            String scriptName = args[1];
                            File scriptFile;

                            if (isAutostartSave) {
                                scriptFile = new File(language.AUTOSTART_DIRECTORY, scriptName + "." + language.getFileExtension());
                            } else {
                                scriptFile = new File(language.SCRIPTLANGUAGE_DIRECTORY, scriptName + "." + language.getFileExtension());
                            }

                            if (scriptFile.exists()) {
                                if (!scriptFile.delete()) {
                                    throw new RuntimeException("Couldn't override " + scriptFile + " (File.delete() returned false)!");
                                }
                                break;
                            }
                            PrintWriter writer;
                            try {
                                writer = new PrintWriter(scriptFile, "UTF-8");
                            } catch (Exception e) {
                                Util.reportException(sender, e);
                                break;
                            }
                            writer.write(code);
                            writer.close();
                            sender.sendMessage(ChatColor.DARK_GREEN + "Code saved!");
                            break;
                        }
                        case ".exec":
                        case ".execute":

                            if (language != null) {
                                setVariables(language, plugin, sender, localShellInstance);
                            }

                            ScriptLanguage contextLanguage = language;
                            try {
                                boolean isParameter = false;
                                String scriptName = null;
                                if (args.length > 1) {
                                    for (String s : availableParamters) {
                                        if (s.equals(args[1])) {
                                            isParameter = true;
                                            break;
                                        }
                                    }
                                }
                                String result;

                                if(!isParameter) {
                                    scriptName = args[1];
                                    String tmp[] = scriptName.split("\\.");

                                    if (tmp.length < 1 && language == null) {
                                        throw new IllegalStateException("Couldn't find extension for language file: " + scriptName);
                                    }

                                    String extension = tmp[tmp.length - 1];
                                    ScriptLanguage
                                            tmpLanguage =
                                            ScriptHandler.getScriptLanguageByExtension(extension); // get language by extension
                                    if (tmpLanguage == null && language == null) {
                                        sender.sendMessage(ChatColor.DARK_RED
                                                           + "Language not set and/or invalid file extension! Use .execute <file> or .setlanguage <language>");
                                    }

                                    if (tmpLanguage != null) {
                                        contextLanguage = tmpLanguage;
                                        if (localShellInstance == null) {
                                            localShellInstance = contextLanguage.createNewShellInstance(sender);
                                            shellInstances.put(getInternalName(sender), localShellInstance);
                                        }
                                    }

                                    scriptName = StringUtil.formatArrayToString(tmp, ".", 0, tmp.length - 1); // remove extension
                                }

                                if (args.length > 1 && !isParameter) {
                                    Debug.log(localShellInstance);
                                    result =
                                            String.valueOf(contextLanguage
                                                                   .run(localShellInstance, Util.loadFile(scriptName, contextLanguage), noImports,
                                                                        clear));
                                } else {
                                    result = String.valueOf(contextLanguage.run(localShellInstance, code, noImports, clear));
                                }

                                if (!skipOutput) {
                                    sender.sendMessage(ChatColor.AQUA + "Output: " + ChatColor.GREEN + contextLanguage.formatCode(result));
                                }
                            } catch (Throwable thr) {
                                Util.reportException(sender, thr);
                            }
                            break;

                        case ".showhistory":
                        case ".history":
                            sender.sendMessage(ChatColor.GOLD + "-------|History|-------");
                            if (localShellInstance.getCode() != null) {
                                for (String s : localShellInstance.getCode().split(nl)) {
                                    if(s == null) continue;
                                    sender.sendMessage(ChatColor.WHITE + language.formatCode(s));
                                }
                            }
                            sender.sendMessage(ChatColor.GOLD + "-----------------------");
                        break;

                        default:
                            if (mode.startsWith(".")) {
                                sender.sendMessage('"' + mode + "\" is not a valid command");
                                break;
                            }
                            String prefix = isReplace ? ChatColor.GOLD + "[Replace]" : ChatColor.DARK_GREEN + "[Input]";
                            sender.sendMessage(prefix + " " + ChatColor.WHITE + language.formatCode(currentLine));
                            break;
                    }
                    shellInstances.put(ScriptHandler.getInternalName(sender), localShellInstance);
                } catch (Throwable e) {
                    Util.reportException(sender, e);
                }
            }
        };

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable); // use tasks instead of because bukkit can handle them
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void setVariables(ScriptLanguage language, Plugin plugin,
                                    CommandSender sender, ShellInstance shellInstance) {
        SinkUser user = SinkLibrary.getInstance().getUser(sender);
        language.setVariable(shellInstance, "me", user);
        language.setVariable(shellInstance, "plugin", plugin);
        language.setVariable(shellInstance, "server", Bukkit.getServer());
        language.setVariable(shellInstance, "players", BukkitUtil.getOnlinePlayers());
        language.setVariable(shellInstance, "sender", sender);
        language.setVariable(shellInstance, "language", language);

        if (sender instanceof Player) {
            Player player = (Player) sender;
            BlockIterator iterator = new BlockIterator(player);
            language.setVariable(shellInstance, "player", player);
            language.setVariable(shellInstance, "at", iterator.next());
            language.setVariable(shellInstance, "x", player.getLocation().getX());
            language.setVariable(shellInstance, "y", player.getLocation().getY());
            language.setVariable(shellInstance, "z", player.getLocation().getZ());
        }
    }

    private static void setLanguage(CommandSender sender, ScriptLanguage lang) {
        languageInstances.put(ScriptHandler.getInternalName(sender), lang);
    }

    private static ScriptLanguage getLanguage(CommandSender sender) {
        return languageInstances.get(ScriptHandler.getInternalName(sender));
    }

    public static HashMap<String, ShellInstance> getShellInstances() {
        return shellInstances;
    }
}
