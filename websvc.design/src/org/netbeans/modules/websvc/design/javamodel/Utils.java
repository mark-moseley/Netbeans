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

package org.netbeans.modules.websvc.design.javamodel;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.jws.WebParam.Mode;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.design.util.SourceUtils;
import org.openide.ErrorManager;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class Utils {
    
    public static boolean isEqualTo(String str1, String str2) {
        if (str1==null) return str2==null;
        else return str1.equals(str2);
    }
    
    public static void populateModel(final FileObject implClass, final ServiceModel serviceModel) {
        
        JavaSource javaSource = JavaSource.forFileObject(implClass);
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                //CompilationUnitTree cut = controller.getCompilationUnit();
                
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                if (srcUtils!=null) {
                    //ClassTree javaClass = srcUtils.getClassTree();
                    // find if class is Injection Target
                    TypeElement classEl = srcUtils.getTypeElement();
                    TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                    if (wsElement!=null) {
                        List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
                        AnnotationMirror webServiceAn=null;
                        for (AnnotationMirror anMirror : annotations) {
                            if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                                webServiceAn = anMirror;
                                break;
                            }
                        }
                        if (webServiceAn==null) {
                            serviceModel.status = ServiceModel.STATUS_NOT_SERVICE;
                            return;
                        }
                        
                        Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = webServiceAn.getElementValues();
                        boolean nameFound=false;
                        boolean serviceNameFound=false;
                        boolean portNameFound=false;
                        boolean tnsFound = false;
                        for(ExecutableElement ex:expressions.keySet()) {
                            if (ex.getSimpleName().contentEquals("serviceName")) { //NOI18N
                                serviceModel.serviceName = (String)expressions.get(ex).getValue();
                                serviceNameFound=true;
                            } else if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                                serviceModel.name = (String)expressions.get(ex).getValue();
                                nameFound=true;
                            } else if (ex.getSimpleName().contentEquals("portName")) { //NOI18N
                                serviceModel.portName = (String)expressions.get(ex).getValue();
                                portNameFound=true;
                            } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                                serviceModel.targetNamespace = (String)expressions.get(ex).getValue();
                                tnsFound = true;
                            } else if (ex.getSimpleName().contentEquals("endpointInterface")) { //NOI18N
                                serviceModel.endpointInterface = (String)expressions.get(ex).getValue();
                            } else if (ex.getSimpleName().contentEquals("wsdlLocation")) { //NOI18N
                                serviceModel.wsdlLocation = (String)expressions.get(ex).getValue();
                            }
                        }
                        // set default names
                        if (!nameFound) serviceModel.name=implClass.getName();
                        if (!portNameFound) serviceModel.portName = serviceModel.getName()+"Port"; //NOI18N
                        if (!serviceNameFound) serviceModel.serviceName = implClass.getName()+"Service"; //NOI18N
                        if (!tnsFound) {
                            String qualifName = classEl.getQualifiedName().toString();
                            int ind = qualifName.lastIndexOf(".");                           
                            serviceModel.targetNamespace = "http://"+(ind>=0?qualifName.substring(0,ind):"")+"/";
                        }
                        
                        //TODO: Also have to apply JAXWS/JAXB rules regarding collision of names
                    }
                    
                    boolean foundWebMethodAnnotation=false;
                    TypeElement methodAnotationEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                    List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
                    for (Element member : classEl.getEnclosedElements()) {
                        if (member.getKind() == ElementKind.METHOD/* && member.getSimpleName().contentEquals("min")*/) {
                            ExecutableElement methodEl = (ExecutableElement) member;
                            if (methodEl.getModifiers().contains(Modifier.PUBLIC)) {
                                List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
                                if (foundWebMethodAnnotation) {
                                    for (AnnotationMirror anMirror : methodAnnotations) {
                                        if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                            methods.add(methodEl);
                                            break;
                                        }
                                    }
                                } else { // until now no @WebMethod annotations
                                    for (AnnotationMirror anMirror : methodAnnotations) {
                                        if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                            // found first @WebMethod annotation
                                            // need to remove all, previously found, public methods
                                            methods.clear();
                                            foundWebMethodAnnotation=true;
                                            methods.add(methodEl);
                                            break;
                                        }
                                    }
                                    if (!foundWebMethodAnnotation) {
                                        // all methods are supposed to be web methods when missing @WebMethod annotation
                                        methods.add(methodEl);
                                    }
                                }
                            }
                        }
                    }
                    // populate methods
                    
                    List<MethodModel> operations = new ArrayList<MethodModel>();
                    if (methods.size()==0) {
                        serviceModel.operations=operations;
                        serviceModel.status = ServiceModel.STATUS_INCORRECT_SERVICE;
                        return;
                    }
                            
                    for (int i=0;i<methods.size();i++) {
                        MethodModel operation = new MethodModel();
                        operation.setImplementationClass(implClass);
                        Utils.populateOperation(controller, methods.get(i), operation, serviceModel.getTargetNamespace());
                        operations.add(operation);
                    }
                    serviceModel.operations=operations;
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
    
    private static void populateOperation(CompilationController controller, ExecutableElement methodEl, MethodModel methodModel, String targetNamespace) {
        TypeElement methodAnotationEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        TypeElement onewayAnotationEl = controller.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
        TypeElement resultAnotationEl = controller.getElements().getTypeElement("javax.jws.WebResult"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
        
        ResultModel resultModel = new ResultModel();
        
        boolean nameFound=false;
        boolean resultNameFound=false;
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("operationName")) { //NOI18N
                        methodModel.setOperationName((String)expressions.get(ex).getValue());
                        nameFound=true;
                    } else if (ex.getSimpleName().contentEquals("action")) { //NOI18N
                        methodModel.setAction((String)expressions.get(ex).getValue());
                    }
                }
                
            } else if (controller.getTypes().isSameType(resultAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                        resultModel.setName((String)expressions.get(ex).getValue());
                        resultNameFound=true;
                    } else if (ex.getSimpleName().contentEquals("partName")) { //NOI18N
                        resultModel.setPartName((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                        resultModel.setTargetNamespace((String)expressions.get(ex).getValue());
                    }
                }
            } else if (controller.getTypes().isSameType(onewayAnotationEl.asType(), anMirror.getAnnotationType())) {
                methodModel.setOneWay(true);
            }
        }
        if (!nameFound) methodModel.setOperationName(methodEl.getSimpleName().toString());
        
        
        // Return type
        if (!methodModel.isOneWay()) {
            // set default result name
            if (!resultNameFound) resultModel.setName("return"); //NOI18N
            // set result type
            TypeMirror returnType = methodEl.getReturnType();
            if (returnType.getKind() == TypeKind.DECLARED) {
                TypeElement element = (TypeElement)((DeclaredType)returnType).asElement();
                resultModel.setResultType(element.getQualifiedName().toString());
            } else { // for primitive types
                resultModel.setResultType(returnType.toString());
            }
        }
        methodModel.setResult(resultModel);
        
        
        // populate faults
        List<? extends TypeMirror> faultTypes = methodEl.getThrownTypes();
        List<FaultModel> faults = new ArrayList<FaultModel>();
        for (TypeMirror faultType:faultTypes) {
            FaultModel faultModel = new FaultModel();
            boolean faultFound=false;
            if (faultType.getKind() == TypeKind.DECLARED) {
                TypeElement faultEl = (TypeElement)((DeclaredType)faultType).asElement();
                TypeElement faultAnotationEl = controller.getElements().getTypeElement("javax.xml.ws.WebFault"); //NOI18N
                List<? extends AnnotationMirror> faultAnnotations = faultEl.getAnnotationMirrors();
                for (AnnotationMirror anMirror : faultAnnotations) {
                    if (controller.getTypes().isSameType(faultAnotationEl.asType(), anMirror.getAnnotationType())) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                        for(ExecutableElement ex:expressions.keySet()) {
                            if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                                faultModel.setName((String)expressions.get(ex).getValue());
                                faultFound=true;
                            } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                                faultModel.setTargetNamespace((String)expressions.get(ex).getValue());
                            }
                        }
                    }
                }
                faultModel.setFaultType(faultEl.getQualifiedName().toString());
            } else {
                faultModel.setFaultType(faultType.toString());
            }
            if (!faultFound) {
                String fullyQualifiedName = faultModel.getFaultType();
                int index = fullyQualifiedName.lastIndexOf("."); //NOI18N
                faultModel.setName(index>=0?fullyQualifiedName.substring(index+1):fullyQualifiedName);
            }
            faults.add(faultModel);
        }
        methodModel.setFaults(faults);
        
        // populate javadoc
        Doc javadoc = controller.getElementUtilities().javaDocFor(methodEl);
        if (javadoc!=null) {
            //methodModel.setJavadoc(javadoc.getRawCommentText());
            JavadocModel javadocModel = new JavadocModel(javadoc.getRawCommentText());
            // @param part
            Tag[] paramTags = javadoc.tags("@param"); //NOI18N
            List<String> paramJavadoc = new ArrayList<String>();
            for (Tag paramTag:paramTags) {
                paramJavadoc.add(paramTag.text());
            }
            javadocModel.setParamJavadoc(paramJavadoc);
            
            // @return part
            Tag[] returnTags = javadoc.tags("@return"); //NOI18N
            if (returnTags.length>0) {
                javadocModel.setReturnJavadoc(returnTags[0].text());
            }
            // @throws part
            Tag[] throwsTags = javadoc.tags("@throws"); //NOI18N
            List<String> throwsJavadoc = new ArrayList<String>();
            for (Tag throwsTag:throwsTags) {
                throwsJavadoc.add(throwsTag.text());
            }
            javadocModel.setThrowsJavadoc(throwsJavadoc);
            
            // rest part
            Tag[] inlineTags = javadoc.inlineTags(); //NOI18N
            List<String> inlineJavadoc = new ArrayList<String>();
            for (Tag inlineTag:inlineTags) {
                throwsJavadoc.add(inlineTag.text());
            }
            javadocModel.setInlineJavadoc(inlineJavadoc);
            methodModel.setJavadoc(javadocModel);
        }
        
        
        // populate params
        List<? extends VariableElement> paramElements = methodEl.getParameters();
        List<ParamModel> params = new ArrayList<ParamModel>();
        for (int i=0;i<paramElements.size();i++) {
            VariableElement paramEl = paramElements.get(i);
            ParamModel param = new ParamModel("arg"+String.valueOf(i));
            populateParam(controller, paramEl, param);
            params.add(param);
        }
        methodModel.setParams(params);
        
        // set SOAP Request
        setSoapRequest(methodModel, targetNamespace);
        
        // set SOAP Response
        setSoapResponse(methodModel, targetNamespace);
    }
    
    private static void populateParam(CompilationController controller, VariableElement paramEl, ParamModel paramModel) {
        TypeMirror type = paramEl.asType();
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement)((DeclaredType)type).asElement();
            paramModel.setParamType(element.getQualifiedName().toString());
        } else { // for primitive type
            paramModel.setParamType(type.toString());
        }
        TypeElement paramAnotationEl = controller.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = paramEl.getAnnotationMirrors();
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (controller.getTypes().isSameType(paramAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                        paramModel.setName((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("partName")) { //NOI18N
                        paramModel.setPartName((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                        paramModel.setTargetNamespace((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("mode")) { //NOI18N
                        paramModel.setMode((Mode)expressions.get(ex).getValue());
                    }
                }
            }
        }
    }
    
    private static void setSoapRequest(MethodModel methodModel, String tns) {

        try {
            // create a sample SOAP request using SAAJ API
            MessageFactory messageFactory = MessageFactory.newInstance();

            SOAPMessage request = messageFactory.createMessage();
            MimeHeaders headers = request.getMimeHeaders();
            String action = methodModel.getAction();
            headers.addHeader("SOAPAction", action==null? "\"\"":action); //NOI18N
            SOAPPart part = request.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            String prefix = envelope.getPrefix();
            if (!"soap".equals(prefix)) { //NOI18N
                envelope.removeAttribute("xmlns:"+prefix); // NOI18N
                envelope.setPrefix("soap"); //NOI18N
            }
            SOAPBody body = envelope.getBody();
            body.setPrefix("soap"); //NOI18N
            
            // removing soap header
            SOAPHeader header = envelope.getHeader();
            envelope.removeChild(header);

            // implementing body
            Name methodName = envelope.createName(methodModel.getOperationName());
            SOAPElement methodElement = body.addBodyElement(methodName);
            methodElement.setPrefix("ns0"); //NOI18N
            methodElement.addNamespaceDeclaration("ns0",tns); //NOI18N
            
            // params
            List<ParamModel> params = methodModel.getParams();
            int i=0;
            for (ParamModel param:params) {
                String paramNs = param.getTargetNamespace();
                Name paramName = null;
                if (paramNs!=null) {                   
                    String pref = "ns"+String.valueOf(++i); //NOI18N
                    paramName = envelope.createName(param.getName(), pref, paramNs);
                    methodElement.addNamespaceDeclaration(pref,paramNs);
                } else {
                    paramName = envelope.createName(param.getName());
                }
                
                SOAPElement paramElement = methodElement.addChildElement(paramName);
                paramElement.addTextNode(getSampleValue(param.getParamType()));
            }
            
            methodModel.setSoapRequest(request);
            
        } catch (SOAPException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static void setSoapResponse(MethodModel methodModel, String tns) {
        if (methodModel.isOneWay()) return;
        
        try {
            // create a sample SOAP request using SAAJ API
            MessageFactory messageFactory = MessageFactory.newInstance();

            SOAPMessage response = messageFactory.createMessage();
            SOAPPart part = response.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            String prefix = envelope.getPrefix();
            if (!"soap".equals(prefix)) { //NOI18N
                envelope.removeAttribute("xmlns:"+prefix); // NOI18N
                envelope.setPrefix("soap"); //NOI18N
            }
            SOAPBody body = envelope.getBody();
            body.setPrefix("soap"); //NOI18N
            
            // removing soap header
            SOAPHeader header = envelope.getHeader();
            envelope.removeChild(header);

            // implementing body
            Name responseName = envelope.createName(methodModel.getOperationName()+"Response"); //NOI18N
            SOAPElement responseElement = body.addBodyElement(responseName);
            responseElement.setPrefix("ns0"); //NOI18N
            responseElement.addNamespaceDeclaration("ns0",tns); //NOI18N
            
            // return
            
            ResultModel resultModel = methodModel.getResult();
            String resultNs = resultModel.getTargetNamespace();
            
            Name resultName = null;
            if (resultNs!=null) {
                responseElement.addNamespaceDeclaration("ns1",resultNs); //NOI18N
                resultName = envelope.createName(resultModel.getName(), "ns1", resultNs); //NOI18N
            } else {
                resultName = envelope.createName(resultModel.getName()); //NOI18N
            }
            
            SOAPElement resultElement = responseElement.addChildElement(resultName);
            resultElement.addTextNode(getSampleValue(resultModel.getResultType()));

            methodModel.setSoapResponse(response);
            
        } catch (SOAPException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static String getSampleValue(String paramType) {
        if ("java.lang.String".equals(paramType)) {
            return "sample text";
        } else if ("int".equals(paramType)) {
            return "99";
        } else if ("double".equals(paramType)) {
            return "999.999";
        } else if ("float".equals(paramType)) {
            return "99.99";
        } else if ("long".equals(paramType)) {
            return "999";
        } else if ("long".equals(paramType)) {
            return "999";
        } else if ("boolean".equals(paramType)) {
            return "false";
        } else if ("byte[]".equals(paramType)) {
            return "";
        } else return "...";
    }
    
    public static void setJavadoc(final FileObject implClass, final MethodModel methodModel, final String text) {
        JavaSource javaSource = JavaSource.forFileObject(implClass);
        CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);            
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = SourceUtils.findPublicTopLevelClass(workingCopy);
                List<? extends Tree> members = classTree.getMembers();
                TypeElement methodAnotationEl = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                if (methodAnotationEl==null) return;
                MethodTree targetMethod=null;
                for (Tree member:members) {
                    if (Tree.Kind.METHOD==member.getKind()) {
                        MethodTree method = (MethodTree)member;
                        TreePath methodPath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), method);
                        ExecutableElement methodEl = (ExecutableElement)workingCopy.getTrees().getElement(methodPath);
                        // browse annotations to find target method
                        List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
                        for (AnnotationMirror anMirror : methodAnnotations) {
                            if (workingCopy.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                                for(ExecutableElement ex:expressions.keySet()) {
                                    if (ex.getSimpleName().contentEquals("operationName")) { //NOI18N
                                        if (methodModel.getOperationName().equals(expressions.get(ex).getValue())) {
                                            targetMethod = method;    
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        // if annotation not found check method name
                        if (targetMethod!=null) break;
                        else if (method.getName().contentEquals(methodModel.getOperationName())) {
                            targetMethod = method;
                            break;
                        }
                    }
                    
                }
                if (targetMethod!=null) {
                    Comment comment = Comment.create(Style.JAVADOC, 0,0,0, text);                   
                    // Issue in Retouche (90302) : the following part couldn't be used for now
                    // MethodTree newMethod = make.addComment(targetMethod, comment , true);
                    // workingCopy.rewrite(targetMethod, newMethod);
                }
                
             }
             public void cancel() {}
            
        };
        try {
            javaSource.runModificationTask(modificationTask).commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public static  String getFormatedDocument(SOAPMessage message) {
        try {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", new Integer(4));
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        
        StreamResult result = new StreamResult(new StringWriter());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        message.writeTo(bos);
        String output = bos.toString();
        InputStream bis = new ByteArrayInputStream(output.getBytes());
        StreamSource source = new StreamSource(bis);
        
        transformer.transform(source, result);
        
        return result.getWriter().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
