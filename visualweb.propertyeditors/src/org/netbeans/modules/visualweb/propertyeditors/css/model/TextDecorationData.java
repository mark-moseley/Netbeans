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
package org.netbeans.modules.visualweb.propertyeditors.css.model;

import java.util.StringTokenizer;

/**
 *
 * @author  Winston Prakash
 */
public class TextDecorationData {
    
    private boolean noDecorationEnabled = false;
    private boolean underlineEnabled = false;
    private boolean overlineEnabled = false;
    private boolean lineThroughEnabled = false;
    private boolean blinkEnabled = false;
    
    /** Creates a new instance of TextDecorationData */
    public TextDecorationData() {
    }
    
    public void setDecoration(String decorationStr){
        StringTokenizer st = new StringTokenizer(decorationStr);
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            if(token.trim().equals("underline")){ //NOI18N
                enableUnderline(true);
            }
            if(token.trim().equals("overline")){ //NOI18N
                enableOverline(true);
            }
            if(token.trim().equals("line-through")){ //NOI18N
                enableLineThrough(true);
            }
            if(token.trim().equals("blink")){ //NOI18N
                enableBlink(true);
            }
            if(token.trim().equals("none")){ //NOI18N
                enableNoDecoration(true);
            }
        }
    }
    
    /**
     * Enable/disable the underling of text.
     */
    public void enableUnderline(boolean underlineEnabled) {
        this.underlineEnabled = underlineEnabled;
    }
    
    public boolean underlineEnabled() {
        return underlineEnabled;
    }
    
    /**
     * Enable/disable the overlining of text.
     */
    public void enableOverline(boolean overlineEnabled) {
        this.overlineEnabled = overlineEnabled;
    }
    
    public boolean overlineEnabled() {
        return overlineEnabled;
    }
    
    /**
     * Enable/disable the line through effect of text.
     */
    public void enableLineThrough(boolean lineThroughEnabled) {
        this.lineThroughEnabled = lineThroughEnabled;
    }
    
    public boolean lineThroughEnabled() {
        return lineThroughEnabled;
    }
    
    /**
     * Enable/disable text blinking.
     */
    public void enableBlink(boolean blinkEnabled) {
        this.blinkEnabled = blinkEnabled;
    }
    
    public boolean blinkEnabled() {
        return blinkEnabled;
    }
    
    /**
     * Enable/disable text blinking.
     */
    public void enableNoDecoration(boolean noDecorationEnabled) {
        this.noDecorationEnabled = noDecorationEnabled;
    }
    
    public boolean noDecorationEnabled() {
        return noDecorationEnabled;
    }
    
    public String toString(){
        String textDecoration="";
        if(noDecorationEnabled){
            return "none";
        }
        if(underlineEnabled){
            textDecoration += " underline"; //NOI18N
        }
        if(overlineEnabled){
            textDecoration += " overline"; //NOI18N
        }
        if(lineThroughEnabled){
            textDecoration += " line-through"; //NOI18N
        }
        if(blinkEnabled){
            textDecoration += " blink"; //NOI18N
        }
        
        return textDecoration;
    }
    
    
}
