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
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.ProgressEvent;
import org.netbeans.progress.module.ProgressListAction;
import org.netbeans.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.progress.spi.TaskModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;


/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class StatusLineComponent extends JPanel implements ProgressUIWorkerWithModel {
    private NbProgressBar bar;
    private JLabel label;
    private JSeparator separator;
    private InternalHandle handle;
    private boolean showingPopup = false;
    private TaskModel model;
    private MouseListener mouseListener;
    private HideAWTListener hideListener;
    private Popup popup;
    private JWindow popupWindow;
    private PopupPane pane;
    private Map<InternalHandle, ListComponent> handleComponentMap;
    private final int prefferedHeight;
    private JButton closeButton;
    /** Creates a new instance of StatusLineComponent */
    public StatusLineComponent() {
        handleComponentMap = new HashMap<InternalHandle, ListComponent>();
        FlowLayout flay = new FlowLayout();
        flay.setVgap(1);
        flay.setHgap(5);
        setLayout(flay);
        mouseListener = new MListener();
        addMouseListener(mouseListener);
        hideListener = new HideAWTListener();
        
        createLabel();
        createBar();
        // tricks to figure out correct height.
        bar.setStringPainted(true);
        bar.setString("XXX");
        label.setText("XXX");
        prefferedHeight = Math.max(label.getPreferredSize().height, bar.getPreferredSize().height) + 2;
        
        discardLabel();
        discardBar();
        
        pane = new PopupPane();
        pane.getActionMap().put("HidePopup", new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
//                System.out.println("escape pressed - hiding");
                hidePopup();
            }
        });
        pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
        pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
        
        
    }
    
    private void createLabel() {
        discardLabel();
        label = new JLabel();
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(mouseListener);
    }
    
    private void discardLabel() {
        if (label != null) {
            label.removeMouseListener(mouseListener);
            label = null;
        }
    }
    private void createBar() {
        discardBar();
        bar = new NbProgressBar();
        bar.setUseInStatusBar(true);
        bar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        // HACK - put smaller font inside the progress bar to keep
        // the height of the progressbar constant for determinate and indeterminate bars
//        Font fnt = UIManager.getFont("ProgressBar.font");
//        bar.setFont(fnt.deriveFont(fnt.getStyle(), fnt.getSize() - 3));
        bar.addMouseListener(mouseListener);
        
    }
    
    private void discardBar() {
        if (bar != null) {
            bar.removeMouseListener(mouseListener);
            bar = null;
        }
    }
    
    private void createCloseButton() {
        discardCloseButton();
        closeButton = new JButton();
        closeButton.setBorderPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        
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
    }
    
    private void discardCloseButton() {
        closeButton = null;
    }
    
    private void createSeparator() {
        discardSeparator();
        separator = new JSeparator(JSeparator.VERTICAL);
//        separator.setPreferredSize(new Dimension(5, prefferedHeight));
        separator.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
    }
    
    private void discardSeparator() {
        separator = null;
    }
    
    public Dimension getPreferredSize() {
        Dimension retValue;
        retValue = super.getPreferredSize();
        retValue.height = prefferedHeight;
        return retValue;
    }

    public Dimension getMinimumSize() {
        Dimension retValue;
        retValue = super.getMinimumSize();
        retValue.height = prefferedHeight;
        return retValue;
    }        
    
    public Dimension getMaximumSize() {
        Dimension retValue;
        retValue = super.getMaximumSize();
        retValue.height = prefferedHeight;
        return retValue;
    }        
    
    public void setModel(TaskModel mod) {
        model = mod;
        model.addListDataListener(new Listener());
        model.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                pane.updateBoldFont(model.getSelectedHandle());
            }
        });
    }
    
    private void setTooltipForAll() {
        int size = model.getSize();
        String key = "NbProgressBar.tooltip1"; //NOI18N
        if (size == 1) {
            key = "NbProgressBar.tooltip2"; //NOI18N
        }
        String text = NbBundle.getMessage(StatusLineComponent.class, key, new Integer(size));
        setToolTipText(text);
        if (label != null) {
            label.setToolTipText(text);
        }
        if (bar != null) {
            bar.setToolTipText(text);
        }
    }
    
    public void processProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_START) {
            createListItem(event.getSource());
        } else if (event.getType() == ProgressEvent.TYPE_PROGRESS || 
                   event.getType() == ProgressEvent.TYPE_SWITCH || 
                   event.getType() == ProgressEvent.TYPE_SILENT) {
            ListComponent comp = (ListComponent)handleComponentMap.get(event.getSource());
            if (comp == null) {
                createListItem(event.getSource());
                comp = (ListComponent)handleComponentMap.get(event.getSource());
            }
            comp.processProgressEvent(event);
        } else if (event.getType() == ProgressEvent.TYPE_FINISH) {
            removeListItem(event.getSource());
            if (model.getSelectedHandle() != null && handle != model.getSelectedHandle()) {
                ProgressEvent snap = model.getSelectedHandle().requestStateSnapshot();
                initiateComponent(snap);
                if (snap.getSource().isInSleepMode()) {
                    bar.setString(snap.getMessage());
                }
                
            }
        }
        
    }
    
    public void processSelectedProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_START) {
            initiateComponent(event);
            return;
        } else if (event.getType() == ProgressEvent.TYPE_FINISH) {
            //happens only when there's no more handles.
            hidePopup();
            removeAll();
            discardSeparator();
            discardCloseButton();
            discardBar();
            discardLabel();
            //#63393, 61940 fix - removeAll() just invalidates. seems to work without revalidate/repaint on some platforms, fail on others.
            revalidate();
            repaint();
            return;
        } else {
            if (event.getSource() != handle || event.isSwitched() || 
                event.getType() == ProgressEvent.TYPE_SILENT ||
                    // the following condition re-initiates the bar when going from/to sleep mode..
                    (event.getSource().isInSleepMode() != (bar.getClientProperty(NbProgressBar.SLEEPY) != null))) { //NIO18N
                initiateComponent(event);
            }
            if (event.getWorkunitsDone() > 0) {
               bar.setValue(event.getWorkunitsDone());
            }
            bar.setString(getBarString(event.getPercentageDone(), event.getEstimatedCompletion()));
            if (event.getDisplayName() != null) {
                label.setText(event.getDisplayName());
            }
            if (event.getSource().isInSleepMode()) {
                bar.setString(event.getMessage());
            }
            
        } 
    }
    
    static String formatEstimate(long estimate) {
        long minutes = estimate / 60;
        long seconds = estimate - (minutes * 60);
        return "" + minutes + (seconds < 10 ? ":0" : ":") + seconds;
    }
    
    static String getBarString(double percentage, long estimatedCompletion) {
        if (estimatedCompletion != -1) {
            return formatEstimate(estimatedCompletion);
        }
        if (percentage != -1) {
            int rounded = (int) Math.round(percentage);
            if (rounded > 100) {
                rounded = 100;
            }
            return "" + rounded + "%"; //NOI18N
        }
        return "";
    }
    
    private void initiateComponent(ProgressEvent event) {
        handle = event.getSource();
        boolean toShow = false;
        if (label == null) {
            createLabel();
            add(label);
            toShow = true;
            label.setToolTipText(getToolTipText());
        }
        label.setText(handle.getDisplayName());
        
        if (bar == null) {
            createBar();
            add(bar);
            toShow = true;
            bar.setToolTipText(getToolTipText());
            
        }
        NbProgressBar.setupBar(event.getSource(), bar);
        
        if (closeButton == null) {
            createCloseButton();
            add(closeButton);
            toShow = true;
        }
        if (separator == null) {
            createSeparator();
            add(separator);
            toShow = true;
        }
        if (handle.isAllowCancel()) {
            closeButton.setAction(new CancelAction(false));
        } else {
            closeButton.setAction(new EmptyCancelAction());
        }
        if (toShow) {
            revalidate();
            repaint();
        }
    }
    
    private class Listener implements ListDataListener {
        public void intervalAdded(ListDataEvent e) {
            setTooltipForAll();
        }
        
        public void intervalRemoved(ListDataEvent e) {
            setTooltipForAll();
        }
        
        
        public void contentsChanged(ListDataEvent e) {
            setTooltipForAll();
        }
    }
    
    public void hidePopup() {
        if (popupWindow != null) {
//            popupWindow.getContentPane().removeAll();
            popupWindow.setVisible(false);
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeComponentListener(hideListener);
        showingPopup = false;
    }
    
    private void createListItem(InternalHandle handle) {
        ListComponent comp;
        if (handleComponentMap.containsKey(handle)) {
            // happens when we click to display on popup and there is a 
            // new handle waiting in the queue.
            comp = handleComponentMap.get(handle);
        } else {
            comp = new ListComponent(handle);
            handleComponentMap.put(handle, comp);
        }
        pane.addListComponent(comp);            
        pane.updateBoldFont(model.getSelectedHandle());
        if (showingPopup) {
            resizePopup();
        }
    }
    
    private void removeListItem(InternalHandle handle) {
        handleComponentMap.remove(handle);
        pane.removeListComponent(handle);
        pane.updateBoldFont(model.getSelectedHandle());
        if (showingPopup) {
            resizePopup();
        }
    }

    
    public void showPopup() {
        if (showingPopup) {
            return;
        }
        InternalHandle[] handles = model.getHandles();
        if (handles.length == 0) {
            // just in case..
            return;
        }
        showingPopup = true;
        
        // NOT using PopupFactory
        // 1. on linux, creates mediumweight popup taht doesn't refresh behind visible glasspane
        // 2. on mac, needs an owner frame otherwise hiding tooltip also hides the popup. (linux requires no owner frame to force heavyweight)
        // 3. the created window is not focusable window
        if (popupWindow == null) {
            popupWindow = new JWindow(WindowManager.getDefault().getMainWindow());
            popupWindow.getContentPane().add(pane);
        }
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        WindowManager.getDefault().getMainWindow().addWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().addComponentListener(hideListener);
        resizePopup();
        popupWindow.setVisible(true);
        pane.requestFocus();
//        System.out.println("     window focusable=" + popupWindow.isFocusableWindow());
    }
    
    private void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0,0);
        SwingUtilities.convertPointToScreen(point, this);
        Dimension dim = popupWindow.getSize();
        //#63265 
        Rectangle usableRect = Utilities.getUsableScreenBounds();
        Point loc = new Point(point.x + this.getSize().width - dim.width - separator.getSize().width - 5 * 2  , point.y - dim.height - 5);
        // -5 in x coordinate is becuase of the hgap between the separator and button and separator and edge
        if (! usableRect.contains(loc)) {
            loc = new Point(loc.x, point.y + 5 + this.getSize().height);
        }
            // +4 here because of the width of the close button in popup, we
            // want the progress bars to align visually.. but there's separator in status now..
        popupWindow.setLocation(loc);
