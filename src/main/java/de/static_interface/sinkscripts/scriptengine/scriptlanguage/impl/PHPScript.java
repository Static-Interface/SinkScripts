/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
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

package de.static_interface.sinkscripts.scriptengine.scriptlanguage.impl;

import com.caucho.quercus.QuercusContext;
import com.caucho.quercus.script.QuercusScriptEngine;
import com.caucho.vfs.FilePath;
import de.static_interface.sinkscripts.scriptengine.scriptcontext.ScriptContext;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.script.ScriptEngine;

public class PHPScript extends ScriptEngineScript {
    public PHPScript(Plugin plugin) {
        super(plugin, "php", "php", "php");
    }

    @Override
    public String formatCode(String code) {
        return code;
    }

    @Override
    public String getDefaultImports(ScriptContext context) {
        return "<?php ";
        //String nl = Util.getNewLine();
        //return "<?php" + nl +
        //       "import de.static_interface.sinklibrary.*;" + nl +
        //       "import de.static_interface.sinklibrary.user.*;" + nl +
        //       "import de.static_interface.sinklibrary.api.*;" + nl +
        //       "import de.static_interface.sinklibrary.api.user.*;" + nl +
        //       "import de.static_interface.sinkscripts.*;" + nl +
        //       "import org.bukkit.block.*;" + nl +
        //       "import org.bukkit.event.*;" + nl +
        //       "import org.bukkit.entity.*;" + nl +
        //       "import org.bukkit.inventory.*;" + nl +
        //       "import org.bukkit.material.*;" + nl +
        //       "import org.bukkit.potion.*; " + nl +
        //       "import org.bukkit.util.*" + nl +
        //       "import org.bukkit.*;" + nl +
        //       "import javax.script.*;" + nl + nl;
    }

    @Override
    public Collection<String> getImportIdentifiers() {
        return new ArrayList<>();
    }

   @Override
   public ScriptEngine createExecutor(final ScriptContext context) {
       QuercusScriptEngine engine = (QuercusScriptEngine) super.createExecutor(context);
       QuercusContext quercus;
       try {
           Method m = engine.getClass().getDeclaredMethod("getQuercus");
           m.setAccessible(true);
           quercus = (QuercusContext) m.invoke(engine);
           quercus.setWorkDir(new FilePath(SCRIPTLANGUAGE_DIRECTORY.getCanonicalPath()));
           quercus.init();
       } catch (Exception e) {
           throw new RuntimeException(e);
           //Util.reportException(context.getUser(), e);
       }
       return engine;
   }
}