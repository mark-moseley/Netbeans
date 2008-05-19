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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.mozilla.javascript.Node;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;
import org.netbeans.modules.javascript.editing.lexer.JsCommentLexer;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * Index Ruby structure into the persistent store for retrieval by
 * {@link JsIndex}.
 * 
 * @todo Index methods as func.in and then distinguish between exact completion and multi-completion.
 * @todo Ensure that all the stub files are compileable!
 * @todo Should I perhaps store globals and functions using the same query prefix (since I typically
 *    have to search for both anyway) ? Or perhaps not - not when doing inherited checks...
 * @todo Index file inclusion dependencies! (Uh oh - that means I -do- have to do models for HTML, etc. right?
 *     Or can I perhaps only compute that stuff live?
 * @todo Use the JsCommentLexer to pull out relevant attributes -- @private and such -- and set these
 *     as function attributes.
 * @todo There are duplicate elements -- why???
 * @todo jquery: Handle this:
 *        // Attach a bunch of functions for handling common AJAX events
 *        jQuery.each( "ajaxStart,ajaxStop,ajaxComplete,ajaxError,ajaxSuccess,ajaxSend".split(","), function(i,o){
 *                jQuery.fn[o] = function(f){
 *    jQuery.each( ("blur,focus,load,resize,scroll,unload,click,dblclick," +
 *            "mousedown,mouseup,mousemove,mouseover,mouseout,change,select," + 
 *            "submit,keydown,keypress,keyup,error").split(","), function(i, name){
 * 
 * @todo jquery- needs to preindex jquery: the stuff to register "ready" and "load" there
 *    on document and element classes.
 * 
 * @author Tor Norbye
 */
public class JsIndexer implements Indexer {
    static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    
    // I need to be able to search several things:
    // (1) by function root name, e.g. quickly all functions that start
    //    with "f" should find unknown.foo.
    // (2) by namespace, e.g. I should be able to quickly find all
    //    "foo.bar.b*" functions
    // (3) constructors
    // (4) global variables, preferably in the same way
    // (5) extends so I can do inheritance inclusion!

    // Solution: Store the following:
    // class:name for each class
    // extend:old:new for each inheritance? Or perhaps do this in the class entry
    // fqn: f.q.n.function/global;sig; for each function
    // base: function;fqn;sig
    // The signature should look like this:
    // ;flags;;args;offset;docoffset;browsercompat;types;
    // (between flags and args you have the case sensitive name for flags)

    static final String FIELD_FQN = "fqn"; //NOI18N
    static final String FIELD_BASE = "base"; //NOI18N
    static final String FIELD_EXTEND = "extend"; //NOI18N
    static final String FIELD_CLASS = "clz"; //NOI18N
    
    private FileObject cachedFo;
    private boolean cachedIndexable;
    
    public String getIndexVersion() {
        return "6.115"; // NOI18N
    }

    public String getIndexerName() {
        return "javascript"; // NOI18N
    }
    
    public boolean isIndexable(ParserFile file) {
        String extension = file.getExtension();

        if (extension.equals("json")) {
            // json: not indexed
            // TODO - skip this check
            return false;
        }
        if (extension.equals("html")) {
            if (file.getNameExt().equals("DataTable.js.html")) {
                // Large file from YUI, skip
                return false;
            }
            return true;
        } else if (extension.equals("rhtml") || extension.equals("jsp") || extension.equals("php")) { // NOI18N
            return true;
        } else if (extension.equals("js"))  {
            String name = file.getNameExt();

            // Yahoo file that is always minimized and not uaually needed - it's an alias for 
            // other stuff
            if (name.equals("utilities.js")) {
                String relative = file.getRelativePath();
                if (relative != null && relative.indexOf("yui") != -1) { // NOI18N
                    return false;
                }
            }
            
            // Avoid double-indexing files that have multiple versions - e.g. foo.js and foo-min.js
            // or foo.uncompressed
            FileObject fo = file.getFileObject();
            if (fo == null) {
                return true;
            }
            if (name.endsWith("min.js") && name.length() > 6 && !Character.isLetter(name.charAt(name.length()-7))) { // NOI18N
                // See if we have a corresponding "un-min'ed" version in the same directory;
                // if so, skip it
                // Subtrack out the -min part
                name = name.substring(0, name.length()-7); // NOI18N
                if (fo.getParent().getFileObject(name, "js") != null) { // NOI18N
                    // The file has been deleted
                    // I still need to return yes here such that the file is deleted from the index.
                    return false;
                }
            } else {
                // PENDING:  http://code.google.com/p/jqueryjs/    -- uses ".min.js" instead of "-min.js"; also has .pack.js
                
                // See if we have -uncompressed or -debug - prefer these over the compressed or non-debug versions
                // TODO - just check for hardcoded "dojo.uncompressed" since that's the common thing? Also crosscheck
                // this list with the common JavaScript frameworks and make sure we hit all the major patterns
                // (Perhaps hardcode the list). It would be good if we could check multiple of the loadpath directories
                // too, not just the same directory since there's a good likelihood (with the library manager) you
                // have these in different dirs.
                FileObject parent = fo.getParent();
                if (parent == null) {
                    // Unlikely but let's play it safe
                    return true;
                }
                if (!name.endsWith(".uncompressed.js")) { // NOI18N
                    String base = name.substring(0, name.length()-3);
                    if (parent.getFileObject(base + ".uncompressed", "js") != null) { // NOI18N
                        return false;
                    }
                }
                if (!name.endsWith("-debug.js")) { // NOI18N
                    String base = name.substring(0, name.length()-3);
                    if (parent.getFileObject(base + "-debug", "js") != null) { // NOI18N
                        return false;
                    }
                }
                
                // From here on, no per-file information is checked; these apply to all files in the
                // same directory (e.g. all files in javascript are skipped if there is a corresponding
                // sibling javascript_uncompressed, and similarly, if there is an everything.sdoc file,
                // all the files are skipped in the directory.
                if (parent == cachedFo) {
                    return cachedIndexable;
                }
                cachedFo = parent;
                if (parent.getFileObject("everything", "sdoc") != null) {
                    cachedIndexable = false;
                    return false;
                }
                for (int i = 0; i <= 3 && parent != null; i++, parent = parent.getParent()) {
                    if (parent.getName().equals("javascript")) { // NOI18N
                        // Webui has a convention where they place the uncompressed files in a parallel directory
                        FileObject grandParent = parent.getParent();
                        if (grandParent != null) {
                            if (grandParent.getFileObject("javascript_uncompressed") != null) { // NOI18N
                                cachedIndexable = false;
                                return false;
                            }
                        }
                        break;
                    }
                }
                cachedIndexable = true;
            }
            
            return true;
        } else if (extension.equals("sdoc")) {
            // sdoc indexing
            return true;
        }
        
        return false;
    }
    
    public boolean acceptQueryPath(String url) {
        return url.indexOf("/ruby2/") == -1 && url.indexOf("/gems/") == -1 && url.indexOf("lib/ruby/") == -1; // NOI18N
    }

    public String getPersistentUrl(File file) {
        String url;
        try {
            url = file.toURI().toURL().toExternalForm();
            // Make relative URLs for urls in the libraries
            return JsIndex.getPreindexUrl(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return file.getPath();
        }

    }

    public List<IndexDocument> index(ParserResult result, IndexDocumentFactory factory) throws IOException {
        JsParseResult r = (JsParseResult)result;
        Node root = r.getRootNode();

        if (root == null && !result.getFile().getExtension().equals("sdoc")) { // NOI18N
            return null;
        }

        TreeAnalyzer analyzer = new TreeAnalyzer(r, factory);
        analyzer.analyze();
        
        return analyzer.getDocuments();
    }
    
    private static class TreeAnalyzer {
        private final ParserFile file;
        private String url;
        private final JsParseResult result;
        private BaseDocument doc;
        private IndexDocumentFactory factory;
        private List<IndexDocument> documents = new ArrayList<IndexDocument>();
        
        private TreeAnalyzer(JsParseResult result, IndexDocumentFactory factory) {
            this.result = result;
            this.file = result.getFile();
            this.factory = factory;
        }

        List<IndexDocument> getDocuments() {
            return documents;
        }

        public void analyze() throws IOException {
            FileObject fo = file.getFileObject();
            if (result.getInfo() != null) {
                this.doc = LexUtilities.getDocument(result.getInfo(), true);
            } else {
                // openide.loaders/src/org/openide/text/DataEditorSupport.java
                // has an Env#inputStream method which posts a warning to the user
                // if the file is greater than 1Mb...
                //SG_ObjectIsTooBig=The file {1} seems to be too large ({2,choice,0#{2}b|1024#{3} Kb|1100000#{4} Mb|1100000000#{5} Gb}) to safely open. \n\
                //  Opening the file could cause OutOfMemoryError, which would make the IDE unusable. Do you really want to open it?
                // I don't want to try indexing these files... (you get an interactive
                // warning during indexing
                if (fo.getSize () > 1024 * 1024) {
                    return;
                }
                
                this.doc = NbUtilities.getBaseDocument(fo, true);
            }

            try {
                url = fo.getURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = JsIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            if (file.getExtension().equals("sdoc")) { // NOI18N
                indexScriptDoc(doc, null);
                return;
            }
            
            AnalysisResult ar = result.getStructure();
            List<?extends AstElement> children = ar.getElements();

            if ((children == null) || (children.size() == 0)) {
                return;
            }
            
            if (url.endsWith(".js")) {
                boolean done = indexRelatedScriptDocs();
                if (done) {
                    return;
                }
            }

            IndexDocument document = factory.createDocument(40); // TODO - measure!
            documents.add(document);
            
            // Add the fields, etc.. Recursively add the children classes or modules if any
            for (AstElement child : children) {
                ElementKind childKind = child.getKind();
                if (childKind == ElementKind.CONSTRUCTOR || childKind == ElementKind.METHOD) {
                    String signature = computeSignature(child);
                    indexFuncOrProperty(child, document, signature);
                    String name = child.getName();
                    if (Character.isUpperCase(name.charAt(0))) {
                        indexClass(child, document, signature);
                    }
                } else if (childKind == ElementKind.GLOBAL || 
                        childKind == ElementKind.PROPERTY || 
                        childKind == ElementKind.CLASS) {
                    indexFuncOrProperty(child, document, computeSignature(child));
                } else {
                    assert false : childKind;
                }
                // XXX what about fields, constants, attributes?
                
                assert child.getChildren().size() == 0;
            }

            Map<String,String> classExtends = ar.getExtendsMap();
            if (classExtends != null && classExtends.size() > 0) {
                for (Map.Entry<String,String> entry : classExtends.entrySet()) {
                    String clz = entry.getKey();
                    String superClz = entry.getValue();
                    document.addPair(FIELD_EXTEND, clz.toLowerCase() + ";" + clz + ";" + superClz, true); // NOI18N
                }
                
                ClassCache.INSTANCE.refresh();
            }
        }

        private void indexClass(AstElement element, IndexDocument document, String signature) {
            final String name = element.getName();
            document.addPair(FIELD_CLASS, name+ ";" + signature, true);
        }

        private String computeSignature(AstElement element) {
            OffsetRange docRange = getDocumentationOffset(element);
            int docOffset = -1;
            if (docRange != OffsetRange.NONE) {
                docOffset = docRange.getStart();
            }
            Map<String,String> typeMap = element.getDocProps();
              
            // Look up compatibility
            int index = IndexedElement.FLAG_INDEX;
            String compatibility = "";
            if (file.getNameExt().startsWith("stub_")) { // NOI18N
                int astOffset = element.getNode().getSourceStart();
                int lexOffset = astOffset;
                TranslatedSource source = result.getTranslatedSource();
                if (source != null) {
                    lexOffset = source.getLexicalOffset(astOffset);
                }
                try {
                    String line = doc.getText(lexOffset,
                            Utilities.getRowEnd(doc, lexOffset)-lexOffset);
                    int compatIdx = line.indexOf("COMPAT="); // NOI18N
                    if (compatIdx != -1) {
                        compatIdx += "COMPAT=".length(); // NOI18N
                        EnumSet<BrowserVersion> es = BrowserVersion.fromFlags(line.substring(compatIdx));
                        compatibility = BrowserVersion.toCompactFlags(es);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            assert index == IndexedElement.FLAG_INDEX;
            StringBuilder sb = new StringBuilder();
            int flags = IndexedElement.getFlags(element);
            // Add in info from documentation
            if (typeMap != null) {
                // Most flags are already handled by AstElement.getFlags()...
                // Consider handling the rest too
                if (typeMap.get("@ignore") != null) { // NOI18N
                    flags = flags | IndexedElement.NODOC;
                }
            }
            if (docOffset != -1) {
                flags = flags | IndexedElement.DOCUMENTED;
            }
            sb.append(IndexedElement.encode(flags));
            
            // Parameters
            sb.append(';');
            index++;
            assert index == IndexedElement.ARG_INDEX;
            if (element instanceof FunctionAstElement) {
                FunctionAstElement func = (FunctionAstElement)element;            

                int argIndex = 0;
                for (String param : func.getParameters()) {
                    if (argIndex == 0 && "$super".equals(param)) { // NOI18N
                        // Prototype inserts these as the first param to handle inheritance/super
                        argIndex++;
                        continue;
                    } 
                    if (argIndex > 0) {
                        sb.append(',');
                    }
                    sb.append(param);
                    if (typeMap != null) {
                        String type = typeMap.get(param);
                        if (type != null) {
                            sb.append(':');
                            sb.append(type);
                        }
                    }
                    argIndex++;
                }
            }

            // Node offset
            sb.append(';');
            index++;
            assert index == IndexedElement.NODE_INDEX;
            sb.append('0');
            //sb.append(IndexedElement.encode(element.getNode().getSourceStart()));
            
            // Documentation offset
            sb.append(';');
            index++;
            assert index == IndexedElement.DOC_INDEX;
            if (docOffset != -1) {
                sb.append(IndexedElement.encode(docOffset));
            }

            // Browser compatibility
            sb.append(';');
            index++;
            assert index == IndexedElement.BROWSER_INDEX;
            sb.append(compatibility);
            
            // Types
            sb.append(';');
            index++;
            assert index == IndexedElement.TYPE_INDEX;
            String type = element.getType();
            if (type == Node.UNKNOWN_TYPE) {
                type = null;
            }
            if (type == null) {
                type = typeMap != null ? typeMap.get(JsCommentLexer.AT_RETURN) : null; // NOI18N
            }
            if (type != null) {
                sb.append(type);
            }
            sb.append(';');
            
            String signature = sb.toString();
            return signature;
        }

        private void indexFuncOrProperty(AstElement element, IndexDocument document, String signature) {
            String in = element.getIn();
            String name = element.getName();
            StringBuilder base = new StringBuilder();
            base.append(name.toLowerCase());
            base.append(';');                
            if (in != null) {
                base.append(in);
            }
            base.append(';');
            base.append(name);
            base.append(';');
            base.append(signature);
            document.addPair(FIELD_BASE, base.toString(), true);
            
            StringBuilder fqn = new StringBuilder();
            if (in != null && in.length() > 0) {
                fqn.append(in.toLowerCase());
                fqn.append('.');
            }
            fqn.append(name.toLowerCase());
            fqn.append(';');
            fqn.append(';');
            if (in != null && in.length() > 0) {
                fqn.append(in);
                fqn.append('.');
            }
            fqn.append(name);
            fqn.append(';');
            fqn.append(signature);
            document.addPair(FIELD_FQN, fqn.toString(), true);

            FunctionCache cache = FunctionCache.INSTANCE;
            if (!cache.isEmpty()) {
                cache.wipe(in != null && in.length() > 0 ? in + "." + name : name);
            }
        }
        
        private OffsetRange getDocumentationOffset(AstElement element) {
            int astOffset = element.getNode().getSourceStart();
            // XXX This is wrong; I should do a
            //int lexOffset = LexUtilities.getLexerOffset(result, astOffset);
            // but I don't have the CompilationInfo in the ParseResult handed to the indexer!!
            int lexOffset = astOffset;
            try {
                if (lexOffset > doc.getLength()) {
                    return OffsetRange.NONE;
                }
                lexOffset = Utilities.getRowStart(doc, lexOffset);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            OffsetRange range = LexUtilities.getCommentBlock(doc, lexOffset, true);
            if (range != OffsetRange.NONE) {
                return range;
            } else {
                return OffsetRange.NONE;
            }
        }
        
        private void indexScriptDoc(BaseDocument doc, String sdocUrl) {
            // I came across the following tags in YUI:
            // @type, @param, @method, @class, @return, @constructor, @namespace, 
            // @static, @private, @event, @property, @extends, @final, @module,
            // @requires, @since, @protected, @default, @name, @see, @title, 
            // @attribute, @deprecated, @todo, @uses, @optional, @description, 
            // @public, @config, @throws
            //
            // I also saw these case variations:
            // @Class, @Extends, @TODO
            //
            // The following were also present, but not in many places
            // @beta, @for, @readonly, @writeonce, @knownissue, @browser, @link, @object, @scope
            //
            // Finally, there were these typos:
            // @propery, @depreciated, @parem, @parm, 
            assert sdocUrl == null || sdocUrl.endsWith(".sdoc") : sdocUrl; // NOI18N
            
            IndexDocument document = factory.createDocument(40, sdocUrl); // TODO - measure!
            documents.add(document);

            // TODO - I need to be able to associate builtin .sdoc files with specific versions found
            // in the libraries
            if (doc == null) {
                return;
            }
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, 0);
            if (ts == null) {
                return;
            }
            ts.moveStart();
            while (ts.moveNext()) {
                JsTokenId tid = ts.token().id();
                if (tid == JsTokenId.BLOCK_COMMENT) {
                    int docOffset = ts.offset();
                    TokenSequence<JsCommentTokenId> cts = ts.embedded(JsCommentTokenId.language());
                    if (cts != null) {
                        int flags = IndexedElement.DOC_ONLY | IndexedElement.DOCUMENTED | IndexedElement.FUNCTION;
                        String id = null;
                        String type = null;
                        String clz = null;
                        String name = null;
                        String superClz = null;
                        String nameSpace = null;
                        String fullName = null;
                        StringBuilder argList = new StringBuilder();
                        cts.moveStart();
                        while (cts.moveNext()) {
                            org.netbeans.api.lexer.Token<? extends JsCommentTokenId> token = cts.token();
                            TokenId cid = token.id();
                            if (cid == JsCommentTokenId.TAG) {
                                CharSequence text = token.text();
                                if (TokenUtilities.textEquals("@id", text)) { // NOI18N
                                    id = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@name", text)) { // NOI18N
                                    fullName = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@param", text)) { // NOI18N
                                    int index = cts.index()+1;
                                    String paramType = JsCommentLexer.nextType(cts);
                                    if (paramType == null) {
                                        cts.moveIndex(index);
                                        cts.moveNext();
                                    }
                                    String paramName = JsCommentLexer.nextIdent(cts);
                                    if (paramName != null) {
                                        if (argList.length() > 0) {
                                            argList.append(',');
                                        }
                                        argList.append(paramName);
                                        if (type != null) {
                                            argList.append(':');
                                            argList.append(paramType);
                                        }
                                    } else {
                                        cts.moveIndex(index);
                                        cts.moveNext();
                                    }
                                } else if (TokenUtilities.textEquals("@return", text)) { // NOI18N
                                    String returnType = JsCommentLexer.nextType(cts);
                                    if (returnType != null) {
                                        type = returnType;
                                    }
                                } else if (TokenUtilities.textEquals("@constructor", text)) { // NOI18N
                                    flags = flags | IndexedElement.CONSTRUCTOR;
                                } else if (TokenUtilities.textEquals("@static", text)) { // NOI18N
                                    flags = flags | IndexedElement.STATIC;
                                } else if (TokenUtilities.textEquals("@deprecated", text)) { // NOI18N
                                    flags = flags | IndexedElement.DEPRECATED;
                                } else if (TokenUtilities.textEquals("@final", text)) { // NOI18N
                                    flags = flags | IndexedElement.FINAL;
                                } else if (TokenUtilities.textEquals("@type", text)) { // NOI18N
                                    type = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@private", text) || // NOI18N
                                        TokenUtilities.textEquals("@protected", text)) { // NOI18N
                                    flags = flags | IndexedElement.PRIVATE;
                                } else if (TokenUtilities.textEquals("@namespace", text)) { // NOI18N
                                    nameSpace = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@class", text)) { // NOI18N
                                    clz = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@memberOf", text)) { // NOI18N
                                    clz = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@method", text)) { // NOI18N
                                    flags = flags | IndexedElement.FUNCTION;
                                    name = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@function", text)) { // NOI18N
                                    flags = flags | IndexedElement.FUNCTION;
                                } else if (TokenUtilities.textEquals("@global", text)) { // NOI18N
                                    flags = flags & (~IndexedElement.FUNCTION);
                                    flags = flags | IndexedElement.GLOBAL;
                                } else if (TokenUtilities.textEquals("@property", text) || TokenUtilities.textEquals("@attribute", text)) { // NOI18N
                                    flags = flags & (~IndexedElement.FUNCTION);
                                    name = JsCommentLexer.nextIdentGroup(cts);
                                } else if (TokenUtilities.textEquals("@extends", text)) { // NOI18N
                                    superClz = JsCommentLexer.nextIdentGroup(cts);
                                }
                                // TODO - how do I encode constants?
                            }
                        }
                        
                        if (fullName != null && id == null) {
                            id = fullName;
                            // When using @name, @class is just used as a description
                            clz = null;
                        }
                        
                        if (id == null && clz != null && name == null) {
                            if (nameSpace != null) {
                                id = nameSpace + "." + clz + "." + name;
                            } else {
                                id = clz + "." + name;
                            }
                        }

                        if (superClz != null && clz != null) {
                            String fqnClz;
                            if (clz.indexOf('.') == -1) {
                                if (nameSpace != null) {
                                    fqnClz = nameSpace + "." + clz;
                                } else if (id != null && id.indexOf('.') != -1) {
                                    int idDot = id.lastIndexOf('.');
                                    fqnClz = id.substring(0, idDot+1) + clz;
                                } else {
                                    fqnClz = clz;
                                }
                            } else {
                                fqnClz = clz;
                            }
                            if (superClz.indexOf('.') == -1 && nameSpace != null) {
                                superClz = nameSpace + "." + superClz;
                            }
                            document.addPair(FIELD_EXTEND, fqnClz.toLowerCase() + ";" + fqnClz + ";" + superClz, true); // NOI18N
                        }

                        if (id != null) {
                            if (clz == null || name == null) {
                                int dot = id.lastIndexOf('.');
                                if (dot != -1) {
                                    clz = id.substring(0, dot);
                                    name = id.substring(dot+1);
                                } else {
                                    clz = null;
                                    name = id;
                                }
                            }

                            // Browser compatibility ... TODO
                            
                            
                            int index = IndexedElement.FLAG_INDEX;
                            StringBuilder sb = new StringBuilder();

                            sb.append(IndexedElement.encode(flags));

                            // Parameters
                            sb.append(';');
                            index++;
                            assert index == IndexedElement.ARG_INDEX;
                            if (argList.length() > 0) {
                                sb.append(argList);
                            }

                            // Node offset
                            sb.append(';');
                            index++;
                            assert index == IndexedElement.NODE_INDEX;
                            sb.append('0');

                            // Documentation offset
                            sb.append(';');
                            index++;
                            assert index == IndexedElement.DOC_INDEX;
                            if (docOffset != -1) {
                                sb.append(IndexedElement.encode(docOffset));
                            }

                            // Browser compatibility
                            sb.append(';');
                            index++;
                            assert index == IndexedElement.BROWSER_INDEX;
                            String compatibility = "";
                            sb.append(compatibility);

                            // Types
                            sb.append(';');
                            index++;
                            assert index == IndexedElement.TYPE_INDEX;
                            if (type != null) {
                                sb.append(type);
                            }
                            sb.append(';');

                            String signature = sb.toString();

                            String in = clz;

                            // Create items
                            StringBuilder base = new StringBuilder();
                            base.append(name.toLowerCase());
                            
                            base.append(';');                
                            if (in != null) {
                                base.append(in);
                            }
                            base.append(';');
                            base.append(name);
                            base.append(';');
                            base.append(signature);
                            document.addPair(FIELD_BASE, base.toString(), true);

                            StringBuilder fqn = new StringBuilder();
                            if (in != null && in.length() > 0) {
                                fqn.append(in.toLowerCase());
                                fqn.append('.');
                            }
                            fqn.append(name.toLowerCase());
                            fqn.append(';');
                            fqn.append(';');
                            if (in != null && in.length() > 0) {
                                fqn.append(in);
                                fqn.append('.');
                            }
                            fqn.append(name);
                            fqn.append(';');
                            fqn.append(signature);
                            document.addPair(FIELD_FQN, fqn.toString(), true);
                        }
                    }
                }
            }
        }
        
        private boolean indexRelatedScriptDocs() {
            // (1) If it's a simple library like JQuery, use the assocaited file, else
            // (2) If it's a YUI file, use the associated file in sdoc, else
            // (3) If it's a YUI "collections" file, use the associated set of files (I must iterate)
            // Finally, in all cases, see if there's a corresponding sdoc file in the dir and if so,
            // use it.
            
            //            if (fo != null) {
            //                // Prioritize sdoc files bundled next to the file
            //                if (fo != null && fo.getParent() != null) {
            //                    String base = fo.getNameExt();
            //                    if (base.endsWith(".js")) { // NOI18N
            //                        base = base.substring(0, base.length()-3);
            //                    }
            //                    fo = fo.getParent().getFileObject(base + ".sdoc"); // NOI18N
            //                    if (fo != null) {
            //                        return fo;
            //                    }
            //                }
            //            }
            
            int begin = url.lastIndexOf('/');
            if (url.startsWith("jquery-", begin+1)) { // NOI18N
                indexScriptDoc("jquery.sdoc", false); // NOI18N
                return true;
            } else if (url.startsWith("dojo.js", begin+1)) { // NOI18N
                indexScriptDoc("dojo.sdoc", false); // NOI18N
                return true;
            } else if (url.startsWith("yahoo.js", begin+1) || // NOI18N
                    url.startsWith("yahoo-debug.js", begin+1) || // NOI18N
                    url.startsWith("yahoo-min.js", begin+1)) { // NOI18N
                int subBegin = begin-"yahoo".length();
                if (url.startsWith("yahoo/yahoo", subBegin)) {
                    // Part of a build tree - just index the yahoo file itself
                    indexScriptDoc("yui/" + url.substring(subBegin), false); // NOI18N
                } else {
                    // Index all the YUI stuff
                    indexScriptDoc("yui", true);
                }
                return true;
            } else {
                // TODO - do something smarter here based on which "collection" files
                // you're using which pull in many individual files...
                // See http://developer.yahoo.com/yui/articles/hosting/
                int yuiIndex = url.indexOf("/yui/build/"); // NOI18N
                if (yuiIndex != -1) {
                    indexScriptDoc("yui/" + url.substring(yuiIndex+"/yui/build/".length()), false);
                    return true;
                }
            }
            
            return false;
        }

        /**
         * Method which recursively indexes directory trees, such as the yui/ folder
         * for example
         */
        private void indexScriptDocRecursively(FileObject fo, String url) {
            if (fo.isFolder()) {
                for (FileObject c : fo.getChildren()) {
                    indexScriptDocRecursively(c, url+ "/" + c.getNameExt()); // NOI18N
                }
                return;
            }
            
            if (fo.getExt().equals("sdoc")) { // NOI18N
                BaseDocument urlDoc = NbUtilities.getBaseDocument(fo, true);
                indexScriptDoc(urlDoc, url);
            }
        }
        
        private static FileObject sdocsRoot;
        private static String sdocsRootUrl;

        private void indexScriptDoc(String relative, boolean recurse) {
            if (relative != null) {
                if (sdocsRootUrl == null) {
                    File sdocs = InstalledFileLocator.getDefault().locate("jsstubs/sdocs.zip",  // NOI18N
                            "org-netbeans-modules-javascript-editing.jar", false); // NOI18N
                    if (sdocs == null) {
                        sdocsRootUrl = "";
                    } else if (sdocs.exists()) {
                        try {
                            String s = sdocs.toURI().toURL().toExternalForm() + "!/sdocs"; // NOI18N
                            URL u = new URL("jar:" + s);// NOI18N
                            sdocsRoot = URLMapper.findFileObject(u);
                            sdocsRootUrl = u.toExternalForm();
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (sdocsRoot == null) {
                        sdocsRootUrl = ""; // NOI18N
                        return;
                    }
                }
                if (sdocsRootUrl.length() > 0) {
                    if (relative.endsWith("-debug.js")) { // NOI18N
                        relative = relative.substring(0, relative.length()-"-debug.js".length()) + ".sdoc"; // NOI18N
                    } else if (relative.endsWith("-min.js")) { // NOI18N
                        relative = relative.substring(0, relative.length()-"-min.js".length()) + ".sdoc"; // NOI18N
                    } else if (relative.endsWith(".js")) { // NOI18N
                        relative = relative.substring(0, relative.length()-2) + "sdoc"; // NOI18N
                    }
                    assert sdocsRoot != null;

                    FileObject fo = sdocsRoot.getFileObject(relative);
                    if (fo != null) {
                        String urlString = sdocsRootUrl+"/"+relative; // NOI18N
                        if (recurse) {
                            indexScriptDocRecursively(fo, urlString);
                        } else {
                            BaseDocument urlDoc = NbUtilities.getBaseDocument(fo, true);
                            indexScriptDoc(urlDoc, urlString);
                        }
                    }
                }
            }
        }
    }
    
    public File getPreindexedData() {
        return null;
    }
    
    private static FileObject preindexedDb;

    /** For testing only */
    public static void setPreindexedDb(FileObject preindexedDb) {
        JsIndexer.preindexedDb = preindexedDb;
    }
    
    public FileObject getPreindexedDb() {
        if (preindexedDb == null) {
            File preindexed = InstalledFileLocator.getDefault().locate(
                    "preindexed-javascript", "org.netbeans.modules.javascript.editing", false); // NOI18N
            if (preindexed == null || !preindexed.isDirectory()) {
                throw new RuntimeException("Can't locate preindexed directory. Installation might be damaged"); // NOI18N
            }
            preindexedDb = FileUtil.toFileObject(preindexed);
        }
        return preindexedDb;
    }
}
