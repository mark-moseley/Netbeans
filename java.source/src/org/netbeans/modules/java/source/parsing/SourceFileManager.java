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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class SourceFileManager implements JavaFileManager {
    
    private FileObject[] sourceRoots;    
    
    /** Creates a new instance of SourceFileManager */
    public SourceFileManager (FileObject... sourceRoots) {
        this.sourceRoots = sourceRoots;
    }

    public List<JavaFileObject> list(final Location l, final String packageName, final Set<JavaFileObject.Kind> kinds, final boolean recursive) {
        //Todo: Caching of results, needs listening on FS
        List<JavaFileObject> result = new ArrayList<JavaFileObject> ();
        String _name = packageName.replace('.','/');    //NOI18N
        for (FileObject root : this.sourceRoots) {
            FileObject tmpFile = root.getFileObject(_name);
            if (tmpFile != null && tmpFile.isFolder()) {
                Enumeration<? extends FileObject> files = tmpFile.getChildren (recursive);
                while (files.hasMoreElements()) {
                    FileObject file = files.nextElement();
                    JavaFileObject.Kind kind;
                    final String ext = file.getExt();
                    if (FileObjects.JAVA.equalsIgnoreCase(ext)) {
                        kind = JavaFileObject.Kind.SOURCE;
                    }
                    else if (FileObjects.CLASS.equalsIgnoreCase(ext) || "sig".equalsIgnoreCase(ext)) {
                        kind = JavaFileObject.Kind.CLASS;
                    }
                    else if (FileObjects.HTML.equalsIgnoreCase(ext)) {
                        kind = JavaFileObject.Kind.HTML;
                    }
                    else {
                        kind = JavaFileObject.Kind.OTHER;
                    }
                    if (kinds.contains(kind)) {                        
                        result.add (SourceFileObject.create(file));
                    }
                }
            }
        }
        return result;
    }

    public javax.tools.FileObject getFileForInput (final Location l, final String pkgName, final String relativeName) {
        String rp = FileObjects.getRelativePath (pkgName, relativeName);
        for (FileObject root : this.sourceRoots) {
            FileObject file = root.getFileObject(rp);
            if (file != null) {
                return SourceFileObject.create (file);
            }
        }
        return null;
    }
    
    public JavaFileObject getJavaFileForInput (Location l, final String className, JavaFileObject.Kind kind) {
        String[] namePair = FileObjects.getParentRelativePathAndName (className);
        if (namePair == null) {
            return null;
        }
        String ext = kind == JavaFileObject.Kind.CLASS ? "sig" : kind.extension.substring(1);   //Skeep the .
        for (FileObject root : this.sourceRoots) {
            FileObject parent = root.getFileObject(namePair[0]);
            if (parent != null) {
                FileObject[] children = parent.getChildren();
                for (FileObject child : children) {
                    if (namePair[1].equals(child.getName()) && ext.equalsIgnoreCase(child.getExt())) {
                        return SourceFileObject.create (child);
                    }
                }
            }
        }
        return null;
    }

    public javax.tools.FileObject getFileForOutput(Location l, String pkgName, String relativeName, javax.tools.FileObject sibling) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ("The SourceFileManager does not support write operations.");   // NOI18N
    }

    public JavaFileObject getJavaFileForOutput (Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException("The SourceFileManager does not support write operations."); // NOI18N
    }       
    
    public void flush() throws java.io.IOException {
        //Nothing to do
    }

    public void close() throws java.io.IOException {
        //Nothing to do
    }            
    
    public int isSupportedOption(String string) {
        return -1;
    }
    
    public boolean handleOption (final String head, final Iterator<String> tail) {
        return false;
    }
 
    public boolean hasLocation(Location location) {
        return true;
    }
       
    public ClassLoader getClassLoader (Location l) {
        return null;
    }
    
    public String inferBinaryName (final Location l, final JavaFileObject jfo) {        
        try {            
            FileObject fo;
            if (jfo instanceof SourceFileObject) {
                fo = ((SourceFileObject)jfo).file;
            }
            else {
                //Should never happen in the IDE
                fo = URLMapper.findFileObject(jfo.toUri().toURL());
            }
            for (FileObject root : this.sourceRoots) {
                if (FileUtil.isParentOf(root,fo)) {
                    String relativePath = FileUtil.getRelativePath(root,fo);
                    int index = relativePath.lastIndexOf('.');
                    assert index > 0;                    
                    final String result = relativePath.substring(0,index).replace('/','.');                    
                    return result;
                }
            }
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(e);
        }        
        return null;
    }

    public boolean isSameFile(javax.tools.FileObject fileObject, javax.tools.FileObject fileObject0) {
        return fileObject instanceof SourceFileObject 
               && fileObject0 instanceof SourceFileObject
               && ((SourceFileObject)fileObject).file == ((SourceFileObject)fileObject0).file;
    }
}
