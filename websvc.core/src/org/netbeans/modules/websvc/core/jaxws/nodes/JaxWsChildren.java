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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.nodes.AbstractNode;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperGenerator;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.io.IOException;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.xml.sax.SAXException;

/*
 *  Children of the web service node, namely,
 *  the operations of the webservice
 */

public class JaxWsChildren extends Children.Keys/* implements MDRChangeListener  */{
    private static final java.awt.Image OPERATION_BADGE =
        org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/core/webservices/ui/resources/wsoperation.png" ); //NOI18N
    
    private FileObject implClass;
    private Service service;
    private FileObject srcRoot;
    
    private WsdlModel wsdlModel;
    private WsdlModeler wsdlModeler;
    private boolean modelGenerationFinished;

    
    public JaxWsChildren(Service service, FileObject srcRoot, FileObject implClass) {
        super();
        this.service = service;
        this.srcRoot=srcRoot;
        this.implClass = implClass;
    }

// Retouche
//    public ComponentMethodViewStrategy createViewStrategy() {
//        WSComponentMethodViewStrategy strategy = WSComponentMethodViewStrategy.instance();
//        return strategy;
//    }
//    
    
    private List<ExecutableElement> getPublicMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method:methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)) {
                publicMethods.add(method);
            }
        }
        return publicMethods;
    }

    protected void addNotify() {
        super.addNotify();
        if (isFromWsdl()) {
            try {
                FileObject localWsdlFolder = getJAXWSSupport().getLocalWsdlFolderForService(service.getName(),false);
                assert localWsdlFolder!=null:"Cannot find folder for local wsdl file"; //NOI18N
                FileObject wsdlFo =
                    localWsdlFolder.getFileObject(service.getLocalWsdlFile());
                if (wsdlFo==null) return;
                wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFo.getURL());
                String packageName = service.getPackageName();
                if (packageName!=null && service.isPackageNameForceReplace()) {
                    // set the package name for the modeler
                    wsdlModeler.setPackageName(packageName);
                } else {
                    wsdlModeler.setPackageName(null);
                }
                JAXWSSupport support = getJAXWSSupport();
                wsdlModeler.setCatalog(support.getCatalog());
                setBindings(support,wsdlModeler,service);
                modelGenerationFinished=false;
                ((JaxWsNode)getNode()).changeIcon();
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                    public void modelCreated(WsdlModel model) {
                        wsdlModel=model;
                        modelGenerationFinished=true;
                        ((JaxWsNode)getNode()).changeIcon();
                        if (model==null) {
                            DialogDisplayer.getDefault().notify(
                                    new JaxWsUtils.WsImportServiceFailedMessage(wsdlModeler.getCreationException()));
                        }
                        updateKeys();
                    }
                });
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
                updateKeys();
            }
        } else {
            assert(implClass != null);
            //registerListener();
            //methods = getMethods();
            //registerMethodListeners();
            updateKeys();
        }
    }

// Retouche
//    private void registerListener() {
//        if (implClass!=null) ((MDRChangeSource)implClass).addListener(this);
//    }
//    
//    private void registerMethodListeners() {
//        if (methods!=null) {
//            for (int i=0;i<methods.length;i++) {
//                ((MDRChangeSource)methods[i]).addListener(this);
//            }
//        }
//    }
//    
//    private void removeListener() {
//        if (implClass!=null) ((MDRChangeSource)implClass).removeListener(this);
//    }
//    
//    
//    private void removeMethodListeners() {
//        if (methods!=null) {
//            for (int i=0;i<methods.length;i++) {
//                ((MDRChangeSource)methods[i]).removeListener(this);
//            }
//        }
//    }
    
