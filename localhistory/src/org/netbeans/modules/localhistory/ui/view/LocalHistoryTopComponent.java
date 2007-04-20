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
package org.netbeans.modules.localhistory.ui.view;

import java.io.Serializable;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;

import javax.swing.*;

/**
 * Top component which displays something.
 * 
 * @author Tomas Stupka
 */
final public class LocalHistoryTopComponent extends TopComponent {

    private static LocalHistoryTopComponent instance;
    private LocalHistoryFileView masterView;
    private static final String PREFERRED_ID = "LocalHistoryTopComponent";
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 

    public LocalHistoryTopComponent() {
        initComponents();
        setToolTipText(NbBundle.getMessage(LocalHistoryTopComponent.class, "HINT_LocalHistoryTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(150);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables

    public void init(JPanel diffPanel, LocalHistoryFileView masterView) {                
        // XXX should be solved in a more genarel way - not ony for LocalHistoryFileView 
        this.masterView = masterView; 
        splitPane.setTopComponent(masterView.getPanel());   
        splitPane.setBottomComponent(diffPanel);                   
    }
    
    public UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }
    
    void setDiffView(JComponent currentDiffView) {
        delegatingUndoRedo.setDiffView(currentDiffView);
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized LocalHistoryTopComponent getDefault() {
        if (instance == null) {
            instance = new LocalHistoryTopComponent();
        }
        return instance;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public void componentOpened() {
        super.componentOpened();
    }

    public void componentClosed() {
        if(masterView != null) {
            masterView.close();
        }
        super.componentClosed();
    }

    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return LocalHistoryTopComponent.getDefault();
        }
    }

}
