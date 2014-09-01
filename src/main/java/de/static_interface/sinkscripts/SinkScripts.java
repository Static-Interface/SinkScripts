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
import de.static_interface.sinkscripts.scriptengine.languages.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

public class SinkScripts extends JavaPlugin
{
    public static File SCRIPTS_FOLDER;
    public static File LIB_FOLDER;
    public static File FRAMEWORK_FOLDER;

    public void onEnable()
    {
        if ( !checkDependencies() ) return;

        SCRIPTS_FOLDER = new File(SinkLibrary.getCustomDataFolder(), "scripts");
        LIB_FOLDER = new File(SCRIPTS_FOLDER, "libs");
        FRAMEWORK_FOLDER = new File(LIB_FOLDER, "framework");

        if ((!LIB_FOLDER.exists() && !LIB_FOLDER.mkdirs()) || (!SCRIPTS_FOLDER.exists() && !SCRIPTS_FOLDER.mkdirs()))
        {
            SinkLibrary.getCustomLogger().severe("Coudln't create scripts or lib directory!");
        }

        registerScriptLanguages();

        try
        {
            File[] files = LIB_FOLDER.listFiles();
            if(files != null)
            {
                int i = 0;
                for ( File file : files )
                {
                    if ( file.getName().endsWith(".jar") )
                    {
                        getLogger().info("SinkScripts: Loading java library: " + file.getName());
                        addURL(file.toURL());
                        i++;
                    }

                    //if(file.getName().endsWith(".so") ||file.getName().endsWith("dll"))
                    //{
                    //    getLogger().info("SinkScripts: Loading native library: " + file.getName());
                    //    System.loadLibrary(file.getCanonicalPath());
                    //    i++;
                    //}
                }
                SinkLibrary.getCustomLogger().info("SinkScripts: Libraries loaded: " + i);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        for(ScriptLanguage language : ScriptUtil.getScriptLanguages())
        {
            language.init();
        }

        registerCommands();
        registerListeners();

        loadAutoStart();
    }

    public void addURL(URL url) throws Exception {
        URLClassLoader classLoader
                = (URLClassLoader) getClassLoader();
        Class clazz= URLClassLoader.class;

        // Use reflection
        Method method= clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(classLoader, url);
    }

    private void registerScriptLanguages()
    {
        ScriptUtil.register(new GroovyScript(this));
        ScriptUtil.register(new JavaScript(this));
        ScriptUtil.register(new LuaScript(this));
        ScriptUtil.register(new PythonScript(this));
        ScriptUtil.register(new PerlScript(this));
    }

    public ClassLoader getClazzLoader()
    {
        return getClassLoader();
    }

    private void loadAutoStart()
    {
       for(ScriptLanguage language : ScriptUtil.getScriptLanguages())
       {
           language.onAutoStart();
       }
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
