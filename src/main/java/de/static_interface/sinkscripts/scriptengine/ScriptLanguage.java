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
import de.static_interface.sinkscripts.ScriptUtil;
import de.static_interface.sinkscripts.SinkScripts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static de.static_interface.sinkscripts.SinkScripts.SCRIPTS_FOLDER;

public abstract class ScriptLanguage
{
    protected static  HashMap<String, ShellInstance> shellInstances = new HashMap<>(); // PlayerName - Shell Instance
    private static HashMap<String, ScriptLanguage> languageInstances = new HashMap<>();
    protected String fileExtension;
    protected Plugin plugin;
    protected String name;

    public ScriptLanguage(Plugin plugin, String name, String fileExtension)
    {
        this.fileExtension = fileExtension.toLowerCase();
        this.plugin = plugin;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public static HashMap<String, ShellInstance> getShellInstances()
    {
        return shellInstances;
    }

    public abstract String formatCode(String code);

    public abstract Object runCode(ShellInstance instance, String code);

    public Object runCode(ShellInstance instance, File file)
    {
        try
        {
            return runCode(instance, loadFile(file));
        }
        catch ( IOException e )
        {
            throw new RuntimeException(e);
        }
    }

    protected static String loadFile(File scriptFile) throws IOException
    {
        String nl = ScriptUtil.getNewLine();
        if ( !scriptFile.exists() )
        {
            throw new FileNotFoundException("Couldn't find file: " + scriptFile);
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

    private String loadFile(String scriptName) throws IOException
    {
        File scriptFile = new File(SCRIPTS_FOLDER, scriptName + "." + fileExtension);
        if(!scriptFile.exists())
        {
            scriptFile = searchRecursively(scriptName, SCRIPTS_FOLDER);
        }
        return loadFile(scriptFile);
    }

    private File searchRecursively(String scriptName, File directory) throws IOException
    {
        File[] files = directory.listFiles();
        if(files == null) return null;
        for (File file : files)
        {
            if (file.isDirectory())
            {
                return searchRecursively(scriptName, file);
            }
            else
            {
                if(file.getName().equals(scriptName + "." + getFileExtension())) return file;
            }
        }
        return null;
    }

    volatile static ShellInstance shellInstance;
    public static void executeScript(final CommandSender sender, final String line, final Plugin plugin)
    {
        final String name = ScriptUtil.getInternalName(sender);
        final List<String> availableParamters = new ArrayList<>();
        availableParamters.add("--async");
        availableParamters.add("--hideoutput");

        boolean isExecute = line.startsWith(".execute");
        boolean async = isExecute && line.contains(" --async"); // bad :(
        final boolean noOutput = isExecute && (line.contains(" --hideoutput"));

        final ScriptLanguage language = getLanguage(sender);
        if(language == null && (!line.startsWith(".setlanguage") && !line.startsWith(".help")))
        {
            sender.sendMessage("Language not set! Use .setlanguage <language>");
            return;
        }
        if(sender instanceof ConsoleCommandSender )
        {
            shellInstance = SinkScripts.getConsoleShellInstance(language);
        }
        else
            shellInstance = shellInstances.get(ScriptUtil.getInternalName(sender));
        if(shellInstance == null)
        {
            SinkLibrary.getCustomLogger().log(Level.INFO, "Initializing ShellInstance for " + sender.getName());

            if(language == null)
                shellInstance = new DummyShellInstance(sender, null); // Used for .setLanguage
            else
                shellInstance = language.createNewShellInstance(sender);

            shellInstances.put(ScriptUtil.getInternalName(sender), shellInstance);
        }

        // Still null?!
        if(shellInstance == null) throw new IllegalStateException("Couldn't create shellInstance!");

        Runnable runnable = new Runnable()
        {
            String nl = ScriptUtil.getNewLine();
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
                        for(String s : language.getImportIdentifier())
                        {
                            if (language != null && currentLine.startsWith(s))
                            {
                                code = currentLine + nl + prevCode;
                                isImport = true;
                                break;
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
                            for(ScriptLanguage lang : ScriptUtil.scriptLanguages.values())
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
                                shellInstance = SinkScripts.getConsoleShellInstance(language);
                            }
                            else
                            {
                                shellInstance = newLanguage.createNewShellInstance(sender);
                            }
                            setLanguage(sender, newLanguage);
                            sender.sendMessage("Language has been set to: " + newLanguage.getName());
                            break;

                        case ".clear":
                            shellInstance.setCode(language.onUpdateImports(""));
                            sender.sendMessage(ChatColor.DARK_RED + "History cleared");
                            break;

                        case ".setvariable":
                            if ( args.length < 1 || !currentLine.contains("=") )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "Usage: .setvariable name=value");
                                break;
                            }
                            try
                            {
                                String[] commandArgs = currentLine.split("=");
                                String variableName = commandArgs[0].split(" ")[1];
                                Object value = language.getValue(commandArgs);

                                language.setVariable(shellInstance, variableName, value);
                            }
                            catch ( Exception e )
                            {
                                ScriptUtil.reportException(sender, e);
                            }
                            break;

                        case ".load":
                        {
                            if ( args.length < 2 )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "Too few arguments! .load <File>");
                                break;
                            }
                            code = language.onUpdateImports("");
                            String scriptName = args[1];

                            try
                            {
                                shellInstance.setCode(code + language.loadFile(scriptName));
                            }
                            catch ( FileNotFoundException ignored )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "File doesn't exists!");
                                break;
                            }
                            catch ( Exception e )
                            {
                                ScriptUtil.reportException(sender, e);
                                break;
                            }
                            sender.sendMessage(ChatColor.DARK_GREEN + "File loaded");
                            break;
                        }

