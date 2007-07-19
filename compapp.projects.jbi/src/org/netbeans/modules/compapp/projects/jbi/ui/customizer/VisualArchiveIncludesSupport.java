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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import org.netbeans.modules.compapp.jbiserver.connectors.HTTPServerConnector;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.ComponentInformationParser;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentDocument;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentStatus;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentObject;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentTableModel;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentTableRenderer;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.TableSorterUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
import org.netbeans.modules.compapp.jbiserver.management.AdministrationService;
import org.netbeans.modules.compapp.projects.jbi.JbiActionProvider;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.CreateComponentInformation;
import org.netbeans.modules.compapp.projects.jbi.ui.NoSelectedServerWarning;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Handles adding and removing of additional war content.
 *
 * @author jqian
 */
final class VisualArchiveIncludesSupport {
    
    private static final String COMPONENT_INFO_FILE_NAME = "ComponentInformation.xml"; // NOI18N    
    private static final String ASSEMBLY_INFO_FILE_NAME = "AssemblyInformation.xml"; // NOI18N
        
    private JbiProjectProperties webProperties;
    
    private Project master;    
    
    private JTable componentTable;    
    private ComponentTableModel mComponentTableModel;
    private ComponentTableRenderer mTableRenderer;
    private Vector mComponentTableColumnNames;
    
    private JTable classpathTable;        
    private ClasspathTableModel classpathTableModel;
    private Object[][] data;
    
    private JButton addProjectButton;    
    private JButton removeProjectButton;  
    private JButton updateComponentsButton;       
      
    private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
    
    private String compInfoFileLoc;
    private String assemblyInfoFileLoc;
    
    private JComboBox comboTarget = null;
    private DefaultComboBoxModel comboModel = null;
    private List<String> componentNames = new ArrayList<String>();
    
