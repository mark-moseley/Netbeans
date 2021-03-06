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
package org.netbeans.modules.editor.guards;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.openide.text.NbDocument;

/**
 * A range bounded by two {@link Position}s. This class is derived from
 * {@link org.openide.text.PositionBounds} in fact.
 *
 * @author Petr Hamernik
 */
public final class PositionBounds {

    /** Begin */
    private Position begin;

    /** End */
    private Position end;

    private final GuardedSectionsImpl guards;
    
    private static final class UnresolvedPosition implements Position {

        private int offset;
        
        public UnresolvedPosition(int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return this.offset;
        }
    }
    
    private static final class BackwardPosition implements Position {
        
        private Position delegate;
        
        public BackwardPosition(Position delegate) {
            this.delegate = delegate;
        }
    
        public int getOffset() {
            return this.delegate.getOffset() + 1;
        }
    }

    /** Creates new <code>PositionBounds</code>.
     * @param begin the start position of the range
     * @param end the end position of the range
    */
    public PositionBounds(Position begin, Position end, GuardedSectionsImpl guards) {
        this.begin = begin;
        this.end = end;
        this.guards = guards;
    }
    
    public static PositionBounds create(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        return new PositionBounds(doc.createPosition(begin), doc.createPosition(end), guards);
    }
    
    /**
     * creates bounds with backward begin position allowing to insert text to 
     * begin position while the begin position remains unchanged. The behavior
     * desired for body sections but not for header or footer sections.
     */
    public static PositionBounds createBodyBounds(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        return new PositionBounds(new BackwardPosition(doc.createPosition(begin - 1)), doc.createPosition(end), guards);
    }
    
    /**
     * creates a position bounds object without checking position validity since the document may be empty yet.
     * @see #resolvePositions
     */
    public static PositionBounds createUnresolved(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        return new PositionBounds(new UnresolvedPosition(begin), new UnresolvedPosition(end), guards);
    }
    
    /**
     * @see #createBodyBounds
     * @see #resolvePositions
     */
    public static PositionBounds createBodyUnresolved(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        return new PositionBounds(new BackwardPosition(new UnresolvedPosition(begin - 1)), new UnresolvedPosition(end), guards);
    }
    
    public void resolvePositions() throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        Position b, e;
        if (end instanceof UnresolvedPosition) {
            if (begin instanceof BackwardPosition) {
                b = ((BackwardPosition) begin).delegate = doc.createPosition(
                        ((BackwardPosition) begin).delegate.getOffset());
            } else {
                b = doc.createPosition(begin.getOffset());
            }
            e = doc.createPosition(end.getOffset());
            this.begin = b;
            this.end = e;
        }
    }

    /**
     * Get the starting position of this range.
     * @return the begin position
     */
    public Position getBegin() {
        return begin;
    }

    /**
     * Get the ending position of this range.
     * @return the end position
     */
    public Position getEnd() {
        return end;
    }

    /** Replaces the text contained in this range.
    * This replacement is done atomically, and so is preferable to manual inserts & removes.
    * <p>If you are running this from user-oriented code, you may want to wrap it in {@link NbDocument#runAtomicAsUser}.
    * @param text new text to insert over existing text
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public void setText(final String text) throws BadLocationException  {
        final StyledDocument doc = guards.getDocument();
        final BadLocationException[] hold = new BadLocationException[] { null };
        Runnable run = new Runnable() {
                public void run() {
                    try {
                        int p1 = begin.getOffset();
                        int p2 = end.getOffset();
                        int len = text.length();

                        if (len == 0) { // 1) set empty string

                            if (p2 > p1) {
                                doc.remove(p1, p2 - p1);
                            }
                        } else { // 2) set non empty string

                            int docLen = doc.getLength();

                            if ((p2 - p1) >= 1) {
                                doc.insertString(p1 + 1, text, null);

                                // [MaM] compute length of inserted string
                                len = doc.getLength() - docLen;
                                doc.remove(p1 + 1 + len, p2 - p1 - 1);
                                doc.remove(p1, 1);
                            } else {
                                // zero or exactly one character:
                                // adjust the positions if they are
                                // biased to not absorb the text inserted at the start/end
                                // it would be ridiculous not to have text set by setText
                                // be part of the bounds.
                                doc.insertString(p1, text, null);

                                // [MaM] compute length of inserted string
                                len = doc.getLength() - docLen;

                                if (p2 > p1) {
                                    doc.remove(p1 + len, p2 - p1);
                                }

                                if (begin.getOffset() != p1) {
                                    begin = doc.createPosition(p1);
                                }

                                if ((end.getOffset() - p1) != len) {
                                    end = doc.createPosition(p1 + len);
                                }
                            }
                        }
                    } catch (BadLocationException e) {
                        hold[0] = e;
                    }
                }
            };

        NbDocument.runAtomic(doc, run);

        if (hold[0] != null) {
            throw hold[0];
        }
    }

    /** Finds the text contained in this range.
    * @return the text
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public String getText() throws BadLocationException {
        StyledDocument doc = this.guards.getDocument();
        int p1 = begin.getOffset();
        int p2 = end.getOffset();

        return doc.getText(p1, p2 - p1);
    }

    /* @return the bounds as the string. */
    public String toString() {
        StringBuilder buf = new StringBuilder("Position bounds["); // NOI18N

        try {
            String content = getText();
            buf.append(begin);
            buf.append(","); // NOI18N
            buf.append(end);
            buf.append(",\""); // NOI18N
            buf.append(content);
            buf.append("\""); // NOI18N
        } catch (BadLocationException e) {
            buf.append("Invalid: "); // NOI18N
            buf.append(e.getMessage());
        }

        buf.append("]"); // NOI18N

        return buf.toString();
    }
}
