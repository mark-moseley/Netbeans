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

import org.netbeans.modules.css.parser.CssParserResultHolder;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.css.parser.CssParserAccess;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.gsf.api.TranslatedSource;

/**
 *
 * @author Marek Fukala
 */
public class CSSGSFParser implements Parser, PositionManager {

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String) sequence;
        } else {
            return sequence.toString();
        }
    }

    public void parseFiles(Job job) {
        List<ParserFile> files = job.files;

        for (ParserFile file : files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            job.listener.started(beginEvent);

            CSSParserResult result = null;

            try {
                CssParserAccess.CssParserResult css_result = null;
                
                //test if the translated source is up-to-date and if so 
                //use cached parser result created during sanitization of the source
                if (job.translatedSource instanceof CssParserResultHolder) {
                    css_result = ((CssParserResultHolder) job.translatedSource).result();
                }
                
                if(css_result == null) {
                    CharSequence buffer = job.reader.read(file);
                    String source = asString(buffer);

                    CssParserAccess parserAccess = CssParserAccess.getDefault();
                    css_result = parserAccess.parse(new StringReader(source));
                }

                result = new CSSParserResult(this, file, css_result.root());

                for (Error error : css_result.errors(file)) {
                    job.listener.error(error);
                }

                //do some semantic checking of the parse tree
                List<Error> semanticErrors = new CssAnalyser(result).checkForErrors(css_result.root());
                for (Error err : semanticErrors) {
                    job.listener.error(err);
                }

            } catch (IOException ioe) {
                job.listener.exception(ioe);
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            job.listener.finished(doneEvent);
        }
    }

    public PositionManager getPositionManager() {
        return this;
    }

    public OffsetRange getOffsetRange(CompilationInfo info, ElementHandle object) {
        if (object instanceof CssElementHandle) {
            ParserResult presult = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
            final TranslatedSource source = presult.getTranslatedSource();
            SimpleNode node = ((CssElementHandle) object).node();
            return new OffsetRange(AstUtils.documentPosition(node.startOffset(), source), AstUtils.documentPosition(node.endOffset(), source));
        } else {
            throw new IllegalArgumentException((("Foreign element: " + object + " of type " +
                    object) != null) ? object.getClass().getName() : "null"); //NOI18N
        }
    }
}
