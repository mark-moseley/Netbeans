/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;

/**
 * @author rm111737
 */
final class FileObj extends BaseFileObj {
    static final long serialVersionUID = -1133540210876356809L;
    private long lastModified = -1;


    FileObj(final File file, final FileNaming name) {
        super(file, name);
        setLastModified(System.currentTimeMillis());
    }
    
    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();

        final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, false);
        FileOutputStream retVal = null;
        try {
            retVal = new FileOutputStream(getFileName().getFile()) {
                                public void close() throws IOException {
                                    if (!closable.isClosed()) {
                                        super.close();
                                        closable.close();
                                        setLastModified(f.lastModified());
                                        fireFileChangedEvent(false);
                                    }
                                }
                            };
        } catch (FileNotFoundException e) {
            if (closable != null) {
                closable.close();
            }
            if (!f.canWrite()) {
                // Duh, it's read-only.
                String msg = NbBundle.getMessage(FileObj.class, "FileObj.readOnlyFile", f.getAbsolutePath());
                ErrorManager.getDefault().annotate(e, ErrorManager.USER, null, msg, null, null);
            }
            throw e;
        }
        return retVal;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        final File f = getFileName().getFile();
                        
        InputStream inputStream;
        MutualExclusionSupport.Closeable closeableReference = null;
        
        try {
            final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, true);
            closeableReference = closable;
            inputStream = new FileInputStream(getFileName().getFile()) {
                public void close() throws IOException {
                    super.close();
                    closable.close();
                }
            };
        } catch (IOException e) {
            if (closeableReference != null) {
                closeableReference.close();    
            }
            
            final FileNotFoundException fileNotFoundException = (FileNotFoundException) new FileNotFoundException(e.toString()).initCause(e);
            FSException.annotateException(fileNotFoundException);
            throw fileNotFoundException;
        }
        assert inputStream != null;
        return inputStream;
    }

    public final Date lastModified() {
        final File f = getFileName().getFile();
        return new Date(f.lastModified());
    }


    private final void setLastModified(long lastModified) {
            this.lastModified = lastModified;
    }
    
    
    public final FileObject createFolder(final String name) throws IOException {
        throw new IOException(getPath());//isn't directory - cannot create neither file nor folder
    }

    public final FileObject createData(final String name, final String ext) throws IOException {
        throw new IOException(getPath());//isn't directory - cannot create neither file nor folder
    }


    public final FileObject[] getChildren() {
        return new FileObject[]{};//isn't directory - no children
    }

    public final FileObject getFileObject(final String name, final String ext) {
        return null;
    }

    public boolean isValid() {
        //0 - because java.io.File.lastModififed returns 0 for not existing files        
        return lastModified != 0;
    }

    protected void setValid(boolean valid) {
        if (valid) {
            //I can't make valid fileobject when it was one invalidated
            assert isValid() : this.toString();
        } else {
            //0 - because java.io.File.lastModififed returns 0 for not existing files
            lastModified = 0;
        }        
    }

    public final boolean isFolder() {
        return false;
    }

    public final void refresh(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FILE);
        stopWatch.start();                
        if (isValid()) {
            final long oldLastModified = lastModified;
            setLastModified(getFileName().getFile().lastModified());

            if (oldLastModified != -1 && lastModified != -1 && lastModified != 0 && oldLastModified < lastModified) {
                fireFileChangedEvent(expected);
            }
            
            boolean validityFlag = getFileName().getFile().exists();                    
            if (!validityFlag) {
                //fileobject is invalidated
                setValid(false);
                fireFileDeletedEvent(expected);    
            }            
        }                 
        stopWatch.stop();
    }
    

    

    public final Enumeration getChildren(final boolean rec) {
        return Enumerations.empty();
    }

    public final Enumeration getFolders(final boolean rec) {
        return Enumerations.empty();
    }

    public final Enumeration getData(final boolean rec) {
        return Enumerations.empty();
    }


    public final FileLock lock() throws IOException {
        final File me = getFileName().getFile();
        try {            
            return WriteLockFactory.tryLock(me);
        } catch (FileNotFoundException ex) {
            FileNotFoundException fex = ex;                        
            if (!me.exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (me.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.getParentFile().exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            }                                                             
            throw fex;
        }
    }

    final boolean checkLock(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        return ((lock instanceof WriteLock) && (((WriteLock) lock).isValid(f)));
    }
}
