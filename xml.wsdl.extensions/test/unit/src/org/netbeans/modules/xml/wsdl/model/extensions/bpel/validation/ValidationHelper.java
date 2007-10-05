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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.xml.wsdl.model.extensions.TestCatalogModel;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author radval
 */
public class ValidationHelper {
    
    private static Pattern p = Pattern.compile("\"?+\\{\\d\\}\"?+");
    
    /** Creates a new instance of ValidationHelper */
    public ValidationHelper() {
    }
    
    public static void dumpExpecedErrors(HashSet<String> expectedErrors) {
        int counter = 1;
        Iterator<String> it = expectedErrors.iterator();
        while(it.hasNext()) {
            String expectedError = it.next();
            System.out.println("expected error :"+ counter + " " +  expectedError);
            counter++;
        }
    }
    
    public static boolean containsExpectedError(HashSet<String> expectedErrors, String actualError) {
        boolean result = false;
        Iterator<String> it = expectedErrors.iterator();
        while(it.hasNext()) {
            String[] needToMatch = null;
            String expectedError = it.next();
            needToMatch = p.split(expectedError);

            //now let see if expected error can be matched with actual error.
            if(needToMatch != null) {
                //assume we have a match unless we found a mismatch below
                boolean foundMatch = true;
                for(int i = 0; i < needToMatch.length; i++) {
                    String match = needToMatch[i];
                    if(!actualError.contains(match)) {
                        //no exact match found.
                        foundMatch = false;
                        break;
                    }
                }
                
                result = foundMatch;
                if(result) {
                    break;
                }
            }
            
        }
        return result;
    }
}
