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

/*
 * PropertyViewFactory.java
 *
 * Created on January 29, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.property.view;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyModelException;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public abstract class PropertyViewFactory {
    
    private static PropertyViewFactory mInstance;
    
    /** Creates a new instance of PropertyViewFactory */
    public PropertyViewFactory() {
    }
    
    public static synchronized PropertyViewFactory getInstance() throws PropertyModelException {
        if (null == mInstance) {
            String fac = System.getProperty(PropertyViewFactory.class.getName(),
                    "org.netbeans.modules.xml.wsdl.ui.property.view.impl.PropertyViewFactoryImpl");//NOI18N
            try {
                mInstance = (PropertyViewFactory) Class.forName(fac).newInstance();
            } catch (Exception e) {
                throw new PropertyModelException(
                        NbBundle.getMessage(PropertyViewFactory.class, "ERR_MSG_PropertyViewFactory_CLASS_NOT_FOUND", fac), e);
            }
        }
        return mInstance;
    }
    
    /**
     * Get all the property set for a given extensibility element
     */
    public abstract Sheet.Set[] getPropertySets(ExtensibilityElement exElement, QName elementQName, Element schemaElement);
}
