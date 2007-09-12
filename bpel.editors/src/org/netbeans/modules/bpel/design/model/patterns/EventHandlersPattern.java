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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;



public class EventHandlersPattern extends CompositePattern {
    
    
    public EventHandlersPattern(DiagramModel model) {
        super(model);
    }
    
    
    protected void onAppendPattern(Pattern nestedPattern) {}
    
    
    protected void onRemovePattern(Pattern nestedPattern) {}
    
    
    public VisualElement getFirstElement() {
        return getBorder();
    }
    
    
    public VisualElement getLastElement() {
        return getBorder();
    }
    
    public boolean isDraggable() {
        return false;
    }
    
    public FBounds layoutPattern(LayoutManager manager) {
        
        float xPosition = 0;
        float height = 0;
        
        EventHandlers eventHandlers = (EventHandlers) getOMReference();
        
        OnEvent[] onEvents = eventHandlers.getOnEvents();
        OnAlarmEvent[] onAlarms = eventHandlers.getOnAlarms();
        
        for (OnEvent onEvent : onEvents) {
            Pattern p = getNestedPattern(onEvent);
            FDimension patternSize = p.getBounds().getSize();
            
            manager.setPatternPosition(p, xPosition, 0);
            
            xPosition += patternSize.width + LayoutManager.HSPACING;
            height = Math.max(height, patternSize.height);
        }
        
        for (OnAlarmEvent onAlarm : onAlarms) {
            Pattern p = getNestedPattern(onAlarm);
            FDimension patternSize = p.getBounds().getSize();
            
            manager.setPatternPosition(p, xPosition, 0);
            
            xPosition += patternSize.width + LayoutManager.HSPACING;
            height = Math.max(height, patternSize.height);
        }
        
        float width = xPosition - LayoutManager.HSPACING;
        
        height = Math.max(height, 20);
        width = Math.max(width, 20);
        
        getBorder().setClientRectangle(0, 0, width, height);
        
        return getBorder().getBounds();
    }
    
    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setLabelText(getDefaultName());
        
        EventHandlers handlers = (EventHandlers) getOMReference();
        
        OnEvent[] onEvents = handlers.getOnEvents();
        OnAlarmEvent[] onAlarms = handlers.getOnAlarms();
        
        for (OnEvent onEvent : onEvents) {
            Pattern p = getModel().createPattern(onEvent);
            p.setParent(this);
        }
        
        for (OnAlarmEvent onAlarm : onAlarms) {
            Pattern p = getModel().createPattern(onAlarm);
            p.setParent(this);
        }
    }
    
    
    public String getDefaultName() {
        return "Event Handlers"; // NOI18N
    }
    
    
    public NodeType getNodeType() {
        return NodeType.EVENT_HANDLERS;
    }
    
}
