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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.gsfret.source.usages;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openide.util.Exceptions;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Tomas Zezula
 */
public final class ClassIndexManager {
    
    private static final byte OP_ADD    = 1;
    private static final byte OP_REMOVE = 2;

    private static ClassIndexManager instance;
    private final Map<URL, ClassIndexImpl> instances = new HashMap<URL, ClassIndexImpl> ();
    private final ReadWriteLock lock;
//    private final List<ClassIndexManagerListener> listeners = new CopyOnWriteArrayList<ClassIndexManagerListener> ();
    private boolean invalid;
    private Set<URL> added;
    private Set<URL> removed;
    private int depth = 0;
    private Thread owner;
    
    
    
    private ClassIndexManager() {
        this.lock = new ReentrantReadWriteLock (false);
    }
    
//    public void addClassIndexManagerListener (final ClassIndexManagerListener listener) {
//        assert listener != null;
//        this.listeners.add(listener);
//    }
//    
//    public void removeClassIndexManagerListener (final ClassIndexManagerListener listener) {
//        assert listener != null;
//        this.listeners.remove(listener);
//    }
    
    public <T> T writeLock (final ExceptionAction<T> r) throws IOException/*, InterruptedException*/ {
        this.lock.writeLock().lock();
        try {
            if (depth == 0) {
                this.owner = Thread.currentThread();
            }
            try {
                depth++;
                try {
                    if (depth == 1) {
                        this.added = new HashSet<URL>();
                        this.removed = new HashSet<URL>();
                    }
                    try {
                        return r.run();
                    } finally {
                        if (depth == 1) {
                            if (!removed.isEmpty()) {
//                                fire (removed, OP_REMOVE);
                                removed.clear();
                            }
                            if (!added.isEmpty()) {
//                                fire (added, OP_ADD);
                                added.clear();
                            }                
                        }
                    }
                } finally {
                    depth--;
                }            
            } finally {
                if (depth == 0) {
                    this.owner = null;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    public <T> T readLock (final ExceptionAction<T> r) throws IOException/*, InterruptedException*/ {
        this.lock.readLock().lock();
        try {
            return r.run();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    public boolean holdsWriteLock () {
        return Thread.currentThread().equals(this.owner);
    }
    
    public synchronized ClassIndexImpl getUsagesQuery (final URL root) throws IOException {
        assert root != null;
        if (invalid) {
            return null;
        }        
        // BEGIN TOR MODIFICATIONS
        // XXX Figure out why I often get this on boot class paths
        //return this.instances.get (root);
        ClassIndexImpl ci = this.instances.get (root);
//        if (ci == null) {
//            ci = createUsagesQuery(root, false);
//        }
        
        return ci;
        // END TOR MODIFICATIONS
    }
    
    public synchronized ClassIndexImpl createUsagesQuery (final URL root, final boolean source) throws IOException {
        assert root != null;
        if (invalid) {
            return null;
        }        
        ClassIndexImpl qi = this.instances.get (root);
        if (qi == null) {  
            qi = PersistentClassIndex.create (root, Index.getDataFolder(root), source);
            this.instances.put(root,qi);
            if (added != null) {
                added.add (root);
            }
        }
//        else if (source && !qi.isSource()){
//            //Wrongly set up freeform project, which is common for it, prefer source
//            qi.close ();
//            qi = PersistentClassIndex.create (root, Index.getDataFolder(root), source);
//            this.instances.put(root,qi);
//            if (added != null) {
//                added.add (root);
//            }
//        }
        return qi;
    }
    
    synchronized void removeRoot (final URL root) throws IOException {
        // BEGIN TOR MODIFICATIONS
        if (bootRoots.contains(root)) {
            return;
        }
        // END TOR MODIFICATIONS
        ClassIndexImpl ci = this.instances.remove(root);
        if (ci != null) {
            ci.close();
            if (removed != null) {
                removed.add (root);
            }
        }
    }
    
    public synchronized  void close () {
        invalid = true;
        for (ClassIndexImpl ci : instances.values()) {
            try {
                ci.close();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    public static interface ExceptionAction<T> {
        public T run () throws IOException/*, InterruptedException*/;
    }
    
//    private void fire (final Set<? extends URL> roots, final byte op) {
//        if (!this.listeners.isEmpty()) {
//            final ClassIndexManagerEvent event = new ClassIndexManagerEvent (this, roots);
//            for (ClassIndexManagerListener listener : this.listeners) {
//                if (op == OP_ADD) {
//                    listener.classIndexAdded(event);
//                }
//                else if (op == OP_REMOVE) {
//                    listener.classIndexRemoved(event);
//                }
//                else {
//                    assert false : "Unknown op: " + op;     //NOI18N
//                }
//            }
//        }
//    }
    
    
    public static synchronized ClassIndexManager getDefault () {
        if (instance == null) {
            instance = new ClassIndexManager ();            
        }
        return instance;
    }        

    // BEGIN TOR MODIFICATIONS
    // This is for development use only (the index browser)
    public synchronized Map<URL, ClassIndexImpl> getAllIndices() {
        if (invalid) {
            return Collections.emptyMap();
        }        
        return Collections.unmodifiableMap(this.instances);
    }
    
    // To avoid excessive compilation at startup etc.
    private final Set<URL> bootRoots = new HashSet<URL>();
    private Set<ClassIndexImpl> bootIndices;
    
    public boolean isBootRoot(URL root) {
        return bootRoots.contains(root);
    }
    
    public void setBootRoots(List<URL> urls) {
        if (bootRoots.size() > 0) {
            bootRoots.clear();
        }
        for (URL url : urls) {
            bootRoots.add(url);
        }
        
        bootIndices = null;
    }
    
    public Set<URL> getBootRoots() {
        return bootRoots;
    }
    
    /** 
     * I need to store the boot indices such that I don't try to open a
     * LuceneIndex multiple times (once for each project type that tries
     * to query the boot index) since as of Lucene 2.1 I get errors in trying
     * to do this...
     */
    public Set<ClassIndexImpl> getBootIndices() {
        if (bootIndices == null) {
            bootIndices = new HashSet<ClassIndexImpl>();
            Set<URL> bootRoots = getBootRoots();
            for (URL u : bootRoots) {
                try {
                    ClassIndexImpl ci = getUsagesQuery(u);
                    if (ci != null) {
                        bootIndices.add(ci);
                    } else {
                        ci = createUsagesQuery(u, false);
                        bootIndices.add(ci);
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        return bootIndices;
    }
    // END TOR MODIFICATIONS
}
