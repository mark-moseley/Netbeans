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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.ErrorManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.util.WeakListeners;

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
                                return new Result (entry, libs[i]);
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
    
    
    private static class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {
        
        private Library lib;
        private URL entry;
        private ArrayList listeners;
        private FileObject[] cache;
        
        public Result (URL queryFor, Library lib) {
            this.entry = queryFor;
            this.lib = lib;
            this.lib.addPropertyChangeListener ((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,this,this.lib));
        }
        
        public synchronized FileObject[] getRoots () {
            if (this.cache == null) {
                if (this.lib.getContent("classpath").contains (entry)) {
                    List src = this.lib.getContent("src");              //NOI18N
                    List result = new ArrayList ();
                    for (Iterator sit = src.iterator(); sit.hasNext();) {
                        FileObject sourceRootURL = URLMapper.findFileObject((URL) sit.next());
                        if (sourceRootURL!=null) {
                            result.add (sourceRootURL);
                        }
                    }
                    this.cache = (FileObject[]) result.toArray(new FileObject[result.size()]);
                }
                else {
                    this.cache = new FileObject[0];
                }
            }
            return this.cache;
        }
        
        public synchronized void addChangeListener (ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove (l);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (Library.PROP_CONTENT.equals(event)) {
                synchronized (this) {                    
                    this.cache = null;
                }
                this.fireChange ();
            }
        }
        
        private void fireChange () {
            Iterator it = null;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                it = ((ArrayList)this.listeners.clone()).iterator();
            }
            ChangeEvent event = new ChangeEvent (this);
            while (it.hasNext ()) {
                ((ChangeListener)it.next()).stateChanged(event);
            }
        }
        
    }
    
}
