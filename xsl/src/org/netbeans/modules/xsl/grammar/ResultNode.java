/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;

/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultNode implements Node {
    
    protected Node peer;
    
    protected String ignorePrefix;
    
    protected String onlyUsePrefix;
    
    /** Creates a new instance of ResultNode 
     * If onlyUsePrefix is non-null, the result node hirarchy will only have elements
     * with this prefix.
     * If ignorePrefix is non-null and onlyUsePrefix is null, the node hirarchy will
     * not include nodes with this prefix.
     * If both ignorePrefix and onlyUsePrefix are null, the node hirarchy will only
     * include nodes with no prefixes.
     * @peer the peer which this object contains
     * @ignorePrefix a prefix (typically ending with ":") which should be ignored in
     *      this node hirarchy
     * @onlyUsePrefix the prefix which all the nodes in the node hirarchy should have.
     */
    public ResultNode(Node peer, String ignorePrefix, String onlyUsePrefix) {
        this.peer = peer;
        this.ignorePrefix = ignorePrefix;
        this.onlyUsePrefix = onlyUsePrefix;
    }
    
    public Node appendChild(Node newChild) throws DOMException {
        return createNode(peer.appendChild(newChild));
    }
    
    public Node cloneNode(boolean deep) {
        return createNode(peer.cloneNode(deep));
    }
    
    public NamedNodeMap getAttributes() {
        return peer.getAttributes();
    }
    
    public NodeList getChildNodes() {
        return new ResultNodeList(peer.getChildNodes());
    }
    
    public Node getFirstChild() {
        NodeList childNodes = getChildNodes();
        if (childNodes.getLength() == 0) {
            return null;
        } else {
            return childNodes.item(0);
        }
    }
    
    public Node getLastChild() {
        NodeList childNodes = new ResultNodeList(peer.getChildNodes());
        if (childNodes.getLength() == 0) {
            return null;
        } else {
            return childNodes.item(childNodes.getLength()-1);
        }
    }
    
    public String getLocalName() {
        return peer.getLocalName();
    }
    
    public String getNamespaceURI() {
        return peer.getNamespaceURI();
    }
    
    public Node getNextSibling() {
        Node node = peer.getNextSibling();
        while (node != null && node.getNodeName() != null && !hasAllowedPrefix(node.getNodeName())) {
            node = node.getNextSibling();
        }
        
        if (node == null) {
            return null;
        } else {
            return createNode(node);
        }
    }
    
    public String getNodeName() {
        return peer.getNodeName();
    }
    
    public short getNodeType() {
        return peer.getNodeType();
    }
    
    public String getNodeValue() throws DOMException {
        return peer.getNodeValue();
    }
    
    public Document getOwnerDocument() {
        return peer.getOwnerDocument();
    }
    
    public Node getParentNode() {
        Node node = peer.getParentNode();
        while (node != null && node.getNodeName() != null && !hasAllowedPrefix(node.getNodeName())) {
            node = node.getParentNode();
        }
        
        if (node == null) {
            return null;
        } else {
            return createNode(node);
        }
    }
    
    public String getPrefix() {
        return peer.getPrefix();
    }
    
    public Node getPreviousSibling() {
        Node node = peer.getPreviousSibling();
        while (node != null && node.getNodeName() != null && !hasAllowedPrefix(node.getNodeName())) {
            node = node.getPreviousSibling();
        }
        
        if (node == null) {
            return null;
        } else {
            return createNode(node);
        }
    }
    
    public boolean hasAttributes() {
        return peer.hasAttributes();
    }
    
    public boolean hasChildNodes() {
        return getChildNodes().getLength() > 0;
    }
    
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return createNode(peer.insertBefore(newChild, refChild));
    }
    
    public boolean isSupported(String feature, String version) {
        return peer.isSupported(feature, version);
    }
    
    public void normalize() {
        peer.normalize();
    }
    
    public Node removeChild(Node oldChild) throws DOMException {
        return createNode(peer.removeChild(oldChild));
    }
    
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return createNode(peer.replaceChild(newChild, oldChild));
    }
    
    public void setNodeValue(String nodeValue) throws DOMException {
        peer.setNodeValue(nodeValue);
    }
    
    public void setPrefix(String prefix) throws DOMException {
        peer.setPrefix(prefix);
    }
    
    protected Node createNode(Node orig) {
        if (orig instanceof Element) {
            return new ResultElement((Element)orig, ignorePrefix, onlyUsePrefix);
        } else if (orig instanceof Document) {
            return new ResultDocument((Document)orig, ignorePrefix, onlyUsePrefix);
            
        } else if (orig instanceof Attr) {
            return new ResultAttr((Attr)orig, ignorePrefix, onlyUsePrefix);
        } else {
            return createNode(orig);            
        }
    }
    
    /**
     * Returns true if the prefix rules described in the constructor javadocs
     * are fulfilled, otherwise returns false.
     */
    protected boolean hasAllowedPrefix(String name) {
        if (onlyUsePrefix != null) {
            return name.startsWith(onlyUsePrefix);
        } else if (ignorePrefix != null){
            return !name.startsWith(ignorePrefix);
        } else {
            return name.indexOf(':') == -1;
        }
    }
    
    public class ResultNodeList implements NodeList{
        java.util.Vector nodeVector;
        
        public ResultNodeList(NodeList list) {
            nodeVector = new java.util.Vector(list.getLength());
            for (int ind = 0; ind < list.getLength(); ind++) {
                Node node = list.item(ind);
                if (node.getNodeName() != null && hasAllowedPrefix(node.getNodeName())) {
                    nodeVector.add(createNode(node));
                }
            }
        }
        
        public int getLength() {
            return nodeVector.size();
        }
        
        public Node item(int index) {
            return (Node)nodeVector.elementAt(index);
        }
    }
}
