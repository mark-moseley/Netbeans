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

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.CSSConstants;

/**
 * This class provides a manager for the 'font-stretch' property values.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class FontStretchManager extends IdentifierManager {
    
    /**
     * The identifier values.
     */
    protected final static StringMap values = new StringMap();
    static {
	values.put(CSSConstants.CSS_ALL_VALUE,
                   ValueConstants.ALL_VALUE);
	values.put(CSSConstants.CSS_CONDENSED_VALUE,
                   ValueConstants.CONDENSED_VALUE);
	values.put(CSSConstants.CSS_EXPANDED_VALUE,
                   ValueConstants.EXPANDED_VALUE);
	values.put(CSSConstants.CSS_EXTRA_CONDENSED_VALUE,
                   ValueConstants.EXTRA_CONDENSED_VALUE);
	values.put(CSSConstants.CSS_EXTRA_EXPANDED_VALUE,
                   ValueConstants.EXTRA_EXPANDED_VALUE);
	values.put(CSSConstants.CSS_NARROWER_VALUE,
                   ValueConstants.NARROWER_VALUE);
	values.put(CSSConstants.CSS_NORMAL_VALUE,
                   ValueConstants.NORMAL_VALUE);
	values.put(CSSConstants.CSS_SEMI_CONDENSED_VALUE,
                   ValueConstants.SEMI_CONDENSED_VALUE);
	values.put(CSSConstants.CSS_SEMI_EXPANDED_VALUE,
                   ValueConstants.SEMI_EXPANDED_VALUE);
	values.put(CSSConstants.CSS_ULTRA_CONDENSED_VALUE,
                   ValueConstants.ULTRA_CONDENSED_VALUE);
	values.put(CSSConstants.CSS_ULTRA_EXPANDED_VALUE,
                   ValueConstants.ULTRA_EXPANDED_VALUE);
	values.put(CSSConstants.CSS_WIDER_VALUE,
                   ValueConstants.WIDER_VALUE);
    }

    /**
     * Implements {@link
     * org.apache.batik.css.engine.value.ValueManager#isInheritedProperty()}.
     */
    public boolean isInheritedProperty() {
	return true;
    }

    /**
     * Implements {@link
     * org.apache.batik.css.engine.value.ValueManager#getPropertyName()}.
     */
    public String getPropertyName() {
	return CSSConstants.CSS_FONT_STRETCH_PROPERTY;
    }
    
    /**
     * Implements {@link
     * org.apache.batik.css.engine.value.ValueManager#getDefaultValue()}.
     */
    public Value getDefaultValue() {
        return ValueConstants.NORMAL_VALUE;
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
        if (value == ValueConstants.NARROWER_VALUE) {
            sm.putParentRelative(idx, true);

            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            if (p == null) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            Value v = engine.getComputedStyle(p, pseudo, idx);
            if (v == ValueConstants.NORMAL_VALUE) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            if (v == ValueConstants.CONDENSED_VALUE) {
                return ValueConstants.EXTRA_CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXPANDED_VALUE) {
                return ValueConstants.SEMI_EXPANDED_VALUE;
            }
            if (v == ValueConstants.SEMI_EXPANDED_VALUE) {
                return ValueConstants.NORMAL_VALUE;
            }
            if (v == ValueConstants.SEMI_CONDENSED_VALUE) {
                return ValueConstants.CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXTRA_CONDENSED_VALUE) {
                return ValueConstants.ULTRA_CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXTRA_EXPANDED_VALUE) {
                return ValueConstants.EXPANDED_VALUE;
            }
            if (v == ValueConstants.ULTRA_CONDENSED_VALUE) {
                return ValueConstants.ULTRA_CONDENSED_VALUE;
            }
            return ValueConstants.EXTRA_EXPANDED_VALUE;
        } else if (value == ValueConstants.WIDER_VALUE) {
            sm.putParentRelative(idx, true);

            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            if (p == null) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            Value v = engine.getComputedStyle(p, pseudo, idx);
            if (v == ValueConstants.NORMAL_VALUE) {
                return ValueConstants.SEMI_EXPANDED_VALUE;
            }
            if (v == ValueConstants.CONDENSED_VALUE) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXPANDED_VALUE) {
                return ValueConstants.EXTRA_EXPANDED_VALUE;
            }
            if (v == ValueConstants.SEMI_EXPANDED_VALUE) {
                return ValueConstants.EXPANDED_VALUE;
            }
            if (v == ValueConstants.SEMI_CONDENSED_VALUE) {
                return ValueConstants.NORMAL_VALUE;
            }
            if (v == ValueConstants.EXTRA_CONDENSED_VALUE) {
                return ValueConstants.CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXTRA_EXPANDED_VALUE) {
                return ValueConstants.ULTRA_EXPANDED_VALUE;
            }
            if (v == ValueConstants.ULTRA_CONDENSED_VALUE) {
                return ValueConstants.EXTRA_CONDENSED_VALUE;
            }
            return ValueConstants.ULTRA_EXPANDED_VALUE;
        }
        return value;
    }

    /**
     * Implements {@link IdentifierManager#getIdentifiers()}.
     */
    protected StringMap getIdentifiers() {
        return values;
    }
}
