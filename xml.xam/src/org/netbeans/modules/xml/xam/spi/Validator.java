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
package org.netbeans.modules.xml.xam.spi;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;


/**
 * Common interface for validation services on models and components.
 * Typical implementation would implement a domain-specific subtype and publish
 * the implemenation through META-INF/services.
 * Typical client would lookup and select applicable services for the validation
 * target model.
 *
 * @author Nam Nguyen
 * @author Ritesh
 * @author Praveen Savur
 */

public interface Validator {
    
    /**
     * Returns name of this validation service.
     * @return Name of the validator.
     */
    String getName();
    
    
    
    /**
     * Validates given model.
     * @return ValidationResult.
     * @param validationType Type of validation. Complete(slow) or partial(fast). 
     * @param model model to validate.
     * @param validation reference to the validation context.
     */
    ValidationResult validate(Model model, Validation validation,
            ValidationType validationType);
    
    
    enum ResultType {
        ADVICE, WARNING, ERROR
    }
    
    public class ResultItem {
        private Validator validator;
        private ResultType type;
        private Component component = null;
        private String description;
        private int lineNumber = -1;
        private int columnNumber = -1;
        private Model model;
        
        /**
         * Constructor to create an instance of ResultItem
         * @param validator Reference to validator.
         * @param type Type of message.
         * @param component Component to which this resultItem points.
         * @param desc Message text string.
         */
        public ResultItem(Validator validator, ResultType type, Component component,
                String desc) {
            this.validator = validator;
            this.type = type;
            this.component = component;
            this.description = desc;            
            this.model = (component == null) ? null : component.getModel();
        }         
        
        
        /**
         * Constructor to create an instance of ResultItem
         * @param validator Reference to validator.
         * @param type Type of message.
         * @param desc Message text string.
         * @param lineNumber Line number where this error happens.
         * @param columnNumber Column Number where this error happens.
         * @param model Model on which this is reported.
         */
        public ResultItem(Validator validator, ResultType type, 
                String desc, int lineNumber, int columnNumber, Model model) {
                this.validator = validator;
                this.type = type;
                this.description = desc;
                this.lineNumber = lineNumber;
                this.columnNumber = columnNumber; 
                this.model = model;
        }    
        
        
        /**
         * Get the validator which generated this error.
         * @return The validator that generated this ResultItem.
         */
        public Validator getValidator() {
            return validator;
        }
        
        /**
         * Returns type of validation result.
         * @return Type of message. Advice/Warning or Error.
         */
        public ResultType getType() {
            return type;
        }
        
        /**
         * Returns target component of the validation result.
         * @return Component on which this validation result is reported.
         * Return value can be null if the model is non-well formed, in this case
         * use line/column numbers.
         * Either getComponents() or getLineNumber/getColumnNumber() will be valid.
         */
        public Component getComponents() {
            return component;
        }
        
        /**
         * Returns description of the validation result item.
         * @return Message describing advice/warning or error.
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Line position of advice/warning/error.
         * @return Line number on which this ResultItem was reported on.
         * Use Component if line number is -1.
         * Either getComponents() or getLineNumber/getColumnNumber() will be valid.
         */
        public int getLineNumber() {
            return lineNumber;
        }
        
        /**
         * Column position of advice/warning/error.
         * @return Column number on which this ResultItem was reported on.
         * Use Component if column number is -1.
         * Either getComponents() or getLineNumber/getColumnNumber() will be valid.
         */
        public int getColumnNumber() {
            return columnNumber;
        }
        
        /**
         * Model on which this ResultItem was reported on.
         * @return Model
         */
        public Model getModel() {
            return model;
        }
        
    }
}
