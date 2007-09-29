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
package org.netbeans.modules.xml.text.syntax;

import java.awt.event.ActionEvent;
import java.awt.Panel;
import java.util.*;

// prevent ambiguous reference to Utilities
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.openide.awt.StatusDisplayer;

// we depend on NetBeans editor stuff
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.modules.editor.*;

import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.text.completion.NodeSelector;
import org.netbeans.modules.xml.text.completion.XMLCompletion;


/**
 * NetBeans editor kit implementation for xml content type.
 * <p>
 * It provides syntax coloring, code completion, actions, abbrevirations, ...
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 * @author Sandeep
 */
public class XMLKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider {

    /** Serial Version UID */
    private static final long serialVersionUID =5326735092324267367L;
    
    // comment action name
    public static final String xmlCommentAction = "xml-comment";
    
    // uncomment action name
    public static final String xmlUncommentAction = "xml-uncomment";

    // dump XML sysntax
    public static final String xmlTestAction = "xml-dump";
    
    // hack to be settings browseable //??? more info needed
    public static Map settings;
    
    //temporary - will be removed when lexer is stabilized
    private static final boolean J2EE_LEXER_COLORING = Boolean.getBoolean("j2ee_lexer_coloring"); //NOI18N
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(XMLKit.class);
    }
    
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        return new XMLDefaultSyntax();
//        return new JJEditorSyntax(
//            new XMLSyntaxTokenManager(null).new Bridge(),
//            new XMLSyntaxTokenMapper(),
//            XMLTokenContext.contextPath
//        );
    }

    public Document createDefaultDocument() {
        if(J2EE_LEXER_COLORING) {
            Document doc = new XMLEditorDocument(this.getClass());
            Object mimeType = doc.getProperty("mimeType"); //NOI18N
            if (mimeType == null){
                doc.putProperty("mimeType", getContentType()); //NOI18N
            }
            doc.putProperty(Language.class, XMLTokenId.language());
            return doc;
        } else {
            return new NbEditorDocument (this.getClass());
        }
    }


    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new XMLSyntaxSupport(doc);
    }
    

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        //return new org.netbeans.modules.xml.text.completion.XMLCompletion(extEditorUI);
        return null;
    }
    
    public Completion createCompletionForProvider(ExtEditorUI extEditorUI) {
        return new XMLCompletion(extEditorUI);
    }
    
    public void install(JEditorPane c) {
        super.install(c);
        if (Boolean.getBoolean("netbeans.experimental.xml.nodeselectors")) {  // NOI18N
            new NodeSelector(c);
        }
    }

    // hack to be settings browseable //??? more info needed    
    public static void setMap(Map map) {
        settings = map;
    }

    // hack to be settings browseable //??? more info needed        
    public Map getMap() {
        return settings;
    }

    //??? +xml handling
    public String getContentType() {
        return XMLDataObject.MIME_TYPE;
    }

    /**
     * Provide XML related actions.
     */
    protected Action[] createActions() {
        Action[] actions = new Action[] {
            new XMLCommentAction(),
            new XMLUncommentAction(),
            new TestAction(),
        };
        return TextAction.augmentList(super.createActions(), actions);
    }
    
    
    public abstract static class XMLEditorAction extends BaseAction {
        
        public XMLEditorAction (String id) {
            super(id);
            String desc = org.openide.util.NbBundle.getMessage(XMLKit.class,id); // NOI18N
            if (desc != null) {
                putValue(SHORT_DESCRIPTION, desc);
            }
        }
        
        /**
         * Uniform way of reporting problem while action executing #15589
         */
        protected void problem(String reason) {
            if (reason != null) StatusDisplayer.getDefault().setStatusText("Cannot proceed: " + reason);
            new Panel().getToolkit().beep();
        }
    }
    
    /**
     * Comment out editor selection.
     */
    public static class XMLCommentAction extends XMLEditorAction {
        
        private static final long serialVersionUID =4004056745446061L;

        private static final String commentStartString = "<!--";  //NOI18N
        private static final String commentEndString = "-->";  //NOI18N
        
        public XMLCommentAction() {
            super( xmlCommentAction);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) return;
            if (!target.isEditable() || !target.isEnabled()) {
                problem(null);
                return;
            }
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            try {
                if (caret.isSelectionVisible()) {
                    int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                    int endPos = target.getSelectionEnd();
                    doc.atomicLock();
                    try {

                        if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                            endPos--;
                        }

                        int pos = startPos;
                        int lineCnt = Utilities.getRowCount(doc, startPos, endPos);                            

                        for (;lineCnt > 0; lineCnt--) {
                            doc.insertString(pos, commentStartString, null); 
                            doc.insertString(Utilities.getRowEnd(doc,pos), commentEndString, null);
                            pos = Utilities.getRowStart(doc, pos, +1);
                        }

                    } finally {
                        doc.atomicUnlock();
                    }
                } else { // selection not visible
                    doc.insertString(Utilities.getRowStart(doc, target.getSelectionStart()),
                        commentStartString, null);
                    doc.insertString(Utilities.getRowEnd(doc, target.getSelectionStart()),
                        commentEndString, null);
                }
            } catch (BadLocationException e) {
                problem(null);
            }
        }
        
    }

    /**
     * Uncomment selected text
     *
     */
    public static class XMLUncommentAction extends XMLEditorAction {
        private static final String commentStartString = "<!--";  //NOI18N
        private static final String commentEndString = "-->";  //NOI18N
        private static final char[] commentStart = {'<','!','-','-'};
        private static final char[] commentEnd = {'-','-','>'};
        
        static final long serialVersionUID = 40040567454546061L;
        
        public XMLUncommentAction() {
            super( xmlUncommentAction);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) return;
            if (!target.isEditable() || !target.isEnabled()) {
                problem(null);
                return;
            }
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            try {
                if (caret.isSelectionVisible()) {
                    int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                    int endPos = target.getSelectionEnd();
                    doc.atomicLock();
                    try {

                        if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                            endPos--;
                        }

                        int pos = startPos;
                        int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                        char[] startChars, endChars;

                        for (; lineCnt > 0; lineCnt-- ) {
                            startChars = doc.getChars(pos, 4 );
                            endChars = doc.getChars(Utilities.getRowEnd(doc,pos)-3, 3 );

                            if(startChars[0] == commentStart[0] && startChars[1] == commentStart[1] &&
                                startChars[2] == commentStart[2] && startChars[3] == commentStart[3] &&
                                endChars[0] == commentEnd[0] && endChars[1] == commentEnd[1] && endChars[2] == commentEnd[2] ){

                                doc.remove(pos,4);
                                doc.remove(Utilities.getRowEnd(doc,pos)-3,3);
                            }                                
                            pos = Utilities.getRowStart(doc, pos, +1);
                        }

                    } finally {
                        doc.atomicUnlock();
                    }
                } else { // selection not visible
                  char[] startChars = doc.getChars(target.getSelectionStart(), 4 );
                  char[] endChars = doc.getChars(Utilities.getRowEnd(doc,target.getSelectionStart())-3, 3 );
                  if(startChars[0] == commentStart[0] && startChars[1] == commentStart[1] &&
                                startChars[2] == commentStart[2] && startChars[3] == commentStart[3] &&
                                endChars[0] == commentEnd[0] && endChars[1] == commentEnd[1] && endChars[2] == commentEnd[2] ){
                        doc.remove(target.getSelectionStart(),4);
                        doc.remove(Utilities.getRowEnd(doc,target.getSelectionStart())-3,3);
                    }
                }
            } catch (BadLocationException e) {
                problem(null);
            }
        }
    }    

    
    /**
     * Dump it.
     */
    public static class TestAction extends XMLEditorAction {
        
        private static final long serialVersionUID =4004056745446099L;

        public TestAction() {
            super( xmlTestAction);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) return;
            if (!target.isEditable() || !target.isEnabled()) {
                problem(null);
                return;
            }
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            try {
                doc.dump(System.out);    
                if (target == null)  throw new BadLocationException(null,0);  // folish compiler
            } catch (BadLocationException e) {
                problem(null);
            }
        }
        
    }
    
    public class XMLEditorDocument extends NbEditorDocument {
        public XMLEditorDocument(Class kitClass) {
            super(kitClass);
        }
        
        public boolean addLayer(DrawLayer layer, int visibility) {
            //filter out the syntax layer adding
            if(!(layer instanceof DrawLayerFactory.SyntaxLayer)) {
                return super.addLayer(layer, visibility);
            } else {
                return false;
            }
        }
    }
}
