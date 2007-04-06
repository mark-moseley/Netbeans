/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class NotifyLaterTest extends NbTestCase {
    public NotifyLaterTest (String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
    }

    protected boolean runInEQ () {
        return false;
    }
    private void waitAWT() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
    }
    
    protected NotifyDescriptor createDescriptor(Object msg) {
        return new NotifyDescriptor.Message(msg);
    }

    public void testIfLasterWhenSplashShownThanWaitTillItFinished() throws Exception {
        class MyObj extends JComponent {
            public int called;
            
            public void addNotify() {
                called = 1;
                super.addNotify();
            }
        }
        MyObj obj = new MyObj();
        
        NotifyDescriptor ownerDD = createDescriptor(obj);
        
        
        
        DialogDisplayer.getDefault ().notifyLater(ownerDD);
        waitAWT();
        assertEquals("No notify yet", 0, obj.called);
        
        DialogDisplayerImplTest.postInAwtAndWaitOutsideAwt(new Runnable () {
            public void run() {
                DialogDisplayerImpl.runDelayed();
            }
        });
        
        
        waitAWT();
        assertEquals("Now it is showing", 1, obj.called);
        
        assertTrue("Is visible", obj.isShowing());
        Window root = SwingUtilities.getWindowAncestor(obj);
        assertNotNull("There is parent window", root);
        assertTrue("It is a dialog", root instanceof JDialog);
        JDialog d = (JDialog)root;
        assertEquals("The owner of d is the same as owner of dialog without owner", new JDialog().getParent(), d.getParent());
        
        SwingUtilities.invokeAndWait(new Runnable () {
            public void run() {
                DialogDisplayerImpl.runDelayed();
            }
        });
    }
}
