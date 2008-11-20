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
package org.netbeans.modules.maven.jaxws.nodes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.maven.jaxws.actions.JaxWsRefreshAction;
import org.netbeans.modules.maven.jaxws.wizards.JaxWsClientCreator;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.support.RefreshClientDialog;
import org.netbeans.modules.websvc.api.support.RefreshCookie;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class JaxWsClientNode extends AbstractNode implements OpenCookie, RefreshCookie {
    JaxWsService client;
    JAXWSLightSupport jaxWsSupport;
    InstanceContent content;
    private FileObject wsdlFileObject;
    boolean modelGenerationFinished;
    WsdlModel wsdlModel;
    
    public JaxWsClientNode(JAXWSLightSupport jaxWsSupport, JaxWsService client) {
        this(jaxWsSupport, client, new InstanceContent());
    }
    
    private JaxWsClientNode(JAXWSLightSupport jaxWsSupport, JaxWsService client, InstanceContent content) {
        super(new JaxWsClientChildren(jaxWsSupport, client), new AbstractLookup(content));
        this.jaxWsSupport=jaxWsSupport;
        this.client=client;
        this.content = content;
        content.add(this);
        content.add(client);
        content.add(jaxWsSupport);
        final WsdlModeler modeler = getWsdlModeler();
        if (modeler!=null) {
            changeIcon();
            modeler.generateWsdlModel(new WsdlModelListener(){
                public void modelCreated(WsdlModel model) {
                    modelGenerationFinished=true;
                    changeIcon();
                    if (modeler.getCreationException() == null && model != null) {
                        wsdlModel = model;
                    }
                }
            });
        }
//        if (wsdlFileObject != null) {
//            setName(wsdlFileObject.getName());
//            setDisplayName(wsdlFileObject.getName());
//        }
//        content.add(new EditWSAttributesCookieImpl(this, jaxWsModel));
//        setValue("wsdl-url",client.getWsdlUrl());
    }
    
    public WsdlModel getWsdlModel(){
        return this.getWsdlModeler().getAndWaitForWsdlModel();
    }

    @Override
    public String getName() {
        return wsdlFileObject.getName();
    }
    
    @Override
    public String getDisplayName() {
        return wsdlFileObject.getName();
    }
    
    @Override
    public String getShortDescription() {
        return client.getLocalWsdl();
    }
    
    private static final String WAITING_BADGE = "org/netbeans/modules/maven/jaxws/resources/waiting.png"; // NOI18N
    private static final String ERROR_BADGE = "org/netbeans/modules/maven/jaxws/resources/error-badge.gif"; //NOI18N
    private static final String SERVICE_BADGE = "org/netbeans/modules/maven/jaxws/resources/XMLServiceDataIcon.png"; //NOI18N

    private java.awt.Image cachedWaitingBadge;
    private java.awt.Image cachedErrorBadge;
    private java.awt.Image cachedServiceBadge;
    
    @Override
    public java.awt.Image getIcon(int type) {
        if (wsdlModel != null) {
            return getServiceImage();
        } else {
            WsdlModeler wsdlModeler = getWsdlModeler();
            if (wsdlModeler!=null && wsdlModeler.getCreationException()==null) {
                if (modelGenerationFinished)
                    return getServiceImage();
                else
                    return ImageUtilities.mergeImages(getServiceImage(), getWaitingBadge(), 15, 8);
            } else {
                java.awt.Image dirtyNodeImage = ImageUtilities.mergeImages(getServiceImage(), getErrorBadge(), 6, 6);
                if (modelGenerationFinished)
                    return dirtyNodeImage;
                else
                    return ImageUtilities.mergeImages(dirtyNodeImage, getWaitingBadge(), 15, 8);
            }
        }
    }
    
    private java.awt.Image getServiceImage() {
        if (cachedServiceBadge == null) {
            cachedServiceBadge = ImageUtilities.loadImage(SERVICE_BADGE);
        }            
        return cachedServiceBadge;        
    }
    private java.awt.Image getErrorBadge() {
        if (cachedErrorBadge == null) {
            cachedErrorBadge = ImageUtilities.loadImage(ERROR_BADGE);
        }            
        return cachedErrorBadge;        
    }
    private java.awt.Image getWaitingBadge() {
        if (cachedWaitingBadge == null) {
            cachedWaitingBadge = ImageUtilities.loadImage(WAITING_BADGE);
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
                    jaxWsSupport.getLocalWsdlFolder(false).getFileObject(client.getLocalWsdl());
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
            SystemAction.get(JaxWsRefreshAction.class),
//            null,
//            SystemAction.get(WSEditAttributesAction.class),
//            null,
//            SystemAction.get(ConfigureHandlerAction.class),
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
        if (wsdlFileObject != null) {
            // remove entry in wsimport configuration
            Project project = FileOwnerQuery.getOwner(wsdlFileObject);
            if (project != null) {
                ModelOperation<POMModel> oper = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        MavenModelUtils.removeWsdlFile(model, client.getLocalWsdl());
                    }
                };
                FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                Utilities.performPOMModelOperations(pom, Collections.singletonList(oper));
            }
            // remove wsdl file
            wsdlFileObject.delete();
        }
    }
    
