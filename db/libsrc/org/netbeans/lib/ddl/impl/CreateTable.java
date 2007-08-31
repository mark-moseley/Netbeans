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

package org.netbeans.lib.ddl.impl;

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*
* @author Slavek Psenicka
*/
public class CreateTable extends ColumnListCommand
{
    static final long serialVersionUID =-6731725400393279232L;
    
    public CreateTable() {
        setNewObject(true);
    }
    
    public TableColumn createColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(TableColumn.COLUMN, name, 
            Specification.CREATE_TABLE, true, true);
    }

    public TableColumn createColumn(String type, String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(type, name, Specification.CREATE_TABLE, true, true);
    }

    public TableColumn createUniqueColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.UNIQUE, name, 
            Specification.CREATE_TABLE, true, true);
        col.setObjectName(name+"_UQ"); // NOI18N
        return col;
    }

    public TableColumn createPrimaryKeyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY, name, 
            Specification.CREATE_TABLE, true, true);
        col.setObjectName(name+"_PK"); // NOI18N
        return col;
    }

    public TableColumn createForeignKeyColumn(String name, String rtablename, String rcolumnname)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.FOREIGN_KEY, name, 
            Specification.CREATE_TABLE, true, true);
        col.setObjectName(name+"_FK"); // NOI18N
        col.setReferencedTableName(rtablename);
        col.setReferencedColumnName(rcolumnname);
        return col;
    }

    public TableColumn createCheckColumn(String name, String expression)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.CHECK, name, 
            Specification.CREATE_TABLE, true, true);
        col.setObjectName(name+"_CH"); // NOI18N
        col.setCheckCondition(expression);
        return col;
    }

    public TableColumn createUniqueConstraint(String columnname)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.UNIQUE_CONSTRAINT, columnname, 
            Specification.CREATE_TABLE, true, true);
        col.setObjectName(columnname+"_UQ"); // NOI18N
        return col;
    }

    public TableColumn createCheckConstraint(String name, String expression)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.CHECK_CONSTRAINT, name, 
            Specification.CREATE_TABLE, true, true);
        col.setObjectName(name+"_CH"); // NOI18N
        col.setCheckCondition(expression);
        return col;
    }

    public TableColumn createPrimaryKeyConstraint(String columnname)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY_CONSTRAINT, 
            columnname, Specification.CREATE_TABLE, true, true);
        col.setObjectName(columnname+"_PK"); // NOI18N
        return col;
    }

    public TableColumn createForeignKeyConstraint(String columnname, String rtablename, String rcolumnname)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.FOREIGN_KEY_CONSTRAINT, 
            columnname, Specification.CREATE_TABLE, true, true);
        col.setObjectName(columnname+"_FK"); // NOI18N
        col.setReferencedTableName(rtablename);
        col.setReferencedColumnName(rcolumnname);
        return col;
    }
}

/*
* <<Log>>
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/10/99  Slavek Psenicka 
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
