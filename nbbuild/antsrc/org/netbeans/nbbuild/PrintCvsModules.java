/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/** Pseudo-task to unpack a set of modules.
 * Resolves build-time dependencies of modules in selected moduleconfig
 * and print list of cvs modules which you need to checkout from cvs
 *
 * @author Rudolf Balada
 * based on dependency resolving code originally by Jesse Glick in NbMerge.java
 */
public class PrintCvsModules extends Task {
    
    private Vector modules = new Vector (); // list of modules defined by build.xml
    private Vector buildmodules = new Vector (); // list of modules which will be built
    private String targetprefix = "all-";    
    private String dummyName;
    private Hashtable targets;
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    /** String which will have a module name appended to it.
     * This will form a target in the same project which should
     * create the <samp>netbeans/</samp> subdirectory.
     */
    public void setTargetprefix (String s) {
        targetprefix = s;
    }
    
    public void execute () throws BuildException {

        buildmodules.addAll(modules);
        Target dummy = new Target ();
        dummyName = "nbmerge-" + target.getName ();
        targets = project.getTargets ();
        while (targets.contains (dummyName))
            dummyName += "-x";
        dummy.setName (dummyName);
        for (int i = 0; i < buildmodules.size (); i++) {
            String module = (String) buildmodules.elementAt (i);
            dummy.addDependency (targetprefix + module);
        }
        project.addTarget (dummy);

        Vector fullList = project.topoSort(dummyName, targets);
        log("fullList is " + fullList, Project.MSG_VERBOSE);
        // Now remove earlier ones: already done.
        Vector doneList = project.topoSort(getOwningTarget().getName(), targets);
        List todo = new ArrayList(fullList.subList(0, fullList.indexOf(dummy)));
        log("todo is " + todo.toString(), Project.MSG_VERBOSE);
        todo.removeAll(doneList.subList(0, doneList.indexOf(getOwningTarget())));
        log("todo is " + todo.toString(), Project.MSG_VERBOSE);
        Iterator targit = todo.iterator();
        Vector cvslist = new Vector();
        cvslist.add("nbbuild");
        while (targit.hasNext()) {
            Target nexttargit = (Target)targit.next();
            String tname = nexttargit.getName();
            log("tname is " + tname, Project.MSG_VERBOSE);
            if (tname.startsWith(targetprefix)) {
                String modname = tname.substring(targetprefix.length());
                log("modname is " + modname, Project.MSG_VERBOSE);
                log("modname.indexOf(\"/\") is " + modname.indexOf("/"), Project.MSG_VERBOSE);
                if (modname.indexOf("/") > 0) {
                    modname = modname.substring(0,modname.indexOf("/"));
                    log("modname is " + modname, Project.MSG_VERBOSE);
                }
                if ( ! cvslist.contains(modname) ) {
                    cvslist.add(modname);
                } else {
                    log("not adding cvs module name " + modname, Project.MSG_VERBOSE);
                }
                
            }
        
        }
        log("cvsmodules="+cvslist);    
    }        

}
