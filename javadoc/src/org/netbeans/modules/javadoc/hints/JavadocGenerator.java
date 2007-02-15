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

package org.netbeans.modules.javadoc.hints;

import com.sun.javadoc.Doc;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Jan Pokorsky
 */
public final class JavadocGenerator {
    
    private final SourceVersion srcVersion;
    
    /** Creates a new instance of JavadocGenerator */
    public JavadocGenerator(SourceVersion version) {
        this.srcVersion = version;
    }
    
    public String generateComment(TypeElement clazz, CompilationInfo javac) {
        StringBuilder builder = new StringBuilder(
                "/**\n" + // NOI18N
                " * \n" // NOI18N
                );
        
        if (clazz.getNestingKind() == NestingKind.TOP_LEVEL) {
            builder.append(" * @author " + System.getProperty("user.name") + "\n"); // NOI18N
        }
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0) {
            for (TypeParameterElement param : clazz.getTypeParameters()) {
                builder.append(" * @param " + param.getSimpleName().toString() + " \n"); // NOI18N
            }
        }
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0 &&
                JavadocUtilities.isDeprecated(javac, clazz)) {
            builder.append(" * @deprecated\n"); // NOI18N
        }
        
        builder.append(" */\n"); // NOI18N

        return builder.toString();
    }
    
    public String generateComment(ExecutableElement method, CompilationInfo javac) {
        StringBuilder builder = new StringBuilder(
                "/**\n" + // NOI18N
                " * \n" // NOI18N
                );
        
        for (VariableElement param : method.getParameters()) {
            builder.append(" * @param ").append(param.getSimpleName().toString()).append(" \n"); // NOI18N
        }
        
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            builder.append(" * @return \n"); // NOI18N
        }
        
        for (TypeMirror exceptionType : method.getThrownTypes()) {
            TypeElement exception = (TypeElement) ((DeclaredType) exceptionType).asElement();
            builder.append(" * @throws ").append(exception.getQualifiedName().toString()).append(" \n"); // NOI18N
        }
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0 &&
                JavadocUtilities.isDeprecated(javac, method)) {
            builder.append(" * @deprecated\n"); // NOI18N
        }

        builder.append(" */\n"); // NOI18N
        
        return builder.toString();
    }
    
    public String generateComment(VariableElement field, CompilationInfo javac) {
        StringBuilder builder = new StringBuilder(
                "/**\n" + // NOI18N
                " * \n" // NOI18N
                );
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0 &&
                JavadocUtilities.isDeprecated(javac, field)) {
            builder.append(" * @deprecated\n"); // NOI18N
        }
        

        builder.append(" */\n"); // NOI18N
        
        return builder.toString();
    }
    
    public String generateComment(Element elm, CompilationInfo javac) {
        switch(elm.getKind()) {
            case CLASS:
            case ENUM:
            case INTERFACE:
            case ANNOTATION_TYPE:
                return generateComment((TypeElement) elm, javac);
            case CONSTRUCTOR:
            case METHOD:
                return generateComment((ExecutableElement) elm, javac);
            case FIELD:
            case ENUM_CONSTANT:
                return generateComment((VariableElement) elm, javac);
            default:
                throw new UnsupportedOperationException(elm.getKind() +
                        ", " + elm.getClass() + ": " + elm.toString()); // NOI18N
        }
    }
    
    public String generateInheritComment() {
        return "/** {@inheritDoc} */"; //NOI18N
    }
    
    public static String indentJavadoc(String jdoc, String tab) {
        return jdoc.replace("\n *", "\n" + tab + " *") + tab; // NOI18N
    }
    
    public static String guessIndentation(Document doc, Position pos) throws BadLocationException {
        String content = doc.getText(0, doc.getLength());
        int offset;
        for (offset = pos.getOffset(); offset >= 0 && content.charAt(offset) != '\n'; offset--);
        return content.substring(offset + 1, pos.getOffset());
    }
    
    public static String guessJavadocIndentation(CompilationInfo javac, Document doc, Doc jdoc) throws BadLocationException {
        Position[] jdBounds = JavadocUtilities.findDocBounds(javac, doc, jdoc);
        if (jdBounds == null) {
            return ""; // NOI18N
        }
        
        String txt = doc.getText(0, doc.getLength());
        int count = 0;
        for (int offset = jdBounds[0].getOffset() - 1; offset >= 0; offset--) {
            char c = txt.charAt(offset);
            if (c == '\n' || !Character.isWhitespace(c)) {
                count = jdBounds[0].getOffset() - offset;
                break;
            }
        }
        
        char[] indent = new char[count];
        for (int i = 0; i < indent.length; i++) {
            indent[i] = ' ';
        }

        return String.valueOf(indent);
    }
    
}
