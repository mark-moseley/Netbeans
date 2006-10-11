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

import javax.swing.JDialog;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import javax.swing.tree.TreePath;

/**
 * Handle NetBeans New File wizard.
 * It is invoked either from main menu File -> New File...
 * <code>NewFileAction.performMenu();</code>
   or from popup menu on folder <code>NewFileAction.performPopup();</code><br>
 * Usage:
 *
 * <pre>
 * NewFileWizardOperator op = NewFileWizardOperator.invoke();
 * op.selectCategory("Java Classes");
 * op.selectFileType("Java Class");
 * </pre>
 *
 * @author tb115823
 */
public class NewFileWizardOperator extends WizardOperator {

    private JLabelOperator      _lblProject;
    private JLabelOperator      _lblCategories;
    private JLabelOperator      _lblFileTypes;
    private JTreeOperator       _treeCategories;
    private JListOperator       _lstFileTypes;
    private JLabelOperator      _lblDescription;
    private JEditorPaneOperator _txtDescription;
    private JComboBoxOperator   _cboProject;
    
    
    /** Creates new NewFileWizardOperator that can handle it.
     */
    public NewFileWizardOperator() {
        super(Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Subtitle"));
    }

    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewFileWizardOperator(String title) {
        super(title);
    }
    
    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke() {
        new NewFileAction().perform();
        return new NewFileWizardOperator();
    }

    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @param initial wizard title
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke(String title) {
        new NewFileAction().perform();
        return new NewFileWizardOperator(title);
    }

    /** Selects specified node and invokes new file wizard by default action.
     * In "Choose File Type" wizard's page it selects given category and filetype.
     * It returns instance of NewFileWizardOperator representing "Name and Location"
     * page of the wizard.
     * @param node node which should be selected before new file wizard is invoked
     * @param category category to be selected
     * @param filetype file type to be selected (exact name - not substring)
     * @return instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke(Node node, String category, String filetype) {
        new NewFileAction().perform(node);
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = new NewFileWizardOperator(wizardTitle);
        nfwo.selectCategory(category);
        nfwo.selectFileType(filetype);
        nfwo.next();
        return new NewFileWizardOperator();
    }
    
    /** Creates a new object from template. It invokes new file wizard, 
     * sets given project, category and file type. On the next panel it
     * sets package and object name. If package name is null or empty, it lets
     * the default one.
     * @param projectName name of project in which new object should be created
     * @param category category to be selected
     * @param fileType file type to be selected
     * @param packageName package name of new object
     * @param name name of created object
     */
    public static void create(String projectName, String category, String fileType, String packageName, String name) {
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.selectCategory(category);
        nfwo.selectFileType(fileType);
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.setObjectName(name);
        if(packageName != null && !"".equals(packageName)) {
            nfnlso.setPackage(packageName);
        }
        nfnlso.finish();
    }
    
    /** Select given project in combobox of projects
     *  @param project name of project
     */
    public void selectProject(String project) {
        cboProject().selectItem(project);
    }
    
    
    /** Selects given project category
     * @param category name of the category to select
     */
    public void selectCategory(String category) {
        // we need to wait until some node is selected because 'please, wait' node
        // is shown before tree is initialized. Then we can change selection.
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    return treeCategories().isSelectionEmpty() ? null: Boolean.TRUE;
                }
                public String getDescription() {
                    return("Wait node is selected");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        } catch(TimeoutExpiredException tee) {
            // ignore it because sometimes can happen that no category is selected by default
        }
        new Node(treeCategories(), category).select();
    }
    
    /** Selects given file type
     * @param filetype name of file type to select (exact name - not substring)
     */
    public void selectFileType(String filetype) {
        lstFileTypes().selectItem(filetype);
    }
    
    
    /** Tries to find "Project:"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Project"));
        }
        return _lblCategories;
    }
    
    /** Tries to find "Categories:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCategories() {
        if (_lblCategories==null) {
            _lblCategories = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Categories"));
        }
        return _lblCategories;
    }

    /** Tries to find "Projects:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFileTypes() {
        if (_lblFileTypes==null) {
            _lblFileTypes = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_FileTypes"));
        }
        return _lblFileTypes;
    }

    /** Tries to find JComboBox Project
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboProject() {
        if (_cboProject==null) {
            _cboProject = new JComboBoxOperator(this);
        }
        return _cboProject;
    }
    
    /** returns selected item for cboProject
     * @return selected project
     */
    public String getSelectedProject() {
        return cboProject().getSelectedItem().toString();
    }
    
    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeCategories() {
        if (_treeCategories==null) {
            _treeCategories = new JTreeOperator(this);
        }
        return _treeCategories;
    }
    
    /** returns selected path in treeCategories
     * @return TreePath
     */
    public TreePath getSelectedCategory() {
        return treeCategories().getSelectionPath();
    }

    /** Tries to find FileTypes ListView in this dialog.
     * @return JListOperator
     */
    public JListOperator lstFileTypes() {
        if (_lstFileTypes==null) {
            _lstFileTypes = new JListOperator(this, 1);
            // set exact comparator because Java Classes has types 'Java Package Info'
            // and 'Java Package'.
            _lstFileTypes.setComparator(new Operator.DefaultStringComparator(true, true));
        }
        return _lstFileTypes;
    }

    
    /** returns selected item in lstFileType
     * @return String selected file type
     */
    public String getSelectedFileType() {
        return lstFileTypes().getSelectedValue().toString();
    }
    
    /** Tries to find "Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescription() {
        if (_lblDescription==null) {
            _lblDescription = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Description"));
        }
        return _lblDescription;
    }

    
    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtDescription() {
        if (_txtDescription==null) {
            _txtDescription = new JEditorPaneOperator(this);
        }
        return _txtDescription;
    }


    
    /** gets text for txtDescription
     * @return String text
     */
    public String getDescription() {
        return txtDescription().getText();
    }

    

    /** Performs verification of NewFileWizardOperator by accessing all its components.
     */
    public void verify() {
        lblCategories();
        lblFileTypes();
        cboProject();
        treeCategories();
        lstFileTypes();
        lblDescription();
        txtDescription();
    }
}

