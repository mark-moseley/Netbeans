/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.bpel.design.model.patterns;

import java.util.Collection;
import org.netbeans.modules.bpel.design.geom.FDimension;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.TerminationHandler;


public class TerminationHandlerPattern extends CompositePattern {
    
    private PlaceHolderElement placeHolder;
    
    public TerminationHandlerPattern(DiagramModel model) {
        super(model);
        placeHolder = new PlaceHolderElement();
    }


    public VisualElement getFirstElement() {
        Pattern nestedPattern = getNestedPattern();
        
        if (nestedPattern == null){
            return placeHolder;
        }
        return nestedPattern.getFirstElement();
    }
    
    
    public VisualElement getLastElement() {
        return null;
    }
    

    public void onAppendPattern(Pattern p) {
        removeElement(placeHolder);
    }
    
    
    public void onRemovePattern(Pattern p) {
        appendElement(placeHolder);
    }
    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setLabelText(getDefaultName());
        
        placeHolder = new PlaceHolderElement();
        
        appendElement(placeHolder);
        
        BpelEntity a = ((TerminationHandler) getOMReference()).getActivity();
        
        if (a != null) {
            Pattern p = getModel().createPattern(a);
            p.setParent(this);
        }
        
    }


    public FRectangle layoutPattern(LayoutManager manager) {
        Collection<Pattern> patterns = super.getNestedPatterns();

        FDimension s; 
        
        if (patterns.isEmpty()){
            placeHolder.setLocation(0, 0);
            s = new FDimension(placeHolder.getWidth(), placeHolder.getHeight());
        } else {
            Pattern p = getNestedPattern();
            manager.setPatternPosition(p, 0, 0);
            s = p.getBounds().getSize();
        }
        
        getBorder().setClientRectangle(0, 0, s.width, s.height);
        return getBorder().getBounds();
        

    }


    public String getDefaultName() {
        return "Termination Handler"; // NOI18N
    }  
    
    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders) 
    {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }

    
    public NodeType getNodeType() {
        return NodeType.TERMINATION_HANDLER;
    }
    
    
    private class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(TerminationHandlerPattern.this, draggedPattern, 
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            Pattern p = getDraggedPattern();
            ((TerminationHandler) getOMReference()) 
                    .setActivity((Activity) p.getOMReference());
        }
    }    
    
}
