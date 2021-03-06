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


package org.netbeans.core.windows.view.ui;


import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.CharConversionException;
import java.text.MessageFormat;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicHTML;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.dnd.ZOrderManager;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;


/** 
 * Implementation of <code>ModeContainer</code> for separate mode kind.
 *
 * @author  Peter Zavadsky
 */
public final class DefaultSeparateContainer extends AbstractModeContainer {

    /** Separate mode represented by JFrame or null if dialog is used */
    private final ModeFrame modeFrame;
    /** Separate mode represented by JDialog or null if frame is used */
    private final ModeDialog modeDialog;

    /** Creates a DefaultSeparateContainer. */
    public DefaultSeparateContainer(final ModeView modeView, WindowDnDManager windowDnDManager, Rectangle bounds, int kind) {
        super(modeView, windowDnDManager, kind);
        // JFrame or JDialog according to the mode kind
        if (kind == Constants.MODE_KIND_EDITOR) {
            modeFrame = new ModeFrame(this, modeView);
            MainWindow.initFrameIcons(modeFrame);
            modeDialog = null;
        } else {
            modeDialog = new ModeDialog(WindowManager.getDefault().getMainWindow(), this, modeView);
            modeFrame = null;
        }
        Window w = getModeUIWindow();
        ((RootPaneContainer) w).getContentPane().add(tabbedHandler.getComponent());
        w.setBounds(bounds);
    }
    
    public void requestAttention (TopComponent tc) {
        //not implemented
    }
    
    public void cancelRequestAttention (TopComponent tc) {
        //not implemented
    }

    /** */
    protected Component getModeComponent() {
        return getModeUIWindow();
    }
    
    protected Tabbed createTabbed() {
        Tabbed tabbed;
        if(getKind() == Constants.MODE_KIND_EDITOR) {
            tabbed = new TabbedAdapter(Constants.MODE_KIND_EDITOR);
        } else {
            tabbed = new TabbedAdapter(Constants.MODE_KIND_VIEW);
        }
        return tabbed;    
    }    
    
    protected void updateTitle (String title) {
        getModeUIBase().updateTitle(title);
    }
    
    protected void updateActive (boolean active) {
        Window w = getModeUIWindow();
        if(active) {
            if (w.isVisible() && !w.isActive()) {
                w.toFront();
            }
        } 
    }
    
    public boolean isActive () {
        return getModeUIWindow().isActive();
    }
    
    protected boolean isAttachingPossible() {
        return false;
    }
    
    protected TopComponentDroppable getModeDroppable() {
        return getModeUIBase();
    }

    private Window getModeUIWindow () {
        return modeFrame != null ? modeFrame : modeDialog;
    }

    private ModeUIBase getModeUIBase () {
        return (ModeUIBase)getModeUIWindow();
    }

    /** Separate mode UI backed by JFrame.
     *
     * [dafe] Whole DnD of window system expects that ModeComponent and
     * TopComponentDroppable implementation must exist in AWT hierarchy,
     * so I have to extend Swing class here, not just use it. That's why all this
     * delegating stuff.     
     */
    private static class ModeFrame extends JFrame implements ModeUIBase {

        /** Base helper to delegate to for common things */
        private SharedModeUIBase modeBase;
   
        public ModeFrame (AbstractModeContainer abstractModeContainer, ModeView view) {
            super();
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            modeBase = new SharedModeUIBaseImpl(abstractModeContainer, view, this);
        }

        public ModeView getModeView() {
            return modeBase.getModeView();
        }

        public int getKind() {
            return modeBase.getKind();
        }

        public Shape getIndicationForLocation(Point location) {
            return modeBase.getIndicationForLocation(location);
        }

        public Object getConstraintForLocation(Point location) {
            return modeBase.getConstraintForLocation(location);
        }

        public Component getDropComponent() {
            return modeBase.getDropComponent();
        }

        public ViewElement getDropViewElement() {
            return modeBase.getDropViewElement();
        }

        public boolean canDrop(TopComponent transfer, Point location) {
            return modeBase.canDrop(transfer, location);
        }

