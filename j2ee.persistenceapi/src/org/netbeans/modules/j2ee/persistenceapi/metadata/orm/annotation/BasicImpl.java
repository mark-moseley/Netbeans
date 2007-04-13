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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import javax.lang.model.element.AnnotationMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class BasicImpl implements Basic {

    private final String name;
    private final Column column;
    private final ParseResult parseResult;

    public BasicImpl(AnnotationModelHelper helper, AnnotationMirror annotation, String name, Column column) {
        this.name = name;
        this.column = column;
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectPrimitive("optional", Boolean.class, parser.defaultValue(true));
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return name;
    }

    public void setFetch(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getFetch() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setOptional(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isOptional() {
        return parseResult.get("optional", Boolean.class); // NOI18N
    }

    public void setColumn(Column value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Column getColumn() {
        return column;
    }

    public Column newColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setLob(Lob value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Lob getLob() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Lob newLob() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTemporal(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getTemporal() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEnumerated(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getEnumerated() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
