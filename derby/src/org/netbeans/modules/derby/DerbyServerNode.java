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

package org.netbeans.modules.derby;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;

/**
 * Provides a node for the local Java DB server under the Databases node
 * in the Database Explorer 
 * 
 * @author David Van Couvering
 */
public class DerbyServerNode extends AbstractNode {
    private static final DerbyServerNode DEFAULT = new DerbyServerNode();
    
    private SystemAction[] actions = new SystemAction[] {
            new StartAction(),
            new StopAction(),
            new CreateDatabaseAction(),
            new DerbySettingsAction()
        };
    
    // I'd like a less generic icon, but this is what we have for now...
    private static final String ICON_BASE = "org/netbeans/modules/derby/resources/database.gif";
    
    public static DerbyServerNode getDefault() {
        return DEFAULT;
    }
    
    private DerbyServerNode() {
        // No node children at this time, maybe later... 
        super(Children.LEAF);
        this.setIconBaseWithExtension(ICON_BASE);
    }
    
    @Override
    public String getDisplayName() {
       // Product name - no need to internationalize
       return "Java DB"; // NOI8N
    }
   
    @Override
    public SystemAction[] getActions() {
        return actions;
    }
    
    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public SystemAction getDefaultAction() {
        return null;
    }   

}
