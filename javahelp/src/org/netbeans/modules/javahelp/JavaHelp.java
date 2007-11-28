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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.javahelp;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

// [PENDING] should event dispatch thread be used thruout?

/** Help implementation using the JavaHelp 1.x system.
* @author Jesse Glick, Richard Gregor
*/
public final class JavaHelp extends AbstractHelp implements AWTEventListener {

    /** Make a JavaHelp implementation of the Help.Impl interface.
     *Or, use {@link #getDefaultJavaHelp}.
     */
    public JavaHelp() {
        Installer.log.fine("JavaHelp created");
        if (!isModalExcludedSupported()) {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
        }
    }
    void deactivate() {
        if (!isModalExcludedSupported()) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        }
    }

    // [PENDING] hold help sets weakly? softly? try to conserve memory...
    /** The master help set.
     */
    private HelpSet master = null;
    /** map from help sets to (soft refs to) components showing them */
    private Map<HelpSet,Reference<JHelp>> availableJHelps = new HashMap<HelpSet,Reference<JHelp>>();
    /** viewer (may be invisible) showing help normally; null until first used; if invisible, is empty */
    private JFrame frameViewer = null;
    /** viewer showing help parented to current modal dialog; initially null */
    private JDialog dialogViewer = null;
    /** whether user explicitly closed dialog viewer.
     * true - frame viewer was initially open, then reparented to dialog viewer,
     * then user closes main dialog and we ought to reparent to frame viewer
     * false - frame viewer not initially open anyway, or it was but the user
     * explicitly closed it as a dialog viewer, we should leave it closed
     */
    private boolean reparentToFrameLater = false;
    /** the modal dialog(s) currently in effect */
    private Stack<Dialog> currentModalDialogs = new Stack<Dialog>();
    /** modal dialogs stack has been used successfully */
    private boolean currentModalDialogsReady = false;
    /** last-displayed JHelp */
    private JHelp lastJH = null;
    
    /** progress of merging help sets; max is # of sets to merge */
    private static final BoundedRangeModel mergeModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
    
    private ProgressHandle progressHandle = null;
    
    private static final boolean isJdk15;
    
    static {
        String javaVersion = System.getProperty("java.version");
        isJdk15 = javaVersion.startsWith("1.5");
    }

    /** Get the master help set that others will be merged into.
     * @return the master help set
     */
    private synchronized HelpSet getMaster() {
        if (master == null) {
            ClassLoader loader = JavaHelp.class.getClassLoader();
            try {
                master = new HelpSet(loader, new URL("nbresloc:/org/netbeans/modules/javahelp/resources/masterHelpSet.xml")); // NOI18N
                Collection<? extends HelpSet> sets = getHelpSets();
                List<HelpSet> toMerge = new ArrayList<HelpSet>(Math.min(1, sets.size()));
                for (HelpSet hs: sets) {
                    if (shouldMerge(hs)) {
                        toMerge.add(hs);
                    }
                }
                mergeModel.setValue(0);
                mergeModel.setMaximum(toMerge.size());
                for (HelpSet hs: toMerge) {
                    master.add(hs);
                    mergeModel.setValue(mergeModel.getValue() + 1);
                }
            } catch (HelpSetException hse) {
                Installer.log.log(Level.WARNING, null, hse);
                master = new HelpSet();
            } catch (MalformedURLException mfue) {
                mfue.printStackTrace();
                throw new IllegalStateException();
            }
        }
        return master;
    }
    
    /** Called when set of helpsets changes.
     * Here, clear the master helpset, since it may
     * need to have different contents (or a different
     * order of contents) when next viewed.
     */    
    protected void helpSetsChanged() {
        synchronized (this) {
            // XXX might be better to incrementally add/remove helpsets?
            // Unfortunately the JavaHelp API does not provide a way to
            // insert them except in last position, which prevents smart
            // navigator ordering.
            master = null;
        }
        mergeModel.setValue(0);
        mergeModel.setMaximum(0);
        super.helpSetsChanged();
    }
    
    private Dialog currentModalDialog() {
        if (currentModalDialogs.empty()) {
            Window w = HelpAction.WindowActivatedDetector.getCurrentActivatedWindow();
            if (!currentModalDialogsReady && (w instanceof Dialog) &&
                    !(w instanceof ProgressDialog) && w != dialogViewer && ((Dialog)w).isModal()) {
                // #21286. A modal dialog was opened before JavaHelp was even created.
                Installer.log.fine("Early-opened modal dialog: " + w.getName() + " [" + ((Dialog)w).getTitle() + "]");
                return (Dialog)w;
            } else {
                return null;
            }
        } else {
            return currentModalDialogs.peek();
        }
    }
    
    private void ensureFrameViewer() {
        Installer.log.fine("ensureFrameViewer");
        if (frameViewer == null) {
            Installer.log.fine("\tcreating new");
            frameViewer = new JFrame();
            frameViewer.setIconImage(Utilities.loadImage("org/netbeans/modules/javahelp/resources/help.gif")); // NOI18N
            frameViewer.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHelp.class, "ACSD_JavaHelp_viewer"));
            
            if (isModalExcludedSupported()) {
                setModalExcluded(frameViewer);
                frameViewer.getRootPane().putClientProperty("netbeans.helpframe", Boolean.TRUE); // NOI18N
            }
        }
    }
    private void ensureDialogViewer() {
        Installer.log.fine("ensureDialogViewer");
        Dialog parent = currentModalDialog();
        if (dialogViewer != null && dialogViewer.getOwner() != parent) {
            Installer.log.fine("\tdisposing old");
            dialogViewer.setVisible(false);
            dialogViewer.dispose();
            dialogViewer = null;
        }
        if (dialogViewer == null) {
            Installer.log.fine("\tcreating new");
            dialogViewer = new JDialog(parent);
            dialogViewer.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHelp.class, "ACSD_JavaHelp_viewer"));
        }
    }
    
    private void displayHelpInFrame(JHelp jh) {
        Installer.log.fine("displayHelpInFrame");
        if (jh == null) jh = lastJH;
        if (jh == null) throw new IllegalStateException();
        boolean newFrameViewer = (frameViewer == null);
        ensureFrameViewer();
        if (dialogViewer != null) {
            Installer.log.fine("\tdisposing old dialog");
            dialogViewer.setVisible(false);
            dialogViewer.getContentPane().removeAll();
            dialogViewer.dispose();
            dialogViewer = null;
        }
        if (frameViewer.getContentPane().getComponentCount() > 0 &&
                frameViewer.getContentPane().getComponent(0) != jh) {
            Installer.log.fine("\treplacing content");
            frameViewer.getContentPane().removeAll();
        }
        if (frameViewer.getContentPane().getComponentCount() == 0) {
            Installer.log.fine("\tadding content");
            frameViewer.getContentPane().add(jh, BorderLayout.CENTER);
            frameViewer.setTitle(jh.getModel().getHelpSet().getTitle());
            frameViewer.pack();
        }
        if (newFrameViewer) {
            // #22445: only do this stuff once when frame is made.
            // After that we need to remember the size and position.
            Rectangle bounds = Utilities.getUsableScreenBounds();
            Dimension frameSize = frameViewer.getSize();
            // #108255: Increase size of Help window by 30%
            frameSize.width = (int) (1.3 * frameSize.width);
            frameSize.height = (int) (1.3 * frameSize.height);
            
            frameViewer.setSize(frameSize);
            
            // #11018: have mercy on little screens
            if (frameSize.width > bounds.width) {
                frameSize.width = bounds.width;
            }
            if (frameSize.height > bounds.height) {
                frameSize.height = bounds.height;
            }
            if ((frameSize.width > bounds.width) || (frameSize.height > bounds.height)) {
                frameViewer.setSize(frameSize);
            }
            //Put frame to top right
            frameViewer.setLocation(new Point(bounds.x + bounds.width - frameViewer.getSize().width, bounds.y));
        }
        
        frameViewer.setState(Frame.NORMAL);
        if (frameViewer.isVisible()) {
            frameViewer.repaint();
            frameViewer.toFront(); // #20048
            Installer.log.fine("\talready visible, just repainting");
        } else {
            frameViewer.setVisible(true);
        }
        //#29417: This call of requestFocus causes lost focus when Help window
        //is reopened => removed.
        //frameViewer.requestFocus();
        lastJH = jh;
    }
    private void displayHelpInDialog(JHelp jh) {
        Installer.log.fine("displayHelpInDialog");
        if (jh == null) jh = lastJH;
        if (jh == null) throw new IllegalStateException();
        ensureDialogViewer();
        Rectangle bounds = null;
        if (frameViewer != null) {
            Installer.log.fine("\thiding old frame viewer");
            if (frameViewer.isVisible()) {
                bounds = frameViewer.getBounds();
                frameViewer.setVisible(false);
            }
            frameViewer.getContentPane().removeAll();
        }
        if (dialogViewer.getContentPane().getComponentCount() > 0 &&
                dialogViewer.getContentPane().getComponent(0) != jh) {
            Installer.log.fine("\tchanging content");
            dialogViewer.getContentPane().removeAll();
        }
        if (dialogViewer.getContentPane().getComponentCount() == 0) {
            Installer.log.fine("\tadding content");
            dialogViewer.getContentPane().add(jh, BorderLayout.CENTER);
            dialogViewer.setTitle(jh.getModel().getHelpSet().getTitle());
            dialogViewer.pack();
        }
        if (bounds != null) {
            Installer.log.fine("\tcopying bounds from frame viewer: " + bounds);
            dialogViewer.setBounds(bounds);
        }
        rearrange(currentModalDialog());
        if (dialogViewer.isVisible()) {
            Installer.log.fine("\talready visible, just repainting");
            dialogViewer.repaint();
        } else {
            dialogViewer.setVisible(true);
        }
        lastJH = jh;
    }
    /*
    private void closeFrameViewer() {
        if (frameViewer == null || !frameViewer.isVisible()) throw new IllegalStateException();
        Installer.log.fine("Closing frame viewer");
        frameViewer.setVisible(false);
        frameViewer.getContentPane().removeAll();
    }
    private void closeDialogViewer() {
        if (dialogViewer == null || !dialogViewer.isVisible()) throw new IllegalStateException();
        Installer.log.fine("Closing dialog viewer");
        dialogViewer.setVisible(false);
        dialogViewer.getContentPane().removeAll();
        dialogViewer.dispose();
        dialogViewer = null;
    }
     */
    
    /** Show some help.
     *This is the basic call which should be used externally
     *and is the result of {@link TopManager#showHelp}.
     *Handles null contexts, missing or null help IDs, and null URLs.
     *If there is any problem, shows the master set
     *instead, or it may also create a new help window.
     *Works correctly if invoked while a modal dialog is open--creates a new modal
     *dialog with the help. Else creates a frame to view the help in.
     * @param ctx the help context to display
     * @param showmaster whether to show the master help set or not
     */
    public void showHelp(HelpCtx ctx, final boolean showmaster) {
        final HelpCtx ctx2 = (ctx != null) ? ctx : HelpCtx.DEFAULT_HELP;
        if (!SwingUtilities.isEventDispatchThread()) {
            Installer.log.fine("showHelp later...");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showHelp(ctx2, showmaster);
                }
            });
            return;
        }
        LogRecord r = new LogRecord(Level.FINE, "LOG_SHOWING_HELP"); // NOI18N
        r.setParameters(new Object[] { ctx2.getHelpID() } );
        r.setResourceBundleName("org.netbeans.modules.javahelp.Bundle"); // NOI18N
        r.setResourceBundle(NbBundle.getBundle(JavaHelp.class)); // NOI18N
        r.setLoggerName(Installer.UI.getName());
        Installer.log.log(r);
        Installer.UI.log(r);
        final HelpSet[] hs_ = new HelpSet[1];
        Runnable run = new Runnable() {
            public void run() {
                String id = ctx2.getHelpID();
                if (showmaster || ctx2.equals(HelpCtx.DEFAULT_HELP) || id == null) {
                    Installer.log.fine("getting master...");
                    hs_[0] = getMaster();
                    Installer.log.fine("getting master...done");
                }
                if (hs_[0] == null ||
                        /* #22670: if ID in hidden helpset, use that HS, even if showmaster */
                        (id != null && !hs_[0].getCombinedMap().isValidID(id, hs_[0]))) {
                    Installer.log.fine("finding help set for " + id + "...");
                    hs_[0] = findHelpSetForID(id);
                    Installer.log.fine("finding help set for " + id + "...done");
                }
            }
        };
        if (master == null) {
            // Computation required. Show the progress dialog and do the computation
            // in a separate thread. When finished, the progress dialog will hide
            // itself and control will return to event thread.
            Installer.log.fine("showing progress dialog...");
            progressHandle = ProgressHandleFactory.createHandle("");
            createProgressDialog(run, currentModalDialog()).setVisible(true);
            progressHandle.finish();
            Installer.log.fine("dialog done.");
        } else {
            // Nothing much to do, run it synchronously in event thread.
            run.run();
        }
        HelpSet hs = hs_[0];
        if (hs == null) {
            // Interrupted dialog?
            return;
        }
        JHelp jh = createJHelp(hs);
        if (jh == null) {
            return;
        }

        if (isModalExcludedSupported()) {
            displayHelpInFrame(jh);
        } else {
            if (currentModalDialog() == null) {
                Installer.log.fine("showing as non-dialog");
                displayHelpInFrame(jh);
            } else {
                Installer.log.fine("showing as dialog");
                displayHelpInDialog(jh);
            }
        }
        displayInJHelp(jh, ctx2.getHelpID(), ctx2.getHelp());
    }

    /** Handle modal dialogs opening and closing. Note reparentToFrameLater state = rTFL.
     * Cases:
     * 1. No viewer open. Dialog opened. Push it on stack. rTFL = false.
     * 2. No viewer open, !rTFL. Top dialog closed. Pop it.
     * 3. No viewer open, rTFL. Only top dialog closed. Pop it. Create frame viewer.
     * 4. No viewer open, rTFL. Some top dialog closed. Pop it. Create dialog viewer.
     * 5. Frame viewer open. Dialog opened. Push it. Close frame viewer. Create dialog viewer. rTFL = true.
     * 6. Dialog viewer open. Dialog opened. Push it. Reparent dialog viewer.
     * 7. Dialog viewer open. Viewer closed. rTFL = false.
     * It cannot happen that the dialog viewer is still open when the top dialog has been
     * closed, as AWT will automatically close the dialog viewer first. However in this case
     * it sends only CLOSED for the dialog viewer. If the user closes it, CLOSING is sent at
     * that time, and CLOSED also later when the main dialog is closed.
     */
    public void eventDispatched(AWTEvent awtev) {
        WindowEvent ev = (WindowEvent)awtev;
        int type = ev.getID();
        Window w = ev.getWindow();
        if (type == WindowEvent.WINDOW_CLOSING && w == dialogViewer) {
            Installer.log.fine("7. Dialog viewer open. Viewer closed. rTFL = false.");
            reparentToFrameLater = false;
        }
        if (type != WindowEvent.WINDOW_CLOSED && type != WindowEvent.WINDOW_OPENED) {
            //Installer.log.fine("uninteresting window event: " + ev);
            return;
        }
        if (w instanceof Dialog) {
            Dialog d = (Dialog)w;
            String dlgClass = d.getClass().getName();
            if ((d.isModal() && !(d instanceof ProgressDialog)) || d == dialogViewer) {
                //#40950: Print and Page Setup dialogs was not displayed from java help window.
                if ("sun.awt.windows.WPageDialog".equals(dlgClass) || // NOI18N
                    "sun.awt.windows.WPrintDialog".equals(dlgClass) || // NOI18N
                    "sun.print.ServiceDialog".equals(dlgClass) ||
                    "apple.awt.CPrinterJobDialog".equals(dlgClass) ||
                    "apple.awt.CPrinterPageDialog".equals(dlgClass)) { // NOI18N
                    //It is the print or print settings dialog for javahelp, do nothing
                    return;
                }
                
                //#47150: Race condition in toolkit if two dialogs are shown in a row
                if (d instanceof JDialog) {
                    if ("true".equals(((JDialog)d).getRootPane().getClientProperty("javahelp.ignore.modality"))) { //NOI18N
                        return;
                    }
                }
                
                if (Installer.log.isLoggable(Level.FINE)) {
                    Installer.log.fine("modal (or viewer) dialog event: " + ev + " [" + d.getTitle() + "]");
                }
                if (type == WindowEvent.WINDOW_CLOSED) {
                    if (d == dialogViewer) {
                        // ignore, expected
                    } else if (d == currentModalDialog()) {
                        if (!currentModalDialogs.isEmpty()) {
                            currentModalDialogs.pop();
                            currentModalDialogsReady = true;
                        } else {
                            Installer.log.log(Level.WARNING, null, new IllegalStateException("Please see IZ #24993")); // NOI18N
                        }
                        showDialogStack();
                        if ((frameViewer == null || !frameViewer.isVisible() ||
                             /* 14393 */frameViewer.getState() == Frame.ICONIFIED) &&
                            (dialogViewer == null || !dialogViewer.isVisible())) {
                            if (!reparentToFrameLater) {
                                Installer.log.fine("2. No viewer open, !rTFL. Top dialog closed. Pop it.");
                            } else if (currentModalDialog() == null) {
                                Installer.log.fine("3. No viewer open, rTFL. Only top dialog closed. Pop it. Create frame viewer.");
                                //#47150 - reusing the old frame viewer can cause
                                //re-showing the frame viewer to re-show the dialog
                                if (frameViewer != null) {
                                    frameViewer.dispose();
                                    frameViewer = null;
                                }
                                displayHelpInFrame(null);
                            } else {
                                Installer.log.fine("4. No viewer open, rTFL. Some top dialog closed. Pop it. Create dialog viewer.");
                                displayHelpInDialog(null);
                            }
                        } else if (dialogViewer != null && dialogViewer.isVisible()) {
                            Installer.log.warning("dialogViewer should not still be open"); // NOI18N
                        } else {
                            Installer.log.warning("frameViewer visible when a dialog was closing"); // NOI18N
                        }
                    } else {
                        Installer.log.fine("some random modal dialog closed: " + d.getName() + " [" + d.getTitle() + "]");
                    }
                } else {
                    // WINDOW_OPENED
                    if (d != dialogViewer) {
                        currentModalDialogs.push(d);
                        showDialogStack();
                        if ((frameViewer == null || !frameViewer.isVisible() ||
                             /* 14393 */frameViewer.getState() == Frame.ICONIFIED) &&
                            (dialogViewer == null || !dialogViewer.isVisible())) {
                            Installer.log.fine("1. No viewer open. Dialog opened. Push it on stack. rTFL = false.");
                            reparentToFrameLater = false;
                        } else if (frameViewer != null && frameViewer.isVisible()) {
                            Installer.log.fine("5. Frame viewer open. Dialog opened. Push it. Close frame viewer. Create dialog viewer. rTFL = true.");
                            displayHelpInDialog(null);
                            reparentToFrameLater = true;
                        } else if (dialogViewer != null && dialogViewer.isVisible()) {
                            Installer.log.fine("6. Dialog viewer open. Dialog opened. Push it. Reparent dialog viewer.");
                            displayHelpInDialog(null);
                        } else {
                            Installer.log.warning("logic error"); // NOI18N
                        }
                    } else {
                        // dialog viewer opened, fine
                    }
                }
            } else {
                //Installer.log.fine("nonmodal dialog event: " + ev);
            }
        } else {
            //Installer.log.fine("frame event: " + ev);
        }
    }
    private void showDialogStack() {
        if (Installer.log.isLoggable(Level.FINE)) {
            StringBuffer buf = new StringBuffer("new modal dialog stack: ["); // NOI18N
            boolean first = true;
            Iterator it = currentModalDialogs.iterator();
            while (it.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", "); // NOI18N
                }
                buf.append(((Dialog)it.next()).getTitle());
            }
            buf.append("]"); // NOI18N
            Installer.log.fine(buf.toString());
        }
    }
    
    /** If needed, visually rearrange dialogViewer and dlg on screen.
     * If they overlap, try to make them not overlap.
     * @param dlg the visible modal dialog
     */
    private void rearrange(Dialog dlg) {
        Rectangle r1 = dlg.getBounds();
        Rectangle r2 = dialogViewer.getBounds();
        if (r1.intersects(r2)) {
            Installer.log.fine("modal dialog and dialog viewer overlap");
            Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
            int xExtra = s.width - r1.width - r2.width;
            int yExtra = s.height - r1.height - r2.height;
            if(xExtra >= yExtra){
                //compare y axes of r1 and r2 to know how to relocate them - horizontal relocation
                int r1Yaxis = r1.x + (r1.width/2);
                int r2Yaxis = r2.x + (r2.width/2);
                if(r1Yaxis <= r2Yaxis) {
                    Installer.log.fine(" send help to the right");
                    if((r1.x + r1.width + r2.width) <= s.width) {
                        Installer.log.fine("there is enough place fo help");
                        r2.x = r1.x + r1.width;
                    } else {
                        Installer.log.fine("there is not enough place");
                        if((r1.width + r2.width) < s.width) {
                            Installer.log.fine("relocate both");
                            r2.x = s.width - r2.width;
                            r1.x = r2.x - r1.width;
                        } else {
                            Installer.log.fine("relocate both and resize help");
                            r1.x = 0;
                            r2.x = r1.width;
                            r2.width = s.width - r1.width;
                        }
                    }
                } else {
                    Installer.log.fine("send help to the left");
                    if((r1.x - r2.width) > 0) {
                        Installer.log.fine("there is enough place for help");
                        r2.x = r1.x - r2.width;
                    } else {
                        Installer.log.fine("there is not enough place");
                        if((r1.width + r2.width) < s.width){
                            Installer.log.fine("relocate both");
                            r2.x = 0;
                            r1.x = r2.width;
                        } else {
                            Installer.log.fine("relocate both and resize help");
                            r1.x = s.width - r1.width;
                            r2.x = 0;
                            r2.width = r1.x;
                        }
                    }
                }
            } else {
                //compare x axes of r1 and r2 to know how to relocate them
                int r1Xaxis = r1.y + (r1.height/2);
                int r2Xaxis = r2.y + (r2.height/2);
                if(r1Xaxis <= r2Xaxis) {
                    Installer.log.fine(" send help to the bottom");
                    if((r1.y + r1.height + r2.height) <= s.height) {
                        Installer.log.fine("there is enough place fo help");
                        r2.y = r1.y + r1.height;
                    } else {
                        Installer.log.fine("there is not enough place");
                        if((r1.height + r2.height) < s.height) {
                            Installer.log.fine("relocate both");
                            r2.y = s.height - r2.height;
                            r1.y = r2.y - r1.height;
                        } else {
                            Installer.log.fine("relocate both and resize help");
                            r1.y = 0;
                            r2.y = r1.height;
                            r2.height = s.height - r1.height;
                        }
                    }
                } else {
                    Installer.log.fine("send help to the top");
                    if((r1.y - r2.height) > 0){
                        Installer.log.fine("there is enough place for help");
                        r2.y = r1.y - r2.height;
                    } else {
                        Installer.log.fine("there is not enough place");
                        if((r1.height + r2.height) < s.height) {
                            Installer.log.fine("relocate both");
                            r2.y = 0;
                            r1.y = r2.height;
                        } else {
                            Installer.log.fine("relocate both and resize help");
                            r1.y = s.height - r1.height;
                            r2.y = 0;  //or with -1
                            r2.height = r1.y;
                        }
                    }
                }
            }
            dlg.setBounds(r1);
            dialogViewer.setBounds(r2);
        }
    }
    
    /** Make a dialog showing progress of parsing & merging help sets.
     * Show it; when the runnable is done, it will hide itself.
     * @param run something to do while it is showing
     * @param parent dialog (may be null)
     * @return a new progress dialog
     */
    private JDialog createProgressDialog(Runnable run, Dialog parent) {
        return (parent == null) ?
        new ProgressDialog(run, WindowManager.getDefault().getMainWindow()) :
            new ProgressDialog(run, parent);
    }
    
    private final class ProgressDialog extends JDialog implements TaskListener, Runnable {
        private Runnable run;
        public ProgressDialog(Runnable run, Dialog parent) {
            super(parent, NbBundle.getMessage(JavaHelp.class, "TITLE_loading_help_sets"), true);
            init(run);
        }
        public ProgressDialog(Runnable run, Frame parent) {
            super(parent, NbBundle.getMessage(JavaHelp.class, "TITLE_loading_help_sets"), true);
            init(run);
        }
        private void init(Runnable run) {
            this.run = run;
            JComponent c = ProgressHandleFactory.createProgressComponent(progressHandle);
            c.setPreferredSize(new Dimension(3 * c.getPreferredSize().width, 3 * c.getPreferredSize().height));
            c.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            getContentPane().add(c);
            progressHandle.start();
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHelp.class, "ACSD_Loading_Dialog"));  //NOI18N
            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension me = getSize();
            setLocation((screen.width - me.width) / 2, (screen.height - me.height) / 2);
        }
        public void setVisible(boolean show) {
            if (show && run != null) {
                Installer.log.fine("posting request from progress dialog...");
                getHelpLoader().post(run).addTaskListener(this);
                run = null;
            }
            super.setVisible(show);
        }
        public void taskFinished(Task task) {
            Installer.log.fine("posting request from progress dialog...request finished.");
            SwingUtilities.invokeLater(this);
        }
        public void run() {
            setVisible(false);
            dispose();
        }
    }
    
    private static RequestProcessor helpLoader = null;
    private static RequestProcessor getHelpLoader() {
        if (helpLoader == null) {
            helpLoader = new RequestProcessor("org.netbeans.modules.javahelp.JavaHelp"); // NOI18N
        }
        return helpLoader;
    }

    /** Find the proper help set for an ID.
    * @param id the ID to look up (may be null)
    * @return the proper help set (master if not otherwise found)
    */
    private HelpSet findHelpSetForID(String id) {
        if (id != null) {
            Iterator it = getHelpSets().iterator();
            while (it.hasNext()) {
                HelpSet hs = (HelpSet)it.next();
                if (hs.getCombinedMap().isValidID(id, hs))
                    return hs;
            }
            warnBadID(id);
        }
        return getMaster();
    }
    
    public Boolean isValidID(String id, boolean force) {
        if (force || helpSetsReady()) {
            Iterator it = getHelpSets().iterator();
            if (MASTER_ID.equals(id)) {
                if (it.hasNext()) {
                    Installer.log.fine("master id, and >=1 help set");
                    return Boolean.TRUE;
                } else {
                    Installer.log.fine("master id, and 0 help sets");
                    return Boolean.FALSE;
                }
            } else {
                // Regular ID.
                while (it.hasNext()) {
                    HelpSet hs = (HelpSet)it.next();
                    if (hs.getCombinedMap().isValidID(id, hs)) {
                        Installer.log.fine("found normal valid id " + id + " in " + hs.getTitle());
                        return Boolean.TRUE;
                    }
                }
                Installer.log.fine("did not find id " + id);
                return Boolean.FALSE;
            }
        } else {
            Installer.log.fine("not checking " + id + " specifically");
            return null;
        }
    }

    /** Warn that an ID was not found in any help set.
    * @param id the help ID
    */
    private static void warnBadID(String id) {
        // PLEASE DO NOT COMMENT OUT...localized warning
        Installer.log.fine(NbBundle.getMessage(JavaHelp.class, "MSG_jh_id_not_found", id));
    }

    /** Display something in a JHelp.
     *Handles {@link #MASTER_ID}, as well as help IDs
     *that were not found in any help set, various exceptions, etc.
     * @param jh the help component
     * @param helpID a help ID string to display, may be <CODE>null</CODE>
     * @param url a URL to display, may be <CODE>null</CODE>; lower priority than the help ID
     */
    private synchronized void displayInJHelp(JHelp jh, String helpID, URL url) {
        if (jh == null) throw new NullPointerException();
        if (jh.getModel() == null) throw new IllegalArgumentException();
        Installer.log.fine("displayInJHelp: " + helpID + " " + url);
        try {
            if (helpID != null && ! helpID.equals(MASTER_ID)) {
                HelpSet hs = jh.getModel().getHelpSet();
                if (hs.getCombinedMap().isValidID(helpID, hs)) {
                    jh.setCurrentID(helpID);
                } else {
                    warnBadID(helpID);
                }
            } else if (url != null) {
                jh.setCurrentURL(url);
            }
        } catch (RuntimeException e) {
            Installer.log.log(Level.WARNING, null, e);
        }
    }

    /** Create &amp; return a JHelp with the supplied help set.
     * In the case of the master help, will show the home page for
     * the distinguished help set if there is exactly one such,
     * or in the case of exactly one home page, will show that.
     * Caches the result and the result may be a reused JHelp.
     * @return the new JHelp
     * @param hs the help set to show
     */
    private JHelp createJHelp(HelpSet hs) {
        if (hs == null) throw new NullPointerException();
        JHelp jh;
        synchronized (availableJHelps) {
            Reference<JHelp> r = availableJHelps.get(hs);
            if (r != null) {
                jh = r.get();
                if (jh != null) {
                    return jh;
                }
            }
        }
        String title = null; // for debugging purposes
        try {
            title = hs.getTitle();
            jh = new JHelp(hs);
        } catch (RuntimeException e) {
            Installer.log.log(Level.WARNING, "While trying to display: " + title, e); // NOI18N
            return null;
        }
        synchronized (availableJHelps) {
            availableJHelps.put(hs, new SoftReference<JHelp>(jh));
        }
        try {
            javax.help.Map.ID home = hs.getHomeID();
            if (home != null) {
                jh.setCurrentID(home);
            }
        } catch (Exception e) {
            Installer.log.log(Level.WARNING, null, e);
        }
        return jh;
    }

    // XXX(ttran) see JDK bug 5092094 for details
    
    private static int modalExcludedSupported = -1;
    
    private static boolean isModalExcludedSupported() {
        if (modalExcludedSupported == -1) {
            modalExcludedSupported = 0;
            if (isJdk15) {
                try {
                    Class<?> clazz = Class.forName("sun.awt.SunToolkit"); // NOI18N
                    Method m = clazz.getMethod("isModalExcludedSupported"); // NOI18N
                    Boolean b = (Boolean) m.invoke(null);
                    modalExcludedSupported = b.booleanValue() ? 1 : 0;
                    Installer.log.fine("isModalExcludedSupported = " + modalExcludedSupported); // NOI18N
                } catch (ThreadDeath ex) {
                    throw ex;
                } catch (Throwable ex) {
                    Installer.log.info("isModalExcludedSupported() failed  " + ex); // NOI18N
                }
            } else {
                //Newer JDK version
                Class typeClass = null;
                try {
                    typeClass = Class.forName("java.awt.Dialog$ModalExclusionType");
                } catch (ClassNotFoundException ex) {
                    Installer.log.info("Cannot get class java.awt.Dialog$ModalExclusionType " + ex); // NOI18N
                    return false;
                }
                Method isSupportedMethod = null;
                try {
                    isSupportedMethod = Toolkit.class.getMethod("isModalExclusionTypeSupported", typeClass);
                } catch (NoSuchMethodException ex) {
                    Installer.log.info("Cannot get method isModalExcludedSupported " + ex); // NOI18N
                    return false;
                }
                Method methodValues = null;
                try {
                    methodValues = typeClass.getMethod("valueOf", String.class);
                } catch (NoSuchMethodException ex) {
                    Installer.log.info("Cannot get method valueOf " + ex); // NOI18N
                    return false;
                }
                Object modalityExclusionType = null;
                try {
                    modalityExclusionType = methodValues.invoke(null, "APPLICATION_EXCLUDE");
                } catch (IllegalAccessException ex) {
                    Installer.log.info("Cannot invoke method valueOf " + ex); // NOI18N
                    return false;
                } catch (InvocationTargetException ex) {
                    Installer.log.info("Cannot invoke method valueOf " + ex); // NOI18N
                    return false;
                }
                try {
                    Boolean b = (Boolean) isSupportedMethod.invoke(Toolkit.getDefaultToolkit(),modalityExclusionType);
                    modalExcludedSupported = b.booleanValue() ? 1 : 0;
                    Installer.log.fine("isModalExcludedSupported = " + modalExcludedSupported); // NOI18N
                } catch (IllegalAccessException ex) {
                    Installer.log.info("Cannot invoke method isModalExcludedSupported " + ex); // NOI18N
                    return false;
                } catch (InvocationTargetException ex) {
                    Installer.log.info("Cannot invoke method isModalExcludedSupported " + ex); // NOI18N
                    return false;
                }
            }
        }
        return modalExcludedSupported == 1;
    }
    
    private static void setModalExcluded(Window window) {
        if (modalExcludedSupported == 0) {
            return;
        }
        
        if (isJdk15) {
            try {
                Class<?> clazz = Class.forName("sun.awt.SunToolkit"); // NOI18N
                Method m = clazz.getMethod("setModalExcluded", Window.class); // NOI18N
                m.invoke(null, window);
            } catch (ThreadDeath ex) {
                throw ex;
            } catch (Throwable ex) {
                Installer.log.info("setModalExcluded(Window) failed  " + ex); // NOI18N
                modalExcludedSupported = 0;
            }
        } else {
            Class typeClass = null;
            try {
                typeClass = Class.forName("java.awt.Dialog$ModalExclusionType");
            } catch (ClassNotFoundException ex) {
                Installer.log.info("Cannot get class java.awt.Dialog$ModalExclusionType " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            }
            Method methodValues = null;
            try {
                methodValues = typeClass.getMethod("valueOf", String.class);
            } catch (NoSuchMethodException ex) {
                Installer.log.info("Cannot get method valueOf(String) " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            }
            Object modalityExclusionType = null;
            try {
                modalityExclusionType = methodValues.invoke(null, new Object [] {"APPLICATION_EXCLUDE"});
            } catch (IllegalAccessException ex) {
                Installer.log.info("Cannot invoke method valueOf(String) " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            } catch (InvocationTargetException ex) {
                Installer.log.info("Cannot invoke method valueOf(String) " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            }
            Method setMethod = null;
            try {
                setMethod = Window.class.getMethod("setModalExclusionType", typeClass); // NOI18N
            } catch (NoSuchMethodException ex) {
                Installer.log.info("Cannot get method setModalExclusionType " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            }
            try {
                setMethod.invoke(window,modalityExclusionType);
            } catch (IllegalAccessException ex) {
                Installer.log.info("Cannot invoke method setModalExclusionType " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            } catch (InvocationTargetException ex) {
                Installer.log.info("Cannot invoke method setModalExclusionType " + ex); // NOI18N
                modalExcludedSupported = 0;
                return;
            }
        }
    }
}
