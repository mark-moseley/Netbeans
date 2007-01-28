/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.engine;

import org.apache.batik.css.engine.value.Value;

/**
 * This class represents objects which contains property/value mappings.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class StyleMap {
    
    //
    // The masks
    //
    public final static short IMPORTANT_MASK = 0x0001;
    public final static short COMPUTED_MASK = 0x0002;
    public final static short NULL_CASCADED_MASK = 0x0004;

    public final static short LINE_HEIGHT_RELATIVE_MASK = 0x0008;
    public final static short FONT_SIZE_RELATIVE_MASK = 0x0010;

    // BEGIN RAVE MODIFICATIONS
    // We need a bit in this bitmask.... and the bitmask is full!! (I don't
    // want to grow from 16 bits to 32)
    // However it turns out COLOR_RELATIVE_MASK is unused... so I'll snag
    // it for my own purposes
    // See also putColorRelative which is commented out as well
    //public final static short COLOR_RELATIVE_MASK = 0x0020;
    public final static short INHERITED_MASK = 0x0020;
    // END RAVE MODIFICATIONS
    
    public final static short PARENT_RELATIVE_MASK = 0x0040;
    public final static short BLOCK_WIDTH_RELATIVE_MASK = 0x0080;
    public final static short BLOCK_HEIGHT_RELATIVE_MASK = 0x0100;
    public final static short BOX_RELATIVE_MASK = 0x0200;

    public final static short ORIGIN_MASK = (short)0xE000; // 3 last bits


    //
    // The origin values.
    //
    public final static short USER_AGENT_ORIGIN = 0;
    public final static short USER_ORIGIN = 0x2000; // 0010
    public final static short NON_CSS_ORIGIN = 0x4000; // 0100
    public final static short AUTHOR_ORIGIN = 0x6000; // 0110
    public final static short INLINE_AUTHOR_ORIGIN = (short)0x8000; // 1000

    /**
     * The values.
     */
    protected Value[] values;

    /**
     * To store the value masks.
     */
    protected short[] masks;

    /**
     * Whether the values of this map cannot be re-cascaded.
     */
    protected boolean fixedCascadedValues;

    /**
     * Creates a new StyleMap.
     */
    public StyleMap(int size) {
        values = new Value[size];
        masks = new short[size];
    }

    /**
     * Whether this map has fixed cascaded value.
     */
    public boolean hasFixedCascadedValues() {
        return fixedCascadedValues;
    }

    /**
     * Sets the fixedCascadedValues property.
     */
    public void setFixedCascadedStyle(boolean b) {
        fixedCascadedValues = b;
    }

    /**
     * Returns the value at the given index, null if unspecified.
     */
    public Value getValue(int i) {
        return values[i];
    }

    /**
     * Returns the mask of the given property value.
     */
    public short getMask(int i) {
        return masks[i];
    }

    /**
     * Tells whether the given property value is important.
     */
    public boolean isImportant(int i) {
        return (masks[i] & IMPORTANT_MASK) != 0;
    }

    /**
     * Tells whether the given property value is computed.
     */
    public boolean isComputed(int i) {
        return (masks[i] & COMPUTED_MASK) != 0;
    }

    /**
     * Tells whether the given cascaded property value is null.
     */
    public boolean isNullCascaded(int i) {
        return (masks[i] & NULL_CASCADED_MASK) != 0;
    }

    /**
     * Returns the origin value.
     */
    public short getOrigin(int i) {
        return (short)(masks[i] & ORIGIN_MASK);
    }

    /**
     * Tells whether the given property value is relative to 'color'.
     */
    public boolean isColorRelative(int i) {
        // BEGIN RAVE MODIFICATIONS
        //return (masks[i] & COLOR_RELATIVE_MASK) != 0;
        // Nobody was setting it (putColorRelative was unused)
        // so I've reused the bitmask position for color relative
        // and I'm just returning false here
        return false;
        // END RAVE MODIFICATIONS
    }

    /**
     * Tells whether the given property value is relative to the parent's
     * property value.
     */
    public boolean isParentRelative(int i) {
        return (masks[i] & PARENT_RELATIVE_MASK) != 0;
    }

    /**
     * Tells whether the given property value is relative to 'line-height'.
     */
    public boolean isLineHeightRelative(int i) {
        return (masks[i] & LINE_HEIGHT_RELATIVE_MASK) != 0;
    }

    /**
     * Tells whether the given property value is relative to 'font-size'.
     */
    public boolean isFontSizeRelative(int i) {
        return (masks[i] & FONT_SIZE_RELATIVE_MASK) != 0;
    }

    /**
     * Tells whether the given property value is relative to the
     * width of the containing block.
     */
    public boolean isBlockWidthRelative(int i) {
        return (masks[i] & BLOCK_WIDTH_RELATIVE_MASK) != 0;
    }

    /**
     * Tells whether the given property value is relative to the
     * height of the containing block.
     */
    public boolean isBlockHeightRelative(int i) {
        return (masks[i] & BLOCK_HEIGHT_RELATIVE_MASK) != 0;
    }

    /**
     * Puts a property value, given the property index.
     * @param i The property index.
     * @param v The property value.
     */
    public void putValue(int i, Value v) {
        values[i] = v;
    }

    /**
     * Puts a property mask, given the property index.
     * @param i The property index.
     * @param m The property mask.
     */
    public void putMask(int i, short m) {
        masks[i] = m;
    }

    /**
     * Sets the priority of a property value.
     */
    public void putImportant(int i, boolean b) {
        masks[i] &= ~IMPORTANT_MASK;
        masks[i] |= (b) ? IMPORTANT_MASK : 0;
    }

    /**
     * Sets the origin of the given value.
     */
    public void putOrigin(int i, short val) {
        masks[i] &= ~ORIGIN_MASK;
        masks[i] |= (short)(val & ORIGIN_MASK);
    }

    /**
     * Sets the computed flag of a property value.
     */
    public void putComputed(int i, boolean b) {
        masks[i] &= ~COMPUTED_MASK;
        masks[i] |= (b) ? COMPUTED_MASK : 0;
    }

    /**
     * Sets the null-cascaded flag of a property value.
     */
    public void putNullCascaded(int i, boolean b) {
        masks[i] &= ~NULL_CASCADED_MASK;
        masks[i] |= (b) ? NULL_CASCADED_MASK : 0;
    }

    // BEGIN RAVE MODIFICATIONS
    // Color relative was unused so I'm reusing the bit position
    // for tracking whether a property is inherited.
