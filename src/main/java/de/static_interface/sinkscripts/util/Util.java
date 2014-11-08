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

package de.static_interface.sinkscripts.util;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinkscripts.SinkScripts;
import de.static_interface.sinkscripts.scriptengine.scriptlanguage.ScriptLanguage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import javax.annotation.Nullable;
import javax.script.ScriptException;

public class Util {

    public static String getFileExtension(File file) {
        String tmp = file.getName();
        int i = tmp.lastIndexOf('.');

        if (i > 0 && i < tmp.length() - 1) {
            return tmp.substring(i + 1).toLowerCase();
        }

        return null;
    }

    public static String getNewLine() {
        return System.getProperty("line.separator");
    }

    public static void reportException(CommandSender sender, Throwable e) {
        if (e == null) {
            return;
        }

        SinkScripts.getInstance().getLogger().log(Level.WARNING, "Exception caused by " + sender.getName() + ": ");
        e.printStackTrace();

        //bad :(
        if(e instanceof RuntimeException && e.getCause() != null && e.getCause() instanceof ScriptException && e.getMessage().contains(e.getCause().getMessage())) {
            e = e.getCause();
        }

        while (e instanceof ScriptException) {
            if(e.getCause() == null) break;
            e = e.getCause();
        }

        String[] lines;
        String msg = ChatColor.DARK_RED + "Unexpected " + ((e instanceof Exception) ? "exception" : "error") + " (see console for more details): ";
        String source;
        StackTraceElement element;
        boolean first = true;
        while (e != null) {
            lines = null;

            if (e.getMessage() != null) {
                lines = e.getMessage().split(Util.getNewLine());
            }

            if (lines != null && lines.length > 0) {
                msg = msg + lines[0] + (lines.length > 1 ? Util.getNewLine() + lines[1] : "");
            }

            source = "";
            try {
                element = e.getStackTrace()[0];
                source = " (" + Class.forName(element.getClassName()).getSimpleName() + ".class:" + element.getLineNumber() + ")";
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            sender.sendMessage(ChatColor.RED + (first ? "" : "Caused by: ") + e.getClass().getCanonicalName() + (msg == null ? "" : ": " + msg) + source);
            if(first) first = false;
            e = e.getCause();
            msg = "";
        }
    }

    public static String loadFile(File scriptFile) throws IOException {
        String nl = Util.getNewLine();
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Couldn't find file: " + scriptFile);
        }
        BufferedReader br = new BufferedReader(new FileReader(scriptFile));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(nl);
            line = br.readLine();
        }
        return sb.toString();
    }

    @Nullable
    public static String loadFile(String scriptName, @Nullable ScriptLanguage language) throws IOException {
        File scriptFile = new File(language.SCRIPTLANGUAGE_DIRECTORY, scriptName + "." + language.getFileExtension());
        if (!scriptFile.exists()) {
            SinkLibrary.getInstance().getCustomLogger().debug("Couldn't find file: " + scriptFile.getAbsolutePath());
            scriptFile = Util.searchRecursively(scriptName, language.SCRIPTLANGUAGE_DIRECTORY, language);
        }
        return loadFile(scriptFile);
    }


    public static File searchRecursively(String scriptName, File directory, ScriptLanguage language) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                return searchRecursively(scriptName, file, language);
            } else {
                if (file.getName().equals(scriptName + "." + language.getFileExtension())) {
                    return file;
                }
            }
        }
        return null;
    }

}
