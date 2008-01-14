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

package org.netbeans.modules.j2ee.deployment.impl.ui;


import org.openide.nodes.Node;
import org.openide.util.Lookup;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.openide.util.Exceptions;
import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;


/*
 * RegistryNodeProvider.java
 *
 * Created on December 19, 2003, 11:21 AM
 * @author  nn136682
 */

public class RegistryNodeProvider {
    RegistryNodeFactory factory;
    
    /** Creates a new instance of RegistryNodeProvider */
    public RegistryNodeProvider(RegistryNodeFactory factory) {
        this.factory = factory;
    }
       
    public Node createInstanceNode(ServerInstance instance) {
        return new InstanceNodeDecorator(createInstanceNodeImpl(instance, true), instance);
    }
    
    public Node createTargetNode(ServerTarget target) {
        if (factory != null) {
            Node original = factory.getTargetNode(createLookup(target));
            if (original != null) {
                TargetBaseNode xnode = new TargetBaseNode(org.openide.nodes.Children.LEAF, target);
                return new FilterXNode(original, xnode, true);
            }
        }
        return new TargetBaseNode(org.openide.nodes.Children.LEAF, target);
    }
    
    public Node createInstanceTargetNode(ServerInstance instance) {
        Node original = createInstanceNodeImpl(instance, false);
        return new InstanceNodeDecorator(new InstanceTargetXNode(original, instance), instance);
    }
    
    private Node createInstanceNodeImpl(ServerInstance instance, boolean addStateListener) {
        InstanceNode xnode = new InstanceNode(instance, addStateListener);
        
        if (factory != null) {
            Node original = factory.getManagerNode(createLookup(instance));
            if (original != null) {
                return new FilterXNode(original, xnode, true, new FilterXNode.XChildren(xnode));
            }
        }
        return xnode;
    }
    
//    static Lookup createLookup(final Server server) {
//        return new Lookup() {
//            public Object lookup(Class clazz) {
//                if (DeploymentFactory.class.isAssignableFrom(clazz))
//                    return server.getDeploymentFactory();
//                if (DeploymentManager.class.isAssignableFrom(clazz)) {
//                    try {
//                        return server.getDisconnectedDeploymentManager();
//                    } catch (DeploymentManagerCreationException dmce) {
//                        Exceptions.printStackTrace(dmce);
//                    }
//                }
//                return null;
//            }
//            public Lookup.Result lookup(Lookup.Template template) {
//                return null;
//            }
//        };
//    }
    
    static Lookup createLookup(final ServerInstance instance) {
        return new Lookup() {
            public Object lookup(Class clazz) {
                if (DeploymentFactory.class.isAssignableFrom(clazz))
                    return instance.getServer().getDeploymentFactory();
                if (DeploymentManager.class.isAssignableFrom(clazz)) {
                    try {
                        return instance.isConnected() ? instance.getDeploymentManager()
                                                      : instance.getDisconnectedDeploymentManager();
                    } catch (DeploymentManagerCreationException dmce) {
                        Exceptions.printStackTrace(dmce);
                    }
                }
                return null;
            }
            public Lookup.Result lookup(Lookup.Template template) {
                return null;
            }
        };
    }
    
    static Lookup createLookup(final ServerTarget target) {
        return new Lookup() {
            public Object lookup(Class clazz) {
                if (DeploymentFactory.class.isAssignableFrom(clazz))
                    return target.getInstance().getServer().getDeploymentFactory();
                if (DeploymentManager.class.isAssignableFrom(clazz)) {
                    ServerInstance instance = target.getInstance();
                    try {
                        return instance.isConnected() ? instance.getDeploymentManager()
                                                      : instance.getDisconnectedDeploymentManager();
                    } catch (DeploymentManagerCreationException dmce) {
                        Exceptions.printStackTrace(dmce);
                    }
                }
                if (Target.class.isAssignableFrom(clazz))
                    return target.getTarget();
                return null;
            }
            public Lookup.Result lookup(Lookup.Template template) {
                return null;
            }
        };
    }
}
