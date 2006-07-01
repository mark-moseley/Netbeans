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
package org.openide.awt;

import java.awt.Component;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** Tests of actions related methods in Utilities class.
 */
public class UtilitiesActionsTest extends NbTestCase {

    public UtilitiesActionsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(UtilitiesActionsTest.class));
    }
    
    public void testDynamicMenuContent() {
        Action[] acts = new Action[] {
            new Act1(),
            new Act2()
            
        };
        JPopupMenu popup = Utilities.actionsToPopup(acts, Lookup.EMPTY);
        Component[] comp = popup.getComponents();
        boolean onepresent = false;
        boolean twopresent = false;
        boolean threepresent = false;
        boolean fourpresent = false;
        boolean zeropresent = false;
        for (int i = 0; i < comp.length; i++) {
            if ("0".equals(((JMenuItem)comp[i]).getText())) {
                zeropresent = true;
            }
            if ("2".equals(((JMenuItem)comp[i]).getText())) {
                twopresent = true;
            }
            if ("4".equals(((JMenuItem)comp[i]).getText())) {
                fourpresent = true;
            }
            if ("3".equals(((JMenuItem)comp[i]).getText())) {
                threepresent = true;
            }
            if ("1".equals(((JMenuItem)comp[i]).getText())) {
                onepresent = true;
            }
        }
        assertTrue(threepresent);
        assertTrue(fourpresent);
        assertTrue(onepresent);
        assertTrue(twopresent);
        assertTrue(zeropresent);
    }
    
    private class Act1 extends AbstractAction implements Presenter.Popup, Presenter.Menu {
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        }
        
        public JMenuItem getPopupPresenter() {
            return new JMenuItem("0");
        }
        
        public JMenuItem getMenuPresenter() {
            return new JMenuItem("0");
        }
    }
    
    private class Act2 extends AbstractAction implements Presenter.Menu, Presenter.Popup {
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        }
        public JMenuItem getPopupPresenter() {
            return new Dyna();
        }
        
        public JMenuItem getMenuPresenter() {
            return new Dyna();
        }
    }
    
    private class Dyna extends JMenuItem implements DynamicMenuContent {
        private JMenuItem itm1;
        private JMenuItem itm2;
        public JComponent[] getMenuPresenters() {
            itm1 = new JMenuItem();
            itm1.setText("1");
            itm2 = new Dyna2();
            itm2.setText("2");
            return new JComponent[] {
                itm1,
                itm2
            };
        }
    
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            ((JMenuItem)items[0]).setText("1x");
            ((JMenuItem)items[1]).setText("2x");
            return items;
        }
    }
    
    private class Dyna2 extends JMenuItem implements DynamicMenuContent {
        private JMenuItem itm1;
        private JMenuItem itm2;
        public JComponent[] getMenuPresenters() {
            itm1 = new JMenuItem();
            itm1.setText("3");
            itm2 = new JMenuItem();
            itm2.setText("4");
            return new JComponent[] {
                itm1,
                itm2,
                this
            };
        }
    
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            ((JMenuItem)items[0]).setText("3x");
            ((JMenuItem)items[1]).setText("4x");
            return items;
        }
    }
    
}
