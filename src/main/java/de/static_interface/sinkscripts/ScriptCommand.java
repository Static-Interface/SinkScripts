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

package de.static_interface.sinkscripts;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.User;
import groovy.lang.GroovyShell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class ScriptCommand implements CommandExecutor
{
    public static ArrayList<String> enabledUsers = new ArrayList<>();
    public static HashMap<String, GroovyShell> shellInstances = new HashMap<>(); // PlayerName - Shell Instance
    public static HashMap<String, String> codeInstances = new HashMap<>(); // PlayerName - Code Instance
    static File scriptFolder;
    Plugin plugin;

    public ScriptCommand(Plugin plugin)
    {
        this.plugin = plugin;
        scriptFolder = new File(SinkLibrary.getCustomDataFolder(), "scripts");
        if ( !scriptFolder.exists() && !scriptFolder.mkdirs() )
        {
            throw new RuntimeException("Failed to create script folder!");
        }
    }

    public static boolean isEnabled(User user)
    {
        return enabledUsers.contains(user.getName());
    }

    public static void enable(User user)
    {
        enabledUsers.add(user.getName());
    }

    public static void disable(User user)
    {
        enabledUsers.remove(user.getName());
    }

    static String formatCode(String code)
    {
        ChatColor defaultColor = ChatColor.DARK_BLUE;
        ChatColor codeColor = ChatColor.RESET;
        ChatColor classColor = ChatColor.BLUE;
        ChatColor stringColor = ChatColor.RED;

        code = code.replace("import", ChatColor.GOLD + "import" + codeColor);
        code = code.replace("package", ChatColor.GOLD + "package" + codeColor);

        code = code.replace("class", defaultColor + "class" + codeColor);
        code = code.replace("implements", defaultColor + "implements" + codeColor);
        code = code.replace("extends", defaultColor + "extends" + codeColor);
        code = code.replace("enum", defaultColor + "enum" + codeColor);
        code = code.replace("interface", defaultColor + "interface" + codeColor);

        code = code.replace("private", defaultColor + "private" + codeColor);
        code = code.replace("public", defaultColor + "public" + codeColor);
        code = code.replace("protected", defaultColor + "protected" + codeColor);

        code = code.replace("final", defaultColor + "final" + codeColor);
        code = code.replace("static", defaultColor + "static" + codeColor);
        code = code.replace("native", defaultColor + "native" + codeColor);
        code = code.replace("throws", defaultColor + "throws" + codeColor);
        code = code.replace("transient", defaultColor + "transient" + codeColor);
        code = code.replace("volatile", defaultColor + "volatile" + codeColor);
        code = code.replace("synchronized", defaultColor + "synchronized" + codeColor);
        code = code.replace("strictfp", defaultColor + "strictfp" + codeColor);
        code = code.replace("const", defaultColor + "const" + codeColor);

        code = code.replace("try", defaultColor + "try" + codeColor);
        code = code.replace("catch", defaultColor + "catch" + codeColor);
        code = code.replace("finally", defaultColor + "finally" + codeColor);
        code = code.replace("throw", defaultColor + "throw" + codeColor);

        code = code.replace("while", defaultColor + "while" + codeColor);
        code = code.replace("continue", defaultColor + "continue" + codeColor);

        code = code.replace("void", defaultColor + "void" + codeColor);
        code = code.replace("return", defaultColor + "return" + codeColor);
        code = code.replace("switch", defaultColor + "switch" + codeColor);
        code = code.replace("case", defaultColor + "case" + codeColor);
        code = code.replace("break", defaultColor + "break" + codeColor);
        code = code.replace("super", defaultColor + "super" + codeColor);
        code = code.replace("new", defaultColor + "new" + codeColor);
        code = code.replace("this", defaultColor + "this" + codeColor);
        code = code.replace("goto", defaultColor + "goto" + codeColor);

        code = code.replace("if", defaultColor + "if" + codeColor);
        code = code.replace("else", defaultColor + "else" + codeColor);
        code = code.replace("true", defaultColor + "true" + codeColor);
        code = code.replace("false", defaultColor + "false" + codeColor);
        code = code.replace("instanceof", defaultColor + "instanceof" + codeColor);
        code = code.replace("for", defaultColor + "for" + codeColor);
        code = code.replace("while", defaultColor + "while" + codeColor);
        code = code.replace("assert", defaultColor + "assert" + codeColor);

        code = code.replace("String", classColor + "String" + codeColor);
        code = code.replace("Bukkit", classColor + "Bukkit" + codeColor);
        code = code.replace("BukkitUtil", classColor + "BukkitUtil" + codeColor);
        code = code.replace("SinkLibrary", classColor + "SinkLibrary" + codeColor);
        code = code.replace("User", classColor + "User" + codeColor);
        code = code.replace("Logger", classColor + "Logger" + codeColor);
        code = code.replace("Player", classColor + "Player" + codeColor);
        code = code.replace("Plugin", classColor + "Plugin" + codeColor);

        code = code.replace("int", defaultColor + "int" + codeColor);
        code = code.replace("boolean", defaultColor + "boolean" + codeColor);
        code = code.replace("long", defaultColor + "long" + codeColor);
        code = code.replace("short", defaultColor + "short" + codeColor);
        code = code.replace("float", defaultColor + "float" + codeColor);
        code = code.replace("byte", defaultColor + "byte" + codeColor);
        code = code.replace("char", defaultColor + "char" + codeColor);

        boolean stringStart = false;

        //Set String colro
        String tmp = "";
        for ( char Char : code.toCharArray() )
        {
            if ( Char == '"' )
            {
                if ( !stringStart )
                {
                    tmp += stringColor;
                }

                tmp += Char;

                if ( stringStart )
                {
                    tmp += codeColor;
                }

                stringStart = !stringStart;
            }
            else
            {
                tmp += Char;
            }
        }

        return tmp;
    }

    static void sendErrorMessage(User user, String message)
    {
        user.sendMessage(ChatColor.DARK_RED + "Exception: " + ChatColor.RED + message);
    }

    public static void executeScript(final User user, final String line, final Plugin plugin)
    {
        Bukkit.getScheduler().runTask(plugin, new Runnable()
        {
            String nl = System.getProperty("line.separator");
            String code = "";

            public void run()
            {
                String currentLine = line;
                String name = user.getName();
                GroovyShell shellInstance;
                String[] args = currentLine.split(" ");

                if ( !shellInstances.containsKey(name) )
                {
                    SinkLibrary.getCustomLogger().log(Level.INFO, "Initializing ShellInstance for " + user.getName());
                    shellInstance = new GroovyShell();
                    //Constant variables, they won't change
                    shellInstance.setVariable("me", user);
                    shellInstance.setVariable("plugin", plugin);
                    shellInstance.setVariable("player", user.getPlayer());
                    shellInstance.setVariable("server", Bukkit.getServer());
                    shellInstances.put(name, shellInstance);
                }
                else shellInstance = shellInstances.get(name);

                //Dynamic variables, will always get updated
                shellInstance.setVariable("players", Bukkit.getOnlinePlayers());
                shellInstance.setVariable("users", SinkLibrary.getOnlineUsers());

                boolean codeSet = false;

                if ( !codeInstances.containsKey(name) || codeInstances.get(name) == null )
                {
                    codeInstances.put(name, currentLine);
                    code = currentLine;
                    codeSet = true;
                }

                boolean useNl = !currentLine.startsWith("<");
                if ( !useNl )
                {
                    currentLine = currentLine.replaceFirst("<", "");
                    nl = "";
                }

                currentLine = currentLine.trim();

                if ( currentLine.startsWith(".") && !currentLine.startsWith(".setvariable") ) // command, don't add to code
                {
                    code = code.replace(currentLine, "");
                    currentLine = "";
                }
                String prevCode = codeInstances.get(name);

                if ( !codeSet )
                {
                    if ( currentLine.startsWith("import") ) code = currentLine + nl + prevCode;
                    else code = prevCode + nl + currentLine;
                    codeInstances.put(name, code);
                }

                String mode = args[0].toLowerCase();
                user.sendDebugMessage(ChatColor.GOLD + "Script mode: " + ChatColor.RED + mode);

                switch ( mode )
                {
                    case ".help":
                        user.sendMessage(ChatColor.GREEN + "[Help] " + ChatColor.GRAY + "Available Commands: help, execute, history");
                        break;

                    case ".clear":
                        codeInstances.remove(name);
                        updateImports(name, "");
                        user.sendMessage(ChatColor.DARK_RED + "History cleared");
                        break;

                    case ".setvariable":
                        //if ( args.length < 3 || !currentLine.contains("=") )
                        //{
                        //    sendErrorMessage(user, "Usage: .setvariable name=value");
                        //    break;
                        //}
                        try
                        {
                            String[] commandArgs = currentLine.split("=");
                            String variableName = commandArgs[0].split(" ")[1];
                            Object value = commandArgs[1];
                            shellInstance.setVariable(variableName, value);
                        }
                        catch ( Exception e )
                        {
                            sendErrorMessage(user, e.getMessage());
                        }
                        break;

                    case ".load":
                    {
                        if ( args.length < 2 )
                        {
                            user.sendMessage(ChatColor.DARK_RED + "Too few arguments! .load <File>");
                            break;
                        }
                        updateImports(name, "");
                        String scriptName = args[1];

                        try
                        {
                            codeInstances.put(name, code + loadFile(scriptName));
                        }
                        catch ( FileNotFoundException ignored )
                        {
                            user.sendMessage(ChatColor.DARK_RED + "File doesn't exists!");
                            break;
                        }
                        catch ( Exception e )
                        {
                            user.sendMessage(ChatColor.DARK_RED + "Exception: " + ChatColor.RED + e.getMessage());
                            break;
                        }
                        user.sendMessage(ChatColor.DARK_GREEN + "File loaded");
                        break;
                    }

                    case ".save":
                        updateImports(name, code);
                        if ( args.length < 2 )
                        {
                            user.sendMessage(ChatColor.DARK_RED + "Too few arguments! .save <File>");
                            break;
                        }
                        String scriptName = args[1];
                        File scriptFile = new File(scriptFolder, scriptName + ".groovy");
                        if ( scriptFile.exists() )
                        {
                            user.sendMessage(ChatColor.DARK_RED + "File already exists!");
                            break;
                        }
                        PrintWriter writer;
                        try
                        {
                            writer = new PrintWriter(scriptFile, "UTF-8");
                        }
                        catch ( Exception e )
                        {
                            user.sendMessage(ChatColor.DARK_RED + "Exception: " + ChatColor.RED + e.getMessage());
                            break;
                        }
                        writer.write(code);
                        writer.close();
                        user.sendMessage(ChatColor.DARK_GREEN + "Code saved!");
                        break;

                    case ".execute":
                        updateImports(name, code);

                        if ( user.isOnline() && !user.isConsole() )
                        {
                            BlockIterator iterator = new BlockIterator(user.getPlayer());
                            shellInstance.setVariable("at", iterator.next());
                            shellInstance.setVariable("x", user.getPlayer().getLocation().getX());
                            shellInstance.setVariable("y", user.getPlayer().getLocation().getY());
                            shellInstance.setVariable("z", user.getPlayer().getLocation().getZ());
                        }

                        try
                        {
                            SinkLibrary.getCustomLogger().logToFile(Level.INFO, user.getName() + " executed script: " + nl + code);
                            if ( args.length >= 2 )
                            {
                                code = loadFile(args[1]);
                            }
                            String result = String.valueOf(shellInstance.evaluate(code));

                            if ( result != null && !result.isEmpty() && !result.equals("null") )
                                user.sendMessage(ChatColor.AQUA + "Output: " + ChatColor.GREEN + formatCode(result));
                            else user.sendMessage(ChatColor.GREEN + "Code executed!");
                        }
                        catch ( Exception e )
                        {
                            sendErrorMessage(user, e.getMessage());
                        }
                        break;

                    case ".history":
                        updateImports(name, code);
                        user.sendMessage(ChatColor.GOLD + "-------|History|-------");
                        user.sendMessage(ChatColor.WHITE + formatCode(code));
                        user.sendMessage(ChatColor.GOLD + "-----------------------");
                        break;

                    default:
                        updateImports(name, code);
                        if ( mode.startsWith(".") )
                        {
                            user.sendMessage('"' + mode + "\" is not a valid command");
                            break;
                        }
                        user.sendMessage(ChatColor.DARK_GREEN + "[Input] " + ChatColor.WHITE + formatCode(currentLine));
                        break;
                }
            }
        });
    }

    static String loadFile(String scriptName) throws IOException
    {
        String nl = System.getProperty("line.separator");
        File scriptFile = new File(scriptFolder, scriptName + ".groovy");
        if ( !scriptFile.exists() )
        {
            throw new FileNotFoundException();
        }
        BufferedReader br = new BufferedReader(new FileReader(scriptFile));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while ( line != null )
        {
            sb.append(line);
            sb.append(nl);
            line = br.readLine();
        }
        return sb.toString();
    }

    static void updateImports(String name, String code)
    {
        String nl = System.getProperty("line.separator");
        String defaultImports = "import de.static_interface.sinklibrary.*;" + nl +
                "import org.bukkit.block.*;" + nl +
                "import org.bukkit.entity.*;" + nl +
                "import org.bukkit.inventory.*;" + nl +
                "import org.bukkit.material.*;" + nl +
                "import org.bukkit.potion.*; " + nl +
                "import org.bukkit.util.*" + nl +
                "import org.bukkit.*;" + nl + nl;
        code = code.replace(defaultImports, "");
        codeInstances.put(name, defaultImports + code);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        User user = SinkLibrary.getUser(sender);
        if ( isEnabled(user) )
        {
            disable(user);
            user.sendMessage(ChatColor.DARK_RED + "Disabled Interactive Groovy Console");
            return true;
        }

        if ( args.length > 0 )
        {
            String currentLine = "";
            for ( String arg : args )
            {
                currentLine += arg + ' ';
            }
            executeScript(SinkLibrary.getUser(sender), currentLine, plugin);
            return true;
        }
        enable(user);
        user.sendMessage(ChatColor.DARK_GREEN + "Enabled Interactive Groovy Console");
        return true;
    }
}
