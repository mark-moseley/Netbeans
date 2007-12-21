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


package org.netbeans.modules.bpel.design.model.patterns;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

/**
 *
 * @author Alexey Yarmolenko
 */
public class EmptyPattern extends BasicActivityPattern{

    /**
     * Creates a new instance of BasicActivityPattern.
     */
    public EmptyPattern(DiagramModel model) {
        super(model);
    }
    
    
    protected void createElementsImpl() {
        VisualElement element = ContentElement.createEmpty();
        appendElement(element);
        registerTextElement(element);
    }
     

    public String getDefaultName() {
        return "Empty"; // NOI18N
    }         

    public NodeType getNodeType() {
        return NodeType.EMPTY;
    }
    
}
