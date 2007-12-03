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

package org.netbeans.modules.cnd.highlight;

import javax.swing.text.Document;
import org.netbeans.modules.cnd.highlight.semantic.MarkOccurrencesHighlighter;
import org.netbeans.modules.cnd.highlight.semantic.SemanticHighlighter;
import org.netbeans.modules.cnd.highlight.semantic.ifdef.*;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Sergey Grinev
 */
public class CppHighlightsLayerFactory implements HighlightsLayerFactory {
    
    public static InactiveCodeHighlighter getInactiveCodeHighlighter(Document doc) {
        InactiveCodeHighlighter ich = (InactiveCodeHighlighter)doc.getProperty(InactiveCodeHighlighter.class);
        if (ich == null)
        {
            doc.putProperty(InactiveCodeHighlighter.class, ich = new InactiveCodeHighlighter(doc));
        }
        return ich;
    }

    /*public static MarkOccurrencesHighlighter getMarkOccurencesHighlighter(Document doc) {
        MarkOccurrencesHighlighter ich = (MarkOccurrencesHighlighter)doc.getProperty(MarkOccurrencesHighlighter.class);
        if (ich == null)
        {
            doc.putProperty(MarkOccurrencesHighlighter.class, ich = new MarkOccurrencesHighlighter(doc));
        }
        return ich;
    }*/

    public HighlightsLayer[] createLayers(Context context) {
        return InactiveCodeHighlighter.USE_MORE_SEMANTIC ? 
            new HighlightsLayer[] {
                HighlightsLayer.create(
                    SemanticHighlighter.class.getName(), 
                    ZOrder.SYNTAX_RACK.forPosition(2000),
                    true,
                    SemanticHighlighter.getHighlightsBag(context.getDocument())),
                HighlightsLayer.create(
                    MarkOccurrencesHighlighter.class.getName(), 
                    ZOrder.CARET_RACK.forPosition(1000),
                    true,
                    MarkOccurrencesHighlighter.getHighlightsBag(context.getDocument())),
            } : 
            new HighlightsLayer[] {    
                HighlightsLayer.create(
                    InactiveCodeHighlighter.class.getName(), 
                    ZOrder.SYNTAX_RACK.forPosition(2000),
                    true,
                    getInactiveCodeHighlighter(context.getDocument()).getHighlightsBag())};

    }

}
