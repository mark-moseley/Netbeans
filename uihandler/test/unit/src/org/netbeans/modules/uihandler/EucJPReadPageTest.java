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

package org.netbeans.modules.uihandler;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import junit.framework.*;
import java.util.Locale;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach
 */
public class EucJPReadPageTest extends NbTestCase {
    
    public EucJPReadPageTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        // setup the listing
        installer.restored();
        
        Locale.setDefault(new Locale("te", "ST"));
        
        DD.d = null;
        MockServices.setServices(DD.class);
    }

    @Override
    protected void tearDown() throws Exception {
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.close();
    }
    
    public void testKFranksFile() throws Exception {
        String jaText = "\u30b3\u30de\u30f3\u30c9";
        
        InputStream is = getClass().getResourceAsStream("index_ja.html");
        assertNotNull("index_ja found", is);
        
        MemoryURL.registerURL("memory://kun.html", is);
        
        boolean res = Installer.displaySummary("KUN", true, false,true);
        assertFalse("Close options was pressed", res);
        assertNotNull("DD.d assigned", DD.d);
        
        List<Object> data = Arrays.asList(DD.d.getOptions());
        assertEquals("three objects: " + data, 3, DD.d.getOptions().length);
        for (Object o : DD.d.getOptions()) {
            assertEquals("is jbutton", JButton.class, o.getClass());
            JButton b = (JButton)o;
            String t = b.getText();
            
            if (t.indexOf(jaText) == -1) {
                failUTF("Expecting the right text (" + jaText + ": " + t);
            }
        }
        
    }
    
    private static void failUTF(String err) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < err.length(); i++) {
            if (err.charAt(i) < 128) {
                sb.append(err.charAt(i));
            } else {
                sb.append("\\u" + Integer.toString(err.charAt(i), 16));
            }
        }
        fail(sb.toString());
    }
    
    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor d;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            return NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            
            return new DialogImpl(d, new Frame());
        }

        private static class DialogImpl extends Dialog 
        implements PropertyChangeListener {
            NotifyDescriptor d;
            
            private DialogImpl(NotifyDescriptor d, Frame owner) {
                super(owner);
                this.d = d;
            }

            @java.lang.Override
            public synchronized void setVisible(boolean b) {
                assertFalse(isModal());
            }

            public synchronized void propertyChange(PropertyChangeEvent evt) {
                if (d != null && d.getOptions().length == 2) {
                    d.setValue(NotifyDescriptor.CLOSED_OPTION);
                    d = null;
                    notifyAll();
                }
            }
        }
        
    }
}
