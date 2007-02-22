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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface Id {
    
    public void setName(String value);
    
    public String getName();
    
    public void setColumn(Column value);
    
    public Column getColumn();
    
    public Column newColumn();
    
    public void setGeneratedValue(GeneratedValue value);
    
    public GeneratedValue getGeneratedValue();
    
    public GeneratedValue newGeneratedValue();
    
    public void setTemporal(String value);
    
    public String getTemporal();
    
    public void setTableGenerator(TableGenerator value);
    
    public TableGenerator getTableGenerator();
    
    public TableGenerator newTableGenerator();
    
    public void setSequenceGenerator(SequenceGenerator value);
    
    public SequenceGenerator getSequenceGenerator();
    
    public SequenceGenerator newSequenceGenerator();
    
}
