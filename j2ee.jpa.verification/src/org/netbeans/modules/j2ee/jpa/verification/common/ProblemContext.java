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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jpa.verification.common;

import com.sun.source.tree.Tree;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;

/**
 * Encapsulate often reused and sometimes expensive to calculate
 * properties of the class being examined
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class ProblemContext {
    private FileObject fileObject;
    private CompilationInfo info;
    private boolean cancelled = false;
    private Tree elementToAnnotate;
    private Object modelElement;
    private TypeElement javaClass;
    
    public FileObject getFileObject(){
        return fileObject;
    }
    
    public void setFileObject(FileObject fileObject){
        this.fileObject = fileObject;
    }
    
    public CompilationInfo getCompilationInfo(){
        return info;
    }
    
    public void setCompilationInfo(CompilationInfo info){
        this.info = info;
    }
    
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }
    
    /**
     * @return true if the problem finding task was cancelled
     */
    public boolean isCancelled(){
        return cancelled;
    }
    
    public Tree getElementToAnnotate(){
        return elementToAnnotate;
    }
    
    public void setElementToAnnotate(Tree elementToAnnotate){
        this.elementToAnnotate = elementToAnnotate;
    }
    
    public void setElementToAnnotateOrNullIfExists(Tree elementToAnnotate){
        if (getElementToAnnotate() == null){
            setElementToAnnotate(elementToAnnotate);
        }
        else{
            setElementToAnnotate(null);
        }
    }
    
    public Object getModelElement(){
        return modelElement;
    }
    
    public void setModelElement(Object modelElement){
        this.modelElement = modelElement;
    }
    
    public TypeElement getJavaClass(){
        return javaClass;
    }
    
    public void setJavaClass(TypeElement element){
        this.javaClass = element;
    }
}
