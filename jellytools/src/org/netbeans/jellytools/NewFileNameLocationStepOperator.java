/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import javax.swing.JTextField;

/**
 * Handle "Name And Location" panel of the New File wizard.
 * Components on the panel differs according to type of Object selected.
 * This one contains only basic components.<br>
 * Usage:
 * <pre>
 *      NewFileWizardOperator wop = NewFileWizardOperator.invoke();
 *      wop.selectCategory("Java Classes");
 *      wop.selectFileType("Java Class");
 *      wop.next();
 *      NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
 *      op.selectLocation("Source Packages");
 *      op.selectPackage("org.netbeans.jellytools");
 * </pre>
 *
 * @author tb115823, Jiri.Skrivanek@sun.com
 */
public class NewFileNameLocationStepOperator extends NewFileWizardOperator {
    
    /** Components operators. */
    private JLabelOperator      _lblObjectName;
    private JTextFieldOperator  _txtObjectName;
    private JLabelOperator      _lblProject;
    private JTextFieldOperator  _txtProject;
    private JLabelOperator      _lblCreatedFile;
    private JTextFieldOperator  _txtCreatedFile;
    private JComboBoxOperator   _cboPackage;
    private JComboBoxOperator   _cboLocation;
     
    /** Returns operator for first label with "Name"
     * @return JLabelOperator
     */
    public JLabelOperator lblObjectName() {
        if(_lblObjectName == null) {
            _lblObjectName = new JLabelOperator(this,2);
        }
        return _lblObjectName;
    }

    
    /** Returns operator of text field bind to lblObjectName
     * @return JTextOperator
     */
    public JTextFieldOperator txtObjectName() {
        if( _txtObjectName==null ) {
            if ( lblObjectName().getLabelFor()!=null ) {
                _txtObjectName = new JTextFieldOperator((JTextField)lblObjectName().getLabelFor());
            } else {
                _txtObjectName = new JTextFieldOperator(this,0);
            }
        }
        return _txtObjectName;
    }
    
    /** Returns operator for first label with "Project"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if(_lblProject == null) {
            _lblProject = new JLabelOperator(this,Bundle.getString("org.netbeans.modules.java.project.Bundle.properties","LBL_JavaTargetChooser_PanelGUI_jLabel5"));
        }
        return _lblProject;
    }

    
    /** Returns operator of text field bind to lblProject
     * @return JTextOperator
     */
    public JTextFieldOperator txtProject() {
        if( _txtProject==null ) {
            if ( lblProject().getLabelFor()!=null ) {
                _txtProject = new JTextFieldOperator((JTextField)lblProject().getLabelFor());
            } else {
                _txtProject = new JTextFieldOperator(this,1);
            }
        }
        return _txtProject;
    }
    
    /** Returns operator for label with "Created File:"
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if(_lblCreatedFile == null) {
            _lblCreatedFile = new JLabelOperator(this,Bundle.getString("org.netbeans.modules.java.project.Bundle.properties","LBL_JavaTargetChooser_CreatedFile_Label"));
        }
        return _lblCreatedFile;
    }

    /** Returns operator of text field bind to lblCreatedFile
     * @return JTextOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if( _txtCreatedFile==null ) {
            if ( lblCreatedFile().getLabelFor()!=null ) {
                _txtCreatedFile = new JTextFieldOperator((JTextField)lblCreatedFile().getLabelFor());
            } else {
                _txtCreatedFile = new JTextFieldOperator(this,3);
            }
        }
        return _txtCreatedFile;
    }
    
    /** Returns operator for combo box Location:
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if ( _cboLocation==null ) {
            _cboLocation = new JComboBoxOperator(this,0);
        }
        return _cboLocation;
    }
    
    
    /** returns operator for combo box Packages:
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboPackage() {
        if ( _cboPackage==null ) {
            _cboPackage = new JComboBoxOperator(this,1);
        }
        return _cboPackage;
    }
    
    /** Selects location in combo box Location: */
    public void selectLocation(String location) {
        cboLocation().selectItem(location);
    }
    
    /** Selects given package in combo box Package.
     * @param packageName name of package to be selected
     */
    public void selectPackage(String packageName) {
        cboPackage().selectItem(packageName);
    }
    
    /** Type given package in combo box Package.
     * @param packageName name of package
     */
    public void setPackage(String packageName) {
        cboPackage().typeText(packageName);
    }

    /** Sets given object name in the text field.
     * @param objectName name of object
     */
    public void setObjectName(String objectName) {
        txtObjectName().setText(objectName);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblObjectName();
        txtObjectName();
        lblCreatedFile();
        txtCreatedFile();
        cboLocation();
        cboPackage();
    }
}
