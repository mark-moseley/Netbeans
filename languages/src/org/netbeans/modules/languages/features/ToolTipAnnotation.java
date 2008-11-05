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

package org.netbeans.modules.languages.features;


import java.util.Collections;
import java.util.Iterator;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParserResult;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class ToolTipAnnotation extends Annotation {

    static final String TOOLTIP = "TOOLTIP";
    
    public String getShortDescription () {
        try {
            Part lp = (Part) getAttachedAnnotatable();
            if (lp == null) return null;
            Line line = lp.getLine ();
            DataObject dob = DataEditorSupport.findDataObject (line);
            EditorCookie ec = dob.getCookie (EditorCookie.class);
            final NbEditorDocument document = (NbEditorDocument) ec.getDocument ();
            String mimeType = (String) document.getProperty ("mimeType");
            final int offset = NbDocument.findLineOffset ( 
                    ec.getDocument (),
                    lp.getLine ().getLineNumber ()
                ) + lp.getColumn ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
            if (tokenHierarchy == null) return null;
            final Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
            document.readLock ();
            try {
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
                if (tokenSequence == null)
                    return null;
                tokenSequence.move (offset);
                if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) return null;
                Token token = tokenSequence.token ();
                Feature tooltip = language.getFeatureList ().getFeature (TOOLTIP, token.id ().name ());
                if (tooltip != null) {
                    String s = c ((String) tooltip.getValue (Context.create (document, offset)));
                    return s;
                }
            } finally {
                document.readUnlock ();
            }
            Source source = Source.create (document);
            try {
                ParserManager.parse (Collections.<Source>singleton (source), new UserTask () {
                    @Override
                    public void run (ResultIterator resultIterator) throws ParseException {
                        ParserResult parserResult = (ParserResult) resultIterator.getParserResult ();
                        ASTNode ast = parserResult.getRootNode ();
                        ASTPath path = ast.findPath (offset);
                        if (path == null) return;
                        int i, k = path.size ();
                        for (i = 0; i < k; i++) {
                            ASTPath p = path.subPath (i);
                            Feature tooltip = language.getFeatureList ().getFeature (TOOLTIP, p);
                            if (tooltip == null) continue;
                            String s = c ((String) tooltip.getValue (SyntaxContext.create (document, p)));
    //                        firePropertyChange (s, path, path)
                            //!return s;
                        }
                        Iterator<SyntaxError> it = parserResult.getSyntaxErrors ().iterator ();
                        while (it.hasNext ()) {
                            SyntaxError syntaxError = it.next ();
                            ASTItem item = syntaxError.getItem ();
                            if (item.getOffset () == ast.getEndOffset ())
                                item = ast.findPath (item.getOffset () - 1).getLeaf ();
                            if (item.getOffset () > offset) break;
                            if (item.getEndOffset () > offset) {
                                //!return syntaxError.getMessage ();
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                ex.printStackTrace ();
            }
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        return null;
    }

    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }
    
    private static String c (String s) {
        if (s == null) return null;
        s = s.replace ("\\n", "\n");
        s = s.replace ("\\r", "\r");
        s = s.replace ("\\t", "\t");
        return s;
    }
}

