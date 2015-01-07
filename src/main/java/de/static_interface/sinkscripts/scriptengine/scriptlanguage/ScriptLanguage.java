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

import static de.static_interface.sinkscripts.SinkScripts.SCRIPTS_FOLDER;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinkscripts.scriptengine.ScriptHandler;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import de.static_interface.sinkscripts.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public abstract class ScriptLanguage<T> {
    public File SCRIPTLANGUAGE_DIRECTORY;
    public File FRAMEWORK_FOLDER;
    public File AUTOSTART_DIRECTORY;
    protected String fileExtension;
    protected Plugin plugin;
    protected String name;

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

    protected abstract Object eval(ScriptContext context, String code);

    /**
     * @param args String[] you want to convert to value
     * @return primite Value
     */

    public Object getValue(String[] args, SinkUser user) {
        String arg = args[0];

        try {
            Long l = Long.parseLong(arg);
            if (l <= Byte.MAX_VALUE) {
                return Byte.parseByte(arg);
            } else if (l <= Short.MAX_VALUE) {
                return Short.parseShort(arg); // Value is a Short
            } else if (l <= Integer.MAX_VALUE) {
                return Integer.parseInt(arg); // Value is an Integer
            }
            return l; // Value is a Long
        } catch (NumberFormatException ignored) {
        }

        try {
            return Float.parseFloat(arg); // Value is Float
        } catch (NumberFormatException ignored) {
        }

        try {
            return Double.parseDouble(arg); // Value is Double
        } catch (NumberFormatException ignored) {
        }

        //Parse Booleans
        if (arg.equalsIgnoreCase("true")) {
            return true;
        } else if (arg.equalsIgnoreCase("false")) {
            return false;
        }

        if (arg.startsWith("'") && arg.endsWith("'") && arg.length() == 3) {
            return arg.toCharArray()[1]; // Value is char
        }


        String tmp = "";
        for (String s : args) {
            if (tmp.equals("")) {
                tmp = s;
            } else {
                tmp += " " + s;
            }
        }

        if(tmp.startsWith("[") && tmp.endsWith("]")){
            List<String> list = Arrays.asList(tmp.substring(1, tmp.length() - 1).split(", "));
            List<Object> arrayList = new ArrayList<>();
            for(String s : list) {
                arrayList.add(getValue(s.split(" "), user));
            }
            return arrayList.toArray(new Object[arrayList.size()]);
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

    public abstract String getDefaultImports();

    public abstract void setVariable(ScriptContext context, String name, Object value);

    public abstract Collection<String> getImportIdentifiers();

    public void onAutoStart(ScriptContext context) {
        autoStartRecur(AUTOSTART_DIRECTORY, context);
    }

    public abstract T createExecutor();

    private void autoStartRecur(File directory, ScriptContext context) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    autoStartRecur(file, context);
                } else {
                    if (!Util.getFileExtension(file).equals(getFileExtension())) {
                        continue;
                    }

                    context.getUser().sendMessage(
                            ChatColor.DARK_GREEN + "[AutoStart] " + ChatColor.GOLD + getName() + ChatColor.WHITE + ": " + file.getName());
                    ScriptHandler.getInstance().setVariables(context);
                    run(context, file);
                }
            } catch (Throwable thr) {
                Util.reportException(context.getUser(), thr);
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
