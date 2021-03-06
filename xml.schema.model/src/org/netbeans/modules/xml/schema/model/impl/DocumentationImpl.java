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

import java.io.IOException;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public class DocumentationImpl extends SchemaComponentImpl implements Documentation {
	
        public DocumentationImpl(SchemaModelImpl model) {
            this(model,createNewComponent(SchemaElements.DOCUMENTATION,model));
        }
    
	/**
	 * Creates a new instance of DocumentationImpl
	 */
	public DocumentationImpl(SchemaModelImpl model, Element el) {
		super(model, el);
	}

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Documentation.class;
	}
	
	/**
	 *
	 */
	public void setLanguage(String lang) {
		setAttribute(LANGUAGE_PROPERTY, SchemaAttributes.LANGUAGE, lang);
	}
	
	/**
	 *
	 */
	public void accept(SchemaVisitor visitor) {
		visitor.visit(this);
	}
	
	/**
	 *
	 */
	public void setSource(String uri) {
		setAttribute(SOURCE_PROPERTY, SchemaAttributes.SOURCE, uri);
	}
	
	/**
	 *
	 */
	public String getSource() {
		return getAttribute(SchemaAttributes.SOURCE);
	}
	
	/**
	 *
	 */
	public String getLanguage() {
		return getAttribute(SchemaAttributes.LANGUAGE);
	}
	
	public void setDocumentationElement(Element content) {
            super.updatePeer(CONTENT_PROPERTY, content);
	}
	
	public Element getDocumentationElement() {
            return Element.class.cast(getPeer().cloneNode(true));
	}
        
	public void setContent(String content) {
            setText(CONTENT_PROPERTY, content);
	}

	public String getContent() {
            return getText();
	}

    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_PROPERTY, text);
    }

    public String getContentFragment() {
        return super.getXmlFragment();
    }
}
