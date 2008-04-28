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

package org.netbeans.performance.test.guitracker;

import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RepaintManager;

/** A repaint manager which will logs information about interesting events.
 *
 * @author  Tim Boudreau, rkubacki@netbeans.org, mmirilovic@netbeans.org
 */
public class LoggingRepaintManager extends RepaintManager {
    
    private static final long MAX_TIMEOUT = 60*1000L;
    
    private ActionTracker tr;
    
    private RepaintManager orig = null;
    
    private long lastPaint = 0L;
    
    private boolean hasDirtyMatches = false;
    
    private LinkedList<RegionFilter> regionFilters;
    
    /** Creates a new instance of LoggingRepaintManager
     * @param tr
     */
    public LoggingRepaintManager(ActionTracker tr) {
        this.tr = tr;
        regionFilters = new LinkedList<RegionFilter>();
        // lastPaint = System.nanoTime();
    }
    
    /**
     * Enable / disable our Repaint Manager
     * @param val true - enable, false - disable
     */
    public void setEnabled(boolean val) {
        if (isEnabled() != val) {
            if (val) {
                enable();
            } else {
                disable();
            }
        }
    }
    
    /**
     * Get an answer on question "Is Repaint Manager enabled?"
     * @return true - repaint manager is enabled, false - it's disabled
     */
    public boolean isEnabled() {
        return orig != null;
    }
    
    /**
     * Enable Repaint Manager
     */
    private void enable() {
        orig = currentManager(new JLabel()); //could be null for standard impl
        setCurrentManager(this);
    }
    
    /**
     * Disable Repaint Manager
     */
    private void disable() {
        setCurrentManager(orig);
        orig = null;
    }
    
