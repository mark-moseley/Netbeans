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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.util.*;
import javax.swing.event.*;
import javax.swing.*;

import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.HelpCtx;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.WizardDescriptor;

/**
 * This Panel launches autoconfiguration during the New J2SE Platform sequence.
 * The UI views properties of the platform, reacts to the end of detection by
 * updating itself. It triggers the detection task when the button is pressed.
 * The inner class WizardPanel acts as a controller, reacts to the UI completness
 * (jdk name filled in) and autoconfig result (passed successfully) - and manages
 * Next/Finish button (valid state) according to those.
 *
 * @author Svata Dedic
 */
public class DetectPanel extends javax.swing.JPanel {

    private JavaPlatform platform;
    private ArrayList listeners;

    /**
     * Creates a detect panel
     * start the task and update on its completion
     * @param p the platform being customized.
     */
    public DetectPanel(JavaPlatform p) {
        initComponents();
        postInitComponents ();
        putClientProperty("WizardPanel_contentData",
            new String[] { NbBundle.getMessage(DetectPanel.class,
                "TITLE_J2SEWizardIterator_Configure")
        });
        this.platform = p;
    }

    public void addNotify() {
        super.addNotify();
    }

    void setFolder(FileObject f) {
        jdkLocation.setText(FileUtil.toFile(f).getAbsolutePath());
    }

    private void postInitComponents () {
        this.jdkName.getDocument().addDocumentListener (new DocumentListener () {

            public void insertUpdate(DocumentEvent e) {
                handleNameChange ();
            }

            public void removeUpdate(DocumentEvent e) {
                handleNameChange ();
            }

            public void changedUpdate(DocumentEvent e) {
                handleNameChange ();
            }
        });

        this.antName.getDocument().addDocumentListener(new DocumentListener () {

            public void insertUpdate(DocumentEvent e) {
                handleAntNameChange();
            }

            public void removeUpdate(DocumentEvent e) {
                handleAntNameChange();
            }

            public void changedUpdate(DocumentEvent e) {
                handleNameChange();
            }
        });
    }

    private void handleNameChange () {
        String name = jdkName.getText ();
        String antName = Util.normalizeName(name);
        this.antName.setText (antName);
        this.fireChange();
    }

    private void handleAntNameChange () {
        this.fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        jdkLocation = new javax.swing.JTextField();
        jTextArea1 = new javax.swing.JTextArea();
        waitLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jdkName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        javaSpecVer = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        javaVendor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        machineName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        antName = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setText(NbBundle.getBundle(DetectPanel.class).getString("LAB_DetectPanel_Location"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel2, gridBagConstraints);

        jdkLocation.setEditable(false);
        jdkLocation.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 6, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(jdkLocation, gridBagConstraints);

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(NbBundle.getBundle(DetectPanel.class).getString("TXT_DetectPanel_Explain"));
        jTextArea1.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTextArea1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 12);
        gridBagConstraints.weightx = 1.0;
        add(waitLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 12);
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);

