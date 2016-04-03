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
import de.static_interface.sinklibrary.api.injection.InjectTarget;
import de.static_interface.sinklibrary.api.injection.Injector;
import de.static_interface.sinklibrary.util.StringUtil;
import de.static_interface.sinkscripts.command.ScriptCommand;
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.AutostartCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ClearCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ExecuteCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.HelpCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.HistoryCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.ListLanguageCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.LoadCommand;
import de.static_interface.sinkscripts.scriptengine.scriptcommand.RunInjectionCommand;
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
import de.static_interface.sinkscripts.util.Util;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SinkScripts extends JavaPlugin {

    public static File SCRIPTS_FOLDER;
    public static File INJECTS_FOLDER;
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
        INJECTS_FOLDER = new File(SinkLibrary.getInstance().getCustomDataFolder(), "injects");
        if ((!FRAMEWORK_FOLDER.exists() && !FRAMEWORK_FOLDER.mkdirs()) || (!SCRIPTS_FOLDER.exists() && !SCRIPTS_FOLDER.mkdirs())) {
            getLogger().warning("Coudln't create framework or scripts directory!");
        }

        if(SinkLibrary.getInstance().isInjectorAvailable()){
            if(!INJECTS_FOLDER.exists()) INJECTS_FOLDER.mkdirs();
        } else {
            getLogger().info("Injections are not available.");
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
        loadInjects();
    }

    private void loadInjects() {
        File autoStartDir = new File(INJECTS_FOLDER, "autostart");
        autoStartDir.mkdirs();

        for(File f : autoStartDir.listFiles()){
            if(!f.getName().endsWith(".patch")) continue;
            try {
                runInjection(f);
            } catch (Exception e) {
                getLogger().severe("Couldn't run injection: " + f.getName() + ": ");
                e.printStackTrace();
            }
        }
    }

    public void runInjection(String file) throws Exception {
        File f = new File(file);
        if(!f.exists()){
            f = new File(INJECTS_FOLDER, file + ".patch");
        }
        if(!f.exists()){
            f = new File(new File(INJECTS_FOLDER, "autostart"), file + ".patch");
        }
        if(!f.exists()){
            throw new IllegalArgumentException("File not found: " + file + "!");
        }
        runInjection(f);
    }

    public void runInjection(File file) throws Exception {
        String[] lines = Util.readLines(file);

        String code = "";
        String targetClass = null;
        boolean constructor = false;
        InjectTarget target = InjectTarget.AFTER_METHOD;
        String method = null;
        List<Class> methodArgs = new ArrayList<>();
        boolean codeStart = false;

        for (String line : lines) {
            if (line.equals("@@INJECT@@")) {
                codeStart = true;
                continue;
            }

            if (!codeStart) {
                if (!line.startsWith("[") && line.endsWith("]")) {
                    line = line.replaceFirst("\\[", "");
                    line = StringUtil.replaceLast(line, "]", "");
                    String[] parts = line.split("=", 2);
                    if(parts[0].equalsIgnoreCase("Constructor")){
                        constructor = true;
                    }

                    if(parts.length < 2) continue;
                    switch (parts[0].toLowerCase()){
                        case "method" :
                            method = parts[1];
                        case "at":
                        case "injecttarget":
                            target = InjectTarget.valueOf(parts[1].toUpperCase());
                        case "arg":
                        case "methodarg":
                            methodArgs.add(Class.forName(parts[1]));
                            break;
                        case "class":
                        case "targetclass":
                        case "target":
                            targetClass = parts[1];
                            break;
                    }
                    continue;
                }

                if(constructor && method != null){
                    throw new Exception("Invalid config: construct & method specified at the same time!");
                }

                continue;
            }

            if (code.equals("")) {
                code = line;
                continue;
            }
            code += System.lineSeparator() + line;
        }

        Validate.notNull(target, "injecttarget is not specified");
        Validate.notNull(targetClass, "class is not specified");
        if(!constructor){
            Validate.notNull(method, "method or constructor is not specified");
        }
        Validate.notEmpty(code, "no code found");

        if (constructor) {
            Injector.injectCode(targetClass, getClassLoader(), method, methodArgs.toArray(new Class[methodArgs.size()]), code, target);
        } else {
            Injector.injectCodeConstructor(targetClass, getClassLoader(), methodArgs.toArray(new Class[methodArgs.size()]), code, target);
        }
    }

    private void initScriptCommands() {
        ScriptCommandBase.registerCommand(new AutostartCommand());
        ScriptCommandBase.registerCommand(new ClearCommand());
        ScriptCommandBase.registerCommand(new ExecuteCommand());
        ScriptCommandBase.registerCommand(new HelpCommand());
        ScriptCommandBase.registerCommand(new HistoryCommand());
        ScriptCommandBase.registerCommand(new ListLanguageCommand());
        ScriptCommandBase.registerCommand(new LoadCommand());
        ScriptCommandBase.registerCommand(new RunInjectionCommand());
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
            if(localContext.getExecutor() == null) continue; //not supported language
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
