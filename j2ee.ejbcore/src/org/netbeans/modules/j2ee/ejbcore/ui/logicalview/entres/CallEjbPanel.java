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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 * @Petr Slechta
 */
public class CallEjbPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = "CallEjbPanel_isValid"; //NOI18N

    private Set<String> refNameSet;
    private final NodeDisplayPanel nodeDisplayPanel;
    private final ServiceLocatorStrategyPanel slPanel;
    private final NodeAcceptor nodeAcceptor;
    private final Project project;
    private final String className;
    private final FileObject srcFile;
    private NotificationLineSupport statusLine;
    
    /** Creates new form CallEjbPanel */
    public CallEjbPanel(FileObject fileObject, Node rootNode, String lastLocator, String className) throws IOException {
        initComponents();
        this.srcFile= fileObject;
        this.project = FileOwnerQuery.getOwner(srcFile);
        this.className = className;
        this.nodeAcceptor = new NodeAcceptorImpl();
        
        nodeDisplayPanel = new NodeDisplayPanel(rootNode);
        nodeDisplayPanel.setBorder(new EtchedBorder());
        displayPanel.add(nodeDisplayPanel);
        nodeDisplayPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                
                if (ExplorerManager.PROP_NODE_CHANGE.equals(pce.getPropertyName())) {
                    Node[] nodes = nodeDisplayPanel.getSelectedNodes();

                    if (nodes.length == 0) {
                        return;
                    }
                    EjbReference ejbReference = nodes[0].getLookup().lookup(EjbReference.class);

                    if (ejbReference != null) {
                        try {
                            setGeneratedName(ejbReference, remoteRadioButton.isSelected(), nodes[0]);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    validateReferences();
                }
            }
        });
        referenceNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                validateReferences();
            }
        });
        
        slPanel = new ServiceLocatorStrategyPanel(lastLocator, ClasspathInfo.create(fileObject));
        slPanel.getUnreferencedServiceLocator().addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                validateReferences();
            }
        });
        slPanel.getClassName().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                validateReferences();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                validateReferences();
            }
            public void changedUpdate(DocumentEvent documentEvent) {
                validateReferences();
            }
        });
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
    }

    public void setNotificationLine(NotificationLineSupport statusLine) {
        this.statusLine = statusLine;
    }

    public void disableServiceLocator() {
        serviceLocatorPanel.setVisible(false);
    }

    // lazy initialization
    private Set<String> getRefNameSet() throws IOException {
        if (refNameSet == null) {
            refNameSet = new HashSet<String>();
            // This is working only for EJB project. Will need some enhancement in EnterpriseReferenceContainer API?
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(srcFile);
            if (ejbModule != null) {

                MetadataModel<EjbJarMetadata> model = ejbModule.getMetadataModel();
                model.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                    public Void run(EjbJarMetadata metadata) throws IOException {
                        EjbJar ejbJar = metadata.getRoot();
                        if (ejbJar != null) {
                            final EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                            JavaSource javaSource = JavaSource.forFileObject(srcFile);
                            if (enterpriseBeans != null && javaSource != null) {
                                javaSource.runUserActionTask(new Task<CompilationController>() {
                                    public void run(CompilationController controller) throws IOException {
                                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        TypeElement beanClass = controller.getElements().getTypeElement(CallEjbPanel.this.className);
                                        for (Ejb ejb : enterpriseBeans.getEjbs()) {
                                            if (beanClass.getQualifiedName().contentEquals(ejb.getEjbClass())) {
                                                EjbRef[] ejbRefs = ejb.getEjbRef();
                                                EjbLocalRef[] ejbLocalRefs = ejb.getEjbLocalRef();
                                                for (int j = 0; j < ejbRefs.length; j++) {
                                                    refNameSet.add(ejbRefs[j].getEjbRefName());
                                                }
                                                for (int j = 0; j < ejbLocalRefs.length; j++) {
                                                    refNameSet.add(ejbLocalRefs[j].getEjbRefName());
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }, true);
                            }
                        }
                        return null;
                    }
                });

            }
        }
        return refNameSet;
    }
    
    public void validateReferences() {
        boolean nodeAccepted = nodeAcceptor.acceptNodes(nodeDisplayPanel.getSelectedNodes());
        if ((slPanel.getUnreferencedServiceLocator().isSelected() &&
                slPanel.getClassName().getText().trim().equals("")) ||
                !nodeAccepted) {
            firePropertyChange(IS_VALID, true, false);
        } else {
            firePropertyChange(IS_VALID, false, true);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        intefaceButtonGroup = new javax.swing.ButtonGroup();
        serviceLocatorPanel = new javax.swing.JPanel();
        convertToRuntime = new javax.swing.JCheckBox();
        displayPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        referenceNameTextField = new javax.swing.JTextField();
        localRadioButton = new javax.swing.JRadioButton();
        remoteRadioButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        serviceLocatorPanel.setLayout(new java.awt.BorderLayout());

        convertToRuntime.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_ConvertToRuntimeMneumonic").charAt(0));
        convertToRuntime.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle"); // NOI18N
        convertToRuntime.setText(bundle.getString("LBL_ConvertToRuntime")); // NOI18N
        serviceLocatorPanel.add(convertToRuntime, java.awt.BorderLayout.SOUTH);
        convertToRuntime.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "ACSD_ConvertToRuntime")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(serviceLocatorPanel, gridBagConstraints);

        displayPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(displayPanel, gridBagConstraints);
        displayPanel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_DisplayPanel")); // NOI18N
        displayPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DisplayPanel")); // NOI18N

        jLabel1.setLabelFor(displayPanel);
        jLabel1.setText(bundle.getString("LBL_ModuleMustBeInSameApplication")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(jLabel1, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("MN_ReferenceName").charAt(0));
        jLabel2.setLabelFor(referenceNameTextField);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferenceName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jLabel2, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencedInterface")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(referenceNameTextField, gridBagConstraints);
        referenceNameTextField.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ReferenceName")); // NOI18N
        referenceNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ReferenceName")); // NOI18N

        intefaceButtonGroup.add(localRadioButton);
        localRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("MN_Local").charAt(0));
        localRadioButton.setSelected(true);
        localRadioButton.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_Local")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(localRadioButton, gridBagConstraints);
        localRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("LBL_Local")); // NOI18N
        localRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_Local")); // NOI18N

        intefaceButtonGroup.add(remoteRadioButton);
        remoteRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("MN_Remote").charAt(0));
        remoteRadioButton.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_Remote")); // NOI18N
        remoteRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                remoteRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(remoteRadioButton, gridBagConstraints);
        remoteRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("LBL_Remote")); // NOI18N
        remoteRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_Remote")); // NOI18N

        getAccessibleContext().setAccessibleName(bundle.getString("ACS_CallEJBPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CallEJBPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void remoteRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_remoteRadioButtonItemStateChanged
        validateReferences();
    }//GEN-LAST:event_remoteRadioButtonItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox convertToRuntime;
    private javax.swing.JPanel displayPanel;
    private javax.swing.ButtonGroup intefaceButtonGroup;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JRadioButton localRadioButton;
    private javax.swing.JTextField referenceNameTextField;
    private javax.swing.JRadioButton remoteRadioButton;
    private javax.swing.JPanel serviceLocatorPanel;
    // End of variables declaration//GEN-END:variables
    
    public boolean convertToRuntime() {
        return convertToRuntime.isSelected();
    }
    
    public Node getEjb() {
        Node[] selectedNodes = nodeDisplayPanel.getSelectedNodes();
        return selectedNodes.length > 0 ? selectedNodes[0] : null;
    }
    
    public String getServiceLocator() {
        return slPanel.classSelected();
    }
    
    public String getReferenceName() {
        return referenceNameTextField.getText();
    }
    
    public boolean isDefaultRefName() {
        Node[] nodes = nodeDisplayPanel.getSelectedNodes();
        if (nodes.length > 0) {
            EjbReference ejbReference = nodes[0].getLookup().lookup(EjbReference.class);
            if (ejbReference != null) {
                try {
                    return getReferenceName().equals(generateName(ejbReference, remoteRadioButton.isSelected(), nodes[0]));
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        return false;
    }
    
    public boolean isRemoteInterfaceSelected() {
        return remoteRadioButton.isSelected();
    }
    
    private String generateName(EjbReference ejbReference, boolean remote, Node selectedNode) throws IOException {
// maven projects never have ant artifacts, if you want to uncomment this, talk to me please, mkleint.
//        if (Utils.getAntArtifact(ejbReference) == null) {
//            return "";
//        }

        String name = "";

        final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(selectedNode);
        FileObject nodeFO = selectedNode.getLookup().lookup(FileObject.class);
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(nodeFO);
        if (ejbModule != null) {
            Map<String, String> names = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {
                public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                    Map<String, String> result = new HashMap<String, String>();
                    EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(elementHandle.getQualifiedName());
                    if (ejb != null) {
                        result.put(EntityAndSession.HOME, ejb.getHome());
                        result.put(EntityAndSession.EJB_NAME, ejb.getEjbName());
                    }
                    return result;
                }
            });
            if (remote) {
                boolean targetIsJavaSE = Utils.isTargetJavaSE(srcFile, className);
                if (targetIsJavaSE && Util.isJavaEE5orHigher(project)){
                    name = elementHandle.getQualifiedName();
                } else if (targetIsJavaSE){
                    name = names.get(EntityAndSession.HOME);
                } else {
                    name = names.get(EntityAndSession.EJB_NAME);
                }
            } else {
                name = names.get(EntityAndSession.EJB_NAME);
            }
        }
        
        int uniquifier = 1;
        String newName = name;
        while (getRefNameSet().contains(newName)) {
            newName = name + String.valueOf(uniquifier++);
        }
        return name;
    }
    
    private void setGeneratedName(EjbReference ejbReference, boolean remote, Node selectedNode) throws IOException {
        referenceNameTextField.setText(generateName(ejbReference, remote, selectedNode));
    }
    
    private class NodeAcceptorImpl implements  NodeAcceptor {
        
        public NodeAcceptorImpl() {}
        
        public boolean acceptNodes(Node[] nodes) {
            statusLine.clearMessages();
            
            // no node selected
            if (nodes.length == 0) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_SelectOneEJB")); //NOI18N
                return false;
            }
            // more than one node selected
            if (nodes.length > 1) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_MoreEJBsSelected")); //NOI18N
                return false;
            }
            ElementHandle<TypeElement> elementHandle = null;
            try {
                elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            // non-EJB node is selected
            if (elementHandle == null) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NodeIsNotEJB")); //NOI18N
                return false;
            }
            
            if (elementHandle.getQualifiedName().equals(className)) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_CannotCallItself", className)); //NOI18N
                return false;
            }
// maven projects never have ant artifacts, if you want to uncomment this, talk to me please, mkleint.
//
//            // builded archive with beans is not available
//            if (!hasJarArtifact()) {
//                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_EJBNotInDistributionArchive")); //NOI18N
//                return false;
//            }
            // node cannot act as EJB reference
            if (nodes[0].getLookup().lookup(EjbReference.class) == null) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencesNotSupported")); //NOI18N
                return false;
            }
            // check interfaces radiobuttons in context of selected node
            if (!acceptInterfaces(nodes)) {
                return false;
            }
            // validate reference name
            if (!validateRefName()) {
                return false;
            }
            
            // if local ref is used, modules must be in same module or J2EE application
            FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
            if (fileObject == null) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NoSourcesForBean")); //NOI18N
                return false;
            }
            Project nodeProject = FileOwnerQuery.getOwner(fileObject);
            if (!isRemoteInterfaceSelected() &&
                    !nodeProject.equals(project) &&
                    !Utils.areInSameJ2EEApp(project, nodeProject)) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NotInSameEarOrProject")); //NOI18N
                return false;
            }
            
            //AC cannot contain references to local beans
            if (!isRemoteInterfaceSelected() &&
                    Utils.isAppClient(project)) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_CannotCallLocalInAC")); //NOI18N
                return false;
            }

            //Unit tests or classes in a JSE project cannot contain references to local beans
            try {
                if (!isRemoteInterfaceSelected() && Utils.isTargetJavaSE(srcFile, className)) {
                    statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_CannotCallLocalInJSE")); //NOI18N
                    return false;
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return false;
            }
            
            // see #75876
            if (!Util.isJavaEE5orHigher(project) && Util.isJavaEE5orHigher(nodeProject)){
                statusLine.setWarningMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_JEESpecificationLevelsDiffer")); //NOI18N
            }
            
            return true;
        }
        
        private boolean acceptInterfaces(Node[] nodes) {
            EjbReference ejbReference = nodes[0].getLookup().lookup(EjbReference.class);
            if (ejbReference == null) {
                return false;
            }
            
            
            boolean shouldEnableLocal = (ejbReference.getLocal() != null);
            boolean shouldEnableRemote = (ejbReference.getRemote() != null);
            localRadioButton.setEnabled(shouldEnableLocal);
            remoteRadioButton.setEnabled(shouldEnableRemote);
            if (!shouldEnableLocal && !shouldEnableRemote) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencesNotSupported")); //NOI18N
                return false;
            } else if (shouldEnableLocal && !shouldEnableRemote) {
                localRadioButton.setSelected(true);
            } else if (!shouldEnableLocal && shouldEnableRemote) {
                remoteRadioButton.setSelected(true);
            }
            statusLine.clearMessages();
            return true;
        }
        
        private boolean hasJarArtifact() {
            Project nodeProject = FileOwnerQuery.getOwner(srcFile);
            if (nodeProject.equals(project)) {
                // we're in same project, no need for output jar
                return true;
            }
            return AntArtifactQuery.findArtifactsByType(nodeProject, JavaProjectConstants.ARTIFACT_TYPE_JAR).length > 0;
        }
        
        private boolean validateRefName() {
            String refName = referenceNameTextField.getText();
            if (refName.trim().length() < 1) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "ERR_ReferenceNameEmpty", refName)); //NOI18N
                return false;
            } else if (refNameSet.contains(refName)) {
                statusLine.setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "ERR_ReferenceNameExists", refName)); //NOI18N
                return false;
            }
            return true;
        }
    }
    
}
