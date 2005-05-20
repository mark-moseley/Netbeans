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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;


/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerPropertiesVisualPanel extends JPanel {
    
    private final Set listeners = new HashSet();
    
    /** Creates a new instance of AddServerPropertiesVisualPanel */
    public AddServerPropertiesVisualPanel() {
        init();
        setName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "TITLE_ServerProperties")); //NOI18N 
    }
    
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l ) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    
    
    private void somethingChanged() {
        fireChangeEvent();
    }
    
    
    private void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    
    public boolean isLocalServer(){
        if (serverType.getSelectedItem().equals("Local"))
            return true;
        else
            return false;
    }
    public String getHost(){
        return hostField.getText().trim();
    }
    public String getPort(){
        return portField.getText().trim();
    }
    public String getUser(){
        return userField.getText();
    }
    public String getPassword(){
        return new String(passwordField.getPassword());
    }
    public String getDomainPath(){
        return domainPathField.getText();
    }
    public String getDomain(){
        
  
        return (String)domainField.getSelectedItem();
    }
   
    private void domainChanged(){
        String domain = (String)domainField.getSelectedItem();
        DomainComboModel model = (DomainComboModel)domainField.getModel();
        
        String path = model.getCurrentPath();
        if (!path.equals("")){
            domainPathField.setText(path);
        } 
    //    serverChanged();
        fireChangeEvent();
        
    }
    
    
    private void serverTypeChanged(){
        
        if (serverType.getSelectedItem().equals("Local")){  //NOI18N 
            domainLabel.setVisible(true);
            domainField.setVisible(true);
            
            
            domainPathLabel.setVisible(true);
            domainPathField.setVisible(true);
//            browseButton.setVisible(true);

            hostField.setEditable(false);
      //      portField.setEditable(false);
            
//            serverChanged();
            
        }else{  // REMOTE
            
            domainLabel.setVisible(false);
            domainField.setVisible(false);
            
            domainPathLabel.setVisible(false);
            domainPathField.setVisible(false);
//            browseButton.setVisible(false);
            
//            serverListField.setVisible(false);
//            serverListLabel.setVisible(false);
            
            hostField.setEditable(true);
            portField.setEditable(true);
        }
        
        somethingChanged();
    }
    
    private void init(){
        
        // Object[] domainsList = WLPluginUtils.getRegisteredDomains().keySet().toArray(new String[WLPluginUtils.getRegisteredDomains().keySet().size()]);
        
        java.awt.GridBagConstraints gridBagConstraints;
        
        label1 = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "TXT_PROPERTY_TEXT")); //NOI18N
        
        serverType = new JComboBox(new String[]{"Local","Remote"});//NOI18N
        serverType.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                serverTypeChanged();
            }
        });
        
        domainLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Domain")); //NOI18N
        domainPathLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_DomainPath"));//NOI18N
        domainPathField = new JTextField();
               
        panel1 = new JPanel();
        domainField = new JComboBox(new DomainComboModel(JBPluginUtils.getRegisteredDomains()));
        //domainField.setEditable(true);
        domainField.addActionListener(new ActionListener(){
            
            
            public void actionPerformed(ActionEvent e) {
                domainChanged();
            }
        });
        
        
        
        hostLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Host"));//NOI18N
        hostField = new JTextField();
        hostField.setEditable(false);
        hostField.addKeyListener(new SomeChangesListener());
        
        
        
        portLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port"));//NOI18N
        portField = new JTextField();
        //portField.setEditable(false);
        portField.addKeyListener(new SomeChangesListener());
        
        
        
        
        userLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_User"));//NOI18N
        userField = new JTextField();
        userField.addKeyListener(new SomeChangesListener());
        
        passwordLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Password"));//NOI18N
        passwordField = new JPasswordField();
        passwordField.addKeyListener(new SomeChangesListener());
        
       
        setLayout(new java.awt.GridBagLayout());
        
        setFocusable(false);
        
        setMinimumSize(new java.awt.Dimension(280, 217));
       //   setNextFocusableComponent(domainPathField);
        
        
        //-------------- some label --------------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(label1, gridBagConstraints);
        
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        
        add(serverType, gridBagConstraints);
        
        
        //-------------- domain ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(domainLabel, gridBagConstraints);
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(domainField, gridBagConstraints);
        
        
        
        
        //-------------- domain path ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(domainPathLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(domainPathField, gridBagConstraints);
        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
