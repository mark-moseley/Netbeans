/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author  Daniel Prusa
 */
public class InfoPanel extends javax.swing.JPanel {

    private static final int PANEL_HEIGHT = 40;
    
    private static final int FILTERS = 0;
    private static final int HITS = 1;
    private static final int DEADLOCKS = 2;
    private static final int DEADLOCKS_BY_DEBUGGER = 3;
    
    private Color hitsPanelColor;
    private Color deadlockPanelColor;
    private Color filterPanelColor;
    private int tapPanelMinimumHeight;
    private TapPanel tapPanel;
    private Item[] items;
    
    private RequestProcessor requestProcessor = new RequestProcessor("Debugging View Info Panel"); // NOI18N
    
    private JButton arrowButton;
    private JPopupMenu arrowMenu;
    private Map<JPDAThread, JMenuItem> threadToMenuItem = new HashMap<JPDAThread, JMenuItem>();
    private JPDAThread debuggerDeadlockThread;
    
    /** Creates new form InfoPanel */
    public InfoPanel(TapPanel tapPanel) {
        this.tapPanel = tapPanel;
        filterPanelColor = tapPanel.getBackground();
        hitsPanelColor = DebuggingView.hitsColor;
        deadlockPanelColor = hitsPanelColor;
        tapPanelMinimumHeight = tapPanel.getMinimumHeight();
        
        initComponents();
        
        items = new Item[4];
        items[FILTERS] = new Item(filterPanelColor, PANEL_HEIGHT, createFilterToolBar()); // options and filters
        items[HITS] = new Item(hitsPanelColor, PANEL_HEIGHT, hitsInnerPanel); // breakpoint hits
        items[DEADLOCKS] = new Item(hitsPanelColor, PANEL_HEIGHT, deadlocksInnerPanel); // deadlock
        items[DEADLOCKS_BY_DEBUGGER] = new Item(deadlockPanelColor, PANEL_HEIGHT * 2, debuggerDeadlocksInnerPanel); // deadlock caused by debugger
        
        items[FILTERS].getPanel().setBorder(new EmptyBorder(1, 2, 1, 5)); // [TODO]

        arrowButton = createArrowButton();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        hitsInnerPanel.add(arrowButton, gridBagConstraints);

        removeAll();
        items[FILTERS].makeVisible(false, true, null);
        items[HITS].makeInvisible();
        items[DEADLOCKS].makeInvisible();
        items[DEADLOCKS_BY_DEBUGGER].makeInvisible();
        for (int x = items.length - 1; x >= 0; x--) {
            add(items[x].scrollPane);
            if (x > 0) {
                add(items[x].separator);
            }
        }
    }

