/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.NbBundle;

/** A single panel for a wizard - the GUI portion.
 *
 * @author vkraemer
 */
// TODO : force validation to key release instead of StateChange
public class CreateServerVisualPanel extends javax.swing.JPanel {
    
    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */
//    private final AddInstancePortsDefPanel panel;
//    private final TargetServerData targetData;
    private static javax.swing.SpinnerNumberModel adminPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel instanceHttpPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel adminJmxPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel jmsPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel orbPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel httpsPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel orbSslPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel orbMutualauthPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    
    /** Create the wizard panel and set up some basic properties. */
    public CreateServerVisualPanel() { 
        PortSetter tmp = new PortSetter();
        instanceHttpPortValue.addChangeListener(tmp);
        adminJmxPortValue.addChangeListener(tmp);
        jmsPortValue.addChangeListener(tmp);
        orbPortValue.addChangeListener(tmp);
        httpsPortValue.addChangeListener(tmp);
        orbSslPortValue.addChangeListener(tmp);
        orbMutualauthPortValue.addChangeListener(tmp);
        adminPortValue.addChangeListener(tmp);
        initComponents();
        
        // XXX picking defaults isn't safe... but it is the best we have for the moment.
        int incr = (new Random()).nextInt(100)+1; //Integer. parseInt(targetData.getPort()) - 4848; // .rng.nextInt(100) + 1;
        adminPortValue.setValue(new Integer(4848+incr));
        instanceHttpPortValue.setValue(new Integer(8080+incr));
        adminJmxPortValue.setValue(new Integer(8686+incr));
        jmsPortValue.setValue(new Integer(7676+incr));
        orbPortValue.setValue(new Integer(3700+incr));
        httpsPortValue.setValue(new Integer(8181+incr));
        orbSslPortValue.setValue(new Integer(3820+incr));
        orbMutualauthPortValue.setValue(new Integer(3920+incr));
        
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(CreateServerVisualPanel.class, "TITLE_ServerPortProperties"));
        //msgLabel.setText(NbBundle.getMessage(CreateServerVisualPanel.class, "Msg_ValidPort"));
    }
    
    Number getAdminPort() {
        return adminPortValue.getNumber();
    }
    
    Number getInstanceHttpPort() {
        return instanceHttpPortValue.getNumber();
    }
    
    Number getAdminJmxPort() {
        return adminJmxPortValue.getNumber();
    }
    
    Number getJmsPort() {
        return jmsPortValue.getNumber();
    }
    
    Number getOrbPort() {
        return orbPortValue.getNumber();
    }
    
    Number getOrbSslPort() {
        return orbSslPortValue.getNumber();
    }
    
    Number getOrbMutualAuthPort() {
        return orbMutualauthPortValue.getNumber();
    }
    
    Number getHttpSslPort() {
        return httpsPortValue.getNumber();
    }

    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    private class PortSetter implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent ce) {
            fireChangeEvent();
        }
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTextArea1 = new javax.swing.JTextArea();
        portConfPanel = new javax.swing.JPanel();
        adminJmxLbl = new javax.swing.JLabel();
        instancePortLbl = new javax.swing.JLabel();
        jmsPortLbl = new javax.swing.JLabel();
        orbListenerPortLbl = new javax.swing.JLabel();
        httpslPortLbl = new javax.swing.JLabel();
        orbSslPortLbl = new javax.swing.JLabel();
        orbMutualauthPortLbl = new javax.swing.JLabel();
        instanceHttpPort = new javax.swing.JSpinner();
        adminJmxPort = new javax.swing.JSpinner();
        jmsPort = new javax.swing.JSpinner();
        orbListenerPort = new javax.swing.JSpinner();
        httpsPort = new javax.swing.JSpinner();
        orbSslPort = new javax.swing.JSpinner();
        orbMutualauthPort = new javax.swing.JSpinner();
        adminPortLbl = new javax.swing.JLabel();
        adminPort = new javax.swing.JSpinner();

        setLayout(new java.awt.GridBagLayout());

        setFocusable(false);
        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("Step_ChooseUserDefinedLocalServer"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("AddUserDefinedLocalServerPanel_Desc"));
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("CreateServerVisualPanel_Desc"));
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 7, 5);
        add(jTextArea1, gridBagConstraints);
        jTextArea1.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("AddUserDefinedLocalServerPanel_Desc"));
        jTextArea1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("AddUserDefinedLocalServerPanel_Desc"));

        portConfPanel.setLayout(new java.awt.GridBagLayout());

        adminJmxLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_AdminJmxPort_Mnemonic").charAt(0));
        adminJmxLbl.setLabelFor(adminJmxPort);
        adminJmxLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_AdminJmxPort"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(adminJmxLbl, gridBagConstraints);

        instancePortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_InstancePort_Mnemonic").charAt(0));
        instancePortLbl.setLabelFor(instanceHttpPort);
        instancePortLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_InstancePort"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(instancePortLbl, gridBagConstraints);

        jmsPortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_JmsPort_Mnemonic").charAt(0));
        jmsPortLbl.setLabelFor(jmsPort);
        jmsPortLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_JmsPort"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(jmsPortLbl, gridBagConstraints);

        orbListenerPortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbListener_Mnemonic").charAt(0));
        orbListenerPortLbl.setLabelFor(orbListenerPort);
        orbListenerPortLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbListener"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(orbListenerPortLbl, gridBagConstraints);

        httpslPortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_HttpSslPort_Mnemonic").charAt(0));
        httpslPortLbl.setLabelFor(httpsPort);
        httpslPortLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_HttpSslPort"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(httpslPortLbl, gridBagConstraints);

        orbSslPortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbSslPort_Mnemonic").charAt(0));
        orbSslPortLbl.setLabelFor(orbSslPort);
        orbSslPortLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbSslPort"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(orbSslPortLbl, gridBagConstraints);

        orbMutualauthPortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbMutualauthPort_Mnemonic").charAt(0));
        orbMutualauthPortLbl.setLabelFor(orbMutualauthPort);
        orbMutualauthPortLbl.setText(org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbMutualauthPort"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 3);
        portConfPanel.add(orbMutualauthPortLbl, gridBagConstraints);

        instanceHttpPort.setModel(instanceHttpPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        portConfPanel.add(instanceHttpPort, gridBagConstraints);

        adminJmxPort.setModel(adminJmxPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        portConfPanel.add(adminJmxPort, gridBagConstraints);

        jmsPort.setModel(jmsPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        portConfPanel.add(jmsPort, gridBagConstraints);

        orbListenerPort.setModel(orbPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 5);
        portConfPanel.add(orbListenerPort, gridBagConstraints);

        httpsPort.setModel(httpsPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        portConfPanel.add(httpsPort, gridBagConstraints);

        orbSslPort.setModel(orbSslPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 5);
        portConfPanel.add(orbSslPort, gridBagConstraints);

        orbMutualauthPort.setModel(orbMutualauthPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 5, 4);
        portConfPanel.add(orbMutualauthPort, gridBagConstraints);

        adminPortLbl.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "MNM_adminPortLbl").charAt(0));
        adminPortLbl.setLabelFor(adminPort);
        adminPortLbl.setText(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "LBL_adminPortLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 3);
        portConfPanel.add(adminPortLbl, gridBagConstraints);

        adminPort.setModel(adminPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        portConfPanel.add(adminPort, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(portConfPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminJmxLbl;
    private javax.swing.JSpinner adminJmxPort;
    private javax.swing.JSpinner adminPort;
    private javax.swing.JLabel adminPortLbl;
    private javax.swing.JSpinner httpsPort;
    private javax.swing.JLabel httpslPortLbl;
    private javax.swing.JSpinner instanceHttpPort;
    private javax.swing.JLabel instancePortLbl;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JSpinner jmsPort;
    private javax.swing.JLabel jmsPortLbl;
    private javax.swing.JSpinner orbListenerPort;
    private javax.swing.JLabel orbListenerPortLbl;
    private javax.swing.JSpinner orbMutualauthPort;
    private javax.swing.JLabel orbMutualauthPortLbl;
    private javax.swing.JSpinner orbSslPort;
    private javax.swing.JLabel orbSslPortLbl;
    private javax.swing.JPanel portConfPanel;
    // End of variables declaration//GEN-END:variables
        
}
