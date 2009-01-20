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

import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.css.parser.ASCII_CharStream;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.css.parser.CSSParser;
import org.netbeans.modules.css.parser.ParseException;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.css.parser.TokenMgrError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Marek Fukala
 */
public class CSSGSFParser extends Parser {

    private final CSSParser PARSER = new CSSParser();
    private CSSGSFParserResult lastResult = null;

    //string which is substituted instead of any 
    //templating language in case of css embedding
    public static final String GENERATED_CODE = "@@@"; //NOI18N
    private static final String ERROR_MESSAGE_PREFIX = NbBundle.getMessage(CSSGSFParser.class, "unexpected_symbols");

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        List<ParseException> parseExceptions = new ArrayList<ParseException>(1);
        SimpleNode root = null;
        try {
            PARSER.errors().clear();
            PARSER.ReInit(new ASCII_CharStream(new StringReader(snapshot.getText().toString())));
            root = PARSER.styleSheet();
            parseExceptions = PARSER.errors();
        } catch (ParseException pe) {
            parseExceptions.add(pe);
        } catch (TokenMgrError tme) {
            parseExceptions.add(new ParseException(tme.getMessage()));
        }

        List<Error> errors = new ArrayList<Error>();
        errors.addAll(errors(parseExceptions, snapshot.getSource().getFileObject())); //parser errors
        errors.addAll(CssAnalyser.checkForErrors(snapshot, root));
        
        this.lastResult = new CSSGSFParserResult(this, snapshot, root, errors);
    }

        @Override
    public Result getResult(Task task) {
        return lastResult;
    }

    @Override
    public void cancel() {
        //xxx do we need this? can we do that?
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        //no-op, no state changes supported
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        //no-op, no state changes supported
    }

    public List<Error> errors(List<ParseException> parseExceptions, FileObject fo) {
        List<Error> errors = new ArrayList<Error>(parseExceptions.size());
        for (ParseException pe : parseExceptions) {
            Error e = createError(pe, fo);
            if (e != null) {
                errors.add(e);
            }
        }
        return errors;
    }

    public static  boolean containsGeneratedCode(CharSequence text) {
        return CharSequenceUtilities.indexOf(text, GENERATED_CODE) != -1;
    }

    private Error createError(ParseException pe, FileObject fo) {
        Token lastSuccessToken = pe.currentToken;
        if (lastSuccessToken == null) {
            //The pe was created in response to a TokenManagerError
            return new DefaultError(pe.getMessage(), pe.getMessage(), null, fo,
                    0, 0, Severity.ERROR);
        }
        Token errorToken = lastSuccessToken.next;
        int from = errorToken.offset;

        if (!(containsGeneratedCode(lastSuccessToken.image) || containsGeneratedCode(errorToken.image))) {
            String errorMessage = buildErrorMessage(pe);
            return new DefaultError(errorMessage, errorMessage, null, fo,
                    from, from, Severity.ERROR);
        }
        return null;
    }

    private String buildErrorMessage(ParseException pe) {
        StringBuilder buff = new StringBuilder();
        buff.append(ERROR_MESSAGE_PREFIX);

        int maxSize = 0;
        for (int i = 0; i < pe.expectedTokenSequences.length; i++) {
            if (maxSize < pe.expectedTokenSequences[i].length) {
                maxSize = pe.expectedTokenSequences[i].length;
            }
        }

        Token tok = pe.currentToken.next;
        buff.append('"');
        for (int i = 0; i < maxSize; i++) {
            buff.append(tok.image);
            if (i < maxSize - 1) {
                buff.append(',');
                buff.append(' ');
            }
            tok = tok.next;
        }
        buff.append('"');

        return buff.toString();
    }

}
