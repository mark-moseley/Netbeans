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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.nodes.RootNode;

//import org.openide.util.Mutex;

// XXX This entire class is junk. Should have a sensible data model independent of
// nodes and display it using Children.Keys (or Looks) and everything would be
// much easier. -jglick

// I totally agree. It was planed to redesign the module and base it on a data model
// unfortunately this project was cancelled. Radko

public class DatabaseNodeChildren extends Children.Array {

    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    private TreeSet children;
    private transient PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private static Object sync = new Object(); // synchronizing object

    private PropertyChangeListener listener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals("finished")) { //NOI18N
//                Mutex.EVENT.writeAccess(new Runnable() {
//                    public void run() {
                remove(getNodes()); //remove wait node
                nodes = getCh(); // change children ...
                refresh(); // ... and refresh them
                removeListener();
//                    }
//                });
            }
        }
    };

    protected Collection initCollection() {
        propertySupport.addPropertyChangeListener(listener);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
                java.util.Map nodeord = (java.util.Map)nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);
                boolean sort = nodeinfo.getName().equals("Drivers") ? false : true; //NOI18N
                TreeSet children = new TreeSet(new NodeComparator(nodeord, sort));

                try {
                    Vector chlist;
                    synchronized (sync) {
                        chlist = nodeinfo.getChildren();
                    }

                    for (int i=0;i<chlist.size();i++) {
                        Node snode = null;
                        Object sinfo = chlist.elementAt(i);

                        if (sinfo instanceof DatabaseNodeInfo) {
                            DatabaseNodeInfo dni = (DatabaseNodeInfo) sinfo;

                            // aware! in this method is clone of instance dni created
                            snode = createNode(dni);

                        }
                        else
                            if (sinfo instanceof Node)
                                snode = (Node)sinfo;
                        if (snode != null)
                            children.add(snode);
                    }
                    if (getNode() instanceof RootNode) {
                        // open connection (after initCollection done)
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    // add connection (if needed) and make the connection to SAMPLE database connected
                                    PointbasePlus.addOrConnectAccordingToOption();
                                    } catch(Exception ex) {
                                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                                    }
                                }
                            });
                    }
                } catch (Exception e) {
                        e.printStackTrace();
                        children.clear();
                }

                setCh(children);
                
                propertySupport.firePropertyChange("finished", null, null); //NOI18N
            }
        }, 0);

        TreeSet ts = new TreeSet();
        ts.add(createWaitNode());
        return ts;
    }

    /* Creates and returns the instance of the node
    * representing the status 'WAIT' of the node.
    * It is used when it spent more time to create elements hierarchy.
    * @return the wait node.
    */
    private Node createWaitNode () {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(bundle.getString("WaitNode")); //NOI18N
        n.setIconBase("org/netbeans/modules/db/resources/wait"); //NOI18N
        return n;
    }

    private TreeSet getCh() {
        return children;
    }

    private void setCh(TreeSet children) {
        this.children = children;
    }

    private void removeListener() {
        propertySupport.removePropertyChangeListener(listener);
    }

    class NodeComparator implements Comparator {
        private java.util.Map map = null;
        private boolean sort;

        public NodeComparator(java.util.Map map, boolean sort) {
            this.map = map;
            this.sort = sort;
        }

        public int compare(Object o1, Object o2) {
            if (! sort)
                return 1;

            if (!(o1 instanceof DatabaseNode))
                return -1;
            if (!(o2 instanceof DatabaseNode))
                return 1;

            int o1val, o2val, diff;
            Integer o1i = (Integer)map.get(o1.getClass().getName());
            if (o1i != null)
                o1val = o1i.intValue();
            else
                o1val = Integer.MAX_VALUE;
            Integer o2i = (Integer)map.get(o2.getClass().getName());
            if (o2i != null)
                o2val = o2i.intValue();
            else
                o2val = Integer.MAX_VALUE;

            diff = o1val-o2val;
            if (diff == 0)
                return ((DatabaseNode)o1).getInfo().getName().compareTo(((DatabaseNode)o2).getInfo().getName());
            return diff;
        }
    }

    public DatabaseNode createNode(DatabaseNodeInfo info) {
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

    public DatabaseNode createSubnode(DatabaseNodeInfo info, boolean addToChildrenFlag) throws DatabaseException {
        DatabaseNode subnode = createNode(info);
        if (subnode != null && addToChildrenFlag) {
            DatabaseNodeInfo ninfo = ((DatabaseNode)getNode()).getInfo();
            ninfo.getChildren().add(info);

            //workaround for issue #31617, children should be initialized if they are not
//            getNodes();

            if (isInitialized())
                add(new Node[] {subnode});
        }

        return subnode;
    }

    /** Creating the database connection to Pointbase SAMPLE database acording of setting PointbaseModule, if module is installed.
     */
    private void createPointbaseConnection() {
        try {
            // only test for PointBase module
            Class.forName("com.sun.forte4j.pointbase.PointBaseSettings", false, this.getClass().getClassLoader()); // NOI18N

            // load the method for creating connection
            Class restore = Class.forName("com.sun.forte4j.pointbase.util.CreatorConnection", false, this.getClass().getClassLoader()); // NOI18N

            Method addOrConnectMethod = restore.getMethod ("addOrConnectPointbase", null); // NOI18N

            // call it
            addOrConnectMethod.invoke (restore.newInstance(), null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (Exception e) {
        }
    }
}