//    protected void removeNotify() {
//        setKeys(Collections.EMPTY_SET);
//        if (!isFromWsdl()) {
//            removeListener();
//            removeMethodListeners();
//            methods=null;
//        }
//        super.removeNotify();
//    }
    
    private void updateKeys() {
        if (isFromWsdl()) {
            List keys = new ArrayList();
            if (wsdlModel!=null) {
                WsdlService wsdlService = wsdlModel.getServiceByName(service.getServiceName());
                if (wsdlService!=null) {
                    WsdlPort wsdlPort = wsdlService.getPortByName(service.getPortName());
                    if (wsdlPort!=null)
                        keys =  wsdlPort.getOperations();
                }
            }
            setKeys(keys);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final List[] keys = new List[]{new ArrayList()};
                    if (implClass != null) {
                        JavaSource javaSource = JavaSource.forFileObject(implClass);
                        if (javaSource!=null) {
                            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                                public void run(CompilationController controller) throws IOException {
                                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                                    SourceUtils srcUtils = SourceUtils.newInstance(controller);
                                    if (srcUtils!=null) {
                                        // find WS operations
                                        // either annotated (@WebMethod) public mathods 
                                        // or all public methods
                                        List<ExecutableElement> publicMethods = getPublicMethods(controller, srcUtils.getTypeElement());
                                        List<ExecutableElement> wsOperations = new ArrayList<ExecutableElement>();
                                        boolean foundWebMethodAnnotation=false;
                                        for(ExecutableElement method:publicMethods) {
                                            List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
                                            boolean hasWebMethodAnnotation=false;
                                            for (AnnotationMirror an:annotations) {
                                                TypeElement webMethodEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                                                if (webMethodEl!=null && controller.getTypes().isSameType(webMethodEl.asType(), an.getAnnotationType())) {
                                                    hasWebMethodAnnotation=true;
                                                    break;
                                                }
                                            }
                                            if (hasWebMethodAnnotation) {
                                                if (!foundWebMethodAnnotation) {
                                                    foundWebMethodAnnotation=true;
                                                    // remove all methods added before
                                                    // because only annotated methods should be added
                                                    if (wsOperations.size()>0) wsOperations.clear();
                                                }
                                                wsOperations.add(method);
                                            } else if (!foundWebMethodAnnotation) {
                                                // there are only non-annotated methods present until now
                                                wsOperations.add(method);
                                            }
                                        } // for
                                        keys[0] = wsOperations;
                                    }
                                }
                                
                                public void cancel() {}
                            };
                            try {
                                javaSource.runUserActionTask(task, true);
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                        
// Retouche
// if (methods==null) 
//     registerMethodListeners();
// methods = keys[0];
                    }
                    setKeys(keys[0]);
                }
            });
        }
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof WsdlOperation) {
            return new Node[] {new OperationNode((WsdlOperation)key)};
        } else if(key instanceof ExecutableElement) {
//            Method method = (Method)key;
//            ComponentMethodViewStrategy cmvs = createViewStrategy();
//            return new Node[] {new MethodNode(method, implClass, new ArrayList(), cmvs)};
//        }
            final ExecutableElement method = (ExecutableElement)key;
            Node n = new AbstractNode(Children.LEAF) {

                @java.lang.Override
                public java.awt.Image getIcon(int type) {
                    return OPERATION_BADGE;
                }

                @Override
                public String getDisplayName() {
                    return method.getSimpleName().toString();
                }
            };
            
            return new Node[]{n};
        }
        return new Node[0];
    }
    
    private boolean isFromWsdl() {
        return service.getWsdlUrl()!=null;
    }
    
    private JAXWSSupport getJAXWSSupport() {
        return JAXWSSupport.getJAXWSSupport(srcRoot);
    }
    
    private void setBindings(JAXWSSupport support, WsdlModeler wsdlModeler, Service service) {
        Binding[] extbindings = service.getBindings();
        if (extbindings==null || extbindings.length==0) {
            wsdlModeler.setJAXBBindings(null);
            return;
        }
        String[] bindingFiles = new String[extbindings.length];
        for(int i = 0; i < extbindings.length; i++){
            bindingFiles[i] = extbindings[i].getFileName();
        }    
        /*
        String[] bindingFiles = service.getBindings();
        if (bindingFiles==null || bindingFiles.length==0) {
            wsdlModeler.setJAXBBindings(null);
            return;
        }
         */
        FileObject bindingsFolder = support.getBindingsFolderForService(getNode().getName(),true);
        List<URL> list = new ArrayList<URL>();
        for (int i=0;i<bindingFiles.length;i++) {
            FileObject fo = bindingsFolder.getFileObject(bindingFiles[i]);
            try {
                list.add(fo.getURL());
            } catch (FileStateInvalidException ex) {
                // if there is problem no bindings will be added
            }
        }
        URL[] bindings = new URL[list.size()];
        list.<URL>toArray(bindings);
        wsdlModeler.setJAXBBindings(bindings);
    }
    
    void refreshKeys(boolean downloadWsdl, final boolean refreshImplClass) {
        if (!isFromWsdl()) return;
        super.addNotify();
        List keys=null;
        try {
            // copy to local wsdl first
            JAXWSSupport support = getJAXWSSupport();
            
            if (downloadWsdl) {
                String serviceName = getNode().getName();
                FileObject xmlResorcesFo = support.getLocalWsdlFolderForService(serviceName,true);
                FileObject localWsdl = null;
                try {
                    localWsdl = WSUtils.retrieveResource(
                            xmlResorcesFo,
                            new URI(service.getWsdlUrl()));
                    // copy resources to WEB-INF/wsdl/${serviceName}
                    FileObject wsdlFolder = getWsdlFolderForService(support, serviceName);
                    WSUtils.copyFiles(xmlResorcesFo, wsdlFolder);
                } catch (URISyntaxException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (UnknownHostException ex) {
                    ErrorManager.getDefault().annotate(ex,
                            NbBundle.getMessage(JaxWsClientChildren.class,"MSG_ConnectionProblem"));
                    return;
                } catch (IOException ex) {
                    ErrorManager.getDefault().annotate(ex,
                            NbBundle.getMessage(JaxWsClientChildren.class,"MSG_ConnectionProblem"));
                    return;
                }
                
                // re-generate also wrapper wsdl file if necessary
                if (localWsdl!=null) {
                    WsdlWrapperHandler handler = null;
                    try {
                        handler = WsdlWrapperGenerator.parse(localWsdl.getURL().toExternalForm());
                    } catch (ParserConfigurationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (SAXException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    }
                    if (!handler.isServiceElement()) {
                        StreamSource source = new StreamSource(localWsdl.getURL().toExternalForm());
                        try {
                            File wrapperWsdlFile = new File(FileUtil.toFile(localWsdl.getParent()), WsdlWrapperGenerator.getWrapperName(localWsdl.getURL())); //NOI18N

                            if(!wrapperWsdlFile.exists()) {
                                try {
                                    wrapperWsdlFile.createNewFile();
                                } catch(IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
                                }
                            }
                            if (wrapperWsdlFile.exists()) {
                                WsdlWrapperGenerator.generateWrapperWSDLContent(wrapperWsdlFile, source, handler.getTargetNsPrefix(),localWsdl.getNameExt());
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                        }
                    }
                }
            }
            FileObject wsdlFo = 
                getJAXWSSupport().getLocalWsdlFolderForService(service.getName(),false).getFileObject(service.getLocalWsdlFile());
            wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFo.getURL());
            String packageName = service.getPackageName();
            if (packageName!=null && service.isPackageNameForceReplace()) {
                // set the package name for the modeler
                wsdlModeler.setPackageName(packageName);
            } else {
                wsdlModeler.setPackageName(null);
            }
            wsdlModeler.setCatalog(support.getCatalog());
            setBindings(support, wsdlModeler, service);
            
            // re-generate java artifacts
            regenerateJavaArtifacts();
            // update nodes and implementation class
            
            modelGenerationFinished=false;
            ((JaxWsNode)getNode()).changeIcon();
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    wsdlModel=model;
                    modelGenerationFinished=true;
                    ((JaxWsNode)getNode()).changeIcon();
                    if (model==null) {
                        DialogDisplayer.getDefault().notify(
                                new JaxWsUtils.WsImportServiceFailedMessage(wsdlModeler.getCreationException()));
                    }
                    updateKeys();
                    if (model!=null) {
                        try {    
                            // test if serviceName, portName are the same, change if necessary
                            String serviceName = service.getServiceName();
                            String portName = service.getPortName();
                            WsdlService wsdlService = model.getServiceByName(serviceName);
                            boolean jaxWsModelChanged=false;
                            if (wsdlService==null) {
                                wsdlService = (WsdlService)model.getServices().get(0);
                                service.setServiceName(wsdlService.getName());                                   
                                jaxWsModelChanged=true;
                            }
                            WsdlPort wsdlPort = wsdlService.getPortByName(portName);
                            if (wsdlPort==null) {
                                wsdlPort = (WsdlPort)wsdlService.getPorts().get(0);
                                service.setPortName(wsdlPort.getName());
                                jaxWsModelChanged=true;
                            }
                            
                            // test if package name for java artifacts hasn't changed
                            String oldPkgName = service.getPackageName();
                            if (wsdlService!=null && oldPkgName!=null && !service.isPackageNameForceReplace()) {
                                String javaName = wsdlService.getJavaName();
                                int dotPosition = javaName.lastIndexOf(".");
                                if (dotPosition>=0) {
                                    String newPkgName = javaName.substring(0,dotPosition);
                                    if (!oldPkgName.equals(newPkgName)) {
                                        service.setPackageName(newPkgName);
                                        jaxWsModelChanged=true;
                                    }
                                }
                            }

                            // save jax-ws model
                            if (jaxWsModelChanged) {
                                Project project = FileOwnerQuery.getOwner(srcRoot);
                                if (project!=null) {
                                    JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
                                    if (jaxWsModel!=null) jaxWsModel.write();
                                }

                            }
                            if (refreshImplClass) {
                                // re-generate implementation class
                                String implClass = service.getImplementationClass();
                                FileObject oldImplClass = srcRoot.getFileObject(implClass.replace('.','/')+".java"); //NOI18N
                                FileObject oldCopy = srcRoot.getFileObject(implClass.replace('.','/')+".java.old"); //NOI18N
                                int index = implClass.lastIndexOf(".");
                                FileObject folder = index>0?srcRoot.getFileObject(implClass.substring(0,index).replace('.','/')):srcRoot;
                                if (folder!=null) {
                                    String name = (index>=0?implClass.substring(index+1):implClass);
                                    if (oldImplClass!=null) {
                                        if (oldCopy!=null) oldCopy.delete();
                                        FileUtil.copyFile(oldImplClass, folder, name+".java", "old"); //NOI18N
                                        oldImplClass.delete();
                                    }
                                    JaxWsUtils.generateJaxWsImplementationClass(FileOwnerQuery.getOwner(srcRoot),
                                        folder, name, model, service);
                                    JaxWsNode parent = (JaxWsNode)getNode();
                                    FileObject newImplClass = srcRoot.getFileObject(implClass.replace('.','/')+".java"); //NOI18N
                                    if (newImplClass!=null) {
                                        JaxWsChildren.this.implClass=newImplClass;
                                    }
                                    parent.refreshImplClass();
                                    
                                }
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ErrorManager.ERROR,ex);
                        }
                    }
                }
            });
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
        }
    }
    
    private void regenerateJavaArtifacts() {
        Project project = FileOwnerQuery.getOwner(srcRoot);
        if (project!=null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
            try {
                String name = service.getName();
                ExecutorTask wsimportTask =
                    ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-service-clean-"+name,"wsimport-service-"+name},null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } 
        }
    }

// Retouche    
//    public static class WSComponentMethodViewStrategy implements ComponentMethodViewStrategy {
//        //  private Image NOT_OPERATION_BADGE = Utilities.loadImage("org/openide/src/resources/error.gif");
//        private static WSComponentMethodViewStrategy wsmvStrategy;
//        private WSComponentMethodViewStrategy(){
//        }
//        
//        public static WSComponentMethodViewStrategy instance(){
//            if(wsmvStrategy == null){
//                wsmvStrategy = new WSComponentMethodViewStrategy();
//            }
//            return wsmvStrategy;
//        }
//        public Image getBadge(Method method, Collection interfaces){
//            
//       /* no need to badge this, it sometimes not a sign for bad operation see 55679    Set paramTypes = new HashSet();
//            //FIX-ME:Need a better way to find out if method is in SEI
//            MethodParameter[] parameters = method.getParameters();
//            for(int i = 0; i < parameters.length; i++){
//                paramTypes.add(parameters[i].getType());
//            }
//            Iterator iter  = interfaces.iterator();
//            while(iter.hasNext()){
//                ClassElement intf = (ClassElement)iter.next();
//                if(intf.getMethod(method.getName(), (Type[])paramTypes.toArray(new Type[paramTypes.size()])) == null){
//                    return NOT_OPERATION_BADGE;
//                }
//        
//            }*/
//            
//            return null;
//        }
//        
//        public void deleteImplMethod(Method m, JavaClass implClass, Collection interfaces) throws IOException{
//            //delete method in the SEI
//            Iterator iter = interfaces.iterator();
//            while (iter.hasNext()){
//                JavaClass intf = (JavaClass)iter.next();
//                try {
//                    intf.getContents().remove(m);
//                } catch (JmiException e) {
//                    throw new IOException(e.getMessage());
//                }
//            }
//            //delete method from Impl class
//            Method[] methods = JMIUtils.getMethods(implClass);
//            for(int i = 0; i < methods.length; i++){
//                Method method = methods[i];
//                if (JMIUtils.equalMethods(m, method)) {
//                    try {
//                        implClass.getContents().remove(method);
//                        break;
//                    } catch (JmiException e) {
//                        throw new IOException(e.getMessage());
//                    }
//                }
//            }
//        }
//        
//        public OpenCookie getOpenCookie(Method m, JavaClass implClass, Collection interfaces) {
//            Method[] methods = JMIUtils.getMethods(implClass);
//            for(int i = 0; i < methods.length; i++) {
//                Method method = methods[i];
//                if (JMIUtils.equalMethods(m, method)) {
//                    return (OpenCookie)JMIUtils.getCookie(method, OpenCookie.class);
//                }
//            }
//            return null;
//        }
//        
//        public Image getIcon(Method me, Collection interfaces) {
//            return Utilities.loadImage("org/openide/src/resources/methodPublic.gif");
//        }
//        
//    }
    
//    public void change(MDRChangeEvent evt) {
//        if (evt.getSource() instanceof JavaClass) {
//            removeMethodListeners();
//            methods=null;
//            updateKeys();
//        } else if (evt.getSource() instanceof Method && evt instanceof AttributeEvent) {
//            AttributeEvent attrEvt = ((AttributeEvent) evt);
//            int type = attrEvt.getType();
//            if (type==AttributeEvent.EVENT_ATTRIBUTE_ADD) {
//                // annotation has been added
//                Object newElement = attrEvt.getNewElement();
//                if (newElement instanceof Annotation) {
//                    if ("javax.jws.WebMethod".equals(((Annotation)newElement).getType().getName())) //NOI18N
//                        updateKeys();
//                }
//            } else if (type == AttributeEvent.EVENT_ATTRIBUTE_REMOVE && "annotations".equals(attrEvt.getAttributeName())) { //NOI18N
//                // annotation has been removed
//                // NOTE: this may require more proper evaluation of deleted Attribute
//                updateKeys();
//            } else if (type == AttributeEvent.EVENT_ATTRIBUTE_SET && "modifiers".equals(attrEvt.getAttributeName())) { //NOI18N
//                // modifier has been changed
//                updateKeys();
//            }
//        }   
//    }
    
    private FileObject getWsdlFolderForService(JAXWSSupport support, String name) throws IOException {
        FileObject globalWsdlFolder = support.getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject(name);
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        return globalWsdlFolder.createFolder(name);
    }
    
    WsdlModeler getWsdlModeler() {
        return wsdlModeler;
    }
    
    boolean isModelGenerationFinished() {
        return modelGenerationFinished;
    }
    
}
