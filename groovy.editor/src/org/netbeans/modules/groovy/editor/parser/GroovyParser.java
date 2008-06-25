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
import java.net.URL;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.ParserResult.AstTreeNode;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.groovy.editor.AstNodeAdapter;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.GroovyUtils;
import org.netbeans.modules.groovy.editor.GroovyCompilerErrorID;
import org.netbeans.modules.groovy.editor.elements.AstRootElement;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;

/**
 *
 * @author Martin Adamek
 */
class GroovyParser implements Parser {

    private final PositionManager positions = createPositionManager();
    private final Logger LOG = Logger.getLogger(GroovyParser.class.getName());
    private boolean waitJavaScanFinished = true;

    public GroovyParser() {
        // LOG.setLevel(Level.FINEST);
    }

    public void parseFiles(Job job) {
        ParseListener listener = job.listener;
        SourceFileReader reader = job.reader;
        for (ParserFile file : job.files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            listener.started(beginEvent);
            
            ParserResult result = null;

            try {
                CharSequence buffer = reader.read(file);
                String source = asString(buffer);
                int caretOffset = reader.getCaretOffset(file);
                Context context = new Context(file, listener, source, caretOffset, AstUtilities.getBaseDocument(file.getFileObject(), true));
                result = parseBuffer(context, Sanitize.NONE);
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

    void setWaitJavaScanFinished(boolean shouldWait) {
        waitJavaScanFinished = shouldWait;
    }

    protected GroovyParserResult createParseResult(ParserFile file, AstRootElement rootElement, AstTreeNode ast, ErrorCollector errorCollector) {
        
        GroovyParserResult parserResult = new GroovyParserResult(this, file, rootElement, ast, errorCollector);
        
        // Register parsing result with parsing-manager:
        
//        Lookup lkp = Lookup.getDefault();
//        
//        if(lkp != null){
//            GroovyParserManager parserManager = lkp.lookup(GroovyParserManager.class);
//            if(parserManager != null){
//                FileObject fo = file.getFileObject();
//                if (fo != null) {
//                    parserManager.registerParsing(fo, parserResult);
//                }
//            } else {
//                LOG.log(Level.FINEST, "Couldn't get GroovyParserManager from global lookup");
//            }
//        } else {
//            LOG.log(Level.FINEST, "Couldn't get global lookup");
//        }
        
        return parserResult;
    }
    
    private boolean sanitizeSource(Context context, Sanitize sanitizing) {

        if (sanitizing == Sanitize.MISSING_END) {
            context.sanitizedSource = context.source + "}";
            int start = context.source.length();
            context.sanitizedRange = new OffsetRange(start, start+1);
            context.sanitizedContents = "";
            return true;
        }

        int offset = context.caretOffset;

        // Let caretOffset represent the offset of the portion of the buffer we'll be operating on
        if ((sanitizing == Sanitize.ERROR_DOT) || (sanitizing == Sanitize.ERROR_LINE)) {
            offset = context.errorOffset;
        }

        // Don't attempt cleaning up the source if we don't have the buffer position we need
        if (offset == -1) {
            return false;
        }

        // The user might be editing around the given caretOffset.
        // See if it looks modified
        // Insert an end statement? Insert a } marker?
        String doc = context.source;
        int docLength = doc.length(); // since doc will not be modified, we can safely optimize here.
        
        if (offset > docLength) {
            return false;
        }

        if (sanitizing == Sanitize.BLOCK_START) {
            try {
                int start = GroovyUtils.getRowFirstNonWhite(doc, offset);
                if (start != -1 && 
                        start+2 < docLength &&
                        doc.regionMatches(start, "if", 0, 2)) {
                    // TODO - check lexer
                    char c = 0;
                    if (start+2 < docLength) {
                        c = doc.charAt(start+2);
                    }
                    if (!Character.isLetter(c)) {
                        int removeStart = start;
                        int removeEnd = removeStart+2;
                        StringBuilder sb = new StringBuilder(docLength);
                        sb.append(doc.substring(0, removeStart));
                        for (int i = removeStart; i < removeEnd; i++) {
                            sb.append(' ');
                        }
                        if (removeEnd < docLength) {
                            sb.append(doc.substring(removeEnd, docLength));
                        }
                        assert sb.length() == docLength;
                        context.sanitizedRange = new OffsetRange(removeStart, removeEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(removeStart, removeEnd);
                        return true;
                    }
                }
                
                return false;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                return false;
            }
        }
        
        try {
            // Sometimes the offset shows up on the next line
            if (GroovyUtils.isRowEmpty(doc, offset) || GroovyUtils.isRowWhite(doc, offset)) {
                offset = GroovyUtils.getRowStart(doc, offset)-1;
                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!(GroovyUtils.isRowEmpty(doc, offset) || GroovyUtils.isRowWhite(doc, offset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = GroovyUtils.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1 && offset < (docLength - 1)) {
                        StringBuilder sb = new StringBuilder(docLength);
                        int lineStart = GroovyUtils.getRowStart(doc, offset);
                        int rest = lineStart + 2;

                        sb.append(doc.substring(0, lineStart));
                        sb.append("//");

                        if (rest < docLength) {
                            sb.append(doc.substring(rest, docLength));
                        }
                        assert sb.length() == docLength;

                        context.sanitizedRange = new OffsetRange(lineStart, lineEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(lineStart, lineEnd);
                        return true;
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT || sanitizing == Sanitize.EDITED_DOT;
                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineStart = GroovyUtils.getRowStart(doc, offset);
                    int lineEnd = offset-1;
                    
                    /* if the "offset" variable provided above was wrong for
                     * various reasons, one might end up with lineStart > lineEnd
                     */
                    
                    if (lineStart > lineEnd)
                        return false;
                    
                    while (lineEnd >= lineStart && lineEnd < docLength) {
                        if (!Character.isWhitespace(doc.charAt(lineEnd))) {
                            break;
                        }
                        lineEnd--;
                    }
                    if (lineEnd > lineStart) {
                        StringBuilder sb = new StringBuilder(docLength);
                        String line = doc.substring(lineStart, lineEnd + 1);
                        int removeChars = 0;
                        int removeEnd = lineEnd+1;

                        if (line.endsWith(".") || line.endsWith("(")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith(",")) { // NOI18N                            removeChars = 1;
                            removeChars = 1;
                        } else if (line.endsWith(",:")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith(", :")) { // NOI18N
                            removeChars = 3;
                        } else if (line.endsWith(", ")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith("=> :")) { // NOI18N
                            removeChars = 4;
                        } else if (line.endsWith("=>:")) { // NOI18N
                            removeChars = 3;
                        } else if (line.endsWith("=>")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith("::")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith(":")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith("@@")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith("@")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith(",)")) { // NOI18N
                            // Handle lone comma in parameter list - e.g.
                            // type "foo(a," -> you end up with "foo(a,|)" which doesn't parse - but
                            // the line ends with ")", not "," !
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd--;
                        } else if (line.endsWith(", )")) { // NOI18N
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd -= 2;
                        }
                        
                        if (removeChars == 0) {
                            return false;
                        }

                        int removeStart = removeEnd-removeChars;

                        sb.append(doc.substring(0, removeStart));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (removeEnd < docLength) {
                            sb.append(doc.substring(removeEnd, docLength));
                        }
                        assert sb.length() == docLength;

                        context.sanitizedRange = new OffsetRange(removeStart, removeEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(removeStart, removeEnd);
                        return true;
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return false;
    }
    
    @SuppressWarnings("fallthrough")
    private GroovyParserResult sanitize(final Context context,
        final Sanitize sanitizing) {

        switch (sanitizing) {
        case NEVER:
            return createParseResult(context.file, null, null, null);

        case NONE:

            // We've currently tried with no sanitization: try first level
            // of sanitization - removing dots/colons at the edited offset.
            // First try removing the dots or double colons around the failing position
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.EDITED_DOT);
            }

        // Fall through to try the next trick
        case EDITED_DOT:

            // We've tried editing the caret location - now try editing the error location
            // (Don't bother doing this if errorOffset==caretOffset since that would try the same
            // source as EDITED_DOT which has no better chance of succeeding...)
            if (context.errorOffset != -1 && context.errorOffset != context.caretOffset) {
                return parseBuffer(context, Sanitize.ERROR_DOT);
            }

        // Fall through to try the next trick
        case ERROR_DOT:

            // We've tried removing dots - now try removing the whole line at the error position
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.BLOCK_START);
            }
            
        // Fall through to try the next trick
        case BLOCK_START:
            
            // We've tried removing dots - now try removing the whole line at the error position
            if (context.errorOffset != -1) {
                return parseBuffer(context, Sanitize.ERROR_LINE);
            }

        // Fall through to try the next trick
        case ERROR_LINE:

            // Messing with the error line didn't work - we could try "around" the error line
            // but I'm not attempting that now.
            // Finally try removing the whole line around the user editing position
            // (which could be far from where the error is showing up - but if you're typing
            // say a new "def" statement in a class, this will show up as an error on a mismatched
            // "end" statement rather than here
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.EDITED_LINE);
            }

        // Fall through to try the next trick
        case EDITED_LINE:
            return parseBuffer(context, Sanitize.MISSING_END);
            
        // Fall through for default handling
        case MISSING_END:
        default:
            // We're out of tricks - just return the failed parse result
            return createParseResult(context.file, null, null, null);
        }
    }

    @SuppressWarnings("unchecked")
    public GroovyParserResult parseBuffer(final Context context, final Sanitize sanitizing) {
        boolean sanitizedSource = false;
        String source = context.source;
        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            boolean ok = sanitizeSource(context, sanitizing);

            if (ok) {
                assert context.sanitizedSource != null;
                sanitizedSource = true;
                source = context.sanitizedSource;
            } else {
                // Try next trick
                return sanitize(context, sanitizing);
            }
        }
        
        final boolean ignoreErrors = sanitizedSource;

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = -1;
        }
        
        String fileName = "";
        if ((context.file != null) && (context.file.getFileObject() != null)) {
            fileName = context.file.getFileObject().getNameExt();
        }
        
        FileObject fo = context.file.getFileObject();
        ClassPath bootPath = fo == null ? ClassPathSupport.createClassPath(new URL[0]) : ClassPath.getClassPath(fo, ClassPath.BOOT);
        ClassPath compilePath = fo == null ? ClassPathSupport.createClassPath(new URL[0]) : ClassPath.getClassPath(fo, ClassPath.COMPILE);
        ClassPath sourcePath = fo == null ? ClassPathSupport.createClassPath(new URL[0]) : ClassPath.getClassPath(fo, ClassPath.SOURCE);
        ClassPath cp = ClassPathSupport.createProxyClassPath(bootPath, compilePath, sourcePath);
        
        ClassLoader parentLoader = cp.getClassLoader(true);
        
        CompilerConfiguration configuration = new CompilerConfiguration();
        GroovyClassLoader classLoader = new GroovyClassLoader(parentLoader, configuration);
        
        ClasspathInfo cpInfo = ClasspathInfo.create(
                // we should try to load everything by javac instead of classloader,
                // but for now it is faster to use javac only for sources
                ClassPathSupport.createClassPath(new FileObject[] {}),
                ClassPathSupport.createClassPath(new FileObject[] {}),
                sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo);

        CompilationUnit compilationUnit = new NbCompilationUnit(configuration, null, classLoader, javaSource, waitJavaScanFinished);
        InputStream inputStream = new ByteArrayInputStream(source.getBytes());
        compilationUnit.addSource(fileName, inputStream);

//        long start = System.currentTimeMillis();
        try {
            compilationUnit.compile(Phases.CLASS_GENERATION);
//            System.out.println("### compilation success in " + (System.currentTimeMillis() - start));
        } catch (Throwable e) {
//            System.out.println("### compilation failure in " + (System.currentTimeMillis() - start));
            int offset = -1;
            String errorMessage = e.getMessage();
            String localizedMessage = e.getLocalizedMessage();
            
            ErrorCollector errorCollector = compilationUnit.getErrorCollector();
            if (errorCollector.hasErrors()) {
                Message message = errorCollector.getLastError();
                if (message instanceof SyntaxErrorMessage) {
                    SyntaxException se = ((SyntaxErrorMessage)message).getCause();
                    
                    // if you have a single line starting with: "$
                    // SyntaxException.getStartLine() returns 0 instead of 1
                    // we have to fix this here, before ending our life  
                    // in an Assertion in AstUtilities.getOffset().
                    
                    int line = se.getStartLine();
                    
                    if(line < 1 )
                        line = 1;
                    
                    int col = se.getStartColumn();
                    
                    if(col < 1 )
                        col = 1;

                    // display Exception information
//                    LOG.log(Level.FINEST, "-----------------------------------------------");
//                    LOG.log(Level.FINEST, "File: " + context.file.getNameExt());
//                    LOG.log(Level.FINEST, "source: " + source);
//                    LOG.log(Level.FINEST, "getStartLine(): " + line);
//                    LOG.log(Level.FINEST, "getStartColumn(): " + col);

//                    System.out.println("-----------------------------------------------");
//                    System.out.println("File: " + context.file.getNameExt());
//                    System.out.println("Error: " + errorMessage);
//                    System.out.println("Sanitizing: " + sanitizing);
//                    System.out.println("source: " + source);
//                    System.out.println("Source Locator: " + se.getSourceLocator());
//                    System.out.println("getStartLine(): " + line);
//                    System.out.println("getLine(): " + se.getLine());
//                    System.out.println("getStartColumn(): " + col);
                    
                    offset = AstUtilities.getOffset(context.document, line, col);
                    errorMessage = se.getMessage();
                    localizedMessage = se.getLocalizedMessage();
                }
            }
            
            // XXX should this be >, and = length?
            if (offset >= source.length()) {
                offset = source.length() - 1;

                if (offset < 0) {
                    offset = 0;
                }
            }

            /*
             
            This used to be a direct call to notifyError(). Now all calls to 
            notifyError() should be done via handleErrorCollector() below
            to make sure to eliminate duplicates and the like.
            
            I've added the two logging calls only for debugging purposes
            
             */
            
             // if (!ignoreErrors) {
             //      notifyError(context, null, Severity.ERROR, errorMessage, localizedMessage, offset, sanitizing);
             // }
            
            LOG.log(Level.FINEST, "Comp-Ex, errorMessage    : {0}", errorMessage);
            LOG.log(Level.FINEST, "Comp-Ex, localizedMessage: {0}", localizedMessage);
            
        }

        CompileUnit compileUnit = compilationUnit.getAST();
        List<ModuleNode> modules = compileUnit.getModules();
        
        // there are more modules if class references another class,
        // there is one module per class
        ModuleNode module = null;
        for (ModuleNode moduleNode : modules) {
            if (fileName.equals(moduleNode.getContext().getName())) {
                module = moduleNode;
            }
        }

        handleErrorCollector(compilationUnit.getErrorCollector(), context, module, ignoreErrors, sanitizing);
        
        if (module != null) {
            context.sanitized = sanitizing;
            AstRootElement astRootElement = new AstRootElement(context.file.getFileObject(), module);
            AstNodeAdapter ast = new AstNodeAdapter(null, module, context.document);
            GroovyParserResult r = createParseResult(context.file, astRootElement, ast, compilationUnit.getErrorCollector());
            r.setSanitized(context.sanitized, context.sanitizedRange, context.sanitizedContents);
            return r;
        } else {
            return sanitize(context, sanitizing);
        }
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

    private static void notifyError(Context context, String key, Severity severity, String description, String details, 
            int offset, Sanitize sanitizing) {
        notifyError(context, key, severity, description, details, offset, offset, sanitizing);
    }

    private static void notifyError(Context context, String key, Severity severity, String description, String displayName, 
            int startOffset, int endOffset, Sanitize sanitizing) {

        Logger LOG = Logger.getLogger(GroovyParser.class.getName());
        // LOG.setLevel(Level.FINEST);
        LOG.log(Level.FINEST, "---------------------------------------------------");
        LOG.log(Level.FINEST, "key         : {0}\n", key);
        LOG.log(Level.FINEST, "description : {0}\n", description);
        LOG.log(Level.FINEST, "displayName : {0}\n", displayName);
        LOG.log(Level.FINEST, "startOffset : {0}\n", startOffset);
        LOG.log(Level.FINEST, "endOffset   : {0}\n", endOffset);
        
        // FIXME: we silently drop errors which have no description here.
        // There might be still a way to recover.
        if(description == null) {
            LOG.log(Level.FINEST, "dropping error");
            return;
        }
        
        // TODO: we might need a smarter way to provide a key in the long run.
        if (key == null) {
            key = description;
        }

        // We gotta have a display name.
        if (displayName == null) {
            displayName = description;
        }
        
        Error error =
            new GroovyError(key, displayName, description, context.file.getFileObject(),
                startOffset, endOffset, severity, getIdForErrorMessage(description));
        
        context.listener.error(error);

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = startOffset;
        }
    }

    static GroovyCompilerErrorID getIdForErrorMessage(String errorMessage) {
        String ERR_PREFIX = "unable to resolve class "; // NOI18N

        if (errorMessage != null) {
            if (errorMessage.startsWith(ERR_PREFIX)) {
                return GroovyCompilerErrorID.CLASS_NOT_FOUND;
            }
        }

        return GroovyCompilerErrorID.UNDEFINED;
    }
    
    
    
    private void handleErrorCollector(ErrorCollector errorCollector, Context context, ModuleNode moduleNode, boolean ignoreErrors, Sanitize sanitizing) {
        LOG.log(Level.FINEST, "handleErrorCollector()");
        if (!ignoreErrors && errorCollector != null) {
            List errors = errorCollector.getErrors();
            if (errors != null) {
                for (Object object : errors) {
                    LOG.log(Level.FINEST, "Error found in collector: {0}", object);
                    if (object instanceof SyntaxErrorMessage) {
                        SyntaxException ex = ((SyntaxErrorMessage)object).getCause();
                        
                        String sourceLocator = ex.getSourceLocator();
                        String name = moduleNode != null ? moduleNode.getContext().getName() : context.file.getNameExt();
                        
                        if (sourceLocator != null && name != null && sourceLocator.equals(name)) {
                            int startLine = ex.getStartLine();
                            int startColumn = ex.getStartColumn();
                            int line = ex.getLine();
                            int endColumn = ex.getEndColumn();
                            int startOffset = AstUtilities.getOffset(context.document, startLine > 0 ? startLine : 1, startColumn > 0 ? startColumn : 1);
                            int endOffset = AstUtilities.getOffset(context.document, line > 0 ? line : 1, endColumn > 0 ? endColumn : 1);
                            notifyError(context, null, Severity.ERROR, ex.getMessage(), null, startOffset, endOffset, sanitizing);
                        }
                    } else if (object instanceof SimpleMessage) {
                        String message = ((SimpleMessage)object).getMessage();
                        notifyError(context, null, Severity.ERROR, message, null, -1, sanitizing);
                    } else {
                        notifyError(context, null, Severity.ERROR, "Error", null, -1, sanitizing);
                    }
                }
            }
        }
    }
    
    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER, 
        /** Perform no sanitization */
        NONE, 
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT, 
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT, 
        /** Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START,
        /** Try to cut out the error line */
        ERROR_LINE, 
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
        /** Attempt to add an "end" to the end of the buffer to make it compile */
        MISSING_END,
    }

    /** Parsing context */
    public static final class Context {
        private final ParserFile file;
        private final ParseListener listener;
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
        private BaseDocument document;
        
        public Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset, BaseDocument doc) {
            this.file = parserFile;
            this.listener = listener;
            this.source = source;
            this.caretOffset = caretOffset;
            this.document = doc;//AstUtilities.getBaseDocument(file.getFileObject(), true);
        }
        
        @Override
        public String toString() {
            return "GroovyParser.Context(" + file.toString() + ")"; // NOI18N
        }
        
        public OffsetRange getSanitizedRange() {
            return sanitizedRange;
        }

        public Sanitize getSanitized() {
            return sanitized;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }
        
        public int getErrorOffset() {
            return errorOffset;
        }
    }
    
}
