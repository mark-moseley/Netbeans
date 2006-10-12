/*
 * NewWSDLGenerator.java
 *
 * Created on September 1, 2006, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;


/**
 *
 * @author radval
 */
public class NewWSDLGenerator {
    
    private FileObject mWsdlFile;
    
    private TemplateWizard mTemplateWizard;
    
    private WSDLModel mModel;
   
    private WsdlGenerationUtil mUtil;
    
    /** Creates a new instance of NewWSDLGenerator */
    public NewWSDLGenerator(FileObject newWSDLFile, TemplateWizard templateWizard) {
        this.mWsdlFile = newWSDLFile;
        this.mTemplateWizard = templateWizard;
        ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(this.mWsdlFile, 
			true);
        
        mModel = WSDLModelFactory.getDefault().getModel(modelSource);
        this.mUtil = new WsdlGenerationUtil(this.mModel);
    }
    
    public void generate() {
        
        if(mModel != null) {
            mModel.startTransaction();
            
            Map configurationMap = new HashMap();
            
            //portType
            String portTypeName = (String) this.mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.PORTTYPE_NAME);
            String operationName = (String) this.mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.OPERATION_NAME);
            OperationType ot = (OperationType) this.mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.OPERATION_TYPE);
            
            configurationMap.put(WizardPortTypeConfigurationStep.PORTTYPE_NAME, portTypeName);
            configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_NAME, operationName);
            configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_TYPE, ot);
           
            //opertion type
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> inputMessageParts = 
                    (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.OPERATION_INPUT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> outputMessageParts = 
                    (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.OPERATION_OUTPUT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> faultMessageParts = 
                    (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.OPERATION_FAULT);

            
            Map<String, String> namespaceToPrefixMap = (Map<String, String>) mTemplateWizard.getProperty(WizardPortTypeConfigurationStep.NAMESPACE_TO_PREFIX_MAP);
            configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_INPUT, inputMessageParts);
            configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_OUTPUT, outputMessageParts);
            configurationMap.put(WizardPortTypeConfigurationStep.OPERATION_FAULT, faultMessageParts);
            configurationMap.put(WizardPortTypeConfigurationStep.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
            //binding
            String bindingName = (String) this.mTemplateWizard.getProperty(WizardBindingConfigurationStep.BINDING_NAME);
            LocalizedTemplateGroup bindingType = (LocalizedTemplateGroup) this.mTemplateWizard.getProperty(WizardBindingConfigurationStep.BINDING_TYPE);
            configurationMap.put(WizardBindingConfigurationStep.BINDING_NAME, bindingName);
            configurationMap.put(WizardBindingConfigurationStep.BINDING_TYPE, bindingType);
           
            //this could be null for a binding which does not have a sub type
            LocalizedTemplate bindingSubType = (LocalizedTemplate) this.mTemplateWizard.getProperty(WizardBindingConfigurationStep.BINDING_SUBTYPE);
            configurationMap.put(WizardBindingConfigurationStep.BINDING_SUBTYPE, bindingSubType);
            
            //service and port
            String serviceName = (String) this.mTemplateWizard.getProperty(WizardBindingConfigurationStep.SERVICE_NAME);
            String servicePortName = (String) this.mTemplateWizard.getProperty(WizardBindingConfigurationStep.SERVICEPORT_NAME);
            configurationMap.put(WizardBindingConfigurationStep.SERVICE_NAME, serviceName);
            configurationMap.put(WizardBindingConfigurationStep.SERVICEPORT_NAME, servicePortName);
            
            if (namespaceToPrefixMap != null) {
                for (String namespace : namespaceToPrefixMap.keySet()) {
                    ((AbstractDocumentComponent) mModel.getDefinitions()).addPrefix(namespaceToPrefixMap.get(namespace), namespace);
                }
            }
            
            PortTypeGenerator ptGenerator = new PortTypeGenerator(this.mModel, configurationMap);
            ptGenerator.execute();
            PortType pt = ptGenerator.getPortType();
            
            if(pt != null) {
                BindingGenerator bg = new BindingGenerator(this.mModel, pt, configurationMap);
                bg.execute();
            }
            
            mModel.endTransaction();
        }
        
    }
    
    
}
