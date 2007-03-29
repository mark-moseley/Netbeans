/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;


/**
 * Finds the locations of sources for various libraries.
 * @since 1.5
 */
public class J2eePlatformSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private final Map/*<URL,SourceForBinaryQuery.Result>*/ cache = new HashMap();
    private final Map/*<URL,URL>*/ normalizedURLCache = new HashMap();

    /** Default constructor for lookup. */
    public J2eePlatformSourceForBinaryQuery() {}

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        SourceForBinaryQuery.Result res = (SourceForBinaryQuery.Result) this.cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        boolean isNormalizedURL = isNormalizedURL(binaryRoot);
        
        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
        ServerRegistry servReg = ServerRegistry.getInstance();
        for (int i=0; i < serverInstanceIDs.length; i++) {
            ServerInstance serInst = servReg.getServerInstance(serverInstanceIDs[i]);
            J2eePlatformImpl platformImpl = serInst.getJ2eePlatformImpl();
            if (platformImpl == null) continue; // TODO this will be removed, when AppServPlg will be ready
            LibraryImplementation[] libs = platformImpl.getLibraries();
            for (int j=0; j< libs.length; j++) {
                String type = libs[j].getType ();
                List classes = libs[j].getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);    //NOI18N
                for (Iterator it = classes.iterator(); it.hasNext();) {
                    URL entry = (URL) it.next();
                    URL normalizedEntry;
                    if (isNormalizedURL) {
                        normalizedEntry = getNormalizedURL(entry);
                    }
                    else {
                        normalizedEntry = entry;
                    }
                    if (normalizedEntry != null && normalizedEntry.equals(binaryRoot)) {
                        res =  new Result(entry, libs[j]);
                        cache.put (binaryRoot, res);
                        return res;
                    }
                }
            }
        }
        return null;
    }
    
    
    private URL getNormalizedURL (URL url) {
        //URL is already nornalized, return it
        if (isNormalizedURL(url)) {
            return url;
        }
        //Todo: Should listen on the LibrariesManager and cleanup cache        
        // in this case the search can use the cache onle and can be faster 
        // from O(n) to O(ln(n))
        URL normalizedURL = (URL) this.normalizedURLCache.get (url);
        if (normalizedURL == null) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                try {
                    normalizedURL = fo.getURL();
                    this.normalizedURLCache.put (url, normalizedURL);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return normalizedURL;
    }
    
    /**
     * Returns true if the given URL is file based, it is already
     * resolved either into file URL or jar URL with file path.
     * @param URL url
     * @return true if  the URL is normal
     */
    private static boolean isNormalizedURL (URL url) {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            url = FileUtil.getArchiveFile(url);
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }
    
    
    private static class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {
        
        private LibraryImplementation lib;
        private URL entry;
        private ArrayList listeners;
        private FileObject[] cache;
        
        public Result (URL queryFor, LibraryImplementation aLib) {
            this.entry = queryFor;
            this.lib = aLib;
            this.lib.addPropertyChangeListener ((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,this,this.lib));
        }
        
        public synchronized FileObject[] getRoots () {
            if (cache == null) {
                if (this.lib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH).contains (entry)) {
                    List src = this.lib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC);
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
            assert l != null : "Listener cannot be null"; // NOI18N
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove (l);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (LibraryImplementation.PROP_CONTENT.equals(event.getPropertyName())) {
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