//    private void removeWsdlFolderContents(){
//        FileObject wsdlFolder = getJAXWSClientSupport().getLocalWsdlFolderForClient(getName(), false);
//        if(wsdlFolder != null){
//            FileLock lock = null;
//            
//            FileObject[] files = wsdlFolder.getChildren();
//            for(int i = 0; i < files.length; i++){
//                try{
//                    FileObject file = files[i];
//                    lock = file.lock();
//                    file.delete(lock);
//                }catch(IOException e){
//                    ErrorManager.getDefault().notify(e);
//                } 
//                finally{
//                    if(lock != null){
//                        lock.releaseLock();
//                        lock = null;
//                    }
//                }
//            }
//        }
//    }
    
    
//    public void configureHandler() {
//        Project project = FileOwnerQuery.getOwner(srcRoot);
//        ArrayList<String> handlerClasses = new ArrayList<String>();
//        BindingsModel bindingsModel = getBindingsModel();
//        if(bindingsModel != null){  //if there is an existing bindings file, load it
//            GlobalBindings gb = bindingsModel.getGlobalBindings();
//            if(gb != null){
//                DefinitionsBindings db = gb.getDefinitionsBindings();
//                if(db != null){
//                    BindingsHandlerChains handlerChains = db.getHandlerChains();
//                    //there is only one handler chain
//                    BindingsHandlerChain handlerChain =
//                            handlerChains.getHandlerChains().iterator().next();
//                    Collection<BindingsHandler> handlers = handlerChain.getHandlers();
//                    for(BindingsHandler handler : handlers){
//                        BindingsHandlerClass handlerClass = handler.getHandlerClass();
//                        handlerClasses.add(handlerClass.getClassName());
//                    }
//                }
//            }
//        }
//        final MessageHandlerPanel panel = new MessageHandlerPanel(project,
//                handlerClasses, true, client.getName());
//        String title = NbBundle.getMessage(JaxWsNode.class,"TTL_MessageHandlerPanel");
//        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
//        dialogDesc.setButtonListener(new ClientHandlerButtonListener(panel,
//                bindingsModel, client, this, jaxWsModel));
//        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
//        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
//        dialog.setVisible(true);
//    }
    
