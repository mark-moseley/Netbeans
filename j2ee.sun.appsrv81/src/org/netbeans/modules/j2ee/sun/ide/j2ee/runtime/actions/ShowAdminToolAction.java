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

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Random;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.awt.HtmlBrowser.URLDisplayer;

import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.ManagerNode;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;

/** Action that can always be invoked and work procedurally.
 * This action will display the URL for the given admin server node in the runtime explorer
 * @author  ludo
 */
public class ShowAdminToolAction extends CookieAction {
    
    protected Class[] cookieClasses() {
        return new Class[] {/* SourceCookie.class */};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
        // return MODE_ALL;
    }
    
    protected void performAction(Node[] nodes) {
        if( (nodes != null) &&  (nodes.length == 1) ) {
            if(nodes[0].getLookup().lookup(ManagerNode.class) != null){
                ManagerNode node = (ManagerNode)nodes[0].getCookie(ManagerNode.class);
                try {
                    if (node.getDeploymentManager().isRunning()) {
                        String url = node.getAdminURL() + 
                                "/?"+genRandomString()+"="+genRandomString();
                        URLDisplayer.getDefault().showURL(new URL(url));
                    } else {
                        Util.showInformation(
                                NbBundle.getMessage(ShowAdminToolAction.class,
                                "MESS_START_INSTANCE"));
                    }
                } catch (MissingResourceException ex) {
                    // this should not happen... If it does, we should find out.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ex);
                } catch (MalformedURLException ex) {
                    // this should not happen... If it does, we should find out.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ex);
                } catch (Exception e){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            e);
               }
            }
        }
    }
    
    
    private String genRandomString() {
        Random r = new Random();
        return "rs"+r.nextInt();
    }
    
    
    public String getName() {
        return NbBundle.getMessage(ShowAdminToolAction.class, "LBL_ShowAdminGUIAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/AddInstanceActionIcon.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RefreshAction.class);
    }
    
    protected boolean enable(Node[] nodes) {
        return (nodes != null) && (nodes.length == 1); // true;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    
}
