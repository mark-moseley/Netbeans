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

package org.netbeans.modules.xml.xpath;

/**
 * Represents a node test on type.
 * 
 * @author Enrico Lelina
 * @version 
 */
public class StepNodeTypeTest extends StepNodeTest {

    /** The node type. */
    private int mNodeType;
    
    
    /**
     * Constructor.
     * @param nodeType the node type
     */
    public StepNodeTypeTest(int nodeType) {
        super();
        mNodeType = nodeType;
    }
    
    
    /**
     * Gets the node type.
     * @return the node type
     */
    public int getNodeType() {
        return mNodeType;
    }
    
    
    /**
     * Gets the display string.
     * @return the display string or null if invalid
     */
    public String getNodeTypeString() {
        int nodeType = getNodeType();
        
        switch (nodeType) {
        case LocationStep.NODETYPE_NODE:
            return "node()";
        case LocationStep.NODETYPE_TEXT:
            return "text()";
        case LocationStep.NODETYPE_COMMENT:
            return "comment()";
        case LocationStep.NODETYPE_PI:
            return "processing-instruction()";
        }
        
        return null;
    }

    public String toString() {
        return getNodeTypeString();
    }
}
