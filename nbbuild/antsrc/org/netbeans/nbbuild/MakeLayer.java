/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

/** Create a fragment of a module's XML layer.
 *
 * @author  Michal Zlamal
 */
public class MakeLayer extends MatchingTask {

    private File dest = null;
    private List topdirs = new ArrayList ();
    private boolean absolutePath = false;

    /** Target file containing list of all classes. */
    public void setDestfile(File f) {
        dest = f;
    }

    /** Set the top directory.
     * There should be subdirectories under this matching pacgages.
     */
    public void setTopdir (File t) {
        topdirs.add (t);
    }
    
    /** Set wheather there is absolute path in top dir or not
     * default value is false
     */
    public void setAbsolutePath( boolean absolutePath ) {
        this.absolutePath = absolutePath;
    }

    /** Nested topdir addition. */
    public class Topdir {
        /** Path to an extra topdir. */
        public void setPath (File t) {
            topdirs.add (t);
        }
    }

    /** Add a nested topdir.
     * If there is more than one topdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later topdirs may override build
     * products in earlier topdirs.)
     */
    public Topdir createTopdir () {
        return new Topdir ();
    }
    
    public void execute()  throws BuildException {
        if (topdirs.isEmpty()) {
            throw new BuildException ("You must set at least one topdir attribute", location);
        }
        if (dest == null) {
            throw new BuildException("You must specify output file", location);
        }
        int lengthAdjust = (absolutePath) ? 0 : 1;
        FileWriter layerFile;
        try {
            layerFile = new FileWriter(dest);
        }
        catch (IOException e) {
            throw new BuildException(e.fillInStackTrace(),location);
        }
        for (int j = 0; j < topdirs.size (); j++) {
            File topdir = (File) topdirs.get (j);
        
            FileScanner scanner = getDirectoryScanner (topdir);
            String[] files = scanner.getIncludedFiles ();
            for (int i=0; i <files.length; i++) {
                File aFileName = new File(topdir, files[i]);
                try {
                    layerFile.write(("<file name=\""+aFileName.getName()+"\"\n").replace(File.separatorChar,'/'));
                    layerFile.write(("  url=\""+aFileName.getAbsolutePath().substring(topdir.getAbsolutePath().length()+lengthAdjust)+"\"/>\n").replace(File.separatorChar,'/'));
                }
                catch(IOException ex) {
                    throw new BuildException(ex.fillInStackTrace(),location);
                }
            }
        }
        
        try {
            layerFile.close();
        }
        catch (IOException e) {
            throw new BuildException(e.fillInStackTrace(),location);
        }
    }
}



