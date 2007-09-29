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

package org.netbeans.modules.xml.catalog.user;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.NbBundle;

/**
 * description of {@link UserXMLCatalog}.
 * @author Milan Kuchtiak
 */
public class UserXMLCatalogBeanInfo extends SimpleBeanInfo {

    public UserXMLCatalogBeanInfo() {}

    private static final int PROPERTY_displayName = 0;
    private static final int PROPERTY_shortDescription = 1;

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties = new PropertyDescriptor[2];
    
        try {
            properties[PROPERTY_displayName] = new PropertyDescriptor ("displayName", UserXMLCatalog.class, "getDisplayName", null);
            properties[PROPERTY_displayName].setDisplayName (NbBundle.getMessage(UserXMLCatalog.class,"PROP_catalog_name"));
            properties[PROPERTY_displayName].setShortDescription (NbBundle.getMessage(UserXMLCatalog.class,"HINT_catalog_name"));
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", UserXMLCatalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName (NbBundle.getMessage(UserXMLCatalog.class,"PROP_catalog_desc"));
            properties[PROPERTY_shortDescription].setShortDescription (NbBundle.getMessage(UserXMLCatalog.class,"HINT_catalog_desc"));
        } catch( java.beans.IntrospectionException e) {
        }
        return properties;
    }
    
}
