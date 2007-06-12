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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.InitMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback;
import org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef;
import org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.EjbLocalRefImpl;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.EjbRefImpl;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.ServiceRefImpl;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.openide.util.Exceptions;

public class SessionImpl implements Session {
    
    protected enum Kind { STATELESS, STATEFUL }
    
    // initialized in constructor
    private final String ejbName;
    private final String ejbClass;
    private final String sessionType;
    
    // lazy initialization
    private String[] businessLocal;
    private String[] businessRemote;
    private EjbRef[] ejbRefs;
    private EjbLocalRef[] ejbLocalRefs;
    private ServiceRef[] serviceRefs;
    private ResourceRef[] resourceRefs;
    private ResourceEnvRef[] resourceEnvRefs = null;
    private EnvEntry[] envEntries = null;
    private MessageDestinationRef[] messageDestinationRefs = null;
    
    // helpers
    private final AnnotationModelHelper helper;
    private final TypeElement typeElement;
    
    public SessionImpl(Kind kind, AnnotationModelHelper helper, TypeElement typeElement) {
        this.helper = helper;
        this.typeElement = typeElement;
        
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", parser.defaultValue(typeElement.getSimpleName().toString())); // NOI18N
        ParseResult parseResult = parser.parse(annByType.get(kind == Kind.STATELESS ? "javax.ejb.Stateless" : "javax.ejb.Stateful")); //NOI18N
        ejbName = parseResult.get("name", String.class); // NOI18N
        ejbClass = typeElement.getQualifiedName().toString();
        sessionType = (kind == Kind.STATELESS) ? Session.SESSION_TYPE_STATELESS : Session.SESSION_TYPE_STATEFUL;
    }
    
    // <editor-fold desc="Helpers">
    
    /**
     * Initializes businessLocal and businessRemote fields
     */
    private void initBusinessInterfaces() {
        
        if (businessLocal != null && businessRemote != null) {
            return;
        }
        
        List<TypeElement> interfaces = new ArrayList<TypeElement>(); // all business interface candidates, EJB 3.0 Spec, Chapter 10.2
        for (TypeMirror typeMirror : typeElement.getInterfaces()) {
            if (TypeKind.DECLARED == typeMirror.getKind()) {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                Element element = declaredType.asElement();
                if (ElementKind.INTERFACE == element.getKind()) {
                    TypeElement interfaceTypeElement = (TypeElement) element;
                    String fqn = interfaceTypeElement.getQualifiedName().toString();
                    if (!"java.io.Serializable".equals(fqn) && !"java.io.Externalizable".equals(fqn) && !fqn.startsWith("javax.ejb")) {
                        interfaces.add(interfaceTypeElement);
                    }
                }
            }
        }
        
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());

        AnnotationMirror beanLocalAnnotation = annByType.get("javax.ejb.Local"); // @Local at bean class
        AnnotationMirror beanRemoteAnnotation = annByType.get("javax.ejb.Remote"); // @Remote at beans class
        
        List<String> annotatedLocalInterfaces = new ArrayList<String>();
        List<String> annotatedRemoteInterfaces = new ArrayList<String>();
        
        for (TypeElement interfaceTypeElement : interfaces) {
            annByType = helper.getAnnotationsByType(interfaceTypeElement.getAnnotationMirrors());
            if (annByType.get("javax.ejb.Local") != null) {
                annotatedLocalInterfaces.add(interfaceTypeElement.getQualifiedName().toString());
            }
            if (annByType.get("javax.ejb.Remote") != null) {
                annotatedRemoteInterfaces.add(interfaceTypeElement.getQualifiedName().toString());
            }
        }
        
