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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Dialog;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsRefreshClientAction;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandler;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerClass;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModelFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.DefinitionsBindings;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.GlobalBindings;
import org.netbeans.modules.websvc.core.ConfigureHandlerAction;
import org.netbeans.modules.websvc.core.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.core.webservices.ui.panels.MessageHandlerPanel;
import org.netbeans.modules.websvc.core.wseditor.support.WSEditAttributesAction;
import org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookieImpl;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class JaxWsClientNode extends AbstractNode implements OpenCookie, JaxWsRefreshCookie,
        ConfigureHandlerCookie{
    Client client;
    FileObject srcRoot;
    JaxWsModel jaxWsModel;
    InstanceContent content;
    private FileObject wsdlFileObject;
    boolean modelGenerationFinished;
    
    public JaxWsClientNode(JaxWsModel jaxWsModel, Client client, FileObject srcRoot) {
        this(jaxWsModel, client, srcRoot, new InstanceContent());
    }
    
    private JaxWsClientNode(JaxWsModel jaxWsModel, Client client, FileObject srcRoot, InstanceContent content) {
        super(new JaxWsClientChildren(client, srcRoot),new AbstractLookup(content));
        this.jaxWsModel=jaxWsModel;
        this.client=client;
        this.srcRoot=srcRoot;
        this.content = content;
        setName(client.getName());
        setDisplayName(client.getName());
        content.add(this);
        content.add(client);
        content.add(srcRoot);
        WsdlModeler modeler = getWsdlModeler();
        if (modeler!=null) {
            changeIcon();
            modeler.generateWsdlModel(new WsdlModelListener(){
                public void modelCreated(WsdlModel model) {
                    modelGenerationFinished=true;
                    changeIcon();
                }
            });
        }
        content.add(new EditWSAttributesCookieImpl(this, jaxWsModel));
        setValue("wsdl-url",client.getWsdlUrl());
    }
    
    @Override
    public String getShortDescription() {
        return client.getWsdlUrl();
    }
    
    private static final String WAITING_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/waiting.png"; // NOI18N
    private static final String ERROR_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/error-badge.gif"; //NOI18N
    private static final String SERVICE_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/XMLServiceDataIcon.png"; //NOI18N

    private java.awt.Image cachedWaitingBadge;
    private java.awt.Image cachedErrorBadge;
    private java.awt.Image cachedServiceBadge;
    
    @Override
    public java.awt.Image getIcon(int type) {
        if (((JaxWsClientChildren)getChildren()).getWsdlModel()!=null) {
            return getServiceImage();
        } else {
            WsdlModeler wsdlModeler = getWsdlModeler();
            if (wsdlModeler!=null && wsdlModeler.getCreationException()==null) {
                if (modelGenerationFinished)
                    return getServiceImage();
                else
                    return org.openide.util.Utilities.mergeImages(getServiceImage(), getWaitingBadge(), 15, 8); 
            } else {
                java.awt.Image dirtyNodeImage = org.openide.util.Utilities.mergeImages(getServiceImage(), getErrorBadge(), 6, 6);
                if (modelGenerationFinished)
                    return dirtyNodeImage;
                else
                    return org.openide.util.Utilities.mergeImages(dirtyNodeImage, getWaitingBadge(), 15, 8);
            }
        }
    }
    
    private java.awt.Image getServiceImage() {
        if (cachedServiceBadge == null) {
            cachedServiceBadge = org.openide.util.Utilities.loadImage(SERVICE_BADGE);
        }            
        return cachedServiceBadge;        
    }
    private java.awt.Image getErrorBadge() {
        if (cachedErrorBadge == null) {
            cachedErrorBadge = org.openide.util.Utilities.loadImage(ERROR_BADGE);
        }            
        return cachedErrorBadge;        
    }
    private java.awt.Image getWaitingBadge() {
        if (cachedWaitingBadge == null) {
            cachedWaitingBadge = org.openide.util.Utilities.loadImage(WAITING_BADGE);
        }            
        return cachedWaitingBadge;        
    }
    
    @Override
    public java.awt.Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public void open() {
        EditCookie ec = getEditCookie();
        if (ec != null) {
            ec.edit();
        }
    }
    
    void changeIcon() {
        fireIconChange();
    }

    private EditCookie getEditCookie() {
        try {
            FileObject wsdlFo =
                    JAXWSClientSupport.getJaxWsClientSupport(srcRoot).getLocalWsdlFolderForClient(client.getName(),false).getFileObject(client.getLocalWsdlFile());
            assert wsdlFo!=null: "Cannot find local WSDL file"; //NOI18N
            if (wsdlFo!=null) {
                DataObject dObj = DataObject.find(wsdlFo);
                return (EditCookie)dObj.getCookie(EditCookie.class);
            }
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
            return null;
        }
        return null;
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>(Arrays.asList(
            SystemAction.get(OpenAction.class),
            SystemAction.get(JaxWsRefreshClientAction.class),
            null,
            SystemAction.get(WSEditAttributesAction.class),
            null,
            SystemAction.get(ConfigureHandlerAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)));
        addFromLayers(actions, "WebServices/Clients/Actions");
        return actions.toArray(new Action[actions.size()]);
    }
    
    private void addFromLayers(ArrayList<Action> actions, String path) {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                actions.add((Action) next);
            } else if (next instanceof javax.swing.JSeparator) {
                actions.add(null);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws java.io.IOException {
        String clientName = client.getName();
//        NotifyDescriptor.Confirmation notifyDesc =
//                new NotifyDescriptor.Confirmation(NbBundle.getMessage(JaxWsClientNode.class, "MSG_CONFIRM_DELETE", clientName));
//        DialogDisplayer.getDefault().notify(notifyDesc);
//        if(notifyDesc.getValue() == NotifyDescriptor.YES_OPTION){
            
            JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
            // removing local wsdl and xml artifacts
            FileObject localWsdlFolder = support.getLocalWsdlFolderForClient(clientName,false);
            if (localWsdlFolder!=null) {
                FileObject clientArtifactsFolder = localWsdlFolder.getParent();
                FileLock lock=null;
                try {
                    lock = clientArtifactsFolder.lock();
                    clientArtifactsFolder.delete(lock);
                } finally {
                    if (lock!=null) lock.releaseLock();
                }
            }
            
            Project project = FileOwnerQuery.getOwner(srcRoot);
            // remove also client xml artifacs from WEB-INF[META-INF]/wsdl
            if (client.getWsdlUrl().startsWith("file:")) { //NOI18N
                if (project.getLookup().lookup(J2eeModuleProvider.class)!=null) {
                    FileObject webInfClientFolder = findWsdlFolderForClient(support, clientName);
                    if (webInfClientFolder!=null) {
                        FileObject webInfClientRootFolder = webInfClientFolder.getParent();
                        FileLock lock=null;
                        try {
                            lock = webInfClientFolder.lock();
                            webInfClientFolder.delete(lock);
                        } finally {
                            if (lock!=null) lock.releaseLock();
                        }
                        if (webInfClientRootFolder.getChildren().length==0) {
                            try {
                                lock = webInfClientRootFolder.lock();
                                webInfClientRootFolder.delete(lock);
                            } finally {
                                if (lock!=null) lock.releaseLock();
                            }
                        }
                    }
                }
            }
            // cleaning java artifacts
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
            try {
                ExecutorTask wsimportTask =
                        ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-client-clean-"+clientName},null); //NOI18N
                wsimportTask.waitFinished();
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
            // removing entry from jax-ws.xml
            support.removeServiceClient(clientName);
            super.destroy();
//        }
    }
    
    /**
     * refresh service information obtained from wsdl (when wsdl file was changed)
     */
    public void refreshService(boolean downloadWsdl) {
        if (downloadWsdl) {
            String result = RefreshClientDialog.open(client.getWsdlUrl());
            if (RefreshClientDialog.CLOSE.equals(result)) return;
            else if (RefreshClientDialog.NO_DOWNLOAD.equals(result)) {
                ((JaxWsClientChildren)getChildren()).refreshKeys(false);
            } else {
                ((JaxWsClientChildren)getChildren()).refreshKeys(true, result);
            }
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JaxWsClientNode.class,
                    "HINT_RefreshClient"))); //NOI18N           
            ((JaxWsClientChildren)getChildren()).refreshKeys(false);
        }
    }
    
    private void removeWsdlFolderContents(){
        FileObject wsdlFolder = getJAXWSClientSupport().getLocalWsdlFolderForClient(getName(), false);
        if(wsdlFolder != null){
            FileLock lock = null;
            
            FileObject[] files = wsdlFolder.getChildren();
            for(int i = 0; i < files.length; i++){
                try{
                    FileObject file = files[i];
                    lock = file.lock();
                    file.delete(lock);
                }catch(IOException e){
                    ErrorManager.getDefault().notify(e);
                } 
                finally{
                    if(lock != null){
                        lock.releaseLock();
                        lock = null;
                    }
                }
            }
        }
    }
    
    
    public void configureHandler() {
        Project project = FileOwnerQuery.getOwner(srcRoot);
        ArrayList<String> handlerClasses = new ArrayList<String>();
        BindingsModel bindingsModel = getBindingsModel();
        if(bindingsModel != null){  //if there is an existing bindings file, load it
            GlobalBindings gb = bindingsModel.getGlobalBindings();
            if(gb != null){
                DefinitionsBindings db = gb.getDefinitionsBindings();
                if(db != null){
                    BindingsHandlerChains handlerChains = db.getHandlerChains();
                    //there is only one handler chain
                    BindingsHandlerChain handlerChain =
                            handlerChains.getHandlerChains().iterator().next();
                    Collection<BindingsHandler> handlers = handlerChain.getHandlers();
                    for(BindingsHandler handler : handlers){
                        BindingsHandlerClass handlerClass = handler.getHandlerClass();
                        handlerClasses.add(handlerClass.getClassName());
                    }
                }
            }
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project,
                handlerClasses, true, client.getName());
        String title = NbBundle.getMessage(JaxWsNode.class,"TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
        dialogDesc.setButtonListener(new ClientHandlerButtonListener(panel,
                bindingsModel, client, this, jaxWsModel));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
        dialog.setVisible(true);
    }
    
    private BindingsModel getBindingsModel(){
        String handlerBindingFile = client.getHandlerBindingFile();
        BindingsModel bindingsModel = null;
        
        //if there is an existing handlerBindingFile, load it
        try{
            if(handlerBindingFile != null){
                JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
                FileObject bindingsFolder = support.getBindingsFolderForClient(getName(), false);
                if(bindingsFolder != null){
                    FileObject handlerBindingFO = bindingsFolder.getFileObject(handlerBindingFile);
                    if(handlerBindingFO != null){
                        ModelSource ms = Utilities.getModelSource(handlerBindingFO, true);
                        bindingsModel =  BindingsModelFactory.getDefault().getModel(ms);
                    }
                }
            }
        }catch(Exception e){
            ErrorManager.getDefault().notify(e);
            return null;
        }
        return bindingsModel;
    }
    
    WsdlModeler getWsdlModeler() {
        if (getLocalWsdl()!=null) {
            try {
                WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFileObject.getURL());
                if (modeler!=null) {
                    String packageName = client.getPackageName();
                    if (packageName!=null && client.isPackageNameForceReplace()) {
                        // set the package name for the modeler
                        modeler.setPackageName(packageName);
                    } else {
                        modeler.setPackageName(null);
                    }
                    modeler.setCatalog(getJAXWSClientSupport().getCatalog());
                    setBindings(modeler);
                    return modeler;
                }
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.ERROR, NbBundle.getMessage(JaxWsNode.class,"ERR_missingLocalWsdl"));
        }
        return null;
    }
    
    FileObject getLocalWsdl() {
        if (wsdlFileObject==null) {
            FileObject localWsdlocalFolder = getJAXWSClientSupport().getLocalWsdlFolderForClient(client.getName(),false);
            if (localWsdlocalFolder!=null) {
                String relativePath = client.getLocalWsdlFile();
                if (relativePath != null) {
                    wsdlFileObject=localWsdlocalFolder.getFileObject(relativePath);
                }
            }
        }
        return wsdlFileObject;
    }
    
    private JAXWSClientSupport getJAXWSClientSupport() {
        return JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
    }
    
    private void setBindings(WsdlModeler wsdlModeler) {
        Binding[] extbindings = client.getBindings();
        if (extbindings==null || extbindings.length==0) {
            wsdlModeler.setJAXBBindings(null);
            return;
        }
        String[] bindingFiles = new String[extbindings.length];
        for(int i = 0; i < extbindings.length; i++){
            bindingFiles[i] = extbindings[i].getFileName();
        }
        
        FileObject bindingsFolder = getJAXWSClientSupport().getBindingsFolderForClient(getName(),true);
        List<URL> list = new ArrayList<URL>();
        for (int i=0;i<bindingFiles.length;i++) {
            FileObject fo = bindingsFolder.getFileObject(bindingFiles[i]);
            try {
                list.add(fo.getURL());
            } catch (FileStateInvalidException ex) {
                // if there is problem no bindings will be added
            }
        }
        URL[] bindings = new URL[list.size()];
        list.<URL>toArray(bindings);
        wsdlModeler.setJAXBBindings(bindings);
    }
    
    void setModelGenerationFinished(boolean value) {
        modelGenerationFinished=value;
    }
    
    JaxWsModel getJaxWsModel() {
        return jaxWsModel;
    }
    
    private FileObject findWsdlFolderForClient(JAXWSClientSupport support, String name) throws IOException {
        FileObject globalWsdlFolder = support.getWsdlFolder(false);
        if (globalWsdlFolder!=null) {
            return globalWsdlFolder.getFileObject("client/"+name);
        }
        return null;
    }
    
 
}
