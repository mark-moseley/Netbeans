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

package org.netbeans.modules.db.explorer;

import java.util.Map;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import java.util.Arrays;
import java.util.TreeSet;
import java.sql.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.DatabaseModule;

public class DatabaseNodeChildren extends Children.Array
{
    protected Collection initCollection()
    {
        DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
        java.util.Map nodeord = (java.util.Map)nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);
        boolean sort = nodeinfo.getName().equals("Drivers") ? false : true; //NOI18N
        TreeSet children = new TreeSet(new NodeComparator(nodeord, sort));

        try {
            Vector chlist = nodeinfo.getChildren();
            
            //is there pointbase driver (is there pointbase installed)?
            boolean isPointbaseDriver;
            try {
                Class.forName("com.pointbase.jdbc.jdbcUniversalDriver"); //NOI18N
                isPointbaseDriver = true;
            } catch (ClassNotFoundException e) {
                isPointbaseDriver = false;
            }
            
            for (int i=0;i<chlist.size();i++) {
                Node snode = null;
                Object sinfo = chlist.elementAt(i);

                if (sinfo instanceof DatabaseNodeInfo) {
                    DatabaseNodeInfo dni = (DatabaseNodeInfo) sinfo;
                    if (dni.getName().equals("Connection")) //NOI18N
                        dni.setName(dni.getName() + " " + dni.getDatabase());

                    // aware! in this method is clone of instance dni created    
                    snode = createNode(dni);
                    
                    // if specific connection to pointbase is restored then this connection is opened
                    // and embedded pointbase driver is installed
                    if ( dni.getName().startsWith("Connection") //NOI18N
                        && dni.getDriver().equals("com.pointbase.jdbc.jdbcUniversalDriver") //NOI18N
                        && dni.getDatabase().equals("jdbc:pointbase://embedded/sample") //NOI18N
                        && dni.getUser().equals("public") //NOI18N
                        && isPointbaseDriver ) {

                            // node reference to ConnectionNodeInfo is set
                            final ConnectionNodeInfo cinfo = (ConnectionNodeInfo)((DatabaseNode)snode).getInfo();

                            // set password
                            cinfo.setPassword("public"); //NOI18N
                            cinfo.put(DatabaseNodeInfo.REMEMBER_PWD, new Boolean(true));

                            try {
                                cinfo.connect(ConnectionNodeInfo.AUTOPBCONN);
                            } catch(Exception ex) {}
                    }
                }
                else
                    if (sinfo instanceof Node)
                        snode = (Node)sinfo;
                if (snode != null)
                    children.add(snode);
            }
            
            /* if this database module is newly installed and embedded pointbase driver is installed
               then connection to pointbase is created (and opened) (as a son of Database node) */
            if (DatabaseModule.isNewlyInstalled && (nodeinfo.getName().startsWith("Databases")) && isPointbaseDriver) { //NOI18N

                ConnectionNodeInfo cni = (ConnectionNodeInfo)DatabaseNodeInfo.createNodeInfo(nodeinfo, DatabaseNode.CONNECTION);
                cni.setName( "jdbc:pointbase://embedded/sample" ); //NOI18N
                cni.setUser( "public" ); //NOI18N
                cni.setDriver( "com.pointbase.jdbc.jdbcUniversalDriver" ); //NOI18N
                cni.setDatabase( "jdbc:pointbase://embedded/sample" ); //NOI18N

                // create of node
                // aware! in this method is clone of instance cni created
                Node cnode = createNode(cni);

                // node reference to ConnectionNodeInfo is set
                ConnectionNodeInfo cinfo = (ConnectionNodeInfo)((DatabaseNode)cnode).getInfo();

                // set password
                cinfo.setPassword("public"); //NOI18N
                cinfo.put(DatabaseNodeInfo.REMEMBER_PWD, new Boolean(true));

                // adding connection to list (in DatabaseOption class)
                Vector cons = RootNode.getOption().getConnections();
                DatabaseConnection conn = (DatabaseConnection)cinfo.getDatabaseConnection();
                cons.add(conn);

                // open connection
                try {
                    cinfo.connect(ConnectionNodeInfo.AUTOPBCONN);
                } catch(Exception ex) {
                }

                // adding node into children of Database node
                children.add(cnode);

            }
        } catch (Exception e) {
            e.printStackTrace();
            children.clear();
        }

        return children;
    }
    /*
    	protected Node[] createNodes()
    	{
    		Node[] nodeorg = super.createNodes();
    		DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
    		java.util.Map nodeord = (java.util.Map)nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);
    		if (nodeord != null) Arrays.sort(nodeorg, new NodeComparator(nodeord));
    		return nodeorg;
    	}
    */
    class NodeComparator implements Comparator
    {
        private java.util.Map map = null;
        private boolean sort;

        public NodeComparator(java.util.Map map, boolean sort)
        {
            this.map = map;
            this.sort = sort;
        }

        public int compare(Object o1, Object o2)
        {
            if (! sort)
                return 1;
            
            int o1val, o2val, diff;
            Integer o1i = (Integer)map.get(o1.getClass().getName());
            if (o1i != null) o1val = o1i.intValue();
            else o1val = Integer.MAX_VALUE;
            Integer o2i = (Integer)map.get(o2.getClass().getName());
            if (o2i != null) o2val = o2i.intValue();
            else o2val = Integer.MAX_VALUE;

            diff = o1val-o2val;
            if (diff == 0) return ((DatabaseNode)o1).getInfo().getName().compareTo(((DatabaseNode)o2).getInfo().getName());
            return diff;
        }
    }

    public DatabaseNode createNode(DatabaseNodeInfo info)
    {
        String ncode = (String)info.get(DatabaseNodeInfo.CODE);
        String nclass = (String)info.get(DatabaseNodeInfo.CLASS);
        DatabaseNode node = null;

        try {
            node = (DatabaseNode)Class.forName(nclass).newInstance();
            node.setInfo(info); /* makes a copy of info, use node.getInfo() to access it */
            node.getInfo().setNode(node); /* this is a weak, be cool, baby ;) */
        } catch (Exception e) {
            e.printStackTrace();
        }

        return node;
    }

    public DatabaseNode createSubnode(DatabaseNodeInfo info, boolean addToChildrenFlag)
    throws DatabaseException
    {
        DatabaseNode subnode = createNode(info);
        if (subnode != null && addToChildrenFlag) {
            DatabaseNodeInfo ninfo = ((DatabaseNode)getNode()).getInfo();
            ninfo.getChildren().add(info);
            if (isInitialized()) add(new Node[] {subnode});
        }

        return subnode;
    }
}
