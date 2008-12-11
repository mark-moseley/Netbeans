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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author Pavel Buzek
 * @author Po-Ting Wu
 * @author mbohm
 */
public final class JsfForm extends EntityClass implements ActiveEditorDrop {
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
    
    public JsfForm() {
    }
    
    protected String getName() {
        return "Form"; // NOI18N
    }
    
    protected String createBody(JTextComponent target, boolean surroundWithFView) throws IOException {
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

    public static void createForm(CompilationController controller, TypeElement bean, int formType, String variable, StringBuffer stringBuffer) {
        createForm(controller, bean, formType, variable, stringBuffer, "", null, "", "");
    }
    
    public static void createForm(CompilationController controller, TypeElement bean, int formType, String variable, StringBuffer stringBuffer, String entityClass, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, String controllerClass, String jsfUtilClass) {
        if (bean != null) {
            ExecutableElement methods [] = JpaControllerUtil.getEntityMethods(bean);
            boolean fieldAccess = JpaControllerUtil.isFieldAccess(bean);
            for (ExecutableElement method : methods) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    List<ExecutableElement> pkMethods = null;
                    boolean isId = isId(controller, method, fieldAccess);
                    boolean isGenerated = false;
                    if (isId) {
                        isGenerated = JpaControllerUtil.isGenerated(controller, method, fieldAccess);
                        TypeMirror t = method.getReturnType();
                        TypeMirror tstripped = JpaControllerUtil.stripCollection(t, controller.getTypes());
                        if (TypeKind.DECLARED == tstripped.getKind()) {
                            DeclaredType declaredType = (DeclaredType)tstripped;
                            TypeElement idClass = (TypeElement) declaredType.asElement();
                            boolean embeddable = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                            if (embeddable) {
                                pkMethods = new ArrayList<ExecutableElement>();
                                TypeElement entityType = controller.getElements().getTypeElement(entityClass);
                                if (embeddedPkSupport == null) {
                                    embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
                                }
                                for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(controller, entityType)) {
                                    if (!embeddedPkSupport.isRedundantWithRelationshipField(controller, entityType, pkMethod)) {
                                        pkMethods.add(pkMethod);
                                    }
                                }
                            }
                        }
                    }
                    if (pkMethods == null) {
                        createFormInternal(method, bean, isId, isGenerated, false, fieldAccess, controller, formType, variable, stringBuffer, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
                    }
                    else {
                        for (ExecutableElement pkMethod : pkMethods) {
                            String propName = JpaControllerUtil.getPropNameFromMethod(methodName);
                            createFormInternal(pkMethod, bean, false, false, true, fieldAccess, controller, formType, variable + "." + propName, stringBuffer, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
                        }
                    }
                }
            }
        }
    }
    
