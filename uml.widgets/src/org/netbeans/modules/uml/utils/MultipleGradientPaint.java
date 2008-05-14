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

package org.netbeans.modules.uml.utils;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

/** This is the superclass for Paints which use a multiple color
 * gradient to fill in their raster.  It provides storage for variables and
 * enumerated values common to LinearGradientPaint and RadialGradientPaint.
 *
 *
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: MultipleGradientPaint.java,v 1.1.2.2 2007/11/19 23:54:03 sherylsu Exp $
 */
public abstract class MultipleGradientPaint implements Paint
{
    
    /** Transparency. */
    protected int transparency;
    
    /** Gradient keyframe values in the range 0 to 1. */
    protected float[] fractions;
    
    /** Gradient colors. */
    protected Color[] colors;
    
    /** Transform to apply to gradient. */
    protected AffineTransform gradientTransform;
    
    /** The method to use when painting out of the gradient bounds. */
    protected CycleMethodEnum cycleMethod;
    
    /** The colorSpace in which to perform the interpolation. */
    protected ColorSpaceEnum colorSpace;
    
    /** Inner class to allow for typesafe enumerated ColorSpace values. */
    public static class ColorSpaceEnum
    {
    }
    
    /** Inner class to allow for typesafe enumerated CycleMethod values. */
    public static class CycleMethodEnum
    {
    }
    
    /** Indicates (if the gradient starts or ends inside the target region)
     *  to use the terminal colors to fill the remaining area. (default)
     */
    public static final CycleMethodEnum NO_CYCLE = new CycleMethodEnum();
    
    /** Indicates (if the gradient starts or ends inside the target region),
     *  to cycle the gradient colors start-to-end, end-to-start to fill the
     *  remaining area.
     */
    public static final CycleMethodEnum REFLECT = new CycleMethodEnum();
    
    /** Indicates (if the gradient starts or ends inside the target region),
     *  to cycle the gradient colors start-to-end, start-to-end to fill the
     *  remaining area.
     */
    public static final CycleMethodEnum REPEAT = new CycleMethodEnum();
    
    /** Indicates that the color interpolation should occur in sRGB space.
     *  (default)
     */
    public static final ColorSpaceEnum SRGB = new ColorSpaceEnum();
    
    /** Indicates that the color interpolation should occur in linearized
     *  RGB space.
     */
    public static final ColorSpaceEnum LINEAR_RGB = new ColorSpaceEnum();
    
    
    /**
     * Superclass constructor, typical user should never have to call this.
     *
     * @param fractions numbers ranging from 0.0 to 1.0 specifying the
     * distribution of colors along the gradient
     *
     * @param colors array of colors corresponding to each fractional value
     *
     * @param cycleMethod either NO_CYCLE, REFLECT, or REPEAT
     *
     * @param colorSpace which colorspace to use for interpolation,
     * either SRGB or LINEAR_RGB
     *
     * @param gradientTransform transform to apply to the gradient
     *
     * @throws NullPointerException if arrays are null, or
     * gradientTransform is null
     *
     * @throws IllegalArgumentException if fractions.length != colors.length,
     * or if colors is less than 2 in size, or if an enumerated value is bad.
     */
    public MultipleGradientPaint(float[] fractions,
            Color[] colors,
            CycleMethodEnum cycleMethod,
            ColorSpaceEnum colorSpace,
            AffineTransform gradientTransform)
    {
        
        if (fractions == null)
        {
            throw new IllegalArgumentException("Fractions array cannot be " +
                    "null");
        }
        
        if (colors == null)
        {
            throw new IllegalArgumentException("Colors array cannot be null");
        }
        
        if (fractions.length != colors.length)
        {
            throw new IllegalArgumentException("Colors and fractions must " +
                    "have equal size");
        }
        
        if (colors.length < 2)
        {
            throw new IllegalArgumentException("User must specify at least " +
                    "2 colors");
        }
        
        if ((colorSpace != LINEAR_RGB) &&
                (colorSpace != SRGB))
        {
            throw new IllegalArgumentException("Invalid colorspace for " +
                    "interpolation.");
        }
        
        if ((cycleMethod != NO_CYCLE) &&
                (cycleMethod != REFLECT) &&
                (cycleMethod != REPEAT))
        {
            throw new IllegalArgumentException("Invalid cycle method.");
        }
        
        if (gradientTransform == null)
        {
            throw new IllegalArgumentException("Gradient transform cannot be "+
                    "null.");
        }
        
        //copy the fractions array
        this.fractions = new float[fractions.length];
        System.arraycopy(fractions, 0, this.fractions, 0, fractions.length);
        
        //copy the colors array
        this.colors = new Color[colors.length];
        System.arraycopy(colors, 0, this.colors, 0, colors.length);
        
        //copy some flags
        this.colorSpace = colorSpace;
        this.cycleMethod = cycleMethod;
        
        //copy the gradient transform
        this.gradientTransform = (AffineTransform)gradientTransform.clone();
        
        // Process transparency
        boolean opaque = true;
        for(int i=0; i<colors.length; i++)
        {
            opaque = opaque && (colors[i].getAlpha()==0xff);
        }
        
        if(opaque)
        {
            transparency = OPAQUE;
        }
        
        else
        {
            transparency = TRANSLUCENT;
        }
    }
    
    /**
     * Returns a copy of the array of colors used by this gradient.
     * @return a copy of the array of colors used by this gradient
     *
     */
    public Color[] getColors()
    {
        Color[] colors = new Color[this.colors.length];
        System.arraycopy(this.colors, 0, colors, 0, this.colors.length);
        return colors;
    }
    
    /**
     * Returns a copy of the array of floats used by this gradient
     * to calculate color distribution.
     * @return a copy of the array of floats used by this gradient to
     * calculate color distribution
     *
     */
    public float[] getFractions()
    {
        float[] fractions = new float[this.fractions.length];
        System.arraycopy(this.fractions, 0, fractions, 0, this.fractions.length);
        return fractions;
    }
    
    /**
     * Returns the transparency mode for this LinearGradientPaint.
     * @return an integer value representing this LinearGradientPaint object's
     * transparency mode.
     */
    public int getTransparency()
    {
        return transparency;
    }
    
    /**
     * Returns the enumerated type which specifies cycling behavior.
     * @return the enumerated type which specifies cycling behavior
     */
    public CycleMethodEnum getCycleMethod()
    {
        return cycleMethod;
    }
    
    /**
     * Returns the enumerated type which specifies color space for
     * interpolation.
     * @return the enumerated type which specifies color space for
     * interpolation
     */
    public ColorSpaceEnum getColorSpace()
    {
        return colorSpace;
    }
    
    /**
     * Returns a copy of the transform applied to the gradient.
     * @return a copy of the transform applied to the gradient.
     */
    public AffineTransform getTransform()
    {
        return (AffineTransform)gradientTransform.clone();
    }
}
