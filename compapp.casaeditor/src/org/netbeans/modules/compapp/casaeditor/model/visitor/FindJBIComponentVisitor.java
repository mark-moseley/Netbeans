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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.casaeditor.model.visitor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author ajit
 */
public class FindJBIComponentVisitor extends JBIVisitor.Deep {
    
    /** Creates a new instance of FindJBIComponentVisitor */
    public FindJBIComponentVisitor() {
    }
    
    public static <T extends JBIComponent> T findComponent(Class<T> type, JBIComponent root, String xpath) {
        JBIComponent ret = new FindJBIComponentVisitor().findComponent(root, xpath);
        if (ret == null) {
            return null;
        } else {
            return type.cast(ret);
        }
    }
    
    public JBIComponent findComponent(JBIComponent root, Element xmlNode) {
        assert xmlNode != null;
        
        this.xmlNode = xmlNode;
        result = null;
        root.accept(this);
        return result;
    }
    
    public JBIComponent findComponent(JBIComponent root, String xpath) {
        Document doc = (Document) root.getModel().getDocument();
        if (doc == null) {
            return null;
        }
        
        // Temporary workaround to get around XDM XPath limitation
        Node result = null;
        if (xpath.startsWith("/jbi/service-assembly/connections/connection[")) {                    // NOI18N
            List<Node> connectionNodes = root.getModel().getAccess().findNodes(doc,
                    "/jbi/service-assembly/connections/connection");                                // NOI18N
            
            Pattern pattern = Pattern.compile("consumer\\[@endpoint-name='(.*?)'\\] and provider\\[@endpoint-name='(.*?)'\\]"); // NOI18N
            Matcher matcher = pattern.matcher(xpath);            
            assert matcher.find() && matcher.groupCount() == 2;
            
            String consumerEndpointName = matcher.group(1);
            String providerEndpointName = matcher.group(2);
            
            result = findConnection(connectionNodes, consumerEndpointName, providerEndpointName);
        } else if (xpath.startsWith("/jbi/service-assembly/service-unit[")) {   // NOI18N
            List<Node> suNodes = root.getModel().getAccess().findNodes(doc,
                    "/jbi/service-assembly/service-unit");                      // NOI18N
            
            int index1 = xpath.indexOf("'");                                    // NOI18N
            int index2 = xpath.lastIndexOf("'");                                // NOI18N
            String componentTargetName = xpath.substring(index1+1, index2);
            
            result = findServiceUnit(suNodes, componentTargetName);
        } else {
            result = root.getModel().getAccess().findNode(doc, xpath);
        }
        
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }
    
    protected void visitComponent(JBIComponent component) {
        if (result != null) return;
        if (component.referencesSameNode(xmlNode)) {
            result = component;
            return;
        } else {
            super.visitChild(component);
        }
    }
    
    private Element findConnection(List<Node> connectionNodes,
            String consumerEndpointName, String providerEndpointName) {
        for (Node connectionNode : connectionNodes) {
            Element connection = (Element) connectionNode;
            Element consumer = (Element) connection.getElementsByTagName("consumer").item(0);       // NOI18N
            Element provider = (Element) connection.getElementsByTagName("provider").item(0);       // NOI18N
            if (consumer.getAttribute("endpoint-name").equals(consumerEndpointName) &&              // NOI18N        
                    provider.getAttribute("endpoint-name").equals(providerEndpointName)) {          // NOI18N
                return connection;
            }
        }
        
        return null;
    }
    
    private Element findServiceUnit(List<Node> suNodes,
            String componentTargetName) {
        for (Node suNode : suNodes) {
            Element su = (Element) suNode;
            Element target = (Element) su.getElementsByTagName("target").item(0);                       // NOI18N
            Element componentName = (Element) target.getElementsByTagName("component-name").item(0);    // NOI18N
            String name = componentName.getFirstChild() != null ? componentName.getFirstChild().getNodeValue() : Constants.EMPTY_STRING; // FIXME
            if (componentTargetName.equals(name)) {
                return su;
            }
        }
        
        return null;
    }
    
    private JBIComponent result;
    private Element xmlNode;
}
