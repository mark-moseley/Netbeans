/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.indent.Indent;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.text.IndentEngine;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * The action supposed to fix an empty javadoc block.
 * The present use case is to complete javadoc generated on <code>/** + &lt;ENTER&gt;</code>.
 * See org.netbeans.modules.editor.java.JavaKit.JavaInsertBreakAction
 * 
 * <p><b>Note:</b> The text action is a temporary solution until the editor
 * introduce some SPI to plug.</p>
 * 
 * @author Jan Pokorsky
 */
public final class GenerateJavadocAction extends TextAction {

    GenerateJavadocAction() {
        super("fix-javadoc-action"); // NOI18N
    }
    
    public static GenerateJavadocAction create() {
        return new GenerateJavadocAction();
    }

    public void actionPerformed(ActionEvent e) {
        final JTextComponent jtc = getTextComponent(e);
        final Document doc = jtc.getDocument();
        
        if (!(doc instanceof StyledDocument)) {
            // unsupported document
            return;
        }
        try {
            Descriptor desc = prepareGenerating(doc, jtc.getCaretPosition());
            
            if (desc != null) {
                // add javadoc content
                generate(doc, desc, jtc);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    private Descriptor prepareGenerating(final Document doc, final int offset) throws IOException {
        JavaSource js = JavaSource.forDocument(doc);
        if (js == null) {
            return null;
        }
        
        final Descriptor desc = new Descriptor();
        
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TokenHierarchy tokens = javac.getTokenHierarchy();
                TokenSequence ts = tokens.tokenSequence();
                ts.move(offset);
                if (!ts.moveNext() || ts.token().id() != JavaTokenId.JAVADOC_COMMENT) {
                    return;
                }
                
                desc.caret = doc.createPosition(offset);
                final int jdBeginOffset = ts.offset();
                int offsetBehindJavadoc = ts.offset() + ts.token().length();
                
                while (ts.moveNext()) {
                    TokenId tid = ts.token().id();
                    if (tid != JavaTokenId.WHITESPACE && tid != JavaTokenId.LINE_COMMENT && tid != JavaTokenId.BLOCK_COMMENT) {
                        offsetBehindJavadoc = ts.offset();
                        if (ts.token().length() > 1) {
                            // it is magic for TreeUtilities.pathFor to proper tree
                            ++offsetBehindJavadoc;
                        }
                        break;
                    }
                }
                
                TreePath tp = javac.getTreeUtilities().pathFor(offsetBehindJavadoc);
                Tree leaf = tp.getLeaf();
                Kind kind = leaf.getKind();
                SourcePositions positions = javac.getTrees().getSourcePositions();
                
                while (kind != Kind.CLASS && kind != Kind.METHOD && kind != Kind.VARIABLE && kind != Kind.COMPILATION_UNIT) {
                    tp = tp.getParentPath();
                    if (tp == null) {
                        leaf = null;
                        kind = null;
                        break;
                    }
                    leaf = tp.getLeaf();
                    kind = leaf.getKind();
                }
                
                if (leaf == null || positions.getStartPosition(javac.getCompilationUnit(), leaf) < jdBeginOffset) {
                    // not a class member javadoc -> ignore
                    return;
                }
                
                if (kind != Kind.COMPILATION_UNIT && !Analyzer.hasErrors(leaf) && Access.PRIVATE.isAccessible(javac, tp, true)) {
                    Element el = javac.getTrees().getElement(tp);
                    if (el != null) {
                        // better move outside the javac task?
                        SourceVersion sv = Analyzer.resolveSourceVersion(javac.getFileObject());
                        JavadocGenerator gen = new JavadocGenerator(sv);
                        desc.javadoc = gen.generateComment(el, javac);
                    }
                }
            }
            
        }, true);
        
        return desc.javadoc != null? desc: null;
    }

    private void generate(final Document doc, final Descriptor desc, final JTextComponent jtc) throws BadLocationException {
        final Indent ie = Indent.get(doc);
        try {
            ie.lock();
            NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {

                public void run() {
                    try {
                        int caretPos = jtc.getCaretPosition();
                        generateJavadoc(doc, desc, ie);
                        // move caret
                        jtc.setCaretPosition(caretPos);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            });
        } finally {
            ie.unlock();
        }
    }

    private void generateJavadoc(Document doc, Descriptor desc, Indent ie) throws BadLocationException {
        StringTokenizer lines = new StringTokenizer(desc.javadoc, "\n"); // NOI18N
        Position pos = desc.caret;
        int startOffset = pos.getOffset();
        for (String line; lines.hasMoreTokens(); ) {
            line = lines.nextToken();
            if (line.startsWith("/**") || line.endsWith("*/")) { // NOI18N
                // ignore first and last line since they are already generated
                continue;
            } else if (line.startsWith(" * ")) { // NOI18N
                line = line.substring(3);
            }
            
            if (line.length() > 0) {
                // ignore empty line
                doc.insertString(pos.getOffset(), '\n' + line, null);
            }
            
        }
        
        if (startOffset != pos.getOffset()) {
            ie.reindent(startOffset, pos.getOffset());
        }

    }
    
    private static final class Descriptor {
        String javadoc;
        /** position inside javadoc where to write */
        Position caret;
    }

}
