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

package org.netbeans.modules.debugger.jpda.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Ant task to reload classes in VM for running debugging session. 
 *
 * @author David Konecny
 */
public class JPDAReload extends Task {

    private List filesets = new ArrayList ();
 
    /**
     * FileSet with .class files to reload. The base dir of the fileset is expected
     * to be classpath root for these classes.
     */
    public void addFileset (FileSet fileset) {
        filesets.add (fileset);
    }
    
    public void execute() throws BuildException {
        if (filesets.size() == 0) {
            throw new BuildException ("A nested fileset with class to refresh in VM must be specified.");
        }
        
        // check debugger state
        DebuggerEngine debuggerEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (debuggerEngine == null) {
            throw new BuildException ("No debugging sessions was found.");
        }
        JPDADebugger debugger = (JPDADebugger) debuggerEngine.lookupFirst 
            (null, JPDADebugger.class);
        if (debugger == null) {
            throw new BuildException("Current debugger is not JPDA one.");
        }
        if (!debugger.canFixClasses ()) {
            throw new BuildException("The debugger does not support Fix action.");
        }
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
            throw new BuildException ("The debugger is not running");
        }
        
        System.out.println ("Classes to be reloaded:");
        
        FileUtils fileUtils = FileUtils.newFileUtils ();
        Map map = new HashMap ();
        
        Iterator it = filesets.iterator ();
        while (it.hasNext ()) {
            FileSet fs = (FileSet) it.next ();
            DirectoryScanner ds = fs.getDirectoryScanner (getProject ());
            String fileNames[] = ds.getIncludedFiles ();
            File baseDir = fs.getDir (getProject ());
            int i, k = fileNames.length;
            for (i = 0; i < k; i++) {
                File f = fileUtils.resolveFile (baseDir, fileNames [i]);
                if (f != null) {
                    FileObject fos[] = FileUtil.fromFile (f);
                    if (fos.length > 0) {
                        try {
                            InputStream is = fos [0].getInputStream ();
                            long fileSize = fos [0].getSize ();
                            byte[] bytecode = new byte [(int) fileSize];
                            is.read (bytecode);
                            // remove ".class" from and use dots for for separator
                            String className = fileNames [i].substring (
                                    0, 
                                    fileNames [i].length () - 6
                                ).replace (File.separatorChar, '.');
                            map.put (
                                className, 
                                bytecode
                            );
                            System.out.println (" " + className);
                        } catch (IOException ex) {
                            ex.printStackTrace ();
                        }
                    }
                }
            }
        }
        if (map.size () == 0) {
            System.out.println (" No class to reload");
            return;
        }
        debugger.fixClasses (map);
    }
}
