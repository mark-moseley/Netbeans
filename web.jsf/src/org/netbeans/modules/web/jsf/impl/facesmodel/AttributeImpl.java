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
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class AttributeImpl extends IdentifiableDescriptionGroupImpl implements ConfigAttribute {

    AttributeImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    AttributeImpl( JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.ATTRIBUTE));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute#getAttributeClass()
     */
    public String getAttributeClass() {
        return getChildElementText(
                JSFConfigQNames.ATTRIBUTE_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute#getAttributeName()
     */
    public String getAttributeName() {
        return getChildElementText(
                JSFConfigQNames.ATTRIBUTE_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute#setAttributeClass(java.lang.String)
     */
    public void setAttributeClass( String clazz ) {
        setChildElementText( ATTRIBUTE_CLASS, clazz, 
                JSFConfigQNames.ATTRIBUTE_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute#setAttributeName(java.lang.String)
     */
    public void setAttributeName( String name ) {
        setChildElementText( ATTRIBUTE_NAME, name, 
                JSFConfigQNames.ATTRIBUTE_NAME.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(5);
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( ATTRIBUTE_NAME);
        SORTED_ELEMENTS.add( ATTRIBUTE_CLASS );  
    }

}
