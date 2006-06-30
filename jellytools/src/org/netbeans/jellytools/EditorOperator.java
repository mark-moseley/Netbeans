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
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.CloneableEditor;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.Set;
import org.openide.windows.TopComponent;

/**
 * Handle an editor top component in NetBeans IDE. It enables to get, select, insert or
 * delete text, move caret, work with annotations and with toolbar buttons.
 * Majority of operations is done by JEditorPane API calls. If you want
 * to do operations by key navigation, use methods of JEditorPaneOperator
 * instance by {@link #txtEditorPane()}. For example, call
 * <code>txtEditorPane().changeCaretPosition(int)</code> instead of
 * <code>{@link #setCaretPosition(int)}</code>.
 * <p>
 * Usage:<br>
 * <pre>
        EditorOperator eo = new EditorOperator(filename);
        eo.setCaretPositionToLine(10);
        eo.insert("// My new comment\n");
        eo.select("// My new comment");
        eo.deleteLine(10);
        eo.getToolbarButton("Toggle Bookmark").push();
        // discard changes and close
        eo.close(false);
        // save changes and close
        eo.close(true);
        // try to close all opened documents (confirmation dialog may appear)
        eo.closeAllDocuments();
        // close all opened documents and discard all changes
        eo.closeDiscardAll();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class EditorOperator extends TopComponentOperator {

    private static int WAIT_TIME = 60000;
    
    static {
	Timeouts.initDefault("EditorOperator.WaitModifiedTimeout", WAIT_TIME);
    }
    
    /** Components operators. */
    private JEditorPaneOperator _txtEditorPane;
    private JLabelOperator _lblRowColumn;
    private JLabelOperator _lblInputMode;
    private JLabelOperator _lblStatusBar;
    
    /** Waits for the first opened editor with given name. 
     * If not active, it is activated.
     * @param filename name of file showed in the editor (it used to be label of tab)
     */
    public EditorOperator(String filename) {
        this(filename, 0);
    }
    
    /** Waits for index-th opened editor with given name.
     * If not active, it is activated.
     * @param filename name of file showed in the editor (it used to be label of tab)
     * @param index index of editor to be find
     */    
    public EditorOperator(String filename, int index) {
        super(waitTopComponent(null, filename, index, new EditorSubchooser()));
        this.requestFocus(); // needed for pushKey() methods
    }

    /** Waits for first open editor with given name in specified container.
     * If not active, it is activated.
     * @param contOper container where to search
     * @param filename name of file showed in the editor (it used to be label of tab)
     */    
    public EditorOperator(ContainerOperator contOper, String filename) {
        this(contOper, filename, 0);
    }
    
    /** Creates new operator instance for given component.
     * It is used in FormDesignerOperator.
     * @param editorComponent instance of editor
     */
    public EditorOperator(JComponent editorComponent) {
        super(editorComponent);
    }

    /** Waits for index-th opened editor with given name in specified container.
     * If not active, it is activated.
     * @param contOper container where to search
     * @param filename name of file showed in the editor (it used to be label of tab)
     * @param index index of editor to be find
     */    
    public EditorOperator(ContainerOperator contOper, String filename, int index) {
        super(waitTopComponent(contOper, filename, index, new EditorSubchooser()));
        copyEnvironment(contOper);
        this.requestFocus(); // needed for pushKey() methods
    }

    /** Closes all opened documents and discards all changes by IDE API calls.
     * It works also if no file is modified, so it is a safe way how to close
     * documents and no block further execution.
     */
    public static void closeDiscardAll() {
        // run in dispatch thread
        ModeImpl mode = (ModeImpl)new QueueTool().invokeSmoothly(new QueueTool.QueueAction("findMode") {    // NOI18N
            public Object launch() {
                return WindowManagerImpl.getInstance().findMode("editor"); //NOI18N
            }
        });
        Iterator iter = mode.getOpenedTopComponents().iterator();
        while(iter.hasNext()) {
            EditorOperator.close(iter.next(), false);
        }
    }

    /** Closes this editor by IDE API call and depending on given flag 
     * it saves or discards changes.
     * @param save true - save changes, false - discard changes
     */
    public void close(boolean save) {
        if(save) {
            super.save();
            close();
        } else {
            closeDiscard();
        }
    }
    
    /** Closes top component. It saves it or not depending on given flag. 
     * Other top components like VCS outputs are closed directly.
     * It is package private because it is also used by EditorWindowOperator. 
     */
    static void close(final Object tc, boolean save) {
        // firstly test whether it is still opened (run in dispatch thread)
        Boolean isOpened = (Boolean)new QueueTool().invokeSmoothly(
                    new QueueTool.QueueAction("isOpened") { // NOI18N
                        public Object launch() {
                            return new Boolean(((TopComponent)tc).isOpened());
                        }
                    }
        );
        if(isOpened.booleanValue()) {
            // it is still opened => try to close (otherwise do nothing)
            TopComponentOperator tco = new TopComponentOperator((TopComponent)tc);
            if(save) {
                tco.save();
                tco.close();
            } else {
                tco.closeDiscard();
            }
        }
    }

    /** Returns operator of currently shown editor pane.
     * @return  JTabbedPaneOperator instance of editor pane
     */
    public JEditorPaneOperator txtEditorPane() {
        if(_txtEditorPane == null) {
            _txtEditorPane = new JEditorPaneOperator(this);
        }
        return _txtEditorPane;
    }
    
    /** Returns operator of label showing current row and column at the left
     * corner of the Source Editor window.
     * @return JLabelOperator instance of row:column label
     */
    public JLabelOperator lblRowColumn() {
        if(_lblRowColumn == null) {
            _lblRowColumn = new JLabelOperator(this, 0);
        }
        return _lblRowColumn;
    }
    
    /** Returns operator of label showing current input mode (INS/OVR -
     * insert/overwrite).
     * @return JLabelOperator instance of input mode label
     */
    public JLabelOperator lblInputMode() {
        if(_lblInputMode == null) {
            _lblInputMode = new JLabelOperator(this, 1);
        }
        return _lblInputMode;
    }
    
    /** Returns operator of status bar at the bottom of the Source Editor.
     * @return JLabelOperator instance of status bar
     */
    public JLabelOperator lblStatusBar() {
        if(_lblStatusBar == null) {
            _lblStatusBar = new JLabelOperator(this, 2);
        }
        return _lblStatusBar;
    }
    
    /************** Get, select, delete, insert text ************************/
    
    /** Gets text from the currently opened Editor window.
     * @return a string representing whole content of the Editor window
     * (including new line characters)
     */
    public String getText() {
        return txtEditorPane().getText();
    }
    
    /** Gets text from specified line.
     * It might fail on the last line of a file because of issues
     * http://www.netbeans.org/issues/show_bug.cgi?id=24434 and
     * http://www.netbeans.org/issues/show_bug.cgi?id=24433.
     * @param lineNumber number of line (beggining from 1)
     * @return a string representing content of the line including new line
     * character
     */
    public String getText(int lineNumber) {
        return ((Line)getLine(lineNumber)).getText();
    }
    
    /** Returns instance of org.openide.text.Line for given line number.
     * @param lineNumber number of line (beggining at 1)
     * @return org.openide.text.Line instance
     */
    private Object getLine(int lineNumber) {
        Document doc = txtEditorPane().getDocument();
        DataObject od = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);
        Set set = ((LineCookie)od.getCookie(LineCookie.class)).getLineSet();
        try {
            return set.getCurrent(lineNumber-1);
        } catch (IndexOutOfBoundsException e) {
            throw new JemmyException("Index must be > 0", e);
        }
    }
    
    /** Checks if editor window contains text specified as parameter text.
     * @param text text to compare to
     * @return true if text was found, false otherwise
     */
    public boolean contains(String text) {
        return getText().indexOf(text) != -1;
    }
    
    /** Selects whole line specified by its number. Caret will stand at the
     * next available line.
     * @param lineNumber number of line (beggining from 1)
     */
    public void select(int lineNumber) {
        int lineOffset = getLineOffset(lineNumber);
        setCaretPosition(lineOffset);
        txtEditorPane().moveCaretPosition(lineOffset+getText(lineNumber).length());
    }
    
    /** Selects text between line1 and line2 (both are included). Caret will
     * stand behing the selection (at the next line if available).
     * @param line1 number of line where to begin (beggining from 1)
     * @param line2 number of line where to finish (beggining from 1)
     */
    public void select(int line1, int line2) {
        setCaretPosition(getLineOffset(line1));
        txtEditorPane().moveCaretPosition(getLineOffset(line2)+getText(line2).length());
    }
    
    /** Selects text in specified line on position defined by column1
     * and column2 (both are included). Caret will stand at the end of
     * the selection.
     * @param lineNumber number of line (beggining from 1)
     * @param column1 column position where selection starts (beggining from 1)
     * @param column2 column position where selection ends (beggining from 1) */
    public void select(int lineNumber, int column1, int column2) {
        int lineOffset = getLineOffset(lineNumber);
        setCaretPosition(lineOffset+column1-1);
        txtEditorPane().moveCaretPosition(lineOffset+column2);
    }
    
    /** Selects index-th occurence of given text.
     * @param text text to be selected
     * @param index index of text occurence (first occurence has index 0)
     * @see #select(String)
     */
    public void select(String text, int index) {
        int position = txtEditorPane().getPositionByText(text, index);
        if(position == -1) {
            throw new JemmyException(index+"-th occurence of \""+text+"\" not found.");
        }
        setCaretPosition(position);
        txtEditorPane().moveCaretPosition(position+text.length());
    }
    
    /** Selects first occurence of given text.
     * @param text text to be selected
     * @see #select(String, int)
     */
    public void select(String text) {
        select(text, 0);
    }
    
    /** Replaces first occurence of oldText by newText.
     * @param oldText text to be replaced
     * @param newText text to write instead
     */
    public void replace(String oldText, String newText) {
        replace(oldText, newText, 0);
    }
    
    /** Replaced index-th occurence of oldText by newText.
     * @param oldText text to be replaced
     * @param newText text to write instead
     * @param index index of oldText occurence (first occurence has index 0)
     */
    public void replace(String oldText, String newText, int index) {
        select(oldText, index);
        txtEditorPane().replaceSelection(newText);
    }
    
    /** Inserts text to current position. Caret will stand at the end
     * of newly inserted text.
     * @param text a string to be inserted
     */
    public void insert(final String text) {
        final int offset = txtEditorPane().getCaretPosition();
        runMapping(new MapVoidAction("insertString") {
            public void map() {
                try {
                    txtEditorPane().getDocument().insertString(offset, text, null);
                } catch (BadLocationException e) {
                    throw new JemmyException("Cannot insert \""+text+"\" to position "+offset+".", e);
                }
            }
        });
    }
    
    /** Inserts text to position specified by line number and column.
     * Caret will stand at the end of newly inserted text.
     * @param text a string to be inserted
     * @param lineNumber number of line (beggining from 1)
     * @param column column position (beggining from 1)
     */
    public void insert(String text, int lineNumber, int column) {
        setCaretPosition(lineNumber, column);
        insert(text);
    }
    
    /** Deletes given number of characters from specified possition.
     * Position of caret will not change.
     * @param offset position inside document (0 means the beginning)
     * @param length number of characters to be deleted
     */
    public void delete(final int offset, final int length) {
        // run in dispatch thread
        runMapping(new MapVoidAction("remove") {    // NOI18N
            public void map() {
                try {
                    txtEditorPane().getDocument().remove(offset, length);
                } catch (BadLocationException e) {
                    throw new JemmyException("Cannot delete "+length+
                                             " characters from position "+
                                             offset+".", e);
                }
            }
        });
    }
    
    /** Deletes given number of characters from current caret possition.
     * Position of caret will not change.
     * @param length number of characters to be deleted
     */
    public void delete(int length) {
        delete(txtEditorPane().getCaretPosition(), length);
    }
    
    /** Delete specified line.
     * Position of caret will not change.
     * @param line number of line (beggining from 1)
     */
    public void deleteLine(int line) {
        delete(getLineOffset(line), getText(line).length());
    }
    
    /** Deletes characters between column1 and column2 (both are included)
     * on the specified line.
     * @param lineNumber number of line (beggining from 1)
     * @param column1 column position where to start deleting (beggining from 1)
     * @param column2 column position where to stop deleting (beggining from 1) */
    public void delete(int lineNumber, int column1, int column2) {
        delete(getLineOffset(lineNumber)+column1-1, column2-column1+1);
    }
    
    /********************** Caret manipulation ************************/
    
    /** Returns current line number.
     * @return number of line where the caret stays (first line == 1)
     */
    public int getLineNumber() {
        StyledDocument doc = (StyledDocument)txtEditorPane().getDocument();
        int offset = txtEditorPane().getCaretPosition();
        return NbDocument.findLineNumber(doc, offset) + 1;
    }
    
    /**
     * Pushes key of requested key code.
     * @param keyCode key code
     */
    public void pushKey(int keyCode) {
        // need to request focus before any key push
        this.requestFocus();
        txtEditorPane().pushKey(keyCode);
    }

    /** Pushes Home key (KeyEvent.VK_HOME) */
    public void pushHomeKey() {
        pushKey(KeyEvent.VK_HOME);
    }
    
    /** Pushes End key (KeyEvent.VK_END) */
    public void pushEndKey() {
        pushKey(KeyEvent.VK_END);
    }
    
    /** Pushes Tab key (KeyEvent.VK_TAB) */
    public void pushTabKey() {
        pushKey(KeyEvent.VK_TAB);
    }
    
    /** Pushes Down key (KeyEvent.VK_DOWN) */
    public void pushDownArrowKey() {
        pushKey(KeyEvent.VK_DOWN);
    }
    
    /** Pushes Up key (KeyEvent.VK_UP) */
    public void pushUpArrowKey() {
        pushKey(KeyEvent.VK_UP);
    }
    
    /** Returns offset of the beginning of a line.
     * @param lineNumber number of line (starts at 1)
     * @return offset offset of line from the beginning of a file
     */
    private int getLineOffset(int lineNumber) {
        try {
            StyledDocument doc = (StyledDocument)txtEditorPane().getDocument();
            return NbDocument.findLineOffset(doc, lineNumber-1);
        } catch (IndexOutOfBoundsException e) {
            throw new JemmyException("Invalid line number "+lineNumber, e);
        }
    }
    
    /** Sets caret position relatively to current position.
     * @param relativeMove count of charaters to move caret
     */
    public void setCaretPositionRelative(int relativeMove) {
        setCaretPosition(txtEditorPane().getCaretPosition()+relativeMove);
    }
    
    /** Sets caret position to the beginning of specified line.
     * Lines are numbered from 1, so setCaretPosition(1) will set caret
     * to the beginning of the first line.
     * @param lineNumber number of line (beggining from 1)
     */
    public void setCaretPositionToLine(int lineNumber) {
        txtEditorPane().setCaretPosition(getLineOffset(lineNumber));
    }
    
    /** Sets caret position to the end of specified line.
     * Lines are numbered from 1, so setCaretPosition(1) will set caret
     * to the end of the first line.
     * @param lineNumber number of line (beggining from 1)
     */
    public void setCaretPositionToEndOfLine(int lineNumber) {
        // getText returns contents of line plus \n, that's why we use length()-1
        txtEditorPane().setCaretPosition(getLineOffset(lineNumber)+
                                         getText(lineNumber).length()-1);
    }

    /** Sets caret position to specified line and column
     * @param lineNumber line number where to set caret
     * @param column column where to set caret (1 means beginning of the row)
     */
    public void setCaretPosition(int lineNumber, int column) {
        setCaretPosition(getLineOffset(lineNumber)+column-1);
    }
    
    /** Sets caret to desired position.
     * @param position a position to set caret to (number of characters from
     * the beggining of the file - 0 means beginning of the file).
     */
    public void setCaretPosition(int position) {
        if(position < 0 || position > getText().length()) {
            throw new JemmyException("Invalid caret position "+position);
        }
        txtEditorPane().setCaretPosition(position);
    }
    
    /** Sets caret position before or after index-th occurence of given string.
     * @param text text to be searched
     * @param index index of text occurence (first occurence has index 0)
     * @param before if true put caret before text, otherwise after.
     */
    public void setCaretPosition(String text, int index, boolean before) {
        setCaretPosition(txtEditorPane().getPositionByText(text, index)
                         +(before ? 0:text.length()));
    }
    
    /** Sets caret position before or after first occurence of given string.
     * @param text text to be searched
     * @param before if true put caret before text, otherwise after.
     */
    public void setCaretPosition(String text, boolean before) {
        setCaretPosition(text, 0, before);
    }
    
    /**************************** Annotations ******************************/
    /************** thanks to Jan Lahoda for valuable input  ***************/
    
    /** Gets an array of annotations attached to given line.
     * @param lineNumber number of line (beggining from 1)
     * @return an array of org.openide.text.Annotation instances
     * @see #getAnnotationShortDescription
     * @see #getAnnotationType
     */
    public Object[] getAnnotations(int lineNumber) {
        ArrayList result = new ArrayList();
        try {
            Class annotationsClass = Class.forName("org.netbeans.editor.Annotations");
            Method getLineAnnotationsMethod = annotationsClass.getDeclaredMethod("getLineAnnotations", new Class[] {int.class});
            getLineAnnotationsMethod.setAccessible(true);
            Object lineAnnotations = getLineAnnotationsMethod.invoke(getAnnotationsInstance(), new Object[] {new Integer(lineNumber-1)});
            if(lineAnnotations != null) {
                result = getAnnotations(lineAnnotations);
            }
        } catch (Exception e) {
            throw new JemmyException("getAnnotations failed.", e);
        }
        return result.toArray(new Annotation[result.size()]);
    }
    
    /**Gets all annotations for current editor (Document).
     * @return array of org.openide.text.Annotation containing all annotations
     *         attached to this editor.
     * @see #getAnnotationShortDescription
     * @see #getAnnotationType
     */
    public Object[] getAnnotations() {
        ArrayList result = new ArrayList();
        try {
            Class annotationsClass = Class.forName("org.netbeans.editor.Annotations");
            Field lineAnnotationsArrayField = annotationsClass.getDeclaredField("lineAnnotationsArray");
            lineAnnotationsArrayField.setAccessible(true);
            ArrayList lineAnnotationsArray = (ArrayList)lineAnnotationsArrayField.get(getAnnotationsInstance());
            // loop through all lines
            for(int i=0;i<lineAnnotationsArray.size();i++) {
                result.addAll(getAnnotations(lineAnnotationsArray.get(i)));
            }
        } catch (Exception e) {
            throw new JemmyException("getAnnotations failed.", e);
        }
        return result.toArray(new Annotation[result.size()]);
    }
    
    /** Returns instance of org.netbeans.editor.Annotations object for this
     * document. */
    private Object getAnnotationsInstance() throws Exception {
        Class baseDocumentClass = Class.forName("org.netbeans.editor.BaseDocument");
        Method getAnnotationsMethod = baseDocumentClass.getDeclaredMethod("getAnnotations", null);
        getAnnotationsMethod.setAccessible(true);
        return getAnnotationsMethod.invoke(txtEditorPane().getDocument(), null);
    }
    
    /** Returns ArrayList of org.openide.text.Annotation from given LineAnnotations
     * object. */
    private ArrayList getAnnotations(Object lineAnnotations) throws Exception {
        Class lineAnnotationsClass = Class.forName("org.netbeans.editor.Annotations$LineAnnotations");
        Class annotationDescDelegateClass = Class.forName("org.netbeans.modules.editor.NbEditorDocument$AnnotationDescDelegate");
        Field delegateField = annotationDescDelegateClass.getDeclaredField("delegate");
        delegateField.setAccessible(true);

        Method getAnnotationsMethod = lineAnnotationsClass.getDeclaredMethod("getAnnotations", null);
        getAnnotationsMethod.setAccessible(true);
        Iterator annotations = (Iterator)getAnnotationsMethod.invoke(lineAnnotations, null);
        ArrayList result = new ArrayList();
        for (Iterator it = annotations; it.hasNext();) {
            result.add(delegateField.get(it.next()));
        }
        return result;
    }
    
    /** Returns a string uniquely identifying annotation. For editor bookmark
     * it is for example
     * org.netbeans.modules.editor.NbEditorKit.BOOKMARK_ANNOTATION_TYPE.
     * @param annotation instance of org.openide.text.Annotation
     * @return a string uniquely identifying annotation
     * @see #getAnnotations()
     *@see #getAnnotations(int)
     */
    public static String getAnnotationType(Object annotation) {
        return ((Annotation)annotation).getAnnotationType();
    }
    
    /** Returns a short description of annotation. It is localized.
     * @param annotation instance of org.openide.text.Annotation
     * @return a short description of annotation according to current locale
     */
    public static String getAnnotationShortDescription(Object annotation) {
        return ((Annotation)annotation).getShortDescription();
    }
    
    /***************** Methods for toolbar manipulation *******************/
    
    /** Return JButtonOperator representing a toolbar button found by given
     * tooltip within the Source Editor.
     * @param buttonTooltip tooltip of toolbar button
     * @return JButtonOperator instance of found toolbar button
     */
    public JButtonOperator getToolbarButton(String buttonTooltip) {
        ToolbarButtonChooser chooser = new ToolbarButtonChooser(buttonTooltip, getComparator());
        return new JButtonOperator(JButtonOperator.waitJButton(
        (Container)this.getSource(), chooser));
    }
    
    /** Return JButtonOperator representing index-th toolbar button within
     * the Source Editor.
     * @param index index of toolbar button to find
     * @return JButtonOperator instance of found toolbar button
     */
    public JButtonOperator getToolbarButton(int index) {
        // finds JToolbar
        ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp instanceof JToolBar;
            }
            public String getDescription() {
                return "javax.swing.JToolBar";
            }
        };
        Container toolbar = (Container)findComponent((Container)getSource(), chooser);
        if(toolbar == null) {
            throw new JemmyException("Toolbar not present.");
        }
        // if "quick browse" combo box is present, skip first button (MetalComboBoxButton usualy)
        Component combo = JComboBoxOperator.findJComboBox(toolbar, 
                                ComponentSearcher.getTrueChooser("JComboBox"));
        if(combo != null) {
            index++;
        }
        return new JButtonOperator(JButtonOperator.waitJButton((Container)toolbar, 
                                   ComponentSearcher.getTrueChooser("JButton"), index));
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * in this case a toolbar button.
     */
    private static class ToolbarButtonChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public ToolbarButtonChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        public String getDescription() {
            return "Toolbar button with tooltip \""+buttonTooltip+"\".";
        }
    }
    
    /********************************** Code folding **************************/
    /*************************** Thanks to Martin Roskanin ********************/

    /** Waits for code folding initialization. */
    public void waitFolding(){
        JTextComponent textComponent = (JTextComponent)txtEditorPane().getSource();
        final AbstractDocument adoc = (AbstractDocument)txtEditorPane().getDocument();
        // Dump fold hierarchy
        final FoldHierarchy hierarchy = FoldHierarchy.get(textComponent);
        getOutput().printTrace("Wait folding is initialized.");
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                adoc.readLock();
                try {
                    hierarchy.lock();
                    try {
                        return hierarchy.getRootFold().getFoldCount() > 0;
                    } finally {
                        hierarchy.unlock();
                    }
                } finally {
                    adoc.readUnlock();
                }
            }
            public String getDescription() {
                return("Folding initialized"); // NOI18N
            }
        });
    }
    
    /** Waits for fold at cursor position is collapsed. */
    public void waitCollapsed() {
        getOutput().printTrace("Wait fold is collapsed at line "+getLineNumber());
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return isCollapsed();
            }
            public String getDescription() {
                return("Fold collapsed");
            }
        });
    }
    
    /** Waits for fold at cursor position is expanded. */
    public void waitExpanded() {
        getOutput().printTrace("Wait fold is expanded at line "+getLineNumber());
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return !isCollapsed();
            }
            public String getDescription() {
                return("Fold expanded");
            }
        });
    }
    
    /** Collapses fold at cursor position using CTRL+'-'. It waits until fold 
     * is not collapsed.
     */
    public void collapseFold(){
	getOutput().printTrace("Collapse fold at line "+getLineNumber());
        requestFocus();
        txtEditorPane().pushKey(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK);
        waitCollapsed();
    }

    /** Collapses fold at specified line using CTRL+'-'. It waits until fold 
     * is not collapsed.
     * @param lineNumber number of line (starts at 1)
     */
    public void collapseFold(int lineNumber){
        setCaretPositionToLine(lineNumber);
        collapseFold();
    }

    /** Expands fold at specified line using CTRL+'+'. It waits until fold 
     * is not expanded.
     */
    public void expandFold(){
	getOutput().printTrace("Expand fold at line "+getLineNumber());
        requestFocus();
        txtEditorPane().pushKey(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK);
        waitExpanded();
    }

    /** Expands fold at specified line using CTRL+'+'. It waits until fold 
     * is not expanded.
     * @param lineNumber number of line (starts at 1)
     */
    public void expandFold(int lineNumber){
        setCaretPositionToLine(lineNumber);
        expandFold();
    }
    
    /** Returns true if fold at cursor position is collapsed, false if it is 
     * expanded.
     * @return true if fold is collapsed, false if it is  expanded.
     */
    public boolean isCollapsed() {
        return isCollapsed(getLineNumber());
    }
    
    /** Returns true if fold at specified line is collapsed, false if it is 
     * expanded.
     * @param lineNumber number of line (starts at 1)
     * @return true if fold is collapsed, false if it is expanded.
     */
    public boolean isCollapsed(int lineNumber) {
        JTextComponent textComponent = (JTextComponent)txtEditorPane().getSource();
        FoldHierarchy hierarchy = FoldHierarchy.get(textComponent);
        int dot = getLineOffset(lineNumber);
        hierarchy.lock();
        try{
            try{
                int rowStart = javax.swing.text.Utilities.getRowStart(textComponent, dot);
                int rowEnd = javax.swing.text.Utilities.getRowEnd(textComponent, dot);
                Fold fold = getLineFold(hierarchy, dot, rowStart, rowEnd);
                if (fold != null){
                    return fold.isCollapsed();
                } else {
                    throw new JemmyException("No fold found at position "+dot+".");
                }
            } catch(BadLocationException ble){
                throw new JemmyException("BadLocationException when seraching for fold.", ble);
            }
        }finally {
            hierarchy.unlock();
        }
    }
    
    /** Returns the fold that should be collapsed/expanded in the caret row
     *  @param hierarchy hierarchy under which all folds should be collapsed/expanded.
     *  @param dot caret position offset
     *  @param lineStart offset of the start of line
     *  @param lineEnd offset of the end of line
     *  @return the fold that meet common criteria in accordance with the caret position
     */
    private static Fold getLineFold(FoldHierarchy hierarchy, int dot, int lineStart, int lineEnd){
        Fold caretOffsetFold = FoldUtilities.findOffsetFold(hierarchy, dot);

        // beginning searching from the lineStart
        Fold fold = FoldUtilities.findNearestFold(hierarchy, lineStart);  
        
        while (fold != null && 
                  (fold.getEndOffset() <= dot || // find next available fold if the 'fold' is one-line
                      // or it has children and the caret is in the fold body
                      // i.e. class A{ |public void method foo(){}}
                      (!fold.isCollapsed() && fold.getFoldCount() > 0  && fold.getStartOffset()+1 < dot) 
                   )
               ){

            // look for next fold in forward direction
            Fold nextFold = FoldUtilities.findNearestFold(hierarchy,
               (fold.getFoldCount()>0) ? fold.getStartOffset()+1 : fold.getEndOffset());
            if (nextFold!=null && nextFold.getStartOffset()<lineEnd){
               if (nextFold == fold) return fold;
               fold = nextFold;
            } else {
               break;
            }
        }
        
        // a fold on the next line was found, returning fold at offset (in most cases inner class)
        if (fold == null || fold.getStartOffset() > lineEnd) {
            // in the case:
            // class A{
            // }     |
            // try to find an offset fold on the offset of the line beginning
            if (caretOffsetFold == null){
                caretOffsetFold = FoldUtilities.findOffsetFold(hierarchy, lineStart);
            }
            return caretOffsetFold;
        }
        
        // no fold at offset found, in this case return the fold
        if (caretOffsetFold == null) {
            return fold;
        }
        
        // skip possible inner class members validating if the innerclass fold is collapsed
        if (caretOffsetFold.isCollapsed()) {
            return caretOffsetFold;
        }
        
        // in the case:
        // class A{
        // public vo|id foo(){} }
        // 'fold' (in this case fold of the method foo) will be returned
        if (caretOffsetFold.getEndOffset() > fold.getEndOffset() && 
             fold.getEndOffset() > dot){
            return fold;
        }
        
        // class A{
        // |} public void method foo(){}
        // inner class fold will be returned
        if (fold.getStartOffset() > caretOffsetFold.getEndOffset()) {
            return caretOffsetFold;
        }
        
        // class A{
        // public void foo(){} |}
        // returning innerclass fold
        if (fold.getEndOffset() < dot) {
            return caretOffsetFold;
        }
        return fold;
    }
    
    /********************************** Miscellaneous **************************/
    
    /** Waits for given modified state of edited source.
     * @param modified boolean true waits for file state change to modified, false for change to
     * unmodified (saved).
     * Throws TimeoutExpiredException when EditorOperator.WaitModifiedTimeout expires.
     */    
    public void waitModified(final boolean modified) {
	try {
	    Waiter waiter = new Waiter(new Waitable() {
		    public Object actionProduced(Object obj) {
                        return isModified()==modified?new Object():null;
		    }
		    public String getDescription() {
			return("Wait Modified State");
		    }
		});
	    Timeouts times = getTimeouts().cloneThis();
	    times.setTimeout("Waiter.WaitingTime", times.getTimeout("EditorOperator.WaitModifiedTimeout"));
	    waiter.setTimeouts(times);
	    waiter.setOutput(getOutput());
	    waiter.waitAction(null);
	} catch(InterruptedException e) {}
    }
        
    /** Saves content of this Editor by API. If it is not applicable or content 
     * is not modified, it does nothing.
     */
    public void save() {
        super.save();
        if (getVerification()) {
            waitModified(false);
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtEditorPane();
        lblInputMode();
        lblRowColumn();
        lblStatusBar();
    }
    
    /** SubChooser to determine Editor TopComponent
     * Used in findTopComponent method.
     */
    public static final class EditorSubchooser implements ComponentChooser {
        /** Checks component.
         * @param comp component
         * @return true if component instance of CloneableEditor
         */
        public boolean checkComponent(Component comp) {
            return (comp instanceof CloneableEditor);
        }
        /** Description.
         * @return Description
         */
        public String getDescription() {
            return "org.openide.text.CloneableEditor";
        }
    }
}