//        System.out.println("count=" + count);
//        System.out.println("offset =" + offset);
    }
    
    private class HideAWTListener extends ComponentAdapter implements  AWTEventListener, WindowStateListener {
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent)aWTEvent;
                if (mv.getClickCount() > 0) {
                    //#118828
                    if (! (aWTEvent.getSource() instanceof Component)) {
                        return;
                    }
                    Component comp = (Component)aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed("progresspopup", comp); //NOI18N
                    Container barpar = SwingUtilities.getAncestorOfClass(StatusLineComponent.class, comp);
                    if (par == null && barpar == null) {
                        hidePopup();
                    }
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
//                } else if (((oldState & Frame.ICONIFIED) == Frame.ICONIFIED) && 
//                           ((newState & Frame.ICONIFIED) == 0 )) {
//                    //TODO remember we showed before and show again? I guess not worth the efford, not part of spec.
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
    
    private class MListener extends MouseAdapter {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                showMenu(e);
            } else {
                if (showingPopup) {
                    hidePopup();
                } else {
                    showPopup();
                }
            }
        }
        
    }
    
    private void showMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new ProgressListAction(NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.ShowProcessList"))); 
        popup.add(new ViewAction());
        popup.add(new CancelAction(true));
        popup.show((Component)e.getSource(), e.getX(), e.getY());
    }
    
  private class CancelAction extends AbstractAction {
        public CancelAction(boolean text) {
            if (text) {
                putValue(Action.NAME, NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.Cancel"));
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
            InternalHandle hndl = handle;
            if (hndl !=null && hndl.getState() == InternalHandle.STATE_RUNNING) {
                String message = NbBundle.getMessage(StatusLineComponent.class, "Cancel_Question", handle.getDisplayName());
                String title = NbBundle.getMessage(StatusLineComponent.class, "Cancel_Question_Title");
                NotifyDescriptor dd = new NotifyDescriptor(message, title, 
                                           NotifyDescriptor.YES_NO_OPTION,
                                           NotifyDescriptor.QUESTION_MESSAGE, null, null);
                Object retType = DialogDisplayer.getDefault().notify(dd);
                if (retType == NotifyDescriptor.YES_OPTION && hndl.getState() == InternalHandle.STATE_RUNNING) {
                    hndl.requestCancel();
                }
            }
        }
    }

    private class ViewAction extends AbstractAction {
        public ViewAction() {
            putValue(Action.NAME, NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.View"));
            setEnabled(handle == null ? false : handle.isAllowView());
            
        }
        public void actionPerformed(ActionEvent actionEvent) {
            if (handle != null) {
                handle.requestView();
            }
        }
    }    
    
    
    private class EmptyCancelAction extends AbstractAction {
        public EmptyCancelAction() {
            setEnabled(false);
            putValue(Action.SMALL_ICON, new Icon() {
                public int getIconHeight() {
                    return 12;
                }
                public int getIconWidth() {
                    return 12;
                }
                public void paintIcon(Component c, Graphics g, int x, int y) {
                }
            });
            putValue(Action.NAME, "");
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

}
