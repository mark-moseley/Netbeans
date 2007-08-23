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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;

/**
 * Provides common support for a <em>standard</em> panels in the NetBeans module
 * and suite customizers.
 *
 * @author Martin Krauskopf
 */
abstract class NbPropertyPanel extends JPanel implements
        ModuleProperties.LazyStorage, PropertyChangeListener, HelpCtx.Provider {

    private Class helpCtxClass;

    /** Property whether <code>this</code> panel is valid. */
    static final String VALID_PROPERTY = "isPanelValid"; // NOI18N
    
    /** Property for error message of this panel. */
    static final String ERROR_MESSAGE_PROPERTY = "errorMessage"; // NOI18N
    
    protected ModuleProperties props;
    protected final ProjectCustomizer.Category category;
    
    /** Creates new NbPropertyPanel */
    NbPropertyPanel(final ModuleProperties props, final Class helpCtxClass, ProjectCustomizer.Category cat) {
        this.props = props;
        category = cat;
        props.addLazyStorage(this);
        initComponents();
        props.addPropertyChangeListener(this);
        this.helpCtxClass = helpCtxClass;
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
     * Gives subclasses a chance to set a warning or an error message after a
     * customizer is loaded/displayed. Just use this method for checking a
     * validity of a panel's data and eventually call {@link
     * #setWarning(String)} or {@link #setErrorMessage(String)}. Default
     * implementation does nothing.
     */
    
    //TODO remove!!
    protected final void checkForm() {}

    
    public void store() { /* empty implementation */ }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleProperties.PROPERTIES_REFRESHED.equals(evt.getPropertyName())) {
            refresh();
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        //TODO replace with something else..
        getRootPane().putClientProperty(BasicCustomizer.LAST_SELECTED_PANEL, category.getName());
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(helpCtxClass);
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
        Single(final SingleModuleProperties props, final Class helpCtxClass, ProjectCustomizer.Category cat) {
            super(props, helpCtxClass, cat);
        }
        SingleModuleProperties getProperties() {
            return (SingleModuleProperties) props;
        }
    }
    
    abstract static class Suite extends NbPropertyPanel {
        Suite(final SuiteProperties props, final Class helpCtxClass, ProjectCustomizer.Category cat) {
            super(props, helpCtxClass, cat);
        }
        SuiteProperties getProperties() {
            return (SuiteProperties) props;
        }
    }
    
}
