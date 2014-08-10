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
import groovy.lang.GroovyShell;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

import static de.static_interface.sinkscripts.Script.SCRIPTS_FOLDER;

public class SinkScripts extends JavaPlugin
{
    public static final File AUTOSTART_FOLDER = new File(SCRIPTS_FOLDER, "autostart");
    static GroovyShell consoleShellInstance;
    public void onEnable()
    {
        if ( !checkDependencies() ) return;

        consoleShellInstance = new GroovyShell();

        registerCommands();
        registerListeners();

        if ( (!SCRIPTS_FOLDER.exists() && !SCRIPTS_FOLDER.mkdirs()) || (!AUTOSTART_FOLDER.exists() && !AUTOSTART_FOLDER.mkdirs()))
        {
            throw new RuntimeException("Failed to create script or autostart folder!");
        }

        loadAutoStart();
    }

    private void loadAutoStart()
    {
        File[] files = AUTOSTART_FOLDER.listFiles();
        if(files == null) return;
        for(File file : files)
        {
            if(!file.getName().endsWith(".groovy")) continue;
            Script.runCode(getConsoleShellInstance(), file);
        }
    }

    public static GroovyShell getConsoleShellInstance()
    {
        return consoleShellInstance;
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
