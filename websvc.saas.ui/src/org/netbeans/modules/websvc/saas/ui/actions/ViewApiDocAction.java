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
package org.netbeans.modules.websvc.saas.ui.actions;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  nam
 */
public class ViewApiDocAction extends NodeAction {

    /** Creates a new instance of ViewWSDLAction */
    public ViewApiDocAction() {
        super();
    }

    protected boolean enable(Node[] nodes) {
        return getApiDocUrl(nodes) != null && HtmlBrowser.URLDisplayer.getDefault() != null;
    }

    private String getApiDocUrl(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            Saas saas = nodes[0].getLookup().lookup(Saas.class);
            if (saas != null) {
                return saas.getApiDoc();
            }
        }
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(ViewWSDLAction.class, "VIEW_API_Action");
    }

    protected void performAction(Node[] activatedNodes) {
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault();
        if (displayer == null) {
            String msg = NbBundle.getMessage(ViewApiDocAction.class, "MSG_NoDefaultBrowser");
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
            return;
        }

        String apiUrl = getApiDocUrl(activatedNodes);
        if (apiUrl == null) {
            throw new IllegalArgumentException("ViewApiDoc should not be allowed without a api doc URL");
        }
        
        try {
            URL href = new URL(apiUrl);
            displayer.showURL(href);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean asynchronous() {
        return true;
    }
}
