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
package org.netbeans.modules.spring.beans.hyperlink;

import javax.swing.text.Document;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.DocumentContext;
import org.netbeans.modules.spring.beans.editor.EditorContextFactory;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class HyperlinkEnv {

    private Document document;
    private SyntaxElement currentTag;
    private String attribName;
    private String valueString;
    private int offset;
    private TokenItem token;
    private DocumentContext documentContext;

    public static enum Type {

        ATTRIB_VALUE,
        ATTRIB,
        TEXT,
        NONE;

        public boolean isValueHyperlink() {
            return this == Type.ATTRIB_VALUE;
        }

        public boolean isAttributeHyperlink() {
            return this == Type.ATTRIB;
        }
    };

    private Type type = Type.NONE;
                  
    public HyperlinkEnv(Document document, int offset) {
        this.document = document;
        this.offset = offset;
        this.documentContext = EditorContextFactory.getDocumentContext(document, offset);
        if(documentContext.isValid()) {
            currentTag = documentContext.getCurrentElement();
            attribName = ContextUtilities.getAttributeTokenImage(documentContext);
            token = documentContext.getCurrentToken();
            
            if (ContextUtilities.isValueToken(documentContext.getCurrentToken())) {
                type = Type.ATTRIB_VALUE;
                currentTag = (Tag) documentContext.getCurrentElement();
                attribName = ContextUtilities.getAttributeTokenImage(documentContext);
                token = documentContext.getCurrentToken();
                valueString = token.getImage();
                valueString = valueString.substring(1, valueString.length() - 1); // Strip quotes
            } else if (ContextUtilities.isAttributeToken(documentContext.getCurrentToken())) {
                type = Type.ATTRIB;
                currentTag = (Tag) documentContext.getCurrentElement();
                token = documentContext.getCurrentToken();
                attribName = token.getImage();
            }
        }
    }

    public String getAttribName() {
        return attribName;
    }

    public Tag getCurrentTag() {
        return currentTag instanceof Tag ? (Tag) currentTag : null;
    }

    public Document getDocument() {
        return document;
    }

    public String getTagName() {
        return getCurrentTag() != null ? getCurrentTag().getTagName() : null;
    }

    public String getValueString() {
        return valueString;
    }

    public Type getType() {
        return type;
    }

    public TokenItem getToken() {
        return token;
    }

    public int getOffset() {
        return offset;
    }
    
    public DocumentContext getDocumentContext() {
        return this.documentContext;
    }
    
    public FileObject getFile() {
        return NbEditorUtilities.getFileObject(document);
    }
}
