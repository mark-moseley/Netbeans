/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.Introspector;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.actions.RegisterMBeanAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask which MBean to instantiate and register.
 * @author  tl156378
 */
public class RegisterMBeanPanel extends javax.swing.JPanel 
        implements ItemListener, DocumentListener {
    
    /** class to add registration of MBean */
    private JavaClass currentClass;
    private JavaClass mbeanClass = null;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    private boolean isMBean = false;
    private boolean isExistingClass = false;
    
    /**
     * Returns the current Java class.
     * @return <CODE>JavaClass</CODE> current specified Java class
     */
    public JavaClass getJavaClass() {
        return currentClass;
    }
    
    /**
     * Returns the current user defined MBean class.
     * @return <CODE>JavaClass</CODE> specified MBean class
     */
    public JavaClass getMBeanClass() {
        return mbeanClass;
    }
    
    /**
     * Returns if the current user defined MBean class is StandardMBean class.
     * @return <CODE>boolean</CODE> true only if StandardMBean class is selected
     */
    public boolean standardMBeanSelected() {
        return standardMBeanRadioButton.isSelected();
    }
    
    /**
     * Returns the current user defined MBean objectName.
     * @return <CODE>String</CODE> specified ObjectName
     */
    public String getMBeanObjectName() {
        if (userMBeanRadioButton.isSelected())
            return objectNameTextField.getText();
        else
            return stdMBObjectNameTextField.getText();
    }
    
    /**
     * Returns the current user defined class name.
     * @return <CODE>String</CODE> specified class name
     */
    public String getClassName() {
        if (standardMBeanRadioButton.isSelected())
            return classNameTextField.getText();
        else
            return WizardConstants.NULL; 
    }
    
    /**
     * Returns the current user defined class name.
     * @return <CODE>String</CODE> specified interface name
     */
    public String getInterfaceName() {
        if (standardMBeanRadioButton.isSelected()) {
            String interfaceName = (String) interfaceComboBox.getSelectedItem();
            if (bundle.getString("LBL_GeneratedInterface").equals(interfaceName)) // NOI18N
                return WizardConstants.NULL;
            else
                return interfaceName;
        } else
            return WizardConstants.NULL;
    }
    
    /**
     * Returns the current user defined constructor signature.
     * @return <CODE>String</CODE> signature of choosed constructor
     */
    public String getConstructorSignature() {
        if (userMBeanRadioButton.isSelected())
            return (String) constructorComboBox.getSelectedItem();
        else {
            String construct = (String) stdMBConstructorComboBox.getSelectedItem();
            if (bundle.getString("LBL_StandardMBeanDefaultConstructor").equals(construct)) // NOI18N
                return WizardConstants.NULL;
            else
                return construct;
        }
    }
    
    /** 
     * Creates new form RegisterMBeanPanel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public RegisterMBeanPanel(Node node) {
        bundle = NbBundle.getBundle(RegisterMBeanPanel.class);
        
        initComponents();
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        JavaClass[] mbeanClasses = WizardHelpers.getMBeanClasses(
                WizardHelpers.getProject(fo));
        
        for (int i = 0; i < mbeanClasses.length; i++) {
            mbeanClassComboBox.addItem(mbeanClasses[i].getName());
        }
        
        Resource rc = JavaModel.getResource(fo);
        currentClass = WizardHelpers.getJavaClass(rc,fo.getName());
        
        // init tags
        userMBeanRadioButton.setSelected(true);
                
        // init labels
        Mnemonics.setLocalizedText(userMBeanRadioButton,
                     bundle.getString("LBL_RegisterUserMBean")); // NOI18N
        Mnemonics.setLocalizedText(standardMBeanRadioButton,
                     bundle.getString("LBL_RegisterStandardMBean")); // NOI18N
        Mnemonics.setLocalizedText(mbeanClassLabel,
                     bundle.getString("LBL_MBean_Class")); // NOI18N
        Mnemonics.setLocalizedText(classNameLabel,
                     bundle.getString("LBL_Class")); // NOI18N
        Mnemonics.setLocalizedText(objectNameLabel,
                     bundle.getString("LBL_ObjectName")); // NOI18N
        Mnemonics.setLocalizedText(stdMBObjectNameLabel,
                     bundle.getString("LBL_StandardMBean_ObjectName")); // NOI18N
        Mnemonics.setLocalizedText(constructorLabel,
                     bundle.getString("LBL_Constructor")); // NOI18N
        Mnemonics.setLocalizedText(stdMBConstructorLabel,
                     bundle.getString("LBL_StandardMBean_Constructor")); // NOI18N
        Mnemonics.setLocalizedText(interfaceLabel,
                     bundle.getString("LBL_Interface")); // NOI18N
        
        //for accesibility
        userMBeanRadioButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_USER_MBEAN")); // NOI18N
        userMBeanRadioButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_USER_MBEAN_DESCRIPTION")); // NOI18N
        mbeanClassComboBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_MBEAN_CLASS")); // NOI18N
        mbeanClassComboBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_MBEAN_CLASS_DESCRIPTION")); // NOI18N
        objectNameTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_OBJECTNAME")); // NOI18N
        objectNameTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_OBJECTNAME_DESCRIPTION")); // NOI18N
        constructorComboBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_CONSTRUCTOR")); // NOI18N
        constructorComboBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_CONSTRUCTOR_DESCRIPTION")); // NOI18N
        standardMBeanRadioButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STANDARD_MBEAN")); // NOI18N
        standardMBeanRadioButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STANDARD_MBEAN_DESCRIPTION")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_CLASSNAME")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_CLASSNAME_DESCRIPTION")); // NOI18N
        stdMBObjectNameTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_OBJECTNAME")); // NOI18N
        stdMBObjectNameTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_OBJECTNAME_DESCRIPTION")); // NOI18N
        interfaceComboBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_INTERFACE")); // NOI18N
        interfaceComboBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_INTERFACE_DESCRIPTION")); // NOI18N
        stdMBConstructorComboBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_CONSTRUCTOR")); // NOI18N
        stdMBConstructorComboBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_CONSTRUCTOR_DESCRIPTION")); // NOI18N
    }
    
    private boolean isAcceptable() {
        return ((standardMBeanSelected() && isExistingClass) || 
                (!standardMBeanSelected() && isMBean));
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options 
     * according to the user's settings.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_RegisterMBeanAction.Title"); // NOI18N
        btnOK = new JButton(bundle.getString("LBL_OK")); //NOI18N
        btnOK.setEnabled(isAcceptable());
        
        //set listeners
        ((JTextField) mbeanClassComboBox.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(this);
        mbeanClassComboBox.addItemListener(this);
        classNameTextField.getDocument().addDocumentListener(
                new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateState(null);
                } catch (BadLocationException excep) {}
            }
            
            public void removeUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateState(null);
                } catch (BadLocationException excep) {}
            }
            
            public void changedUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateState(null);
                } catch (BadLocationException excep) {}
            }
        });
        
        //init state
        updateComponentsState();
        updateState((String) mbeanClassComboBox.getSelectedItem());
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                btnOK,                      //initial value
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(RegisterMBeanAction.class),
                (ActionListener) null
         ));
                
         if (returned == btnOK) {
             return true;
         }
         return false;
    }
    
    private void updateState(String currentMBeanClass) {
        updateComponentsState();
        if (standardMBeanSelected()) {
            String className = classNameTextField.getText();
            updateIntfAndConst(className);
        } else
            updateConstructors(currentMBeanClass);
    }
    
    private void updateComponentsState() {
        boolean standardMBean = standardMBeanSelected();
        // update state of user MBean use case
        mbeanClassLabel.setEnabled(!standardMBean);
        mbeanClassComboBox.setEnabled(!standardMBean);
        objectNameLabel.setEnabled(!standardMBean);
        objectNameTextField.setEnabled(!standardMBean);
        constructorLabel.setEnabled(!standardMBean);
        constructorComboBox.setEnabled(!standardMBean);
        // update state of StandardMBean use case
        classNameLabel.setEnabled(standardMBean);
        classNameTextField.setEnabled(standardMBean);
        stdMBObjectNameLabel.setEnabled(standardMBean);
        stdMBObjectNameTextField.setEnabled(standardMBean);
        interfaceLabel.setEnabled(standardMBean);
        interfaceComboBox.setEnabled(standardMBean);
        stdMBConstructorLabel.setEnabled(standardMBean);
        stdMBConstructorComboBox.setEnabled(standardMBean);
    }
    
    private void updateIntfAndConst(String className) {
        stateLabel.setText(""); // NOI18N
        JavaModelPackage pkg = (JavaModelPackage) currentClass.refImmediatePackage();
        JavaClass clazz = (JavaClass) pkg.getJavaClass().resolve(className);
        //clear combobox list of interfaces and constructors
        interfaceComboBox.removeAllItems();
        stdMBConstructorComboBox.removeAllItems();
        isExistingClass = ((clazz != null) && (!clazz.getClass().getName().startsWith(
                "org.netbeans.jmi.javamodel.UnresolvedClass"))); // NOI18N
        stdMBObjectNameLabel.setEnabled(isExistingClass);
        stdMBObjectNameTextField.setEnabled(isExistingClass);
        interfaceLabel.setEnabled(isExistingClass);
        interfaceComboBox.setEnabled(isExistingClass);
        stdMBConstructorLabel.setEnabled(isExistingClass);
        stdMBConstructorComboBox.setEnabled(isExistingClass);
        if (isExistingClass) {
            stdMBObjectNameTextField.setText(WizardHelpers.reversePackageName(
                    WizardHelpers.getPackageName(className)) +
                    ":type=" + WizardHelpers.getClassName(className)); // NOI18N
            interfaceComboBox.addItem(bundle.getString("LBL_GeneratedInterface")); // NOI18N
            String[] interfaces = WizardHelpers.getInterfaceNames(clazz);
            boolean hasIntf = (interfaces.length > 0);
            if (hasIntf) {
                for (int i = 0; i < interfaces.length ; i++) {
                    interfaceComboBox.addItem(interfaces[i]);
                }
            }
            //select first item
            interfaceComboBox.setSelectedItem(bundle.getString("LBL_GeneratedInterface")); // NOI18N
            
            //discovery of class constructors
            Constructor[] constructors =
                    WizardHelpers.getConstructors(clazz);
            if (constructors.length > 0) {
                stdMBConstructorComboBox.addItem(
                    bundle.getString("LBL_StandardMBeanDefaultConstructor")); // NOI18N
                for (int i = 0; i < constructors.length; i++) {
                    Constructor currentConstruct = constructors[i];
                    List params = currentConstruct.getParameters();
                    String construct = clazz.getSimpleName() + "("; // NOI18N
                    for (Iterator<Parameter> it = params.iterator(); it.hasNext();) {
                        construct += WizardHelpers.getClassName(
                                it.next().getType().getName());
                        if (it.hasNext())
                            construct += ", "; // NOI18N
                    }
                    construct += ")"; // NOI18N
                    stdMBConstructorComboBox.addItem(construct);
                }
                //select first row
                stdMBConstructorComboBox.setSelectedItem(
                        bundle.getString("LBL_StandardMBeanDefaultConstructor")); // NOI18N
            } else {
                stdMBConstructorComboBox.setEnabled(false);
                stateLabel.setText(bundle.getString("LBL_ClassWithNoConstructor")); // NOI18N
            }
        } else {
            if (className.equals("")) // NOI18N
                stateLabel.setText(""); // NOI18N
            else
                stateLabel.setText(bundle.getString("LBL_ClassNotExist")); // NOI18N
        }
        btnOK.setEnabled(isAcceptable());
    }
    
    private void updateConstructors(String currentMBeanClass) {
        //clear the comboBox list of MBean constructors
        constructorComboBox.removeAllItems();
        //clear information message
        stateLabel.setText( ""); // NOI18N
        JavaModelPackage pkg = (JavaModelPackage) currentClass.refImmediatePackage();
        mbeanClass = (JavaClass) pkg.getJavaClass().resolve(currentMBeanClass);
        if ((mbeanClass != null) && (!mbeanClass.getClass().getName().startsWith(
                "org.netbeans.jmi.javamodel.UnresolvedClass"))) // NOI18N
            isMBean = Introspector.isMBeanClass(mbeanClass);
        else
            isMBean = false;
        objectNameLabel.setEnabled(isMBean);
        objectNameTextField.setEnabled(isMBean);
        constructorLabel.setEnabled(isMBean);
        constructorComboBox.setEnabled(isMBean);
        if (isMBean) {
            objectNameTextField.setText(WizardHelpers.reversePackageName(
                    WizardHelpers.getPackageName(mbeanClass.getName())) +
                    ":type=" + mbeanClass.getSimpleName()); // NOI18N  
            Constructor[] constructors =
                    WizardHelpers.getConstructors(mbeanClass);
            if (constructors.length > 0) {
                constructorComboBox.setEnabled(true);
                for (int i = 0; i < constructors.length; i++) {
                    Constructor currentConstruct = constructors[i];
                    List params = currentConstruct.getParameters();
                    String construct = mbeanClass.getSimpleName() + "("; // NOI18N
                    for (Iterator<Parameter> it = params.iterator(); it.hasNext();) {
                        construct += WizardHelpers.getClassName(
                                it.next().getType().getName());
                        if (it.hasNext())
                            construct += ", "; // NOI18N
                    }
                    construct += ")"; // NOI18N
                    constructorComboBox.addItem(construct);
                }
                //select first row
                constructorComboBox.setSelectedItem(0); // NOI18N
            } else {
                stateLabel.setText(bundle.getString("LBL_ClassWithNoConstructor")); // NOI18N
            }
        }
        if (!isMBean)
            stateLabel.setText(bundle.getString("LBL_NotMBeanClass")); // NOI18N
        btnOK.setEnabled(isAcceptable());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mbeanGroup = new javax.swing.ButtonGroup();
        northPanel = new javax.swing.JPanel();
        objectNameLabel = new javax.swing.JLabel();
        objectNameTextField = new javax.swing.JTextField();
        stateLabel = new javax.swing.JLabel();
        mbeanClassComboBox = new javax.swing.JComboBox();
        classNameTextField = new javax.swing.JTextField();
        classNameLabel = new javax.swing.JLabel();
        mbeanClassLabel = new javax.swing.JLabel();
        userMBeanRadioButton = new javax.swing.JRadioButton();
        standardMBeanRadioButton = new javax.swing.JRadioButton();
        stdMBObjectNameLabel = new javax.swing.JLabel();
        stdMBObjectNameTextField = new javax.swing.JTextField();
        interfaceLabel = new javax.swing.JLabel();
        interfaceComboBox = new javax.swing.JComboBox();
        constructorLabel = new javax.swing.JLabel();
        constructorComboBox = new javax.swing.JComboBox();
        stdMBConstructorLabel = new javax.swing.JLabel();
        stdMBConstructorComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.GridBagLayout());

        objectNameLabel.setLabelFor(objectNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(objectNameLabel, gridBagConstraints);

        objectNameTextField.setName("objectNameTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(objectNameTextField, gridBagConstraints);

        stateLabel.setForeground(java.awt.SystemColor.activeCaption);
        stateLabel.setMinimumSize(new java.awt.Dimension(0, 20));
        stateLabel.setName("stateLabel");
        stateLabel.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        northPanel.add(stateLabel, gridBagConstraints);

        mbeanClassComboBox.setEditable(true);
        mbeanClassComboBox.setName("mbeanClassComboBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(mbeanClassComboBox, gridBagConstraints);

        classNameTextField.setName("classNameTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(classNameTextField, gridBagConstraints);

        classNameLabel.setLabelFor(classNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(classNameLabel, gridBagConstraints);

        mbeanClassLabel.setLabelFor(mbeanClassComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(mbeanClassLabel, gridBagConstraints);

        mbeanGroup.add(userMBeanRadioButton);
        userMBeanRadioButton.setName("userMBeanRadioButton");
        userMBeanRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userMBeanRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        northPanel.add(userMBeanRadioButton, gridBagConstraints);

        mbeanGroup.add(standardMBeanRadioButton);
        standardMBeanRadioButton.setName("standardMBeanRadioButton");
        standardMBeanRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stabdardMBeanRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        northPanel.add(standardMBeanRadioButton, gridBagConstraints);

        stdMBObjectNameLabel.setLabelFor(stdMBObjectNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(stdMBObjectNameLabel, gridBagConstraints);

        stdMBObjectNameTextField.setName("stdMBObjectNameTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(stdMBObjectNameTextField, gridBagConstraints);

        interfaceLabel.setLabelFor(interfaceComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(interfaceLabel, gridBagConstraints);

        interfaceComboBox.setMinimumSize(new java.awt.Dimension(270, 25));
        interfaceComboBox.setName("interfaceComboBox");
        interfaceComboBox.setPreferredSize(new java.awt.Dimension(270, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(interfaceComboBox, gridBagConstraints);

        constructorLabel.setLabelFor(constructorComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(constructorLabel, gridBagConstraints);

        constructorComboBox.setName("constructorComboBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(constructorComboBox, gridBagConstraints);

        stdMBConstructorLabel.setLabelFor(stdMBConstructorComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 34, 0, 11);
        northPanel.add(stdMBConstructorLabel, gridBagConstraints);

        stdMBConstructorComboBox.setName("stdMBConstructorComboBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        northPanel.add(stdMBConstructorComboBox, gridBagConstraints);

        add(northPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void stabdardMBeanRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stabdardMBeanRadioButtonActionPerformed
        updateState(null);
    }//GEN-LAST:event_stabdardMBeanRadioButtonActionPerformed

    private void userMBeanRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userMBeanRadioButtonActionPerformed
        String newMBeanClass = (String) mbeanClassComboBox.getSelectedItem();
        updateState(newMBeanClass);
    }//GEN-LAST:event_userMBeanRadioButtonActionPerformed
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JComboBox constructorComboBox;
    private javax.swing.JLabel constructorLabel;
    private javax.swing.JComboBox interfaceComboBox;
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JComboBox mbeanClassComboBox;
    private javax.swing.JLabel mbeanClassLabel;
    private javax.swing.ButtonGroup mbeanGroup;
    private javax.swing.JPanel northPanel;
    private javax.swing.JLabel objectNameLabel;
    private javax.swing.JTextField objectNameTextField;
    private javax.swing.JRadioButton standardMBeanRadioButton;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JComboBox stdMBConstructorComboBox;
    private javax.swing.JLabel stdMBConstructorLabel;
    private javax.swing.JLabel stdMBObjectNameLabel;
    private javax.swing.JTextField stdMBObjectNameTextField;
    private javax.swing.JRadioButton userMBeanRadioButton;
    // End of variables declaration//GEN-END:variables
    
    public void itemStateChanged(ItemEvent e) {
        String newMBeanClass = (String) mbeanClassComboBox.getSelectedItem();
        updateState(newMBeanClass);
    }
    
    public void insertUpdate(DocumentEvent e) {
        Document doc = e.getDocument();
        try {
            String newMBeanClass = doc.getText(0,doc.getLength());
            updateState(newMBeanClass);
        } catch (BadLocationException excep) {}
    }

    
    public void removeUpdate(DocumentEvent e) {
        Document doc = e.getDocument();
        try {
            String newMBeanClass = doc.getText(0,doc.getLength());
            updateState(newMBeanClass);
        } catch (BadLocationException excep) {}
    }

    public void changedUpdate(DocumentEvent e) {
        Document doc = e.getDocument();
        try {
            String newMBeanClass = doc.getText(0,doc.getLength());
            updateState(newMBeanClass);
        } catch (BadLocationException excep) {}
    }
}
