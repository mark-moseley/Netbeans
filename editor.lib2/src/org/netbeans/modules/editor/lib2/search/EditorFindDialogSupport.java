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

package org.netbeans.modules.editor.lib2.search;

import java.awt.Dialog;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import java.util.Iterator;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.lib2.KeyEventBlocker;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.netbeans.modules.editor.lib2.ComponentUtils;
import org.netbeans.modules.editor.lib2.DialogSupport;
import org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting;
import org.netbeans.modules.editor.lib2.highlighting.Factory;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport.SPW;
import org.openide.util.NbBundle;

/**
* Support for displaying find and replace dialogs
*
* <p><b>IMPORTANT:</b> Do not subclass this class and do not use its constructor.
* Always use <code>getInstance</code> method.
* 
* @author Miloslav Metelka
* @version 1.00
*/
public class EditorFindDialogSupport extends WindowAdapter implements ActionListener {

    private static final Logger LOG = Logger.getLogger(EditorFindDialogSupport.class.getName());
    
    /** This lock is used to create a barrier between showing/hiding/changing
     *  the dialog and testing if the dialog is already shown.
     *  it is used to make test-and-change / test-and-display actions atomic.
     *  It covers the following four fields: findDialog, isReplaceDialog,
     *  findPanel, findButtons
     */
    private static Object dialogLock = new Object();
    
    /** Whether the currently visible dialog is for replace */
    private  static boolean isReplaceDialog = false;

    /** The buttons used in the visible dialog */
    private  static JButton findButtons[];
    
    private static JButton findDialogButtons[];
    private static JButton replaceDialogButtons[];
    
    /** The FindPanel used inside the visible dialog */
    private  static FindPanel findPanel;

    /** Currently visible dialog */
    private  static Dialog findDialog = null;

    private int caretPosition;

    private static EditorFindDialogSupport singleton = null;
    private static PropertyChangeListener historyChangeListener;
    
    private boolean findPerformed = false;
    
    private static int xPos = Integer.MIN_VALUE;
    private static int yPos = Integer.MIN_VALUE;

    /** Flag for determining a dialog invocation. It the dialog
     *  is invoked by keystroke or by the menu the value is true.
     *  If the dialog was already shown and the focus was bring to it only,
     *  value is false - needed for fixing the issue #68021
     */
    private static boolean dialogInvokedViaKeystroke;
    
    public static EditorFindDialogSupport getInstance() {
        if (singleton == null) {
            singleton = new EditorFindDialogSupport();
        }
        return singleton;
    }

    /**
     * <p><b>IMPORTANT:</b> This is public only to maintain backwards compatibility
     * of the FindDialogSupport class. Do not use this constructor, use <code>getInstance</code>
     * method instead.
     */
    public EditorFindDialogSupport() {
    }

    private void createFindButtons() {
        if (findButtons == null) {
            ResourceBundle bundle = NbBundle.getBundle(EditorFindDialogSupport.class);
            findButtons = new JButton[] {
                new JButton(bundle.getString("find-button-find")), // NOI18N
                new JButton(bundle.getString("find-button-replace")), // NOI18N
                new JButton(bundle.getString("find-button-replace-all")), // NOI18N
                new JButton(bundle.getString("find-button-cancel")) // NOI18N
            };

            findButtons[0].setMnemonic(bundle.getString("find-button-find-mnemonic").charAt(0)); // NOI18N
            findButtons[1].setMnemonic(bundle.getString("find-button-replace-mnemonic").charAt(0)); // NOI18N
            findButtons[2].setMnemonic(bundle.getString("find-button-replace-all-mnemonic").charAt(0)); // NOI18N

            findButtons[0].getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_find-button-find")); // NOI18N
            findButtons[1].getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_find-button-replace")); // NOI18N
            findButtons[2].getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_find-button-replace-all")); // NOI18N
            findButtons[3].getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_find-button-cancel")); // NOI18N
            
            findDialogButtons = new JButton[2];
            findDialogButtons[0] = findButtons[0];
            findDialogButtons[1] = findButtons[3];
            
            replaceDialogButtons = new JButton[4];
            replaceDialogButtons[0] = findButtons[0];
            replaceDialogButtons[1] = findButtons[1];
            replaceDialogButtons[2] = findButtons[2];
            replaceDialogButtons[3] = findButtons[3];
        }
    }

    private void createFindPanel() {
        if (findPanel == null) {
            findPanel = new FindPanel();
        }
    }

