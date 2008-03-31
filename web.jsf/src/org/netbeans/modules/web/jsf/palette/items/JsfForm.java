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

package org.netbeans.modules.web.jsf.palette.items;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Buzek
 * @author Po-Ting Wu
 * @author mbohm
 */
public final class JsfForm implements ActiveEditorDrop {
        
//                    columnClasses="list-column-left, list-column-left,
//                    list-column-right, list-column-center"
//                    rowClasses="list-row-even, list-row-odd"
    
    public static final int FORM_TYPE_EMPTY = 0;
    public static final int FORM_TYPE_DETAIL = 1;
    public static final int FORM_TYPE_NEW = 2;
    public static final int FORM_TYPE_EDIT = 3;
    
    private static String [] BEGIN = {
        "<h:form>\n",
        "<h2>Detail</h2>\n <h:form>\n<h:panelGrid columns=\"2\">\n",
        "<h2>Create</h2>\n <h:form>\n<h:panelGrid columns=\"2\">\n",
        "<h2>Edit</h2>\n <h:form>\n<h:panelGrid columns=\"2\">\n",
    };
    private static String [] END = {
        "</h:form>\n",
        "</h:panelGrid>\n </h:form>\n",
        "</h:panelGrid>\n </h:form>\n",
        "</h:panelGrid>\n </h:form>\n",
    };
    
    private String variable = "";
    private String bean = "";
    private int formType = 0;
    
    public JsfForm() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        
        JsfFormCustomizer jsfFormCustomizer = new JsfFormCustomizer(this, targetComponent);
        boolean accept = jsfFormCustomizer.showDialog();
        if (accept) {
            try {
                Caret caret = targetComponent.getCaret();
                int position0 = Math.min(caret.getDot(), caret.getMark());
                int position1 = Math.max(caret.getDot(), caret.getMark());
                int len = targetComponent.getDocument().getLength() - position1;
                boolean containsFView = targetComponent.getText(0, position0).contains("<f:view>")
                    && targetComponent.getText(position1, len).contains("</f:view>");
                String body = createBody(targetComponent, !containsFView);
                JSFPaletteUtilities.insert(body, targetComponent);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                accept = false;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                accept = false;
            }
        }
        
