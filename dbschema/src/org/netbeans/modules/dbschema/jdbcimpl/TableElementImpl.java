/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.*;

public class TableElementImpl extends DBElementImpl implements TableElement.Impl {

    private DBElementsCollection columns;
    private DBElementsCollection indexes;
    private DBElementsCollection keys;
    transient private DBElementsCollection columnPairs;
    
    private String table;
    
    private boolean isTable;

    public TableElementImpl() {
		this(null);
    }

    /** Creates new TableElementImpl */
    public TableElementImpl(String table) {
		super(table);
		columns = new DBElementsCollection(this, new ColumnElement[0]);
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        Object hc = String.valueOf(columns.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		columns = new DBElementsCollection(this, new ColumnElement[0]);
            hc = String.valueOf(columns.hashCode());
        }
        DBElementsCollection.instances.add(hc);
        
		indexes = new DBElementsCollection(this, new IndexElement[0]);
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        hc = String.valueOf(indexes.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		indexes = new DBElementsCollection(this, new IndexElement[0]);
            hc = String.valueOf(indexes.hashCode());
        }
        DBElementsCollection.instances.add(hc);
        
		keys = new DBElementsCollection(this, new KeyElement[0]);
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        hc = String.valueOf(keys.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		keys = new DBElementsCollection(this, new KeyElement[0]);
            hc = String.valueOf(keys.hashCode());
        }
        DBElementsCollection.instances.add(hc);
        
		columnPairs = new DBElementsCollection(this, new ColumnPairElement[0]);
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        hc = String.valueOf(columnPairs.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		columnPairs = new DBElementsCollection(this, new ColumnPairElement[0]);
            hc = String.valueOf(columnPairs.hashCode());
        }
        DBElementsCollection.instances.add(hc);
        
		this.table = table;
    }
    
    /** Get the name of this element.
    * @return the name
    */
    public DBIdentifier getName() {
        if (_name.getFullName() == null)
            _name.setFullName(((TableElement) element).getDeclaringSchema().getName().getFullName() + "." + _name.getName());

        return _name;
    }
    
    /** Set whether this is really a table, or a view.
     * @param isTable one of {@link #TABLE} or {@link #VIEW}
     * @throws DBException if impossible
     */
    public void setTableOrView(boolean isTable) throws DBException {
        this.isTable = isTable;
    }
  
    /** Test whether this is really a class, or an interface.
     * @return one of {@link #TABLE} or {@link #VIEW}
     */
    public boolean isTableOrView() {
        return isTable;
    }
  
    /** Change the set of columns.
     * @param elems the columns to change
     * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception DBException if the action cannot be handled
     */
    public void changeColumns(ColumnElement[] elems,int action) throws DBException {
        columns.changeElements(elems, action);
    }
  
    /** Get all columns.
     * @return the columns
     */
    public ColumnElement[] getColumns() {
        DBElement[] dbe = columns.getElements();
        return (ColumnElement[]) Arrays.asList(dbe).toArray(new ColumnElement[dbe.length]);
    }
  
    /** Find a column by name.
     * @param name the name for which to look
     * @return the column, or <code>null</code> if it does not exist
    */
    public ColumnElement getColumn(DBIdentifier name) {
		return (ColumnElement) columns.find(name);
    }
  