//        add(browseButton, gridBagConstraints);
//        
        
        
        
        
        
        //-------------- host ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(hostLabel, gridBagConstraints);
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(hostField, gridBagConstraints);
        
        
        //-------------- port ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(portLabel, gridBagConstraints);
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(portField, gridBagConstraints);
        
        
        //-------------- User ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(userLabel, gridBagConstraints);
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(userField, gridBagConstraints);
        
        
        
        //-------------- Password ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(passwordLabel, gridBagConstraints);
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 5);
        add(passwordField, gridBagConstraints);
        
        //-------------  panell to fill out free space ------------------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        
        domainPathField.setEnabled(false);
        
        userField.setVisible(false);
        userLabel.setVisible(false);
        passwordField.setVisible(false);
        passwordLabel.setVisible(false);
        
        
        serverType.setVisible(false);
        
        add(panel1, gridBagConstraints);
        
        hostField.setText("localhost");//NOI18N
        portField.setText("8080");//NOI18N
    //    serverTypeChanged();
        domainChanged();
        
    }
    
    
    
    class SomeChangesListener implements KeyListener{
        
        public void keyTyped(KeyEvent e){}
        
        public void keyPressed(KeyEvent e){}
        
        public void keyReleased(KeyEvent e){ somethingChanged();}
        
    }
    
    
    
    
    
//    private javax.swing.JButton    browseButton; // Browse domain location
    
    private javax.swing.JComboBox  domainField;  // Domain name (list of registered domains) can be edited
    private javax.swing.JTextField domainPathField;  //
    
    private javax.swing.JLabel     domainLabel;
    private javax.swing.JLabel     domainPathLabel;
    
    private javax.swing.JLabel     label1;
    private javax.swing.JPanel     panel1;
    
    private javax.swing.JLabel     hostLabel;
    private javax.swing.JTextField hostField;
    
    private javax.swing.JLabel     portLabel;
    private javax.swing.JTextField portField;
    
    private javax.swing.JLabel     userLabel;
    private javax.swing.JTextField userField;
    
    private javax.swing.JLabel     passwordLabel;
    private javax.swing.JPasswordField passwordField;
    
    private javax.swing.JComboBox  serverType;  // Local or Remote
    
    
    private String browseDomainLocation(){
        String insLocation = null;
        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(this, NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_ChooseButton")); //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }
    
    private JFileChooser getJFileChooser(){
        JFileChooser chooser = new JFileChooser();
        
        chooser.setDialogTitle("LBL_Chooser_Name"); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new dirFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText("LBL_Chooser_Name"); //NOI18N
        
        chooser.getAccessibleContext().setAccessibleName("LBL_Chooser_Name"); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription("LBL_Chooser_Name"); //NOI18N
        
        return chooser;
    }
    
    private static class dirFilter extends javax.swing.filechooser.FileFilter {
        
        public boolean accept(File f) {
            if(!f.exists() || !f.canRead() || !f.isDirectory() ) {
                return false;
            }else{
                return true;
            }
        }
        
        public String getDescription() {
            return NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_DirType"); //NOI18N
        }
        
    }
    
}


class DomainComboModel extends AbstractListModel implements ComboBoxModel{
    private int current = 0;
    private String currentVal = "";
    private String[][] domains = null;
    
    
    public void addDomain(String domain, String path){
        String[][] newDomains = new String[domains.length+1][2];
        int i = 0;
        for(;i<domains.length; i++){
            newDomains[i][0] = domains[i][0];
            newDomains[i][1] = domains[i][1];
        }
        newDomains[i][0] = domain;
        newDomains[i][1] = path;
        domains = newDomains;
        
    }
    
    public DomainComboModel(Hashtable domains){
        int len = domains.size();
        this.domains = new String[len][2];
        Enumeration en = domains.keys();
        
        int i = 0;
        while(en.hasMoreElements()){
            this.domains[i][0] = (String)en.nextElement();
            this.domains[i][1] = (String)domains.get(this.domains[i][0]);
            if(this.domains[i][0].equalsIgnoreCase("default")) //NOI18N
                current=i;
            i++;
        }
    }
    
    public Object  getSelectedItem() {
        if (current ==-1 )
            return currentVal;
        return domains[current][0];
    }
    
    public void setSelectedItem(Object anItem) {
        for (int i = 0; i < getSize(); i++){
            if (domains[i][0].equals(anItem)){
                current = i;
                fireContentsChanged(this, -1, -1);
                return;
            }
        }
        current = -1;
        //currentVal = (String)anItem;
        fireContentsChanged(this, -1, -1);
    }
    
    public Object getElementAt(int index){
        return domains[index][0];
    }
    
    public int 	getSize(){
        return domains.length;
    }
    //----------------------------------------------------
    
    public String getCurrentPath(){
        if (current == -1) return "";
        return domains[current][1];
    }
    
    public boolean hasDomain(String domain){
        for (int i = 0; i < getSize(); i++){
            if (domains[i][0].equals(domain)){
                return true;
            }
        }
        return false;
    }
    
}





