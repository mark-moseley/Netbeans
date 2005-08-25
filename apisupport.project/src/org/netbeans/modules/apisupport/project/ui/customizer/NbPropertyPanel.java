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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/**
 * Provides common support for a <em>standard</em> panels in the NetBeans module
 * and suite customizers.
 *
 * @author Martin Krauskopf
 */
abstract class NbPropertyPanel extends JPanel implements
        BasicCustomizer.LazyStorage, PropertyChangeListener {
    
    /** Property whether <code>this</code> panel is valid. */
    static final String VALID_PROPERTY = "isPanelValid"; // NOI18N
    
    /** Property for error message of this panel. */
    static final String ERROR_MESSAGE_PROPERTY = "errorMessage"; // NOI18N
    
    protected ModuleProperties props;
    
    /** Whether this panel is valid or not. */
    private boolean valid;
    
    /** Error message for this panel (may be null). */
    private String errMessage;
    
    /** Creates new NbPropertyPanel */
    NbPropertyPanel(final ModuleProperties props) {
        this.valid = true; // panel is valid by default
        this.props = props;
        initComponents();
        props.addPropertyChangeListener(this);
    }
    
    /**
     * This method is called whenever {@link ModuleProperties} are refreshed.
     */
    abstract void refresh();
    
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
    
    /**
     * Sets whether panel is valid and fire property change. See {@link
     * #VALID_PROPERTY}
     */
    protected void setValid(boolean valid) {
        if (this.valid != valid) {
            this.valid = valid;
            firePropertyChange(NbPropertyPanel.VALID_PROPERTY, !valid, valid);
        }
    }
    
    /**
     * Sets an error message which will be shown in the customizer. Pass
     * <code>null</code> for blank message. Also set this panel to be invalid
     * for nonnull, nonempty message. Invalid otherwise.
     */
    protected void setErrorMessage(String message) {
        String newMessage = message == null ? "" : message;
        if (!newMessage.equals(this.errMessage)) {
            String oldMessage = this.errMessage;
            this.errMessage = newMessage;
            firePropertyChange(NbPropertyPanel.ERROR_MESSAGE_PROPERTY, oldMessage, newMessage);
        }
        setValid("".equals(newMessage));
    }
    
    public void store() { /* empty implementation */ }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleProperties.PROPERTIES_REFRESHED == evt.getPropertyName()) {
            refresh();
        }
    }
    
    public void addNotify() {
        super.addNotify();
        firePropertyChange(CustomizerProviderImpl.LAST_SELECTED_PANEL, null, this);
    }
    
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
    
    abstract static class Single extends NbPropertyPanel {
        Single(final SingleModuleProperties props) {
            super(props);
        }
        SingleModuleProperties getProperties() {
            return (SingleModuleProperties) props;
        }
    }
    
    abstract static class Suite extends NbPropertyPanel {
        Suite(final SuiteProperties props) {
            super(props);
        }
        SuiteProperties getProperties() {
            return (SuiteProperties) props;
        }
    }
    
}
