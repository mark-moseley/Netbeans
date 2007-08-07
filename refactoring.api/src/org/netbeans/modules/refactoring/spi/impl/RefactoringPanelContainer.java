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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ActionMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.MouseUtils;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class RefactoringPanelContainer extends TopComponent {
    
    private static RefactoringPanelContainer usages = null;
    private static RefactoringPanelContainer refactorings = null;
    private transient boolean isVisible = false;
    private JPopupMenu pop;
    /** Popup menu listener */
    private PopupListener listener;
    private CloseListener closeL;
    private boolean isRefactoring;
    private static Image REFACTORING_BADGE = Utilities.loadImage( "org/netbeans/modules/refactoring/api/resources/refactoringpreview.png" ); // NOI18N
    private static Image USAGES_BADGE = Utilities.loadImage( "org/netbeans/modules/refactoring/api/resources/findusages.png" ); // NOI18N
    
    private RefactoringPanelContainer() {
        this("", false);
    }
    /** Creates new form RefactoringPanelContainer */
    private RefactoringPanelContainer(String name, boolean isRefactoring) {
        setName(name);
        setToolTipText(name);
        setFocusable(true);
        setLayout(new java.awt.BorderLayout());
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(RefactoringPanelContainer.class, "ACSD_usagesPanel")
        );
        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        this.isRefactoring = isRefactoring;
        setFocusCycleRoot(true);
        JLabel label = new JLabel(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_NoUsages"));
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        this.add(label, BorderLayout.CENTER);
        initActions();
    }
    
    void addPanel(JPanel panel) {
        RefactoringPanel.checkEventThread();
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
        } else {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).addTab(panel.getName() + "  ", null, panel, panel.getToolTipText()); //NOI18N
                ((JTabbedPane) comp).setSelectedComponent(panel);
                comp.validate();
            } else if (comp instanceof JLabel) {
                remove(comp);
                add(panel, BorderLayout.CENTER);
            } else {
                remove(comp);
                JTabbedPane pane = new CloseButtonTabbedPane();
                pane.addMouseListener(listener);
                pane.addPropertyChangeListener(closeL);
                add(pane, BorderLayout.CENTER);
                pane.addTab(comp.getName() + "  ", null, comp, ((JPanel) comp).getToolTipText()); //NOI18N
                pane.addTab(panel.getName() + "  ", null, panel, panel.getToolTipText()); //NOI18N
                pane.setSelectedComponent(panel);
                pane.validate();
            }
        }
        if (!isVisible) {
            isVisible = true;
            open();
        }
        validate();
        requestActive();
    }

    protected void componentActivated () {
        super.componentActivated();
        JPanel panel = getCurrentPanel();
        if (panel!=null)
            panel.requestFocus();
    }
    
    void removePanel(JPanel panel) {
        RefactoringPanel.checkEventThread();
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            if (panel == null) {
                panel = (JPanel) tabs.getSelectedComponent();
            }
            tabs.remove(panel);
            if (tabs.getComponentCount() == 1) {
                Component c = tabs.getComponent(0);
                tabs.removeMouseListener(listener);
                tabs.removePropertyChangeListener(closeL);
                remove(tabs);
                add(c, BorderLayout.CENTER);
            }
        } else {
            if (comp != null)
                remove(comp);
            isVisible = false;
            close();
        }
        validate();
    }
    
    void closeAllButCurrent() {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            Component current = tabs.getSelectedComponent();
            Component[] c =  tabs.getComponents();
            for (int i = 0; i< c.length; i++) {
                if (c[i]!=current) {
                    ((RefactoringPanel) c[i]).close();
                }
            }
        }
    }
    
    public static synchronized RefactoringPanelContainer getUsagesComponent() {
        if ( usages == null ) {
            usages = (RefactoringPanelContainer) WindowManager.getDefault().findTopComponent( "find-usages" ); //NOI18N
        } 
        return usages;
    }
    
    public static synchronized RefactoringPanelContainer getRefactoringComponent() {
        if (refactorings == null) {
            refactorings = (RefactoringPanelContainer) WindowManager.getDefault().findTopComponent( "refactoring-preview" ); //NOI18N
        } 
        return refactorings;
    }
    
    public static synchronized RefactoringPanelContainer createRefactoringComponent() {
        if (refactorings == null)
            refactorings = new RefactoringPanelContainer(org.openide.util.NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_Refactoring"), true);
        return refactorings;
    }
    
    public static synchronized RefactoringPanelContainer createUsagesComponent() {
        if (usages == null)
            usages = new RefactoringPanelContainer(org.openide.util.NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_Usages"), false);
        return usages;
    }
    
    protected void closeNotify() {
        isVisible = false;
        if (getComponentCount() == 0) {
            return ;
        }
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) comp;
            Component[] c =  pane.getComponents();
            for (int i = 0; i< c.length; i++) {
                ((RefactoringPanel) c[i]).close();
            }
        } else if (comp instanceof RefactoringPanel) {
            ((RefactoringPanel) comp).close();
        }
    }
    
    protected String preferredID() {
        return "RefactoringPanel"; // NOI18N
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
    
    private void initActions() {
        ActionMap map = getActionMap();

        map.put("jumpNext", new PrevNextAction (false)); // NOI18N
        map.put("jumpPrev", new PrevNextAction (true)); // NOI18N
    }
    
    public RefactoringPanel getCurrentPanel() {
        if (getComponentCount() > 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) comp;
                return (RefactoringPanel) tabs.getSelectedComponent();
            } else {
                if (comp instanceof RefactoringPanel)
                    return (RefactoringPanel) comp;
            }
        }
        return null;
    }
    
    private final class PrevNextAction extends javax.swing.AbstractAction {
        private boolean prev;
        
        public PrevNextAction (boolean prev) {
            this.prev = prev;
        }

        public void actionPerformed (java.awt.event.ActionEvent actionEvent) {
            RefactoringPanel panel = getCurrentPanel();
            if (panel != null) {
                if (prev) {
                    panel.selectPrevUsage();
                } else {
                    panel.selectNextUsage(); 
                }
            }
        }
    }
    
    
    private class CloseListener implements PropertyChangeListener {
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (CloseButtonTabbedPane.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((JPanel) evt.getNewValue());
            }
        }
        
    }
    /**
    * Class to showing popup menu
    */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {        

        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        protected void showPopup (MouseEvent e) {
            pop.show(RefactoringPanelContainer.this, e.getX(), e.getY());
        }
    } // end of PopupListener
        
    private class Close extends AbstractAction {
        
        public Close() {
            super(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_CloseWindow"));
        }
        
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }
    
    private final class CloseAll extends AbstractAction {
        
        public CloseAll() {
            super(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_CloseAll"));
        }
        
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }
    
    private class CloseAllButCurrent extends AbstractAction {
        
        public CloseAllButCurrent() {
            super(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_CloseAllButCurrent"));
        }
        
        public void actionPerformed(ActionEvent e) {
            closeAllButCurrent();
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RefactoringPanelContainer.class.getName() + (isRefactoring ? ".refactoring-preview" : ".find-usages") ); //NOI18N
    }

    @Override
    public java.awt.Image getIcon() {
        if (isRefactoring)
            return REFACTORING_BADGE;
        else
            return USAGES_BADGE;
    }
}
