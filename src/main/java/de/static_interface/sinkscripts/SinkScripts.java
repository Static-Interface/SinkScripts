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

package de.static_interface.sinkscripts;

import de.static_interface.sinklibrary.*;
import de.static_interface.sinkscripts.command.ScriptCommand;
import de.static_interface.sinkscripts.scriptengine.*;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.*;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.*;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl.*;
import org.bukkit.*;
import org.bukkit.plugin.java.*;

import java.io.*;
import java.util.logging.*;

public class SinkScripts extends JavaPlugin {

    public static File SCRIPTS_FOLDER;
    public static File FRAMEWORK_FOLDER;
    private static SinkScripts instance;

    public static SinkScripts getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (!checkDependencies()) {
            return;
        }
        instance = this;
        SCRIPTS_FOLDER = new File(SinkLibrary.getInstance().getCustomDataFolder(), "scripts");
        FRAMEWORK_FOLDER = new File(SCRIPTS_FOLDER, "framework");
        if ((!FRAMEWORK_FOLDER.exists() && !FRAMEWORK_FOLDER.mkdirs()) || (!SCRIPTS_FOLDER.exists() && !SCRIPTS_FOLDER.mkdirs())) {
            getLogger().warning("Coudln't create framework or scripts directory!");
        }
        setupProperties();
        registerCommands();
        registerScriptLanguages();

        for (ScriptLanguage language : ScriptHandler.getScriptLanguages()) {
            language.init();
        }

        registerListeners();
        initScriptCommands();
        loadAutoStart();
    }

    private void initScriptCommands() {
        ScriptCommandBase.registerCommand(new ClearCommand());
        ScriptCommandBase.registerCommand(new ExecuteCommand());
        ScriptCommandBase.registerCommand(new HelpCommand());
        ScriptCommandBase.registerCommand(new HistoryCommand());
        ScriptCommandBase.registerCommand(new ListLanguageCommand());
        ScriptCommandBase.registerCommand(new LoadCommand());
        ScriptCommandBase.registerCommand(new SaveCommand());
        ScriptCommandBase.registerCommand(new SetLanguageCommand());
        ScriptCommandBase.registerCommand(new SetVariableCommand());
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void setupProperties() {
        System.setProperty("CMSClassUnloadingEnabled", "true");
        System.setProperty("UseConcMarkSweepGC", "true");
        System.setProperty("CMSPermGenSweepingEnabled", "true");
    }

    private void registerScriptLanguages() {
        ScriptHandler.register(new GroovyScript(this));
        ScriptHandler.register(new JavaScript(this));
        ScriptHandler.register(new LuaScript(this));
        ScriptHandler.register(new PythonScript(this));
        ScriptHandler.register(new RubyScript(this));
    }

    public ClassLoader getClazzLoader() {
        return getClassLoader();
    }

    private void loadAutoStart() {
        for (ScriptLanguage language : ScriptHandler.getScriptLanguages()) {
            language.onAutoStart();
        }
    }

    private boolean checkDependencies() {
        if (Bukkit.getPluginManager().getPlugin("SinkLibrary") == null) {
            getLogger().log(Level.WARNING, "This Plugin requires SinkLibrary!");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        return SinkLibrary.getInstance().validateApiVersion(SinkLibrary.API_VERSION, this);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ScriptChatListener(this), this);
    }

    private void registerCommands() {
        SinkLibrary.getInstance().registerCommand("script", new ScriptCommand(this));
    }
}
