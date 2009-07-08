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


package org.netbeans.modules.web.jsf;


import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 *
 * @author  Petr Pisl
 */
public class JSFCatalog implements CatalogReader, CatalogDescriptor, org.xml.sax.EntityResolver {

    private static final String JSF_ID_1_0 = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN"; // NOI18N
    private static final String JSF_ID_1_1 = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN"; // NOI18N
    
    private static final String URL_JSF_1_0 ="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_0.dtd"; // NOI18N
    private static final String URL_JSF_1_1 ="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_1.dtd"; // NOI18N
    
    public static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee";  // NOI18N
    private static final String JSF_1_2_XSD="web-facesconfig_1_2.xsd"; // NOI18N
    private static final String JSF_2_0_XSD="web-facesconfig_2_0.xsd"; // NOI18N
    private static final String JSF_1_2=JAVAEE_NS+"/"+JSF_1_2_XSD; // NOI18N
    private static final String JSF_2_0=JAVAEE_NS+"/"+JSF_2_0_XSD; // NOI18N
    public static final String JSF_ID_1_2="SCHEMA:"+JSF_1_2; // NOI18N
    public static final String JSF_ID_2_0="SCHEMA:"+JSF_2_0; // NOI18N
    private static final String URL_JSF_1_2="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_2.xsd"; // NOI18N
    private static final String URL_JSF_2_0="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_2_0.xsd"; // NOI18N
    
    /** Creates a new instance of StrutsCatalog */
    public JSFCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        java.util.List list = new java.util.ArrayList();
        list.add(JSF_ID_1_0);
        list.add(JSF_ID_1_1);
        list.add(JSF_ID_1_2);
        list.add(JSF_ID_2_0);
        return list.listIterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (JSF_ID_1_0.equals(publicId))
            return URL_JSF_1_0;
        else if (JSF_ID_1_1.equals(publicId))
            return URL_JSF_1_1;
        else if (JSF_ID_1_2.equals(publicId))
            return URL_JSF_1_2;
        else if (JSF_ID_2_0.equals(publicId))
            return URL_JSF_2_0;
        else return null;
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh() {
    }
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
    }
    
    /** Registers new listener.  */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
     /** Unregister the listener.  */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage (JSFCatalog.class, "LBL_JSFCatalog");
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public java.awt.Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/web/jsf/resources/JSFCatalog.png"); // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage (JSFCatalog.class, "DESC_JSFCatalog");
    }
    
   /**
     * Resolves schema definition file for taglib descriptor (spec.1_1, 1_2, 2_0)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publisId, 
     */    
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
       if (JSF_ID_1_0.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_0);
        } else if (JSF_ID_1_1.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_1);
        } else if (JSF_1_2.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_2);
        } else if (JSF_2_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_2_0);
        } else if (systemId!=null && systemId.endsWith(JSF_1_2_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_1_2);    
        } else if (systemId!=null && systemId.endsWith(JSF_2_0_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_2_0);
        } else {
            return null;
        }
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */ 
    public String resolvePublic(String publicId) {
        return null;
    }
    
    public static JSFVersion extractVersion(Document document) {
        // first check the doc type to see if there is one
        DocumentType dt = document.getDoctype();
        JSFVersion value = JSFVersion.JSF_1_0;
        // This is the default version
        if (dt != null) {
            if (JSF_ID_1_0.equals(dt.getPublicId())) {
                value = JSFVersion.JSF_1_0;
            }
            if (JSF_ID_1_1.equals(dt.getPublicId())) {
                value = JSFVersion.JSF_1_1;
            }
            if (JSF_ID_1_2.equals(dt.getPublicId())) {
                value = JSFVersion.JSF_1_2;
            }
            if (JSF_ID_2_0.equals(dt.getPublicId())) {
                value = JSFVersion.JSF_2_0;
            }
        }
        return value;

    }
    
}
