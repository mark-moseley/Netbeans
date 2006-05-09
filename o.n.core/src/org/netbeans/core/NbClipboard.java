/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.ExClipboard;

public final class NbClipboard extends ExClipboard
implements LookupListener, Runnable, FlavorListener, AWTEventListener
{
    private Logger log;
    private Clipboard systemClipboard;
    private ExClipboard.Convertor[] convertors;
    private Lookup.Result<ExClipboard.Convertor> result;
    final boolean slowSystemClipboard;
    private Transferable last;
    private long lastWindowActivated;
    private long lastWindowDeactivated;
    private Object lastWindowDeactivatedSource;

    public NbClipboard() {
        super("NBClipboard");   // NOI18N
        systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        log = Logger.getLogger("org.netbeans.core.NbClipboard"); // NOI18N

        result = Lookup.getDefault().lookupResult(ExClipboard.Convertor.class);
        result.addLookupListener(this);

        systemClipboard.addFlavorListener(this);

        resultChanged(null);

        if (System.getProperty("netbeans.slow.system.clipboard.hack") != null) // NOI18N
            slowSystemClipboard = Boolean.getBoolean("netbeans.slow.system.clipboard.hack"); // NOI18N
        else
            slowSystemClipboard = true;




        if (!slowSystemClipboard) {
            if (System.getProperty("sun.awt.datatransfer.timeout") == null) { // NOI18N
                System.setProperty("sun.awt.datatransfer.timeout", "1000"); // NOI18N
            }
        } else {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                this, AWTEvent.WINDOW_EVENT_MASK);
        }
    }
    
    protected synchronized ExClipboard.Convertor[] getConvertors () {
        return convertors;
    }

    public synchronized void resultChanged(LookupEvent ev) {
        Collection<? extends ExClipboard.Convertor> c = result.allInstances();
        ExClipboard.Convertor[] temp = new ExClipboard.Convertor[c.size()];
        convertors = c.toArray(temp);
    }

    // XXX(-ttran) on Unix (and also on Windows as we discovered recently)
    // calling getContents() on the system clipboard is very expensive, the
    // call can take up to 1000 milliseconds.  We need to examine the clipboard
    // contents each time the Node is activated, the method must be fast.
    // Therefore we cache the contents of system clipboard and use the cache
    // when someone calls getContents().  The cache is sync'ed with the system
    // clipboard when _any_ of our Windows gets WINDOW_ACTIVATED event.  It
    // means if some other apps modify the contents of the system clipboard in
    // the background then the change won't be propagated to us immediately.
    // The other drawback is that if module code bypasses NBClipboard and
    // accesses the system clipboard directly then we don't see these changes.
    //
    // The other problem is an AWT bug
    // 
    // http://developer.java.sun.com/developer/bugParade/bugs/4818143.html
    //
    // sun.awt.datatransfer.ClipboardTransferable.getClipboardData() can hang
    // for very long time (maxlong == eternity).  We tries to avoid the hang by
    // access the system clipboard from a separate thread.  If the hang happens
    // the thread will wait for the system clipboard forever but not the whole
    // IDE

    private RequestProcessor.Task syncTask =
        new RequestProcessor("System clipboard synchronizer").create(this, true); // NOI18N

    private Transferable data;
    private ClipboardOwner dataOwner;
    
    public void setContents(Transferable contents, ClipboardOwner owner) {
        synchronized (this) {
            // XXX(-dstrupl) the following line might lead to a double converted
            // transferable. Can be fixed as Jesse describes in #32485
            if (log.isLoggable (Level.FINER)) {
                log.log (Level.FINER, "setContents called with: "); // NOI18N
                logFlavors (contents, Level.FINER);
            }
            contents = convert(contents);
            if (log.isLoggable (Level.FINER)) {
                log.log (Level.FINER, "After conversion:"); // NOI18N
                logFlavors (contents, Level.FINER);
            }

            if (slowSystemClipboard) {
                super.setContents(contents, owner);
            } else {
                if (last != null) transferableOwnershipLost(last);
                last = contents;
            }

            data = contents;
            dataOwner = owner;
            syncTask.schedule(0);
        }
        fireClipboardChange();
    }

    public Transferable getContents(Object requestor) {
        Transferable prev;

        try {
            if (slowSystemClipboard) {
                // The purpose of lastWindowActivated+100 is to ignore calls
                // which immediatelly follow WINDOW_ACTIVATED event.
                // This is workaround of JDK bug described in issue 41098.
                if (lastWindowActivated != 0 && lastWindowActivated+100 < System.currentTimeMillis()) {
                    lastWindowActivated = 0;
                    
                    syncTask.schedule(0);
                    syncTask.waitFinished (100);
                }
                
                
                prev = super.getContents (requestor);
            } else {
                syncTask.waitFinished ();
                prev = systemClipboard.getContents (requestor);
            }

            synchronized (this) {
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "getContents by " + requestor); // NOI18N
                    logFlavors (prev, Level.FINE);
                }
                if (prev == null)  // if system clipboard has no contents
                    return null;

                Transferable res = convert (prev);
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "getContents by " + requestor); // NOI18N
                    logFlavors (res, Level.FINE);

                    res = new LoggableTransferable (res);
                }
                return res;
            }
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        } catch (Throwable ex) {
            org.openide.ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }

    public void run() {
        Transferable contents = null;
        ClipboardOwner owner = null;

        synchronized (this) {
            if (data != null) {
             contents = data;
             owner = dataOwner;
            }
            data = null;
            dataOwner = null;
        }
        if (contents != null) {
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "systemClipboard updated:"); // NOI18N
                logFlavors (contents, Level.FINE);
            }
            systemClipboard.setContents(contents, owner);
            return;
        }

        try {
            Transferable transferable = systemClipboard.getContents(this);
            super.setContents(transferable, null);
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "internal clipboard updated:"); // NOI18N
                logFlavors (transferable, Level.FINE);
            }
            fireClipboardChange();
        }
        catch (ThreadDeath ex) {
            throw ex;
        }
        catch (Throwable ignore) {
        }
    }

    /** For testing purposes.
     */
    final void waitFinished () {
        syncTask.waitFinished ();
    }
    
    final void activateWindowHack (boolean reschedule) {
        // if WINDOW_DEACTIVATED is followed immediatelly with
        // WINDOW_ACTIVATED then it is JDK bug described in 
        // issue 41098.
        lastWindowActivated = System.currentTimeMillis();
        if (reschedule) {
            syncTask.schedule (0);
        }
    }
    
    private void logFlavors (Transferable trans, Level level) {
        if (trans == null)
            log.log (level, "  no clipboard contents");
        else {
            java.awt.datatransfer.DataFlavor[] arr = trans.getTransferDataFlavors();
            for (int i = 0; i < arr.length; i++) {
                log.log (level, "  " + i + " = " + arr[i]);
            }
        }
    }

    public void flavorsChanged(FlavorEvent e) {
        fireClipboardChange();
    }

    public void eventDispatched(AWTEvent ev) {
        if (!(ev instanceof WindowEvent))
            return;

        if (ev.getID() == WindowEvent.WINDOW_DEACTIVATED) {
            lastWindowDeactivated = System.currentTimeMillis();
            lastWindowDeactivatedSource = ev.getSource();
        }
        if (ev.getID() == WindowEvent.WINDOW_ACTIVATED) {
            if (System.currentTimeMillis() - lastWindowDeactivated < 100 &&
                ev.getSource() == lastWindowDeactivatedSource) {
                activateWindowHack (false);
            }
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "window activated scheduling update"); // NOI18N
            }
            syncTask.schedule(0);
        }
    }
    
    /** Transferable that logs operations on itself.
     */
    private final class LoggableTransferable implements Transferable {
        private Transferable delegate;
        
        public LoggableTransferable (Transferable delegate) {
            this.delegate = delegate;
        }
        public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException {
            log.log (Level.FINE, "Request for flavor: " + flavor); // NOI18N
            Object res = delegate.getTransferData (flavor);
            log.log (Level.FINE, "Returning value: " + res); // NOI18N
            return res;
        }
        
        public DataFlavor[] getTransferDataFlavors () {
            return delegate.getTransferDataFlavors ();
        }
        
        public boolean isDataFlavorSupported (DataFlavor flavor) {
            boolean res = delegate.isDataFlavorSupported (flavor);
            log.log (Level.FINE, "isDataFlavorSupported: " + flavor + " result: " + res); // NOI18N
            return res;
        }
        
    }
}
