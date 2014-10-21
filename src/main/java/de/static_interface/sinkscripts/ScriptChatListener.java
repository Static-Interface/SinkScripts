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
import de.static_interface.sinklibrary.api.event.IrcReceiveMessageEvent;
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

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
        if (!ScriptHandler.isEnabled(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        String currentLine = event.getMessage();
        ScriptHandler.handleLine(event.getPlayer(), currentLine, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleIrcMessage(IrcReceiveMessageEvent event) {
        CommandSender sender = SinkLibrary.getInstance().getIrcUser(event.getUser(), event.getChannel().getName()).getSender();

        if (!ScriptHandler.isEnabled(sender)) {
            return;
        }

        String currentLine = event.getMessage();
        String ircCommandPrefix = getIrcCommandPrefix();
        if (currentLine.startsWith(ircCommandPrefix)) {
            return;
        }
        ScriptHandler.handleLine(sender, currentLine, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = ScriptHandler.getInternalName(event.getPlayer());
        ScriptHandler.setEnabled(event.getPlayer(), false);
        if (ScriptHandler.getShellInstances().containsKey(name)) {
            ScriptHandler.getShellInstances().remove(name);
        }
    }
}
