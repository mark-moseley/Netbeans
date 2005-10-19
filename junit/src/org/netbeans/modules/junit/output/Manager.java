/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.Mutex;

/**
 * This class gets informed about started and finished JUnit test sessions
 * and manages that the result windows and reports in them are appropriately
 * displayed, closed etc.
 *
 * @author Marian Petras
 */
final class Manager {
    
    /** singleton */
    private static Manager instance;
    
    /** list of sessions without windows displayed */
    //private List/*<AntSession>*/ pendingSessions;
    /** list of sessions with windows displayed */
    //private List/*<AntSession>*/ displayedSessions;
    /** */
    //private List/*<Report>*/ displayedReports;
    /**
     * dummy object for use in the weak set of JUnit sessions
     *
     * @see  #junitSessions
     */
    private final Object dummy = new Object();
    /**
     * this map serves as a weak set of running JUnit sessions.
     * All its keys have assigned the same value - the {@link #dummy} object.
     */
    private final Map junitSessions = new WeakHashMap(5);

    /**
     */
    static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }
    
    /**
     */
    synchronized void taskStarted(final AntSession session) {
        /*
        if ((pendingSessions != null) && pendingSessions.contains(session)) {
            return;
        }
        if ((displayedSessions != null)
                && displayedSessions.contains(session)) {
            return;
        }
         */
        
        if (junitSessions.put(session, Boolean.FALSE) == null) {
            sessionStarted(session);
        }
    }
    
    /**
     */
    private void sessionStarted(final AntSession session) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                ResultWindow.getInstance().displayTestRunning(true);
            }
        });
        
        /*
         * This method is called only from method taskStarted(AntSession)
         * which is synchronized.
         */
        
        /*
        if (pendingSessions == null) {
            pendingSessions = new ArrayList(4);
        }
        pendingSessions.add(session);
         */
        
        /* Close all windows with reports displayed: */
        /*
        assert (displayedSessions == null) == (displayedReports == null);
        if (displayedSessions != null) {
            assert displayedReports.size() == displayedSessions.size();
            
            ListIterator iDispRep
                    = displayedReports.listIterator(displayedReports.size());
            ListIterator iDispSes
                    = displayedSessions.listIterator(displayedSessions.size());
            final List indexes
                    = new ArrayList(displayedReports.size());
            while (iDispRep.hasPrevious()) {
                int index = iDispSes.previousIndex();
                Object r = iDispRep.previous();
                Object s = iDispSes.previous();
                if (r == null) {
                    indexes.add(new Integer(index));
                    iDispRep.remove();
                    iDispSes.remove();
                }
            }

            assert displayedSessions.size() == displayedReports.size();

            if (displayedSessions.isEmpty()) {
                displayedSessions = null;
                displayedReports = null;
            }
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    ResultWindow win = ResultWindow.getInstance();
                    for (Iterator i = indexes.iterator(); i.hasNext(); ) {
                        win.removeView(((Integer) i.next()).intValue());
                    }
                }
            });
        }
         */
    }
    
    /** */
    private ComponentListener listener;
    
    /**
     */
    void reportStarted(final AntSession session) {
        
        if (junitSessions.put(session, Boolean.TRUE) == Boolean.TRUE) {
            return;
        }
        
        /**
         * This class detects when the JUnit Results window is hidden
         * by the text output window and makes it appear again.
         */
        class DishonourAvenger implements ComponentListener {
            private boolean activated = false;
            
            public void componentMoved(ComponentEvent e) {}
            public void componentResized(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {
                if (!activated) {
                    activated = true;
                    
                    final ResultWindow window = ResultWindow.getInstance();
                    window.removeComponentListener(this);
                    window.requestVisible();
                }
            }
        }
        
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                ResultWindow.getInstance().addComponentListener(
                        listener = new DishonourAvenger());
            }
        });
    }
    
    /**
     */
    synchronized void sessionFinished(final AntSession session,
                                      final Report report) {
        if (junitSessions.remove(session) == null) {
            /* This session did not run the "junit" task. */
            return;
        }
        
        if (listener != null) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    ResultWindow.getInstance().removeComponentListener(listener);
                }
            });
        }
        
        /*
        assert ((pendingSessions != null
                        && pendingSessions.contains(session)))
               ^ ((displayedSessions != null)
                        && (displayedSessions.contains(session)));
        
        int indexDisp = -1;
        if (displayedSessions != null) {
            indexDisp = displayedSessions.indexOf(session);
        }
        int indexPend = -1;
        if (indexDisp == -1) {
            indexPend = pendingSessions.indexOf(session);
        }
         */
        
        /*
         * Display windows for pending sessions started earlier,
         * display the report in a new window ...
         */
        /*
        final int index = (indexDisp != -1) 
                          ? indexDisp
                          : (displayedSessions == null)
                            ? indexPend
                            : displayedSessions.size() + indexPend;
         */
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                final ResultWindow win = ResultWindow.getInstance();
                /*
                final int emptyViewsToOpen
                        = Math.max(0, index - win.getViewsCount());
                for (int i = 0; i < emptyViewsToOpen; i++) {
                    win.openEmptyView();
                }
                 */
                //win.displayReport(index, report);
                win.displayReport(0, report, false);
            }
        });
        /* ... and update information about displayed and pending sessions: */
        /*
        if (indexDisp != -1) {
            displayedReports.set(indexDisp, report);
        } else {     //(indexPend != -1)
            if (displayedSessions == null) {
                displayedSessions = new ArrayList(4);
            }
            if (indexPend == pendingSessions.size() - 1) {
                displayedSessions.addAll(pendingSessions);
                pendingSessions = null;
            } else {
                displayedSessions.addAll(
                        pendingSessions.subList(0,
                                                indexPend + 1));
                pendingSessions = new ArrayList(
                        pendingSessions.subList(indexPend + 1,
                                                pendingSessions.size()));
            }
            
            if (displayedReports == null) {
                displayedReports = new ArrayList(4);
            }
            for (int i = 0; i < indexPend; i++) {
                displayedReports.add(null);
            }
            displayedReports.add(report);
        }
         */
    }
    
}