    private List<VisualClassPathItem> bindingVisualClassPathItems = null; 
    private AntArtifact bcJar = null;
    
    
    /**
     * Creates a new VisualArchiveIncludesSupport object.
     *
     * @param webProperties 
     * @param componentTable 
     * @param classpathTable 
     * @param updateComponentsButton 
     * @param addProjectButton 
     * @param removeProjectButton 
     */
    public VisualArchiveIncludesSupport(
            JbiProjectProperties webProperties, 
            JTable componentTable, 
            JTable classpathTable,
            JButton updateComponentsButton, 
            JButton addProjectButton,
            JButton removeProjectButton) {
        
        this.webProperties = webProperties;
        this.componentTable = componentTable;
        this.classpathTable = classpathTable;
        this.updateComponentsButton = updateComponentsButton;
        this.addProjectButton = addProjectButton;
        this.removeProjectButton = removeProjectButton;        
        this.master = webProperties.getProject();       
                
        this.bindingVisualClassPathItems = webProperties.getBindingList();
        
        // combobox cell editor for target selection
        comboModel = new DefaultComboBoxModel(new String[] {" "}); // NOI18N
        comboTarget = new JComboBox(comboModel);
        
        initClassPathTable();          
        
        initComponentTable();
                
        // Register the listeners
        ClasspathSupportListener csl = new ClasspathSupportListener();        
        this.updateComponentsButton.addActionListener(csl);
        this.addProjectButton.addActionListener(csl);
        this.removeProjectButton.addActionListener(csl);
        this.classpathTable.getSelectionModel().addListSelectionListener(csl);
        classpathTableModel.addTableModelListener(csl);
        
        // Set the initial state of the buttons
        csl.valueChanged(null);
        
        // init locals        
        Project p = webProperties.getProject();
        File pf = FileUtil.toFile(p.getProjectDirectory());
        
        List os = (List) webProperties.get(JbiProjectProperties.META_INF);        
        if ((os != null) && (os.size() > 0)) {            
            String path = pf.getPath() + "/" + os.get(0).toString(); // NOI18N
            compInfoFileLoc = path + "/" + COMPONENT_INFO_FILE_NAME; // NOI18N
            assemblyInfoFileLoc = path + "/" + ASSEMBLY_INFO_FILE_NAME; // NOI18N
        }
        
        AntProjectHelper helper = p.getLookup().lookup(AntProjectHelper.class);
        bcJar = helper.createSimpleAntArtifact(
                "CAPS.jbiserver:bpelse", "build/BCDeployment.jar", // NOI18N
                helper.getStandardPropertyEvaluator(), "dist_bc", "clean" // NOI18N
                );
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @param items DOCUMENT ME!
     */
    public void setVisualWarItems(List<VisualClassPathItem> items) {
        this.data = new Object[items.size()][2];
        
        for (int i = 0; i < items.size(); i++) {
            VisualClassPathItem vi = items.get(i);
            classpathTableModel.setValueAt(vi, i, 0);
            classpathTableModel.setValueAt("", i, 1); // NOI18N
        }
        
        updateAsaTarget();
        
        classpathTableModel.fireTableDataChanged();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<VisualClassPathItem> getVisualWarItems() {
        List<VisualClassPathItem> items = new ArrayList<VisualClassPathItem>();
        
        for (int i = 0; i < data.length; i++) {
            items.add((VisualClassPathItem) classpathTableModel.getValueAt(i, 0));
        }
        
        return items;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param tml DOCUMENT ME!
     */
    public void addTableModelListener(TableModelListener tml) {
        classpathTableModel.addTableModelListener(tml);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param tml DOCUMENT ME!
     */
    public void removeTableModelListener(TableModelListener tml) {
        classpathTableModel.removeTableModelListener(tml);
    }
    
    /**
     * Action listeners will be informed when the value of the list changes.
     *
     * @param listener DOCUMENT ME!
     */
    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }
    
    private void fireActionPerformed() {
        ArrayList listeners;
        
        synchronized (this) {
            listeners = new ArrayList(actionListeners);
        }
        
        ActionEvent ae = new ActionEvent(this, 0, null);
        
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ActionListener al = (ActionListener) it.next();
            al.actionPerformed(ae);
        }
    }
    
    // Private methods ---------------------------------------------------------
    private void addArtifacts(AntArtifact[] artifacts) {
        // Detect duplicate first
        Map<AntArtifact, VisualClassPathItem> viMap = 
                new HashMap<AntArtifact, VisualClassPathItem>();
        
        List<AntArtifact> uniqueArtifacts = new ArrayList<AntArtifact>();
        for (int i = 0; i < artifacts.length; i++) {
            VisualClassPathItem vi = new VisualClassPathItem(
                        artifacts[i], VisualClassPathItem.TYPE_ARTIFACT, null,
                        artifacts[i].getArtifactLocations()[0].toString(), true
                        );
            viMap.put(artifacts[i], vi);
            boolean duplicate = false;
            for (int j = 0; j < data.length; j++) {
                if (data[j][0].toString().equals(vi.toString())) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                String msg = NbBundle.getMessage(AddProjectAction.class, 
                        "MSG_DuplicateJBIModule", vi.toString()); // NOI18N
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                uniqueArtifacts.add(artifacts[i]);
            }
        }
        
        if (uniqueArtifacts.size() == 0) {
            return;
        }
        
        Object[][] newData = new Object[data.length + uniqueArtifacts.size()][2];
        
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        
        for (int i = 0; i < uniqueArtifacts.size(); i++) {
            AntArtifact artifact = uniqueArtifacts.get(i);
            VisualClassPathItem vi = viMap.get(artifact);
            newData[data.length + i][0] = vi;
            newData[data.length + i][1] = getDefaultTarget(vi.getAsaType());
            if (VisualClassPathItem.isJavaEEProjectAntArtifact(artifact)){
                webProperties.addSunResourceProject(artifact);
            }
        }
        
        data = newData;
        classpathTableModel.fireTableRowsInserted(data.length,
                (data.length + uniqueArtifacts.size()) - 1);
        
        fireActionPerformed();
    }
    
    private void removeElements() {
        ListSelectionModel sm = classpathTable.getSelectionModel();
        Object aa = null;
        
        int index = sm.getMinSelectionIndex();
        
        if (sm.isSelectionEmpty()) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        
        Collection elements = new ArrayList();
        final int n0 = data.length;
        
        for (int i = 0; i < n0; i++) {
            if (!sm.isSelectedIndex(i)) {
                elements.add(data[i]);
            } else {
                if (data[i][0] instanceof VisualClassPathItem){
                    aa = ((VisualClassPathItem)data[i][0]).getObject();
                    if ((aa instanceof AntArtifact) &&
                            (VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact)aa))){
                        webProperties.removeSunResourceProject((AntArtifact)aa);
                    }
                }
            }
        }
        
        final int n = elements.size();
        data = (Object[][]) elements.toArray(new Object[n][2]);
        classpathTableModel.fireTableRowsDeleted(elements.size(), n0 - 1);
        
        if (index >= n) {
            index = n - 1;
        }
        
        sm.setSelectionInterval(index, index);
        
        fireActionPerformed();
    }
    
