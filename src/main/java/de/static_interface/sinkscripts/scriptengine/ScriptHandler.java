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

package de.static_interface.sinkscripts.scriptengine;

import de.static_interface.sinklibrary.api.user.*;
import de.static_interface.sinklibrary.user.*;
import de.static_interface.sinklibrary.util.*;
import de.static_interface.sinklibrary.util.StringUtil;
import de.static_interface.sinkscripts.*;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.*;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.impl.*;
import de.static_interface.sinkscripts.util.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;
import org.bukkit.util.*;

import java.util.*;
import java.util.logging.*;

public class ScriptHandler {

    protected static HashMap<String, ScriptContext> shellInstances = new HashMap<>(); // PlayerName - Shell Instance
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

    public static boolean isEnabled(SinkUser user) {
        return enabledUsers.contains(userToKey(user));
    }

    public static void setEnabled(SinkUser user, boolean enabled) {
        if (enabled) {
            enabledUsers.add(userToKey(user));
        } else {
            enabledUsers.remove(userToKey(user));
        }
    }

    public static String userToKey(SinkUser user) {
        String suffix = user.getProvider().getTabCompleterSuffix();
        if(suffix == null) suffix = "";
        return user.getName() + suffix;
    }

    public static void register(ScriptLanguage scriptLanguage) {
        scriptLanguages.put(scriptLanguage.getFileExtension(), scriptLanguage);
        scriptLanguage.preInit();
    }



    public static void handleCommand(ScriptContext context, String cmd, String[] args, String label, String nl) {
        ScriptCommandBase command = ScriptCommandBase.get(cmd);

        if(command == null) {
            context.getUser().sendMessage(ChatColor.RED + "Unknown command: " + cmd);
            return;
        }

        try {
            command.onPreExecute(context.getUser(), args, label, nl);
        } catch (Exception e) {
            Util.reportException(context.getUser(), e);
        }
    }

    public static void handleLine(final SinkUser user, final String line, final Plugin plugin) {
        ScriptContext context;

        boolean async = line.contains(" --async");

        final ScriptLanguage language = getLanguage(user);

        if (user instanceof ConsoleUser) {
            context = language.getConsoleContext();
        } else {
            context = shellInstances.get(ScriptHandler.userToKey(user));
        }
        if (context == null) {
            SinkScripts.getInstance().getLogger().log(Level.INFO, "Initializing ShellInstance for " + user.getName());

            if (language == null) {
                context = new DummyContext(user, plugin); // Used for .setLanguage
            } else {
                context = language.createNewShellInstance(user);
            }

            shellInstances.put(ScriptHandler.userToKey(user), context);
        }

        // Still null?!
        if (context == null) {
            throw new IllegalStateException("Couldn't create shellInstance!");
        }

        final ScriptContext tmpInstance = context;

        Runnable runnable = new Runnable() {
            String nl = Util.getNewLine();
            String code = null;
            ScriptContext localShellInstance = tmpInstance;

            @SuppressWarnings("ConstantConditions")
            public void run() {
                try {
                    if(line.toCharArray().length > 0 && line.toCharArray()[0] == '.') {
                        String[] args = line.split(" ");
                        String cmd = args[0].replaceFirst("\\Q" + "." + "\\E", "");
                        String[] cmdArgs = new String[args.length - 1];

                        for (int i = 0; i < cmdArgs.length; i++) {
                            cmdArgs[i] = args[i + 1];
                        }

                        handleCommand(localShellInstance, cmd, cmdArgs, line, nl);
                        return;
                    }

                    if(language == null) {
                        user.sendMessage(ChatColor.RED + "Language not set! Use .setlanguage <language>");
                        return;
                    }

                    String currentLine = line;

                    //check if code was set before
                    boolean codeSet = false;
                    boolean isReplace = false;

                    if (StringUtil.isEmptyOrNull(localShellInstance.getCode())) { // code not set
                            code = currentLine;
                            codeSet = true;
                    } else {
                        code = localShellInstance.getCode();
                    }

                    boolean useNl = !currentLine.startsWith("<");
                    if (!useNl) {
                        currentLine = currentLine.replaceFirst("<", "");
                        nl = "";
                    }

                    // remove last line and add the code after ^
                    if(line.length() > 0 && line.toCharArray()[0] == '^') {
                        List<String> lines = new ArrayList<>();
                        lines.addAll(Arrays.asList(code.split(System.lineSeparator())));

                        // remove last line
                        if(lines.size() > 0) lines.remove(lines.size()-1);

                        code = "";
                        for (String s : lines) {
                            code += s + System.lineSeparator();
                        }

                        // if there is more than just a "^", add everything after that
                        if(line.length() > 1) {
                            code +=  line.replaceFirst("\\Q^\\E", "") + nl;
                        }

                        //bad :(
                        //while(code.startsWith(System.lineSeparator())) {
                        //    code = code.replaceFirst("\\Q" + System.lineSeparator() + "\\E", "") + nl;
                        //}

                        if(!currentLine.trim().equals("^"))  {
                            currentLine = currentLine.replaceFirst("\\Q^\\E", "");
                            isReplace = true;
                            codeSet = true;
                        } else {
                            user.sendMessage(ChatColor.GOLD + "Removed last line");
                            localShellInstance.setCode(code);
                            return;
                        }
                    }

                    boolean isImport = false;

                    if (language != null && language.getImportIdentifier() != null && !codeSet) {
                        for (String s : language.getImportIdentifier()) {
                            if (currentLine.startsWith(s)) {
                                code = currentLine + nl + code + nl;
                                isImport = true;
                                break;
                            }
                        }
                    }

                    //dont use new line if line starts with "<" (e.g. if the line is too long or you want to add something to it)


                    if (!isImport && !codeSet) {
                        code = code + nl + currentLine + nl;
                    }
                    localShellInstance.setCode(code);

                    String prefix = isReplace ? ChatColor.GOLD + "[Replace]" : ChatColor.DARK_GREEN + "[Input]";
                    user.sendMessage(prefix + " " + ChatColor.WHITE + language.formatCode(currentLine));
                    //shellInstances.put(ScriptHandler.userToKey(user), localShellInstance);
                } catch (Throwable e) {
                    Util.reportException(user, e);
                }
            }
        };

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable); // use tasks instead of because bukkit can handle them
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void setVariables(ScriptContext context) {
        SinkUser user = context.getUser();
        ScriptLanguage language = context.getScriptLanguage();

        language.setVariable(context, "me", user);
        language.setVariable(context, "plugin", context.getPlugin());
        language.setVariable(context, "server", Bukkit.getServer());
        language.setVariable(context, "players", BukkitUtil.getOnlinePlayers());
        language.setVariable(context, "base", user.getBase());
        language.setVariable(context, "sender", user.getSender());
        language.setVariable(context, "language", language);

        if (user instanceof IngameUser) {
            Player player = ((IngameUser) user).getPlayer();
            BlockIterator iterator = new BlockIterator(player);
            language.setVariable(context, "player", player);
            language.setVariable(context, "at", iterator.next());
            language.setVariable(context, "x", player.getLocation().getX());
            language.setVariable(context, "y", player.getLocation().getY());
            language.setVariable(context, "z", player.getLocation().getZ());
        }
    }

    public static void setLanguage(SinkUser user, ScriptLanguage lang) {
        languageInstances.put(ScriptHandler.userToKey(user), lang);
    }

    public static ScriptLanguage getLanguage(SinkUser user) {
        return languageInstances.get(ScriptHandler.userToKey(user));
    }

    public static HashMap<String, ScriptContext> getScriptContexts() {
        return shellInstances;
    }
}