    /**
     * Log the action when region is add to dirty regions.
     *
     * @param c component which is add to this region
     * @param x point where the region starts
     * @param y point where the region starts
     * @param w width of the region
     * @param h hieght of the region
     */
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        synchronized (this) {
            String log = c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h;

            // fix for issue 73361, It looks like the biggest cursor is on Sol 10 (10,19) in textfields
            // of some dialogs
            if (w > 10 || h > 19) { // painted region isn't cursor (or painted region is greater than cursor)
                if (regionFilters != null && !acceptedByRegionFilters(c)) {
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "IGNORED DirtyRegion: " + log);
                } else { // no filter || accepted by filter =>  measure it
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "ADD DirtyRegion: " + log);
                    hasDirtyMatches = true;
                }
            }
        }
        //System.out.println(log);
        super.addDirtyRegion(c, x, y, w, h);
    }
    
    /**
     * Check all region filters
     * @param c component to be checked
     * @return true - it's accepted, false it isn't accepted
     */
    public synchronized boolean acceptedByRegionFilters(JComponent c){
        for (RegionFilter filter : regionFilters) {
            if(!filter.accept(c)) // if not accepted it has to be IGNORED
                return false;
        }
        return true;
    }
    
    /**
     * Set region filter
     * @param filter
     */
    public void  addRegionFilter(RegionFilter filter) {
        if(filter != null){
            tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "ADD FILTER: " + filter.getFilterName());
            regionFilters.add(filter);
        }
    }
    /**
     * Remove region filter
     * @param filter
     */
    public void removeRegionFilter(RegionFilter filter) {
        if(filter != null){
            tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "REMOVE FILTER: " + filter.getFilterName());
            regionFilters.remove(filter);
        }
    }
    /**
     * Reset region filters
     */
    public void  resetRegionFilters() {
        tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "FILTER: reset");
        regionFilters.clear();
    }
    
    /**
     * Region filter - define paints those will be accepted
     */
    public interface RegionFilter {
        /**
         * Accept paints from component
         * @param c component
         * @return true - paint is accepted, false it isn't
         */
        public boolean accept(JComponent c);
        
        /**
         * Get filter name
         * @return name of the filter
         */
        public String getFilterName();
    }
    
    /**
     * Accept paints from Windows Vista :
     *  - component is not default button (JButton)
     * This button is repainted periodically on Window Vista with Aero L&F,
     * so we need to ignore these paints
     */
    public static final RegionFilter VISTA_FILTER =
            new RegionFilter() {
        
        public boolean accept(JComponent c) {
            return !(c instanceof JButton && ((JButton)c).isDefaultButton());
        }
        
        public String getFilterName() {
            return "Don't accept paints from Default JButton";
        }
    };
    
    /**
     * Accept paints only from Explorer :
     *  - org.openide.explorer.view
     */
    public static final RegionFilter EXPLORER_FILTER =
            new RegionFilter() {
        
        public boolean accept(JComponent c) {
            Class clz = null;
            
            for (clz = c.getClass(); clz != null; clz = clz.getSuperclass()) {
                if (clz.getPackage().getName().equals("org.openide.explorer.view")) {
                    return true;
                }
            }
            return false;
        }
        
        public String getFilterName() {
            return "Accept paints from package: org.openide.explorer.view";
        }
    };
    
    
    /**
     * Accept paints only from Editor :
     *  - org.openide.text.QuietEditorPane
     */
    public static final RegionFilter EDITOR_FILTER =
            new RegionFilter() {
        
        public boolean accept(JComponent c) {
            return c.getClass().getName().equals("org.openide.text.QuietEditorPane");
        }
        
        public String getFilterName() {
            return "Accept paints from org.openide.text.QuietEditorPane";
        }
    };
    
    /**
     * Log the action when dirty regions are painted.
     */
    public void paintDirtyRegions() {
        super.paintDirtyRegions();
        //System.out.println("Done superpaint ("+tr+","+hasDirtyMatches+").");
        if (tr != null && hasDirtyMatches) {
            lastPaint = System.nanoTime();
            tr.add(tr.TRACK_PAINT, "PAINTING - done");
            //System.out.println("Done painting - " +tr);
            hasDirtyMatches = false;
        }
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing
     *
     * @param timeout
     * @return time of last painting
     */
    public long waitNoPaintEvent(long timeout) {
        return waitNoPaintEvent(timeout, false);
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing.
     *
     * @param afterPaint when set to true then this method checks if there was any paint
     *        and measures quiet period from this time
     *
     * @return time of last painting
     */
    private long waitNoPaintEvent(long timeout, boolean afterPaint) {
        long current = System.nanoTime();
        long first = current;
        while ((ActionTracker.nanoToMili(current - lastPaint) < timeout) || ((lastPaint == 0L) && afterPaint)) {
            try {
                Thread.currentThread().sleep(Math.min(ActionTracker.nanoToMili(current - lastPaint) + 20, timeout));
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            current = System.nanoTime();
            if (ActionTracker.nanoToMili(current - first) > MAX_TIMEOUT)
                return ActionTracker.nanoToMili(lastPaint);
        }
        return ActionTracker.nanoToMili(lastPaint);
    }
    
    /** Utility method used from NetBeans to measure startup time.
     * Initializes RepaintManager and associated ActionTracker and than
     * waits until paint happens and there is 5 seconds of inactivity.
     *
     * @return time of last paint
     */
    public static long measureStartup() {
        // load our EQ and repaint manager
        ActionTracker tr = ActionTracker.getInstance();
        LoggingRepaintManager rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        
        tr.startNewEventList("Startup time measurement");
        
        long waitAfterStartup = Long.getLong("org.netbeans.performance.waitafterstartup", 10000).longValue();
        long time = rm.waitNoPaintEvent(waitAfterStartup, true);
        
        String fileName = System.getProperty( "org.netbeans.log.startup.logfile" );
        java.io.File logFile = new java.io.File(fileName.substring(0,fileName.lastIndexOf('.')) + ".xml");
        
        tr.stopRecording();
        try {
            tr.exportAsXML(new java.io.PrintStream(logFile));
        }catch(Exception exc){
            System.err.println("Exception rises during writing log from painting of the main window :");
            exc.printStackTrace(System.err);
        }
        
        rm.setEnabled(false);
        return time;
    }
    
}
