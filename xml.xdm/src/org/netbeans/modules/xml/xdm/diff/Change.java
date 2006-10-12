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

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * This class represents node change between 2 DOM tree
 *
 * @author Ayub Khan
 */
public class Change extends Difference {
    
    public enum Type {
        TOKEN("token"),
        ATTRIBUTE("attribute"),
        POSITION("position"),
        UNKNOWN("unknown"),
        NO_CHANGE("noChange");
        
        String name;
        Type(String name) {
            this.name = name;
        }
    }
    
    /** Creates a new instance of DiffEvent */
    public Change(NodeInfo.NodeType nodeType,
            List<Node> ancestors1, List<Node> ancestors2,
            Node n1, Node n2, int n1Pos, int n2Pos, List<Type> changes) {
        super(nodeType, ancestors1, ancestors2, n1, n2, n1Pos, n2Pos);
        if (ancestors2.size() > 0) {
            assert ancestors2.get(0).getIndexOfChild(n2) > -1;
        }
        assert changes != null && !changes.isEmpty();
        this.changes = changes;
        
        if(changes.contains(Type.ATTRIBUTE)) {
            if ( n1 instanceof Element ) {//find attr changes and add them
                NamedNodeMap nm1 = n1.getAttributes();
                NamedNodeMap nm2 = n2.getAttributes();
                HashMap<Node, Integer> posMap = new HashMap<Node, Integer>();
                List<String> allAttrNames = new ArrayList<String>();
                for ( int i=0; i < nm1.getLength(); i++ ) {
                    Node oldAttr = (Node) nm1.item(i);
                    String name = oldAttr.getNodeName();
                    if ( !allAttrNames.contains( name ) )
                        allAttrNames.add( name );
                    posMap.put(oldAttr, new Integer(i));
                }
                for ( int i=0; i < nm2.getLength(); i++ ) {
                    Node newAttr = (Node) nm2.item(i);
                    String name = newAttr.getNodeName();
                    if ( !allAttrNames.contains( name ) )
                        allAttrNames.add( name );
                    posMap.put(newAttr, new Integer(i));
                }
                for ( int i=0; i < allAttrNames.size(); i++ ) {
                    String attrName = allAttrNames.get( i );
                    Node oldAttr = (Node) nm1.getNamedItem(attrName);
                    Node currAttr = (Node) nm2.getNamedItem(attrName);
                    int oldAttrPos = oldAttr!=null?posMap.get(oldAttr).intValue():-1;
                    int newAttrPos = currAttr!=null?posMap.get(currAttr).intValue():-1;
                    if ( oldAttr != null ) {
                        if ( currAttr == null ) {
                            AttributeDelete delete = 
                                new AttributeDelete((Attribute) oldAttr, oldAttrPos);
                            addAttrChanges(delete);
                        } else {
                            boolean tokenChange = DiffFinder.checkChange(
                                    oldAttr, currAttr).size() > 0;
                            boolean posChange = oldAttrPos != newAttrPos;
                            if(tokenChange || posChange) {
                                AttributeChange change = 
                                new AttributeChange((Attribute) oldAttr,
                                    (Attribute) currAttr, oldAttrPos, newAttrPos, 
                                        tokenChange, posChange);
                                addAttrChanges(change);
                            }
                        }
                    } else if ( currAttr != null ) {
                        AttributeAdd add = 
                                new AttributeAdd((Attribute) currAttr, newAttrPos);
                            addAttrChanges(add);
                    }
                }
            }
        }
    }
    
    public List<AttributeDiff> getAttrChanges() {
        return attrChanges;
    }
    
    public void addAttrChanges(AttributeDiff attrDif) {
        attrChanges.add(attrDif);
    }
    
    public void removeAttrChanges(AttributeDiff attrDif) {
        attrChanges.remove(attrDif);
    }    
    
    public boolean isTokenChanged() {
        return changes.contains(Type.TOKEN);
    }
    
    /**
     * @return true if attribute list changes or attribute tokens change.
     */
    public boolean isAttributeChanged() {
        return changes.contains(Type.ATTRIBUTE);
    }
    
    public boolean isPositionChanged() {
        return changes.contains(Type.POSITION);
    }
    
    void setPositionChanged(boolean posChange) {
        if(posChange && !changes.contains(Type.POSITION))
            changes.add(Type.POSITION);
        else if(!posChange && changes.contains(Type.POSITION))
            changes.remove(Type.POSITION);
    }
    
    public void setNewParent(Node parent2) {
        if (parent2 == null) return;
        assert parent2.getId() == getOldNodeInfo().getParent().getId();
        getOldNodeInfo().setNewParent(parent2);
        getNewNodeInfo().setNewParent(parent2);
    }
    
    public Node getNewParent() {
        return getNewNodeInfo().getNewParent();
    }
    
    public List<Node> getNewAncestors() {
        return getNewNodeInfo().getNewAncestors();
    }
    
    public boolean isValid() {
        return !changes.isEmpty();
    }
    
    public String toString() {
        return "CHANGE"+changes+"("+ getOldNodeInfo() + "," + getNewNodeInfo() + ")";
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private List<Type> changes;
    private List<AttributeDiff> attrChanges = new ArrayList<AttributeDiff>();
        
    public class AttributeAdd extends AttributeDiff {

        public AttributeAdd(Attribute newAttr, int newAttrPos) {
            super(null, newAttr, -1, newAttrPos);
        }      
    } 
    
    public class AttributeDelete extends AttributeDiff {

        public AttributeDelete(Attribute oldAttr, int oldAttrPos) {
            super(oldAttr, null, oldAttrPos, -1);
        }
    }
    
    public class AttributeChange extends AttributeDiff {

        private boolean posChanged;

        private boolean tokenChanged;
        
        public AttributeChange(Attribute oldAttr, Attribute newAttr, 
                int oldAttrPos, int newAttrPos, boolean tokenChanged, 
                boolean posChanged) {
            super(oldAttr, newAttr, oldAttrPos, newAttrPos);
            this.tokenChanged = tokenChanged;
            this.posChanged = posChanged;
        }
                
        public boolean isTokenChanged() {
            return tokenChanged;
        }

        public boolean isPositionChanged() {
            return posChanged;
        }        
    }    
    
    public class AttributeDiff {

        private Attribute oldAttr;

        private Attribute newAttr;

        private int oldAttrPos;

        private int newAttrPos;
        
        public AttributeDiff(Attribute oldAttr, Attribute newAttr, 
                int oldAttrPos, int newAttrPos) {
            this.oldAttr = oldAttr;
            this.newAttr = newAttr;
            this.oldAttrPos = oldAttrPos;
            this.newAttrPos = newAttrPos;
        }
        
        public Attribute getOldAttribute() {
            return oldAttr;
        }
        
        public Attribute getNewAttribute() {
            return newAttr;
        }
        
        public int getOldAttributePosition() {
            return oldAttrPos;
        }
        
        public int getNewAttributePosition() {
            return newAttrPos;
        }        
    }
    
}
