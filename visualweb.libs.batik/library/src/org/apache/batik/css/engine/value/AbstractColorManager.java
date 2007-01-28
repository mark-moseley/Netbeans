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

package org.apache.batik.css.engine.value;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * This class provides a manager for the property with support for
 * CSS color values.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public abstract class AbstractColorManager extends IdentifierManager {
    
    /**
     * The identifier values.
     */
    public final static StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_AQUA_VALUE,
                   ValueConstants.AQUA_VALUE);
        values.put(CSSConstants.CSS_BLACK_VALUE,
                   ValueConstants.BLACK_VALUE);
        values.put(CSSConstants.CSS_BLUE_VALUE,
                   ValueConstants.BLUE_VALUE);
        values.put(CSSConstants.CSS_FUCHSIA_VALUE,
                   ValueConstants.FUCHSIA_VALUE);
        values.put(CSSConstants.CSS_GRAY_VALUE,
                   ValueConstants.GRAY_VALUE);
        values.put(CSSConstants.CSS_GREEN_VALUE,
                   ValueConstants.GREEN_VALUE);
        values.put(CSSConstants.CSS_LIME_VALUE,
                   ValueConstants.LIME_VALUE);
        values.put(CSSConstants.CSS_MAROON_VALUE,
                   ValueConstants.MAROON_VALUE);
        values.put(CSSConstants.CSS_NAVY_VALUE,
                   ValueConstants.NAVY_VALUE);
        values.put(CSSConstants.CSS_OLIVE_VALUE,
                   ValueConstants.OLIVE_VALUE);
        values.put(CSSConstants.CSS_PURPLE_VALUE,
                   ValueConstants.PURPLE_VALUE);
        values.put(CSSConstants.CSS_RED_VALUE,
                   ValueConstants.RED_VALUE);
        values.put(CSSConstants.CSS_SILVER_VALUE,
                   ValueConstants.SILVER_VALUE);
        values.put(CSSConstants.CSS_TEAL_VALUE,
                   ValueConstants.TEAL_VALUE);
        values.put(CSSConstants.CSS_WHITE_VALUE,
                   ValueConstants.WHITE_VALUE);
        values.put(CSSConstants.CSS_YELLOW_VALUE,
                   ValueConstants.YELLOW_VALUE);

        values.put(CSSConstants.CSS_ACTIVEBORDER_VALUE,
                   ValueConstants.ACTIVEBORDER_VALUE);
        values.put(CSSConstants.CSS_ACTIVECAPTION_VALUE,
                   ValueConstants.ACTIVECAPTION_VALUE);
        values.put(CSSConstants.CSS_APPWORKSPACE_VALUE,
                   ValueConstants.APPWORKSPACE_VALUE);
        values.put(CSSConstants.CSS_BACKGROUND_VALUE,
                   ValueConstants.BACKGROUND_VALUE);
        values.put(CSSConstants.CSS_BUTTONFACE_VALUE,
                   ValueConstants.BUTTONFACE_VALUE);
        values.put(CSSConstants.CSS_BUTTONHIGHLIGHT_VALUE,
                   ValueConstants.BUTTONHIGHLIGHT_VALUE);
        values.put(CSSConstants.CSS_BUTTONSHADOW_VALUE,
                   ValueConstants.BUTTONSHADOW_VALUE);
        values.put(CSSConstants.CSS_BUTTONTEXT_VALUE,
                   ValueConstants.BUTTONTEXT_VALUE);
        values.put(CSSConstants.CSS_CAPTIONTEXT_VALUE,
                   ValueConstants.CAPTIONTEXT_VALUE);
        values.put(CSSConstants.CSS_GRAYTEXT_VALUE,
                   ValueConstants.GRAYTEXT_VALUE);
        values.put(CSSConstants.CSS_HIGHLIGHT_VALUE,
                   ValueConstants.HIGHLIGHT_VALUE);
        values.put(CSSConstants.CSS_HIGHLIGHTTEXT_VALUE,
                   ValueConstants.HIGHLIGHTTEXT_VALUE);
        values.put(CSSConstants.CSS_INACTIVEBORDER_VALUE,
                   ValueConstants.INACTIVEBORDER_VALUE);
        values.put(CSSConstants.CSS_INACTIVECAPTION_VALUE,
                   ValueConstants.INACTIVECAPTION_VALUE);
        values.put(CSSConstants.CSS_INACTIVECAPTIONTEXT_VALUE,
                   ValueConstants.INACTIVECAPTIONTEXT_VALUE);
        values.put(CSSConstants.CSS_INFOBACKGROUND_VALUE,
                   ValueConstants.INFOBACKGROUND_VALUE);
        values.put(CSSConstants.CSS_INFOTEXT_VALUE,
                   ValueConstants.INFOTEXT_VALUE);
        values.put(CSSConstants.CSS_MENU_VALUE,
                   ValueConstants.MENU_VALUE);
        values.put(CSSConstants.CSS_MENUTEXT_VALUE,
                   ValueConstants.MENUTEXT_VALUE);
        values.put(CSSConstants.CSS_SCROLLBAR_VALUE,
                   ValueConstants.SCROLLBAR_VALUE);
        values.put(CSSConstants.CSS_THREEDDARKSHADOW_VALUE,
                   ValueConstants.THREEDDARKSHADOW_VALUE);
        values.put(CSSConstants.CSS_THREEDFACE_VALUE,
                   ValueConstants.THREEDFACE_VALUE);
        values.put(CSSConstants.CSS_THREEDHIGHLIGHT_VALUE,
                   ValueConstants.THREEDHIGHLIGHT_VALUE);
        values.put(CSSConstants.CSS_THREEDLIGHTSHADOW_VALUE,
                   ValueConstants.THREEDLIGHTSHADOW_VALUE);
        values.put(CSSConstants.CSS_THREEDSHADOW_VALUE,
                   ValueConstants.THREEDSHADOW_VALUE);
        values.put(CSSConstants.CSS_WINDOW_VALUE,
                   ValueConstants.WINDOW_VALUE);
        values.put(CSSConstants.CSS_WINDOWFRAME_VALUE,
                   ValueConstants.WINDOWFRAME_VALUE);
        values.put(CSSConstants.CSS_WINDOWTEXT_VALUE,
                   ValueConstants.WINDOWTEXT_VALUE);
        // BEGIN RAVE MODIFICATIONS
        // Dynamically computed color; defaults to a blue color
        // for links but will consult value attributes if necessary.
        // Treated as a system color.
        values.put(CSSConstants.CSS_LINKCOLOR_VALUE,
                   ValueConstants.LINKCOLOR_VALUE);
        // END RAVE MODIFICATIONS
    }

    /**
     * The computed identifier values.
     */
    protected final static StringMap computedValues = new StringMap();
    static {
        computedValues.put(CSSConstants.CSS_BLACK_VALUE,
                           ValueConstants.BLACK_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_SILVER_VALUE,
                           ValueConstants.SILVER_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_GRAY_VALUE,
                           ValueConstants.GRAY_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_WHITE_VALUE,
                           ValueConstants.WHITE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_MAROON_VALUE,
                           ValueConstants.MAROON_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_RED_VALUE,
                           ValueConstants.RED_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_PURPLE_VALUE,
                           ValueConstants.PURPLE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_FUCHSIA_VALUE,
                           ValueConstants.FUCHSIA_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_GREEN_VALUE,
                           ValueConstants.GREEN_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_LIME_VALUE,
                           ValueConstants.LIME_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_OLIVE_VALUE,
                           ValueConstants.OLIVE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_YELLOW_VALUE,
                           ValueConstants.YELLOW_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_NAVY_VALUE,
                           ValueConstants.NAVY_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_BLUE_VALUE,
                           ValueConstants.BLUE_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_TEAL_VALUE,
                           ValueConstants.TEAL_RGB_VALUE);
        computedValues.put(CSSConstants.CSS_AQUA_VALUE,
                           ValueConstants.AQUA_RGB_VALUE);
    }

    /**
     * Implements {@link ValueManager#createValue(LexicalUnit,CSSEngine)}.
     */
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR) {
            lu = lu.getParameters();
            Value red = createColorComponent(lu);
            lu = lu.getNextLexicalUnit().getNextLexicalUnit();
            Value green = createColorComponent(lu);
            lu = lu.getNextLexicalUnit().getNextLexicalUnit();
            Value blue = createColorComponent(lu);
            return createRGBColor(red, green, blue);
        }
        return super.createValue(lu, engine);
    }

    /**
     * Implements {@link
     * ValueManager#computeValue(CSSStylableElement,String,CSSEngine,int,StyleMap,Value)}.
     */
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            String ident = value.getStringValue();
            // Search for a direct computed value.
            Value v = (Value)computedValues.get(ident);
            if (v != null) {
                return v;
            }
            // Must be a system color...
            if (values.get(ident) == null) {
                throw new InternalError();
            }
            return engine.getCSSContext().getSystemColor(ident);
        }
        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }

    /**
     * Creates an RGB color.
     */
    protected Value createRGBColor(Value r, Value g, Value b) {
        return new RGBColorValue(r, g, b);
    }

    /**
     * Creates a color component from a lexical unit.
     */
    protected Value createColorComponent(LexicalUnit lu) throws DOMException {
	switch (lu.getLexicalUnitType()) {
	case LexicalUnit.SAC_INTEGER:
	    return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getIntegerValue());

	case LexicalUnit.SAC_REAL:
	    return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_PERCENTAGE:
	    return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE,
                                  lu.getFloatValue());
        }
        throw createInvalidRGBComponentUnitDOMException
            (lu.getLexicalUnitType());
    }

    /**
     * Implements {@link IdentifierManager#getIdentifiers()}.
     */
    protected StringMap getIdentifiers() {
        return values;
    }

    private DOMException createInvalidRGBComponentUnitDOMException
        (short type) {
        Object[] p = new Object[] { getPropertyName(),
                                    new Integer(type) };
        String s = Messages.formatMessage("invalid.rgb.component.unit", p);
        return new DOMException(DOMException.NOT_SUPPORTED_ERR, s);
    }

}
