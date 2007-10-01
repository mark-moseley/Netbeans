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
package org.netbeans.modules.refactoring.java.api;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Replaces the type usages in a project with those
 * of the super type, where applicable
 * @author Bharath Ravi Kumar
 */
public final class UseSuperTypeRefactoring extends AbstractRefactoring{
    
    private final TreePathHandle javaClassHandle;
    private ElementHandle<TypeElement> superType;
    
    //Forced to create an array since the ComboBoxModel (for the panel)
    //takes only a vector or an array.
    private ElementHandle<TypeElement>[] candidateSuperTypes;
    
    /**
     * Creates a new instance of UseSuperTypeRefactoring
     * @param javaClassHandle  The class whose occurences must be replaced by
     * that of it's supertype
     */
    public UseSuperTypeRefactoring(TreePathHandle javaClassHandle) {
        super(Lookups.fixed(javaClassHandle));
        this.javaClassHandle = javaClassHandle;
        deriveSuperTypes(javaClassHandle);
    }
    
    /**
     * Returns the type whose occurence must be replaced by that of it's supertype.
     * @return The array of elements to be safely deleted
     */
    public TreePathHandle getTypeElement(){
        return javaClassHandle;
    }
    
    /**
     * Sets the SuperType to be used by this refactoring
     * @param superClass The SuperType to be used by this refactoring
     */
    public void setTargetSuperType(ElementHandle<TypeElement> superClass) {
        this.superType = superClass;
    }
    
    /**
     * Returns the SuperType used by this refactoring
     * @return superClass The SuperType used by this refactoring
     */
    public ElementHandle<TypeElement> getTargetSuperType() {
        return this.superType;
    }
    
    /**
     * Returns the possible SuperTypes that could be used for the initial Type
     * @return The list of possible SuperTypes for the current type
     */
    public ElementHandle<TypeElement>[] getCandidateSuperTypes(){
        return candidateSuperTypes;
    }
    
    //private helper methods follow
    
    private void deriveSuperTypes(final TreePathHandle javaClassHandle) {
        
        
        JavaSource javaSrc = JavaSource.forFileObject(javaClassHandle.
                getFileObject());
        try{
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                }
                
                public void run(CompilationController complController) throws IOException {
                    
                    complController.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement javaClassElement = (TypeElement) 
                            javaClassHandle.resolveElement(complController);
                    candidateSuperTypes = deduceSuperTypes(javaClassElement, 
                            complController);
                }
            }, false);
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
        return ;
    }
    
    //    --private helper methods follow--
    
    /* Checks each Object in the collection that's
     * passed as the second parameter, converts it to a raw type from
     * a ParameterizedType, if necessary, and adds it to the candidateSuperTypesList
     */
    //TODO: Rewrite this for retouche
    private void reduceParamTypes(Collection candidateSuperTypeList, Collection javaClassList) {
        //        Iterator interfacesIterator = javaClassList.iterator();
        //        while(interfacesIterator.hasNext()){
        //            Object superClass = (Object) interfacesIterator.next();
        //            if(superClass instanceof ParameterizedType)
        //                superClass = ((ParameterizedType)superClass).getDefinition();
        //            candidateSuperTypeList.add(superClass);
        //        }
    }
    
    private ElementHandle[] deduceSuperTypes(TypeElement subTypeElement, 
            CompilationController compCtlr){

        TypeMirror subtypeMirror = subTypeElement.asType();
        Types types = compCtlr.getTypes();
        Comparator<TypeMirror> comparator = new TypeMirrorComparator();
        //TODO:The working set (workingTypeMirrors) doesn't have to be a TreeSet. 
        //Why unnecessarily do the additional work of ordering in an intermediate Set?
        TreeSet<TypeMirror> finalSuperTypeMirrors = new TreeSet<TypeMirror>(comparator);
        TreeSet<TypeMirror> workingTypeMirrors = new TreeSet<TypeMirror>(comparator);
        workingTypeMirrors.add(subtypeMirror);
        getAllSuperIFs(subtypeMirror, workingTypeMirrors, finalSuperTypeMirrors,
                compCtlr);
        ElementHandle[] superTypeHandles = new ElementHandle[finalSuperTypeMirrors.size()];
        int index = 0;
        for (Iterator<TypeMirror> it = finalSuperTypeMirrors.iterator(); it.hasNext();) {
            TypeMirror typeMirror = it.next();
            superTypeHandles[index++] = ElementHandle.create(types.asElement(typeMirror));
        }
        return superTypeHandles;

    }    

    private void getAllSuperIFs(TypeMirror subTypeMirror,
            Collection<TypeMirror> uniqueIFs, Collection<TypeMirror> finalIFCollection,
            CompilationController compCtlr){
        Types types = compCtlr.getTypes();
        Iterator<? extends TypeMirror> subTypeIFs = types.directSupertypes(subTypeMirror).
                iterator();
        while(subTypeIFs.hasNext()){
            TypeMirror superType = subTypeIFs.next();
            finalIFCollection.add(superType);
            if(!uniqueIFs.contains(superType)){
                getAllSuperIFs(superType, uniqueIFs, finalIFCollection, compCtlr);
            }
        }
        return;
    }

    //Compares two types alphabetically based on their fully qualified name
    private static class TypeMirrorComparator implements Comparator<TypeMirror>{

        public int compare(TypeMirror type1, TypeMirror type2) {
            return type1.toString().compareTo(type2.toString());
        }
        
    }
    
}