        return accept;
    }
    
    private String createBody(JTextComponent target, boolean surroundWithFView) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append("<f:view>\n");
        }
        stringBuffer.append(MessageFormat.format(BEGIN [formType], new Object [] {variable}));

        FileObject targetJspFO = getFO(target);
        JavaSource javaSource = JavaSource.create(createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(bean);
                createForm(controller, typeElement, formType, variable, stringBuffer);
            }
        }, true);

        stringBuffer.append(END [formType]);
        if (surroundWithFView) {
            stringBuffer.append("</f:view>\n");
        }
        return stringBuffer.toString();
    }
    
    public static final int REL_NONE = 0;
    public static final int REL_TO_ONE = 1;
    public static final int REL_TO_MANY = 2;
    
    public static int isRelationship(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            if (isAnnotatedWith(element, "javax.persistence.OneToOne") || isAnnotatedWith(element, "javax.persistence.ManyToOne")) {
                return REL_TO_ONE;
            }
            if (isAnnotatedWith(element, "javax.persistence.OneToMany") || isAnnotatedWith(element, "javax.persistence.ManyToMany")) {
                return REL_TO_MANY;
            }
        }
        return REL_NONE;
    }
    
    public static ExecutableElement getOtherSideOfRelation(CompilationController controller, ExecutableElement executableElement, boolean isFieldAccess) {
        TypeMirror passedReturnType = executableElement.getReturnType();
        if (TypeKind.DECLARED != passedReturnType.getKind() || !(passedReturnType instanceof DeclaredType)) {
            return null;
        }
        Types types = controller.getTypes();
        TypeMirror passedReturnTypeStripped = stripCollection((DeclaredType)passedReturnType, types);
        if (passedReturnTypeStripped == null) {
            return null;
        }
        TypeElement passedReturnTypeStrippedElement = (TypeElement) types.asElement(passedReturnTypeStripped);
        
        //try to find a mappedBy annotation element on the possiblyAnnotatedElement
        Element possiblyAnnotatedElement = isFieldAccess ? guessField(controller, executableElement) : executableElement;
        String mappedBy = null;
        AnnotationMirror persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.OneToOne");  //NOI18N"
        if (persistenceAnnotation == null) {
            persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.OneToMany");  //NOI18N"
        }
        if (persistenceAnnotation == null) {
            persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.ManyToOne");  //NOI18N"
        }
        if (persistenceAnnotation == null) {
            persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.ManyToMany");  //NOI18N"
        }
        if (persistenceAnnotation != null) {
            mappedBy = findAnnotationValueAsString(persistenceAnnotation, "mappedBy");  //NOI18N
        }
        for (ExecutableElement method : getEntityMethods(passedReturnTypeStrippedElement)) {
            if (mappedBy != null && mappedBy.length() > 0) {
                String tail = mappedBy.length() > 1 ? mappedBy.substring(1) : "";
                String getterName = "get" + mappedBy.substring(0,1).toUpperCase() + tail;
                if (getterName.equals(method.getSimpleName().toString())) {
                    return method;
                }
            }
            else {
                TypeMirror iteratedReturnType = method.getReturnType();
                iteratedReturnType = stripCollection(iteratedReturnType, types);
                TypeMirror executableElementEnclosingType = executableElement.getEnclosingElement().asType();
                if (types.isSameType(executableElementEnclosingType, iteratedReturnType)) {
                    return method;
                }
            }
        }
        return null;
    }
    
    public static String findAnnotationValueAsString(AnnotationMirror annotation, String annotationKey) {
        String value = null;
        Map<? extends ExecutableElement,? extends AnnotationValue> annotationMap = annotation.getElementValues();
        for (ExecutableElement key : annotationMap.keySet()) {
            if (annotationKey.equals(key.getSimpleName().toString())) {
                AnnotationValue annotationValue = annotationMap.get(key);
                value = annotationValue.getValue().toString();
                break;
            }
        }
        return value;
    }
    
    public static TypeMirror stripCollection(TypeMirror passedType, Types types) {
        if (TypeKind.DECLARED != passedType.getKind() || !(passedType instanceof DeclaredType)) {
            return passedType;
        }
        TypeElement passedTypeElement = (TypeElement) types.asElement(passedType);
        String passedTypeQualifiedName = passedTypeElement.getQualifiedName().toString();   //does not include type parameter info
        Class passedTypeClass = null;
        try {
            passedTypeClass = Class.forName(passedTypeQualifiedName);
        } catch (ClassNotFoundException e) {
            //just let passedTypeClass be null
        }
        if (passedTypeClass != null && Collection.class.isAssignableFrom(passedTypeClass)) {
            List<? extends TypeMirror> passedTypeArgs = ((DeclaredType)passedType).getTypeArguments();
            if (passedTypeArgs.size() == 0) {
                return passedType;
            }
            return passedTypeArgs.get(0);
        }
        return passedType;
    }
    
    /** Returns all methods in class and its super classes which are entity
     * classes or mapped superclasses.
     */
    public static ExecutableElement[] getEntityMethods(TypeElement entityTypeElement) {
        List<ExecutableElement> result = new LinkedList<ExecutableElement>();
        TypeElement typeElement = entityTypeElement;
        while (typeElement != null) {
            if (isAnnotatedWith(typeElement, "javax.persistence.Entity") || isAnnotatedWith(typeElement, "javax.persistence.MappedSuperclass")) { // NOI18N
                result.addAll(ElementFilter.methodsIn(typeElement.getEnclosedElements()));
            }
            Element enclosingElement = typeElement.getEnclosingElement();
            if (ElementKind.CLASS == enclosingElement.getKind()) {
                typeElement = (TypeElement) enclosingElement;
            } else {
                typeElement = null;
            }
        }
        return result.toArray(new ExecutableElement[result.size()]);
    }
    
    static boolean isId(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    public static boolean isGenerated(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            if (isAnnotatedWith(element, "javax.persistence.GeneratedValue")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    static String getTemporal(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            AnnotationMirror annotationMirror = findAnnotation(element, "javax.persistence.Temporal"); // NOI18N
            if (annotationMirror != null) {
                Collection<? extends AnnotationValue> attributes = annotationMirror.getElementValues().values();
                if (attributes.iterator().hasNext()) {
                    AnnotationValue annotationValue = attributes.iterator().next();
                    if (annotationValue != null) {
                        return annotationValue.getValue().toString();
                    }
                }
            }
        }
        return null;
    }

    static FileObject getFO(JTextComponent target) {
        Document doc = target.getDocument();
        if (doc != null) {
            return NbEditorUtilities.getFileObject(doc);
        }
        return null;
    }
    
    static ClasspathInfo createClasspathInfo(FileObject fileObject) {
        return ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT),
                ClassPath.getClassPath(fileObject, ClassPath.COMPILE),
                ClassPath.getClassPath(fileObject, ClassPath.SOURCE)
                );
    }
    
    static boolean hasModuleJsf(JTextComponent target) {
        FileObject fileObject = getFO(target);
        if (fileObject != null) {
            WebModule webModule = WebModule.getWebModule(fileObject);
            String[] configFiles = JSFConfigUtilities.getConfigFiles(webModule);
            return configFiles != null && configFiles.length > 0;
        }
        return false;
    }
    
    public static boolean isEntityClass(TypeElement typeElement) {
        if (isAnnotatedWith(typeElement, "javax.persistence.Entity")) {
            return true;
        }
        return false;
    }
    
    public static boolean isEmbeddableClass(TypeElement typeElement) {
        if (isAnnotatedWith(typeElement, "javax.persistence.Embeddable")) {
            return true;
        }
        return false;
    }
    
    public static boolean isFieldAccess(TypeElement clazz) {
        boolean fieldAccess = false;
        boolean accessTypeDetected = false;
        TypeElement typeElement = clazz;
//        while (typeElement != null) {
        if (typeElement != null) {
            for (Element element : typeElement.getEnclosedElements()) {
                if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) {
                    if (ElementKind.FIELD == element.getKind()) {
                        fieldAccess = true;
                    }
                    accessTypeDetected = true;
                }
            }
            if (!accessTypeDetected) {
                Logger.getLogger("global").log(Level.WARNING, "Failed to detect correct access type for class:" + typeElement.getQualifiedName()); // NOI18N
            }
        }
