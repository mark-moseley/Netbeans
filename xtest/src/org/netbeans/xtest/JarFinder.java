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

/*
 * JarFinder.java
 *
 * Created on November 26, 2001, 3:46 PM
 */

package org.netbeans.xtest;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.io.File;

/**
 * @author lm97939
 */
public class JarFinder extends Task {

    private String dirlist,filelist,property;
    
    public void setDirs(String d) {
        dirlist = d;
    }
    
    public void setFiles(String f) {
        filelist=f;
    }
    
    public void setProperty(String p) {
        property = p;
    }

    public void execute() throws BuildException {
        if (dirlist == null) throw new BuildException("Attribute dirs is empty.");
        if (filelist == null) throw new BuildException("Attribute files is empty.");
        
        StringBuffer buffer = new StringBuffer();
        StringTokenizer filetokens = new StringTokenizer(filelist,",;"+File.pathSeparator);
        boolean found = false;
        while (filetokens.hasMoreTokens()) {
            found = false;
            String file = filetokens.nextToken();
            File ffile = new File(file);
            if (ffile.exists()) {
                found = true;
                if (buffer.length() > 0) buffer.append(File.pathSeparator);
                buffer.append(ffile.getAbsolutePath());
                continue;
            }
            StringTokenizer dirtokens = new StringTokenizer(dirlist,",;"+File.pathSeparator);
            while (dirtokens.hasMoreTokens()) {
                String dir = dirtokens.nextToken();
                File fdir = new File(dir);
                if (!fdir.exists() || !fdir.isDirectory()) 
                    throw new BuildException("Directory " + fdir.getAbsolutePath() + "not found.");
                ffile = new File(fdir,file);
                if (ffile.exists()) {
                    found = true;
                    if (buffer.length() > 0) buffer.append(File.pathSeparator);
                    buffer.append(ffile.getAbsolutePath());
                    break;
                }
            }
            if (found == false) 
                throw new BuildException("File "+file+" was not found in any directory of "+dirlist+".");
        }
        log ("Setting property " + property + " to " + buffer.toString());
        project.setProperty(property,buffer.toString());
    }

}
