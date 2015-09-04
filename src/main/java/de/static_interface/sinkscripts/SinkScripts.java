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

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinkscripts.command.ScriptCommand;
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.AutostartCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ClearCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ExecuteCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.HelpCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.HistoryCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ListLanguageCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.LoadCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.SaveCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ScriptCommandBase;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.SetLanguageCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.SetVariableCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl.GroovyScript;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl.JavaScriptScript;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl.LuaScript;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl.PythonScript;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl.RubyScript;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

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

        for (ScriptLanguage language : ScriptHandler.getInstance().getScriptLanguages()) {
            language.init();
        }

        registerListeners();
        initScriptCommands();
        loadAutoStart();
    }

    private void initScriptCommands() {
        ScriptCommandBase.registerCommand(new AutostartCommand());
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

    public void registerScriptLanguages() {
        ScriptHandler.getInstance().register(new GroovyScript(this));
        ScriptHandler.getInstance().register(new JavaScriptScript(this));
        ScriptHandler.getInstance().register(new LuaScript(this));
        ScriptHandler.getInstance().register(new PythonScript(this));
        ScriptHandler.getInstance().register(new RubyScript(this));
        //ScriptHandler.getInstance().register(new PHPScript(this));
    }

    public ClassLoader getClazzLoader() {
        return getClassLoader();
    }

    public void loadAutoStart() {
        loadAutoStart(getConsoleContext());
    }

    public void loadAutoStart(ScriptContext executorContext) {
        ScriptContext localContext = new ScriptContext(executorContext);
        for (ScriptLanguage language : ScriptHandler.getInstance().getScriptLanguages()) {
            localContext.setScriptLanguage(language);
            language.onAutoStart(localContext);
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

    public ScriptContext getConsoleContext() {
        return ScriptHandler.getInstance().getScriptContext(SinkLibrary.getInstance().getConsoleUser());
    }
}
