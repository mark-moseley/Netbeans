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

package org.netbeans.modules.groovy.editor.parser;

import groovy.lang.GroovyClassLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementHandle;
import org.netbeans.api.gsf.OccurrencesFinder;
import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.PositionManager;
import org.netbeans.api.gsf.SemanticAnalyzer;
import org.netbeans.api.gsf.Severity;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.modules.groovy.editor.AstNodeAdapter;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.elements.AstRootElement;
import org.netbeans.spi.gsf.DefaultError;
import org.netbeans.spi.gsf.DefaultPosition;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class GroovyParser implements Parser {

    private final PositionManager positions = createPositionManager();

    public void parseFiles(List<ParserFile> files, ParseListener listener, SourceFileReader reader) {
        for (ParserFile file : files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            listener.started(beginEvent);
            
            ParserResult result = null;

            try {
                CharSequence buffer = reader.read(file);
                String source = asString(buffer);
                int caretOffset = reader.getCaretOffset(file);
                Context context = new Context(file, listener, source, caretOffset);
                result = parseBuffer(context);
            } catch (IOException ioe) {
                listener.exception(ioe);
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            listener.finished(doneEvent);
        }
    }

    public PositionManager getPositionManager() {
        return positions;
    }

    public SemanticAnalyzer getSemanticAnalysisTask() {
        return new GroovySemanticAnalyzer();
    }

    public OccurrencesFinder getMarkOccurrencesTask(int caretPosition) {
        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
        finder.setCaretPosition(caretPosition);

        return finder;
    }

    public <T extends Element> ElementHandle<T> createHandle(CompilationInfo info, T element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends Element> T resolveHandle(CompilationInfo info, ElementHandle<T> handle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("unchecked")
    public GroovyParserResult parseBuffer(final Context context) {

        String fileName = "";
        if ((context.file != null) && (context.file.getFileObject() != null)) {
            fileName = context.file.getFileObject().getNameExt();
        }
        
        FileObject fo = context.file.getFileObject();

        ClassLoader parentClassLoader = this.getClass().getClassLoader();
        GroovyClassLoader classLoader = new GroovyClassLoader(parentClassLoader);

        CompilerConfiguration configuration = new CompilerConfiguration();
        CompilationUnit compilationUnit = new CompilationUnit(configuration, null, classLoader);
        InputStream inputStream = new ByteArrayInputStream(context.source.getBytes());
        compilationUnit.addSource(fileName, inputStream);

        try {
//            compilationUnit.compile(Phases.SEMANTIC_ANALYSIS); // which phase should be used?
            compilationUnit.compile(); // which phase should be used?
        } catch (Exception e) {
        }

        handleErrorCollector(compilationUnit.getErrorCollector(), context);
        
        CompileUnit compileUnit = compilationUnit.getAST();
        List<ModuleNode> modules = compileUnit.getModules();

        if (modules.size() == 1) {
            AstRootElement astRootElement = new AstRootElement(context.file.getFileObject(), modules.get(0));
            AstNodeAdapter ast = new AstNodeAdapter(null, modules.get(0), context.source);
            GroovyParserResult r = new GroovyParserResult(context.file, astRootElement, ast);
            return r;
        }
        return new GroovyParserResult(context.file, null, null);
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }

    private PositionManager createPositionManager() {
        return new GroovyPositionManager();
    }

    private static void notifyError(Context context, String key, Severity severity, String description, String details, int offset) {
        notifyError(context, key, severity, description, details, offset, offset);
    }

    private static void notifyError(Context context, String key,
        Severity severity, String description, String details, int startOffset, int endOffset) {
        // Initialize keys for errors needing it
        if (key == null) {
            key = description;
        }
        
        org.netbeans.api.gsf.Error error =
            new DefaultError(key, description, details, context.file.getFileObject(),
                new DefaultPosition(startOffset), new DefaultPosition(endOffset), severity);
        context.listener.error(error);

        context.errorOffset = startOffset;
    }

    private static void handleErrorCollector(ErrorCollector errorCollector, Context context) {
        if (errorCollector != null) {
            List errors = errorCollector.getErrors();
            if (errors != null) {
                for (Object object : errors) {
                    if (object instanceof SyntaxErrorMessage) {
                        SyntaxException ex = ((SyntaxErrorMessage)object).getCause();
                        int startOffset = AstUtilities.getOffset(context.source, ex.getStartLine(), ex.getStartColumn());
                        int endOffset = AstUtilities.getOffset(context.source, ex.getLine(), ex.getEndColumn());
                        notifyError(context, null, Severity.ERROR, ex.getMessage(), null, startOffset, endOffset);
                    } else if (object instanceof SimpleMessage) {
                        String message = ((SimpleMessage)object).getMessage();
                        notifyError(context, null, Severity.ERROR, message, null, -1);
                    } else {
                        notifyError(context, null, Severity.ERROR, "Error", null, -1);
                    }
                }
            }
        }
    }
    
    /** Parsing context */
    public static final class Context {
        private final ParserFile file;
        private final ParseListener listener;
        private int errorOffset;
        private String source;
        private int caretOffset;
        
        public Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset) {
            this.file = parserFile;
            this.listener = listener;
            this.source = source;
            this.caretOffset = caretOffset;

        }
        
        @Override
        public String toString() {
            return "GroovyParser.Context(" + file.toString() + ")"; // NOI18N
        }
        
        public int getErrorOffset() {
            return errorOffset;
        }
    }

}