    private static void createFormInternal(ExecutableElement method, TypeElement bean, boolean isId, boolean isGenerated, boolean isEmbeddedPkMethod, boolean fieldAccess, CompilationController controller, int formType, String variable, StringBuffer stringBuffer, String entityClass, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, String controllerClass, String jsfUtilClass) {
        String simpleEntityName = JpaControllerUtil.simpleClassName(entityClass);
        TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType();
        String methodName = method.getSimpleName().toString();
        int isRelationship = JpaControllerUtil.isRelationship(controller, method, fieldAccess);
        String name = methodName.substring(3);
        String propName = JpaControllerUtil.getPropNameFromMethod(methodName);
        TypeMirror t = method.getReturnType();
        Types types = controller.getTypes();
        TypeMirror tstripped = JpaControllerUtil.stripCollection(t, types);
        
        boolean isCollection = t != tstripped;
        boolean isCollectionTypeAssignableToSet = false;
        if (isCollection) {
            TypeElement tAsElement = (TypeElement) types.asElement(t);
            isCollectionTypeAssignableToSet = isCollectionTypeAssignableToSet(tAsElement);
        }
        
        String relType = tstripped.toString();
        if (relType.endsWith("[]")) {
            relType = relType.substring(0, relType.length() - 2);
        }
        String simpleRelType = JpaControllerUtil.simpleClassName(relType); //just "Pavilion"
        String relatedController = JpaControllerUtil.fieldFromClassName(simpleRelType);
        boolean fieldOptionalAndNullable = JpaControllerUtil.isFieldOptionalAndNullable(controller, method, fieldAccess);
        String requiredMessage = fieldOptionalAndNullable ? null : "The " + propName + " field is required.";
        
        //only applies if method/otherSide are relationship methods
        boolean isMethodRedundantWithItsPkFields = false;
        boolean isOtherSideRedundantWithItsPkFields = false;
        if (!isEmbeddedPkMethod && isRelationship != JpaControllerUtil.REL_NONE) {
            if (embeddedPkSupport == null) {
                embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
            }
            isMethodRedundantWithItsPkFields = embeddedPkSupport.isRedundantWithPkFields(controller, bean, method);
            if (!isMethodRedundantWithItsPkFields) {
                ExecutableElement otherSide = JpaControllerUtil.getOtherSideOfRelation(controller, method, fieldAccess);
                if (otherSide != null) {
                    TypeElement relTypeElement = controller.getElements().getTypeElement(relType);
                    isOtherSideRedundantWithItsPkFields = embeddedPkSupport.isRedundantWithPkFields(controller, relTypeElement, otherSide);
                }
            }
        }
        
        if ( (formType == FORM_TYPE_NEW && 
                ( isId &&  isGenerated) ) || 
                formType == FORM_TYPE_EMPTY ) {
            //skip if formType is new and field is generated (or if formType is "empty")
        } else if (formType == FORM_TYPE_DETAIL && isRelationship == JpaControllerUtil.REL_TO_ONE && 
                (entityClass.length() == 0 || controllerClass.length() == 0) ) {
            //this method was called from outside the jsfcrud generator feature
            String template = "<h:outputText value=\"{0}:\"/>\n" +
                    "<h:outputText value=\" #'{'{1}.{2}'}'\"/>\n";
            Object[] args = new Object [] {name, variable, propName};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if (formType == FORM_TYPE_DETAIL && isRelationship == JpaControllerUtil.REL_TO_ONE) {
            String template = "<h:outputText value=\"{0}:\"/>\n" +
                "<h:panelGroup>\n" + 
                "<h:outputText value=\" #'{'{1}.{2}'}'\"/>\n" +
                "<h:panelGroup rendered=\"#'{'{1}.{2} != null'}'\">\n" +
                "<h:outputText value=\" (\"/>\n" +
                "<h:commandLink value=\"Show\" action=\"#'{'{4}.detailSetup'}'\">\n" +
                "<f:param name=\"jsfcrud.current{3}\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{1}][{6}.converter].jsfcrud_invoke'}'\"/>\n" +
                "<f:param name=\"jsfcrud.current{5}\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{1}.{2}][{4}.converter].jsfcrud_invoke'}'\"/>\n" +
                "<f:param name=\"jsfcrud.relatedController\" value=\"{6}\"/>\n" +
                "<f:param name=\"jsfcrud.relatedControllerType\" value=\"{7}\"/>\n" +
                "</h:commandLink>\n" +
                "<h:outputText value=\" \"/>\n" +
                "<h:commandLink value=\"Edit\" action=\"#'{'{4}.editSetup'}'\">\n" +
                "<f:param name=\"jsfcrud.current{3}\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{1}][{6}.converter].jsfcrud_invoke'}'\"/>\n" +
                "<f:param name=\"jsfcrud.current{5}\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{1}.{2}][{4}.converter].jsfcrud_invoke'}'\"/>\n" +
                "<f:param name=\"jsfcrud.relatedController\" value=\"{6}\"/>\n" +
                "<f:param name=\"jsfcrud.relatedControllerType\" value=\"{7}\"/>\n" +
                "</h:commandLink>\n" +
                "<h:outputText value=\" \"/>\n" +
                "<h:commandLink value=\"Destroy\" action=\"#'{'{4}.destroy'}'\">\n" +
                "<f:param name=\"jsfcrud.current{3}\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{1}][{6}.converter].jsfcrud_invoke'}'\"/>\n" +
                "<f:param name=\"jsfcrud.current{5}\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{1}.{2}][{4}.converter].jsfcrud_invoke'}'\"/>\n" +
                "<f:param name=\"jsfcrud.relatedController\" value=\"{6}\"/>\n" +
                "<f:param name=\"jsfcrud.relatedControllerType\" value=\"{7}\"/>\n" +
                "</h:commandLink>\n" +
                "<h:outputText value=\" )\"/>\n" +
                "</h:panelGroup>\n" +
                "</h:panelGroup>\n";
            Object[] args = new Object [] {name, variable, propName, simpleEntityName, relatedController, simpleRelType, variable.substring(0, variable.lastIndexOf('.')), controllerClass};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( (formType == FORM_TYPE_DETAIL && isRelationship == JpaControllerUtil.REL_NONE) || 
                ( formType == FORM_TYPE_EDIT && (isId || isEmbeddedPkMethod || isMethodRedundantWithItsPkFields || isOtherSideRedundantWithItsPkFields || isReadOnly(controller.getTypes(), method)) && isRelationship != JpaControllerUtil.REL_TO_MANY ) || 
                (formType == FORM_TYPE_NEW && (isOtherSideRedundantWithItsPkFields || isReadOnly(controller.getTypes(), method)) && isRelationship != JpaControllerUtil.REL_TO_MANY) ) {
            //non editable
            String temporal = ( isRelationship == JpaControllerUtil.REL_NONE && controller.getTypes().isSameType(dateTypeMirror, method.getReturnType()) ) ? getTemporal(controller, method, fieldAccess) : null;
            String template = "<h:outputText value=\"{0}:\"/>\n <h:outputText value=\"" + (isRelationship == JpaControllerUtil.REL_NONE ? "" : " ") + "#'{'{1}.{2}'}'\" title=\"{0}\" ";
            template += temporal == null ? "/>\n" : ">\n<f:convertDateTime pattern=\"{4}\" />\n</h:outputText>\n";
            Object[] args = temporal == null ? new Object [] {name, variable, propName} : new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal)};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( isRelationship == JpaControllerUtil.REL_NONE && (formType == FORM_TYPE_NEW || formType == FORM_TYPE_EDIT) ) {
            //editable
            String temporal = controller.getTypes().isSameType(dateTypeMirror, method.getReturnType()) ? getTemporal(controller, method, fieldAccess) : null;
            String template = temporal == null ? "<h:outputText value=\"{0}:\"/>\n" : "<h:outputText value=\"{0} ({4}):\"/>\n";
            Element fieldElement = fieldAccess ? JpaControllerUtil.guessField(controller, method) : method;
            boolean isLob = JpaControllerUtil.isAnnotatedWith(fieldElement, "javax.persistence.Lob");
            template += isLob ? "<h:inputTextarea rows=\"4\" cols=\"30\"" : "<h:inputText";
            template += " id=\"{2}\" value=\"#'{'{1}.{2}'}'\" title=\"{0}\" ";
            template += requiredMessage == null ? "" : "required=\"true\" requiredMessage=\"{5}\" ";
            template += temporal == null ? "/>\n" : ">\n<f:convertDateTime pattern=\"{4}\" />\n</h:inputText>\n";
            Object[] args = temporal == null ? new Object [] {name, variable, propName, null, null, requiredMessage} : new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal), requiredMessage};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( isRelationship == JpaControllerUtil.REL_TO_ONE && (formType == FORM_TYPE_EDIT || formType == FORM_TYPE_NEW) ) {
            //combo box for editing toOne relationships
            String template = "<h:outputText value=\"{0}:\"/>\n <h:selectOneMenu id=\"{2}\" value=\"#'{'{1}.{2}'}'\" title=\"{0}\" ";
            template += requiredMessage == null ? "" : "required=\"true\" requiredMessage=\"{3}\" ";
            template += ">\n <f:selectItems value=\"#'{'{4}.{4}ItemsAvailableSelectOne'}'\"/>\n </h:selectOneMenu>\n";
            Object[] args = new Object [] {name, variable, propName, requiredMessage, relatedController};
            stringBuffer.append(MessageFormat.format(template, args));
        } else if ( isRelationship == JpaControllerUtil.REL_TO_MANY && (formType == FORM_TYPE_EDIT || formType == FORM_TYPE_NEW) ) {
            if (isOtherSideRedundantWithItsPkFields) {
                String template = "<h:outputText value=\"{0}:\"/>\n <h:outputText escape=\"false\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getCollectionAsString''][{3}.{3}.{2} == null ? jsfcrud_null : {3}.{3}.{2}].jsfcrud_invoke'}'\" title=\"{0}\" />\n";
                Object[] args = new Object [] {name, simpleEntityName, propName, variable.substring(0, variable.lastIndexOf('.'))};
                stringBuffer.append(MessageFormat.format(template, args));
            }
            else {
                //listbox for editing toMany relationships
                String arrayToCollection = isCollectionTypeAssignableToSet ? "arrayToSet" : "arrayToList";
                String template = "<h:outputText value=\"{0}:\"/>\n <h:selectManyListbox id=\"{2}\" value=\"#'{'{3}.{3}.jsfcrud_transform[jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method.collectionToArray][jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method." + arrayToCollection + "].{2}'}'\" title=\"{0}\" size=\"6\" converter=\"#'{'{4}.converter'}'\" ";
                template += requiredMessage == null ? "" : "required=\"true\" requiredMessage=\"{5}\" ";
                template += ">\n <f:selectItems value=\"#'{'{4}.{4}ItemsAvailableSelectMany'}'\"/>\n </h:selectManyListbox>\n";
                Object[] args = new Object [] {name, simpleEntityName, propName, variable.substring(0, variable.lastIndexOf('.')), relatedController, requiredMessage};
                stringBuffer.append(MessageFormat.format(template, args));
            }
        }
    }
    
