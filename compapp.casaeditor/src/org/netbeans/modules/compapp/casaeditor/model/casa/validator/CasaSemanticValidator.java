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

package org.netbeans.modules.compapp.casaeditor.model.casa.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.validator.visitor.CasaSemanticValidationVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

/**
 *
 * @author jqian
 */
public class CasaSemanticValidator implements Validator {    
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult(Collections.EMPTY_SET, Collections.EMPTY_SET);
    
    public String getName() {
        return "CasaSemanticValidator"; // NOI18N
    }
    
    public ValidationResult validate(Model model, 
            Validation validation, 
            ValidationType validationType) {
        
        if (model instanceof CasaModel) {
            CasaModel casaModel = (CasaModel) model;

            List<Model> validatedModels = new ArrayList<Model>();
            
            if (validationType.equals(ValidationType.COMPLETE) ||
                    validationType.equals(ValidationType.PARTIAL)) {
                if (casaModel.getState() == Model.State.NOT_WELL_FORMED){
                    return EMPTY_RESULT;
                }
                 
                CasaSemanticValidationVisitor visitor =
                        new CasaSemanticValidationVisitor(
                            this, validation, validatedModels);
                casaModel.getRootComponent().accept(visitor);
                
                validatedModels.add(model);
                
                List<ResultItem> resultItems = visitor.getResultItems();
                return new ValidationResult(resultItems, validatedModels);                
            }
        } 
        
        return EMPTY_RESULT;
    }    
}
