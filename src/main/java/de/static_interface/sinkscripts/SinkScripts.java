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
import de.static_interface.sinklibrary.exceptions.NotInitializedException;
import de.static_interface.sinkscripts.commands.ScriptCommand;
import de.static_interface.sinkscripts.scriptengine.ScriptLanguage;
import de.static_interface.sinkscripts.scriptengine.ScriptUtil;
import de.static_interface.sinkscripts.scriptengine.languages.GroovyScript;
import de.static_interface.sinkscripts.scriptengine.languages.JavaScript;
import de.static_interface.sinkscripts.scriptengine.languages.LuaScript;
import de.static_interface.sinkscripts.scriptengine.shellinstances.ShellInstance;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import sun.plugin.security.PluginClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

public class SinkScripts extends JavaPlugin
{
    public static File AUTOSTART_FOLDER;
    public static File SCRIPTS_FOLDER;
    public static File LIB_FOLDER;
    static HashMap<String, ShellInstance> consoleInstances = new HashMap<>();
    public void onEnable()
    {
        if ( !checkDependencies() ) return;

        AUTOSTART_FOLDER = new File(SCRIPTS_FOLDER, "autostart");
        SCRIPTS_FOLDER = new File(SinkLibrary.getCustomDataFolder(), "scripts");
        LIB_FOLDER = new File(SinkLibrary.getCustomDataFolder(), "libs");

        try {
            File[] files = LIB_FOLDER.listFiles();
            if(files != null)
            {
                getLogger().info("BukkitConsole: JARs found: " + files.length);
                for ( File file : files )
                {
                    if ( file.getName().endsWith(".jar") )
                    {
                        getLogger().info("SinkScripts: Loading lib:  - " + file.getName());
                        PluginClassLoader loader = (PluginClassLoader) this.getClassLoader();
                        Method m = loader.getClass().getDeclaredMethod("addURL", URL.class);
                        m.setAccessible(true);
                        m.invoke(loader, new File(LIB_FOLDER, file.getName()).toURI().toURL());
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        registerCommands();
        registerListeners();
        registerScriptLanguages();

        if ( (!SCRIPTS_FOLDER.exists() && !SCRIPTS_FOLDER.mkdirs()) || (!AUTOSTART_FOLDER.exists() && !AUTOSTART_FOLDER.mkdirs()))
        {
            SinkLibrary.getCustomLogger().severe("Coudln't create scripts or autostart folder!");
        }
        loadAutoStart();
    }

    private void registerScriptLanguages()
    {
        ScriptUtil.register(new GroovyScript(this));
        ScriptUtil.register(new JavaScript(this));
        ScriptUtil.register(new LuaScript(this));
    }

    public PluginClassLoader getPluginClassLoader()
    {
        return (PluginClassLoader) getClassLoader();
    }

    private void loadAutoStart()
    {
        File[] files = AUTOSTART_FOLDER.listFiles();
        if(files == null) return;
        for(File file : files)
        {
            String ext = ScriptUtil.getFileExtension(file);
            if(!ScriptUtil.scriptLanguages.containsKey(ext)) continue;
            ScriptLanguage language = ScriptUtil.scriptLanguages.get(ext);
            language.runCode(getConsoleShellInstance(language), file);
        }
    }

    public static ShellInstance getConsoleShellInstance(ScriptLanguage language)
    {
        ShellInstance instance = consoleInstances.get(language.getFileExtension());
        if(instance == null)
        {
            instance = language.createNewShellInstance(Bukkit.getConsoleSender());
            consoleInstances.put(language.getFileExtension(), instance);
        }
        return instance;
    }

    private boolean checkDependencies()
    {
        if ( Bukkit.getPluginManager().getPlugin("SinkLibrary") == null )
        {
            getLogger().log(Level.WARNING, "This Plugin requires SinkLibrary!");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        if ( !SinkLibrary.initialized )
        {
            throw new NotInitializedException("SinkLibrary is not initialized!");
        }
        return true;
    }

    private void registerListeners()
    {
        Bukkit.getPluginManager().registerEvents(new ScriptChatListener(this), this);
    }

    private void registerCommands()
    {
        SinkLibrary.registerCommand("script", new ScriptCommand(this));
    }
}
