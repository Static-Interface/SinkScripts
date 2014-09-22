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

package de.static_interface.sinkscripts.command;

import de.static_interface.sinklibrary.command.Command;
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public class ScriptCommand extends Command
{
    public ScriptCommand(Plugin plugin)
    {
        super(plugin);
    }

        @Override
    public boolean isIrcOpOnly()
    {
        return true;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String[] args)
    {
        if ( !(sender instanceof ConsoleCommandSender) && ScriptHandler.isEnabled(sender) )
        {
            ScriptHandler.setEnabled(sender, false);
            sender.sendMessage(ChatColor.DARK_RED + "Disabled Interactive Scripting Console");
            return true;
        }

        if ( args.length > 0 )
        {
            String currentLine = "";
            for ( String arg : args )
            {
                currentLine += arg + ' ';
            }
            ScriptHandler.handleLine(sender, currentLine, plugin);
            return true;
        }

        if(sender instanceof ConsoleCommandSender)
        {
            return false;
        }

        ScriptHandler.setEnabled(sender, true);
        sender.sendMessage(ChatColor.DARK_GREEN + "Enabled Interactive Scripting Console");
        return true;
    }
}
