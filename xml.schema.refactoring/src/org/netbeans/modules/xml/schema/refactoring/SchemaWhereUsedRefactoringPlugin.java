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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.ErrorManager;



/**
 *
 * @author Sonali Kochar
 */
public class SchemaWhereUsedRefactoringPlugin extends SchemaRefactoringPlugin  {
    
   
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    private WhereUsedQuery query;
    
    
    /**
     * Creates a new instance of SchemaWhereUsedRefactoringPlugin
     */
    public SchemaWhereUsedRefactoringPlugin(WhereUsedQuery refactoring) {
        this.query = refactoring;
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
        Problem problem = null;
       
        return problem;
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
       // System.out.println("SchemaWhereusedRefactoring:: prepare called");
        Referenceable ref = query.getRefactoringSource().lookup(Referenceable.class);
        if (ref == null)
            return null;
        
        fireProgressListenerStart(ProgressEvent.START, -1);
        Component searchRoot = query.getContext().lookup(Component.class);
        Set<Component> searchRoots = new HashSet<Component>();
        this.findErrors = new ArrayList<ErrorItem>();
        if(searchRoot == null )
            searchRoots = getSearchRoots(ref);
        else {
           // System.out.println("got search roots");
            searchRoots.add(searchRoot);
        }
        
        List<SchemaRefactoringElement> elements = null;
        for (Component root : searchRoots) {
            elements = find(ref, root);
                if (elements != null) {
                    for (SchemaRefactoringElement ug : elements) {
                        //System.out.println("SchemaWhereusedRefactoring::adding element");
                        refactoringElements.add(query, ug);
                        fireProgressListenerStep();
                    }
                }
        }
        
        //for embedded schemas, we need to add the usage elements so that they can be refactored
        XMLRefactoringTransaction transaction = query.getContext().lookup(XMLRefactoringTransaction.class);
        if(transaction != null)
            transaction.register(elements);
        fireProgressListenerStop();
       
        
      return null;
    }

    public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException {
       //do nothing
    }
    
      
    
}

