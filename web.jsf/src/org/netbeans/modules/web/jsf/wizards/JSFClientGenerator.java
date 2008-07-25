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

package org.netbeans.modules.web.jsf.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.EmbeddedPkSupport;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.palette.items.JsfForm;
import org.netbeans.modules.web.jsf.palette.items.JsfTable;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.TypeInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.MethodInfo;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Buzek
 * @author mbohm
 */
public class JSFClientGenerator {
    
    private static final String WELCOME_JSF_PAGE = "welcomeJSF.jsp";  //NOI18N
    private static final String JSFCRUD_STYLESHEET = "jsfcrud.css"; //NOI18N
    private static final String JSFCRUD_JAVASCRIPT = "jsfcrud.js"; //NOI18N
    private static final String JSPF_FOLDER = "WEB-INF/jspf"; //NOI18N
    private static final String JSFCRUD_AJAX_JSPF = "AjaxScripts.jspf"; //NOI18N
    private static final String JSFCRUD_AJAX_BUSY_IMAGE = "busy.gif"; //NOI18N
    static final String RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/resources/"; //NOI18N
    static final int PROGRESS_STEP_COUNT = 8;
    
    public static void generateJSFPages(ProgressContributor progressContributor, ProgressPanel progressPanel, final Project project, final String entityClass, String jsfFolderBase, String jsfFolderName, final String controllerPackage, final String controllerClass, FileObject pkg, FileObject controllerFileObject, final EmbeddedPkSupport embeddedPkSupport, final List<String> entities, final boolean ajaxify, String jpaControllerPackage, FileObject jpaControllerFileObject, FileObject converterFileObject, int progressIndex) throws IOException {
        final boolean isInjection = true;//Util.isSupportedJavaEEVersion(project);
        
//        String simpleControllerName = JpaControllerUtil.simpleClassName(controllerClass);
        String simpleControllerName = controllerFileObject.getName();
        
        String progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Controller_Pre", simpleControllerName + ".java");//NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg); 
        
        final String simpleEntityName = JpaControllerUtil.simpleClassName(entityClass);
        String jsfFolder = jsfFolderBase.length() > 0 ? jsfFolderBase + "/" + jsfFolderName : jsfFolderName;
        
//        String simpleConverterName = converterFileObject.getName();
        String simpleConverterName = simpleEntityName + "Converter";
        
//        String jpaControllerSuffix = "JpaController"; //NOI18N
        String jpaControllerClass = ((jpaControllerPackage == null || jpaControllerPackage.length() == 0) ? "" : jpaControllerPackage + ".") + jpaControllerFileObject.getName();
//        String simpleJpaControllerName = simpleEntityName + jpaControllerSuffix;
        
        String utilPackage = ((controllerPackage == null || controllerPackage.length() == 0) ? "" : controllerPackage + ".") + PersistenceClientIterator.UTIL_FOLDER_NAME;
        
        Sources srcs = (Sources) project.getLookup().lookup(Sources.class);
        int lastIndexOfDotInControllerClass = controllerClass.lastIndexOf('.');
        String pkgName = lastIndexOfDotInControllerClass == -1 ? "" : controllerClass.substring(0, lastIndexOfDotInControllerClass);
        
        String persistenceUnit = null;
        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project);
        if (persistenceScopes.length > 0) {
            FileObject persXml = persistenceScopes[0].getPersistenceXml();
            if (persXml != null) {
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
                PersistenceUnit units[] = persistence.getPersistenceUnit();
                if (units.length > 0) {
                    persistenceUnit = units[0].getName();
                }
            }
        }
        SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        FileObject pagesRootFolder = sgWeb[0].getRootFolder();
        int jsfFolderNameAttemptIndex = 1;
        while (pagesRootFolder.getFileObject(jsfFolder) != null && jsfFolderNameAttemptIndex < 1000) {
            jsfFolder += "_" + jsfFolderNameAttemptIndex++;
        }
        final FileObject jsfRoot = FileUtil.createFolder(pagesRootFolder, jsfFolder);
        
//        int lastIndexOfController = controllerClass.lastIndexOf("Controller");
//        String controllerSuffix = controllerClass.substring(lastIndexOfController);
//        String converterSuffix = controllerSuffix.replace("Controller", "Converter");
//        String simpleConverterName = simpleEntityName + converterSuffix; //NOI18N
//        int converterNameAttemptIndex = 1;
//        while (pkg.getFileObject(simpleConverterName, "java") != null && converterNameAttemptIndex < 1000) {
//            simpleConverterName += "_" + converterNameAttemptIndex++;
//        }
        String converterName = ((pkgName == null || pkgName.length() == 0) ? "" : pkgName + ".") + simpleConverterName;
        final String fieldName = JpaControllerUtil.fieldFromClassName(simpleEntityName);

        final List<ElementHandle<ExecutableElement>> idGetter = new ArrayList<ElementHandle<ExecutableElement>>();
        final FileObject[] arrEntityClassFO = new FileObject[1];
        final List<ElementHandle<ExecutableElement>> toOneRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final List<ElementHandle<ExecutableElement>> toManyRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final boolean[] fieldAccess = new boolean[] { false };
        final String[] idProperty = new String[1];

        //detect access type
        final ClasspathInfo classpathInfo = ClasspathInfo.create(pkg);
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement jc = controller.getElements().getTypeElement(entityClass);
                arrEntityClassFO[0] = org.netbeans.api.java.source.SourceUtils.getFile(jc, controller.getClasspathInfo());
                fieldAccess[0] = JpaControllerUtil.isFieldAccess(jc);
                for (ExecutableElement method : JpaControllerUtil.getEntityMethods(jc)) {
                    String methodName = method.getSimpleName().toString();
                    if (methodName.startsWith("get")) {
                        Element f = fieldAccess[0] ? JpaControllerUtil.guessField(controller, method) : method;
                        if (f != null) {
                            if (JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.Id") ||
                                    JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.EmbeddedId")) {
                                idGetter.add(ElementHandle.create(method));
                                idProperty[0] = JpaControllerUtil.getPropNameFromMethod(methodName);
                            } else if (JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToOne") ||
                                    JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToOne")) {
                                toOneRelMethods.add(ElementHandle.create(method));
                            } else if (JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToMany") ||
                                    JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToMany")) {
                                toManyRelMethods.add(ElementHandle.create(method));
                            }
                        }
                    }
                }
            }
        }, true);
        
        if (idGetter.size() < 1) {
            String msg = entityClass + ": " + NbBundle.getMessage(JSFClientGenerator.class, "ERR_GenJsfPages_CouldNotFindIdProperty"); //NOI18N
            if (fieldAccess[0]) {
                msg += " " + NbBundle.getMessage(JSFClientGenerator.class, "ERR_GenJsfPages_EnsureSimpleIdNaming"); //NOI18N
            }
            throw new IOException(msg);
        }
        
        //now done in JpaControllerGenerator
