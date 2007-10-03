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

package org.netbeans.modules.j2ee.sun.validation.constraints;

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * ZeroToMaxIntegerConstraint  is a {@link Constraint} to validate numbers between
 * Zero to Integer.MAX_VALUE.
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns empty
 * <code>Collection</code> if the value being validated is a number between Zero
 * to Integer.MAX_VALUE; else it returns a <code>Collection</code> with a 
 * {@link ConstraintFailure} object in it. 
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ZeroToMaxIntegerConstraint extends ConstraintUtils implements Constraint {
    
    /** Creates a new instance of <code>NumberConstraint</code>. */
    public ZeroToMaxIntegerConstraint() {
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to construct
     * the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty if 
     * there are no failures. This method will fail, if the given value
     * is not between Zero and Integer.MAX_VALUE.
     */
    public Collection match(String value, String name) {
        ArrayList failed_constrained_list = new ArrayList();
        if((value != null) && (value.length() != 0)){
            try {
                int intValue = Integer.parseInt(value);
                if((intValue < 0) || (intValue > Integer.MAX_VALUE)){
                    String failureMessage = formatFailureMessage(toString(),
                        value, name);
                    failed_constrained_list.add(new ConstraintFailure(toString(),
                        value, name, failureMessage, BundleReader.getValue(
                           "MSG_ZeroToMaxIntegerConstraint_Failure"))); //NOI18N
                }
                return failed_constrained_list;
            } catch(NumberFormatException e) {
                String failureMessage = formatFailureMessage(toString(), value,
                    name);
                failed_constrained_list.add(new ConstraintFailure(toString(),
                    value, name, failureMessage, BundleReader.getValue(
                        "MSG_NumberConstraint_Failure")));              //NOI18N
            }
        }
        return failed_constrained_list;
    }
}
