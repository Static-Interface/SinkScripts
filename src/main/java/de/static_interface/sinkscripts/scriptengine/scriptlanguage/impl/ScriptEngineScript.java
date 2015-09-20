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

package de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinkscripts.SinkScripts;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import de.static_interface.sinkscripts.util.JoinClassLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public abstract class ScriptEngineScript extends ScriptLanguage<ScriptEngine> {

    String engineName;

    public ScriptEngineScript(Plugin plugin, String name, String fileExtension, String engineName) {
        super(plugin, name, fileExtension);
        this.engineName = engineName;
    }

    @Override
    public Object eval(ScriptContext context, String code) throws Throwable {
        return ((ScriptEngine)context.getExecutor()).eval(code);
    }

    @Override
    public ScriptEngine createExecutor(@Nullable final ScriptContext context) {
        ClassLoader cl = new JoinClassLoader(SinkLibrary.getInstance().getClazzLoader(), Bukkit.class.getClassLoader(),
                            ((SinkScripts)plugin).getClazzLoader(), SinkLibrary.class.getClassLoader());

        Thread.currentThread().setContextClassLoader(cl);
        ScriptEngine engine = new ScriptEngineManager(cl).getEngineByName(engineName);


        if(context != null) {
            final StringWriter writer = new StringWriter() {
                @Override
                public void flush() {
                    super.flush();
                    String msg = toString();
                    for(String s : msg.split("\n")) {
                        context.getUser().sendMessage(
                                ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + "System.out: " + ChatColor.RESET + "" + ChatColor.WHITE + s);
                    }
                    getBuffer().delete(0, getBuffer().length());
                }
            };
            PrintWriter pw = new PrintWriter(writer, true);
            engine.getContext().setWriter(pw);
        }

        return engine;
    }

    @Override
    public void setVariable(ScriptContext context, String name, Object value) {
        ScriptEngine engine = (ScriptEngine) context.getExecutor();
        engine.put(name, value);
    }
}
