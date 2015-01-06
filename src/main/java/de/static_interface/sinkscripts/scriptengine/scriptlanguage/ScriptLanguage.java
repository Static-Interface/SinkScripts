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

package de.static_interface.sinkscripts.scriptengine.scriptlanguage;

import static de.static_interface.sinkscripts.SinkScripts.*;

import de.static_interface.sinklibrary.*;
import de.static_interface.sinklibrary.api.user.*;
import de.static_interface.sinkscripts.scriptengine.*;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.*;
import de.static_interface.sinkscripts.util.*;
import org.bukkit.plugin.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public abstract class ScriptLanguage {

    public File SCRIPTLANGUAGE_DIRECTORY;
    public File FRAMEWORK_FOLDER;
    public File AUTOSTART_DIRECTORY;
    protected String fileExtension;
    protected Plugin plugin;
    protected String name;
    private ScriptContext consoleContext;

    public ScriptLanguage(Plugin plugin, String name, String fileExtension) {
        this.fileExtension = fileExtension.toLowerCase();
        this.plugin = plugin;
        this.name = name;
        SCRIPTLANGUAGE_DIRECTORY = new File(SCRIPTS_FOLDER, name);
        FRAMEWORK_FOLDER = new File(SCRIPTLANGUAGE_DIRECTORY, "framework");
        AUTOSTART_DIRECTORY = new File(SCRIPTLANGUAGE_DIRECTORY, "autostart");
        if ((!SCRIPTLANGUAGE_DIRECTORY.exists() && !SCRIPTLANGUAGE_DIRECTORY.mkdirs())
            || (!FRAMEWORK_FOLDER.exists() && !FRAMEWORK_FOLDER.mkdirs())
            || (!AUTOSTART_DIRECTORY.exists() && !AUTOSTART_DIRECTORY.mkdirs())) {
            throw new RuntimeException(getName() + ": Couldn't create required directories!");
        }
    }

    public String getName() {
        return name;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public abstract String formatCode(String code);

    public Object run(ScriptContext context, String code, boolean skipImports, boolean clear) {
        SinkLibrary.getInstance().getCustomLogger().logToFile(Level.INFO, context.getUser().getName() + " executed script: " + code);

        if (!skipImports) {
            code = onUpdateImports(code);
        }

        Object result = eval(context, code);

        if (clear) {
            context.setCode("");
        }

        return result;
    }

    protected Object run(ScriptContext context, File file) {
        SinkLibrary.getInstance().getCustomLogger().logToFile(Level.INFO, context.getUser().getName() + " executed script file: " + file);

        setVariable(context, "scriptfile", file);
        try {
            return eval(context, onUpdateImports(Util.loadFile(file)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Object eval(ScriptContext context, String code);

    /**
     * @param commandArgs String[] you want to convert to value
     * @return primite Value
     */

    public Object getValue(String[] commandArgs) {
        String commandArg = commandArgs[0];

        if (commandArg.equalsIgnoreCase("null")) {
            return null;
        }

        try {
            Long l = Long.parseLong(commandArg);
            if (l <= Byte.MAX_VALUE) {
                return Byte.parseByte(commandArg);
            } else if (l <= Short.MAX_VALUE) {
                return Short.parseShort(commandArg); // Value is a Short
            } else if (l <= Integer.MAX_VALUE) {
                return Integer.parseInt(commandArg); // Value is an Integer
            }
            return l; // Value is a Long
        } catch (Exception ignored) {
        }

        try {
            return Float.parseFloat(commandArg); // Value is Float
        } catch (Exception ignored) {
        }

        try {
            return Double.parseDouble(commandArg); // Value is Double
        } catch (Exception ignored) {
        }

        //Parse Booleans
        if (commandArg.equalsIgnoreCase("true") || commandArg.equals("1")) {
            return true;
        } else if (commandArg.equalsIgnoreCase("false") || commandArg.equals("0")) {
            return false;
        }

        if (commandArg.startsWith("'") && commandArg.endsWith("'") && commandArg.length() == 3) {
            return commandArg.toCharArray()[1]; // Value is char
        }

        String tmp = "";
        for (String s : commandArgs) {
            if (tmp.equals("")) {
                tmp = s;
            } else {
                tmp += " " + s;
            }
        }
        if (tmp.startsWith("\"") && tmp.endsWith("\"")) {
            StringBuilder b = new StringBuilder(tmp);
            b.replace(tmp.lastIndexOf("\""), tmp.lastIndexOf("\"") + 1, "");
            return b.toString().replaceFirst("\"", "");  // Value is a String
        }
        throw new IllegalArgumentException("Unknown value");
    }

    protected String onUpdateImports(String code) {
        String defaultImports = getDefaultImports();
        if (defaultImports == null) {
            return code;
        }
        code = code.replace(defaultImports, "");
        return defaultImports + code;
    }

    protected abstract String getDefaultImports();

    public abstract ScriptContext createNewShellInstance(SinkUser user);

    public abstract void setVariable(ScriptContext context, String name, Object value);

    public abstract List<String> getImportIdentifier();

    public void onAutoStart() {
        autoStartRecur(AUTOSTART_DIRECTORY);
    }

    private void autoStartRecur(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                autoStartRecur(file);
            } else {
                if (!Util.getFileExtension(file).equals(getFileExtension())) {
                    continue;
                }

                ScriptHandler.setVariables(getConsoleContext());

                run(getConsoleContext(), file);
            }
        }
    }

    public void preInit() {
        try {
            onPreInit();
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }

    /**
     * Called before the libraries are loaded, you can e.g. setuo enviroment values here
     * Don't try to call the library or classes from it
     */
    public void onPreInit() {
    }


    public ScriptContext getConsoleContext() {
        if (consoleContext == null) {
            consoleContext = createNewShellInstance(SinkLibrary.getInstance().getConsoleUser());
        }
        return consoleContext;
    }

    public final void init() {
        try {
            onInit();
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }

    /**
     * Called when the libraries has been loaded, you can setup the language here
     */
    public void onInit() {
    }
}
