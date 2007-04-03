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

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.util.Util;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class AddOperationAction extends AbstractAction {
    
    
    private FileObject implementationClass;
    private Service service;
    private File wsdlFile;
    /**
     * Creates a new instance of AddOperationAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddOperationAction(Service service, FileObject implementationClass) {
        super(getName());
        this.service=service;
        this.implementationClass = implementationClass;
        this.wsdlFile = getWSDLFile();
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation");
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
    
    public void actionPerformed(ActionEvent arg0) {
        if(wsdlFile != null && wsdlFile.exists()){
            final AddOperationFromSchemaPanel panel = new AddOperationFromSchemaPanel(wsdlFile);
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(AddOperationAction.class, "TTL_AddWsOperation"));
            desc.setButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        OperationGeneratorHelper generatorHelper = new OperationGeneratorHelper(wsdlFile);
                        
                        WSDLModel wsdlModel = Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
                        String operationName = panel.getOperationName();
                        List<ParamModel> parameterTypes = panel.getParameterTypes();
                        ReferenceableSchemaComponent returnType = panel.getReturnType();
                        List<ReferenceableSchemaComponent> faultTypes = panel.getFaultTypes();
                        Operation operation = generatorHelper.addWsOperation(wsdlModel, generatorHelper.getPortTypeName(implementationClass),
                                operationName, parameterTypes, returnType, faultTypes);
                        generatorHelper.generateJavaArtifacts(service.getName(), implementationClass, operationName);
                        
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
                }
            });
            
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
         } else { // WS from Java
            AddWsOperationHelper strategy = new AddWsOperationHelper(getName());
            try {
                String className = _RetoucheUtil.getMainClassName(implementationClass);
                if (className != null) {
                    strategy.addMethod(implementationClass, className);
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        //save the changes so events will be fired
        try {
            DataObject dobj = DataObject.find(implementationClass);
            if(dobj.isModified()) {
                SaveCookie cookie = dobj.getCookie(SaveCookie.class);
                if(cookie!=null) cookie.save();
            }
        } catch (IOException ex) {
        }
        
    }
    
}
