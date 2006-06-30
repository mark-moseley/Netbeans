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

package org.netbeans.modules.db.explorer.sql.editor;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.spi.sql.editor.SQLEditorProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class SQLEditorSupport {

    public static void openSQLEditor(DatabaseConnection dbconn, String sql, boolean execute) {
        SQLEditorProvider provider = (SQLEditorProvider)Lookup.getDefault().lookup(SQLEditorProvider.class);
        if (provider != null) {
            provider.openSQLEditor(dbconn, sql, execute);
        }
    }
}