        public boolean supportsKind(int kind, TopComponent transfer) {
            return modeBase.supportsKind(kind, transfer);
        }

        /** Actually sets title for the frame
         */
        public void updateTitle(String title) {
            // extract HTML from text - Output window (and soon others) uses it
            if (BasicHTML.isHTMLString(title)) {
                char[] c = title.toCharArray();
                StringBuffer sb = new StringBuffer(title.length());
                boolean inTag = false;
                boolean inEntity = false;
                for (int i=0; i < c.length; i++) {
                    if (inTag && c[i] == '>') { //NOI18N
                        inTag = false;
                        continue;
                    }
                    if (!inTag && c[i] == '<') { //NOI18N
                        inTag = true;
                        continue;
                    }
                    if (!inTag) {
                        sb.append(c[i]);
                    }
                }
                //XXX, would be nicer to support the full complement of entities...
                title = Utilities.replaceString(sb.toString(), "&nbsp;", " "); //NOI18N
            }
            String completeTitle = MessageFormat.format(
                    NbBundle.getMessage(DefaultSeparateContainer.class, "CTL_SeparateEditorTitle"),
                    title);
            setTitle(completeTitle);
        }

    } // end of ModeFrame

    /** Separate mode UI backed by JFrame.
     *
     * [dafe] Whole DnD of window system expects that ModeComponent and
     * TopComponentDroppable implementation must exist in AWT hierarchy,
     * so I have to extend Swing class here, not just use it. That's why all this
     * delegating stuff.
     */     
    private static class ModeDialog extends JDialog implements ModeUIBase {

        /** Base helper to delegate to for common things */
        private SharedModeUIBase modeBase;
    
        public ModeDialog (Frame owner, AbstractModeContainer abstractModeContainer, ModeView view) {
            super(owner);
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            modeBase = new SharedModeUIBaseImpl(abstractModeContainer, view, this);
        }

        public ModeView getModeView() {
            return modeBase.getModeView();
        }

        public int getKind() {
            return modeBase.getKind();
        }

        public Shape getIndicationForLocation(Point location) {
            return modeBase.getIndicationForLocation(location);
        }

        public Object getConstraintForLocation(Point location) {
            return modeBase.getConstraintForLocation(location);
        }

        public Component getDropComponent() {
            return modeBase.getDropComponent();
        }

        public ViewElement getDropViewElement() {
            return modeBase.getDropViewElement();
        }

        public boolean canDrop(TopComponent transfer, Point location) {
            return modeBase.canDrop(transfer, location);
        }

        public boolean supportsKind(int kind, TopComponent transfer) {
            return modeBase.supportsKind(kind, transfer);
        }

        public void updateTitle(String title) {
            // noop - no title for dialogs
        }

    } // end of ModeDialog

    /** Defines shared common attributes of UI element for separate mode. */
    public interface SharedModeUIBase extends ModeComponent, TopComponentDroppable {
    }

    /** Defines base of UI element for separate mode, containing extras
     * in which JDialog and JFrame separate mode differs
     */
    public interface ModeUIBase extends ModeComponent, TopComponentDroppable {
        public void updateTitle (String title);
    }

    /** Base impl of separate UI element, used as delegatee for shared things.
     */
    private static class SharedModeUIBaseImpl implements SharedModeUIBase {
        
        private final AbstractModeContainer abstractModeContainer;
        private final ModeView modeView;
        private long frametimestamp = 0;

        /** UI representation of separate window */
        private Window window;
        
        public SharedModeUIBaseImpl (AbstractModeContainer abstractModeContainer, ModeView view, Window window) {
            this.abstractModeContainer = abstractModeContainer;
            this.modeView = view;
            this.window = window;
            initWindow(window);
            attachListeners(window);
        }

