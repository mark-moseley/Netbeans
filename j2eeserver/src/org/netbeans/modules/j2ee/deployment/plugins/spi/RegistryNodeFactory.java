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

package org.netbeans.modules.j2ee.deployment.plugins.spi;


import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This interface allows plugin to create all the registry nodes
 * (other than the root node) as {@link org.openide.nodes.Node} subclasses,
 * and use {@link org.openide.nodes.FilterNode} to generate the display,
 * adding infrastructure actions in, and exposing certain infrastructure to
 * the plugins for use in constructing nodes.
 * Use a look-like infrastructure so migration to looks can happen easier.
 * Plugins need to register an instance of this class in module layer in folder
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code>.
 *
 * @see org.openide.nodes.Node
 * @see org.openide.nodes.FilterNode
 *
 * @author  George Finklang
 */
public interface RegistryNodeFactory {

     /**
      * Return node representing the admin server.  Children of this node are filtered.
      * Start/Stop/Remove/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
      * @return admin server node.
      */
     public Node getManagerNode(Lookup lookup);

     /**
      * Provide node representing Deployment API Target object.  
      * Start/Stop/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Target, Management objects.
      * @return target server node
      */
     public Node getTargetNode(Lookup lookup);
}
