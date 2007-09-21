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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.java;

import java.util.List;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSyntaxSupport;

/**
* Various java-layers
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaDrawLayerFactory {

    public static final String JAVA_LAYER_NAME = "java-layer"; // NOI18N

    public static final int JAVA_LAYER_VISIBILITY = 1010;

    /** Layer that colors extra java information like the methods or special
     * characters in the character and string literals.
     */
    public static class JavaLayer extends DrawLayer.AbstractLayer {

        /** End of the area that is resolved right now. It saves
         * repetitive searches for '(' for multiple fragments
         * inside one identifier token.
         */
        private int resolvedEndOffset;

        private boolean resolvedValue;

        private NonWhitespaceFwdFinder nwFinder = new NonWhitespaceFwdFinder();

        public JavaLayer() {
            super(JAVA_LAYER_NAME);
        }

        public void init(DrawContext ctx) {
            resolvedEndOffset = 0; // nothing resolved
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            int nextOffset = ctx.getTokenOffset() + ctx.getTokenLength();

            setNextActivityChangeOffset(nextOffset);
            return true;
        }

        protected Coloring getMethodColoring(DrawContext ctx) {
            TokenContextPath path = ctx.getTokenContextPath().replaceStart(
                JavaLayerTokenContext.contextPath);
            return ctx.getEditorUI().getColoring(
                path.getFullTokenName(JavaLayerTokenContext.METHOD));
        }

        private boolean isMethod(DrawContext ctx) {
            int idEndOffset = ctx.getTokenOffset() + ctx.getTokenLength();
            if (idEndOffset > resolvedEndOffset) { // beyond the resolved area
                resolvedEndOffset = idEndOffset; // will resolve now
                int endOffset = ctx.getEndOffset();
                int bufferStartOffset = ctx.getBufferStartOffset();
                char[] buffer = ctx.getBuffer();
                ExtSyntaxSupport sup = (ExtSyntaxSupport) ctx.getEditorUI().getDocument().getSyntaxSupport().get(ExtSyntaxSupport.class);
                int nwOffset = Analyzer.findFirstNonWhite(buffer,
                        idEndOffset - bufferStartOffset,
                        endOffset - idEndOffset);
                if (nwOffset >= 0) { // found non-white
                    resolvedValue = (buffer[nwOffset] == '(');
                    if (!resolvedValue && buffer[nwOffset] == '<') {
                        try {
                            int[] block = sup.findMatchingBlock(ctx.getBufferStartOffset() + nwOffset, true);
                            if (block != null) {
                                int off = Utilities.getFirstNonWhiteFwd(ctx.getEditorUI().getDocument(), block[1]);
                                if (off > -1) {
                                    if (bufferStartOffset + buffer.length > off) {
                                        resolvedValue = (buffer[off - bufferStartOffset] == '(');
                                    } else {
                                        resolvedValue = (ctx.getEditorUI().getDocument().getChars(off, 1)[0] == '(');
                                    }
                                }
                            }
                        } catch (BadLocationException e) {
                            resolvedValue = false;
                        }
                    }
                } else { // must resolve after buffer end
                    try {
                        int off = ctx.getEditorUI().getDocument().find(nwFinder, endOffset, -1);
                        resolvedValue = off >= 0 && (nwFinder.getFoundChar() == '(');
                        if (!resolvedValue && nwFinder.getFoundChar() == '<') {
                            int[] block = sup.findMatchingBlock(off, true);
                            if (block != null) {
                                off = Utilities.getFirstNonWhiteFwd(ctx.getEditorUI().getDocument(), block[1]);
                                if (off > -1)
                                    resolvedValue = (ctx.getEditorUI().getDocument().getChars(off, 1)[0] == '(');
                            }
                        }
                    } catch (BadLocationException e) {
                        resolvedValue = false;
                    }
                }
            }

            return resolvedValue;
        }

        public void updateContext(DrawContext ctx) {
            if (ctx.getTokenID() == JavaTokenContext.IDENTIFIER && isMethod(ctx)) {
                Coloring mc = getMethodColoring(ctx);
                if (mc != null) {
                    mc.apply(ctx);
                }
            }
        }

    }

    /** Find first non-white character forward */
    static class NonWhitespaceFwdFinder extends FinderFactory.GenericFwdFinder {

        private char foundChar;

        public char getFoundChar() {
            return foundChar;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!Character.isWhitespace(ch)) {
                found = true;
                foundChar = ch;
                return 0;
            }
            return 1;
        }
    }

    /** Find first non-white character backward */
    public static class NonWhitespaceBwdFinder extends FinderFactory.GenericBwdFinder {

        private char foundChar;

        public char getFoundChar() {
            return foundChar;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!Character.isWhitespace(ch)) {
                found = true;
                foundChar = ch;
                return 0;
            }
            return -1;
        }
    }

}

