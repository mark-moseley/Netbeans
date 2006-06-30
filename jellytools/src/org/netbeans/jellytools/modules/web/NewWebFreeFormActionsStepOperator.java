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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;


/** Class implementing all necessary methods for handling "New Web Application
 * with Existing Ant Script - Build and Run Actions" wizard step.
 *
 * @author Martin.Schovanek@sun.com
 * @version 1.0
 */
public class NewWebFreeFormActionsStepOperator extends WizardOperator {

    /** Creates new NewWebFreeFormActionsStepOperator that can handle it.
     */
    public NewWebFreeFormActionsStepOperator() {
        super(Helper.freeFormWizardTitle());
    }
    
    private JLabelOperator _lblBuildProject;
    private JLabelOperator _lblCleanProject;
    private JLabelOperator _lblRunProject;
    private JLabelOperator _lblGenerateJavadoc;
    private JLabelOperator _lblTestProject;
    private JComboBoxOperator _cboBuildProject;
    private JComboBoxOperator _cboCleanProject;
    private JComboBoxOperator _cboGenerateJavadoc;
    private JComboBoxOperator _cboRunProject;
    private JComboBoxOperator _cboTestProject;
    private JComboBoxOperator _cboDeployProject;
    private JLabelOperator _lblDeployProject;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Build Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBuildProject() {
        if (_lblBuildProject==null) {
            String buildProject = Bundle.getStringTrimmed(
                    "org.netbeans.modules.ant.freeform.ui.Bundle",
                    "LBL_TargetMappingPanel_jLabel2");
            _lblBuildProject = new JLabelOperator(this, buildProject);
        }
        return _lblBuildProject;
    }
    
    /** Tries to find "Clean Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCleanProject() {
        if (_lblCleanProject==null) {
            String cleanProject = Bundle.getStringTrimmed(
                    "org.netbeans.modules.ant.freeform.ui.Bundle",
                    "LBL_TargetMappingPanel_jLabel4");
            _lblCleanProject = new JLabelOperator(this, cleanProject);
        }
        return _lblCleanProject;
    }
    
    /** Tries to find "Run Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRunProject() {
        if (_lblRunProject==null) {
            String runProject = Bundle.getStringTrimmed(
                    "org.netbeans.modules.ant.freeform.ui.Bundle",
                    "LBL_TargetMappingPanel_jLabel5");
            _lblRunProject = new JLabelOperator(this, runProject);
        }
        return _lblRunProject;
    }
    
    /** Tries to find "Generate Javadoc:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblGenerateJavadoc() {
        if (_lblGenerateJavadoc==null) {
            String generateJavadoc= Bundle.getStringTrimmed(
                    "org.netbeans.modules.ant.freeform.ui.Bundle",
                    "LBL_TargetMappingPanel_jLabel6");
            _lblGenerateJavadoc = new JLabelOperator(this, generateJavadoc);
        }
        return _lblGenerateJavadoc;
    }
    
    /** Tries to find "Test Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTestProject() {
        if (_lblTestProject==null) {
            String testProject = Bundle.getStringTrimmed(
                    "org.netbeans.modules.ant.freeform.ui.Bundle",
                    "LBL_TargetMappingPanel_jLabel7");
            _lblTestProject = new JLabelOperator(this, testProject);
        }
        return _lblTestProject;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboBuildProject() {
        if (_cboBuildProject==null) {
            if (lblBuildProject().getLabelFor()!=null) {
                _cboBuildProject = new JComboBoxOperator(
                        (JComboBox) lblBuildProject().getLabelFor());
            } else {
                _cboBuildProject = new JComboBoxOperator(this);
            }
        }
        return _cboBuildProject;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboCleanProject() {
        if (_cboCleanProject==null) {
            if (lblCleanProject().getLabelFor()!=null) {
                _cboCleanProject = new JComboBoxOperator(
                        (JComboBox) lblCleanProject().getLabelFor());
            } else {
                _cboCleanProject = new JComboBoxOperator(this, 1);
            }
        }
        return _cboCleanProject;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboGenerateJavadoc() {
        if (_cboGenerateJavadoc==null) {
            if (lblGenerateJavadoc().getLabelFor()!=null) {
                _cboGenerateJavadoc = new JComboBoxOperator(
                        (JComboBox) lblGenerateJavadoc().getLabelFor());
            } else {
                _cboGenerateJavadoc = new JComboBoxOperator(this, 2);
            }
        }
        return _cboGenerateJavadoc;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRunProject() {
        if (_cboRunProject==null) {
            if (lblRunProject().getLabelFor()!=null) {
                _cboRunProject = new JComboBoxOperator(
                        (JComboBox) lblRunProject().getLabelFor());
            } else {
                _cboRunProject = new JComboBoxOperator(this, 3);
            }
        }
        return _cboRunProject;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboTestProject() {
        if (_cboTestProject==null) {
            if (lblTestProject().getLabelFor()!=null) {
                _cboTestProject = new JComboBoxOperator(
                        (JComboBox) lblTestProject().getLabelFor());
            } else {
                _cboTestProject = new JComboBoxOperator(this, 4);
            }
        }
        return _cboTestProject;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboDeployProject() {
        if (_cboDeployProject==null) {
            if (lblDeployProject().getLabelFor()!=null) {
                _cboDeployProject = new JComboBoxOperator(
                        (JComboBox) lblDeployProject().getLabelFor());
            } else {
                _cboDeployProject = new JComboBoxOperator(this, 5);
            }
        }
        return _cboDeployProject;
    }
    
    /** Tries to find "Deploy Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDeployProject() {
        if (_lblDeployProject==null) {
            String deployProject = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.Bundle",
                    "LBL_TargetMappingPanel_Deploy");
            _lblDeployProject = new JLabelOperator(this, deployProject);
        }
        return _lblDeployProject;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboBuildProject
     * @return String item
     */
    public String getSelectedBuildProject() {
        return cboBuildProject().getSelectedItem().toString();
    }
    
