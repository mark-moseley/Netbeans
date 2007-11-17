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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

/**
* Document implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseDocumentEvent extends AbstractDocument.DefaultDocumentEvent {

    private static final boolean debugUndo
            = Boolean.getBoolean("netbeans.debug.editor.document.undo");

    private DocumentContent.Edit modifyUndoEdit;
    
    private FixLineSyntaxState fixLineSyntaxState;

    /** Previous event in the chain of the events that were
    * connected together to be undone/redone at once.
    */
    private UndoableEdit previous;

    private boolean inUndo;

    private boolean inRedo;

    /** Unfortunately the undo() and redo() methods
     * call writeLock() which the protected final method
     * in AbstractDocument. If somebody calls runAtomic()
     * or runAtomicAsUser() and the exception is thrown
     * in the body of the executed runnables, the document
     * automatically undoes the changes. Unfortunately
     * the undo() in AbstractDocument has the writeLock()
     * call hardcoded which throws IllegalStateException()
     * in this situation.
     * Therefore the BaseDocumentEvent cannot call
     * the super.undo() and has to reimplement the functionality
     * of all the parents. The extWriteLock() and extWriteUnlock()
     * are used instead.
     */

    private boolean hasBeenDone2;

    private boolean alive2;

    private boolean inProgress2;

    private Hashtable changeLookup2;
    
    private int lfCount = -1;

    private AttributeSet attribs = null;
    
    static final long serialVersionUID =-7624299835780414963L;

    /** Construct document event instance.
    * @param offset position in the document where the insert/remove/change
    *   occured
    * @param length number of the characters affected by the event
    * @param type type of the event - INSERT/REMOVE/CHANGE
    */
    public BaseDocumentEvent(BaseDocument doc, int offset, int length,
                             DocumentEvent.EventType type) {
        ((AbstractDocument)doc).super(offset, length, type);

	hasBeenDone2 = true;
	alive2 = true;
        inProgress2 = true;
    }

    /* package */ void attachChangeAttribs(AttributeSet attribs) {
        this.attribs = attribs;
    }
    
    /**
     * Gets the attributes associated with the change that caused this event.
     * If no attributes were associated with the document change, this method
     * may return <code>null</code>;
     * 
     * @return The <code>AttributeSet</code> associated with the document
     *   change or <code>null</code>.
     * @since 1.17
     */
    public final AttributeSet getChangeAttributes() {
        return attribs;
    }
    
    protected UndoableEdit findEdit(Class editClass) {
        for (int i = edits.size() - 1; i >= 0; i--) {
            Object edit = edits.get(i);
            if (editClass.isInstance(edit)) {
                return (UndoableEdit)edit;
            }
        }
        return null;
    }

    private DocumentContent.Edit getModifyUndoEdit() {
        if (getType() == DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Cannot be called for CHANGE events."); // NOI18N
        }

        if (modifyUndoEdit == null) {
            modifyUndoEdit = (DocumentContent.Edit)findEdit(DocumentContent.Edit.class);
        }
        return modifyUndoEdit;
    }

    private FixLineSyntaxState getFixLineSyntaxState() {
        if (getType() == DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Cannot be called for CHANGE events."); // NOI18N
        }

        if (fixLineSyntaxState == null) {
            fixLineSyntaxState = ((FixLineSyntaxState.BeforeLineUndo)findEdit(
                FixLineSyntaxState.BeforeLineUndo.class)).getMaster();
        }
        return fixLineSyntaxState;
    }

    /** Gets the characters that were inserted/removed or null
    * for change event.
    * Characters must be used only in readonly mode as the
    * character array is shared by all listeners and also by 
    * modification event itself.
     * @deprecated
    */
    public char[] getChars() {
        String text = getText();
        return (text != null) ? text.toCharArray() : null;
    }

    /** Get the text that was inserted/removed or null
    * for change event.
    */
    public String getText() {
        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getUndoRedoText() : null;
    }

    /**
     * Get the line at which the insert/remove occured.
     * @deprecated
     */
    public int getLine() {
        Element lineRoot = ((BaseDocument)getDocument()).getParagraphElement(0).getParentElement();
        int lineIndex = lineRoot.getElementIndex(getOffset());
        return lineIndex;
//        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getLine() : 0;
    }

    /** Get the count of '\n' (line-feeds) contained in the inserted/removed text. */
    public int getLFCount() {
        if (getType() == DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Not available for CHANGE events"); // NOI18N
        }

        if (lfCount == -1) {
            String text = getText();
            int lfCnt = 0;
            for (int i = text.length() - 1; i >= 0; i--) {
                if (text.charAt(i) == '\n') {
                    lfCnt++;
                }
            }
            lfCount = lfCnt;
        }
        
        return lfCount;
    }
            
    /** Get the offset at which the updating of the syntax stopped so there
    * are no more changes in the tokens after this point.
    */
    public int getSyntaxUpdateOffset() {
        if (getType() == DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Not available for CHANGE events"); // NOI18N
        }

        return getFixLineSyntaxState().getSyntaxUpdateOffset();
    }
    
    List getSyntaxUpdateTokenList() {
        return getFixLineSyntaxState().getSyntaxUpdateTokenList();
    }
    
    public String getDrawLayerName() {
        if (getType() != DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Can be called for CHANGE events only."); // NOI18N
        }

        DrawLayerChange dlc = (DrawLayerChange)findEdit(DrawLayerChange.class);

        return (dlc != null) ? dlc.getDrawLayerName() : null;
    }

    public int getDrawLayerVisibility() {
        if (getType() != DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Can be called for CHANGE events only."); // NOI18N
        }

        DrawLayerChange dlc = (DrawLayerChange)findEdit(DrawLayerChange.class);

        return (dlc != null) ? dlc.getDrawLayerVisibility() : -1;
    }

    /** Whether this event is being fired because it's being undone. */
    public boolean isInUndo() {
        return inUndo;
    }

    /** Whether this event is being fired because it's being redone. */
    public boolean isInRedo() {
        return inRedo;
    }

    public void undo() throws CannotUndoException {
        BaseDocument doc = (BaseDocument)getDocument();
        inUndo = true;

        boolean notifyMod;
        try {
            notifyMod = doc.notifyModifyCheckStart(0, "undo() vetoed"); // NOI18N
        } catch (BadLocationException ex) {
            throw new CannotUndoException();
        }
        boolean modFinished = false;

        // Super of undo()
        doc.extWriteLock(); // call this extWriteLock() instead of writeLock()
        try {
            if (!canUndo()) {
                throw new CannotUndoException();
            }
            hasBeenDone2 = false;
           
            doc.lastModifyUndoEdit = null; // #8692 check last modify undo edit

            if (debugUndo) {
                /*DEBUG*/System.err.println("UNDO in doc=" + doc);
            }
            
            int i = edits.size();
            if (i > 0) {
                doc.markModsUndoneOrRedone();
            }
            while (i-- > 0) {
                UndoableEdit e = (UndoableEdit)edits.elementAt(i);
                e.undo();
            }

            // fire a DocumentEvent to notify the view(s)
            if (getType() == DocumentEvent.EventType.REMOVE) {
                doc.fireInsertUpdate(this);
            } else if (getType() == DocumentEvent.EventType.INSERT) {
                doc.fireRemoveUpdate(this);
            } else {
                doc.fireChangedUpdate(this);
            }
        } finally {
            doc.extWriteUnlock(); // call this extWriteUnlock() instead of writeUnlock()
            if (notifyMod) {
                doc.notifyModifyCheckEnd(modFinished);
            }
        }
        // End super of undo()

        if (previous != null) {
            previous.undo();
        }

        inUndo = false;
    }

    public void redo() throws CannotRedoException {
        BaseDocument doc = (BaseDocument)getDocument();
        boolean notifyMod;
        try {
            notifyMod = doc.notifyModifyCheckStart(0, "redo() vetoed"); // NOI18N
        } catch (BadLocationException ex) {
            throw new CannotRedoException();
        }

        inRedo = true;
        if (previous != null) {
            previous.redo();
        }

        boolean modFinished = false; // Whether modification succeeded

        // Super of redo()
        doc.extWriteLock(); // call this extWriteLock() instead of writeLock()
        try {

            if (!canRedo()) {
                throw new CannotRedoException();
            }
            hasBeenDone2 = true;

            if (debugUndo) {
                /*DEBUG*/System.err.println("REDO in doc=" + doc);
            }
            
            Enumeration cursor = edits.elements();
            if (cursor.hasMoreElements()) {
                doc.markModsUndoneOrRedone();
            }
            while (cursor.hasMoreElements()) {
                ((UndoableEdit)cursor.nextElement()).redo();
            }

            // fire a DocumentEvent to notify the view(s)
            if (getType() == DocumentEvent.EventType.INSERT) {
                doc.fireInsertUpdate(this);
            } else if (getType() == DocumentEvent.EventType.REMOVE) {
                doc.fireRemoveUpdate(this);
            } else {
                doc.fireChangedUpdate(this);
            }
        } finally {
            doc.extWriteUnlock(); // call this extWriteUnlock() instead of writeUnlock()
            if (notifyMod) {
                doc.notifyModifyCheckEnd(modFinished);
            }
        }
        // End super of redo()

        inRedo = false;
    }

    public boolean addEdit(UndoableEdit anEdit) {
        // Super of addEdit()

        // if the number of changes gets too great, start using
        // a hashtable for to locate the change for a given element.
        if ((changeLookup2 == null) && (edits.size() > 10)) {
            changeLookup2 = new Hashtable();
            int n = edits.size();
            for (int i = 0; i < n; i++) {
                Object o = edits.elementAt(i);
                if (o instanceof DocumentEvent.ElementChange) {
                    DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange) o;
                    changeLookup2.put(ec.getElement(), ec);
                }
            }
        }

        // if we have a hashtable... add the entry if it's 
        // an ElementChange.
        if ((changeLookup2 != null) && (anEdit instanceof DocumentEvent.ElementChange)) {
            DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange) anEdit;
            changeLookup2.put(ec.getElement(), ec);
        }

	if (!inProgress2) {
	    return false;

	} else {
	    UndoableEdit last = lastEdit();

	    // If this is the first subedit received, just add it.
	    // Otherwise, give the last one a chance to absorb the new
	    // one.  If it won't, give the new one a chance to absorb
	    // the last one.

	    if (last == null) {
		edits.addElement(anEdit);
	    }
	    else if (!last.addEdit(anEdit)) {
		if (anEdit.replaceEdit(last)) {
		    edits.removeElementAt(edits.size()-1);
		}
		edits.addElement(anEdit);
	    }

	    return true;
        }
        // End super of addEdit()
    }

    private boolean isLastModifyUndoEdit() {
        if (true)
            return true; // #83740 - make this method always return true
        if (getType() == DocumentEvent.EventType.CHANGE) {
            return true; // OK in this case
        }
        
        BaseDocument doc = (BaseDocument)getDocument();
        doc.extWriteLock(); // lock to sync if ongoing doc change
        try {
            // #8692 check last modify undo edit
            if (doc.lastModifyUndoEdit == null) {
                return true; // OK in this case
            }
            
            DocumentContent.Edit undoEdit = getModifyUndoEdit();
            return (undoEdit == doc.lastModifyUndoEdit);
        } finally {
            doc.extWriteUnlock();
        }
    }

    public boolean canUndo() {
        // Super of canUndo
	return !inProgress2 && alive2 && hasBeenDone2
        // End super of canUndo
            && isLastModifyUndoEdit();
    }

    /**
     * Returns false if isInProgress or if super does.
     * 
     * @see	#isInProgress
     */
    public boolean canRedo() {
        // Super of canRedo
	return !inProgress2 && alive2 && !hasBeenDone2;
        // End super of canRedo
    }

    public boolean isInProgress() {
        // Super of isInProgress()
        return inProgress2;
        // End super of isInProgress()
    }

    public String getUndoPresentationName() {
        return "";
    }

    public String getRedoPresentationName() {
        return "";
    }

    /** Returns true if this event can be merged by the previous
    * one (given as parameter) in the undo-manager queue.
    */
    public boolean canMerge(BaseDocumentEvent evt) {
        if (getType() == DocumentEvent.EventType.INSERT) { // last was insert
            if (evt.getType() == DocumentEvent.EventType.INSERT) { // adding insert to insert
                String text = getText();
                String evtText = evt.getText();
                if ((getLength() == 1 || (getLength() > 1 && Analyzer.isSpace(text)))
                        && (evt.getLength() == 1 || (evt.getLength() > 1
                                                     && Analyzer.isSpace(evtText)))
                        && (evt.getOffset() + evt.getLength() == getOffset()) // this follows the previous
                   ) {
                    BaseDocument doc = (BaseDocument)getDocument();
                    boolean thisWord = doc.isIdentifierPart(text.charAt(0));
                    boolean lastWord = doc.isIdentifierPart(evtText.charAt(0));
                    if (thisWord && lastWord) { // add word char to word char(s)
                        return true;
                    }
                    boolean thisWhite = doc.isWhitespace(text.charAt(0));
                    boolean lastWhite = doc.isWhitespace(evtText.charAt(0));
                    if ((lastWhite && thisWhite)
                            || (!lastWhite && !lastWord && !thisWhite && !thisWord)
                       ) {
                        return true;
                    }
                }
            } else { // adding remove to insert
            }
        } else { // last was remove
            if (evt.getType() == DocumentEvent.EventType.INSERT) { // adding insert to remove
            } else { // adding remove to remove
            }
        }
        return false;
    }

    /** Try to determine whether this event can replace the old one.
    * This is used to batch the one-letter modifications into larger
    * parts (words) and undoing/redoing them at once.
    * This method returns true whether 
    */
    public boolean replaceEdit(UndoableEdit anEdit) {
        BaseDocument doc = (BaseDocument)getDocument();
        if (anEdit instanceof BaseDocument.AtomicCompoundEdit) {
            BaseDocument.AtomicCompoundEdit compEdit
                    = (BaseDocument.AtomicCompoundEdit)anEdit;

            if (!doc.undoMergeReset && compEdit.getEdits().size() == 1) {
                UndoableEdit edit = (UndoableEdit)compEdit.getEdits().get(0);
                if (edit instanceof BaseDocumentEvent && canMerge((BaseDocumentEvent)edit)) {
                    previous = anEdit;
                    return true;
                }
            }
        } else if (anEdit instanceof BaseDocumentEvent) {
            BaseDocumentEvent evt = (BaseDocumentEvent)anEdit;

            if (!doc.undoMergeReset && canMerge(evt)) {
                previous = anEdit;
                return true;
            }
        }
        doc.undoMergeReset = false;
        return false;
    }

    public void die() {
        // Super of die()
	int size = edits.size();
	for (int i = size-1; i >= 0; i--)
	{
	    UndoableEdit e = (UndoableEdit)edits.elementAt(i);
	    e.die();
	}

        alive2 = false;
        // End super of die()
        
        if (previous != null) {
            previous.die();
            previous = null;
        }
    }

    public void end() {
        // Super of end()
	inProgress2 = false;
        // End super of end()
    }

    public DocumentEvent.ElementChange getChange(Element elem) {
        // Super of getChange()
        if (changeLookup2 != null) {
            return (DocumentEvent.ElementChange) changeLookup2.get(elem);
        }
        int n = edits.size();
        for (int i = 0; i < n; i++) {
            Object o = edits.elementAt(i);
            if (o instanceof DocumentEvent.ElementChange) {
                DocumentEvent.ElementChange c = (DocumentEvent.ElementChange) o;
                if (c.getElement() == elem) {
                    return c;
                }
            }
        }
        return null;
        // End super of getChange()
    }


    public String toString() {
        return System.identityHashCode(this) + " " + super.toString() // NOI18N
               + ", type=" + getType() // NOI18N
               + ((getType() != DocumentEvent.EventType.CHANGE)
                  ? ("text='" + getText() + "'") : ""); // NOI18N
    }

    /** Edit describing the change of the document draw-layers */
    static class DrawLayerChange extends AbstractUndoableEdit {

        String drawLayerName;

        int drawLayerVisibility;

        DrawLayerChange(String drawLayerName, int drawLayerVisibility) {
            this.drawLayerName = drawLayerName;
            this.drawLayerVisibility = drawLayerVisibility;
        }

        public String getDrawLayerName() {
            return drawLayerName;
        }

        public int getDrawLayerVisibility() {
            return drawLayerVisibility;
        }

    }
    
}
