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

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.ui.*;

/**
 *
 *
 */
public class ManagePublicConversationAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(CreateConversationAction.class, "LBL_ManagePublicConversationAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/chat_png.gif"; // NOI18N
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
    protected Class[] cookieClasses() {
        return new Class[] { CollabSessionCookie.class, PublicConversationNode.class };
    }

    /**
     *
     *
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

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
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();

        if (manager == null) {
            return false;
        }

        // Disable if cookies not present
        if (!super.enable(nodes)) {
            return false;
        }

        assert nodes.length == 1 : "Only one node should be available when this method is called";

        PublicConversationNode node = (PublicConversationNode) nodes[0].getCookie(PublicConversationNode.class);

        if (node == null) {
            return false;
        }

        CollabSession session = node.getCollabSession();
        String name = node.getConversationName();

        //Fix for bug#6239787, disable manage if conv is in progress  
        //		Conversation conversation=node.getConversation(); 
        //		Debug.out.println("ManagePublic Conversation: "); 
        //		if(conversation!=null) 
        //		{ 
        //			CollabPrincipal[] convUsers = conversation.getParticipants(); 
        //			Debug.out.println("ManagePublic Conversation: "+ convUsers.length); 
        //			if((convUsers!=null && convUsers.length>0))//conv users are enough 
        //			{             
        //				return false; 
        //			} 
        //		}		
        return session.canManagePublicConversation(name);
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        PublicConversationNode node = (PublicConversationNode) nodes[0].getCookie(PublicConversationNode.class);
        assert node != null : "PublicConversationNode was null despite enable check"; // NOI18N

        CollabSession session = node.getCollabSession();
        String name = node.getConversationName();

        CollabManager.getDefault().getUserInterface().managePublicConversation(session, name);
    }
}
