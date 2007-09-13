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

package org.netbeans.spi.project.ui.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * Support class for creating Project node's children nodes from NodeFactory instances
 * in layers.
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.18
 */
public class NodeFactorySupport {
    
    private NodeFactorySupport() {
    }
    
    /**
     * Creates children list that works on top of {@link NodeFactory} instances
     * in layers.
     * @param project the project which is being displayed
     * @param folderPath the path in the System Filesystem that is used as root for subnode composition.
     *        The content of the folder is assumed to be {@link org.netbeans.spi.project.ui.support.NodeFactory} instances
     * @return a new children list
     */
    public static Children createCompositeChildren(Project project, String folderPath) {
        return new DelegateChildren(project, folderPath);
    }

    /**
     * Utility method for creating a non variable NodeList instance.
     * @param nodes a fixed set of nodes to display
     * @return a constant node list
     */
    public static NodeList fixedNodeList(Node... nodes) {
        return new FixedNodeList(nodes);
    }
    
    private static class FixedNodeList implements NodeList<Node> {
        
        private List<Node> nodes;
        
        FixedNodeList(Node... nds) {
            nodes = Arrays.asList(nds);
        }
        public List<Node> keys() {
            return nodes;
        }
        
        public void addChangeListener(ChangeListener l) { }
        
        public void removeChangeListener(ChangeListener l) { }
        
        public void addNotify() {
        }
        
        public void removeNotify() {
        }

        public Node node(Node key) {
            return key;
        }
    }
    
    static class DelegateChildren extends Children.Keys<NodeListKeyWrapper> implements LookupListener, ChangeListener {
        
        private String folderPath;
        private Project project;
        private List<NodeList<?>> nodeLists = new ArrayList<NodeList<?>>();
        private List<NodeFactory> factories = new ArrayList<NodeFactory>();
        private Lookup.Result<NodeFactory> result;
        private HashMap<NodeList<?>, List<NodeListKeyWrapper>> keys;
        
        public DelegateChildren(Project proj, String path) {
            folderPath = path;
            project = proj;
        }
        
        // protected for tests..
        protected Lookup createLookup() {
            return Lookups.forPath(folderPath);
        }
        
       protected Node[] createNodes(NodeListKeyWrapper key) {
           @SuppressWarnings("unchecked") // needs to handle NodeList's of different types
           Node nd = key.nodeList.node(key.object);
           if (nd != null) {
               return new Node[] { nd };
           }
           return new Node[0];
        }
       
       private Collection<NodeListKeyWrapper> createKeys() {
           Collection<NodeListKeyWrapper> col = new ArrayList<NodeListKeyWrapper>();
           synchronized (keys) {
               for (NodeList lst : nodeLists) {
                   List<NodeListKeyWrapper> x = keys.get(lst);
                   if (x != null) {
                       col.addAll(x);
                   }
               }
           }
           return col;
       }
      
        protected @Override void addNotify() {
            super.addNotify();
            keys = new HashMap<NodeList<?>, List<NodeListKeyWrapper>>();
            result = createLookup().lookupResult(NodeFactory.class);
            for (NodeFactory factory : result.allInstances()) {
                NodeList<?> lst = factory.createNodes(project);
                assert lst != null : "Factory " + factory.getClass() + " has broken the NodeFactory contract."; //NOI18N
                lst.addNotify();
                synchronized (keys) {
                    nodeLists.add(lst);
                    addKeys(lst);
                }
                lst.addChangeListener(this);
                factories.add(factory);
            }
            result.addLookupListener(this);
            setKeys(createKeys());
        }
        
        protected @Override void removeNotify() {
            super.removeNotify();
            setKeys(Collections.<NodeListKeyWrapper>emptySet());
            for (NodeList elem : nodeLists) {
                elem.removeChangeListener(this);
                elem.removeNotify();
            }
            synchronized (keys) {
                keys.clear();
                nodeLists.clear();
            }
            factories.clear();
            if (result != null) {
                result.removeLookupListener(this);
                result = null;
            }
        }
        
        public void stateChanged(ChangeEvent e) {
            NodeList list = (NodeList) e.getSource();
            synchronized (keys) {
                removeKeys(list);
                addKeys(list);
            }
            setKeys(createKeys());
        }
        
        //to be called under lock.
        private void addKeys(NodeList list) {
            List<NodeListKeyWrapper> wrps = new ArrayList<NodeListKeyWrapper>();
            for (Object key : list.keys()) {
                wrps.add(new NodeListKeyWrapper(key, list));
            }
            keys.put(list, wrps);
            
        }
        
        //to be called under lock.
        private void removeKeys(NodeList list) {
            keys.remove(list);
        }


        public void resultChanged(LookupEvent ev) {
            int index = 0;
            for (NodeFactory factory : result.allInstances()) {
                if (!factories.contains(factory)) {
                    factories.add(index, factory);
                    NodeList<?> lst = factory.createNodes(project);
                    assert lst != null;
                    synchronized (keys) {
                        nodeLists.add(index, lst);
                        addKeys(lst);
                    }
                    lst.addNotify();
                    lst.addChangeListener(this);
                } else {
                    while (!factory.equals(factories.get(index))) {
                        factories.remove(index);
                        synchronized (keys) {
                            NodeList<?> lst = nodeLists.remove(index);
                            removeKeys(lst);
                            lst.removeNotify();
                            lst.removeChangeListener(this);                            
                        }
                    }
                }
                index++;
            }
            //#115128 prevent deadlock in Children mutex
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setKeys(createKeys());
                }
            });
        }
    }
    
    /**
     * this class makes sure the bond between the NodeList and individial
     * items is not lost, prevents duplicates about different NodeLists
     * while allowing for fine-grained updating of nodes on stateChange()
     * 
     */ 
    private static class NodeListKeyWrapper  {
        NodeList nodeList;
        Object object;

        NodeListKeyWrapper(Object obj, NodeList list) {
            nodeList = list;
            object = obj;
        }
        
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NodeListKeyWrapper other = (NodeListKeyWrapper) obj;
            if (this.nodeList != other.nodeList && (this.nodeList == null || !this.nodeList.equals(other.nodeList))) {
                return false;
            }
            if (this.object != other.object && (this.object == null || !this.object.equals(other.object))) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + (this.nodeList != null ? this.nodeList.hashCode() : 0);
            hash = 79 * hash + (this.object != null ? this.object.hashCode() : 0);
            return hash;
        }
        
    }
    
}
