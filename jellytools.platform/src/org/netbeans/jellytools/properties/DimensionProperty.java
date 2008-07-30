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
package org.netbeans.jellytools.properties;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator;

/** Operator serving property of type Dimension
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DimensionProperty extends Property {

    /** Creates a new instance of DimensionProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public DimensionProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return DimensionCustomEditorOperator */    
    public DimensionCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new DimensionCustomEditorOperator(getName());
    }
    
    /** setter for Dimension valuethrough Custom Editor
     * @param width String width
     * @param height String height */    
    public void setDimensionValue(String width, String height) {
        DimensionCustomEditorOperator customizer=invokeCustomizer();
        customizer.setDimensionValue(width, height);
        customizer.ok();
    }        
    
    /** getter for Dimension valuethrough Custom Editor
     * @return String[2] width and height */    
    public String[] getDimensionValue() {
        String[] value=new String[2];
        DimensionCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getWidthValue();
        value[1]=customizer.getHeightValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }         
}
