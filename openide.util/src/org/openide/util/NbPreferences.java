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

package org.openide.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.openide.util.PreferencesProvider;

/**
 * Provides an implementation of the Preferences API which may be backed by
 * a NetBeans-specific implementation.
 * @see <a href="doc-files/preferences.html">Preferences API in NetBeans</a>
 * @since org.openide.util 7.4
 * @author Radek Matous
 */
public final class NbPreferences {
    private static PreferencesProvider PREFS_IMPL;
    
    private  NbPreferences() {}
    
    /**
     * Returns user preference node . {@link Preferences#absolutePath} of such
     * a node depends whether class provided as a parameter was loaded as a part of any module
     * or not. If so, then absolute path corresponds to slashified code name base of module.
     * If not, then absolute path corresponds to class's package.
     *
     * @param cls the class for which a user preference node is desired.
     * @return the user preference node
     */
    public static Preferences forModule(Class cls) {
          if (PREFS_IMPL == null) {
                PREFS_IMPL = getPreferencesProvider();
          }
          return PREFS_IMPL.preferencesForModule(cls);
    }
    
    /**
     * Returns the root preference node.
     *
     * @return the root preference node.
     */
    public static Preferences root() {
          if (PREFS_IMPL == null) {
                PREFS_IMPL = getPreferencesProvider();
          }
          return PREFS_IMPL.preferencesRoot();
    }    
         
    private static PreferencesProvider getPreferencesProvider() {
        PreferencesProvider retval = Lookup.getDefault().lookup(PreferencesProvider.class);
        if (retval == null) {
             retval = new PreferencesProvider() {
                  public Preferences preferencesForModule(Class cls) {
                       return Preferences.userNodeForPackage(cls);
                  }

                  public Preferences preferencesRoot() {
                       return Preferences.userRoot();
                  }                         
             };
             // Avoided warning in case it is set 
             //(e.g. from NbTestCase - org.netbeans.junit.internal.MemoryPreferencesFactory).
             String prefsFactory = System.getProperty("java.util.prefs.PreferencesFactory");//NOI18N
             if (!"org.netbeans.junit.internal.MemoryPreferencesFactory".equals(prefsFactory)) {//NOI18N
                 Logger logger = Logger.getLogger(NbPreferences.class.getName());
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 new Exception().printStackTrace(new PrintStream(bos));
                 logger.log(prefsFactory == null ? Level.WARNING : Level.FINE,
                         "NetBeans implementation of Preferences not found: " + bos.toString() );//NOI18N
             } 
        }
        return retval;
    }    
}
