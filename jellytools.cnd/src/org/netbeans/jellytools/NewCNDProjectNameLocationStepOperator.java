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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.jellytools;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import javax.swing.JTextField;


/**
 * Handle "Name And Location" panel of the New Project wizard (CND).
 * Components on the panel differs according to type of project selected. 
 */
public class NewCNDProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //C/C++ Dynamic/Static Library
    private JLabelOperator      _lblProjectName;
    private JTextFieldOperator  _txtProjectName;
    private JLabelOperator      _lblProjectLocation;
    private JTextFieldOperator  _txtProjectLocation;
    private JLabelOperator      _lblProjectFolder;
    private JTextFieldOperator  _txtProjectFolder;
    private JButtonOperator     _btBrowseLocation;
    private JCheckBoxOperator   _cbSetAsMainProject;
    //C/C++ Application
    private JCheckBoxOperator   _cbCreateMainFile;
    private JTextFieldOperator  _txtCreateMainFile;

    //TODO: add functionality for C/C++ project from existing code (or create a new operator)
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectLocation() {
        if(_lblProjectLocation == null) {
            _lblProjectLocation = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                    "LBL_NWP1_ProjectLocation_Label"));
        }
        return _lblProjectLocation;
    }

    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectLocation() {
        if(_txtProjectLocation == null) {
            if ( lblProjectLocation().getLabelFor()!=null ) {
                _txtProjectLocation = new JTextFieldOperator((JTextField)lblProjectLocation().getLabelFor());
            }
        }
        return _txtProjectLocation;
    }
    
    
    /** Returns operator for label Project Name
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectName() {
        if(_lblProjectName == null) {
            _lblProjectName = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                    "LBL_NWP1_ProjectName_Label"));
        }
        return _lblProjectName;
    }

    
    /** Returns operator of project name textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectName() {
        if(_txtProjectName == null) {
            if ( lblProjectName().getLabelFor()!=null ) {
                _txtProjectName = new JTextFieldOperator((JTextField)lblProjectName().getLabelFor());
            }
        }
        return _txtProjectName;
    }
    
    
    /** Returns operator for label Project Folder
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectFolder() {
        if(_lblProjectFolder == null) {
            _lblProjectFolder = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                    "LBL_NWP1_CreatedProjectFolder_Lablel"));
        }
        return _lblProjectFolder;
    }

    
    /** Returns operator of project folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectFolder() {
        if(_txtProjectFolder == null) {
            if ( lblProjectFolder().getLabelFor()!=null ) {
                _txtProjectFolder = new JTextFieldOperator((JTextField)lblProjectFolder().getLabelFor());
            }    
        }
        return _txtProjectFolder;
    }
    
    
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectLocation() {
        if ( _btBrowseLocation==null ) {
             _btBrowseLocation = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                     "LBL_NWP1_BrowseLocation_Button"));
        }
        return _btBrowseLocation;
    }
    
    
    /** Returns operator for checkbox 'Set as Main Project'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSetAsMainProject() {
        if ( _cbSetAsMainProject==null ) {
            _cbSetAsMainProject = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                        "LBL_setAsMainCheckBox"));
        }
        return _cbSetAsMainProject;    
    }
    
    
    /** Returns operator for checkbox 'Create Main File'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCreateMainFile() {
        if ( _cbCreateMainFile==null ) {
            _cbCreateMainFile = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                        "LBL_createMainfile"));
        }
        return _cbCreateMainFile;
    }
    
    
    /** Returns operator for text field 'Create Main File'
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreateMainFile() {
        if ( _txtCreateMainFile==null ) {
            _txtCreateMainFile = new JTextFieldOperator(this, 4);
        }
        return _txtCreateMainFile;
    }
        
    /** Performs verification by accessing all sub-components */    
    @Override
    public void verify() {
        //TODO fill this
    
    }
}
