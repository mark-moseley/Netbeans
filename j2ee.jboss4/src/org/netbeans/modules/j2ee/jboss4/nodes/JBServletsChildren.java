/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the Web Module node.
 *
 * @author Michal Mocnak
 */
public class JBServletsChildren extends Children.Keys {
    
    private static final String WAIT_NODE = "wait_node"; //NOI18N
    
    private String name;
    private Lookup lookup;
    
    JBServletsChildren(String name, Lookup lookup) {
        this.lookup = lookup;
        this.name = name;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            
            public void run() {
                try {
                    // Query to the jboss4 server
                    Object server = Util.getRMIServer(lookup);
                    ObjectName searchPattern = new ObjectName("jboss.management.local:WebModule="+name+",j2eeType=Servlet,*");
                    Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});
                    
                    Iterator it = managedObj.iterator();
                    
                    // Query results processing
                    while(it.hasNext()) {
                        ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                        String s = elem.getKeyProperty("name");
                        keys.add(new JBServletNode(s));
                    }                    
                } catch (MalformedObjectNameException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                } catch (NullPointerException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                } catch (SecurityException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                } catch (InvocationTargetException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                } catch (IllegalAccessException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getMessage());
                }
                
                setKeys(keys);
            }
        }, 0);
    }
    
    protected void addNotify() {
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBServletNode){
            return new Node[]{(JBServletNode)key};
        }
        
        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node[]{createWaitNode()};
        }
        
        return null;
    }
    
    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    private Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBApplicationsChildren.class, "LBL_WaitNode_DisplayName")); //NOI18N
        n.setIconBaseWithExtension("org/netbeans/modules/j2ee/jboss4/resources/wait.gif"); // NOI18N
        return n;
    }
}
