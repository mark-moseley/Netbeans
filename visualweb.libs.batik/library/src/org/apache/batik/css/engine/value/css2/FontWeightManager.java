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

package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * This class provides a manager for the 'font-weight' property values.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class FontWeightManager extends IdentifierManager {
    
    /**
     * The identifier values.
     */
    public final static StringMap values = new StringMap();
    static {
        // BEGIN RAVE MODIFICATIONS
        // ALL is not a valid CSS 2 option for font-weight
        //values.put(CSSConstants.CSS_ALL_VALUE,
        //           ValueConstants.ALL_VALUE);
        // END RAVE MODIFICATIONS
	values.put(CSSConstants.CSS_BOLD_VALUE,
                   ValueConstants.BOLD_VALUE);
	values.put(CSSConstants.CSS_BOLDER_VALUE,
                   ValueConstants.BOLDER_VALUE);
	values.put(CSSConstants.CSS_LIGHTER_VALUE,
                   ValueConstants.LIGHTER_VALUE);
	values.put(CSSConstants.CSS_NORMAL_VALUE,
                   ValueConstants.NORMAL_VALUE);
    }

    /**
     * Implements {@link ValueManager#isInheritedProperty()}.
     */
    public boolean isInheritedProperty() {
	return true;
    }

    /**
     * Implements {@link ValueManager#getPropertyName()}.
     */
    public String getPropertyName() {
	return CSSConstants.CSS_FONT_WEIGHT_PROPERTY;
    }
    
    /**
     * Implements {@link ValueManager#getDefaultValue()}.
     */
    public Value getDefaultValue() {
        return ValueConstants.NORMAL_VALUE;
    }

    /**
     * Implements {@link ValueManager#createValue(LexicalUnit,CSSEngine)}.
     */
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
	if (lu.getLexicalUnitType() == LexicalUnit.SAC_INTEGER) {
	    int i = lu.getIntegerValue();
	    switch (i) {
	    case 100:
		return ValueConstants.NUMBER_100;
	    case 200:
		return ValueConstants.NUMBER_200;
	    case 300:
		return ValueConstants.NUMBER_300;
	    case 400:
		return ValueConstants.NUMBER_400;
	    case 500:
		return ValueConstants.NUMBER_500;
	    case 600:
		return ValueConstants.NUMBER_600;
	    case 700:
		return ValueConstants.NUMBER_700;
	    case 800:
		return ValueConstants.NUMBER_800;
	    case 900:
		return ValueConstants.NUMBER_900;
	    }
            throw createInvalidFloatValueDOMException(i, engine);
        }
        return super.createValue(lu, engine);
    }

    /**
     * Implements {@link ValueManager#createFloatValue(short,float)}.
     */
    public Value createFloatValue(short type, float floatValue, CSSEngine engine)
        throws DOMException {
	if (type == CSSPrimitiveValue.CSS_NUMBER) {
	    int i = (int)floatValue;
	    if (floatValue == i) {
		switch (i) {
                case 100:
                    return ValueConstants.NUMBER_100;
                case 200:
                    return ValueConstants.NUMBER_200;
                case 300:
                    return ValueConstants.NUMBER_300;
                case 400:
                    return ValueConstants.NUMBER_400;
                case 500:
                    return ValueConstants.NUMBER_500;
                case 600:
                    return ValueConstants.NUMBER_600;
                case 700:
                    return ValueConstants.NUMBER_700;
                case 800:
                    return ValueConstants.NUMBER_800;
                case 900:
                    return ValueConstants.NUMBER_900;
		}
	    }
	}
        throw createInvalidFloatValueDOMException(floatValue, engine);
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
        if (value == ValueConstants.BOLDER_VALUE) {
            sm.putParentRelative(idx, true);

            CSSContext ctx = engine.getCSSContext();
            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            float fw;
            if (p == null) {
                fw = 400;
            } else {
                Value v = engine.getComputedStyle(p, pseudo, idx);
                fw = v.getFloatValue();
            }
            return createFontWeight(ctx.getBolderFontWeight(fw));
        } else if (value == ValueConstants.LIGHTER_VALUE) {
            sm.putParentRelative(idx, true);

            CSSContext ctx = engine.getCSSContext();
            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            float fw;
            if (p == null) {
                fw = 400;
            } else {
                Value v = engine.getComputedStyle(p, pseudo, idx);
                fw = v.getFloatValue();
            }
            return createFontWeight(ctx.getLighterFontWeight(fw));
        } else if (value == ValueConstants.NORMAL_VALUE) {
            return ValueConstants.NUMBER_400;
        } else if (value == ValueConstants.BOLD_VALUE) {
            return ValueConstants.NUMBER_700;
        }
        return value;
    }

    /**
     * Returns the CSS value associated with the given font-weight.
     */
    protected Value createFontWeight(float f) {
        switch ((int)f) {
        case 100:
            return ValueConstants.NUMBER_100;
        case 200:
            return ValueConstants.NUMBER_200;
        case 300:
            return ValueConstants.NUMBER_300;
        case 400:
            return ValueConstants.NUMBER_400;
        case 500:
            return ValueConstants.NUMBER_500;
        case 600:
            return ValueConstants.NUMBER_600;
        case 700:
            return ValueConstants.NUMBER_700;
        case 800:
            return ValueConstants.NUMBER_800;
        default: // 900
            return ValueConstants.NUMBER_900;
        }
    }

    /**
     * Implements {@link IdentifierManager#getIdentifiers()}.
     */
    protected StringMap getIdentifiers() {
        return values;
    }
}
