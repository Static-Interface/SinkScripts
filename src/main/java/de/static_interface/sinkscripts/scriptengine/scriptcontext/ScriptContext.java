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

package de.static_interface.sinkscripts.scriptengine.scriptcontext;

import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class ScriptContext {

    private final Plugin plugin;
    private final SinkUser user;
    private Object executor;
    private ScriptLanguage language;
    private String code;

    public ScriptContext(SinkUser user, ScriptLanguage language, Plugin plugin) {
        this.user = user;
        this.language = language;
        this.plugin = plugin;
        if(language != null) {
            this.executor = language.createExecutor();
        }
    }

    public ScriptContext(ScriptContext context) {
        this.user = context.getUser();
        this.language = context.getScriptLanguage();
        this.plugin = context.getPlugin();
        this.executor = context.getExecutor();
        this.code = context.getCode();
    }

    public Object getExecutor() {
        return executor;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        if(code == null || code.replaceAll("\\Q" + System.lineSeparator() + "\\E", "").equals("null")) {
            code = "";
        }
        this.code = code;
    }

    public SinkUser getUser() {
        return user;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [executor=" + (executor == null ? "null" : executor.toString())
               + ", user=" + (user== null ? "null" : user.toString()) + "]";
    }

    public ScriptLanguage getScriptLanguage() {
        return language;
    }

    public void setScriptLanguage(ScriptLanguage language) {
        this.language = language;
        this.executor = language.createExecutor();
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