    private String getDefaultTarget(String type) {
        int tsize = componentNames.size();
        
        for (int i = 0; i < tsize; i++) {
            String val = componentNames.get(i);
            
            if (val.startsWith(type)) {
                return val;
            }
        }
        
        return ""; // NOI18N
    }
    
    private void updateClassPathTableModel(String jar, String suName, String suDesc, String compName) {
        for (int i = 0, size = classpathTableModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vcpi = (VisualClassPathItem) classpathTableModel.getValueAt(i, 0);
            
            String shortName = vcpi.getShortName();
            if (shortName.compareTo(jar) == 0 ||
                    // backward compatibility
                    shortName.endsWith(".jar") &&   // NOI18N
                    jar.endsWith("@SEDeployment.jar") &&    // NOI18N
                    shortName.substring(0, shortName.length() - 4).equals(
                    jar.substring(0, jar.length() - 17))) {
                vcpi.setAsaDescription(suDesc);
                vcpi.setAsaTarget(compName);
                
                classpathTableModel.setValueAt(compName, i, 1);
                
//                // lookup the targe list
//                for (int j = 0, tsize = comboValues.size(); j < tsize; j++) {
//                    String target = comboValues.get(j);
//                    
//                    if (target.indexOf(compName) > 0) {
//                        classpathTableModel.setValueAt(target, i, 1);
//                        
//                        return;
//                    }
//                }
//                
//                // not set yet.. default to the first non-blank traget on the list
//                classpathTableModel.setValueAt(getDefaultTarget(vcpi.getAsaType()), i, 1);
//                
//                return;
            }
        }
        
        // OK this is not a SE jar..
        for (VisualClassPathItem vi : bindingVisualClassPathItems) {
            if (vi.getAsaTarget().compareTo(compName) == 0) {
                vi.setAsaDescription(suDesc);                
                return;
            }
        }
    }
        