//        if (arrEntityClassFO[0] != null) {
//            addImplementsClause(arrEntityClassFO[0], entityClass, "java.io.Serializable"); //NOI18N
//        }
            
        final BaseDocument doc = new BaseDocument(false, "text/x-jsp");
        WebModule wm = WebModule.getWebModule(jsfRoot);
        
        //automatically add JSF framework if it is not added
        JSFFrameworkProvider fp = new JSFFrameworkProvider();
        if (!fp.isInWebModule(wm)) {
            ExtenderController ec = ExtenderController.create();
            String j2eeLevel = wm.getJ2eePlatformVersion();
            ec.getProperties().setProperty("j2eeLevel", j2eeLevel);
            J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            if (moduleProvider != null) {
                String serverInstanceID = moduleProvider.getServerInstanceID();
                ec.getProperties().setProperty("serverInstanceID", serverInstanceID);
            }
            WebModuleExtender wme = fp.createWebModuleExtender(wm, ec);
            wme.update();
            wme.extend(wm);
        }
        
        FileObject dd = wm.getDeploymentDescriptor();
        WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
        if (ajaxify && ddRoot != null) {
            boolean foundAjaxInitParam = false;
            Servlet servlet = ConfigurationUtils.getFacesServlet(wm);
            InitParam[] initParams = servlet.getInitParam();
            for (InitParam initParam : initParams) {
                if ("javax.faces.LIFECYCLE_ID".equals(initParam.getParamName()) &&
                        "com.sun.faces.lifecycle.PARTIAL".equals(initParam.getParamValue())) {
                    foundAjaxInitParam = true;
                    break;
                }
            }
            if (!foundAjaxInitParam) {
                InitParam contextParam = null;
                try {
                    contextParam = (InitParam)servlet.createBean("InitParam"); // NOI18N
                } catch (ClassNotFoundException cnfe) {
                    Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.WARNING, "CNFE attempting to create javax.faces.LIFECYCLE_ID init parameter in web.xml", cnfe);
                }
                contextParam.setParamName("javax.faces.LIFECYCLE_ID"); // NOI18N
                contextParam.setParamValue("com.sun.faces.lifecycle.PARTIAL"); // NOI18N
                servlet.addInitParam(contextParam);
            }
            ddRoot.write(dd);
        }
        
        String projectEncoding = JpaControllerUtil.getProjectEncodingAsString(project, controllerFileObject);
        
        if (wm.getDocumentBase().getFileObject(WELCOME_JSF_PAGE) == null) {
//            String content = JSFFrameworkProvider.readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + WELCOME_JSF_PAGE), "UTF-8"); //NOI18N
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + WELCOME_JSF_PAGE), "UTF-8"); //NOI18N
//            Charset encoding = FileEncodingQuery.getDefaultEncoding();
            content = content.replaceAll("__ENCODING__", projectEncoding);
            FileObject target = FileUtil.createData(wm.getDocumentBase(), WELCOME_JSF_PAGE);//NOI18N
            JSFFrameworkProvider.createFile(target, content, projectEncoding);  //NOI18N
        }
        
        //FileObject jsfFolderBaseFileObject = jsfFolderBase.length() > 0 ? pagesRootFolder.getFileObject(jsfFolderBase) : pagesRootFolder;
        if (pagesRootFolder.getFileObject(JSFCRUD_STYLESHEET) == null) {
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_STYLESHEET), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(pagesRootFolder, JSFCRUD_STYLESHEET);//NOI18N
            JSFFrameworkProvider.createFile(target, content, projectEncoding);  //NOI18N
        }
        
        //final String styleHrefPrefix = wm.getContextPath() + "/faces/" + (jsfFolderBase.length() > 0 ? jsfFolderBase + "/" : "");
        final String rootRelativePathToWebFolder = wm.getContextPath() + "/faces/";
        
        if (pagesRootFolder.getFileObject(JSFCRUD_JAVASCRIPT) == null) {
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_JAVASCRIPT), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(pagesRootFolder, JSFCRUD_JAVASCRIPT);//NOI18N
            content = content.replaceAll("__WEB_FOLDER_PATH__", rootRelativePathToWebFolder);
            JSFFrameworkProvider.createFile(target, content, projectEncoding);  //NOI18N
        }

        if (ajaxify) {
            String ajaxJspfPath = JSPF_FOLDER + "/" + JSFCRUD_AJAX_JSPF;
            if (pagesRootFolder.getFileObject(ajaxJspfPath) == null) {
                String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_AJAX_JSPF), "UTF-8"); //NOI18N
                FileObject target = FileUtil.createData(pagesRootFolder, ajaxJspfPath);//NOI18N
                JSFFrameworkProvider.createFile(target, content, projectEncoding);  //NOI18N
            }
            
            if (pagesRootFolder.getFileObject(JSFCRUD_AJAX_BUSY_IMAGE) == null) {
                FileObject target = FileUtil.createData(pagesRootFolder, JSFCRUD_AJAX_BUSY_IMAGE);//NOI18N
                FileLock lock = target.lock();
                try {
                    InputStream is = JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_AJAX_BUSY_IMAGE);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    OutputStream os = target.getOutputStream(lock);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    int c;
                    while ((c = bis.read()) != -1) {
                        bos.write(c);
                    }
                    bis.close();
                    bos.close();
                } finally {
                    lock.releaseLock();
                }
            }
        }
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", simpleControllerName + ".java"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        
        controllerFileObject = generateControllerClass(fieldName, pkg, idGetter.get(0), persistenceUnit, controllerPackage, controllerClass, simpleConverterName, 
                entityClass, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection, fieldAccess[0], controllerFileObject, embeddedPkSupport, jpaControllerPackage, jpaControllerClass, utilPackage);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", simpleConverterName + ".java"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        
        final String managedBean =  getManagedBeanName(simpleEntityName);
        converterFileObject = generateConverter(converterFileObject, controllerFileObject, pkg, controllerClass, simpleControllerName, entityClass, 
                simpleEntityName, idGetter.get(0), managedBean, jpaControllerClass, isInjection);
        
        final String styleAndScriptTags = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + rootRelativePathToWebFolder + JSFCRUD_STYLESHEET + "\" />" +
            (ajaxify ? "<%@ include file=\"/" + JSPF_FOLDER + "/" + JSFCRUD_AJAX_JSPF + "\" %><script type=\"text/javascript\" src=\"" + rootRelativePathToWebFolder + JSFCRUD_JAVASCRIPT + "\"></script>" : "");
            
        boolean welcomePageExists = addLinkToListJspIntoIndexJsp(wm, simpleEntityName, styleAndScriptTags, projectEncoding);
        final String linkToIndex = welcomePageExists ? "<br />\n<h:commandLink value=\"Index\" action=\"welcome\" />\n" : "";  //NOI18N

        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + "/List.jsp"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        generateListJsp(project, jsfRoot, classpathInfo, entityClass, simpleEntityName, managedBean, linkToIndex, fieldName, idProperty[0], doc, embeddedPkSupport, styleAndScriptTags, entities, controllerPackage);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + "/New.jsp"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateNewJsp(project, controller, entityClass, simpleEntityName, managedBean, fieldName, toOneRelMethods, fieldAccess[0], linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass, styleAndScriptTags, controllerPackage);
            }
        }, true);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + "/Edit.jsp"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateEditJsp(project, controller, entityClass, simpleEntityName, managedBean, fieldName, linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass, styleAndScriptTags, controllerPackage);
            }
        }, true);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + "/Detail.jsp"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateDetailJsp(project, controller, entityClass, simpleEntityName, managedBean, fieldName, idProperty[0], isInjection, linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass, styleAndScriptTags, entities, controllerPackage);
            }
        }, true);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Updating_Faces_Config", simpleEntityName); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        String facesConfigSimpleControllerName = simpleEntityName + "Controller";
        String facesConfigControllerClass = pkgName.length() == 0 ? facesConfigSimpleControllerName : pkgName + "." + facesConfigSimpleControllerName;
        String facesConfigJsfFolderName = simpleEntityName.substring(0, 1).toLowerCase() + simpleEntityName.substring(1);
        String facesConfigJsfFolder = jsfFolderBase.length() > 0 ? jsfFolderBase + "/" + facesConfigJsfFolderName : facesConfigJsfFolderName;
        addStuffToFacesConfigXml(classpathInfo, wm, managedBean, facesConfigControllerClass, jpaControllerClass, entityClass, converterName, fieldName, facesConfigJsfFolder, idGetter.get(0), pkgName, utilPackage);
    }

    private static boolean addLinkToListJspIntoIndexJsp(WebModule wm, String simpleEntityName, String styleAndScriptTags, String projectEncoding) throws FileNotFoundException, IOException {
        FileObject documentBase = wm.getDocumentBase();
        FileObject indexjsp = documentBase.getFileObject(WELCOME_JSF_PAGE); //NOI18N
        //String indexjspString = "faces/" + WELCOME_JSF_PAGE;
        
        if (indexjsp != null) {
            String content = JSFFrameworkProvider.readResource(indexjsp.getInputStream(), projectEncoding); //NO18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            
            //insert style and script tags if not already present
            if (content.indexOf(styleAndScriptTags) == -1) {
                String justTitleEnd = "</title>"; //NOI18N
                String replaceHeadWith = justTitleEnd + endLine + styleAndScriptTags;    //NOI18N
                content = content.replace(justTitleEnd, replaceHeadWith); //NOI18N
            }
            
            //make sure <f:view> is outside of <html>
            String html = "<html>";
            String htmlEnd = "</html>";
            int htmlIndex = content.indexOf(html);
            int htmlEndIndex = content.indexOf(htmlEnd);
            if (htmlIndex != -1 && htmlEndIndex != -1) {
                String fview = "<f:view>";
                String fviewEnd = "</f:view>";
                int fviewIndex = content.indexOf(fview);
                if (fviewIndex != -1 && fviewIndex > htmlIndex) {
                    content = content.replace(fview, ""); //NOI18N
                    content = content.replace(fviewEnd, ""); //NOI18N
                    String fviewPlusHtml = fview + endLine + html;
                    String htmlEndPlusFviewEnd = htmlEnd + endLine + fviewEnd;
                    content = content.replace(html, fviewPlusHtml); //NOI18N
                    content = content.replace(htmlEnd, htmlEndPlusFviewEnd); //NOI18N
                }
            }
            
            String find = "<h1><h:outputText value=\"JavaServer Faces\" /></h1>"; //NOI18N
            if ( content.indexOf(find) > -1){
                StringBuffer replace = new StringBuffer();
                String findForm = "<h:form>";
                boolean needsForm = content.indexOf(findForm) == -1;
                if (needsForm) {
                    replace.append(findForm);
                    replace.append(endLine);
                }
                replace.append(find);
                replace.append(endLine);
                StringBuffer replaceCrux = new StringBuffer();
                replaceCrux.append("    <br/>");                        //NOI18N
                replaceCrux.append(endLine);
                String managedBeanName = getManagedBeanName(simpleEntityName);
                replaceCrux.append("<h:commandLink action=\"#{" + managedBeanName + ".listSetup}\" value=\"");
                replaceCrux.append("Show All " + simpleEntityName + " Items");
                replaceCrux.append("\"/>");
                replaceCrux.append(endLine);
                if (content.indexOf(replaceCrux.toString()) > -1) {
                    //return, indicating welcomeJsp exists
                    return true;
                }
                replace.append(replaceCrux);
                if (needsForm) {
                    replace.append("</h:form>");
                    replace.append(endLine);
                }
                content = content.replace(find, replace.toString()); //NOI18N
                JSFFrameworkProvider.createFile(indexjsp, content, projectEncoding); //NOI18N
                //return, indicating welcomeJsp exists
                return true;
            }
        }
        return false;
    }

    private static void generateListJsp(Project project, final FileObject jsfRoot, ClasspathInfo classpathInfo, final String entityClass, String simpleEntityName, 
            final String managedBean, String linkToIndex, final String fieldName, String idProperty, BaseDocument doc, final EmbeddedPkSupport embeddedPkSupport, String styleAndScriptTags, List<String> entities, String controllerPackage) throws FileStateInvalidException, IOException {
        final String tableVarName = JsfForm.getFreeTableVarName("item", entities); //NOI18N
        FileSystem fs = jsfRoot.getFileSystem();
        final StringBuffer listSb = new StringBuffer();
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);
        listSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<f:view>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>Listing " + simpleEntityName + " Items</title>\n"
                + styleAndScriptTags
                + "\n</head>\n<body>\n<h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        listSb.append("<h1>Listing " + simpleEntityName + " Items</h1>\n");
        listSb.append("<h:form styleClass=\"jsfcrud_list_form\">\n");
        listSb.append("<h:outputText escape=\"false\" value=\"(No " + simpleEntityName + " Items Found)<br />\" rendered=\"#{" + managedBean + ".pagingInfo.itemCount == 0}\" />\n");
        listSb.append("<h:panelGroup rendered=\"#{" + managedBean + ".pagingInfo.itemCount > 0}\">\n");
        listSb.append(MessageFormat.format("<h:outputText value=\"Item #'{'{0}.pagingInfo.firstItem + 1'}'..#'{'{0}.pagingInfo.lastItem'}' of #'{'{0}.pagingInfo.itemCount}\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.prev'}'\" value=\"Previous #'{'{0}.pagingInfo.batchSize'}'\" rendered=\"#'{'{0}.pagingInfo.firstItem >= {0}.pagingInfo.batchSize'}'\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Next #'{'{0}.pagingInfo.batchSize'}'\" rendered=\"#'{'{0}.pagingInfo.lastItem + {0}.pagingInfo.batchSize <= {0}.pagingInfo.itemCount}\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Remaining #'{'{0}.pagingInfo.itemCount - {0}.pagingInfo.lastItem'}'\"\n"
                + "rendered=\"#'{'{0}.pagingInfo.lastItem < {0}.pagingInfo.itemCount && {0}.pagingInfo.lastItem + {0}.pagingInfo.batchSize > {0}.pagingInfo.itemCount'}'\"/>\n", managedBean));
        listSb.append("<h:dataTable value=\"#{" + managedBean + "." + fieldName + "Items}\" var=\"" + tableVarName + "\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" rowClasses=\"jsfcrud_odd_row,jsfcrud_even_row\" rules=\"all\" style=\"border:solid 1px\">\n");
        
        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        final  String commands = "<h:column>\n <f:facet name=\"header\">\n <h:outputText escape=\"false\" value=\"&nbsp;\"/>\n </f:facet>\n"
                + "<h:commandLink value=\"Show\" action=\"#'{'" + managedBean + ".detailSetup'}'\">\n" 
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{0}][" + managedBean + ".converter].jsfcrud_invoke'}'\"/>\n"               
                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                + "<h:commandLink value=\"Edit\" action=\"#'{'" + managedBean + ".editSetup'}'\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{0}][" + managedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                + "<h:commandLink value=\"Destroy\" action=\"#'{'" + managedBean + ".destroy'}'\">\n" 
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'jsfcrud_class[''" + jsfUtilClass + "''].jsfcrud_method[''getAsConvertedString''][{0}][" + managedBean + ".converter].jsfcrud_invoke'}'\"/>\n"
                + "</h:commandLink>\n </h:column>\n";
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                JsfTable.createTable(controller, typeElement, managedBean + "." + fieldName, listSb, commands, embeddedPkSupport, tableVarName);
            }
        }, true);
        listSb.append("</h:dataTable>\n</h:panelGroup>\n");
        listSb.append("<br />\n<h:commandLink action=\"#{" + managedBean + ".createSetup}\" value=\"New " + simpleEntityName + "\"/>\n"
                + linkToIndex + "\n");
        listSb.append("</h:form>\n</body>\n</html>\n</f:view>\n");
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, listSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            listSb.replace(0, listSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }
        
        final String listText = listSb.toString();

        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject list = FileUtil.createData(jsfRoot, "List.jsp");//NOI18N
                FileLock lock = list.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(list.getOutputStream(lock), encoding));
                    bw.write(listText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }
    
    private static void generateNewJsp(Project project, CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            List<ElementHandle<ExecutableElement>> toOneRelMethods, boolean fieldAccess, String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass, String styleAndScriptTags, String controllerPackage) throws FileStateInvalidException, IOException {
        StringBuffer newSb = new StringBuffer();
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);
        newSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<f:view>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>New " + simpleEntityName + "</title>\n"
                + styleAndScriptTags
                + "\n</head>\n<body>\n<h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        newSb.append("<h1>New " + simpleEntityName + "</h1>\n");
        newSb.append("<h:form>\n  <h:inputHidden id=\"validateCreateField\" validator=\"#{" + managedBean + ".validateCreate}\" value=\"value\"/>\n <h:panelGrid columns=\"2\">\n");
        
        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_NEW, managedBean + "." + fieldName, newSb, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
        newSb.append("</h:panelGrid>\n<br />\n");
        
        newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\"/>\n<br />\n");
        
        newSb.append("<br />\n<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + " Items\" immediate=\"true\"/>\n " + linkToIndex
                + "</h:form>\n</body>\n</html>\n</f:view>\n");
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, newSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            newSb.replace(0, newSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }
        final String newText = newSb.toString();

        FileSystem fs = jsfRoot.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject newForm = FileUtil.createData(jsfRoot, "New.jsp");//NOI18N
                FileLock lock = newForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(newForm.getOutputStream(lock), encoding));
                    bw.write(newText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }
    
    private static void generateEditJsp(Project project, CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass, String styleAndScriptTags, String controllerPackage) throws FileStateInvalidException, IOException {
        StringBuffer editSb = new StringBuffer();
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);
        editSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<f:view>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>Editing " + simpleEntityName + "</title>\n"
                + styleAndScriptTags
                + "\n</head>\n<body>\n<h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        editSb.append("<h1>Editing " + simpleEntityName + "</h1>\n");
        editSb.append("<h:form>\n"
                + "<h:panelGrid columns=\"2\">\n");
        
        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);        
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_EDIT, managedBean + "." + fieldName, editSb, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
        editSb.append("</h:panelGrid>\n<br />\n<h:commandLink action=\"#{" + managedBean + ".edit}\" value=\"Save\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{jsfcrud_class['" + jsfUtilClass + "'].jsfcrud_method['getAsConvertedString'][" + managedBean + "." + fieldName + "][" + managedBean + ".converter].jsfcrud_invoke}\"/>\n"
                + "</h:commandLink>\n"
                + "<br />\n<br />\n"
                + "<h:commandLink action=\"#{" + managedBean + ".detailSetup}\" value=\"Show\" immediate=\"true\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{jsfcrud_class['" + jsfUtilClass + "'].jsfcrud_method['getAsConvertedString'][" + managedBean + "." + fieldName + "][" + managedBean + ".converter].jsfcrud_invoke}\"/>\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + " Items\" immediate=\"true\"/>\n" + linkToIndex
                + "</h:form>\n</body>\n</html>\n</f:view>\n");

        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, editSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            editSb.replace(0, editSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }

        final String editText = editSb.toString();

        FileSystem fs = jsfRoot.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject editForm = FileUtil.createData(jsfRoot, "Edit.jsp");//NOI18N
                FileLock lock = editForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(editForm.getOutputStream(lock), encoding));
                    bw.write(editText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }

    private static void generateDetailJsp(Project project, CompilationController controller, String entityClass, String simpleEntityName, String managedBean, 
            String fieldName, String idProperty, boolean isInjection, String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass, String styleAndScriptTags, List<String> entities, String controllerPackage) throws FileStateInvalidException, IOException {
        StringBuffer detailSb = new StringBuffer();
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);
        detailSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<f:view>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>" + simpleEntityName + " Detail</title>\n"
                + styleAndScriptTags
                + "\n</head>\n<body>\n<h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        detailSb.append("<h1>" + simpleEntityName + " Detail</h1>\n");
        detailSb.append("<h:form>\n  <h:panelGrid columns=\"2\">\n");
        
        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, detailSb, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
        JsfForm.createTablesForRelated(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, idProperty, isInjection, detailSb, embeddedPkSupport, controllerClass, entities, jsfUtilClass);
        detailSb.append("</h:panelGrid>\n");
        detailSb.append("<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".destroy}\" value=\"Destroy\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{jsfcrud_class['" + jsfUtilClass + "'].jsfcrud_method['getAsConvertedString'][" + managedBean + "." + fieldName + "][" + managedBean + ".converter].jsfcrud_invoke}\" />\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".editSetup}\" value=\"Edit\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{jsfcrud_class['" + jsfUtilClass + "'].jsfcrud_method['getAsConvertedString'][" + managedBean + "." + fieldName + "][" + managedBean + ".converter].jsfcrud_invoke}\" />\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".createSetup}\" value=\"New " + simpleEntityName + "\" />\n<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + " Items\"/>\n" + linkToIndex
                + "</h:form>\n</body>\n</html>\n</f:view>\n");

        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, detailSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            detailSb.replace(0, detailSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }

        final String detailText = detailSb.toString();

        FileSystem fs = jsfRoot.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject detailForm = FileUtil.createData(jsfRoot, "Detail.jsp");//NOI18N
                FileLock lock = detailForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(detailForm.getOutputStream(lock), encoding));
                    bw.write(detailText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }

    private static void addStuffToFacesConfigXml(ClasspathInfo classpathInfo, WebModule wm, String managedBean, String controllerClass, String jpaControllerClass, String entityClass, 
            String converterName, String fieldName, String jsfFolder, final ElementHandle<ExecutableElement> idGetterHandle, String pkgName, String utilPackage) {
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(wm);
        if (configFiles.length > 0) {
            // using first found faces-config.xml, is it OK?
            FileObject fo = configFiles[0];
            JSFConfigModel model = null;
            try {
                model = ConfigurationUtils.getConfigModel(fo, true);
                model.startTransaction();
                FacesConfig config = model.getRootComponent();
                
                boolean resolverFound = false;
                final String elResolverTagName = "el-resolver"; //NOI18N
                String resolverClass = utilPackage + ".JsfCrudELResolver"; //NOI18N
                List<Application> applications = config.getApplications();
                applicationsLoop:
                for (Application existingApplication : applications) {
                    org.w3c.dom.Element existingApplicationPeer = existingApplication.getPeer();
                    org.w3c.dom.NodeList elResolverNodes = existingApplicationPeer.getElementsByTagName(elResolverTagName);
                    for (int i = 0; i < elResolverNodes.getLength(); i++) {
                        org.w3c.dom.Node elResolverNode = elResolverNodes.item(i);
                        org.w3c.dom.NodeList elResolverNodeChildren = elResolverNode.getChildNodes();
                        for (int j = 0; j < elResolverNodeChildren.getLength(); j++) {
                            org.w3c.dom.Node elResolverNodeChild = elResolverNodeChildren.item(j);
                            if (resolverClass.equals(elResolverNodeChild.getNodeValue())) {
                                resolverFound = true;
                                break applicationsLoop;
                            }
                        }
                    }
                }

                if (!resolverFound) {
                    org.w3c.dom.Element configPeer = config.getPeer();
                    org.w3c.dom.Document doc = configPeer.getOwnerDocument();
                    org.w3c.dom.Element elRes = doc.createElement(elResolverTagName);
                    org.w3c.dom.Text text = doc.createTextNode(resolverClass);
                    elRes.appendChild(text);
                    Application appl = model.getFactory().createApplication();
                    org.w3c.dom.Element applPeer = appl.getPeer();
                    applPeer.appendChild(elRes);
                    config.addApplication(appl);
                }
                
                addNavigationRuleToFacesConfig(model, config, "welcome", "/welcomeJSF.jsp");
                
                addManagedBeanToFacesConfig(model, config, managedBean, controllerClass);
                addManagedBeanToFacesConfig(model, config, managedBean + "Jpa", jpaControllerClass);   //NOI18N
                
                Converter cv = null;
                List<Converter> converters = config.getConverters();
                for (Converter existingConverter : converters) {
                    if (entityClass.equals(existingConverter.getConverterForClass())) {
                        cv = existingConverter;
                        break;
                    }
                }
                boolean cvIsNew = false;
                if (cv == null) {
                    cv = model.getFactory().createConverter();
                    cvIsNew = true;
                }
                cv.setConverterForClass(entityClass);
                cv.setConverterClass(converterName);
                if (cvIsNew) {
                    config.addConverter(cv);
                }
                
                String[] fromOutcomes = {
                    fieldName + "_create", 
                    fieldName + "_list", 
                    fieldName + "_edit",
                    fieldName + "_detail"
                };
                String[] toViewIds = {
                    "/" + jsfFolder + "/New.jsp", 
                    "/" + jsfFolder + "/List.jsp",  
                    "/" + jsfFolder + "/Edit.jsp", 
                    "/" + jsfFolder + "/Detail.jsp", 
                };
                
                for (int i = 0; i < fromOutcomes.length; i++) {
                    addNavigationRuleToFacesConfig(model, config, fromOutcomes[i], toViewIds[i]);
                }
            }
            finally {
                //TODO: RETOUCHE correct write to JSF model?
                model.endTransaction();
            }
        }
    }
    
    private static void addManagedBeanToFacesConfig(JSFConfigModel model, FacesConfig config, String managedBean, String managedBeanClass) {
        ManagedBean mb = null;
        List<ManagedBean> managedBeans = config.getManagedBeans();
        for (ManagedBean existingManagedBean : managedBeans) {
            if (managedBean.equals(existingManagedBean.getManagedBeanName())) {
                mb = existingManagedBean;
                break;
            }
        }
        boolean mbIsNew = false;
        if (mb == null) {
            mb = model.getFactory().createManagedBean();
            mbIsNew = true;
        }
        mb.setManagedBeanName(managedBean);
        mb.setManagedBeanClass(managedBeanClass);
        mb.setManagedBeanScope(ManagedBean.Scope.SESSION);
        if (mbIsNew) {
            config.addManagedBean(mb);
        }
    }
    
    private static void addNavigationRuleToFacesConfig(JSFConfigModel model, FacesConfig config, String fromOutcome, String toViewId) {
        NavigationRule nr = null;
        NavigationCase nc = null;
        List<NavigationRule> navigationRules = config.getNavigationRules();
        for (NavigationRule existingNavigationRule : navigationRules) {
            List<NavigationCase> navigationCases = existingNavigationRule.getNavigationCases();
            for (NavigationCase existingNavigationCase : navigationCases) {
                if ( fromOutcome.equals(existingNavigationCase.getFromOutcome()) ) {
                    nr = existingNavigationRule;
                    nc = existingNavigationCase;
                    break;
                }
            }
        }
        boolean nrIsNew = false;
        if (nr == null) {
            nr = model.getFactory().createNavigationRule();
            nc = model.getFactory().createNavigationCase();
            nrIsNew = true;
        }

        nc.setFromOutcome(fromOutcome);
        nc.setToViewId(toViewId);
        if (nrIsNew) {
            nr.addNavigationCase(nc);
            config.addNavigationRule(nr);
        }
    }
    
    private static FileObject generateConverter(
            final FileObject converterFileObject,
            final FileObject controllerFileObject,
            final FileObject pkg,
            final String controllerClass,
            final String simpleControllerName,
            final String entityClass,
            final String simpleEntityName,
            final ElementHandle<ExecutableElement> idGetter,
            final String managedBeanName,
            final String jpaControllerClass,
            final boolean isInjection) throws IOException {

        final boolean[] embeddable = new boolean[] { false };
        final String[] idClassSimpleName = new String[1];
        final String[] idPropertyType = new String[1];
        final ArrayList<MethodModel> paramSetters = new ArrayList<MethodModel>();
        //final boolean[] fieldAccess = new boolean[] { false };
        final String[] idGetterName = new String[1];
        JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
        controllerJavaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController compilationController) throws IOException {
                compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement idGetterElement = idGetter.resolve(compilationController);
                idGetterName[0] = idGetterElement.getSimpleName().toString();
                TypeMirror idType = idGetterElement.getReturnType();
                if (TypeKind.DECLARED == idType.getKind()) {
                    DeclaredType declaredType = (DeclaredType) idType;
                    TypeElement idClass = (TypeElement) declaredType.asElement();
                    embeddable[0] = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                    idClassSimpleName[0] = idClass.getSimpleName().toString();
                    idPropertyType[0] = idClass.getQualifiedName().toString();
                    for (ExecutableElement method : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                        if (method.getSimpleName().toString().startsWith("set")) {
                            paramSetters.add(MethodModelSupport.createMethodModel(compilationController, method));
                        }
                    }
                }
            }
        }, true);
        
        String controllerReferenceName = controllerClass;
        StringBuffer getAsObjectBody = new StringBuffer();
        getAsObjectBody.append("if (string == null || string.length() == 0) {\n return null;\n }\n");

        String controllerVariable;
        if (isInjection) {
            controllerVariable = jpaControllerClass + " controller = (" 
                    + jpaControllerClass 
                    + ") facesContext.getApplication().getELResolver().getValue(\nfacesContext.getELContext(), null, \"" 
                    + managedBeanName + "Jpa\");\n";
        } else {
            controllerVariable = jpaControllerClass + " controller = ("
                    + jpaControllerClass 
                    + ") facesContext.getApplication().getVariableResolver().resolveVariable(\nfacesContext, \"" 
                    + managedBeanName + "Jpa\");\n";
        }
        if (embeddable[0]) {
            getAsObjectBody.append(idPropertyType[0] + " id = getId(string);\n");
            getAsObjectBody.append(controllerVariable + "\n return controller.find" + simpleEntityName + "(id);");
        } else {
            getAsObjectBody.append(createIdFieldDeclaration(idPropertyType[0], "string") + "\n"
                    + controllerVariable
                    + "\n return controller.find" + simpleEntityName + "(id);");
        }
        
        final MethodModel getAsObject = MethodModel.create(
                "getAsObject",
                "java.lang.Object",
                getAsObjectBody.toString(),
                Arrays.asList(
                    MethodModel.Variable.create("javax.faces.context.FacesContext", "facesContext"),
                    MethodModel.Variable.create("javax.faces.component.UIComponent", "component"),
                    MethodModel.Variable.create("java.lang.String", "string")
                ),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );
        
        StringBuffer getIdBody = null;
        if (embeddable[0]) {
            getIdBody = new StringBuffer();
            getIdBody.append(idPropertyType[0] + " id = new " + idPropertyType[0] + "();\n");
            int params = paramSetters.size();
            getIdBody.append("String params[] = new String[" + params + "];\n" +
                    "int p = 0;\n" +
                    "int grabStart = 0;\n" +
                    "String delim = \"#\";\n" +
                    "String escape = \"~\";\n" +
                    "Pattern pattern = Pattern.compile(escape + \"*\" + delim);\n" +
                    "Matcher matcher = pattern.matcher(string);\n" +
                    "while (matcher.find()) {\n" +
                    "String found = matcher.group();\n" +
                    "if (found.length() % 2 == 1) {\n" +
                    "params[p] = string.substring(grabStart, matcher.start());\n" +
                    "p++;\n" +
                    "grabStart = matcher.end();\n" +
                    "}\n" +
                    "}\n" +
                    "if (p != params.length - 1) {\n" +
                    "throw new IllegalArgumentException(\"string \" + string + \" is not in expected format. expected " + params + " ids delimited by \" + delim);\n" +
                    "}\n" +
                    "params[p] = string.substring(grabStart);\n" +
                    "for (int i = 0; i < params.length; i++) {\n" +
                    "params[i] = params[i].replace(escape + delim, delim);\n" +
                    "params[i] = params[i].replace(escape + escape, escape);\n" +
                    "}\n\n"
                    );
                    
            for (int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                String type = setter.getParameters().get(0).getType();
                getIdBody.append("id." + setter.getName() + "(" 
                        + createIdFieldInitialization(type, "params[" + i + "]") + ");\n");
            }
            
            getIdBody.append("return id;\n");
        }
        
        final MethodModel getId = embeddable[0] ? MethodModel.create(
                "getId",
                idPropertyType[0],
                getIdBody.toString(),
                Arrays.asList(
                    MethodModel.Variable.create("java.lang.String", "string")
                ),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()    //no modifiers 
                ) : null;

        String entityReferenceName = entityClass;
        StringBuffer getAsStringBody = new StringBuffer();
        getAsStringBody.append("if (object == null) {\n return null;\n }\n"
                + "if(object instanceof " + entityReferenceName + ") {\n"
                + entityReferenceName + " o = (" + entityReferenceName +") object;\n");
        if (embeddable[0]) {
            getAsStringBody.append(idPropertyType[0] + " id  = o." + idGetterName[0] + "();\n" +
                    "if (id == null) {\n" +
                    "return \"\";\n" +
                    "}\n" +
                    "String delim = \"#\";\n" +
                    "String escape = \"~\";\n\n"               
                    );
            for(int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                String propName = JpaControllerUtil.getPropNameFromMethod(setter.getName());
                String type = setter.getParameters().get(0).getType();
                boolean isString = "String".equals(type) || "java.lang.String".equals(type);
                boolean isPrimitive = "boolean".equals(type) || "char".equals(type) ||
                        "double".equals(type) || "float".equals(type) || "int".equals(type) || "long".equals(type);
                if (isString) {
                    getAsStringBody.append("String " + propName + " = id.g" + setter.getName().substring(1) + "();\n");
                }
                else if (isPrimitive) {
                    getAsStringBody.append("String " + propName + " = String.valueOf(id.g" + setter.getName().substring(1) + "());\n");
                }
                else {
                    getAsStringBody.append("Object " + propName + "Obj = id.g" + setter.getName().substring(1) + "();\n" +
                            "String " + propName + " = " + propName + "Obj == null ? \"\" : String.valueOf(" + propName + "Obj);\n");
                }
                getAsStringBody.append(propName + " = ");
                if (isString) {
                    getAsStringBody.append(propName + " == null ? \"\" : ");
                }
                getAsStringBody.append(propName + ".replace(escape, escape + escape);\n" +
                        propName + " = " + propName + ".replace(delim, escape + delim);\n");
            }
            getAsStringBody.append("return ");
            for(int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                String propName = JpaControllerUtil.getPropNameFromMethod(setter.getName());
                if (i > 0) {
                    getAsStringBody.append(" + delim + ");
                }
                getAsStringBody.append(propName);
            }
            getAsStringBody.append(";\n");
        } else {
            String oDotGetId = "o." + idGetterName[0] + "()";
            getAsStringBody.append("return " + oDotGetId + " == null ? \"\" : " + oDotGetId + ".toString();\n");
        }
        getAsStringBody.append("} else {\n"
                + "throw new IllegalArgumentException(\"object \" + object + \" is of type \" + object.getClass().getName() + \"; expected type: " + entityClass +"\");\n}");
        
        final MethodModel getAsString = MethodModel.create(
                "getAsString",
                "java.lang.String",
                getAsStringBody.toString(),
                Arrays.asList(
                    MethodModel.Variable.create("javax.faces.context.FacesContext", "facesContext"),
                    MethodModel.Variable.create("javax.faces.component.UIComponent", "component"),
                    MethodModel.Variable.create("java.lang.Object", "object")
                ),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );

//        FileObject converterFileObject = GenerationUtils.createClass(pkg, simpleConverterName, null);
        JavaSource converterJavaSource = JavaSource.forFileObject(converterFileObject);
        converterJavaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                TypeElement converterTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree classTree = workingCopy.getTrees().getTree(converterTypeElement);
                ClassTree modifiedClassTree = generationUtils.addImplementsClause(classTree, "javax.faces.convert.Converter");
                MethodTree getAsObjectTree = MethodModelSupport.createMethodTree(workingCopy, getAsObject);
                MethodTree getIdTree = embeddable[0] ? MethodModelSupport.createMethodTree(workingCopy, getId) : null;
                MethodTree getAsStringTree = MethodModelSupport.createMethodTree(workingCopy, getAsString);
                modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsObjectTree);
                if (embeddable[0]) {
                    modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getIdTree);
                }
                modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsStringTree);
                if (embeddable[0]) {
                    String[] importFqs = {"java.util.regex.Pattern",
                                "java.util.regex.Matcher",
                                jpaControllerClass
                    };
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }
                }
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();

        return converterFileObject;
    }
    
    private static FileObject generateControllerClass(
            final String fieldName, 
            final FileObject pkg, 
            final ElementHandle<ExecutableElement> idGetter, 
            final String persistenceUnit, 
            final String controllerPackage,
            final String controllerClass,
            final String simpleConverterName,
            final String entityClass, 
            final String simpleEntityName,
            final List<ElementHandle<ExecutableElement>> toOneRelMethods,
            final List<ElementHandle<ExecutableElement>> toManyRelMethods,
            final boolean isInjection,
            final boolean isFieldAccess,
            final FileObject controllerFileObject, 
            final EmbeddedPkSupport embeddedPkSupport,
            final String jpaControllerPackage,
            final String jpaControllerClass,
            final String utilPackage) throws IOException {
        
            final String[] idPropertyType = new String[1];
            final String[] idGetterName = new String[1];
            final boolean[] embeddable = new boolean[] { false };
            
            JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
            controllerJavaSource.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    ExecutableElement idGetterElement = idGetter.resolve(workingCopy);
                    idGetterName[0] = idGetterElement.getSimpleName().toString();
                    TypeMirror idType = idGetterElement.getReturnType();
                    TypeElement idClass = null;
                    if (TypeKind.DECLARED == idType.getKind()) {
                        DeclaredType declaredType = (DeclaredType) idType;
                        idClass = (TypeElement) declaredType.asElement();
                        embeddable[0] = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                        idPropertyType[0] = idClass.getQualifiedName().toString();
                    }
                    
                    String simpleIdPropertyType = JpaControllerUtil.simpleClassName(idPropertyType[0]);
                    
//                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    TypeElement controllerTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    ClassTree classTree = workingCopy.getTrees().getTree(controllerTypeElement);
                    ClassTree modifiedClassTree = classTree;
                    
                    int privateModifier = java.lang.reflect.Modifier.PRIVATE;
                    int publicModifier = java.lang.reflect.Modifier.PUBLIC;
//                    int publicStaticModifier = publicModifier + java.lang.reflect.Modifier.STATIC;
                    
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName, entityClass, privateModifier, null, null);
                   
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName + "Items", new TypeInfo("java.util.List", new String[]{entityClass}), privateModifier, null, null);
                    
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "jpaController", jpaControllerClass, privateModifier, null, null);
                    
                    String converterClass = ((controllerPackage == null || controllerPackage.length() == 0) ? "" : controllerPackage + ".") + simpleConverterName;
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "converter", converterClass, privateModifier, null, null);
                    
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "pagingInfo", utilPackage + ".PagingInfo", privateModifier, null, null);
                    
                    String bodyText;
                    MethodInfo methodInfo;
                    
                    String managedBeanName = getManagedBeanName(simpleEntityName);
                    bodyText = "FacesContext facesContext = FacesContext.getCurrentInstance();\n" +
                            "jpaController = (" + simpleEntityName + "JpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, \"" + managedBeanName + "Jpa\");\n" +
                            "pagingInfo = new PagingInfo();\n" +
                            "converter = new " + simpleConverterName + "();";
                    methodInfo = new MethodInfo("<init>", publicModifier, "void", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.modifyDefaultConstructor(classTree, modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "if pagingInfo.getItemCount() == -1) {\n" +
                            "pagingInfo.setItemCount(jpaController.get" + simpleEntityName + "Count());\n" +
                            "}\n" +
                            "return pagingInfo;";
                    methodInfo = new MethodInfo("getPagingInfo", publicModifier, utilPackage + ".PagingInfo", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

//                    StringBuffer updateRelatedInCreate = new StringBuffer();
//                    StringBuffer updateRelatedInEditPre = new StringBuffer();
//                    StringBuffer attachRelatedInEdit = new StringBuffer();
//                    StringBuffer updateRelatedInEditPost = new StringBuffer();
//                    StringBuffer updateRelatedInDestroy = new StringBuffer();
//                    StringBuffer initRelatedInCreate = new StringBuffer();
//                    StringBuffer illegalOrphansInCreate = new StringBuffer();
//                    StringBuffer illegalOrphansInEdit = new StringBuffer();
//                    StringBuffer illegalOrphansInDestroy = new StringBuffer();
//                    StringBuffer initCollectionsInCreate = new StringBuffer();  //useful in case user removes listbox from New.jsp

                    List<ElementHandle<ExecutableElement>> allRelMethods = new ArrayList<ElementHandle<ExecutableElement>>(toOneRelMethods);
                    allRelMethods.addAll(toManyRelMethods);
                    
                    String jpaExceptionsPackage = jpaControllerPackage == null || jpaControllerPackage.length() == 0 ? JpaControllerIterator.EXCEPTION_FOLDER_NAME : jpaControllerPackage + "." + JpaControllerIterator.EXCEPTION_FOLDER_NAME;
                    
                    //fixme(mbohm): examine jpa controller create method to determine if it throws IllegalOrphanException
                    boolean methodThrowsIllegalOrphanException = true;
                    
                    String[] importFqs = methodThrowsIllegalOrphanException ? new String[]{
                                "java.lang.reflect.InvocationTargetException",
                                "java.lang.reflect.Method",
                                "javax.faces.FacesException",
                                utilPackage + ".JsfUtil",
                                jpaExceptionsPackage + ".NonexistentEntityException",
                                jpaExceptionsPackage + ".IllegalOrphanException"
                    } : new String[]{
                                "java.lang.reflect.InvocationTargetException",
                                "java.lang.reflect.Method",
                                "javax.faces.FacesException",
                                utilPackage + ".JsfUtil",
                                jpaExceptionsPackage + ".NonexistentEntityException"
                    };
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }
                    
                    if (embeddable[0] && !controllerClass.startsWith(entityClass + "Controller")) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, idPropertyType[0]);
                    }