    /** selects item for cboBuildProject
     * @param item String item
     */
    public void selectBuildProject(String item) {
        cboBuildProject().selectItem(item);
    }
    
    /** types text for cboBuildProject
     * @param text String text
     */
    public void typeBuildProject(String text) {
        cboBuildProject().typeText(text);
    }
    
    /** returns selected item for cboCleanProject
     * @return String item
     */
    public String getSelectedCleanProject() {
        return cboCleanProject().getSelectedItem().toString();
    }
    
    /** selects item for cboCleanProject
     * @param item String item
     */
    public void selectCleanProject(String item) {
        cboCleanProject().selectItem(item);
    }
    
    /** types text for cboCleanProject
     * @param text String text
     */
    public void typeCleanProject(String text) {
        cboCleanProject().typeText(text);
    }
    
    /** returns selected item for cboGenerateJavadoc
     * @return String item
     */
    public String getSelectedGenerateJavadoc() {
        return cboGenerateJavadoc().getSelectedItem().toString();
    }
    
    /** selects item for cboGenerateJavadoc
     * @param item String item
     */
    public void selectGenerateJavadoc(String item) {
        cboGenerateJavadoc().selectItem(item);
    }
    
    /** types text for cboGenerateJavadoc
     * @param text String text
     */
    public void typeGenerateJavadoc(String text) {
        cboGenerateJavadoc().typeText(text);
    }
    
    /** returns selected item for cboRunProject
     * @return String item
     */
    public String getSelectedRunProject() {
        return cboRunProject().getSelectedItem().toString();
    }
    
    /** selects item for cboRunProject
     * @param item String item
     */
    public void selectRunProject(String item) {
        cboRunProject().selectItem(item);
    }
    
    /** types text for cboRunProject
     * @param text String text
     */
    public void typeRunProject(String text) {
        cboRunProject().typeText(text);
    }
    
    /** returns selected item for cboTestProject
     * @return String item
     */
    public String getSelectedTestProject() {
        return cboTestProject().getSelectedItem().toString();
    }
    
    /** selects item for cboTestProject
     * @param item String item
     */
    public void selectTestProject(String item) {
        cboTestProject().selectItem(item);
    }
    
    /** types text for cboTestProject
     * @param text String text
     */
    public void typeTestProject(String text) {
        cboTestProject().typeText(text);
    }
    
    /** returns selected item for cboDeployProject
     * @return String item
     */
    public String getSelectedDeployProject() {
        return cboDeployProject().getSelectedItem().toString();
    }
    
    /** selects item for cboDeployProject
     * @param item String item
     */
    public void selectDeployProject(String item) {
        cboDeployProject().selectItem(item);
    }
    
    /** types text for cboDeployProject
     * @param text String text
     */
    public void typeDeployProject(String text) {
        cboDeployProject().typeText(text);
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of NewWebFreeFormActionsStepOperator by accessing
     * all its components.
     */
    public void verify() {
        lblBuildProject();
        lblCleanProject();
        lblRunProject();
        lblGenerateJavadoc();
        lblTestProject();
        cboBuildProject();
        cboCleanProject();
        cboGenerateJavadoc();
        cboRunProject();
        cboTestProject();
        cboDeployProject();
        lblDeployProject();
    }
}
