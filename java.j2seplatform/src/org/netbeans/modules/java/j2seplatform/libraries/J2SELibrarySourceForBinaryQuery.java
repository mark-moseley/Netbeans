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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.ErrorManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;

/**
 * Finds the locations of sources for various libraries.
 * @author Tomas Zezula
 */
public class J2SELibrarySourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    
    /** Creates a new instance of J2SELibrarySourceForBinaryQuery */
    public J2SELibrarySourceForBinaryQuery() {
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        LibraryManager lm = LibraryManager.getDefault ();
        // XXX this is very inefficient - linear search over all libraries!
        Library[] libs = lm.getLibraries();
        for (int i=0; i< libs.length; i++) {
            String type = libs[i].getType ();
            if (J2SELibraryTypeProvider.LIBRARY_TYPE.equalsIgnoreCase(type)) {
                // XXX could cache various portions of this calculation - profile it...
                List classes = libs[i].getContent("classpath");    //NOI18N
                for (Iterator it = classes.iterator(); it.hasNext();) {
                    URL entry = (URL) it.next();
                    FileObject file = URLMapper.findFileObject (entry);
                    if (file != null) {
                        try {
                            if (file.getURL().equals(binaryRoot)) {
                                return new Result (libs[i]);
                            }
                        } catch (FileStateInvalidException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    
    private static class Result implements SourceForBinaryQuery.Result {
        
        private Library lib;
        
        public Result (Library lib) {
            this.lib = lib;
        }
        
        public FileObject[] getRoots () {
            //TODO: Cache result when listening will be implemented
            List src = this.lib.getContent("src");              //NOI18N
            List result = new ArrayList ();
            for (Iterator sit = src.iterator(); sit.hasNext();) {
                FileObject sourceRootURL = URLMapper.findFileObject((URL) sit.next());
                if (sourceRootURL!=null) {
                    result.add (sourceRootURL);
                }
            }
            return (FileObject[]) result.toArray(new FileObject[result.size()]);
        }
        
        public void addChangeListener (ChangeListener l) {
            //TODO: Implement this
        }
        
        public void removeChangeListener (ChangeListener l) {
            //TODO: Implement this
        }
        
    }
    
}
