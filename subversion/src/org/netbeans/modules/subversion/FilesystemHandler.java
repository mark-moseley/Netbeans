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

package org.netbeans.modules.subversion;

import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import java.io.File;
import java.io.IOException;
import java.util.*;   
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.Utils;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler extends VCSInterceptor {
        
    private final FileStatusCache   cache;
    
    /**
     * Stores all moved files for a later cache refresh in afterMove
     */
    private Set<File> movedFiles = new HashSet<File>();    
    
    /**
     * Stores .svn folders that should be deleted ASAP.
     */ 
    private final Set<File> invalidMetadata = new HashSet<File>(5);
    
    public FilesystemHandler(Subversion svn) {
        cache = svn.getStatusCache();
    }

    @Override
    public boolean beforeDelete(File file) {     
        Subversion.LOG.fine("beforeDelete " + file);
        if (SvnUtils.isPartOfSubversionMetadata(file)) return true;
        // calling cache results in SOE, we must check manually        
        return hasMetadata(file.getParentFile());
    }

    /**
     * This interceptor ensures that subversion metadata is NOT deleted. 
     * 
     * @param file file to delete
     */ 
    @Override
    public void doDelete(File file) throws IOException {
        Subversion.LOG.fine("doDelete " + file);
        boolean isMetadata = SvnUtils.isPartOfSubversionMetadata(file);        
        if (!isMetadata) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false);                
                client.remove(new File [] { file }, true); // delete all files recursively                           
                // with the cache refresh we rely on afterDelete 
            } catch (SVNClientException e) {                
                SvnClientExceptionHandler.notifyException(e, false, false); // log this
                return;
            }                    
        }
    }

    @Override
    public void afterDelete(final File file) {
        Subversion.LOG.fine("afterDelete " + file);
        Utils.post(new Runnable() {
            public void run() {                
                if (file == null) return;
                try {   
                    // I. check if svn is aware that the file was deleted - update its Entries 
                    SvnClient client = Subversion.getInstance().getClient(false);
                    ISVNStatus status = getStatus(client, file);                    
                    if (FilesystemHandler.this.equals(status, SVNStatusKind.UNVERSIONED) ||
                        FilesystemHandler.this.equals(status, SVNStatusKind.DELETED)) 
                    {                       
                        try {   
                            client.remove(new File [] { file }, true);
                        } catch (SVNClientException e) {
                            // ignore; we do not know what to do here; does no harm, the file was probably Locally New
                            Subversion.LOG.log(Level.FINER, null, e);
                        } 
                    }
                } catch (SVNClientException e) {                    
                    SvnClientExceptionHandler.notifyException(e, false, false);
                } finally {                    
                    // II. refresh cache
                    if (!SvnUtils.isPartOfSubversionMetadata(file)) {
                        cache.refreshAsync(file);                            
                    }                     
                }   
            }
        });
    }

    @Override
    public boolean beforeMove(File from, File to) {
        Subversion.LOG.fine("beforeMove " + from +  " -> " + to);
        File destDir = to.getParentFile();
        if (from != null && destDir != null) {            
            // a direct cache call could, because of the synchrone beforeMove handling, 
            // trigger an reentrant call on FS => we have to check manually            
            if (isVersioned(from)) {
                return SvnUtils.isManaged(to);
            }
            // else XXX handle file with saved administative
            // right now they have old status in cache but is it guaranteed?
        }
        return false;
    }

    @Override
    public void doMove(final File from, final File to) throws IOException {        
        Subversion.LOG.fine("doMove " + from +  " -> " + to);
        if (SwingUtilities.isEventDispatchThread()) {
            
            Subversion.LOG.log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace());
            final Throwable innerT[] = new Throwable[1];
            Runnable outOfAwt = new Runnable() {
                public void run() {
                    try {
                        svnMoveImplementation(from, to);
                    } catch (Throwable t) {
                        innerT[0] = t;
                    }
                }
            };
            
            Subversion.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
            if (innerT[0] != null) {
                if (innerT[0] instanceof IOException) {
                    throw (IOException) innerT[0];
                } else if (innerT[0] instanceof RuntimeException) {
                    throw (RuntimeException) innerT[0];
                } else if (innerT[0] instanceof Error) {
                    throw (Error) innerT[0];
                } else {
                    throw new IllegalStateException("Unexpected exception class: " + innerT[0]);  // NOI18N
                }
            }
            
            // end of hack
            
        } else {
            svnMoveImplementation(from, to);
        }
    }
        
    @Override
    public void afterMove(final File from, final File to) {
        Subversion.LOG.fine("afterMove " + from +  " -> " + to);
        Utils.post(new Runnable() {
            public void run() {    
                File[] files;
                synchronized(movedFiles) {
                    movedFiles.add(from);
                    files = movedFiles.toArray(new File[movedFiles.size()]);
                    movedFiles.clear();
                }
                cache.refreshAsync(true, to);  // refresh the whole target tree                         
                cache.refreshAsync(files);      
                File parent = to.getParentFile(); 
                if (parent != null) {
                    if (from.equals(to)) {
                        Subversion.LOG.warning( "Wrong (identity) rename event for " + from.getAbsolutePath());                        
                    }
                }
            }
        });
    }
    
    @Override
    public boolean beforeCreate(File file, boolean isDirectory) {
        Subversion.LOG.fine("beforeCreate " + file);
        if (SvnUtils.isPartOfSubversionMetadata(file)) {            
            synchronized(invalidMetadata) {
                File p = file;
                while(!SvnUtils.isAdministrative(p.getName())) {                    
                    p = p.getParentFile();
                    assert p != null : "file " + file + " doesn't have a .svn parent";
                }                            
                invalidMetadata.add(p);
            }            
            return false;
        } else {
            if (!file.exists()) {                
                try {
                    SvnClient client = Subversion.getInstance().getClient(false);                                        
                    // check if the file wasn't just deleted in this session
                    revertDeleted(client, file, true); 
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
            return false;
        }
    }
    
    @Override
    public void doCreate(File file, boolean isDirectory) throws IOException {
        // do nothing
    }

    @Override
    public void afterCreate(final File file) {   
        Subversion.LOG.fine("afterCreate " + file);
        Utils.post(new Runnable() {
            public void run() {
                if (file == null) return;
                // I. refresh cache 
                int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                if ((status & FileInformation.STATUS_MANAGED) == 0) {
                    return;
                }                
                if (file.isDirectory()) {
                    // II. refresh the whole dir 
                    cache.directoryContentChanged(file);
                }
            }
        });
    }
    
    @Override
    public void afterChange(final File file) {        
        Subversion.LOG.fine("afterChange " + file);
        Utils.post(new Runnable() {
            public void run() {                
                if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {                    
                    cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);                                        
                }
            }
        });
    }

    /**
     * Removes invalid metadata from all known folders.
     */ 
    void removeInvalidMetadata() {
        synchronized(invalidMetadata) {
            for (File file : invalidMetadata) {
                Utils.deleteRecursively(file);
            }
            invalidMetadata.clear();
        }
    }
    
    // private methods ---------------------------
    
    private boolean hasMetadata(File file) {
        return new File(file, ".svn/entries").canRead() || new File(file, "_svn/entries").canRead();
    }
    
    private boolean isVersioned(File file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) return false;            
        return  ( !file.isFile() && hasMetadata(file) ) || ( file.isFile() && hasMetadata(file.getParentFile()) );        
    }

    /**
     * Returns all direct parent folders from the given file which are scheduled for deletion
     * 
     * @param file
     * @param client
     * @return a list of folders 
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private static List<File> getDeletedParents(File file, SvnClient client) throws SVNClientException {
        List<File> ret = new ArrayList<File>();
        for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {        
            ISVNStatus status = getStatus(client, parent);
            if (status == null || !status.getTextStatus().equals(SVNStatusKind.DELETED)) {                                                            
                return ret;
            }
            ret.add(parent);                                      
        }        
        return ret;
    }        
    
    private void revertDeleted(SvnClient client, final File file, boolean checkParents) {
        try {
            ISVNStatus status = getStatus(client, file);
            if (FilesystemHandler.this.equals(status, SVNStatusKind.DELETED)) {
                if(checkParents) {
                    // we have a file scheduled for deletion but it's going to be created again,
                    // => it's parent folder can't stay deleted either
                    List<File> deletedParents = getDeletedParents(file, client);
                    client.revert(deletedParents.toArray(new File[deletedParents.size()]), false);                        
                }        
                        
                // reverting the file will set the metadata uptodate
                client.revert(file, false);
                // our goal was ony to fix the metadata ->
                //  -> get rid of the reverted file
                file.delete();
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
        }
    }

    private void svnMoveImplementation(final File from, final File to) throws IOException {
        try {                        
            boolean force = true; // file with local changes must be forced
            SvnClient client = Subversion.getInstance().getClient(false);
            
            File tmpMetadata = null;
            try {
                // prepare destination, it must be under Subversion control
                removeInvalidMetadata();

                File parent;
                if (to.isDirectory()) {
                    parent = to;
                } else {
                    parent = to.getParentFile();
                }

                if (parent != null) {
                    assert SvnUtils.isManaged(parent);  // see implsMove above                                        
                    // a direct cache call could, because of the synchrone svnMoveImplementation handling, 
                    // trigger an reentrant call on FS => we have to check manually            
                    if (!hasMetadata(parent)) {
                        addDirectories(parent);
                    }
                }

                // perform
                int retryCounter = 6;
                while (true) {
                    try {
                        // check if the file wasn't just deleted in this session
                        revertDeleted(client, to, false);
                        
                        // check the status - if the file isn't in the repository yet ( ADDED | UNVERSIONED )
                        // then it also can't be moved via the svn client
                        ISVNStatus status = getStatus(client, from);
                        
                        // store all from-s children -> they also have to be refreshed in after move
                        List<File> srcChildren = null;
                        try {
                            srcChildren = SvnUtils.listRecursively(from);
                            if (status != null && status.getTextStatus().equals(SVNStatusKind.ADDED)) {                                            
                                client.revert(from, true);  
                                from.renameTo(to);                
                            } else if (status != null && status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {                                            
                                from.renameTo(to);                            
                            } else {                                                        
                                client.move(from, to, force);    
                            }                                                       
                        } finally {
                            // we moved the files so schedule them a for a refresh 
                            // in the following afterMove call
                            synchronized(movedFiles) {
                                if(srcChildren != null) {
                                    movedFiles.addAll(srcChildren);
                                }
                            }
                        }
                        break;
                    } catch (SVNClientException e) {                        
                        // svn: Working copy '/tmp/co/svn-prename-19/AnagramGame-pack-rename/src/com/toy/anagrams/ui2' locked
                        if (e.getMessage().endsWith("' locked") && retryCounter > 0) { // NOI18N
                            // XXX HACK AWT- or FS Monitor Thread performs
                            // concurrent operation
                            try {
                                Thread.sleep(107);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                            retryCounter--;
                            continue;
                        }
                        
                        IOException ex = new IOException("Subversion failed to rename " + from.getAbsolutePath() + " to: " + to.getAbsolutePath()); // NOI18N
                        ex.initCause(e);
                        throw ex;
                            
                    }
                }
            } finally {
                if (tmpMetadata != null) {
                    FileUtils.deleteRecursively(tmpMetadata);
                }
            }
        } catch (SVNClientException e) {
            IOException ex = new IOException("Subversion failed to rename " + from.getAbsolutePath() + " to: " + to.getAbsolutePath()); // NOI18N
            ex.initCause(e);
            throw ex;
        }
    }
    
    /**
     * Seeks versioned root and then adds all folders
     * under Subversion (so it contains metadata),
     */
    private void addDirectories(final File dir) throws SVNClientException  {
        File parent = dir.getParentFile();
        if (parent != null) {            
            if (SvnUtils.isManaged(parent) && !hasMetadata(parent)) {
                addDirectories(parent);  // RECURSION
            }
            SvnClient client = Subversion.getInstance().getClient(false);
            client.addDirectory(dir, false);
            Utils.post(new Runnable() {
                public void run() {
                    cache.refresh(dir, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            });                
        } else {
            throw new SVNClientException("Reached FS root, but it's still not Subversion versioned!"); // NOI18N
        }
    }
    
    private static ISVNStatus getStatus(SvnClient client, File file) throws SVNClientException {
        // a direct cache call could, because of the synchrone beforeCreate handling, 
        // trigger an reentrant call on FS => we have to check manually 
        return client.getSingleStatus(file);
    }

    private boolean equals(ISVNStatus status, SVNStatusKind kind) {
        return status != null && status.getTextStatus().equals(kind);
    }
}
