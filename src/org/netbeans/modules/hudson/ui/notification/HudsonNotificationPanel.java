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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui.notification;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Hudson notification panel
 * 
 * @author  Michal Mocnak
 */
public class HudsonNotificationPanel extends javax.swing.JPanel implements ChangeListener {
    
    public final static int POPUP_TYPE = 0;
    public final static int MESSAGE_TYPE = 1;
    
    private JWindow popupWindow;
    private HudsonNotificationPopupPanel pane;
    private boolean showingPopup = false;
    private HideAWTListener hideListener = new HideAWTListener();
    
    /** Creates new form HudsonNotificationPanel */
    public HudsonNotificationPanel() {
        initComponents();
        setVisible(false);
    }
    
    public void stateChanged(ChangeEvent e) {
        int jobs = 0;
        
        if ((jobs = getPopupPanel().setContent(HudsonNotificationController.getDefault().getFailedJobs())) > 0) {
            setVisible(true);
            
            // Set tooltil text
            notificationLabel.setToolTipText(NbBundle.getMessage(HudsonNotificationPanel.class, "MSG_NotificationToolTip",
                    jobs, (jobs > 1) ? "s" : ""));
            
            // If the popup is visible resize it
            if (showingPopup)
                resizePopup();
        } else {
            setVisible(false);
            
            // If the popup is visible hide it
            if (showingPopup)
                hidePopup();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        notificationLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        notificationLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/hudson/ui/resources/red_anime.gif"))); // NOI18N
        notificationLabel.setText(org.openide.util.NbBundle.getMessage(HudsonNotificationPanel.class, "notificationLabel.text")); // NOI18N
        notificationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                notificationLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(notificationLabel, gridBagConstraints);
        notificationLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }// </editor-fold>//GEN-END:initComponents
    
private void notificationLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_notificationLabelMouseClicked
    if (showingPopup)
        hidePopup();
    else
        showPopup();
}//GEN-LAST:event_notificationLabelMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel notificationLabel;
    // End of variables declaration//GEN-END:variables
    
    private HudsonNotificationPopupPanel getPopupPanel() {
        if (null == pane) {
            pane = new HudsonNotificationPopupPanel();
            
            // Set popup pane
            pane.getActionMap().put("HidePopup", new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    hidePopup();
                }
            });
            pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
            pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
        }
        
        return pane;
    }
    
    private void showPopup() {
        if (showingPopup)
            return;
        
        // set showing flag to true
        showingPopup = true;
        
        if (null == popupWindow) {
            popupWindow = new JWindow(WindowManager.getDefault().getMainWindow());
            popupWindow.getContentPane().add(getPopupPanel());
        }
        
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        WindowManager.getDefault().getMainWindow().addWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().addComponentListener(hideListener);
        
        resizePopup();
        
        popupWindow.setVisible(true);
        getPopupPanel().requestFocus();
    }
    
    private void hidePopup() {
        if (popupWindow != null)
            popupWindow.setVisible(false);
        
        Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeComponentListener(hideListener);
        
        // set showing flag to false
        showingPopup = false;
    }
    
    private void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0,0);
        SwingUtilities.convertPointToScreen(point, this);
        Dimension dim = popupWindow.getSize();
        
        //#63265
        Rectangle usableRect = Utilities.getUsableScreenBounds();
        Point loc = new Point(point.x + this.getSize().width - dim.width - 5 * 2  , point.y - dim.height - 5);
        
        // -5 in x coordinate is becuase of the hgap between the separator and button and separator and edge
        if (! usableRect.contains(loc)) {
            loc = new Point(loc.x, point.y + 5 + this.getSize().height);
        }
        
        // +4 here because of the width of the close button in popup, we
        // want the progress bars to align visually.. but there's separator in status now..
        popupWindow.setLocation(loc);
    }
    
    // Helper classes
    
    private class HideAWTListener extends ComponentAdapter implements  AWTEventListener, WindowStateListener {
        
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent)aWTEvent;
                
                if (mv.getClickCount() > 0) {
                    Component comp = (Component) aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed("HudsonNotificationPopupPanel", comp);
                    Container barpar = SwingUtilities.getAncestorOfClass(HudsonNotificationPanel.class, comp);
                    
                    if (null == par && null == barpar)
                        hidePopup();
                }
            }
        }
        
        public void windowStateChanged(WindowEvent windowEvent) {
            if (showingPopup) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();
                
                if (((oldState & Frame.ICONIFIED) == 0) &&
                        ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hidePopup();
                }
            }
            
        }
        
        public void componentResized(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }
        
        public void componentMoved(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }       
    }
}