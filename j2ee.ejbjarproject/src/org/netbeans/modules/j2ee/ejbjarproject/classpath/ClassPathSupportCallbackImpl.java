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

package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport.Item;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hrebejk
 * @author Andrei Badea
 */
public class ClassPathSupportCallbackImpl implements ClassPathSupport.Callback {
    
    public final static String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N
    
    private static String[] ejbjarElemOrder = new String[] { "name", "minimum-ant-version", "explicit-platform", "use-manifest", "included-library", "web-services", "source-roots", "test-roots" }; //NOI18N
    
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    public static final String INCLUDE_IN_DEPLOYMENT = "includeInDeployment";
    
    private AntProjectHelper helper;

    public ClassPathSupportCallbackImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    /** 
     * Returns a list with the classpath items which are to be included 
     * in deployment.
     */
    private static List<String> getIncludedLibraries( AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List<String> libraries = new ArrayList<String>(libs.getLength());
        for ( int i = 0; i < libs.getLength(); i++ ) {
            Element item = (Element)libs.item( i );
            // ejbjar is different from other j2ee projects - it stores reference without ${ and }
            libraries.add( "${"+findText( item )+"}"); // NOI18N
        }
        return libraries;
    }
    
    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private static void putIncludedLibraries(List<ClassPathSupport.Item> classpath, AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        while ( libs.getLength() > 0 ) {
            Node n = libs.item( 0 );
            n.getParentNode().removeChild( n );
        }

        Document doc = data.getOwnerDocument();
        for (ClassPathSupport.Item item : classpath) {
            if("true".equals(item.getAdditionalProperty(INCLUDE_IN_DEPLOYMENT))) { // NOI18N
                appendChildElement(data, 
                    createLibraryElement(antProjectHelper, doc, item, includedLibrariesElement), 
                    ejbjarElemOrder);
            }
        }
        
        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    /**
     * Find all direct child elements of an element.
     * More useful than {@link Element#getElementsByTagNameNS} because it does
     * not recurse into recursive child elements.
     * Children which are all-whitespace text nodes are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     */
    private static List<Element> findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // skip
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }
    
    /**
     * Append child element to the correct position according to given
     * order.
     * @param parent parent to which the child will be added
     * @param el element to be added
     * @param order order of the elements which must be followed
     */
    private static void appendChildElement(Element parent, Element el, String[] order) {
        Element insertBefore = null;
        List l = Arrays.asList(order);
        int index = l.indexOf(el.getLocalName());
        assert index != -1 : el.getLocalName()+" was not found in "+l; // NOI18N
        Iterator it = findSubElements(parent).iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            int index2 = l.indexOf(e.getLocalName());
            assert index2 != -1 : e.getLocalName()+" was not found in "+l; // NOI18N
            if (index2 > index) {
                insertBefore = e;
                break;
            }
        }
        parent.insertBefore(el, insertBefore);
    }
        
    private static Element createLibraryElement(AntProjectHelper antProjectHelper, Document doc, Item item, String includedLibrariesElement) {
        Element libraryElement = doc.createElementNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        // ejbjar is different from other j2ee projects - it stores reference without ${ and }
        libraryElement.appendChild( doc.createTextNode( CommonProjectUtils.getAntPropertyName(item.getReference()) ) );
        return libraryElement;
    }
       
    /**
     * Extracts <b>the first</b> nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    private static String findText( Element parent ) {
        NodeList l = parent.getChildNodes();
        for ( int i = 0; i < l.getLength(); i++ ) {
            if ( l.item(i).getNodeType() == Node.TEXT_NODE ) {
                Text text = (Text)l.item( i );
                return text.getNodeValue();
            }
        }
        return null;
    }
        

    public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
        List<String> l = getIncludedLibraries(helper, projectXMLElement);
        for (Item item : items) {
            item.setAdditionalProperty(INCLUDE_IN_DEPLOYMENT, Boolean.toString(l.contains(item.getReference())));
        }
    }

    public void storeAdditionalProperties(List<Item> items, String projectXMLElement) {
        putIncludedLibraries(items, helper, projectXMLElement);
    }

}

