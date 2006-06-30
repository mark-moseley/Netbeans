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

/*
 * FlushVisitor.java
 *
 * Created on August 15, 2005, 10:09 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.nodes.TokenType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Srividhya Narayanan
 */
public class FlushVisitor extends ChildVisitor {
    
    public String flushModel(org.netbeans.modules.xml.xdm.nodes.Document root) {
        buffer = new StringBuilder();
        root.accept(this);
        return buffer.toString();
    }
    
    public void visit(Element e) {
        java.util.ListIterator<Token> tokensIter = e.getTokens().listIterator();
        while(tokensIter.hasNext()) {
            Token token = tokensIter.next();
            buffer.append(token.getValue());
            if(token.getType()==TokenType.TOKEN_ELEMENT_START_TAG) break;
        }

        
        if(e.hasAttributes()) {
            NamedNodeMap attributes = e.getAttributes();
            for (int i =0; i<attributes.getLength(); i++) {
                Node l = (Node)attributes.item(i);
                l.accept(this);
            }
        }

        while (tokensIter.hasNext()) {
            Token token = tokensIter.next();
            buffer.append(token.getValue());
            if(token.getType()==TokenType.TOKEN_ELEMENT_END_TAG) break;
        }
        
        if(e.hasChildNodes()) {
            NodeList children = e.getChildNodes();
            for (int i =0; i<children.getLength(); i++) {
                Node l = (Node)children.item(i);
                if (l instanceof Attribute) {
                    //
                } else {
                    l.accept(this);
                }
            }
        }

        while(tokensIter.hasNext()) {
            buffer.append(tokensIter.next().getValue());
        }
    }
    
    protected void visitNode(Node node) {
        List<Token> tokens = ((NodeImpl)node).getTokens();
        for (Token token :tokens)
            buffer.append(token.getValue());
        super.visitNode(node);
    }
    
    private StringBuilder buffer;
}
