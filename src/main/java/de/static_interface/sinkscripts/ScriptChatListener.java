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
import de.static_interface.sinklibrary.api.event.*;
import de.static_interface.sinklibrary.api.user.*;
import de.static_interface.sinklibrary.user.*;
import de.static_interface.sinkscripts.scriptengine.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.*;

import java.lang.reflect.*;

public class ScriptChatListener implements Listener {

    Plugin plugin;

    public ScriptChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public static String getIrcCommandPrefix() {
        if (!SinkLibrary.getInstance().isIrcAvailable()) {
            throw new IllegalStateException("SinkIRC is not available!");
        }
        try {
            Class<?> c = Class.forName("de.static_interface.sinkirc.IrcUtil");
            Method method = c.getMethod("getCommandPrefix", null);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return (String) method.invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handleChatScript(final AsyncPlayerChatEvent event) {
        IngameUser user = SinkLibrary.getInstance().getIngameUser(event.getPlayer());

        if (!ScriptHandler.isEnabled(user)) {
            return;
        }
        event.setCancelled(true);
        String currentLine = event.getMessage();
        ScriptHandler.handleLine(user, currentLine, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleIrcMessage(IrcReceiveMessageEvent event) {
        SinkUser user = SinkLibrary.getInstance().getIrcUser(event.getUser(), event.getChannel().getName());

        if (!ScriptHandler.isEnabled(user)) {
            return;
        }

        String currentLine = event.getMessage();
        String ircCommandPrefix = getIrcCommandPrefix();
        if (currentLine.startsWith(ircCommandPrefix)) {
            return;
        }
        ScriptHandler.handleLine(user, currentLine, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        IngameUser user = SinkLibrary.getInstance().getIngameUser(event.getPlayer());

        String name = ScriptHandler.getInternalName(user);
        ScriptHandler.setEnabled(user, false);
        if (ScriptHandler.getShellInstances().containsKey(name)) {
            ScriptHandler.getShellInstances().remove(name);
        }
    }
}
