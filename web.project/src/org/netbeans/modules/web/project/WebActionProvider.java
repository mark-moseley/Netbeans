/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.awt.Dialog;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.execution.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.web.api.webmodule.URLCookie;
import org.netbeans.modules.web.project.ui.NoSelectedServerWarning;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.project.ui.ServletUriPanel;
import org.netbeans.modules.web.project.ui.SetExecutionUriAction;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.*;
import org.netbeans.api.project.ProjectInformation;

import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.api.web.dd.Servlet;
import org.netbeans.api.web.dd.ServletMapping;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.web.api.webmodule.WebModule;

import org.netbeans.modules.web.api.webmodule.WebProjectConstants;



/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class WebActionProvider implements ActionProvider {
    
    // Definition of commands
    
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
        
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_COMPILE_SINGLE, 
        COMMAND_RUN, 
        COMMAND_RUN_SINGLE, 
        COMMAND_DEBUG, 
        WebProjectConstants.COMMAND_REDEPLOY,
        COMMAND_DEBUG_SINGLE, 
        JavaProjectConstants.COMMAND_JAVADOC, 
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_COMPILE,
    };
    
    // Project
    WebProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public WebActionProvider(WebProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        
        commands = new HashMap();
            commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
            commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
            commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
            commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
            commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_RUN_SINGLE, new String[] {"run"}); // NOI18N
            commands.put(WebProjectConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
            commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        
        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName ());
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
        Properties p;
        String[] targetNames = (String[])commands.get(command);
        //EXECUTION PART
        if (command.equals (COMMAND_RUN) || command.equals (COMMAND_RUN_SINGLE) || command.equals (WebProjectConstants.COMMAND_REDEPLOY)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                String text;
                if (command.equals (COMMAND_RUN)) {
                    ProjectInformation pi = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
                    text = pi.getDisplayName();
                } else { //COMMAND_RUN_SINGLE
                    FileObject[] files = findJsps(context);
                    text = files[0].getNameExt();
                }
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(WebActionProvider.class, "MSG_SessionRunning", text),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }
            p = new Properties();
            if (command.equals (WebProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
            if (command.equals (COMMAND_RUN_SINGLE)) {
                FileObject[] files = findJsps( context );
                if (files!=null && files.length>0) {
                    try {
                        URLCookie uc = (URLCookie) DataObject.find (files [0]).getCookie (URLCookie.class);
                        if (uc != null) {
                            p.setProperty("client.urlPart", uc.getURL ()); //NOI18N
                        } else {
                            return;
                        }
                    } catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault ().notify (e);
                        return;
                    }
                } else {
                    FileObject[] javaFiles = findJavaSources(context);
                    FileObject servlet = javaFiles[0];
                    String executionUri = (String)servlet.getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI);
                    if (executionUri!=null) {
                        p.setProperty("client.urlPart", executionUri); //NOI18N
                    } else {
                        WebModule webModule = WebModule.getWebModule(servlet);
                        String[] urlPatterns = SetExecutionUriAction.getServletMappings(webModule,servlet);
                        if (urlPatterns!=null && urlPatterns.length>0) {
                            ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns,null,true);
                            DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                NbBundle.getMessage (SetExecutionUriAction.class, "TTL_setServletExecutionUri"));
                            Object res = DialogDisplayer.getDefault().notify(desc);
                            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                                p.setProperty("client.urlPart", uriPanel.getServletUri()); //NOI18N
                                try {
                                    servlet.setAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI,uriPanel.getServletUri());
                                } catch (IOException ex){}
                            } else return;
                        } else {
                            String mes = java.text.MessageFormat.format (
                                    NbBundle.getMessage (SetExecutionUriAction.class, "TXT_missingServletMappings"),
                                    new Object [] {servlet.getName()});
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                            return;
                        }
                    }
                }

            }
        //DEBUGGING PART
        } else if (command.equals (COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(WebActionProvider.class, "MSG_FinishSession"),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }
            J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            ServerDebugInfo sdi = jmp.getServerDebugInfo ();
            String h = sdi.getHost();
            String transport = sdi.getTransport();
            String address = "";                                                //NOI18N
            
            if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                address = sdi.getShmemName();
            } else {
                address = Integer.toString(sdi.getPort());
            }
            
            p = new Properties();
            p.setProperty("jpda.transport", transport);
            p.setProperty("jpda.host", h);
            p.setProperty("jpda.address", address);
        
            if (command.equals (COMMAND_DEBUG)) {
                p.setProperty("client.urlPart", project.getWebModule().getUrl());
            } else { //COMMAND_DEBUG_SINGLE
                FileObject[] files = findJsps( context );
                // debug jsp
                if ((files != null) && (files.length>0)) {
                    try {
                        URLCookie uc = (URLCookie) DataObject.find (files [0]).getCookie (URLCookie.class);
                        if (uc != null) {
                            p.setProperty("client.urlPart", uc.getURL ());
                        } else {
                            return;
                        }
                    } catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault ().notify (e);
                        return;
                    }
                // debug servlet
                } else {
                    FileObject[] javaFiles = findJavaSources(context);
                    FileObject servlet = javaFiles[0];
                    String executionUri = (String)servlet.getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI);
                    if (executionUri!=null) {
                        p.setProperty("client.urlPart", executionUri); //NOI18N
                    } else {
                        WebModule webModule = WebModule.getWebModule(servlet);
                        String[] urlPatterns = SetExecutionUriAction.getServletMappings(webModule,servlet);
                        if (urlPatterns!=null && urlPatterns.length>0) {
                            ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns,null,true);
                            DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                NbBundle.getMessage (SetExecutionUriAction.class, "TTL_setServletExecutionUri"));
                            Object res = DialogDisplayer.getDefault().notify(desc);
                            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                                p.setProperty("client.urlPart", uriPanel.getServletUri()); //NOI18N
                                try {
                                    servlet.setAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI,uriPanel.getServletUri());
                                } catch (IOException ex){}
                            } else return;
                        } else {
                            String mes = java.text.MessageFormat.format (
                                    NbBundle.getMessage (SetExecutionUriAction.class, "TXT_missingServletMappings"),
                                    new Object [] {servlet.getName()});
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                            return;
                        }
                    }
                }
            }            
            
        //COMPILATION PART
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] files = findJavaSources( context );
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
            } else {
                files = findJsps (context);
                if (files != null) {
                    p.setProperty("jsp.includes", getBuiltJspFileNamesAsPath(files) /*ActionUtils.antIncludesList(files, project.getWebModule ().getDocumentBase ())*/); // NOI18N
                    targetNames = new String [] {"compile-single-jsp"};
                } else {
                    return;
                }
            }
        } else {
            p = null;
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }

        try {
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } 
        catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public File getBuiltJsp(FileObject jsp) {
        ProjectWebModule pwm = project.getWebModule ();
        FileObject webDir = pwm.getDocumentBase ();
        String relFile = FileUtil.getRelativePath(webDir, jsp).replace('/', File.separatorChar);
        File webBuildDir = pwm.getContentDirectoryAsFile();
        return new File(webBuildDir, relFile);
    }
    
    public String getBuiltJspFileNamesAsPath(FileObject[] files) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            String path = getBuiltJsp(files[i]).getAbsolutePath();
            if (i > 0) {
                b.append(File.pathSeparator);
            }
            b.append(path);
        }
        return b.toString();
    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findJavaSources( context ) != null || findJsps (context) != null;
        }
        if ( command.equals( COMMAND_RUN_SINGLE ) ) {
            // test for jsps
            FileObject jsps [] = findJsps (context);
            if (jsps != null && jsps.length >0) return true;
            // test for servlets
            FileObject[] javaFiles = findJavaSources(context);
            if (javaFiles!=null && javaFiles.length > 0) {
                if (javaFiles[0].getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI)!=null)
                    return true;
                else if (Boolean.TRUE.equals(javaFiles[0].getAttribute("org.netbeans.modules.web.IsServletFile"))) //NOI18N
                    return true;
                else if (isDDServlet(context, javaFiles[0])) {
                    try {
                        javaFiles[0].setAttribute("org.netbeans.modules.web.IsServletFile",Boolean.TRUE); //NOI18N
                    } catch (IOException ex){}
                    return true;
                } else return false;
            } else return false;
        }
        else {
            // other actions are global
            return true;
        }

        
    }
    
    // Private methods -----------------------------------------------------
    
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    
    /** Find selected java sources 
     */
    private FileObject[] findJavaSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory ();
        FileObject[] files = null;
        if (srcDir != null) {
            files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true);
        }
        return files;
    }
    
    /** Find selected jsps
     */
    private FileObject[] findJsps(Lookup context) {
        FileObject webDir = project.getWebModule ().getDocumentBase ();
        FileObject[] files = null;
        if (webDir != null) {
            files = ActionUtils.findSelectedFiles(context, webDir, ".jsp", true);
        }
        return files;
    }
    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        ServerDebugInfo sdi = jmp.getServerDebugInfo ();
