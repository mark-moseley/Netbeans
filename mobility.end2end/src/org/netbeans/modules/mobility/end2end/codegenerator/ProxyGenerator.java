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

/*
 * ProxyGenerator.java
 *
 * Created on August 12, 2005, 11:35 AM
 *
 */
package org.netbeans.modules.mobility.end2end.codegenerator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.PortData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.OutputFileFormatter;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 *
 * @author suchys
 */
public class ProxyGenerator {
    final private E2EDataObject dataObject;
    private DataObject createdProxy;
    
    /** Creates a new instance of ProxyGenerator */
    public ProxyGenerator(E2EDataObject dataObject) {
        this.dataObject = dataObject;
    }
    
    public String generate() {
        String generatedProxyName = null;
        try {
            final Configuration configuration = dataObject.getConfiguration();
            final ServerConfiguration sc = configuration.getServerConfigutation();
            final WSDLService wsdlService = (WSDLService)configuration.getServices().get( 0 );
            
            final Sources s = ProjectUtils.getSources(dataObject.getServerProject());
            final SourceGroup sourceGroup = Util.getPreselectedGroup(
                    s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA ),
                    sc.getClassDescriptor().getLocation());
            final FileObject srcDirectory = sourceGroup.getRootFolder();
            final ClassPath cp = ClassPath.getClassPath( srcDirectory, ClassPath.SOURCE );
            final FileObject fo = cp.getRoots()[0]; //TODO fix me - find src or test folder @see sc.getProjectPath();
            String targetFolderName = ( sc.getClassDescriptor().getPackageName()).replace( '.', '/' );
            String pkgName = sc.getClassDescriptor().getPackageName();
            
            FileObject targetFolder = fo.getFileObject( targetFolderName );
            if( targetFolder == null ){
                targetFolder = FileUtil.createFolder( fo, targetFolderName );
            }
            
            final PortData pd = (PortData)wsdlService.getData().get( 0 );
            String proxyClassName = wsdlService.getType();
            proxyClassName = proxyClassName.substring(proxyClassName.lastIndexOf('.') + 1); // NOI18N
            proxyClassName = proxyClassName + "_Proxy"; // NOI18N
            
            generatedProxyName = pkgName.length() > 0 ? pkgName + "." + proxyClassName : proxyClassName;
                
            FileObject outputFile = targetFolder.getFileObject( proxyClassName, "java" ); // NOI18N
            if( outputFile == null ) {
                outputFile = targetFolder.createData( proxyClassName, "java" ); // NOI18N
            }
            
            // Get Nodes from the J2EE
            final Project serverProject = Util.getServerProject( configuration );
            Node rootNode = JAXWSClientView.getJAXWSClientView().createJAXWSClientView( serverProject );
            
            for( Node nn : rootNode.getChildren().getNodes()) {
                if( nn.getDisplayName().equals( wsdlService.getName()))
                    rootNode = nn;
            }
                        
            FileObject generatedClientFO = 
                    serverProject.getProjectDirectory().getFileObject( "build/generated/wsimport/client/" ); // NOI18N
            // Add all paths to the ClasspathInfo structure
            List<ClasspathInfo> classpaths = Collections.singletonList( ClasspathInfo.create( generatedClientFO ));
            // Get the registry for all available classes
            ClassDataRegistry registry = ClassDataRegistry.getRegistry( ClassDataRegistry.DEFAULT_PROFILE, classpaths );
            
            String servicePackage = null;
            String serviceClassName = null;
            String portClassName = null;
            String portGetterName = null;
            List<MethodData> methodList = new ArrayList<MethodData>();
            for( Node serviceNode : rootNode.getChildren().getNodes()) {       
                WsdlService service = serviceNode.getLookup().lookup(WsdlService.class);
                if (service == null) break;
                for( Node portNode : serviceNode.getChildren().getNodes()) {
                    WsdlPort wsdlPort = portNode.getLookup().lookup( WsdlPort.class );
                    if( wsdlPort == null ) break;
                    serviceClassName = service.getJavaName();
                    portGetterName = wsdlPort.getPortGetter();
                    portClassName = wsdlPort.getJavaName();
                    if( portClassName.equals( pd.getType())) {
                        org.netbeans.modules.mobility.e2e.classdata.ClassData cd = registry.getClassData( wsdlPort.getJavaName());
                        servicePackage = cd.getPackage();
                        for( Node operationNode : portNode.getChildren().getNodes()) {
                            WsdlOperation wsdlOperation = operationNode.getLookup().lookup( WsdlOperation.class );
                            org.netbeans.modules.mobility.e2e.classdata.MethodData methodData = null;
                            for( org.netbeans.modules.mobility.e2e.classdata.MethodData md : cd.getMethods()) {
                                if( md.getName().equals( wsdlOperation.getJavaName())) {
                                    for( OperationData op : pd.getOperations()) {
                                        if( md.getName().equals( op.getMethodName())) {
                                            methodList.add( md );
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
            
            OutputFileFormatter off = new OutputFileFormatter( outputFile );

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine eng = mgr.getEngineByName( "freemarker" ); // NOI18N
            Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

            FileObject template = Repository.getDefault().getDefaultFileSystem().
                    getRoot().getFileObject( "Templates/Server/Proxy.java" ); // NOI18N
            
            // Set code generation for server part
            JavonMapping mapping = dataObject.getMapping();
            mapping.setProperty( "target", "server" );  // NOI18N
            
            bind.put( "mapping", mapping ); // NOI18N
            bind.put( "proxyClassPackage", sc.getClassDescriptor().getPackageName()); // NOI18N
            bind.put( "proxyClassName", proxyClassName ); // NOI18N
            bind.put( "servicePackage", servicePackage ); // NOI18N
            bind.put( "service", serviceClassName ); // NOI18N
            
            bind.put( "methods", methodList ); // NOI18N
            bind.put( "portGetterName", portGetterName ); // NOI18N
            bind.put( "portClassName", portClassName ); // NOI18N
            
            Writer w = null;
            Reader is = null;
            try {
                w = new StringWriter();
                is = new InputStreamReader( template.getInputStream());

                eng.getContext().setWriter( w );
                eng.getContext().setAttribute( FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE );
                eng.getContext().setAttribute( ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE );

                eng.eval( is );
            } catch( Exception e ) {
                e.printStackTrace();
            } finally {
                if( w != null ) {
                    off.write( w.toString());
                    w.close();
                }
                if( is != null ) is.close();
                off.close();
            }                  
        } catch( Exception e ) {                
            ErrorManager.getDefault().notify( e );
            return null;
        }
        return generatedProxyName;
        
    }    
}
