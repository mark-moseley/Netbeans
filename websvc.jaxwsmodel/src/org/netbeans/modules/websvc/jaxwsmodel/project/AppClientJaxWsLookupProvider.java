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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
public class AppClientJaxWsLookupProvider implements LookupProvider {
    
    private String JAX_WS_XML_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"; //NOI18N
    private String JAX_WS_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jaxws-app-client.xsl"; //NOI18N
    private String JAXWS_EXTENSION = "jaxws"; //NOI18N
    
    /** Creates a new instance of JaxWSLookupProvider */
    public AppClientJaxWsLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        if (prj==null) return null;
        final JaxWsModel jaxWsModel = getJaxWsModel(prj);
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            private FileChangeListener jaxWsListener;
            private FileChangeListener jaxWsCreationListener;
            
            protected void projectOpened() {
                if (jaxWsModel!=null) { 
                    AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                    if (ext != null) {
                        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                        try {
                            if (jaxws_build==null && jaxWsModel.getClients().length>0) {
                                // generate nbproject/jaxws-build.xml
                                // add jaxws extension
                                addJaxWsExtension(prj, JAX_WS_STYLESHEET_RESOURCE, ext);
                            } else if (jaxWsModel.getClients().length==0) {
                                // remove nbproject/jaxws-build.xml
                                // remove the jaxws extension
                                removeJaxWsExtension(prj, jaxws_build, ext);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        FileObject jaxws_fo = getJaxWsFileObject(prj);
                        if (jaxws_fo!=null) {                     
                            jaxWsListener = new FileChangeAdapter() {
                                public void fileChanged(FileEvent fe) {
                                    handleJaxsClientBuildScript();
                                }
                            };  
                            jaxws_fo.addFileChangeListener(jaxWsListener);
                        } else {
                            FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
                            if (nbprojectDir!=null) {
                                jaxWsCreationListener = new FileChangeAdapter() {
                                    public void fileDataCreated(FileEvent fe) {
                                        if ("jax-ws.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                                            FileObject jaxws_fo = getJaxWsFileObject(prj);
                                            if (jaxws_fo!=null) {
                                                jaxWsListener = new FileChangeAdapter() {
                                                    public void fileChanged(FileEvent fe) {
                                                        handleJaxsClientBuildScript();
                                                    }
                                                };  
                                                jaxws_fo.addFileChangeListener(jaxWsListener);                                           
                                            }
                                        }
                                    }
                                };
                                nbprojectDir.addFileChangeListener(jaxWsCreationListener);
                            }
                        }
                    }
                }
            }
            protected void projectClosed() {
                FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
                if (nbprojectDir!=null) {
                    nbprojectDir.removeFileChangeListener(jaxWsCreationListener);
                    FileObject jaxws_fo = getJaxWsFileObject(prj);
                    if (jaxws_fo!=null)
                        jaxws_fo.removeFileChangeListener(jaxWsListener);
                }
                
            }
            
            private void handleJaxsClientBuildScript() {
                AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                if (ext != null) {
                    FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                    try {
                        if (jaxWsModel.getClients().length==0) {
                            // remove nbproject/jaxws-build.xml
                            // remove the jaxws extension
                            removeJaxWsExtension(prj, jaxws_build, ext);
                        } else {
                            // re-generate nbproject/jaxws-build.xml
                            // add jaxws extension, if needed
                            addJaxWsExtension(prj, JAX_WS_STYLESHEET_RESOURCE, ext);                            
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        return Lookups.fixed(new Object[] {
            openhook,
            jaxWsModel 
        });
    }
    
    private JaxWsModel getJaxWsModel(Project prj) {
        try {
            FileObject fo = getJaxWsFileObject(prj);
            if (fo==null)
                return JaxWsModelProvider.getDefault().getJaxWsModel(
                        WSUtils.class.getResourceAsStream(JAX_WS_XML_RESOURCE));
            else {
                JaxWsModel jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(fo);
                return jaxWsModel;
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
    
    private FileObject getJaxWsFileObject(Project prj) {
        FileObject jaxWsFo = findJaxWsFileObject(prj);
        return jaxWsFo;
    }
    
    public FileObject findJaxWsFileObject(Project prj) {
        return prj.getProjectDirectory().getFileObject(TransformerUtils.JAX_WS_XML_PATH);
    }
    
    private void addJaxWsExtension(
                        final Project prj, 
                        final String styleSheetResource,
                        AntBuildExtender ext) throws IOException {
        
        TransformerUtils.transformClients(prj.getProjectDirectory(), styleSheetResource);
        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        assert jaxws_build!=null;
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension==null) {
            extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
            //adding dependencies
            extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
            extension.addDependency("-do-compile", "wsimport-client-compile"); //NOI18N
            extension.addDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
            ProjectManager.getDefault().saveProject(prj);
        }
    }
    
    private void removeJaxWsExtension(
                        Project prj,
                        FileObject jaxws_build, 
                        AntBuildExtender ext) throws IOException {
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension!=null) {
            ext.removeExtension(JAXWS_EXTENSION);
            ProjectManager.getDefault().saveProject(prj);
        }
        if (jaxws_build!=null) {
            FileLock fileLock = jaxws_build.lock();
            if (fileLock!=null) {
                try {
                    jaxws_build.delete(fileLock);
                } finally {
                    fileLock.releaseLock();
                }
            }
        }

    }
}
