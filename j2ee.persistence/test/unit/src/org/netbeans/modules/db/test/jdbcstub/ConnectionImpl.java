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

package org.netbeans.modules.db.test.jdbcstub;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.netbeans.modules.j2ee.persistence.editor.completion.*;
import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionImpl extends StubDelegate {
    
    private DatabaseMetaData metaData;
    
    public ConnectionImpl(DatabaseMetaData metaData) {
        this.metaData = metaData;
    }
    
    public String getCatalog() {
        return null;
    }

    public DatabaseMetaData getMetaData() {
        return metaData;
    }
}
