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

package org.netbeans.modules.navigator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Navigator TopComponent. Simple visual envelope for navigator graphics
 * content. Behaviour is delegated and separated into NavigatorController.
 *
 * @author Dafe Simonek
 */
public final class NavigatorTC extends TopComponent {
    
    /** singleton instance */
    private static NavigatorTC instance;
    
    /** Currently active panel in navigator (or null if empty) */
    private NavigatorPanel selectedPanel;
    /** A list of panels currently available (or null if empty) */
    private List<NavigatorPanel> panels;
    /** Controller, controls behaviour and reacts to user actions */
    private NavigatorController controller;
    /** label signalizing no available providers */
    private final JLabel notAvailLbl = new JLabel(
            NbBundle.getMessage(NavigatorTC.class, "MSG_NotAvailable")); //NOI18N
   /** special lookup for naviagtor TC */
    private Lookup navTCLookup;
    
    /** Creates new NavigatorTC, singleton */
    private NavigatorTC() {
        initComponents();
        
        setName(NbBundle.getMessage(NavigatorTC.class, "LBL_Navigator")); //NOI18N
        setIcon(Utilities.loadImage("org/netbeans/modules/navigator/resources/navigator.png")); //NOI18N        
        // accept focus when empty to work correctly in nb winsys
        setFocusable(true);
        // special title for sliding mode
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", getName());
        
        notAvailLbl.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailLbl.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailLbl.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailLbl.setOpaque(true);
        
        getController().installActions();

        // empty initially
        setToEmpty();
    }

    /** Singleton accessor, finds instance in winsys structures */
    public static final NavigatorTC getInstance () {
        NavigatorTC navTC = (NavigatorTC)WindowManager.getDefault().
                        findTopComponent("navigatorTC"); //NOI18N
        if (navTC == null) {
            // shouldn't happen under normal conditions
            navTC = privateGetInstance();
            Logger.getAnonymousLogger().warning(
                "Could not locate the navigator component via its winsys id"); //NOI18N
        }
        return navTC;
    }
    
    /** Singleton intance accessor, to be used only from module's layer.xml
     * file, winsys section and as fallback from getInstance().
     *
     * Please don't call directly otherwise.
     */ 
    public static final NavigatorTC privateGetInstance () {
        if (instance == null) {
            instance = new NavigatorTC();
        }
        return instance;
    }

    /** Shows given navigator panel's component
     */
    public void setSelectedPanel (NavigatorPanel panel) {
        int panelIdx = panels.indexOf(panel);
        assert panelIdx != -1 : "Panel to select is not available"; //NOI18N
        
        if (panel.equals(selectedPanel)) {
            return;
        }
        
        this.selectedPanel = panel;
        ((CardLayout)contentArea.getLayout()).show(contentArea, String.valueOf(panelIdx));
        // #93123: follow-up, synchronizing combo selection with content area selection
        panelSelector.setSelectedIndex(panelIdx);
    }
    
    /** Returns panel currently selected.
     * @return Panel currently selected or null if navigator is empty
     */
    public NavigatorPanel getSelectedPanel () {
        return selectedPanel;
    }
    
    /** List of panels currently contained in navigator component.
     * @return List of NavigatorPanel instances or null if navigator is empty
     */
    public List<NavigatorPanel> getPanels () {
        return panels;
    }
    
    /** Sets content of navigator to given panels, selecting the first one
     */ 
    public void setPanels (List<NavigatorPanel> panels) {
        this.panels = panels;
        int panelsCount = panels == null ? -1 : panels.size();
        // no panel, so make UI look empty
        if (panelsCount <= 0) {
            selectedPanel = null;
            setToEmpty();
        } else {
            // clear regular content 
            contentArea.removeAll();
            panelSelector.removeAllItems();
            // #63777: hide panel selector when only one panel available
            panelSelector.setVisible(panelsCount != 1);
            // fill with new content
            JComponent curComp = null;
            int i = 0;
            for (NavigatorPanel curPanel : panels) {
                panelSelector.addItem(curPanel.getDisplayName());
                curComp = curPanel.getComponent();
                // for better error report in cases like #68544
                if (curComp == null) {
                    Throwable npe = new NullPointerException(
                            "Method " + curPanel.getClass().getName() +  //NOI18N
                            ".getComponent() must not return null under any condition!"  //NOI18N
                    );
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, npe);
                } else {
                    contentArea.add(curComp, String.valueOf(i));
                }
                if (i == 0) {
                    selectedPanel = curPanel;
                }
                i++;
            }
            // show if was hidden
            resetFromEmpty();
        }
    }
    
    /** Returns combo box, UI for selecting proper panels */
    public JComboBox getPanelSelector () {
        return panelSelector;
    }
    
    public JComponent getContentArea () {
        return contentArea;
    }
    
    // Window System related methods >>

    public String preferredID () {
        return "navigatorTC"; //NOI18N
    }

    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }

    /** Overriden to pass focus directly into content panel */
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        boolean result = false;
        if (selectedPanel != null) {
            result = selectedPanel.getComponent().requestFocusInWindow();
        }
        return result;
    }

    /** Defines nagivator Help ID */
    public HelpCtx getHelpCtx () {
        return new HelpCtx("navigator.java");
    }

    /** Just delegates to controller */
    public void componentOpened () {
        getController().navigatorTCOpened();
    }
    
    /** Just delegates to controller */
    public void componentClosed () {
        getController().navigatorTCClosed();
    }
    
    // << Window system

    
    /** Combines default Lookup of TC with lookup from active navigator
     * panel.
     */
    public Lookup getLookup() {
        if (navTCLookup == null) {
            Lookup defaultLookup = super.getLookup();
            Lookup clientLookup = getController().getPanelLookup();
            navTCLookup = new ProxyLookup(
                    new Lookup [] { defaultLookup, clientLookup }
            ); 
        }
        return navTCLookup;
    }

    /** Accessor for controller which controls UI behaviour */
    public NavigatorController getController () {
        if (controller == null) {
            controller = new NavigatorController(this);
        }
        return controller;
    }
    
    
    /*************** private stuff ************/
    
    /** Removes regular UI content and sets UI to empty state */
    private void setToEmpty () {
        if (notAvailLbl.isShowing()) {
            // already empty
            return;
        }
        remove(panelSelector);
        remove(contentArea);
        add(notAvailLbl, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    /** Puts regular UI content back */
    private void resetFromEmpty () {
        if (contentArea.isShowing()) {
            // content already shown
        }
        remove(notAvailLbl);
        add(panelSelector, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        panelSelector = new javax.swing.JComboBox();
        contentArea = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        add(panelSelector, java.awt.BorderLayout.NORTH);

        contentArea.setLayout(new java.awt.CardLayout());

        add(contentArea, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentArea;
    private javax.swing.JComboBox panelSelector;
    // End of variables declaration//GEN-END:variables

    
    
    
}
