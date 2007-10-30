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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.common.method.impl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.j2ee.common.method.MethodModel;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizerPanel extends javax.swing.JPanel {

    public static final String NAME = "name";
    public static final String RETURN_TYPE = "returnType";
    public static final String INTERFACES = "interfaces";
    // immutable method prototype
    private final MethodModel methodModel;
    private final ParametersPanel parametersPanel;
    private final ExceptionsPanel exceptionsPanel;
    private final boolean hasInterfaces;
    private final ClasspathInfo cpInfo;

    private MethodCustomizerPanel(MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote, 
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String ejbql, 
            boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces) {
        initComponents();

        this.methodModel = methodModel;
        this.hasInterfaces = hasInterfaces;
        this.cpInfo = cpInfo;

        nameTextField.setText(methodModel.getName());
        returnTypeTextField.setText(methodModel.getReturnType());

        localRadio.setEnabled(hasLocal);
        remoteRadio.setEnabled(hasRemote);
        bothRadio.setEnabled(hasLocal && hasRemote);
        localRadio.setSelected(selectLocal);
        remoteRadio.setSelected(selectRemote && !selectLocal);

        if (!hasReturnType) {
            disableReturnType();
        }
        if (ejbql == null) {
            ejbqlPanel.setVisible(false);
        } else {
            ejbqlTextArea.setText(ejbql);
        }
        cardinalityPanel.setVisible(hasFinderCardinality);
        exceptionsContainerPanel.setVisible(hasExceptions);
        interfacesPanel.setVisible(hasInterfaces);

        parametersPanel = new ParametersPanel(cpInfo, methodModel.getParameters());
        parametersContainerPanel.add(parametersPanel);

        exceptionsPanel = hasExceptions ? new ExceptionsPanel(methodModel.getExceptions(), cpInfo) : null;
        if (hasExceptions) {
            exceptionsContainerPanel.add(exceptionsPanel);
        }

        // listeners
        nameTextField.getDocument().addDocumentListener(new SimpleListener(NAME));
        returnTypeTextField.getDocument().addDocumentListener(new SimpleListener(RETURN_TYPE));
        SimpleListener interfacesListener = new SimpleListener(INTERFACES);
        localRadio.addActionListener(interfacesListener);
        remoteRadio.addActionListener(interfacesListener);
        bothRadio.addActionListener(interfacesListener);
    }

    public static MethodCustomizerPanel create(MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote,
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String  ejbql, 
            boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces) {
        return new MethodCustomizerPanel(methodModel, cpInfo, hasLocal, hasRemote, selectLocal, selectRemote,
                hasReturnType, ejbql, hasFinderCardinality, hasExceptions, hasInterfaces);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        // first validation before any real event is send
        firePropertyChange(NAME, null, null);
        firePropertyChange(RETURN_TYPE, null, null);
        firePropertyChange(INTERFACES, null, null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        finderCardinalityButtonGroup = new javax.swing.ButtonGroup();
        interfaceButtonGroup = new javax.swing.ButtonGroup();
        exceptionAndParameterPane = new javax.swing.JTabbedPane();
        parametersContainerPanel = new javax.swing.JPanel();
        exceptionsContainerPanel = new javax.swing.JPanel();
        errorTextField = new javax.swing.JTextField();
        returnTypeLabel = new javax.swing.JLabel();
        returnTypeTextField = new javax.swing.JTextField();
        nameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        interfacesPanel = new javax.swing.JPanel();
        interfaceLabel = new javax.swing.JLabel();
        localRadio = new javax.swing.JRadioButton();
        remoteRadio = new javax.swing.JRadioButton();
        bothRadio = new javax.swing.JRadioButton();
        cardinalityPanel = new javax.swing.JPanel();
        cardinalityLabel = new javax.swing.JLabel();
        oneRadioButton = new javax.swing.JRadioButton();
        manyRadioButton = new javax.swing.JRadioButton();
        ejbqlPanel = new javax.swing.JPanel();
        ejbqlLabel = new javax.swing.JLabel();
        ejbqlScrollPane = new javax.swing.JScrollPane();
        ejbqlTextArea = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        parametersContainerPanel.setLayout(new java.awt.BorderLayout());
        exceptionAndParameterPane.addTab(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.parametersContainerPanel.TabConstraints.tabTitle"), parametersContainerPanel); // NOI18N

        exceptionsContainerPanel.setLayout(new java.awt.BorderLayout());
        exceptionAndParameterPane.addTab(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.exceptionsPanel.TabConstraints.tabTitle"), exceptionsContainerPanel); // NOI18N

        errorTextField.setBackground(java.awt.SystemColor.control);
        errorTextField.setEditable(false);
        errorTextField.setBorder(null);

        returnTypeLabel.setLabelFor(returnTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(returnTypeLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.returnTypeLabel.text")); // NOI18N

        returnTypeTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.returnTypeTextField.text")); // NOI18N
        returnTypeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                returnTypeTextFieldFocusGained(evt);
            }
        });

        nameTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.nameTextField.text")); // NOI18N
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameTextFieldFocusGained(evt);
            }
        });

        jLabel1.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(interfaceLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.interfaceLabel.text")); // NOI18N

        interfaceButtonGroup.add(localRadio);
        localRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(localRadio, "&Local");
        localRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        interfaceButtonGroup.add(remoteRadio);
        org.openide.awt.Mnemonics.setLocalizedText(remoteRadio, "&Remote");
        remoteRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        interfaceButtonGroup.add(bothRadio);
        org.openide.awt.Mnemonics.setLocalizedText(bothRadio, "&Both");
        bothRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout interfacesPanelLayout = new org.jdesktop.layout.GroupLayout(interfacesPanel);
        interfacesPanel.setLayout(interfacesPanelLayout);
        interfacesPanelLayout.setHorizontalGroup(
            interfacesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(interfacesPanelLayout.createSequentialGroup()
                .add(interfaceLabel)
                .add(18, 18, 18)
                .add(localRadio)
                .add(18, 18, 18)
                .add(remoteRadio)
                .add(18, 18, 18)
                .add(bothRadio)
                .addContainerGap(174, Short.MAX_VALUE))
        );
        interfacesPanelLayout.setVerticalGroup(
            interfacesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(interfacesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(interfaceLabel)
                .add(localRadio)
                .add(remoteRadio)
                .add(bothRadio))
        );

        localRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_LocalRadioButton")); // NOI18N
        remoteRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_RemoteRadioButton")); // NOI18N
        bothRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_BothRadioButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cardinalityLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.cardinalityLabel.text")); // NOI18N

        finderCardinalityButtonGroup.add(oneRadioButton);
        oneRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(oneRadioButton, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.oneRadioButton.text")); // NOI18N
        oneRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        finderCardinalityButtonGroup.add(manyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(manyRadioButton, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.manyRadioButton.text")); // NOI18N
        manyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout cardinalityPanelLayout = new org.jdesktop.layout.GroupLayout(cardinalityPanel);
        cardinalityPanel.setLayout(cardinalityPanelLayout);
        cardinalityPanelLayout.setHorizontalGroup(
            cardinalityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cardinalityPanelLayout.createSequentialGroup()
                .add(cardinalityLabel)
                .add(18, 18, 18)
                .add(oneRadioButton)
                .add(18, 18, 18)
                .add(manyRadioButton)
                .addContainerGap(251, Short.MAX_VALUE))
        );
        cardinalityPanelLayout.setVerticalGroup(
            cardinalityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cardinalityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(cardinalityLabel)
                .add(oneRadioButton)
                .add(manyRadioButton))
        );

        org.openide.awt.Mnemonics.setLocalizedText(ejbqlLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.ejbqlLabel.text")); // NOI18N

        ejbqlScrollPane.setBorder(null);

        ejbqlTextArea.setColumns(20);
        ejbqlTextArea.setRows(5);
        ejbqlTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ejbqlTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ejbqlTextAreaFocusGained(evt);
            }
        });
        ejbqlScrollPane.setViewportView(ejbqlTextArea);

        org.jdesktop.layout.GroupLayout ejbqlPanelLayout = new org.jdesktop.layout.GroupLayout(ejbqlPanel);
        ejbqlPanel.setLayout(ejbqlPanelLayout);
        ejbqlPanelLayout.setHorizontalGroup(
            ejbqlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ejbqlPanelLayout.createSequentialGroup()
                .add(ejbqlLabel)
                .addContainerGap(461, Short.MAX_VALUE))
            .add(ejbqlScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
        );
        ejbqlPanelLayout.setVerticalGroup(
            ejbqlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ejbqlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ejbqlLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ejbqlScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, exceptionAndParameterPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .add(ejbqlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(errorTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cardinalityPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, interfacesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1)
                            .add(returnTypeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(returnTypeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton1))
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(returnTypeLabel)
                    .add(jButton1)
                    .add(returnTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(exceptionAndParameterPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(interfacesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cardinalityPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ejbqlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        returnTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_ReturnType")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_Name")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void nameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFieldFocusGained
        nameTextField.selectAll();
    }//GEN-LAST:event_nameTextFieldFocusGained

    private void returnTypeTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_returnTypeTextFieldFocusGained
        returnTypeTextField.selectAll();
    }//GEN-LAST:event_returnTypeTextFieldFocusGained

    private void ejbqlTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ejbqlTextAreaFocusGained
        ejbqlTextArea.selectAll();
    }//GEN-LAST:event_ejbqlTextAreaFocusGained

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, new TypeElementFinder.Customizer() {
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {                                            
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle != null) {
            returnTypeTextField.setText(handle.getQualifiedName());
        }
    }                                        

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bothRadio;
    private javax.swing.JLabel cardinalityLabel;
    private javax.swing.JPanel cardinalityPanel;
    private javax.swing.JLabel ejbqlLabel;
    private javax.swing.JPanel ejbqlPanel;
    private javax.swing.JScrollPane ejbqlScrollPane;
    private javax.swing.JTextArea ejbqlTextArea;
    private javax.swing.JTextField errorTextField;
    private javax.swing.JTabbedPane exceptionAndParameterPane;
    private javax.swing.JPanel exceptionsContainerPanel;
    private javax.swing.ButtonGroup finderCardinalityButtonGroup;
    private javax.swing.ButtonGroup interfaceButtonGroup;
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JPanel interfacesPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton localRadio;
    private javax.swing.JRadioButton manyRadioButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton oneRadioButton;
    private javax.swing.JPanel parametersContainerPanel;
    private javax.swing.JRadioButton remoteRadio;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JTextField returnTypeTextField;
    // End of variables declaration//GEN-END:variables

    public void setError(String message) {
        setErrorFieldColor(true);
        errorTextField.setText(message);
    }

    public void setWarning(String message) {
        setErrorFieldColor(false);
        errorTextField.setText(message);
    }

    public String getMethodName() {
        return nameTextField.getText().trim();
    }

    public String getReturnType() {
        return returnTypeTextField.getText().trim();
    }

    public List<MethodModel.Variable> getParameters() {
        return parametersPanel.getParameters();
    }

    public List<String> getExceptions() {
        List<String> result = new ArrayList<String>();
        if (exceptionsPanel != null) {
            for (String exception : exceptionsPanel.getExceptions()) {
                if (!"".equals(exception.trim())) {
                    result.add(exception);
                }
            }
        }
        return result;
    }

    public Set<Modifier> getModifiers() {
        // not changing?
        return methodModel.getModifiers();
    }

    public String getMethodBody() {
        // not changing?
        return methodModel.getBody();
    }

    public boolean supportsInterfacesChecking() {
        return hasInterfaces;
    }

    public boolean hasLocal() {
        return (localRadio.isEnabled() && localRadio.isSelected()) || hasBothInterfaces();
    }

    public boolean hasRemote() {
        return (remoteRadio.isEnabled() && remoteRadio.isSelected()) || hasBothInterfaces();
    }

    public String getEjbql() {
        if (ejbqlTextArea != null) {
            return ejbqlTextArea.getText().trim();
        }
        return null;
    }

    public boolean finderReturnIsSingle() {
        return oneRadioButton != null ? oneRadioButton.isSelected() : false;
    }

    private boolean hasBothInterfaces() {
        return localRadio.isEnabled() && remoteRadio.isEnabled() && bothRadio.isSelected();
    }

    private void disableReturnType() {
        returnTypeLabel.setVisible(false);
        returnTypeTextField.setVisible(false);
    }

    private void setErrorFieldColor(boolean error) {
        if (error) {
            Color color = UIManager.getColor("nb.errorForeground"); //NOI18N
            errorTextField.setForeground(color == null ? new Color(89, 79, 191) : color);
        } else {
            Color color = UIManager.getColor("nb.warningForeground"); //NOI18N
            errorTextField.setForeground(color == null ? Color.DARK_GRAY : color);
        }
    }

/**
     * Listener on text fields.
     * Fires change event for specified property of this JPanel,
     * old and new value of event is null.
     * After receiving event, client can get property value by
     * calling {@link #getProperty(String)}
     */
    private class SimpleListener implements DocumentListener, ActionListener {

        private final String propertyName;

        public SimpleListener(String propertyName) {
            this.propertyName = propertyName;
        }

        public void insertUpdate(DocumentEvent documentEvent) {
            fire();
        }

        public void removeUpdate(DocumentEvent documentEvent) {
            fire();
        }

        public void changedUpdate(DocumentEvent documentEvent) {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            fire();
        }

        private void fire() {
            firePropertyChange(propertyName, null, null);
        }
        
    }
    
}