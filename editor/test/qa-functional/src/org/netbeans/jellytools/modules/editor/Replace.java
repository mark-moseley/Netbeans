/*
 * Replace.java
 *
 * Created on 1/24/05 5:01 PM
 */
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "Replace" NbDialog.
 *
 * @author rs155161
 * @version 1.0
 */
public class Replace extends JDialogOperator {

    /** Creates new Replace that can handle it.
     */
    public Replace() {
        super("Replace");
    }

    private JLabelOperator _lblFindWhat;
    private JComboBoxOperator _cboFindWhat;
    private JLabelOperator _lblReplaceWith;
    private JComboBoxOperator _cboReplaceWith;
    private JCheckBoxOperator _cbHighlightSearch;
    private JCheckBoxOperator _cbIncrementalSearch;
    private JCheckBoxOperator _cbMatchCase;
    private JCheckBoxOperator _cbSmartCase;
    private JCheckBoxOperator _cbMatchWholeWordsOnly;
    private JCheckBoxOperator _cbBackwardSearch;
    private JCheckBoxOperator _cbWrapSearch;
    private JCheckBoxOperator _cbRegularExpressions;
    private JCheckBoxOperator _cbSearchSelection;
    private JButtonOperator _btFind;
    private JButtonOperator _btReplace;
    private JButtonOperator _btReplaceAll;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Find What:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFindWhat() {
        if (_lblFindWhat==null) {
            _lblFindWhat = new JLabelOperator(this, "Find What:");
        }
        return _lblFindWhat;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboFindWhat() {
        if (_cboFindWhat==null) {
            _cboFindWhat = new JComboBoxOperator(this);
        }
        return _cboFindWhat;
    }

    /** Tries to find "Replace With:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblReplaceWith() {
        if (_lblReplaceWith==null) {
            _lblReplaceWith = new JLabelOperator(this, "Replace With:");
        }
        return _lblReplaceWith;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboReplaceWith() {
        if (_cboReplaceWith==null) {
            _cboReplaceWith = new JComboBoxOperator(this, 1);
        }
        return _cboReplaceWith;
    }

    /** Tries to find " Highlight Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbHighlightSearch() {
        if (_cbHighlightSearch==null) {
            _cbHighlightSearch = new JCheckBoxOperator(this, " Highlight Search");
        }
        return _cbHighlightSearch;
    }

    /** Tries to find " Incremental Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncrementalSearch() {
        if (_cbIncrementalSearch==null) {
            _cbIncrementalSearch = new JCheckBoxOperator(this, " Incremental Search");
        }
        return _cbIncrementalSearch;
    }

    /** Tries to find " Match Case" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMatchCase() {
        if (_cbMatchCase==null) {
            _cbMatchCase = new JCheckBoxOperator(this, " Match Case");
        }
        return _cbMatchCase;
    }

    /** Tries to find " Smart Case" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSmartCase() {
        if (_cbSmartCase==null) {
            _cbSmartCase = new JCheckBoxOperator(this, " Smart Case");
        }
        return _cbSmartCase;
    }

    /** Tries to find " Match Whole Words Only" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMatchWholeWordsOnly() {
        if (_cbMatchWholeWordsOnly==null) {
            _cbMatchWholeWordsOnly = new JCheckBoxOperator(this, " Match Whole Words Only");
        }
        return _cbMatchWholeWordsOnly;
    }

    /** Tries to find " Backward Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbBackwardSearch() {
        if (_cbBackwardSearch==null) {
            _cbBackwardSearch = new JCheckBoxOperator(this, " Backward Search");
        }
        return _cbBackwardSearch;
    }

    /** Tries to find " Wrap Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbWrapSearch() {
        if (_cbWrapSearch==null) {
            _cbWrapSearch = new JCheckBoxOperator(this, " Wrap Search");
        }
        return _cbWrapSearch;
    }

    /** Tries to find " Regular Expressions" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRegularExpressions() {
        if (_cbRegularExpressions==null) {
            _cbRegularExpressions = new JCheckBoxOperator(this, " Regular Expressions");
        }
        return _cbRegularExpressions;
    }

    /** Tries to find " Search Selection" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSearchSelection() {
        if (_cbSearchSelection==null) {
            _cbSearchSelection = new JCheckBoxOperator(this, " Search Selection");
        }
        return _cbSearchSelection;
    }

    /** Tries to find "Find" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFind() {
        if (_btFind==null) {
            _btFind = new JButtonOperator(this, "Find");
        }
        return _btFind;
    }

    /** Tries to find "Replace" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btReplace() {
        if (_btReplace==null) {
            _btReplace = new JButtonOperator(this, "Replace");
        }
        return _btReplace;
    }

    /** Tries to find "Replace All" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btReplaceAll() {
        if (_btReplaceAll==null) {
            _btReplaceAll = new JButtonOperator(this, "Replace All");
        }
        return _btReplaceAll;
    }

    /** Tries to find "Close" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClose() {
        if (_btClose==null) {
            _btClose = new JButtonOperator(this, "Close");
        }
        return _btClose;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboFindWhat
     * @return String item
     */
    public String getSelectedFindWhat() {
        return cboFindWhat().getSelectedItem().toString();
    }

    /** selects item for cboFindWhat
     * @param item String item
     */
    public void selectFindWhat(String item) {
        cboFindWhat().selectItem(item);
    }

    /** types text for cboFindWhat
     * @param text String text
     */
    public void typeFindWhat(String text) {
        cboFindWhat().typeText(text);
    }

    /** returns selected item for cboReplaceWith
     * @return String item
     */
    public String getSelectedReplaceWith() {
        return cboReplaceWith().getSelectedItem().toString();
    }

    /** selects item for cboReplaceWith
     * @param item String item
     */
    public void selectReplaceWith(String item) {
        cboReplaceWith().selectItem(item);
    }

    /** types text for cboReplaceWith
     * @param text String text
     */
    public void typeReplaceWith(String text) {
        cboReplaceWith().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkHighlightSearch(boolean state) {
        if (cbHighlightSearch().isSelected()!=state) {
            cbHighlightSearch().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncrementalSearch(boolean state) {
        if (cbIncrementalSearch().isSelected()!=state) {
            cbIncrementalSearch().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMatchCase(boolean state) {
        if (cbMatchCase().isSelected()!=state) {
            cbMatchCase().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSmartCase(boolean state) {
        if (cbSmartCase().isSelected()!=state) {
            cbSmartCase().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMatchWholeWordsOnly(boolean state) {
        if (cbMatchWholeWordsOnly().isSelected()!=state) {
            cbMatchWholeWordsOnly().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkBackwardSearch(boolean state) {
        if (cbBackwardSearch().isSelected()!=state) {
            cbBackwardSearch().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkWrapSearch(boolean state) {
        if (cbWrapSearch().isSelected()!=state) {
            cbWrapSearch().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRegularExpressions(boolean state) {
        if (cbRegularExpressions().isSelected()!=state) {
            cbRegularExpressions().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSearchSelection(boolean state) {
        if (cbSearchSelection().isSelected()!=state) {
            cbSearchSelection().push();
        }
    }

    /** clicks on "Find" JButton
     */
    public void find() {
        btFind().push();
    }

    /** clicks on "Replace" JButton
     */
    public void replace() {
        btReplace().push();
    }

    /** clicks on "Replace All" JButton
     */
    public void replaceAll() {
        btReplaceAll().push();
    }

    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of Replace by accessing all its components.
     */
    public void verify() {
        lblFindWhat();
        cboFindWhat();
        lblReplaceWith();
        cboReplaceWith();
        cbHighlightSearch();
        cbIncrementalSearch();
        cbMatchCase();
        cbSmartCase();
        cbMatchWholeWordsOnly();
        cbBackwardSearch();
        cbWrapSearch();
        cbRegularExpressions();
        cbSearchSelection();
        btFind();
        btReplace();
        btReplaceAll();
        btClose();
        btHelp();
    }

    /** Performs simple test of Replace
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new Replace().verify();
        System.out.println("Replace verification finished.");
    }
}

