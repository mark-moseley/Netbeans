/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * Provides common support for a <em>standard</em> panels in the NetBeans module
 * customizer.
 *
 * @author Martin Krauskopf
 */
abstract class NbPropertyPanel extends JPanel implements ComponentFactory.StoragePanel {
    
    protected SingleModuleProperties props;
    
    /** Creates new NbPropertyPanel */
    NbPropertyPanel(final SingleModuleProperties props) {
        this.props = props;
        initComponents();
    }
    
    String getProperty(String key) {
        return props.getProperty(key);
    }
    
    void setProperty(String key, String property) {
        props.setProperty(key, property);
    }
    
    boolean getBooleanProperty(String key) {
        return props.getBooleanProperty(key);
    }
    
    void setBooleanProperty(String key, boolean property) {
        props.setBooleanProperty(key, property);
    }
    
    static void setText(JTextComponent textComp, String text) {
        textComp.setText(text);
        textComp.setCaretPosition(text == null ? 0 : text.length());
    }
    
    public void store() { /* empty implementation */ }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
