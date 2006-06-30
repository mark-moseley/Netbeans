/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.syntax.dom;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * It should envolve in DocumentType implementation.
 *
 * @author Petr Kuzel
 */
public class DocumentTypeImpl extends SyntaxNode implements DocumentType, XMLTokenIDs {

    public DocumentTypeImpl(XMLSyntaxSupport syntax, TokenItem first, int to) {
        super (syntax, first, to);
    }

    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }
        
    public String getPublicId() {
        String doctype = first.getImage();
        if (doctype.indexOf("PUBLIC") != -1) {                                  // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                String publicId = next.getImage();
                return publicId.substring(1, publicId.length() - 1);
            }
        }
        return null;
    }
    
    public org.w3c.dom.NamedNodeMap getNotations() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getName() {
        //<!DOCTYPE id ...
        String docType = first.getImage();
        int idIndex = docType.indexOf(' ');
        if(idIndex > 0) {
            int idEndIndex = docType.indexOf(' ', idIndex + 1);
            if(idEndIndex > 0 && idEndIndex > idIndex) {
                return docType.substring(idIndex + 1, idEndIndex);
            }
        }
        return null;
    }
    
    public org.w3c.dom.NamedNodeMap getEntities() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getSystemId() {
        String doctype = first.getImage();
        if (doctype.indexOf("PUBLIC") != -1) {                                  // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                next = next.getNext();
                if (next == null) return null;
                next = next.getNext();
                if (next != null && next.getTokenID() == VALUE) {
                    String system = next.getImage();
                    return system.substring(1, system.length() -1);
                }
            }
        } else if (doctype.indexOf("SYSTEM") != -1) {                           // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                String system = next.getImage();
                return system.substring(1, system.length() - 1);
            }
        }        
        return null;
    }
    
    public String getInternalSubset() {
        return null;
    }
    
}

