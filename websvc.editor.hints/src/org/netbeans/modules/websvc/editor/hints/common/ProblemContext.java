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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.Tree;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Encapsulate often reused and sometimes expensive to calculate
 * properties of the class being examined
 *
 * @author Ajit.Bhate@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class ProblemContext implements Lookup.Provider {
    private FileObject fileObject;
    private CompilationInfo info;
    private boolean cancelled = false;
    private Tree elementToAnnotate;
    private TypeElement javaClass;
    private AbstractLookup lookup;
    private InstanceContent ic;
    
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
    
    public TypeElement getJavaClass(){
        return javaClass;
    }
    
    public void setJavaClass(TypeElement element){
        this.javaClass = element;
    }

    public Lookup getLookup() {
        if(lookup == null) {
            if (ic == null) ic = new InstanceContent();
            lookup = new AbstractLookup(ic);
        }
        return lookup;
    }
    
    public void addUserObject(Object info) {
        if (ic == null) ic = new InstanceContent();
        ic.add(info);
    }

    public void removeUserObject(Object info) {
        if (ic == null) return;
        ic.remove(info);
    }

}
