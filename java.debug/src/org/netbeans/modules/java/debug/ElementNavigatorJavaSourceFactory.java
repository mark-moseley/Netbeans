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
package org.netbeans.modules.java.debug;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.netbeans.modules.java.debug.TreeNavigatorJavaSourceFactory.WrapperTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public final class ElementNavigatorJavaSourceFactory extends LookupBasedJavaSourceTaskFactory {
    
    private CancellableTask<CompilationInfo> task;
    
    static ElementNavigatorJavaSourceFactory getInstance() {
        return Lookup.getDefault().lookup(ElementNavigatorJavaSourceFactory.class);
    }
    
    public ElementNavigatorJavaSourceFactory() {
        super(Phase.UP_TO_DATE, Priority.NORMAL);
    }

    public synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
        //XXX: should not be necessary to do the wrapper task, but for some reason it is necessary:
        return new WrapperTask(task);
    }

    public List<FileObject> getFileObjects() {
        List<FileObject> result = super.getFileObjects();

        if (result.size() == 1)
            return result;

        return Collections.emptyList();
    }

    public FileObject getFile() {
        List<FileObject> result = super.getFileObjects();
        
        if (result.size() == 1)
            return result.get(0);
        
        return null;
    }

    public synchronized void setLookup(Lookup l, CancellableTask<CompilationInfo> task) {
        this.task = task;
        super.setLookup(l);
    }

}
