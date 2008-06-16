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

package org.netbeans.lib.editor.util.swing;

import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.AbstractCharSequence;
import org.netbeans.lib.editor.util.CompactMap;

/**
 * Various utility methods related to swing text documents.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public final class DocumentUtilities {
    
    private static final Object TYPING_MODIFICATION_DOCUMENT_PROPERTY = new Object();
    
    private static final Object TYPING_MODIFICATION_KEY = new Object();
    
    private static Field numReadersField;
    
    private static Field currWriterField;
    
    
    private DocumentUtilities() {
        // No instances
    }

    /**
     * Add document listener to document with given priority
     * or default to using regular {@link Document#addDocumentListener(DocumentListener)}
     * if the given document is not listener priority aware.
     * 
     * @param doc document to which the listener should be added.
     * @param listener document listener to add.
     * @param priority priority with which the listener should be added.
     *  If the document does not support document listeners ordering
     *  then the listener is added in a regular way by using
     *  {@link javax.swing.text.Document#addDocumentListener(
     *  javax.swing.event.DocumentListener)} method.
     */
    public static void addDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        if (!addPriorityDocumentListener(doc, listener, priority))
            doc.addDocumentListener(listener);
    }
    
    /**
     * Suitable for document implementations - adds document listener
     * to document with given priority and does not do anything
     * if the given document is not listener priority aware.
     * <br/>
     * Using this method in the document impls and defaulting
     * to super.addDocumentListener() in case it returns false
     * will ensure that there won't be an infinite loop in case the super constructors
     * would add some listeners prior initing of the priority listening.
     * 
     * @param doc document to which the listener should be added.
     * @param listener document listener to add.
     * @param priority priority with which the listener should be added.
     * @return true if the priority listener was added or false if the document
     *  does not support priority listening.
     */
    public static boolean addPriorityDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.add(listener, priority.getPriority());
            return true;
        } else
            return false;
    }

    /**
     * Remove document listener that was previously added to the document
     * with given priority or use default {@link Document#removeDocumentListener(DocumentListener)}
     * if the given document is not listener priority aware.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     *  It should correspond to the priority with which the listener
     *  was added originally.
     */
    public static void removeDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        if (!removePriorityDocumentListener(doc, listener, priority))
            doc.removeDocumentListener(listener);
    }

    /**
     * Suitable for document implementations - removes document listener
     * from document with given priority and does not do anything
     * if the given document is not listener priority aware.
     * <br/>
     * Using this method in the document impls and defaulting
     * to super.removeDocumentListener() in case it returns false
     * will ensure that there won't be an infinite loop in case the super constructors
     * would remove some listeners prior initing of the priority listening.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     * @return true if the priority listener was removed or false if the document
     *  does not support priority listening.
     */
    public static boolean removePriorityDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.remove(listener, priority.getPriority());
            return true;
        } else
            return false;
    }

    /**
     * This method should be used by swing document implementations that
     * want to support document listeners prioritization.
     * <br>
     * It should be called from document's constructor in the following way:<pre>
     *
     * class MyDocument extends AbstractDocument {
     *
     *     MyDocument() {
     *         super.addDocumentListener(DocumentUtilities.initPriorityListening(this));
     *     }
     *
     *     public void addDocumentListener(DocumentListener listener) {
     *         if (!DocumentUtilities.addDocumentListener(this, listener, DocumentListenerPriority.DEFAULT))
     *             super.addDocumentListener(listener);
     *     }
     *
     *     public void removeDocumentListener(DocumentListener listener) {
     *         if (!DocumentUtilities.removeDocumentListener(this, listener, DocumentListenerPriority.DEFAULT))
     *             super.removeDocumentListener(listener);
     *     }
     *
     * }</pre>
     *
     *
     * @param doc document to be initialized.
     * @return the document listener instance that should be added as a document
     *   listener typically by using <code>super.addDocumentListener()</code>
     *   in document's constructor.
     * @throws IllegalStateException when the document already has
     *   the property initialized.
     */
    public static DocumentListener initPriorityListening(Document doc) {
        if (doc.getProperty(PriorityDocumentListenerList.class) != null) {
            throw new IllegalStateException(
                    "PriorityDocumentListenerList already initialized for doc=" + doc); // NOI18N
        }
        PriorityDocumentListenerList listener = new PriorityDocumentListenerList();
        doc.putProperty(PriorityDocumentListenerList.class, listener);
        return listener;
    }
    
    /**
     * Get total count of document listeners attached to a particular document
     * (useful e.g. for logging).
     * <br/>
     * If the document uses priority listening then get the count of listeners
     * at all levels. If the document is not {@link AbstractDocument} the method
     * returns zero.
     * 
     * @param doc non-null document.
     * @return total count of document listeners attached to the document.
     */
    public static int getDocumentListenerCount(Document doc) {
        PriorityDocumentListenerList pdll;
        return (pdll = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class)) != null
                ? pdll.getListenerCount()
                : ((doc instanceof AbstractDocument)
                        ? ((AbstractDocument)doc).getListeners(DocumentListener.class).length
                        : 0);
    }

    /**
     * Mark that the ongoing document modification(s) will be caused
     * by user's typing.
     * It should be used by default-key-typed-action and the actions
     * for backspace and delete keys.
     * <br/>
     * The document listeners being fired may
     * query it by using {@link #isTypingModification(Document)}.
     * This method should always be used in the following pattern:
     * <pre>
     * DocumentUtilities.setTypingModification(doc, true);
     * try {
     *     doc.insertString(offset, typedText, null);
     * } finally {
     *    DocumentUtilities.setTypingModification(doc, false);
     * }
     * </pre>
     *
     * @see #isTypingModification(Document)
     */
    public static void setTypingModification(Document doc, boolean typingModification) {
        doc.putProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY, Boolean.valueOf(typingModification));
    }
    
    /**
     * This method should be used by document listeners to check whether
     * the just performed document modification was caused by user's typing.
     * <br/>
     * Certain functionality such as code completion or code templates
     * may benefit from that information. For example the java code completion
     * should only react to the typed "." but not if the same string was e.g.
     * pasted from the clipboard.
     *
     * @see #setTypingModification(Document, boolean)
     */
    public static boolean isTypingModification(Document doc) {
        Boolean b = (Boolean)doc.getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * @deprecated
     * @see #isTypingModification(Document)
     */
    public static boolean isTypingModification(DocumentEvent evt) {
        return isTypingModification(evt.getDocument());
    }

    /**
     * Get text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @return non-null character sequence.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc) {
        CharSequence text = (CharSequence)doc.getProperty(CharSequence.class);
        if (text == null) {
            text = new DocumentCharSequence(doc);
            doc.putProperty(CharSequence.class, text);
        }
        return text;
    }
    
    /**
     * Get a portion of text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @param offset starting offset of the charsequence to obtain.
     * @param length length of the charsequence to obtain
     * @return non-null character sequence.
     * @exception BadLocationException  some portion of the given range
     *   was not a valid part of the document.  The location in the exception
     *   is the first bad position encountered.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc, int offset, int length) throws BadLocationException {
        CharSequence text = getText(doc);
        try {
            return text.subSequence(offset, offset + length);
        } catch (IndexOutOfBoundsException e) {
            int badOffset = offset;
            if (offset >= 0 && offset + length > text.length()) {
                badOffset = length;
            }
            throw new BadLocationException(e.getMessage(), badOffset);
        }
    }
    
    /**
     * Document provider should call this method to allow for document event
     * properties being stored in document events.
     *
     * @param evt document event to which the storage should be added.
     *   It must be an undoable edit allowing to add an edit.
     */
    public static void addEventPropertyStorage(DocumentEvent evt) {
        // Parameter is DocumentEvent because it's more logical
        if (!(evt instanceof UndoableEdit)) {
            throw new IllegalStateException("evt not instanceof UndoableEdit: " + evt); // NOI18N
        }
        ((UndoableEdit)evt).addEdit(new EventPropertiesElementChange());
    }
    
    /**
     * Get a property of a given document event.
     *
     * @param evt non-null document event from which the property should be retrieved.
     * @param key non-null key of the property.
     * @return value for the given property.
     */
    public static Object getEventProperty(DocumentEvent evt, Object key) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        return (change != null) ? change.getProperty(key) : null;
    }
    
    /**
     * Set a property of a given document event.
     *
     * @param evt non-null document event to which the property should be stored.
     * @param key non-null key of the property.
     * @param value for the given property.
     */
    public static void putEventProperty(DocumentEvent evt, Object key, Object value) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        if (change == null) {
            throw new IllegalStateException("addEventPropertyStorage() not called for evt=" + evt); // NOI18N
        }
        change.putProperty(key, value);
    }
    
    /**
     * Set a property of a given document event by using the given map entry.
     * <br/>
     * The present implementation is able to directly store instances
     * of <code>CompactMap.MapEntry</code>. Other map entry implementations
     * will be delegated to {@link #putEventProperty(DocumentEvent, Object, Object)}.
     *
     * @param evt non-null document event to which the property should be stored.
     * @param mapEntry non-null map entry which should be stored.
     *  Generally after this method finishes the {@link #getEventProperty(DocumentEvent, Object)}
     *  will return <code>mapEntry.getValue()</code> for <code>mapEntry.getKey()</code> key.
     */
    public static void putEventProperty(DocumentEvent evt, Map.Entry mapEntry) {
        if (mapEntry instanceof CompactMap.MapEntry) {
            EventPropertiesElementChange change = (EventPropertiesElementChange)
                    evt.getChange(EventPropertiesElement.INSTANCE);
            if (change == null) {
                throw new IllegalStateException("addEventPropertyStorage() not called for evt=" + evt); // NOI18N
            }
            change.putEntry((CompactMap.MapEntry)mapEntry);

        } else {
            putEventProperty(evt, mapEntry.getKey(), mapEntry.getValue());
        }
    }
    
    /**
     * Fix the given offset according to the performed modification.
     * 
     * @param offset >=0 offset in a document.
     * @param evt document event describing change in the document.
     * @return offset updated by applying the document change to the offset.
     */
    public static int fixOffset(int offset, DocumentEvent evt) {
        int modOffset = evt.getOffset();
        if (evt.getType() == DocumentEvent.EventType.INSERT) {
            if (offset >= modOffset) {
                offset += evt.getLength();
            }
        } else if (evt.getType() == DocumentEvent.EventType.REMOVE) {
            if (offset > modOffset) {
                offset = Math.min(offset - evt.getLength(), modOffset);
            }
        }
        return offset;
    }
    
    /**
     * Get text of the given document modification.
     * <br/>
     * It's implemented as retrieving of a <code>String.class</code>.
     *
     * @param evt document event describing either document insertion or removal
     *  (change event type events will produce null result).
     * @return text that was inserted/removed from the document by the given
     *  document modification or null if that information is not provided
     *  by that document event.
     */
    public static String getModificationText(DocumentEvent evt) {
        return (String)getEventProperty(evt, String.class);
    }
    
    /**
     * Check whether the given document is read-locked by at least one thread
     * or whether it was write-locked by the current thread (write-locking
     * grants the read-access automatically).
     * <br/>
     * The method currently only works for {@link javax.swing.text.AbstractDocument}
     * based documents and it uses reflection.
     * <br/>
     * Unfortunately the AbstractDocument only records number of read-lockers
     * but not the thread references that performed the read-locking. Thus it can't be verified
     * whether current thread has performed read locking or another thread.
     * 
     * @param doc non-null document instance.
     * @return true if the document was read-locked by some thread
     *   or false if not (or if doc not-instanceof AbstractDocument).
     * @since 1.17
     */
    public static boolean isReadLocked(Document doc) {
        if (checkAbstractDoc(doc)) {
            if (isWriteLocked(doc))
                return true;
            if (numReadersField == null) {
                try {
                    numReadersField = AbstractDocument.class.getDeclaredField("numReaders");
                } catch (NoSuchFieldException ex) {
                    throw new IllegalStateException(ex);
                }
                numReadersField.setAccessible(true);
            }
            try {
                synchronized (doc) {
                    return numReadersField.getInt(doc) > 0;
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return false;
    }
    
    /**
     * Check whether the given document is write-locked by the current thread.
     * <br/>
     * The method currently only works for {@link javax.swing.text.AbstractDocument}
     * based documents and it uses reflection.
     * 
     * @param doc non-null document instance.
     * @return true if the document was write-locked by the current thread
     *   or false if not (or if doc not-instanceof AbstractDocument).
     * @since 1.17
     */
    public static boolean isWriteLocked(Document doc) {
        if (checkAbstractDoc(doc)) {
            if (currWriterField == null) {
                try {
                    currWriterField = AbstractDocument.class.getDeclaredField("currWriter");
                } catch (NoSuchFieldException ex) {
                    throw new IllegalStateException(ex);
                }
                currWriterField.setAccessible(true);
            }
            try {
                synchronized (doc) {
                    return currWriterField.get(doc) == Thread.currentThread();
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return false; // not AbstractDocument
    }
    
    private static boolean checkAbstractDoc(Document doc) {
        if (doc == null)
            throw new IllegalArgumentException("document is null");
        return (doc instanceof AbstractDocument);
    }
    
    /**
     * Get the paragraph element for the given document.
     *
     * @param doc non-null document instance.
     * @param offset offset in the document >=0
     * @return paragraph element containing the given offset.
     */
    public static Element getParagraphElement(Document doc, int offset) {
        Element paragraph;
        if (doc instanceof StyledDocument) {
            paragraph = ((StyledDocument)doc).getParagraphElement(offset);
        } else {
            Element rootElem = doc.getDefaultRootElement();
            int index = rootElem.getElementIndex(offset);
            paragraph = rootElem.getElement(index);
            if ((offset < paragraph.getStartOffset()) || (offset >= paragraph.getEndOffset())) {
                paragraph = null;
            }
        }
        return paragraph;
    }
    
    /**
     * Get the root of the paragraph elements for the given document.
     *
     * @param doc non-null document instance.
     * @return root element of the paragraph elements.
     */
    public static Element getParagraphRootElement(Document doc) {
        if (doc instanceof StyledDocument) {
            return ((StyledDocument)doc).getParagraphElement(0).getParentElement();
        } else {
            return doc.getDefaultRootElement().getElement(0).getParentElement();
        }
    }

    /**
     * Get string representation of an offset for debugging purposes
     * in form "offset[line:column]". Both lines and columns start counting from 1
     * like in the editor's status bar. Tabs are expanded when counting the column.
     *
     * @param doc non-null document in which the offset is located.
     * @param offset offset of the document.
     * @return string representation of the offset.
     * @since 
     */
    public static String debugOffset(Document doc, int offset) {
        Element paragraphRoot = getParagraphRootElement(doc);
        int lineIndex = paragraphRoot.getElementIndex(offset);
        Element lineElem = paragraphRoot.getElement(lineIndex);
        return String.valueOf(offset) + '[' + (lineIndex+1) + ':' +
                (debugColumn(doc, lineElem.getStartOffset(), offset)+1) + ']';
    }
    
    private static int debugColumn(Document doc, int lineStartOffset, int offset) {
        Integer tabSizeInteger = (Integer) doc.getProperty(PlainDocument.tabSizeAttribute);
        int tabSize = (tabSizeInteger != null) ? tabSizeInteger : 8;
        CharSequence docText = getText(doc);
        int column = 0;
        for (int i = lineStartOffset; i < offset; i++) {
            char c = docText.charAt(i);
            if (c == '\t') {
                column = (column + tabSize) / tabSize * tabSize;
            } else {
                column++;
            }
        }
        return column;
    }

    /**
     * Implementation of the character sequence for a generic document
     * that does not provide its own implementation of character sequence.
     */
    private static final class DocumentCharSequence extends AbstractCharSequence.StringLike {
        
        private final Segment segment = new Segment();
        
        private final Document doc;
        
        DocumentCharSequence(Document doc) {
            this.doc = doc;
        }

        public int length() {
            return doc.getLength();
        }

        public synchronized char charAt(int index) {
            try {
                doc.getText(index, 1, segment);
            } catch (BadLocationException e) {
                throw new IndexOutOfBoundsException(e.getMessage()
                    + " at offset=" + e.offsetRequested()); // NOI18N
            }
            char ch = segment.array[segment.offset];
            segment.array = null; // Allow GC of large char arrays
            return ch;
        }

    }
    
    /**
     * Helper element used as a key in searching for an element change
     * being a storage of the additional properties in a document event.
     */
    private static final class EventPropertiesElement implements Element {
        
        static final EventPropertiesElement INSTANCE = new EventPropertiesElement();
        
        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return 0;
        }

        public int getElementCount() {
            return 0;
        }

        public int getElementIndex(int offset) {
            return -1;
        }

        public Element getElement(int index) {
            return null;
        }

        public boolean isLeaf() {
            return true;
        }

        public Element getParentElement() {
            return null;
        }

        public String getName() {
            return "Helper element for modification text providing"; // NOI18N
        }

        public Document getDocument() {
            return null;
        }

        public javax.swing.text.AttributeSet getAttributes() {
            return null;
        }
        
        public String toString() {
            return getName();
        }

    }
    
    private static final class EventPropertiesElementChange
    implements DocumentEvent.ElementChange, UndoableEdit  {
        
        private CompactMap eventProperties = new CompactMap();
        
        public synchronized Object getProperty(Object key) {
            return (eventProperties != null) ? eventProperties.get(key) : null;
        }

        @SuppressWarnings("unchecked")
        public synchronized Object putProperty(Object key, Object value) {
            return eventProperties.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public synchronized CompactMap.MapEntry putEntry(CompactMap.MapEntry entry) {
            return eventProperties.putEntry(entry);
        }

        public int getIndex() {
            return -1;
        }

        public Element getElement() {
            return EventPropertiesElement.INSTANCE;
        }

        public Element[] getChildrenRemoved() {
            return null;
        }

        public Element[] getChildrenAdded() {
            return null;
        }

        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean addEdit(UndoableEdit anEdit) {
            return false;
        }

        public void undo() throws CannotUndoException {
            // do nothing
        }

        public void redo() throws CannotRedoException {
            // do nothing
        }

        public boolean isSignificant() {
            return false;
        }

        public String getUndoPresentationName() {
            return "";
        }

        public String getRedoPresentationName() {
            return "";
        }

        public String getPresentationName() {
            return "";
        }

        public void die() {
            // do nothing
        }

        public boolean canUndo() {
            return true;
        }

        public boolean canRedo() {
            return true;
        }

    }
    
}