//    /**
//     * Sets the color-relative flag of a property value.
//     */
//    public void putColorRelative(int i, boolean b) {
//        masks[i] &= ~COLOR_RELATIVE_MASK;
//        masks[i] |= (b) ? COLOR_RELATIVE_MASK : 0;
//    }

    /**
     * Tells whether the given property has been inherited
     */
    public boolean isInherited(int i) {
        return (masks[i] & INHERITED_MASK) != 0;
    }    
    /**
     * Sets the color-relative flag of a property value.
     */
    public void putInherited(int i, boolean b) {
        masks[i] &= ~INHERITED_MASK;
        masks[i] |= (b) ? INHERITED_MASK : 0;
    }
    // END RAVE MODIFICATIONS

    
    /**
     * Sets the parent-relative flag of a property value.
     */
    public void putParentRelative(int i, boolean b) {
        masks[i] &= ~PARENT_RELATIVE_MASK;
        masks[i] |= (b) ? PARENT_RELATIVE_MASK : 0;
    }

    /**
     * Sets the line-height-relative flag of a property value.
     */
    public void putLineHeightRelative(int i, boolean b) {
        masks[i] &= ~LINE_HEIGHT_RELATIVE_MASK;
        masks[i] |= (b) ? LINE_HEIGHT_RELATIVE_MASK : 0;
    }

    /**
     * Sets the font-size-relative flag of a property value.
     */
    public void putFontSizeRelative(int i, boolean b) {
        masks[i] &= ~FONT_SIZE_RELATIVE_MASK;
        masks[i] |= (b) ? FONT_SIZE_RELATIVE_MASK : 0;
    }

    /**
     * Sets the block-width-relative flag of a property value.
     */
    public void putBlockWidthRelative(int i, boolean b) {
        masks[i] &= ~BLOCK_WIDTH_RELATIVE_MASK;
        masks[i] |= (b) ? BLOCK_WIDTH_RELATIVE_MASK : 0;
    }

    /**
     * Sets the block-height-relative flag of a property value.
     */
    public void putBlockHeightRelative(int i, boolean b) {
        masks[i] &= ~BLOCK_HEIGHT_RELATIVE_MASK;
        masks[i] |= (b) ? BLOCK_HEIGHT_RELATIVE_MASK : 0;
    }

    /**
     * Returns a printable representation of this style map.
     */
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            Value v = values[i];
            if (v != null) {
                sb.append(eng.getPropertyName(i));
                sb.append(": ");
                sb.append(v);
                if (isImportant(i)) {
                    sb.append(" !important");
                }
                sb.append(";\n");
            }
        }
        return sb.toString();
    }

// BEGIN RAVE MODIFICATIONS
    /** Return the size of the stylemap.
     * @param total If true, return the number of available slots, not necessarily set in this map.
     *   If false, return only the number of non-null entries in the map.
     */
    public int getSize(boolean total) {
        if (total) {
            return values.length;
        } else {
            int count = 0;
            for (int i = 0, n = values.length; i < n; i++) {
                Value v = values[i];
                if (v != null) {
                    count++;
                }
            }
            return count;
        }
    }
    
    /**
     * Returns a single line string suitable as a "style" attribute
     * for an element. Note - this is not for debugging so don't put
     * arbitrary info in here.
     */
    public String toStyleString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (int i = 0, n = values.length; i < n; i++) {
            Value v = values[i];
            if (v != null) {
                if (first) {
                    first = false;
                } else {
                    sb.append("; ");
                }
                sb.append(eng.getPropertyName(i));
                sb.append(": ");
                sb.append(v);
                if (isImportant(i)) {
                    sb.append(" !important");
                }
            }
        }
        return sb.toString();
    }
    
//  END RAVE MODIFICATIONS

    
    /**
     * Returns a printable representation of this style map.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append('\n');
        for (int i = 0; i < values.length; i++) {
            Value v = values[i];
            if (v != null) {
                //sb.append(eng.getPropertyName(i));
                sb.append("prop"); sb.append(Integer.toString(i));
                sb.append(": ");
                sb.append(v);
                if (isImportant(i)) {
                    sb.append(" !important");
                }
                sb.append(";\n");
            }
        }
        return sb.toString();
    }
    
}