    protected void initColumns(ConnectionProvider cp) {
        if (cp != null)
            try {
				DatabaseMetaData dmd = cp.getDatabaseMetaData();
				String shortTableName = getName().getName();

                DDLBridge bridge = null;
                if (IDEUtil.isIDERunning())
                    bridge = new DDLBridge(cp.getConnection(), cp.getSchema(), dmd);
                
                ResultSet rs;
                if (bridge != null) {
                    bridge.getDriverSpecification().getColumns(shortTableName, "%");
                    rs = bridge.getDriverSpecification().getResultSet();
                } else
//                    rs = dmd.getColumns(cp.getConnection().getCatalog(), dmd.getUserName().trim(), shortTableName, "%");
                    rs = dmd.getColumns(cp.getConnection().getCatalog(), cp.getSchema(), shortTableName, "%");
                
                int sqlType;
                String sqlTypeName;
                String colName, colNull, colSize, colDec;
                if (rs != null) {
                    HashMap rset = new HashMap();
                    while (rs.next()) {
                        if (bridge != null) {
                            rset = bridge.getDriverSpecification().getRow();
                            Object type = rset.get(new Integer(5));
                            if (type != null)
                                sqlType = (new Integer((String) rset.get(new Integer(5)))).intValue();
                            else
                                sqlType = 0; //java.sql.Types.NULL
                            sqlTypeName = (String) rset.get(new Integer(6));
                            colName = (String) rset.get(new Integer(4));
                            colNull = (String) rset.get(new Integer(11));
                            colSize = (String) rset.get(new Integer(7));
                            colDec = (String) rset.get(new Integer(9));
                            rset.clear();
                        } else {
                            sqlType = rs.getInt("DATA_TYPE"); //NOI18N
                            sqlTypeName = rs.getString("TYPE_NAME").trim(); //NOI18N
                            colName = rs.getString("COLUMN_NAME").trim(); //NOI18N
                            colNull = Integer.toString(rs.getInt("NULLABLE")); //NOI18N
                            colSize = rs.getString("COLUMN_SIZE"); //NOI18N
                            colDec = rs.getString("DECIMAL_DIGITS"); //NOI18N
                        }

                        //Oracle driver hacks
                        if (dmd.getDatabaseProductName().trim().indexOf("Oracle") != -1) { //NOI18N
                            if (sqlType == 11 || ((sqlType == 1111) && sqlTypeName.startsWith("TIMESTAMP")))
                                sqlType = Types.TIMESTAMP;
                            if ((sqlType == 1111) && sqlTypeName.equals("FLOAT")) //NOI18N
                                sqlType = Types.DOUBLE;
                            if ((sqlType == 1111) && sqlTypeName.equals("BLOB")) //NOI18N
                                sqlType = Types.BLOB;
                            if ((sqlType == 1111) && sqlTypeName.equals("CLOB")) //NOI18N
                                sqlType = Types.CLOB;
                        }
                        
                        //workaround for i-net Oranxo driver
                        //value in int range is expected by JDBC API but 4294967296 is returned
                        try {
                            new Integer(colSize);
                        } catch (NumberFormatException exc) {
                            colSize = Integer.toString(Integer.MAX_VALUE);
                        }
                        
                        ColumnElementImpl cei = new ColumnElementImpl(colName, Integer.toString(sqlType), colNull, colSize, colDec);
                        ColumnElement ce = new ColumnElement(cei, (TableElement) element);
                        ColumnElement[] c = {ce};
                        changeColumns(c, DBElement.Impl.ADD);
                    }
                    rs.close();
                }
            } catch (Exception exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    exc.printStackTrace();
            }
    }
  
    /** Change the set of indexes.
     * @param elems the indexes to change
     * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception DBException if the action cannot be handled
     */
    public void changeIndexes(IndexElement[] elems,int action) throws DBException {
        indexes.changeElements(elems, action);
    }
  
    /** Get all indexes.
     * @return the indexes
     */
    public IndexElement[] getIndexes() {
        DBElement[] dbe = indexes.getElements();        
        return (IndexElement[]) Arrays.asList(dbe).toArray(new IndexElement[dbe.length]);
    }
  
    /** Find an index by name.
     * @param name the name for which to look
     * @return the index, or <code>null</code> if it does not exist
     */
    public IndexElement getIndex(DBIdentifier name) {
		return (IndexElement) indexes.find(name);
    }
  
    protected void initIndexes(ConnectionProvider cp) {
        initIndexes(cp, null);
    }
    