    // Load AsseemblyInfo.xml and update classpath table and component table's first column
    private void updateClassPathTable() {
        try {            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            
            Document doc = factory.newDocumentBuilder().parse(new File(assemblyInfoFileLoc));
            
            NodeList serviceUnitNodeList = doc.getElementsByTagName("service-unit"); // NOI18N
            String suName = null;
            String suDescription = null;
            String compName = null;
            String jar = null;
            
            // a list of component names currently being used in this compapp
            List<String> compNames = new ArrayList<String>();
            
            for (int i = 0, isize = serviceUnitNodeList.getLength(); i < isize; i++) {
                NodeList kids = serviceUnitNodeList.item(i).getChildNodes();
                
                for (int k = 0, ksize = kids.getLength(); k < ksize; k++) {
                    Node n = kids.item(k);
                    
                    if (n.getNodeName().equals("identification")) { // NOI18N
                        NodeList ids = n.getChildNodes();
                        
                        for (int j = 0, jsize = ids.getLength(); j < jsize; j++) {
                            Node m = ids.item(j);
                            
                            if (m.getNodeName().equals("name")) { // NOI18N
                                suName = m.getFirstChild().getNodeValue();
                            } else if (m.getNodeName().compareTo("description") == 0) { // NOI18N
                                suDescription = m.getFirstChild() == null ? "" : m.getFirstChild().getNodeValue();
                            }
                        }
                    } else if (n.getNodeName().equals("target")) { // NOI18N
                        NodeList ids = n.getChildNodes();
                        
                        for (int j = 0, jsize = ids.getLength(); j < jsize; j++) {
                            Node m = ids.item(j);
                            
                            if (m.getNodeName().equals("component-name")) { // NOI18N
                                compName = m.getFirstChild().getNodeValue();
                            } else if (m.getNodeName().equals("artifacts-zip")) { // NOI18N
                                jar = m.getFirstChild().getNodeValue();
                            }
                        }
                    }
                }
                
                if (jar != null) {
                    updateClassPathTableModel(jar, suName, suDescription, compName);
                    compNames.add(compName);
                }
            }
            
            for (int i = 0, size = mComponentTableModel.getRowCount(); i < size; i++) {
                String name = (String) mComponentTableModel.getValueAt(i, COMPONENT_NAME_COLUMN);
                mComponentTableModel.setValueAt(compNames.contains(name), i, COMPONENT_IN_DEPLOYMENT_COLUMN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  
    
    private void initClassPathTable() {
        
        this.classpathTableModel = new ClasspathTableModel();
        this.classpathTable.setModel(classpathTableModel);
        this.classpathTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        TableColumnModel columnModel = this.classpathTable.getColumnModel();
        TableColumn firstColumn = columnModel.getColumn(0);
        TableColumn secondColumn = columnModel.getColumn(1);
        firstColumn.setHeaderValue(NbBundle.getMessage(getClass(), "TXT_Archive_Item")); // NOI18N
        secondColumn.setHeaderValue(NbBundle.getMessage(getClass(), "TXT_Archive_PathInArchive")); // NOI18N
        firstColumn.setCellRenderer(new ClassPathCellRenderer());
        secondColumn.setCellEditor(new TargetComboBoxEditor(comboTarget));  
    }
    
    private static final int COMPONENT_IN_DEPLOYMENT_COLUMN = 0;
    private static final int COMPONENT_TYPE_COLUMN = 1;
    private static final int COMPONENT_NAME_COLUMN = 2;
    
    // Target Component support...
    //--------------------------------------------------------------------------
    private void initComponentTable() {
        Vector datas = new Vector(1);
        
        // setup the table model to  use
        mComponentTableColumnNames = new Vector();
        mComponentTableColumnNames.addElement(" "); // NOI18N
        mComponentTableColumnNames.addElement(NbBundle.getMessage(getClass(), "Type"));  // NOI18N
        mComponentTableColumnNames.addElement(NbBundle.getMessage(getClass(), "Component_ID"));  // NOI18N
        mComponentTableModel = new ComponentTableModel(datas, mComponentTableColumnNames);
        
        // setup table sorter to use
        TableSorterUtil mTableSorter = new TableSorterUtil(mComponentTableModel);
        componentTable.setModel(mTableSorter);
        componentTable.setShowHorizontalLines(true);
        componentTable.setShowVerticalLines(false);
        componentTable.setShowGrid(false);
        componentTable.setAutoCreateColumnsFromModel(false);
        componentTable.setRowSelectionAllowed(true);
        componentTable.setColumnSelectionAllowed(false);
        componentTable.getTableHeader().setReorderingAllowed(false);
        componentTable.getTableHeader().setAlignmentY(JTable.LEFT_ALIGNMENT);
        componentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // setup renderer
        mTableRenderer = new ComponentTableRenderer(mComponentTableModel);
        TableColumnModel columnModel = componentTable.getColumnModel();
        columnModel.getColumn(COMPONENT_IN_DEPLOYMENT_COLUMN).setCellRenderer(mTableRenderer);
        columnModel.getColumn(COMPONENT_TYPE_COLUMN).setCellRenderer(mTableRenderer);
        columnModel.getColumn(COMPONENT_NAME_COLUMN).setCellRenderer(mTableRenderer);
        columnModel.getColumn(COMPONENT_IN_DEPLOYMENT_COLUMN).setMaxWidth(30);
        columnModel.getColumn(COMPONENT_TYPE_COLUMN).setPreferredWidth(70);
        columnModel.getColumn(COMPONENT_NAME_COLUMN).setPreferredWidth(300);
        
        mComponentTableModel.addTableModelListener(new TargetSupportListener());
    }
    
    /**
     * DOCUMENT ME!
     */
    public void initTableValues() {
        updateComponentTable(false);    
        updateClassPathTable();
    }
    
//    private void updateComboTarget() {
//        comboModel.removeAllElements();
//        comboModel.addElement(" "); // NOI18N
//        
//        for (int i = 0; i < componentNames.size(); i++) {
//            comboModel.addElement(componentNames.get(i));
//        }
//    }
        
    /**
     * Update component table with components from ComponentInformation.xml.
     * Also rebuild componentNames and bindingVisualClassPathItems.
     */
    private void updateComponentTable(boolean inDeployment) {
        
        File dst = new File(compInfoFileLoc);
        
        try {
            if (dst.exists()) {
                JBIComponentDocument compDoc = ComponentInformationParser.parse(dst);
                List<JBIComponentStatus> compList = compDoc.getJbiComponentList();
                updateComponentTable(compList, inDeployment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
                
    /**
     * Update component table with components from the given list.
     * Also rebuild componentNames and bindingVisualClassPathItems.
     */            
    private void updateComponentTable(List<JBIComponentStatus> compList, 
            boolean inDeployment) {
        
        Vector rowData = new Vector(1);
        
        componentNames.clear();
        bindingVisualClassPathItems.clear();
        
        for (JBIComponentStatus component : compList) {
            if (component.isSharedLibrary()) {
                continue;
            }
            
            ComponentObject comp = new ComponentObject(
                    component.getType(),
                    component.getState(),
                    component.getName(),
                    component.getDescription(),
                    inDeployment); // update this when loading assembly info
            rowData.add(comp);
            
            componentNames.add(component.getName());
            
            if (component.isBindingComponent()) {
                VisualClassPathItem vi = new VisualClassPathItem(
                        bcJar, VisualClassPathItem.TYPE_ARTIFACT,
                        "BCDeployment.jar", null, // NOI18N
                        inDeployment);
                vi.setAsaTarget(component.getName());
                bindingVisualClassPathItems.add(vi);
            }
        }
        
        mComponentTableModel.setDataVector(
                rowData, mComponentTableColumnNames);
        mTableRenderer.setModel(mComponentTableModel);
        mComponentTableModel.fireTableDataChanged();

        //updateComboTarget();
    }
    
    private void updateAsaTarget() {
        for (int i = 0, size = classpathTableModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vi = (VisualClassPathItem) classpathTableModel.getValueAt(i, 0);
            String tid = (String) classpathTableModel.getValueAt(i, 1);
            
            if ((tid == null) || (tid.trim().length() < 1)) {
                // not set yet.. default to the first non-blank traget on the list
                classpathTableModel.setValueAt(getDefaultTarget(vi.getAsaType()), i, 1);
            }
        }
    }
    
    
    private boolean isSelectedServer() {
        String instance = (String) webProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected = true;
        
        if ((instance == null) || !JbiManager.isAppServer(instance)) {
            String[] serverIDs = JbiManager.getAppServers();
            
            if (serverIDs.length < 1) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(
                        NbBundle.getMessage(JbiActionProvider.class, "MSG_NoInstalledServerError"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }
            
            NoSelectedServerWarning panel = new NoSelectedServerWarning(serverIDs);
            
            Object[] options = new Object[] {
                DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
                    true, options, options[0], 
                    DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance();
                selected = instance != null;
                
                if (selected) {
//                    JbiProjectProperties wpp = new JbiProjectProperties(
//                            project, antProjectHelper, refHelper
//                        );
                    webProperties.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    webProperties.store();
//                    System.out.println("setting server instance to be " + instance);
//                    wpp.store();
                }
            }
            
            dlg.dispose();
        }
        
        if ((instance == null) || (!selected)) {
            String msg = NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoSelectedServerError"); // NOI18N
            NotifyDescriptor d = 
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        } else if (!JbiManager.isRunningAppServer(instance)) {
            String msg = NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoRunningServerError"); // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        }
        
        return true;
    }
    
    private void fetchInfo() {
        
        String serverInstance = (String) webProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
        
        if (serverInstance == null) {
            if (!isSelectedServer()) {
                return;
            }
            serverInstance = (String) webProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
            
        } else if (!JbiManager.isRunningAppServer(serverInstance)) {
            String msg = NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoRunningServerError"); // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        
        String hostName = (String) webProperties.get(JbiProjectProperties.HOST_NAME_PROPERTY_KEY);
        String port = (String) webProperties.get(JbiProjectProperties.ADMINISTRATION_PORT_PROPERTY_KEY);
        String userName = (String) webProperties.get(JbiProjectProperties.USER_NAME_PROPERTY_KEY);
        String password = (String) webProperties.get(JbiProjectProperties.PASSWORD_PROPERTY_KEY);
        String location = (String) webProperties.get(JbiProjectProperties.LOCATION_PROPERTY_KEY);
        
        if (hostName == null || port == null || userName == null || password == null
                || location == null) {
            
            Properties properties = JbiManager.getServerInstanceProperties(serverInstance);
            
            hostName = (String) properties.getProperty(JbiManager.HOSTNAME_ATTR);
            port = (String) properties.getProperty(JbiManager.PORT_ATTR);
            userName = (String) properties.getProperty(JbiManager.USERNAME_ATTR);
            password = (String) properties.getProperty(JbiManager.PASSWORD_ATTR);
            //location = (String) properties.getProperty(JbiManager.
            
            webProperties.put(JbiProjectProperties.HOST_NAME_PROPERTY_KEY, hostName);
            webProperties.put(JbiProjectProperties.ADMINISTRATION_PORT_PROPERTY_KEY, port);
            webProperties.put(JbiProjectProperties.USER_NAME_PROPERTY_KEY, userName);
            webProperties.put(JbiProjectProperties.PASSWORD_PROPERTY_KEY, password);
        }
        
        ClassLoader jbiClassLoader = JbiManager.getJBIClassLoader(serverInstance);
        
//        System.out.println("VisualArchiveIncludesSupport.fetchInfo():");
//        System.out.println("hostName=" + hostName);
//        System.out.println("port=" + port);
//        System.out.println("userName=" + userName);
//        System.out.println("password=" + password);
        
        if (hostName == null || port == null || userName == null || password == null || jbiClassLoader == null) {
            String msg = "The application server is not set up correctly or it is not running.";   // FIXME // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else {
            HTTPServerConnector httpServerConnector =
                    new HTTPServerConnector(hostName, port, userName, password, jbiClassLoader);
            
            AdministrationService adminService = null;
            try {
                adminService = new AdministrationService(httpServerConnector);
            } catch (Exception e) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        e.getMessage(), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
            
            if (adminService != null) {
                adminService.constructDocumentObject();
                JBIComponentDocument componentDocument = 
                        adminService.getJBIComponentDocument();
                //componentDocument.dump();   
                
                List<JBIComponentStatus> compList = 
                        componentDocument.getJbiComponentList();                
                updateComponentTable(compList, true);
                
                
                // TODO: save on OK instead of Update; merge instead of overwrite
//                try {
//                    updateComponentInformationFiles(componentDocument);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
            }
        }
    }
    
    private void updateComponentInformationFiles(JBIComponentDocument componentDocument) throws IOException {
        FileObject rootFileObject = webProperties.getProject().getProjectDirectory();
        FileObject confFileObject = rootFileObject.getFileObject("src").getFileObject("conf"); // NOI18N
        File confFile = FileUtil.toFile(confFileObject);
        try {
            CreateComponentInformation infoDoc = new CreateComponentInformation();
            infoDoc.buildComponentDOMTree(componentDocument);
            infoDoc.writeToComponentFile(confFile.getPath()); // confRoot.getPath());
            // TODO: namespace missing for BC, resulting empty BCComponentList file
//            infoDoc.buildBCDOMTree(componentDocument);
//            infoDoc.writeToBCFile(confFile.getPath()); // confRoot.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateProperties(JbiProjectProperties prop, 
            ClasspathTableModel classpathModel) {
        List<String> targetIDs = new ArrayList<String>();
        VisualClassPathItem vcpi = null;
        List<VisualClassPathItem> javaEEProjs = new ArrayList<VisualClassPathItem>();
        Object aa = null;
        
        for (int i = 0; i < classpathModel.getRowCount(); i++) {
            targetIDs.add((String) classpathModel.getValueAt(i, 1));
            
            vcpi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
            if (vcpi != null) {
                aa = vcpi.getObject();
                if ( (aa instanceof AntArtifact) && 
                        VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact) aa)){
                    javaEEProjs.add(vcpi);
                }
            }
        }
        
        prop.put(JbiProjectProperties.JBI_CONTENT_COMPONENT, targetIDs);
        prop.put(JbiProjectProperties.JBI_JAVAEE_JARS, javaEEProjs);
    }
    
    
    // -------------------- private inner classes ------------------------------
    
    private class TargetSupportListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            if ((e.getType() == TableModelEvent.UPDATE) && 
                    (e.getColumn() == COMPONENT_IN_DEPLOYMENT_COLUMN)) {
                int rn = e.getFirstRow();
                String tid = (String) mComponentTableModel.getValueAt(rn, COMPONENT_NAME_COLUMN);
                for (VisualClassPathItem vi : bindingVisualClassPathItems) {                    
                    if (vi != null) {
                        String sid = vi.getAsaTarget();
                        
                        if ((sid != null) && (sid.equalsIgnoreCase(tid))) {
                            boolean b = ((Boolean) mComponentTableModel.getValueAt(rn, COMPONENT_IN_DEPLOYMENT_COLUMN)).booleanValue();
                            vi.setInDeployment(b);
                        }
                    }
                }
            }
        }
    }
    
    private class ClasspathSupportListener
            implements ActionListener, ListSelectionListener, TableModelListener {
        
        //--------------------------- ActionListener  --------------------------
        /**
         * Handles button events
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            
            if (source == addProjectButton) {
                List<String> javaeeAntArtifactTypes = new ArrayList<String>();
                javaeeAntArtifactTypes.addAll(JbiProjectConstants.JAVA_EE_AA_TYPES);
                javaeeAntArtifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);                
                AntArtifact[] artifacts = AntArtifactChooser.showDialog(
                        javaeeAntArtifactTypes, master, null, null);
                
                if (artifacts != null) {
                    addArtifacts(artifacts);
                }
            } else if (source == removeProjectButton) {
                removeElements();
            } else if (source == updateComponentsButton) {
                RequestProcessor.getDefault().post(
                        new Runnable() {
                    public void run() {
                        fetchInfo();
                    }
                }
                );
            }
        }
        
        //--------------------------- ListSelectionListener --------------------
        /**
         * Handles changes in the selection
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel selectionModel = 
                    (DefaultListSelectionModel) classpathTable.getSelectionModel();
            int index = selectionModel.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean remove = index != -1;
            
            // and when the selection does not contain unremovable item
            if (remove) {
                VisualClassPathItem vcpi = 
                        (VisualClassPathItem) classpathTableModel.getValueAt(index, 0);
                
                if (!vcpi.canDelete()) {
                    remove = false;
                }
            }
            
            removeProjectButton.setEnabled(remove);
        }
        
        //--------------------------- TableModelListener -----------------------
        public void tableChanged(TableModelEvent e) {
            updateProperties(webProperties, classpathTableModel);
            
            if (e.getColumn() == 1) {
                //VisualClassPathItem cpItem = (VisualClassPathItem) classpathModel.getValueAt(e.getFirstRow(), 0);
                // cpItem.setPathInWAR((String) classpathModel.getValueAt(e.getFirstRow(), 1));
                fireActionPerformed();
            }
        }
    }
    
    private static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        
        public Component getTableCellRendererComponent(
                JTable table, Object value, 
                boolean isSelected, boolean hasFocus, 
                int row, int column) {
            
            assert value == null || value instanceof VisualClassPathItem;                 
            return super.getTableCellRendererComponent(
                    table,
                    (value == null) ? null : value.toString(), 
                    isSelected, false, row, column);
        }
    }
    
    private class ClasspathTableModel extends AbstractTableModel {
        
        public int getColumnCount() {
            return 2; //classpath item name, item location within WAR
        }
        
        public int getRowCount() {            
            return data == null ? 0 : data.length;
        }
        
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }
        
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }
    
    private class TargetComboBoxEditor extends DefaultCellEditor {
        
        public TargetComboBoxEditor(JComboBox combo) {
            super(combo);
        }
        
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, 
                int row, int column) {
            
            VisualClassPathItem vcpi = 
                    (VisualClassPathItem) classpathTableModel.getValueAt(row, 0);
            
            JComboBox comboBox = (JComboBox) getComponent();
            DefaultComboBoxModel comboModel = (DefaultComboBoxModel) comboBox.getModel();
            comboModel.removeAllElements();
            
            String type = null;
            if (vcpi != null) {
                type = vcpi.getAsaType();
            }
            
            if (type != null) {
                comboModel.addElement(type);
            }
            
            return comboBox;
        }
    }
}
