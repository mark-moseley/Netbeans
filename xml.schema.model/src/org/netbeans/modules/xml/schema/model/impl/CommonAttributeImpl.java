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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public abstract class CommonAttributeImpl extends NamedImpl
    implements Attribute {
    
    /** 
     * Creates a new instance of CommonAttributeImpl 
     */
    public CommonAttributeImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
 
    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case FIXED:
                return String.class;
            default:
                return super.getAttributeType(attr);
        }
    }
    
    /**
     *
     */
    public String getFixed() {
        return getAttribute(SchemaAttributes.FIXED);
    }
    
    /**
     *
     */
    public void setFixed(String fixedValue) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, fixedValue);
    }

    /**
     *
     */
    public String getDefault() {
        return getAttribute(SchemaAttributes.DEFAULT);
    }
    
    /**
     *
     */
    public void setDefault(String defaultValue) {
        setAttribute(DEFAULT_PROPERTY, SchemaAttributes.DEFAULT, defaultValue);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalSimpleType> getType() { 
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.TYPE);
    }
    
    /**
     *
     */
    public void setType(NamedComponentReference<GlobalSimpleType> type) {
        setAttribute(TYPE_PROPERTY, SchemaAttributes.TYPE, type);
    }

    /**
     *
     */
    public LocalSimpleType getInlineType() {
        java.util.Collection<LocalSimpleType> types = getChildren(LocalSimpleType.class);        
        if(!types.isEmpty()){
            return types.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public void setInlineType(LocalSimpleType type) {
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalSimpleType.class, INLINE_TYPE_PROPERTY, type, classes);
    }
 
}
