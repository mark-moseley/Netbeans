/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring.operators;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class FindUsagesClassOperator extends ParametersPanelOperator {

    public FindUsagesClassOperator() {
        super(java.util.ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_FindUsagesDialog"));
    }
    private JButtonOperator find;
    private JButtonOperator cancel;
    private JLabelOperator label;
    private JCheckBoxOperator searchInComments;
    private JRadioButtonOperator findUsages;    
    private JRadioButtonOperator findDirectSubtypes;
    private JRadioButtonOperator findAllSubtypes;
    private JCheckBoxOperator findOverridding;
    private JCheckBoxOperator findFromBaseClass;
    private JCheckBoxOperator findMethodUsage;
    
    private JComboBoxOperator scope;
    
    public JButtonOperator getFind() {
        if (find == null) {
            find = new JButtonOperator(this, "Find");
        }
        return find;
    }
    
    public JButtonOperator getCancel() {
        if (cancel == null) {
            cancel = new JButtonOperator(this, "Cancel");
        }
        return cancel;
    }

    public JLabelOperator getLabel() {
        if (label == null) {
            label = new JLabelOperator(this);
        }
        return label;

    }

    public JRadioButtonOperator getFindAllSubtypes() {
        if (findAllSubtypes == null) {
            findAllSubtypes = new JRadioButtonOperator(this, getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_FindAllSubtypes"));
        }
        return findAllSubtypes;
    }

    public JRadioButtonOperator getFindDirectSubtypes() {
        if (findDirectSubtypes == null) {
            findDirectSubtypes = new JRadioButtonOperator(this,  getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_FindDirectSubtypesOnly"));
        }
        return findDirectSubtypes;
    }

    public JRadioButtonOperator getFindUsages() {
        if (findUsages == null) {
            findUsages = new JRadioButtonOperator(this,  getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_FindUsages"));
        }
        return findUsages;
    }

    public JCheckBoxOperator getSearchInComments() {
        if (searchInComments == null) {
            searchInComments = new JCheckBoxOperator(this,  getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_SearchInComents"));
        }
        return searchInComments;
    }

    public JComboBoxOperator getScope() {
        if (scope == null) {
            scope = new JComboBoxOperator(this);
        }
        return scope;
    }

    public JCheckBoxOperator getFindFromBaseClass() {
        if (findFromBaseClass == null) {
            findFromBaseClass = new JCheckBoxOperator(this,  getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","DSC_WhereUsedFromBaseClass"));
        }
        return findFromBaseClass;
    }

    public JCheckBoxOperator getFindMethodUsage() {
        if (findMethodUsage == null) {
            findMethodUsage = new JCheckBoxOperator(this,  getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_FindUsages"));
        }
        return findMethodUsage;
    }

    public JCheckBoxOperator getFindOverridding() {
        if (findOverridding == null) {
            findOverridding = new JCheckBoxOperator(this,  getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","DSC_WhereUsedMethodOverriders"));
        }
        return findOverridding;
    }

    /**
     * Select the scope
     * @param projectName The name of project or null if find should be performed on all projects
     */
    public void setScope(String projectName) {        
        JComboBoxOperator scopeOperator = getScope();
        if(projectName == null) {                        
            ComboBoxModel model = scopeOperator.getModel();            
            scopeOperator.selectItem(getBungleText("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_AllProjects"));
        }
        else scopeOperator.selectItem(projectName);
    }
}
