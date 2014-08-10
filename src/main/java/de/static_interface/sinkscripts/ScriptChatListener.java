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
import de.static_interface.sinklibrary.events.IrcReceiveMessageEvent;
import de.static_interface.sinklibrary.irc.IrcCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class ScriptChatListener implements Listener
{
    Plugin plugin;

    public ScriptChatListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handleChatScript(final AsyncPlayerChatEvent event)
    {
        if ( !Script.isEnabled(event.getPlayer()) ) return;
        event.setCancelled(true);
        String currentLine = event.getMessage();
        Script.executeScript(event.getPlayer(), currentLine, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleIrcMessage(IrcReceiveMessageEvent event)
    {
        IrcCommandSender sender = new IrcCommandSender(event.getUser(), event.getChannel().getName());
        if ( !Script.isEnabled(sender) )
        {
            return;
        }

        String currentLine = event.getMessage().trim();
        String ircCommandPrefix = getIrcCommandPrefix();
        if(currentLine.startsWith(ircCommandPrefix)) return;
        Script.executeScript(sender, currentLine, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        String name = event.getPlayer().getName();
        Script.setEnabled(event.getPlayer(), false);
        if ( Script.shellInstances.containsKey(name) )
        {
            Script.shellInstances.get(name).getClassLoader().clearCache();
            Script.shellInstances.remove(name);
        }
    }

    public static String getIrcCommandPrefix()
    {
        if(!SinkLibrary.ircAvailable) throw new IllegalStateException("SinkIRC is not available!");
        try
        {
            Class<?> c = Class.forName("de.static_interface.sinkirc.IrcUtil");
            Method method = c.getMethod("getCommandPrefix", null);
            if ( !method.isAccessible() )
            {
                method.setAccessible(true);
            }
            return (String) method.invoke(null);
        }
        catch(Exception e)
        {
            throw new AssertionError(e);
        }
    }
}