                        case ".save":
                            code = language.onUpdateImports(code);
                            if ( args.length < 2 )
                            {
                                sender.sendMessage(ChatColor.DARK_RED + "Too few arguments! .save <File>");
                                break;
                            }
                            String scriptName = args[1];
                            File scriptFile = new File(SCRIPTS_FOLDER, scriptName + "." + language.getFileExtension());
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
                                ScriptUtil.reportException(sender, e);
                                break;
                            }
                            writer.write(code);
                            writer.close();
                            sender.sendMessage(ChatColor.DARK_GREEN + "Code saved!");
                            break;

                        case ".execute":
                            code = language.onUpdateImports(code);

                            language.setVariable(shellInstance, "me", SinkLibrary.getUser(sender));
                            language.setVariable(shellInstance, "plugin", plugin);
                            language.setVariable(shellInstance, "server", Bukkit.getServer());
                            language.setVariable(shellInstance, "players", Bukkit.getOnlinePlayers());
                            language.setVariable(shellInstance, "users", SinkLibrary.getOnlineUsers());

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
                                SinkLibrary.getCustomLogger().logToFile(Level.INFO, sender.getName() + " executed script: " + nl + code);
                                boolean isParameter = false;
                                for(String s : availableParamters)
                                {
                                    if(s.equals(args[1]))
                                    {
                                        isParameter = true;
                                        break;
                                    }
                                }
                                if ( args.length >= 2 && !isParameter )
                                {
                                    code = language.loadFile(args[1]);
                                }
                                String result = String.valueOf(language.runCode(shellInstance, code));

                                if ( !noOutput ) sender.sendMessage(ChatColor.AQUA + "Output: " + ChatColor.GREEN + language.formatCode(result));
                            }
                            catch ( Exception e )
                            {
                                ScriptUtil.reportException(sender, e);
                            }
                            break;

                        case ".history":
                            code = language.onUpdateImports(code);
                            sender.sendMessage(ChatColor.GOLD + "-------|History|-------");
                            sender.sendMessage(ChatColor.WHITE + language.formatCode(code));
                            sender.sendMessage(ChatColor.GOLD + "-----------------------");
                            break;

                        default:
                            code = language.onUpdateImports(code);
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
                catch(Exception e)
                {
                    ScriptUtil.reportException(sender, e);
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
        languageInstances.put(ScriptUtil.getInternalName(sender), lang);
    }

    private static ScriptLanguage getLanguage(CommandSender sender)
    {
        return languageInstances.get(ScriptUtil.getInternalName(sender));
    }

    /**
     * Does not work with chars
     * @param commandArgs Args to data, first string is skipped
     * @return Value
     */

    private Object getValue(String[] commandArgs)
    {
        String commandArg = commandArgs[1];

        if ( commandArg.equalsIgnoreCase("null") ) return null;

        try
        {
            Long l = Long.parseLong(commandArg);
            if ( l <= Byte.MAX_VALUE )
            {
                return Byte.parseByte(commandArg);
            }
            else if ( l <= Short.MAX_VALUE )
            {
                return Short.parseShort(commandArg); // Value is a Short
            }
            else if ( l <= Integer.MAX_VALUE )
            {
                return Integer.parseInt(commandArg); // Value is an Integer
            }
            return l; // Value is a Long
        }
        catch ( Exception ignored ) { }

        try
        {
            return Float.parseFloat(commandArg); // Value is Float
        }
        catch ( Exception ignored ) { }

        try
        {
            return Double.parseDouble(commandArg); // Value is Double
        }
        catch ( Exception ignored ) { }

        try
        {
            return Boolean.parseBoolean(commandArg);
        }
        catch ( Exception ignored ) {}

        if ( commandArg.startsWith("'") && commandArg.endsWith("'") && commandArg.length() == 3 )
        {
            return commandArg.toCharArray()[1]; // ???
        }


        String tmp = "";
        for ( int i = 1; i < commandArgs.length; i++ )
        {
            if ( tmp.equals("") )
            {
                tmp = commandArgs[i];
            }
            else tmp += " " + commandArgs[i];
        }
        if ( tmp.startsWith("\"") && tmp.endsWith("\"") )
        {
            StringBuilder b = new StringBuilder(tmp);
            b.replace(tmp.lastIndexOf("\""), tmp.lastIndexOf("\"") + 1, "" );
            return b.toString().replaceFirst("\"", "");  // Value is a String
        }
        throw new IllegalArgumentException("Unknown value");
    }

    protected String onUpdateImports(String code)
    {
        String defaultImports = getDefaultImports();
        code = code.replace(defaultImports, "");
        return defaultImports + code;
    }
    protected abstract String getDefaultImports();
    public abstract ShellInstance createNewShellInstance(CommandSender sender);
    public abstract void setVariable(ShellInstance instance, String name, Object value);
    public abstract List<String> getImportIdentifier();
}
