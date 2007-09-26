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
package org.netbeans.modules.collab.ui.actions;

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.AccountNode;

/**
 * TAF 10-22-2004: This class does not currently function.
 *
 */
public class ToggleAutoLoginAccountAction extends CookieAction {
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected boolean enable(Node[] nodes) 
    //	{
    //		if (nodes.length!=1)
    //		{
    //	Debug.out.println("Nodes.length <> 1");
    //			return false;
    //		}
    //		// Sanity check to make sure the collab manager is present
    //		CollabManager manager=CollabManager.getDefault();
    //		if (manager==null)
    //		{
    //	Debug.out.println("Cookie was null");
    //			return false;
    //		}
    //else
    //	Debug.out.println("Manager not null");
    //
    //		AccountNode node=(AccountNode)
    //			nodes[0].getCookie(AccountNode.class);
    //
    //Debug.out.println("Enabled was "+(node!=null));
    //
    //		return node!=null;
    //	}

    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(ToggleAutoLoginAccountAction.class, "LBL_ToggleAutoLoginAccountAction_Name");
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    protected boolean isAutoLogin() {
        CollabSession session = null;

        if (getActivatedNodes().length > 0) {
            AccountNode cookie = (AccountNode) getActivatedNodes()[0].getCookie(AccountNode.class);

            if (cookie != null) {
                CollabManager manager = CollabManager.getDefault();

                if (manager != null) {
                    Debug.out.println("Result = " + manager.getUserInterface().isAutoLoginAccount(cookie.getAccount()));

                    return cookie.isAutoLogin();
                }
            } else {
                Debug.out.println("Cookie was null");
                new Exception().printStackTrace(Debug.out);
            }
        } else {
            Debug.out.println("No active nodes");
        }

        return false;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public JMenuItem getPopupPresenter()
    //	{
    //		JMenuItem result=
    //			new JCheckBoxMenuItem()
    //			{
    //				public boolean getSelected()
    //				{
    //					return isAutoLogin();
    //				}
    //			};
    //
    //		Actions.connect(result,this);
    //		return result;
    //
    //		
    //
    //	}

    /**
     *
     *
     */
    protected boolean asynchronous() {
        return true;
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "CollabManager was null; action should not have been enabled"; // NOI18N

        // Flip the auto-login bit
        AccountNode node = (AccountNode) nodes[0].getCookie(AccountNode.class);

        if (node != null) {
            manager.getUserInterface().setAutoLoginAccount(
                node.getAccount(), !manager.getUserInterface().isAutoLoginAccount(node.getAccount())
            );
        }
    }

    protected Class[] cookieClasses() {
        return new Class[] { AccountNode.class };
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
}
