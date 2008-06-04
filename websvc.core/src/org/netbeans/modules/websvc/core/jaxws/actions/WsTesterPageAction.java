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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.core.JaxWsStackProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie;
import org.netbeans.modules.websvc.serverapi.api.WSStackFeature;
import org.netbeans.modules.websvc.serverapi.api.WSStack;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

public class WsTesterPageAction extends CookieAction {
    public String getName() {
        return NbBundle.getMessage(WsTesterPageAction.class, "LBL_TesterPageAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {JaxWsTesterCookie.class};
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void performAction(Node[] activatedNodes) {
        JaxWsTesterCookie cookie = 
           activatedNodes[0].getCookie(JaxWsTesterCookie.class);
        String wsdlURL = cookie.getTesterPageURL();
        try {
            final URL url = new URL(wsdlURL);
            if (url!=null) {  
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        boolean connectionOK=false;
                        try {
                            URLConnection connection = url.openConnection();
                            if (connection instanceof HttpURLConnection) {
                                HttpURLConnection httpConnection = (HttpURLConnection)connection;
                                try {
                                    httpConnection.setRequestMethod("GET"); //NOI18N
                                    httpConnection.connect();
                                    int responseCode = httpConnection.getResponseCode();
                                    // for secured web services the response code is 405: we should allow to show the response
                                    if (HttpURLConnection.HTTP_OK == responseCode || HttpURLConnection.HTTP_BAD_METHOD == responseCode)
                                        connectionOK=true;
                                } catch (java.net.ConnectException ex) {
                                    // doing nothing - display warning
                                } finally {
                                    if (httpConnection!=null) 
                                    httpConnection.disconnect();
                                }
                            }

                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                        if (connectionOK) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        } else {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(
                                        NbBundle.getMessage(WsTesterPageAction.class,"MSG_UNABLE_TO_OPEN_TEST_PAGE",url),
                                        NotifyDescriptor.WARNING_MESSAGE));
                        }
                    }
                    
                });
            }
        } catch (MalformedURLException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WsTesterPageAction.class,
                    "TXT_TesterPageUrl", wsdlURL));   //NOI18N
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes==null || activatedNodes.length==0) return false;
        FileObject srcRoot = (FileObject)activatedNodes[0].getLookup().lookup(FileObject.class);
        if (srcRoot!=null) {
            Project project = FileOwnerQuery.getOwner(srcRoot);
            if (project!=null) {
                return isTesterPageSupported(project);
            }
        }
        return false;
    }
    
    private boolean isTesterPageSupported(Project project) {
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                WSStack wsStack = JaxWsStackProvider.getJaxWsStack(j2eePlatform);
                System.out.println("TesterPage:wsStack="+wsStack.hashCode());
                return wsStack != null && wsStack.getServiceFeatures().contains(WSStackFeature.TESTER_PAGE);
//                if (j2eePlatform != null) {
//                    return j2eePlatform.isToolSupported("jaxws-tester"); //NOI18N
//                }
            }
        }
        return false;
    }
    

}
