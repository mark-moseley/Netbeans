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

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Represents the "padding" shorthand property for setting
 * padding-left, padding-right, padding-top and padding-bottom.
 *
 * @author Tor Norbye
 */
public class PaddingShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {

    public String getPropertyName() {
        return CssConstants.CSS_PADDING_PROPERTY;
    }

    /** Set the values. This is a bit complicated since the number
     * of "arguments" in the value determines how we distribute
     * the children.
     */
    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {

        LexicalUnit first = lu;
        LexicalUnit second = first.getNextLexicalUnit();
        if (second == null) {
            // Only one value specified
            // 1 value: applies to all four sides
            ph.property(CssConstants.CSS_PADDING_LEFT_PROPERTY,
                        first, imp);
            ph.property(CssConstants.CSS_PADDING_RIGHT_PROPERTY,
                        first, imp);
            ph.property(CssConstants.CSS_PADDING_TOP_PROPERTY,
                        first, imp);
            ph.property(CssConstants.CSS_PADDING_BOTTOM_PROPERTY,
                        first, imp);
        } else {
            LexicalUnit third = second.getNextLexicalUnit();
            if (third == null) {
                // Only two values specified

                // 2 values: (1) top & bottom  (2) left & right
                ph.property(CssConstants.CSS_PADDING_TOP_PROPERTY,
                            first, imp);
                ph.property(CssConstants.CSS_PADDING_BOTTOM_PROPERTY,
                            first, imp);
                ph.property(CssConstants.CSS_PADDING_LEFT_PROPERTY,
                            second, imp);
                ph.property(CssConstants.CSS_PADDING_RIGHT_PROPERTY,
                            second, imp);
            } else {
                LexicalUnit fourth = third.getNextLexicalUnit();
                if (fourth == null) {
                    // Only three values specified

                    // 3 values: (1) top, (2) left & right, (3) bottom
                    ph.property(CssConstants.CSS_PADDING_TOP_PROPERTY,
                                first, imp);
                    ph.property(CssConstants.CSS_PADDING_LEFT_PROPERTY,
                                second, imp);
                    ph.property(CssConstants.CSS_PADDING_RIGHT_PROPERTY,
                                second, imp);
                    ph.property(CssConstants.CSS_PADDING_BOTTOM_PROPERTY,
                                third, imp);
                } else {
                    // 4 values: (1) top, (2) right, (3) bottom, (4) left
                    ph.property(CssConstants.CSS_PADDING_TOP_PROPERTY,
                                first, imp);
                    ph.property(CssConstants.CSS_PADDING_RIGHT_PROPERTY,
                                second, imp);
                    ph.property(CssConstants.CSS_PADDING_BOTTOM_PROPERTY,
                                third, imp);
                    ph.property(CssConstants.CSS_PADDING_LEFT_PROPERTY,
                                fourth, imp);
                }
            }
        }
    }
}
