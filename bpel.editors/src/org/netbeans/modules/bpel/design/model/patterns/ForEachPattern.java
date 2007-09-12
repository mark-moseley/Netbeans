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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.bpel.design.model.patterns;

import java.awt.geom.Area;
import java.util.Collection;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.SubprocessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;


public class ForEachPattern extends CompositePattern {
  
    private VisualElement gateway;
    private PlaceHolderElement placeHolder;

    private Connection connection1;
    private Connection connection2;
    
    public ForEachPattern(DiagramModel model) {
        super(model);
    }
    
    
    public VisualElement getFirstElement() {
        return getBorder();
    }
    
    
    public VisualElement getLastElement() {
        return getBorder();
    }
    
    public Pattern getActivityPattern(){
        Scope scope = ((ForEach) getOMReference()).getScope();
        return (scope == null) ? null : getNestedPattern(scope);
    }
    
    
    
    protected void createElementsImpl() {
        connection1 = new Connection(this);
        connection2 = new Connection(this);
        
        setBorder(new SubprocessBorder());
        getBorder().setLabelText(getDefaultName());
        registerTextElement(getBorder());
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        gateway = ContentElement.createForEachGateway();
        appendElement(gateway);
        
        ForEach forEachOM = (ForEach) getOMReference();
        
        Scope scope = (Scope) forEachOM.getScope();
        
        if (scope != null) {
            Pattern p = getModel().createPattern(scope);
            p.setParent(this);
        }
    }
    
    
    public void onAppendPattern(Pattern p) {
        removeElement(placeHolder);
    }
    
    
    public void onRemovePattern(Pattern p) {
        appendElement(placeHolder);
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        double width = 0;
        double height = 0;
        
        if (getNestedPatterns().isEmpty()) {
            placeHolder.setLocation(0, 0);
            
            width = placeHolder.getWidth();
            height = placeHolder.getHeight();
            
        } else {
            Pattern p = getNestedPattern();
            FBounds pBounds = p.getBounds();
            manager.setPatternPosition(p, 0, 0);
            
            width += pBounds.width;
            height += pBounds.height;
        }
        
        double gx = -gateway.getWidth() - LayoutManager.HSPACING;
        double gy = height / 2 - gateway.getHeight() / 2;

        gateway.setLocation(gx, gy);
        
        double x0 = gx;
        double y0 = Math.min(gy, 0);
        
        double totalWidth = width - gx;
        double totalHeight = Math.max(height, gateway.getHeight());
        
        setOrigin(x0 + totalWidth / 2, y0 + totalHeight / 2);

        getBorder().setClientRectangle(x0, y0 - Connection.BRACKET_SIZE, 
                totalWidth, totalHeight + 2 * Connection.BRACKET_SIZE);
        
        return getBorder().getBounds();
    }
    
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (draggedPattern == this) return;
        if (isNestedIn(draggedPattern)) return;
        if (!(draggedPattern.getOMReference() instanceof ExtendableActivity)) return;
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    public String getDefaultName() {
        return "ForEach"; // NOI18N
    }
    
    
    public void reconnectElements() {
        Pattern p = getNestedPattern();

        if (p != null) {
            connection1.connect(gateway, Direction.TOP, 
                    p.getFirstElement(), Direction.TOP);
            
            connection2.connect(p.getLastElement(), Direction.BOTTOM, 
                    gateway, Direction.BOTTOM);
        } else {
            connection1.connect(gateway, Direction.TOP, 
                    placeHolder, Direction.TOP);
            
            connection2.connect(placeHolder, Direction.BOTTOM, 
                    gateway, Direction.BOTTOM);
        }
    }
    
    
    public NodeType getNodeType() {
        return NodeType.FOR_EACH;
    }
    
    
    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(gateway.getShape()));
        return res;
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ForEachPattern.this, draggedPattern, placeHolder.getCenterX(),
                    placeHolder.getCenterY());
        }
        
        public void drop() {
            
            if (getDraggedPattern().getOMReference() instanceof Scope) {
                ((ForEach) getOMReference()).setScope(
                        (Scope) getDraggedPattern().getOMReference());
            } else {
                Scope scope = getOwnerPattern().getBpelModel().getBuilder()
                        .createScope();
                scope.setActivity((ExtendableActivity) getDraggedPattern()
                        .getOMReference());
                ((ForEach) getOMReference()).setScope(scope);
            }
        }
    }
}