//    private BindingsModel getBindingsModel(){
//        String handlerBindingFile = client.getHandlerBindingFile();
//        BindingsModel bindingsModel = null;
//        
//        //if there is an existing handlerBindingFile, load it
//        try{
//            if(handlerBindingFile != null){
//                JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
//                FileObject bindingsFolder = support.getBindingsFolderForClient(getName(), false);
//                if(bindingsFolder != null){
//                    FileObject handlerBindingFO = bindingsFolder.getFileObject(handlerBindingFile);
//                    if(handlerBindingFO != null){
//                        ModelSource ms = Utilities.getModelSource(handlerBindingFO, true);
//                        bindingsModel =  BindingsModelFactory.getDefault().getModel(ms);
//                    }
//                }
//            }
//        }catch(Exception e){
//            ErrorManager.getDefault().notify(e);
//            return null;
//        }
//        return bindingsModel;
//    }
    
    WsdlModeler getWsdlModeler() {
        if (getLocalWsdl()!=null) {
            try {
                WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFileObject.getURL());
                if (modeler!=null) {
//                    String packageName = client.getPackageName();
//                    if (packageName!=null && client.isPackageNameForceReplace()) {
//                        // set the package name for the modeler
//                        modeler.setPackageName(packageName);
//                    } else {
//                        modeler.setPackageName(null);
//                    }
                    modeler.setCatalog(jaxWsSupport.getCatalog());
//                    setBindings(modeler);
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
    
    private FileObject getLocalWsdl() {
        if (wsdlFileObject==null) {
            FileObject localWsdlocalFolder = jaxWsSupport.getLocalWsdlFolder(false);
            if (localWsdlocalFolder!=null) {
                String relativePath = client.getLocalWsdl();
                if (relativePath != null) {
                    wsdlFileObject=localWsdlocalFolder.getFileObject(relativePath);
                }
            }
        }
        return wsdlFileObject;
    }
    
//    private void setBindings(WsdlModeler wsdlModeler) {
//        Binding[] extbindings = client.getBindings();
//        if (extbindings==null || extbindings.length==0) {
//            wsdlModeler.setJAXBBindings(null);
//            return;
//        }
//        String[] bindingFiles = new String[extbindings.length];
//        for(int i = 0; i < extbindings.length; i++){
//            bindingFiles[i] = extbindings[i].getFileName();
//        }
//        
//        FileObject bindingsFolder = getJAXWSClientSupport().getBindingsFolderForClient(getName(),true);
//        List<URL> list = new ArrayList<URL>();
//        for (int i=0;i<bindingFiles.length;i++) {
//            FileObject fo = bindingsFolder.getFileObject(bindingFiles[i]);
//            if (fo != null) {
//                try {
//                    list.add(fo.getURL());
//                } catch (FileStateInvalidException ex) {
//                    // if there is problem no bindings will be added
//                }
//            }
//        }
//        URL[] bindings = new URL[list.size()];
//        list.<URL>toArray(bindings);
//        wsdlModeler.setJAXBBindings(bindings);
//    }
    
    void setModelGenerationFinished(boolean value) {
        modelGenerationFinished=value;
    }

    public void refreshService(boolean replaceLocalWsdl) {
        if (replaceLocalWsdl) {
            String wsdlUrl = client.getWsdlUrl();
            if (wsdlUrl == null) {
                if (wsdlFileObject != null) {
                    Project project = FileOwnerQuery.getOwner(wsdlFileObject);
                    Preferences prefs = ProjectUtils.getPreferences(project, JaxWsService.class,true);
                    if (prefs != null) {
                        wsdlUrl = prefs.get(wsdlFileObject.getName(), null);
                        if (wsdlUrl != null) {
                            client.setWsdlUrl(wsdlUrl);
                        }
                    }
                }
            }
            RefreshClientDialog.Result result = RefreshClientDialog.open(true, wsdlUrl);
            if (RefreshClientDialog.Result.CLOSE.equals(result)) {
                return;
            } else if (RefreshClientDialog.Result.REFRESH_ONLY.equals(result)) {
                updateNode();               
            } else {
                // replace local wsdl with downloaded version
                FileObject localWsdlFolder = jaxWsSupport.getLocalWsdlFolder(false);                
                if (localWsdlFolder != null) {
                    String newWsdlUrl = result.getWsdlUrl();
                    boolean wsdlUrlChanged = false;
                    if (newWsdlUrl.length() > 0 && !newWsdlUrl.equals(wsdlUrl)) {
                        wsdlUrlChanged = true;
                    }
                    FileObject wsdlFo = null;
                    try {
                        wsdlFo = WSUtils.retrieveResource(
                                localWsdlFolder,
                                jaxWsSupport.getCatalog().toURI(),
                                new URI(wsdlUrl));
                    } catch (URISyntaxException ex) {
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(JaxWsClientCreator.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } catch (UnknownHostException ex) {
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(JaxWsClientCreator.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } catch (IOException ex) {
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(JaxWsClientCreator.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                    if (wsdlFo != null) {
                        final String relativePath = FileUtil.getRelativePath(localWsdlFolder, wsdlFo);

                        if (!relativePath.equals(client.getLocalWsdl())) {
                            Project project = FileOwnerQuery.getOwner(wsdlFo);
                            // update wsdl URL property
                            if (wsdlUrlChanged) {
                                wsdlUrl = newWsdlUrl;
                                client.setWsdlUrl(wsdlUrl);
                                Preferences prefs = ProjectUtils.getPreferences(project, JaxWsService.class,true);
                                if (prefs != null) {
                                    prefs.remove(wsdlFileObject.getName());
                                    prefs.put(wsdlFo.getName(), wsdlUrl);
                                }
                            }
                            wsdlFileObject = wsdlFo;
                            // update project's pom.xml
                            ModelOperation<POMModel> oper = new ModelOperation<POMModel>() {
                                public void performOperation(POMModel model) {
                                    MavenModelUtils.renameWsdlFile(model, client.getLocalWsdl(), relativePath);
                                }
                            };
                            FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                            Utilities.performPOMModelOperations(pom, Collections.singletonList(oper));
                        }
                    } // endif
                    updateNode();
                } // endif
            } //end if-else
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JaxWsClientNode.class, "MSG_RefreshClient")));        
            updateNode();
        }
    }
    
    private void updateNode() {
        final WsdlModeler wsdlModeler = getWsdlModeler();
        if (wsdlModeler != null) {
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                public void modelCreated(WsdlModel model) {
                    wsdlModel = model;
                    setModelGenerationFinished(true);
                    changeIcon();
                    if (model == null) {
                        DialogDisplayer.getDefault().notify(
                                new WsImportFailedMessage(false, wsdlModeler.getCreationException()));
                    }
                    ((JaxWsClientChildren)getChildren()).setWsdlModel(wsdlModel);
                    ((JaxWsClientChildren)getChildren()).updateKeys();
                }
            });
        }
    }
 
}
