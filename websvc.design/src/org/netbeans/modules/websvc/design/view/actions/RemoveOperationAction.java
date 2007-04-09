/*
 * RemoveOperationAction.java
 *
 * Created on April 6, 2007, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.util.Util;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author rico
 */
public class RemoveOperationAction extends AbstractAction{
    
    private MethodModel method;
    private Service service;
    private FileObject implementationClass;
    private File wsdlFile;
    private String methodName;
    
    /** Creates a new instance of RemoveOperationAction */
    public RemoveOperationAction(Service service, MethodModel method) {
        super(getName());
        this.service = service;
        this.method = method;
        this.implementationClass = method.getImplementationClass();
        this.wsdlFile = getWSDLFile();
        this.methodName = method.getOperationName();
    }
    
    public void actionPerformed(ActionEvent arg0) {
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation
                (NbBundle.getMessage(RemoveOperationAction.class, "MSG_OPERATION_DELETE", methodName));
        Object retVal = DialogDisplayer.getDefault().notify(desc);
        if (retVal == NotifyDescriptor.YES_OPTION) {
            if(wsdlFile != null){
                WSDLModel wsdlModel = Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
                OperationGeneratorHelper generatorHelper = new OperationGeneratorHelper(wsdlFile);
                //TODO: methodName should be the equivalent operation name in the WSDL
                //i.e., should look at operationName annotation if present
                generatorHelper.removeWSOperation(wsdlModel, generatorHelper.
                        getPortTypeName(implementationClass), methodName);
                generatorHelper.generateJavaArtifacts(service.getName(), implementationClass, methodName, true);
                //TODO:this will go away when the recopying of the changed wsdls and schemas
                //from the src/conf to the WEB-INF/wsdl directory is done in the build script.
                try{
                    FileObject wsdlFolder = generatorHelper.
                            getWsdlFolderForService(implementationClass, service.getName());
                    FileObject localWsdlFolder = generatorHelper.
                            getLocalWsdlFolderForService(implementationClass, service.getName());
                    WSUtils.copyFiles(localWsdlFolder, wsdlFolder);
                }catch(IOException e){
                    ErrorManager.getDefault().notify(e);
                }
            }
            else{
                //WS from Java
            }
        }
        
    }
    
    private File getWSDLFile(){
        String localWsdlUrl = service.getLocalWsdlFile();
        if (localWsdlUrl!=null) { //WS from e
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(implementationClass);
            if (support!=null) {
                FileObject localWsdlFolder = support.getLocalWsdlFolderForService(service.getName(),false);
                if (localWsdlFolder!=null) {
                    File wsdlFolder = FileUtil.toFile(localWsdlFolder);
                    return  new File(wsdlFolder.getAbsolutePath()+File.separator+localWsdlUrl);
                }
            }
        }
        return null;
    }
    
    private static String getName() {
        return NbBundle.getMessage(RemoveOperationAction.class, "LBL_RemoveOperation");
    }
}
