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

package org.netbeans.jellytools.modules.web;

import javax.swing.JComboBox;
import javax.swing.JTable;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;


/** Class implementing all necessary methods for handling "New Web Application
 * with Existing Ant Script - Source Package Folder" wizard step.
 *
 * @author Martin.Schovanek@sun.com
 * @version 1.0
 */
public class NewWebFreeFormSrcFoldersStepOperator extends WizardOperator{

    /** Creates new NewWebFreeFormSrcFoldersStepOperator that can handle it.
     */
    public NewWebFreeFormSrcFoldersStepOperator() {
        super(Helper.freeFormWizardTitle());
    }
    
    private JLabelOperator _lblSourcePackageFolders;
    private JLabelOperator _lblSourceLevel;
    private JComboBoxOperator _cboSourceLevel;
    public static final String ITEM_JDK13 = Bundle.getStringTrimmed(
            "org.netbeans.modules.java.freeform.ui.Bundle",
            "LBL_SourceFoldersPanel_JDK13");
    public static final String ITEM_JDK14 = Bundle.getStringTrimmed(
            "org.netbeans.modules.java.freeform.ui.Bundle",
            "LBL_SourceFoldersPanel_JDK14");
    public static final String ITEM_JDK15 = Bundle.getStringTrimmed(
            "org.netbeans.modules.java.freeform.ui.Bundle",
            "LBL_SourceFoldersPanel_JDK15");
    private JButtonOperator _btAddFolderSrc;
    private JButtonOperator _btRemoveSrc;
    private JTableOperator _tabSourcePackageFolders;
    private JLabelOperator _lblTestPackageFolders;
    private JButtonOperator _btAddFolderTest;
    private JButtonOperator _btRemoveTest;
    private JButtonOperator _btMoveUpSrc;
    private JButtonOperator _btMoveDownSrc;
    private JButtonOperator _btMoveDownTest;
    private JButtonOperator _btMoveUpTest;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Source Package Folders:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcePackageFolders() {
        if (_lblSourcePackageFolders==null) {
            String sourcePkg = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "LBL_SourceFoldersPanel_jLabel2");
            _lblSourcePackageFolders = new JLabelOperator(this, sourcePkg);
        }
        return _lblSourcePackageFolders;
    }
    
    /** Tries to find "Source Level:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourceLevel() {
        if (_lblSourceLevel==null) {
            String sourceLevel = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "LBL_SourceFoldersPanel_jLabel3");
            _lblSourceLevel = new JLabelOperator(this, "Source Level:");
        }
        return _lblSourceLevel;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSourceLevel() {
        if (_cboSourceLevel==null) {
            if (lblSourceLevel().getLabelFor()!=null) {
                _cboSourceLevel = new JComboBoxOperator(
                        (JComboBox) lblSourceLevel().getLabelFor());
            } else {
                _cboSourceLevel = new JComboBoxOperator(this);
            }
        }
        return _cboSourceLevel;
    }
    
    /** Tries to find "Add Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddFolderSrc() {
        if (_btAddFolderSrc==null) {
            String addFolder = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_addFolder");
            _btAddFolderSrc = new JButtonOperator(this, addFolder);
        }
        return _btAddFolderSrc;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemoveSrc() {
        if (_btRemoveSrc==null) {
            String remove = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_removeFolder");
            _btRemoveSrc = new JButtonOperator(this, remove);
        }
        return _btRemoveSrc;
    }
    
    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabSourcePackageFolders() {
        if (_tabSourcePackageFolders==null) {
            if (lblSourcePackageFolders().getLabelFor()!=null) {
                _tabSourcePackageFolders = new JTableOperator(
                        (JTable) lblSourcePackageFolders().getLabelFor());
            } else {
                _tabSourcePackageFolders = new JTableOperator(this);
            }
        }
        return _tabSourcePackageFolders;
    }
    
    /** Tries to find "Test Package Folders:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTestPackageFolders() {
        if (_lblTestPackageFolders==null) {
            String testPkg = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "LBL_TestSourceFoldersPanel");
            _lblTestPackageFolders = new JLabelOperator(this, testPkg);
        }
        return _lblTestPackageFolders;
    }
    
    /** Tries to find "Add Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddFolderTest() {
        if (_btAddFolderTest==null) {
            String addFolder = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_addTestFolder");
            _btAddFolderTest = new JButtonOperator(this, addFolder, 1);
        }
        return _btAddFolderTest;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemoveTest() {
        if (_btRemoveTest==null) {
            String remove = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_removeTestFolder");
            _btRemoveTest = new JButtonOperator(this, remove, 1);
        }
        return _btRemoveTest;
    }
    
    /** Tries to find "Move Up" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUpSrc() {
        if (_btMoveUpSrc==null) {
            String moveUp = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_upFolder");
            _btMoveUpSrc = new JButtonOperator(this, moveUp);
        }
        return _btMoveUpSrc;
    }
    
    /** Tries to find "Move Down" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDownSrc() {
        if (_btMoveDownSrc==null) {
            String moveDown = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_downFolder");
            _btMoveDownSrc = new JButtonOperator(this, moveDown);
        }
        return _btMoveDownSrc;
    }
    
    /** Tries to find "Move Down" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDownTest() {
        if (_btMoveDownTest==null) {
            String moveDown = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_downTestFolder");
            _btMoveDownTest = new JButtonOperator(this, moveDown, 1);
        }
        return _btMoveDownTest;
    }
    
    /** Tries to find "Move Up" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUpTest() {
        if (_btMoveUpTest==null) {
            String moveUp = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_upTestFolder");
            _btMoveUpTest = new JButtonOperator(this, moveUp, 1);
        }
        return _btMoveUpTest;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboSourceLevel
     * @return String item
     */
    public String getSelectedSourceLevel() {
        return cboSourceLevel().getSelectedItem().toString();
    }
    
    /** selects item for cboSourceLevel
     * @param item String item
     */
    public void selectSourceLevel(String item) {
        cboSourceLevel().selectItem(item);
    }
    
    /** clicks on "Add Folder..." JButton
     */
    public void addFolderSrc() {
        btAddFolderSrc().push();
    }
    
    /** clicks on "Remove" JButton
     */
    public void removeSrc() {
        btRemoveSrc().push();
    }
    
    /** clicks on "Add Folder..." JButton
     */
    public void addFolderTest() {
        btAddFolderTest().push();
    }
    
    /** clicks on "Remove" JButton
     */
    public void removeTest() {
        btRemoveTest().push();
    }
    
    /** clicks on "Move Up" JButton
     */
    public void moveUpSrc() {
        btMoveUpSrc().push();
    }
    
    /** clicks on "Move Down" JButton
     */
    public void moveDownSrc() {
        btMoveDownSrc().push();
    }
    
    /** clicks on "Move Down" JButton
     */
    public void moveDownTest() {
        btMoveDownTest().push();
    }
    
    /** clicks on "Move Up" JButton
     */
    public void moveUpTest() {
        btMoveUpTest().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of NewWebFreeFormSrcFoldersStepOperator by
     * accessing all its components.
     */
    public void verify() {
        lblSourcePackageFolders();
        lblSourceLevel();
        cboSourceLevel();
        btAddFolderSrc();
        btRemoveSrc();
        tabSourcePackageFolders();
        lblTestPackageFolders();
        btAddFolderTest();
        btRemoveTest();
        btMoveUpSrc();
        btMoveDownSrc();
        btMoveDownTest();
        btMoveUpTest();
    }
}
