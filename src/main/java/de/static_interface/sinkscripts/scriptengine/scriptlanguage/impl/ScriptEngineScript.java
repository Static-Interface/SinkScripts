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

import de.static_interface.sinklibrary.*;
import de.static_interface.sinklibrary.api.user.*;
import de.static_interface.sinkscripts.*;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.impl.*;
import de.static_interface.sinkscripts.util.*;
import org.bukkit.*;
import org.bukkit.plugin.*;

import javax.script.*;

public abstract class ScriptEngineScript extends ScriptLanguage {

    String engineName;

    public ScriptEngineScript(Plugin plugin, String name, String fileExtension, String engineName) {
        super(plugin, name, fileExtension);
        this.engineName = engineName;
    }

    @Override
    public Object eval(ScriptContext context, String code) {
        try {
            return ((ScriptEngine) context.getExecutor()).eval(code);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScriptContext createNewShellInstance(SinkUser user) {
        ScriptEngine e = new ScriptEngineManager
                (new JoinClassLoader(SinkLibrary.getInstance().getClazzLoader(), Bukkit.class.getClassLoader(),
                                     ((SinkScripts)plugin).getClazzLoader())).getEngineByName(engineName);
        if (e == null) {
            throw new NullPointerException("Couldn't find ScriptEngine: " + engineName + ". Did you forgot to add a library?");
        }
        return new ScriptEngineContext(user, e, this, plugin);
    }

    @Override
    public void setVariable(ScriptContext context, String name, Object value) {
        ScriptEngine engine = (ScriptEngine) context.getExecutor();
        engine.put(name, value);
    }
}