//        server.getServerInstance().getStartServer().getDebugInfo(null);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isSelectedServer () {
        String instance = antProjectHelper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected;
        if (instance != null) {
            selected = true;
        } else {
            // no selected server => warning
            String server = antProjectHelper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_TYPE);
            NoSelectedServerWarning panel = new NoSelectedServerWarning (server);

            Object[] options = new Object[] {
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor (panel,
                    NbBundle.getMessage (NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance ();
                selected = instance != null;
                if (selected) {
                    WebProjectProperties wpp = new WebProjectProperties (project, antProjectHelper, refHelper);
                    wpp.put (WebProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    wpp.store ();
                }
            }
            dlg.dispose();            
        }
        return selected;
    }
    
    private boolean isDDServlet(Lookup context, FileObject javaClass) {
        FileObject webDir = project.getWebModule ().getDocumentBase ();
        if (webDir==null) return false;
        FileObject fo = webDir.getFileObject("WEB-INF/web.xml"); //NOI18N
        ClassPath classPath = ClassPath.getClassPath(project.getSourceDirectory (),ClassPath.SOURCE);
        String className = classPath.getResourceName(javaClass,'.',false);
        if (fo==null) return false;
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
            Servlet servlet = (Servlet)webApp.findBeanByName("Servlet","ServletClass",className); //NOI18N
            if (servlet!=null) return true;
            else return false;
        } catch (IOException ex) {return false;}  
    }   
}
