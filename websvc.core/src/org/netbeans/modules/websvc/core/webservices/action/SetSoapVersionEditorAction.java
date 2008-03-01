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
package org.netbeans.modules.websvc.core.webservices.action;

import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.SetSoapVersionCookie;
import org.netbeans.modules.websvc.core.jaxws.actions.SetSoapVersionCookieImpl;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/** Editor action that opens the Add Operation dialog and adds new methods to
 *  the service
 */
public class SetSoapVersionEditorAction extends NodeAction {

    private SetSoapVersionCookie cookie;

    public String getName() {
        String name = "LBL_SetSoap12";

        Node[] nodes = this.getActivatedNodes();
        FileObject fo = getFileObjectFromNode(nodes[0]);
        if (fo != null) {
            if (JaxWsUtils.isSoap12(fo)) {
                name = "LBL_SetSoap11";
            }
        }
        return NbBundle.getMessage(SetSoapVersionEditorAction.class, name);
    }

    private boolean changeToSoap12(FileObject wsImplFo) {
        return !JaxWsUtils.isSoap12(wsImplFo);

    }

    public HelpCtx getHelpCtx() {
        // If you will provide context help then use:
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    private FileObject getFileObjectFromNode(Node n) {

        DataObject dObj = (DataObject) n.getCookie(DataObject.class);
        if (dObj != null) {
            return dObj.getPrimaryFile();
        }
        return null;
    }

    protected void performAction(Node[] activatedNodes) {
        FileObject fo = getFileObjectFromNode(activatedNodes[0]);
        if (cookie == null) {
            cookie = new SetSoapVersionCookieImpl();
        }
        cookie.setSoapVersion(fo, changeToSoap12(fo));
    }

    private Service getService() {
        FileObject implClassFo = getFileObjectFromNode(this.getActivatedNodes()[0]);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(implClassFo);
        if (jaxWsSupport != null) {
            List services = jaxWsSupport.getServices();
            for (int i = 0; i < services.size(); i++) {
                Service serv = (Service) services.get(i);
                String implClass = serv.getImplementationClass();
                ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
                if (classPath != null) {
                    if (classPath.getResourceName(implClassFo).equals(implClass.replaceAll("\\.", "/") + ".java")) {
                        return serv;
                    }
                }
            }
        }
        return null;
    }

    private boolean isFromWSDL() {
        Service service = getService();
        if (service != null) {
            return service.getWsdlUrl() != null;
        }
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return !isFromWSDL() && getFileObjectFromNode(activatedNodes[0]) != null;
    }
}
