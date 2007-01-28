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

import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.CSSConstants;

/**
 * This class provides a manager for the 'display' property values.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class DisplayManager extends IdentifierManager {
    
    /**
     * The identifier values.
     */
    protected final static StringMap values = new StringMap();
    static {
	values.put(CSSConstants.CSS_BLOCK_VALUE,
                   ValueConstants.BLOCK_VALUE);
// BEGIN RAVE MODIFICATIONS
// What is "compact" doing here? Not CSS2!
// Ditto for Marker
// END RAVE MODIFICATIONS
	values.put(CSSConstants.CSS_COMPACT_VALUE,
                   ValueConstants.COMPACT_VALUE);
	values.put(CSSConstants.CSS_INLINE_VALUE,
                   ValueConstants.INLINE_VALUE);
	values.put(CSSConstants.CSS_INLINE_TABLE_VALUE,
                   ValueConstants.INLINE_TABLE_VALUE);
	values.put(CSSConstants.CSS_LIST_ITEM_VALUE,
                   ValueConstants.LIST_ITEM_VALUE);
	values.put(CSSConstants.CSS_MARKER_VALUE,
                   ValueConstants.MARKER_VALUE);
	values.put(CSSConstants.CSS_NONE_VALUE,
                   ValueConstants.NONE_VALUE);
	values.put(CSSConstants.CSS_RUN_IN_VALUE,
                   ValueConstants.RUN_IN_VALUE);
	values.put(CSSConstants.CSS_TABLE_VALUE,
                   ValueConstants.TABLE_VALUE);
	values.put(CSSConstants.CSS_TABLE_CAPTION_VALUE,
                   ValueConstants.TABLE_CAPTION_VALUE);
	values.put(CSSConstants.CSS_TABLE_CELL_VALUE,
                   ValueConstants.TABLE_CELL_VALUE);
	values.put(CSSConstants.CSS_TABLE_COLUMN_VALUE,
                   ValueConstants.TABLE_COLUMN_VALUE);
	values.put(CSSConstants.CSS_TABLE_COLUMN_GROUP_VALUE,
                   ValueConstants.TABLE_COLUMN_GROUP_VALUE);
	values.put(CSSConstants.CSS_TABLE_FOOTER_GROUP_VALUE,
                   ValueConstants.TABLE_FOOTER_GROUP_VALUE);
	values.put(CSSConstants.CSS_TABLE_HEADER_GROUP_VALUE,
                   ValueConstants.TABLE_HEADER_GROUP_VALUE);
	values.put(CSSConstants.CSS_TABLE_ROW_VALUE,
                   ValueConstants.TABLE_ROW_VALUE);
	values.put(CSSConstants.CSS_TABLE_ROW_GROUP_VALUE,
                   ValueConstants.TABLE_ROW_GROUP_VALUE);

// BEGIN RAVE MODIFICATIONS
	values.put(CSSConstants.CSS_INLINE_BLOCK_VALUE,
                   ValueConstants.INLINE_BLOCK_VALUE);
// END RAVE MODIFICATIONS
    }

    /**
     * Implements {@link
     * org.apache.batik.css.engine.value.ValueManager#isInheritedProperty()}.
     */
    public boolean isInheritedProperty() {
	return false;
    }

    /**
     * Implements {@link
     * org.apache.batik.css.engine.value.ValueManager#getPropertyName()}.
     */
    public String getPropertyName() {
	return CSSConstants.CSS_DISPLAY_PROPERTY;
    }
    
    /**
     * Implements {@link
     * org.apache.batik.css.engine.value.ValueManager#getDefaultValue()}.
     */
    public Value getDefaultValue() {
        return ValueConstants.INLINE_VALUE;
    }

    /**
     * Implements {@link IdentifierManager#getIdentifiers()}.
     */
    protected StringMap getIdentifiers() {
        return values;
    }
}
