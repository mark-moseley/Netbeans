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

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.netbeans.modules.websvc.core.ClientCreator;

/**
 *
 * @author Radko, Milan Kuchtiak
 */
public class JaxWsClientCreator implements ClientCreator {
    private Project project;
    private WizardDescriptor wiz;
    
    private static final boolean DEBUG = false;
    private static final int JSE_PROJECT_TYPE = 0;
    private static final int WEB_PROJECT_TYPE = 1;
    private static final int EJB_PROJECT_TYPE = 2;
    private static final int CAR_PROJECT_TYPE = 3;
    
    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsClientCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
        
    public void createClient() throws IOException {
        
        final boolean isJsr109Supported = isJsr109Supported();
        final boolean isJWSDPSupported = isJWSDPSupported();
        
        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsClientCreator.class, "MSG_WizCreateClient")); //NOI18N
        
        task = new Task(new Runnable() {
            public void run() {
                try {
                    handle.start();
                    generate15Client((isJsr109Supported || isJWSDPSupported), handle);
                } catch (IOException exc) {
                    //finish progress bar
                    handle.finish();
                    
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
                }
            }
        });
        RequestProcessor.getDefault().post(task);
    }
    
    private void generate15Client(boolean isJsr109Platform, ProgressHandle handle) throws IOException {
        
        // !PW Get client support from project (from first page of wizard)
        JAXWSClientSupport jaxWsClientSupport=null;
        if(project != null) {
            jaxWsClientSupport = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        }
        if(jaxWsClientSupport == null) {
            // notify no client support
//			String mes = MessageFormat.format (
//				NbBundle.getMessage (WebServiceClientWizardIterator.class, "ERR_WebServiceClientSupportNotFound"),
//				new Object [] {"Servlet Listener"}); //NOI18N
            String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_NoWebServiceClientSupport"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            handle.finish();
            return;
        }
        
        String wsdlUrl = (String)wiz.getProperty(ClientWizardProperties.WSDL_DOWNLOAD_URL);
        String filePath = (String)wiz.getProperty(ClientWizardProperties.WSDL_FILE_PATH);
        Boolean useDispatch = (Boolean) wiz.getProperty(ClientWizardProperties.USEDISPATCH);
        //if (wsdlUrl==null) wsdlUrl = "file:"+(filePath.startsWith("/")?filePath:"/"+filePath); //NOI18N
        if(wsdlUrl == null){
            wsdlUrl = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath))).getURL().toExternalForm();
        }
        String packageName = (String)wiz.getProperty(ClientWizardProperties.WSDL_PACKAGE_NAME);
        if (packageName!=null && packageName.length()==0) packageName=null;
        String clientName = jaxWsClientSupport.addServiceClient(getWsdlName(wsdlUrl),wsdlUrl,packageName, isJsr109Platform); 
        if (useDispatch) {
            List<Client> clients = jaxWsClientSupport.getServiceClients();
            for (Client c : clients) {
                if (c.getName().equals(clientName)) {
                    c.setUseDispatch(useDispatch);
                }
            }
            JaxWsModel jaxWsModel = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            jaxWsModel.write();
        }
        handle.finish();
    }
    
    private String getWsdlName(String wsdlUrl) {
        int ind = wsdlUrl.lastIndexOf("/"); //NOI18N
        String wsdlName = ind>=0?wsdlUrl.substring(ind+1):wsdlUrl;
        if (wsdlName.toUpperCase().endsWith("?WSDL")) wsdlName = wsdlName.substring(0,wsdlName.length()-5); //NOI18N
        ind = wsdlName.lastIndexOf(".wsdl"); //NOI18N
        if (ind>0) wsdlName = wsdlName.substring(0,ind);
        // replace special characters with '_'
        return convertAllSpecialChars(wsdlName);
    }
    
    private String convertAllSpecialChars(String resultStr){
        StringBuffer sb = new StringBuffer(resultStr);
        for(int i = 0; i < sb.length(); i++){
            char c = sb.charAt(i);
            if( Character.isLetterOrDigit(c) ||
                    (c == '/') ||
                    (c == '.') ||
                    (c == '_') ||
                    (c == ' ') ||
                    (c == '-')){
                continue;
            }else{
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(project, sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return result.<SourceGroup>toArray(new SourceGroup[result.size()]);
    }
    
    private static Set<SourceGroup> getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map<FileObject, SourceGroup> foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList<SourceGroup>();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = sourceRoots.get(i);
            SourceGroup srcGroup = foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    private static Map<FileObject, SourceGroup> createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.<FileObject, SourceGroup>emptyMap();
        } else {
            result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }
    
    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(severity, new IllegalStateException(
                            "No FileObject found for the following URL: " + urls[i])); //NOI18N
                }
            }
        }
        return result;
    }
    
    private J2eePlatform getJ2eePlatform(){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            }
        }
        return null;
    }
    
    private boolean isJWSDPSupported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
        }
        return false;
    }
    
    private boolean isJsr109Supported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
        }
        return false;
    }
    
    private boolean isJsr109OldSupported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
        }
        return false;
    }

    /**
      *
      * <b>DON'T USE</b>, for tests only
      */
    Task task;
}
