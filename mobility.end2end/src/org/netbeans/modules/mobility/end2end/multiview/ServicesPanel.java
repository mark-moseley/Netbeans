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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ServicesPanel.java
 *
 * Created on July 25, 2005, 10:28 AM
 */
package org.netbeans.modules.mobility.end2end.multiview;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.*;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.ui.treeview.MethodCheckedTreeBeanView;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.mobility.end2end.util.WebServiceNodeManager;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;


/**
 *
 * @author  Michal Skvor, Bohemius
 */
public class ServicesPanel extends SectionInnerPanel implements ExplorerManager.Provider, PropertyChangeListener {
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    protected transient E2EDataObject dataObject;
    
    private transient ExplorerManager manager;
    private transient Node rootNode;
    
    private MethodCheckedTreeBeanView checkedTreeView;
    private RequestProcessor.Task repaintingTask;
    
    boolean wsdl = false;
    
    private Configuration configuration;
    
    private javax.swing.JLabel servicesLabel;
    private BeanTreeView treeView;
    private Node waitNode;
    private TreeUpdater updater;
    
    private FileObject serverProjectFolder;
    
    private ServicePCL servicePcl;
    
    public ServicesPanel() {
        this( null, null, null );
    }
    
    /** Creates new form ServicesPanel */
    public ServicesPanel( SectionView sectionView, E2EDataObject dataObject, Configuration configuration ) {
        super( sectionView );
        
        this.dataObject = dataObject;
        this.configuration = configuration;
        initComponents();
        servicesLabel = new javax.swing.JLabel();
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        if (dataObject != null){
            gridBagConstraints.insets = new java.awt.Insets(5, 11, 5, 0);
        } else {
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        }
        add(servicesLabel, gridBagConstraints);
        
        if( dataObject == null ){
            generateButton.setVisible( false ); //not visible for wizard
        } else {
            if( dataObject.isGenerating()) //for case we are generating
                generateButton.setEnabled( false );
        }
        
        if (configuration != null){
            create();
        }
    }
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_ServicesPanel" ));
        
