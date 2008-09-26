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
 *
 * Portions Copyrighted 2008 Craig MacKay.
*/

package org.netbeans.modules.spring.beans.catalog;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Catalog for Spring Framework XML schemas.
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class SpringCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    private static final String SPRING_AOP_2_0_XSD = "spring-aop-2.0.xsd";                                                                                            // NOI18N
    private static final String SPRING_AOP_2_0_XSD_URI = "http://www.springframework.org/schema/aop/spring-aop-2.0.xsd";                                              // NOI18N
    private static final String SPRING_AOP_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-aop-2.0.xsd";                       // NOI18N
    private static final String SPRING_AOP_2_0_XSD_ID = "SCHEMA:" + SPRING_AOP_2_0_XSD_URI;                                                                           // NOI18N

    private static final String SPRING_AOP_2_1_XSD = "spring-aop-2.1.xsd";                                                                                            // NOI18N
    private static final String SPRING_AOP_2_1_XSD_URI = "http://www.springframework.org/schema/aop/spring-aop-2.1.xsd";                                              // NOI18N
    private static final String SPRING_AOP_2_1_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-aop-2.1.xsd";                       // NOI18N
    private static final String SPRING_AOP_2_1_XSD_ID = "SCHEMA:" + SPRING_AOP_2_1_XSD_URI;                                                                           // NOI18N

    private static final String SPRING_AOP_2_5_XSD = "spring-aop-2.5.xsd";                                                                                            // NOI18N
    private static final String SPRING_AOP_2_5_XSD_URI = "http://www.springframework.org/schema/aop/spring-aop-2.5.xsd";                                              // NOI18N
    private static final String SPRING_AOP_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-aop-2.5.xsd";                       // NOI18N
    private static final String SPRING_AOP_2_5_XSD_ID = "SCHEMA:" + SPRING_AOP_2_5_XSD_URI;                                                                           // NOI18N

    private static final String SPRING_BEANS_DTD = "spring-beans.dtd";                                                                                                // NOI18N
    private static final String SPRING_BEANS_DTD_PUBLIC_ID = "-//SPRING//DTD BEAN//EN";                                                                               // NOI18N
    private static final String SPRING_BEANS_DTD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-beans.dtd";                           // NOI18N

    private static final String SPRING_BEANS_2_0_DTD = "spring-beans-2.0.dtd";                                                                                        // NOI18N
    private static final String SPRING_BEANS_2_0_DTD_PUBLIC_ID = "-//SPRING//DTD BEAN 2.0//EN";                                                                       // NOI18N
    private static final String SPRING_BEANS_2_0_DTD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-beans-2.0.dtd";                   // NOI18N

    private static final String SPRING_BEANS_2_0_XSD = "spring-beans-2.0.xsd";                                                                                        // NOI18N
    private static final String SPRING_BEANS_2_0_XSD_URI = "http://www.springframework.org/schema/beans/spring-beans-2.0.xsd";                                        // NOI18N
    private static final String SPRING_BEANS_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-beans-2.0.xsd";                   // NOI18N
    private static final String SPRING_BEANS_2_0_XSD_ID = "SCHEMA:" + SPRING_BEANS_2_0_XSD_URI;                                                                       // NOI18N

    private static final String SPRING_BEANS_2_5_XSD = "spring-beans-2.5.xsd";                                                                                        // NOI18N
    private static final String SPRING_BEANS_2_5_XSD_URI = "http://www.springframework.org/schema/beans/spring-beans-2.5.xsd";                                        // NOI18N
    private static final String SPRING_BEANS_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-beans-2.5.xsd";                   // NOI18N
    private static final String SPRING_BEANS_2_5_XSD_ID = "SCHEMA:" + SPRING_BEANS_2_5_XSD_URI;                                                                       // NOI18N

    private static final String SPRING_CONTEXT_2_5_XSD = "spring-context-2.5.xsd";                                                                                    // NOI18N
    private static final String SPRING_CONTEXT_2_5_XSD_URI = "http://www.springframework.org/schema/context/spring-context-2.5.xsd";                                  // NOI18N
    private static final String SPRING_CONTEXT_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-context-2.5.xsd";               // NOI18N
    private static final String SPRING_CONTEXT_2_5_XSD_ID = "SCHEMA:" + SPRING_CONTEXT_2_5_XSD_URI;                                                                   // NOI18N

    private static final String SPRING_JEE_2_0_XSD = "spring-jee-2.0.xsd";                                                                                            // NOI18N
    private static final String SPRING_JEE_2_0_XSD_URI = "http://www.springframework.org/schema/jee/spring-jee-2.0.xsd";                                              // NOI18N
    private static final String SPRING_JEE_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-jee-2.0.xsd";                       // NOI18N
    private static final String SPRING_JEE_2_0_XSD_ID = "SCHEMA:" + SPRING_JEE_2_0_XSD_URI;                                                                           // NOI18N

    private static final String SPRING_JEE_2_5_XSD = "spring-jee-2.5.xsd";                                                                                            // NOI18N
    private static final String SPRING_JEE_2_5_XSD_URI = "http://www.springframework.org/schema/jee/spring-jee-2.5.xsd";                                              // NOI18N
    private static final String SPRING_JEE_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-jee-2.5.xsd";                       // NOI18N
    private static final String SPRING_JEE_2_5_XSD_ID = "SCHEMA:" + SPRING_JEE_2_5_XSD_URI;                                                                           // NOI18N

    private static final String SPRING_JMS_2_5_XSD = "spring-jms-2.5.xsd";                                                                                            // NOI18N
    private static final String SPRING_JMS_2_5_XSD_URI = "http://www.springframework.org/schema/jms/spring-jms-2.5.xsd";                                              // NOI18N
    private static final String SPRING_JMS_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-jms-2.5.xsd";                       // NOI18N
    private static final String SPRING_JMS_2_5_XSD_ID = "SCHEMA:" + SPRING_JMS_2_5_XSD_URI;                                                                           // NOI18N

    private static final String SPRING_LANG_2_0_XSD = "spring-lang-2.0.xsd";                                                                                          // NOI18N
    private static final String SPRING_LANG_2_0_XSD_URI = "http://www.springframework.org/schema/lang/spring-lang-2.0.xsd";                                           // NOI18N
    private static final String SPRING_LANG_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-lang-2.0.xsd";                     // NOI18N
    private static final String SPRING_LANG_2_0_XSD_ID = "SCHEMA:" + SPRING_LANG_2_0_XSD_URI;                                                                         // NOI18N

    private static final String SPRING_LANG_2_5_XSD = "spring-lang-2.5.xsd";                                                                                          // NOI18N
    private static final String SPRING_LANG_2_5_XSD_URI = "http://www.springframework.org/schema/lang/spring-lang-2.5.xsd";                                           // NOI18N
    private static final String SPRING_LANG_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-lang-2.5.xsd";                     // NOI18N
    private static final String SPRING_LANG_2_5_XSD_ID = "SCHEMA:" + SPRING_LANG_2_5_XSD_URI;                                                                         // NOI18N

    private static final String SPRING_OSGI_XSD = "spring-osgi.xsd";                                                                                                  // NOI18N
    private static final String SPRING_OSGI = "http://www.springframework.org/schema/osgi/spring-osgi.xsd";                                                           // NOI18N
    private static final String SPRING_OSGI_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-osgi.xsd";                             // NOI18N
    private static final String SPRING_OSGI_XSD_ID = "SCHEMA:" + SPRING_OSGI;                                                                                         // NOI18N

    private static final String SPRING_TOOL_2_0_XSD = "spring-tool-2.0.xsd";                                                                                          // NOI18N
    private static final String SPRING_TOOL_2_0_XSD_URI = "http://www.springframework.org/schema/lang/spring-tool-2.0.xsd";                                           // NOI18N
    private static final String SPRING_TOOL_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tool-2.0.xsd";                     // NOI18N
    private static final String SPRING_TOOL_2_0_XSD_ID = "SCHEMA:" + SPRING_TOOL_2_0_XSD_URI;                                                                         // NOI18N

    private static final String SPRING_TOOL_2_5_XSD = "spring-tool-2.5.xsd";                                                                                          // NOI18N
    private static final String SPRING_TOOL_2_5_XSD_URI = "http://www.springframework.org/schema/lang/spring-tool-2.5.xsd";                                           // NOI18N
    private static final String SPRING_TOOL_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tool-2.5.xsd";                     // NOI18N
    private static final String SPRING_TOOL_2_5_XSD_ID = "SCHEMA:" + SPRING_TOOL_2_5_XSD_URI;                                                                         // NOI18N

    private static final String SPRING_TX_2_0_XSD = "spring-tx-2.0.xsd";                                                                                              // NOI18N
    private static final String SPRING_TX_2_0_XSD_URI = "http://www.springframework.org/schema/tx/spring-tx-2.0.xsd";                                                 // NOI18N
    private static final String SPRING_TX_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tx-2.0.xsd";                         // NOI18N
    private static final String SPRING_TX_2_0_XSD_ID = "SCHEMA:" + SPRING_TX_2_0_XSD_URI;                                                                             // NOI18N

    private static final String SPRING_TX_2_1_XSD = "spring-tx-2.1.xsd";                                                                                              // NOI18N
    private static final String SPRING_TX_2_1_XSD_URI = "http://www.springframework.org/schema/tx/spring-tx-2.1.xsd";                                                 // NOI18N
    private static final String SPRING_TX_2_1_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tx-2.1.xsd";                         // NOI18N
    private static final String SPRING_TX_2_1_XSD_ID = "SCHEMA:" + SPRING_TX_2_1_XSD_URI;                                                                             // NOI18N

    private static final String SPRING_TX_2_5_XSD = "spring-tx-2.5.xsd";                                                                                              // NOI18N
    private static final String SPRING_TX_2_5_XSD_URI = "http://www.springframework.org/schema/tx/spring-tx-2.5.xsd";                                                 // NOI18N
    private static final String SPRING_TX_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tx-2.5.xsd";                         // NOI18N
    private static final String SPRING_TX_2_5_XSD_ID = "SCHEMA:" + SPRING_TX_2_5_XSD_URI;                                                                             // NOI18N

    private static final String SPRING_UTIL_2_0_XSD = "spring-util-2.0.xsd";                                                                                          // NOI18N
    private static final String SPRING_UTIL_2_0_XSD_URI = "http://www.springframework.org/schema/util/spring-util-2.0.xsd";                                           // NOI18N
    private static final String SPRING_UTIL_2_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-util-2.0.xsd";                     // NOI18N
    private static final String SPRING_UTIL_2_0_XSD_ID = "SCHEMA:" + SPRING_UTIL_2_0_XSD_URI;                                                                         // NOI18N

    private static final String SPRING_UTIL_2_5_XSD = "spring-util-2.5.xsd";                                                                                          // NOI18N
    private static final String SPRING_UTIL_2_5_XSD_URI = "http://www.springframework.org/schema/util/spring-util-2.5.xsd";                                           // NOI18N
    private static final String SPRING_UTIL_2_5_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-util-2.5.xsd";                     // NOI18N
    private static final String SPRING_UTIL_2_5_XSD_ID = "SCHEMA:" + SPRING_UTIL_2_5_XSD_URI;                                                                         // NOI18N

    private static final String SPRING_WEBFLOW_CONFIG_1_0_XSD = "spring-webflow-config-1.0.xsd";                                                                      // NOI18N
    private static final String SPRING_WEBFLOW_CONFIG_1_0 = "http://www.springframework.org/schema/webflow-config/spring-webflow-config-1.0.xsd";                     // NOI18N
    private static final String SPRING_WEBFLOW_CONFIG_1_0_XSD_LOCAL_URI = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-webflow-config-1.0.xsd"; // NOI18N
    private static final String SPRING_WEBFLOW_CONFIG_1_0_XSD_ID = "SCHEMA:" + SPRING_WEBFLOW_CONFIG_1_0;                                                             // NOI18N

    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(SPRING_AOP_2_0_XSD_ID);
        list.add(SPRING_AOP_2_1_XSD_ID);
        list.add(SPRING_AOP_2_5_XSD_ID);
        list.add(SPRING_BEANS_DTD_PUBLIC_ID);
        list.add(SPRING_BEANS_2_0_DTD_PUBLIC_ID);
        list.add(SPRING_BEANS_2_0_XSD_ID);
        list.add(SPRING_BEANS_2_5_XSD_ID);
        list.add(SPRING_CONTEXT_2_5_XSD_ID);
        list.add(SPRING_JEE_2_0_XSD_ID);
        list.add(SPRING_JEE_2_5_XSD_ID);
        list.add(SPRING_JMS_2_5_XSD_ID);
        list.add(SPRING_LANG_2_0_XSD_ID);
        list.add(SPRING_LANG_2_5_XSD_ID);
        list.add(SPRING_OSGI_XSD_ID);
        list.add(SPRING_TOOL_2_0_XSD_ID);
        list.add(SPRING_TOOL_2_5_XSD_ID);
        list.add(SPRING_TX_2_0_XSD_ID);
        list.add(SPRING_TX_2_1_XSD_ID);
        list.add(SPRING_TX_2_5_XSD_ID);
        list.add(SPRING_UTIL_2_0_XSD_ID);
        list.add(SPRING_UTIL_2_5_XSD_ID);
        list.add(SPRING_WEBFLOW_CONFIG_1_0_XSD_ID);
        return list.iterator();
    }

    public void refresh() {
    }

    public String getSystemID(String publicId) {
        if(publicId.equals(SPRING_AOP_2_0_XSD_ID)) {
            return SPRING_AOP_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_AOP_2_1_XSD_ID)) {
            return SPRING_AOP_2_1_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_AOP_2_5_XSD_ID)) {
            return SPRING_AOP_2_5_XSD_LOCAL_URI;
        } else if (publicId.equals(SPRING_BEANS_DTD_PUBLIC_ID)) {
            return SPRING_BEANS_DTD_LOCAL_URI;
        } else if (publicId.equals(SPRING_BEANS_2_0_DTD_PUBLIC_ID)) {
            return SPRING_BEANS_2_0_DTD_LOCAL_URI;
        } else if(publicId.equals(SPRING_BEANS_2_0_XSD_ID)) {
            return SPRING_BEANS_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_BEANS_2_5_XSD_ID)) {
            return SPRING_BEANS_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_CONTEXT_2_5_XSD_ID)) {
            return SPRING_CONTEXT_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_JEE_2_0_XSD_ID)) {
            return SPRING_JEE_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_JEE_2_5_XSD_ID)) {
            return SPRING_JEE_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_JMS_2_5_XSD_ID)) {
            return SPRING_JMS_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_LANG_2_0_XSD_ID)) {
            return SPRING_LANG_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_LANG_2_5_XSD_ID)) {
            return SPRING_LANG_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_OSGI_XSD_ID)) {
            return SPRING_OSGI_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_TOOL_2_0_XSD_ID)) {
            return SPRING_TOOL_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_TOOL_2_5_XSD_ID)) {
            return SPRING_TOOL_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_TX_2_0_XSD_ID)) {
            return SPRING_TX_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_TX_2_1_XSD_ID)) {
            return SPRING_TX_2_1_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_TX_2_5_XSD_ID)) {
            return SPRING_TX_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_UTIL_2_0_XSD_ID)) {
            return SPRING_UTIL_2_0_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_UTIL_2_5_XSD_ID)) {
            return SPRING_UTIL_2_5_XSD_LOCAL_URI;
        } else if(publicId.equals(SPRING_WEBFLOW_CONFIG_1_0_XSD_ID)) {
            return SPRING_WEBFLOW_CONFIG_1_0_XSD_LOCAL_URI;
        }
        return null;
    }

    public String resolveURI(String arg0) {
        return null;
    }

    public String resolvePublic(String arg0) {
        return null;
    }

    public void addCatalogListener(CatalogListener catalogListener) {
    }

    public void removeCatalogListener(CatalogListener catalogListener) {
    }

    public Image getIcon(int i) {
        return ImageUtilities.loadImage("org/netbeans/modules/spring/beans/resources/spring.png"); // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SpringCatalog.class, "LBL_SpringCatalog");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(SpringCatalog.class, "LBL_SpringCatalogDescription");
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (SPRING_AOP_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_AOP_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_AOP_2_1_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_1_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_AOP_2_1_XSD)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_1_XSD_LOCAL_URI);
        }

        if (SPRING_AOP_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_AOP_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_BEANS_DTD_PUBLIC_ID.equals(publicId)) {
            return new org.xml.sax.InputSource(SPRING_BEANS_DTD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_BEANS_DTD)) {
            return new org.xml.sax.InputSource(SPRING_BEANS_DTD_LOCAL_URI);
        }

        if (SPRING_BEANS_2_0_DTD_PUBLIC_ID.equals(publicId)) {
            return new org.xml.sax.InputSource(SPRING_BEANS_2_0_DTD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_BEANS_2_0_DTD)) {
            return new org.xml.sax.InputSource(SPRING_BEANS_2_0_DTD_LOCAL_URI);
        }

        if (SPRING_BEANS_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_BEANS_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_BEANS_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_BEANS_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_CONTEXT_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_CONTEXT_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_CONTEXT_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_CONTEXT_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_JEE_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_JEE_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_JEE_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_JEE_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_JMS_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_JMS_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_JMS_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_JMS_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_LANG_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_LANG_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_LANG_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_LANG_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_OSGI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_OSGI_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_OSGI_XSD)){
            return new org.xml.sax.InputSource(SPRING_OSGI_XSD_LOCAL_URI);
        }

        if (SPRING_TOOL_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_TOOL_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_TOOL_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_TOOL_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_TX_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TX_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_TX_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_TX_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_TX_2_1_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TX_2_1_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_TX_2_1_XSD)){
            return new org.xml.sax.InputSource(SPRING_TX_2_1_XSD_LOCAL_URI);
        }

        if (SPRING_TX_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TX_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_TX_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_TX_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_UTIL_2_0_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_UTIL_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_0_XSD_LOCAL_URI);
        }

        if (SPRING_UTIL_2_5_XSD_URI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_5_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_UTIL_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_5_XSD_LOCAL_URI);
        }

        if (SPRING_WEBFLOW_CONFIG_1_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_WEBFLOW_CONFIG_1_0_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(SPRING_WEBFLOW_CONFIG_1_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_WEBFLOW_CONFIG_1_0_XSD_LOCAL_URI);
        }

        return null;
    }

}
