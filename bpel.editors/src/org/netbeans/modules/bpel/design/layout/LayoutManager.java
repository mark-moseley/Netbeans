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

package org.netbeans.modules.bpel.design.layout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

public class LayoutManager {
    
    /**
     * Coordinates of pattern origins
     * Can be used to align patters along some axis
     */
    private Map<Pattern, FPoint> patternOrigins
            = new HashMap<Pattern, FPoint>();
 
    
    private DesignView designView;
    
    
    public LayoutManager(DesignView designView) {
        this.designView = designView;
    }
    
    /**
     * Perform layout for whole diagram starting from root pattern
     * Positioning is performed relatice to specified coordinates
     **/
    
    public void layout(Pattern pattern, float x, float y) {
        if (pattern != null) {
            /**
             * Pass ONE: each pattern should position it's elements and nested patterns.
             * Starting from LEAF patterns
             **/
            positionElements(pattern);
            
            setPatternPosition(pattern, x, y);
        }
    }
    
    /**
     * Perform layout for whole diagram starting from root pattern
     * Positioning is performed relative to 0,0
     **/
    public void layout() {
        Pattern root = designView.getModel().getRootPattern();
        layout(root, HMARGIN, VMARGIN);
    }
    
    
    
 
    
    private void positionElements(Pattern pattern) {
        
        boolean composite = pattern instanceof CompositePattern;
        
        if (composite) {
            CompositePattern cPattern = (CompositePattern) pattern;
            
            for (Pattern nestedPattern : cPattern.getNestedPatterns()) {
                positionElements(nestedPattern);
            }
        }
        
        //Pattern can optionaly report it's sizes for optimisation
        FBounds patternBox = pattern.layoutPattern(this);
        
        if (patternBox == null) {
            /**
             * If pattern does not provide it's bounding box,
             * calculate it automaticaly
             **/
            
            List<FBounds> boundsList = new ArrayList<FBounds>();
            
            if (composite) {
                CompositePattern cPattern = (CompositePattern) pattern;
                
                for (Pattern nestedPattern : cPattern.getNestedPatterns()) {
                    boundsList.add(nestedPattern.getBounds());
                }
                
                BorderElement border = cPattern.getBorder();
                
                if (border != null) {
                    boundsList.add(border.getBounds());
                }
            }
            
            for (VisualElement element : pattern.getElements()) {
                boundsList.add(element.getBounds());
            }
            
            patternBox = new FBounds(boundsList);
        }
        
        pattern.setBounds(patternBox);
    }
    
    
    
    
    
    public static void translatePattern(Pattern pattern, double dx, double dy) {
        
        FBounds bounds = pattern.getBounds();
        
        for (VisualElement e : pattern.getElements()) {
            e.setLocation(e.getX() + dx, e.getY()+ dy);
        }
        
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            
            if (border != null) {
                border.setLocation(border.getX() + dx, border.getY() + dy);
            }
            
            for (Pattern p : ((CompositePattern) pattern).getNestedPatterns()) {
                translatePattern(p, dx, dy);
            }
        }
        
        pattern.setBounds(bounds.translate(dx, dy));
    }
    
    
    
    
    public FBounds setPatternPosition(Pattern pattern, double x, double y) {
        FBounds bounds = pattern.getBounds();
        translatePattern(pattern, x - bounds.x, y - bounds.y);
        return pattern.getBounds();
    }
    
    
    public FBounds setPatternCenterPosition(Pattern pattern, 
            double cx, double cy) 
    {
        FBounds bounds = pattern.getBounds();
        translatePattern(pattern, 
                cx - bounds.getCenterX(), 
                cy - bounds.getCenterY());
        return pattern.getBounds();
    }
    
    
    /**
     * Reporting the offset of pattern origin from upper-left corner of pattern box
     **/
    
    public FPoint getOriginOffset(Pattern pattern){
        FPoint origin = pattern.getOrigin();
        FBounds bounds = pattern.getBounds();
        
        if (origin == null){
            // If no origin was provided, taking 0,0 in pattern ccordinates
            // as default origin
            origin = new FPoint(0, 0);
        }
        
        return new FPoint(origin.x - bounds.x, origin.y - bounds.y);
    }
    
   
    public static final float HMARGIN = 1;
    public static final float VMARGIN = 1;
    
    public static final float HSPACING = 20f;
    public static final float VSPACING = 24f;
}