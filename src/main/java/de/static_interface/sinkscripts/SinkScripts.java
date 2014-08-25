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
import de.static_interface.sinkscripts.scriptengine.GroovyScript;
import de.static_interface.sinkscripts.scriptengine.ScriptLanguage;
import de.static_interface.sinkscripts.scriptengine.ShellInstance;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

public class SinkScripts extends JavaPlugin
{
    public static File AUTOSTART_FOLDER;
    public static File SCRIPTS_FOLDER;
    static HashMap<String, ShellInstance> consoleInstances = new HashMap<>();
    public void onEnable()
    {
        if ( !checkDependencies() ) return;

        AUTOSTART_FOLDER = new File(SCRIPTS_FOLDER, "autostart");
        SCRIPTS_FOLDER = new File(SinkLibrary.getCustomDataFolder(), "scripts");

        registerCommands();
        registerListeners();
        registerScriptLanguages();

        for(ScriptLanguage language : ScriptUtil.scriptLanguages.values())
        {
            consoleInstances.put(language.getFileExtension(), language.getNewShellInstance());
        }

        if ( (!SCRIPTS_FOLDER.exists() && !SCRIPTS_FOLDER.mkdirs()) || (!AUTOSTART_FOLDER.exists() && !AUTOSTART_FOLDER.mkdirs()))
        {
            SinkLibrary.getCustomLogger().severe("Coudln't create scripts or autostart folder!");
        }

        loadAutoStart();
    }

    private void registerScriptLanguages()
    {
        ScriptUtil.register(new GroovyScript(this));
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
        return consoleInstances.get(language.getFileExtension());
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
