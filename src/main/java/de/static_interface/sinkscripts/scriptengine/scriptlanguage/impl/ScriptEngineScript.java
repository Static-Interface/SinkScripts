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

package de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinkscripts.SinkScripts;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import de.static_interface.sinkscripts.scriptengine.shellinstance.ShellInstance;
import de.static_interface.sinkscripts.scriptengine.shellinstance.impl.ScriptEngineShellInstance;
import de.static_interface.sinkscripts.util.JoinClassLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public abstract class ScriptEngineScript extends ScriptLanguage {

    String engineName;

    public ScriptEngineScript(Plugin plugin, String name, String fileExtension, String engineName) {
        super(plugin, name, fileExtension);
        this.engineName = engineName;
    }

    @Override
    public Object eval(ShellInstance instance, String code) {
        try {
            return ((ScriptEngine) instance.getExecutor()).eval(code);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShellInstance createNewShellInstance(CommandSender sender) {
        ScriptEngine e = new ScriptEngineManager
                (new JoinClassLoader(SinkLibrary.getInstance().getClazzLoader(), Bukkit.class.getClassLoader(),
                                     ((SinkScripts)plugin).getClazzLoader())).getEngineByName(engineName);
        if (e == null) {
            throw new NullPointerException("Couldn't find ScriptEngine: " + engineName + ". Did you forgot to add a library?");
        }
        return new ScriptEngineShellInstance(sender, e);
    }

    @Override
    public void setVariable(ShellInstance instance, String name, Object value) {
        ((ScriptEngine) instance.getExecutor()).put(name, value);
    }
}
