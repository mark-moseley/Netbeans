/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.output2;

import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.FindAction;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import org.openide.xml.XMLUtil;

/**
 * Master controller for an output window, and supplier of the default instance.
 * The controller handles all actions of interest in an output window - the components
 * are merely containers for data which pass events of interest up the component hierarchy
 * to the controller via OutputWindow.getController(), for processing by the master
 * controller.  The controller is fully stateless, and stores information of interest in
 * the components as appropriate.
 */
public class Controller { //XXX public only for debug access to logging code

    public static void ensureViewInDefault (final NbIO io, final boolean reuse) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                OutputWindow.findDefault();
                IOEvent evt = new IOEvent (io, IOEvent.CMD_CREATE, reuse);
                NbIO.post(evt);
            }
        });
    }

    private static final int ACTION_COPY = 0;
    private static final int ACTION_WRAP = 1;
    private static final int ACTION_SAVEAS = 2;
    private static final int ACTION_CLOSE = 3;
    private static final int ACTION_NEXTERROR = 4;
    private static final int ACTION_PREVERROR = 5;
    private static final int ACTION_SELECTALL = 6;
    private static final int ACTION_FIND = 7;
    private static final int ACTION_FINDNEXT = 8;
    private static final int ACTION_NAVTOLINE = 9;
    private static final int ACTION_POSTMENU = 10;
    private static final int ACTION_FINDPREVIOUS = 11;
    private static final int ACTION_CLEAR = 12;

    //Package private for unit tests
    Action copyAction = new ControllerAction (ACTION_COPY,
            "ACTION_COPY", CopyAction.class); //NOI18N
    Action wrapAction = new ControllerAction (ACTION_WRAP,
            "ACTION_WRAP"); //NOI18N
    Action saveAsAction = new ControllerAction (ACTION_SAVEAS,
            "ACTION_SAVEAS"); //NOI18N
    Action closeAction = new ControllerAction (ACTION_CLOSE,
            "ACTION_CLOSE"); //NOI18N
    Action nextErrorAction = new ControllerAction (ACTION_NEXTERROR,
            "ACTION_NEXT_ERROR", NextOutJumpAction.class); //NOI18N
    Action prevErrorAction = new ControllerAction (ACTION_PREVERROR,
            "ACTION_PREV_ERROR", PreviousOutJumpAction.class); //NOI18N
    Action selectAllAction = new ControllerAction (ACTION_SELECTALL,
            "ACTION_SELECT_ALL"); //NOI18N
    Action findAction = new ControllerAction (ACTION_FIND,
            "ACTION_FIND", FindAction.class); //NOI18N
    Action findNextAction = new ControllerAction (ACTION_FINDNEXT,
            "ACTION_FIND_NEXT"); //NOI18N
    Action findPreviousAction = new ControllerAction (ACTION_FINDPREVIOUS,
            "ACTION_FIND_PREVIOUS"); //NOI18N
    Action navToLineAction = new ControllerAction (ACTION_NAVTOLINE, "navToLine", //NOI18N
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    Action postMenuAction = new ControllerAction (ACTION_POSTMENU, "postMenu", //NOI18N
            KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK));
    Action clearAction = new ControllerAction (ACTION_CLEAR, "ACTION_CLEAR");

    private Object[] popupItems = new Object[] {
        copyAction, new JSeparator(), findAction, findNextAction,
        new JSeparator(),
        wrapAction, new JSeparator(), saveAsAction, clearAction, closeAction,
    };
    
    private Action[] kbdActions = new Action[] {
        copyAction, selectAllAction, findAction, findNextAction, 
        findPreviousAction, wrapAction, saveAsAction, closeAction,
        navToLineAction, postMenuAction, clearAction,
    };

    Controller() {}

    private OutputTab createOutputTab (OutputWindow win, NbIO io, boolean activateContainer, boolean reuse) {
        AbstractOutputTab[] ov = win.getTabs();
        OutputTab result = null;
        if (log) log ("Find or create component for nbio " + io);
        
        for (int i=0; i < ov.length; i++) {
            OutputTab oc = (OutputTab) ov[i];
            if (oc.getIO() == io) {
                if (log) log ("Found an existing tab");
                result = oc;
                break;
            }
        }
        if (result == null) {
            if (log) log ("Didn't find an existing open tab, checking hidden tabs");
            OutputTab[] hidden = win.getHiddenTabs();
            for (int i=0; i < hidden.length; i++) {
                OutputTab oc = hidden[i];
                if (hidden[i].getIO() == io) {
                    if (log) log ("Found a hidden tab with the same IO.  Unhiding it for reuse");
                    result = oc;
                    unhideHiddenView (win, result);
                    break;
                }
            }
        }
        
        if (log) log ("FindOrCreate: " + io.getName() + " found=" + (result != 
            null) + " for io " + io);
        
        if (result == null) {
            if (log) log ("Find or create creating " + io.getName());
            result = createAndInstallView (win, io);
        }
        if (result != null) {
            win.setSelectedTab(result);
        }
        if (!activateContainer) {
            win.requestVisibleForNewTab();
        } else {
            win.requestActiveForNewTab();
        }
        return result;
    }

    /**
     * Creates and installs an output view
     *
     * @param win The owning container
     * @param io The IO whose output is to be displayed
     * @return A new OutputTab attached to the passed IO
     */
    private OutputTab createAndInstallView (OutputWindow win, NbIO io) {
        if (log) log ("Create and install a new tab for : " + io.getName());
        OutputTab result = new OutputTab (io);
        result.setName (io.getName() + " ");
        Action[] a = io.getToolbarActions();
        if (a != null) {
            result.setToolbarActions(a);
        }
        for (int i=0; i < kbdActions.length; i++) {
            result.installKeyboardAction(kbdActions[i]);
        }
        
        if (log) log ("Adding and selecting new tab " + result);
        win.add (result);
        win.setSelectedTab(result);
        //Make sure names are boldfaced for all open streams - if the tabbed
        //pane was just added in, it will just have used the name of the 
        //component, which won't contain html
        AbstractOutputTab[] aot = win.getTabs();
        for (int i=0; i < aot.length; i++) {
            updateName(win, (OutputTab) aot[i]);
        }
        return result;
    }

    /**
     * Output views can be hidden by the user invoking close before the output stream for
     * the output has been closed.  In this case, they are stored in the OutputWindow,
     * and can be reopened if new output arrives.  This method will remove a component
     * from the set of hidden components and re-add it to the component hierarchy.
     *
     * @param win The owning container
     * @param hidden The output component which is hidden but was not closed when it was hidden
     */
    private void unhideHiddenView (OutputWindow win, OutputTab hidden) {
        if (log) log ("Unhiding hidden tab for " + hidden.getIO());
        win.add (hidden);
        win.removeHiddenView(hidden);
    }

    /**
     * Boldfaces the name of the output component if its NbIO's stream is open.
     * The update is delayed, and runs subsequently on the event queue - a process may
     * synchronously open and close tabs, all of which affects names, so we use this
     * technique and the CoalescedNameUpdater to coalesce all name changes - otherwise
     * the name change may be delayed.
     *
     * @param tab The component whose name may need adjusting
     */
    private void updateName (OutputWindow win, OutputTab tab) {
        if (nameUpdater == null) {
            nameUpdater = new CoalescedNameUpdater(win);
            SwingUtilities.invokeLater(nameUpdater);
        }
        nameUpdater.add (tab);
    }

    private CoalescedNameUpdater nameUpdater = null;
    /**
     * Calls to methods invoked on NbIO done on the EQ are invoked synchronously
     * (this avoids a delay in the output window appearing, so output starts
     * immediately).  However, we want to avoid multiple name changes being
     * propagated up to the window system because one tab was removed, another
     * was added, and so forth - the result is the title won't be updated until
     * the output run is nearly done, otherwise.  Also, the call to update the
     * TopComponent name is not terribly quick, so we don't want to do it any
     * more times than we need to.
     * <p>
     * This class coalesces name changes, which are run afterward on the event
     * queue.
     */
    private class CoalescedNameUpdater implements Runnable {
        private HashSet components = new HashSet();
        private OutputWindow win;
        CoalescedNameUpdater (OutputWindow win) {
            this.win = win;
        }

        /**
         * Add a tab whose name should be changed.
         * @param tab The tab
         */
        public void add (OutputTab tab) {
            components.add (tab);
        }

        public void run() {
            for (Iterator i=components.iterator(); i.hasNext();) {
                OutputTab t = (OutputTab) i.next();
                if (log) {
                    log ("Update name for " + t.getIO().getName() + " stream " +
                        "closed is " + t.getIO().isStreamClosed());
                }
                if (win.isAncestorOf(t)) {
                    String escaped;
                    try {
                        escaped = XMLUtil.toAttributeValue(t.getIO().getName());
                    } catch (CharConversionException e) {
                        escaped = t.getIO().getName();
                    }
                    
                    String name = t.getIO().isStreamClosed() ? escaped + " " :
                            "<html><b>" + escaped 
                            + " </b>&nbsp;</html>";  //NOI18N
                    win.setTabTitle (t, name);
                }
            }
            nameUpdater = null;
        }
    }

    /**
     * Called when a ControllerAction is invoked, either by the keyboard or
     * from the popup menu.
     *
     * @param win The output window where it was invoked
     * @param tab The tab it was invoked on
     * @param id The ID of the action
     */
    public void actionPerformed(OutputWindow win, OutputTab tab, int id) {
        switch (id) {
            case ACTION_COPY:
                tab.getOutputPane().copy();
                break;
            case ACTION_WRAP:
                boolean wrapped = tab.getOutputPane().isWrapped();
                tab.getOutputPane().setWrapped(!wrapped);
                break;
            case ACTION_SAVEAS:
                saveAs (tab);
                break;
            case ACTION_CLOSE:
                close (win, tab, false);
                break;
            case ACTION_NEXTERROR:
                sendCaretToError(win, tab, false);
                break;
            case ACTION_PREVERROR:
                sendCaretToError(win, tab, true);
                break;
            case ACTION_SELECTALL:
                tab.getOutputPane().selectAll();
                break;
            case ACTION_FIND:
                FindDialogPanel.showFindDialog(new FindActionListener (win, tab, findNextAction, findPreviousAction, copyAction));
                break;
            case ACTION_FINDNEXT:
                findNext (tab);
                break;
            case ACTION_FINDPREVIOUS :
                findPrevious (tab);
                break;
            case ACTION_NAVTOLINE :
                if (log) log ("Action NAVTOLINE received");
                openLineIfError (tab);
                break;
            case ACTION_POSTMENU :
                if (log) log ("Action POSTMENU received");
                postPopupMenu(win, tab, new Point(0,0), tab);
                break;
            case ACTION_CLEAR :
                if (log) log ("Action CLEAR receieved");
                NbIO io = tab.getIO();

                if (io != null) {
                    NbWriter writer = io.writer();
                    if (writer != null) {
                        try {
                            if (log) log ("Reseting the writer for Clear");
                            writer.reset();
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        }
                    } else if (log) {
                        log ("IO's NbWriter is null");
                    }
                } else if (log) {
                    log ("Clear on a tab with no IO");
                }
                break;
            default :
                assert false;
        }
    }

    /**
     * Called when a line is clicked - if an output listener is listening on that
     * line, it will be sent <code>outputLineAction</code>.
     * @param tab
     */
    private void openLineIfError(OutputTab tab) {
        OutWriter out = tab.getIO().out();
        if (out != null) {
            int line = tab.getOutputPane().getCaretLine();
            OutputListener lis = out.getLines().getListenerForLine(line);
            if (lis != null) {
                if (log) log (" Sending action for getLine " + line);
                ignoreCaretChanges = true;
                tab.getOutputPane().sendCaretToLine(line, true);
                ignoreCaretChanges = false;
                ControllerOutputEvent coe = new ControllerOutputEvent (tab.getIO(), line);
                lis.outputLineAction(coe);
            }
        }
    }

    /**
     * An action listener which listens to the default button of the find
     * dialog.
     */
    private static class FindActionListener implements ActionListener {
        OutputTab tab;
        Action findNextAction;
        Action findPreviousAction;
        Action copyAction;
        FindActionListener (OutputWindow win, OutputTab tab, Action findNextAction, Action findPreviousAction, Action copyAction) {
            this.tab = tab;
            this.findNextAction = findNextAction;
            this.findPreviousAction = findPreviousAction;
            this.copyAction = copyAction;
        }

        public void actionPerformed(ActionEvent e) {
            FindDialogPanel panel = (FindDialogPanel)
                SwingUtilities.getAncestorOfClass(FindDialogPanel.class,
                (JComponent) e.getSource());
            if (panel == null) {
                //dialog disposed
                panel = (FindDialogPanel) ((JComponent)
                    e.getSource()).getClientProperty("panel"); //NOI18N
            }

/*          //XXX the code below may actually be correct - pending discussion
            int pos = tab.getOutputPane().getCaretPos();
            if (pos >= tab.getOutputPane().getLength() || pos < 0) {
                pos = 0;
            }
            */
            int pos = 0;
            String s = panel.getPattern();
            if (s == null || s.length() == 0) {
                Toolkit.getDefaultToolkit().beep();
                if (log)
                    log("Find string is null");
                return;
            }
            OutWriter out = tab.getIO().out();
            if (out != null) {
                Matcher matcher = out.getLines().find(s);
                if (matcher != null && matcher.find(pos)) {
                    int start = matcher.start();
                    int end = matcher.end();
                    tab.getOutputPane().setSelection(start, end);
                    findNextAction.setEnabled(true);
                    findPreviousAction.setEnabled(true);
                    copyAction.setEnabled(true);
                    panel.getTopLevelAncestor().setVisible(false);
                    tab.requestFocus();
                }
            }
        }
    }

    /**
     * Find the next match for the previous search contents, starting at
     * the current caret position.
     *
     * @param tab The tab
     */
    private void findNext (OutputTab tab) {
        OutWriter out = tab.getIO().out();
        if (out != null) {
            Matcher matcher = out.getLines().getForwardMatcher();
            int pos = tab.getOutputPane().getCaretPos();
            if (pos >= tab.getOutputPane().getLength() || pos < 0) {
                pos = 0;
            }

            if (matcher != null && matcher.find (pos)) {
                tab.getOutputPane().setSelection(matcher.start(), matcher.end());
                copyAction.setEnabled(true);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * Find the match before the current caret position, using the previously
     * searched for value.
     *
     * @param tab The tab
     */
    private void findPrevious (OutputTab tab) {
        OutWriter out = tab.getIO().out();
        if (out != null) {
            Matcher matcher = out.getLines().getReverseMatcher();

            int length = tab.getOutputPane().getLength();
            int pos = length - tab.getOutputPane().getSelectionStart();

            if (pos >= tab.getOutputPane().getLength()-1 || pos < 0) {
                pos = 0;
            }
            if (log) log ("Reverse search from " + pos);
            if (matcher != null && matcher.find (pos)) {
                int start = length - matcher.end();
                int end = length - matcher.start();
                tab.getOutputPane().setSelection(start, end);
                copyAction.setEnabled(true);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * Update the enabled state of the actions based on the state of the passed
     * tab.  If the tab is not currently selected, does nothing.
     *
     * @param win The output window
     * @param tab The tab, presumably the selected one
     */
    private void updateActions (OutputWindow win, OutputTab tab) {
        if (tab == win.getSelectedTab()) {
            OutputPane pane = (OutputPane) tab.getOutputPane();
            int len = pane.getLength();
            boolean enable = len > 0;
            findAction.setEnabled (enable);
            OutWriter out = tab.getIO().out();
            findNextAction.setEnabled (out != null && out.getLines().getForwardMatcher() != null);
            findPreviousAction.setEnabled (out != null && out.getLines().getForwardMatcher() != null);
            saveAsAction.setEnabled (enable);
            selectAllAction.setEnabled(enable);
            copyAction.setEnabled(pane.hasSelection());
            boolean hasErrors = out == null ? false : out.getLines().firstListenerLine() != -1;
            nextErrorAction.setEnabled(hasErrors);
            prevErrorAction.setEnabled(hasErrors);
        }
    }

    /**
     * Close the tab.  If <code>programmatic</code> is false and it is the last
     * tab, the output window will be closed as well.
     *
     * @param win The owning output window
     * @param tab The tab
     * @param programmatic False if the user requested the tab to be closed
     */
    public void close(OutputWindow win, OutputTab tab, boolean programmatic) {
        //NotifyRemoved callback will take care of putting it into the hidden view list if
        //its output is still open.
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        boolean hadFocus = focusOwner != null && (focusOwner == win || win.isAncestorOf(focusOwner));

        win.remove(tab);  //Triggers a call to notifyRemoved()
        boolean winClosed = false;
        if (!programmatic && win.getTabs().length == 0) {
            if (log) log ("Last tab closed by user, closing output window.");
            win.close();
            winClosed = true;
        }
        if (hadFocus) {
            if (!winClosed && win.getSelectedTab() != null) {
                if (log) log ("Trying to send focus to the newly selected tab");
                win.getSelectedTab().requestFocus();
            }
        }
        if (log) log ("Close received, removing " + tab + " from component");
    }

    /**
     * Holds the last written to directory for the save as file chooser.
     */
    private static String lastDir = null;

    /**
     * Invokes a file dialog and if a file is chosen, saves the output to
     * that file.
     *
     * @param tab The tab
     */
    private void saveAs(OutputTab tab) {
        OutWriter out = tab.getIO().out();
        if (out == null) {
            return;
        }
        File f = showFileChooser (tab);
        if (f != null) {
            try {
                synchronized (out) {
                    out.getLines().saveAs(f.getPath());
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }

    /**
     * Shows a file dialog and an overwrite dialog if the file exists, returning
     * null if the user chooses not to overwrite.  Will use an AWT FileDialog for
     * Aqua, per Apple UI guidelines.
     *
     * @param owner A parent component for the dialog - the top level ancestor will
     *        actually be used so positioning is correct
     * @return A file to write to
     */
    private static File showFileChooser (JComponent owner) {
        File f = null;
        String dlgTtl = NbBundle.getMessage (Controller.class, "TITLE_SAVE_DLG"); //NOI18N

        boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

        if (isAqua) {
            //Apple UI guidelines recommend against ever using JFileChooser
            FileDialog fd = new FileDialog((Frame) owner.getTopLevelAncestor(), dlgTtl, FileDialog.SAVE);
            if (lastDir != null && new File (lastDir).exists()) {
                fd.setDirectory(lastDir);
            }
            fd.setModal(true);
            fd.setVisible(true);
            String s = fd.getDirectory() + fd.getFile();
            f = new File(s);
            if (f.exists() && f.isDirectory()) {
                f = null;
            }
        } else {
            JFileChooser jfc = new JFileChooser();
            if (lastDir != null && new File(lastDir).exists()) {
                File dir = new File (lastDir);
                if (dir.exists()) {
                    jfc.setCurrentDirectory(dir);
                }
            }
            jfc.setName(dlgTtl);
            jfc.setDialogTitle(dlgTtl);

            if (jfc.showSaveDialog(owner.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
            }
        }

        if (f != null && f.exists() && !isAqua) { //Aqua's file dialog takes care of this
            String msg = NbBundle.getMessage(Controller.class,
                "FMT_FILE_EXISTS", new Object[] { f.getName() }); //NOI18N
            String title = NbBundle.getMessage(Controller.class,
                "TITLE_FILE_EXISTS"); //NOI18N
            if (JOptionPane.showConfirmDialog(owner.getTopLevelAncestor(), msg, title,
            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                f = null;
            }
        }
        if (f != null) {
            lastDir = f.getParent();
        }
        return f;
    }

    /**
     * Called when the selected component is changed in an output container.
     *
     * @param win The owning container
     * @param former The previously selected output view, or null
     * @param current The newly selected output view, or null
     */
    public void selectionChanged(OutputWindow win, OutputTab former,
                                                     OutputTab current) {
        if (former != null) {
            former.updateTimestamp();
        }
        if (current != null) {
            current.updateTimestamp();
            updateActions (win, current);
        }
    }

    /**
     * Messaged when the container becomes activated in the netbeans window system
     * @param win The container
     */
    public void notifyActivated(OutputWindow win) {
        OutputTab tab = (OutputTab) win.getSelectedTab();
        if (tab != null) {
            updateActions (win, tab);
        }
    }

    /**
     * Messaged when the container is deactivated in the netbeans window system
     */
    public void notifyDeactivated() {
        for (int i=0; i < popupItems.length; i++) {
            if (popupItems[i] instanceof Action && popupItems[i] != nextErrorAction && popupItems[i] != prevErrorAction) {
                ((ControllerAction) popupItems[i]).detachPerformer();
            }
        }
    }

    /**
     * Sends the caret in a tab to the nearest error line to its current position, selecting
     * that line.
     *
     * @param win The output window
     * @param tab the tab
     * @param backward If the search should be done in reverse
     */
    private void sendCaretToError(OutputWindow win, OutputTab tab, boolean backward) {
        if (tab == null) {
            //We're being invoked from SystemAction via main menu - no associated component
            tab = (OutputTab) win.getSelectedTab();
            if (tab == null) {
                return;
            }
        }
        OutWriter out = tab.getIO().out();
        if (out != null) {
            int line = Math.max(0, tab.getOutputPane().getCaretLine());
            if (line >= tab.getOutputPane().getLineCount()-1) {
                line = 0;
            }
            int newline = out.getLines().nearestListenerLine(line, backward);
            if (newline == line) {
                if (!backward && line != tab.getOutputPane().getLineCount()) {
                    newline = out.getLines().nearestListenerLine(line+1, backward);
                } else if (backward && line > 0) {
                    newline = out.getLines().nearestListenerLine(line-1, backward);
                } else {
                    return;
                }
            }
            if (newline != -1) {
                if (log)
                    log("Sending caret to error line " + newline);
                tab.getOutputPane().sendCaretToLine(newline, true);
                if (!win.isActivated()) {
                    OutputListener l = out.getLines().getListenerForLine(newline);
                    
                    ControllerOutputEvent ce = new ControllerOutputEvent (tab.getIO(), newline);
                    l.outputLineAction(ce);
                }
            }
        }
    }

    /**
     * Called when an output tab has been removed from the component hierarchy.
     * If its io is not closed, holds a reference to it in a list of closed
     * tabs, re-showing it on request, or finally disposing its IO and
     * releasing it if it has not been shown again.
     */
    public void notifyRemoved(OutputTab tab) {
        assert SwingUtilities.isEventDispatchThread();
        if (log) log ("Tab " + tab + " has been CLOSED.  Disposing its IO.");
        NbIO io = tab.getIO();
        if (io != null) {
            io.setClosed(true);
        }
        NbWriter w = io.writer();
        if (w != null && w.isClosed()) {
            //Will dispose the document
            tab.setDocument(null);
        } else if (w != null) {
            //Something is still writing to the stream, but we're getting rid of the tab.  Don't dispose
            //the writer, just kill the tab's document
            tab.getDocument().disposeQuietly();
        }
    }

    /**
     * Called when input has been sent by the user via the input component
     *
     * @param win The output window
     * @param tab The tab component
     * @param txt The input entered
     */
    public void notifyInput(OutputWindow win, OutputTab tab, String txt) {
        if (Controller.log) Controller.log ("Notify input on " + tab + " - " + txt);
        NbIO io = tab.getIO();
        if (io != null) {
            NbIO.IOReader in = io.in();
            if (in != null) {
                if (Controller.log) Controller.log ("Sending input to " + in);

                in.pushText (txt + "\n");
            }
        }
    }

    /**
     * Fetch the output listener for a given line in a given tab
     *
     * @param tab The output tab
     * @param line The line to find a listener on
     * @return An output listener or null
     */
    private OutputListener listenerForLine (OutputTab tab, int line) {
        OutWriter out = tab.getIO().out();
        if (out != null) {
            return out.getLines().getListenerForLine(line);
        }
        return null;
    }

    /**
     * Called when the user has clicked a line in the text view
     * @param win The output window
     * @param tab The tab
     * @param line The line which was clicked
     */
    public void lineClicked(OutputWindow win, OutputTab tab, int line) {
        OutputListener l = listenerForLine (tab, line);
        if (l != null) {
            ControllerOutputEvent oe = new ControllerOutputEvent (tab.getIO(), line);
            l.outputLineAction(oe);
            //Select the text on click
            tab.getOutputPane().sendCaretToLine(line, true);
        }
    }

    /**
     * Post the output window's popup menu
     *
     * @param win The output window
     * @param tab The tab
     * @param p The point clicked
     * @param src The source of the click event
     */
    public void postPopupMenu(OutputWindow win, OutputTab tab, Point p, Component src) {
        if (log) {
            log ("post popup menu for " + tab.getName());
        }
        JPopupMenu popup = new JPopupMenu();
        popup.putClientProperty ("container", win); //NOI18N
        popup.putClientProperty ("component", tab); //NOI18N
        Action[] a = tab.getToolbarActions();
        if (a.length > 0) {
            boolean added = false;
            for (int i=0; i < a.length; i++) {
                if (a[i].getValue(Action.NAME) != null) {
                    popup.add (a[i]);
                    added = true;
                }
            }
            if (added) {
                popup.add (new JSeparator());
            }
        }
        for (int i=0; i < popupItems.length; i++) {
            if (popupItems[i] instanceof JSeparator) {
                popup.add ((JSeparator) popupItems[i]);
            } else {
                if (popupItems[i] != wrapAction) {
                    popup.add ((Action) popupItems[i]);
                } else {
                    JCheckBoxMenuItem item = 
                        new JCheckBoxMenuItem((Action) popupItems[i]);
                    
                    item.setSelected(tab.getOutputPane().isWrapped());
                    popup.add (item);
                }
            }
        }
        popup.addPopupMenuListener(new PMListener(popupItems));
        popup.show(src, p.x, p.y);
    }
    
    /**
     * #47166 - a disposed tab which has had its popup menu shown remains
     * referenced through PopupItems->JSeparator->PopupMenu->Invoker->OutputPane->OutputTab
     */
    private static class PMListener implements PopupMenuListener {
        private Object[] popupItems;
        PMListener (Object[] popupItems) {
            this.popupItems = popupItems;
        }
        
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            JPopupMenu popup = (JPopupMenu) e.getSource();
            popup.removeAll();
            popup.setInvoker(null);
            popup.putClientProperty ("container", null); //NOI18N
            popup.putClientProperty ("component", null); //NOI18N
            popup.removePopupMenuListener(this);
            for (int i=0; i < popupItems.length; i++) {
                if (popupItems[i] instanceof ControllerAction) {
                    ((ControllerAction) popupItems[i]).clearListeners();
                }
            }
        }
        
        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenuWillBecomeInvisible(e);
        }
        
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            //do nothing
        }
    }

    /**
     * Called when the text caret has changed lines - will call OutputListener.outputLineSelected if
     * there is a listener for that line.
     *
     * @param tab The output tab
     * @param line The line the caret is in
     */
    public void caretEnteredLine(OutputTab tab, int line) {
        if (!ignoreCaretChanges) {
            OutputListener l = listenerForLine (tab, line);
            if (log) {
                log ("Caret entered line " + line + " notifying listener " + l);
            }
            if (l != null) {
                ControllerOutputEvent oe = new ControllerOutputEvent (tab.getIO(), line);
                l.outputLineSelected(oe);
            }
        } else {
            if (log) {
                log ("Caret entered line " + line + " which has no listener");
            }
        }
    }

    /**
     * Called when an event has been received from the document (indicating that new
     * output has been appended.  Handles InputOutput.isFocusTaken(), and updates the
     * tab title in the case the stream has been closed, and updates the actions if
     * it is the selected tab.
     *
     * @param win
     * @param tab
     */
    public void documentChanged(OutputWindow win, OutputTab tab) {
        if (tab.getIO().isFocusTaken()) {
            //Not at all sure that isFocusTaken() is a terribly bright idea to begin with
            win.setSelectedTab(tab);
            win.requestVisible();
        }
        updateName(win, tab);
        if (tab == win.getSelectedTab() && win.isActivated()) {
            updateActions(win, tab);
        }
    }

    /**
     * Handles IOEvents posted into the AWT Event Queue by NbIO instances whose methods have
     * been called, as received by an OutputTab which has identified the event as being
     * intended for it.
     *
     * @param win The output container owning the IO, or in the case of CMD_CREATE, the
     *                        one that received the event
     * @param tab The output component associated with this IO, if any
     * @param io The IO which originated the event
     * @param command The ID, one of those defined in IOEvent, of the command
     * @param value The boolean value of the command, if pertinent
     * @param data The data associated with the command, if pertinent
     */
    public void performCommand(OutputWindow win, OutputTab tab, NbIO io, int command,
                               boolean value, Object data) {

        if (log) {
            log ("PERFORMING: " +  IOEvent.cmdToString(command) + " value=" + value + " on " + io + " tob " + tab);
        }

        OutWriter out = io.out();

        switch (command) {
            case IOEvent.CMD_CREATE :
                createOutputTab(win, io, io.isFocusTaken(), value);
                break;
            case IOEvent.CMD_INPUT_VISIBLE :
                if (value && tab == null) {
                    tab = createOutputTab(win, io, io.isFocusTaken(), value);
                }
                if (tab != null) {
                    tab.setInputVisible(value);
                    win.setSelectedTab(tab);
                }
                break;
            case IOEvent.CMD_SELECT :

                if (tab == null) {
                    tab = createOutputTab(win, io, io.isFocusTaken(), value);
                }
                if (win.getSelectedTab() != tab) {
                    if (tab.getParent() == null) {
                        //It was hidden
                        win.add(tab);
                    }
                    win.setSelectedTab(tab);
                    win.requestVisible();
                }
                break;
            case IOEvent.CMD_SET_TOOLBAR_ACTIONS :
                if (tab == null && data != null) {
                    tab = createOutputTab(win, io, io.isFocusTaken(), value);
                }
                Action[] a = (Action[]) data;
                tab.setToolbarActions(a);
                break;
            case IOEvent.CMD_CLOSE :
                if (tab != null) {
                    close(win, tab, true);
                } else {
                    io.dispose();
                }
                break;
            case IOEvent.CMD_STREAM_CLOSED :
                if (value) {
                    if (tab == null) {
                        //The tab was already closed, throw away the storage.
                        if (io.out() != null) {
                            io.out().dispose();
                        }
                    } else if (tab != null) {
                        if (tab.getParent() != null) {
                            updateName(win, tab);
                            if (tab.getIO().out() != null && tab.getIO().out().getLines().firstListenerLine() == -1) {
                                tab.getOutputPane().ensureCaretPosition();
                            }
                            if (tab == win.getSelectedTab()) {
                                updateActions (win, tab);
                            }
                        } else {
                            //The tab had been kept around to be re-shown, but now the stream is closed, dispose it
                            win.removeHiddenView(tab);
                            if (io.out() != null) {
                                io.out().dispose();
                            }
                        }
                    }
                } else {
                    if (tab != null && tab.getParent() != null) {
                        updateName(win, tab);
                    }
                }
                break;
            case IOEvent.CMD_RESET :
                if (tab == null) {
                    if (log) log ("Got a reset on an io with no tab.  Creating a tab.");
                    performCommand (win, tab, io, IOEvent.CMD_CREATE, value, data);
                    win.requestVisible();
                    return;
                }
                if (log) log ("Setting io " + io + " on tab " + tab);
//                tab.setDocument (new OutputDocument((OutWriter)io.getOut()));
                tab.setIO(io);
                
                win.setSelectedTab(tab);
                updateName(win, tab);
                win.requestVisibleForNewTab();
                if (log) log ("Reset on " + tab + " tab displayable " + tab.isDisplayable() + " io " + io + " io.out " + io.out());
                break;
        }
    }

    /**
     * Called when the output stream has been closed, to navigate to the
     * first line which shows an error (if any).
     *
     * @param comp The output component whose IO's stream has been closed.
     */
    private void navigateToFirstErrorLine (OutputTab comp) {
        OutWriter out = comp.getIO().out();
        if (out != null) {
            int line = comp.getFirstNavigableListenerLine();
            if (Controller.log) Controller.log ("NAV TO FIRST LISTENER LINE: " + line);
            if (line > 0) {
                comp.getOutputPane().sendCaretToLine (line, false);
            }
        }
    }
    
    /**
     * Flag used to block navigating the editor to the first error line when
     * selecting the error line in the output window after a build (or maybe
     * it should navigate the editor there?  Could be somewhat rude...)
     */
    boolean ignoreCaretChanges = false;

    public void hasSelectionChanged(OutputWindow outputWindow, OutputTab tab, boolean val) {
        if (tab == outputWindow.getSelectedTab()) {
            copyAction.setEnabled(val);
            selectAllAction.setEnabled(!tab.getOutputPane().isAllSelected());
        }
    }

    public void hasOutputListenersChanged(OutputWindow win, OutputTab tab, boolean hasOutputListeners) {
        if (hasOutputListeners && win.getSelectedTab() == tab && tab.isShowing()) {
            navigateToFirstErrorLine(tab);
        }
    }
    
    /**
     * A stateless action which will find the owning OutputTab's controller and call
     * actionPerformed with its ID as an argument.
     */
    private static class ControllerAction extends AbstractAction implements ActionPerformer {
        private int id;
        private Class callbackActionClass = null;
        /**
         * Create a ControllerAction with the specified action ID (constants defined in Controller),
         * using the specified bundle key.  Expects the following contents in the bundle:
         * <ul>
         * <li>A name for the action matching the passed key</li>
         * <li>An accelerator for the action matching [key].accel</li>
         * </ul>
         * @param id An action ID
         * @param bundleKey A key for the bundle associated with the Controller class
         * @see org.openide.util.Utilities#stringToKey
         */
        ControllerAction (int id, String bundleKey) {
            if (bundleKey != null) {
                String name = NbBundle.getMessage(Controller.class, bundleKey);
                KeyStroke accelerator = getAcceleratorFor(bundleKey);
                this.id = id;
                putValue (NAME, name);
                putValue (ACCELERATOR_KEY, accelerator);
            }
        }

        /**
         * Create a ControllerAction with the specified ID, name and keystroke.  Actions created
         * using this constructor will not be added to the popup menu of the component.
         *
         * @param id The ID
         * @param name A programmatic name for the item
         * @param stroke An accelerator keystroke
         */
        ControllerAction (int id, String name, KeyStroke stroke) {
            this.id = id;
            putValue (NAME, name);
            putValue (ACCELERATOR_KEY, stroke);
        }
        
        void clearListeners() {
            PropertyChangeListener[] l = changeSupport.getPropertyChangeListeners();
            for (int i=0; i < l.length; i++) {
                removePropertyChangeListener (l[i]);
            }
        }

        /**
         * Get a keyboard accelerator from the resource bundle, with special handling
         * for the mac keyboard layout.
         *
         * @param name The bundle key prefix
         * @return A keystroke
         */
        private static KeyStroke getAcceleratorFor (String name) {
            String key = name + ".accel"; //NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                key += ".mac"; //NOI18N
            }
            return Utilities.stringToKey(NbBundle.getMessage(Controller.class, key));
        }

        /**
         * Create a controller which will also act as the action performer for a
         * CallbackSystemAction.
         *
         * @param id The action ID
         * @param name The bundle key for the action
         * @param callbackActionClass
         */
        ControllerAction (int id, String name, Class callbackActionClass) {
            this (id, name);
            this.callbackActionClass = callbackActionClass;
        }

        public int getID() {
            return id;
        }

        public void actionPerformed(ActionEvent e) {
            if (log) log ("ACTION PERFORMED: " + getValue(NAME));
            Component c = (Component) e.getSource();

            OutputTab outComp = c instanceof OutputTab ? (OutputTab) c :
                c instanceof OutputWindow ? null :
                (OutputTab) SwingUtilities.getAncestorOfClass(OutputTab.class, c);

            OutputWindow win= c instanceof OutputWindow ? (OutputWindow) c :
                (OutputWindow) SwingUtilities.getAncestorOfClass(OutputWindow.class, outComp);

            if (win == null) {
                win = OutputWindow.findDefault();
            }
            if (outComp == null && win != null) {
                outComp = (OutputTab) win.getSelectedTab();
            }

            if (win == null && outComp == null) {
                //For popup menus, we store the component they were invoked over in
                //client properties
                JPopupMenu jpm = (JPopupMenu) SwingUtilities.getAncestorOfClass (JPopupMenu.class, c);
                if (jpm != null) {
                    win = (OutputWindow) jpm.getClientProperty ("win"); //NOI18N
                    outComp = (OutputTab) jpm.getClientProperty ("component"); //NOI18N
                }
            }
            Controller cont = win.getController();
            if (cont != null) {
                cont.actionPerformed (win, outComp, getID());
            }
        }

        /**
         * Set the enabled state of the action.  Updates the action performer for its
         * action class as needed.
         *
         * @param val The state
         */
        public void setEnabled (boolean val) {
            super.setEnabled(val);
            if (callbackActionClass != null) {
                updateActionPerformer();
            }
        }

        /**
         * Update the state of the CallbackAction which is proxying this action.
         */
        private void updateActionPerformer() {
            CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(callbackActionClass);
            boolean val = isEnabled();
            if (!val && action.getActionPerformer() == this) {
                if (log)
                    log ("Clearing action performer for " + getValue(NAME));
                action.setActionPerformer(null);
            } else {
                if (log) log ("Setting action performer for " + getValue(NAME));
                action.setActionPerformer(this);
            }
        }

        /**
         * Generates an action event over the default output window's selected tab and
         * calls <code>actionPerformed</code> with it.
         * @param action
         */
        public void performAction(SystemAction action) {
            OutputWindow ow = OutputWindow.findDefault();
            ActionEvent ae = new ActionEvent(ow, ActionEvent.ACTION_PERFORMED,
                    "");
            actionPerformed(ae);
        }

        /**
         * Resign this action from being the performer for its associated CallbackSystemAction.
         */
        public void detachPerformer() {
            if (callbackActionClass != null) {
                CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(callbackActionClass);
                if (action.getActionPerformer() == this) {
                    if (log) log ("Detaching action performer for " + getValue(NAME));
                    action.setActionPerformer(null);
                }
            }
        }
    }

    /**
     * An OutputEvent implementation with a settable line index so it can be
     * reused.
     */
    static class ControllerOutputEvent extends OutputEvent {
        private int line;
        ControllerOutputEvent (NbIO io, int line) {
            super (io);
            this.line = line;
        }

        void setLine (int line) {
            this.line = line;
        }

        public String getLine() {
            NbIO io = (NbIO) getSource();
            OutWriter out = io.out();
            try {
                if (out != null) {
                    String s = out.getLines().getLine(line);
                    //#46892 - newlines should not be appended to returned strings
                    if (s.endsWith("\n")) { //NOI18N
                        s = s.substring(0, s.length()-1);
                    }
                    return s;
                }
            } catch (IOException ioe) {
                IOException nue = new IOException ("Could not fetch line " + line + " on " + io.getName()); //NOI18N
                ErrorManager.getDefault().annotate (nue, ioe);
                ErrorManager.getDefault().notify(ioe);
            }
            return null;
        }
    }

    public static boolean log = Boolean.getBoolean("nb.output.log") || Boolean.getBoolean("nb.output.log.verbose"); //NOI18N
    public static boolean verbose = Boolean.getBoolean("nb.output.log.verbose");
    static boolean logStdOut = Boolean.getBoolean("nb.output.log.stdout"); //NOI18N
    public static void log (String s) {
        s = Long.toString(System.currentTimeMillis()) + ":" + s + "(" + Thread.currentThread() + ")  ";
        if (logStdOut) {
            System.out.println(s);
            return;
        }
        OutputStream os = getLogStream();
        byte b[] = new byte[s.length() + 1];
        char[] c = s.toCharArray();
        for (int i=0; i < c.length; i++) {
            b[i] = (byte) c[i];
        }
        b[b.length-1] = (byte) '\n';
        try {
            os.write(b);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(s);
        }
        try {
            os.flush();
        } catch (Exception e ) {}
    }
    
    public static void logStack() {
        if (logStdOut) {
            new Exception().printStackTrace();
            return;
        }
        Exception e = new Exception();
        e.fillInStackTrace();
        StackTraceElement[] ste = e.getStackTrace();

        for (int i=1; i < Math.min (22, ste.length); i++) {
            log ("   *   " + ste[i]);
        }
    }

    private static OutputStream logStream = null;
    private static OutputStream getLogStream() {
        if (logStream == null) {
            String spec = System.getProperty ("java.io.tmpdir") + File.separator + "outlog.txt";
            synchronized (Controller.class) {
                try {
                    File f = new File (spec);
                    if (f.exists()) {
                        f.delete();
                    }
                    f.createNewFile();
                    logStream = new FileOutputStream(f);
                } catch (Exception e) {
                    e.printStackTrace();
                    logStream = System.err;
                }
            }
        }
        return logStream;
    }

    public void inputEof(OutputTab tab) {
        if (Controller.log) Controller.log ("Input EOF");
        NbIO io = tab.getIO();
        NbIO.IOReader in = io.in();
        if (in != null) {
            in.eof();
        }
        io.setInputVisible(false);
    }
}

