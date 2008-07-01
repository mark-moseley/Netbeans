
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
import java.util.Set;
import javax.swing.AbstractAction;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.MethodGenerator;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.util.WSDLUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author rico
 */
public class RemoveOperationAction extends AbstractAction{
    
    private Set<MethodModel> methods;
    private Service service;
    
    /** Creates a new instance of RemoveOperationAction */
    public RemoveOperationAction(Service service) {
        super(getName());
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(RemoveOperationAction.class, "Hint_RemoveOperation"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddOperationAction.class, "LBL_RemoveOperation_mnem_pos")));
        this.service = service;
        setEnabled(false);
    }
    
    public void setWorkingSet(Set<MethodModel> methods) {
        this.methods = methods;
        setEnabled(methods!=null&&!methods.isEmpty());
    }

    public void actionPerformed(ActionEvent arg0) {
        if(methods.size()<1) return;
        boolean singleSelection = methods.size()==1;
        String methodName = singleSelection?methods.iterator().next().getOperationName():""+methods.size();
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation
                (NbBundle.getMessage(RemoveOperationAction.class, 
                (singleSelection?"MSG_OPERATION_DELETE":"MSG_OPERATIONS_DELETE"), methodName));
        Object retVal = DialogDisplayer.getDefault().notify(desc);
        if (retVal == NotifyDescriptor.YES_OPTION) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.
                    getMessage(RemoveOperationAction.class, 
                    (singleSelection?"MSG_RemoveOperation":"MSG_RemoveOperations"), methodName)); //NOI18N
            Task task = new Task(new Runnable() {
                public void run() {
                    handle.start();
                    try{
                        removeOperation(methods);
                    }catch(IOException e){
                        handle.finish();
                        ErrorManager.getDefault().notify(e);
                    } finally{
                        handle.finish();
                    }
                }});
                RequestProcessor.getDefault().post(task);
        }
    }
    
     //converts the wsdlOperation name to Java name according to JAXWS rules
    private String convertOperationName(final String wsdlOperation){
        String name = wsdlOperation;
        String firstChar = name.substring(0,1);
        firstChar = firstChar.toLowerCase();
        name= firstChar.concat(name.substring(1));
        return name;
    }
    
    
    private void removeOperation(Set<MethodModel> methods) throws IOException {
        for(MethodModel method:methods) {
            String methodName = method.getOperationName();
            FileObject implementationClass = getImplementationClass(method);
            File wsdlFile = getWSDLFile(implementationClass);
            if(wsdlFile != null) {
                OperationGeneratorHelper generatorHelper = new OperationGeneratorHelper(wsdlFile);
                WSDLModel wsdlModel = WSDLUtils.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);    
                //TODO: methodName should be the equivalent operation name in the WSDL
                //i.e., should look at operationName annotation if present
                generatorHelper.removeWSOperation(wsdlModel, OperationGeneratorHelper.
                        getPortTypeNameFromImpl(implementationClass), methodName);
                generatorHelper.generateJavaArtifacts(service.getName(), implementationClass, convertOperationName(methodName), true);
            } else{
                //WS from Java
                MethodGenerator.deleteMethod(implementationClass, methodName);
            }
            //save the changes so events will be fired
            
            DataObject dobj = DataObject.find(implementationClass);
            if(dobj.isModified()) {
                SaveCookie cookie = dobj.getCookie(SaveCookie.class);
                if(cookie!=null) cookie.save();
            }
        }
    }
  
    private File getWSDLFile(FileObject implementationClass){
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
    
    private FileObject getImplementationClass(MethodModel method){
        FileObject implementationClass= null;
        FileObject classFO = method.getImplementationClass();
        String implClassName = service.getImplementationClass();
        if(service.getLocalWsdlFile() != null){
            Project project = FileOwnerQuery.getOwner(classFO);
            SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if(sgs.length > 0){
                ClassPath classPath = null;
                for(int i = 0; i < sgs.length; i++){
                    classPath = ClassPath.getClassPath(sgs[i].getRootFolder(),ClassPath.SOURCE);
                    if(classPath != null){
                        implementationClass = classPath.findResource(implClassName.replace('.', '/') + ".java");
                        if(implementationClass != null){
                            break;
                        }
                    }
                }
            }
        }else{
            implementationClass = classFO;
        }
        return implementationClass;
    }
    
}