        jLabel3.setLabelFor(jdkName);
        jLabel3.setText(NbBundle.getBundle(DetectPanel.class).getString("LBL_DetailsPanel_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);

        jdkName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jdkName, gridBagConstraints);

        jLabel4.setLabelFor(javaSpecVer);
        jLabel4.setText(NbBundle.getBundle(DetectPanel.class).getString("LBL_DetectPanel_SpecificationVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);

        javaSpecVer.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(javaSpecVer, gridBagConstraints);

        jLabel5.setLabelFor(javaVendor);
        jLabel5.setText(NbBundle.getBundle(DetectPanel.class).getString("LBL_DetectPanel_Vendor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);

        javaVendor.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        gridBagConstraints.weightx = 1.0;
        add(javaVendor, gridBagConstraints);

        jLabel6.setLabelFor(machineName);
        jLabel6.setText(NbBundle.getBundle(DetectPanel.class).getString("LBL_DetectPanel_VMName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel6, gridBagConstraints);

        machineName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 12);
        add(machineName, gridBagConstraints);

        jLabel1.setLabelFor(antName);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seplatform/wizard/Bundle").getString("LBL_AntName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        antName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(antName, gridBagConstraints);

    }//GEN-END:initComponents

    public void setNotifyMessage (String message) {
        waitLabel.setText (message);
    }

    public final synchronized void addChangeListener (ChangeListener listener) {
        if (this.listeners == null)
            this.listeners = new ArrayList ();
        this.listeners.add (listener);
    }

    public final synchronized void removeChangeListener (ChangeListener listener) {
        if (this.listeners == null)
            return;
        this.listeners.remove (listener);
    }

    public String getPlatformName() {
	    return jdkName.getText();
    }

    public String getAntName () {
        return this.antName.getText();
    }

    protected final void fireChange () {
        Iterator it = null;
        synchronized (this) {
            if (this.listeners == null)
                return;
            it = ((ArrayList)this.listeners.clone()).iterator();
        }
        ChangeEvent event = new ChangeEvent (this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(event);
        }
    }

    /**
     * Updates static information from the detected platform's properties
     */
    void updateData() {
        Map m = platform.getSystemProperties();
        String v = (String)m.get("java.version");
        this.javaSpecVer.setText(v == null ? "" : v);
        v = (String)m.get("java.vendor");
        this.javaVendor.setText(v == null ? "" : v);
        v = (String)m.get("java.vm.name");
        this.machineName.setText(v == null ? "" : v);

        // if the name is empty, fill something in:
        if ("".equals(jdkName.getText())) {
            jdkName.setText(getInitialName (m));
            jdkName.setEditable(true);
            antName.setEditable (true);
        }
    }


    private static String getInitialName (Map m) {
        String vmName = (String)m.get("java.vm.name");              //NOI18N
        String vmVersion = (String)m.get("java.vm.version");        //NOI18N
        StringBuffer result = new StringBuffer();
        if (vmName != null)
            result.append(vmName);
        if (vmVersion != null) {
            if (result.length()>0) {
                result.append (" ");
            }
            result.append (vmVersion);
        }
        return result.toString();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField antName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField javaSpecVer;
    private javax.swing.JTextField javaVendor;
    private javax.swing.JTextField jdkLocation;
    private javax.swing.JTextField jdkName;
    private javax.swing.JTextField machineName;
    private javax.swing.JLabel waitLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Controller for the outer class: manages wizard panel's valid state
     * according to the user's input and detection state.
     */
    static class WizardPanel implements WizardDescriptor.Panel, TaskListener, ChangeListener {
        private DetectPanel         component;
        private RequestProcessor.Task task;
        private final J2SEWizardIterator  iterator;
        private Collection          changeList = new ArrayList();
        private boolean             detected;
	    private J2SEPlatformImpl    platform;
        private boolean             valid;

        WizardPanel(J2SEWizardIterator iterator) {
            this.iterator = iterator;
            JavaPlatform platform = iterator.getPlatform();
            assert platform instanceof J2SEPlatformImpl;
	        this.platform = (J2SEPlatformImpl) iterator.getPlatform();
        }

        public void addChangeListener(ChangeListener l) {
            changeList.add(l);
        }

        public java.awt.Component getComponent() {
            if (component == null) {
                component = new DetectPanel(iterator.getPlatform());
                component.setFolder(iterator.getInstallFolder());
                component.addChangeListener (this);
                task = RequestProcessor.getDefault().create(iterator);
                task.addTaskListener(this);
            }
            return component;
        }

        void setValid(boolean v) {
            if (v == valid)
                return;
            valid = v;
            fireStateChange();
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public boolean isValid() {
            return valid;
        }

        public void readSettings(Object settings) {
            component.setNotifyMessage(NbBundle.getMessage(DetectPanel.class,"TXT_DetectPanel_WaitMessage"));
            task.schedule(0);
        }

        void fireStateChange() {
            ChangeListener[] ll;
            synchronized (this) {
                if (changeList.isEmpty())
                    return;
                ll = (ChangeListener[])changeList.toArray(new ChangeListener[0]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++)
                ll[i].stateChanged(ev);
        }

        public void removeChangeListener(ChangeListener l) {
            changeList.remove(l);
        }

	/**
	 Updates the Platform's display name with the one the user
	 has entered. Stores user-customized display name into the Platform.
	 */
        public void storeSettings(Object settings) {
	        platform.setDisplayName(component.getPlatformName());
            platform.setAntName (component.getAntName());
        }

        /**
         * Revalidates the Wizard Panel
         */
        public void taskFinished(Task task) {
            detected = iterator.isValid();
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run () {
                            component.updateData();
                            checkValid();
                        }
                    }
            );
        }


        public void stateChanged(ChangeEvent e) {
             this.checkValid();
        }

        private void checkValid () {
            boolean validAntName = isValidAntName (component.getAntName());
            boolean validDisplayName = component.getPlatformName().length() > 0;
            boolean v = detected && validDisplayName &&  validAntName;
            setValid(v);
            if (!v) {
                component.setNotifyMessage(NbBundle.getMessage(DetectPanel.class,"TXT_DetectPanel_ConfigUnsuccess"));
            }
            else if (!validAntName) {
                component.setNotifyMessage(NbBundle.getMessage(DetectPanel.class,"TXT_DetectPanel_InvalidAntName"));
            }
            else if (!validDisplayName) {
                component.setNotifyMessage(NbBundle.getMessage(DetectPanel.class,"TXT_DetectPanel_InvalidDisplayName"));
            }
            else {
                component.setNotifyMessage(NbBundle.getMessage(DetectPanel.class, "TXT_DetectPanel_ConfigSuccess"));
            }
        }

        private static boolean isValidAntName (String name) {
            if (name == null || name.length() == 0)
                return false;
            if (!Character.isJavaIdentifierStart (name.charAt(0)))
                return false;
            for (int i=0; i< name.length(); i++) {
                char c = name.charAt(i);
                if (!Character.isJavaIdentifierPart(c) &&
                    c !='-' && c!='.')
                    return false;
            }
            return true;
        }
    }
}
