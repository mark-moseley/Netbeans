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
package org.netbeans.modules.bpel.properties.choosers;

import java.awt.Component;
import java.awt.Container;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.VirtualVariableContainer;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.form.ChooserLifeCycle;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class NewMessageVarChooser extends JPanel
        implements ChooserLifeCycle<VirtualVariableContainer>,
        Validator.Provider, CustomNodeEditor.Owner {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor myEditor;
    private DefaultValidator myValidator;
    private Message myVarType;
    
    /**
     * Keeps ths BPEL element from which it's initiated the creation of a new variable
     */
    private BpelEntity myTargetEntity;
    private String myTypeOwnerName;
    private Constants.MessageDirection myMsgDirection;
    
    public NewMessageVarChooser(BpelEntity targetEntity, String typeOwnerName,
            Message message, Constants.MessageDirection msgDirection) {
        this.myTargetEntity = targetEntity;
        this.myTypeOwnerName = typeOwnerName;
        this.myVarType = message;
        
        this.myMsgDirection = msgDirection;
        createContent();
    }
    
    public void createContent() {
        initComponents();
        //
        cbxScope.setRenderer(new DefaultListCellRenderer() {
            static final long serialVersionUID = 1L;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null && value instanceof NamedElement) {
                    String name = ((NamedElement)value).getName();
                    setText(name);
                    // setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
                }
                return this;
            }
        });
        //
        Util.activateInlineMnemonics(this);
    }
    
    public boolean initControls() {
        String varName = calculateMessageVarName();
        if (varName != null) {
            fldVariableName.setText(varName);
        }
        //
        if (myVarType != null) {
            String name = ResolverUtility.getDisplayName(myVarType, myTargetEntity);
            myTargetEntity.getNamespaceContext();
            fldVariableType.setText(name);
        }
        //
        // Populate scope combo-box
        FindHelper helper =
                (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
        if (helper != null) {
            Iterator<BaseScope> scopeIterator = helper.scopeIterator(myTargetEntity);
            //
            Vector<BaseScope> scopeVector = new Vector<BaseScope>();
            while (scopeIterator.hasNext()) {
                BaseScope baseScope = scopeIterator.next();
                scopeVector.add(baseScope);
            }
            //
            DefaultComboBoxModel model = new DefaultComboBoxModel(scopeVector);
            cbxScope.setModel(model);
            cbxScope.setSelectedIndex(0);
        }
        //
        return true;
    }
    
    private String calculateMessageVarName() {
        String nameCandidate = null;
        String namePrefix = null;
        String ownerName = myTypeOwnerName;
        ownerName = capitalizeFirstLetter(ownerName);
        if (ownerName == null || ownerName.length() == 0) {
            nameCandidate = "NewVariable"; // NOI18N
        } else {
            switch (myMsgDirection) {
                case INPUT:
                    namePrefix = ownerName + "In"; // NOI18N
                    break;
                case OUTPUT:
                    namePrefix = ownerName + "Out"; // NOI18N
                    break;
                case FAULT:
                    String suffix = "Fault"; // NOI18N
                    String currSuffix = ownerName.substring(
                            ownerName.length() - suffix.length());
                    if (currSuffix.equalsIgnoreCase(suffix)) {
                        namePrefix = ownerName + "Var"; // NOI18N
                    } else {
                        namePrefix = ownerName + suffix + "Var"; // NOI18N
                    }
                    break;
            }
        }
        //
        // Calculate unique suffix
        for (int counter = 0; counter < 1000; counter++) {
            if (counter == 0) {
                nameCandidate = namePrefix;
            } else {
                nameCandidate = namePrefix + counter;
            }
            //
            boolean sameNameFound = false;
            Iterator<BaseScope> scopeIterator = getScopeIterator();
            if (scopeIterator != null) {
                scope: while (scopeIterator.hasNext()) {
                    BaseScope baseScope = scopeIterator.next();
                    VariableContainer vc = baseScope.getVariableContainer();
                    if (vc != null) {
                        Variable[] varArr = vc.getVariables();
                        for (Variable var : varArr) {
                            if (nameCandidate.equals(var.getName())) {
                                sameNameFound = true;
                                break scope;
                            }
                        }
                    }
                }
            }
            //
            if (!sameNameFound) {
                break;
            }
        }
        //
        return nameCandidate;
    }
    
    private String capitalizeFirstLetter(final String operationName) {
        String firstLetter = operationName.substring(0, 1);
        String theRest = operationName.substring(1);
        String namePrefix = firstLetter.toUpperCase() + theRest;
        return namePrefix;
    }
    
    private Iterator<BaseScope> getScopeIterator() {
        FindHelper helper =
                (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
        if (helper != null) {
            Iterator<BaseScope> iterator = helper.scopeIterator(myTargetEntity);
            return iterator;
        }
        return null;
    }
    
    public CustomNodeEditor getParentEditor() {
        while(true) {
            Container parent = this.getParent();
            if (parent == null) break;
            if (parent instanceof CustomNodeEditor) {
                return (CustomNodeEditor)parent;
            }
        }
        return null;
    }
    
    public BaseScope getTargetScope() {
        return (BaseScope)cbxScope.getSelectedItem();
    }
    
    public VirtualVariableContainer getSelectedValue() {
        String varName = fldVariableName.getText();
        TypeContainer varType = new TypeContainer(myVarType);
        BaseScope varScope = (BaseScope)cbxScope.getSelectedItem();
        //
        return new VirtualVariableContainer(varName, varType, varScope);
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null && myEditor != null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public boolean doFastValidation() {
                    Object item = cbxScope.getSelectedItem();
                    if (item == null) {
                        addReasonKey("ERR_SCOPE_NOT_SPECIFIED"); // NOI18N
                        return false;
                    } else {
                        return true;
                    }
                }
                
                public boolean doDetailedValidation() {
                    boolean isValid = super.doDetailedValidation();
                    //
                    // Check that the variable name is unique
                    BaseScope scope = (BaseScope)cbxScope.getSelectedItem();
                    if (scope != null) {
                        VariableContainer vc = scope.getVariableContainer();
                        if (vc != null) {
                            String varName = fldVariableName.getText();
                            Variable[] variables = vc.getVariables();
                            for (Variable variable : variables) {
                                if (varName.equals( variable.getName())){
                                    addReasonKey("ERR_NOT_UNIQUE_VARIABLE_NAME"); //NOI18N
                                    isValid = false;
                                }
                            }
                        }
                    }
                    //
                    return isValid;
                }
                
            };
        }
        return myValidator;
    }
    
    public void setEditor(CustomNodeEditor editor) {
        myEditor = editor;
    }
    
    public CustomNodeEditor getEditor() {
        return myEditor;
    }
    
    public boolean unsubscribeListeners() {
        return true;
    }
    
    public boolean subscribeListeners() {
        return true;
    }
    
    public boolean afterClose() {
        return true;
    }
    
    public void setSelectedValue(VirtualVariableContainer newValue) {
        // DO NOTHING HERE BECAUSE OF THE ASSIGNMENT ISN'T SUPPORTED
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblVariableName = new javax.swing.JLabel();
        lblType = new javax.swing.JLabel();
        fldVariableName = new javax.swing.JTextField();
        fldVariableType = new javax.swing.JTextField();
        lblScope = new javax.swing.JLabel();
        cbxScope = new javax.swing.JComboBox();

        lblVariableName.setLabelFor(fldVariableName);
        lblVariableName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_VariableName"));
        lblVariableName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_VariableName"));
        lblVariableName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_VariableName"));

        lblType.setLabelFor(fldVariableType);
        lblType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Type"));
        lblType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Type"));
        lblType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Type"));

        fldVariableName.setColumns(30);

        fldVariableType.setColumns(30);
        fldVariableType.setEditable(false);

        lblScope.setLabelFor(cbxScope);
        lblScope.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Scope"));
        lblScope.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Scope"));
        lblScope.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Scope"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, lblType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, lblVariableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(lblScope))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cbxScope, 0, 267, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fldVariableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldVariableType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fldVariableName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblVariableName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblType)
                    .add(fldVariableType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbxScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblScope))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbxScope;
    private javax.swing.JTextField fldVariableName;
    private javax.swing.JTextField fldVariableType;
    private javax.swing.JLabel lblScope;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblVariableName;
    // End of variables declaration//GEN-END:variables
}
