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

package org.netbeans.jellytools.modules.db.nodes;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.db.actions.DisableDebugAction;
import org.netbeans.jellytools.modules.db.actions.EnableDebugAction;
import org.netbeans.jellytools.nodes.Node;

/** Node representing "Databases" node in Runtime tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      DatabasesNode databases = DatabasesNode.invoke();
 *      databases.enableDebug();
 *      ....
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class DatabasesNode extends Node {
    static final String TREE_PATH = Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "NDN_Databases");
    private static final Action enableDebugAction = new EnableDebugAction();
    private static final Action disableDebugAction = new DisableDebugAction();
    
    public DatabasesNode() {
        super(new RuntimeTabOperator().getRootNode(), TREE_PATH);
    }
    
    /** Finds "Databases" node in Runtime tab
     */
    public static DatabasesNode invoke() {
        RuntimeTabOperator.invoke();
        return new DatabasesNode();
    }
    
    /** performs EnableDebugAction with this node */
    public void enableDebug() {
        enableDebugAction.perform(this);
    }
    
    /** performs DisableDebugAction with this node */
    public void disableDebug() {
        disableDebugAction.perform(this);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            enableDebugAction,
            disableDebugAction,
        });
    }
}
