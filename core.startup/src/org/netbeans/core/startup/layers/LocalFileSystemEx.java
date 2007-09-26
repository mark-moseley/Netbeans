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

package org.netbeans.core.startup.layers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.*;

/** Extends LocalFileSystem by useful features. It is used as
 * delegates being part of SystemFileSystem.
 *
 * @author  Vita Stejskal
 */
public final class LocalFileSystemEx extends LocalFileSystem {

    /** name -> FileObject */
    private static HashMap<String,FileObject> allLocks = new HashMap<String,FileObject> (7);
    private static HashSet<String> pLocks = new HashSet<String> (7);
//    private static HashMap allThreads = new HashMap (7);

    public static String [] getLocks () {
        synchronized (allLocks) {
            removeInvalid (pLocks);
            LinkedList<String> l = new LinkedList<String> ();
            l.addAll (allLocks.keySet ());
            l.addAll (pLocks);
            return l.toArray (new String [l.size ()]);
        }
    }

    public static boolean hasLocks () {
        synchronized (allLocks) {
            removeInvalid (pLocks);
            return !allLocks.isEmpty () || !pLocks.isEmpty ();
        }
    }
    
    public static void potentialLock (String name) {
        synchronized (allLocks) {
            pLocks.add (name);
        }
    }
    
    public static void potentialLock (String o, String n) {
        synchronized (allLocks) {
            if (pLocks.remove (o)) {
                pLocks.add (n);
            }
        }
    }

    private static void removeInvalid (Set names) {
        FileSystem sfs = Repository.getDefault ().getDefaultFileSystem ();
        Iterator i = names.iterator ();
        while (i.hasNext ()) {
            String name = (String) i.next ();
            FileObject fo = sfs.findResource (name);
            if (null == fo || !fo.isLocked()) {
                // file lock recorded in potentialLock has been used
                // in operation which masked file as hidden and nothing
                // was actually locked
                i.remove ();
            }
        }
    }

    /** Creates new LocalFileSystemEx */
    public LocalFileSystemEx () {
        this( false );
    }
    
    /**
     * @since 1.8
     */
    LocalFileSystemEx( boolean supportRemoveWritablesAttr ) {
        if( supportRemoveWritablesAttr ) {
            attr = new DelegatingAttributes( attr );
        }
    }

    protected void lock (String name) throws IOException {
        super.lock (name);
        synchronized (allLocks) {
            FileObject fo = findResource (name);
            allLocks.put (name, fo);
            pLocks.remove (name);
//            allThreads.put (name, new Throwable ("LocalFileSystemEx.lock() is locking file: " + name));
        }
    }    
    
    protected void unlock (String name) {
        synchronized (allLocks) {
            if (allLocks.containsKey (name)) {
                allLocks.remove (name);
//                allThreads.remove (name);
            } else {
                FileObject fo = findResource (name);
                if (fo != null) {
		    for (Map.Entry entry: allLocks.entrySet()) {
                        if (fo.equals (entry.getValue ())) {
                            allLocks.remove (entry.getKey ());
//                            allThreads.remove (entry.getKey ());
                            break;
                        }
                    }
                } else {
                    Logger.getLogger(LocalFileSystemEx.class.getName()).log(Level.WARNING, null,
                                      new Throwable("Can\'t unlock file " + name +
                                                    ", it\'s lock was not found or it wasn\'t locked."));
                }
            }
        }
        super.unlock (name);
    }
    
    private class DelegatingAttributes implements AbstractFileSystem.Attr {
        
        private AbstractFileSystem.Attr a;
        
        public DelegatingAttributes( AbstractFileSystem.Attr a ) {
            this.a = a;
        }

        public Object readAttribute(String name, String attrName) {
            if( "removeWritables".equals( attrName ) ) {
                return new WritableRemover( name );
            }
            return a.readAttribute( name, attrName );
        }

        public void writeAttribute(String name, String attrName, Object value) throws IOException {
            a.writeAttribute( name, attrName, value );
        }

        public Enumeration<String> attributes(String name) {
            return a.attributes( name );
        }

        public void renameAttributes(String oldName, String newName) {
            a.readAttribute( oldName, newName );
        }

        public void deleteAttributes(String name) {
            a.deleteAttributes( name );
        }
    }

    private class WritableRemover implements Callable {
        private String name;
        public WritableRemover( String name ) {
            this.name = name;
        }
        
        public Object call() throws Exception {
            FileObject fo = findResource( name );
            if( null != fo ) {
                fo.delete();
            }
            return null;
        }
        
    }
}
