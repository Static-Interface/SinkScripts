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

import de.static_interface.sinklibrary.irc.IrcCommandSender;
import de.static_interface.sinkscripts.scriptengine.ScriptLanguage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ScriptUtil
{
    public static ArrayList<String> enabledUsers = new ArrayList<>();
    public static HashMap<String, ScriptLanguage> scriptLanguages = new HashMap<>();
    public static boolean isEnabled(CommandSender sender)
    {
        return enabledUsers.contains(getInternalName(sender));
    }

    public static void setEnabled(CommandSender sender, boolean enabled)
    {
        if(enabled)
            enabledUsers.add(getInternalName(sender));
        else
            enabledUsers.remove(getInternalName(sender));
    }

    public static String getInternalName(CommandSender sender)
    {
        return sender instanceof IrcCommandSender ? "IRC_" + sender.getName() : sender.getName();
    }

    public static void reportException(CommandSender sender, Exception e)
    {
        sender.sendMessage(ChatColor.DARK_RED + "Unhandled exception: ");
        String msg = e.getLocalizedMessage();
        sender.sendMessage(ChatColor.RED + e.getClass().getCanonicalName() + (msg == null ? "" : ": " + msg));

        Throwable cause = e.getCause();

        //Report all "Caused By" exceptions but don't show stacktraces
        while(cause != null)
        {
            msg = cause.getLocalizedMessage();
            sender.sendMessage(ChatColor.RED + "Caused by: " + cause.getClass().getCanonicalName() + (msg == null ? "" : ": " + msg));
            cause = cause.getCause();
        }
    }

    public static void register(ScriptLanguage scriptLanguage)
    {
        scriptLanguages.put(scriptLanguage.getFileExtension(), scriptLanguage);
    }

    public static String getFileExtension(File file)
    {
        String tmp = file.getName();
        int i = tmp.lastIndexOf('.');

        if (i > 0 &&  i < tmp.length() - 1) {
            return tmp.substring(i+1).toLowerCase();
        }

        return null;
    }
}
