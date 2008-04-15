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
package org.openide;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.junit.NbTestSuite;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.*;
import org.openide.util.HelpCtx;

/** Test coveres implementation of issue 58530 - Background wizard validation.
 * @author Jiri Rechtacek
 */
public class AsynchronousValidatingPanelTest extends LoggingTestCaseHid {

    public AsynchronousValidatingPanelTest (String name) {
        super(name);
    }
    
    WizardDescriptor wd;
    String exceptedValue;

    private ErrorManager err;

    protected final void setUp () {
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[2];
        panels[0] = new Panel("first panel");
        panels[1] = new Panel("second panel");
        wd = new WizardDescriptor(panels);
        wd.addPropertyChangeListener(new Listener());
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog (wd);
        //d.show();
        err = ErrorManager.getDefault ().getInstance ("test-" + getName ());
    }
    
    public void testAsynchronousLazyValidation () throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }
        Panel panels[] = new Panel[3];
        
        class MyPanel extends Panel implements WizardDescriptor.AsynchronousValidatingPanel {
            public String validateMsg;
            public String failedMsg;
            public boolean running;
            public boolean wasEnabled;
            
            public MyPanel () {
                super ("enhanced panel");
            }
            
            public void prepareValidation () {
                running = true;
            }
            
            public synchronized void validate () throws WizardValidationException {
                err.log ("validate() entry.");
                wasEnabled = wd.isNextEnabled () || wd.isFinishEnabled ();
                running = false;
                notifyAll ();
                if (validateMsg != null) {
                    failedMsg = validateMsg;
                    err.log ("Throw WizardValidationException.");
                    throw new WizardValidationException (null, "MyPanel.validate() failed.", validateMsg);
                }
                err.log ("validate() exit.");
                return;
            }
        }
        
        class MyFinishPanel extends MyPanel implements WizardDescriptor.FinishablePanel {
            public boolean isFinishPanel () {
                return true;
            }
        }
        
        MyPanel mp = new MyPanel ();
        MyFinishPanel mfp = new MyFinishPanel ();
        panels[0] = mp;
        panels[1] = mfp;
        panels[2] = new Panel ("Last one");
        wd = new WizardDescriptor(panels);
        
        assertNull ("Component has not been yet initialized", panels[1].component);
        mp.failedMsg = null;
        mp.validateMsg = "xtest-fail-without-msg";
        assertTrue ("Next button must be enabled.", wd.isNextEnabled ());
        err.log ("Do Next. Validation will run with: validateMsg=" + mp.validateMsg + ", failedMsg=" + mp.failedMsg);
        synchronized (mp) {
            wd.doNextClick ();
            assertTrue ("Validation runs.", mp.running);
            assertFalse ("Wizard is not valid when validation runs.",  wd.isForwardEnabled ());
            // let's wait till wizard is valid
            while (mp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mp.wait ();
            }
        }
        waitWhileSetValidDone ();
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is ready to next validation however previous failed.",  wd.isForwardEnabled ());
        assertEquals ("The lazy validation failed on Next.", mp.validateMsg, mp.failedMsg);
        assertNull ("The lazy validation failed, still no initialiaation", panels[1].component);
        assertNull ("The lazy validation failed, still no initialiaation", panels[2].component);
        mp.failedMsg = null;
        mp.validateMsg = null;
        synchronized (mp) {
            err.log ("Do Next. Validation will run with: validateMsg=" + mp.validateMsg + ", failedMsg=" + mp.failedMsg);
            assertTrue ("Wizard is valid before validation.",  wd.isForwardEnabled ());
            wd.doNextClick ();
            assertTrue ("Validation runs.", mp.running);
            while (mp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mp.wait ();
            }
        }
        waitWhileSetValidDone ();
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is valid when validation passes.",  wd.isForwardEnabled ());
        assertNull ("Validation on Next passes", mp.failedMsg);
        assertNotNull ("Now we switched to another panel", panels[1].component);
        assertNull ("The lazy validation failed, still no initialiaation", panels[2].component);
        
        // remember previous state
        Object state = wd.getValue();
        mfp.validateMsg = "xtest-fail-without-msg";
        mfp.failedMsg = null;
        synchronized (mfp) {
            err.log ("Do Finish. Validation will run with: validateMsg=" + mfp.validateMsg + ", failedMsg=" + mfp.failedMsg);
            wd.doFinishClick();
            while (mfp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mfp.wait ();
            }
        }
        waitWhileSetValidDone ();
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is ready to next validation.",  wd.isForwardEnabled ());
        assertEquals ("The lazy validation failed on Finish.", mfp.validateMsg, mfp.failedMsg);
        assertNull ("The validation failed, still no initialiaation", panels[2].component);
        assertEquals ("State has not changed", state, wd.getValue ());
        
        mfp.validateMsg = null;
        mfp.failedMsg = null;
        synchronized (mfp) {
            err.log ("Do Finish. Validation will run with: validateMsg=" + mfp.validateMsg + ", failedMsg=" + mfp.failedMsg);
            wd.doFinishClick ();
            while (mfp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mfp.wait ();
            }
        }
        waitWhileSetValidDone ();
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is valid when validation passes.",  wd.isForwardEnabled ());
        assertNull ("Validation on Finish passes", mfp.failedMsg);        
        assertNull ("Finish was clicked, no initialization either", panels[2].component);
        assertEquals ("The state is finish", WizardDescriptor.FINISH_OPTION, wd.getValue ());
    }
    
    public class Panel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
        private JLabel component;
        private String text;
        public Panel(String text) {
            this.text = text;
        }
        
        public Component getComponent() {
            if (component == null) {
                component = new JLabel (text);
            }
            return component;
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public HelpCtx getHelp() {
            return null;
        }
        
        public boolean isValid() {
            return true;
        }
        
        public boolean isFinishPanel () {
            return true;
        }
        
        public void readSettings(Object settings) {
            log ("readSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void storeSettings(Object settings) {
            log ("storeSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
            if (exceptedValue != null) {
                assertEquals ("WD.getValue() returns excepted value.", exceptedValue, handleValue (wd.getValue ()));
            }
        }
        
    }
    
    public class Listener implements java.beans.PropertyChangeListener {
        
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            if (WizardDescriptor.PROP_VALUE.equals(propertyChangeEvent.getPropertyName ())) {
                log("propertyChange [time: " + System.currentTimeMillis () +
                                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));

            }
        }
        
    }
    
    public String handleValue (Object val) {
        if (val == null) return "NULL";
        if (val instanceof String) return (String) val;
        if (WizardDescriptor.FINISH_OPTION.equals (val)) return "FINISH_OPTION";
        if (WizardDescriptor.CANCEL_OPTION.equals (val)) return "CANCEL_OPTION";
        if (WizardDescriptor.CLOSED_OPTION.equals (val)) return "CLOSED_OPTION";
        if (val instanceof JButton) {
            JButton butt = (JButton) val;
            ResourceBundle b = NbBundle.getBundle ("org.openide.Bundle"); // NOI18N
            if (b.getString ("CTL_NEXT").equals (butt.getText ())) return "NEXT_OPTION";
            if (b.getString ("CTL_PREVIOUS").equals (butt.getText ())) return "NEXT_PREVIOUS";
            if (b.getString ("CTL_FINISH").equals (butt.getText ())) return "FINISH_OPTION";
            if (b.getString ("CTL_CANCEL").equals (butt.getText ())) return "CANCEL_OPTION";
        }
        return "UNKNOWN OPTION: " + val;
    }

    private void waitWhileSetValidDone () {
        err.log ("Start waitWhileSetValidDone.");
        try {
            WizardDescriptor.ASYNCHRONOUS_JOBS_RP.post (new Runnable () {
                public void run () {
                }
            }).waitFinished ();
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                }
            });
        } catch (InterruptedException ex) {
            fail (ex.getMessage ());
        } catch (InvocationTargetException ex) {
            fail (ex.getMessage ());
        }
        err.log ("End of waitWhileSetValidDone.");
    }

}
