/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.core.syntax.formatting;

import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.formatting.api.support.AbstractIndenter;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.html.editor.HTMLKit;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.html.editor.coloring.EmbeddingUpdater;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.java.source.parsing.ClassParserFactory;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.indent.JspIndentTaskFactory;
import org.netbeans.test.web.core.syntax.TestBase2;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author david
 */
public class JspIndenterTest extends TestBase2 {

    public JspIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initParserJARs();
        copyWebProjectJarsTo(new File(getDataDir(), "FormattingProject/lib"));
        NbReaderProvider.setupReaders();
        AbstractIndenter.inUnitTestRun = true;

        
        
        MockServices.setServices(TestLanguageProvider.class, MockMimeLookup.class);
        // init TestLanguageProvider
        Lookup.getDefault().lookup(TestLanguageProvider.class);

        TestLanguageProvider.register(CSSTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());

        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-css"), cssFactory);
        JspIndentTaskFactory jspReformatFactory = new JspIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), new JSPKit("text/x-jsp"), jspReformatFactory, new EmbeddingProviderImpl.Factory());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HTMLKit("text/x-jsp"));
        Reformatter.Factory factory = new Reformatter.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), factory, new JavacParserFactory(), new ClassParserFactory());
    }

    @Override
    protected void configureIndenters(final BaseDocument document, final Formatter formatter,
            final CompilationInfo compilationInfo, boolean indentOnly, String mimeType) throws BadLocationException {
        // override it because I've already done in setUp()
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
             DataObject dobj = DataObject.find(fo);
             assertNotNull(dobj);

             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
             assertNotNull(ec);

             return (BaseDocument)ec.openDocument();
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    private void forceHTMLParsingAndWait(String file, String mimeType, Language language) throws Exception {
        FileObject fo = getTestFile(file);
        BaseDocument doc = getDocument(fo, mimeType, language);
        LanguagePath htmlLP = LanguagePath.get(language);
        Semaphore s = new Semaphore(1);
        Listener l = new Listener(doc, s);
        SyntaxParser.get(doc, htmlLP).addSyntaxParserListener(l);
        s.acquire();
        s.release();
    }

    private static class Listener extends EmbeddingUpdater {
        private Semaphore s;
        public Listener(Document doc, Semaphore s) throws InterruptedException {
            super(doc);
            this.s = s;
            s.acquire();
        }
        @Override
        public void parsingFinished(List<SyntaxElement> elements) {
            super.parsingFinished(elements);
            s.release();
        }

    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testFormattingCase001() throws Exception {
        reformatFileContents("FormattingProject/web/case001.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase002() throws Exception {
        forceHTMLParsingAndWait("FormattingProject/web/case002.jsp", "text/x-jsp", JspTokenId.language());
        reformatFileContents("FormattingProject/web/case002.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase003() throws Exception {
        reformatFileContents("FormattingProject/web/case003.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase004() throws Exception {
        /*Project p = ProjectManager.getDefault().findProject(getTestFile("FormattingProject"));
        DataObject dobj = DataObject.find(getTestFile("FormattingProject/web/simple.jsp"));
        System.err.println("dobj="+dobj);
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        System.err.println("ec="+ec);
        StyledDocument doc = ec.openDocument();*/

        reformatFileContents("FormattingProject/web/case004.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase005() throws Exception {
        reformatFileContents("FormattingProject/web/case005.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase006() throws Exception {
        reformatFileContents("FormattingProject/web/case006.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue121102() throws Exception {
        reformatFileContents("FormattingProject/web/issue121102.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue129778() throws Exception {
        reformatFileContents("FormattingProject/web/issue129778.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue89174() throws Exception {
        reformatFileContents("FormattingProject/web/issue89174.jsp",new IndentPrefs(4,4));
    }

    public void testIndentation() throws Exception {
//        insertNewline("<style>\n     h1 {\n        <%= System.\n   somth() ^%>",
//                      "<style>\n     h1 {\n        <%= System.\n   somth() \n        ^%>", null);

        insertNewline("<jsp:useBean>^", "<jsp:useBean>\n    ^", null);
        insertNewline("^<jsp:body>", "\n^<jsp:body>", null);

        insertNewline("<jsp:body>\n    <html>^", "<jsp:body>\n    <html>\n        ^", null);
        insertNewline("<jsp:body>\n^<html>", "<jsp:body>\n\n    ^<html>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n            ^", null);
        insertNewline("<jsp:body>\n    <html>\n^<jsp:useBean>", "<jsp:body>\n    <html>\n\n        ^<jsp:useBean>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n            <table>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n            <table>\n                ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n^<table>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n\n            ^<table>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n        ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n^</jsp:useBean>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n\n        ^</jsp:useBean>", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>^</jsp:useBean>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        ^</jsp:useBean>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    </html>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    </html>\n    ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^</html>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    ^</html>", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^<table>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n        ^<table>", null);

        insertNewline("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\"\n        prefix=\"c\" %>^",
                      "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\"\n        prefix=\"c\" %>\n^",null);

        // TODO: impl matching of INDENT/RETURN and use it to properly match incorrect document:
        //insertNewline("<jsp:body>\n    <html>^</jsp:body>", "<jsp:body>\n    <html>\n^</jsp:body>", null);

    }

}