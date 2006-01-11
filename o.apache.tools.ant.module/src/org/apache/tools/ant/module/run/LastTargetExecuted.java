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

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Records the last Ant target(s) that was executed.
 * @author Jesse Glick
 */
public class LastTargetExecuted {
    
    private LastTargetExecuted() {}
    
    private static File buildScript;
    private static int verbosity;
    private static String[] targets;
    private static Properties properties;
    
    /** Called from {@link TargetExecutor}. */
    static void record(File buildScript, int verbosity, String[] targets, Properties properties) {
        LastTargetExecuted.buildScript = buildScript;
        LastTargetExecuted.verbosity = verbosity;
        LastTargetExecuted.targets = targets;
        LastTargetExecuted.properties = properties;
        fireChange();
    }
    
    /**
     * Get the last build script to be run.
     * @return the last-run build script, or null if nothing has been run yet (or the build script disappeared etc.)
     */
    public static AntProjectCookie getLastBuildScript() {
        if (buildScript != null && buildScript.isFile()) {
            FileObject fo = FileUtil.toFileObject(buildScript);
            assert fo != null;
            try {
                return (AntProjectCookie) DataObject.find(fo).getCookie(AntProjectCookie.class);
            } catch (DataObjectNotFoundException e) {
                assert false : e;
            }
        }
        return null;
    }
    
    /**
     * Get the last target names to be run.
     * @return a list of one or more targets, or null for the default target
     */
    public static String[] getLastTargets() {
        return targets;
    }
    
    /**
     * Get a display name (as it would appear in the Output Window) for the last process.
     * @return a process display name, or null if nothing has been run yet
     */
    public static String getProcessDisplayName() {
        AntProjectCookie apc = getLastBuildScript();
        if (apc != null) {
            return TargetExecutor.getProcessDisplayName(apc, targets != null ? Arrays.asList(targets) : null);
        } else {
            return null;
        }
    }
    
    /**
     * Try to rerun the last task.
     */
    public static ExecutorTask rerun() throws IOException {
        AntProjectCookie apc = getLastBuildScript();
        if (apc == null) {
            throw new IOException("No last process"); // NOI18N
        }
        TargetExecutor t = new TargetExecutor(apc, targets);
        t.setVerbosity(verbosity);
        t.setProperties(properties);
        return t.execute();
    }
    
    private static final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    public static void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public static void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private static void fireChange() {
        ChangeEvent ev = new ChangeEvent(LastTargetExecuted.class);
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new ArrayList(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
}