        /** Creates and returns window appropriate for type of dragged TC;
         * either frame or dialog.
         */
        private void initWindow (Window w) {
            // mark this as separate window, so that ShortcutAndMenuKeyEventProcessor
            // allows normal shorcut processing like inside main window
            ((RootPaneContainer)w).getRootPane().putClientProperty(
                    Constants.SEPARATE_WINDOW_PROPERTY, Boolean.TRUE);

            // register in z-order mng
            ZOrderManager.getInstance().attachWindow((RootPaneContainer)w);
        }

        private void attachListeners (Window w) {
            w.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    modeView.getController().userClosingMode(modeView);
                    ZOrderManager.getInstance().detachWindow((RootPaneContainer)window);
                }

                public void windowClosed (WindowEvent evt) {
                    ZOrderManager.getInstance().detachWindow((RootPaneContainer)window);
                }

                public void windowActivated(WindowEvent event) {
                    if (frametimestamp != 0 && System.currentTimeMillis() > frametimestamp + 500) {
                        modeView.getController().userActivatedModeWindow(modeView);
                    }
                    frametimestamp = System.currentTimeMillis();
                }
                public void windowOpened(WindowEvent event) {
                    frametimestamp = System.currentTimeMillis();
                }
            });  // end of WindowListener

            w.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent evt) {
                    /*if(DefaultSeparateContainer.this.frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                        // Ignore changes when the frame is in maximized state.
                        return;
                    }*/

                    modeView.getController().userResizedModeBounds(modeView, window.getBounds());
                }

                public void componentMoved(ComponentEvent evt) {
                    /*if(DefaultSeparateContainer.this.frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                        // Ignore changes when the frame is in maximized state.
                        return;
                    }*/

                    modeView.getController().userResizedModeBounds(modeView, window.getBounds());
                }

            }); // end of ComponentListener
        
        
            window.addWindowStateListener(new WindowStateListener() {
                public void windowStateChanged(WindowEvent evt) {
                    if (!Constants.AUTO_ICONIFY) {
                        modeView.getController().userChangedFrameStateMode(modeView, evt.getNewState());
                    } else {
                         // All the timestamping is a a workaround beause of buggy GNOME
                        // and of its kind who iconify the windows on leaving the desktop.
                        Component comp = modeView.getComponent();
                        if (comp instanceof Frame /*&& comp.isVisible() */) {
                            long currentStamp = System.currentTimeMillis();
                            if (currentStamp > (modeView.getUserStamp() + 500) && currentStamp > (modeView.getMainWindowStamp() + 1000)) {
                                modeView.getController().userChangedFrameStateMode(modeView, evt.getNewState());
                            } else {
                                modeView.setUserStamp(0);
                                modeView.setMainWindowStamp(0);
                                modeView.updateFrameState();
                            }
                            long stamp = System.currentTimeMillis();
                            modeView.setUserStamp(stamp);
                        }
                    }
                }
            }); // end of WindowStateListener

        }

        public void setVisible(boolean visible) {
            frametimestamp = System.currentTimeMillis();
            window.setVisible(visible);
        }
        
        public void toFront() {
            frametimestamp = System.currentTimeMillis();
            window.toFront();
        }
        
        public ModeView getModeView() {
            return abstractModeContainer.getModeView();
        }
        
        public int getKind() {
            return abstractModeContainer.getKind();
        }

        // TopComponentDroppable>>
        public Shape getIndicationForLocation(Point location) {
            return abstractModeContainer.getIndicationForLocation(location);
        }
        
        public Object getConstraintForLocation(Point location) {
            return abstractModeContainer.getConstraintForLocation(location);
        }
        
        public Component getDropComponent() {
            return abstractModeContainer.getDropComponent();
        }
        
        public ViewElement getDropViewElement() {
            return abstractModeContainer.getDropModeView();
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            return abstractModeContainer.canDrop(transfer);
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            // this is not a typo, yes it should be the same as canDrop
            return abstractModeContainer.canDrop(transfer);
            //return true;
            /*
             if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            return kind == Constants.MODE_KIND_VIEW || kind == Constants.MODE_KIND_SLIDING;
             */
        }
        // TopComponentDroppable<<


    } // End of ModeWindow.
    
}

