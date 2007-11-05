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

package org.netbeans.modules.web.core.syntax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective;
import org.netbeans.modules.web.jsps.parserapi.Node.Visitor;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static org.netbeans.api.jsp.lexer.JspTokenId.JavaCodeType;

/**
 * Utility class for generating a simplified <em>JSP servlet</em> class from a JSP file.
 * Using a full featured JSP parser would be too resource demanding,
 * we need a lightweight solution to be used with code completion.
 *
 * Inputs: original JSP document, caret offset within the original document
 * Outputs: a body of a simplified JSP servlet class, offset of the corresponding
 *          position in the servlet class
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class SimplifiedJSPServlet {

    private static final String CLASS_HEADER = "\nclass SimplifiedJSPServlet extends %s {\n"; //NOI18N
    private static final String METHOD_HEADER = "\n\tvoid mergedScriptlets(\n"
            + "\t\tHttpServletRequest request,\n" 
            + "\t\tHttpServletResponse response,\n" 
            + "\t\tHttpSession session,\n" 
            + "\t\tServletContext application,\n" 
            + "\t\tJspWriter out,\n" 
            + "\t\tServletConfig config,\n"
            + "\t\tJspContext jspContext,\n" 
            + "\t\tObject page,\n" 
            + "\t\tPageContext pageContext,\n"
            + "\t\tThrowable exception\n"
            + "\t) throws Throwable {\n"; //NOI18N
    private static final String CLASS_FOOTER = "\n\t}\n}"; //NOI18N
    private final Document doc;
    private CharSequence charSequence;
    private final FileObject fobj;
    private final ArrayList<CodeBlockData> codeBlocks = new ArrayList<CodeBlockData>();

    private String header = null;
    private StringBuilder scriptlets = new StringBuilder();
    private StringBuilder declarations = new StringBuilder();
    private boolean processCalled = false;
    private String importStatements = null;
    private int expressionIndex = 1;
    private static final Logger logger = Logger.getLogger(SimplifiedJSPServlet.class.getName());
    private boolean processingSuccessful = true;

    public SimplifiedJSPServlet(Document doc){
        this(doc, null);
    }
    
    public SimplifiedJSPServlet(Document doc, CharSequence charSequence) {
        this.doc = doc;
        
        if (charSequence == null) {
            try {
                this.charSequence = doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                assert false; // this can never happen!
            }
        } else {
            this.charSequence = charSequence;
        }
        
        if (doc != null){
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile(): null;
        } else {
            logger.log(Level.SEVERE, "Unable to find FileObject for document");
            fobj = null;
        }
    }
    
    public void process() throws BadLocationException {
        processCalled = true;
        
        if (!isServletAPIOnClasspath()){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    displayServletAPIMissingWarning();
                }
            });
            
            processingSuccessful = false;
            return;
        }
         //Workaround of issue #120195 - Deadlock in jspparser while reformatting JSP
        //Needs to be removed after properly fixing the issue
        if (!DocumentUtilities.isWriteLocked(doc)) {
            JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(doc, fobj, false, false);
            if (parseResult == null || !parseResult.isParsingSuccess()) {
                processingSuccessful = false;
                return;
            }
        }
        
        final BadLocationException[] ex = new BadLocationException[1];
        
        processIncludes();

        TokenHierarchy tokenHierarchy = TokenHierarchy.create(charSequence, JspTokenId.language());//TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence(); //get top level token sequence
        if (!tokenSequence.moveNext()) {
            return; //no tokens in token sequence
        }

        /**
         * process java code blocks one by one
         * note: We count on the fact the scripting language in JSP is Java
         */
        do {
            Token token = tokenSequence.token();

            if (token.id() == JspTokenId.SCRIPTLET) {
                int blockStart = token.offset(tokenHierarchy);
                int blockEnd = blockStart + token.length();

                JavaCodeType blockType = (JavaCodeType) token.getProperty(JspTokenId.SCRIPTLET_TOKEN_TYPE_PROPERTY);

                String blockBody = charSequence.subSequence(blockStart, blockEnd).toString(); //doc.getText(blockStart, blockEnd - blockStart);
                StringBuilder buff = blockType == JavaCodeType.DECLARATION ? declarations : scriptlets;
                int newBlockStart = buff.length();

                if (blockType == JavaCodeType.EXPRESSION) {
                    String exprPrefix = String.format("\t\tObject expr%1$d = ", expressionIndex++); //NOI18N
                    newBlockStart += exprPrefix.length();
                    buff.append(exprPrefix + blockBody + ";\n");
                } else {
                    buff.append(blockBody + "\n");
                }

                CodeBlockData blockData = new CodeBlockData(blockStart, newBlockStart, blockEnd, blockType);
                codeBlocks.add(blockData);
            }
        } while (tokenSequence.moveNext());
            

        if (ex[0] != null) {
            throw ex[0];
        }

        header = getClassHeader();
        importStatements = createImportStatements();
        declarations.append("\n" + createBeanVarDeclarations());
    }
    
    private void processIncludes()  {
        PageInfo pageInfo = getPageInfo();
        
        if (pageInfo == null) {
            //if we do not get pageinfo it is unlikely we will get something reasonable from 
            //jspSyntax.getParseResult()...
            return ;
        }
        
        for (String preludePath : (List<String>)pageInfo.getIncludePrelude()){
            processIncludedFile(preludePath);
        }
        
        Visitor visitor = new Visitor() {

            public void visit(IncludeDirective includeDirective) throws JspException {
                String fileName = includeDirective.getAttributeValue("file");
                processIncludedFile(fileName);
            }
        };

        JspSyntaxSupport jspSyntax = JspSyntaxSupport.get(doc);
        try {
            jspSyntax.getParseResult().getNodes().visit(visitor);
        } catch (JspException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void processIncludedFile(String filePath) {
        // FileObject.getFileObject() doesn't handle .. in relative path
        File file = new File(FileUtil.toFile(fobj.getParent()), filePath);
        FileObject includedFile = null;
        
        try{
             includedFile = FileUtil.toFileObject(file.getCanonicalFile());
        } catch (IOException e){
            //ignore, file name may be invalid
        }

        if (includedFile != null && includedFile.canRead()) {

            try {
                DataObject includedFileDO = DataObject.find(includedFile);
                String mimeType = includedFile.getMIMEType();
                
                if ("text/x-jsp".equals(mimeType) || "text/x-tag".equals(mimeType)) { //NOI18N
                    EditorCookie editor = includedFileDO.getCookie(EditorCookie.class);

                    if (editor != null) {
                        SimplifiedJSPServlet simplifiedServlet = new SimplifiedJSPServlet(editor.openDocument());
                        simplifiedServlet.process();

                        declarations.append(simplifiedServlet.declarations);
                        scriptlets.append(simplifiedServlet.scriptlets);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private boolean isServletAPIOnClasspath() {
        ClassPath cp = ClassPath.getClassPath(fobj, ClassPath.COMPILE);

        if (cp != null && cp.findResource("javax/servlet/http/HttpServlet.class") != null) { //NOI18N
            return true;
        }
        
        return false;
    }

    private void displayServletAPIMissingWarning() {
        try {
            DataObject doJsp = DataObject.find(fobj);
            EditorCookie editor = doJsp.getCookie(EditorCookie.class);

            if (editor != null && editor.getOpenedPanes() != null) {

                JTextComponent component = editor.getOpenedPanes()[0];
                if (component != null) {
                    org.netbeans.editor.Utilities.setStatusBoldText(component, 
                            NbBundle.getMessage(SimplifiedJSPServlet.class, "MSG_MissingServletAPI"));
                }
            }
        } catch (DataObjectNotFoundException e) {
            // ignore
        }
    }

    private String createBeanVarDeclarations() {
        StringBuilder beanDeclarationsBuff = new StringBuilder();

        PageInfo pageInfo = getPageInfo();
        
        if (pageInfo != null) {
            PageInfo.BeanData[] beanData = getBeanData();

            if (beanData != null) {
                for (PageInfo.BeanData bean : beanData) {
                    beanDeclarationsBuff.append(bean.getClassName() + " " + bean.getId() + ";\n"); //NOI18N
                }
            }
            
            if (pageInfo.isTagFile()){
                for (TagAttributeInfo info : pageInfo.getTagInfo().getAttributes()){
                    beanDeclarationsBuff.append(info.getTypeName() + " " + info.getName() + ";\n"); //NOI18N
                }
            }
        }

        return beanDeclarationsBuff.toString();
    }
    
    private String[] getImports() {
        PageInfo pi = getPageInfo();
        if (pi == null) {
            //we need at least some basic imports
            return new String[]{"javax.servlet.*", "javax.servlet.http.*", "javax.servlet.jsp.*"};
        }
        List<String> imports = pi.getImports();
        return imports.toArray(new String[imports.size()]);
    }
    
    private PageInfo.BeanData[] getBeanData() {

        PageInfo pageInfo = getPageInfo();
        //pageInfo can be null in some cases when the parser cannot parse
        //the webmodule or the page itself
        if (pageInfo != null) {
            return pageInfo.getBeans();
        }

        //TagLibParseSupport support = (dobj == null) ?
        //null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        //return support.getTagLibEditorData().getBeanData();
        return null;
    }
    
    private PageInfo getPageInfo() {
        //Workaround of issue #120195 - Deadlock in jspparser while reformatting JSP
        //Needs to be removed after properly fixing the issue
        if(DocumentUtilities.isWriteLocked(doc)) {
            return null;
        }
        
        JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(doc, fobj, true, false);

        if (parseResult != null) {
            return parseResult.getPageInfo();
        }

        //report error but do not break the entire CC
        logger.log(Level.INFO, null, "PageInfo obtained from JspParserAPI.ParseResult is null");

        return null;
    }

    private String createImportStatements() {
        StringBuilder importsBuff = new StringBuilder();
        String[] imports = getImports();
        
        if (imports == null || imports.length == 0){
            processingSuccessful = false;
        } else {
            // TODO: better support for situation when imports is null
            // (JSP doesn't belong to a project)
            for (String pckg : imports) {
                importsBuff.append("import " + pckg + ";\n"); //NOI18N
            }
        }

        return importsBuff.toString();
    }
    
    private String getClassHeader() {
        String extendsClass = null; //NOI18N
        PageInfo pageInfo = getPageInfo();
        
        if (pageInfo != null) {
            extendsClass = pageInfo.getExtends();
        }
        
        if (extendsClass == null || 
                // workaround for issue #116314
                "org.apache.jasper.runtime.HttpJspBase".equals(extendsClass)){ //NOI18N
            extendsClass = "HttpServlet"; //NOI18N
        }

        return String.format(CLASS_HEADER, extendsClass);
    }

    private void assureProcessCalled() {
        if (!processCalled) {
            throw new IllegalStateException("process() method must be called first!"); //NOI18N
        }
    }

    public int getShiftedOffset(int originalOffset) {
        assureProcessCalled();

        CodeBlockData codeBlock = getCodeBlockAtOffset(originalOffset);

        if (codeBlock == null) {
            return -1; // no embedded java code at the offset
        }

        int offsetWithinBlock = originalOffset - codeBlock.getStartOffset();
        int shiftedOffset = codeBlock.getNewBlockStart() + offsetWithinBlock;

        return shiftedOffset;
    }

    public int getRealOffset(int offset) {
        assureProcessCalled();

        if (processingSuccessful) {
            for (CodeBlockData codeBlock : codeBlocks) {
                int len = codeBlock.getEndOffset() - codeBlock.getStartOffset();

                if (codeBlock.getNewBlockStart() <= offset && codeBlock.getNewBlockStart() + len >= offset) {
                    return codeBlock.getStartOffset() + offset - codeBlock.getNewBlockStart();
                }
            }
        }

        return -1;
    }

    public String getVirtualClassBody() {
        assureProcessCalled();
        
        if (!processingSuccessful){
            return ""; //NOI18N
        }
        
        return importStatements + header + declarations + METHOD_HEADER + scriptlets + CLASS_FOOTER;
    }

    private CodeBlockData getCodeBlockAtOffset(int offset) {

        for (CodeBlockData codeBlock : codeBlocks) {
            if (codeBlock.getStartOffset() <= offset && codeBlock.getEndOffset() >= offset) {
                return codeBlock;
            }
        }

        return null;
    }

    public static abstract class VirtualJavaClass {

        public final void create(Document doc, String virtualClassBody) {
            FileObject fileDummyJava = null;
            List<? extends CompletionItem> javaCompletionItems = null;

            try {
                FileSystem memFS = FileUtil.createMemoryFileSystem();
                fileDummyJava = memFS.getRoot().createData("SimplifiedJSPServlet", "java"); //NOI18N
                PrintWriter writer = new PrintWriter(fileDummyJava.getOutputStream());
                writer.print(virtualClassBody);
                writer.close();

                FileObject jspFile = NbEditorUtilities.getFileObject(doc);
                ClasspathInfo cpInfo = ClasspathInfo.create(jspFile);
                JavaSource source = JavaSource.create(cpInfo, fileDummyJava);
                process(fileDummyJava, source);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            } 
        }

        protected abstract void process(FileObject fileObject, JavaSource javaSource);
    }

    private class CodeBlockData {

        private int startOffset;
        private int endOffset;
        private int newRelativeBlockStart; // offset in created java class
        private JavaCodeType type;

        public CodeBlockData(int startOffset, int newRelativeBlockStart, int endOffset, JavaCodeType type) {
            this.startOffset = startOffset;
            this.newRelativeBlockStart = newRelativeBlockStart;
            this.endOffset = endOffset;
            this.type = type;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public JavaCodeType getType() {
            return type;
        }

        public int getNewBlockStart() {
            int newBlockStart = newRelativeBlockStart + header.length() + importStatements.length();

            if (getType() != JavaCodeType.DECLARATION) {
                newBlockStart += declarations.length() + METHOD_HEADER.length();
            }

            return newBlockStart;
        }
    }
}
