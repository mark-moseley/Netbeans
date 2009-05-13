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

package org.netbeans.editor;

import javax.swing.text.Segment;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.AbstractCharSequence;

/**
 * Content of the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class DocumentContent implements AbstractDocument.Content, CharSeq, GapStart {
    
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * Invalid undoable edit being used to mark that the line undo was already
     * processed. It must never be undone/redone as it's used in a flyweight
     * way but the undomanager's operation changes states of undoable edits
     * being undone/redone.
     */
    private static final UndoableEdit INVALID_EDIT = new AbstractUndoableEdit();
    
    private static final boolean debugUndo
            = Boolean.getBoolean("netbeans.debug.editor.document.undo");
        
    /** Vector holding the marks for the document */
    private final MarkVector markVector;
    
    /** Array with gap holding the text of the document */
    private char[] charArray;

    /** Start index of the gap */
    private int gapStart;

    /** Length of the gap */
    private int gapLength;
    
    private boolean conservativeReallocation;
    
    DocumentContent() {
        charArray = EMPTY_CHAR_ARRAY;
        markVector = new MarkVector();
        
        // Insert implied '\n'
        insertText(0, "\n"); // NOI18N
    }
    
    public final int getGapStart() { // to implement GapStart
        return gapStart;
    }

    public UndoableEdit insertString(int offset, String text)
    throws BadLocationException {

        checkBounds(offset, 0, length() - 1);
        return new Edit(offset, text);
    }
    
    public UndoableEdit remove(int offset, int length)
    throws BadLocationException {

        checkBounds(offset, length, length() - 1);
        return new Edit(offset, length);
    }

    public Position createPosition(int offset) throws BadLocationException {
        checkOffset(offset);
        BasePosition pos = new BasePosition();
        markVector.insert(markVector.createMark(pos, offset));
        return pos;
    }

    public Position createBiasPosition(int offset, Position.Bias bias)
    throws BadLocationException {
        checkOffset(offset);
        BasePosition pos = new BasePosition();
        markVector.insert(markVector.createBiasMark(pos, offset, bias));
        return pos;
    }

    MultiMark createBiasMark(int offset, Position.Bias bias) throws BadLocationException {
        checkOffset(offset);
        return markVector.insert(markVector.createBiasMark(offset, bias));
    }

    MultiMark createMark(int offset) throws BadLocationException {
        checkOffset(offset);
        return markVector.insert(markVector.createMark(offset));
    }

    public int length() {
        return charArray.length - gapLength;
    }
    
    public void getChars(int offset, int length, Segment chars)
    throws BadLocationException {

        checkBounds(offset, length, length());
        
        if ((offset + length) <= gapStart) { // completely below gap
            chars.array = charArray;
            chars.offset = offset;
            
        } else if (offset >= gapStart) { // completely above gap
            chars.array = charArray;
            chars.offset = offset + gapLength;
            
        } else { // spans the gap, must copy
            chars.array = copySpanChars(offset, length);
            chars.offset = 0;
        }
        
        chars.count = length;
    }

    public String getString(int offset, int length)
    throws BadLocationException {

        checkBounds(offset, length, length());
        return getText(offset, length);
    }
    
    String getText(int offset, int length) {
        if (offset < 0 || length < 0) {
            throw new IllegalStateException("offset=" + offset + ", length=" + length); // NOI18N
        }

        String ret;
        if ((offset + length) <= gapStart) { // completely below gap
            ret = new String(charArray, offset, length);
            
        } else if (offset >= gapStart) { // completely above gap
            ret = new String(charArray, offset + gapLength, length);
            
        } else { // spans the gap, must copy
            ret = new String(copySpanChars(offset, length));
        }
        
        return ret;
    }

    public char charAt(int index) {
        return charArray[getRawIndex(index)];
    }
    
    public CharSequence createCharSequenceView() {
        return new CharSequenceImpl();
    }
    
    void compact() {
        if (gapLength > 0) {
            int newLength = charArray.length - gapLength;
            char[] newCharArray = new char[newLength];
            int gapEnd = gapStart + gapLength;
            System.arraycopy(charArray, 0, newCharArray, 0, gapStart);
            System.arraycopy(charArray, gapEnd, newCharArray, gapStart, 
                charArray.length - gapEnd);
            charArray = newCharArray;
            gapStart = charArray.length;
            gapLength = 0;
        }
        
        markVector.compact();
    }
    
    boolean isConservativeReallocation() {
        return conservativeReallocation;
    }
    
    void setConservativeReallocation(boolean conservativeReallocation) {
        this.conservativeReallocation = conservativeReallocation;
    }
    
    private int getRawIndex(int index) {
        return (index < gapStart) ? index : (index + gapLength);
    }
    
    private void moveGap(int index) {
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(charArray, index, charArray,
                gapStart + gapLength - moveSize, moveSize);
            gapStart = index;

        } else { // above gap
            int gapEnd = gapStart + gapLength;
            int moveSize = index - gapStart;
            System.arraycopy(charArray, gapEnd, charArray, gapStart, moveSize);
            gapStart += moveSize;
        }
    }
    
    private void enlargeGap(int extraLength) {
        int newLength; // means expansion length first
        if (conservativeReallocation)
            newLength = Math.min(4096, charArray.length / 10);
        else
            newLength = charArray.length;
        newLength = Math.max(10, charArray.length + newLength + extraLength);
        int gapEnd = gapStart + gapLength;
        int afterGapLength = (charArray.length - gapEnd);
        int newGapEnd = newLength - afterGapLength;
        char[] newCharArray = new char[newLength];
        System.arraycopy(charArray, 0, newCharArray, 0, gapStart);
        System.arraycopy(charArray, gapEnd, newCharArray, newGapEnd, afterGapLength);
        charArray = newCharArray;
        gapLength = newGapEnd - gapStart;
    }

    private char[] copyChars(int offset, int length) {
        char[] ret;
        if ((offset + length) <= gapStart) { // completely below gap
            ret = new char[length];
            System.arraycopy(charArray, offset, ret, 0, length);
            
        } else if (offset >= gapStart) { // completely above gap
            ret = new char[length];
            System.arraycopy(charArray, offset + gapLength, ret, 0, length);
            
        } else { // spans the gap, must copy
            ret = copySpanChars(offset, length);
        }
        
        return ret;
    }

    private char[] copySpanChars(int offset, int length) {
        char[] ret = new char[length];
        int belowGap = gapStart - offset;
        System.arraycopy(charArray, offset, ret, 0, belowGap);
        System.arraycopy(charArray, gapStart + gapLength,
            ret, belowGap, length - belowGap);
        return ret;
    }

    void insertText(int offset, String text) {
        ///*DEBUG*/System.err.println("DocumentContent.insertText(" + offset + ", \"" + text + "\")");
        int textLength = text.length();
        int extraLength = textLength - gapLength;
        if (extraLength > 0) {
            enlargeGap(extraLength);
        }
        if (offset != gapStart) {
            moveGap(offset);
        }
        text.getChars(0, textLength, charArray, gapStart);
        gapStart += textLength;
        gapLength -= textLength;
    }
    
    void removeText(int offset, int length) {
        ///*DEBUG*/System.err.println("DocumentContent.removeText(" + offset + ", " + length + ")");
        if (offset >= gapStart) { // completely over gap
            if (offset > gapStart) {
                moveGap(offset);
            }

        } else { // completely below gap or spans the gap
            int endOffset = offset + length;
            if (endOffset <= gapStart) {
                if (endOffset < gapStart) {
                    moveGap(endOffset);
                }
                gapStart -= length;
                
            } else { // spans gap
                gapStart = offset;
            }
        }

        gapLength += length;
    }
    
    private void checkOffset(int offset) throws BadLocationException {
        if (offset > length()) { // can be doc.getLength() + 1 i.e. getEndPosition()
            throw new BadLocationException("Invalid offset=" + offset // I18N // NOI18N
                + ", docLength=" + (length() - 1), offset); // I18N // NOI18N
        }
    }

    private void checkBounds(int offset, int length, int limitOffset)
    throws BadLocationException {

	if (offset < 0) {
	    throw new BadLocationException("Invalid offset=" + offset, offset); // NOI18N
	}
        if (length < 0) {
            throw new BadLocationException("Invalid length" + length, length); // NOI18N
        }
	if (offset + length > limitOffset) {
	    throw new BadLocationException(
                "docLength=" + (length() - 1) // NOI18N
                + ":  Invalid offset" // NOI18N
                + ((length != 0) ? "+length" : "") // NOI18N
                + "=" + (offset + length), // NOI18N
                (offset + length)
            );
	}
    }
    
    private final class CharSequenceImpl extends AbstractCharSequence.StringLike {

        public char charAt(int index) {
            return DocumentContent.this.charAt(index);
        }

        public int length() {
            // this is slightly different from AbstractDocument.getText(), which does not include ending '\n'
            // see #159502; in general various highliging code needs accessing the artifical
            // '\n' at the end of a document, because it is the only way how to define
            // line highlights (ie. highligh that expands beyond EOL) for the last line in the document.
            return DocumentContent.this.length();
        }

        @Override
        public String toString() {
            return DocumentContent.this.getText(0, length());
        }

    } // End of CharSequenceImpl class

    class Edit extends AbstractUndoableEdit {
        
        /** Constructor used for insert.
         * @param offset offset of insert.
         * @param text inserted text.
         */
        Edit(int offset, String text) {
            this.offset = offset;
            this.length = text.length();
            this.text = text;

            undoOrRedo(length, false); // pretend redo
            
        }
        
        /** Constructor used for remove.
         * @param offset offset of remove.
         * @param length length of the removed text.
         */
        Edit(int offset, int length) {
            this.offset = offset;
            this.length = -length;
            
            // Added to make sure the text is not inited later at unappropriate time
            this.text = getText(offset, length);

            undoOrRedo(-length, false); // pretend redo
            
        }
        
        private int offset;
        
        private int length;
        
        private String text;
        
        private MarkVector.Undo markVectorUndo;
        
        public @Override void undo() throws CannotUndoException {
            super.undo();

            if (debugUndo) {
                /*DEBUG*/System.err.println("UNDO-" + dump()); // NOI18N
            }
            undoOrRedo(-length, true);
        }
        
        public @Override void redo() throws CannotRedoException {
            super.redo();

            if (debugUndo) {
                /*DEBUG*/System.err.println("REDO-" + dump()); // NOI18N
            }
            undoOrRedo(length, false);
        }
        
        private String dump() {
            return ((length >= 0) ? "INSERT" : "REMOVE") // NOI18N
                + ":offset=" + offset + ", length=" + length // NOI18N
                + ", text='" + text + '\''; // NOI18N
        }
        
        private void undoOrRedo(int len, boolean undo) {
            // Fix text content
            if (len < 0) { // do remove
                removeText(offset, -len);
            } else { // do insert
                insertText(offset, text);
            }
            
            // Update marks
            markVectorUndo = markVector.update(offset, len, markVectorUndo);
        }

        /**
         * @return text of the modification.
         */
        final String getUndoRedoText() {
            return text;
        }
        
    }
}
