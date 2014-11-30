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

package de.static_interface.sinkscripts.scriptengine.shellinstance;

import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

public abstract class ShellInstance {

    private Object executor;
    private String code;
    private CommandSender sender;

    public ShellInstance(CommandSender sender, Object executor) {
        this.sender = sender;
        this.executor = executor;
    }

    public Object getExecutor() {
        return executor;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public CommandSender getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [executor=" + (executor == null ? "null" : executor.toString())
               + ", sender=" + (sender == null ? "null" : sender.toString()) + "]";
    }
}
