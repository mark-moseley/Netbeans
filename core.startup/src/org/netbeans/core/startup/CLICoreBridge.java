/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.CLIHandler;
import org.netbeans.Module;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Handler for core.jar options.
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.CLIHandler.class)
public class CLICoreBridge extends CLIHandler {
    /**
     * Create a default handler.
     */
    public CLICoreBridge() {
        super(WHEN_INIT);
    }
    
    protected int cli(Args arguments) {
        Lookup clis = Lookup.getDefault();
        Collection handlers = clis.lookupAll(CLIHandler.class);
        int h = notifyHandlers(arguments, handlers, WHEN_EXTRA, true, true);
        if (h == 0) {
            h = CoreBridge.getDefault().cli(
                arguments.getArguments(),
                arguments.getInputStream(),
                arguments.getOutputStream(),
                arguments.getErrorStream(),
                arguments.getCurrentDirectory()
            );
        }
        return h;
    }

    protected void usage(PrintWriter w) {
        if (MainLookup.isStarted()) {
            Lookup clis = Lookup.getDefault();
            Collection handlers = clis.lookupAll(CLIHandler.class);
            showHelp(w, handlers, WHEN_EXTRA);
            w.flush();
            return;
        }
        
        ModuleSystem moduleSystem;
        try {
            moduleSystem = new ModuleSystem(FileUtil.getConfigRoot().getFileSystem());
        } catch (IOException ioe) {
            // System will be screwed up.
            throw (IllegalStateException) new IllegalStateException("Module system cannot be created").initCause(ioe); // NOI18N
        }

//        moduleSystem.loadBootModules();
        moduleSystem.readList();
        
        
        ArrayList<URL> urls = new ArrayList<URL>();
        for (Module m : moduleSystem.getManager().getModules()) {
            for (File f : m.getAllJars()) {
                try {
                    urls.add(f.toURI().toURL());
                }
                catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
        MainLookup.systemClassLoaderChanged(loader);
        Lookup clis = Lookup.getDefault();
        Collection handlers = clis.lookupAll(CLIHandler.class);
        showHelp(w, handlers, WHEN_EXTRA);
        w.flush();
    }
}
