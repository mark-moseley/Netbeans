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

import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class IdImpl implements Id {

    private final String name;
    private final GeneratedValue generatedValue;
    private final Column column;
    private final String temporal;

    public IdImpl(String name, GeneratedValue generatedValue, Column column, String temporal) {
        this.name = name;
        this.generatedValue = generatedValue;
        this.column = column;
        this.temporal = temporal;
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return name;
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

    public void setGeneratedValue(GeneratedValue value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public GeneratedValue getGeneratedValue() {
        return generatedValue;
    }

    public GeneratedValue newGeneratedValue() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTemporal(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getTemporal() {
        return temporal;
    }

    public void setTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator getTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator newTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator getSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator newSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
