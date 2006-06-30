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


package org.netbeans.tests.j2eeserver.plugin.registry;

import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
/**
 *
 * @author  nn136682
 */
public class NodeFactory implements org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory {

    /** Creates a new instance of NodeFactory */
    public NodeFactory() {
    }

    public org.openide.nodes.Node getFactoryNode(org.openide.util.Lookup lookup) {
        DeploymentFactory depFactory = (DeploymentFactory) lookup.lookup(DeploymentFactory.class);
        if (depFactory == null || ! (depFactory instanceof DepFactory)) {
            System.out.println("WARNING: getFactoryNode lookup returned "+depFactory);
            return null;
        }
        System.out.println("INFO: getFactoryNode returning new plugin node");
        return new PluginNode((DepFactory)depFactory);
    }
    
    public org.openide.nodes.Node getManagerNode(org.openide.util.Lookup lookup) {
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
        if (depManager == null || ! (depManager instanceof DepManager)) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);
            return null;
        }
        System.out.println("INFO: getManagerNode returning new Manager node");
        return new ManagerNode((DepManager)depManager);
    }
    
    public org.openide.nodes.Node getTargetNode(org.openide.util.Lookup lookup) {
        Target target = (Target) lookup.lookup(Target.class);
        if (target == null || ! (target instanceof Targ) ) {
            System.out.println("WARNING: getTargetNode lookup returned "+target);
            return null;
        }
        System.out.println("INFO: getManagerNode returning new Target node");
        return new TargNode((Targ)target);
    }
}
