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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public final class ValidationUtil {
    public static final String SCHEMA_COMPONENT_ATTRIBUTE_BASE = "base"; // NOI18N

    public static Collection<GlobalSimpleType> BUILT_IN_SIMPLE_TYPES = 
        SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().getSimpleTypes();
    
    private ValidationUtil() {}
    
    // vlv 
    public static SchemaComponent getBasedSimpleType(SchemaComponent component) {
      SchemaComponent type = getBuiltInSimpleType(component);

      if (type == null) {
        return component;
      }
      return type;
    }

    public static String getTypeName(SchemaComponent component) {
      if (component == null) {
        return "n/a"; // NOI18N
      }
      if (component instanceof Named) {
        return ((Named) component).getName();
      }
      return component.toString();
    }

    public static GlobalSimpleType getBuiltInSimpleType(SchemaComponent schemaComponent) {
        if (schemaComponent == null) return null;
        Collection<GlobalSimpleType> 
            schemaSimpleTypes = schemaComponent.getModel().getSchema().getSimpleTypes();
        
        String baseTypeName = schemaComponent.getAnyAttribute(new QName(
            SCHEMA_COMPONENT_ATTRIBUTE_BASE));
        if (baseTypeName != null) {
            baseTypeName = ignoreNamespace(baseTypeName);
            GlobalSimpleType globalSimpleType = findGlobalSimpleType(baseTypeName, 
                BUILT_IN_SIMPLE_TYPES);
            if (globalSimpleType != null) return globalSimpleType;
            globalSimpleType = findGlobalSimpleType(baseTypeName, schemaSimpleTypes);
            if (globalSimpleType != null) {
                for (SchemaComponent childComponent : schemaComponent.getChildren()) {
                    globalSimpleType = getBuiltInSimpleType(childComponent);
                    if (globalSimpleType != null) return globalSimpleType;
                }
                return null;
            } else {
                return null;
            }
        }
        return null;
    }
    
    public static GlobalSimpleType findGlobalSimpleType(String typeName,
        Collection<GlobalSimpleType> globalSimpleTypes) {
        for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
            if (globalSimpleType.toString().equals(typeName)) {
                return globalSimpleType;
            }
        }
        return null;
    }

    public static String ignoreNamespace(String dataWithNamespace) {
        int index = dataWithNamespace.indexOf(":");
        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }
}
