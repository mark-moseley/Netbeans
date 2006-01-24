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

package org.netbeans.modules.subversion.util;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Encapsulates context of an action. There are two ways in which context may be defined:
 * - list of files (f/b.txt, f/c.txt, f/e.txt)
 * - list of roots (top folders) plus list of exclusions (f),(a.txt, d.txt)
 * 
 * @author Maros Sandor
 */
public class Context implements Serializable {

    public static final Context Empty = new Context(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST); 
    
    private static final long serialVersionUID = 1L;
    
    private final List filteredFiles;
    private final List rootFiles;
    private final List exclusions;

    public Context(List filteredFiles, List rootFiles, List exclusions) {
        this.filteredFiles = filteredFiles;
        this.rootFiles = rootFiles;
        this.exclusions = exclusions;
        while (normalize());
    }

    private boolean normalize() {
        for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            for (Iterator j = exclusions.iterator(); j.hasNext();) {
                File exclusion = (File) j.next();
                if (SvnUtils.isParentOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }

    private void removeDuplicates(List files) {
        List newFiles = new ArrayList();
        outter: for (Iterator i = files.iterator(); i.hasNext();) {
            File file = (File) i.next();
            for (Iterator j = newFiles.iterator(); j.hasNext();) {
                File includedFile = (File) j.next();
                if (SvnUtils.isParentOrEqual(includedFile, file) && (file.isFile() || !(includedFile instanceof FlatFolder))) continue outter;
                if (SvnUtils.isParentOrEqual(file, includedFile) && (includedFile.isFile() || !(file instanceof FlatFolder))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
    
    private void exclusionRemoved(File exclusion, File root) {
        File [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            File child = exclusionChildren[i];
            if (!SvnUtils.isParentOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    public List getRoots() {
        return rootFiles;
    }

    public List getExclusions() {
        return exclusions;
    }

    /**
     * Gets exact set of files to operate on, it is effectively defined as (rootFiles - exclusions). This set
     * is NOT suitable for Update command because Update should operate on all rootFiles and just exclude some subfolders.
     * Otherwise update misses new files and folders directly in rootFiles folders. 
     *  
     * @return files to operate on
     */ 
    public File [] getFiles() {
        return (File[]) filteredFiles.toArray(new File[filteredFiles.size()]);
    }

    public File[] getRootFiles() {
        return (File[]) rootFiles.toArray(new File[rootFiles.size()]);
    }
    
    public boolean contains(File file) {
        outter : for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            if (SvnUtils.isParentOrEqual(root, file)) {
                for (Iterator j = exclusions.iterator(); j.hasNext();) {
                    File excluded = (File) j.next();
                    if (SvnUtils.isParentOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
