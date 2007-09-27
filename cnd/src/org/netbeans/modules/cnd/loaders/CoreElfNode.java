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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.loaders;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;


/** A node to represent an Elf core object */
public class CoreElfNode extends CndDataNode {

    public CoreElfNode(CoreElfObject obj) {
	this(obj, Children.LEAF);
    }

    public CoreElfNode(CoreElfObject obj, Children ch) {
	super(obj, ch);
	setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/CoreElfIcon.gif"); // NOI18N
    }

    protected Sheet createSheet() {
	Sheet sheet = super.createSheet();

	Sheet.Set set = sheet.get(BinaryExecSupport.PROP_EXECUTION);
	if (set == null) {
	    set = new Sheet.Set();
	    set.setName(BinaryExecSupport.PROP_EXECUTION);
	    set.setDisplayName(NbBundle.getBundle(CoreElfNode.class).
		    getString("displayNameForExeElfNodeExecSheet"));  // NOI18N
	    set.setShortDescription(NbBundle.getBundle(CoreElfNode.class).
		    getString("hintForExeElfNodeExecSheet"));   // NOI18N
	    BinaryExecSupport es = ((BinaryExecSupport)
				getCookie(BinaryExecSupport.class));
	    if (es != null) {
		es.addProperties(set);
	    }

	    // Trick from org/apache/tools/ant/module/nodes/AntProjectNode.java
	    // Get rid of Arguments property and the Execution property;
	    // corefiles can only be debugged.
	    set.remove(ExecutionSupport.PROP_FILE_PARAMS);
	    set.remove(ExecutionSupport.PROP_EXECUTION);
	    
	    sheet.put(set);
	}
	return sheet;
    }

    private CoreElfObject getCoreElfObject() {
	return (CoreElfObject) getDataObject();
    }
}
