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

package com.sun.rave.designtime.ext.componentgroup.impl;

import com.sun.rave.designtime.ext.componentgroup.ColorWrapper;
import java.awt.Color;

/**
 * <p>Implementation that wraps a color that is stored in the design context data.</p>
 * @author mbohm
 */
public class ColorWrapperImpl implements ColorWrapper {

    private Color color;
    
    /**
     * <p>Constructor that accepts a <code>Color</code>.</p>
     */ 
    public ColorWrapperImpl(Color color) {
        this.color = color;
    }
    
    /**
     * <p>Constructor that accepts a <code>String</code> 
     * representing a color.</p>
     */ 
    public ColorWrapperImpl(String fromString) {
        String[] split = fromString.split(","); // NOI18N
        if (split.length > 2) {
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            this.color = new Color(r, g, b);
        }
    }
   
    /**
     * <p>Get the wrapped color.</p>
     */ 
    public Color getColor() {
        return color;
    }
    
    /**
     * <p>Get a string containing the RGB information for the wrapped color.</p>
     */
    public String toString() {
        if (color != null) {
            return color.getRed() + "," + // NOI18N
                   color.getGreen() + "," +  // NOI18N
                   color.getBlue();
        }
        return "";  // NOI18N
    }
}