    protected void initIndexes(ConnectionProvider cp, String tbl) {
        if (cp != null)
            try {
                boolean unique;
                DatabaseMetaData dmd = cp.getDatabaseMetaData();
				String shortTableName;
                if (tbl == null)
                    shortTableName = getName().getName();
                else
                    shortTableName = tbl;
                
                DDLBridge bridge = null;
                if (IDEUtil.isIDERunning())
                    bridge = new DDLBridge(cp.getConnection(), cp.getSchema(), dmd);
                
                ResultSet rs;
                if (bridge != null) {
                    bridge.getDriverSpecification().getIndexInfo(shortTableName, false, false);
                    rs = bridge.getDriverSpecification().getResultSet();
                } else
//                    rs = dmd.getIndexInfo(cp.getConnection().getCatalog(), dmd.getUserName().trim(), shortTableName, false, false);
                    rs = dmd.getIndexInfo(cp.getConnection().getCatalog(), cp.getSchema(), shortTableName, false, false);
                
                String name, columnName;
                boolean unq;
                LinkedList idxs = new LinkedList();
                if (rs != null) {
                    HashMap rset = new HashMap();
                    String uniqueStr;
                    while (rs.next()) {
                        if (bridge != null) {
                            rset = bridge.getDriverSpecification().getRow();
                            name = (String) rset.get(new Integer(6));
                            columnName = (String) rset.get(new Integer(9));
                            uniqueStr = (String) rset.get(new Integer(4));
                            if (uniqueStr == null || uniqueStr.equals("0") || uniqueStr.equalsIgnoreCase("false"))
                                unq = false;
                            else
                                unq = true;
                            rset.clear();
                        } else {
                            name = rs.getString("INDEX_NAME"); //NOI18N
                            columnName = rs.getString("COLUMN_NAME"); //NOI18N
                            if (columnName != null)
                                columnName = columnName.trim();
                            unq = rs.getBoolean("NON_UNIQUE"); //NOI18N
                        }
                        
                        if (name == null)
                            continue;
                        else
                            name = name.trim();

                        if (unq)
                            idxs.add(name + "." + columnName + ".false"); //NOI18N
                        else
                            idxs.add(name + "." + columnName + ".true"); //NOI18N
                    }
                    rs.close();
                }
                
                String info;
                int start, end;
                for (int i = 0; i < idxs.size(); i++) {
                    info = idxs.get(i).toString();
                    start = info.indexOf('.'); //NOI18N
                    end = info.lastIndexOf('.'); //NOI18N
                    name = info.substring(0, start);
                    if ((info.substring(end + 1)).equals("true")) //NOI18N
                        unique = true;
                    else
                        unique = false;
                    
                    if (indexes.find(DBIdentifier.create(name)) != null)
                        continue;
                    
                    IndexElementImpl iei = new IndexElementImpl(this, name, unique);
                    IndexElement[] ie = {new IndexElement(iei, (TableElement) element)};
                    iei.initColumns(idxs);
                    changeIndexes(ie, DBElement.Impl.ADD);
                }
            } catch (Exception exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    exc.printStackTrace();
            }
    }
      
    /** Change the set of keys.
     * @param elems the keys to change
     * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception DBException if the action cannot be handled
     */
    public void changeKeys(KeyElement[] elems,int action) throws DBException {
        keys.changeElements(elems, action);
    }
  
    /** Get all keys.
     * @return the keys
     */
    public KeyElement[] getKeys() {
        DBElement[] dbe = keys.getElements();
        return (KeyElement[]) Arrays.asList(dbe).toArray(new KeyElement[dbe.length]);
    }
  
    /** Find a key by name.
     * @param name the name for which to look
     * @return the key, or <code>null</code> if it does not exist
     */
    public KeyElement getKey(DBIdentifier name) {
		return (KeyElement) keys.find(name);
    }

    
    protected void initKeys(ConnectionProvider cp) {
        initKeys(cp, 0);
    }
        
    protected void initKeys(ConnectionProvider cp, int id) {
        // id == 1 ... capture PK only
        // id == 2 ... capture FKs only
        initKeys(cp, id, null);
    }
    
    protected void initKeys(ConnectionProvider cp, int id, String tbl) {
        // id == 1 ... capture PK only
        // id == 2 ... capture FKs only
        
        if (cp != null)
            try {
                String shortTableName;
                if (tbl == null)
                    shortTableName = getName().getName();
                else
                    shortTableName = tbl;
                
                DDLBridge bridge = null;
                if (IDEUtil.isIDERunning())
                    bridge = new DDLBridge(cp.getConnection(), cp.getSchema(), cp.getDatabaseMetaData());
                
                if (id != 1)
                    initFKs(cp, bridge, shortTableName);
                if (id != 2)
                    initPK(cp, bridge, shortTableName);
            } catch (Exception exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    exc.printStackTrace();
            }
    }
    
