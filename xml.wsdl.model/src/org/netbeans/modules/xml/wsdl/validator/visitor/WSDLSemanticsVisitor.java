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

package org.netbeans.modules.xml.wsdl.validator.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLAttribute;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.wsdl.validator.visitor.schema.SchemaSemanticsVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;



/**
 * Visits the model nodes and validates them.
 *
 * @author  sbyn
 * TODO: add validation for extensibility elements like bpws: property, propertyAlias
 * in a generic way. someone can register a validator for these and validate them
 * @version $Revision$
 */
public class WSDLSemanticsVisitor  implements WSDLVisitor {
    
    /** MessageManager for localized strings. */
    private static MessageManager mMsg = MessageManager.getManager(WSDLSemanticsVisitor.class);
    
    private ValidateSupport mValidateSupport = null;
    /** Validate configuration singleton. */
    private static ValidateConfiguration mValConfig;
    
    /** Fault can not be thrown by one-way or notification type operation */
    public static final String VAL_FAULT_NOT_ALLOWED_IN_OPERATION = "VAL_FAULT_NOT_ALLOWED_IN_OPERATION";  // Not I18N
    
    /** Fix 'Fault can not be thrown by one-way or notification type operation'
     * by removing faults */
    public static final String FIX_FAULT_NOT_ALLOWED_IN_OPERATION = "FIX_FAULT_NOT_ALLOWED_IN_OPERATION";  // Not I18N
    
    /** Message not found for operation input */
    public static final String VAL_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT =
            "VAL_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT";
    
    /** Message not found for operation output */
    public static final String VAL_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT =
            "VAL_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT";
    
    /** Message not found for operation fault */
    public static final String VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT =
            "VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT";
    
    /** Fix for message not found for operation input */
    public static final String FIX_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT =
            "FIX_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT";
    
    /** Fix for Message not found for operation output */
    public static final String FIX_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT =
            "FIX_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT";
    
    /** Fix for Message not found for operation fault */
    public static final String FIX_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT =
            "FIX_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT";
    
    /** Schema in part not found */
    public static final String VAL_SCHEMA_DEFINED_NOT_FOUND = "VAL_SCHEMA_DEFINED_NOT_FOUND";
    
    /** Fix for Schema in part not found */
    public static final String FIX_SCHEMA_DEFINED_NOT_FOUND = "FIX_SCHEMA_DEFINED_NOT_FOUND";
    
    /** Schema is not defined in part */
    public static final String VAL_NO_SCHEMA_DEFINED = "VAL_NO_SCHEMA_DEFINED";
    
    /** Fix for Schema is not defined in part */
    public static final String FIX_NO_SCHEMA_DEFINED = "FIX_NO_SCHEMA_DEFINED";
    
    /** partnerLinkType portType does not exist in wsdl file */
    public static final String VAL_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_WSDL = "VAL_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_WSDL";
    
    /** Fix for partnerLinkType portType does not exist in wsdl file */
    public static final String FIX_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_WSDL = "FIX_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_WSDL";
    
    /**Message has zero parts so it is valid as per wsdl schema but we need a warning*/
    public static final String VAL_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED = "VAL_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED";
    
    /**Message has zero parts so it is valid as per wsdl schema but we need a warning*/
    public static final String FIX_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED = "FIX_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED";
    
    /** part does not have element or type attribute */
    public static final String VAL_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "VAL_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    /** part does not have element or type attribute */
    public static final String FIX_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "FIX_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    /** part has both element and type  attribute */
    public static final String VAL_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "VAL_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    /** part has both element and type  attribute */
     public static final String FIX_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "FIX_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    
    /**part has element attribute but the referenced element object can not be located*/
    public static final String VAL_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "VAL_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**part has element attribute but the referenced element object can not be located*/
    public static final String FIX_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "FIX_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**part has type attribute but the referenced type object can not be located*/
    public static final String VAL_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "VAL_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**part has element attribute but the referenced type object can not be located*/
    public static final String FIX_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "FIX_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**Binding has wrong or missing PortType */
    public static final String VAL_MISSING_PORTTYPE_IN_BINDING = "VAL_MISSING_PORTTYPE_IN_BINDING";
    public static final String FIX_MISSING_PORTTYPE_IN_BINDING = "FIX_MISSING_PORTTYPE_IN_BINDING";
    