    private Dialog createFindDialog(JPanel findPanel, final JButton[] buttons,
                                      final ActionListener l) {
        Dialog d = DialogSupport.getInstance().createDialog(
                isReplaceDialog
                    ? NbBundle.getBundle(EditorFindDialogSupport.class).getString ("replace-title")
                    : NbBundle.getBundle(EditorFindDialogSupport.class).getString ("find-title" ), // NOI18N
                findPanel, false, // non-modal
                buttons, true, // sidebuttons,
                0, // defaultIndex = 0 => findButton
                isReplaceDialog ? 3 : 1, // cancelIndex = 3 => cancelButton
                l //listener
        );

        return d;
    }

    private void showFindDialogImpl( boolean isReplace, KeyEventBlocker blocker) {
        dialogInvokedViaKeystroke = true;
        synchronized( dialogLock ) {
            if (findDialog != null && isReplaceDialog != isReplace ) {
                xPos = findDialog.getLocation().x;
                yPos = findDialog.getLocation().y;
                findDialog.dispose();
                findDialog = null;
            }
            if (findDialog == null) { // create and show new dialog of required type
                isReplaceDialog = isReplace;                            
                createFindButtons();
                createFindPanel();
                findPanel.changeVisibility(isReplace);
                
                findDialog = createFindDialog( findPanel, isReplace ? replaceDialogButtons : findDialogButtons, this );
                findDialog.addWindowListener( this );
                ((JDialog)findDialog).getRootPane().setFocusable(false);
                if(xPos > Integer.MIN_VALUE){
                    findDialog.setLocation(xPos, yPos);
                }
            }            
        } // end of synchronized section
        
        findDialog.pack();
        findPanel.init(isReplace, blocker);
        findDialog.setVisible(true);
        findPanel.showNotify();
        updateCaretPosition();
    }

    private void updateCaretPosition() {
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        if (c != null) {
            caretPosition = c.getCaret().getDot();
        }
    }

    public void windowActivated(WindowEvent evt) {
        findPerformed = false;
        createFindPanel();
        findPanel.initBlockSearch();
        updateCaretPosition();
    }
       
    public void windowDeactivated(WindowEvent evt) {
        Map<String, Object> findProps = findPanel.getFindProps();
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        if (c != null) {
            boolean blockSearch = getBooleanProp(EditorFindSupport.FIND_BLOCK_SEARCH, findProps);
            if (blockSearch && !findPerformed){
                Integer bsStartInt = (Integer)findProps.get(EditorFindSupport.FIND_BLOCK_SEARCH_START);
                int bsStart = (bsStartInt == null) ? -1 : bsStartInt.intValue();
                Position pos = (Position) findProps.get(EditorFindSupport.FIND_BLOCK_SEARCH_END);
                int bsEnd = (pos != null) ? pos.getOffset() : -1;
                if (bsStart >=0 && bsEnd > 0){
                    c.select(bsStart, bsEnd);
                }
            } else {
                BlockHighlighting layer = EditorFindSupport.getInstance()
                    .findLayer(c, Factory.INC_SEARCH_LAYER);
                
                if (layer != null) {
                    int[] block = layer.gethighlightedBlock();
                    if (block != null) {
                        c.select(block[0], block[1]);
                    }
                }
                
//                EditorUI editorUI = ((BaseTextUI)c.getUI()).getEditorUI();
//                DrawLayerFactory.IncSearchLayer incLayer
//                = (DrawLayerFactory.IncSearchLayer)editorUI.findLayer(
//                      DrawLayerFactory.INC_SEARCH_LAYER_NAME);
//                if (incLayer != null) {
//                    if (incLayer.isEnabled()) {
//                        int offs = incLayer.getOffset();
//                        int len = incLayer.getLength();
//                        if (len > 0){
//                            c.select(offs, offs + len);
//                        }
//                    }
//                }
            }
        }
        EditorFindSupport.getInstance().incSearchReset();
        findPanel.resetBlockSearch();
        KeyEventBlocker blocker = findPanel.getBlocker();
        if (blocker!=null){
            blocker.stopBlocking(false);
        }
    }

    public void windowClosing(WindowEvent e) {
        hideDialog();
    }

