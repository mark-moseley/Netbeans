/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * TestSuiteTargetPanel.java
 *
 * Created on April 10, 2002, 1:46 PM
 */

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestSuiteTargetPanel extends javax.swing.JPanel implements WizardDescriptor.Panel {
    
    /** Creates new form TestSuitePanel1 */
    public TestSuiteTargetPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        
        setLayout(new java.awt.BorderLayout());
        
    }//GEN-END:initComponents

    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }    
    
    public java.awt.Component getComponent() {
        return this;
    }    
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(TestSuiteTargetPanel.class);
    }
    
    public void readSettings(Object obj) {
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener changeListener) {
    }
    
    public void storeSettings(Object obj) {
    }

    public boolean isValid() {
        return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
