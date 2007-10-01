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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;

/**
 * Token with a custom text and the token length likely different
 * from text's length.
 * <br/>
 * Token with the custom text cannot be branched by a language embedding.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CustomTextToken<T extends TokenId> extends DefaultToken<T> {
    
    private final CharSequence text; // 28 bytes (24-super + 4)
    
    private final PartType partType; // 32 bytes
    
    /**
     * @param id non-null identification of the token.
     * @param length length of the token.
     * @param text non-null text of the token.
     */
    public CustomTextToken(T id, int length, CharSequence text, PartType partType) {
        super(id, length);
        assert (text != null);
        this.text = text;
        this.partType = partType;
    }
    
    @Override
    public final CharSequence text() {
        return text;
    }
    
    @Override
    protected String dumpInfoTokenType() {
        return "CusT"; // NOI18N "TextToken" or "FlyToken"
    }
    
}