//            typeElement = (TypeElement) typeElement.getEnclosingElement();
//        }
        return fieldAccess;
    }

    public static VariableElement guessField(CompilationController controller, ExecutableElement getter) {
        String name = getter.getSimpleName().toString().substring(3);
        String guessFieldName = name.substring(0,1).toLowerCase() + name.substring(1);
        TypeElement typeElement = (TypeElement) getter.getEnclosingElement();
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (variableElement.getSimpleName().contentEquals(guessFieldName)) {
                return variableElement;
            }
        }
        Logger.getLogger("global").log(Level.WARNING, "Cannot detect the field associated with property: " + guessFieldName);
        return null;
    }

    /** Check if there is a setter corresponding with the getter */
    public static boolean isReadOnly(Types types, ExecutableElement getter) {
        String setterName = "set" + getter.getSimpleName().toString().substring(3); //NOI18N
        TypeMirror propertyType = getter.getReturnType();
        TypeElement enclosingClass = (TypeElement) getter.getEnclosingElement();
        for (ExecutableElement executableElement : ElementFilter.methodsIn(enclosingClass.getEnclosedElements())) {
            if (executableElement.getSimpleName().contentEquals(setterName)) {
                if (executableElement.getParameters().size() == 1) {
                    VariableElement firstParam = executableElement.getParameters().get(0);
                    if (types.isSameType(firstParam.asType(), propertyType)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static String getDateTimeFormat(String temporal) {
        if ("DATE".equals(temporal)) {
            return "MM/dd/yyyy";
        } else if ("TIME".equals(temporal)) {
            return "HH:mm:ss";
        } else {
            return "MM/dd/yyyy HH:mm:ss";
        }
    }
    
    public static void createForm(CompilationController controller, TypeElement bean, int formType, String variable, StringBuffer stringBuffer) {
        createForm(controller, bean, formType, variable, stringBuffer, "", null, "");
    }
    
    public static void createForm(CompilationController controller, TypeElement bean, int formType, String variable, StringBuffer stringBuffer, String entityClass, JSFClientGenerator.EmbeddedPkSupport embeddedPkSupport, String controllerClass) {
        ExecutableElement methods [] = getEntityMethods(bean);
        boolean fieldAccess = isFieldAccess(bean);
        for (ExecutableElement method : methods) {
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith("get")) {
                List<ExecutableElement> pkMethods = null;
                boolean isId = isId(controller, method, fieldAccess);
                boolean isGenerated = false;
                if (isId) {
                    isGenerated = isGenerated(controller, method, fieldAccess);
                    TypeMirror t = method.getReturnType();
                    TypeMirror tstripped = stripCollection(t, controller.getTypes());
                    if (TypeKind.DECLARED == tstripped.getKind()) {
                        DeclaredType declaredType = (DeclaredType)tstripped;
                        TypeElement idClass = (TypeElement) declaredType.asElement();
                        boolean embeddable = idClass != null && isEmbeddableClass(idClass);
                        if (embeddable) {
                            pkMethods = new ArrayList<ExecutableElement>();
                            TypeElement entityType = controller.getElements().getTypeElement(entityClass);
                            for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(controller, entityType)) {
                                if (!embeddedPkSupport.isRedundantWithRelationshipField(controller, entityType, pkMethod)) {
                                    pkMethods.add(pkMethod);
                                }
                            }
                        }
                    }
                }
                if (pkMethods == null) {
                    createFormInternal(method, isId, isGenerated, fieldAccess, controller, formType, variable, stringBuffer, entityClass, controllerClass);
                }
                else {
                    for (ExecutableElement pkMethod : pkMethods) {
                        String propName = JSFClientGenerator.getPropNameFromMethod(methodName);
                        createFormInternal(pkMethod, isId, isGenerated, fieldAccess, controller, formType, variable + "." + propName, stringBuffer, entityClass, controllerClass);
                    }
                }
            }
        }
    }
    
    private static void createFormInternal(ExecutableElement method, boolean isId, boolean isGenerated, boolean fieldAccess, CompilationController controller, int formType, String variable, StringBuffer stringBuffer, String entityClass, String controllerClass) {
        String simpleEntityName = JSFClientGenerator.simpleClassName(entityClass);
        TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType();
        String methodName = method.getSimpleName().toString();
        int isRelationship = isRelationship(controller, method, fieldAccess);
        String name = methodName.substring(3);
        String propName = JSFClientGenerator.getPropNameFromMethod(methodName);
        TypeMirror t = method.getReturnType();
        TypeMirror tstripped = stripCollection(t, controller.getTypes());
        String relType = tstripped.toString();
        if (relType.endsWith("[]")) {
            relType = relType.substring(0, relType.length() - 2);
        }
        String simpleRelType = JSFClientGenerator.simpleClassName(relType); //just "Pavilion"
        String relatedController = JSFClientGenerator.fieldFromClassName(simpleRelType);
        boolean fieldOptionalAndNullable = isFieldOptionalAndNullable(controller, method, fieldAccess);
        String requiredMessage = fieldOptionalAndNullable ? null : "The " + propName + " field is required.";

        if ( (formType == FORM_TYPE_NEW && 
                ( isId &&  isGenerated) ) || 
                formType == FORM_TYPE_EMPTY ) {
            //skip if formType is new and field is generated (or if formType is "empty")
        } else if (formType == FORM_TYPE_DETAIL && isRelationship == REL_TO_ONE) {
            String template = "<h:outputText value=\"{0}:\"/>\n" +
                "<h:panelGroup>\n" + 
                "<h:outputText value=\" #'{'{1}.{2}'}'\"/>\n" +
                "<h:panelGroup rendered=\"#'{'{1}.{2} != null'}'\">\n" +
                "<h:outputText value=\" (\"/>\n" +
                "<h:commandLink value=\"Show\" action=\"#'{'{4}.detailSetup'}'\">\n" +
                "<f:param name=\"jsfcrud.current{3}\" value=\"#'{'{6}.asString[{1}]'}'\"/>\n" +
                "<f:param name=\"jsfcrud.current{5}\" value=\"#'{'{4}.asString[{1}.{2}]'}'\"/>\n" +
                "<f:param name=\"jsfcrud.relatedController\" value=\"{6}\"/>\n" +
                "<f:param name=\"jsfcrud.relatedControllerType\" value=\"{7}\"/>\n" +
                "</h:commandLink>\n" +
                "<h:outputText value=\" \"/>\n" +
                "<h:commandLink value=\"Edit\" action=\"#'{'{4}.editSetup'}'\">\n" +
                "<f:param name=\"jsfcrud.current{3}\" value=\"#'{'{6}.asString[{1}]'}'\"/>\n" +
                "<f:param name=\"jsfcrud.current{5}\" value=\"#'{'{4}.asString[{1}.{2}]'}'\"/>\n" +
                "<f:param name=\"jsfcrud.relatedController\" value=\"{6}\"/>\n" +
                "<f:param name=\"jsfcrud.relatedControllerType\" value=\"{7}\"/>\n" +
                "</h:commandLink>\n" +
                "<h:outputText value=\" \"/>\n" +
                "<h:commandLink value=\"Destroy\" action=\"#'{'{4}.destroy'}'\">\n" +
                "<f:param name=\"jsfcrud.current{3}\" value=\"#'{'{6}.asString[{1}]'}'\"/>\n" +
                "<f:param name=\"jsfcrud.current{5}\" value=\"#'{'{4}.asString[{1}.{2}]'}'\"/>\n" +
                "<f:param name=\"jsfcrud.relatedController\" value=\"{6}\"/>\n" +
                "<f:param name=\"jsfcrud.relatedControllerType\" value=\"{7}\"/>\n" +
                "</h:commandLink>\n" +
                "<h:outputText value=\" )\"/>\n" +
                "</h:panelGroup>\n" +
                "</h:panelGroup>\n";
            Object[] args = new Object [] {name, variable, propName, simpleEntityName, relatedController, simpleRelType, variable.substring(0, variable.lastIndexOf('.')), controllerClass};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( (formType == FORM_TYPE_DETAIL && isRelationship == REL_NONE) || 
                ( formType == FORM_TYPE_EDIT && (isId(controller, method, fieldAccess) || isReadOnly(controller.getTypes(), method)) && isRelationship != REL_TO_MANY ) || 
                (formType == FORM_TYPE_NEW && isReadOnly(controller.getTypes(), method) && isRelationship != REL_TO_MANY) ) {
            //non editable
            String temporal = ( isRelationship == REL_NONE && controller.getTypes().isSameType(dateTypeMirror, method.getReturnType()) ) ? getTemporal(controller, method, fieldAccess) : null;
            String template = "<h:outputText value=\"{0}:\"/>\n <h:outputText value=\"" + (isRelationship == REL_NONE ? "" : " ") + "#'{'{1}.{2}'}'\" title=\"{0}\" ";
            template += temporal == null ? "/>\n" : ">\n<f:convertDateTime type=\"{3}\" pattern=\"{4}\" />\n</h:outputText>\n";
            Object[] args = temporal == null ? new Object [] {name, variable, propName} : new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal)};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( isRelationship == REL_NONE && (formType == FORM_TYPE_NEW || formType == FORM_TYPE_EDIT) ) {
            //editable
            String temporal = controller.getTypes().isSameType(dateTypeMirror, method.getReturnType()) ? getTemporal(controller, method, fieldAccess) : null;
            String template = temporal == null ? "<h:outputText value=\"{0}:\"/>\n" : "<h:outputText value=\"{0} ({4}):\"/>\n";
            template += "<h:inputText id=\"{2}\" value=\"#'{'{1}.{2}'}'\" title=\"{0}\" ";
            template += requiredMessage == null ? "" : "required=\"true\" requiredMessage=\"{5}\" ";
            template += temporal == null ? "/>\n" : ">\n<f:convertDateTime type=\"{3}\" pattern=\"{4}\" />\n</h:inputText>\n";
            Object[] args = temporal == null ? new Object [] {name, variable, propName, null, null, requiredMessage} : new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal), requiredMessage};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( isRelationship == REL_TO_ONE && (formType == FORM_TYPE_EDIT || formType == FORM_TYPE_NEW) ) {
            //combo box for editing toOne relationships
            String template = "<h:outputText value=\"{0}:\"/>\n <h:selectOneMenu id=\"{2}\" value=\"#'{'{1}.{2}'}'\" title=\"{0}\" ";
            template += requiredMessage == null ? "" : "required=\"true\" requiredMessage=\"{3}\" ";
            template += ">\n <f:selectItems value=\"#'{'{4}.{4}sAvailableSelectOne'}'\"/>\n </h:selectOneMenu>\n";
            Object[] args = new Object [] {name, variable, propName, requiredMessage, relatedController};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( isRelationship == REL_TO_MANY && (formType == FORM_TYPE_EDIT || formType == FORM_TYPE_NEW) ) {
            //listbox for editing toMany relationships
            String template = "<h:outputText value=\"{0}:\"/>\n <h:selectManyListbox id=\"{2}\" value=\"#'{'{3}.{2}Of{1}'}'\" title=\"{0}\" size=\"6\" ";
            template += requiredMessage == null ? "" : "required=\"true\" requiredMessage=\"{5}\" ";
            template += ">\n <f:selectItems value=\"#'{'{4}.{4}sAvailableSelectMany'}'\"/>\n </h:selectManyListbox>\n";
            Object[] args = new Object [] {name, simpleEntityName, propName, variable.substring(0, variable.lastIndexOf('.')), relatedController, requiredMessage};
            stringBuffer.append(MessageFormat.format(template, args));
        }
    }
    
    public static boolean isFieldOptionalAndNullable(CompilationController controller, ExecutableElement method, boolean fieldAccess) {
        boolean isFieldOptional = true;
        Boolean isFieldNullable = null;
        Element fieldElement = fieldAccess ? guessField(controller, method) : method;
        String[] fieldAnnotationFqns = {"javax.persistence.ManyToOne", "javax.persistence.OneToOne", "javax.persistence.Basic"};
        Boolean isFieldOptionalBoolean = findAnnotationValueAsBoolean(fieldElement, fieldAnnotationFqns, "optional");
        if (isFieldOptionalBoolean != null) {
            isFieldOptional = isFieldOptionalBoolean.booleanValue();
        }
        if (!isFieldOptional) {
            return false;
        }
        //field is optional
        fieldAnnotationFqns = new String[]{"javax.persistence.Column", "javax.persistence.JoinColumn"};
        isFieldNullable = findAnnotationValueAsBoolean(fieldElement, fieldAnnotationFqns, "nullable");
        if (isFieldNullable != null) {
            return isFieldNullable.booleanValue();
        }
        //new ballgame
        boolean result = true;
        AnnotationMirror fieldAnnotation = findAnnotation(fieldElement, "javax.persistence.JoinColumns"); //NOI18N
        if (fieldAnnotation != null) {
            //all joinColumn annotations must indicate nullable = false to return a false result
            List<AnnotationMirror> joinColumnAnnotations = JsfForm.findNestedAnnotations(fieldAnnotation, "javax.persistence.JoinColumn");
            for (AnnotationMirror joinColumnAnnotation : joinColumnAnnotations) {
                String columnNullableValue = JsfForm.findAnnotationValueAsString(joinColumnAnnotation, "nullable"); //NOI18N
                if (columnNullableValue != null) {
                    result = Boolean.parseBoolean(columnNullableValue);
                    if (result) {
                        break;  //one of the joinColumn annotations is nullable, so return true
                    }
                }
                else {
                    result = true;
                    break;  //one of the joinColumn annotations is nullable, so return true
                }
            }
        }
        return result;
    }
    
    private static Boolean findAnnotationValueAsBoolean(Element fieldElement, String[] fieldAnnotationFqns, String annotationKey) {
        Boolean isFieldXable = null;
        for (int i = 0; i < fieldAnnotationFqns.length; i++) {
            String fieldAnnotationFqn = fieldAnnotationFqns[i];
            AnnotationMirror fieldAnnotation = findAnnotation(fieldElement, fieldAnnotationFqn); //NOI18N
            if (fieldAnnotation != null) {  
                String annotationValueString = findAnnotationValueAsString(fieldAnnotation, annotationKey); //NOI18N
                if (annotationValueString != null) {
                    isFieldXable = Boolean.valueOf(annotationValueString);
                }
                else {
                    isFieldXable = Boolean.TRUE;
                }
                break;
            }
        }
        return isFieldXable;
    }
    
    public static void createTablesForRelated(CompilationController controller, TypeElement bean, int formType, String variable, 
            String idProperty, boolean isInjection, StringBuffer stringBuffer, JSFClientGenerator.EmbeddedPkSupport embeddedPkSupport, String controllerClass) {
        ExecutableElement methods [] = getEntityMethods(bean);
        String entityClass = bean.getQualifiedName().toString();
        String simpleClass = bean.getSimpleName().toString();
        String managedBean = JSFClientGenerator.getManagedBeanName(simpleClass);
        boolean fieldAccess = isFieldAccess(bean);
        //generate tables of objects with ToMany relationships
        if (formType == FORM_TYPE_DETAIL) {
            for (ExecutableElement method : methods) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    int isRelationship = isRelationship(controller, method, fieldAccess);
                    String name = methodName.substring(3);
                    String propName = JSFClientGenerator.getPropNameFromMethod(methodName);
                    if (isRelationship == REL_TO_MANY) {
                        Types types = controller.getTypes();                        
                        TypeMirror typeArgMirror = stripCollection(method.getReturnType(), types);
                        TypeElement typeElement = (TypeElement)types.asElement(typeArgMirror);
                        if (typeElement != null) {
                            String relatedClass = typeElement.getSimpleName().toString();
                            String relatedManagedBean = JSFClientGenerator.getManagedBeanName(relatedClass);
                            stringBuffer.append("<h:outputText value=\"" + name + ":\" />\n");
                            stringBuffer.append("<h:panelGroup>\n");
                            stringBuffer.append("<h:outputText rendered=\"#{empty " + variable + "." + propName + "}\" value=\"(No " + name + " Items Found)\"/>\n");
                            stringBuffer.append("<h:dataTable value=\"#{" + variable + "." + propName + "}\" var=\"item\" \n");
                            stringBuffer.append("border=\"0\" cellpadding=\"2\" cellspacing=\"0\" rowClasses=\"jsfcrud_oddrow,jsfcrud_evenrow\" rules=\"all\" style=\"border:solid 1px\" \n rendered=\"#{not empty " + variable + "." + propName + "}\">\n"); //NOI18N
                            String commands = "<h:column>\n"
                                    + "<f:facet name=\"header\">\n"
                                    + "<h:outputText escape=\"false\" value=\"&nbsp;\"/>\n"
                                    + "</f:facet>\n"
                                    + "<h:commandLink value=\"Show\" action=\"#'{'" + relatedManagedBean + ".detailSetup'}'\">\n" 
                                    + "<f:param name=\"jsfcrud.current" + simpleClass + "\" value=\"#'{'" + managedBean + ".asString[" + variable + "]'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.current" + relatedClass + "\" value=\"#'{'" + relatedManagedBean + ".asString[{0}]'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.relatedController\" value=\"" + managedBean + "\" />\n"
                                    + "<f:param name=\"jsfcrud.relatedControllerType\" value=\"" + controllerClass + "\" />\n"
                                    + "</h:commandLink>\n"
                                    + "<h:outputText value=\" \"/>\n"
                                    + "<h:commandLink value=\"Edit\" action=\"#'{'" + relatedManagedBean + ".editSetup'}'\">\n"
                                    + "<f:param name=\"jsfcrud.current" + simpleClass + "\" value=\"#'{'" + managedBean + ".asString[" + variable + "]'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.current" + relatedClass + "\" value=\"#'{'" + relatedManagedBean + ".asString[{0}]'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.relatedController\" value=\"" + managedBean + "\" />\n"
                                    + "<f:param name=\"jsfcrud.relatedControllerType\" value=\"" + controllerClass + "\" />\n"
                                    + "</h:commandLink>\n"
                                    + "<h:outputText value=\" \"/>\n"
                                    + "<h:commandLink value=\"Destroy\" action=\"#'{'" + relatedManagedBean + ".destroy'}'\">\n" 
                                    + "<f:param name=\"jsfcrud.current" + simpleClass + "\" value=\"#'{'" + managedBean + ".asString[" + variable + "]'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.current" + relatedClass + "\" value=\"#'{'" + relatedManagedBean + ".asString[{0}]'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.relatedController\" value=\"" + managedBean + "\" />\n"
                                    + "<f:param name=\"jsfcrud.relatedControllerType\" value=\"" + controllerClass + "\" />\n"
                                    + "</h:commandLink>\n"
                                    + "</h:column>\n";
                            
                            JsfTable.createTable(controller, typeElement, variable + "." + propName, stringBuffer, commands, embeddedPkSupport);
                            stringBuffer.append("</h:dataTable>\n");
                            stringBuffer.append("</h:panelGroup>\n");
                        } else {
                            Logger.getLogger("global").log(Level.INFO, "cannot find referenced class: " + method.getReturnType()); // NOI18N
                        }
                    }
                }
            }
        }
    }

    public static ExecutableElement getIdGetter(CompilationController controller, final boolean isFieldAccess, final TypeElement typeElement) {
        ExecutableElement[] methods = getEntityMethods(typeElement);
        for (ExecutableElement method : methods) {
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith("get")) {
                Element element = isFieldAccess ? JsfForm.guessField(controller, method) : method;
                if (element != null) {
                    if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) {
                        return method;
                    }
                }
            }
        }
        Logger.getLogger("global").log(Level.WARNING, "Cannot find ID getter in class: " + typeElement.getQualifiedName());
        return null;
    }
    
    public String getVariable() {
        return variable;
    }
    
    public void setVariable(String variable) {
        this.variable = variable;
    }
    
    public String getBean() {
        return bean;
    }
    
    public void setBean(String collection) {
        this.bean = collection;
    }
    
    public int getFormType() {
        return formType;
    }
    
    public void setFormType(int formType) {
        this.formType = formType;
    }
    
    public static boolean isAnnotatedWith(Element element, String annotationFqn) {
        return findAnnotation(element, annotationFqn) != null;
    }
    
    public static AnnotationMirror findAnnotation(Element element, String annotationFqn) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            String annotationQualifiedName = getAnnotationQualifiedName(annotationMirror);
            if (annotationQualifiedName.equals(annotationFqn)) {
                return annotationMirror;
            }
        }
        return null;
    }   
    
    public static String getAnnotationQualifiedName(AnnotationMirror annotationMirror) {
        DeclaredType annotationDeclaredType = annotationMirror.getAnnotationType();
        TypeElement annotationTypeElement = (TypeElement) annotationDeclaredType.asElement();
        Name name = annotationTypeElement.getQualifiedName();
        return name.toString();
    } 
    
    public static List<AnnotationMirror> findNestedAnnotations(AnnotationMirror annotationMirror, String annotationFqn) {
        List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
        findNestedAnnotationsInternal(annotationMirror, annotationFqn, result);
        return result;
    }

    private static void findNestedAnnotationsInternal(Object object, String annotationFqn, List<AnnotationMirror> result) {
        Collection<? extends AnnotationValue> annotationValueCollection = null;
        if (object instanceof AnnotationMirror) {
            AnnotationMirror annotationMirror = (AnnotationMirror)object;
            String annotationQualifiedName = getAnnotationQualifiedName(annotationMirror);
            if (annotationQualifiedName.equals(annotationFqn)) {
                result.add(annotationMirror);
            }
            else {
                //prepare to recurse
                Map<? extends ExecutableElement,? extends AnnotationValue> annotationMap = annotationMirror.getElementValues();
                annotationValueCollection = annotationMap.values();
            }
        }
        else if (object instanceof List) {
            //prepare to recurse
            annotationValueCollection = (Collection<? extends AnnotationValue>)object;
        }

        //recurse
        if (annotationValueCollection != null) {
            for (AnnotationValue annotationValue : annotationValueCollection) {
                Object value = annotationValue.getValue();
                findNestedAnnotationsInternal(value, annotationFqn, result);
            }
        }
    }
}
