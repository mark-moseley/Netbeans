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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.dbgp.annotations;

import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.dbgp.breakpoints.LineBreakpoint;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotatable;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class BrkpntAnnotation extends BreakpointAnnotation {

    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint";     // NOI18N

    private static final String BREAKPOINT                = "ANTN_BREAKPOINT";// NOI18N

    private Breakpoint breakpoint;

    public BrkpntAnnotation( Annotatable annotatable, Breakpoint breakpoint ) {
        this.breakpoint = breakpoint;
        attach(annotatable);
    }

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getAnnotationType()
     */
    @Override
    public String getAnnotationType() {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
            Line line = lineBreakpoint.getLine();
            DataObject dataObject = DataEditorSupport.findDataObject(line);
            EditorCookie editorCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
            StyledDocument document = editorCookie.getDocument();
            if (document != null) {
                int offset = NbDocument.findLineOffset(document, line.getLineNumber());
                int l = line.getLineNumber();
                int col = NbDocument.findLineColumn(document, offset);
                Element lineElem = NbDocument.findLineRootElement(document).getElement(l);
                int startOffset = lineElem.getStartOffset();
                int endOffset = lineElem.getEndOffset();
                TokenHierarchy th = TokenHierarchy.get(document);
                TokenSequence<TokenId> ts = th.tokenSequence();
                boolean isValid = false;
                ts.move(startOffset);
                ts.moveNext();
                for (; !isValid && ts.offset() < endOffset;) {
                    TokenId id = ts.token().id();
                    isValid = id != PHPTokenId.T_INLINE_HTML;
                    if (!ts.moveNext()) {
                        break;
                    }
                }
                if (!isValid) {
                    lineBreakpoint.setInvalid(null);
                } else {
                    lineBreakpoint.setValid(null);
                }
            }
        }
        return (breakpoint.getValidity() == Breakpoint.VALIDITY.INVALID) ?
            BREAKPOINT_ANNOTATION_TYPE+"_broken" : BREAKPOINT_ANNOTATION_TYPE;//NOI18N
    }

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getShortDescription()
     */
    @Override
    public String getShortDescription()
    {
        return NbBundle.getBundle(DebuggerAnnotation.class).getString(BREAKPOINT);
    }

    @Override
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

}