        if (interfaces.size() == 1 && beanLocalAnnotation == null && beanRemoteAnnotation == null &&
                annotatedLocalInterfaces.size() == 0 && annotatedRemoteInterfaces.size() == 0) {
            businessLocal = new String[] { interfaces.get(0).getQualifiedName().toString() };
            businessRemote = new String[] {};
        } else {
            if (beanLocalAnnotation != null) {
                List<String> annotationsValues = getClassesFromLocalOrRemote(beanLocalAnnotation);
                businessLocal = annotationsValues.toArray(new String[annotationsValues.size()]);
            } else {
                businessLocal = annotatedLocalInterfaces.toArray(new String[annotatedLocalInterfaces.size()]);
            }
            if (beanRemoteAnnotation != null) {
                List<String> annotationsValues = getClassesFromLocalOrRemote(beanRemoteAnnotation);
                businessRemote = annotationsValues.toArray(new String[annotationsValues.size()]);
            } else {
                businessRemote = annotatedRemoteInterfaces.toArray(new String[annotatedRemoteInterfaces.size()]);
            }
        }
    }
    
    /**
     * Extracts Class[] from @Local and @Remote annotations
     */
    private List<String> getClassesFromLocalOrRemote(AnnotationMirror beanLocalAnnotation) {
        final List<String> result = new ArrayList<String>();
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectClassArray("value", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    TypeMirror typeMirror = (TypeMirror) arrayMember.getValue();
                    if (TypeKind.DECLARED == typeMirror.getKind()) {
                        DeclaredType declaredType = (DeclaredType) typeMirror;
                        Element element = declaredType.asElement();
                        if (ElementKind.INTERFACE == element.getKind()) {
                            TypeElement interfaceTypeElement = (TypeElement) element;
                            result.add(interfaceTypeElement.getQualifiedName().toString());
                        }
                    }
                }
                return null;
            }
        }, null);
        parser.parse(beanLocalAnnotation);
        return result;
    }
    
    private void initLocalAndRemoteEjbRefs() {
        
        if (ejbRefs != null && ejbLocalRefs != null) {
            return;
        }
        
        final List<EjbRef> resultEjbRefs = new ArrayList<EjbRef>();
        final List<EjbLocalRef> resultEjbLocalRefs = new ArrayList<EjbLocalRef>();
        
        // javax.ejb.EJBs is array of javax.ejb.EJB and is applicable to class
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror ejbsAnnotation = annByType.get("javax.ejb.EJBs"); // NOI18N
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectAnnotationArray("value", helper.resolveType("javax.ejb.EJB"), new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    Object arrayMemberValue = arrayMember.getValue();
                    if (arrayMemberValue instanceof AnnotationMirror) {
                        createReference((AnnotationMirror) arrayMemberValue, resultEjbRefs, resultEjbLocalRefs);
                    }
                }
                return null;
            }
        }, null);
        parser.parse(ejbsAnnotation);
        
        // @EJB at class
        if (helper.hasAnnotation(typeElement.getAnnotationMirrors(), "javax.ejb.EJB")) { // NOI18N
            createReference(typeElement, resultEjbRefs, resultEjbLocalRefs);
        }
        // @EJB at field
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(variableElement.getAnnotationMirrors(), "javax.ejb.EJB")) { // NOI18N
                createReference(variableElement, resultEjbRefs, resultEjbLocalRefs);
            }
        }
        // @EJB at method
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(executableElement.getAnnotationMirrors(), "javax.ejb.EJB")) {
                createReference(executableElement, resultEjbRefs, resultEjbLocalRefs);
            }
        }
        
        ejbRefs = resultEjbRefs.toArray(new EjbRef[resultEjbRefs.size()]);
        ejbLocalRefs = resultEjbLocalRefs.toArray(new EjbLocalRef[resultEjbLocalRefs.size()]);
                
    }
    
    /** search for web service service references at fields
     *  (the only reasonable usage of WebServiceRef)
     */ 
    private void initServiceRefs() {
        
        if (serviceRefs != null) {
            return;
        }
        final List<ServiceRef> resultServiceRefs = new ArrayList<ServiceRef>();
        
        // @WebServiceRef at field
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(variableElement.getAnnotationMirrors(), "javax.xml.ws.WebServiceRef")) { //NOI18N
                createServiceReference(variableElement, resultServiceRefs);
            }
        }
        // @Resource
        List<ServiceRef> serviceRefsFromResources = Arrays.asList(CommonAnnotationHelper.getServiceRefs(helper, typeElement));
        resultServiceRefs.addAll(serviceRefsFromResources);
        serviceRefs = resultServiceRefs.toArray(new ServiceRef[resultServiceRefs.size()]);                
    }
        
    /**
     * Creates service reference
     */    
    private void createServiceReference(Element element, List<ServiceRef> resultServiceRefs) {
            
            TypeMirror fieldTypeMirror = element.asType();
            
            if (TypeKind.DECLARED == fieldTypeMirror.getKind()) {
                DeclaredType fieldDeclaredType = (DeclaredType) fieldTypeMirror;
                Element fieldTypeElement = fieldDeclaredType.asElement();
                if (ElementKind.INTERFACE == fieldTypeElement.getKind() || ElementKind.CLASS == fieldTypeElement.getKind() ) {
                    TypeElement typeElement = (TypeElement) fieldTypeElement;
                    ServiceRef newServiceRef = new ServiceRefImpl(element, typeElement, helper);
                    // test if already exists
                    ServiceRef existingServiceRef=null;
                    for (ServiceRef sr:resultServiceRefs) {
                        if (newServiceRef.getServiceRefName().equals(sr.getServiceRefName())) {
                            existingServiceRef = sr;
                        }
                    }
                    if (existingServiceRef!=null) {
                        if (newServiceRef.sizePortComponentRef()>0) {
                            PortComponentRef newPortComp = newServiceRef.getPortComponentRef(0);
                            // eventiually add new PortComponentRef
                            PortComponentRef[] portComps = existingServiceRef.getPortComponentRef();
                            boolean foundPortComponent=false;
                            for (PortComponentRef portComp:portComps) {
                                if (portComp.getServiceEndpointInterface().equals(newPortComp.getServiceEndpointInterface())) {
                                    foundPortComponent=true;
                                }
                            }
                            if (!foundPortComponent) {
                                existingServiceRef.addPortComponentRef(newPortComp);
                            }
                        }
                    } else {
                        resultServiceRefs.add(newServiceRef);
                    }
                }              
            }
    }

    /**
     * Creates local or remote reference
     */
    private void createReference(Element element, List<EjbRef> resultEjbRefs, List<EjbLocalRef> resultEjbLocalRefs) {
        
        String name = null;
        String beanInterface = null;
        String beanName = null;
        String mappedName = null;
        String description = null;
        
        TypeElement interfaceTypeElement = null;
        
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(element.getAnnotationMirrors());
        
        if (ElementKind.CLASS == element.getKind()) {

            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectString("name", null);
            parser.expectClass("beanInterface", null);
            parser.expectString("beanName", null);
            parser.expectString("mappedName", null);
            parser.expectString("description", null);
            ParseResult parseResult = parser.parse(annByType.get("javax.ejb.EJB"));
            
            name = parseResult.get("name", String.class);
            beanInterface = parseResult.get("beanInterface", String.class);
            beanName = parseResult.get("beanName", String.class);
            mappedName = parseResult.get("mappedName", String.class);
            description = parseResult.get("description", String.class);
            
            if (beanInterface != null) {
                interfaceTypeElement = helper.getCompilationController().getElements().getTypeElement(beanInterface);
            }
            
        } else if (ElementKind.FIELD == element.getKind() || ElementKind.METHOD == element.getKind()) {
            
            TypeMirror fieldTypeMirror = element.asType();

            if (ElementKind.METHOD == element.getKind()) {
                if (!element.getSimpleName().toString().startsWith("set")) {
                    return;
                }
                ExecutableElement method = (ExecutableElement) element;
                List<? extends VariableElement> parameters = method.getParameters();
                if (parameters.size() != 1) {
                    return;
                }
                fieldTypeMirror = parameters.get(0).asType();
            }
            
            if (TypeKind.DECLARED == fieldTypeMirror.getKind()) {
                DeclaredType fieldDeclaredType = (DeclaredType) fieldTypeMirror;
                Element fieldTypeElement = fieldDeclaredType.asElement();
                if (ElementKind.INTERFACE == fieldTypeElement.getKind()) {
                    interfaceTypeElement = (TypeElement) fieldTypeElement;
                    beanInterface = interfaceTypeElement.getQualifiedName().toString();
                }
            }
            
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectString("name", parser.defaultValue(element.getSimpleName().toString()));
            ParseResult parseResult = parser.parse(annByType.get("javax.ejb.EJB"));
            name = parseResult.get("name", String.class);
            
        } else {
            return;
        }
        
        if (interfaceTypeElement != null) {
            createReference(interfaceTypeElement, resultEjbRefs, resultEjbLocalRefs, name, beanInterface, beanName, mappedName, description);
        }
        
    }
    
    /**
     * Creates local or remote reference
     */
    private void createReference(AnnotationMirror annotationMirror, List<EjbRef> resultEjbRefs, List<EjbLocalRef> resultEjbLocalRefs) {
        
        String name;
        String beanInterface;
        String beanName;
        String mappedName;
        String description;
        
        TypeElement interfaceTypeElement;
        
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", null);
        parser.expectClass("beanInterface", null);
        parser.expectString("beanName", null);
        parser.expectString("mappedName", null);
        parser.expectString("description", null);
        ParseResult parseResult = parser.parse(annotationMirror);

        name = parseResult.get("name", String.class);
        beanInterface = parseResult.get("beanInterface", String.class);
        beanName = parseResult.get("beanName", String.class);
        mappedName = parseResult.get("mappedName", String.class);
        description = parseResult.get("description", String.class);

        interfaceTypeElement = helper.getCompilationController().getElements().getTypeElement(beanInterface);

        createReference(interfaceTypeElement, resultEjbRefs, resultEjbLocalRefs, name, beanInterface, beanName, mappedName, description);
        
    }
    
    private void createReference(TypeElement interfaceTypeElement, List<EjbRef> resultEjbRefs, List<EjbLocalRef> resultEjbLocalRefs, 
            String name, String beanInterface, String beanName, String mappedName, String description) {
        
        // TODO: implement good-enough algorithm to recognize if referenced interface is local or remote
        // this one just checks if there is @Remote annotation; if not, it is local
        boolean isLocal = true;
        Map<String, ? extends AnnotationMirror> memberAnnByType = helper.getAnnotationsByType(interfaceTypeElement.getAnnotationMirrors());
        if (memberAnnByType.get("javax.ejb.Remote") != null) {
            isLocal = false;
        }
        
        if (isLocal) {
            resultEjbLocalRefs.add(new EjbLocalRefImpl(name, beanInterface, beanName, mappedName, description));
        } else {
            resultEjbRefs.add(new EjbRefImpl(name, beanInterface, beanName, mappedName, description));
        }
    }
    
    private void initResourceRefs() {
        if (resourceRefs != null) {
            return;
        }
        resourceRefs = CommonAnnotationHelper.getResourceRefs(helper, typeElement);
    }
    
    private void initResourceEnvRefs() {
        if (resourceEnvRefs != null) {
            return;
        }
        resourceEnvRefs = CommonAnnotationHelper.getResourceEnvRefs(helper, typeElement);
    }
    
    private void initEnvEntries() {
        if (envEntries != null) {
            return;
        }
        envEntries = CommonAnnotationHelper.getEnvEntries(helper, typeElement);
    }
    
    
    private void initMessageDestinationRefs() {
        if (messageDestinationRefs != null) {
            return;
        }
        messageDestinationRefs = CommonAnnotationHelper.getMessageDestinationRefs(helper, typeElement);
    }
    // </editor-fold>

    // <editor-fold desc="Model implementation">
    
    public String getEjbName() {
        return ejbName;
    }
    
    public String getEjbClass() {
        return ejbClass;
    }
    
    public String getSessionType() {
        return sessionType;
    }
    
    public String[] getBusinessLocal() throws VersionNotSupportedException {
        initBusinessInterfaces();
        return businessLocal;
    }
    
    public String[] getBusinessRemote() throws VersionNotSupportedException {
        initBusinessInterfaces();
        return businessRemote;
    }
    
    public EjbRef[] getEjbRef() {
        initLocalAndRemoteEjbRefs();
        return ejbRefs;
    }
    
    public EjbLocalRef[] getEjbLocalRef() {
        initLocalAndRemoteEjbRefs();
        return ejbLocalRefs;
    }
    
    public ServiceRef getServiceRef(int index) throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs[index];
    }
    
    public ServiceRef[] getServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs;
    }
    
    public int sizeServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs.length;
    }
    
    public ResourceRef[] getResourceRef() {
        initResourceRefs();
        return resourceRefs;
    }
    
    public ResourceRef getResourceRef(int index) {
        initResourceRefs();
        return resourceRefs[index];
    }
    
    public int sizeResourceRef() {
        initResourceRefs();
        return resourceRefs.length;
    }
    
    
    public ResourceEnvRef[] getResourceEnvRef() {
        initResourceEnvRefs();
        return resourceEnvRefs;
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        initResourceEnvRefs();
        return resourceEnvRefs[index];
    }

    public int sizeResourceEnvRef() {
        initResourceEnvRefs();
        return resourceEnvRefs.length;
    }

    public EnvEntry[] getEnvEntry() {
        initEnvEntries();
        return envEntries;
    }

    public EnvEntry getEnvEntry(int index) {
        initEnvEntries();
        return envEntries[index];
    }

    public int sizeEnvEntry() {
        initEnvEntries();
        return envEntries.length;
    }
    
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs;
    }

    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs[index];
    }

    public int sizeMessageDestinationRef() throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs.length;
    }
    
    public String getServiceEndpoint() throws VersionNotSupportedException {
        // TODO
        return null;
    }
    
    public String getDefaultDisplayName() {
        // TODO
        return getEjbName();
    }
    
    public String getLocal() {
        // TODO
        try {
            String[] businessLocal = getBusinessLocal();
            if (businessLocal.length > 0) {
                return businessLocal[0];
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public String getRemote() {
        // TODO
        try {
            String[] businessRemote = getBusinessRemote();
            if (businessRemote.length > 0) {
                return businessRemote[0];
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public String getLocalHome() {
        // TODO
        return null;
    }
    
    public String getHome() {
        // TODO
        return null;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSessionType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getTransactionType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setTransactionType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setServiceEndpoint(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setMappedName(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getMappedName() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessLocal(int index, String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getBusinessLocal(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeBusinessLocal() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessLocal(String[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addBusinessLocal(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeBusinessLocal(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessRemote(int index, String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getBusinessRemote(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeBusinessRemote() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessRemote(String[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addBusinessRemote(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeBusinessRemote(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setTimeoutMethod(NamedMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamedMethod getTimeoutMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setInitMethod(int index, InitMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InitMethod getInitMethod(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeInitMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setInitMethod(InitMethod[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InitMethod[] getInitMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addInitMethod(InitMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeInitMethod(InitMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRemoveMethod(int index, RemoveMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public RemoveMethod getRemoveMethod(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeRemoveMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRemoveMethod(RemoveMethod[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public RemoveMethod[] getRemoveMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addRemoveMethod(RemoveMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeRemoveMethod(RemoveMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAroundInvoke(int index, AroundInvoke valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public AroundInvoke getAroundInvoke(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeAroundInvoke() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAroundInvoke(AroundInvoke[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public AroundInvoke[] getAroundInvoke() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPersistenceContextRef(int index,
            PersistenceContextRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PersistenceContextRef getPersistenceContextRef(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePersistenceContextRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPersistenceContextRef(PersistenceContextRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PersistenceContextRef[] getPersistenceContextRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPersistenceUnitRef(int index,
            PersistenceUnitRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PersistenceUnitRef getPersistenceUnitRef(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePersistenceUnitRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPersistenceUnitRef(PersistenceUnitRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PersistenceUnitRef[] getPersistenceUnitRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPostConstruct(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback getPostConstruct(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePostConstruct() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPostConstruct(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback[] getPostConstruct() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPreDestroy(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback getPreDestroy(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePreDestroy() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPreDestroy(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback[] getPreDestroy() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPostActivate(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback getPostActivate(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePostActivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPostActivate(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback[] getPostActivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPostActivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePostActivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPrePassivate(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback getPrePassivate(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePrePassivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPrePassivate(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback[] getPrePassivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPrePassivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePrePassivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamedMethod newNamedMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InitMethod newInitMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public RemoveMethod newRemoveMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public AroundInvoke newAroundInvoke() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PersistenceContextRef newPersistenceContextRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PersistenceUnitRef newPersistenceUnitRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback newLifecycleCallback() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRemote(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLocal(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLocalHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSecurityRoleRef(int index, SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityRoleRef getSecurityRoleRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSecurityRoleRef(SecurityRoleRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityRoleRef[] getSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeSecurityRoleRef(SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addSecurityRoleRef(SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityRoleRef newSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EjbJar getRoot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEjbName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEjbClass(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEnvEntry(int index, EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEnvEntry(EnvEntry[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addEnvEntry(EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeEnvEntry(EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EnvEntry newEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEjbRef(int index, EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EjbRef getEjbRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEjbRef(EjbRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeEjbRef(EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addEjbRef(EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EjbRef newEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEjbLocalRef(int index, EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EjbLocalRef getEjbLocalRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setEjbLocalRef(EjbLocalRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addEjbLocalRef(EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeEjbLocalRef(EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EjbLocalRef newEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityIdentity getSecurityIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSecurityIdentity(SecurityIdentity value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityIdentity newSecurityIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setResourceRef(int index, ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setResourceRef(ResourceRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeResourceRef(ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addResourceRef(ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ResourceRef newResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setResourceEnvRef(int index, ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setResourceEnvRef(ResourceEnvRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addResourceEnvRef(ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeResourceEnvRef(ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ResourceEnvRef newResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setServiceRef(int index, ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeServiceRef(ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addServiceRef(ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ServiceRef newServiceRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean addBean(String beanName, String[] propertyNames,
            Object[] propertyValues, String keyProperty) throws ClassNotFoundException,
            NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean findBeanByName(String beanName, String propertyName,
            String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAllIcons(String[] locales, String[] smallIcons,
            String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // </editor-fold>

}