    /**Service Port has wrong or missing Binding */
    public static final String VAL_MISSING_BINDING_IN_SERVICE_PORT = "VAL_MISSING_BINDING_IN_SERVICE_PORT";
    public static final String FIX_MISSING_BINDING_IN_SERVICE_PORT = "FIX_MISSING_BINDING_IN_SERVICE_PORT";
    
    /**Import does not have imported document object */
    public static final String VAL_MISSING_IMPORTED_DOCUMENT = "VAL_MISSING_IMPORTED_DOCUMENT";
    public static final String FIX_MISSING_IMPORTED_DOCUMENT = "FIX_MISSING_IMPORTED_DOCUMENT";
    
    /** PortType operation input name should be unique across all operation inputs in a port type*/
    public static final String VAL_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE = "VAL_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE";
    public static final String FIX_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE = "FIX_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE";
    
    /** PortType operation output name should be unique across all operation outputs in a port type*/
    public static final String VAL_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE = "VAL_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE";
    public static final String FIX_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE = "FIX_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE";
    
    
    /** operation falut name should be unique across all operation faults*/
    public static final String VAL_DUPLICATE_OPRATION_FAULT_NAME = "VAL_DUPLICATE_OPRATION_FAULT_NAME";
    public static final String FIX_DUPLICATE_OPRATION_FAULT_NAME = "FIX_DUPLICATE_OPRATION_FAULT_NAME";
    
    /** binding operation name does not match name of portType operations*/
    public static final String VAL_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE";
    
    private static final String VAL_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE = "VAL_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE";
    
    private static final String FIX_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE = "FIX_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE";
    
    private static final String VAL_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "VAL_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    private static final String FIX_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "FIX_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    private static final String VAL_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "VAL_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    private static final String FIX_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "FIX_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE";
    
     public static final String VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE";
    
    
    public List<ResultItem> mResultItems;
    private Validation mValidation;
    private List<Model> mValidatedModels;
    private Validator mValidator;
    
