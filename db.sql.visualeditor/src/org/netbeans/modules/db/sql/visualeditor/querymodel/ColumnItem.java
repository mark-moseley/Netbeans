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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL Set function (AVG, COUNT, MAX, MIN, SUM)
 * Example Form: SUM(Orders.Quantity), MAX(Employee.Salary), COUNT(Employee.Name)
 */

public abstract class ColumnItem implements Value {

    abstract Column getReferencedColumn();

    public String genText(SQLIdentifiers.Quoter quoter, boolean select) {
        return genText(quoter);
    }
}

