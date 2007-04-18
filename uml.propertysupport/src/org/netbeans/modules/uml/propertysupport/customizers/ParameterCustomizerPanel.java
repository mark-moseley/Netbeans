/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * ParameterCustomizerTemp.java
 *
 * Created on August 10, 2005, 2:47 PM
 */

package org.netbeans.modules.uml.propertysupport.customizers;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import javax.swing.JButton;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import org.openide.util.NbBundle;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder.ValidValues;
import java.awt.Color;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;


/**
 *
 * @author  thuy
 */
public class ParameterCustomizerPanel extends javax.swing.JPanel {
    private ResourceBundle bundle = NbBundle.getBundle(ParameterCustomizerPanel.class);
    public ListSelectionModel listSelModel;
    private DefaultListModel listModel;
    //private ArrayList<ElementData> paramArrayList = new ArrayList <ElementData>();
    private ArrayList <ElementData> removedElements = new ArrayList();
    private IPropertyElement parentElement;
    private IPropertyDefinition rootDef;
    
    /**
     * Creates new form ParameterCustomizerTemp
     */
    public ParameterCustomizerPanel() {
        initComponents();
        
        // resize buttons to have their sizes the same as the size of the widest button.
        JButton buttonGrp[] = {moveDwnButton, moveUpButton};
        Dimension preferredSize = getMaxButtonWidth(buttonGrp);
        moveUpButton.setPreferredSize(preferredSize);
        moveDwnButton.setPreferredSize(preferredSize);
        
        JButton buttonGrp2[] = {addButton, editParamButton, removeButton};
        preferredSize = getMaxButtonWidth(buttonGrp2);
        addButton.setPreferredSize(preferredSize);
        editParamButton.setPreferredSize(preferredSize);
        removeButton.setPreferredSize(preferredSize);
        
        listSelModel = paramList.getSelectionModel();
        listSelModel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                selectionChangedHandler(evt);
            }
        });
        
        paramName.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateName();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateName();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateName();
            }
            
        });
        
        // create mutiplicty table and its data model
        Vector tableHeaders = new Vector(3);
        tableHeaders.add(bundle.getString("LOWER"));
        tableHeaders.add(bundle.getString("UPPER"));
        
        IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
        String colName = translator.translateWord("PSK_COLLECTION_OVERRIDE_DATA_TYPE");
        tableHeaders.add(colName);
        TableModel tableModel = new  MultiplicityTableModel(tableHeaders);
        multiplicityTable = new JTable(tableModel);
        
        multiplicityTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        tableScrollPane.setViewportView(multiplicityTable);
        
        // set foreground color for messageArea
        Color c = javax.swing.UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            c = new Color(89,79,191);
        }
        messageArea.setForeground(c);
        messageArea.setFont(this.getFont());
    }
    
    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(final JTable table) {
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                MultiplicityTableModel model = (MultiplicityTableModel)table.getModel();
                TableColumn column = null;
                Component comp = null;
                int headerWidth = 0;
                int cellWidth = 0;
        //        Object[] longValues = model.longValues;
                TableCellRenderer headerRenderer =
                    table.getTableHeader().getDefaultRenderer();

                for (int i = 0; i < 2; i++) {
                    column = table.getColumnModel().getColumn(i);

                    comp = headerRenderer.getTableCellRendererComponent(
                                         null, column.getHeaderValue(),
                                         false, false, 0, 0);
                    headerWidth = comp.getPreferredSize().width;

        //            comp = table.getDefaultRenderer(model.getColumnClass(i)).
        //                             getTableCellRendererComponent(
        //                                 table, longValues[i],
        //                                 false, false, 0, i);
        //            cellWidth = comp.getPreferredSize().width;

        //            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
                    System.out.println("Max Width = " + headerWidth);
                    column.setPreferredWidth(headerWidth + 10);
        //            column.setMaxWidth(headerWidth + 10);
                }
            }
        });
    }
    
    public void setRootProp(IPropertyElement element, IPropertyDefinition def) {
        parentElement = element;
        rootDef = def;
    }
    
    public void setParamList(ArrayList<ElementData> paramArrayList) {
        //paramArrayList = paramArray;
        initalizeParamList(paramArrayList);
        populateParameterData();
    }
    
    private void initalizeParamList(ArrayList<ElementData> paramArrayList) {
        listModel = new DefaultListModel();
        if (paramArrayList != null) {
            for (ElementData param : paramArrayList){
                listModel.addElement(param);
            }
        }
        paramList.setModel(listModel);
    }
    
    private void populateParameterData() {
        DefaultListModel dlm = (DefaultListModel) paramList.getModel();
        if (dlm.getSize() == 0) {
            enableButtons(false);
            enablePropPanel(false);
        } else {
            paramList.setSelectedIndex(0);
            paramList.ensureIndexIsVisible(0);
        }
    }
    
    public void setTypeList(Object[] typeList) {
        if (typeList != null) {
            ComboBoxModel model = new DefaultComboBoxModel(typeList);
            typesCombo.setModel(model);
        }
    }
    
    public void setKindList(ValidValues values) {
        if (values != null) {
            ComboBoxModel model = new DefaultComboBoxModel(values.getValidValues());
            kindCombo.setModel(model);
        }
    }
    
    public void setDirectionList(ValidValues values) {
        if (values != null) {
            ComboBoxModel model = new DefaultComboBoxModel(values.getValidValues());
            directionCombo.setModel(model);
        }
    }
    
    public void saveDataModel() {
        // - 1st, remove MutiplicityRanges, if any
        MultiplicityTableModel tableModel = (MultiplicityTableModel) multiplicityTable.getModel();
        Vector <ElementData> removedMultiElemVec = tableModel.getRemovedMultiElemVec();
        if (removedMultiElemVec != null) {
            for (ElementData elem : removedMultiElemVec) {
                elem.remove();
            }
        }
        
        // 2nd, save parameters
        Object [] dataList = listModel.toArray();
        // save the elements in the current data model list
        if (dataList != null) {
            Vector <IPropertyElement> paramSubElements = new Vector <IPropertyElement> ();
            ElementData elem = null;
            for (int i=0; i<dataList.length; i++ ) {
                elem = (ElementData) dataList[i];
                // construct a sub-element list in the same order as the listModel
                paramSubElements.add(elem.getElement());
                //elem.save();
            }
            
            if (this.parentElement != null ) {
                this.parentElement.setSubElements(paramSubElements);
                save(parentElement);
                //this.parentElement.save();
            }
        }
        
        // 3rd, remove parameters which have been cached in remvovedElements
        if (removedElements != null) {
            for (ElementData elem : removedElements) {
                elem.remove();
            }
        }
    }
    
    protected void save(IPropertyElement element) {
        if(element != null) {
            element.save();
            Vector < IPropertyElement > children = element.getSubElements();
            for(IPropertyElement child : children) {
                save(child);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        paramListLabel = new javax.swing.JLabel();
        ParamListPanel = new javax.swing.JPanel();
        listScrollPane = new javax.swing.JScrollPane();
        paramList = new javax.swing.JList();
        listButtonsPanel = new javax.swing.JPanel();
        moveUpButton = new javax.swing.JButton();
        moveDwnButton = new javax.swing.JButton();
        paramPropPanel = new javax.swing.JPanel();
        nameValuePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        paramName = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typesCombo = new javax.swing.JComboBox();
        directionLabel = new javax.swing.JLabel();
        directionCombo = new javax.swing.JComboBox();
        kindLabel = new javax.swing.JLabel();
        kindCombo = new javax.swing.JComboBox();
        multiplicityPanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        mulButtonPanel = new javax.swing.JPanel();
        addMulButton = new javax.swing.JButton();
        removeMulButton = new javax.swing.JButton();
        messageArea = new javax.swing.JTextArea();
        propButtonPanel = new javax.swing.JPanel();
        editParamButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        paramListLabel.setLabelFor(paramList);
        org.openide.awt.Mnemonics.setLocalizedText(paramListLabel, bundle.getString("LBL_PARAMETER"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(paramListLabel, gridBagConstraints);
        paramListLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_PARAMETER"));
        paramListLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NA"));

        ParamListPanel.setLayout(new java.awt.GridBagLayout());

        paramList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listScrollPane.setViewportView(paramList);
        paramList.getAccessibleContext().setAccessibleName(bundle.getString("LBL_PARAMETER"));
        paramList.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_PARAMETER"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        ParamListPanel.add(listScrollPane, gridBagConstraints);

        listButtonsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, bundle.getString("BTN_MOVEUP"));
        moveUpButton.setActionCommand("MOVEUP");
        moveUpButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        listButtonsPanel.add(moveUpButton, gridBagConstraints);
        moveUpButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_MOVEUP"));
        moveUpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_MOVEUP"));

        org.openide.awt.Mnemonics.setLocalizedText(moveDwnButton, bundle.getString("BTN_MOVEDWN"));
        moveDwnButton.setActionCommand("MOVEDOWN");
        moveDwnButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        listButtonsPanel.add(moveDwnButton, gridBagConstraints);
        moveDwnButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_MOVEDWN"));
        moveDwnButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_MOVEDWN"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        ParamListPanel.add(listButtonsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 11, 5);
        add(ParamListPanel, gridBagConstraints);

        paramPropPanel.setLayout(new java.awt.GridBagLayout());

        nameValuePanel.setLayout(new java.awt.GridBagLayout());

        nameValuePanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nameLabel.setLabelFor(paramName);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, bundle.getString("LBL_NAME"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        nameValuePanel.add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_NAME"));
        nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NAME"));

        paramName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        nameValuePanel.add(paramName, gridBagConstraints);
        paramName.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_NAME"));
        paramName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NAME"));

        typeLabel.setLabelFor(typesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, bundle.getString("LBL_TYPE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        nameValuePanel.add(typeLabel, gridBagConstraints);
        typeLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_TYPE"));
        typeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_TYPE"));

        typesCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        nameValuePanel.add(typesCombo, gridBagConstraints);
        typesCombo.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TYPE"));
        typesCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_TYPE"));

        directionLabel.setLabelFor(directionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(directionLabel, bundle.getString("LBL_DIRECTION"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        nameValuePanel.add(directionLabel, gridBagConstraints);
        directionLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_DIRECTION"));
        directionLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_DIRECTION"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        nameValuePanel.add(directionCombo, gridBagConstraints);
        directionCombo.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DIRECTION"));
        directionCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_DIRECTION"));

        kindLabel.setLabelFor(kindCombo);
        org.openide.awt.Mnemonics.setLocalizedText(kindLabel, bundle.getString("LBL_KIND"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        nameValuePanel.add(kindLabel, gridBagConstraints);
        kindLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_KIND"));
        kindLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_KIND"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        nameValuePanel.add(kindCombo, gridBagConstraints);
        kindCombo.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_KIND"));
        kindCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_KIND"));

        multiplicityPanel.setLayout(new java.awt.GridBagLayout());

        multiplicityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MULTIPLICITY")));
        tableScrollPane.setPreferredSize(new java.awt.Dimension(260, 132));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        multiplicityPanel.add(tableScrollPane, gridBagConstraints);
        tableScrollPane.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_NA"));
        tableScrollPane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NA"));

        mulButtonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addMulButton, bundle.getString("BTN_ADDRANGE"));
        addMulButton.setActionCommand("ADD_MULTI");
        addMulButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mulButtonPanel.add(addMulButton, gridBagConstraints);
        addMulButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ADDRANGE"));
        addMulButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_ADDRANGE"));

        org.openide.awt.Mnemonics.setLocalizedText(removeMulButton, bundle.getString("BTN_REMOVERANGE"));
        removeMulButton.setActionCommand("REMOVE_MULTI");
        removeMulButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mulButtonPanel.add(removeMulButton, gridBagConstraints);
        removeMulButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_REMOVERANGE"));
        removeMulButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_REMOVERANGE"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        multiplicityPanel.add(mulButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        nameValuePanel.add(multiplicityPanel, gridBagConstraints);
        multiplicityPanel.getAccessibleContext().setAccessibleName(bundle.getString("MULTIPLICITY"));
        multiplicityPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NA"));

        messageArea.setBackground(nameValuePanel.getBackground());
        messageArea.setEditable(false);
        messageArea.setFont(new java.awt.Font("Tahoma", 0, 11));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setAutoscrolls(false);
        messageArea.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        nameValuePanel.add(messageArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        paramPropPanel.add(nameValuePanel, gridBagConstraints);

        propButtonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(editParamButton, bundle.getString("BTN_UPDATE"));
        editParamButton.setActionCommand("UPDATE_PARAM");
        editParamButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        propButtonPanel.add(editParamButton, gridBagConstraints);
        editParamButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_UPDATE"));
        editParamButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_UPDATE"));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("BTN_NEWPARAM"));
        addButton.setActionCommand("ADD_PARAM");
        addButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        propButtonPanel.add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_NEWPARAM"));
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NEWPARAM"));

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("BTN_REMOVE"));
        removeButton.setActionCommand("REMOVE_PARAM");
        removeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                actionHandler(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        propButtonPanel.add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_REMOVE"));
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_REMOVE"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        paramPropPanel.add(propButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 11, 6);
        add(paramPropPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void actionHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionHandler
        // reset message area
        messageArea.setText(ElementData.EMPTY_STR);
        String actionCmd = evt.getActionCommand();
        
        if (actionCmd != null && actionCmd.length() > 0) {
            if ("ADD_PARAM".equals(actionCmd)) {
                addParamHander();
                
            } else if ("REMOVE_PARAM".equals(actionCmd)) {
                removeParamHandler();
                
            } else if ("UPDATE_PARAM".equals(actionCmd)) {
                updateParamHander();
                
            } else if ("MOVEUP".equals(actionCmd)) {
                moveUpDownHandler(actionCmd);
                
            } else if ("MOVEDOWN".equals(actionCmd)) {
                moveUpDownHandler(actionCmd);   
                
            } else if ("ADD_MULTI".equals(actionCmd)) {
                ((MultiplicityTableModel)multiplicityTable.getModel()).addRow();
                
            } else if ("REMOVE_MULTI".equals(actionCmd)) {
                int [] selectedRows = multiplicityTable.getSelectedRows();
                ((MultiplicityTableModel)multiplicityTable.getModel()).removeRows(selectedRows);
                
            } else {
                //DO NOTHING
            }
        }
    }//GEN-LAST:event_actionHandler
    
    // handle the selection changed for paramList (JList)
    public void selectionChangedHandler(javax.swing.event.ListSelectionEvent evt) {
        ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
        if (evt.getValueIsAdjusting() == false) {
            if (lsm.isSelectionEmpty()) {
                //No selection, disable some components.
                enableButtons(false);
                enablePropPanel(false);
            } else {
                enableButtons(true);
                enablePropPanel(true);
                ElementData selValue = (ElementData)paramList.getSelectedValue();
                populatePropData(selValue);
            }
        }
    }
    
    private void enableButtons(boolean enabled) {
        moveDwnButton.setEnabled(enabled);
        moveUpButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        editParamButton.setEnabled(enabled);
    }
    
    private void enablePropPanel(boolean enabled) {
        paramName.setEnabled(enabled);
        typesCombo.setEnabled(enabled);
        directionCombo.setEnabled(enabled);
        kindCombo.setEnabled(enabled);
        addMulButton.setEnabled(enabled);
        removeMulButton.setEnabled(enabled);
    }
    
    private void clearPropPanel() {
        paramName.setText(ElementData.EMPTY_STR);
        typesCombo.setSelectedItem(ElementData.EMPTY_STR);
        directionCombo.setSelectedItem(ElementData.EMPTY_STR);
        kindCombo.setSelectedItem(ElementData.EMPTY_STR);
        messageArea.setText(ElementData.EMPTY_STR);
        ((MultiplicityTableModel)multiplicityTable.getModel()).clearRows();
    }
    
    private void populatePropData(ElementData paramData) {
        int  kindInt = 0;
        int directionInt = 0;
        if ( paramData != null) {
            typesCombo.setSelectedItem(paramData.getType());
            paramName.setText(paramData.getName());
            
            // convert a numeric string to a an int
            try {
                kindInt = Integer.parseInt(paramData.getKind());
                directionInt = Integer.parseInt(paramData.getDirection());
            } catch (NumberFormatException ex) {
                // do nothing;
            }
            directionCombo.setSelectedIndex(directionInt);
            kindCombo.setSelectedIndex(kindInt);
            messageArea.setText(ElementData.EMPTY_STR);
            populateMultiplicity(paramData);
        }
    }
    
    private void populateMultiplicity(ElementData paramProp) {
        String lower = ElementData.EMPTY_STR;
        String upper = ElementData.EMPTY_STR;
        Vector <ElementData> multiRanges = null;
        if (paramProp != null) {
            MultiplicityTableModel tableModel = (MultiplicityTableModel) multiplicityTable.getModel();
            tableModel.setRootProp(paramProp.getMultiRangesProp(), paramProp.getMultiRangesSubDef());
            multiRanges = paramProp.getMultiRanges();
            if (multiRanges != null) {
                tableModel.setDataVector(multiRanges);
                
                // The collection type column needs a custom column render and
                // editor.
                TableColumn column = multiplicityTable.getColumnModel().getColumn(2);
                column.setCellEditor(new CollectionTypeEditor());
                column.setCellRenderer(new CollectionTypeRender());
                
                initColumnSizes(multiplicityTable);
            }
        }
    }
    
    private void removeParamHandler() {
        int selectedIndex = paramList.getSelectedIndex();
        if (selectedIndex == -1)  // nothing is selected; return.
            return;
        
        // remove the item from the data model and
        // cache item being removed in memory, not actually remove it yet
        removedElements.add((ElementData)listModel.remove(selectedIndex));
        
        int size = listModel.size();
        if (size == 0) {
            //List is empty: disable buttons.
            enableButtons(false);
            clearPropPanel();
            this.enablePropPanel(false);
            
        } else {
            //Adjust the selection.
            if (selectedIndex == size) {
                selectedIndex--;
            }
            paramList.setSelectedIndex(selectedIndex);
            paramList.ensureIndexIsVisible(selectedIndex);
        }
    }
    
    private void updateParamHander() {
        int selectedIndex = paramList.getSelectedIndex();
        if (selectedIndex == -1)  // nothing is selected; return.
            return;
        
        String name = (String) paramName.getText();
        String type = (String) typesCombo.getSelectedItem();
//        if (!isDataValid(name, type, selectedIndex)) {
//            return;
//        }
        
        ElementData targetElem = (ElementData) listModel.get(selectedIndex);
        if (targetElem != null) {
            targetElem.setName(name);
            targetElem.setType(type);
            targetElem.setDirection(new String().valueOf(directionCombo.getSelectedIndex()));
            targetElem.setKind(new String().valueOf(kindCombo.getSelectedIndex()));
            setLastEditedCell();
            Vector multiRangeVec = targetElem.getMultiRanges();
            if (multiRangeVec != null) {
                Vector <IPropertyElement> elemVec = new Vector();
                for (int i=0; i < multiRangeVec.size(); i++) {
                    ElementData elemData  = (ElementData) multiRangeVec.get(i);
                    elemVec.add(elemData.getElement());
                }
                targetElem.getMultiRangesProp().setSubElements(elemVec);
            }
            listModel.set(selectedIndex, targetElem);
        }
    }
    
    private void addParamHander() {
        Dialog dlg = null;
        DefaultComboBoxModel typeModel = (DefaultComboBoxModel) typesCombo.getModel();
        
        //create "New Parameter" dialog and display it
        NewParameterPanel innerPane = new NewParameterPanel(typeModel, listModel.toArray());
        DialogDescriptor dd = new DialogDescriptor(innerPane, bundle.getString("LBL_NEWPARAM"),
                true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        innerPane.setDialogDescriptor(dd);
        dd.setValid(false);
        dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
        
        // when Ok button on the dialog is pressed.
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            String type = innerPane.getParamType();
            String name = innerPane.getParamName();
            int size = listModel.getSize();
            ElementData newElement = createNewElement(rootDef, parentElement);
            if (newElement != null) {
                newElement.setName(name);
                newElement.setType(type);
                newElement.setMultiRanges(new Vector());
                
                listModel.addElement(newElement);
                paramList.setSelectedIndex(size);
                paramList.ensureIndexIsVisible(size);
            }
        }
    }
    
    private ElementData createNewElement(IPropertyDefinition def, IPropertyElement parentElem) {
        IPropertyElement newElem = null;
        DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
        newElem = builder.retrievePropertyElement(def, parentElem);
        ElementData newParamElem = new ElementData(newElem, false);  // false: no data loading
        return newParamElem;
    }
    
    // This method is not used for now.
    public boolean isDataValid(String name, String type, int selIndex) {
        if ((name == null || name.trim().length() == 0) ||
                (type == null || type.trim().length() == 0)) {
            messageArea.setText(bundle.getString("MSG_EMTPY_FIELD"));
            return false;
        }
        // validate the name to make sure it is unique
        name = name.trim();
        Object[] elements = listModel.toArray();
        boolean valid = true;
        ElementData elem = null;
        if (elements != null ) {
            for (int i = 0; i < elements.length; i++) {
                if (i != selIndex ) {
                    elem = (ElementData) elements[i];
                    if (elem.getName().equals(name)) {
                        valid = false;
                        String message = NbBundle.getMessage(ParameterCustomizerPanel.class,"MSG_DUPLICATE", name);
                        messageArea.setText(message);
                        break;
                    }
                }
            }
        }
        return valid;
    }
    
    public boolean validateName() {
        String name = paramName.getText();
        int selIndex = paramList.getSelectedIndex();
        String message = ElementData.EMPTY_STR;
        boolean valid = true;
        
        if (name == null || name.trim().length() == 0)  {
            message = bundle.getString("MSG_EMTPY_NAME");
            valid = false;
        } else {
            // validate the name to make sure it is unique
            name = name.trim();
            Object[] elements = listModel.toArray();
            ElementData elem = null;
            if (elements != null ) {
                for (int i = 0; i < elements.length; i++) {
                    if (i != selIndex ) {
                        elem = (ElementData) elements[i];
                        if (elem.getName().equals(name)) {
                            valid = false;
                            message = NbBundle.getMessage(ParameterCustomizerPanel.class,"MSG_DUPLICATE", name);
                            break;
                        }
                    }
                }
            }
        }
        
        editParamButton.setEnabled(valid);
        messageArea.setText(message);
        return valid;
    }
    
    private void moveUpDownHandler(String actionCmd) {
        int movedRow = paramList.getSelectedIndex();
        if (movedRow == -1) // nothing is selected; return.
            return;
        if (actionCmd.equals("MOVEUP")) {  // NO I18N
            if (movedRow != 0) { //not already at top
                swap(movedRow, movedRow-1);
                paramList.setSelectedIndex(movedRow-1);
                paramList.ensureIndexIsVisible(movedRow-1);
            }
        } else { // move down
            if (movedRow != listModel.getSize()-1) { //not already at bottom
                swap(movedRow, movedRow+1);
                paramList.setSelectedIndex(movedRow+1);
                paramList.ensureIndexIsVisible(movedRow+1);
            }
        }
    }
    
    //Swap two elements in the list.
    private void swap(int indx1, int indx2) {
        Object Obj1 = listModel.getElementAt(indx1);
        Object Obj2 = listModel.getElementAt(indx2);
        listModel.set(indx1, Obj2);
        listModel.set(indx2, Obj1);
    }
    
    private Dimension getMaxButtonWidth(JButton [] buttonList) {
        Dimension maxDim = null;
        Dimension dimension = null;
        
        if (buttonList != null && buttonList.length > 0) {
            int max = 0;
            int width = 0;
            for (int i=0; i < buttonList.length; i++) {
                dimension = buttonList[i].getPreferredSize();
                width  = dimension.width;
                if (width > max) {
                    max = width;
                    maxDim = dimension;
                }
            }
        }
        return maxDim;
    }
    
    // This method processes the value of the last cell beeing edited.
    // The value of the cell is either saved to the data model or reset to empty string
    // based on the "saved" flag.
    private void setLastEditedCell() {
        int editingRow = multiplicityTable.getEditingRow();
        int editingCol = multiplicityTable.getEditingColumn();
        // if no cell is currently being edited, simply return.
        if (editingRow < 0 || editingCol < 0)
            return;
        javax.swing.DefaultCellEditor cellEditor =
                (javax.swing.DefaultCellEditor) multiplicityTable.getCellEditor(editingRow, editingCol);
        java.awt.Component editorComponent = cellEditor.getComponent();
        if (editorComponent instanceof javax.swing.JTextField) {
            String lastEditedText = ((javax.swing.JTextField)editorComponent).getText();
            ((MultiplicityTableModel)multiplicityTable.getModel()).setValueAt(
                    lastEditedText, editingRow, editingCol);
            cellEditor.stopCellEditing();
        }
        editorComponent.transferFocus();
        return;
    }
    
    ////////////////////////////////////////////////////////
    class MultiplicityTableModel extends DefaultTableModel {
        private IPropertyDefinition multRangeDef;
        private IPropertyElement parentMultRangeElement;
        protected Vector <ElementData> rowsData; // a vector of ElementData
        protected Vector colNames;
        protected Vector <ElementData> removedMultiElemVec = new Vector();
        
        public MultiplicityTableModel(Vector columnNames) {
            super();
            colNames = columnNames;
        }
        
        public void setRootProp(IPropertyElement element, IPropertyDefinition def) {
            parentMultRangeElement = element;
            multRangeDef = def;
        }
        
        public Vector <ElementData> getRemovedMultiElemVec() {
            return removedMultiElemVec;
        }

        @Override
        public String getColumnName(int arg0) {
            return super.getColumnName(arg0);
        }
        
        public void setDataVector(Vector <ElementData> dataRows) {
            rowsData = dataRows;
            Vector rows = new Vector();
            Vector aRow = null;
            if (rowsData != null ) {
                for (ElementData elem : rowsData) {
                    aRow = new Vector(3);
                    aRow.add(elem.getLower());
                    aRow.add(elem.getUpper());
                    aRow.add(elem);
                    rows.add(aRow);
                }
                super.setDataVector(rows, colNames);
            }
        }
        
        public void clearRows() {
            if (rowsData != null) {
                rowsData.clear();
            }
            Vector rows = super.getDataVector();
            if (rows != null) {
                rows.clear();
                super.setRowCount(0);
            }
        }
        
        /**
         * Sets the identified cell to a given value
         * @param rowIndex to identify row
         * @param columnIndex to identify col
         * @param value of type Object the value the cell is set to
         */
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Vector <IPropertyElement> rowCells = null;
            if (rowsData.size() > 0) {
                ElementData aRow = (ElementData) rowsData.get(rowIndex);
                rowCells = aRow.getSubElements();
                if (rowCells != null) {
                    //System.out.println(rowCells.toString());
                    IPropertyElement col = (IPropertyElement) rowCells.elementAt(columnIndex);
                    
                    String s = PropertyDataFormatter.translateToFullyQualifiedName((String)value);
                    col.setValue(s);
                }
            }
            super.setValueAt(value, rowIndex, columnIndex);
        }
        
        // Adds new empty row to the data model
        public void addRow() {
            ElementData newElement = createNewElement(multRangeDef, parentMultRangeElement);
            if (newElement != null) {
                if (rowsData != null) {
                    rowsData.add(newElement);
                    super.addRow(new Object[] {"", "", newElement});
                }
            }
        }
        
        // Removes multi selected row intervals.
        // The indices of the selected rows are specified
        // in the array rowIndices in an increasing order.
        public void removeRows(int[] rowIndex) {
            if (rowIndex == null || rowIndex.length == 0)
                return;
            
            // extract the removed object by their indices
            ElementData [] rowsToBeRemoved = new ElementData [rowIndex.length];
            for (int i = 0; i < rowIndex.length; i++) {
                rowsToBeRemoved[i] =  rowsData.get(rowIndex[i]);
            }
            // search for each rowsToBeRemoved in rows;
            // if found, remove the row from rows
            ElementData target = null;
            ElementData aRow = null;
            for (int i = 0; i < rowsToBeRemoved.length; i++) {
                target = rowsToBeRemoved[i];
                for (int j = 0; j < getRowCount(); j++) {
                    aRow = (ElementData) rowsData.get(j);
                    if (aRow == target) {  // intentionally compare the objects' addresses
                        rowsData.remove(aRow);
                        super.removeRow(j);
                        // cache the removed rows to delete later if Ok button if pressed
                        removedMultiElemVec.add(aRow);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * The table cell editor used to render the collection type property.
     */
    public class CollectionTypeEditor extends DefaultCellEditor
    {
        public CollectionTypeEditor()
        {
            super(new JComboBox());
        }
        
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column)
        {
            JComboBox retVal = (JComboBox) super.getTableCellEditorComponent(table, 
                                                                             value,
                                                                             isSelected, 
                                                                             row,
                                                                             column);
            
            if(value instanceof ElementData)
            {
                ElementData data = (ElementData)value;
                for(String curType : data.getValidCollectionTypes())
                {
                    String s = PropertyDataFormatter.translateFullyQualifiedName(curType);
                    retVal.addItem(s);
                }
                
                String t = PropertyDataFormatter.translateFullyQualifiedName(data.getCollectionType());
                retVal.setSelectedItem(t);
            }
            
            return retVal;
        }
    }
    
    /**
     * The cell render used to correctly render the collection type information.
     */
    public class CollectionTypeRender extends DefaultTableCellRenderer
    {
        protected void setValue(Object value)
        {
            Object realValue = value;
            
            if(value instanceof ElementData)
            {
                ElementData data = (ElementData)value;
                realValue = data.getCollectionType();
                
                if(realValue instanceof String)
                {
                    String s = (String)realValue;
                    realValue = PropertyDataFormatter.translateFullyQualifiedName(s);
                }
            }
            
            
            super.setValue(realValue);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ParamListPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addMulButton;
    private javax.swing.JComboBox directionCombo;
    private javax.swing.JLabel directionLabel;
    private javax.swing.JButton editParamButton;
    private javax.swing.JComboBox kindCombo;
    private javax.swing.JLabel kindLabel;
    private javax.swing.JPanel listButtonsPanel;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JButton moveDwnButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JPanel mulButtonPanel;
    private javax.swing.JPanel multiplicityPanel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel nameValuePanel;
    private javax.swing.JList paramList;
    private javax.swing.JLabel paramListLabel;
    private javax.swing.JTextField paramName;
    private javax.swing.JPanel paramPropPanel;
    private javax.swing.JPanel propButtonPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeMulButton;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JComboBox typesCombo;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTable multiplicityTable;
}
