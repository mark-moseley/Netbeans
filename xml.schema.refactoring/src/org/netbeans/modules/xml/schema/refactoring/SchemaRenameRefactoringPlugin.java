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

package org.netbeans.modules.xml.schema.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.FauxRefactoringElement;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;



/**
 *
 * @author Sonali Kochar
 */
public class SchemaRenameRefactoringPlugin extends SchemaRefactoringPlugin {
    
    private RenameRefactoring request;
  //  List<RefactoringElementImplementation> elements;
        
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        ErrorItem error = null;
        if(obj instanceof Model) {
           error = RefactoringUtil.precheck((Model)obj, request.getNewName());
        } else if(obj instanceof Nameable) {
           error = RefactoringUtil.precheck((Nameable)obj, request.getNewName());
        }
                
        if (error != null) {
            Problem p = new Problem(true, error.getMessage());
            return p;
        }
        
        return null;
    }
    
    
    /**
     * Creates a new instance of XMLWhereUsedRefactoringPlugin
     */
    public SchemaRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.request = refactoring;
    }
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
       Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
       if( obj == null)
           return null;
       if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
         
        Model model = SharedUtils.getModel(obj);
        ErrorItem error = RefactoringUtil.precheckTarget(model, true);
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
       
        if(obj instanceof Model)
            error  = RefactoringUtil.precheck((Model)model, request.getNewName());
        else if(obj instanceof Nameable)
            error = RefactoringUtil.precheck((Nameable)obj, request.getNewName());
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
        
              
        return null;
       
        
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        //System.out.println("SchemaRenameRefactoringPluging : prepare");
        if(obj == null)
            return null;
        if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
             
        fireProgressListenerStart(ProgressEvent.START, -1);
        //get the gloabl XML transaction object
        XMLRefactoringTransaction transaction = request.getContext().lookup(XMLRefactoringTransaction.class);
        
        //check if the scope if local. if the scope is local, restrict search to target model
        Set<Component> searchRoots = new HashSet<Component>();
        this.findErrors = new ArrayList<ErrorItem>();
        if(transaction.isLocal())
            searchRoots = SharedUtils.getLocalSearchRoots(obj);
        else {
            //do we have any given search roots??
            Component searchRoot = request.getContext().lookup(Component.class);
        
            if(searchRoot == null )
                searchRoots = getSearchRoots(obj);
            else
                searchRoots.add(searchRoot);
        }
        
        List<SchemaRefactoringElement> elements = new ArrayList<SchemaRefactoringElement>();
        for (Component root : searchRoots) {
            List<SchemaRefactoringElement> founds = find(obj, root);
            if (founds != null && founds.size() > 0) {
                   elements.addAll(founds);
            }
        }
       
       
        if(elements.size() > 0) {
            List<Model> models = getModels(elements);
            List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);
            if(errors !=null && errors.size() > 0 ){
                return processErrors(errors);
              } 
        } 
        //register with the gloabl XML transaction object
         transaction.register((XMLRefactoringPlugin)this, elements);
        
        //register with the Refactoring API
        refactoringElements.registerTransaction(transaction);
        
        
        for (SchemaRefactoringElement elem : elements) {
            //System.out.println("SchemaRenameRefactoring::adding element");
            elem.addTransactionObject(transaction);
            refactoringElements.add(request, elem);
            fireProgressListenerStep();
         }
        
        //add a faux refactoring element to represent the target/object being refactored
        //this element is to be added to the bag only as it will not participate in actual refactoring
        Model mod = SharedUtils.getModel(obj);
        FileObject fo = mod.getModelSource().getLookup().lookup(FileObject.class);
       if ( XSD_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
           refactoringElements.add(request, new FauxRefactoringElement(obj, "Rename"));
       }
       
       if(findErrors.size() > 0)
           return processErrors(findErrors);
        
        fireProgressListenerStop();
        return null;
    }
    
   /** Does the change for a given refactoring.
     * @param refactoringElements Collection of refactoring elements 
     */
      public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException {
        Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = SharedUtils.getModelMap(elements);
        Set<Model> models = modelsInRefactoring.keySet();
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        
        for (Model model : models) {
                if (obj instanceof Nameable ) {
                    new RenameReferenceVisitor().rename(model, modelsInRefactoring.get(model), request);
                } else if (obj instanceof Model ) {
                     _refactorUsages(model, modelsInRefactoring.get(model), (RenameRefactoring)request);
                }
           
        }
    }   
    
    private void _refactorUsages(Model mod,Set<RefactoringElementImplementation> elements, RenameRefactoring request ) {
        if (mod == null) return;             
        if (! (mod instanceof SchemaModel)) return;
        SchemaModel model = (SchemaModel)mod;
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation u : elements) {
                if ( !(u instanceof SchemaRefactoringElement) )
                    continue;
                SchemaModelReference ref = (SchemaModelReference) u.getLookup().lookup(SchemaModelReference.class);
                if (ref!=null) {
                    String newLocation = SharedUtils.calculateNewLocationString(ref.getSchemaLocation(), request);
                    ref.setSchemaLocation(newLocation);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    /**
     * @param component the component to check for model reference.
     * @return the reference string if this component is a reference to an 
     * external model, for example, the schema <import> component, 
     * otherwise returns null.
     */
     public String getModelReference(Component component) {
        if (component instanceof SchemaModelReference) {
            return ((SchemaModelReference)component).getSchemaLocation();
        }
        return null;
    }

   }

