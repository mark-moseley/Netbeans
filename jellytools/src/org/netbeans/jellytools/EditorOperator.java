/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.jellytools.actions.SaveAction;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;

import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.Set;

/**
 * Handle an editor pane in NetBeans IDE. It enables to get, select, insert or
 * delete text, move caret, work with annotations and with toolbar buttons.
 * Majority of operations is done by JEditorPane API calls. If you want
 * to do operations by key navigation, use methods of JEditorPaneOperator
 * instance by {@link #txtEditorPane()}. For example, call
 * <code>txtEditorPane().changeCaretPosition(int)</code> instead of
 * <code>{@link #setCaretPosition(int)}</code>.
 * Use {@link EditorWindowOperator} to locate the Source Editor window and
 * to switch between editor panes.
 *
 * <p>
 * Usage:<br>
 * <pre>
        EditorWindowOperator ewo = new EditorWindowOperator();
        String filename = "MyClass";
        // switches to requested editor and gets EditorOperator instance
        EditorOperator eo = ewo.getEditor(filename);
        eo.setCaretPositionToLine(10);
        eo.insert("// My new comment\n");
        eo.select("// My new comment");
        eo.deleteLine(10);
        eo.getToolbarButton("Toggle Bookmark").push();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see EditorWindowOperator
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
    private JComboBoxOperator _cboQuickBrowse;
    
    /** Waits for first visible TopComponent with given name in whole IDE.
     * @param filename name of file showed in the editor (it used to be label of tab)
     */
    public EditorOperator(String filename) {
        super(filename);
    }
    
    /** Waits for index-th visible TopComponent with given name in whole IDE.
     * @param filename name of file showed in the editor (it used to be label of tab)
     * @param index index of TopComponent to be find
     */    
    public EditorOperator(String filename, int index) {
        super(filename, index);
    }

    /** Waits for first visible TopComponent with given name in specified container.
     * It doesn't find editor if it is not selected.
     * @param contOper container where to search
     * @param filename name of file showed in the editor (it used to be label of tab)
     */    
    public EditorOperator(ContainerOperator contOper, String filename) {
        super(contOper, filename);
    }

    /** Waits for index-th visible TopComponent with given name in specified container.
     * It doesn't find editor if it is not selected.
     * @param contOper container where to search
     * @param filename name of file showed in the editor (it used to be label of tab)
     * @param index index of TopComponent to be find
     */    
    public EditorOperator(ContainerOperator contOper, String filename, int index) {
        super(contOper, filename, index);
    }


    /** Tries to close Editor and if a file is modified and confirmation
     * dialog appears, it discards all changes.
     * It works also if no file is modified, so it is a safe way how to close
     * Editor and no block further execution.
     */
    public void closeDiscard() {
        close(false);
    }
        
    /** Tries to close Editor and if a file is modified and confirmation
     * dialog appears, it save or discards all changes.
     * It works also if no file is modified, so it is a safe way how to close
     * Editor and no block further execution.
     * @param save boolean true confirms save, false discards changes
     */
    public void close(final boolean save) {
        produceNoBlocking(new NoBlockingAction("Close Save/Discard dialog") {
            public Object doAction(Object param) {
                String title = Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title");
                int timeout = 10000;
                int time = 0;
                JDialog dialog = null;
                // every 200 ms of 10000 ms try to find dialog
                while(dialog == null && time < timeout) {
                    dialog = JDialogOperator.findJDialog(title, false, false);
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        throw new JemmyException("Waiting for Save/Discard dialog interrupted.", e);
                    }
                    time += 200;
                }
                if(dialog != null) {
                    String name;
                    if (save) {
                        name = Bundle.getStringTrimmed("org.openide.text.Bundle", 
                                                             "CTL_Save");
                    } else {
                        name = Bundle.getStringTrimmed("org.openide.text.Bundle", 
                                                             "CTL_Discard");
                    }
                    new JButtonOperator(new JDialogOperator(dialog), name).push();
                }
                return(null);
            }
        });
        super.close();
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
    
    /** Returns operator of combo box showing members of the class. It
     * is applicable only for Java objects.
     * @return JComboBoxOperator instance of members combo box
     */
    public JComboBoxOperator cboQuickBrowse() {
        if (_cboQuickBrowse == null) {
            _cboQuickBrowse = new JComboBoxOperator(this);
        }
        return _cboQuickBrowse;
    }
    
    /** Selects item in quick browse combo box.
     * @param item itme to be selected
     */
    public void setQuickBrowse(String item) {
        cboQuickBrowse().selectItem(item);
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
        return getLine(lineNumber).getText();
    }
    
    /** Returns instance of org.openide.text.Line for given line number.
     * @param lineNumber number of line (beggining at 1)
     * @return org.openide.text.Line instance
     */
    private Line getLine(int lineNumber) {
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
    public void insert(String text) {
        int offset = txtEditorPane().getCaretPosition();
        try {
            txtEditorPane().getDocument().insertString(offset, text, null);
        } catch (BadLocationException e) {
            throw new JemmyException("Cannot insert \""+text+"\" to position "+offset+".", e);
        }
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
    public void delete(int offset, int length) {
        try {
            txtEditorPane().getDocument().remove(offset, length);
        } catch (BadLocationException e) {
            throw new JemmyException("Cannot delete "+length+" characters from position "
                                     +offset+".", e);
        }
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
    
    /** Pushes Home key (KeyEvent.VK_HOME) */
    public void pushHomeKey() {
        txtEditorPane().pushKey(KeyEvent.VK_HOME);
    }
    
    /** Pushes End key (KeyEvent.VK_END) */
    public void pushEndKey() {
        txtEditorPane().pushKey(KeyEvent.VK_END);
    }
    
    /** Pushes Tab key (KeyEvent.VK_TAB) */
    public void pushTabKey() {
        txtEditorPane().pushKey(KeyEvent.VK_TAB);
    }
    
    /** Pushes Down key (KeyEvent.VK_DOWN) */
    public void pushDownArrowKey() {
        txtEditorPane().pushKey(KeyEvent.VK_DOWN);
    }
    
    /** Pushes Up key (KeyEvent.VK_UP) */
    public void pushUpArrowKey() {
        txtEditorPane().pushKey(KeyEvent.VK_UP);
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
    public Annotation[] getAnnotations(int lineNumber) {
        return (Annotation[])getAnnotations(getLine(lineNumber)).toArray(new Annotation[0]);
    }
    
    /** Gets annotations from given Line object
     * @param line instance of org.openide.text.Line
     * @return list of annotations
     */
    private List getAnnotations(Line line) {
        try {
            Method getAnnotations = Annotatable.class.getDeclaredMethod("getAnnotations", null);
            getAnnotations.setAccessible(true);
            return (List)getAnnotations.invoke(line, null);
        } catch (Exception e) {
            throw new JemmyException("getAnnotations() by reflection failed.", e);
        }
    }
    
    /**Gets all annotations for current editor (Document).
     * @return array of org.openide.text.Annotation containing all annotations
     *         attached to this editor.
     * @see #getAnnotationShortDescription
     * @see #getAnnotationType
     */
    public Annotation[] getAnnotations() {
        Document doc = txtEditorPane().getDocument();
        DataObject od = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);
        Set set = ((LineCookie)od.getCookie(LineCookie.class)).getLineSet();
        Iterator iter = set.getLines().iterator();
        List result = new ArrayList();
        while(iter.hasNext()) {
            result.addAll(getAnnotations((Line)iter.next()));
        }
        return (Annotation[])result.toArray(new Annotation[0]);
    }
    
    /** Returns a string uniquely identifying annotation. For editor bookmark
     * it is for example
     * org.netbeans.modules.editor.NbEditorKit.BOOKMARK_ANNOTATION_TYPE.
     * @param annotation instance of org.openide.text.Annotation
     * @return a string uniquely identifying annotation
     * @see #getAnnotations()
     *@see #getAnnotations(int)
     */
    public static String getAnnotationType(Annotation annotation) {
        return annotation.getAnnotationType();
    }
    
    /** Returns a short description of annotation. It is localized.
     * @param annotation instance of org.openide.text.Annotation
     * @return a short description of annotation according to current locale
     */
    public static String getAnnotationShortDescription(Annotation annotation) {
        return annotation.getShortDescription();
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
    
    /** Pushes popup menu on toolbar. It doesn't matter on which position it is
     * invoked, everytime it is the same. That's why popup menu is invoked on
     * the toolbar button with index 0. To switch toolbar on use Options ->
     * Editing -> Editor Settings -> Toolbar Visible -> true.
     * @param popupPath path to menu item (e.g. "Toolbar Visible")
     */
    public void pushToolbarPopupMenu(String popupPath) {
        getToolbarButton(0).clickForPopup();
        new JPopupMenuOperator().pushMenu(popupPath, "|");
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
    
    /** Returns current modify state of edited source
     * @return boolean true when edited source is modified
     */    
    public boolean isModified() {
        return getName().endsWith("*");
    }
    
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
        
    /** Performs save action with optional verification. */    
    public void save() {
        new SaveAction().perform(this);
        if (getVerification())
            waitModified(false);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtEditorPane();
        cboQuickBrowse();
        lblInputMode();
        lblRowColumn();
        lblStatusBar();
    }
}


