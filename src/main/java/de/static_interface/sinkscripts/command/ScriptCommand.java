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

package de.static_interface.sinkscripts.command;

import de.static_interface.sinklibrary.*;
import de.static_interface.sinklibrary.api.command.*;
import de.static_interface.sinklibrary.api.user.*;
import de.static_interface.sinkscripts.scriptengine.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.plugin.*;

public class ScriptCommand extends SinkCommand {

    public ScriptCommand(Plugin plugin) {
        super(plugin);
        getCommandOptions().setIrcOpOnly(true);
        getCommandOptions().setCliOptions(null);
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String[] args) {
        SinkUser user = SinkLibrary.getInstance().getUser((Object)sender);

        if (args.length > 0) {
            String currentLine = "";
            for (String arg : args) {
                currentLine += arg + ' ';
            }
            ScriptHandler.getInstance().handleLine(user, currentLine, getPlugin());
            return true;
        }

        if (sender instanceof ConsoleCommandSender) {
            return false;
        }

        if (!(sender instanceof ConsoleCommandSender) && ScriptHandler.getInstance().isEnabled(user)){
            ScriptHandler.getInstance().setEnabled(user, false);
            sender.sendMessage(ChatColor.DARK_RED + "Disabled Interactive Scripting Console");
            return true;
        }

        ScriptHandler.getInstance().setEnabled(user, true);
        sender.sendMessage(ChatColor.DARK_GREEN + "Enabled Interactive Scripting Console");
        return true;
    }
}
