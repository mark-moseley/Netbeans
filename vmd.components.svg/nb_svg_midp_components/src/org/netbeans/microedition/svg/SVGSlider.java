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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.microedition.svg;

import org.netbeans.microedition.svg.input.InputHandler;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;


/**
 * Suggested svg snippet:
 * <pre>
 * &lt;g id="size_slider" transform="translate(20,110)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=slider&lt;/text>
 *
 *       &lt;rect x="0" y="-10" rx="5" ry="5" width="200" height="30" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="size_slider.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="size_slider.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *   &lt;g>
 *       &lt;text display="none">type=rule&lt;/text>
 *       &lt;rect  x="10.0" y="1.0" width="180" height="4" fill="rgb(240,240,255)" stroke="black" stroke-width="1"/>
 *   &lt;/g>
 *       &lt;g transform="translate(0,-5)">
 *       &lt;text display="none">type=knob&lt;/text>
 *           &lt;polygon transform="scale(0.2,0.2)"  points="20,10 80,10 80,40 50,70 20,40" 
 *              fill="rgb(220,220,255)" stroke="black" stroke-width="1"/>
 *       &lt;/g>
 *   &lt;/g>
 * </pre>
 * @author ads
 *
 */
public class SVGSlider extends SVGComponent {

    private static final String TRANSFORM   = "transform";      // NOI18N

    private static final int DEFAULT_MAX    = 10;
    
    private static final String KNOB        = "knob";           // NOI18N
    private static final String RULE        = "rule";           // NOI18N
    
    public SVGSlider( SVGForm form, String elemId ) {
        super(form, elemId);
        myStep = 1;
        myMax = DEFAULT_MAX;
        myKnobElement = (SVGLocatableElement)getElementByMeta( getElement(), 
                TYPE, KNOB );
        myRuleElement = (SVGLocatableElement) getElementByMeta( getElement(), 
                TYPE, RULE );
        myInputHandler = new SliderInputHandler();
        setValue( myMin );
    }
    
    public SVGSlider( int min, int max, SVGForm form, String elemId ) {
        this( form, elemId );
        myMin = min;
        myMax = max;
    }
    
    public int getValue(){
        return myValue;
    }
    
    public InputHandler getInputHandler() {
        return myInputHandler;
    }
    
    public void setValue( final int value ){
        if ( myValue > myMax || myValue < myMin ){
            throw new IllegalArgumentException( value +" is out of range"); // NOI18N
        }
        
        getForm().invokeLaterSafely(new Runnable() {

            public void run() {
                SVGRect rect = myRuleElement.getBBox();
                float width = rect.getWidth();
                SVGMatrix matrix = myKnobElement.getMatrixTrait(TRANSFORM);
                matrix.mTranslate((value - myValue) * width / (myMax - myMin),
                        0);

                myKnobElement.setMatrixTrait(TRANSFORM, matrix);
            }
        });
        
        myValue = value;
        fireActionPerformed();
    }
    
    public void setMin( int min ){
        myMin = min;
    }
    
    public void setMax( int max ){
        myMax = max;
    }
    
    public int getMin(){
        return myMin;
    }
    
    public int getMax(){
        return myMax;
    }
    
    private class SliderInputHandler extends InputHandler {

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.input.InputHandler#handleKeyPress(org.netbeans.microedition.svg.SVGComponent, int)
         */
        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            if ( comp instanceof SVGSlider) {
                return  keyCode == LEFT || keyCode == RIGHT;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.input.InputHandler#handleKeyRelease(org.netbeans.microedition.svg.SVGComponent, int)
         */
        public boolean handleKeyRelease( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGSlider) {
                if ( keyCode == LEFT ) {
                    setValue( Math.max( myMin , myValue - myStep ) );
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    setValue(  Math.min( myMax , myValue + myStep ) );
                    ret = true;
                }
            }
            return ret;
        }
        
    }
    
    private int myMin;
    private int myMax;
    private int myStep;
    private int myValue;
    private final InputHandler myInputHandler;
    
    private final SVGLocatableElement myKnobElement;
    private final SVGLocatableElement myRuleElement;

}
