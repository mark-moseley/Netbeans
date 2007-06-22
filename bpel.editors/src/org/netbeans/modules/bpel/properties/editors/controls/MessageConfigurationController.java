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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.concurrent.Callable;
import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.bpel.properties.VirtualVariableContainer;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VisibilityScope;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.choosers.VariableChooserPanel;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.choosers.NewMessageVarChooser;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.bpel.model.api.ToPartsHolder;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.FromPartsHolder;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.openide.nodes.Node.Property;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Collects the common behaviour for selection Partner Link, Operation,
 * Input and Output variables for the message related activities.
 *
 * @author  nk160297
 */
public class MessageConfigurationController extends EditorLifeCycleAdapter
        implements Validator.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor myEditor;
    private DefaultValidator myValidator;
    
    private JButton btnNewInputVariable;
    private JButton btnNewOutputVariable;
    private JComboBox cbxOperation;
    private JComboBox cbxPartnerLink;
    private JTextField fldVariableName;

    private JRadioButton rbtnInputVariable;
    private JRadioButton rbtnInputParts;
    private JTextField fldInputVariable;
    private JButton btnChooseInputVariable;
    private PartVariableTable tblInputParts;
    private ButtonGroup inputButtonGroup;
    
    private JRadioButton rbtnOutputVariable;
    private JRadioButton rbtnOutputParts;
    private JTextField fldOutputVariable;
    private JButton btnChooseOutputVariable;
    private PartVariableTable tblOutputParts;
    private ButtonGroup outputButtonGroup;
    
    /**
     * Specifies what role (My or Partner) has to be used
     * to load operation list. Generally, the only Invoke uses the Partner role
     * and all others (Pick, Receive, Reply) should use My role.
     */
    private boolean useMyRole;
    
    private VirtualVariableContainer currInputVar;
    private VirtualVariableContainer currOutputVar;
    private boolean inputVisible = true;
    private boolean outputVisible = true;
    private boolean declarationVisible = true;
    private PortType currPortType;
    
    private boolean isOutputVarEnabled = true;
    private ConfigurationListener myListener;
    
    public MessageConfigurationController(CustomNodeEditor anEditor) {
        this.myEditor = anEditor;
        this.useMyRole = true;
        createContent();
    }
    
    public void useParthnerRole() {
        useMyRole = false;
    }
    
    public void useMyRole() {
        useMyRole = true;
    }
    
    public void setVisibleVariables(
            boolean showInput, boolean showOutput, boolean showVariableDecl)  {
        if (inputVisible != showInput) {
            fldInputVariable.setVisible(showInput);
            btnNewInputVariable.setVisible(showInput);
            btnChooseInputVariable.setVisible(showInput);
            inputVisible = showInput;
        }
        //
        if (outputVisible != showOutput) {
            fldOutputVariable.setVisible(showOutput);
            btnNewOutputVariable.setVisible(showOutput);
            btnChooseOutputVariable.setVisible(showOutput);
            outputVisible = showOutput;
        }
        //
        if (declarationVisible != showVariableDecl) {
            fldVariableName.setVisible(showVariableDecl);
            declarationVisible = showVariableDecl;
        }
    }
    
    
    private boolean hasInputParts() {
        return (getEditedObject() instanceof Invoke) 
                ? hasToParts() : hasFromParts();
    }
    
    
    private boolean hasOutputParts() {
        return (getEditedObject() instanceof Invoke) 
                ? hasFromParts() : hasToParts();
    }
    
    
    private boolean hasToParts() {
        BpelEntity bpelEntity = getEditedObject();
        
        if (bpelEntity instanceof ToPartsHolder) {
            ToPartsHolder holder = (ToPartsHolder) bpelEntity;
            ToPartContainer container = holder.getToPartContaner();
            if (container != null) {
                return (container.sizeOfToParts() > 0);
            }
        }
        
        return false;
    }
    
    
    private boolean hasFromParts() {
        BpelEntity bpelEntity = getEditedObject();
        
        if (bpelEntity instanceof FromPartsHolder) {
            FromPartsHolder holder = (FromPartsHolder) bpelEntity;
            FromPartContainer container = holder.getFromPartContaner();
            if (container != null) {
                return (container.sizeOfFromParts() > 0);
            }
        }
        
        return false;
    }
    
    
    public void createContent() {
        inputButtonGroup = new ButtonGroup();
        outputButtonGroup = new ButtonGroup();
        
        //
        btnChooseInputVariable = new JButton();
        btnChooseOutputVariable = new JButton();
        btnNewInputVariable = new JButton();
        btnNewOutputVariable = new JButton();
        cbxOperation = new JComboBox();
        cbxPartnerLink = new JComboBox();
        
        ActionListener inputRadioActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateEnabledStateForInputUI();
            }
        };
        rbtnInputVariable = new JRadioButton();
        rbtnInputVariable.addActionListener(inputRadioActionListener);
        rbtnInputParts = new JRadioButton();
        rbtnInputParts.addActionListener(inputRadioActionListener);
        fldInputVariable = new JTextField();
        inputButtonGroup = new ButtonGroup();
        inputButtonGroup.add(rbtnInputVariable);
        inputButtonGroup.add(rbtnInputParts);
        tblInputParts = new PartVariableTable(this, true);

        ActionListener outputRadioActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateEnabledStateForOutputUI();
            }
        };
        fldOutputVariable = new JTextField();
        rbtnOutputVariable = new JRadioButton();
        rbtnOutputVariable.addActionListener(outputRadioActionListener);
        rbtnOutputParts = new JRadioButton();
        rbtnOutputParts.addActionListener(outputRadioActionListener);
        outputButtonGroup = new ButtonGroup();
        outputButtonGroup.add(rbtnOutputVariable);
        outputButtonGroup.add(rbtnOutputParts);
        tblOutputParts = new PartVariableTable(this, false);
        
