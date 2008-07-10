/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.components.databinding;

import org.netbeans.modules.vmd.midp.components.*;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class AddressDataSetCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.databinding.pim.AddressDataSet"); //NOI18N
    public static final String PROP_INDEX = "index"; //NOI18N
    public static final String PROP_ATTRIBUTE = "attribute"; //NOI18N
    @Override
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(DataSetAbstractCD.TYPEID, TYPEID, true, true);
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_INDEX, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_ATTRIBUTE, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static Presenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(AddressDataSetCD.class, "DISP_AddrressDataSet_Index_Contact"), //NOI18N
                        PropertyEditorNumber.createIntegerInstance(true, NbBundle.getMessage(AddressDataSetCD.class, "DISP_AddrressDataSet_Index_Contact")), PROP_INDEX) //NOI18N
                    .addProperty(NbBundle.getMessage(AddressDataSetCD.class, "DISP_AddrressDataSet_Index_Attribute"), //NOI18N
                        PropertyEditorNumber.createIntegerInstance(true, NbBundle.getMessage(AddressDataSetCD.class, "DISP_AddrressDataSet_Index_Attribute")), PROP_ATTRIBUTE); //NOI18N
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_INDEX, PROP_ATTRIBUTE))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(PROP_INDEX, PROP_ATTRIBUTE)
                );
    }
    
    @Override
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(  
            //Properties
            createPropertiesPresenter(),
            //code
            createSetterPresenter()
        );
    }
}