    void clearBreakpointHits() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                hideHitsPanel();
            }
        });
    }

    void removeBreakpointHit(final JPDAThread thread, final int newHitsCount) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JMenuItem item = threadToMenuItem.remove(thread);
                if (item == null) {
                    return;
                }
                arrowMenu.remove(item);
                setHitsText(newHitsCount);
                if (newHitsCount == 0) {
                    hideHitsPanel();
                }
            }
        });
    }
    
    void addBreakpointHit(final JPDAThread thread, final int newHitsCount) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (threadToMenuItem.get(thread) != null) {
                    return;
                }
                JMenuItem item = createMenuItem(thread);
                threadToMenuItem.put(thread, item);
                arrowMenu.add(item);
                setHitsText(newHitsCount);
                if (newHitsCount == 1) {
                    showHitsPanel();
                }
            }
        });
    }

    void setBreakpointHits(final List<JPDAThread> hits) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                for (JPDAThread thread : hits) {
                    JMenuItem item = createMenuItem(thread);
                    threadToMenuItem.put(thread, item);
                    arrowMenu.add(item);
                }
                if (hits.size() == 0) {
                    hideHitsPanel();
                } else {
                    setHitsText(hits.size());
                    showHitsPanel();
                }
            }
        });
    }

    public void recomputeMenuItems(final List<JPDAThread> hits) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                for (JPDAThread thread : hits) {
                    JMenuItem item = createMenuItem(thread);
                    threadToMenuItem.put(thread, item);
                    arrowMenu.add(item);
                }
            }
        });
    }

    private JMenuItem createMenuItem(final JPDAThread thread) {
        String displayName;
        try {
            displayName = DebuggingNodeModel.getDisplayName(thread, false);
            Method method = thread.getClass().getMethod("getDebugger"); // [TODO]
            JPDADebugger debugger = (JPDADebugger)method.invoke(thread);
            method = debugger.getClass().getMethod("getSession");
            Session session = (Session) method.invoke(debugger);
            Session currSession = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (session != currSession) {
                String str = NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_Session", // [TODO] bundle
                        session.getName());
                displayName = displayName.charAt(0) + str + ", " + displayName.substring(1);
            }
        } catch (Exception e) { // [TODO]
            displayName = thread.getName();
        }
        Image image = Utilities.loadImage(DebuggingNodeModel.getIconBase(thread));
        Icon icon = image != null ? new ImageIcon(image) : null;
        JMenuItem item = new JMenuItem(displayName, icon);
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                thread.makeCurrent();
            }
        });
        return item;
    }
    
    private void setHitsText(int hitsNumber) {
        String text;
        if (hitsNumber == 1) {
            text = NbBundle.getMessage(InfoPanel.class, "LBL_OneNewHit");
        } else {
            text = NbBundle.getMessage(InfoPanel.class, "LBL_NewHits", hitsNumber);
        }
        hitsLabel.setText(text);
    }
    
    void setShowDeadlock(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (visible) {
                    showDeadlocksPanel();
                } else {
                    hideDeadlocksPanel();
                }
            }
        });
    }
    
    void setShowThreadLocks(final JPDAThread thread, final List<JPDAThread> lockerThreads) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (lockerThreads != null) {
                    showDebuggerDeadlockPanel(thread, lockerThreads);
                } else {
                    hideDebuggerDeadlockPanel();
                }
            }
        });
    }

    // **************************************************************************

    private void hidePanel(int index) {
        Item item = items[index];
        if (!item.isVisible()) {
            return;
        }
        item.makeInvisible();
        boolean wasOnTop = true;
        for (int i = index + 1; i < items.length; i++) {
            if (items[i].isVisible()) {
                wasOnTop = false;
                break;
            } // if
        }  // for
        if (wasOnTop) {
            for (int i = index - 1; i >= 0; i--) {
                if (items[i].isVisible()) {
                    items[i].setTop(true);
                    break;
                } // if
            } // for
        } // if
    }

    private void showPanel(int index) {
        Item item = items[index];
        if (item.isVisible()) {
            return;
        }
        boolean isOnTop = true;
        for (int i = index + 1; i < items.length; i++) {
            if (items[i].isVisible()) {
                isOnTop = false;
                break;
            } // if
        }  // for
        Item previousTop = null;
        if (isOnTop) {
            for (int i = index - 1; i >= 0; i--) {
                if (items[i].isVisible()) {
                    previousTop = items[i];
                    break;
                } // if
            } // for
        } // if
        item.makeVisible(true, isOnTop, previousTop);
    }

    private void hideHitsPanel() {
        hidePanel(HITS);
    }
    
    private void showHitsPanel() {
        showPanel(HITS);
    }
    
    private void hideDeadlocksPanel() {
        hidePanel(DEADLOCKS);
    }
    
    private void showDeadlocksPanel() {
        showPanel(DEADLOCKS);
    }
    
    private void hideDebuggerDeadlockPanel() {
        hidePanel(DEADLOCKS_BY_DEBUGGER);
    }

    private boolean isInStep(JPDAThread t) {
        // TODO: Make JPDAThread.isInStep()
        try {
            java.lang.reflect.Method isInStepMethod = t.getClass().getMethod("isInStep", new Class[] {});
            return (Boolean) isInStepMethod.invoke(t, new Object[] {});
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private void showDebuggerDeadlockPanel(JPDAThread thread, List<JPDAThread> lockerThreads) {
        //this.debuggerDeadlockThreads = lockerThreads;
        this.debuggerDeadlockThread = thread;
        String labelResource;
        if (isInStep(thread)) {
            labelResource = "InfoPanel.debuggerDeadlocksLabel.text"; // NOI18N
        } else {
            labelResource = "InfoPanel.debuggerDeadlocksLabel.Method.text"; // NOI18N
        }
        debuggerDeadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, labelResource));
        if (items[DEADLOCKS].isVisible()) {
            // Show only if there is not a real deadlock.
            return;
        }
        showPanel(DEADLOCKS_BY_DEBUGGER);
    }

    private JButton createArrowButton() {
        arrowMenu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
            new ImageIcon(Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/unvisited_bpkt_arrow_small_16.png")), arrowMenu);
        button.setPreferredSize(new Dimension(40, button.getPreferredSize().height)); // [TODO]
        button.setMaximumSize(new Dimension(40, button.getPreferredSize().height)); // [TODO]
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (arrowMenu.getComponentCount() > 0) {
                    Object item = arrowMenu.getComponent(0);
                    for (Map.Entry<JPDAThread, JMenuItem> entry : threadToMenuItem.entrySet()) {
                        if (entry.getValue() == item) {
                            entry.getKey().makeCurrent();
                        } // if
                    } // for
                } // if
            } // actionPerformed
        });
        return button;
    }
    
    private JToolBar createFilterToolBar() {
        final FiltersDescriptor filtersDesc = FiltersDescriptor.getInstance();
        // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        // create toggle buttons
        int filterCount = filtersDesc.getFilterCount();
        ArrayList<JToggleButton> toggles = new ArrayList<JToggleButton>(filterCount);
        JToggleButton toggleButton = null;

        for (int i = 0; i < filterCount; i++) {
            toggleButton = createToggle(filtersDesc, i);
            toggles.add(toggleButton);
        }

        // add toggle buttons
        Dimension space = new Dimension(3, 0);
        for (int i = 0; i < toggles.size(); i++) {
            final int index = i;
            final JToggleButton curToggle = toggles.get(i);
            curToggle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filtersDesc.setSelected(index, curToggle.isSelected());
                }
            });
            toolbar.add(curToggle);
            if (i != toggles.size() - 1) {
                toolbar.addSeparator(space);
            }
        }
        return toolbar;
    }

    private JToggleButton createToggle (FiltersDescriptor filtersDesc, int index) {
        boolean isSelected = filtersDesc.isSelected(index);
        Icon icon = filtersDesc.getSelectedIcon(index);
        // ensure small size, just for the icon
        JToggleButton toggleButton = new JToggleButton(icon, isSelected);
        Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        toggleButton.setPreferredSize(size);
        toggleButton.setMargin(new Insets(2,3,2,3));
        toggleButton.setToolTipText(filtersDesc.getTooltip(index));
        toggleButton.setFocusable(false);
        filtersDesc.connectToggleButton(index, toggleButton);
        return toggleButton;
    }
    
    private void resumeThreadToFreeMonitor(JPDAThread thread) {
        // Do not have monitor breakpoints in the API.
        // Have to do that in the implementation module.
        try {
            java.lang.reflect.Method resumeToFreeMonitorMethod = thread.getClass().getMethod("resumeBlockingThreads");
            resumeToFreeMonitorMethod.invoke(thread);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        debuggerDeadlocksInnerPanel = new javax.swing.JPanel();
        infoIcon2 = new javax.swing.JLabel();
        debuggerDeadlocksLabel = new javax.swing.JLabel();
        emptyPanel2 = new javax.swing.JPanel();
        resumeDebuggerDeadlockLabel = new javax.swing.JLabel();
        resumeDebuggerDeadlockButton = new javax.swing.JButton();
        deadlocksInnerPanel = new javax.swing.JPanel();
        infoIcon1 = new javax.swing.JLabel();
        deadlocksLabel = new javax.swing.JLabel();
        emptyPanel1 = new javax.swing.JPanel();
        hitsInnerPanel = new javax.swing.JPanel();
        infoIcon = new javax.swing.JLabel();
        hitsLabel = new javax.swing.JLabel();
        emptyPanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        debuggerDeadlocksInnerPanel.setOpaque(false);
        debuggerDeadlocksInnerPanel.setPreferredSize(new java.awt.Dimension(0, 16));
        debuggerDeadlocksInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/wrong_pass.png"))); // NOI18N
        infoIcon2.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        debuggerDeadlocksInnerPanel.add(infoIcon2, gridBagConstraints);

        debuggerDeadlocksLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        debuggerDeadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.debuggerDeadlocksLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        debuggerDeadlocksInnerPanel.add(debuggerDeadlocksLabel, gridBagConstraints);

        emptyPanel2.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        debuggerDeadlocksInnerPanel.add(emptyPanel2, gridBagConstraints);

        resumeDebuggerDeadlockLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.resumeDebuggerDeadlockLabel.text")); // NOI18N
        resumeDebuggerDeadlockLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        debuggerDeadlocksInnerPanel.add(resumeDebuggerDeadlockLabel, gridBagConstraints);

        resumeDebuggerDeadlockButton.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.resumeDebuggerDeadlockButton.text")); // NOI18N
        resumeDebuggerDeadlockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeDebuggerDeadlockButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
        debuggerDeadlocksInnerPanel.add(resumeDebuggerDeadlockButton, gridBagConstraints);

        add(debuggerDeadlocksInnerPanel);

        deadlocksInnerPanel.setOpaque(false);
        deadlocksInnerPanel.setPreferredSize(new java.awt.Dimension(0, 16));
        deadlocksInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/wrong_pass.png"))); // NOI18N
        infoIcon1.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        deadlocksInnerPanel.add(infoIcon1, gridBagConstraints);

        deadlocksLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        deadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.deadlocksLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        deadlocksInnerPanel.add(deadlocksLabel, gridBagConstraints);

        emptyPanel1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        deadlocksInnerPanel.add(emptyPanel1, gridBagConstraints);

        add(deadlocksInnerPanel);

        hitsInnerPanel.setOpaque(false);
        hitsInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/info_big.png"))); // NOI18N
        infoIcon.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        hitsInnerPanel.add(infoIcon, gridBagConstraints);

        hitsLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.hitsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        hitsInnerPanel.add(hitsLabel, gridBagConstraints);

        emptyPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        hitsInnerPanel.add(emptyPanel, gridBagConstraints);

        add(hitsInnerPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void resumeDebuggerDeadlockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeDebuggerDeadlockButtonActionPerformed
        //final List<JPDAThread> threadsToResume = debuggerDeadlockThreads;
        final JPDAThread blockedThread = debuggerDeadlockThread;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                resumeThreadToFreeMonitor(blockedThread);
            }
        });
        hideDebuggerDeadlockPanel();
    }//GEN-LAST:event_resumeDebuggerDeadlockButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel deadlocksInnerPanel;
    private javax.swing.JLabel deadlocksLabel;
    private javax.swing.JPanel debuggerDeadlocksInnerPanel;
    private javax.swing.JLabel debuggerDeadlocksLabel;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JPanel emptyPanel1;
    private javax.swing.JPanel emptyPanel2;
    private javax.swing.JPanel hitsInnerPanel;
    private javax.swing.JLabel hitsLabel;
    private javax.swing.JLabel infoIcon;
    private javax.swing.JLabel infoIcon1;
    private javax.swing.JLabel infoIcon2;
    private javax.swing.JButton resumeDebuggerDeadlockButton;
    private javax.swing.JLabel resumeDebuggerDeadlockLabel;
    // End of variables declaration//GEN-END:variables

    public class Item {
        private Color backgroundColor;
        private int preferredHeight;
        private JPanel topGapPanel;
        private JPanel bottomGapPanel;
        private JPanel outerPanel;
        private JComponent innerPanel;
        private JScrollPane scrollPane;
        private JPanel separator;
        private boolean animationRunning = false;
        private boolean isTop = false;

        Item(Color backgroundColor, int preferredHeight, JComponent innerPanel) {
            this.backgroundColor = backgroundColor;
            this.preferredHeight = preferredHeight;
            this.innerPanel = innerPanel;
            topGapPanel = createGapPanel();
            bottomGapPanel = createGapPanel();
            separator = createSeparator();
            outerPanel = new JPanel();
            outerPanel.setBackground(backgroundColor);
            outerPanel.setLayout(new BorderLayout());
            outerPanel.add(BorderLayout.NORTH, topGapPanel);
            outerPanel.add(BorderLayout.CENTER, innerPanel);
            outerPanel.add(BorderLayout.SOUTH, bottomGapPanel);
            outerPanel.setPreferredSize(new Dimension(0, preferredHeight));
            scrollPane = new JScrollPane();
            scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            scrollPane.setPreferredSize(new Dimension(0, preferredHeight));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setViewportView(outerPanel);
        }
        
        public JPanel getPanel() {
            return outerPanel;
        }

        public boolean isVisible() {
            return scrollPane.isVisible() || animationRunning;
        }

        private JPanel createGapPanel() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setPreferredSize(new java.awt.Dimension(0, tapPanelMinimumHeight));
            return panel;
        }
        
        private JPanel createSeparator() {
            JPanel panel = new JPanel();
            panel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
            panel.setMaximumSize(new java.awt.Dimension(32767, 1));
            panel.setMinimumSize(new java.awt.Dimension(10, 1));
            panel.setPreferredSize(new java.awt.Dimension(0, 1));
            return panel;
        }

        private synchronized void makeInvisible() {
            scrollPane.setVisible(false);
            separator.setVisible(false);
            if (animationRunning) {
                animationRunning = false;
            }
            setTop(isTop);
        }
        
        private synchronized void makeVisible(boolean animate, final boolean top, final Item lastTop) {
            if (animationRunning) {
                return;
            }
            int height = top ? preferredHeight - tapPanelMinimumHeight : preferredHeight;
            if (!animate) {
                setTop(top);
                if (top && lastTop != null) {
                    lastTop.setTop(false);
                }
                scrollPane.setPreferredSize(new Dimension(0, height));
                outerPanel.setPreferredSize(new Dimension(0, height));
                scrollPane.setVisible(true);
                separator.setVisible(true);
            } else {
                scrollPane.setPreferredSize(new Dimension(0, 1));
                outerPanel.setPreferredSize(new Dimension(0, height));
                animationRunning = true;
                isTop = top;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (isTop && lastTop != null) {
                            lastTop.setTop(false);
                        }
                        topGapPanel.setVisible(!isTop);
                        if (animationRunning) {
                            scrollPane.setVisible(true);
                            separator.setVisible(true);
                            tapPanel.revalidate();
                        }
                        if (isTop) {
                            tapPanel.setBackground(backgroundColor);
                        }
                    }
                });
                int delta = 1;
                int currHeight = 1;
                Timer animationTimer = new Timer(20, null);
                animationTimer.addActionListener(new AnimationTimerListener(animationTimer, delta, currHeight));
                animationTimer.setCoalesce(false);
                animationTimer.start();
            } // else
        }

        private class AnimationTimerListener implements ActionListener {

            private int delta;
            private int currHeight;
            private Timer animationTimer;
            private long time = 0l;

            public AnimationTimerListener(Timer animationTimer, int delta, int currHeight) {
                this.delta = delta;
                this.currHeight = currHeight;
                this.animationTimer = animationTimer;
            }

            public void actionPerformed(ActionEvent e) {
                long now = System.nanoTime();
                int step;
                if (time > 0) {
                    // Do bigger step if time is running out...
                    step = delta*((int) (now - time)/(animationTimer.getDelay()*1000000) + 1);
                    //System.err.println("interval = "+(now-time)/1000000+"step = "+step);
                } else {
                    step = delta;
                }
                time = now;
                currHeight += step;
                int height = isTop ? preferredHeight - tapPanelMinimumHeight : preferredHeight;
                if (currHeight > height) {
                    currHeight = height;
                }
                scrollPane.setPreferredSize(new Dimension(0, currHeight));
                //System.err.println("currHeight = "+currHeight);
                revalidate();
                doLayout();
                if (currHeight >= (isTop ? preferredHeight - tapPanelMinimumHeight : preferredHeight)) {
                    animationTimer.stop();
                    synchronized (Item.this) {
                        animationRunning = false;
                    }
                }
            }
        }

        private void setTop(boolean isTop) {
            this.isTop = isTop;
            if (isTop) {
                topGapPanel.setVisible(false);
                if (!animationRunning) {
                    outerPanel.setPreferredSize(new Dimension(0, preferredHeight - tapPanelMinimumHeight));
                    scrollPane.setPreferredSize(new Dimension(0, preferredHeight - tapPanelMinimumHeight));
                }
                tapPanel.setBackground(backgroundColor);
            } else {
                topGapPanel.setVisible(true);
                outerPanel.setPreferredSize(new Dimension(0, preferredHeight));
                scrollPane.setPreferredSize(new Dimension(0, preferredHeight));
            }
        }
        
    }

}
