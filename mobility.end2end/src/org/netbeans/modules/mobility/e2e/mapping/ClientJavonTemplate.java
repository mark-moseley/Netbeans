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

package org.netbeans.modules.mobility.e2e.mapping;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.netbeans.modules.mobility.javon.OutputFileFormatter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ClientJavonTemplate extends JavonTemplate {
        
    private static final String CLIENT_OUTPUT = "client-template";
    
    /**
     * 
     * @param mapping 
     */
    public ClientJavonTemplate( JavonMapping mapping ) {
        super( mapping );
    }
    
    public Set<String> getTargets() {
        Set<JavonMapping.Service> services = mapping.getServiceMappings();
        Set<String> targets = new HashSet<String>();
        for( JavonMapping.Service service : services ) {
            targets.add( service.getType());
        }
        return Collections.unmodifiableSet( targets );
    }

    public boolean generateTarget( ProgressHandle ph, String target ) {
        if( mapping.getServiceMapping( target ) != null ) {
            ph.progress( NbBundle.getMessage( ClientJavonTemplate.class, "MSG_Client" ));   // NOI18N
            try {
                mapping.setProperty( "target", "client" );
                
                JavonMapping.Service service = mapping.getServiceMapping( target );
                FileObject outputDir = FileUtil.toFileObject( FileUtil.normalizeFile( 
                        new File( mapping.getClientMapping().getOutputDirectory())));
                outputDir = outputDir.getFileObject( mapping.getClientMapping().getPackageName().replace( '.', '/' ));

                FileObject outputFile = outputDir.getFileObject( mapping.getClientMapping().getClassName(), "java" );
                if( outputFile == null ) {
                    outputFile = outputDir.createData( mapping.getClientMapping().getClassName(), "java" );
                }
                OutputFileFormatter off = new OutputFileFormatter( outputFile );

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine eng = mgr.getEngineByName( "freemarker" );
                Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

                FileObject template = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject( "Templates/Client/Client.java" );        
                
                Set<ClassData> returnTypes = service.getReturnTypes();
                Set<ClassData> parameterTypes = service.getParameterTypes();
                bind.put( "mapping", mapping );
                bind.put( "registry", mapping.getRegistry());
                bind.put( "returnTypes", returnTypes );
                bind.put( "parameterTypes", parameterTypes );
                bind.put( "service", service );
                bind.put( "utils", new Utils( mapping.getRegistry()));

                // Compute imports for JavaBeans
                Set<String> imports = new HashSet<String>();
                for( ClassData cd : parameterTypes ) {
                    while( cd.isArray()) {
                        cd = cd.getComponentType();
                    }
                    if( cd.isPrimitive()) continue;
                    if( cd.getPackage().equals( "java.lang" )) continue;
                    if( cd.getFullyQualifiedName().equals( "java.util.List" )) continue;
                    imports.add( cd.getFullyQualifiedName());
                }
                for( ClassData cd : returnTypes ) {
                    while( cd.isArray()) {
                        cd = cd.getComponentType();
                    }
                    if( cd.isPrimitive()) continue;
                    if( cd.getPackage().equals( "java.lang" )) continue;
                    if( cd.getFullyQualifiedName().equals( "java.util.List" )) continue;
                    imports.add( cd.getFullyQualifiedName());
                }
                bind.put( "imports", imports );
                
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
//                            System.err.println( "" + w.toString());
                        w.close();
                    }
                    if( is != null ) is.close();
                    off.close();
                }                  
            } catch( Exception e ) {                
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