//                    String oldMe = null;
//            
                    // <editor-fold desc=" all relations ">
//                    for(Iterator<ElementHandle<ExecutableElement>> it = allRelMethods.iterator(); it.hasNext();) {
//                        ElementHandle<ExecutableElement> handle = it.next();
//                        ExecutableElement m = handle.resolve(workingCopy);
//                        int multiplicity = JpaControllerUtil.isRelationship(workingCopy, m, isFieldAccess);
//                        ExecutableElement otherSide = JpaControllerUtil.getOtherSideOfRelation(workingCopy, m, isFieldAccess);
//
//                        if (otherSide != null) {
//                            TypeElement relClass = (TypeElement)otherSide.getEnclosingElement();
//                            boolean isRelFieldAccess = JpaControllerUtil.isFieldAccess(relClass);
//                            int otherSideMultiplicity = JpaControllerUtil.isRelationship(workingCopy, otherSide, isRelFieldAccess);
//                            TypeMirror t = m.getReturnType();
//                            TypeMirror tstripped = JpaControllerUtil.stripCollection(t, workingCopy.getTypes());
//                            boolean isCollection = t != tstripped;
//                            String relType = tstripped.toString();
//                            String simpleRelType = JpaControllerUtil.simpleClassName(relType); //just "Pavilion"
//                            String relTypeReference = simpleRelType;
//                            String mName = m.getSimpleName().toString();
//                            String otherName = otherSide.getSimpleName().toString();
//                            String relFieldName = JpaControllerUtil.getPropNameFromMethod(mName);
//                            String otherFieldName = JpaControllerUtil.getPropNameFromMethod(otherName);
//                            
//                            boolean columnNullable = JpaControllerUtil.isFieldOptionalAndNullable(workingCopy, m, isFieldAccess);
//                            boolean relColumnNullable = JpaControllerUtil.isFieldOptionalAndNullable(workingCopy, otherSide, isFieldAccess);
//                            
//                            String relFieldToAttach = isCollection ? relFieldName + relTypeReference + "ToAttach" : relFieldName;
//                            String scalarRelFieldName = isCollection ? relFieldName + relTypeReference : relFieldName;
//                            
////                            if (!isCollection && !controllerClass.startsWith(entityClass + "Controller")) {
////                                modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, relType);
////                            }
//                            
//                            ExecutableElement relIdGetterElement = JpaControllerUtil.getIdGetter(workingCopy, isFieldAccess, relClass);
//                            String refOrMergeString = JpaControllerGenerator.getRefOrMergeString(relIdGetterElement, relFieldToAttach);
//                            
//                            if (isCollection) {
//                                initCollectionsInCreate.append("if (" + fieldName + "." + mName + "() == null) {\n" +
//                                        fieldName + ".s" + mName.substring(1) + "(new ArrayList<" + relTypeReference + ">());\n" +
//                                        "}\n");
//
//                                
////                                modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.ArrayList");
//                                
//                                initRelatedInCreate.append("List<" + relTypeReference + "> attached" + mName.substring(3) + " = new ArrayList<" + relTypeReference + ">();\n" +
//                                        "for (" + relTypeReference + " " + relFieldToAttach + " : " + fieldName + "." + mName + "()) {\n" +
//                                        relFieldToAttach + " = " + refOrMergeString +
//                                        "attached" + mName.substring(3) + ".add(" + relFieldToAttach + ");\n" +
//                                        "}\n" +
//                                        fieldName + ".s" + mName.substring(1) + "(attached" + mName.substring(3) + ");\n"
//                                        );
//                            }
//                            else {
//                                initRelatedInCreate.append(relTypeReference + " " + scalarRelFieldName + " = " + fieldName + "." + mName +"();\n" +
//                                    "if (" + scalarRelFieldName + " != null) {\n" +
//                                    scalarRelFieldName + " = " + refOrMergeString +
//                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + ");\n" +
//                                    "}\n");
//                            }
//                            
//                            String relrelInstanceName = "old" + otherName.substring(3) + "Of" + scalarRelFieldName.substring(0, 1).toUpperCase() + (scalarRelFieldName.length() > 1 ? scalarRelFieldName.substring(1) : "");
//                            String relrelGetterName = otherName;
//                            
//                            if (!columnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && multiplicity == JpaControllerUtil.REL_TO_ONE) {
//                                illegalOrphansInCreate.append(
//                                        relTypeReference + " " + scalarRelFieldName + "OrphanCheck = " + fieldName + "." + mName +"();\n" +
//                                                            "if (" + scalarRelFieldName + "OrphanCheck != null) {\n");
//                                illegalOrphansInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "OrphanCheck." + relrelGetterName + "();\n");
//                                illegalOrphansInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
//                                        "addErrorMessage(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "OrphanCheck + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
//                                                "illegalOrphans = true;\n" +
//                                        "}\n");
//                                illegalOrphansInCreate.append("}\n");
//                            }
//                            
//                            updateRelatedInCreate.append( (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + fieldName + "." + mName + "()){\n" :
//                                                            "if (" + scalarRelFieldName + " != null) {\n"));
//                                                            //if 1:1, be sure to orphan the related entity's current related entity
//                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE){
//                                if (multiplicity != JpaControllerUtil.REL_TO_ONE || columnNullable) { //no need to declare relrelInstanceName if we have already examined it in the 1:1 orphan check
//                                    updateRelatedInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "." + relrelGetterName + "();\n");
//                                }
//                                if (multiplicity == JpaControllerUtil.REL_TO_ONE) {
//                                    if (columnNullable) {
//                                        updateRelatedInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
//                                        relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
//                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
//                                        "}\n");    
//                                    }
//                                }
//                            }
//                            
//                            updateRelatedInCreate.append( ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
//                                                            scalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
//                                                        scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n");
//                            if (multiplicity == JpaControllerUtil.REL_TO_MANY && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE){
//                                updateRelatedInCreate.append("if " + relrelInstanceName + " != null) {\n" +
//                                        relrelInstanceName + "." + mName + "().remove(" + scalarRelFieldName + ");\n" +
//                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" +
//                                        "}\n");
//                            }
//                            updateRelatedInCreate.append("}\n");
//                            
//                            if (oldMe == null) {
//                                oldMe = "persistent" + simpleEntityName;
//                                String oldMeStatement = simpleEntityName + " " + oldMe + " = em.find(" +
//                                simpleEntityName + ".class, " + fieldName + "." + idGetterName[0] + "());\n";
//                                updateRelatedInEditPre.append("\n " + oldMeStatement);
//                            }
//                            
//                            if (isCollection) {
//                                String relFieldOld = relFieldName + "Old";
//                                String relFieldNew = relFieldName + "New";
//                                String oldScalarRelFieldName = relFieldOld + relTypeReference;
//                                String newScalarRelFieldName = relFieldNew + relTypeReference;
//                                String oldOfNew = "old" + otherName.substring(3) + "Of" + newScalarRelFieldName.substring(0, 1).toUpperCase() + newScalarRelFieldName.substring(1);
//                                updateRelatedInEditPre.append("\n Collection<" + relTypeReference + "> " + relFieldOld + " = " + oldMe + "." + mName + "();\n");
//                                updateRelatedInEditPre.append("Collection <" + relTypeReference + "> " + relFieldNew + " = " + fieldName + "." + mName + "();\n");
//                                if (!relColumnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
//                                    illegalOrphansInEdit.append(
//                                            "for(" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
//                                            "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
//                                            "addErrorMessage(\"You must retain " + relTypeReference + " \" + " + oldScalarRelFieldName + " + \" since its " + otherFieldName + " field is not nullable.\");\n" +
//                                            "illegalOrphans = true;\n" +
//                                            "}\n" +
//                                            "}\n");
//                                }
//                                String relFieldToAttachInEdit = newScalarRelFieldName + "ToAttach";
//                                String refOrMergeStringInEdit = JpaControllerGenerator.getRefOrMergeString(relIdGetterElement, relFieldToAttachInEdit);
//                                String attachedRelFieldNew = "attached" + mName.substring(3) + "New";
//                                attachRelatedInEdit.append("List<" + relTypeReference + "> " + attachedRelFieldNew + " = new ArrayList<" + relTypeReference + ">();\n" +
//                                        "for (" + relTypeReference + " " + relFieldToAttachInEdit + " : " + relFieldNew + ") {\n" +
//                                        relFieldToAttachInEdit + " = " + refOrMergeStringInEdit +
//                                        attachedRelFieldNew + ".add(" + relFieldToAttachInEdit + ");\n" +
//                                        "}\n" +
//                                        relFieldNew + " = " + attachedRelFieldNew + ";\n" +
//                                        fieldName + ".s" + mName.substring(1) + "(" + relFieldNew + ");\n"
//                                        );
//                                if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
//                                    updateRelatedInEditPost.append(
//                                        "for (" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
//                                        "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
//                                        ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? oldScalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
//                                            oldScalarRelFieldName + "." + otherName + "().remove(" + fieldName + ");\n") +
//                                        oldScalarRelFieldName + " = em.merge(" + oldScalarRelFieldName + ");\n" +
//                                        "}\n" +
//                                        "}\n");
//                                }
//                                updateRelatedInEditPost.append("for (" + relTypeReference + " " + newScalarRelFieldName + " : " + relFieldNew + ") {\n" +
//                                "if (!" + relFieldOld + ".contains(" + newScalarRelFieldName + ")) {\n" +
//                                ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? simpleEntityName + " " + oldOfNew + " = " + newScalarRelFieldName + "." + relrelGetterName + "();\n" +
//                                    newScalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
//                                    newScalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
//                                newScalarRelFieldName + " = em.merge(" + newScalarRelFieldName + ");\n");
//                                if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
//                                    updateRelatedInEditPost.append("if " + oldOfNew + " != null && !" + oldOfNew + ".equals(" + fieldName + ")) {\n" +
//                                        oldOfNew + "." + mName + "().remove(" + newScalarRelFieldName + ");\n" +
//                                        oldOfNew + " = em.merge(" + oldOfNew + ");\n" +
//                                        "}\n");
//                                }
//                                updateRelatedInEditPost.append("}\n}\n");
//                            } else {
//                                updateRelatedInEditPre.append("\n" + relTypeReference + " " + scalarRelFieldName + "Old = " + oldMe + "." + mName + "();\n");
//                                updateRelatedInEditPre.append(relTypeReference + " " + scalarRelFieldName + "New = " + fieldName + "." + mName +"();\n");
//                                if (!relColumnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
//                                    illegalOrphansInEdit.append(
//                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
//                                        "addErrorMessage(\"You must retain " + relTypeReference + " \" + " + scalarRelFieldName + "Old + \" since its " + otherFieldName + " field is not nullable.\");\n" +
//                                        "illegalOrphans = true;\n" +
//                                        "}\n");
//                                }
//                                String refOrMergeStringInEdit = JpaControllerGenerator.getRefOrMergeString(relIdGetterElement, scalarRelFieldName + "New"); 
//                                attachRelatedInEdit.append("if (" + scalarRelFieldName + "New != null) {\n" +
//                                    scalarRelFieldName + "New = " + refOrMergeStringInEdit +
//                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + "New);\n" +
//                                    "}\n");
//                                if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
//                                     updateRelatedInEditPost.append(   
//                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
//                                        ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + "Old.s" + otherName.substring(1) + "(null);\n" :
//                                            scalarRelFieldName + "Old." + otherName + "().remove(" + fieldName +");\n") +
//                                        scalarRelFieldName + "Old = em.merge(" + scalarRelFieldName +"Old);\n}\n");
//                                }
//                                if (multiplicity == JpaControllerUtil.REL_TO_ONE && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && !columnNullable) {
//                                    illegalOrphansInEdit.append(
//                                        "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
//                                    illegalOrphansInEdit.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" + 
//                                                "if (" + relrelInstanceName + " != null) {\n" + 
//                                                "addErrorMessage(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "New + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
//                                                "illegalOrphans = true;\n" +
//                                                "}\n");
//                                    illegalOrphansInEdit.append("}\n");
//                                }
//                                updateRelatedInEditPost.append(
//                                    "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
//                                if (multiplicity == JpaControllerUtil.REL_TO_ONE && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && columnNullable) {
//                                    updateRelatedInEditPost.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" + 
//                                            "if (" + relrelInstanceName + " != null) {\n" + 
//                                            relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
//                                            relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
//                                            "}\n");
//                                }
//                                updateRelatedInEditPost.append(
//                                    ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + "New.s" + otherName.substring(1) + "(" + fieldName + ");\n" :
//                                        scalarRelFieldName + "New." + otherName + "().add(" + fieldName +");\n") +
//                                    scalarRelFieldName + "New = em.merge(" + scalarRelFieldName + "New);\n}\n"
//                                    );
//                            } 
//                            
//                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && !relColumnNullable) {
//                                String orphanCheckCollection = relFieldName + "OrphanCheck";
//                                String orphanCheckScalar = isCollection ? orphanCheckCollection + relTypeReference : relFieldName + "OrphanCheck";
//                                illegalOrphansInDestroy.append(
//                                        (isCollection ? "Collection<" + relTypeReference + "> " + orphanCheckCollection : relTypeReference + " " + orphanCheckScalar) + " = " + fieldName + "." + mName +"();\n" +
//                                        (isCollection ? "for(" + relTypeReference + " " + orphanCheckScalar + " : " + orphanCheckCollection : "if (" + orphanCheckScalar + " != null") + ") {\n" +
//                                        "addErrorMessage(\"This " + simpleEntityName + " (\" + " +  fieldName + " + \") cannot be destroyed since the " + relTypeReference + " \" + " + orphanCheckScalar + " + \" in its " + relFieldName + " field has a non-nullable " + otherFieldName + " field.\");\n" +
//                                        "illegalOrphans = true;\n" +
//                                        "}\n");
//                            }
//                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
//                                updateRelatedInDestroy.append( (isCollection ? "Collection<" + relTypeReference + "> " + relFieldName : relTypeReference + " " + scalarRelFieldName) + " = " + fieldName + "." + mName +"();\n" +
//                                        (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + relFieldName : "if (" + scalarRelFieldName + " != null") + ") {\n" +
//                                        ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
//                                            scalarRelFieldName + "." + otherName + "().remove(" + fieldName +");\n") +
//                                        scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n}\n\n");
//                            }
//                            
////                            if (multiplicity == JpaControllerUtil.REL_TO_MANY) {
////                                importFqs = new String[]{"java.util.Arrays",
////                                            "java.util.Collection"
////                                  };
////                                for (String importFq : importFqs) {
////                                    modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
////                                }
////                                
////                            }
//                            
//                        } else {
//                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
//                        }
//
//                    }
                    // </editor-fold>
                    
                    bodyText = "return JsfUtil.getSelectItems(jpaController.find" + simpleEntityName + "Entities(), false);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "ItemsAvailableSelectMany", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return JsfUtil.getSelectItems(jpaController.find" + simpleEntityName + "Entities(), true);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "ItemsAvailableSelectOne", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//                    String getFromReqParamMethod = "get" + simpleEntityName + "FromRequest";
                    
                    bodyText = "if (" + fieldName + " == null) {\n" +
                            fieldName + " = (" + simpleEntityName + ")JsfUtil.getObjectFromRequestParameter(\"jsfcrud.current" + simpleEntityName + "\", converter, null);\n" +
                            "}\n" + 
                            "if (" + fieldName + " == null) {\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" +
                            "}\n" + 
                            "return " + fieldName + ";";
                    methodInfo = new MethodInfo("get" + simpleEntityName, publicModifier, entityClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    bodyText = "reset(true);\n" + 
                            "return \"" + fieldName + "_list\";";
                    methodInfo = new MethodInfo("listSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "reset(false);\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" + 
                            (embeddable[0] ? fieldName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" : "") +
                            "return \"" + fieldName + "_create\";";
                    methodInfo = new MethodInfo("createSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//                    String BEGIN = isInjection ? "utx.begin();" : "em.getTransaction().begin();";
//                    String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
//                    String ROLLBACK = isInjection ? "utx.rollback();" : "em.getTransaction().rollback();";
                    
                    String newEntityStringVar = "new" + simpleEntityName + "String";
                    String entityStringVar = fieldName + "String";
                    
//                    if (illegalOrphansInCreate.length() > 0) {
//                        illegalOrphansInCreate.insert(0, "boolean illegalOrphans = false;\n");
//                        illegalOrphansInCreate.append("if (illegalOrphans) {\n" +
//                                "return null;\n" +
//                                "}\n");
//                    }
                    
                    TypeElement entityType = workingCopy.getElements().getTypeElement(entityClass);
                    StringBuffer codeToPopulatePkFields = new StringBuffer();
                    if (embeddable[0]) {
                        for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(workingCopy, entityType)) {
                            if (embeddedPkSupport.isRedundantWithRelationshipField(workingCopy, entityType, pkMethod)) {
                                codeToPopulatePkFields.append(fieldName + "." +idGetterName[0] + "().s" + pkMethod.getSimpleName().toString().substring(1) + "(" +  //NOI18N
                                    fieldName + "." + embeddedPkSupport.getCodeToPopulatePkField(workingCopy, entityType, pkMethod) + ");\n");
                            }
                        }
                    }

//                    boolean isGenerated = JpaControllerUtil.isGenerated(workingCopy, idGetterElement, isFieldAccess);
//                    bodyText = initCollectionsInCreate.toString() +
//                            codeToPopulatePkFields.toString() +
//                            illegalOrphansInCreate.toString() +
//                            "EntityManager em = null;\n" + 
//                            "try {\n " + BEGIN + "\n " + 
//                            "em = getEntityManager();\n" +
//                            initRelatedInCreate.toString() + "em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n" +   //NOI18N
//                            "addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n"  + //NOI18N
//                            "} catch (Exception ex) {\n try {\n" +
//                            (isGenerated ? "ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" : 
//                            "if (find" + simpleEntityName + "(" + fieldName + "." + idGetterName[0] + "()) != null) {\n" +
//                            "addErrorMessage(\"" + simpleEntityName + " \" + " + fieldName + " + \" already exists.\");\n" +
//                            "} else {\n" +
//                            "ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" + 
//                            "}\n") +
//                            ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
//                            "}\nreturn null;\n} " +   //NOI18N
//                            "finally {\n if (em != null) {\nem.close();\n}\n }\n" + 
//                            "return listSetup();";

                    bodyText = "try {\n" +
                            "jpaController.create(" + fieldName + ");\n" +
                            "JsfUtil.addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n"  + //NOI18N
                            (methodThrowsIllegalOrphanException ? "} catch (IllegalOrphanException oe) {\n" + 
                            "JsfUtil.addErrorMessages(oe.getMessages());\n" +
                            "return null;\n" : "") +
                            "} catch (Exception e) {\n" +
                            "JsfUtil.ensureAddErrorMessage(e, \"A persistence error occurred.\");\n" +
                            "return null;\n" +
                            "}\n" +
                            "return listSetup();";
                            
                    methodInfo = new MethodInfo("create", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_detail\");";
                    methodInfo = new MethodInfo("detailSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_edit\");";
                    methodInfo = new MethodInfo("editSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = "reset(false);\n" + 
                            fieldName + " = (" + simpleEntityName + ")JsfUtil.getObjectFromRequestParameter(\"jsfcrud.current" + simpleEntityName + "\", converter, null);\n" +
                            "if (" + fieldName + " == null) {\n" +
                            "String request" + simpleEntityName + "String = JsfUtil.getRequestParameter(\"jsfcrud.current" +  simpleEntityName + "\");\n" +
                            "JsfUtil.addErrorMessage(\"The " + fieldName + " with id \" + request" + simpleEntityName + "String + \" no longer exists.\");\n" +
                            "return relatedOrListOutcome();\n" +
                            "}\n" +
                            "return destination;";
                    methodInfo = new MethodInfo("scalarSetup", privateModifier, "java.lang.String", null, new String[]{"java.lang.String"}, new String[]{"destination"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    entityStringVar = fieldName + "String";
                    String currentEntityStringVar = "current" + simpleEntityName + "String";
                    
//                    if (illegalOrphansInEdit.length() > 0) {
//                        illegalOrphansInEdit.insert(0, "boolean illegalOrphans = false;\n");
//                        illegalOrphansInEdit.append("if (illegalOrphans) {\n" +
//                                "utx.rollback();\n" +
//                                "return null;\n" +
//                                "}\n");
//                    }                    
                    
                    bodyText = codeToPopulatePkFields.toString() + 
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "String " + currentEntityStringVar + " = JsfUtil.getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "if " + entityStringVar + " == null || " + entityStringVar + ".length() == 0 || !" + entityStringVar + ".equals(" + currentEntityStringVar + ")) {\n" +
                            "String outcome = editSetup();\n" +
                            "if (\"" + fieldName + "_edit\".equals(outcome)) {\n" +
                            "JsfUtil.addErrorMessage(\"Could not edit " + fieldName + ". Try again.\");\n" +
                            "}\n" +
                            "return outcome;\n" +
                            "}\n";
//                    bodyText += "EntityManager em = null;\n" + 
//                        "try {\n " + BEGIN + "\n" + 
//                        "em = getEntityManager();\n" +
//                        updateRelatedInEditPre.toString() + illegalOrphansInEdit.toString() + attachRelatedInEdit.toString() +
//                        fieldName + " = em.merge(" + fieldName + ");\n " + 
//                        updateRelatedInEditPost.toString() + COMMIT + "\n" +   //NOI18N
//                        "addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n" +   //NOI18N
//                        "} catch (Exception ex) {\n try {\n String msg = ex.getLocalizedMessage();\n" + 
//                        "if (msg != null && msg.length() > 0) {\n" +
//                        "addErrorMessage(msg);\n" +
//                        "}\n" +
//                        "else if (" + getFromReqParamMethod + "() == null) {\n" +
//                        "addErrorMessage(\"The " + fieldName + " with id \" + current" + simpleEntityName + "String + \" no longer exists.\");\n" +
//                        ROLLBACK +
//                        "\nreturn listSetup();\n" +
//                        "}\n" +
//                        "else {\n" +
//                        "addErrorMessage(\"A persistence error occurred.\");\n" +
//                        "}\n" +
//                        ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
//                        "}\nreturn null;\n} " +   //NOI18N
//                        "finally {\n if (em != null) {\nem.close();\n}\n }\n" +  //NOI18N
//                        "return detailSetup();";
                    bodyText += "try {\n" +
                            "jpaController.edit(" + fieldName + ");\n" +
                            "JsfUtil.addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n"  + //NOI18N
                            (methodThrowsIllegalOrphanException ? "} catch (IllegalOrphanException oe) {\n" + 
                            "JsfUtil.addErrorMessages(oe.getMessages());\n" +
                            "return null;\n" : "") +
                            "} catch (NonexistentEntityException ne) {\n" +
                            "JsfUtil.addErrorMessage(ne.getLocalizedMessage());\n" +
                            "return listSetup();\n" +
                            "} catch (Exception e) {\n" +
                            "JsfUtil.ensureAddErrorMessage(e, \"A persistence error occurred.\");\n" +
                            "return null;\n" +
                            "}\n" +
                            "return detailSetup();";
                    methodInfo = new MethodInfo("edit", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//                    if (illegalOrphansInDestroy.length() > 0) {
//                        illegalOrphansInDestroy.insert(0, "boolean illegalOrphans = false;\n");
//                        illegalOrphansInDestroy.append("if (illegalOrphans) {\n" +
//                                ROLLBACK + "\n" +
//                                "return null;\n" +
//                                "}\n");
//                    }
                    
//                    String refOrMergeStringInDestroy = "em.merge(" + fieldName + ");\n";
//                    if (idGetterElement != null) {
//                        refOrMergeStringInDestroy = "em.getReference(" + simpleEntityName + ".class, ";
//                        if (embeddable[0]) {
//                            refOrMergeStringInDestroy += "new " + simpleConverterName + "().getId(idAsString));\n";
//                        }
//                        else {
//                            refOrMergeStringInDestroy += "id);\n";
//                        }
//                    }
//                    bodyText = "EntityManager em = null;\n" + 
//                        "try {\n " + BEGIN + "\n" + 
//                        "em = getEntityManager();\n" +
//                        "String idAsString = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
//                        "try {\n " + 
//                        (embeddable[0] ? "" : createIdFieldDeclaration(idPropertyType[0], "idAsString") + "\n") + 
//                        fieldName + " = " + refOrMergeStringInDestroy + 
//                        fieldName + "." + idGetterName[0] + "();\n" +
//                        "} catch (EntityNotFoundException enfe) {\n" +
//                        "addErrorMessage(\"The " + fieldName + " with id \" + idAsString + \" no longer exists.\");\n" +
//                        "String notFoundOutcome = relatedControllerOutcome();\n" +
//                        "if (notFoundOutcome == null) {\n" +
//                        "notFoundOutcome = listSetup();\n" +
//                        "}\n" +
//                        ROLLBACK + "\n" +
//                        "return notFoundOutcome;\n" +
//                        "}\n" + 
//                        illegalOrphansInDestroy.toString() +
//                        updateRelatedInDestroy.toString() + 
//                        "em.remove(" + fieldName + ");\n " + COMMIT + "\n" +   //NOI18N
//                        "addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n" +   //NOI18N
//                        "} catch (Exception ex) {\n try {\n ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" + ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
//                        "}\nreturn null;\n} " +   //NOI18N
//                        "finally {\n if (em != null) {\nem.close();\n}\n }\n" +  //NOI18N
//                        relatedControllerOutcomeSwath + 
//                            "return listSetup();";
                    bodyText = "String idAsString = JsfUtil.getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            (embeddable[0] ? simpleIdPropertyType + " id = converter.getId(idAsString);" : createIdFieldDeclaration(idPropertyType[0], "idAsString")) +
                            "\n";
                    bodyText += "try {\n" +
                            "jpaController.destroy(id);\n" +
                            "JsfUtil.addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n"  + //NOI18N
                            (methodThrowsIllegalOrphanException ? "} catch (IllegalOrphanException oe) {\n" + 
                            "JsfUtil.addErrorMessages(oe.getMessages());\n" +
                            "return null;\n" : "") +
                            "} catch (NonexistentEntityException ne) {\n" +
                            "JsfUtil.addErrorMessage(ne.getLocalizedMessage());\n" +
                            "return relatedOrListOutcome();\n" +
                            "} catch (Exception e) {\n" +
                            "JsfUtil.ensureAddErrorMessage(e, \"A persistence error occurred.\");\n" +
                            "return null;\n" +
                            "}\n" +
                            "return relatedOrListOutcome();";
                    methodInfo = new MethodInfo("destroy", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = "String relatedControllerOutcome = relatedControllerOutcome();\n" +
                            "if (relatedControllerOutcome != null {\n" +
                            "return relatedControllerOutcome;\n" +
                            "}\n" +
                            "return listSetup();";
                    methodInfo = new MethodInfo("relatedOrListOutcome", privateModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    TypeInfo listOfEntityType = new TypeInfo("java.util.List", new String[]{entityClass});
                    
                    bodyText = "if (" + fieldName + "Items == null) {\n" +
                            "getPagingInfo();\n" +
                            fieldName + "Items = jpaController.find" + simpleEntityName + "Entities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());\n" +
                            "}\n" +
                            "return " + fieldName + "Items;";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "Items", publicModifier, listOfEntityType, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "reset(false);\n" +
                            "pagingInfo.nextPage();\n "+
                            "return \"" + fieldName + "_list\"";
                    methodInfo = new MethodInfo("next", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "reset(false);\n" +
                        "pagingInfo.previousPage();\n" +
                        "return \"" + fieldName + "_list\";\n";
                    methodInfo = new MethodInfo("prev", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "String relatedControllerString = JsfUtil.getRequestParameter(\"jsfcrud.relatedController\");\n" +
                        "String relatedControllerTypeString = JsfUtil.getRequestParameter(\"jsfcrud.relatedControllerType\");\n" +
                        "if (relatedControllerString != null && relatedControllerTypeString != null) {\n" +
                        "FacesContext context = FacesContext.getCurrentInstance();\n" +
                        "Object relatedController = context.getApplication().getELResolver().getValue(context.getELContext(), null, relatedControllerString);\n" +
                        "try {\n" +
                        "Class<?> relatedControllerType = Class.forName(relatedControllerTypeString);\n" +
                        "Method detailSetupMethod = relatedControllerType.getMethod(\"detailSetup\");\n" +
                        "return (String)detailSetupMethod.invoke(relatedController);\n" +
                        "} catch (ClassNotFoundException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "} catch (NoSuchMethodException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "} catch (IllegalAccessException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "} catch (InvocationTargetException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "}\n" +
                        "}\n" +
                        "return null;";
                    methodInfo = new MethodInfo("relatedControllerOutcome", privateModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = fieldName + " = null;\n" +
                            fieldName + "Items = null;\n" +
                            "pagingInfo.setItemCount(-1);\n" +
                            "if (resetFirstItem) {\n" +
                            "pagingInfo.setFirstItem(0);\n" +
                            "}\n";
                    methodInfo = new MethodInfo("reset", privateModifier, "void", null, new String[]{"boolean"}, new String[]{"resetFirstItem"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    String newEntityStringInit;
                    if (embeddable[0]) {
                        newEntityStringInit = "new" + simpleEntityName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" + 
                                "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new" + simpleEntityName + ");\n";
                    }
                    else {
                        newEntityStringInit = "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new" + simpleEntityName + ");\n";
                    }
                    bodyText = simpleEntityName + " new" + simpleEntityName + " = new " + simpleEntityName + "();\n" +
                            newEntityStringInit +
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "if (!" + newEntityStringVar + ".equals(" + entityStringVar + ")) {\n" +
                            "createSetup();\n" +
                            //"throw new ValidatorException(new FacesMessage(\"Could not create " + fieldName + ". Try again.\"));\n" +
                            "}\n";
                    methodInfo = new MethodInfo("validateCreate", publicModifier, "void", null, new String[]{"javax.faces.context.FacesContext", "javax.faces.component.UIComponent", "java.lang.Object"}, new String[]{"facesContext", "component", "value"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    
                    
                    methodInfo = new MethodInfo("getConverter", publicModifier, "javax.faces.convert.Converter", null, null, null, "return converter;", null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }).commit();
    
        return controllerFileObject;
    }

    private static HashSet<String> CONVERTED_TYPES = new HashSet<String>();
    static {
        CONVERTED_TYPES.add("Boolean");
        CONVERTED_TYPES.add("Byte");
        CONVERTED_TYPES.add("Double");
        CONVERTED_TYPES.add("Float");
        CONVERTED_TYPES.add("Integer");
        CONVERTED_TYPES.add("Long");
        CONVERTED_TYPES.add("Short");
        CONVERTED_TYPES.add("StringBuffer");
    }
    private static HashMap<String,String> PRIMITIVE_TYPES = new HashMap<String, String>();
    static {
        PRIMITIVE_TYPES.put("boolean", "Boolean");
        PRIMITIVE_TYPES.put("byte", "Byte");
        PRIMITIVE_TYPES.put("double", "Double");
        PRIMITIVE_TYPES.put("float", "Float");
        PRIMITIVE_TYPES.put("int", "Integer");
        PRIMITIVE_TYPES.put("long", "Long");
        PRIMITIVE_TYPES.put("short", "Short");
    }
    
    /** @param valueVar is name of a String variable */
    private static String createIdFieldDeclaration(String idPropertyType, String valueVar) {
    	String idField;
        if (idPropertyType.startsWith("java.lang.")) {
            String shortName = idPropertyType.substring(10);
            idField = shortName + " id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        } else if (idPropertyType.equals("java.math.BigInteger") || "BigInteger".equals(idPropertyType)) {
            idField = "java.math.BigInteger id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        } else if (idPropertyType.equals("java.math.BigDecimal") || "BigDecimal".equals(idPropertyType)) {
            idField = "java.math.BigDecimal id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        } else {
            idField = idPropertyType + " id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        }
        return idField;
    }
    
    /** @param valueVar is name of a String variable */
    private static String createIdFieldInitialization(String idPropertyType, String valueVar) {
    	String idField;
        //PENDING cannot assume that key type is Integer, Long, String, int or long
    	if ("char".equals(idPropertyType)) {
            idField = valueVar + ".charAt(0);";
        } else if (PRIMITIVE_TYPES.containsKey(idPropertyType)) {
            String objectType = PRIMITIVE_TYPES.get(idPropertyType);
            String methodName = "parse" + idPropertyType.substring(0,1).toUpperCase() + idPropertyType.substring(1);
            idField = objectType + "." + methodName + "(" + valueVar + ")";
        } else if (idPropertyType.equals("java.math.BigInteger") || "BigInteger".equals(idPropertyType)) {
            idField = "new java.math.BigInteger(" + valueVar + ")";
        } else if (idPropertyType.equals("java.math.BigDecimal") || "BigDecimal".equals(idPropertyType)) {
            idField = "new java.math.BigDecimal(" + valueVar + ")";
        } else if (idPropertyType.equals("java.lang.String") || "String".equals(idPropertyType)) {
            idField = valueVar;
        } else if (idPropertyType.equals("java.lang.Character") || "Character".equals(idPropertyType)) {
            idField = "new Character(" + valueVar + ".charAt(0))";
        } else if (idPropertyType.startsWith("java.lang.")) {
            String shortName = idPropertyType.substring(10);
            idField = "new " + shortName + "(" + valueVar + ")";
        } else if (CONVERTED_TYPES.contains(idPropertyType)) {
            idField = "new " + idPropertyType + "(" + valueVar + ")";
        } else {
            idField = "(" + idPropertyType + ") javax.faces.context.FacesContext.getCurrentInstance().getApplication().\n"
                    + "createConverter(" + idPropertyType + ".class).getAsObject(FacesContext.\n"
                    + "getCurrentInstance(), null, " + valueVar + ")";
        }
        return idField;
    }
    
    public static String getManagedBeanName(String simpleEntityName) {
        int len = simpleEntityName.length();
        return len > 1 ? simpleEntityName.substring(0,1).toLowerCase() + simpleEntityName.substring(1) : simpleEntityName.toLowerCase();
    }
    
    private static MethodTree createMethod(WorkingCopy workingCopy, Modifier[] modifiers, String returnType, String name, 
            String[] params, String[] exceptions, String body) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Number of params can't be odd");
        }
        List<MethodModel.Variable> paramsList = new ArrayList<MethodModel.Variable>();
        for (int i = 0; i < params.length; i++) {
            paramsList.add(MethodModel.Variable.create(params[i], params[i + 1]));
            i++;
        }

        MethodModel methodModel = MethodModel.create(
                name,
                returnType,
                body,
                paramsList,
                Arrays.asList(exceptions),
                new HashSet<Modifier>(Arrays.asList(modifiers))
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
}
