/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.io.*;
import java.lang.reflect.*;
import java.net.SocketException;
import java.sql.*;
import java.util.*;
import java.text.MessageFormat;

import org.openide.nodes.Node;
import org.openide.TopManager;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.dlg.SchemaDialog;
import org.netbeans.modules.db.explorer.dlg.UnsupportedDatabaseDialog;

public class ConnectionNodeInfo extends DatabaseNodeInfo implements ConnectionOperations {
    
    static final long serialVersionUID =-8322295510950137669L;

    public void connect(String dbsys) throws DatabaseException {
        String drvurl = getDriver();
        String dburl = getDatabase();
        String schema = null;
        
        try {
            // check if there is connected connection by Pointbase driver
            // Pointbase driver doesn't permit the concurrently connection
            if (drvurl.startsWith("com.pointbase.jdbc.jdbcUniversalDriver")) {
                Node n[] = getParent().getNode().getChildren().getNodes();
                for (int i = 0; i < n.length; i++)
                    if(n[i] instanceof ConnectionNode) {
                        ConnectionNodeInfo cinfo = (ConnectionNodeInfo)((ConnectionNode)n[i]).getInfo();
                        if(cinfo.getDriver().startsWith("com.pointbase.jdbc.jdbcUniversalDriver")) //NOI18N
                            if(!(cinfo.getDatabase().equals(dburl)&&cinfo.getUser().equals(getUser())))
                                if((cinfo.getConnection()!=null))
                                    throw new Exception(bundle.getString("EXC_PBConcurrentConn")); // NOI18N
                    }
            }

            Properties dbprops = getConnectionProperties();

            DatabaseConnection con = new DatabaseConnection(drvurl, dburl, getUser(), getPassword());
            Connection connection = con.createJDBCConnection();
            
            SpecificationFactory factory = (SpecificationFactory)getSpecificationFactory();
            Specification spec;
            DriverSpecification drvSpec;

            if (dbsys != null) {
                spec = (Specification)factory.createSpecification(con, dbsys, connection);
            } else {
                setReadOnly(false);
                spec = (Specification)factory.createSpecification(con, connection);
            }
            put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            if(getSchema()==null)
                throw new DatabaseException("Database schema is not set");
            
            setSpecification(spec);

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(connection.getCatalog());
            drvSpec.setSchema(getSchema());
            setDriverSpecification(drvSpec);

            setConnection(connection); // fires change
            
        } catch (DatabaseProductNotFoundException e) {

            UnsupportedDatabaseDialog dlg = new UnsupportedDatabaseDialog();
            dlg.show();
            setReadOnly(false);
            switch (dlg.getResult()) {
            case UnsupportedDatabaseDialog.GENERIC: connect("GenericDatabaseSystem"); break; //NOI18N
            case UnsupportedDatabaseDialog.READONLY: connectReadOnly(); break;
            default: return;
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void connect() throws DatabaseException {
        connect(null);
    }

    public void connectReadOnly() throws DatabaseException {
        setReadOnly(true);
        connect("GenericDatabaseSystem"); //NOI18N
    }

    public void disconnect() throws DatabaseException {
        Connection connection = getConnection();
        if (connection != null) {
            try {
                connection.close();
                setConnection(null); // fires change
            } catch (Exception exc) {
                // connection is broken, connection state has been changed
                setConnection(null); // fires change
                
                String message = MessageFormat.format(bundle.getString("EXC_ConnectionIsBroken"), new String[] {exc.getMessage()}); // NOI18N
                throw new DatabaseException(message);
            }
        }
    }

    public void delete() throws IOException {
        try {
            disconnect();
            Vector cons = RootNode.getOption().getConnections();
            DatabaseConnection cinfo = (DatabaseConnection)getDatabaseConnection();
            if (cons.contains(cinfo))
                cons.remove(cinfo);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public Object put(Object key, Object obj) {
        if (key.equals(USER) || key.equals(DRIVER) || key.equals(DATABASE) || key.equals(SCHEMA)) {
            String oldVal = (String)get(key);
            String newVal = (String)obj;
            updateConnection((String)key, oldVal, newVal);
        }
        return super.put(key, obj);
    }
    
    private void updateConnection(String key, String oldVal, String newVal) {
        DatabaseOption option = RootNode.getOption();
        Vector cons = option.getConnections();
        Enumeration enu = cons.elements();
        DBConnection infoConn = getDatabaseConnection();
        int idx = cons.indexOf(infoConn);
        if(idx>=0) {
            DatabaseConnection connFromList = (DatabaseConnection)cons.elementAt(idx);
            if (key.equals(SCHEMA))
                connFromList.setSchema(newVal);
            else if (key.equals(USER))
                connFromList.setUser(newVal);
            else if (key.equals(DRIVER)) {
                connFromList.setDriver(newVal);
            } else if (key.equals(DATABASE)) {
                connFromList.setDatabase(newVal);
            }
        }
        setName(infoConn.getName());
    }
    
}