    private static boolean isCollectionTypeAssignableToSet(TypeElement tAsElement) {
        String collectionTypeClass = tAsElement.getQualifiedName().toString();   //java.util.Collection, java.util.List, java.util.Set

        Class collectionTypeAsClass = null;
        try {
            collectionTypeAsClass = Class.forName(collectionTypeClass);
        } catch (ClassNotFoundException cfne) {
            //let collectionTypeAsClass be null
        }
        if (collectionTypeAsClass != null && Set.class.isAssignableFrom(collectionTypeAsClass)) {
            return true;
        }
        
        return false;
    }
    
    public static String getFreeTableVarName(String name, List<String> entities) {
        //return a permutation of name that is not a managed bean name among the entities
        String newName = name;
        int i = 0;
        while (i < 1000) {
            boolean match = false;
            for (String entityClass : entities) {
                String simpleEntityName = JpaControllerUtil.simpleClassName(entityClass);
                String managedBeanName = JSFClientGenerator.getManagedBeanName(simpleEntityName);
                if (newName.equals(managedBeanName)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                newName = name + (++i);
            }
            else {
                return newName;
            }
        }
        return newName;
    }
    
    public static void createTablesForRelated(CompilationController controller, TypeElement bean, int formType, String variable, 
            String idProperty, boolean isInjection, StringBuffer stringBuffer, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, String controllerClass, List<String> entities, String jsfUtilClass) {
        ExecutableElement methods [] = JpaControllerUtil.getEntityMethods(bean);
        String entityClass = bean.getQualifiedName().toString();
        String simpleClass = bean.getSimpleName().toString();
        String managedBean = JSFClientGenerator.getManagedBeanName(simpleClass);
        boolean fieldAccess = JpaControllerUtil.isFieldAccess(bean);
        //generate tables of objects with ToMany relationships
        if (formType == FORM_TYPE_DETAIL) {
            for (ExecutableElement method : methods) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    int isRelationship = JpaControllerUtil.isRelationship(controller, method, fieldAccess);
                    String name = methodName.substring(3);
                    String propName = JpaControllerUtil.getPropNameFromMethod(methodName);
                    if (isRelationship == JpaControllerUtil.REL_TO_MANY) {
                        Types types = controller.getTypes(); 
                        TypeMirror t = method.getReturnType();
                        TypeMirror typeArgMirror = JpaControllerUtil.stripCollection(t, types);
                        TypeElement typeElement = (TypeElement)types.asElement(typeArgMirror);
                        if (typeElement != null) {
                            TypeElement tAsElement = (TypeElement) types.asElement(t);
                            boolean isCollectionTypeAssignableToSet = isCollectionTypeAssignableToSet(tAsElement);
                            String relatedClass = typeElement.getSimpleName().toString();
                            String relatedManagedBean = JSFClientGenerator.getManagedBeanName(relatedClass);
                            String tableVarName = getFreeTableVarName("item", entities); //NOI18N
                            stringBuffer.append("<h:outputText value=\"" + name + ":\" />\n");
                            stringBuffer.append("<h:panelGroup>\n");
                            stringBuffer.append("<h:outputText rendered=\"#{empty " + variable + "." + propName + "}\" value=\"(No Items)\"/>\n");
                            String valueAttribute = isCollectionTypeAssignableToSet ? variable + ".jsfcrud_transform[jsfcrud_class['" + jsfUtilClass + "'].jsfcrud_method.setToList][jsfcrud_null]." + propName : variable + "." + propName;
                            stringBuffer.append("<h:dataTable value=\"#{" + valueAttribute + "}\" var=\"" + tableVarName + "\" \n");
                            stringBuffer.append("border=\"0\" cellpadding=\"2\" cellspacing=\"0\" rowClasses=\"jsfcrud_odd_row,jsfcrud_even_row\" rules=\"all\" style=\"border:solid 1px\" \n rendered=\"#{not empty " + variable + "." + propName + "}\">\n"); //NOI18N
                            
                            String commands = "<h:column>\n"
                                    + "<f:facet name=\"header\">\n"
                                    + "<h:outputText escape=\"false\" value=\"&nbsp;\"/>\n"
                                    + "</f:facet>\n"
                                    + "<h:commandLink value=\"Show\" action=\"#'{'" + relatedManagedBean + ".detailSetup'}'\">\n" 
                                    + "<f:param name=\"jsfcrud.current" + simpleClass + "\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][" + variable + "][" + managedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.current" + relatedClass + "\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{0}][" + relatedManagedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.relatedController\" value=\"" + managedBean + "\" />\n"
                                    + "<f:param name=\"jsfcrud.relatedControllerType\" value=\"" + controllerClass + "\" />\n"
                                    + "</h:commandLink>\n"
                                    + "<h:outputText value=\" \"/>\n"
                                    + "<h:commandLink value=\"Edit\" action=\"#'{'" + relatedManagedBean + ".editSetup'}'\">\n"
                                    + "<f:param name=\"jsfcrud.current" + simpleClass + "\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][" + variable + "][" + managedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.current" + relatedClass + "\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{0}][" + relatedManagedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.relatedController\" value=\"" + managedBean + "\" />\n"
                                    + "<f:param name=\"jsfcrud.relatedControllerType\" value=\"" + controllerClass + "\" />\n"
                                    + "</h:commandLink>\n"
                                    + "<h:outputText value=\" \"/>\n"
                                    + "<h:commandLink value=\"Destroy\" action=\"#'{'" + relatedManagedBean + ".destroy'}'\">\n" 
                                    + "<f:param name=\"jsfcrud.current" + simpleClass + "\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][" + variable + "][" + managedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.current" + relatedClass + "\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{0}][" + relatedManagedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                                    + "<f:param name=\"jsfcrud.relatedController\" value=\"" + managedBean + "\" />\n"
                                    + "<f:param name=\"jsfcrud.relatedControllerType\" value=\"" + controllerClass + "\" />\n"
                                    + "</h:commandLink>\n"
                                    + "</h:column>\n";
                            
                            JsfTable.createTable(controller, typeElement, variable + "." + propName, stringBuffer, commands, embeddedPkSupport, tableVarName);
                            stringBuffer.append("</h:dataTable>\n");
                            stringBuffer.append("</h:panelGroup>\n");
                        } else {
                            Logger.getLogger(JsfForm.class.getName()).log(Level.INFO, "cannot find referenced class: " + method.getReturnType()); // NOI18N
                        }
                    }
                }
            }
        }
    }
}