    /** Creates a new instance of WSDLSchemaVisitor */
    public WSDLSemanticsVisitor(Validator validator, Validation validation, List<Model> validatedModels) {
        
        Properties defaults = new Properties();
        defaults.setProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_REQUIRED, "true");
        defaults.setProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_QNAME, "true");
        defaults.setProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_NCNAME, "false");
        defaults.setProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_BOOLEAN, "true");
        defaults.setProperty(ValidateConfiguration.WSDL_SYNTAX_ELEM_MIN, "true");
        defaults.setProperty(ValidateConfiguration.WSDL_SYNTAX_ELEM_REQUIRED, "true");
        
        synchronized (this.getClass()) {
            mValConfig = new ValidateConfiguration(defaults);
            mResultItems = new Vector<ResultItem>();
        }
        
        mValidator = validator;
        mValidation = validation;
        mValidatedModels = validatedModels;
        
         getValidateSupport().setValidator(mValidator);
         getValidateSupport().setResultItems(mResultItems);
    }
    
    public List<ResultItem> getResultItems() {
        return mResultItems;
    }
    
    /** Gets the validate visitor support.
     * @return  Visitor support.
     */
    public ValidateSupport getValidateSupport() {
        if (null == mValidateSupport) {
            mValidateSupport = new ValidateSupport(mValConfig);
        }
        return mValidateSupport;
    }
    
    public Validation getValidation() {
        return mValidation;
    }
    
    public void setValidation(Validation validation) {
        this.mValidation = validation;
    }
    
    
   
    
    
    /**
     * Visits a WSDL definitions.
     * @param w a WSDL definitions element
     * @return  <code>true</code> if auto-traversal is to continue.
     */
    public void visit(Definitions w) {
        if (w.getTargetNamespace() == null || w.getTargetNamespace().trim().length() == 0) {
            getValidateSupport().fireToDo(Validator.ResultType.WARNING, w, mMsg.getString(VAL_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE),
                    mMsg.getString(FIX_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE));
            
        }
        visitChildren(w);
    }
    
    /** Visits a wsdl:message element.
     * @param   w   a wsdl:message element
     * @return  <tt>true</tt> if traversal is to continue.
     */
    public void visit(Message w) {
        //if Message does not have any part defined, show user a warning.
        if(w.getParts().size() == 0 ) {
            getValidateSupport().fireToDo(
                    Validator.ResultType.WARNING, w,
                    mMsg.getString(VAL_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED, w.getName()),
                    mMsg.getString(FIX_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED, w.getName()));
        }
        
        visitChildren(w);
        //return true;d
    }
    
    /** Visits a part element.
     * @param   p   a part element
     * @return  <tt>true</tt> if traversal is to continue.
     */
    public void visit(final Part p) {
        //if both element and type attribute are missing then part is invalid
        if(p.getElement() == null && p.getType() == null) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, p,
                    mMsg.getString(VAL_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART, p.getName()),
                    mMsg.getString(FIX_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART));
        }
        
        //if both element and type attribute are specified then part is invalid
        if(p.getElement() != null && p.getType() != null) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, p,
                    mMsg.getString(VAL_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART, p.getName()),
                    mMsg.getString(FIX_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART));
        }
        
        //if element attribute is specified and xsd element object can not be resolved
        //then its an error.
        if(p.getElement() != null && p.getElement().get() == null) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, p,
                    mMsg.getString(VAL_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID, p.getName(), p.getAttribute(new StringAttribute(Part.ELEMENT_PROPERTY))), //NOTI18N
                    mMsg.getString(FIX_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID));
        }
        
        //if type attribute is specified and xsd type object can not be resolved
        //then its an error.
        if(p.getType() != null && p.getType().get() == null) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, p,
                    mMsg.getString(VAL_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID, p.getName(), p.getAttribute(new StringAttribute(Part.TYPE_PROPERTY))), //NOTI18N
                    mMsg.getString(FIX_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID));
        }
        
        visitChildren(p);
        // Validate that the part refers to a message existing
        //return true;
    }
    
    /**
     * Visits a portType element.
     * @param portType a portType element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(PortType portType) {
        //wsdl 1.1 spec validation:
        //2.4.5 Names of Elements within an Operation:
        //The name attribute of the input and output elements provides a unique name
        //among all input and output elements within the enclosing port type
        
        //(a)validate if all operation input name are unique
        //(b)validate if all operation output name are unique
        ArrayList<String> inputNames = new ArrayList<String>();
        ArrayList<String> outputNames = new ArrayList<String>();
        Collection operations = portType.getOperations();
        Iterator it = operations.iterator();
        while(it.hasNext()) {
            Operation operation = (Operation) it.next();
            Input input = operation.getInput();
            if(input != null) {
                String inputName = input.getName();
                if(inputName != null) {
                    if(!inputNames.contains(inputName)) {
                        inputNames.add(inputName);
                    } else {
                        //found duplicate input name in this portType operations
                        getValidateSupport().fireToDo
                                (Validator.ResultType.ERROR, input,
                                mMsg.getString(VAL_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE, inputName, portType.getName()),
                                mMsg.getString(FIX_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE));
                    }
                }
            }
            
            Output output = operation.getOutput();
            if(output != null) {
                String outputName = output.getName();
                if(outputName != null) {
                    if(!outputNames.contains(outputName)) {
                        outputNames.add(outputName);
                    } else {
                        //found duplicate output name in this portType operations
                        getValidateSupport().fireToDo
                                (Validator.ResultType.ERROR, output,
                                mMsg.getString(VAL_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE, outputName, portType.getName()),
                                mMsg.getString(FIX_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE));
                    }
                }
            }
        }
        
        visitChildren(portType);
        //return true;
    }
    
    /**
     * Visits a portType operation element.
     * @param operation a portType operation element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(NotificationOperation operation) {
        // if the opertion is an one-way operation or a notification operation,
        // then there should be no faults
        if (operation.getFaults() != null && operation.getFaults().size() > 0) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.WARNING, operation,
                    mMsg.getString(VAL_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()),
                    mMsg.getString(FIX_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()));
        }
        
                
        visitChildren(operation);
        //return true;
    }
    
    public void visit(RequestResponseOperation operation) {
        // if the opertion is an one-way operation or a notification operation,
        // then there should be no faults
        if ((operation.getInput() == null || operation.getOutput() == null)
        && (operation.getFaults() != null && operation.getFaults().size() > 0)) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.WARNING, operation,
                    mMsg.getString(VAL_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()),
                    mMsg.getString(FIX_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()));
        }
        
        //wsdl spec:
        //2.4.5 Names of Elements within an Operation:
        //The name of the fault element is unique within the set of faults defined for the operation
        validateFaultNames(operation);
        
        visitChildren(operation);
        // return true;
    }
    
    
    
    /**
     * Visits a portType operation element.
     * @param operation a portType operation element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(SolicitResponseOperation operation) {
        // if the opertion is an one-way operation or a notification operation,
        // then there should be no faults
        if ((operation.getInput() == null || operation.getOutput() == null)
        && (operation.getFaults() != null && operation.getFaults().size() > 0)) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.WARNING, operation,
                    mMsg.getString(VAL_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()),
                    mMsg.getString(FIX_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()));
        }
        
        //wsdl spec:
        //2.4.5 Names of Elements within an Operation:
        //The name of the fault element is unique within the set of faults defined for the operation
        validateFaultNames(operation);
        
        visitChildren(operation);
        // return true;
    }
    
    /**
     * Visits a portType operation element.
     * @param operation a portType operation element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(OneWayOperation operation) {
        // if the opertion is an one-way operation or a notification operation,
        // then there should be no faults
        if (operation.getFaults() != null && operation.getFaults().size() > 0) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.WARNING, operation,
                    mMsg.getString(VAL_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()),
                    mMsg.getString(FIX_FAULT_NOT_ALLOWED_IN_OPERATION, operation.getName()));
        }
        
        visitChildren(operation);
        //return true;
    }
    
    
    
    /**
     * Visits an operation input element.
     * @param input an operation input element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(Input input) {
        NamedComponentReference<Message> msgRef = input.getMessage();
        Message message = null;
        if(msgRef != null) {
            message = msgRef.get();
            
        }
        
        String messageName = input.getAttribute(new StringAttribute(Input.MESSAGE_PROPERTY));
        
        if (messageName != null && message == null) {
            // throw validation error
            Operation operation = (Operation) input.getParent();
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, input,
                    mMsg.getString(VAL_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT,
                    messageName, operation.getName()),
                    mMsg.getString(FIX_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT,
                    messageName, operation.getName()));
            
        }
        
        visitChildren(input);
        // return true;
    }
    
    /**
     * Visits an operation output element.
     * @param output an operation output element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(Output output) {
        NamedComponentReference<Message> msgRef = output.getMessage();
        Message message = null;
        if(msgRef != null) {
            message = msgRef.get();
            
        }
        
        String messageName = output.getAttribute(new StringAttribute(Output.MESSAGE_PROPERTY));
        
        if (messageName != null && message == null) {
            // throw validation error
            Operation operation = (Operation) output.getParent();
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, output,
                    mMsg.getString(VAL_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT,
                    messageName, operation.getName()),
                    mMsg.getString(FIX_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT,
                    messageName, operation.getName()));
            
        }
        
        visitChildren(output);
        // return true;
    }
    
    /**
     * Visits an operation fault element.
     * @param fault an operation fault element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(Fault fault) {
        
        
        NamedComponentReference<Message> msgRef = fault.getMessage();
        Message message = null;
        if(msgRef != null) {
            message = msgRef.get();
            
        }
        
        String messageName = fault.getAttribute(new StringAttribute(Fault.MESSAGE_PROPERTY));
        
        if (messageName != null && message == null) {
            // throw validation error
            Operation operation = (Operation) fault.getParent();
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, fault,
                    mMsg.getString(VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT,
                    messageName, fault.getName(), operation.getName()),
                    mMsg.getString(FIX_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT,
                    fault.getName(), operation.getName()));
            
        }
        
        visitChildren(fault);
        //return true;
    }
    
    /**
     * Visits a service element.
     * @param service a service element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(Service service) {
        visitChildren(service);
    }
    
    /**
     * Visits a service port element.
     * @param port a service port element
     * @return  <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(Port port) {
        //verify if binding exists
        NamedComponentReference<Binding> bindingRef = port.getBinding();
        Binding binding = null;
        if(bindingRef != null) {
            binding = bindingRef.get();
            
        }
        
        if(binding == null) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, port,
                    mMsg.getString(VAL_MISSING_BINDING_IN_SERVICE_PORT,
                    port.getName(), port.getAttribute(new StringAttribute(Port.BINDING_PROPERTY))),
                    mMsg.getString(FIX_MISSING_BINDING_IN_SERVICE_PORT)
                    );
        }
        
        visitChildren(port);
        // return true;
    }
    
    
    
    /**
     * Visits an import element.
     * @param wsdlImport an import element
     * @return <tt>true</tt> if auto-traversal is to continue.
     */
    public void visit(Import wsdlImport) {
        
        //verify if imported document is available
        List<WSDLModel> importedDocuments = wsdlImport.getModel().findWSDLModel(wsdlImport.getNamespace());
        
        
        if(importedDocuments == null || importedDocuments.isEmpty()) {
            // it can be a xsd import
            Collection xsdImports = wsdlImport.getModel().findSchemas(wsdlImport.getNamespace());
            if (xsdImports == null || xsdImports.size() == 0) {
                getValidateSupport().fireToDo
                        (Validator.ResultType.ERROR, wsdlImport,
                        mMsg.getString(VAL_MISSING_IMPORTED_DOCUMENT,
                        wsdlImport.getNamespace(),
                        wsdlImport.getLocation()),
                        mMsg.getString(FIX_MISSING_IMPORTED_DOCUMENT)
                        );
            }
        }
        visitChildren(wsdlImport);
        
        for (WSDLModel imp : importedDocuments) {
            mValidation.validate(imp, ValidationType.COMPLETE);
        }
        // return true;
    }
    
    /**
     * @see WSDLVisitor#visit(Binding)
     */
    public void visit(Binding binding) {
        NamedComponentReference<PortType> ptRef = binding.getType();
        PortType portType = null;
        if(ptRef != null) {
            
            portType = ptRef.get();
        }
        
        //now make sure portType is available
        String type = binding.getAttribute(new StringAttribute(Binding.TYPE_PROPERTY));
        
        if(type != null && portType == null) {
            getValidateSupport().fireToDo
                    (Validator.ResultType.ERROR, binding,
                    mMsg.getString(VAL_MISSING_PORTTYPE_IN_BINDING,
                    binding.getName(),
                    type),
                    mMsg.getString(FIX_MISSING_PORTTYPE_IN_BINDING)
                    );
        }
        
        visitChildren(binding);
        //return true;
    }
    
    /**
     * @see WSDLVisitor#visit(BindingOperation)
     */
    public void visit(BindingOperation bindingOp) {
        //wsdl spec:
        //section 2.5 Bindings:
        //An operation element within a binding specifies binding information for the
        //operation with the same name within the binding's portType
        
        //validate if binding operation name is same as porttype operation name
        //XMLNode parent = bindingOp.getParent();
        //if(parent instanceof Binding) {
        Binding binding = (Binding) bindingOp.getParent();
        NamedComponentReference<PortType> ptRef = binding.getType();
        PortType portType = null;
        if(ptRef != null) {
            portType = ptRef.get();
        }
        
        String portTypeName = null;
        if(portType != null) {
            portTypeName = portType.getName();
        }
        
        String bindingName = binding.getName();
        String operationName = bindingOp.getName();
        if(portType != null && operationName != null) {
            Collection<Operation> operations = portType.getOperations();
            Reference<Operation> matchingOpRef = bindingOp.getOperation();
            Operation matchingOp = null;
            if(matchingOpRef != null) {
                matchingOp = matchingOpRef.get();
            }
            
            
            //if no matching operation is found then it is an error
            if(matchingOp == null){
                getValidateSupport().fireToDo
                        (Validator.ResultType.ERROR, bindingOp,
                        mMsg.getString(VAL_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE, operationName, binding.getName(), portType.getName()),
                        mMsg.getString(FIX_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE, portType.getName()));
            } else{
                //check if the signatures match
                BindingInput bindingInput = bindingOp.getBindingInput();
                boolean bindingOpHasInput = bindingInput != null;
                Input portTypeInput = matchingOp.getInput();
                boolean portTypeOpHasInput = portTypeInput != null;
                BindingOutput bindingOutput = bindingOp.getBindingOutput();
                boolean bindingOpHasOutput = bindingOutput != null;
                Output portTypeOutput = matchingOp.getOutput();
                boolean portTypeOpHasOutput = portTypeOutput != null;
                Collection<BindingFault> bindingFaults =  bindingOp.getBindingFaults();
                Collection<Fault> matchingFaults = matchingOp.getFaults();
                
                if(bindingOpHasInput != portTypeOpHasInput){
                    //Input in portType operation does not match input in binding operation
                    getValidateSupport().fireToDo
                            (Validator.ResultType.ERROR, bindingOp,
                            mMsg.getString(VAL_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE, operationName, bindingName, portTypeName),
                            mMsg.getString(FIX_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE, operationName));
                }else{
                    if(bindingOpHasInput){
                        if(!inputNamesMatch(bindingInput, portTypeInput, operationName)){
                            getValidateSupport().fireToDo
                                    (Validator.ResultType.WARNING, bindingInput,
                                    mMsg.getString(VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE, operationName, bindingName, portTypeName),
                                    mMsg.getString(FIX_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE, operationName));
                        }
                    }
                }
                
                if(bindingOpHasOutput != portTypeOpHasOutput){
                    //Output in portType operation does not match output in binding operation
                    getValidateSupport().fireToDo
                            (Validator.ResultType.ERROR, bindingOp,
                            mMsg.getString(VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE, operationName, bindingName, portTypeName),
                            mMsg.getString(FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE, operationName));
                }else{
                    if(bindingOpHasOutput){
                        if(!outputNamesMatch(bindingOutput, portTypeOutput, operationName)){
                            getValidateSupport().fireToDo
                                    (Validator.ResultType.WARNING, bindingOutput,
                                    mMsg.getString(VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE, operationName, bindingName, portTypeName),
                                    mMsg.getString(FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE, operationName));
                        }
                    }
                }
                if(!faultsMatch(bindingFaults, matchingFaults)){
                    //Faults do not match
                    getValidateSupport().fireToDo
                            (Validator.ResultType.ERROR, bindingOp,
                            mMsg.getString(VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE, operationName, bindingName, portTypeName),
                            mMsg.getString(FIX_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE, operationName));
                }
            }
        } else {
            //binding operation's parent should be binding
            //taken care by schema validation
        }
        //}
        visitChildren(bindingOp);
        // return true;
    }
    
    private boolean inputNamesMatch(BindingInput bindingInput, Input portTypeInput, String operationName){
        String bindingInputName = bindingInput.getAttribute(WSDLAttribute.NAME);
        String portTypeInputName = portTypeInput.getAttribute(WSDLAttribute.NAME);
        if(bindingInputName != null){
            return bindingInputName.equals(portTypeInputName);
        } else {
        	return portTypeInputName == null;
        }
        
    }
    
    private boolean outputNamesMatch(BindingOutput bindingOutput, Output portTypeOutput, String operationName){
        String bindingOutputName = bindingOutput.getName();
        String portTypeOutputName = portTypeOutput.getAttribute(WSDLAttribute.NAME);
        
        if(bindingOutputName != null){
            return bindingOutputName.equals(portTypeOutput.getName());
        } else {
        	return portTypeOutputName == null;
        }
        
    }
    
    private boolean faultsMatch(Collection<BindingFault> bindingFaults, Collection<Fault> portTypeFaults){
        if(bindingFaults.size() != portTypeFaults.size()) return false;
        if(portTypeFaults.size() == 0) return true;
        
        Set<String> portTypeFaultNames = new HashSet<String>();
        for(Fault portTypeFault : portTypeFaults){
            portTypeFaultNames.add(portTypeFault.getName());
        }
        for(BindingFault bindingFault : bindingFaults){
            if(!portTypeFaultNames.contains(bindingFault.getName()))
                return false;
        }
        return true;
    }
    
    /**
     * @see WSDLVisitor#visit(BindingInput)
     */
    public void visit(BindingInput bindingIn) {
        visitChildren(bindingIn);
    }
    
    /**
     * @see WSDLVisitor#visit(BindingOutput)
     */
    public void visit(BindingOutput bindingOut) {
        visitChildren(bindingOut);
    }
    
    /**
     * @see WSDLVisitor#visit(BindingFault)
     */
    public void visit(BindingFault bindingFault) {
        visitChildren(bindingFault);
    }
    
    /**
     * @see DocumentationVisitor#visit(Documentation)
     */
    public void visit(Documentation doc) {
    }
    
    /**
     * @see WSDLVisitor#visit(Types)
     */
    public void visit(Types types) {
        visitChildren(types);
    }
    
    /**
     * @see WSDLVisitor#visit(ExtensibilityElement)
     */
    public void visit(ExtensibilityElement ext) {
        if (ext instanceof WSDLSchema) {
          /* R2105 All xsd:schema elements contained in a wsdl:types element of a
           * DESCRIPTION MUST have a targetNamespace attribute with a valid and non-null
           * value, UNLESS the xsd:schema element has xsd:import and/or xsd:annotation as
           * its only child element(s).
           */
            SchemaModel model = ((WSDLSchema)ext).getSchemaModel();
            if (model != null && model.getSchema() != null) {
                String targetNamespace = model.getSchema().getTargetNamespace();
                if (targetNamespace == null || targetNamespace.length() == 0) {
                    Schema schema = model.getSchema();
                    Collection<SchemaComponent> allTopLevelElements = schema.getChildren();
                    for (SchemaComponent sc : allTopLevelElements) {
                        if (!(sc instanceof Annotation ||
                                sc instanceof org.netbeans.modules.xml.schema.model.Import)) {
                            getValidateSupport().fireToDo
                                    (Validator.ResultType.ERROR, ext,
                                    mMsg.getString(VAL_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST),
                                    mMsg.getString(FIX_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST));
                            break;
                        }
                    }
                }
                
                SchemaSemanticsVisitor v = new SchemaSemanticsVisitor(mValidator, mValidation, mValidatedModels);
                model.getSchema().accept(v);
                
                List<ResultItem> r =  v.getResultItems();
                if(r != null) {
                    mResultItems.addAll(r);
                }
            }
           
        }
    }
    
    private void visitChildren(WSDLComponent w) {
        Collection coll = w.getChildren();
        if (coll != null) {
            Iterator iter = coll.iterator();
            while (iter.hasNext()) {
                WSDLComponent component = (WSDLComponent) iter.next();
                component.accept(this);
            }
        }
    }
    
    private void validateFaultNames(Operation operation) {
        //wsdl spec:
        //2.4.5 Names of Elements within an Operation:
        //The name of the fault element is unique within the set of faults defined for the operation
        ArrayList<String> faultNames = new ArrayList<String>();
        
        Collection faults = operation.getFaults();
        Iterator it = faults.iterator();
        while(it.hasNext()) {
            Fault fault = (Fault) it.next();
            String faultName = fault.getName();
            if(faultName != null) {
                if(!faultNames.contains(faultName)) {
                    faultNames.add(faultName);
                } else {
                    //found duplicate output name in this portType operations
                    getValidateSupport().fireToDo
                            (Validator.ResultType.ERROR, operation,
                            mMsg.getString(VAL_DUPLICATE_OPRATION_FAULT_NAME, faultName, operation.getName()),
                            mMsg.getString(FIX_DUPLICATE_OPRATION_FAULT_NAME));
                }
            }
        }
    }
    
   
}
