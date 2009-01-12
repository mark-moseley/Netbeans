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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponentFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandler;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerClass;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModelFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.DefinitionsBindings;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.GlobalBindings;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerName;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;

/**
 *
 * @author Roderico Cruz
 */
public class ClientHandlerButtonListener implements ActionListener {

    private MessageHandlerPanel panel;
    private BindingsModel bindingsModel;
    private Client client;
    private Node node;
    private JaxWsModel jaxWsModel;
    //private FileObject bindingHandlerFO;
    private String bindingsHandlerFile;

    public ClientHandlerButtonListener(MessageHandlerPanel panel,
            BindingsModel bindingsModel, Client client, Node node, JaxWsModel jaxWsModel) {

        this.panel = panel;
        this.bindingsModel = bindingsModel;
        this.client = client;
        this.node = node;
        this.jaxWsModel = jaxWsModel;
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == NotifyDescriptor.OK_OPTION) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    configureHandler();
                }
            });
        }
    }

    private void configureHandler() {
        if (!panel.isChanged()) {
            return;
        }
        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ClientHandlerButtonListener.class, "MSG_ConfigureHandler")); //NOI18N
        handle.start();
        handle.switchToIndeterminate();
        FileObject srcRoot = (FileObject) node.getLookup().lookup(FileObject.class);
        JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
        final FileObject bindingsFolder = support.getBindingsFolderForClient(node.getName(), true);
        Client client = (Client) node.getLookup().lookup(Client.class);
        assert client != null;

        try {
            bindingsHandlerFile = client.getHandlerBindingFile();
            if (bindingsHandlerFile == null) {
                String baseBindingsHandlerFile = node.getName() + "_handler";
                bindingsHandlerFile = FileUtil.findFreeFileName(bindingsFolder, baseBindingsHandlerFile, "xml") +
                        ".xml";
                client.setHandlerBindingFile(bindingsHandlerFile);
            }
            final FileObject bindingHandlerFO = FileUtil.createData(bindingsFolder, bindingsHandlerFile);
            //if bindingsModel is null, create it
            if (bindingsModel == null) {
                InputStream is = FileUtil.getConfigFile("jax-ws/default-binding-handler.xml").getInputStream();
                final String bindingsContent = readResource(is); //NOI18N
                is.close();

                bindingsFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                    public void run() throws IOException {
                        BufferedWriter bw = null;
                        OutputStream os = null;
                        OutputStreamWriter osw = null;
                        FileLock lock = bindingHandlerFO.lock();
                        try {
                            os = bindingHandlerFO.getOutputStream(lock);
                            osw = new OutputStreamWriter(os);
                            bw = new BufferedWriter(osw);
                            bw.write(bindingsContent);
                        } finally {
                            try {
                                if (bw != null) {
                                    bw.close();
                                }
                                if (os != null) {
                                    os.close();
                                }
                                if (osw != null) {
                                    osw.close();
                                }
                            } catch (IOException e) {
                                ErrorManager.getDefault().notify(e);
                            }

                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                    }
                });

                //now load the model and add the entry
                ModelSource ms = Utilities.getModelSource(bindingHandlerFO, true);
                bindingsModel = BindingsModelFactory.getDefault().getModel(ms);
                //get the relative path of the wsdl
                FileObject localWsdlFile =
                        support.getLocalWsdlFolderForClient(client.getName(), false).getFileObject(client.getLocalWsdlFile());
                File f = FileUtil.toFile(bindingHandlerFO);
                String relativePath = Utilities.relativize(f.toURI(), new URI(localWsdlFile.getURL().toExternalForm()));
                GlobalBindings gb = bindingsModel.getGlobalBindings();
                try {
                    bindingsModel.startTransaction();
                    gb.setWsdlLocation(relativePath);
                } finally {
                    bindingsModel.endTransaction();  //becomes locked here
                }

                DataObject dobj = DataObject.find(bindingHandlerFO);
                if (dobj.isModified()) {
                    SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);
                    saveCookie.save();
                }

            }//end if bindingsModel == null

            //get handler chain
            TableModel tableModel = panel.getHandlerTableModel();
            GlobalBindings gb = bindingsModel.getGlobalBindings();
            DefinitionsBindings db = gb.getDefinitionsBindings();
            BindingsHandlerChains bhc = db.getHandlerChains();
            BindingsHandlerChain chain = bhc.getHandlerChains().iterator().next();

            //refresh handlers
            try {
                bindingsModel.startTransaction();
                Collection<BindingsHandler> handlers = chain.getHandlers();
                for (BindingsHandler handler : handlers) {
                    chain.removeHandler(handler);
                }

                if (tableModel.getRowCount() > 0) {
                    BindingsComponentFactory factory = bindingsModel.getFactory();
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String className = (String) tableModel.getValueAt(i, 0);
                        BindingsHandler handler = factory.createHandler();

                        String handlerName = className.substring(className.indexOf(".") + 1);
                        BindingsHandlerName name = factory.createHandlerName();
                        name.setHandlerName(handlerName);
                        handler.setHandlerName(name);

                        BindingsHandlerClass handlerClass = factory.createHandlerClass();
                        handlerClass.setClassName(className);
                        handler.setHandlerClass(handlerClass);

                        chain.addHandler(handler);
                    }
                }
            } finally {
                bindingsModel.endTransaction();
            }

            //save bindingshandler file
            DataObject dobj = DataObject.find(bindingHandlerFO);
            if (dobj.isModified()) {
                SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);
                saveCookie.save(); //becomes false here
            }

            if (tableModel.getRowCount() > 0) {
                Binding binding = client.getBindingByFileName(bindingsHandlerFile);
                if (binding == null) {
                    binding = client.newBinding();
                    binding.setFileName(bindingsHandlerFile);
                    client.addBinding(binding);
                }
            } else {
                Binding binding = client.getBindingByFileName(bindingsHandlerFile);
                if (binding != null) {
                    client.removeBinding(binding);
                }
                removeHandlerAnnotation();
            }
            //save the jaxws model
            jaxWsModel.write();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        handle.finish();
        invokeWsImport(srcRoot);
    }

    private void removeHandlerAnnotation() {
        JaxWsClientNode clientNode = node.getLookup().lookup(JaxWsClientNode.class);
        WsdlModel wsdlModel = clientNode.getWsdlModel();
        WsdlService service = wsdlModel.getServices().get(0);
        String serviceName = service.getJavaName();
        FileObject srcRoot = (FileObject) node.getLookup().lookup(FileObject.class);
        Project project = FileOwnerQuery.getOwner(srcRoot);
        
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath cp = ClassPath.getClassPath(groups[0].getRootFolder(), ClassPath.SOURCE);
        final FileObject serviceFO = cp.findResource(serviceName.replaceAll("\\.", "/")  + ".java");  //NOI18N

        //if serviceFO is null, the Service interface has not been generated, so no need to remove any annotation
        if(serviceFO == null) return;

        final JavaSource javaSource = JavaSource.forFileObject(serviceFO);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);

                TypeElement bindingElement = workingCopy.getElements().getTypeElement("javax.jws.HandlerChain");  //NOI18N
                if (bindingElement != null) {
                    AnnotationTree handlerAnnotation = null;
                    List<? extends AnnotationTree> annots = javaClass.getModifiers().getAnnotations();
                    for (AnnotationTree an : annots) {
                        IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                        TreePath anTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), ident);
                        TypeElement anElement = (TypeElement) workingCopy.getTrees().getElement(anTreePath);
                        if (anElement != null && anElement.getQualifiedName().contentEquals("javax.jws.HandlerChain")) {  //NOI18N
                            handlerAnnotation = an;
                            break;
                        }
                    }
                    ModifiersTree modifiers = javaClass.getModifiers();
                    ModifiersTree newModifiers = make.removeModifiersAnnotation(modifiers, handlerAnnotation);
                    workingCopy.rewrite(modifiers, newModifiers);
                    CompilationUnitTree compileUnitTree = workingCopy.getCompilationUnit();
                    List<? extends ImportTree> imports = compileUnitTree.getImports();
                    for (ImportTree imp : imports) {
                        Tree impTree = imp.getQualifiedIdentifier();
                        TreePath impTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), impTree);
                        TypeElement impElement = (TypeElement) workingCopy.getTrees().getElement(impTreePath);
                        if (impElement != null && impElement.getQualifiedName().contentEquals("javax.jws.HandlerChain")) {  //NOI18N
                            CompilationUnitTree newCompileUnitTree = make.removeCompUnitImport(compileUnitTree, imp);
                            workingCopy.rewrite(compileUnitTree, newCompileUnitTree);
                            break;
                        }
                    }
                }
            }

            public void cancel() {
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        javaSource.runModificationTask(modificationTask).commit();
                        saveFile(serviceFO);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
        } else {
            try {
                javaSource.runModificationTask(modificationTask).commit();
                saveFile(serviceFO);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    private static void saveFile(FileObject file) throws IOException {
        DataObject dataObject = DataObject.find(file);
        if (dataObject != null) {
            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }

    private void invokeWsImport(FileObject srcRoot) {
        // re-generate java artifacts
        Project project = FileOwnerQuery.getOwner(srcRoot);
        if (project != null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
            try {
                String name = client.getName();
                ExecutorTask wsimportTask =
                        ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-client-clean-" + name, "wsimport-client-" + name}, null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
            // refresh client artifacts directory due to code completion
            String packageName = client.getPackageName();
            if (packageName != null) {
                packageName = packageName.replace(".", "/"); //NOI18N
                FileObject clientArtifactsFolder = project.getProjectDirectory().getFileObject("build/generated/wsimport/client/" + packageName); //NOI18N
                if (clientArtifactsFolder != null) {
                    clientArtifactsFolder.refresh();
                }
            }
        }
    }

//TODO: close all streams properly
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        BufferedReader br = null;
        InputStreamReader isr = null;
        StringBuilder sb = new StringBuilder();
        try {

            String lineSep = System.getProperty("line.separator");//NOI18N
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(lineSep);
                line = br.readLine();
            }
        } finally {
            isr.close();
            br.close();
            is.close();
        }
        return sb.toString();
    }
}
