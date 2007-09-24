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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import org.netbeans.modules.visualweb.propertyeditors.ValidatorPropertyEditor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.validator.*;

/**
 * An extension of the base validator property editor that filters out validators
 * added by JSF 1.2. This editor is a hack, necessitated by the fact that at present
 * both releases of JSF are represented at design-time by just the JSF 1.2 library.
 *
 * @author gjmurphy
 */
public class JSF1_1ValidatorPropertyEditor extends ValidatorPropertyEditor {

    private static Set<Class> facesValidatorClassSet = new HashSet<Class>();

    static {
        facesValidatorClassSet.add(DoubleRangeValidator.class);
        facesValidatorClassSet.add(LengthValidator.class);
        facesValidatorClassSet.add(LongRangeValidator.class);
    }

    private Class[] ValidatorClasses;

    protected Class[] getValidatorClasses() {
        if (ValidatorClasses == null) {
            Class[] inheritedValidatorClasses = super.getValidatorClasses();
            List<Class> ValidatorClassList = new ArrayList<Class>();
            for (Class ValidatorClass : inheritedValidatorClasses) {
                if (!ValidatorClass.getCanonicalName().startsWith("javax.faces.validator.") ||
                        facesValidatorClassSet.contains(ValidatorClass))
                    ValidatorClassList.add(ValidatorClass);
            }
            ValidatorClasses = ValidatorClassList.toArray(new Class[ValidatorClassList.size()]);
        }
        return ValidatorClasses;
    }

}
