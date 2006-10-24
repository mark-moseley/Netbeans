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

package org.netbeans.modules.editor.guards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.openide.text.NbDocument;

/** Represents one guarded section.
 */
public abstract class GuardedSectionImpl {
    /** Name of the section. */
    String name;
    
    /** If the section is valid or if it was removed. */
    boolean valid = false;
    
    final GuardedSectionsImpl guards;
    
    GuardedSection guard;
    
    /** Get the name of the section.
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /** Creates new section.
     * @param name Name of the new section.
     */
    GuardedSectionImpl(String name, GuardedSectionsImpl guards) {
        this.name = name;
        this.guards = guards;
    }
    
    public final void attach(GuardedSection guard) {
        this.guard = guard;
        valid = true;
    }
    
    /** Set the name of the section.
     * @param name the new name
     * @exception PropertyVetoException if the new name is already in use
     */
    public void setName(String name) throws PropertyVetoException {
        if (!this.name.equals(name)) {
            synchronized (this.guards.sections) {
                if (valid) {
                    if (this.guards.sections.get(name) != null)
                        throw new PropertyVetoException("", new PropertyChangeEvent(this, "name", this.name, name)); // NOI18N
                    this.guards.sections.remove(this.name);
                    this.name = name;
                    this.guards.sections.put(name, this);
                }
            }
        }
        
    }
    
    /** Deletes the text of the section and
     * removes it from the table. The section will then be invalid
     * and it will be impossible to use its methods.
     */
    public void deleteSection() {
        synchronized (this.guards.sections) {
            if (valid) {
                try {
                    this.guards.sections.remove(name);
                    // get document should always return the document, when section
                    // is deleted, because it is still valid (and valid is only
                    // when document is loaded.
                    unmarkGuarded(this.guards.getDocument());
                    deleteText();
                    valid = false;
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
    
    /**
     * Tests if the section is still valid - it is not removed from the
     * source.
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Removes the section from the Document, but retains the text contained
     * within. The method should be used to unprotect a region of code
     * instead of calling NbDocument.
     * @return true if the operation succeeded.
     */
    public void removeSection() {
        synchronized (this.guards.sections) {
            if (valid) {
                this.guards.sections.remove(name);
                // get document should always return the document, when section
                // is deleted, because it is still valid (and valid is only
                // when document is loaded.
                unmarkGuarded(this.guards.getDocument());
                valid = false;
            }
        }
    }
    
    /** Delete one new-line character before the specified offset.
     * This method is used when guarded blocks are deleted. When new guarded block is created,
     * there is added one more new-line before it, so this method remove this char in the end of
     * guarded block life cycle.
     * It works only when there is "\n" char before the offset and no problem occured (IOException...)
     * @param offset The begin of removed guarded block.
     */
    void deleteNewLineBeforeBlock(int offset) {
        if (offset > 1) {
            try {
                PositionBounds b = PositionBounds.create(offset - 1, offset, guards);
                String s = b.getText();
                if (s.equals("\n")) { // NOI18N
                    b.setText(""); // NOI18N
                }
            } catch (BadLocationException e) {
            }
        }
    }
    
    /** Set the text contained in this section.
     * Newlines are automatically added to all text segments handled,
     * unless there was already one.
     * All guarded blocks must consist of entire lines.
     * This applies to the contents of specific guard types as well.
     * @param bounds the bounds indicating where the text should be set
     * @param text the new text
     * @param minLen If true the text has to have length more than 2 chars.
     * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
     */
    protected boolean setText(PositionBounds bounds, String text, boolean minLen) {
        if (!valid)
            return false;
        
        // modify the text - has to end with new line and the length
        // has to be more then 2 characters
        if (minLen) {
            if (text.length() == 0)
                text = " \n"; // NOI18N
            else if (text.length() == 1)
                text = text.equals("\n") ? " \n" : text + "\n"; // NOI18N
        }
        
        if (!text.endsWith("\n")) // NOI18N
            text = text + "\n"; // NOI18N
        
        try {
            bounds.setText(text);
            return true;
        } catch (BadLocationException e) {
        }
        return false;
    }
    
    /** Marks or unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     * @param bounds The rangeof text which should be marked or unmarked.
     * @param mark true means mark, false unmark.
     */
    void markGuarded(StyledDocument doc, PositionBounds bounds, boolean mark) {
        int begin = bounds.getBegin().getOffset();
        int end = bounds.getEnd().getOffset();
        if (mark) {
            NbDocument.markGuarded(doc, begin, end - begin);
        } else
            NbDocument.unmarkGuarded(doc, begin, end - begin);
    }
    
    /** Marks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    abstract void markGuarded(StyledDocument doc);
    
    /** Unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    abstract void unmarkGuarded(StyledDocument doc);
    
    /** Deletes the text in the section.
     * @exception BadLocationException
     */
    abstract void deleteText() throws BadLocationException;
    
    /** Gets the begin of section. To this position is set the caret
     * when section is open in the editor.
     */
    public abstract Position getCaretPosition();
    
    /** Gets the text contained in the section.
     * @return The text contained in the section.
     */
    public abstract String getText();

    /** Assures that a position is not inside the guarded section. Complex guarded sections
     * that contain portions of editable text can return true if the tested position is
     * inside one of such portions provided that permitHoles is true.
     * @param pos position in question
     * @param permitHoles if false, guarded section is taken as a monolithic block
     * without any holes in it regardless of its complexity.
     */
    public abstract boolean contains(Position pos, boolean permitHoles);
    /** Returns a position after the whole guarded block that is safe for insertions.
     */
    public abstract Position getEndPosition();
    /** Returns position before the whole guarded block that is safe for insertions.
     */
    public abstract Position getStartPosition();
    
    public abstract void resolvePositions() throws BadLocationException;

}