    public void windowClosed(WindowEvent e) {
        synchronized (dialogLock) {
            if (findDialog != null){
                xPos = findDialog.getLocation().x;
                yPos = findDialog.getLocation().y;
            }
        }
        Map<String, Object> findProps = findPanel.getFindProps();
        EditorFindSupport.getInstance().incSearchReset();
        findPanel.resetBlockSearch();
        EditorFindSupport.getInstance().setBlockSearchHighlight(0, 0);
        findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
        findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, new Integer(0));
        findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
        EditorFindSupport.getInstance().putFindProperties(findProps);
        KeyEventBlocker blocker = findPanel.getBlocker();
        if (blocker!=null){
            blocker.stopBlocking(false);
        }
        ComponentUtils.returnFocus();
    }

    public void showFindDialog(KeyEventBlocker blocker) {
        showFindDialogImpl(false, blocker);
    }

    public void showReplaceDialog(KeyEventBlocker blocker) {
        showFindDialogImpl(true, blocker);
    }

    public void hideDialog() {
        synchronized (dialogLock) {
            if (findDialog != null){
                xPos = findDialog.getLocation().x;
                yPos = findDialog.getLocation().y;
                findDialog.dispose();
            }
            findDialog = null;
        }
    }
    
    private Vector<String> getHistoryVector(){
        List<SPW> histList = EditorFindSupport.getInstance().getHistory();
        if (histList == null) histList = new ArrayList<SPW>();
        boolean isRegExpChecked = ((Boolean)findPanel.getFindProps().get(EditorFindSupport.FIND_REG_EXP)).booleanValue();
        Vector<String> vec = new Vector<String>();
        for (int i=0; i<histList.size(); i++){
            SPW spw = (SPW)histList.get(i);
            String searchExpression = spw.getSearchExpression();
            if (isRegExpChecked == spw.isRegExp() && !vec.contains(searchExpression)){
                vec.add(searchExpression);
            }
        }
        return vec;
    }
    
    private boolean getBooleanProp(String propName, Map map){
        Boolean b = (Boolean) map.get(propName);
        return (b!=null) ? b.booleanValue() : false;
    }

    public void actionPerformed(ActionEvent evt) {
        if( findButtons == null ) return;
        
        Object src = evt.getSource();
        EditorFindSupport fSup = EditorFindSupport.getInstance();
        Map<String, Object> findPanelMap = findPanel.getFindProps();
        
        SPW spw = new SPW((String)findPanelMap.get(EditorFindSupport.FIND_WHAT),
                getBooleanProp(EditorFindSupport.FIND_WHOLE_WORDS, findPanelMap),
                getBooleanProp(EditorFindSupport.FIND_MATCH_CASE, findPanelMap),
                getBooleanProp(EditorFindSupport.FIND_REG_EXP, findPanelMap));

        if (src == findButtons[0]) { // Find button
            fSup.addToHistory(spw);
            fSup.putFindProperties(findPanelMap);
            fSup.find(null, false);
            updateCaretPosition();
            findPerformed = true;
        } else if (src == findButtons[1]) { // Replace button
            fSup.addToHistory(spw);
            findPanel.updateReplaceHistory();
            fSup.putFindProperties(findPanelMap);
            try {
                if (fSup.replace(null, false)) { // replaced
                    fSup.find(null, false);
                }
            } catch (BadLocationException e) {
                // replace in guarded block
                if (!ComponentUtils.isGuardedException(e)) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
            updateCaretPosition();
            findPerformed = true;
        } else if (src == findButtons[2]) { // Replace All button
            fSup.addToHistory(spw);
            findPanel.updateReplaceHistory();
            fSup.putFindProperties(findPanelMap);
            fSup.replaceAll(null);
            findPerformed = true;
        } else if (src == findButtons[3]) { // Cancel button
            hideDialog();
        }
    }
    
    private int getBlockEndOffset(){
        Position pos = (Position) EditorFindSupport.getInstance().getFindProperties().get(EditorFindSupport.FIND_BLOCK_SEARCH_END);
        return (pos != null) ? pos.getOffset() : -1;
    }

    /** Panel that holds the find logic */
    private class FindPanel extends EditorFindDialogPanel
        implements ItemListener, KeyListener, ActionListener, FocusListener {

        private Map<String, Object> findProps = Collections.synchronizedMap(new HashMap<String, Object>(20));
        private Map<Object, String> objToProps = Collections.synchronizedMap(new HashMap<Object, String>(20));

        private javax.swing.DefaultComboBoxModel findHistory = new javax.swing.DefaultComboBoxModel();
        private javax.swing.DefaultComboBoxModel replaceHistory = new javax.swing.DefaultComboBoxModel();

        private KeyEventBlocker blocker;
        
        private int blockSearchStartPos = 0;
        private int blockSearchEndPos = 0;

        FindPanel() {
            objToProps.put(findWhat, EditorFindSupport.FIND_WHAT);
            objToProps.put(replaceWith, EditorFindSupport.FIND_REPLACE_WITH);
            objToProps.put(highlightSearch, EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
            objToProps.put(incSearch, EditorFindSupport.FIND_INC_SEARCH);
            objToProps.put(matchCase, EditorFindSupport.FIND_MATCH_CASE);
            //objToProps.put(smartCase, FindSupport.FIND_SMART_CASE);
            objToProps.put(wholeWords, EditorFindSupport.FIND_WHOLE_WORDS);
            objToProps.put(regExp, EditorFindSupport.FIND_REG_EXP);
            objToProps.put(bwdSearch, EditorFindSupport.FIND_BACKWARD_SEARCH);
            objToProps.put(wrapSearch, EditorFindSupport.FIND_WRAP_SEARCH);
            objToProps.put(blockSearch, EditorFindSupport.FIND_BLOCK_SEARCH);
            
            findProps.putAll(EditorFindSupport.getInstance().getFindProperties());
            revertMap();

            findWhat.setModel(findHistory);
            findWhat.getEditor().setItem(getProperty(findWhat));
            replaceWith.setModel(replaceHistory);
            replaceWith.getEditor().setItem(getProperty(replaceWith));
            highlightSearch.setSelected(getBooleanProperty(highlightSearch));
            incSearch.setSelected(getBooleanProperty(incSearch));
            matchCase.setSelected(getBooleanProperty(matchCase));
            //smartCase.setSelected(getBooleanProperty(smartCase));
            wholeWords.setSelected(getBooleanProperty(wholeWords));
            regExp.setSelected(getBooleanProperty(regExp));
            bwdSearch.setSelected(getBooleanProperty(bwdSearch));
            wrapSearch.setSelected(getBooleanProperty(wrapSearch));

            findWhat.getEditor().getEditorComponent().addKeyListener(this);
            findWhat.addActionListener(this);
            replaceWith.getEditor().getEditorComponent().addKeyListener(this);
            replaceWith.addActionListener(this);
            highlightSearch.addItemListener(this);
            incSearch.addItemListener(this);
            matchCase.addItemListener(this);
            //smartCase.addItemListener(this);
            wholeWords.addItemListener(this);
            regExp.addItemListener(this);
            bwdSearch.addItemListener(this);
            wrapSearch.addItemListener(this);
            blockSearch.addItemListener(this);
            historyChangeListener = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt){
                    if (evt == null || !EditorFindSupport.FIND_HISTORY_CHANGED_PROP.equals(evt.getPropertyName())){
                        return;
                    }
                    updateFindHistory();
                }
            };
            EditorFindSupport.getInstance().addPropertyChangeListener(historyChangeListener);
        }

        protected Map<String, Object> getFindProps() {
            return findProps;
        }
        
        private KeyEventBlocker getBlocker(){
            return blocker;
        }

        private void putProperty(Object component, Object value) {
            String prop = (String)objToProps.get(component);
            if (prop != null) {
                findProps.put(prop, value);
            }
        }

        private Object getProperty(Object component) {
            String prop = (String)objToProps.get(component);
            return (prop != null) ? findProps.get(prop) : null;
        }

        private boolean getBooleanProperty(Object component) {
            Object prop = getProperty(component);
            return (prop != null) ? ((Boolean)prop).booleanValue() : false;
        }

        protected void changeVisibility(boolean v) {
            replaceWith.setVisible(v);
            replaceWithLabel.setVisible(v);
        }

        public void resetBlockSearch(){
            blockSearch.setSelected(false);
            blockSearch.setEnabled(false);
            findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
            findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, new Integer(0));
            findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
            blockSearchStartPos = 0;
            blockSearchEndPos = 0;
            EditorFindSupport.getInstance().setBlockSearchHighlight(0,0);
            EditorFindSupport.getInstance().putFindProperties(findProps);
        }
        
        private void initBlockSearch(){
            JTextComponent c = EditorRegistry.lastFocusedComponent();
            String selText = null;
            int startSelection = 0;
            int endSelection = 0;
            boolean blockSearchVisible = false;
            
            if (c != null) {
                startSelection = c.getSelectionStart();
                endSelection = c.getSelectionEnd();
                
                Document doc = c.getDocument();
                try{
                    int startLine = DocUtils.getLineOffset(doc, startSelection);
                    int endLine = DocUtils.getLineOffset(doc, endSelection);
                    if (endLine > startLine) {
                        blockSearchVisible = true;
                    }
                } catch (BadLocationException ble){
                }

                caretPosition = bwdSearch.isSelected() ? c.getSelectionEnd() : c.getSelectionStart();
                
                if (blockSearchVisible == false && dialogInvokedViaKeystroke){
                    dialogInvokedViaKeystroke = false;
                    selText = c.getSelectedText();
                    if (selText != null) {
                        int n = selText.indexOf( '\n' );
                        if (n >= 0 ) selText = selText.substring(0, n);
                        findWhat.getEditor().setItem(selText);
                        changeFindWhat(true);
                    }
                }
            
                blockSearchStartPos = blockSearchVisible ? startSelection : 0;
                blockSearchEndPos = blockSearchVisible ? endSelection : 0;
                
                try{
                    blockSearch.setEnabled(blockSearchVisible);
                    blockSearch.setSelected(blockSearchVisible);
                    findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.valueOf(blockSearchVisible));
                    findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, new Integer(blockSearchStartPos));
                    int be = getBlockEndOffset();
                    if (be < 0){
                        findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_END, doc.createPosition(blockSearchEndPos));
                    }else{
                        blockSearchEndPos = be;
                    }
                    EditorFindSupport.getInstance().setBlockSearchHighlight(blockSearchStartPos, blockSearchEndPos);
                }catch(BadLocationException ble){
                    blockSearch.setSelected(false);
                    findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
                    findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, null);
                }
            }
            
        }

        protected void init(boolean isReplace, KeyEventBlocker blocker) {
            this.blocker = blocker;
            findHistory.setSelectedItem(null);
            replaceHistory.setSelectedItem(null);
            findWhat.getEditor().getEditorComponent().addFocusListener(this);
            if (isReplace) {
                replaceWith.getEditor().getEditorComponent().addFocusListener(this);
            }

            findProps.putAll(EditorFindSupport.getInstance().getFindProperties());
            revertMap();

            highlightSearch.setSelected(getBooleanProperty(highlightSearch));
            incSearch.setSelected(getBooleanProperty(incSearch));
            matchCase.setSelected(getBooleanProperty(matchCase));
            //smartCase.setSelected(getBooleanProperty(smartCase));
            wholeWords.setSelected(getBooleanProperty(wholeWords));
            boolean regExpValue = getBooleanProperty(regExp);
            regExp.setSelected(regExpValue);
            wholeWords.setEnabled(!regExpValue);
            incSearch.setEnabled(!regExpValue);
            bwdSearch.setSelected(getBooleanProperty(bwdSearch));
            wrapSearch.setSelected(getBooleanProperty(wrapSearch));
            findHistory = new DefaultComboBoxModel(getHistoryVector());
            findWhat.setModel(findHistory);
        }

        protected void showNotify() {
            // fix of issue #66217 
            boolean focused = findWhat.getEditor().getEditorComponent().requestFocusInWindow();
            if (focused == false){
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        findWhat.getEditor().getEditorComponent().requestFocusInWindow();
                    }
                });
            }
        }

        private void updateHistory(JComboBox c, javax.swing.DefaultComboBoxModel history) {
            Object item = c.getEditor().getItem();
            if( item != null && !item.equals("")) { //NOI18N
                history.removeElement(item);
                history.insertElementAt(item, 0);
                history.setSelectedItem(null);
            }
            c.getEditor().setItem(item);
        }
        
        protected void updateFindHistory() {
            //updateHistory(findWhat, findHistory);
            /*
            List list = new ArrayList();
            for (int i = 0; i<findHistory.getSize(); i++){
                list.add(findHistory.getElementAt(i));
            }
            FindSupport.getFindSupport().putFindProperty(FindSupport.FIND_HISTORY, list);
            findProps.put(FindSupport.FIND_HISTORY, list);
             */
            Object obj = findWhat.getEditor().getItem();
            findHistory = new DefaultComboBoxModel(getHistoryVector());
            findWhat.setModel(findHistory);
            if (obj != null){
                findWhat.getEditor().setItem(obj);
            }
        }

        protected void updateReplaceHistory() {
            updateHistory(replaceWith, replaceHistory);
        }

        private void revertMap(){
            Object prop = findProps.get(EditorFindSupport.REVERT_MAP);
            if (!(prop instanceof Map)) return;
            Map revertMap = (Map)prop;

            for( Iterator i = revertMap.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();

                Object obj = findProps.get(key);
                boolean value = ( obj != null ) ? ((Boolean)obj).booleanValue() : false;
                if (value != ((Boolean)revertMap.get(key)).booleanValue());
                    findProps.put(key, value ? Boolean.FALSE : Boolean.TRUE);
            }

            findProps.put(EditorFindSupport.REVERT_MAP, null);
        }

        private void changeFindWhat(boolean performIncSearch) {
            Object old = getProperty(findWhat);
            Object cur = findWhat.getEditor().getItem();
            if ((old == null && cur != null && !cur.equals("")) || (old != null && !old.equals(cur))) { // NOI18N
                putProperty(findWhat, cur);
                if (performIncSearch){
                    findPerformed = EditorFindSupport.getInstance().incSearch(getFindProps(), caretPosition);  
                }
            }
        }

        private void changeReplaceWith() {
            Object old = getProperty(replaceWith);
            Object cur = replaceWith.getEditor().getItem();
            if ((old == null && cur != null && !cur.equals("")) || (old != null && !old.equals(cur))) { // NOI18N
                putProperty(replaceWith, cur);
            }
        }

        private void postChangeCombos(final boolean performIncSearch) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        changeFindWhat(performIncSearch);
                        changeReplaceWith();
                    }
                }
            );
        }

        public void keyPressed(KeyEvent evt) {
            if (evt.getKeyChar() == '\n') {
                evt.consume();
            }
        }

        public void keyReleased(KeyEvent evt) {
            if (evt.getKeyChar() == '\n') {
                evt.consume();
            } else if (evt.getKeyCode() == KeyEvent.VK_INSERT){
                postChangeCombos(true);
            }
        }

        public void keyTyped(KeyEvent evt) {
            if (evt.getKeyChar() == '\n') {
                findButtons[0].doClick(20);
                evt.consume();
                ((JComboBox)((JTextField)evt.getSource()).getParent()).hidePopup();
            } else {
                postChangeCombos(true);
            }
        }

        public void itemStateChanged(ItemEvent evt)  {
            Boolean val = (evt.getStateChange() == ItemEvent.SELECTED) ? Boolean.TRUE
                          : Boolean.FALSE;
            if (evt.getItem() == bwdSearch){
                if (blockSearch.isEnabled() && blockSearch.isSelected()) {
                    boolean value = val.booleanValue();
                    JTextComponent c = EditorRegistry.lastFocusedComponent();
                    if (c!=null){
                        c.getCaret().setDot(value ? blockSearchEndPos : blockSearchStartPos);
                        updateCaretPosition();
                    }
                    
                }
            }
            if (evt.getItem() == regExp){
                boolean value = !val.booleanValue();
                incSearch.setEnabled(value);
                wholeWords.setEnabled(value);
            }
            if (evt.getItem() == blockSearch){
                boolean value = val.booleanValue();
                if (value){
                    if (blockSearchStartPos <= 0  && blockSearchEndPos <= 0 ) {
                        initBlockSearch();
                    }else{
                        JTextComponent c = EditorRegistry.lastFocusedComponent();
                        if (c!=null){
                            c.getCaret().setDot(bwdSearch.isSelected() ? blockSearchEndPos : blockSearchStartPos);
                            updateCaretPosition();
                        }
                    }
                    EditorFindSupport.getInstance().setBlockSearchHighlight(blockSearchStartPos, blockSearchEndPos);
                } else {
                    EditorFindSupport.getInstance().putFindProperty(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
                    EditorFindSupport.getInstance().setBlockSearchHighlight(0, 0);
                }
            }
                          
            putProperty(evt.getSource(), val);
            
            if (evt.getItem() == regExp){
                updateFindHistory();
            }
        }

        public void actionPerformed(ActionEvent evt) {
            postChangeCombos(false);
        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() instanceof JTextField) {
                ((JTextField)e.getSource()).selectAll();
                if (blocker != null){
                    blocker.stopBlocking();
                }
            }
            ((JComponent)e.getSource()).removeFocusListener(this);
        }

        public void focusLost(FocusEvent e) {
            
        }



    }

}
