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


import java.util.Map;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.html.editor.coloring.EmbeddingUpdater;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;
import org.netbeans.modules.web.core.syntax.JspUtils;
import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;
import org.netbeans.modules.web.core.syntax.deprecated.ELDrawLayerFactory;
import java.awt.event.ActionEvent;
import java.beans.*;
import javax.swing.Action;
import javax.swing.text.*;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.editor.ext.java.JavaDrawLayerFactory;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.RequestProcessor;

/**
 * Editor kit implementation for JSP content type
 *
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @author Marek.Fukala@Sun.COM
 * @version 1.5
 */
public class JSPKit extends LanguagesEditorKit implements org.openide.util.HelpCtx.Provider{
    
    public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    /** serialVersionUID */
    private static final long serialVersionUID = 8933974837050367142L;
    
    public static final boolean debug = false;
    
    /** Default constructor */
    public JSPKit() {
        super(JSP_MIME_TYPE);
    }
    
    public String getContentType() {
        return JSP_MIME_TYPE;
    }
    
    public Object clone() {
        return new JSPKit();
    }
    
    /** Creates a new instance of the syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        //TODO - place the coloring listener initialization to
        //more appropriate place. The createSyntax method is likely
        //going to be removed.
        initLexerColoringListener(doc);
        
        Syntax contentSyntax   = getSyntaxForLanguage(doc, JspUtils.getContentLanguage());
        Syntax scriptingSyntax = getSyntaxForLanguage(doc, JspUtils.getScriptingLanguage());
        final Jsp11Syntax newSyntax = new Jsp11Syntax(contentSyntax, scriptingSyntax);

        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        
        // tag library coloring data stuff
        JSPColoringData data = data = JspUtils.getJSPColoringData(doc, fobj);
        // construct the listener
        PropertyChangeListener pList = new ColoringListener(doc, data, newSyntax);
        // attach the listener
        // PENDING - listen on the language
        //jspdo.addPropertyChangeListener(WeakListeners.propertyChange(pList, jspdo));
        if (data != null) {
            data.addPropertyChangeListener(WeakListeners.propertyChange(pList, data));
        }
        
        return newSyntax;
    }
    
    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
            new JspInsertBreakAction(),
            new JspDefaultKeyTypedAction(),
            new JspDeleteCharAction(deletePrevCharAction, false)
        };
        
        return TextAction.augmentList(super.createActions(), javaActions);
    }
    
     private static class ColoringListener implements PropertyChangeListener {
        private Document doc;
        private Object parsedDataRef; // NOPMD: hold a reference to the data we are listening on
        // so it does not get garbage collected
        private Jsp11Syntax syntax;
        //private JspDataObject jspdo;
        
        public ColoringListener(Document doc, JSPColoringData data, Jsp11Syntax syntax) {
            this.doc = doc;
            // we must keep the reference to the structure we are listening on so it's not gc'ed
            this.parsedDataRef = data;
            this.syntax = syntax;
            // syntax must keep a reference to this object so it's not gc'ed
            syntax.listenerReference = this;
            syntax.data = data;
            /* jspdo = (JspDataObject)NbEditorUtilities.getDataObject(doc);*/
        }
        
        private void recolor() {
            if (doc instanceof BaseDocument)
                ((BaseDocument)doc).invalidateSyntaxMarks();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            //            System.out.println("**************** PCHL - propertyChange()");
            if (syntax == null)
                return;
            if (syntax.listenerReference != this) {
                syntax = null; // should help garbage collection
                return;
            }
           /* if (JspDataObject.PROP_CONTENT_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setContentSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getContentLanguage()));
                recolor();
            }
            if (JspDataObject.PROP_SCRIPTING_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setScriptingSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getScriptingLanguage()));
                recolor();
            }*/
            if (JSPColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                recolor();
            }
        }
    }
    
    
    private static class LexerColoringListener implements PropertyChangeListener {
        
        private Document doc;
        private JSPColoringData data;
        private JspParseData jspParseData;
        
        private LexerColoringListener(Document doc, JSPColoringData data, JspParseData jspParseData) {
            this.doc = doc;
            this.data = data; //hold ref to JSPColoringData so LCL is not GC'ed
            this.jspParseData = jspParseData;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (JSPColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                //THC.rebuild() must run under document write lock. Since it is not guaranteed that the
                //event from the JSPColoringData is not fired under document read lock, synchronous call
                //to write lock could deadlock. So the rebuild is better called asynchronously from RP thread.
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        NbEditorDocument nbdoc = (NbEditorDocument)doc;
                        nbdoc.extWriteLock();
                        try {
                            recolor();
                        } finally {
                            nbdoc.extWriteUnlock();
                        }
                    }
                });
            }
        }
        private void recolor() {
            jspParseData.updateParseData((Map<String,String>)data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax());

            MutableTextInput mti = (MutableTextInput)doc.getProperty(MutableTextInput.class);
            if(mti != null) {
                mti.tokenHierarchyControl().rebuild();
            }
        }
        
    }
    
    public static Syntax getSyntaxForLanguage(Document doc, String language) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(language);
        if (kit instanceof JavaKit) {
            JavaKit jkit = (JavaKit)kit;
            String sourceLevel = jkit.getSourceLevel((BaseDocument)doc);
            //create a special javasyntax patched for use in JSPs (fix of #55628)
            return new JavaSyntax(sourceLevel, true);
        } else {
            return new HTMLSyntax();
        }
    }

    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addLayer(new ELDrawLayerFactory.ELLayer(),
                ELDrawLayerFactory.EL_LAYER_VISIBILITY);
        
        //listen on the HTML parser and create javascript and css embeddings
        SyntaxParser.get(doc).addSyntaxParserListener(new EmbeddingUpdater(doc));
        //initialize JSP embedding updater
        //just a prototype - better disable it for 6.0
        //JspColoringUpdater.init(doc);
    }
    
    private void initLexerColoringListener(Document doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        JSPColoringData data = JspUtils.getJSPColoringData(doc, fobj);
        
        if(data == null) {
            return ;
        }
        
        JspParseData jspParseData = new JspParseData();
        jspParseData.updateParseData((Map<String,String>)data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax());
        PropertyChangeListener lexerColoringListener = new LexerColoringListener(doc, data, jspParseData);
        
        data.addPropertyChangeListener(WeakListeners.propertyChange(lexerColoringListener, data));
        //reference LCL from document to prevent LCL to be GC'ed
        doc.putProperty(LexerColoringListener.class, lexerColoringListener);
        
        //add an instance of InputAttributes to the document property,
        //lexer will use it to read coloring information
        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(JspTokenId.language(), JspParseData.class, jspParseData, false);
        doc.putProperty(InputAttributes.class, inputAttributes);
    }
    
    // <RAVE> #62993
    // Implement HelpCtx.Provider to provide help for CloneableEditor
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(JSPKit.class);
    }
    
    public static class JspInsertBreakAction extends InsertBreakAction {
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(DefaultEditorKit.insertBreakAction);
                        if (action != null && action instanceof JavaKit.JavaInsertBreakAction){
                            ((JavaKit.JavaInsertBreakAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
    }
    
    public static class JspDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(DefaultEditorKit.defaultKeyTypedAction);
                        if (action != null && action instanceof JavaKit.JavaDefaultKeyTypedAction){
                            ((JavaKit.JavaDefaultKeyTypedAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
    }
    
    public static class JspDeleteCharAction extends ExtDeleteCharAction {
        
        public JspDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }
        
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(DefaultEditorKit.deletePrevCharAction);
                        if (action != null && action instanceof JavaKit.JavaDeleteCharAction){
                            ((JavaKit.JavaDeleteCharAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
    }
    
}

