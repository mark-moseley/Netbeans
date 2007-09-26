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

package org.netbeans.modules.masterfs.providers;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Encapsulate a group of individual factory methods that are responsible for creating objects
 * of specific interfaces. If subclassed and provided by
 * {@link AnnotationProvider#getInterceptionListener} then
 * individual instances will be called by <code>MasterFileSystem</code>
 * There may exist more than one instance of <code>ProvidedExtensions</code>
 * at a given moment and therefore there is defined for
 * every method wheter will be called by <code>MasterFileSystem</code>
 * for every present instance or just for the first one.
 *
 * @see ProvidedExtensions.IOHandler
 * @see InterceptionListener
 *
 * @author Radek Matous
 */
public class ProvidedExtensions implements InterceptionListener {
    /**
     * Return instance of {@link ProvidedExtensions.IOHandler}
     * that is responsible for moving the file or null.
     *
     * Just the first non null instance of <code>IOHandler</code> is used by
     *  <code>MasterFileSystem</code>
     *
     * @param from file to be moved
     * @param to target to move this file to
     * @return instance of {@link ProvidedExtensions.IOHandler} 
     * that is responsible for moving the file or null
     */
    public ProvidedExtensions.IOHandler getMoveHandler(
            File from, File to) {
        return null;
    }
    
    /*
     * Return instance of {@link ProvidedExtensions.IOHandler}
     * that is responsible for renaming the file or null.
     *
     * Just the first non null instance of <code>IOHandler</code> is used by
     *  <code>MasterFileSystem</code>
     *
     * @param from file to be renamed
     * @param newName new name of file
     * @return instance of {@link ProvidedExtensions.IOHandler} 
     * that is responsible for renaming the file or null
     */
    public ProvidedExtensions.IOHandler getRenameHandler(
            File from, String newName) {
        return null;
    }

    /*
     * Return instance of {@link ProvidedExtensions.DeleteHandler}
     * that is responsible for deleting the file or null.
     *
     * Just the first non null instance of <code>DeleteHandler</code> is used by
     *  <code>MasterFileSystem</code>
     *
     * @param f file or folder to be deleted
     * @return instance of {@link ProvidedExtensions.DeleteHandler} 
     * that is responsible for deleting the file or null
     */    
    public ProvidedExtensions.DeleteHandler getDeleteHandler(File f) {
        return null;
    }
    
    
    public interface IOHandler {
        /**
         * @throws java.io.IOException if handled operation isn't successful
         */
        void handle() throws IOException;
    }
    
    public interface DeleteHandler {
        /**
         * Deletes the file or directory denoted by this abstract pathname.  If
         * this pathname denotes a directory, then the directory must be empty in
         * order to be deleted.
         *
         * @return  <code>true</code> if and only if the file or directory is
         *          successfully deleted; <code>false</code> otherwise
         */
        boolean delete(File file);
    }
    
        
    public void createSuccess(FileObject fo) {}    
    public void createFailure(FileObject parent, String name, boolean isFolder) {}   
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {}    
    public void deleteSuccess(FileObject fo) {}    
    public void deleteFailure(FileObject fo) {}
    public void beforeDelete(FileObject fo) {}


    /*
     * Called by <code>MasterFileSystem</code> when <code>FileObject</code>
     * is going to be modified by asking for <code>OutputStream<code>
     * @see org.openide.filesystems.FileObject#getOutputStream
     * @param fo file which is going to be notified
     * @since 1.10
     */        
    public void beforeChange(FileObject fo) {}    
    
    /*
     * Called by <code>MasterFileSystem</code> after <code>FileObject</code>
     * is locked
     * @see org.openide.filesystems.FileObject#lock
     * @param fo file which was locked
     * @since 1.11
     */            
    public void fileLocked(FileObject fo) {}    
    
    /*
     * Called by <code>MasterFileSystem</code> after <code>FileLock</code>
     * is released
     * @see org.openide.filesystems.FileLock#releaseLock
     * @param fo file which <code>FileLock</code> was released
     * @since 1.11
     */                
    public void fileUnlocked(FileObject fo) {}        
}
