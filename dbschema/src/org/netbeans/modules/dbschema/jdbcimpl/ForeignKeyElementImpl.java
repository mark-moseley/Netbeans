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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.Arrays;

import org.netbeans.modules.dbschema.*;

public class ForeignKeyElementImpl  extends KeyElementImpl implements ForeignKeyElement.Impl {

    private TableElementImpl tei;

    public ForeignKeyElementImpl() {
		this(null, null);
    }

    public ForeignKeyElementImpl(TableElementImpl tei, String name) {
		super(name);

        this.tei = tei;
    }

    protected DBElementsCollection initializeCollection() {
        return new DBElementsCollection(this, new ColumnPairElement[0]);
    }
  
    public ColumnPairElement[] getColumnPairs() {
        DBElement[] dbe = getColumnCollection().getElements();
        return (ColumnPairElement[]) Arrays.asList(dbe).toArray(new ColumnPairElement[dbe.length]);
    }
    
    public ColumnPairElement getColumnPair(DBIdentifier name) {
		return (ColumnPairElement) getColumnCollection().find(name);
    }
    
    public void changeColumnPairs(ColumnPairElement[] pairs,int action) throws DBException {
        getColumnCollection().changeElements(pairs, action);
    }
    
    public ColumnElement[] getColumns() {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        ColumnElement[] ce = new ColumnElement[cpe.length];
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            ce[i] = ((ForeignKeyElement) element).getDeclaringTable().getColumn(DBIdentifier.create(localColumn));
        }
        
        return ce;
    }
    
    public ColumnElement getColumn(DBIdentifier name) {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            if (name.getName().equals(DBIdentifier.create(localColumn).getName())) //need to check
                return ((ForeignKeyElement) element).getDeclaringTable().getColumn(name);
        }
        
        return null;
    }
    
}