    private void initFKs(ConnectionProvider cp, DDLBridge bridge, String shortTableName) throws SQLException, DBException {
        ResultSet rs;
        
        if (bridge != null) {
            bridge.getDriverSpecification().getImportedKeys(shortTableName);
            rs = bridge.getDriverSpecification().getResultSet();
        } else
            rs = cp.getDatabaseMetaData().getImportedKeys(cp.getConnection().getCatalog(), cp.getSchema(), shortTableName);

        String name, fkColName, pkTableName, pkColName, c1, c2, s1, s2;
        if (rs != null) {
            HashMap rset = new HashMap();
            while (rs.next()) {
                if (bridge != null) {
                    rset = bridge.getDriverSpecification().getRow();
                    
                    //test references between two schemas
                    c1 = (String) rset.get(new Integer(1));
                    s1 = (String) rset.get(new Integer(2));
                    c2 = (String) rset.get(new Integer(5));
                    s2 = (String) rset.get(new Integer(6));                    
                            
                    name = (String) rset.get(new Integer(12));
                    fkColName = (String) rset.get(new Integer(8));
                    pkTableName = (String) rset.get(new Integer(3));
                    pkColName = (String) rset.get(new Integer(4));
                    rset.clear();
                } else {
                    //test references between two schemas
                    c1 = rs.getString("PKTABLE_CAT"); //NOI18N
                    s1 = rs.getString("PKTABLE_SCHEM"); //NOI18N
                    c2 = rs.getString("FKTABLE_CAT"); //NOI18N
                    s2 = rs.getString("FKTABLE_SCHEM"); //NOI18N

                    name = rs.getString("FK_NAME"); //NOI18N
                    fkColName = rs.getString("FKCOLUMN_NAME").trim(); //NOI18N
                    pkTableName = rs.getString("PKTABLE_NAME").trim(); //NOI18N
                    pkColName = rs.getString("PKCOLUMN_NAME").trim(); //NOI18N
                }
                
                if (comp(c1, c2)) {
                    if (! comp(s1, s2))
                        continue;
                } else
                    continue;                            

                ColumnPairElement cpe;

                if (name == null || name.trim().equals(""))
                    name = "GENERATED_FK_" + pkTableName;
                else
                    name = name.trim();

                ColumnElement lce = getColumn(DBIdentifier.create(fkColName)); //NOI18N
                if (lce == null) //should be null only in same cases when FK is computed for view
                    continue;
                
                SchemaElement se = ((TableElement) element).getDeclaringSchema();
                TableElement fte = se.getTable(DBIdentifier.create(pkTableName));
                ColumnElement fce = fte.getColumn(DBIdentifier.create(pkColName));
                ColumnPairElementImpl cpei = new ColumnPairElementImpl(lce.getName().getFullName() + ";" + fce.getName().getFullName()); //NOI18N
                cpe = new ColumnPairElement(cpei, lce, fce, (TableElement) element);
                changeColumnPairs(new ColumnPairElement[] {cpe}, DBElement.Impl.ADD);

                ForeignKeyElement fk = (ForeignKeyElement) keys.find(DBIdentifier.create(name));
                if (fk != null)
                    fk.addColumnPair(cpe); //add pair
                else {
                    ForeignKeyElementImpl fkei = new ForeignKeyElementImpl(this, name);
                    ForeignKeyElement fke = new ForeignKeyElement(fkei, (TableElement) element);
                    fke.addColumnPair(cpe);
                    changeKeys(new ForeignKeyElement[] {fke}, DBElement.Impl.ADD);
                }
            }
            rs.close();
        }
    }
    
    private void initPK(ConnectionProvider cp, DDLBridge bridge, String shortTableName) throws SQLException, DBException {
        ResultSet rs;
        
        IndexElement[] iearr = getIndexes();
        if (iearr != null) {
            for (int i = 0; i < iearr.length; i++)
                if (iearr[i].isUnique()) {
                    UniqueKeyElementImpl ukei = new UniqueKeyElementImpl(iearr[i].getName().getName(), false); //false = not primary key (primary flag is setted later)
                    UniqueKeyElement uke = new UniqueKeyElement(ukei, (TableElement) element, iearr[i]);
                    uke.setColumns(iearr[i].getColumns());
                    changeKeys(new UniqueKeyElement[]{uke}, DBElement.Impl.ADD);
               }

            UniqueKeyElement[] ukes = ((TableElement) element).getUniqueKeys();
            if (ukes != null && ukes.length > 0) {
                if (bridge != null) {
                    bridge.getDriverSpecification().getPrimaryKeys(shortTableName);
                    rs = bridge.getDriverSpecification().getResultSet();
                } else
                    rs = cp.getDatabaseMetaData().getPrimaryKeys(cp.getConnection().getCatalog(), cp.getSchema(), shortTableName);

                TreeMap cols = new TreeMap();
                Object keySeq;
                String colName;
                if (rs != null) {
                    HashMap rset = new HashMap();
                    while (rs.next()) {
                        if (bridge != null) {
                            rset = bridge.getDriverSpecification().getRow();
                            keySeq = (Object) rset.get(new Integer(5));
                            colName = (String) rset.get(new Integer(4));
                            rset.clear();
                        } else {
                            keySeq = rs.getObject("KEY_SEQ"); //NOI18N
                            colName = rs.getString("COLUMN_NAME").trim(); //NOI18N
                        }

                        cols.put(keySeq, colName); //NOI18N
                    }
                    rs.close();
                }

                boolean primary = false;
                if (cols != null && cols.size() > 0)
                    primary = true;

                if (primary)
                    if (ukes.length == 1)
                        ukes[0].setPrimaryKey(primary);
                    else {
                        ColumnElement[] ces;
                        Object[] o = cols.values().toArray();
                        boolean equals;
                        for (int i = 0; i < ukes.length; i++) {
                            ces = ukes[i].getColumns();
                            if (ces.length != o.length)
                                continue;
                            else {
                                equals = true;
                                for (int j = 0; j < ces.length; j++)
                                    if (! o[j].toString().equals(ces[j].getName().getName())) {
                                        equals = false;
                                        break;
                                    }
                                if (equals) {
                                    ukes[i].setPrimaryKey(primary);
                                    break;
                                }
                            }
                        }
                    }
            }
        }
    }

