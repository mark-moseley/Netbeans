/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.gsf;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
public class CssAstElement extends CSSElement {

    private SimpleNode node;

    public static CssAstElement createElement(SimpleNode node) {
        return new CssAstElement(node);
    }
    
    CssAstElement(SimpleNode node) {
        super(node.image());
        this.node = node;
    }

    public SimpleNode node() {
        return node;
    }

    /** Note(I): the css structure itema are renewed after each modification so we can
     * return the cached offset range here instead of searching the new parser
     * result. The only problem may happen if someone modifies the source
     * and very quickly doubleclicks the navigator before it gets refreshed.
     *
     * TODO: fix this so we resolve this element to the new element in the fresh result.
     */
    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        Snapshot s = result.getSnapshot();
        int from = node.startOffset();
        int to = node.endOffset();

        if(s.getText().length() == 0) {
            return null;
        }

        //check the boundaries bacause of (I)
        int origFrom = from > s.getText().length() ? 0 : s.getOriginalOffset(from);
        int origTo = to > s.getText().length() ? 0 : s.getOriginalOffset(to);

        if(origFrom == origTo || origTo == 0) {
            return null;
        }
        
        return new OffsetRange(origFrom, origTo);
                
                
    }



}
