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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
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
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
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
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator.AnnotationInfo;
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator.TypeInfo;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Pavel Buzek
 * @author mbohm
 */
public class JSFClientGenerator {
    
    private static String INDEX_PAGE = "index.jsp"; //NOI18N
    private static String WELCOME_JSF_PAGE = "welcomeJSF.jsp";  //NOI18N
    
    public static void generateJSFPages(Project project, final String entityClass, String jsfFolder, final String controllerClass, FileObject pkg, FileObject controllerFileObject, final EmbeddedPkSupport embeddedPkSupport) throws IOException {
        final boolean isInjection = true;//Util.isSupportedJavaEEVersion(project);
        
        String simpleControllerName = simpleClassName(controllerClass);
        final String simpleEntityName = simpleClassName(entityClass);
        if (jsfFolder.startsWith("/")) {
            jsfFolder = jsfFolder.substring(1);
        }
        
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
        
        int lastIndexOfController = controllerClass.lastIndexOf("Controller");
        String controllerSuffix = controllerClass.substring(lastIndexOfController);
        String converterSuffix = controllerSuffix.replace("Controller", "Converter");
        String simpleConverterName = simpleEntityName + converterSuffix; //NOI18N
        int converterNameAttemptIndex = 1;
        while (pkg.getFileObject(simpleConverterName, "java") != null && converterNameAttemptIndex < 1000) {
            simpleConverterName += "_" + converterNameAttemptIndex++;
        }
        String converterName = ((pkgName == null || pkgName.length() == 0) ? "" : pkgName + ".") + simpleConverterName;
        final String fieldName = fieldFromClassName(simpleEntityName);

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
                fieldAccess[0] = JsfForm.isFieldAccess(jc);
                for (ExecutableElement method : JsfForm.getEntityMethods(jc)) {
                    String methodName = method.getSimpleName().toString();
                    if (methodName.startsWith("get")) {
                        Element f = fieldAccess[0] ? JsfForm.guessField(controller, method) : method;
                        if (f != null) {
                            if (JsfForm.isAnnotatedWith(f, "javax.persistence.Id") ||
                                    JsfForm.isAnnotatedWith(f, "javax.persistence.EmbeddedId")) {
                                idGetter.add(ElementHandle.create(method));
                                idProperty[0] = getPropNameFromMethod(methodName);
                            } else if (JsfForm.isAnnotatedWith(f, "javax.persistence.OneToOne") ||
                                    JsfForm.isAnnotatedWith(f, "javax.persistence.ManyToOne")) {
                                toOneRelMethods.add(ElementHandle.create(method));
                            } else if (JsfForm.isAnnotatedWith(f, "javax.persistence.OneToMany") ||
                                    JsfForm.isAnnotatedWith(f, "javax.persistence.ManyToMany")) {
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
        
        if (arrEntityClassFO[0] != null) {
            addImplementsClause(arrEntityClassFO[0], entityClass, "java.io.Serializable"); //NOI18N
        }
            
        JEditorPane ep = new JEditorPane("text/x-jsp", "");
        final BaseDocument doc = new BaseDocument(ep.getEditorKit().getClass(), false);
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
        
        if (wm.getDocumentBase().getFileObject(WELCOME_JSF_PAGE) == null) {
            String content = JSFFrameworkProvider.readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/netbeans/modules/web/jsf/resources/" + WELCOME_JSF_PAGE), "UTF-8"); //NOI18N
            Charset encoding = FileEncodingQuery.getDefaultEncoding();
            content = content.replaceAll("__ENCODING__", encoding.name());
            FileObject target = FileUtil.createData(wm.getDocumentBase(), WELCOME_JSF_PAGE);//NOI18N
            JSFFrameworkProvider.createFile(target, content, encoding.name());  //NOI18N
        }
        
        controllerFileObject = generateControllerClass(fieldName, pkg, idGetter.get(0), persistenceUnit, controllerClass, simpleConverterName, 
                entityClass, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection, fieldAccess[0], controllerFileObject, embeddedPkSupport);
        
        final String managedBean =  getManagedBeanName(simpleEntityName);
        FileObject converter = generateConverter(controllerFileObject, pkg, simpleConverterName, controllerClass, simpleControllerName, entityClass, 
                simpleEntityName, idGetter.get(0), managedBean, isInjection);
            
        final String indexJspToUse = addLinkToListJspIntoIndexJsp(wm, jsfFolder, simpleEntityName);
        final String linkToIndex = indexJspToUse != null ? "<br />\n<a href=\"" + wm.getContextPath() + "/" + indexJspToUse + "\">Index</a>\n" : "";  //NOI18N

        generateListJsp(jsfRoot, classpathInfo, entityClass, simpleEntityName, managedBean, linkToIndex, fieldName, idProperty[0], doc, embeddedPkSupport);
        
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateNewJsp(controller, entityClass, simpleEntityName, managedBean, fieldName, toOneRelMethods, fieldAccess[0], linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass);
            }
        }, true);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateEditJsp(controller, entityClass, simpleEntityName, managedBean, fieldName, linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass);
            }
        }, true);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateDetailJsp(controller, entityClass, simpleEntityName, managedBean, fieldName, idProperty[0], isInjection, linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass);
            }
        }, true);
        
        addStuffToFacesConfigXml(classpathInfo, wm, managedBean, controllerClass, entityClass, converterName, fieldName, jsfFolder, idGetter.get(0), pkgName, controllerFileObject);
    }

    private static String addLinkToListJspIntoIndexJsp(WebModule wm, String jsfFolder, String simpleEntityName) throws FileNotFoundException, IOException {
        FileObject documentBase = wm.getDocumentBase();
        
        FileObject indexjsp = documentBase.getFileObject(WELCOME_JSF_PAGE); //NOI18N
        String indexjspString = "faces/" + WELCOME_JSF_PAGE;
        String find = "<h1><h:outputText value=\"JavaServer Faces\" /></h1>"; //NOI18N
        
        if (indexjsp != null) {
            String content = JSFFrameworkProvider.readResource(indexjsp.getInputStream(), "UTF-8"); //NO18N
            String endLine = System.getProperty("line.separator"); //NOI18N
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
                    return indexjspString;
                }
                replace.append(replaceCrux);
                if (needsForm) {
                    replace.append("</h:form>");
                    replace.append(endLine);
                }
                content = content.replaceFirst(find, new String (replace.toString().getBytes("UTF8"), "UTF-8")); //NOI18N
                JSFFrameworkProvider.createFile(indexjsp, content, "UTF-8"); //NOI18N
                return indexjspString;
            }
        }
        return null;
    }

    private static void generateListJsp(final FileObject jsfRoot, ClasspathInfo classpathInfo, final String entityClass, String simpleEntityName, 
            final String managedBean, String linkToIndex, final String fieldName, String idProperty, BaseDocument doc, final EmbeddedPkSupport embeddedPkSupport) throws FileStateInvalidException, IOException {
        FileSystem fs = jsfRoot.getFileSystem();
        final StringBuffer listSb = new StringBuffer();
        final Charset encoding = FileEncodingQuery.getDefaultEncoding();
        listSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>Listing " + simpleEntityName + " Items</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        listSb.append("<h1>Listing " + simpleEntityName + " Items</h1>\n");
        listSb.append("<h:form>\n");
        listSb.append("<h:outputText escape=\"false\" value=\"(No " + simpleEntityName + " Items Found)<br />\" rendered=\"#{" + managedBean + ".itemCount == 0}\" />\n");
        listSb.append("<h:panelGroup rendered=\"#{" + managedBean + ".itemCount > 0}\">\n");
        listSb.append(MessageFormat.format("<h:outputText value=\"Item #'{'{0}.firstItem + 1'}'..#'{'{0}.lastItem'}' of #'{'{0}.itemCount}\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.prev'}'\" value=\"Previous #'{'{0}.batchSize'}'\" rendered=\"#'{'{0}.firstItem >= {0}.batchSize'}'\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Next #'{'{0}.batchSize'}'\" rendered=\"#'{'{0}.lastItem + {0}.batchSize <= {0}.itemCount}\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Remaining #'{'{0}.itemCount - {0}.lastItem'}'\"\n"
                + "rendered=\"#'{'{0}.lastItem < {0}.itemCount && {0}.lastItem + {0}.batchSize > {0}.itemCount'}'\"/>\n", managedBean));
        listSb.append("<h:dataTable value='#{" + managedBean + "." + fieldName + "s}' var='item' border=\"0\" cellpadding=\"2\" cellspacing=\"0\" rowClasses=\"jsfcrud_oddrow,jsfcrud_evenrow\" rules=\"all\" style=\"border:solid 1px\">\n");
        final  String commands = "<h:column>\n <f:facet name=\"header\">\n <h:outputText escape=\"false\" value=\"&nbsp;\"/>\n </f:facet>\n"
                + "<h:commandLink value=\"Show\" action=\"#'{'" + managedBean + ".detailSetup'}'\">\n" 
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'" + managedBean + ".asString[{0}]'}'\"/>\n"               
                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                + "<h:commandLink value=\"Edit\" action=\"#'{'" + managedBean + ".editSetup'}'\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'" + managedBean + ".asString[{0}]'}'\"/>\n"
                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                + "<h:commandLink value=\"Destroy\" action=\"#'{'" + managedBean + ".destroy'}'\">\n" 
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'" + managedBean + ".asString[{0}]'}'\"/>\n"
                + "</h:commandLink>\n </h:column>\n";
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                JsfTable.createTable(controller, typeElement, managedBean + "." + fieldName, listSb, commands, embeddedPkSupport);
            }
        }, true);
        listSb.append("</h:dataTable>\n</h:panelGroup>\n");
        listSb.append("<br />\n<h:commandLink action=\"#{" + managedBean + ".createSetup}\" value=\"New " + simpleEntityName + "\"/>\n"
                + linkToIndex + "\n");
        listSb.append("</h:form>\n</f:view>\n</body>\n</html>\n");
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, listSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            listSb.replace(0, listSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
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
    
    private static void generateNewJsp(CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            List<ElementHandle<ExecutableElement>> toOneRelMethods, boolean fieldAccess, String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass) throws FileStateInvalidException, IOException {
        StringBuffer newSb = new StringBuffer();
        final Charset encoding = FileEncodingQuery.getDefaultEncoding();
        newSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>New " + simpleEntityName + "</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        newSb.append("<h1>New " + simpleEntityName + "</h1>\n");
        newSb.append("<h:form>\n  <h:inputHidden id=\"validateCreateField\" validator=\"#{" + managedBean + ".validateCreate}\" value=\"value\"/>\n <h:panelGrid columns=\"2\">\n");
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_NEW, managedBean + "." + fieldName, newSb, entityClass, embeddedPkSupport, controllerClass);
        newSb.append("</h:panelGrid>\n<br />\n");
        
        newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\"/>\n<br />\n");
        
        newSb.append("<br />\n<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + " Items\" immediate=\"true\"/>\n " + linkToIndex
                + "</h:form>\n </f:view>\n</body>\n</html>\n");
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, newSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            newSb.replace(0, newSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
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
    
    private static void generateEditJsp(CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass) throws FileStateInvalidException, IOException {
        StringBuffer editSb = new StringBuffer();
        final Charset encoding = FileEncodingQuery.getDefaultEncoding();
        editSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>Editing " + simpleEntityName + "</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        editSb.append("<h1>Editing " + simpleEntityName + "</h1>\n");
        editSb.append("<h:form>\n"
                + "<h:panelGrid columns=\"2\">\n");
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_EDIT, managedBean + "." + fieldName, editSb, entityClass, embeddedPkSupport, controllerClass);
        editSb.append("</h:panelGrid>\n<br />\n<h:commandLink action=\"#{" + managedBean + ".edit}\" value=\"Save\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\"/>\n"
                + "</h:commandLink>\n"
                + "<br />\n<br />\n"
                + "<h:commandLink action=\"#{" + managedBean + ".detailSetup}\" value=\"Show\" immediate=\"true\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\"/>\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + " Items\" immediate=\"true\"/>\n" + linkToIndex
                + "</h:form>\n </f:view>\n</body>\n</html>\n");

        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, editSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            editSb.replace(0, editSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
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

    private static void generateDetailJsp(CompilationController controller, String entityClass, String simpleEntityName, String managedBean, 
            String fieldName, String idProperty, boolean isInjection, String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass) throws FileStateInvalidException, IOException {
        StringBuffer detailSb = new StringBuffer();
        final Charset encoding = FileEncodingQuery.getDefaultEncoding();
        detailSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"" + encoding.name() + "\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding.name() + "\" />\n"
                + "<title>" + simpleEntityName + " Detail</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        detailSb.append("<h1>" + simpleEntityName + " Detail</h1>\n");
        detailSb.append("<h:form>\n  <h:panelGrid columns=\"2\">\n");
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, detailSb, entityClass, embeddedPkSupport, controllerClass);
        JsfForm.createTablesForRelated(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, idProperty, isInjection, detailSb, embeddedPkSupport, controllerClass);
        detailSb.append("</h:panelGrid>\n");
        detailSb.append("<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".destroy}\" value=\"Destroy\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\" />\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".editSetup}\" value=\"Edit\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\" />\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".createSetup}\" value=\"New " + simpleEntityName + "\" />\n<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + " Items\"/>\n" + linkToIndex
                + "</h:form>\n </f:view>\n</body>\n</html>\n");

        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, detailSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            detailSb.replace(0, detailSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
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

    private static void addStuffToFacesConfigXml(ClasspathInfo classpathInfo, WebModule wm, String managedBean, String controllerClass, String entityClass, 
            String converterName, String fieldName, String jsfFolder, final ElementHandle<ExecutableElement> idGetterHandle, String pkgName, FileObject controllerFileObject) {
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(wm);
        if (configFiles.length > 0) {
            // using first found faces-config.xml, is it OK?
            FileObject fo = configFiles[0];
            JSFConfigModel model = null;
            try {
                model = ConfigurationUtils.getConfigModel(fo, true);
                model.startTransaction();
                FacesConfig config = model.getRootComponent();
                
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
                mb.setManagedBeanClass(controllerClass);
                mb.setManagedBeanScope(ManagedBean.Scope.SESSION);
                if (mbIsNew) {
                    config.addManagedBean(mb);
                }

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
                    NavigationRule nr = null;
                    NavigationCase nc = null;
                    List<NavigationRule> navigationRules = config.getNavigationRules();
                    for (NavigationRule existingNavigationRule : navigationRules) {
                        List<NavigationCase> navigationCases = existingNavigationRule.getNavigationCases();
                        for (NavigationCase existingNavigationCase : navigationCases) {
                            if ( fromOutcomes[i].equals(existingNavigationCase.getFromOutcome()) ) {
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

                    nc.setFromOutcome(fromOutcomes[i]);
                    nc.setToViewId(toViewIds[i]);
                    if (nrIsNew) {
                        nr.addNavigationCase(nc);
                        config.addNavigationRule(nr);
                    }
                }
            }
            finally {
                //TODO: RETOUCHE correct write to JSF model?
                model.endTransaction();
            }
        }
    }
    
    private static FileObject generateConverter(
            final FileObject controllerFileObject,
            final FileObject pkg,
            final String simpleConverterName,
            final String controllerClass,
            final String simpleControllerName,
            final String entityClass,
            final String simpleEntityName,
            final ElementHandle<ExecutableElement> idGetter,
            final String managedBeanName,
            final boolean isInjection) throws IOException {

        final boolean[] embeddable = new boolean[] { false };
        final String[] idClassSimpleName = new String[1];
        final String[] idPropertyType = new String[1];
        final ArrayList<MethodModel> paramSetters = new ArrayList<MethodModel>();
        final boolean[] fieldAccess = new boolean[] { false };
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
                    embeddable[0] = idClass != null && JsfForm.isEmbeddableClass(idClass);
                    idClassSimpleName[0] = idClass.getSimpleName().toString();
                    idPropertyType[0] = idClass.getQualifiedName().toString();
                    fieldAccess[0] = JsfForm.isFieldAccess(idClass);
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
            controllerVariable = controllerReferenceName + " controller = (" 
                    + controllerReferenceName 
                    + ") facesContext.getApplication().getELResolver().getValue(\nfacesContext.getELContext(), null, \"" 
                    + managedBeanName +"\");\n";
        } else {
            controllerVariable = controllerReferenceName + " controller = ("
                    + controllerReferenceName 
                    + ") facesContext.getApplication().getVariableResolver().resolveVariable(\nfacesContext, \"" 
                    + managedBeanName +"\");\n";
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
                String propName = getPropNameFromMethod(setter.getName());
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
                String propName = getPropNameFromMethod(setter.getName());
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

        FileObject converterFileObject = GenerationUtils.createClass(pkg, simpleConverterName, null);
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
                                "java.util.regex.Matcher"
                    };
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
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
            final String controllerClass,
            final String simpleConverterName,
            final String entityClass, 
            final String simpleEntityName,
            final List<ElementHandle<ExecutableElement>> toOneRelMethods,
            final List<ElementHandle<ExecutableElement>> toManyRelMethods,
            final boolean isInjection,
            final boolean isFieldAccess,
            final FileObject controllerFileObject, 
            final EmbeddedPkSupport embeddedPkSupport) throws IOException {
        
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
                        embeddable[0] = idClass != null && JsfForm.isEmbeddableClass(idClass);
                        idPropertyType[0] = idClass.getQualifiedName().toString();
                    }
                    
                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    TypeElement controllerTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    ClassTree classTree = workingCopy.getTrees().getTree(controllerTypeElement);
                    ClassTree modifiedClassTree = classTree;
                    
                    int privateModifier = java.lang.reflect.Modifier.PRIVATE;
                    int publicModifier = java.lang.reflect.Modifier.PUBLIC;
                    int publicStaticModifier = publicModifier + java.lang.reflect.Modifier.STATIC;
                    
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName, entityClass, privateModifier, null, null);
                   
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName + "s", new TypeInfo("java.util.List", new String[]{entityClass}), privateModifier, null, null);
                    
                    AnnotationInfo[] annotations = null;
                    if (isInjection) {
                        annotations = new AnnotationInfo[1];
                        annotations[0] = new AnnotationInfo("javax.annotation.Resource");
                        modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "utx", "javax.transaction.UserTransaction", privateModifier, null, annotations);
                        
                        if (persistenceUnit == null) {
                            annotations[0] = new AnnotationInfo("javax.persistence.PersistenceUnit");
                        } else {
                            annotations[0] = new AnnotationInfo("javax.persistence.PersistenceUnit", new String[]{"unitName"}, new Object[]{persistenceUnit});
                        }
                    } else {
                        Set<Modifier> publicModifierSet = new HashSet<Modifier>();
                        publicModifierSet.add(Modifier.PUBLIC);
                        MethodTree modifiedConstructor = make.Method(
                                make.Modifiers(publicModifierSet), // public
                                "<init>",
                                null, // return type
                                Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                Collections.<VariableTree>emptyList(), // arguments - none
                                Collections.<ExpressionTree>emptyList(), // throws 
                                "{ emf = Persistence.createEntityManagerFactory(\"" + persistenceUnit + "\"); }", // body text
                                null // default value - not applicable here, used by annotations
                            );
                        MethodTree constructor = null;
                        for(Tree tree : modifiedClassTree.getMembers()) {
                            if(Tree.Kind.METHOD == tree.getKind()) {
                                MethodTree mtree = (MethodTree)tree;
                                List<? extends VariableTree> mTreeParameters = mtree.getParameters();
                                if(mtree.getName().toString().equals("<init>") &&
                                        (mTreeParameters == null || mTreeParameters.size() == 0) &&
                                        !workingCopy.getTreeUtilities().isSynthetic(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree))) {
                                        constructor = mtree;
                                        break;
                                }
                            }
                        }
                        if (constructor == null) {
                            modifiedClassTree = make.addClassMember(modifiedClassTree, modifiedConstructor);
                        }
                        else {
                            workingCopy.rewrite(constructor, modifiedConstructor);
                        }
                    }
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "emf", "javax.persistence.EntityManagerFactory", privateModifier, null, annotations);
                    
                    MethodInfo methodInfo = new MethodInfo("getEntityManager", publicModifier, "javax.persistence.EntityManager", null, null, null, "return emf.createEntityManager();", null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "batchSize", "int", publicModifier, new Integer(5), null);
                    
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "firstItem", "int", privateModifier, new Integer(0), null);
                    
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "itemCount", "int", privateModifier, new Integer(-1), null);
                    
                    String bodyText;
                    StringBuffer updateRelatedInCreate = new StringBuffer();
                    StringBuffer updateRelatedInEditPre = new StringBuffer();
                    StringBuffer attachRelatedInEdit = new StringBuffer();
                    StringBuffer updateRelatedInEditPost = new StringBuffer();
                    StringBuffer updateRelatedInDestroy = new StringBuffer();
                    StringBuffer initRelatedInCreate = new StringBuffer();
                    StringBuffer illegalOrphansInCreate = new StringBuffer();
                    StringBuffer illegalOrphansInEdit = new StringBuffer();
                    StringBuffer illegalOrphansInDestroy = new StringBuffer();
                    StringBuffer initCollectionsInCreate = new StringBuffer();  //useful in case user removes listbox from New.jsp

                    List<ElementHandle<ExecutableElement>> allRelMethods = new ArrayList<ElementHandle<ExecutableElement>>(toOneRelMethods);
                    allRelMethods.addAll(toManyRelMethods);
                    
                    String[] importFqs = {"javax.persistence.Query",
                                "javax.persistence.EntityNotFoundException",
                                "javax.faces.application.FacesMessage",
                                "java.lang.reflect.InvocationTargetException",
                                "java.lang.reflect.Method",
                                "javax.faces.FacesException",
                                "java.util.HashMap"
                    };
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }

                    String oldMe = null;
            
                    // <editor-fold desc=" all relations ">
                    for(Iterator<ElementHandle<ExecutableElement>> it = allRelMethods.iterator(); it.hasNext();) {
                        ElementHandle<ExecutableElement> handle = it.next();
                        ExecutableElement m = handle.resolve(workingCopy);
                        int multiplicity = JsfForm.isRelationship(workingCopy, m, isFieldAccess);
                        ExecutableElement otherSide = JsfForm.getOtherSideOfRelation(workingCopy, m, isFieldAccess);

                        if (otherSide != null) {
                            TypeElement relClass = (TypeElement)otherSide.getEnclosingElement();
                            boolean isRelFieldAccess = JsfForm.isFieldAccess(relClass);
                            int otherSideMultiplicity = JsfForm.isRelationship(workingCopy, otherSide, isRelFieldAccess);
                            TypeMirror t = m.getReturnType();
                            TypeMirror tstripped = JsfForm.stripCollection(t, workingCopy.getTypes());
                            boolean isCollection = t != tstripped;
                            String relType = tstripped.toString();
                            String simpleRelType = simpleClassName(relType); //just "Pavilion"
                            String relTypeReference = simpleRelType;
                            String mName = m.getSimpleName().toString();
                            String otherName = otherSide.getSimpleName().toString();
                            String relFieldName = getPropNameFromMethod(mName);
                            String otherFieldName = getPropNameFromMethod(otherName);
                            
                            boolean columnNullable = JsfForm.isFieldOptionalAndNullable(workingCopy, m, isFieldAccess);
                            boolean relColumnNullable = JsfForm.isFieldOptionalAndNullable(workingCopy, otherSide, isFieldAccess);
                            
                            String relFieldToAttach = isCollection ? relFieldName + relTypeReference + "ToAttach" : relFieldName;
                            String scalarRelFieldName = isCollection ? relFieldName + relTypeReference : relFieldName;
                            
                            if (!isCollection && !controllerClass.equals(entityClass + "Controller")) {
                                modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, relType);
                            }
                            
                            ExecutableElement relIdGetterElement = JsfForm.getIdGetter(workingCopy, isFieldAccess, relClass);
                            String refOrMergeString = getRefOrMergeString(relIdGetterElement, relFieldToAttach);
                            
                            if (isCollection) {
                                initCollectionsInCreate.append("if (" + fieldName + "." + mName + "() == null) {\n" +
                                        fieldName + ".s" + mName.substring(1) + "(new ArrayList<" + relTypeReference + ">());\n" +
                                        "}\n");

                                
                                modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.ArrayList");
                                
                                initRelatedInCreate.append("List<" + relTypeReference + "> attached" + mName.substring(3) + " = new ArrayList<" + relTypeReference + ">();\n" +
                                        "for (" + relTypeReference + " " + relFieldToAttach + " : " + fieldName + "." + mName + "()) {\n" +
                                        relFieldToAttach + " = " + refOrMergeString +
                                        "attached" + mName.substring(3) + ".add(" + relFieldToAttach + ");\n" +
                                        "}\n" +
                                        fieldName + ".s" + mName.substring(1) + "(attached" + mName.substring(3) + ");\n"
                                        );
                            }
                            else {
                                initRelatedInCreate.append(relTypeReference + " " + scalarRelFieldName + " = " + fieldName + "." + mName +"();\n" +
                                    "if (" + scalarRelFieldName + " != null) {\n" +
                                    scalarRelFieldName + " = " + refOrMergeString +
                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + ");\n" +
                                    "}\n");
                            }
                            
                            String relrelInstanceName = "old" + otherName.substring(3) + "Of" + scalarRelFieldName.substring(0, 1).toUpperCase() + (scalarRelFieldName.length() > 1 ? scalarRelFieldName.substring(1) : "");
                            String relrelGetterName = otherName;
                            
                            if (!columnNullable && otherSideMultiplicity == JsfForm.REL_TO_ONE && multiplicity == JsfForm.REL_TO_ONE) {
                                illegalOrphansInCreate.append(
                                        relTypeReference + " " + scalarRelFieldName + "OrphanCheck = " + fieldName + "." + mName +"();\n" +
                                                            "if (" + scalarRelFieldName + "OrphanCheck != null) {\n");
                                illegalOrphansInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "OrphanCheck." + relrelGetterName + "();\n");
                                illegalOrphansInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
                                        "addErrorMessage(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "OrphanCheck + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
                                                "illegalOrphans = true;\n" +
                                        "}\n");
                                illegalOrphansInCreate.append("}\n");
                            }
                            
                            updateRelatedInCreate.append( (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + fieldName + "." + mName + "()){\n" :
                                                            "if (" + scalarRelFieldName + " != null) {\n"));
                                                            //if 1:1, be sure to orphan the related entity's current related entity
                            if (otherSideMultiplicity == JsfForm.REL_TO_ONE){
                                if (multiplicity != JsfForm.REL_TO_ONE || columnNullable) { //no need to declare relrelInstanceName if we have already examined it in the 1:1 orphan check
                                    updateRelatedInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "." + relrelGetterName + "();\n");
                                }
                                if (multiplicity == JsfForm.REL_TO_ONE) {
                                    if (columnNullable) {
                                        updateRelatedInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
                                        relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
                                        "}\n");    
                                    }
                                }
                            }
                            
                            updateRelatedInCreate.append( ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                                            scalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
                                                        scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n");
                            if (multiplicity == JsfForm.REL_TO_MANY && otherSideMultiplicity == JsfForm.REL_TO_ONE){
                                updateRelatedInCreate.append("if " + relrelInstanceName + " != null) {\n" +
                                        relrelInstanceName + "." + mName + "().remove(" + scalarRelFieldName + ");\n" +
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" +
                                        "}\n");
                            }
                            updateRelatedInCreate.append("}\n");
                            
                            if (oldMe == null) {
                                oldMe = "persistent" + simpleEntityName;
                                String oldMeStatement = simpleEntityName + " " + oldMe + " = em.find(" +
                                simpleEntityName + ".class, " + fieldName + "." + idGetterName[0] + "());\n";
                                updateRelatedInEditPre.append("\n " + oldMeStatement);
                            }
                            
                            if (isCollection) {
                                String relFieldOld = relFieldName + "Old";
                                String relFieldNew = relFieldName + "New";
                                String oldScalarRelFieldName = relFieldOld + relTypeReference;
                                String newScalarRelFieldName = relFieldNew + relTypeReference;
                                String oldOfNew = "old" + otherName.substring(3) + "Of" + newScalarRelFieldName.substring(0, 1).toUpperCase() + newScalarRelFieldName.substring(1);
                                updateRelatedInEditPre.append("\n Collection<" + relTypeReference + "> " + relFieldOld + " = " + oldMe + "." + mName + "();\n");
                                updateRelatedInEditPre.append("Collection <" + relTypeReference + "> " + relFieldNew + " = " + fieldName + "." + mName + "();\n");
                                if (!relColumnNullable && otherSideMultiplicity == JsfForm.REL_TO_ONE) {
                                    illegalOrphansInEdit.append(
                                            "for(" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
                                            "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
                                            "addErrorMessage(\"You must retain " + relTypeReference + " \" + " + oldScalarRelFieldName + " + \" since its " + otherFieldName + " field is not nullable.\");\n" +
                                            "illegalOrphans = true;\n" +
                                            "}\n" +
                                            "}\n");
                                }
                                String relFieldToAttachInEdit = newScalarRelFieldName + "ToAttach";
                                String refOrMergeStringInEdit = getRefOrMergeString(relIdGetterElement, relFieldToAttachInEdit);
                                String attachedRelFieldNew = "attached" + mName.substring(3) + "New";
                                attachRelatedInEdit.append("List<" + relTypeReference + "> " + attachedRelFieldNew + " = new ArrayList<" + relTypeReference + ">();\n" +
                                        "for (" + relTypeReference + " " + relFieldToAttachInEdit + " : " + relFieldNew + ") {\n" +
                                        relFieldToAttachInEdit + " = " + refOrMergeStringInEdit +
                                        attachedRelFieldNew + ".add(" + relFieldToAttachInEdit + ");\n" +
                                        "}\n" +
                                        relFieldNew + " = " + attachedRelFieldNew + ";\n" +
                                        fieldName + ".s" + mName.substring(1) + "(" + relFieldNew + ");\n"
                                        );
                                if (otherSideMultiplicity == JsfForm.REL_TO_MANY || relColumnNullable) {
                                    updateRelatedInEditPost.append(
                                        "for (" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
                                        "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
                                        ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? oldScalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
                                            oldScalarRelFieldName + "." + otherName + "().remove(" + fieldName + ");\n") +
                                        oldScalarRelFieldName + " = em.merge(" + oldScalarRelFieldName + ");\n" +
                                        "}\n" +
                                        "}\n");
                                }
                                updateRelatedInEditPost.append("for (" + relTypeReference + " " + newScalarRelFieldName + " : " + relFieldNew + ") {\n" +
                                "if (!" + relFieldOld + ".contains(" + newScalarRelFieldName + ")) {\n" +
                                ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? simpleEntityName + " " + oldOfNew + " = " + newScalarRelFieldName + "." + relrelGetterName + "();\n" +
                                    newScalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                    newScalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
                                newScalarRelFieldName + " = em.merge(" + newScalarRelFieldName + ");\n");
                                if (otherSideMultiplicity == JsfForm.REL_TO_ONE) {
                                    updateRelatedInEditPost.append("if " + oldOfNew + " != null && !" + oldOfNew + ".equals(" + fieldName + ")) {\n" +
                                        oldOfNew + "." + mName + "().remove(" + newScalarRelFieldName + ");\n" +
                                        oldOfNew + " = em.merge(" + oldOfNew + ");\n" +
                                        "}\n");
                                }
                                updateRelatedInEditPost.append("}\n}\n");
                            } else {
                                updateRelatedInEditPre.append("\n" + relTypeReference + " " + scalarRelFieldName + "Old = " + oldMe + "." + mName + "();\n");
                                updateRelatedInEditPre.append(relTypeReference + " " + scalarRelFieldName + "New = " + fieldName + "." + mName +"();\n");
                                if (!relColumnNullable && otherSideMultiplicity == JsfForm.REL_TO_ONE) {
                                    illegalOrphansInEdit.append(
                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
                                        "addErrorMessage(\"You must retain " + relTypeReference + " \" + " + scalarRelFieldName + "Old + \" since its " + otherFieldName + " field is not nullable.\");\n" +
                                        "illegalOrphans = true;\n" +
                                        "}\n");
                                }
                                String refOrMergeStringInEdit = getRefOrMergeString(relIdGetterElement, scalarRelFieldName + "New"); 
                                attachRelatedInEdit.append("if (" + scalarRelFieldName + "New != null) {\n" +
                                    scalarRelFieldName + "New = " + refOrMergeStringInEdit +
                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + "New);\n" +
                                    "}\n");
                                if (otherSideMultiplicity == JsfForm.REL_TO_MANY || relColumnNullable) {
                                     updateRelatedInEditPost.append(   
                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
                                        ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? scalarRelFieldName + "Old.s" + otherName.substring(1) + "(null);\n" :
                                            scalarRelFieldName + "Old." + otherName + "().remove(" + fieldName +");\n") +
                                        scalarRelFieldName + "Old = em.merge(" + scalarRelFieldName +"Old);\n}\n");
                                }
                                if (multiplicity == JsfForm.REL_TO_ONE && otherSideMultiplicity == JsfForm.REL_TO_ONE && !columnNullable) {
                                    illegalOrphansInEdit.append(
                                        "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
                                    illegalOrphansInEdit.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" + 
                                                "if (" + relrelInstanceName + " != null) {\n" + 
                                                "addErrorMessage(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "New + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
                                                "illegalOrphans = true;\n" +
                                                "}\n");
                                    illegalOrphansInEdit.append("}\n");
                                }
                                updateRelatedInEditPost.append(
                                    "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
                                if (multiplicity == JsfForm.REL_TO_ONE && otherSideMultiplicity == JsfForm.REL_TO_ONE && columnNullable) {
                                    updateRelatedInEditPost.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" + 
                                            "if (" + relrelInstanceName + " != null) {\n" + 
                                            relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
                                            relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
                                            "}\n");
                                }
                                updateRelatedInEditPost.append(
                                    ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? scalarRelFieldName + "New.s" + otherName.substring(1) + "(" + fieldName + ");\n" :
                                        scalarRelFieldName + "New." + otherName + "().add(" + fieldName +");\n") +
                                    scalarRelFieldName + "New = em.merge(" + scalarRelFieldName + "New);\n}\n"
                                    );
                            } 
                            
                            if (otherSideMultiplicity == JsfForm.REL_TO_ONE && !relColumnNullable) {
                                String orphanCheckCollection = relFieldName + "OrphanCheck";
                                String orphanCheckScalar = isCollection ? orphanCheckCollection + relTypeReference : relFieldName + "OrphanCheck";
                                illegalOrphansInDestroy.append(
                                        (isCollection ? "Collection<" + relTypeReference + "> " + orphanCheckCollection : relTypeReference + " " + orphanCheckScalar) + " = " + fieldName + "." + mName +"();\n" +
                                        (isCollection ? "for(" + relTypeReference + " " + orphanCheckScalar + " : " + orphanCheckCollection : "if (" + orphanCheckScalar + " != null") + ") {\n" +
                                        "addErrorMessage(\"This " + simpleEntityName + " (\" + " +  fieldName + " + \") cannot be destroyed since the " + relTypeReference + " \" + " + orphanCheckScalar + " + \" in its " + relFieldName + " field has a non-nullable " + otherFieldName + " field.\");\n" +
                                        "illegalOrphans = true;\n" +
                                        "}\n");
                            }
                            if (otherSideMultiplicity == JsfForm.REL_TO_MANY || relColumnNullable) {
                                updateRelatedInDestroy.append( (isCollection ? "Collection<" + relTypeReference + "> " + relFieldName : relTypeReference + " " + scalarRelFieldName) + " = " + fieldName + "." + mName +"();\n" +
                                        (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + relFieldName : "if (" + scalarRelFieldName + " != null") + ") {\n" +
                                        ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
                                            scalarRelFieldName + "." + otherName + "().remove(" + fieldName +");\n") +
                                        scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n}\n\n");
                            }
                            
                            if (multiplicity == JsfForm.REL_TO_MANY) {
                                importFqs = new String[]{"java.util.Arrays",
                                            "java.util.Collection"
                                  };
                                for (String importFq : importFqs) {
                                    modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                                }
                                
                                String relatedToAddName = getPropNameFromMethod(mName);
                   
                                bodyText = "List<" + simpleRelType + "> " + relatedToAddName + "List = Arrays.asList(" + relatedToAddName + ");\n" +
                                        fieldName + ".s" + mName.substring(1) + "(" + relatedToAddName + "List);";
                                methodInfo = new MethodInfo("s" + mName.substring(1) + "Of" + simpleEntityName, publicModifier, "void", null, new String[]{relType + "[]"}, new String[]{relatedToAddName}, bodyText, null, null);
                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                
                                bodyText = "Collection<" + simpleRelType + "> " + relatedToAddName + " = " + fieldName + "." + mName + "();\n" +
                                        "if (" + relatedToAddName + " == null) {\n" + 
                                        "return new " + simpleRelType + "[0];\n" + 
                                        "}\n" + 
                                        "return " + relatedToAddName + ".toArray(new " + simpleRelType + "[0]);";
                                methodInfo = new MethodInfo(mName + "Of" + simpleEntityName, publicModifier, relType + "[]", null, null, null, bodyText, null, null);
                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                            }
                            
                        } else {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
                        }

                    }
                    // </editor-fold>
                    
                    bodyText = "return get" + simpleEntityName + "sAvailable(false);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "sAvailableSelectMany", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return get" + simpleEntityName + "sAvailable(true);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "sAvailableSelectOne", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "List<" + simpleEntityName + "> all" + simpleEntityName + "s = get" + simpleEntityName + "s(true);\n" + 
                        "int size = one ? all" + simpleEntityName + "s.size() + 1 : all" + simpleEntityName + "s.size();\n" + 
                        "SelectItem[] items = new SelectItem[size];\n" + 
                        "int i = 0;\n" + 
                        "if (one) {\n" + 
                        "items[0] = new SelectItem(\"\", \"---\");\n" + 
                        "i++;\n" + 
                        "}\n" + 
                        "for (" + simpleEntityName + " x : all" + simpleEntityName + "s) {\n" + 
                        "items[i++] = new SelectItem(x, x.toString());\n" + 
                        "}\n" + 
                        "return items;";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "sAvailable", privateModifier, "javax.faces.model.SelectItem[]", null, new String[]{"boolean"}, new String[]{"one"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    String getFromReqParamMethod = "get" + simpleEntityName + "FromRequest";
                    
                    bodyText = "if (" + fieldName + " == null) {\n" +
                            fieldName + " = " + getFromReqParamMethod + "();\n" +
                            "}\n" + 
                            "if (" + fieldName + " == null) {\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" +
                            "}\n" + 
                            "return " + fieldName + ";";
                    methodInfo = new MethodInfo("get" + simpleEntityName, publicModifier, entityClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    bodyText = "reset(true);\n" + 
                            "return \"" + fieldName + "_list\";";
                    methodInfo = new MethodInfo("listSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "reset(false);\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" + 
                            (embeddable[0] ? fieldName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" : "") +
                            "return \"" + fieldName + "_create\";";
                    methodInfo = new MethodInfo("createSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    //mbohm: have calls to em.joinTransaction() after BEGIN been removed on purpose?
                    String BEGIN = isInjection ? "utx.begin();" : "em.getTransaction().begin();";
                    String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
                    String ROLLBACK = isInjection ? "utx.rollback();" : "em.getTransaction().rollback();";
                    
                    String newEntityStringVar = "new" + simpleEntityName + "String";
                    String entityStringVar = fieldName + "String";
                    
                    if (illegalOrphansInCreate.length() > 0) {
                        illegalOrphansInCreate.insert(0, "boolean illegalOrphans = false;\n");
                        illegalOrphansInCreate.append("if (illegalOrphans) {\n" +
                                "return null;\n" +
                                "}\n");
                    }
                    
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

                    boolean isGenerated = JsfForm.isGenerated(workingCopy, idGetterElement, isFieldAccess);
                    bodyText = initCollectionsInCreate.toString() +
                            codeToPopulatePkFields.toString() +
                            illegalOrphansInCreate.toString() +
                            "EntityManager em = getEntityManager();\n" + 
                            "try {\n " + BEGIN + "\n " + initRelatedInCreate.toString() + "em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n" +   //NOI18N
                            "addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n"  + //NOI18N
                            "} catch (Exception ex) {\n try {\n" +
                            (isGenerated ? "ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" : 
                            "if (find" + simpleEntityName + "(" + fieldName + "." + idGetterName[0] + "()) != null) {\n" +
                            "addErrorMessage(\"" + simpleEntityName + " \" + " + fieldName + " + \" already exists.\");\n" +
                            "} else {\n" +
                            "ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" + 
                            "}\n") +
                            ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
                            "}\nreturn null;\n} " +   //NOI18N
                            "finally {\n em.close();\n }\n" + 
                            "return listSetup();";
                    methodInfo = new MethodInfo("create", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_detail\");";
                    methodInfo = new MethodInfo("detailSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_edit\");";
                    methodInfo = new MethodInfo("editSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = "reset(false);\n" + 
                            fieldName + " = " + getFromReqParamMethod + "();\n" +
                            "if (" + fieldName + " == null) {\n" +
                            "String request" + simpleEntityName + "String = getRequestParameter(\"jsfcrud.current" +  simpleEntityName + "\");\n" +
                            "addErrorMessage(\"The " + fieldName + " with id \" + request" + simpleEntityName + "String + \" no longer exists.\");\n";
                    String relatedControllerOutcomeSwath = "String relatedControllerOutcome = relatedControllerOutcome();\n" +
                            "if (relatedControllerOutcome != null {\n" +
                            "return relatedControllerOutcome;\n" +
                            "}\n";
                    bodyText += relatedControllerOutcomeSwath + 
                            "return listSetup();\n" +
                            "}\n" +
                            "return destination;";
                    methodInfo = new MethodInfo("scalarSetup", privateModifier, "java.lang.String", null, new String[]{"java.lang.String"}, new String[]{"destination"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    entityStringVar = fieldName + "String";
                    String currentEntityStringVar = "current" + simpleEntityName + "String";
                    
                    if (illegalOrphansInEdit.length() > 0) {
                        illegalOrphansInEdit.insert(0, "boolean illegalOrphans = false;\n");
                        illegalOrphansInEdit.append("if (illegalOrphans) {\n" +
                                "utx.rollback();\n" +
                                "return null;\n" +
                                "}\n");
                    }                    
                    
                    bodyText = codeToPopulatePkFields.toString() + 
                            simpleConverterName + " converter = new " + simpleConverterName + "();\n" +
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "String " + currentEntityStringVar + " = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "if " + entityStringVar + " == null || " + entityStringVar + ".length() == 0 || !" + entityStringVar + ".equals(" + currentEntityStringVar + ")) {\n" +
                            "String outcome = editSetup();\n" +
                            "if (\"" + fieldName + "_edit\".equals(outcome)) {\n" +
                            "addErrorMessage(\"Could not edit " + fieldName + ". Try again.\");\n" +
                            "}\n" +
                            "return outcome;\n" +
                            "}\n";
                    bodyText += "EntityManager em = getEntityManager();\n" + 
                        "try {\n " + BEGIN + "\n" + updateRelatedInEditPre.toString() + illegalOrphansInEdit.toString() + attachRelatedInEdit.toString() +
                        fieldName + " = em.merge(" + fieldName + ");\n " + 
                        updateRelatedInEditPost.toString() + COMMIT + "\n" +   //NOI18N
                        "addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n" +   //NOI18N
                        "} catch (Exception ex) {\n try {\n String msg = ex.getLocalizedMessage();\n" + 
                        "if (msg != null && msg.length() > 0) {\n" +
                        "addErrorMessage(msg);\n" +
                        "}\n" +
                        "else if (" + getFromReqParamMethod + "() == null) {\n" +
                        "addErrorMessage(\"The " + fieldName + " with id \" + current" + simpleEntityName + "String + \" no longer exists.\");\n" +
                        ROLLBACK +
                        "\nreturn listSetup();\n" +
                        "}\n" +
                        "else {\n" +
                        "addErrorMessage(\"A persistence error occurred.\");\n" +
                        "}\n" +
                        ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
                        "}\nreturn null;\n} " +   //NOI18N
                        "finally {\n em.close();\n }\n" +  //NOI18N
                        "return detailSetup();";
                    methodInfo = new MethodInfo("edit", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    if (illegalOrphansInDestroy.length() > 0) {
                        illegalOrphansInDestroy.insert(0, "boolean illegalOrphans = false;\n");
                        illegalOrphansInDestroy.append("if (illegalOrphans) {\n" +
                                ROLLBACK + "\n" +
                                "return null;\n" +
                                "}\n");
                    }
                    
                    String refOrMergeStringInDestroy = "em.merge(" + fieldName + ");\n";
                    if (idGetterElement != null) {
                        refOrMergeStringInDestroy = "em.getReference(" + simpleEntityName + ".class, ";
                        if (embeddable[0]) {
                            refOrMergeStringInDestroy += "new " + simpleConverterName + "().getId(idAsString));\n";
                        }
                        else {
                            refOrMergeStringInDestroy += "id);\n";
                        }
                    }
                    bodyText = "EntityManager em = getEntityManager();\n" + 
                        "try {\n " + BEGIN + "\n" + 
                        "String idAsString = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                        "try {\n " + 
                        (embeddable[0] ? "" : createIdFieldDeclaration(idPropertyType[0], "idAsString") + "\n") + 
                        fieldName + " = " + refOrMergeStringInDestroy + 
                        fieldName + "." + idGetterName[0] + "();\n" +
                        "} catch (EntityNotFoundException enfe) {\n" +
                        "addErrorMessage(\"The " + fieldName + " with id \" + idAsString + \" no longer exists.\");\n" +
                        "String notFoundOutcome = relatedControllerOutcome();\n" +
                        "if (notFoundOutcome == null) {\n" +
                        "notFoundOutcome = listSetup();\n" +
                        "}\n" +
                        ROLLBACK + "\n" +
                        "return notFoundOutcome;\n" +
                        "}\n" + 
                        illegalOrphansInDestroy.toString() +
                        updateRelatedInDestroy.toString() + 
                        "em.remove(" + fieldName + ");\n " + COMMIT + "\n" +   //NOI18N
                        "addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n" +   //NOI18N
                        "} catch (Exception ex) {\n try {\n ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" + ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
                        "}\nreturn null;\n} " +   //NOI18N
                        "finally {\n em.close();\n }\n" +  //NOI18N
                        relatedControllerOutcomeSwath + 
                            "return listSetup();";
                    methodInfo = new MethodInfo("destroy", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "String theId = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "return (" + simpleEntityName + ")new " + simpleConverterName + "().getAsObject(FacesContext.getCurrentInstance(), null, theId);";
                    methodInfo = new MethodInfo(getFromReqParamMethod, privateModifier, entityClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
                    bodyText = "return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);";
                    methodInfo = new MethodInfo("getRequestParameter", privateModifier, "java.lang.String", null, new String[]{"java.lang.String"}, new String[]{"key"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);        
                    
                    TypeInfo listOfEntityType = new TypeInfo("java.util.List", new String[]{entityClass});
                    
                    bodyText = "if (" + fieldName + "s == null) {\n" +
                            fieldName + "s = get" + simpleEntityName + "s(false);\n" +
                            "}\n" +
                            "return " + fieldName + "s;";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "s", publicModifier, listOfEntityType, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        "Query q = em.createQuery(\"select object(o) from " + simpleEntityName +" as o\");\n" + 
                        "if (!all) {\n" +
                        "q.setMaxResults(batchSize);\n" + 
                        "q.setFirstResult(getFirstItem());\n" + 
                        "}\n" +
                        "return q.getResultList();\n" + 
                        "} finally {\n em.close();\n}\n";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "s", publicModifier, listOfEntityType, null, TypeInfo.fromStrings(new String[]{"boolean"}), new String[]{"all"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
                    bodyText = "String msg = ex.getLocalizedMessage();\n" +
                            "if (msg != null && msg.length() > 0) {\n" +
                            "addErrorMessage(msg);\n" +
                            "}\n" +
                            "else {\n" +
                            "addErrorMessage(defaultMsg);\n" +
                            "}\n";
                    methodInfo = new MethodInfo("ensureAddErrorMessage", privateModifier, "void", null, new String[]{"java.lang.Exception", "java.lang.String"}, new String[]{"ex", "defaultMsg"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);\n" + //NOI18N
                        "FacesContext.getCurrentInstance().addMessage(null, facesMsg);"; //NOI18N
                    methodInfo = new MethodInfo("addErrorMessage", publicStaticModifier, "void", null, new String[]{"java.lang.String"}, new String[]{"msg"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);\n" + //NOI18N
                        "FacesContext.getCurrentInstance().addMessage(\"successInfo\", facesMsg);"; //NOI18N
                    methodInfo = new MethodInfo("addSuccessMessage", publicStaticModifier, "void", null, new String[]{"java.lang.String"}, new String[]{"msg"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    //getter for converter
                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        simpleEntityName + " o = (" + simpleEntityName + ") em.find(" + simpleEntityName + ".class, id);\n" + 
                        "return o;\n" + 
                        "} finally {\n em.close();\n}\n";
                    methodInfo = new MethodInfo("find" + simpleEntityName, publicModifier, entityClass, null, new String[]{idPropertyType[0]}, new String[]{"id"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
                    bodyText = "if (itemCount == -1) {\n" +
                            "EntityManager em = getEntityManager();\n try{\n" + 
                        "itemCount = ((Long) em.createQuery(\"select count(o) from " + simpleEntityName + " as o\").getSingleResult()).intValue();\n" + 
                        "} finally {\n em.close();\n}\n" +
                        "}\n" +
                        "return itemCount;";
                    methodInfo = new MethodInfo("getItemCount", publicModifier, "int", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "getItemCount();\n" +
                            "if (firstItem >= itemCount) {\n" +
                            "if (itemCount == 0) {\n" +
                            "firstItem = 0;\n" +
                            "}\n" +
                            "else {\n" +
                            "int zeroBasedItemCount = itemCount - 1;\n" +
                            "double pageDouble = zeroBasedItemCount / batchSize;\n" +
                            "int page = (int)Math.floor(pageDouble);\n" +
                            "firstItem = page * batchSize;\n" +
                            "}\n" +
                            "}\n" +
                            "return firstItem;";
                    methodInfo = new MethodInfo("getFirstItem", publicModifier, "int", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "getFirstItem();\n" +
                            "return firstItem + batchSize > itemCount ? itemCount : firstItem + batchSize;";
                    methodInfo = new MethodInfo("getLastItem", publicModifier, "int", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    methodInfo = new MethodInfo("getBatchSize", publicModifier, "int", null, null, null, "return batchSize;", null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "reset(false);\n" +
                            "getFirstItem();\n" +
                            "if firstItem + batchSize < itemCount) {\n" +
                            "firstItem += batchSize;\n" +
                            "}\n" +
                            "return \"" + fieldName + "_list\"";
                    methodInfo = new MethodInfo("next", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "reset(false);\n" +
                            "getFirstItem();\n" +
                            "firstItem -= batchSize;\n if (firstItem < 0) {\nfirstItem = 0;\n}\n" + 
                        "return \"" + fieldName + "_list\";\n";
                    methodInfo = new MethodInfo("prev", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "String relatedControllerString = getRequestParameter(\"jsfcrud.relatedController\");\n" +
                        "String relatedControllerTypeString = getRequestParameter(\"jsfcrud.relatedControllerType\");\n" +
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
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = fieldName + " = null;\n" +
                            fieldName + "s = null;\n" +
                            "itemCount = -1;\n" +
                            "if (resetFirstItem) {\n" +
                            "firstItem = 0;\n" +
                            "}\n";
                    methodInfo = new MethodInfo("reset", privateModifier, "void", null, new String[]{"boolean"}, new String[]{"resetFirstItem"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    TypeInfo asStringType = new TypeInfo("java.util.Map", new String[]{"java.lang.Object", "java.lang.String"});
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "asString", asStringType, privateModifier, null, null);

                    bodyText = "if (asString == null) {\n" +
                            "asString = new HashMap<Object,String>() {" +
                            "@Override\n" +
                            "public String get(Object key) {\n" +
                            "if (key instanceof Object[]) {\n" +
                            "Object[] keyAsArray = (Object[])key;\n" +
                            "if (keyAsArray.length == 0) {\n" +
                            "return \"(No Items)\";\n" +
                            "}\n" +
                            "StringBuffer sb = new StringBuffer();\n" +
                            "for (int i = 0; i < keyAsArray.length; i++) {\n" +
                            "if (i > 0) {\n" +
                            "sb.append(\"<br />\");\n" +
                            "}\n" +
                            "sb.append(keyAsArray[i]);\n" +
                            "}\n" +
                            "return sb.toString();\n" +
                            "}\n" +
                            "return new " + simpleConverterName + "().getAsString(FacesContext.getCurrentInstance(), null, (" + simpleEntityName + ")key);\n" +
                            "}\n" +
                            "};\n" +
                            "}\n" +
                            "return asString;";
                    methodInfo = new MethodInfo("getAsString", publicModifier, asStringType, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    String newEntityStringInit;
                    if (embeddable[0]) {
                        newEntityStringInit = simpleEntityName + " new" + simpleEntityName + " = new " + simpleEntityName + "();\n" +
                                "new" + simpleEntityName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" + 
                                "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new" + simpleEntityName + ");\n";
                    }
                    else {
                        newEntityStringInit = "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new " + simpleEntityName + "());\n";
                    }
                    bodyText = simpleConverterName + " converter = new " + simpleConverterName + "();\n" +
                            newEntityStringInit +
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "if (!" + newEntityStringVar + ".equals(" + entityStringVar + ")) {\n" +
                            "createSetup();\n" +
                            //"throw new ValidatorException(new FacesMessage(\"Could not create " + fieldName + ". Try again.\"));\n" +
                            "}\n";
                    methodInfo = new MethodInfo("validateCreate", publicModifier, "void", null, new String[]{"javax.faces.context.FacesContext", "javax.faces.component.UIComponent", "java.lang.Object"}, new String[]{"facesContext", "component", "value"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }).commit();
    
        return controllerFileObject;
    }
    
    private static String getRefOrMergeString(ExecutableElement relIdGetterElement, String relFieldToAttach) {
        String refOrMergeString = "em.merge(" + relFieldToAttach + ");\n";
        if (relIdGetterElement != null) {
            String relIdGetter = relIdGetterElement.getSimpleName().toString();
            refOrMergeString = "em.getReference(" + relFieldToAttach + ".getClass(), " + relFieldToAttach + "." + relIdGetter + "());\n";
        }
        return refOrMergeString;
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
    
    public static String simpleClassName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot > 0 ? fqn.substring(lastDot + 1) : fqn;
    }

    public static String fieldFromClassName(String className) {
        boolean makeFirstLower = className.length() == 1 || (!Character.isUpperCase(className.charAt(1)));
        String candidate = makeFirstLower ? className.substring(0,1).toLowerCase() + className.substring(1) : className;
        if (!Utilities.isJavaIdentifier(candidate)) {
            candidate += "1"; //NOI18N
        }
        return candidate;
    }
    
    public static String getManagedBeanName(String simpleEntityName) {
        int len = simpleEntityName.length();
        return len > 1 ? simpleEntityName.substring(0,1).toLowerCase() + simpleEntityName.substring(1) : simpleEntityName.toLowerCase();
    }
    
    public static String getPropNameFromMethod(String name) {
        //getABcd should be converted to ABcd, getFooBar should become fooBar
        //getA1 is "a1", getA_ is a_, getAB is AB
        boolean makeFirstLower = name.length() < 5 || (!Character.isUpperCase(name.charAt(4)));
        return makeFirstLower ? name.substring(3,4).toLowerCase() + name.substring(4) : name.substring(3);
    }

    private static void addImplementsClause(FileObject fileObject, final String className, final String interfaceName) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] modified = new boolean[] { false };
        ModificationResult modificationResult = javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                TypeMirror interfaceType = workingCopy.getElements().getTypeElement(interfaceName).asType();
                if (!workingCopy.getTypes().isSubtype(typeElement.asType(), interfaceType)) {
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    GenerationUtils.newInstance(workingCopy).addImplementsClause(classTree, interfaceName);
                    modified[0] = true;
                }
            }
        });
        if (modified[0]) {
            modificationResult.commit();
        }
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
    
    static class TypeInfo {
        
        private String rawType;
        private TypeInfo[] declaredTypeParameters;
        
        public String getRawType() {
            return rawType;
        }

        public TypeInfo[] getDeclaredTypeParameters() {
            return declaredTypeParameters;
        }
       
        public TypeInfo(String rawType) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
        }
        
        public TypeInfo(String rawType, TypeInfo[] declaredTypeParameters) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                return;
            }
            this.declaredTypeParameters = declaredTypeParameters;
        }
        
        public TypeInfo(String rawType, String[] declaredTypeParamStrings) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
            if (declaredTypeParamStrings == null || declaredTypeParamStrings.length == 0) {
                return;
            }
            this.declaredTypeParameters = TypeInfo.fromStrings(declaredTypeParamStrings);
        }
        
        public static TypeInfo[] fromStrings(String[] strings) {
            if (strings == null || strings.length == 0) {
                return null;
            }
            TypeInfo[] typeInfos = new TypeInfo[strings.length];
            for (int i = 0; i < strings.length; i++) {
                typeInfos[i] = new TypeInfo(strings[i]);
            }
            return typeInfos;
        }
    }
    
    static class MethodInfo {
        
        private String         name;
        private int            modifiers;
        private TypeInfo          returnType;
        private TypeInfo[]        exceptionTypes;
        private TypeInfo[]        parameterTypes;
        private String[]       parameterNames;
        private String         methodBodyText;
        private AnnotationInfo[] annotations;
        private String         commentText;
        
        /**
         * Constructs a MethodInfo with the specified name, modifiers,
         * returnType, parameterTypes, parameterNames, methodBody, and commentText.
         *
         * @param name The method name for this MethodInfo
         * @param modifiers The method {@link Modifier} bits
         * @param returnType The return type for this MethodInfo
         * @param exceptionsThrown The exceptions the method throws
         * @param parameterTypes The parameter types for this MethodInfo
         * @param parameterNames The parameter names for this MethodInfo
         * @param methodBodyText The Java source code for the body of this MethodInfo
         * @param annotations The annotation for this MethodInfo
         * @param commentText The comment text for this MethodInfo
         */
        public MethodInfo(String name, int modifiers, TypeInfo returnType, TypeInfo[] exceptionTypes,
                TypeInfo[] parameterTypes, String[] parameterNames, String methodBodyText,  AnnotationInfo[] annotations,
                String commentText) {
            
            this.name = name;
            this.modifiers = modifiers;
            this.returnType = returnType;
            this.exceptionTypes = exceptionTypes;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.methodBodyText = methodBodyText;
            this.annotations = annotations;
            this.commentText = commentText;
        }
        
        public MethodInfo(String name, int modifiers, String returnType, String[] exceptionTypes,
                String[] parameterTypes, String[] parameterNames, String methodBodyText,  AnnotationInfo[] annotations,
                String commentText) {
            
            this.name = name;
            this.modifiers = modifiers;
            this.returnType = new TypeInfo(returnType);
            this.exceptionTypes = TypeInfo.fromStrings(exceptionTypes);
            this.parameterTypes = TypeInfo.fromStrings(parameterTypes);
            this.parameterNames = parameterNames;
            this.methodBodyText = methodBodyText;
            this.annotations = annotations;
            this.commentText = commentText;
        }
        
        public String getName() {
            return name;
        }
        
        public int getModifiers() {
            return modifiers;
        }
        
        public TypeInfo getReturnType() {
            return returnType;
        }
        
        public TypeInfo[] getExceptionTypes() {
            return exceptionTypes;
        }
        
        public String getMethodBodyText() {
            return methodBodyText;
        }
        
        public TypeInfo[] getParameterTypes() {
            return parameterTypes;
        }
        
        public String[] getParameterNames() {
            return parameterNames;
        }
        
        public  AnnotationInfo[] getAnnotations() {
            return annotations;
        }
        
        public String getCommentText() {
            return commentText;
        }
    }
    
    static class AnnotationInfo {
        private String type;
        private String[] argNames;
        private Object[] argValues;
        
        public AnnotationInfo(String type) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
        }
        
        public AnnotationInfo(String type, String[] argNames, Object[] argValues) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
            if (argNames == null) {
                if (argValues != null) {
                    throw new IllegalArgumentException();
                }
            } else if (argValues == null || argValues.length != argNames.length) {
                throw new IllegalArgumentException();
            }
            this.argNames = argNames;
            this.argValues = argValues;
        }
        
        public String getType() {
            return type;
        }
        
        public String[] getArgNames() {
            return argNames;
        }
        
        public Object[] getArgValues() {
            return argValues;
        }
    }
    
    static class TreeMakerUtils {
        
        public static ClassTree addVariable(ClassTree classTree, WorkingCopy wc, String name, TypeInfo type, int modifiers, Object initializer, AnnotationInfo[] annotations) {
            Tree typeTree = createType(wc, type);
            ModifiersTree modTree = createModifiers(wc, modifiers, annotations);
            TreeMaker make = wc.getTreeMaker();
            VariableTree tree = make.Variable(modTree, name, typeTree, make.Literal(initializer));
            return make.addClassMember(classTree, tree);
        }
        
        public static ClassTree addVariable(ClassTree classTree, WorkingCopy wc, String name, String type, int modifiers, Object initializer, AnnotationInfo[] annotations) {
            return addVariable(classTree, wc, name, new TypeInfo(type), modifiers, initializer, annotations);
        }
        
    /*
     * Creates a new variable tree for a given name and type
     */
        private static VariableTree createVariable(WorkingCopy wc, String name, TypeInfo type) {
            return createVariable(wc, name, createType(wc, type));
        }
        
    /*
     * Creates a new variable tree for a given name and type
     */
        private static VariableTree createVariable(WorkingCopy wc, String name, Tree type) {
            TreeMaker make = wc.getTreeMaker();
            return make.Variable(createModifiers(wc), name, type, null);
        }
        
        public static ClassTree addMethod(ClassTree classTree, WorkingCopy wc, MethodInfo mInfo) {
            MethodTree tree = createMethod(wc, mInfo);
            return wc.getTreeMaker().addClassMember(classTree, tree);
        }
        
    /*
     * Creates a method given context method and return type name
     */
        private static MethodTree createMethod(WorkingCopy wc, MethodInfo mInfo) {
            TreeMaker make = wc.getTreeMaker();
            TypeInfo[] pTypes = mInfo.getParameterTypes();
            String[] pNames = mInfo.getParameterNames();
            List<VariableTree> params = new ArrayList<VariableTree>();
            for (int i = 0 ; pTypes != null && i < pTypes.length; i++) {
                VariableTree vtree = createVariable(wc, pNames[i], pTypes[i]);
                params.add(vtree);
            }
            
            TypeInfo[] excepTypes = mInfo.getExceptionTypes();
            List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
            for (int i = 0 ; excepTypes != null && i < excepTypes.length; i++) {
                throwsList.add((ExpressionTree)createType(wc, excepTypes[i]));
            }
            
            String body = mInfo.getMethodBodyText();
            if(body == null) {
                body = "";
            }
            
            MethodTree mtree = make.Method(createModifiers(wc, mInfo.getModifiers(), mInfo.getAnnotations()),
                    mInfo.getName(),
                    createType(wc, mInfo.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    params,
                    throwsList,
                    "{" + body + "}",
                    null
                    );
            
            //         if(mInfo.getCommentText() != null) {
            //             Comment comment = Comment.create(Comment.Style.JAVADOC, -2,
            //                     -2, -2, mInfo.getCommentText());
            //             make.addComment(mtree, comment, true);
            //         }
            
            return mtree;
        }
        
    /*
     * Returns a tree for a given type in string format
     * Note that import for type is handled by make.QualIdent()
     */
        private static Tree createType(WorkingCopy wc, TypeInfo type) {
            if(type == null) {
                return null;
            }
            String rawType = type.getRawType();
            
            TreeMaker make = wc.getTreeMaker();
            if (rawType.endsWith("[]")) { // NOI18N
                String rawTypeName = rawType.substring(0, rawType.length()-2);
                TypeInfo scalarTypeInfo = new TypeInfo(rawTypeName, type.getDeclaredTypeParameters());
                return make.ArrayType(createType(wc, scalarTypeInfo));
            }
            
            TypeKind primitiveTypeKind = null;
            if ("boolean".equals(rawType)) {           // NOI18N
                primitiveTypeKind = TypeKind.BOOLEAN;
            } else if ("byte".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.BYTE;
            } else if ("short".equals(rawType)) {      // NOI18N
                primitiveTypeKind = TypeKind.SHORT;
            } else if ("int".equals(rawType)) {        // NOI18N
                primitiveTypeKind = TypeKind.INT;
            } else if ("long".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.LONG;
            } else if ("char".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.CHAR;
            } else if ("float".equals(rawType)) {      // NOI18N
                primitiveTypeKind = TypeKind.FLOAT;
            } else if ("double".equals(rawType)) {     // NOI18N
                primitiveTypeKind = TypeKind.DOUBLE;
            } else if ("void".equals(rawType)) {
                primitiveTypeKind = TypeKind.VOID;
            }
            if (primitiveTypeKind != null) {
                return make.PrimitiveType(primitiveTypeKind);
            }
            
            TypeInfo[] declaredTypeParameters = type.getDeclaredTypeParameters();
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                TypeElement typeElement = wc.getElements().getTypeElement(rawType);
                if (typeElement == null) {
                    throw new IllegalArgumentException("Type " + rawType + " cannot be found"); // NOI18N
                }
                return make.QualIdent(typeElement);
            }
            else {
                TypeMirror typeMirror = getTypeMirror(wc, type);
                return make.Type(typeMirror);
            }
        }
        
        private static TypeMirror getTypeMirror(WorkingCopy wc, TypeInfo type) {
            TreeMaker make = wc.getTreeMaker();
            String rawType = type.getRawType();
            TypeElement rawTypeElement = wc.getElements().getTypeElement(rawType);
            if (rawTypeElement == null) {
                throw new IllegalArgumentException("Type " + rawType + " cannot be found"); // NOI18N
            }
            TypeInfo[] declaredTypeParameters = type.getDeclaredTypeParameters();
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                make.QualIdent(rawTypeElement);
                return rawTypeElement.asType();
            }
            else {
                TypeMirror[] declaredTypeMirrors = new TypeMirror[declaredTypeParameters.length];
                for (int i = 0; i < declaredTypeParameters.length; i++) {
                    declaredTypeMirrors[i] = getTypeMirror(wc, declaredTypeParameters[i]);
                }
                DeclaredType declaredType = wc.getTypes().getDeclaredType(rawTypeElement, declaredTypeMirrors);
                return declaredType;
            }
        }
        
        private static ModifiersTree createModifiers(WorkingCopy wc) {
            return wc.getTreeMaker().Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
        }
        
        private static ModifiersTree createModifiers(WorkingCopy wc, long flags, AnnotationInfo[] annotations) {
            if (annotations == null || annotations.length == 0) {
                return wc.getTreeMaker().Modifiers(flags, Collections.<AnnotationTree>emptyList());
            }
            GenerationUtils generationUtils = GenerationUtils.newInstance(wc);
            List<AnnotationTree> annotationTrees = new ArrayList<AnnotationTree>();
            for (AnnotationInfo annotation : annotations) {
                //append an AnnotationTree
                String[] argNames = annotation.getArgNames();
                if (argNames != null && argNames.length > 0) {
                    //one or more args in this annotation
                    Object[] argValues = annotation.getArgValues();
                    List<ExpressionTree> argTrees = new ArrayList<ExpressionTree>();
                    for (int i = 0; i < argNames.length; i++) {
                        ExpressionTree argTree = generationUtils.createAnnotationArgument(argNames[i], argValues[i]);
                        argTrees.add(argTree);
                    }
                    AnnotationTree annotationTree = generationUtils.createAnnotation(annotation.getType(), argTrees);
                    annotationTrees.add(annotationTree);
                } else {
                    //no args in this annotation
                    AnnotationTree annotationTree = generationUtils.createAnnotation(annotation.getType());
                    annotationTrees.add(annotationTree);
                }
            }
            return wc.getTreeMaker().Modifiers(flags, annotationTrees);
        }
        
        public static CompilationUnitTree createImport(WorkingCopy wc, CompilationUnitTree modifiedCut, String fq) {
            if (modifiedCut == null) {
                modifiedCut = wc.getCompilationUnit();  //use committed cut as modifiedCut
            }
            List<? extends ImportTree> imports = modifiedCut.getImports();
            boolean found = false;
            for (ImportTree imp : imports) {
               if (fq.equals(imp.getQualifiedIdentifier().toString())) {
                   found = true; 
                   break;
               }
            }
            if (!found) {
                TreeMaker make = wc.getTreeMaker();
                CompilationUnitTree newCut = make.addCompUnitImport(
                    modifiedCut, 
                    make.Import(make.Identifier(fq), false)
                );                                              //create a newCut from modifiedCut
                wc.rewrite(wc.getCompilationUnit(), newCut);    //replace committed cut with newCut in change map
                return newCut;                                  //return the newCut we just created
            }
            return modifiedCut; //no newCut created from modifiedCut, so just return modifiedCut
        }
        
    }
    
    public static class EmbeddedPkSupport {
        private Map<TypeElement,EmbeddedPkSupportInfo> typeToInfo = new HashMap<TypeElement,EmbeddedPkSupportInfo>();
        
        public Set<ExecutableElement> getPkAccessorMethods(CompilationController controller, TypeElement type) {
            EmbeddedPkSupportInfo info = getInfo(controller, type);
            return info.getPkAccessorMethods();
        }
        
        public String getCodeToPopulatePkField(CompilationController controller, TypeElement type, ExecutableElement pkAccessorMethod) {
            EmbeddedPkSupportInfo info = getInfo(controller, type);
            String code = info.getCodeToPopulatePkField(pkAccessorMethod);
            if (code != null) {
                return code;
            }
            
            code = "";
            ExecutableElement relationshipMethod = info.getRelationshipMethod(pkAccessorMethod);
            String referencedColumnName = info.getReferencedColumnName(pkAccessorMethod);
            if (relationshipMethod == null || referencedColumnName == null) {
                info.putCodeToPopulatePkField(pkAccessorMethod, code);
                return code;
            }
            
            TypeMirror relationshipTypeMirror = relationshipMethod.getReturnType();
            if (TypeKind.DECLARED != relationshipTypeMirror.getKind()) {
                info.putCodeToPopulatePkField(pkAccessorMethod, code);
                return code;
            }
            DeclaredType declaredType = (DeclaredType) relationshipTypeMirror;
            TypeElement relationshipType = (TypeElement) declaredType.asElement();
            
            EmbeddedPkSupportInfo relatedInfo = getInfo(controller, relationshipType);
            String accessorString = relatedInfo.getAccessorString(referencedColumnName);
            if (accessorString == null) {
                info.putCodeToPopulatePkField(pkAccessorMethod, code);
                return code;
            }
            
            code = relationshipMethod.getSimpleName().toString() + "()." + accessorString;
            info.putCodeToPopulatePkField(pkAccessorMethod, code);
            return code;
        }
        
        public boolean isRedundantWithRelationshipField(CompilationController controller, TypeElement type, ExecutableElement pkAccessorMethod) {
            return getCodeToPopulatePkField(controller, type, pkAccessorMethod).length() > 0;
        }
        
        public boolean isRedundantWithPkFields(CompilationController controller, TypeElement type, ExecutableElement relationshipMethod) {
            EmbeddedPkSupportInfo info = getInfo(controller, type);
            return info.isRedundantWithPkFields(relationshipMethod);
        }
        
        private EmbeddedPkSupportInfo getInfo(CompilationController controller, TypeElement type) {
            EmbeddedPkSupportInfo info = typeToInfo.get(type);
            if (info == null) {
                info = new EmbeddedPkSupportInfo(controller, type);
                typeToInfo.put(type, info);
            }
            return info;
        }
    }
    
    private static class EmbeddedPkSupportInfo {
        private TypeElement type;
        private Map<String,ExecutableElement> joinColumnNameToRelationshipMethod = new HashMap<String,ExecutableElement>();
        private Map<ExecutableElement,List<String>> relationshipMethodToJoinColumnNames = new HashMap<ExecutableElement,List<String>>(); //used only in isRedundantWithPkFields
        private Map<String,String> joinColumnNameToReferencedColumnName = new HashMap<String,String>();
        private Map<String,String> columnNameToAccessorString = new HashMap<String,String>();
        private Map<ExecutableElement,String> pkAccessorMethodToColumnName = new HashMap<ExecutableElement,String>();
        private Map<ExecutableElement,String> pkAccessorMethodToPopulationCode = new HashMap<ExecutableElement,String>(); //derived
        private boolean isFieldAccess;
        
        public Set<ExecutableElement> getPkAccessorMethods() {
            return pkAccessorMethodToColumnName.keySet();
        }
        
        public ExecutableElement getRelationshipMethod(ExecutableElement pkAccessorMethod) {
            String columnName = pkAccessorMethodToColumnName.get(pkAccessorMethod);
            if (columnName == null) {
                return null;
            }
            return joinColumnNameToRelationshipMethod.get(columnName);
        }
        
        public String getReferencedColumnName(ExecutableElement pkAccessorMethod) {
            String columnName = pkAccessorMethodToColumnName.get(pkAccessorMethod);
            if (columnName == null) {
                return null;
            }
            return joinColumnNameToReferencedColumnName.get(columnName);
        }
        
        public String getAccessorString(String columnName) {
            return columnNameToAccessorString.get(columnName);
        }
        
        public String getCodeToPopulatePkField(ExecutableElement pkAccessorMethod) {
            return pkAccessorMethodToPopulationCode.get(pkAccessorMethod);
        }
        
        public void putCodeToPopulatePkField(ExecutableElement pkAccessorMethod, String code) {
            pkAccessorMethodToPopulationCode.put(pkAccessorMethod, code);
        }
        
        public boolean isRedundantWithPkFields(ExecutableElement relationshipMethod) {
            List<String> joinColumnNameList = relationshipMethodToJoinColumnNames.get(relationshipMethod);
            if (joinColumnNameList == null) {
                return false;
            }
            Collection<String> pkColumnNames = pkAccessorMethodToColumnName.values();
            for (String columnName : joinColumnNameList) {
                if (!pkColumnNames.contains(columnName)) {
                    return false;
                }
            }
            return true;
        }
        
        EmbeddedPkSupportInfo(CompilationController controller, TypeElement type) {
            this.type = type;
            isFieldAccess = JsfForm.isFieldAccess(type);
            for (ExecutableElement method : JsfForm.getEntityMethods(type)) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    Element f = isFieldAccess ? JsfForm.guessField(controller, method) : method;
                    if (f != null) {
                        int a = -1;
                        AnnotationMirror columnAnnotation = null;
                        String[] columnAnnotationFqns = {"javax.persistence.EmbeddedId", "javax.persistence.JoinColumns", "javax.persistence.JoinColumn", "javax.persistence.Column"}; //NOI18N
                        for (int i = 0; i < columnAnnotationFqns.length; i++) {
                            String columnAnnotationFqn = columnAnnotationFqns[i];
                            AnnotationMirror columnAnnotationMirror = JsfForm.findAnnotation(f, columnAnnotationFqn);
                            if (columnAnnotationMirror != null) {
                                a = i;
                                columnAnnotation = columnAnnotationMirror;
                                break;
                            }
                        }
                        if (a == 0) {
                            //populate pkAccessorMethodToColumnName and columnNameToAccessorString
                            populateMapsForEmbedded(controller, method);
                        } else if ( (a == 1 || a == 2) && 
                                (JsfForm.isAnnotatedWith(f, "javax.persistence.OneToOne") ||
                                JsfForm.isAnnotatedWith(f, "javax.persistence.ManyToOne")) )  {
                            //populate joinColumnNameToRelationshipMethod, relationshipMethodToJoinColumnNames, and joinColumnNameToReferencedColumnName
                            populateJoinColumnNameMaps(method, columnAnnotationFqns[a], columnAnnotation);
                        }
                        else if (a == 3) {
                            //populate columnNameToAccessorString
                            String columnName = JsfForm.findAnnotationValueAsString(columnAnnotation, "name"); //NOI18N
                            if (columnName != null) {
                                columnNameToAccessorString.put(columnName, method.getSimpleName().toString() + "()");
                            }
                        } 
                    }
                }
            }
        }
        
        private void populateMapsForEmbedded(CompilationController controller, ExecutableElement idGetterElement) {
            TypeMirror idType = idGetterElement.getReturnType();
            if (TypeKind.DECLARED != idType.getKind()) {
                return;
            }
            DeclaredType declaredType = (DeclaredType) idType;
            TypeElement idClass = (TypeElement) declaredType.asElement();
            
            for (ExecutableElement pkMethod : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                String pkMethodName = pkMethod.getSimpleName().toString();
                if (pkMethodName.startsWith("get")) {
                    Element pkFieldElement = isFieldAccess ? JsfForm.guessField(controller, pkMethod) : pkMethod;
                    AnnotationMirror columnAnnotation = JsfForm.findAnnotation(pkFieldElement, "javax.persistence.Column"); //NOI18N
                    if (columnAnnotation != null) {
                        String columnName = JsfForm.findAnnotationValueAsString(columnAnnotation, "name"); //NOI18N
                        if (columnName != null) {
                            pkAccessorMethodToColumnName.put(pkMethod, columnName);
                            columnNameToAccessorString.put(columnName, 
                                    idGetterElement.getSimpleName().toString() + "()." + 
                                    pkMethod.getSimpleName() + "()");
                        }
                    }
                }
            }
        }
        
        private void populateJoinColumnNameMaps(ExecutableElement m, String columnAnnotationFqn, AnnotationMirror columnAnnotation) {
            List<AnnotationMirror> joinColumnAnnotations;
            if ("javax.persistence.JoinColumn".equals(columnAnnotationFqn)) {
                joinColumnAnnotations = new ArrayList<AnnotationMirror>();
                joinColumnAnnotations.add(columnAnnotation);
            }
            else {  //columnAnnotation is a javax.persistence.JoinColumns
                joinColumnAnnotations = JsfForm.findNestedAnnotations(columnAnnotation, "javax.persistence.JoinColumn"); //NOI18N
            }
            for (AnnotationMirror joinColumnAnnotation : joinColumnAnnotations) {
                String columnName = JsfForm.findAnnotationValueAsString(joinColumnAnnotation, "name"); //NOI18N
                if (columnName != null) {
                    String referencedColumnName = JsfForm.findAnnotationValueAsString(joinColumnAnnotation, "referencedColumnName"); //NOI18N
                    joinColumnNameToRelationshipMethod.put(columnName, m);
                    joinColumnNameToReferencedColumnName.put(columnName, referencedColumnName);
                    List<String> joinColumnNameList = relationshipMethodToJoinColumnNames.get(m);
                    if (joinColumnNameList == null) {
                        joinColumnNameList = new ArrayList<String>();
                        relationshipMethodToJoinColumnNames.put(m, joinColumnNameList);
                    }
                    joinColumnNameList.add(columnName);
                }
            }
        }
    }
}
