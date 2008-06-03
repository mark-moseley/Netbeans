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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import javax.swing.JDialog;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
 * Test of issue 131128. When preferences are not writable for some reason (e.g.
 * read-only file), uihandler module must not cycle in throwing exceptions.
 * It must show just one dialog with real cause.
 * 
 * @author Jiri Skrivanek
 */
public class WritablePreferences131128Test extends TestCase {

    public void testPreferencies() throws Exception {
        // it works only if this class extends plain TestCase
        System.setProperty("java.util.prefs.PreferencesFactory", "org.netbeans.modules.uihandler.WritablePreferences131128Test$MyPreferencesFactory");//NOI18N
        MockServices.setServices(Displayer.class);
        UIHandler.registerExceptionHandler(true);
        Installer o = Installer.findObject(Installer.class, true);
        assertNotNull("Installer wasn't created.", o);
        o.restored();

        Preferences prefs = NbPreferences.forModule(WritablePreferences131128Test.class);
        prefs.putBoolean("anything", true);
        Thread.sleep(1000);
        assertEquals("Musn't cycle when preferences are not writable.", 3, MyPreferencesFactory.flushSpiCount);
        assertTrue("DialogDisplayer.notify not called to inform user about not writable preferences.", Displayer.notifyCalled);
    }

    public static final class Displayer extends DialogDisplayer {

        public static boolean notifyCalled = false;
        
        public Object notify(NotifyDescriptor descriptor) {
            notifyCalled = true;
            return NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            return new JDialog() {

                @Override
                public void setVisible(boolean v) {
                }
            };
        }
    }

    public static class MyPreferencesFactory implements PreferencesFactory {

        public static int flushSpiCount = 0;

        /** Creates a new instance  */
        public MyPreferencesFactory() {
        }

        public Preferences userRoot() {
            return NbPreferences.userRootImpl();
        }

        public Preferences systemRoot() {
            return NbPreferences.systemRootImpl();
        }

        private static class NbPreferences extends AbstractPreferences {

            private static Preferences USER_ROOT;
            private static Preferences SYSTEM_ROOT;
            /*private*/
            Properties properties;

            static Preferences userRootImpl() {
                if (USER_ROOT == null) {
                    USER_ROOT = new NbPreferences();
                }
                return USER_ROOT;
            }

            static Preferences systemRootImpl() {
                if (SYSTEM_ROOT == null) {
                    SYSTEM_ROOT = new NbPreferences();
                }
                return SYSTEM_ROOT;
            }

            private NbPreferences() {
                super(null, "");
            }

            /** Creates a new instance of PreferencesImpl */
            private NbPreferences(NbPreferences parent, String name) {
                super(parent, name);
                newNode = true;
            }

            protected final String getSpi(String key) {
                return properties().getProperty(key);
            }

            protected final String[] childrenNamesSpi() throws BackingStoreException {
                return new String[0];
            }

            protected final String[] keysSpi() throws BackingStoreException {
                return properties().keySet().toArray(new String[0]);
            }

            protected final void putSpi(String key, String value) {
                properties().put(key, value);
                try {
                    flushSpi();
                } catch (BackingStoreException ex) {
                    Logger.getLogger(MyPreferencesFactory.class.getName()).log(Level.SEVERE, "putSpi", ex);
                }
            }

            protected final void removeSpi(String key) {
                properties().remove(key);
            }

            protected final void removeNodeSpi() throws BackingStoreException {
            }

            protected void flushSpi() throws BackingStoreException {
                flushSpiCount++;
                throw new BackingStoreException("Cannot write to storage");
            }

            protected void syncSpi() throws BackingStoreException {
                properties().clear();
            }

            @Override
            public void put(String key, String value) {
                try {
                    super.put(key, value);
                } catch (IllegalArgumentException iae) {
                    if (iae.getMessage().contains("too long")) {
                        // Not for us!
                        putSpi(key, value);
                    } else {
                        throw iae;
                    }
                }
            }

            Properties properties() {
                if (properties == null) {
                    properties = new Properties();
                }
                return properties;
            }

            protected AbstractPreferences childSpi(String name) {
                return new NbPreferences(this, name);
            }
        }
    }
}
