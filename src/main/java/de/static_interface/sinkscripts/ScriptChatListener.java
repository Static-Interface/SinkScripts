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

import de.static_interface.sinkscripts.commands.ScriptCommand;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

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
        if ( !ScriptCommand.isEnabled(event.getPlayer()) ) return;
        event.setCancelled(true);
        String currentLine = ChatColor.stripColor(event.getMessage());
        ScriptCommand.executeScript(event.getPlayer(), currentLine, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        String name = event.getPlayer().getName();
        ScriptCommand.disable(event.getPlayer());
        if ( ScriptCommand.shellInstances.containsKey(name) )
        {
            ScriptCommand.shellInstances.get(name).getClassLoader().clearCache();
            ScriptCommand.shellInstances.remove(name);
        }
    }
}
