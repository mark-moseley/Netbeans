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
package org.netbeans.modules.visualweb.designer.cssengine;

import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;

/**
 * This class provides a manager for the "list-style-type" CSS property
 *
 * @author Tor Norbye
 */
public class ListStyleTypeManager extends IdentifierManager {

    protected final static StringMap values = new StringMap();
    static {
        values.put(CssConstants.CSS_LOWER_ROMAN_VALUE,
                   CssValueConstants.LOWER_ROMAN_VALUE);
        values.put(CssConstants.CSS_DISC_VALUE,
                   CssValueConstants.DISC_VALUE);
        values.put(CssConstants.CSS_CIRCLE_VALUE,
                   CssValueConstants.CIRCLE_VALUE);
        values.put(CssConstants.CSS_SQUARE_VALUE,
                   CssValueConstants.SQUARE_VALUE);
        values.put(CssConstants.CSS_DECIMAL_VALUE,
                   CssValueConstants.DECIMAL_VALUE);
        values.put(CssConstants.CSS_DECIMAL_LEADING_ZERO_VALUE,
                   CssValueConstants.DECIMAL_LEADING_ZERO_VALUE);
        values.put(CssConstants.CSS_UPPER_ROMAN_VALUE,
                   CssValueConstants.UPPER_ROMAN_VALUE);
        values.put(CssConstants.CSS_LOWER_LATIN_VALUE,
                   CssValueConstants.LOWER_LATIN_VALUE);
        values.put(CssConstants.CSS_UPPER_LATIN_VALUE,
                   CssValueConstants.UPPER_LATIN_VALUE);
        values.put(CssConstants.CSS_NONE_VALUE,
                   CssValueConstants.NONE_VALUE);
        // "lower-alpha" and "upper-alpha" are not part of the CSS2.1
        // spec. But it seems to be used in older documents so we'll
        // support it.
        values.put(CssConstants.CSS_LOWER_ALPHA_VALUE,
                   CssValueConstants.LOWER_ALPHA_VALUE);
        values.put(CssConstants.CSS_UPPER_ALPHA_VALUE,
                   CssValueConstants.UPPER_ALPHA_VALUE);
    }

    public boolean isInheritedProperty() {
        return true;
    }

    public String getPropertyName() {
        return CssConstants.CSS_LIST_STYLE_TYPE_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.DISC_VALUE;
    }

    protected StringMap getIdentifiers() {
        return values;
    }
}