//        if (hasInputParts()) {
//            rbtnInputVariable.setSelected(false);
//            rbtnInputParts.setSelected(true);
//        } else {
//            rbtnInputVariable.setSelected(true);
//            rbtnInputParts.setSelected(false);
//        }
//        
//        updateEnabledStateForInputUI();
//
//        if (hasOutputParts()) {
//            rbtnOutputVariable.setSelected(false);
//            rbtnOutputParts.setSelected(true);
//        } else {
//            rbtnOutputVariable.setSelected(true);
//            rbtnOutputParts.setSelected(false);
//        }
//        updateEnabledStateForOutputUI();
        
        fldVariableName = new JTextField();
        
        
        //
        cbxPartnerLink.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    Object oldOperation = cbxOperation.getSelectedItem();
                    reloadOperationList();
                    //
                    if (cbxOperation.getModel().getSize() > 0) {
                        cbxOperation.setSelectedIndex(0);
                    } else {
                        cbxOperation.setSelectedIndex(-1);
                    }
                    //
                    if (myListener != null) {
                        Object newOperation = cbxOperation.getSelectedItem();
                        if (!Util.isEqual(oldOperation, newOperation)) {
                            myListener.operationChanged();
                        }
                    }
                    //
                    resetVariables();
                    //
                    if (myListener != null) {
                        myListener.partnerLinkChanged();
                    }
                }
            }
        });
        //
        cbxOperation.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    resetVariables();
                    //
                    if (myListener != null) {
                        myListener.operationChanged();
                    }
                }
            }
        });
        //
        cbxPartnerLink.setRenderer(new DefaultListCellRenderer() {
            static final long serialVersionUID = 1L;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null && value instanceof PartnerLink) {
                    String plName = ((PartnerLink)value).getName();
                    setText(plName);
                    // setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
                }
                return this;
            }
        });
        //
        cbxOperation.setRenderer(new DefaultListCellRenderer() {
            static final long serialVersionUID = 1L;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null && value instanceof Operation) {
                    String plName = ((Operation)value).getName();
                    setText(plName);
                    // setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
                }
                return this;
            }
        });
        //
        ActionListener listener = new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                Object source = evt.getSource();
                chooseVariable(btnChooseInputVariable.equals(source));
            }
        };
        //
        btnChooseInputVariable.addActionListener( listener );
        btnChooseOutputVariable.addActionListener( listener );
        //
        ActionListener newListener = new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                Object source = evt.getSource();
                boolean isInput = btnNewInputVariable.equals(source);
                VirtualVariableContainer vvc = prepareNewVariable(isInput);
                if (vvc != null) {
                    if (isInput) {
                        setCurrInputVar(vvc);
                    } else {
                        setCurrOutputVar(vvc);
                    }
                }
            }
        };
        //
        btnNewInputVariable.addActionListener( newListener );
        btnNewOutputVariable.addActionListener( newListener );
        //
        resetVariables();
    }
    
    private void chooseVariable(final boolean isInputVar) {
        Operation operation = (Operation)cbxOperation.getSelectedItem();
        if (operation == null) {
            return;
        }
        if (!checkIfMessageSpecified(isInputVar, operation)) {
            return;
        }
        //
        String title = null;
        VirtualVariableContainer vvc = null;
        Message messageType = null;
        NamedComponentReference<Message> msgTypeRef = null;
        //
        if (isInputVar){
            vvc = getCurrInputVar();
            title = NbBundle.getMessage(FormBundle.class,
                    "DLG_InputVariableChooser"); // NOI18N
            Input input = operation.getInput();
            if (input == null) {
                return;
            }
            msgTypeRef = input.getMessage();
        } else {
            title = NbBundle.getMessage(FormBundle.class,
                    "DLG_OutputVariableChooser"); // NOI18N
            vvc = getCurrOutputVar();
            Output output = operation.getOutput();
            if (output == null) {
                return;
            }
            msgTypeRef = output.getMessage();
        }
        //
        if (msgTypeRef != null) {
            messageType = msgTypeRef.get();
        }
        //
        // Construct context lookup
        Lookup lookup = myEditor.getLookup();
        BpelEntity entry = (BpelEntity)myEditor.getEditedObject();
        VisibilityScope visScope = new VisibilityScope(entry, lookup);
        VariableTypeFilter typeFilter = new VariableTypeFilter(
                null, msgTypeRef.getQName());
        // Constants.VariableStereotype.MESSAGE, msgTypeRef.getQName());
        typeFilter.setShowAppropriateVarOnly(true);
        //
        NodesTreeParams treeParams = new NodesTreeParams();
        treeParams.setTargetNodeClasses(VariableNode.class);
        treeParams.setLeafNodeClasses(VariableNode.class);
        //
        Lookup contextLookup = new ExtendedLookup(
                lookup, visScope, typeFilter, treeParams);
        //
        VariableChooserPanel varChooserPanel =
                new VariableChooserPanel(contextLookup);
        TreeNodeChooser chooser = new TreeNodeChooser(varChooserPanel);
        chooser.initControls();
        if (vvc != null && vvc.isExisting()) {
            varChooserPanel.setSelectedValue(vvc.getVariableDeclaration());
        }
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(chooser, title);
        SoaDialogDisplayer.getDefault().notify( descriptor );
        if (descriptor.isOkHasPressed()) {
            VariableDeclaration newVariable = varChooserPanel.getSelectedValue();
            if (newVariable == null) {
                return;
            }
            boolean isCorrectType = false;
            WSDLReference<Message> newMsgRef = newVariable.getMessageType();
            if (newMsgRef != null) {
                VirtualVariableContainer newVvc =
                        new VirtualVariableContainer(newVariable, lookup);
                if (isInputVar){
                    if (messageType.equals(newMsgRef.get())) {
                        setCurrInputVar(newVvc);
                        isCorrectType = true;
                    }
                } else {
                    if (messageType.equals(newMsgRef.get())) {
                        setCurrOutputVar(newVvc);
                        isCorrectType = true;
                    }
                }
            }
            //
            if (!isCorrectType) {
                String msgText = NbBundle.getMessage(
                        ErrorMessagesBundle.class, "ERR_INCORRECT_MESSAGE_TYPE"); // NOI18N
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        msgText, NotifyDescriptor.WARNING_MESSAGE);
                SoaDialogDisplayer.getDefault().notify(nd);
            }
        }
    }
    
    private VirtualVariableContainer prepareNewVariable(final boolean isInputVar) {
        Lookup lookup = myEditor.getLookup();
        BpelModel model = (BpelModel)lookup.lookup(BpelModel.class);
        Process process = model.getProcess();
        BPELElementsBuilder elementBuilder = model.getBuilder();
        //
        Operation operation = (Operation)cbxOperation.getSelectedItem();
        if (operation == null) {
            return null;
        }
        if (!checkIfMessageSpecified(isInputVar, operation)) {
            return null;
        }
        //
        Constants.MessageDirection direction;
        NamedComponentReference<Message> msgRef = null;
        if (isInputVar) {
            Input input = operation.getInput();
            if (input != null) {
                msgRef = input.getMessage();
            }
            direction = Constants.MessageDirection.INPUT;
        } else {
            Output output = operation.getOutput();
            if (output != null) {
                msgRef = output.getMessage();
            }
            direction = Constants.MessageDirection.OUTPUT;
        }
        //
        if (msgRef == null) {
            return null;
        }
        Message message = msgRef.get();
        //
        BpelEntity omElement = (BpelEntity)myEditor.getEditedObject();
        NewMessageVarChooser chooser = new NewMessageVarChooser(
                omElement, operation.getName(), message, direction);
        chooser.initControls();
        //
        String title = NbBundle.getMessage(FormBundle.class,
                isInputVar ? "DLG_NewInputVariable" : "DLG_NewOutputVariable"); // NOI18N
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(chooser, title);
        //
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.isOkHasPressed()) {
            return chooser.getSelectedValue();
        }
        //
        return null;
    }
    
    /**
     * Check if the message (parameter) is specified for the operation.
     */
    private boolean checkIfMessageSpecified(
            final boolean isInputVar, final Operation operation) {
        Message msg = null;
        NamedComponentReference<Message> msgRef = null;
        if (isInputVar) {
            Input input = operation.getInput();
            if (input != null) {
                msgRef = input.getMessage();
            }
        } else {
            Output output = operation.getOutput();
            if (output != null) {
                msgRef = output.getMessage();
            }
        }
        if (msgRef != null) {
            msg = msgRef.get();
        }
        if (msg == null) {
            String text = NbBundle.getMessage(
                    FormBundle.class, "MSG_EmptyOperatonMessage");
            //
            UserNotification.showMessageAsinc(text);
            return false;
        }
        return true;
    }
    
    public boolean initControls() {
        try {
            Lookup lookup = myEditor.getLookup();
//            BusinessProcessHelper helper = (BusinessProcessHelper)lookup.
//                    lookup(BusinessProcessHelper.class);
            
            BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
            
            if (bpelModel != null) {
                Process bpelModelRoot = bpelModel.getProcess();
                if (bpelModelRoot != null) {
                    PartnerLinkContainer plContainer =
                            bpelModelRoot.getPartnerLinkContainer();
                    if (plContainer != null) {
                        //
                        // Load a list of PartnerLink
                        PartnerLink[] partnerLinkArr = plContainer.getPartnerLinks();
                        cbxPartnerLink.setModel(
                                new DefaultComboBoxModel(partnerLinkArr));
                        //
                        // Set selection to PartnerLink combo-box
                        cbxPartnerLink.setSelectedIndex(-1);
                        Property plProp = PropertyUtils.lookForPropertyByType(
                                myEditor.getEditedNode(), PropertyType.PARTNER_LINK);
                        if (plProp != null) {
                            BpelReference<PartnerLink> pLinkRef =
                                    (BpelReference<PartnerLink>)plProp.getValue();
                            if (pLinkRef != null) {
                                PartnerLink pLink = pLinkRef.get();
                                if (pLink != null) {
                                    cbxPartnerLink.setSelectedItem(pLink);
                                }
                            }
                        }
                        
                        //
                        reloadOperationList();
                        setCurrentOperationSelection();
                        //
                        setCurrentVariables();

                        if (hasInputParts()) {
                            rbtnInputVariable.setSelected(false);
                            rbtnInputParts.setSelected(true);
                        } else {
                            rbtnInputVariable.setSelected(true);
                            rbtnInputParts.setSelected(false);
                        }
                        tblInputParts.setMessage(getSelectedOperationInputMessage());

                        updateEnabledStateForInputUI();

                        if (hasOutputParts()) {
                            rbtnOutputVariable.setSelected(false);
                            rbtnOutputParts.setSelected(true);
                        } else {
                            rbtnOutputVariable.setSelected(true);
                            rbtnOutputParts.setSelected(false);
                        }
                        tblOutputParts.setMessage(getSelectedOperationOutputMessage());
                        
                        updateEnabledStateForOutputUI();
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        updateEnabledStateForInputUI();
        updateEnabledStateForOutputUI();
        //
        return true;
    }
    
    public boolean applyNewValues() {
        try {
            Node node = myEditor.getEditedNode();
            Object omRef = myEditor.getEditedObject();
            assert omRef instanceof ReferenceCollection;
            ReferenceCollection refColl = (ReferenceCollection)omRef;
            Property prop = null;
            //
            PartnerLink pLink = (PartnerLink)cbxPartnerLink.getSelectedItem();
            prop = PropertyUtils.lookForPropertyByType(node, PropertyType.PARTNER_LINK);
            if ( prop != null ) {
                if (pLink != null) {
                    BpelReference<PartnerLink> pLinkRef =
                            refColl.createReference(pLink, PartnerLink.class);
                    prop.setValue(pLinkRef);
                } else {
                    // prop.setValue(null);
                }
            }
            //
            Operation operation = (Operation)cbxOperation.getSelectedItem();
            prop = PropertyUtils.lookForPropertyByType(node, PropertyType.OPERATION);
            if ( prop != null ) {
                if (operation != null) {
                    WSDLReference<Operation> operRef =
                            refColl.createWSDLReference(operation, Operation.class);
                    prop.setValue(operRef);
                } else {
                    // prop.setValue(null);
                }
            }
            //
            prop = PropertyUtils.lookForPropertyByType(node, PropertyType.PORT_TYPE);
            if ( prop != null ) {
                if (currPortType != null){
                    WSDLReference<PortType> pTypeRef =
                            refColl.createWSDLReference(currPortType,
                            PortType.class);
                    prop.setValue(pTypeRef);
                } else {
                    prop.setValue(null);
                }
            }
            //
            //
            if (declarationVisible) {
                prop = PropertyUtils.lookForPropertyByType(
                        node, PropertyType.EVENT_VARIABLE_NAME);
                if (prop != null) {
                    String varName = fldVariableName.getText();
                    if (varName == null || varName.length() == 0) {
                        prop.setValue(null);
                    } else {
                        prop.setValue(varName);
                    }
                }
            }
            //
            if (inputVisible) {
                prop = PropertyUtils.lookForPropertyByType(node, PropertyType.INPUT);
                
                if (rbtnInputVariable.isSelected()) {
                    if (prop != null) {
                        if (currInputVar != null && rbtnInputVariable.isSelected()) 
                        {
                            VariableDeclaration varDecl = currInputVar.createNewVariable();
                            BpelReference<VariableDeclaration> varRef =
                                    refColl.createReference(varDecl,
                                    VariableDeclaration.class);
                            prop.setValue(varRef);
                        } else {
                            prop.setValue(null);
                        }
                    }
                    tblInputParts.removeParts();
                } else {
                    if (prop != null) {
                        prop.setValue(null);
                    }
                    tblInputParts.applyChanges();
                }
            }
            //
            if (outputVisible) {
                prop = PropertyUtils.lookForPropertyByType(node, PropertyType.OUTPUT);
                if (isOutputVarEnabled) {
                    if (rbtnOutputVariable.isSelected()) {
                        if (prop != null) {
                            if (currOutputVar != null && rbtnOutputVariable.isSelected()) {
                                VariableDeclaration varDecl = currOutputVar.createNewVariable();
                                BpelReference<VariableDeclaration> varRef =
                                        refColl.createReference(varDecl,
                                        VariableDeclaration.class);
                                prop.setValue(varRef);
                            } else {
                                prop.setValue(null);
                            }
                        }
                        tblOutputParts.removeParts();
                    } else {
                        if (prop != null) {
                            prop.setValue(null);
                        }
                        tblOutputParts.applyChanges();
                    }
                } else {
                    prop.setValue(null);
                }
            }
            //
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    private void reloadOperationList() {
        try {
            cbxOperation.setModel(new DefaultComboBoxModel());
            cbxOperation.setSelectedIndex(-1);
            //
            PartnerLink pl = (PartnerLink)cbxPartnerLink.getSelectedItem();
            
            //Reset member variable storing reference to current PortType
            currPortType = null;
            
            if(pl == null) {
                cbxOperation.setModel(new DefaultComboBoxModel());
            } else {
                WSDLReference<Role> roleRef =
                        useMyRole ? pl.getMyRole() : pl.getPartnerRole();
                WSDLReference<PartnerLinkType> pLinkTypeRef = pl.getPartnerLinkType();
                if (roleRef != null && pLinkTypeRef != null) {
                    //
                    // Look for the Partner Link Type
                    PartnerLinkType plType = pLinkTypeRef.get();
                    if (plType != null) {
                        //
                        // Look for the Port Type;
                        NamedComponentReference<PortType> portTypeRef = null;
                        Role role = roleRef.get();
                        if (role != null) {
                            if (role.equals(plType.getRole1())) {
                                portTypeRef = plType.getRole1().
                                        getPortType();
                            } else if (role.equals(plType.getRole2())) {
                                portTypeRef = plType.getRole2().
                                        getPortType();
                            }
                        } else {
                            String refString = roleRef.getRefString();
                            if (refString != null) {
                                if (refString.equals(plType.getRole1().getName())) {
                                    portTypeRef = plType.getRole1().
                                            getPortType();
                                } else if (refString.equals(plType.getRole2().getName())) {
                                    portTypeRef = plType.getRole2().
                                            getPortType();
                                }
                            }
                        }
                        if (portTypeRef != null){
                            currPortType = portTypeRef.get();
                        }
                        //
                        if (currPortType != null) {
                            //
                            // Load Operations to the combo-box
                            Collection<Operation> operations =
                                    currPortType.getOperations();
                            Object[] operArr = operations.toArray();
                            cbxOperation.setModel(new DefaultComboBoxModel(operArr));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void setCurrentOperationSelection() {
        Object oldOperation = cbxOperation.getSelectedItem();
        try {
            //
            // Try set selection to current operation
            Property operProp = PropertyUtils.lookForPropertyByType(
                    myEditor.getEditedNode(), PropertyType.OPERATION);
            if(operProp != null) {
                WSDLReference<Operation> operRef =
                        (WSDLReference<Operation>)operProp.getValue();
                if (operRef != null) {
                    Operation operation = operRef.get();
                    if (operation != null) {
                        cbxOperation.setSelectedItem(operation);
                        resetVariables();
                        return;
                    }
                }
            }
            //
            cbxOperation.setSelectedIndex(-1);
            resetVariables();
            //
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            if (myListener != null) {
                Object newOperation = cbxOperation.getSelectedItem();
                if (!Util.isEqual(oldOperation, newOperation)) {
                    myListener.operationChanged();
                }
            }
        }
    }
    
    /**
     * This operation is a part of form initialization process.
     * It takes the Variables references from the Node and put them
     * to the form.
     */
    private void setCurrentVariables() {
        try {
            //
            if (declarationVisible) {
                Property varNameProp = PropertyUtils.lookForPropertyByType(
                        myEditor.getEditedNode(), PropertyType.EVENT_VARIABLE_NAME);
                if (varNameProp != null) {
                    String varName = (String)varNameProp.getValue();
                    fldVariableName.setText(varName);
                }
            }
            //
            if (inputVisible || outputVisible) {
                //
                Object item = cbxOperation.getSelectedItem();
                if (item == null) {
                    setCurrInputVar(null);
                    setCurrOutputVar(null);
                } else {
                    assert item instanceof Operation;
                    Operation operation = (Operation)item;
                    if (inputVisible) {
                        //
                        // Try set input to current value
                        Input input = operation.getInput();
                        boolean inputFound = false;
                        if (input != null) {
                            Property inputProp = PropertyUtils.lookForPropertyByType(
                                    myEditor.getEditedNode(), PropertyType.INPUT);
                            if (inputProp != null) {
                                BpelReference<VariableDeclaration> varRef =
                                        (BpelReference<VariableDeclaration>)
                                        inputProp.getValue();
                                if (varRef != null) {
                                    VariableDeclaration var = varRef.get();
                                    if (var != null) {
                                        Lookup lookup = myEditor.getLookup();
                                        VirtualVariableContainer vvc =
                                                new VirtualVariableContainer(
                                                var, lookup);
                                        setCurrInputVar(vvc);
                                        inputFound = true;
                                    }
                                }
                            }
                        }
                        if (!inputFound) {
                            setCurrInputVar(null);
                        }
                    }
                    //
                    if (outputVisible && isOutputVarEnabled) {
                        //
                        // Try set output to current value
                        Output output = operation.getOutput();
                        boolean outputFound = false;
                        if (output != null) {
                            Property outputProp = PropertyUtils.lookForPropertyByType(
                                    myEditor.getEditedNode(), PropertyType.OUTPUT);
                            if (outputProp != null) {
                                BpelReference<VariableDeclaration> varRef =
                                        (BpelReference<VariableDeclaration>)
                                        outputProp.getValue();
                                if (varRef != null) {
                                    VariableDeclaration var = varRef.get();
                                    if (var != null) {
                                        Lookup lookup = myEditor.getLookup();
                                        VirtualVariableContainer vvc =
                                                new VirtualVariableContainer(
                                                var, lookup);
                                        setCurrOutputVar(vvc);
                                        outputFound = true;
                                    }
                                }
                            }
                        }
                        if (!outputFound) {
                            setCurrOutputVar(null);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            // cbxOperation.setSelectedIndex(-1);
            resetVariables();
        }
    }
    
    
    private void setEnabled(boolean value, Component... components) {
        for (Component c : components) {
            c.setEnabled(value);
        }
    }
    
    /**
     * Update variables state according to selected Partner Link or Operation.
     * It can clear current variables if they have not suitable type and
     * disable variable fields if current operation doesn't support
     * input or output variables.
     */
    private void resetVariables() {
        Object element = cbxOperation.getSelectedItem();
        if (element == null) {
            setCurrInputVar(null);
            setCurrOutputVar(null);
        } else {
            assert element instanceof Operation;
            //
            Operation operation = (Operation)element;
            //
            if (inputVisible) {
                //
                // Leave old input and otput variables if the
                // Message type is not changed.
                // Otherwise clear current Variable.
                //
                Message inMessage = null;
                Input input = operation.getInput();
                if (input != null) {
                    NamedComponentReference<Message> varTypeRef =
                            input.getMessage();
                    if (varTypeRef != null) {
                        inMessage = varTypeRef.get();
                    }
                }
                //
                if (inMessage == null) {
                    setCurrInputVar(null);
                } else {
                    VirtualVariableContainer currInputVvc = getCurrInputVar();
                    if (currInputVvc != null) {
                        Message varType = currInputVvc.getType().getMessage();
                        if (varType != null && !varType.equals(inMessage)) {
                            setCurrInputVar(null);
                        }
                    }
                    
                }
                
                tblInputParts.setMessage(inMessage);
                //
            }
            //
            if (outputVisible) { // && isOutputVarEnabled) {
                Message outMessage = null;
                Output output = operation.getOutput();
                if (output != null) {
                    NamedComponentReference<Message> varTypeRef =
                            output.getMessage();
                    if (varTypeRef != null) {
                        outMessage = varTypeRef.get();
                    }
                }
                //
                if (outMessage == null) {
                    setCurrOutputVar(null);
                } else {
                    VirtualVariableContainer currOutputVvc = getCurrOutputVar();
                    if (currOutputVvc != null) {
                        Message varType = currOutputVvc.getType().getMessage();
                        if (varType != null && !varType.equals(outMessage)) {
                            setCurrInputVar(null);
                        }
                    }
                }
                
                tblOutputParts.setMessage(outMessage);
                //
            }
        }
        updateEnabledStateForInputUI();
        updateEnabledStateForOutputUI();
    }
    
    
    public Lookup getLookup() {
        return myEditor.getLookup();
    }
    
    
    public BpelEntity getEditedObject() {
        return (BpelEntity) myEditor.getEditedObject();
    }
    
    
    private TableModel createToPartsTableModel(Message message) {
        DefaultTableModel result = new DefaultTableModel(
                new Object[] { "From Variable", "To Part"}, 0);
        
        if (message != null) {
            Collection<Part> parts = message.getParts();

            for (Part part : parts) {
                result.addRow(new Object[] { null, part });
            }
        }
        
        return result;
    }

    
    private TableModel createFromPartsTableModel(Message message) {
        DefaultTableModel result = new DefaultTableModel(
                new Object[] { "From Part", "To Variable"}, 0);
        
        if (message != null) {
            Collection<Part> parts = message.getParts();

            for (Part part : parts) {
                result.addRow(new Object[] { part, null });
            }
        }
        
        return result;
    }
    
    
    /**
     * This method allows to globally mark the controls
     * for the output variabl as disabled.
     * It is used for the Reply activity in the "reply fault" mode.
     */
    public void setOutputVarEnabled(boolean newValue) {
        isOutputVarEnabled = newValue;
        updateEnabledStateForOutputUI();
    }
    
    
    public VirtualVariableContainer getCurrInputVar() {
        return currInputVar;
    }
    
    public void setCurrInputVar(VirtualVariableContainer newValue) {
        if ((currInputVar == null && newValue != null) ||
                (currInputVar != null && !currInputVar.equals(newValue))) {
            currInputVar = newValue;
            fldInputVariable.setText(
                    currInputVar == null ? "" : currInputVar.getName());
        }
    }
    
    public VirtualVariableContainer getCurrOutputVar() {
        return currOutputVar;
    }
    
    public void setCurrOutputVar(VirtualVariableContainer newValue) {
        if ((currOutputVar == null && newValue != null) ||
                (currOutputVar != null && !currOutputVar.equals(newValue))) {
            currOutputVar = newValue;
            fldOutputVariable.setText(
                    currOutputVar == null ? "" : currOutputVar.getName());
        }
    }
    
    public void setCurrVariableName(String name) {
        fldVariableName.setText(name);
    }
    
    public String getCurrVariableName() {
        return fldVariableName.getText();
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public boolean doFastValidation() {
                    return true;
                }
                
                public boolean doDetailedValidation() {
                    int plIndex = cbxPartnerLink.getSelectedIndex();
                    if (plIndex == -1) {
                        addReasonKey("ERR_EMPTY_PARTNER_LINK"); //NOI18N
                    } else {
                        int operCount = cbxOperation.getItemCount();
                        if (operCount == 0) {
                            addReasonKey("ERR_PARTNER_LINK_WITHOUT_OPERATIONS"); //NOI18N
                        } else {
                            int operIndex = cbxOperation.getSelectedIndex();
                            if (operIndex == -1) {
                                addReasonKey("ERR_EMPTY_OPERATION"); //NOI18N
                            }
                            //
                            // Check operation parameters types
                            if (inputVisible && !isCorrectType(true, currInputVar)) {
                                addReasonKey("ERR_INCORRECT_INPUT_VAR_TYPE"); //NOI18N
                            }
                            //
                            if (outputVisible && isOutputVarEnabled &&
                                    !isCorrectType(false, currOutputVar)) {
                                addReasonKey("ERR_INCORRECT_OUTPUT_VAR_TYPE"); //NOI18N
                            }
                        }
                    }
                    //
                    return isReasonsListEmpty();
                }
                
            };
        }
        return myValidator;
    }
    
    /**
     * Check if the variable has the correct type.
     */
    private boolean isCorrectType(boolean isInput, VirtualVariableContainer vvc) {
        Object item = cbxOperation.getSelectedItem();
        if (item != null) {
            Operation operation = (Operation)item;
            Message requiredType = null;
            if (isInput) {
                Input input = operation.getInput();
                if (input != null) {
                    NamedComponentReference<Message> inputMessage =
                            input.getMessage();
                    if (inputMessage != null) {
                        requiredType = inputMessage.get();
                    }
                }
            } else {
                Output output = operation.getOutput();
                if (output != null) {
                    NamedComponentReference<Message> outputMessage =
                            output.getMessage();
                    if (outputMessage != null) {
                        requiredType = outputMessage.get();
                    }
                }
            }
            //
            if (requiredType != null) {
                if (vvc != null) {
                    Message varType = vvc.getType().getMessage();
                    if (varType != null) {
                        return varType.equals(requiredType);
                    }
                }
            }
        }
        //
        return true;
    }
    
    public Operation getCurrentOperation() {
        Object operationObj = cbxOperation.getSelectedItem();
        return operationObj == null ? null : (Operation)operationObj;
    }

    public JTable getTblInputParts() {
        return tblInputParts;
    }
    
    
    public JTable getTblOutputParts() {
        return tblOutputParts;
    }

    public JRadioButton getRbtnInputParts() {
        return rbtnInputParts;
    }

    public JRadioButton getRbtnInputVariable() {
        return rbtnInputVariable;
    }

    public JRadioButton getRbtnOutputParts() {
        return rbtnOutputParts;
    }

    public JRadioButton getRbtnOutputVariable() {
        return rbtnOutputVariable;
    }
    
    
// ==============================================================
// Fields accessors
// ==============================================================
    
    public JButton getBtnChooseInputVariable() {
        return btnChooseInputVariable;
    }
    
    public JButton getBtnChooseOutputVariable() {
        return btnChooseOutputVariable;
    }
    
    public JButton getBtnNewInputVariable() {
        return btnNewInputVariable;
    }
    
    public JButton getBtnNewOutputVariable() {
        return btnNewOutputVariable;
    }
    
    public JComboBox getCbxOperation() {
        return cbxOperation;
    }
    
    public JComboBox getCbxPartnerLink() {
        return cbxPartnerLink;
    }
    
    public JTextField getFldInputVariable() {
        return fldInputVariable;
    }
    
    public JTextField getFldOutputVariable() {
        return fldOutputVariable;
    }
    
    public JTextField getFldVariableName() {
        return fldVariableName;
    }

    
    public void setConfigurationListener(ConfigurationListener newValue) {
        myListener = newValue;
    }

    
    private Message getSelectedOperationInputMessage() {
        Object selectedOperation = getCbxOperation().getSelectedItem();
        if (!(selectedOperation instanceof Operation)) {
            return null;
        }
        
        Operation operation = (Operation) selectedOperation;
        BpelEntity bpelEntity = getEditedObject();
        
        NamedComponentReference<Message> messageReference = null;
        
        
//        if (bpelEntity instanceof Invoke) {
            Input input = operation.getInput();
            messageReference = (input == null) ? null : input.getMessage();
//        } else {
//            Output output = operation.getOutput();
//            messageReference = (output == null) ? null : output.getMessage();
//        }
        
        return (messageReference == null) ? null : messageReference.get();
    }

    
    private Message getSelectedOperationOutputMessage() {
        Object selectedOperation = getCbxOperation().getSelectedItem();
        if (!(selectedOperation instanceof Operation)) {
            return null;
        }
        
        Operation operation = (Operation) selectedOperation;
        BpelEntity bpelEntity = getEditedObject();
        
        NamedComponentReference<Message> messageReference = null;
        
        
//        if (bpelEntity instanceof Invoke) {
            Output output = operation.getOutput();
            messageReference = (output == null) ? null : output.getMessage();
//        } else {
//            Input input = operation.getInput();
//            messageReference = (input == null) ? null : input.getMessage();
//        }
        
        return (messageReference == null) ? null : messageReference.get();
    }
    
    
    private void updateEnabledStateForInputUI() {
        boolean radioButtonsEnabled;
        boolean variableEnabled;
        boolean partsEnabled;
        
        if (getSelectedOperationInputMessage() == null) {
            radioButtonsEnabled = false;
            variableEnabled = false;
            partsEnabled = false;
        } else {
            radioButtonsEnabled = true;
            variableEnabled = rbtnInputVariable.isSelected();
            partsEnabled = rbtnInputParts.isSelected();
        }
        setEnabled(radioButtonsEnabled, rbtnInputVariable, rbtnInputParts);
        setEnabled(variableEnabled, fldInputVariable, 
                btnNewInputVariable, btnChooseInputVariable);
        setEnabled(partsEnabled, tblInputParts);
    }
    
    private void updateEnabledStateForOutputUI() {
        boolean radioButtonsEnabled;
        boolean variableEnabled;
        boolean partsEnabled;
        
        if (getSelectedOperationOutputMessage() == null || !isOutputVarEnabled) 
        {
            radioButtonsEnabled = false;
            variableEnabled = false;
            partsEnabled = false;
        } else {
            radioButtonsEnabled = true;
            variableEnabled = rbtnOutputVariable.isSelected();
            partsEnabled = rbtnOutputParts.isSelected();
        }

        setEnabled(radioButtonsEnabled, rbtnOutputVariable, rbtnOutputParts);
        setEnabled(variableEnabled, fldOutputVariable, 
                btnNewOutputVariable, btnChooseOutputVariable);
        setEnabled(partsEnabled, tblOutputParts);
    }
    
    
    public interface ConfigurationListener {
        void partnerLinkChanged();
        void operationChanged();
    }
}
