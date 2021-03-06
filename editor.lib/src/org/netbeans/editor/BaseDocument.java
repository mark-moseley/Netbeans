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

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.EventListener;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Position;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyleConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Segment;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.modules.editor.lib.FormatterOverride;
import org.openide.util.Lookup;

/**
* Document implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseDocument extends AbstractDocument implements SettingsChangeListener, AtomicLockDocument {

    // -J-Dorg.netbeans.editor.BaseDocument.level=FINE
    private static final Logger LOG = Logger.getLogger(BaseDocument.class.getName());
    
    // -J-Dorg.netbeans.editor.BaseDocument.listener.level=FINE
    private static final Logger LOG_LISTENER = Logger.getLogger(BaseDocument.class.getName() + ".listener");
    
    /** Registry identification property */
    public static final String ID_PROP = "id"; // NOI18N

    /** Line separator property for reading files in */
    public static final String READ_LINE_SEPARATOR_PROP
    = DefaultEditorKit.EndOfLineStringProperty;

    /** Line separator property for writing content into files. If not set
     * the writing defaults to the READ_LINE_SEPARATOR_PROP.
     */
    public static final String WRITE_LINE_SEPARATOR_PROP
    = "write-line-separator"; // NOI18N

    /** File name property */
    public static final String FILE_NAME_PROP = "file-name"; // NOI18N

    /** Wrap search mark property */
    public static final String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"; // NOI18N

    /** Undo manager property. This can be used to implement undo
    * in a simple way. Default undo and redo actions try to get this
    * property and perform undo and redo through it.
    */
    public static final String UNDO_MANAGER_PROP = "undo-manager"; // NOI18N

    /** Kit class property. This can become useful for getting
    * the settings that logicaly belonging to the document.
    */
    public static final String KIT_CLASS_PROP = "kit-class"; // NOI18N

    /** String forward finder property */
    public static final String STRING_FINDER_PROP = "string-finder"; // NOI18N

    /** String backward finder property */
    public static final String STRING_BWD_FINDER_PROP = "string-bwd-finder"; // NOI18N

    /** Highlight search finder property. */
    public static final String BLOCKS_FINDER_PROP = "blocks-finder"; // NOI18N

    /** Maximum line width encountered during the initial read operation.
    * This is filled by Analyzer and used by UI to set the correct initial width
    * of the component.
    * Values: java.lang.Integer
    */
    public static final String LINE_LIMIT_PROP = "line-limit"; // NOI18N


    /** Size of the line batch. Line batch can be used at various places
    * especially when processing lines by syntax scanner.
    */
    public static final String LINE_BATCH_SIZE = "line-batch-size"; // NOI18N

    /** Line separator is marked by CR (Macintosh) */
    public static final String  LS_CR = "\r"; // NOI18N

    /** Line separator is marked by LF (Unix) */
    public static final String  LS_LF = "\n"; // NOI18N

    /** Line separator is marked by CR and LF (Windows) */
    public static final String  LS_CRLF = "\r\n"; // NOI18N
    
    /** Name of the formatter setting. */
    public static final String FORMATTER = "formatter"; // NOI18N

    /**
     * Document property holding either null or VetoableChangeListener
     * that should be notified prior insert/remove or atomicLock()
     * starts outside of a document lock.
     */
    private static final String BEFORE_MODIFICATION_LISTENER = "modificationListener"; // NOI18N

    /** Maximum of concurrent read threads (other will wait until
    * one of these will leave).
    */
    private static final int MAX_READ_THREADS = 10;

    /** Write lock without write lock */
    private static final String WRITE_LOCK_MISSING
    = "extWriteUnlock() without extWriteLock()"; // NOI18N

    private static final Object annotationsLock = new Object();
    private static final Object getVisColFromPosLock = new Object();
    private static final Object getOffsetFromVisColLock = new Object();

    /** Debug modifications performed on the document */
    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.document"); // NOI18N
    /** Debug the stack of calling of the insert/remove */
    private static final boolean debugStack
        = Boolean.getBoolean("netbeans.debug.editor.document.stack"); // NOI18N
    /** Debug the document insert/remove but do not output text inserted/removed */
    private static final boolean debugNoText
        = Boolean.getBoolean("netbeans.debug.editor.document.notext"); // NOI18N

    /** Debug the StreamDescriptionProperty during read() */
    private static final boolean debugRead
        = Boolean.getBoolean("netbeans.debug.editor.document.read"); // NOI18N
    
    public static final ThreadLocal THREAD_LOCAL_LOCK_DEPTH = new ThreadLocal();
    private static final Integer[] lockIntegers
        = new Integer[] {
            null,
            new Integer(1),
            new Integer(2),
            new Integer(3)
        };

    /** How many spaces should be displayed instead of '\t' character */
    private int tabSize = SettingsDefaults.defaultTabSize.intValue();

    /** Size of one indentation level. If this variable is null (value
     * is not set in Settings, then the default algorithm will be used.
     */
    private Integer shiftWidth;

    /** How many times current writer requested writing */
    private int writeDepth;

    /** How many times atomic writer requested writing */
    private int atomicDepth;

    /* Was the document initialized by reading? */
    protected boolean inited;

    /* Was the document modified by doing inert/remove */
    protected boolean modified;

    /** Listener to changes in find support */
    PropertyChangeListener findSupportListener;

    /** Default element - lazily inited */
    protected Element defaultRootElem;

    private SyntaxSupport syntaxSupport;

    /** Layer list for document level layers */
    private DrawLayerList drawLayerList = new DrawLayerList();

    /** Reset merging next created undoable edit to the last one. */
    boolean undoMergeReset;

    /** Kit class stored here */
    Class kitClass;

    /** Undo event for atomic events is fired after the successful
    * atomic operation is finished. The changes are stored in this variable
    * during the atomic operation. If the operation is broken, these edits
    * are used to restore previous state.
    */
    private AtomicCompoundEdit atomicEdits;

    private Acceptor identifierAcceptor;

    private Acceptor whitespaceAcceptor;

    private ArrayList syntaxList = new ArrayList();

    /** Root element of line elements representation */
    protected LineRootElement lineRootElement;

    /** Last document event to be undone. The field is filled
     * by the lastly done modification undoable edit.
     * BaseDocumentEvent.canUndo() checks this flag.
     */
    UndoableEdit lastModifyUndoEdit; // #8692 check last modify undo edit

    /** List of annotations for this document. */
    private Annotations annotations;

    /* Bug #6258 during composing I18N text using input method the Undoable Edit
     * actions must be disabled so only the final push (and not all intermediate
     * ones) of the I18N word will be stored as undoable edit.
     */
     private boolean composedText = false;
    
    /**
     * Map of [multi-mark, Mark-instance] pairs.
     * These multi-marks need to be stored separately and not be undone/redone.
     */
    final Map<MultiMark, Mark> marks = new HashMap<MultiMark, Mark>();
    final MarkVector marksStorage = new MarkVector();

    /** Finder for visual x-coord to position conversion */
    private FinderFactory.VisColPosFwdFinder visColPosFwdFinder;

    /** Finder for position to x-coord conversion */
    private FinderFactory.PosVisColFwdFinder posVisColFwdFinder;
    
    /** Atomic lock event instance shared by all the atomic lock firings done for this document */
    private AtomicLockEvent atomicLockEventInstance = new AtomicLockEvent(this);
    
    private FixLineSyntaxState fixLineSyntaxState;
    private UndoableEdit removeUpdateLineUndo;

    private Object[] atomicLockListenerList;

    /**
     * ThreadLocal variable holding status of notification about subsequent
     * modification. It must be notified prior acquiring of the document's lock.
     */
    private final ThreadLocal STATUS = new ThreadLocal ();
    
    private boolean modsUndoneOrRedone;
    
    private DocumentListener postModificationDocumentListener;
    
    private Position lastPositionEditedByTyping = null;
    
    /** Formatter being used. */
    private Formatter formatter;

    /** Create base document with a specified syntax.
    * @param kitClass class used to initialize this document with proper settings
    *   category based on the editor kit for which this document is created
    * @param syntax syntax scanner to use with this document
    */
    public BaseDocument(Class kitClass, boolean addToRegistry) {
        super(new DocumentContent());
        this.kitClass = kitClass;

        setDocumentProperties(createDocumentProperties(getDocumentProperties()));
        super.addDocumentListener(
                org.netbeans.lib.editor.util.swing.DocumentUtilities.initPriorityListening(this));

        putProperty(GapStart.class, getDocumentContent());
        putProperty(CharSequence.class, getDocumentContent().createCharSequenceView());
        putProperty("supportsModificationListener", Boolean.TRUE); // NOI18N
        
        lineRootElement = new LineRootElement(this);

        settingsChange(null); // initialize variables from settings
        Settings.addSettingsChangeListener(this);

        // Line separators default to platform ones
        putProperty(READ_LINE_SEPARATOR_PROP, Analyzer.getPlatformLS());

        // Additional initialization of the document through the kit
        BaseKit kit = BaseKit.getKit(kitClass);
        if (kit != null) {
            kit.initDocument(this);
        }

        // Possibly add the document to registry
        if (addToRegistry) {
            Registry.addDocument(this); // add if created thru the kit
        }

        // Start listen on find-support
        findSupportListener = new PropertyChangeListener() {
                                  public void propertyChange(PropertyChangeEvent evt) {
                                      findSupportChange(evt);
                                  }
                              };
        FindSupport.getFindSupport().addPropertyChangeListener(findSupportListener);
        findSupportChange(null); // update doc by find settings
    }
    
    private DocumentContent getDocumentContent() {
        return (DocumentContent)getContent();
    }
    
    public CharSeq getText() {
        return getDocumentContent();
    }

    private void findSupportChange(PropertyChangeEvent evt) {
        // set all finders to null
        putProperty(STRING_FINDER_PROP, null);
        putProperty(STRING_BWD_FINDER_PROP, null);
        putProperty(BLOCKS_FINDER_PROP, null);
    }

    /** Called when settings were changed. The method is called
    * also in constructor, so the code must count with the evt being null.
    */
    public void settingsChange(SettingsChangeEvent evt) {
        String settingName = (evt != null) ? evt.getSettingName() : null;

        if (settingName == null || SettingsNames.TAB_SIZE.equals(settingName)) {
            tabSize = SettingsUtil.getPositiveInteger(kitClass, SettingsNames.TAB_SIZE,
                          SettingsDefaults.defaultTabSize);
        }

        if (settingName == null || SettingsNames.INDENT_SHIFT_WIDTH.equals(settingName)) {
            Object shw = Settings.getValue(kitClass, SettingsNames.INDENT_SHIFT_WIDTH);
            if (shw instanceof Integer) { // currently only Integer values are supported
                shiftWidth = (Integer)shw;
            }
        }
        
        if (settingName == null || SettingsNames.READ_BUFFER_SIZE.equals(settingName)) {
            int readBufferSize = SettingsUtil.getPositiveInteger(kitClass,
                                 SettingsNames.READ_BUFFER_SIZE, SettingsDefaults.defaultReadBufferSize);
            putProperty(SettingsNames.READ_BUFFER_SIZE, new Integer(readBufferSize));
        }

        if (settingName == null || SettingsNames.WRITE_BUFFER_SIZE.equals(settingName)) {
            int writeBufferSize = SettingsUtil.getPositiveInteger(kitClass,
                                  SettingsNames.WRITE_BUFFER_SIZE, SettingsDefaults.defaultWriteBufferSize);
            putProperty(SettingsNames.WRITE_BUFFER_SIZE, new Integer(writeBufferSize));
        }

        if (settingName == null || SettingsNames.MARK_DISTANCE.equals(settingName)) {
            int markDistance = SettingsUtil.getPositiveInteger(kitClass,
                               SettingsNames.MARK_DISTANCE, SettingsDefaults.defaultMarkDistance);
            putProperty(SettingsNames.MARK_DISTANCE, new Integer(markDistance));
        }

        if (settingName == null || SettingsNames.MAX_MARK_DISTANCE.equals(settingName)) {
            int maxMarkDistance = SettingsUtil.getPositiveInteger(kitClass,
                                  SettingsNames.MAX_MARK_DISTANCE, SettingsDefaults.defaultMaxMarkDistance);
            putProperty(SettingsNames.MAX_MARK_DISTANCE, new Integer(maxMarkDistance));
        }

        if (settingName == null || SettingsNames.MIN_MARK_DISTANCE.equals(settingName)) {
            int minMarkDistance = SettingsUtil.getPositiveInteger(kitClass,
                                  SettingsNames.MIN_MARK_DISTANCE, SettingsDefaults.defaultMinMarkDistance);
            putProperty(SettingsNames.MIN_MARK_DISTANCE, new Integer(minMarkDistance));
        }

        if (settingName == null || SettingsNames.READ_MARK_DISTANCE.equals(settingName)) {
            int readMarkDistance = SettingsUtil.getPositiveInteger(kitClass,
                                   SettingsNames.READ_MARK_DISTANCE, SettingsDefaults.defaultReadMarkDistance);
            putProperty(SettingsNames.READ_MARK_DISTANCE, new Integer(readMarkDistance));
        }

        if (settingName == null || SettingsNames.SYNTAX_UPDATE_BATCH_SIZE.equals(settingName)) {
            int syntaxUpdateBatchSize = SettingsUtil.getPositiveInteger(kitClass,
                                        SettingsNames.SYNTAX_UPDATE_BATCH_SIZE, SettingsDefaults.defaultSyntaxUpdateBatchSize);
            putProperty(SettingsNames.SYNTAX_UPDATE_BATCH_SIZE, new Integer(syntaxUpdateBatchSize));
        }

        if (settingName == null || SettingsNames.LINE_BATCH_SIZE.equals(settingName)) {
            int lineBatchSize = SettingsUtil.getPositiveInteger(kitClass,
                                SettingsNames.LINE_BATCH_SIZE, SettingsDefaults.defaultLineBatchSize);
            putProperty(SettingsNames.LINE_BATCH_SIZE, new Integer(lineBatchSize));
        }

        if (settingName == null || SettingsNames.IDENTIFIER_ACCEPTOR.equals(settingName)) {
            identifierAcceptor = SettingsUtil.getAcceptor(kitClass,
                                 SettingsNames.IDENTIFIER_ACCEPTOR, AcceptorFactory.LETTER_DIGIT);
        }

        if (settingName == null || SettingsNames.WHITESPACE_ACCEPTOR.equals(settingName)) {
            whitespaceAcceptor = SettingsUtil.getAcceptor(kitClass,
                                 SettingsNames.WHITESPACE_ACCEPTOR, AcceptorFactory.WHITESPACE);
        }

        boolean stopOnEOL = SettingsUtil.getBoolean(kitClass,
                            SettingsNames.WORD_MOVE_NEWLINE_STOP, true);
        if (settingName == null || SettingsNames.NEXT_WORD_FINDER.equals(settingName)) {
            putProperty(SettingsNames.NEXT_WORD_FINDER,
                        SettingsUtil.getValue(kitClass, SettingsNames.NEXT_WORD_FINDER,
                                              new FinderFactory.NextWordFwdFinder(this, stopOnEOL, false)));
        }

        if (settingName == null || SettingsNames.PREVIOUS_WORD_FINDER.equals(settingName)) {
            putProperty(SettingsNames.PREVIOUS_WORD_FINDER,
                        SettingsUtil.getValue(kitClass, SettingsNames.PREVIOUS_WORD_FINDER,
                                              new FinderFactory.PreviousWordBwdFinder(this, stopOnEOL, false)));
        }

        // Refresh formatter
        formatter = null;
    }

    Syntax getFreeSyntax() {
        BaseKit kit = BaseKit.getKit(kitClass);
        synchronized (Settings.class) {
            int cnt = syntaxList.size();
            return (cnt > 0) ? (Syntax)syntaxList.remove(cnt - 1)
                   : kit.createSyntax(this);
        }
    }

    void releaseSyntax(Syntax syntax) {
        synchronized (Settings.class) {
            syntaxList.add(syntax);
        }
    }

    public Formatter getLegacyFormatter() {
        if (formatter == null) {
            formatter = (Formatter)Settings.getValue(getKitClass(), FORMATTER);
            if (formatter == null)
                formatter = Formatter.getFormatter(kitClass);
        }
        return formatter;
    }

    /** Get the formatter for this document. */
    public Formatter getFormatter() {
        Formatter f = getLegacyFormatter();
        FormatterOverride fp = Lookup.getDefault().lookup(FormatterOverride.class);
        return (fp != null) ? fp.getFormatter(this, f) : f;
    }

    public SyntaxSupport getSyntaxSupport() {
        if (syntaxSupport == null) {
            syntaxSupport = BaseKit.getKit(kitClass).createSyntaxSupport(this);
        }
        return syntaxSupport;
    }
    
    /** Perform any generic text processing. The advantage of this method
    * is that it allows the text to processed in line batches. The initial
    * size of the batch is given by the SettingsNames.LINE_BATCH_SIZE.
    * The TextBatchProcessor.processTextBatch() method is called for every
    * text batch. If the method returns true, it means the processing should
    * continue with the next batch of text which will have double line count
    * compared to the previous one. This guarantees there will be not too many
    * batches so the processing should be more efficient.
    * @param tbp text batch processor to be used to process the text batches
    * @param startPos starting position of the processing.
    * @param endPos ending position of the processing. This can be -1 to signal
    *   the end of document. If the endPos is lower than startPos then the batches
    *   are created in the backward direction.
    * @return the returned value from the last tpb.processTextBatch() call.
    *   The -1 will be returned for (startPos == endPos).
    */
    public int processText(TextBatchProcessor tbp, int startPos, int endPos)
    throws BadLocationException {
        if (endPos == -1) {
            endPos = getLength();
        }
        int batchLineCnt = ((Integer)getProperty(SettingsNames.LINE_BATCH_SIZE)).intValue();
        int batchStart = startPos;
        int ret = -1;
        if (startPos < endPos) { // batching in forward direction
            while (ret < 0 && batchStart < endPos) {
                int batchEnd = Math.min(Utilities.getRowStart(this, batchStart, batchLineCnt), endPos);
                if (batchEnd == -1) { // getRowStart() returned -1
                    batchEnd = endPos;
                }
                ret = tbp.processTextBatch(this, batchStart, batchEnd, (batchEnd == endPos));
                batchLineCnt *= 2; // double the scanned area
                batchStart = batchEnd;
            }
        } else {
            while (ret < 0 && batchStart > endPos) {
                int batchEnd = Math.max(Utilities.getRowStart(this, batchStart, -batchLineCnt), endPos);
                ret = tbp.processTextBatch(this, batchStart, batchEnd, (batchEnd == endPos));
                batchLineCnt *= 2; // double the scanned area
                batchStart = batchEnd;
            }
        }
        return ret;
    }


    public boolean isIdentifierPart(char ch) {
        return identifierAcceptor.accept(ch);
    }

    public boolean isWhitespace(char ch) {
        return whitespaceAcceptor.accept(ch);
    }

    /** Inserts string into document */
    public @Override void insertString(int offset, String text, AttributeSet a)
    throws BadLocationException {
        if (text == null || text.length() == 0) {
            return;
        }

        // Check offset correctness
        if (offset < 0 || offset > getLength()) {
            throw new BadLocationException("Wrong insert position " + offset, offset); // NOI18N
        }

        // possible CR-LF conversion
        text = Analyzer.convertLSToLF(text);
        
        // Check whether there is an active postModificationDocumentListener
        // and if so then start an atomic transaction.
        boolean activePostModification;
        DocumentEvent postModificationEvent = null;
        synchronized (this) {
            activePostModification = (postModificationDocumentListener != null);
            if (activePostModification) {
                atomicLock();
            }
        }
        try {

        // Perform the insert
        boolean notifyMod = notifyModifyCheckStart(offset, "insertString() vetoed"); // NOI18N
        boolean modFinished = false; // Whether modification succeeded
        extWriteLock();
        try {

            /*
            boolean checkSpaces = inited && org.netbeans.lib.editor.util.swing.DocumentUtilities.isTypingModification(this);
            if (checkSpaces) {
                Position offsPosition = createPosition(offset);
                checkTrailingSpaces(offset);
                offset = offsPosition.getOffset();
            }
             */
            
            preInsertCheck(offset, text, a);

            // Do the real insert into the content
            UndoableEdit edit = getContent().insertString(offset, text);

            /*
            if (checkSpaces) {
                lastPositionEditedByTyping = createPosition(offset);
            }
             */
            
            if (debug) {
                System.err.println("BaseDocument.insertString(): doc=" + this // NOI18N
                    + (modified ? "" : " - first modification") // NOI18N
                    + ", offset=" + Utilities.offsetToLineColumnString(this, offset) // NOI18N
                    + (debugNoText ? "" : (", text='" + text + "'")) // NOI18N
                );
            }
            if (debugStack) {
                Thread.dumpStack();
            }

            BaseDocumentEvent evt = getDocumentEvent(offset, text.length(), DocumentEvent.EventType.INSERT, a);

            preInsertUpdate(evt, a);

            if (edit != null) {
                evt.addEdit(edit);
                
                lastModifyUndoEdit = edit; // #8692 check last modify undo edit
            }

            modified = true;

            if (atomicDepth > 0) {
                if (atomicEdits == null) {
                    atomicEdits = new AtomicCompoundEdit();
                }
                atomicEdits.addEdit(evt); // will be added
            }

            insertUpdate(evt, a);

            evt.end();

            fireInsertUpdate(evt);

            boolean isComposedText = ((a != null)
                                      && (a.isDefined(StyleConstants.ComposedTextAttribute)));

            if (composedText && !isComposedText)
                composedText = false;
            if (!composedText && isComposedText)
                composedText = true;
            
            if (atomicDepth == 0 && !isComposedText) { // !!! check
                fireUndoableEditUpdate(new UndoableEditEvent(this, evt));
            }
            modFinished = true;
            postModificationEvent = evt;
        } finally {
            extWriteUnlock();
            // Notify no mod done if notified mod but mod did not succeeded
            if (notifyMod) {
                notifyModifyCheckEnd(modFinished);
            }
        }
        
        } finally { // for post modification
            if (activePostModification) {
                try {
                    if (postModificationEvent != null) { // Modification finished successfully
                        if (postModificationDocumentListener != null) {
                            postModificationDocumentListener.insertUpdate(postModificationEvent);
                        }
                    }
                } finally {
                    atomicUnlock();
                }
            }
        }
    }
    
    public void checkTrailingSpaces(int offset) {
        try {
            int lineNum = Utilities.getLineOffset(this, offset);
            int lastEditedLine = lastPositionEditedByTyping != null ? Utilities.getLineOffset(this, lastPositionEditedByTyping.getOffset()) : -1;
            if (lastEditedLine != -1 && lastEditedLine != lineNum) {
                // clear trailing spaces in the last edited line
                Element root = getDefaultRootElement();
                Element elem = root.getElement(lastEditedLine);
                int start = elem.getStartOffset();
                int end = elem.getEndOffset();
                String line = getText(start, end - start);
                
                int endIndex = line.length() - 1;
                if (endIndex >= 0 && line.charAt(endIndex) == '\n') {
                    endIndex--;
                    if (endIndex >= 0 && line.charAt(endIndex) == '\r') {
                        endIndex--;
                    }
                }

                int startIndex = endIndex;
                while (startIndex >= 0 && Character.isWhitespace(line.charAt(startIndex)) && line.charAt(startIndex) != '\n' && 
                        line.charAt(startIndex) != '\r') {
                    startIndex--;
                }
                startIndex++;
                if (startIndex >= 0 && startIndex <= endIndex) {
                    remove(start + startIndex, endIndex - startIndex + 1);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /** Removes portion of a document */
    public @Override void remove(int offset, int len) throws BadLocationException {
        if (len > 0) {
            if (offset < 0) {
                throw new BadLocationException("Wrong remove position " + offset, offset); // NOI18N
            }

            // Check whether there is an active postModificationDocumentListener
            // and if so then start an atomic transaction.
            boolean activePostModification;
            DocumentEvent postModificationEvent = null;
            synchronized (this) {
                activePostModification = (postModificationDocumentListener != null);
                if (activePostModification) {
                    atomicLock();
                }
            }
            try {
                
            boolean notifyMod = notifyModifyCheckStart(offset, "remove() vetoed"); // NOI18N
            boolean modFinished = false; // Whether modification succeeded
            extWriteLock();
            try {
                int docLen = getLength();
                if (offset < 0 || offset > docLen) {
                    throw new BadLocationException("Wrong remove position " + offset, offset); // NOI18N
                }
                if (offset + len > docLen) {
                    throw new BadLocationException("End offset of removed text " // NOI18N
                        + (offset + len) + " > getLength()=" + docLen, // NOI18N
                        offset + len
                    ); // NOI18N
                }

                preRemoveCheck(offset, len);

                BaseDocumentEvent evt = getDocumentEvent(offset, len, DocumentEvent.EventType.REMOVE, null);

                removeUpdate(evt);

                UndoableEdit edit = getContent().remove(offset, len);
                if (edit != null) {
                    evt.addEdit(edit);
                
                    lastModifyUndoEdit = edit; // #8692 check last modify undo edit
                }

                if (debug) {
                    System.err.println("BaseDocument.remove(): doc=" + this // NOI18N
                        + ", origDocLen=" + docLen // NOI18N
                        + ", offset=" + Utilities.offsetToLineColumnString(this, offset) // NOI18N
                        + ", len=" + len // NOI18N
                        + (debugNoText ? "" : (", removedText='" + ((DocumentContent.Edit)edit).getUndoRedoText() + "'")) // NOI18N
                    );
                }
                if (debugStack) {
                    Thread.dumpStack();
                }

                if (atomicDepth > 0) { // add edits as soon as possible
                    if (atomicEdits == null) {
                        atomicEdits = new AtomicCompoundEdit();
                    }
                    atomicEdits.addEdit(evt); // will be added
                }

                postRemoveUpdate(evt);

                evt.end();

                fireRemoveUpdate(evt);
                if (atomicDepth == 0 && !composedText) {
                    fireUndoableEditUpdate(new UndoableEditEvent(this, evt));
                }
                
                modFinished = true;
                postModificationEvent = evt;
            } finally {
                extWriteUnlock();
                // Notify no mod done if notified mod but mod did not succeeded
                if (notifyMod) {
                    notifyModifyCheckEnd(modFinished);
                }
            }

            } finally { // for post modification
                if (activePostModification) {
                    if (postModificationEvent != null) { // Modification finished successfully
                        if (postModificationDocumentListener != null) {
                            postModificationDocumentListener.removeUpdate(postModificationEvent);
                        }
                    }
                    atomicUnlock();
                }
            }

        }
    }

    /**
     * Check notify modify status and initialize it if necessary
     * before the actual modification is going to be done.
     * <br>
     * This method is invoked internally by this document implementation
     * and from the document event implementation as well during undo/redo.
     *
     * @param offset &gt;=0 offset at which the modification has been done.
     * @param vetoExceptionText text to be used for exception in case
     *  the modification has been vetoed.
     * @return whether the modification had to be notified or not.
     *  In case it had to be notified the {@link #notifyModifyCheckEnd(boolean)}
     *  has to be called.
     */
    boolean notifyModifyCheckStart(int offset, String vetoExceptionText) throws BadLocationException {
        NotifyModifyStatus notifyModifyStatus = (NotifyModifyStatus)STATUS.get();
        boolean notifyMod;
        if (notifyModifyStatus == null) { // not in atomic lock
            notifyModifyStatus = new NotifyModifyStatus (this);
            STATUS.set (notifyModifyStatus);
            notifyModify(notifyModifyStatus);
            notifyMod = true;
        } else {
            notifyMod = false;
        }

        if (notifyModifyStatus.isModificationVetoed()) {
            if (notifyMod) {
                STATUS.set (null);
            }
            throw new BadLocationException(vetoExceptionText, offset);
        }
        return notifyMod;
    }
    
    /**
     * Check notify modify status after the modification has been done.
     * <br>
     * This method is invoked internally by this document implementation
     * and from the document event implementation as well during undo/redo.
     * <br>
     * It should only be called if the {@link #notifyModifyCheckStart(int, String)}
     * has returned <code>true</code>.
     *
     * @param modFinished whether the actual modification of the document succeeded.
     */
    void notifyModifyCheckEnd(boolean modFinished) {
        NotifyModifyStatus notifyModifyStatus = (NotifyModifyStatus)STATUS.get();
        STATUS.set (null);
        if (!modFinished) {
            notifyUnmodify(notifyModifyStatus);
        }
    }
    
    /** This method is called automatically before the document
    * insertion occurs and can be used to revoke the insertion before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    * @param offset position where the insertion will be done
    * @param text string to be inserted
    * @param a attributes of the inserted text
    */
    protected void preInsertCheck(int offset, String text, AttributeSet a)
    throws BadLocationException {
    }

    /** This method is called automatically before the document
    * removal occurs and can be used to revoke the removal before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    * @param offset position where the insertion will be done
    * @param len length of the removal
    */
    protected void preRemoveCheck(int offset, int len)
    throws BadLocationException {
    }

    protected @Override void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        super.insertUpdate(chng, attr);

        // Store modification text as an event's property
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addEventPropertyStorage(chng);
        org.netbeans.lib.editor.util.swing.DocumentUtilities.putEventProperty(chng, String.class,
                ((BaseDocumentEvent)chng).getText());

        MarksStorageUndo marksStorageUndo = new MarksStorageUndo(chng);
        marksStorageUndo.updateMarksStorage();
        chng.addEdit(marksStorageUndo); // fix compatible marks

        UndoableEdit lineUndo = lineRootElement.insertUpdate(chng.getOffset(), chng.getLength());
        if (lineUndo != null) {
            chng.addEdit(lineUndo);
        }
        
        fixLineSyntaxState.update(false);
        chng.addEdit(fixLineSyntaxState.createAfterLineUndo());
        fixLineSyntaxState = null;

        // Useful for lexer snapshots
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addEventPropertyStorage(chng);
        BaseDocumentEvent bEvt = (BaseDocumentEvent)chng;
        org.netbeans.lib.editor.util.swing.DocumentUtilities.putEventProperty(
                chng, String.class, bEvt.getText());
        
    }
    
    protected void preInsertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        fixLineSyntaxState = new FixLineSyntaxState(chng);
        chng.addEdit(fixLineSyntaxState.createBeforeLineUndo());
    }

    protected @Override void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);

        // Store modification text as an event's property
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addEventPropertyStorage(chng);
        String removedText;
        try {
            removedText = getText(chng.getOffset(), chng.getLength());
        } catch (BadLocationException e) {
            // Ignore
            removedText = null;
        }
        org.netbeans.lib.editor.util.swing.DocumentUtilities.putEventProperty(chng, String.class,
                removedText);

        // Remember the line changes here but add them to chng during postRemoveUpdate()
        removeUpdateLineUndo = lineRootElement.removeUpdate(chng.getOffset(), chng.getLength());
        
        fixLineSyntaxState = new FixLineSyntaxState(chng);
        chng.addEdit(fixLineSyntaxState.createBeforeLineUndo());
    }

    protected @Override void postRemoveUpdate(DefaultDocumentEvent chng) {
        super.postRemoveUpdate(chng);

        MarksStorageUndo marksStorageUndo = new MarksStorageUndo(chng);
        marksStorageUndo.updateMarksStorage();
        chng.addEdit(marksStorageUndo); // fix compatible marks

        if (removeUpdateLineUndo != null) {
            chng.addEdit(removeUpdateLineUndo);
            removeUpdateLineUndo = null;
        }
        
        fixLineSyntaxState.update(false);
        chng.addEdit(fixLineSyntaxState.createAfterLineUndo());
        fixLineSyntaxState = null;

        // Useful for lexer snapshots
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addEventPropertyStorage(chng);
        BaseDocumentEvent bEvt = (BaseDocumentEvent)chng;
        org.netbeans.lib.editor.util.swing.DocumentUtilities.putEventProperty(
                chng, String.class, bEvt.getText());
    }

    public String getText(int[] block) throws BadLocationException {
        return getText(block[0], block[1] - block[0]);
    }

    /**
     * @param pos position of the first character to get.
     * @param len number of characters to obtain.
     * @return array with the requested characters.
     */
    public char[] getChars(int pos, int len) throws BadLocationException {
        char[] chars = new char[len];
        getChars(pos, chars, 0, len);
        return chars;
    }

    /**
     * @param block two-element array with starting and ending offset
     * @return array with the requested characters.
     */
    public char[] getChars(int[] block) throws BadLocationException {
        return getChars(block[0], block[1] - block[0]);
    }

    /**
     * @param pos position of the first character to get.
     * @param ret destination array
     * @param offset offset in the destination array.
     * @param len number of characters to obtain.
     * @return array with the requested characters.
     */
    public void getChars(int pos, char ret[], int offset, int len)
    throws BadLocationException {
        DocumentUtilities.copyText(this, pos, pos + len, ret, offset);
    }

    /** Find something in document using a finder.
    * @param finder finder to be used for the search
    * @param startPos position in the document where the search will start
    * @param limitPos position where the search will be end with reporting
    *   that nothing was found.
    */
    public int find(Finder finder, int startPos, int limitPos)
    throws BadLocationException {
        int docLen = getLength();
        if (limitPos == -1) {
            limitPos = docLen;
        }
        if (startPos == -1) {
            startPos = docLen;
        }

        if (finder instanceof AdjustFinder) {
            if (startPos == limitPos) { // stop immediately
                finder.reset(); // reset() should be called in all the cases
                return -1; // must stop here because wouldn't know if fwd/bwd search?
            }

            boolean forwardAdjustedSearch = (startPos < limitPos);
            startPos = ((AdjustFinder)finder).adjustStartPos(this, startPos);
            limitPos = ((AdjustFinder)finder).adjustLimitPos(this, limitPos);
            boolean voidSearch = (forwardAdjustedSearch ? (startPos >= limitPos) : (startPos <= limitPos));
            if (voidSearch) {
                finder.reset();
                return -1;
            }
        }

        finder.reset();
        if (startPos == limitPos) {
            return -1;
        }

        Segment text = new Segment();
        int gapStart = DocumentUtilities.getGapStart(this);
        if (gapStart == -1) {
            throw new IllegalStateException("Cannot get gapStart"); // NOI18N
        }

        int pos = startPos; // pos at which the search starts (continues)
        boolean fwdSearch = (startPos <= limitPos); // forward search
        if (fwdSearch) {
            while (pos >= startPos && pos < limitPos) {
                int p0; // low bound
                int p1; // upper bound
                if (pos < gapStart) { // part below gap
                    p0 = startPos;
                    p1 = Math.min(gapStart, limitPos);
                } else { // part above gap
                    p0 = Math.max(gapStart, startPos);
                    p1 = limitPos;
                }

                getText(p0, p1 - p0, text);
                pos = finder.find(p0 - text.offset, text.array,
                        text.offset, text.offset + text.count, pos, limitPos);

                if (finder.isFound()) {
                    return pos;
                }
            }

        } else { // backward search limitPos < startPos
            pos--; // start one char below the upper bound
            while (limitPos <= pos && pos <= startPos) {
                int p0; // low bound
                int p1; // upper bound
                if (pos < gapStart) { // part below gap
                    p0 = limitPos;
                    p1 = Math.min(gapStart, startPos);
                } else { // part above gap
                    p0 = Math.max(gapStart, limitPos);
                    p1 = startPos;
                }

                getText(p0, p1 - p0, text);
                pos = finder.find(p0 - text.offset, text.array,
                        text.offset, text.offset + text.count, pos, limitPos);

                if (finder.isFound()) {
                    return pos;
                }
            }
        }

        return -1; // position outside bounds => not found
    }

    /** Fire the change event to repaint the given block of text.
     * @deprecated Please use <code>JTextComponent.getUI().damageRange()</code> instead.
     */
    public void repaintBlock(int startOffset, int endOffset) {
        BaseDocumentEvent evt = getDocumentEvent(startOffset,
                endOffset - startOffset, DocumentEvent.EventType.CHANGE, null);
        fireChangedUpdate(evt);
    }

    public void print(PrintContainer container) {
        print(container, true, true,0,getLength());
    }

    /**
     * Print into given container.
     *
     * @param container printing container into which the printing will be done.
     * @param usePrintColoringMap use printing coloring settings instead
     *  of the regular ones.
     * @param lineNumberEnabled if set to false the line numbers will not be printed.
     *  If set to true the visibility of line numbers depends on the settings
     *  for the line number visibility.
     * @param startOffset start offset of text to print
     * @param endOffset end offset of text to print
     */
    public void print(PrintContainer container, boolean usePrintColoringMap, boolean lineNumberEnabled, int startOffset,
                      int endOffset) {
        readLock();
        try {
            EditorUI editorUI = BaseKit.getKit(kitClass).createPrintEditorUI(this,
                usePrintColoringMap, lineNumberEnabled);

            DrawGraphics.PrintDG printDG = new DrawGraphics.PrintDG(container);
            DrawEngine.getDrawEngine().draw(printDG, editorUI, startOffset, endOffset, 0, 0, Integer.MAX_VALUE);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            readUnlock();
        }
    }

    /**
     * Print into given container.
     *
     * @param container printing container into which the printing will be done.
     * @param usePrintColoringMap use printing coloring settings instead
     *  of the regular ones.
     * @param lineNumberEnabled if null, the visibility of line numbers is the same as it is given by settings
     *  for the line number visibility, otherwise the visibility equals the boolean value of the parameter
     * @param startOffset start offset of text to print
     * @param endOffset end offset of text to print
     */
    public void print(PrintContainer container, boolean usePrintColoringMap, Boolean lineNumberEnabled, int startOffset,
                      int endOffset) {
        readLock();
        try {
            boolean lineNumberEnabledPar = true;
            boolean forceLineNumbers = false;
            if (lineNumberEnabled != null) {
                lineNumberEnabledPar = lineNumberEnabled.booleanValue();
                forceLineNumbers = lineNumberEnabled.booleanValue();
            }
            EditorUI editorUI = BaseKit.getKit(kitClass).createPrintEditorUI(this, usePrintColoringMap, lineNumberEnabledPar);
            if (forceLineNumbers) {
                editorUI.setLineNumberVisibleSetting(true);
                editorUI.setLineNumberEnabled(true);
                editorUI.updateLineNumberWidth(0);
            }

            DrawGraphics.PrintDG printDG = new DrawGraphics.PrintDG(container);
            DrawEngine.getDrawEngine().draw(printDG, editorUI, startOffset, endOffset, 0, 0, Integer.MAX_VALUE);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            readUnlock();
        }
    }
    
    /** Create biased position in document */
    public Position createPosition(int offset, Position.Bias bias)
    throws BadLocationException {
        return getDocumentContent().createBiasPosition(offset, bias);
    }
    
    MultiMark createMark(int offset) throws BadLocationException {
        return getDocumentContent().createMark(offset);
    }
    
    MultiMark createBiasMark(int offset, Position.Bias bias) throws BadLocationException {
        return getDocumentContent().createBiasMark(offset, bias);
    }
    
    /** Return array of root elements - usually only one */
    public @Override Element[] getRootElements() {
        Element[] elems = new Element[1];
        elems[0] = getDefaultRootElement();
        return elems;
    }

    /** Return default root element */
    public Element getDefaultRootElement() {
        if (defaultRootElem == null) {
            defaultRootElem = getLineRootElement();
        }
        return defaultRootElem;
    }

    /** Runs the runnable under read lock. */
    public @Override void render(Runnable r) {
        readLock();
        assert incrementThreadLocalLockDepth();
        try {
            r.run();
        } finally {
            assert decrementThreadLocalLockDepth();
            readUnlock();
        }
    }
    
    private boolean incrementThreadLocalLockDepth() {
        Integer depthInteger = (Integer)THREAD_LOCAL_LOCK_DEPTH.get();
        if (depthInteger == null) {
            depthInteger = lockIntegers[1];
        } else {
            int newDepth = depthInteger.intValue() + 1;
            depthInteger = (newDepth < lockIntegers.length)
                    ? lockIntegers[newDepth]
                    : new Integer(newDepth);
        }
        THREAD_LOCAL_LOCK_DEPTH.set(depthInteger);
        return true;
    }
    
    private boolean decrementThreadLocalLockDepth() {
        Integer depthInteger = (Integer)THREAD_LOCAL_LOCK_DEPTH.get();
        assert (depthInteger != null);
        int newDepth = depthInteger.intValue() - 1;
        assert (newDepth >= 0);
        THREAD_LOCAL_LOCK_DEPTH.set(
            (newDepth < lockIntegers.length)
                ? lockIntegers[newDepth]
                : new Integer(newDepth)
        );
        return true;
    }

    /** Runs the runnable under write lock. This is a stronger version
    * of the runAtomicAsUser() method, because if there any locked sections
    * in the documents this methods breaks the modification locks and modifies
    * the document.
    * If there are any excpeptions thrown during the processing of the runnable,
    * all the document modifications are rolled back automatically.
    */
    public void runAtomic(Runnable r) {
        runAtomicAsUser(r);
    }

    /** Runs the runnable under write lock.
    * If there are any excpeptions thrown during the processing of the runnable,
    * all the document modifications are rolled back automatically.
    */
    public void runAtomicAsUser(Runnable r) {
        boolean completed = false;
        atomicLock();
        try {
            r.run();
            completed = true;
        } finally {
            try {
                if (!completed) {
                    breakAtomicLock();
                }
            } finally {
                atomicUnlock();
            }
        }
    }

    /** Insert contents of reader at specified position into document.
    * @param reader reader from which data will be read
    * @param pos on which position that data will be inserted
    */
    public void read(Reader reader, int pos)
    throws IOException, BadLocationException {
        extWriteLock();
        try {

            if (pos < 0 || pos > getLength()) {
                throw new BadLocationException("BaseDocument.read()", pos); // NOI18N
            }

            if (inited || modified) { // was the document already initialized?
                Analyzer.read(this, reader, pos);
            } else { // not initialized yet, we can use initialRead()
                Analyzer.initialRead(this, reader, true);
                inited = true; // initialized but not modified
            }
            if (debugRead) {
                System.err.println("BaseDocument.read(): StreamDescriptionProperty: "+getProperty(StreamDescriptionProperty));
            }
            
            // Compact storage - can also be called from Paste so only compact for first read
            Content content = getContent();
            if (content instanceof DocumentContent) {
                DocumentContent docContent = (DocumentContent)content;
                if (!docContent.isConservativeReallocation())
                    docContent.compact();
                docContent.setConservativeReallocation(true);
            }
            lastModifyUndoEdit = null; 
        } finally {
            extWriteUnlock();
        }
    }

    /** Write part of the document into specified writer.
    * @param writer writer into which data will be written.
    * @param pos from which position get the data
    * @param len how many characters write
    */
    public void write(Writer writer, int pos, int len)
    throws IOException, BadLocationException {
        readLock();
        try {

            if ((pos < 0) || ((pos + len) > getLength())) {
                throw new BadLocationException("BaseDocument.write()", pos); // NOI18N
            }
            Analyzer.write(this, writer, pos, len);
            writer.flush();
        } finally {
            readUnlock();
        }
    }

    /** Invalidate the state-infos in all the syntax-marks
     * in the whole document. The Syntax can call this method
     * if it changes its internal state in the way that affects
     * the future returned tokens. The syntax-state-info in all
     * the marks is reset and it will be lazily restored when necessary.
     */
    public void invalidateSyntaxMarks() {
        extWriteLock();
        try {
            FixLineSyntaxState.invalidateAllSyntaxStateInfos(this);
            BaseDocumentEvent evt = getDocumentEvent(0, getLength(), DocumentEvent.EventType.CHANGE, null);
            fireChangedUpdate(evt);
        } finally {
          extWriteUnlock();
        }
    }

    /** Get the number of spaces the TAB character ('\t') visually represents. 
     * This is related to <code>SettingsNames.TAB_SIZE</code> setting.
     */
    public int getTabSize() {
        return tabSize;
    }
    
    /** Get the width of one indentation level.
     * The algorithm first checks whether there's a value for the INDENT_SHIFT_WIDTH
     * setting. If so it uses it, otherwise it uses <code>formatter.getSpacesPerTab()</code>.
     * 
     * @see getTabSize()
     * @see Formatter.getSpacesPerTab()
     */
    public int getShiftWidth() {
        if (shiftWidth != null) {
            return shiftWidth.intValue();

        } else {
            return getFormatter().getSpacesPerTab();
        }
    }

    public final Class getKitClass() {
        return kitClass;
    }

    /** This method prohibits merging of the next document modification
    * with the previous one even if it would be normally possible.
    */
    public void resetUndoMerge() {
        undoMergeReset = true;
    }

    /* Defined because of the hack for undo()
     * in the BaseDocumentEvent.
     */
    protected @Override void fireChangedUpdate(DocumentEvent e) {
        super.fireChangedUpdate(e);
    }
    protected @Override void fireInsertUpdate(DocumentEvent e) {
        super.fireInsertUpdate(e);
    }
    protected @Override void fireRemoveUpdate(DocumentEvent e) {
        super.fireRemoveUpdate(e);
    }

    protected @Override void fireUndoableEditUpdate(UndoableEditEvent e) {
	// Fire to the list of listeners that was used before the atomic lock started
        // This fixes issue #47881 and appears to be somewhat more logical
        // than the default approach to fire all the current listeners
	Object[] listeners = (atomicLockListenerList != null)
            ? atomicLockListenerList
            : listenerList.getListenerList();

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == UndoableEditListener.class) {
		((UndoableEditListener)listeners[i + 1]).undoableEditHappened(e);
	    }	       
	}
    }

    /** Extended write locking of the document allowing
    * reentrant write lock acquiring.
    */
    public synchronized final void extWriteLock() {
        if (Thread.currentThread() != getCurrentWriter()) {
            super.writeLock();
            assert incrementThreadLocalLockDepth();
        } else { // inner locking block
            writeDepth++; // only increase write deepness
        }
    }

    /** Extended write unlocking.
    * @see extWriteLock()
    */
    public synchronized final void extWriteUnlock() {
        if (Thread.currentThread() != getCurrentWriter()) {
            throw new RuntimeException(WRITE_LOCK_MISSING);
        }

        if (writeDepth == 0) { // most outer locking block
            assert decrementThreadLocalLockDepth();
            super.writeUnlock();
        } else { // just inner locking block
            writeDepth--;
        }
    }
    
    public final void atomicLock() {
        synchronized (this) {
            NotifyModifyStatus notifyModifyStatus = (NotifyModifyStatus)STATUS.get();
            if (notifyModifyStatus == null) {
                // Notify that the modification will be done prior locking
                notifyModifyStatus = new NotifyModifyStatus (this);
                STATUS.set (notifyModifyStatus);
                //assert atomicDepth == 0 : "New status only on depth 0, but: " + atomicDepth; // NOI18N
            } else {
                //assert atomicDepth > 0 : "When there is a status: " + notifyModifyStatus+ " there needs to be a lot as well: " + atomicDepth; // NOI18N
            }
            
            extWriteLock();
            atomicDepth++;
            if (atomicDepth == 1) { // lock really started
                fireAtomicLock(atomicLockEventInstance);
                // Copy the listener list - will be used for firing undo
                atomicLockListenerList = listenerList.getListenerList();
            }
        }
    }

    public final void atomicUnlock() {
        boolean modsDone = false;
        boolean lastAtomic = false;
        synchronized (this) {
            extWriteUnlock();
            if (atomicDepth == 0) {
                throw new IllegalStateException("atomicUnlock() without atomicLock()"); // NOI18N
            }


            if (--atomicDepth == 0) { // lock really ended
                lastAtomic = true;
                fireAtomicUnlock(atomicLockEventInstance);

                if (atomicEdits != null && atomicEdits.size() > 0) {
                    modsDone = true;
                    // Some edits performed
                    atomicEdits.end();
                    fireUndoableEditUpdate(new UndoableEditEvent(this, atomicEdits));
                    atomicEdits = null;
                }
                
                if (modsUndoneOrRedone) { // Check whether any modifications were undone or redone
                    modsUndoneOrRedone = false;
                    modsDone = true;
                }
                atomicLockListenerList = null;
            }
        }
        
        // Notify unmodification if there were document modifications
        // inside the atomic section
        // or in case when in undo/redo because in such case 
        // no insertString() or remove() would be done (just undoing
        // physical changes in the buffer and firing document listeners)
        if (modsDone) {
            NotifyModifyStatus notifyModifyStatus = (NotifyModifyStatus)STATUS.get();
            notifyModify(notifyModifyStatus);
        }
        
        if (lastAtomic) {
            STATUS.set (null);
        }
    }
    
    void markModsUndoneOrRedone() {
        modsUndoneOrRedone = true;
    }

    /** Is the document currently atomically locked?
    * It's not synced as this method must be called only from writer thread.
    */
    public final boolean isAtomicLock() {
        return (atomicDepth > 0);
    }

    /** Break the atomic lock so that doc is no longer in atomic mode.
    * All the performed changes are rolled back automatically.
    * Even after calling this method, the atomicUnlock() must still be called.
    * This method is not synced as it must be called only from writer thread.
    */
    public final void breakAtomicLock() {
        if (atomicEdits != null && atomicEdits.size() > 0) {
            atomicEdits.end();
            atomicEdits.undo();
            atomicEdits = null;
        }
    }
    
    /**
     * Notify the beforeModificationListener that there is going to be
     * a document modification or atomic lock started.
     */
    private void notifyModify(NotifyModifyStatus notifyModifyStatus) {
        notifyModifyStatus.setModificationVetoed(false);
        VetoableChangeListener bml = notifyModifyStatus.getBeforeModificationListener();
        if (bml != null) {
            try {
                bml.vetoableChange(new PropertyChangeEvent(this, "modified", null, Boolean.TRUE)); // NOI18N
            } catch (PropertyVetoException ex) {
                // Modification is prohibited
                notifyModifyStatus.setModificationVetoed(true);
            }
        }
    }
    
    /**
     * Notify the beforeModificationListener that the modification
     * was not performed during the atomic lock and that the previously
     * supposed modification notification should be reverted.
     */
    private void notifyUnmodify(NotifyModifyStatus notifyModifyStatus) {
        VetoableChangeListener bml = notifyModifyStatus.getBeforeModificationListener();
        if (bml != null) {
            try {
                bml.vetoableChange(new PropertyChangeEvent(this, "modified", null, Boolean.FALSE)); // NOI18N
            } catch (PropertyVetoException e) {
                // ignore
            }
            notifyModifyStatus.setModificationVetoed(false);
        }
    }

    public void atomicUndo() {
        breakAtomicLock();
    }
    
    public void addAtomicLockListener(AtomicLockListener l) {
        listenerList.add(AtomicLockListener.class, l);
    }
    
    public void removeAtomicLockListener(AtomicLockListener l) {
        listenerList.remove(AtomicLockListener.class, l);
    }
    
    private void fireAtomicLock(AtomicLockEvent evt) {
        EventListener[] listeners = listenerList.getListeners(AtomicLockListener.class);
        int cnt = listeners.length;
        for (int i = 0; i < cnt; i++) {
            ((AtomicLockListener)listeners[i]).atomicLock(evt);
        }
    }
    
    private void fireAtomicUnlock(AtomicLockEvent evt) {
        EventListener[] listeners = listenerList.getListeners(AtomicLockListener.class);
        int cnt = listeners.length;
        for (int i = 0; i < cnt; i++) {
            ((AtomicLockListener)listeners[i]).atomicUnlock(evt);
        }
    }
    
    protected final int getAtomicDepth() {
        return atomicDepth;
    }

    @Override
    public void addDocumentListener(DocumentListener listener) {
        if (LOG_LISTENER.isLoggable(Level.FINE)) {
            LOG_LISTENER.fine("ADD DocumentListener to " +
                    org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentListenerCount(this) +
                    " present: " + listener + '\n'
            );
            if (LOG_LISTENER.isLoggable(Level.FINER)) {
                LOG_LISTENER.log(Level.FINER, "    StackTrace:\n", new Exception());
            }
        }
	if (!org.netbeans.lib.editor.util.swing.DocumentUtilities.addPriorityDocumentListener(
                this, listener, DocumentListenerPriority.DEFAULT))
            super.addDocumentListener(listener);
    }

    @Override
    public void removeDocumentListener(DocumentListener listener) {
        if (LOG_LISTENER.isLoggable(Level.FINE)) {
            LOG_LISTENER.fine("REMOVE DocumentListener from " +
                    org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentListenerCount(this) +
                    " present: " + listener + '\n'
            );
            if (LOG_LISTENER.isLoggable(Level.FINER)) {
                LOG_LISTENER.log(Level.FINER, "    StackTrace:\n", new Exception());
            }
        }
	if (!org.netbeans.lib.editor.util.swing.DocumentUtilities.removePriorityDocumentListener(
                this, listener, DocumentListenerPriority.DEFAULT))
            super.removeDocumentListener(listener);
    }
    
    protected BaseDocumentEvent createDocumentEvent(int pos, int length,
            DocumentEvent.EventType type) {
        return new BaseDocumentEvent(this, pos, length, type);
    }
    
    /* package */ final BaseDocumentEvent getDocumentEvent(int pos, int length, DocumentEvent.EventType type, AttributeSet attribs) {
        BaseDocumentEvent bde = createDocumentEvent(pos, length, type);
        bde.attachChangeAttribs(attribs);
        return bde;
    }
    
    /**
     * Set or clear a special document listener that gets notified
     * after the modification and that is allowed to do further
     * mutations to the document.
     * <br>
     * Additional mutations will be made in a single atomic transaction
     * with an original mutation.
     * <br>
     * This functionality may be used for example by code templates
     * to synchronize other regions of the document with the one
     * currently being modified.
     * <br>
     * If there is an active post modification document listener
     * then each document modification is encapsulated in an atomic lock
     * transaction automatically to allow further changes inside a transaction.
     */
    public void setPostModificationDocumentListener(DocumentListener listener) {
        this.postModificationDocumentListener = listener;
    }

    /** Was the document modified by either insert/remove
    * but not the initial read)?
    */
    public boolean isModified() {
        return modified;
    }

    /** 
     * Get the layer with the specified name. Using of <code>DrawLayer</code>s
     * has been deprecated.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public DrawLayer findLayer(String layerName) {
        return drawLayerList.findLayer(layerName);
    }

    /**
     * Using of <code>DrawLayer</code>s has been deprecated.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public boolean addLayer(DrawLayer layer, int visibility) {
        if (drawLayerList.add(layer, visibility)) {
            BaseDocumentEvent evt = getDocumentEvent(0, 0, DocumentEvent.EventType.CHANGE, null);
            evt.addEdit(new BaseDocumentEvent.DrawLayerChange(layer.getName(), visibility));
            fireChangedUpdate(evt);
            return true;
        } else {
            return false;
        }
    }

    final DrawLayerList getDrawLayerList() {
        return drawLayerList;
    }

    private LineRootElement getLineRootElement() {
        return lineRootElement;
    }

    public Element getParagraphElement(int pos) {
        return getLineRootElement().getElement(
                   getLineRootElement().getElementIndex(pos));
    }

    /** Returns object which represent list of annotations which are
     * attached to this document. 
     * @return object which represent attached annotations
     */
    public Annotations getAnnotations() {
        synchronized (annotationsLock) {
            if (annotations == null) {
                annotations = new Annotations(this);
            }
            return annotations;
        }
    }
    
    /**
     * @see LineRootElement#prepareSyntax()
     */
    void prepareSyntax(Segment text, Syntax syntax, int reqPos, int reqLen,
    boolean forceLastBuffer, boolean forceNotLastBuffer) throws BadLocationException {
        FixLineSyntaxState.prepareSyntax(this, text, syntax, reqPos, reqLen,
            forceLastBuffer, forceNotLastBuffer);
    }

    int getTokenSafeOffset(int offset) {
        return FixLineSyntaxState.getTokenSafeOffset(this, offset);
    }

    /** Get position on line from visual column. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param visCol visual column
    * @param startLinePos position of line start
    * @return position on line for particular x-coord
    */
    int getOffsetFromVisCol(int visCol, int startLinePos)
    throws BadLocationException {
        
        synchronized (getOffsetFromVisColLock) {
            if (startLinePos < 0 || startLinePos >= getLength()) {
                throw new BadLocationException("Invalid start line offset", startLinePos); // NOI18N
            }
            if (visCol <= 0) {
                return startLinePos;
            }
            if (visColPosFwdFinder == null) {
                visColPosFwdFinder = new FinderFactory.VisColPosFwdFinder();
            }
            visColPosFwdFinder.setVisCol(visCol);
            visColPosFwdFinder.setTabSize(getTabSize());
            int pos = find(visColPosFwdFinder, startLinePos, -1);
            return (pos != -1)
                ? pos
                : Utilities.getRowEnd(this, startLinePos);
        }
    }

    /** Get visual column from position. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param pos position for which the visual column should be returned
    *   the function itself computes the begining of the line first
    */ 
    int getVisColFromPos(int pos) throws BadLocationException {
        synchronized (getVisColFromPosLock) {
            if (pos < 0 || pos > getLength()) {
                throw new BadLocationException("Invalid offset", pos); // NOI18N
            }

            if (posVisColFwdFinder == null) {
                posVisColFwdFinder = new FinderFactory.PosVisColFwdFinder();
            }

            int startLinePos = Utilities.getRowStart(this, pos);
            posVisColFwdFinder.setTabSize(getTabSize());
            find(posVisColFwdFinder, startLinePos, pos);
            return posVisColFwdFinder.getVisCol();
        }
    }
    
    protected Dictionary createDocumentProperties(Dictionary origDocumentProperties) {
        return new LazyPropertyMap(origDocumentProperties);
    }

    public @Override String toString() {
        return super.toString() + ", kitClass=" + getKitClass() // NOI18N
            + ", docLen=" + getLength(); // NOI18N
    }
    
    /** Detailed debug info about the document */
    public String toStringDetail() {
        return toString();
    }

    /** Compound edit that write-locks the document for the whole processing
     * of its undo operation.
     */
    class AtomicCompoundEdit extends CompoundEdit {
        
        private UndoableEdit previousEdit;
        
        public @Override void undo() throws CannotUndoException {
            atomicLock();
            try {
                super.undo();
            } finally {
                atomicUnlock();
            }

            if (previousEdit != null) {
                previousEdit.undo();
            }
            
        }
        
        public @Override void redo() throws CannotRedoException {
            if (previousEdit != null) {
                previousEdit.redo();
            }
            
            atomicLock();
            try {
                super.redo();
            } finally {
                atomicUnlock();
            }
        }
        
        public @Override void die() {
            super.die();
            
            if (previousEdit != null) {
                previousEdit.die();
                previousEdit = null;
            }
        }
        
        public int size() {
            return edits.size();
        }
        
        public @Override boolean replaceEdit(UndoableEdit anEdit) {
            UndoableEdit childEdit;
            if (size() == 1 && ((childEdit = (UndoableEdit)getEdits().get(0)) instanceof BaseDocumentEvent)) {
                BaseDocumentEvent childEvt = (BaseDocumentEvent)childEdit;
                if (anEdit instanceof BaseDocument.AtomicCompoundEdit) {
                    BaseDocument.AtomicCompoundEdit compEdit
                            = (BaseDocument.AtomicCompoundEdit)anEdit;

                    if (!undoMergeReset && compEdit.getEdits().size() == 1) {
                        UndoableEdit edit = (UndoableEdit)compEdit.getEdits().get(0);
                        if (edit instanceof BaseDocumentEvent && childEvt.canMerge((BaseDocumentEvent)edit)) {
                            previousEdit = anEdit;
                            return true;
                        }
                    }
                } else if (anEdit instanceof BaseDocumentEvent) {
                    BaseDocumentEvent evt = (BaseDocumentEvent)anEdit;

                    if (!undoMergeReset && childEvt.canMerge(evt)) {
                        previousEdit = anEdit;
                        return true;
                    }
                }
            }
            undoMergeReset = false;
            return false;
        }
        
        java.util.Vector getEdits() {
            return edits;
        }

    }
    
    /** Property evaluator is useful for lazy evaluation
     * of properties of the document when
     * {@link javax.swing.text.Document#getProperty(java.lang.String)}
     * is called.
     */
    public interface PropertyEvaluator {
        
        /** Get the real value of the property */
        public Object getValue();
        
    }
    
    protected static class LazyPropertyMap extends Hashtable {
        
        protected LazyPropertyMap(Dictionary dict) {
            super(5);
            
            Enumeration en = dict.keys();
            while (en.hasMoreElements()) {
                Object key = en.nextElement();
                put(key, dict.get(key));
            }
        }
        
        public @Override Object get(Object key) {
            Object val = super.get(key);
            if (val instanceof PropertyEvaluator) {
                val = ((PropertyEvaluator)val).getValue();
            }
            
            return val;
        }
        
    }

    private static final class MarksStorageUndo extends AbstractUndoableEdit {
        
        private DocumentEvent evt;
        
        MarksStorageUndo(DocumentEvent evt) {
            this.evt = evt;
        }
        
        private int getLength() {
            int length = evt.getLength();
            if (evt.getType() == DocumentEvent.EventType.REMOVE) {
                length = -length;
            }
            return length;
        }
        
        void updateMarksStorage() {
            BaseDocument doc = (BaseDocument)evt.getDocument();
            // Update document's compatible marks storage - no undo
            doc.marksStorage.update(evt.getOffset(), getLength(), null);
        }
        
        public @Override void undo() throws CannotUndoException {
            BaseDocument doc = (BaseDocument)evt.getDocument();
            // Update document's compatible marks storage - no undo
            doc.marksStorage.update(evt.getOffset(), -getLength(), null);
            super.undo();
        }
        
        public @Override void redo() throws CannotRedoException {
            updateMarksStorage();
            super.redo();
        }
        

    }

    private static final class NotifyModifyStatus {
        /**
         * Listener instance that was modified prior obtaining
         * of the document lock.
         * If there will be no modification performed
         * the notifyUnmodify() needs to be called to the same listener
         * instance.
         */
        private final VetoableChangeListener beforeModificationListener;
        
        /**
         * When true then the modification ended by veto exception
         * so all the future modifications should be prohibited
         * by ending with a BadLocationException.
         * <br>
         * The atomicLock() itself cannot throw the BadLocationException
         * so the unallowed modification can't be notified earlier.
         */
        private boolean modificationVetoed;

        NotifyModifyStatus(BaseDocument document) {
            beforeModificationListener = (VetoableChangeListener)document.getProperty(BEFORE_MODIFICATION_LISTENER);
        }
        
        public boolean isModificationVetoed() {
            return modificationVetoed;
        }

        public void setModificationVetoed(boolean modificationVetoed) {
            this.modificationVetoed = modificationVetoed;
        }
        
        public VetoableChangeListener getBeforeModificationListener() {
            return beforeModificationListener;
        }
    }
    
}
