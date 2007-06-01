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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource.AuthenticationType;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.modules.j2ee.dd.util.AnnotationUtils;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

/**
 * Default implemetation of {@link javax.annotation.Resource}.
 * For more info see <a href="http://jcp.org/en/jsr/detail?id=250">JSR 250</a>.
 * <p>
 * This class is used by {@link ResourceRefImpl}, {@link EnvEntryImpl} and {@link ResourceEnvRefImpl}.
 * @author Tomas Mysik
 * @see CommonAnnotationHelper
 */
public class ResourceImpl {
    
    // package private because of unit tests
    static final String DEFAULT_AUTHENTICATION_TYPE = AuthenticationType.CONTAINER.name();
    static final String DEFAULT_SHAREABLE = Boolean.TRUE.toString();
    static final String DEFAULT_MAPPED_NAME = "";
    static final String DEFAULT_DESCRIPTION = "";
    
    private final ParseResult parseResult;
    
    // helpers
    private final AnnotationModelHelper annotationModelHelper;
    private final TypeElement typeElement;
    private final Element element;
    
    /**
     * Creates a new instance of Resource.
     * @param element               program element which is annotated.
     *                              Valid elements are <tt>class</tt>, <tt>method</tt> and <tt>field</tt>.
     * @param typeElement           a <tt>class</tt> or <tt>interface</tt> in which there is an annotated element.
     * @param annotationModelHelper annotation model helper.
     */
    public ResourceImpl(Element element, TypeElement typeElement, AnnotationModelHelper annotationModelHelper) {
        this.annotationModelHelper = annotationModelHelper;
        this.typeElement = typeElement;
        this.element = element;
        
        Map<String, ? extends AnnotationMirror> annByType = annotationModelHelper.getAnnotationsByType(element.getAnnotationMirrors());
        
        parseResult = parseAnnotation(annByType.get(javax.annotation.Resource.class.getName()));
    }
    
    private ParseResult parseAnnotation(AnnotationMirror resourceAnnotation) {
        
        AnnotationParser parser = AnnotationParser.create(annotationModelHelper);
        
        // The JNDI name of the resource, default ""
        parser.expectString("name", new DefaultProvider() { // NOI18N
            public Object getDefaultValue() {
                switch (element.getKind()) {
                case METHOD:
                    return AnnotationUtils.setterNameToPropertyName(element.getSimpleName().toString());
                case FIELD:
                    return element.getSimpleName().toString();
                }
                return "";
            }
        });
        
        // The Java type of the resource, default Object.class
        parser.expectClass("type", new DefaultProvider() { // NOI18N
            public Object getDefaultValue() {
                switch (element.getKind()) {
                case CLASS:
                    TypeElement typeElement = (TypeElement) element;
                    return typeElement.getQualifiedName().toString();
                case METHOD:
                    // only setter is expected
                    ExecutableElement executableElement = (ExecutableElement) element;
                    List<? extends VariableElement> parameters = executableElement.getParameters();
                    if (!parameters.isEmpty()) {
                        VariableElement parameter = parameters.get(0);
                        // XXX: see #104194
                        return parameter.asType().toString();
                    }
                    break;
                case FIELD:
                    VariableElement field = (VariableElement) element;
                    // XXX: see #104194
                    return field.asType().toString();
                }
                return Object.class.getName();
            }
        });
        
        // The authentication type to use for the resource, default AuthenticationType.CONTAINER
        parser.expectEnumConstant(
                "authenticationType", // NOI18N
                annotationModelHelper.resolveType("javax.annotation.Resource.AuthenticationType"),
                parser.defaultValue(DEFAULT_AUTHENTICATION_TYPE));
        
        // Indicates whether the resource can be shared, default true
        parser.expectPrimitive("shareable", Boolean.class, parser.defaultValue(Boolean.parseBoolean(DEFAULT_SHAREABLE))); // NOI18N
        
        // A product specific name that the resource should map to, default ""
        parser.expectString("mappedName", parser.defaultValue(DEFAULT_MAPPED_NAME)); // NOI18N
                
        // Description of the resource, default ""
        parser.expectString("description", parser.defaultValue(DEFAULT_DESCRIPTION)); // NOI18N
        
        return parser.parse(resourceAnnotation);
    }
    
    public String getName() {
        return parseResult.get("name", String.class); // NOI18N
    }

    public String getType() {
        return parseResult.get("type", String.class); // NOI18N
    }

    public String getAuthenticationType() {
        return parseResult.get("authenticationType", String.class); // NOI18N
    }
    
    public String getShareable() {
        return parseResult.get("shareable", Boolean.class).toString(); // NOI18N
    }

    public String getMappedName() {
        return parseResult.get("mappedName", String.class); // NOI18N
    }

    public String getDescription() {
        return parseResult.get("description", String.class); // NOI18N
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);

        sb.append(" Name: ");
        sb.append(getName());
        sb.append(newLine);

        sb.append(" Type: ");
        sb.append(getType());
        sb.append(newLine);

        sb.append(" AuthenticationType: ");
        sb.append(getAuthenticationType());
        sb.append(newLine);

        sb.append(" Shareable: ");
        sb.append(getShareable());
        sb.append(newLine);

        sb.append(" MappedName: ");
        sb.append(getMappedName());
        sb.append(newLine);

        sb.append(" Description: ");
        sb.append(getDescription());
        sb.append(newLine);

        sb.append("}");
        return sb.toString();
    }
}