        if( checkedTreeView != null ) {
            checkedTreeView.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_Services" ));
        } else if( treeView != null ) {
            treeView.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_Services" ));
        }
    }
    
    private void create() {
        wsdl = Configuration.WSDLCLASS_TYPE.equals( configuration.getServiceType());
        Mnemonics.setLocalizedText(servicesLabel,
                !wsdl ? NbBundle.getMessage( ServicesPanel.class, "LBL_Methods" ) :
                    NbBundle.getMessage( ServicesPanel.class, "LBL_Operations" ));
        
        if( checkedTreeView != null ){
            remove( checkedTreeView );
            checkedTreeView = null;
        }
        final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        if( dataObject != null ) { //different borders for wizard and panel
            gridBagConstraints.insets = new java.awt.Insets( 5, 12, 5, 12 );
        }
        
        treeView = new BeanTreeView();
        treeView.setBorder( new LineBorder( Color.BLACK, 1, true ));
        add( treeView, gridBagConstraints );
        
        servicesLabel.setLabelFor( treeView );
        
        waitNode = new WaitNode();
        waitNode.setDisplayName( !wsdl ?
            NbBundle.getMessage( ServicesPanel.class, "MSG_WaitComputingMethods" ) :
            NbBundle.getMessage( ServicesPanel.class, "MSG_WaitComputingWebServices" ));
        getExplorerManager().setRootContext( waitNode );
        
        updater = new TreeUpdater();
        repaintingTask = RequestProcessor.getDefault().create( updater );
        
        initAccessibility();
    }
    
    public void setConfiguration( final Configuration configuration ){
        this.configuration = configuration;
        
        if (configuration != null){
            create();
        }
    }
    
    public Configuration getConfiguration(){
        return configuration;
    }
    
    public void addNotify() {
        super.addNotify();
        if ( dataObject != null ){
            dataObject.addPropertyChangeListener(this);
        }
        repaintingTask.schedule( 100 );
    }
    
    public void removeNotify(){
        if (repaintingTask != null){
            repaintingTask.cancel();
        }
        getExplorerManager().removePropertyChangeListener(this);
        if ( dataObject != null ){
            dataObject.removePropertyChangeListener(this);
        }
        super.removeNotify();
    }
    
    /** Get the explorer manager.
     * @return the manager
     */
    public ExplorerManager getExplorerManager() {
        if( manager == null )
            manager = new ExplorerManager();
        return manager;
    }
    
    protected void updateTree() {
//        if( JavaMetamodel.getManager().isScanInProgress()) {
//            final Runnable update = new Runnable() {
//                public void run() {
//                    JavaMetamodel.getManager().waitScanFinished();
//                    initTreeView();
//                }
//            };
//            update.run();
//        } else {
            initTreeView();
//        }
    }
    
    protected void initTreeView() {
        final Project serverProject = Util.getServerProject( configuration );
        if( !wsdl ) {
            rootNode = ServiceNodeManager.getRootNode( serverProject );
        } else {
            final List<AbstractService> services = configuration.getServices();
//            //assert services.length != 1; //there is problem
            final WSDLService service = (WSDLService)services.get(0);
            final WebServicesRegistryView wsrv = Lookup.getDefault().lookup(WebServicesRegistryView.class);
            if (servicePcl != null){
                wsrv.removePropertyChangeListener(servicePcl);
            }
//            if (dataObject != null && wsrv != null && !wsrv.isServiceRegistered(service.getName())){
//                if (servicePcl == null){
//                    servicePcl = new ServicePCL();
//                }
//                wsrv.addPropertyChangeListener(servicePcl);
//                rootNode = WebServiceNodeManager.getAvailableWSRootNode( serverProject, service.getFile() );
//                waitNode.setDisplayName(NbBundle.getMessage( ServicesPanel.class, "MSG_WaitComputingWebServices"));
//                return;
//            }
            rootNode = WebServiceNodeManager.getAvailableWSRootNode( serverProject, service.getFile() );
            
            final JAXWSClientView a = JAXWSClientView.getJAXWSClientView();
            Node n = a.createJAXWSClientView( serverProject );
            for( Node nn : n.getChildren().getNodes()) {
                if( nn.getDisplayName().equals( service.getName()))
                    rootNode = nn;
            }
            if (rootNode.getChildren().getNodesCount() == 0){
                repaintingTask = RequestProcessor.getDefault().create( updater  );
                repaintingTask.schedule( 500 );
                return;
            } else {
                final List<ClassData> ports = service.getData();
                
                FileObject generatedClientFO = 
                        serverProject.getProjectDirectory().getFileObject( "build/generated/wsimport/client/" );
                // Add all paths to the ClasspathInfo structure
                List<ClasspathInfo> classpaths = Collections.singletonList( ClasspathInfo.create( generatedClientFO ));
                // Get the registry for all available classes
                ClassDataRegistry registry = ClassDataRegistry.getRegistry( ClassDataRegistry.DEFAULT_PROFILE, classpaths );
                
                PortData port = null;
                if( ports != null && ports.size() > 0 ) port = (PortData)ports.get( 0 ); // Only one port allowed
                
                for( Node serviceNode : rootNode.getChildren().getNodes()) {
                    for( Node portNode : serviceNode.getChildren().getNodes()) {
                        WsdlPort wsdlPort = portNode.getLookup().lookup( WsdlPort.class );
                        String serviceFQN = wsdlPort.getJavaName();
                        if( port != null && !portNode.getName().equals( port.getName())) continue;
                        org.netbeans.modules.mobility.e2e.classdata.ClassData cd = registry.getClassData( wsdlPort.getJavaName());
                        for( Node operationNode : portNode.getChildren().getNodes()) {
                            WsdlOperation wsdlOperation = operationNode.getLookup().lookup( WsdlOperation.class );
                            org.netbeans.modules.mobility.e2e.classdata.MethodData methodData = null;
                            for( org.netbeans.modules.mobility.e2e.classdata.MethodData md : cd.getMethods()) {
                                if( md.getName().equals( wsdlOperation.getJavaName())) {
                                    methodData = md;
                                }
                            }
                            if( methodData == null ) {
                                operationNode.setValue( ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE, Boolean.FALSE );
                            } else {
                                operationNode.setValue( ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE, Boolean.TRUE );
                            }
                        }
                    }
                }                
            }
        }
        
        //remove listeners while selecting
        getExplorerManager().removePropertyChangeListener( this );
        
        if( treeView != null && rootNode.getChildren().getNodesCount() != 0 ) {
            setVisible( false );
            remove( treeView );
        }
        
        if( !wsdl && rootNode.getChildren().getNodesCount() == 0 ) {
            waitNode.setDisplayName( NbBundle.getMessage( ServicesPanel.class, "MSG_NoMethodAvailable" ));
            return;
        }
        
        final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        if( dataObject != null ) //different borders for wizard and panel
            gridBagConstraints.insets = new java.awt.Insets( 5, 10, 5, 0 );
        
        checkedTreeView = new MethodCheckedTreeBeanView();
        checkedTreeView.setPopupAllowed( false );
        checkedTreeView.setDefaultActionAllowed( false );
        checkedTreeView.setBorder( new LineBorder( Color.BLACK, 1, true ));
        checkedTreeView.setRootVisible( false );
        checkedTreeView.setRoot( rootNode );
        getExplorerManager().setRootContext( rootNode );
        checkedTreeView.initTreeWithAllUnselected();
        add( checkedTreeView, gridBagConstraints );
        setVisible( true );
        if( configuration != null && configuration.getServices() != null ) {
            setSelectedMethods();
        }
        
        servicesLabel.setLabelFor( checkedTreeView );
        
        //add back after selection
        getExplorerManager().addPropertyChangeListener( this );
        
        checkedTreeView.addChangeListener( new ChangeListener() {
            public void stateChanged( @SuppressWarnings( "unused" ) ChangeEvent e ) {
                if( dataObject != null ) {
                    ServicesPanel.this.dataObject.setModified( true );
                }
            }
        });
        
        checkedTreeView.setEnabled( true );
        if( dataObject != null && !dataObject.isGenerating()){
            generateButton.setEnabled( true );
        }
        treeView = null;
        
        initAccessibility();
    }
    
    private void setSelectedMethods() {
        if( !wsdl ) {
            final AbstractService classService = configuration.getServices().get(0);
            final List<ClassData> classes = classService.getData();
            if (classes == null) return;
            final Error error = new Error( Error.TYPE_WARNING, Error.MISSING_VALUE_MESSAGE,
                                    NbBundle.getMessage( ServicesPanel.class, "ERR_ServiceMethodNotFound" ), this );
            for ( final ClassData classData : classes ) {
                final String className = classData.getClassName();
                final String pkgName = classData.getPackageName();
                final List<OperationData> methods = classData.getOperations();
                for ( final OperationData methodData : methods ) {
                    final String methodName = methodData.getName();
                    try {
                        checkedTreeView.setState(
                                NodeOp.findPath(rootNode, new String[]{pkgName, className, methodName}), true );
                        checkedTreeView.expandNode(
                                NodeOp.findPath(rootNode, new String[]{pkgName, className }));
                    } catch ( NodeNotFoundException ex) {
                        //System.err.println(" SETTING ERROR OUTPUT");
                        final SectionView sectionView = getSectionView();
                        if (sectionView != null){
                            sectionView.getErrorPanel().setError( error );
                        }
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        } else {            
//            //assert dataObject.getConfiguration().getServices().length == 1;
            final WSDLService wsdlService = (WSDLService)configuration.getServices().get(0); //TODO we are assuming only one service
//            //assert services != null; //fail situation TODO how to solve?
            final String serviceName = wsdlService.getName();
            final List<ClassData> ports = wsdlService.getData();
            final Error error = new Error( Error.TYPE_WARNING, Error.MISSING_VALUE_MESSAGE,
                                    NbBundle.getMessage( ServicesPanel.class, "ERR_ServiceMethodNotFound" ), this );
            //search for selections
            for( int i = 0; ports != null && i < ports.size(); i++ ) {
                final PortData portData = (PortData)ports.get( i );
                final String port = portData.getName();
                final List<OperationData> operations = portData.getOperations();
                for ( final OperationData operationData : operations ) {
                    final String operation = operationData.getMethodName();
                    try {
                        final Node pkgNode = NodeOp.findPath( rootNode, new String[]{serviceName, port, operation});
                        checkedTreeView.setState( pkgNode, true );
                    } catch (Exception e){
                        final SectionView sectionView = getSectionView();
                        if( sectionView != null ) {
                            sectionView.getErrorPanel().setError( error );
                        }
                        ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
                    }
                }
            }
            checkedTreeView.expandAll();
        }
    }
    
    public void getSelectedMethods() {
//        //todo should distinguish between WS and classes
        if (!wsdl) {
            final List<ClassData> classData = new ArrayList<ClassData>();
            final Node packageNodes[] = rootNode.getChildren().getNodes();

               for ( Node pkNode : packageNodes ) {
                    final Node classNodes[] = pkNode.getChildren().getNodes();
                    for ( Node clNode : classNodes ) {
                        final Node methodNodes[] = clNode.getChildren().getNodes();
                        final List<OperationData> methodData = new ArrayList<OperationData>();
                        for ( Node node : methodNodes ) {
                            if( checkedTreeView.getState( node ).equals( MethodCheckedTreeBeanView.SELECTED )) {
                                final org.netbeans.modules.mobility.e2e.classdata.MethodData mthData = (
                                        org.netbeans.modules.mobility.e2e.classdata.MethodData) node.getLookup().lookup(org.netbeans.modules.mobility.e2e.classdata.MethodData.class);
                                final List<org.netbeans.modules.mobility.e2e.classdata.MethodParameter> params = mthData.getParameters();
                                final List<TypeData> newParams = new ArrayList<TypeData>(params.size());
                                for ( final org.netbeans.modules.mobility.e2e.classdata.MethodParameter param : params ) {
                                    //have a list of parameters for each method
                                    final TypeData td = new TypeData( param.getName(), param.getType().getName());
                                    newParams.add( td );
                                }
                                final OperationData od = new OperationData( mthData.getName());
                                od.setReturnType( mthData.getReturnType().getName());
                                od.setParameterTypes( newParams );
                                methodData.add( od );
                            }
                        }
                        if (methodData.size() != 0){
                            final String classFQN = ((org.netbeans.modules.mobility.e2e.classdata.ClassData)
                                clNode.getLookup().lookup(org.netbeans.modules.mobility.e2e.classdata.ClassData.class)).getFullyQualifiedName();

                            final ClassData cd = new ClassData( classFQN );
                            cd.setOperations( methodData );
                            classData.add( cd );
                        }
                    }
                }
                final ClassService classService = (ClassService)configuration.getServices().get(0);
                classService.setData( classData );
                final List<AbstractService> services = new ArrayList<AbstractService>();
                services.add(classService);
                configuration.setServices( services );

        } else { //we are wsdl
            final List<AbstractService> servicesData = new ArrayList<AbstractService>();
            final Node serviceNodes[] = rootNode.getChildren().getNodes();
            WSI serviceClassInfo = null;
                                
            for( int i = 0; i < serviceNodes.length; i++ ) { //there is only one service node now!
                final Node portNodes[] = serviceNodes[i].getChildren().getNodes();
                final List<ClassData> classData = new ArrayList<ClassData>();
                                
                for( int j = 0; j < portNodes.length; j++ ) {
                    final Node operationNodes[] = portNodes[j].getChildren().getNodes();
                    final List<OperationData> methodData = new ArrayList<OperationData>();
                    
                    for( int k = 0; k < operationNodes.length; k++ ) {
                        final String operationName = operationNodes[k].getName(); //name of the operation (selection)
                        
                        if( checkedTreeView.getState( operationNodes[k] ).equals( MethodCheckedTreeBeanView.SELECTED )) {
                            serviceClassInfo = computeMethodCall( operationNodes[ k ] );
                            if( serviceClassInfo == null ) {
                                continue;
                            }
//                            final OperationData md = getMethodData( serviceClassInfo.getFqPortTypeName(), operationName );
                            final OperationData md = new OperationData( operationName );
                            WsdlOperation wsdlOp = operationNodes[k].getLookup().lookup( WsdlOperation.class );
                            md.setMethodName( wsdlOp.getName());
                            md.setReturnType( wsdlOp.getReturnTypeName());
                            List<WsdlParameter> wsdlParams = wsdlOp.getParameters();
                            List<TypeData> params = new ArrayList<TypeData>();
                            for( WsdlParameter param : wsdlParams ) {
                                params.add( new TypeData( param.getName(), param.getTypeName()));
                            }
                            md.setParameterTypes( params );
                            if( md != null ) {
                                methodData.add( md );
                            }
                        }
                    }
                    if( serviceClassInfo != null && methodData.size() != 0 ) { //class was found
                        final PortData pd = new PortData( serviceClassInfo.getFqPortTypeName());
                        pd.setName( portNodes[j].getName());
                        pd.setOperations( methodData );
                        classData.add( pd );
                    }
                }
//                final DataObject wsdlObj = serviceNodes[i].getLookup().lookup(DataObject.class);
//                String fileURL = "";
//                try {
//                    //TODO store/read it in project relative style
//                    fileURL = FileUtil.toFile(wsdlObj.getPrimaryFile()).toURL().toString();
//                } catch (MalformedURLException ex) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                }
                final WSDLService wsdlService = (WSDLService)configuration.getServices().get(0);
//                if (serviceClassInfo != null)
//                    wsdlService.setType(serviceClassInfo.getFqServiceClassName());
                wsdlService.setData( classData );
//                wsdlService.setUrl( fileURL );
//                wsdlService.setFile( wsdlObj.getPrimaryFile().getNameExt());
//                final ServiceInformation si = (ServiceInformation) wsdlObj.getCookie(ServiceInformation.class);
//                wsdlService.setName( normalizeAgainstWsdl(serviceNodes[i].getName(), si.getServiceNames()));
                wsdlService.setName( serviceNodes[i].getDisplayName());
                servicesData.add( wsdlService );
            }
            configuration.setServices( servicesData );
        }
    }
            
    private static WSI computeMethodCall( final Node serviceOperationNode ) {        
        try {
            final Node servicePortNode = serviceOperationNode.getParentNode();
            final Node serviceNode = servicePortNode.getParentNode();
//            
            final Client client = serviceNode.getParentNode().getLookup().lookup( Client.class );
//            final DataObject wsdlObj = serviceNode.getLookup().lookup(DataObject.class);
//            
            final String servicePortName = client.getName();
//            
//            final ServiceInformation si = (ServiceInformation) wsdlObj.getCookie(ServiceInformation.class);
//            final String serviceName = normalizeAgainstWsdl(serviceNode.getName(), si.getServiceNames()); //MUST BE STORED FOR LATTER USAGE
            final String serviceName = client.getName();
            final String serviceClassName = client.getName(); //here is the class name //??
//            
            String servicePackageName = client.getPackageName();
            String servicePortTypeName = servicePortName;
//          
//            final ServiceInformation serviceInfo = (ServiceInformation) serviceNode.getCookie(ServiceInformation.class);
//            if (serviceInfo != null) {
//                servicePackageName = serviceInfo.getServicePackageName();
//                final List<PortInformation.PortInfo> portInfoList = serviceInfo.getServicePorts(serviceName);
//                for ( final PortInformation.PortInfo portInfo : portInfoList ) {
//                    if(servicePortName.equals(portInfo.getPort())) {
//                        servicePortTypeName = classFromName(portInfo.getPortType());
//                        break;
//                    }
//                }
//            }
//            
            if( servicePackageName == null ) {
                return null;
            }
            final String fqServiceClassName = servicePackageName + "." + serviceClassName; //NOI18N //MUST BE STORED FOR LATTER USAGE
            final String fqPortTypeName = servicePackageName + "." + servicePortTypeName; //NOI18N //MUST BE STORED FOR LATTER USAGE
////            
            return new WSI( serviceName, fqServiceClassName, fqPortTypeName );
        } catch (NullPointerException npe) {
            ErrorManager.getDefault().notify(npe);
        }
        return null;
    }
    
    private static String classFromName(final String name) {
        String result = name;
        
        if(name.length() > 0 && !Character.isUpperCase(name.charAt(0))) {
            final StringBuffer buf = new StringBuffer(name);
            buf.setCharAt(0, Character.toUpperCase(name.charAt(0)));
            result = buf.toString();
        }
        
        return result;
    }
    private static String normalizeAgainstWsdl(final String suggestedName, final String [] serviceNames) {
        String result = suggestedName; // default to what was passed in.
        
        for(int i = 0; i < serviceNames.length; i++) {
            if(suggestedName.equalsIgnoreCase(serviceNames[i])) {
                result = serviceNames[i];
                break;
            }
        }
        
        return result;
    }
        
    public JComponent getErrorComponent( @SuppressWarnings("unused")
	final String errorId ) {
        return null;
    }
    
    public void linkButtonPressed( @SuppressWarnings("unused")
	final Object ddBean, @SuppressWarnings("unused")
	final String ddProperty ) {
    }
    
    public void setValue( @SuppressWarnings("unused")
	final JComponent source, @SuppressWarnings("unused")
	final Object value ) {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        generateButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(500, 350));
        setLayout(new java.awt.GridBagLayout());

        generateButton.setMnemonic(org.openide.util.NbBundle.getBundle(ServicesPanel.class).getString("MNM_Generate").charAt(0));
        generateButton.setText(org.openide.util.NbBundle.getBundle(ServicesPanel.class).getString("LBL_Generate")); // NOI18N
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(generateButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
//        generateButton.setEnabled(false);
        dataObject.generate(false);
    }//GEN-LAST:event_generateButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton generateButton;
    // End of variables declaration//GEN-END:variables
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())){
            getSelectedMethods();
            System.err.println(" - selection changed");
//            final AbstractService service = configuration.getServices().get(0);
//            final SectionView sectionView = getSectionView();
//            if (sectionView != null){
//                if( service == null || ( service != null && service.getData().size() == 0 )) {
//                    sectionView.getErrorPanel().setError(
//                            new Error( Error.TYPE_FATAL, Error.MISSING_VALUE_MESSAGE,
//                            NbBundle.getMessage( ServicesPanel.class, "ERR_MissingServiceSelection" ), checkedTreeView ));
//                    //dataObject.setSaveEnable( false );
//                    generateButton.setEnabled( false );
//                } else {
//                    sectionView.getErrorPanel().clearError();
//                    //dataObject.setSaveEnable( true );
//                    generateButton.setEnabled( true );
//                }
//            }
//            fireChange();
        } else if (E2EDataObject.PROP_GENERATING.equals(evt.getPropertyName())){
            generateButton.setEnabled(!Boolean.TRUE.equals(evt.getNewValue()));
        }
    }
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    
    private static class WSI {
        final private String serviceName;
        final private String fqServiceClassName;
        final private String fqPortTypeName;
        
        WSI(String serviceName, String fqServiceClassName, String fqPortTypeName) {
            this.serviceName = serviceName;
            this.fqServiceClassName = fqServiceClassName;
            this.fqPortTypeName = fqPortTypeName;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getFqServiceClassName() {
            return fqServiceClassName;
        }
        
        public String getFqPortTypeName() {
            return fqPortTypeName;
        }
    }
    
    private class TreeUpdater implements Runnable {
        
        TreeUpdater() {
            //to avoid creation of accessor class
        }
        
        public void run() {
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    updateTree();
                }
            });
        }
    }
    
    public void setServerProjectFolder(final FileObject serverProjectFolder) {
        this.serverProjectFolder = serverProjectFolder;
    }
    
    private class ServicePCL implements PropertyChangeListener {
        ServicePCL() {
            //to avoid creation of accessor class
        }
        
        public void propertyChange(@SuppressWarnings("unused")
		final PropertyChangeEvent evt) {
            updateTree();
        }
    }
    
    private static final class WaitNode extends AbstractNode {
        
        public WaitNode() {
            super( Children.LEAF );
        }
    }
}
