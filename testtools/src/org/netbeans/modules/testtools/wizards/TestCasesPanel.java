/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * TestCasesPanel.java
 *
 * Created on April 10, 2002, 1:46 PM
 */

import java.util.Vector;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.src.MethodElement;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/** Wizard Panel with Test Cases configuration
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestCasesPanel extends JPanel {
    
    public final Panel panel = new Panel();
    
    private class Panel extends Object implements WizardDescriptor.FinishPanel {
    
        /** adds ChangeListener of current Panel
         * @param changeListener ChangeListener */    
        public void addChangeListener(ChangeListener changeListener) {}    

        /** returns current Panel
         * @return Component */    
        public Component getComponent() {
            return TestCasesPanel.this;
        }    

        /** returns Help Context
         * @return HelpCtx */    
        public HelpCtx getHelp() {
            return new HelpCtx(TestCasesPanel.class);
        }

        /** read settings from given Object
         * @param obj TemplateWizard with settings */    
        public void readSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            if (set.methods==null) {
                testCaseTypes.setModel(new DefaultComboBoxModel(set.templateMethods));
                listData.removeAllElements();
                testCases.setListData(listData);
            }
            refreshAdd();
            refreshButtons();
        }

        /** removes Change Listener of current Panel
         * @param changeListener ChangeListener */    
        public void removeChangeListener(ChangeListener changeListener) {
        }

        /** stores settings to given Object
         * @param obj TemplateWizard with settings */    
        public void storeSettings(Object obj) {
            WizardSettings.get(obj).methods=(WizardIterator.CaseElement[])listData.toArray(new WizardIterator.CaseElement[listData.size()]);
        }

        /** test current Panel state for data validity
         * @return boolean true if data are valid and Wizard can continue */    
        public boolean isValid() {
            return true;
        }
    }
    
    private Vector listData;
    static final long serialVersionUID = 981620069379306317L;
    
    /** Creates new form TestCasesPanel */
    public TestCasesPanel() {
        setName(NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesCreate")); // NOI18N
        initComponents();
        caseName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {refreshAdd();}
            public void removeUpdate(DocumentEvent e) {refreshAdd();}
            public void changedUpdate(DocumentEvent e) {refreshAdd();}
        });
        testCaseTypes.setRenderer(new WizardIterator.MyCellRenderer());
        testCases.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {refreshButtons();}
        });
        listData=new Vector();
        testCases.setListData(listData);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        testCaseTypes = new javax.swing.JComboBox();
        caseName = new javax.swing.JTextField();
        add = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        up = new javax.swing.JButton();
        down = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        testCases = new javax.swing.JList();
        nameLabel = new javax.swing.JLabel();
        templateLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        separator2 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        testCaseTypes.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseTemplate", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(testCaseTypes, gridBagConstraints);

        caseName.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesSampleMethodName"));
        caseName.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseName", new Object[] {}));
        caseName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                caseNameActionPerformed(evt);
            }
        });

        caseName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                caseNameFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(caseName, gridBagConstraints);

        add.setMnemonic(NbBundle.getMessage(TestCasesPanel.class, "MNM_TestCasesAdd").charAt(0));
        add.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesAdd"));
        add.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseAdd", new Object[] {}));
        add.setMinimumSize(new java.awt.Dimension(80, 27));
        add.setPreferredSize(new java.awt.Dimension(80, 27));
        add.setEnabled(false);
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(add, gridBagConstraints);

        remove.setMnemonic(NbBundle.getMessage(TestCasesPanel.class, "MNM_TestCasesRemove").charAt(0));
        remove.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesRemove"));
        remove.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseRemove", new Object[] {}));
        remove.setMinimumSize(new java.awt.Dimension(80, 27));
        remove.setPreferredSize(new java.awt.Dimension(80, 27));
        remove.setEnabled(false);
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(remove, gridBagConstraints);

        up.setMnemonic(NbBundle.getMessage(TestCasesPanel.class, "MNM_TestCasesUp").charAt(0));
        up.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesUp"));
        up.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseUp", new Object[] {}));
        up.setMinimumSize(new java.awt.Dimension(80, 27));
        up.setPreferredSize(new java.awt.Dimension(80, 27));
        up.setEnabled(false);
        up.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(17, 11, 0, 11);
        add(up, gridBagConstraints);

        down.setMnemonic(NbBundle.getMessage(TestCasesPanel.class, "MNM_TestCasesDown").charAt(0));
        down.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesDown"));
        down.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseDown", new Object[] {}));
        down.setMinimumSize(new java.awt.Dimension(80, 27));
        down.setPreferredSize(new java.awt.Dimension(80, 27));
        down.setEnabled(false);
        down.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        add(down, gridBagConstraints);

        testCases.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        testCases.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseList", new Object[] {}));
        scroll.setViewportView(testCases);
        testCases.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCaseList", new Object[] {}));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 0);
        add(scroll, gridBagConstraints);

        nameLabel.setDisplayedMnemonic(NbBundle.getMessage(TestCasesPanel.class, "MNM_TestCasesName").charAt(0));
        nameLabel.setLabelFor(caseName);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesName"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseName", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        templateLabel.setDisplayedMnemonic(NbBundle.getMessage(TestCasesPanel.class, "MNM_TestCasesTemplate").charAt(0));
        templateLabel.setLabelFor(testCaseTypes);
        templateLabel.setText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "LBL_TestCasesTemplate"));
        templateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestCasesPanel.class, "TTT_TestCaseTemplate", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(templateLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 0, 11);
        add(separator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        add(separator2, gridBagConstraints);

    }//GEN-END:initComponents

    private void caseNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_caseNameActionPerformed
        if (add.isEnabled())
            addActionPerformed(evt);
    }//GEN-LAST:event_caseNameActionPerformed

    private void caseNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_caseNameFocusGained
        caseName.selectAll();
    }//GEN-LAST:event_caseNameFocusGained

    private void downActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downActionPerformed
        int index=testCases.getSelectedIndex();
        listData.add(index+1,listData.remove(index));
        testCases.setListData(listData);
        testCases.setSelectedIndex(index+1);
    }//GEN-LAST:event_downActionPerformed

    private void upActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upActionPerformed
        int index=testCases.getSelectedIndex();
        listData.add(index-1,listData.remove(index));
        testCases.setListData(listData);
        testCases.setSelectedIndex(index-1);
    }//GEN-LAST:event_upActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        int index=testCases.getSelectedIndex();
        listData.remove(index);
        testCases.setListData(listData);
        if (index>=listData.size())
            index--;
        if (index>=0)
            testCases.setSelectedIndex(index);
        refreshAdd();
    }//GEN-LAST:event_removeActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        listData.add(new WizardIterator.CaseElement(caseName.getText(),(MethodElement)testCaseTypes.getSelectedItem()));
        testCases.setListData(listData);
        testCases.setSelectedIndex(listData.size()-1);
        refreshAdd();
    }//GEN-LAST:event_addActionPerformed
    
    private void refreshAdd() {
        String name=caseName.getText();
        boolean b=true;
        for (int i=0; b&&i<listData.size(); i++)
            b=!name.equals(((WizardIterator.CaseElement)listData.get(i)).getName());
        add.setEnabled(Utilities.isJavaIdentifier(name)&&b);
    }
    
    private void refreshButtons() {
        int index=testCases.getSelectedIndex();
        remove.setEnabled(index>-1);
        up.setEnabled(index>0);
        down.setEnabled((index>-1)&&(index<listData.size()-1));
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox testCaseTypes;
    private javax.swing.JSeparator separator2;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton down;
    private javax.swing.JButton add;
    private javax.swing.JButton up;
    private javax.swing.JTextField caseName;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JList testCases;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JButton remove;
    // End of variables declaration//GEN-END:variables

}

