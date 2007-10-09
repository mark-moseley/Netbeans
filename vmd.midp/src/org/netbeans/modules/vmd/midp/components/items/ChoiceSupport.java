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

package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Karol Harezlak
 */

public final class ChoiceSupport {

    private static Map<String, PropertyValue> listTypes;
    private static Map<String, PropertyValue> fitPolicyValues;
    private static Map<String, PropertyValue> choiceGroupTypes;
    
    public static final int VALUE_EXCLUSIVE = 1;
    public static final int VALUE_MULTIPLE = 2;
    public static final int VALUE_IMPLICIT = 3;
    public static final int VALUE_POPUP = 4;
    
    public static final int VALUE_TEXT_WRAP_DEFAULT = 0;
    public static final int VALUE_TEXT_WRAP_ON = 1;
    public static final int VALUE_TEXT_WRAP_OFF = 2;
    
    public static Map<String, PropertyValue> getListTypes() {
        if (listTypes == null) {
            listTypes = new TreeMap<String, PropertyValue>();
            listTypes.put("EXCLUSIVE", MidpTypes.createIntegerValue(VALUE_EXCLUSIVE)); // NOI18N
            listTypes.put("IMPLICIT", MidpTypes.createIntegerValue(VALUE_IMPLICIT));   // NOI18N
            listTypes.put("MULTIPLE", MidpTypes.createIntegerValue(VALUE_MULTIPLE));   // NOI18N
        }        
        return listTypes;
    }
    
    public static Map<String, PropertyValue>  getChoiceGroupTypes() {
        if (choiceGroupTypes == null) {
            choiceGroupTypes = new TreeMap<String, PropertyValue>();
            choiceGroupTypes.put("EXCLUSIVE", MidpTypes.createIntegerValue(VALUE_EXCLUSIVE));  // NOI18N
            choiceGroupTypes.put("POPUP", MidpTypes.createIntegerValue(VALUE_POPUP));          // NOI18N
            choiceGroupTypes.put("MULTIPLE", MidpTypes.createIntegerValue(VALUE_MULTIPLE));    // NOI18N
        }        
        return choiceGroupTypes;
    }
    
    public static Map<String, PropertyValue> getFitPolicyValues() {
        if (fitPolicyValues == null) {
            fitPolicyValues = new TreeMap<String, PropertyValue>();
            fitPolicyValues.put("TEXT_WRAP_DEFAULT", MidpTypes.createIntegerValue(VALUE_TEXT_WRAP_DEFAULT)); // NOI18N
            fitPolicyValues.put("TEXT_WRAP_ON", MidpTypes.createIntegerValue(VALUE_TEXT_WRAP_ON));           // NOI18N
            fitPolicyValues.put("TEXT_WRAP_OFF", MidpTypes.createIntegerValue(VALUE_TEXT_WRAP_OFF));         // NOI18N
        }
        return fitPolicyValues;
    }
    
}
