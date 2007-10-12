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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.core.windows.services;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/** Tests issue 96282 - Memory leak in org.netbeans.core.windows.services.NbPresenter
 *
 * @author Jiri Rechtacek
 */
public class NbPresenterLeakTest extends NbTestCase {

    public NbPresenterLeakTest (String testName) {
        super (testName);
    }

    protected boolean runInEQ () {
        return false;
    }

    public void testLeakingNbPresenterDescriptor () throws InterruptedException, InvocationTargetException {
        WizardDescriptor wizardDescriptor = new WizardDescriptor (getPanels ());
        wizardDescriptor.setModal (false);
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        WeakReference<WizardDescriptor> w = new WeakReference<WizardDescriptor> (wizardDescriptor);
        
        SwingUtilities.invokeAndWait (new EDTJob(dialog, true));
        SwingUtilities.invokeAndWait (new EDTJob(dialog, false));
        boolean cancelled = wizardDescriptor.getValue() !=
            WizardDescriptor.FINISH_OPTION;
        Dialog d = new JDialog();
        
        // workaround for JDK bug 6575402
        JPanel p = new JPanel();
        d.setLayout(new BorderLayout());
        d.add(p, BorderLayout.CENTER);
        JButton btn = new JButton("Button");
        p.add(btn, BorderLayout.NORTH);
        
        SwingUtilities.invokeAndWait (new EDTJob(d, true));
        Thread.sleep(1000); // let it actually paint
        SwingUtilities.invokeAndWait (new EDTJob(d, false));

        //assertNull ("BufferStrategy was disposed.", dialog.getBufferStrategy ());
        
        dialog = null;
        wizardDescriptor = null;
        
        assertGC ("Dialog disappears.", w);
    }
    
    private static class EDTJob implements Runnable {
        private Dialog d;
        private boolean visibility;
        
        EDTJob (Dialog d, boolean vis) {
            this.d = d;
            visibility = vis;
        }
        public void run() {
            d.setVisible(visibility);
            if (!visibility) {
                d.dispose();
            }
        }
    }
    
    private WizardDescriptor.Panel [] getPanels () {
        WizardDescriptor.Panel p1 = new WizardDescriptor.Panel () {
            public Component getComponent() {
                return new JLabel ("test");
            }

            public HelpCtx getHelp() {
                return null;
            }

            public void readSettings(Object settings) {
            }

            public void storeSettings(Object settings) {
            }

            public boolean isValid() {
                return true;
            }

            public void addChangeListener(ChangeListener l) {
            }

            public void removeChangeListener(ChangeListener l) {
            }
        };
        
        return new WizardDescriptor.Panel [] {p1};
    }
    
    
    
}
