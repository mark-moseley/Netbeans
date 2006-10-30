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
package org.netbeans.api.java.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.tools.JavaFileManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.CachingFileManager;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.parsing.ProxyFileManager;
import org.netbeans.modules.java.source.parsing.SourceFileManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/** Class which contains info about classpath
 *
 * @author Tomas Zezula, Petr Hrebejk
 */
public final class ClasspathInfo {
    
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    
    static {
        ClasspathInfoAccessor.INSTANCE = new ClasspathInfoAccessorImpl ();
        try {
            Class.forName(ClassIndex.class.getName(), true, CompilationInfo.class.getClassLoader());
        } catch (ClassNotFoundException ex) {            
            ErrorManager.getDefault().notify (ex);
        }
    }    
    
    private final CachingArchiveProvider archiveProvider;
    
    private final ClassPath srcClassPath;
    private final ClassPath bootClassPath;
    private final ClassPath compileClassPath;
    private ClassPath outputClassPath;
    
    private final ClassPathListener cpListener;
    private final boolean backgroundCompilation;
    private JavaFileManager fileManager;
    private EventListenerList listenerList =  null;
    private ClassIndex usagesQuery;
    
    /** Creates a new instance of ClasspathInfo (private use the fatctory methods) */
    private ClasspathInfo(CachingArchiveProvider archiveProvider, ClassPath bootCp, ClassPath compileCp, ClassPath srcCp, boolean backgroundCompilation) {
        assert archiveProvider != null && bootCp != null && compileCp != null;
        this.cpListener = new ClassPathListener ();
        this.archiveProvider = archiveProvider;
        if (bootCp.entries().size() == 0) {
            //Broken platform, use the default one
            JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            assert defaultPlatform != null;
            this.bootClassPath = defaultPlatform.getBootstrapLibraries();
            assert this.bootClassPath.entries().size() > 0;
        }
        else {
            this.bootClassPath = CacheClassPath.forClassPath(bootCp);
        }
        this.compileClassPath = CacheClassPath.forClassPath(compileCp);
	this.bootClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.bootClassPath));
	this.compileClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.compileClassPath));
	if ( srcCp != null ) {
            this.srcClassPath = srcCp;
            this.outputClassPath = CacheClassPath.forSourcePath (this.srcClassPath);
	    this.srcClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.srcClassPath));
	}
        else {
            this.srcClassPath = ClassPathSupport.createClassPath(new URL[0]);
            this.outputClassPath = ClassPathSupport.createClassPath(new URL[0]);
        }
        this.backgroundCompilation = backgroundCompilation;
    }
    
    public String toString() {
        return "ClasspathInfo boot:[" + bootClassPath + "],compile:[" + compileClassPath + "],src:[" + srcClassPath + "]";  //NOI18N
    }
    
    // Factory methods ---------------------------------------------------------
    
    
    /** Creates new interface to the compiler
     * @param file for which the CompilerInterface should be created
     * @return ClasspathInfo or null if the file does not exist on the
     * local file system or it has no classpath associated
     */
    public static ClasspathInfo create (final File file) {
        if (file == null) {
            throw new IllegalArgumentException ("Cannot pass null as parameter of ClasspathInfo.create(java.io.File)");     //NOI18N
        }
        final FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return null;
        }
        else {
            return create (fo);
        }
    }
    
    
    private static ClasspathInfo create (FileObject fo, boolean backgroundCompilation) {
        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (bootPath == null) {
            //javac requires at least java.lang
            bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        if (compilePath == null) {
            compilePath = EMPTY_PATH;
        }
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (srcPath == null) {
            srcPath = EMPTY_PATH;
        }
        return create (bootPath, compilePath, srcPath, backgroundCompilation);
    }
    
    /** Creates new interface to the compiler
     * @param fo for which the CompilerInterface should be created
     */
    public static ClasspathInfo create(FileObject fo) {
        return create (fo, false);
    }            
    
    private static ClasspathInfo create(ClassPath bootPath, ClassPath classPath, ClassPath sourcePath, boolean backgroundCompilation) {
        return new ClasspathInfo(CachingArchiveProvider.getDefault(), bootPath, classPath, sourcePath, backgroundCompilation);
    }
    
    public static ClasspathInfo create(ClassPath bootPath, ClassPath classPath, ClassPath sourcePath) {        
        return new ClasspathInfo(CachingArchiveProvider.getDefault(), bootPath, classPath, sourcePath, false);
    }
       
    // Public methods ----------------------------------------------------------
        
    /** Registers ChangeListener which will be notified about the changes in the classpath.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        if (listenerList == null ) {
            listenerList = new EventListenerList();
        }
        listenerList.add (ChangeListener.class, listener);
    }

    /**Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        listenerList.remove (ChangeListener.class, listener);
    }

    public ClassPath getClassPath (PathKind pathKind) {
	switch( pathKind ) {
	    case BOOT:
		return this.bootClassPath;
	    case COMPILE:
		return this.compileClassPath;
	    case SOURCE:
		return this.srcClassPath;
	    case OUTPUT:
		return this.outputClassPath;
	    default:
		assert false : "Unknown path type";     //NOI18N
		return null;
	}
    }    
    
    
    public synchronized ClassIndex getClassIndex () {
        if ( usagesQuery == null ) {
            usagesQuery = new ClassIndex (
                    this.bootClassPath,
                    this.compileClassPath,
                    this.srcClassPath);
        }
        return usagesQuery;
    }
    
    // Package private methods -------------------------------------------------
    
    synchronized JavaFileManager getFileManager() {
        if (this.fileManager == null) {
            boolean hasSources = this.srcClassPath != null;
            this.fileManager = new ProxyFileManager (
                new CachingFileManager (this.archiveProvider, this.bootClassPath, true),
                new CachingFileManager (this.archiveProvider, this.compileClassPath, false),
                hasSources ? (backgroundCompilation ? new CachingFileManager (this.archiveProvider, this.srcClassPath, false)
                    : new SourceFileManager (this.srcClassPath.getRoots())) : null,
                hasSources ? new OutputFileManager (this.archiveProvider, this.outputClassPath, this.srcClassPath) : null
            );
        }
        return this.fileManager;
    }
    
    // Private methods ---------------------------------------------------------

    private void fireChangeListenerStateChanged() {
        ChangeEvent e = null;
        if (listenerList == null) return;
        Object[] listeners = listenerList.getListenerList ();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged (e);
           }
        }
    }

    private static ClassPath createOutputClassPath (final ClassPath cp) throws IOException {
        assert cp != null;
        final List<ClassPath.Entry> entries = cp.entries();
        final List<URL> roots = new ArrayList<URL>(entries.size());
        for (ClassPath.Entry entry : entries) {
            final URL root = Index.getClassFolder(entry.getURL());
            if (root != null) {
                roots.add(root);
            }
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    // Innerclasses ------------------------------------------------------------
    
    public static enum PathKind {	
	BOOT,	
	COMPILE,	
	SOURCE,	
	OUTPUT,
	
    }
    
    private class ClassPathListener implements PropertyChangeListener {	
		
        public void propertyChange (PropertyChangeEvent event) {
            if (ClassPath.PROP_ROOTS.equals(event.getPropertyName())) {
                synchronized (this) {
                    // Kill FileManager
                    fileManager = null;
                    // Kill indexes
                    usagesQuery = null;
                    // Reset the root files
                    if (event.getSource() == ClasspathInfo.this.srcClassPath) {
                        try {
                            ClasspathInfo.this.outputClassPath = createOutputClassPath (ClasspathInfo.this.srcClassPath);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }
                fireChangeListenerStateChanged();
            }
        }
    }
    
    private static class ClasspathInfoAccessorImpl extends ClasspathInfoAccessor {
        
        public JavaFileManager getFileManager(ClasspathInfo cpInfo) {
            return cpInfo.getFileManager();
        }
        
        public ClasspathInfo create (ClassPath bootPath, ClassPath classPath, ClassPath sourcePath, boolean backgroundCompilation) {
            return ClasspathInfo.create(bootPath, classPath, sourcePath, backgroundCompilation);
        }
        
        public ClasspathInfo create (FileObject fo, boolean backgroundCompilation) {
            return ClasspathInfo.create(fo, backgroundCompilation);
        }
    }
}
