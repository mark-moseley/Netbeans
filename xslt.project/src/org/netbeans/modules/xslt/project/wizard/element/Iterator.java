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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentFactory;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.impl.VariableReferenceImpl;
import org.openide.cookies.SaveCookie;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.loaders.DataFolder;
import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
public final class Iterator implements TemplateWizard.Iterator {

  /**{@inheritDoc}*/
  public static Iterator createXsl() {
    return new Iterator();
  }

  /**{@inheritDoc}*/
  public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
    return Collections.singleton(createFile(wizard));
  }

  /**{@inheritDoc}*/
  public void initialize(TemplateWizard wizard) {
    myPanel = new PanelStartup<WizardDescriptor>(Templates.getProject(wizard), null);
    myWizard = wizard;
  }

  /**{@inheritDoc}*/
  public void uninitialize(TemplateWizard wizard) {
    myPanel = null;
  }

  /**{@inheritDoc}*/
  public String name() {
    return i18n(Iterator.class, "LBL_Title"); // NOI18N
  }
  
  /**{@inheritDoc}*/
  public boolean hasNext() {
    return myPanel.getNext() != null;
  }
  
  /**{@inheritDoc}*/
  public boolean hasPrevious() {
    return myPanel.getPrevious() != null;
  }
  
  /**{@inheritDoc}*/
  public void nextPanel() {
    myPanel = myPanel.getNext();
  }
  
  /**{@inheritDoc}*/
  public void previousPanel() {
    myPanel = myPanel.getPrevious();
  }

  /**{@inheritDoc}*/
  public WizardDescriptor.Panel<WizardDescriptor> current() {
    return myPanel;
  }
  
  /**{@inheritDoc}*/
  public void addChangeListener(ChangeListener listener) {}

  /**{@inheritDoc}*/
  public void removeChangeListener(ChangeListener listener) {}

  private DataObject createFile(TemplateWizard wizard) throws IOException {
    FileObject file = null;
    Project project = Templates.getProject(wizard);
    String choice = (String) wizard.getProperty(Panel.CHOICE);

    TransformationUseCasesFactory tUseCaseFactory = TransformationUseCasesFactory.getInstance();
    file = tUseCaseFactory.createUseCase(project, wizard);
    
    return file == null ? null : DataObject.find(file);
  }

  private static TransformationUseCase getUseCase(TemplateWizard wizard) {
    String choice = wizard == null ? null : (String) wizard.getProperty(Panel.CHOICE);      
    return choice == null ? null :  TRANSFORMATION_USE_CASE.get(choice);
  }
  
  private enum TransformationUseCase {
    REQUEST_REPLY,
    FILTER_ONE_WAY,
    FILTER_REQUEST_REPLY;
  }
  
  private static class TransformationUseCasesFactory {

    private static TransformationUseCasesFactory INSTANCE = new TransformationUseCasesFactory();

    private TransformationUseCasesFactory() {}

    public static TransformationUseCasesFactory getInstance() {
        return INSTANCE;
    }

    public FileObject createUseCase(Project project, TemplateWizard wizard) throws IOException {
        assert project != null && wizard != null;

        List<FileObject> createdFos = new ArrayList<FileObject>();
        TransformationUseCase useCase = getUseCase(wizard);
        assert useCase != null;

        FileObject fo = createXslFiles(useCase, project, wizard, createdFos);

        if (fo != null) {
            try {
                configureTMapModel(useCase, project, wizard);
            } catch (IOException ex) {
                rollbackCreatedXslFiles(createdFos);
                fo = null;
                throw ex;
            }
        }
        return fo;
    }

    private void rollbackCreatedXslFiles(List<FileObject> createdFos) throws IOException {
        assert createdFos != null;
        for (FileObject fo : createdFos) {
            if (fo != null && fo.isValid()) {
                fo.delete();
            }
        }
    }
    
    private FileObject createXslFiles(TransformationUseCase useCase, 
            Project project, TemplateWizard wizard, 
            List<FileObject> createdFos) throws IOException
    {
        assert project != null && useCase != null && wizard !=null;

        String file1 = (String) wizard.getProperty(Panel.INPUT_FILE);
        String file2 = TransformationUseCase.FILTER_REQUEST_REPLY.equals(useCase) 
                ?  (String) wizard.getProperty(Panel.OUTPUT_FILE) : null;

        FileObject file = null;
        if (file1 != null) {
            file = createXslFile(
                    project, file1, createdFos, wizard);
        }

        if (file2 != null) {
          file = createXslFile(project, file2, createdFos, wizard);
        }
        return file;
    }

    private FileObject createXslFile(
        Project project,
        String file, List<FileObject> createdFos, TemplateWizard wizard) throws IOException
    {
        if (file == null || "".equals(file)) {
            return null;
        }

        int extIndex = file.lastIndexOf(XSL)-1; 
        if (extIndex <= 0) {
            return null;
        }

        file = file.substring(0, extIndex);

        if ("".equals(file)) {
            return null;
        }
        
        boolean isAllowSlash = false;
        boolean isAllowBackslash = false;
        if (File.separatorChar == '\\') {
            isAllowBackslash = true;
            file = file.replace('/', File.separatorChar);
        } else {
            isAllowSlash = true;
        }
        StringTokenizer dirTokens = new StringTokenizer(file, File.separator);
        int numDirs = dirTokens.countTokens();
        String[] dirs = new String[numDirs];
        int i = 0;
        while (dirTokens.hasMoreTokens()) {
            dirs[i] = dirTokens.nextToken();
            i++;
        }

        FileObject dirFo = Util.getSrcFolder(project);
        boolean isCreatedDir = false;
        if ( numDirs > 1 ) {
            file = dirs[numDirs-1];
            for (int j = 0; j < numDirs-1; j++) {
                FileObject tmpDirFo = 
                        dirFo.getFileObject(dirs[j]);
                if (tmpDirFo == null) {
                    try {
                        dirFo = dirFo.createFolder(dirs[j]);
                    } catch (IOException ex) {
                        rollbackCreatedXslFiles(createdFos);
                        throw ex;
                    }
                    
                    if (dirFo == null) {
                        rollbackCreatedXslFiles(createdFos);
                        break;
                    }
                    // add just parentFo 
                    if (!isCreatedDir) {
                        isCreatedDir = true;
                        createdFos.add(dirFo);
                    }
                } else {
                    dirFo = tmpDirFo;
                }
            }
        }
        FileObject xslFo = null;

        if (dirFo != null) {
            xslFo = dirFo.getFileObject(file);
            if (xslFo == null) {
                xslFo = Util.copyFile(dirFo, 
                    TEMPLATES_PATH, XSLT_SERVICE,
                    file, XSL);
                if (!isCreatedDir) {
                    createdFos.add(xslFo);
                }
                SoaUiUtil.fixEncoding(DataObject.find(xslFo), dirFo);
            }
        }
        return xslFo;
    }

  private void configureTMapModel(TransformationUseCase useCase, 
            Project project, TemplateWizard wizard) throws IOException
    {
        assert useCase != null && project != null && wizard != null;

        FileObject tMapFo = getTMapFo(project);
        TMapModel tMapModel = tMapFo == null ? null : 
                org.netbeans.modules.xslt.tmap.util.Util.getTMapModel(tMapFo);        

        if (tMapModel == null 
                || ! TMapModel.State.VALID.equals(tMapModel.getState())) 
        {
            throw new IllegalStateException(""+tMapModel.getState());
        }

        switch (useCase) {
            case REQUEST_REPLY:
                configureRequestReply(tMapModel, wizard);
                break;
            case FILTER_ONE_WAY:
                configureFilterOneWay(tMapModel, wizard);
                break;
            case FILTER_REQUEST_REPLY:
                configureFilterRequestReply(tMapModel, wizard);
                break;
        }

        saveConfiguredModel(tMapFo);
    }

    private void saveConfiguredModel(FileObject tMapFo) throws IOException {
        if (tMapFo == null) {
            return;
        }

        DataObject dObj = DataObject.find(tMapFo);
        if (dObj != null && dObj.isModified()) {

            SaveCookie saveCookie = dObj.getLookup().
                    lookup(SaveCookie.class);
            assert saveCookie != null;
            saveCookie.save();
        }
    }

    private FileObject getTMapFo(Project project) {
        FileObject tMapFo = org.netbeans.modules.xslt.tmap.util.Util.getTMapFo(project);
        if (tMapFo == null) {
            tMapFo = org.netbeans.modules.xslt.tmap.util.Util.createDefaultTransformmap(project);
        }
        return tMapFo;
    }

    public void configureRequestReply(TMapModel tMapModel, TemplateWizard wizard) {
        assert tMapModel != null && wizard != null;
        try {
            tMapModel.startTransaction();
            TMapComponentFactory componentFactory = tMapModel.getFactory();

            org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = 
                  setOperation(tMapModel, wizard, componentFactory);

            Transform rrTransform = null;
            if (tMapOp != null) {
              rrTransform = createTransform(componentFactory, 
                      (String)wizard.getProperty(Panel.INPUT_FILE), tMapOp);
            }

            if (rrTransform != null) {
              tMapOp.addTransform(rrTransform);
            }
        } finally {
          tMapModel.endTransaction();
        }
    }

    public void configureFilterOneWay(TMapModel tMapModel, 
            TemplateWizard wizard) 
    {
        assert tMapModel != null && wizard != null;
        try {
            tMapModel.startTransaction();
            TMapComponentFactory componentFactory = tMapModel.getFactory();

            org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = 
                  setOperation(tMapModel, wizard, componentFactory);

            Transform foTransform = null;
            if (tMapOp != null) {
              foTransform = createTransform(componentFactory, 
                      (String)wizard.getProperty(Panel.INPUT_FILE), tMapOp);
            }

            Invoke invoke = null;
            if (tMapOp != null) {
                invoke = createInvoke(tMapOp, wizard, componentFactory);
            }
            

            if (foTransform != null) {
                tMapOp.addTransform(foTransform);
            }
            if (invoke != null) {
                tMapOp.addInvoke(invoke);
            }
            
            if (tMapOp != null) {
                List<Transform> children =  tMapOp.getTransforms();
                if (children != null) {
                    for (Transform child : children) {
//                        System.out.println("child: "+child+";   orig: "+foTransform+"; isEquals::: "+(child.equals(foTransform)));
                        if (child.equals(foTransform)) {
                            foTransform = child;
                            break;
                        }
                    }
                }
                
               List<Invoke> invokes = tMapOp.getInvokes();
//               System.out.println("invoke before changes: "+invoke);
               invoke = invokes == null || invokes.size() < 1 ? invoke : invokes.get(invokes.size()-1);
//               System.out.println("invoke after changes: "+invoke);
                
            }
            
            if (foTransform != null) {
                String result = getTMapVarRef(invoke.getInputVariable());
                if (result != null) {
                    foTransform.setResult(result);
                }
            }
            
        } finally {
          tMapModel.endTransaction();
        }
    }

    public void configureFilterRequestReply(TMapModel tMapModel, 
            TemplateWizard wizard) 
    {
        assert tMapModel != null && wizard != null;
        try {
            tMapModel.startTransaction();
            TMapComponentFactory componentFactory = tMapModel.getFactory();

            org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = 
                  setOperation(tMapModel, wizard, componentFactory);

            if (tMapOp == null) {
                return;
            }
            Invoke invoke = null;
            String inputInvokeVar = getVariableName(INPUT_INVOKE_VARIABLE_PREFIX, 
                getVariableNumber(tMapOp, INPUT_INVOKE_VARIABLE_PREFIX, 1));
            String outputInvokeVar = getVariableName(OUTPUT_INVOKE_VARIABLE_PREFIX, 
                getVariableNumber(tMapOp, OUTPUT_INVOKE_VARIABLE_PREFIX, 1));


            Transform inTransform = null;
            inTransform = createTransform(componentFactory, 
                    (String)wizard.getProperty(Panel.INPUT_FILE), 
                    tMapOp);

            invoke = createInvoke(tMapOp, inputInvokeVar, outputInvokeVar,
                      wizard, componentFactory);

            Transform outTransform = null;
            outTransform = createTransform(componentFactory, 
                    (String)wizard.getProperty(Panel.OUTPUT_FILE), 
                    tMapOp);

            if (inTransform != null) {
                tMapOp.addTransform(inTransform);
            }
            if (invoke != null) {
                tMapOp.addInvoke(invoke);
            }
            if (outTransform != null) {
                tMapOp.addTransform(outTransform);
                String source = getTMapVarRef(invoke.getOutputVariable());
                if (source != null) {
                    outTransform.setSource(source);
                }
            }
            
            if (inTransform != null) {
                String result = getTMapVarRef(invoke.getInputVariable());
                if (result != null) {
                    inTransform.setResult(result);
                }
            }
            
        } finally {
          tMapModel.endTransaction();
        }
    }

    private int getVariableNumber(
            org.netbeans.modules.xslt.tmap.model.api.Operation operation, 
            String varNamePrefix, int startNumber) 
    {
        if (operation == null || varNamePrefix == null) {
            return startNumber;
        }

        List<Variable> vars = operation.getVariables();
        if (vars == null || vars.size() < 1) {
        }

        int count = startNumber;
        List<String> varNames = new ArrayList<String>();

        for (Variable var : vars) {
            String tmpVarName = var == null ? null : var.getName();
            if (tmpVarName != null) {
                varNames.add(tmpVarName);
            }
        }

        while (true) {
            if (!varNames.contains(varNamePrefix + count)) {
                break;
            }
            count++;
        }
        return count;
    }

    private String getVariableName(String varPrefix, int varNumber) {
        varPrefix = varPrefix == null ? DEFAULT_VARIABLE_PREFIX : varPrefix;
        return varPrefix + varNumber;
    }

    private Service getTMapService(TMapModel model, 
            PartnerLinkType wizardInPlt, Role wizardInRole) 
    {
        if (model == null || wizardInPlt == null || wizardInRole == null) {
            return null;
        }

        Service service = null;
        TransformMap root = model.getTransformMap();
        if (root == null) {
            return service;
        }

        List<Service> services = root.getServices();
        if (services == null || services.size() < 1) {
            return service;
        }

        for (Service serviceElem : services) {
            WSDLReference<PartnerLinkType> pltRef = serviceElem.getPartnerLinkType();
            WSDLReference<Role> roleRef = serviceElem.getRole();

            if (roleRef != null && pltRef != null 
                    && wizardInPlt.equals(pltRef.get()) 
                    && wizardInRole.equals(roleRef.get())) 
            {
                service = serviceElem;
                break;
            }
        }
        return service;
    }

    private org.netbeans.modules.xslt.tmap.model.api.Operation getTMapOperation(
            Service tMapService, Operation wizardInputOperation) 
    {
        org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = null;

        if (tMapService == null) {
            return tMapOp;
        }

        List<org.netbeans.modules.xslt.tmap.model.api.Operation> operations = 
                tMapService.getOperations();
        if (operations == null || operations.size() < 1) {
            return tMapOp;
        }

        for (org.netbeans.modules.xslt.tmap.model.api.Operation operationElem : operations) {
             Reference<Operation> opRef = operationElem.getOperation();

            if (opRef != null && wizardInputOperation.equals(opRef.get())) {
                tMapOp = operationElem;
                break;
            }
        }
        return tMapOp;
  }

    private org.netbeans.modules.xslt.tmap.model.api.Operation getTMapOperation(
            TMapModel model, PartnerLinkType wizardInPlt, Role wizardInRole, 
            Operation wizardInputOperation) 
    {
        return getTMapOperation(
                getTMapService(model, wizardInPlt, wizardInRole), 
                wizardInputOperation);
    }

    private org.netbeans.modules.xslt.tmap.model.api.Operation setOperation(
        TMapModel tMapModel, 
        TemplateWizard wizard, 
        TMapComponentFactory componentFactory) {
        assert tMapModel != null && wizard != null && componentFactory != null;

        String inputFileStr = (String) wizard.getProperty(Panel.INPUT_FILE);
        Operation wizardInputOperation = 
                (Operation) wizard.getProperty(Panel.INPUT_OPERATION);
        Panel.PartnerRolePort wizardInputPartnerRolePort = 
                (Panel.PartnerRolePort) wizard.getProperty(Panel.INPUT_PARTNER_ROLE_PORT);

        PartnerLinkType wizardInPlt = wizardInputPartnerRolePort == null 
                ? null : wizardInputPartnerRolePort.getPartnerLinkType();
        Role wizardInRole = wizardInputPartnerRolePort == null 
                ? null : wizardInputPartnerRolePort.getRole();

        if (wizardInPlt == null || wizardInRole == null 
                || wizardInputOperation == null) 
        {
            return null;
        }

        Service tMapService = getTMapService(tMapModel, wizardInPlt, wizardInRole);
        if (tMapService == null) {
            tMapService = createTMapService(componentFactory, tMapModel, 
                    wizardInPlt, wizardInRole);
        }
        if (tMapService == null) {
            return null;
        }
        org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = null;
        tMapOp = getTMapOperation(tMapService, wizardInputOperation);

        if (tMapOp == null) {
            tMapOp = componentFactory.createOperation();
            tMapOp.setOperation(
                    tMapOp.createWSDLReference(wizardInputOperation, Operation.class));

            tMapService.addOperation(tMapOp);
            tMapOp.setInputVariableName(
                    getVariableName(INPUT_OPERATION_VARIABLE_PREFIX, 1));

            tMapOp.setOutputVariableName(
                    getVariableName(OUTPUT_OPERATION_VARIABLE_PREFIX, 1));
        }
        return tMapOp;
    }

    private Service createTMapService(TMapComponentFactory componentFactory, TMapModel tMapModel, PartnerLinkType wizardInPlt, Role wizardInRole) {
        assert componentFactory != null && tMapModel != null && wizardInPlt != null && wizardInRole != null;
        Service tMapService = null;

        TransformMap root = tMapModel.getTransformMap();

        if (root == null) {
            root = componentFactory.createTransformMap();
            tMapModel.addChildComponent(null, root, -1);
        }
        if (root == null) {
            return null;
        }
        tMapService = componentFactory.createService();
        tMapService.setPartnerLinkType(tMapService.createWSDLReference(wizardInPlt, PartnerLinkType.class));
        tMapService.setRole(tMapService.createWSDLReference(wizardInRole, Role.class));

        root.addService(tMapService);

        return tMapService;
    }

    private Transform createTransform(TMapComponentFactory componentFactory, 
            String inputFileStr, Variable source, Variable result) 
    {
        if (source == null || result == null) {
            return null;
        }

        Transform transform = componentFactory.createTransform();
        if (inputFileStr != null && !"".equals(inputFileStr)) {
            transform.setFile(inputFileStr);
        }
        String sourcePartName = getFirstPartName(source);
        transform.setSource(getTMapVarRef(source, sourcePartName));

        String resultPartName = getFirstPartName(result);
        transform.setResult(getTMapVarRef(result, resultPartName));
        return transform;
    }

    private Transform createTransform(TMapComponentFactory componentFactory, 
            String inputFileStr, VariableDeclarator variableHolder) 
    {
        if (variableHolder == null) {
            return null;
        }
        return createTransform(componentFactory, inputFileStr, 
                variableHolder.getInputVariable(), 
                variableHolder.getOutputVariable());
    }

    private String getTMapVarRef(Variable var) {
        String firstPartName = var == null ? null : getFirstPartName(var);
        return getTMapVarRef(var, firstPartName);
    }

    private String getTMapVarRef(Variable var, String partName) {
        if (partName == null || var == null) {
            return null;
        }
        String varName = var.getName();

        return varName == null 
              ? null : VariableReferenceImpl.getVarRefString(varName, partName);
    }

    private String getFirstPartName(Reference<Message> messageRef) {
        String partName = null;
        if (messageRef == null) {
            return partName;
        }

        Message message = messageRef.get();

        Collection<Part> parts = null;
        if (message != null) {
            parts = message.getParts();
        }

        Part part = null;
        if (parts != null && parts.size() > 0) {
            java.util.Iterator<Part> partIter = parts.iterator();
            part = partIter.next();
        }

        if (part != null) {
            partName = part.getName();
        }
        return partName;
    }

    private String getFirstPartName(Variable var) {
        if (var == null) {
            return null;
        }
        return getFirstPartName(var.getMessage());
    }

    private String getFirstPartName(OperationParameter opParam) {
        if (opParam == null) {
            return null;
        }
        return getFirstPartName(opParam.getMessage());
    }

    private Invoke createInvoke(
          org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp, 
          TemplateWizard wizard, TMapComponentFactory componentFactory) 
    {
        return createInvoke(tMapOp, null, null, wizard, componentFactory);
    }

    private Invoke createInvoke(
          org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp, 
          String inputInvokeVar,
          String outputInvokeVar,
          TemplateWizard wizard, TMapComponentFactory componentFactory) 
    {
        assert tMapOp != null && wizard != null && componentFactory != null;
        Invoke invoke = null;

        Operation wizardOutputOperation = 
                (Operation) wizard.getProperty(Panel.OUTPUT_OPERATION);
        Panel.PartnerRolePort wizardOutputPartnerRolePort = 
                (Panel.PartnerRolePort) wizard.getProperty(Panel.OUTPUT_PARTNER_ROLE_PORT);

        PartnerLinkType wizardOutPlt = wizardOutputPartnerRolePort == null 
                ? null : wizardOutputPartnerRolePort.getPartnerLinkType();
        Role wizardOutRole = wizardOutputPartnerRolePort == null 
                ? null : wizardOutputPartnerRolePort.getRole();

        if (wizardOutPlt != null 
                && wizardOutRole != null 
                && wizardOutputOperation != null) 
        {
          invoke = componentFactory.createInvoke();
          invoke.setPartnerLinkType(
                  invoke.createWSDLReference(wizardOutPlt, PartnerLinkType.class));
          invoke.setRole(
                  invoke.createWSDLReference(wizardOutRole, Role.class));
          invoke.setOperation(
                  invoke.createWSDLReference(wizardOutputOperation, Operation.class));

          if (inputInvokeVar == null || "".equals(inputInvokeVar)) {
              inputInvokeVar = getVariableName(INPUT_INVOKE_VARIABLE_PREFIX, 
                  getVariableNumber(tMapOp, INPUT_INVOKE_VARIABLE_PREFIX, 1));
          }
          if (outputInvokeVar == null || "".equals(outputInvokeVar)) {
              outputInvokeVar = getVariableName(OUTPUT_INVOKE_VARIABLE_PREFIX, 
                  getVariableNumber(tMapOp, OUTPUT_INVOKE_VARIABLE_PREFIX, 1));
          }
          invoke.setInputVariableName(inputInvokeVar);
          invoke.setOutputVariableName(outputInvokeVar);
        }
        return invoke;
    }
  }

  private static Map<String, TransformationUseCase> TRANSFORMATION_USE_CASE = 
          new HashMap<String, TransformationUseCase>();
  static {
      TRANSFORMATION_USE_CASE.put(Panel.CHOICE_REQUEST_REPLY,
              TransformationUseCase.REQUEST_REPLY);
      TRANSFORMATION_USE_CASE.put(Panel.CHOICE_FILTER_ONE_WAY,
              TransformationUseCase.FILTER_ONE_WAY);
      TRANSFORMATION_USE_CASE.put(Panel.CHOICE_FILTER_REQUEST_REPLY,
              TransformationUseCase.FILTER_REQUEST_REPLY);
  }
  
  private static String TEMPLATES_PATH = "Templates/SOA_XSLT/"; // NOI18N
  private static String XSLT_SERVICE = "xslt.service"; // NOI18N
  private static String XSL = "xsl"; // NOI18N
  private Panel<WizardDescriptor> myPanel;
  private TemplateWizard myWizard;
  
  private static final String DEFAULT_VARIABLE_PREFIX = "var"; // NOI18N
  private static final String INPUT_OPERATION_VARIABLE_PREFIX = "inOpVar"; // NOI18N
  private static final String OUTPUT_OPERATION_VARIABLE_PREFIX = "outOpVar"; // NOI18N
  private static final String INPUT_INVOKE_VARIABLE_PREFIX = "inInvokeVar"; // NOI18N
  private static final String OUTPUT_INVOKE_VARIABLE_PREFIX = "outInvokeVar"; // NOI18N
}