    public ColumnPairElement[] getColumnPairs() {
        DBElement[] dbe = columnPairs.getElements();
        return (ColumnPairElement[]) Arrays.asList(dbe).toArray(new ColumnPairElement[dbe.length]);
    }
    
    public ColumnPairElement getColumnPair(DBIdentifier name) {
        ColumnPairElement cpe = (ColumnPairElement) columnPairs.find(name);
        if (cpe == null)
            try {
                String fullName = name.getFullName();
                if (fullName == null) {
                    return null;
                }

                int pos = fullName.indexOf(";");
                String firstHalf = fullName.substring(0, pos);
                String secondHalf = fullName.substring(pos + 1);

                ColumnElement lce = getColumn(DBIdentifier.create(firstHalf));
                
                pos = secondHalf.lastIndexOf(".");
                TableElement te = ((TableElement) element).getDeclaringSchema().getTable(DBIdentifier.create(secondHalf.substring(0, pos)));
                if (te == null)
                    return null;
                
                ColumnElement fce = te.getColumn(DBIdentifier.create(secondHalf));
                
                if (lce == null || fce == null)
                    return null;

                ColumnPairElementImpl cpei = new ColumnPairElementImpl(lce.getName().getFullName() + ";" + fce.getName().getFullName()); //NOI18N
                cpe = new ColumnPairElement(cpei, lce, fce, (TableElement) element);
                changeColumnPairs(new ColumnPairElement[] {cpe}, DBElement.Impl.ADD);
            } catch (DBException exc) {
                exc.printStackTrace();
                return null;
            }
        
		return cpe;
    }
    
    public void changeColumnPairs(ColumnPairElement[] pairs,int action) throws DBException {
        columnPairs.changeElements(pairs, action);
    }

    
	//=============== extra methods needed for xml archiver ==============

	/** Returns the column collection of this table element.  This method
	 * should only be used internally and for cloning and archiving.
	 * @return the column collection of this table element
	 */
	public DBElementsCollection getColumnCollection () {
		return columns;
	}

	/** Set the column collection of this table element to the supplied
	 * collection.  This method should only be used internally and for
	 * cloning and archiving.
	 * @param collection the column collection of this table element
	 */
	public void setColumnCollection (DBElementsCollection collection) {
		columns = collection;
	}
        
	/** Returns the index collection of this table element.  This method
	 * should only be used internally and for cloning and archiving.
	 * @return the index collection of this table element
	 */
	public DBElementsCollection getIndexCollection () {
		return indexes;
	}

	/** Set the indwx collection of this table element to the supplied
	 * collection.  This method should only be used internally and for
	 * cloning and archiving.
	 * @param collection the index collection of this table element
	 */
	public void setIndexCollection (DBElementsCollection collection) {
		indexes = collection;
	}
        
	/** Returns the key collection of this table element.  This method
	 * should only be used internally and for cloning and archiving.
	 * @return the key collection of this table element
	 */
	public DBElementsCollection getKeyCollection () {
		return keys;
	}

	/** Set the key collection of this table element to the supplied
	 * collection.  This method should only be used internally and for
	 * cloning and archiving.
	 * @param collection the key collection of this table element
	 */
	public void setKeyCollection (DBElementsCollection collection) {
		keys = collection;
	}
    
}
