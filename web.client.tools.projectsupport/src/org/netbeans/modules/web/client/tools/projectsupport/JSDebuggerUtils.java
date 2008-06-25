/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.projectsupport;

import java.util.Collection;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * Utility class that allows access to a specific browser, regardless of the
 * default browser settings in NetBeans.  This is intended to be used for invoking
 * the JavaScript debugger.
 * 
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class JSDebuggerUtils {
    private static final String CLIENT_DEBUG_PROP = "client_debug"; // NOI18N
    private static final String SERVER_DEBUG_PROP = "server_debug"; // NOI18N
    
    private static final boolean CLIENT_DEBUG_DEFAULT = false;
    private static final boolean SERVER_DEBUG_DEFAULT = true;
    
    
    public static HtmlBrowser.Factory getFirefoxBrowser() {
        return findBrowser("org.netbeans.modules.extbrowser.FirefoxBrowser"); // NOI18N
    }
    
    public static HtmlBrowser.Factory getInternetExplorerBrowser() {
        return findBrowser("org.netbeans.modules.extbrowser.IExplorerBrowser"); // NOI18N
    }
    
    public static boolean getClientDebugProperty(Project project) {
        return getProjectProperty(project, CLIENT_DEBUG_PROP, CLIENT_DEBUG_DEFAULT);
    }

    public static boolean getServerDebugProperty(Project project) {
        return getProjectProperty(project, SERVER_DEBUG_PROP, SERVER_DEBUG_DEFAULT);
    }
    
    private static boolean getProjectProperty(Project project, String propKey, boolean def) {
        Preferences prefs = ProjectUtils.getPreferences(project, JSDebuggerUtils.class, true);
        assert prefs != null;
        
        return prefs.getBoolean(propKey, def);
    }

    static void setProjectProperties(final Project project, final boolean serverDebug, final boolean clientDebug) {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {

                public Boolean run() throws BackingStoreException {
                    Preferences prefs = ProjectUtils.getPreferences(project, JSDebuggerUtils.class, true);
                    assert prefs != null;

                    boolean currentServerValue = prefs.getBoolean(SERVER_DEBUG_PROP, SERVER_DEBUG_DEFAULT);
                    boolean currentClientValue = prefs.getBoolean(CLIENT_DEBUG_PROP, CLIENT_DEBUG_DEFAULT);
                    boolean changed = false;
                    
                    if (currentServerValue != serverDebug) {
                        prefs.putBoolean(SERVER_DEBUG_PROP, serverDebug);
                        changed = true;
                    }
                    if (currentClientValue != clientDebug) {
                        prefs.putBoolean(CLIENT_DEBUG_PROP, clientDebug);
                        changed = true;
                    }
                    
                    if (changed) {
                        prefs.sync();
                    }

                    return Boolean.TRUE;
                }
            });
        } catch (MutexException ex) {
            Log.getLogger().log(Level.SEVERE, "Unable to set javascript debugger project properties", ex);
        }
    }
    
    private static HtmlBrowser.Factory findBrowser(String browserClass) {
        Collection<? extends HtmlBrowser.Factory> htmlBrowserFactories = Lookup.getDefault().lookupAll(HtmlBrowser.Factory.class);
        for (HtmlBrowser.Factory factory : htmlBrowserFactories) {
            if (factory.getClass().getName().equals(browserClass)) {
                return factory;
            }
        }
        return null;
    }
    
    
}
