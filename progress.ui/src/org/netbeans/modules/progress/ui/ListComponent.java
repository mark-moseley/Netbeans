/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.progress.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.ProgressEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class ListComponent extends JPanel {
    private NbProgressBar bar;
    private JLabel mainLabel;
    private JLabel dynaLabel;
    private JButton closeButton;
    private InternalHandle handle;
    private boolean watched;
    private Action cancelAction;
    private Color selectBgColor;
    private Color selectFgColor;
    private Color bgColor;
    private Color fgColor;
//    private Color dynaFgColor;
    private int mainHeight;
    private int dynaHeight;
    
    /** Creates a new instance of ListComponent */
    public ListComponent(InternalHandle hndl) {
        setFocusable(true);
        setRequestFocusEnabled(true);
//        setVerifyInputWhenFocusTarget(false);
        mainLabel = new JLabel();
        dynaLabel = new JLabel();
        // in gtk, the panel is non-opague, meaning we cannot color background
        // #59419
        setOpaque(true);
        dynaLabel.setFont(dynaLabel.getFont().deriveFont((float) (dynaLabel.getFont().getSize() - 2)));
        bar = new NbProgressBar();        
        handle = hndl;
        Color bg = UIManager.getColor("nbProgressBar.popupText.background");
        if (bg != null) {
            setBackground(bg);
            mainLabel.setBackground(bg);
            dynaLabel.setBackground(bg);
        }
        bgColor = getBackground();
        Color dynaFg = UIManager.getColor("nbProgressBar.popupDynaText.foreground");
        if (dynaFg != null) {
            dynaLabel.setForeground(dynaFg);
        }
//        dynaFgColor = dynaLabel.getForeground();
        fgColor = UIManager.getColor("nbProgressBar.popupText.foreground");
        if (fgColor != null) {
            mainLabel.setForeground(fgColor);
        }
        fgColor = mainLabel.getForeground();
        selectBgColor = UIManager.getColor("nbProgressBar.popupText.selectBackground");
        if (selectBgColor == null) {
            selectBgColor = UIManager.getColor("List.selectionBackground");
        }
        selectFgColor = UIManager.getColor("nbProgressBar.popupText.selectForeground");
        if (selectFgColor == null) {
            selectFgColor = UIManager.getColor("List.selectionForeground");
        }
        bar.setToolTipText(NbBundle.getMessage(ListComponent.class, "ListComponent.bar.tooltip"));
        bar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //start figure out height
        mainLabel.setText("XYZ");
        dynaLabel.setText("XYZ");
        mainHeight = mainLabel.getPreferredSize().height;
        dynaHeight = dynaLabel.getPreferredSize().height;
        mainLabel.setText(null);
        dynaLabel.setText(null);
        //end figure out height
        
        setLayout(new CustomLayout());
        add(mainLabel);
        add(bar);
        MListener list = new MListener();
        if (handle.isAllowCancel()) {
            cancelAction = new CancelAction(false);
            closeButton = new JButton(cancelAction);
            closeButton.setBorderPainted(false);
            closeButton.setBorder(BorderFactory.createEmptyBorder());
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setFocusable(false);
            
            Image img = (Image)UIManager.get("nb.progress.cancel.icon");
            if( null != img ) {
                closeButton.setIcon( new ImageIcon( img ) );
            }
            img = (Image)UIManager.get("nb.progress.cancel.icon.mouseover");
            if( null != img ) {
                closeButton.setRolloverEnabled(true);
                closeButton.setRolloverIcon( new ImageIcon( img ) );
            }
            img = (Image)UIManager.get("nb.progress.cancel.icon.pressed");
            if( null != img ) {
                closeButton.setPressedIcon( new ImageIcon( img ) );
            }
            
            closeButton.setToolTipText(NbBundle.getMessage(ListComponent.class, "ListComponent.btnClose.tooltip"));
            add(closeButton);
            if (handle.getState() != InternalHandle.STATE_RUNNING) {
                closeButton.setEnabled(false);
            }
        }
        add(dynaLabel);
        setBorder(BorderFactory.createEmptyBorder());
        addMouseListener(list);
        bar.addMouseListener(list);
        mainLabel.addMouseListener(list);
        dynaLabel.addMouseListener(list);
        if (handle.isAllowCancel()) {
            closeButton.addMouseListener(list);
        }
        
        mainLabel.setText(handle.getDisplayName());
        NbProgressBar.setupBar(handle, bar);
        addFocusListener(new FocusListener() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (!e.isTemporary()) {
                    setBackground(selectBgColor);
                    mainLabel.setBackground(selectBgColor);
                    dynaLabel.setBackground(selectBgColor);
                    mainLabel.setForeground(selectFgColor);
                    // TODO assuming now that dynalabel has always the same foreground color
                    // seems to be the case according to the spec
                    scrollRectToVisible(getBounds());
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (!e.isTemporary()) {
                    setBackground(bgColor);
                    mainLabel.setBackground(bgColor);
                    dynaLabel.setBackground(bgColor);
                    mainLabel.setForeground(fgColor);
                    // TODO assuming now that dynalabel has always the same foreground color
                    // seems to be the case according to the spec
                    
                }
            }
            
        });
        
    }
    
    
    Action getCancelAction() {
        return cancelAction;
    }
    
    InternalHandle getHandle() {
        return handle;
    }
    
    void processProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_PROGRESS || 
                event.getType() == ProgressEvent.TYPE_SWITCH || 
                event.getType() == ProgressEvent.TYPE_SILENT) {
            if (event.getSource() != handle) {
                throw new IllegalStateException();
            }
            if (event.isSwitched()) {
                NbProgressBar.setupBar(event.getSource(), bar);
            }
            if (event.getWorkunitsDone() > 0) {
                bar.setValue(event.getWorkunitsDone());
            }
            bar.setString(StatusLineComponent.getBarString(event.getPercentageDone(), event.getEstimatedCompletion()));
            if (event.getMessage() != null) {
                dynaLabel.setText(event.getMessage());
            }
            if (event.getSource().getState() == InternalHandle.STATE_REQUEST_STOP) {
                closeButton.setEnabled(false);
            }
            if (event.getDisplayName() != null) {
                mainLabel.setText(event.getDisplayName());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    void markAsActive(boolean sel) {
        if (sel == watched) {
            return;
        }
        watched = sel;
        if (sel) {
            mainLabel.setFont(mainLabel.getFont().deriveFont(Font.BOLD));
        } else {
            mainLabel.setFont(mainLabel.getFont().deriveFont(Font.PLAIN));
        }
        if (mainLabel.isVisible()) {
            mainLabel.repaint();
        }
    }
    
    private class MListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == bar) {
                handle.requestExplicitSelection();
//                markAsSelected(true);
            }
            if (e.getClickCount() > 1 && (e.getSource() == mainLabel || e.getSource() == dynaLabel)) {
                handle.requestView();
            }
            if (e.getButton() != e.BUTTON1) {
                showMenu(e);
            } else {
                ListComponent.this.requestFocus();
            }
            
//            System.out.println("list component requesting focus..");
        }
    }
    
   private void showMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        //mark teh popup for the status line awt listener
        popup.setName("progresspopup");
        popup.add(new ViewAction());
        popup.add(new WatchAction());
        popup.add(new CancelAction(true));
        popup.show((Component)e.getSource(), e.getX(), e.getY());
    }    
    
   private class CancelAction extends AbstractAction {
       CancelAction(boolean text) {
           if (text) {
               putValue(Action.NAME, NbBundle.getMessage(ListComponent.class, "StatusLineComponent.Cancel"));
               putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
           } else {
               Image icon = (Image)UIManager.get("nb.progress.cancel.icon");
               if (icon == null) {
                   // for custom L&F?
                   icon = Utilities.loadImage("org/netbeans/progress/module/resources/buton.png");
               }
               putValue(Action.SMALL_ICON, new ImageIcon(icon));
           }
            setEnabled(handle == null ? false : handle.isAllowCancel());
       }

        public void actionPerformed(ActionEvent actionEvent) {
            if (handle.getState() == InternalHandle.STATE_RUNNING) {
                String message = NbBundle.getMessage(ListComponent.class, "Cancel_Question", handle.getDisplayName());
                String title = NbBundle.getMessage(ListComponent.class, "Cancel_Question_Title");
                NotifyDescriptor dd = new NotifyDescriptor(message, title, 
                                           NotifyDescriptor.YES_NO_OPTION,
                                           NotifyDescriptor.QUESTION_MESSAGE, null, null);
                Object retType = DialogDisplayer.getDefault().notify(dd);
                if (retType == NotifyDescriptor.YES_OPTION) {
                    handle.requestCancel();
                }
            }
        }
    }

    private class ViewAction extends AbstractAction {
        public ViewAction() {
            putValue(Action.NAME, NbBundle.getMessage(ListComponent.class, "StatusLineComponent.View"));
            setEnabled(handle == null ? false : handle.isAllowView());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        }
        public void actionPerformed(ActionEvent actionEvent) {
            if (handle != null) {
                handle.requestView();
            }
        }
    }    

    private class WatchAction extends AbstractAction {
        public WatchAction() {
            putValue(Action.NAME, NbBundle.getMessage(ListComponent.class, "ListComponent.Watch"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
            setEnabled(true);
        }
        public void actionPerformed(ActionEvent actionEvent) {
            if (handle != null) {
                handle.requestExplicitSelection();
            }
        }
    }    
    
    
    private static final int UPPERMARGIN = 3;
    private static final int LEFTMARGIN = 2;
    private static final int BOTTOMMARGIN = 2;
    private static final int BETWEENTEXTMARGIN = 3;
    
    static final int ITEM_WIDTH = 400;
    
    private class CustomLayout implements LayoutManager {
        
        /**
         * If the layout manager uses a per-component string,
         * adds the component <code>comp</code> to the layout,
         * associating it 
         * with the string specified by <code>name</code>.
         * 
         * @param name the string to be associated with the component
         * @param comp the component to be added
         */
        public void addLayoutComponent(String name, java.awt.Component comp) {
        }

        /**
         * Calculates the preferred size dimensions for the specified 
         * container, given the components it contains.
         * @param parent the container to be laid out
         *  
         * @see #minimumLayoutSize
         */
        public Dimension preferredLayoutSize(java.awt.Container parent) {
            int height = UPPERMARGIN + mainHeight + BETWEENTEXTMARGIN + dynaHeight + BOTTOMMARGIN;
            return new Dimension(ITEM_WIDTH, height);
        }

        /**
         * 
         * Lays out the specified container.
         * @param parent the container to be laid out 
         */
        public void layoutContainer(java.awt.Container parent) {
            int parentWidth = parent.getWidth();
            int parentHeight = parent.getHeight();
            int offset = parentWidth - 18;
            if (closeButton != null) {
                closeButton.setBounds(offset, UPPERMARGIN, 18, mainHeight);            
            }
            // have the bar approx 30 percent of the width
            int barOffset = offset - (ITEM_WIDTH / 3);
            bar.setBounds(barOffset, UPPERMARGIN, offset - barOffset, mainHeight);
            mainLabel.setBounds(LEFTMARGIN, UPPERMARGIN, barOffset - LEFTMARGIN, mainHeight);
            dynaLabel.setBounds(LEFTMARGIN, mainHeight + UPPERMARGIN + BETWEENTEXTMARGIN, 
                                parentWidth - LEFTMARGIN, dynaHeight);
        }

        /**
         * 
         * Calculates the minimum size dimensions for the specified 
         * container, given the components it contains.
         * @param parent the component to be laid out
         * @see #preferredLayoutSize
         */
        public Dimension minimumLayoutSize(java.awt.Container parent) {
            return preferredLayoutSize(parent);
        }

        /**
         * Removes the specified component from the layout.
         * @param comp the component to be removed
         */
        public void removeLayoutComponent(java.awt.Component comp) {
        }

        
    }
}