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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.filesystems.FileObject;
import java.util.Iterator;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.util.Parameters;

/** 
 * Abstraction of J2EE module. Provides access to basic server-neutral properties 
 * of the modules: J2EE version, module type, deployment descriptor.
 * <p>
 * It is not possible to instantiate this class directly. Implementators have to
 * implement the {@link J2eeModuleImplementation} first and then use the
 * {@link org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory}
 * to create a J2eeModule instance.
 * 
 * @author  Pavel Buzek
 */
public class J2eeModule {

    /** MIME type for ContentDescriptor of build targets that have J2eeModule in lookup.
     * This can be used to search implementations of targets providing J2eeModule 
     * in project's ContainersList.
     */
    public static final String MIME_J2EE_MODULE_TARGET = "MIME-org-nb-j2eeserver-J2eeModule-BuildTarget"; //NOI18N
    
    /**
     * The module is an EAR archive.
     * @deprecated use {@link Type#EAR}
     */
    public static final Object EAR = ModuleType.EAR;
    
    /**
     * The module is an Web Application archive.
     * @deprecated use {@link Type#WAR}
     */
    public static final Object WAR = ModuleType.WAR;

    /**
     * The module is an Enterprise Java Bean archive.
     * @deprecated use {@link Type#EJB}
     */
    public static final Object EJB = ModuleType.EJB;

    /**
     * The module is an Connector archive.
     * @deprecated use {@link Type#RAR}
     */
    public static final Object CONN = ModuleType.RAR;

    /**
     * The module is an Client Application archive.
     * @deprecated use {@link Type#CAR}
     */
    public static final Object CLIENT = ModuleType.CAR;
    
    /**
     * J2EE specification version 1.3
     * @since 1.5
     * @deprecated use {@link Profile#J2EE_13}
     */
    public static final String J2EE_13 = "1.3"; //NOI18N
    /**
     * J2EE specification version 1.4
     * @since 1.5
     * @deprecated use {@link Profile#J2EE_14}
     */
    public static final String J2EE_14 = "1.4"; //NOI18N
    /**
     *
     * JAVA EE 5 specification version
     *
     * @since 1.6
     * @deprecated use {@link Profile#JAVA_EE_5}
     */
    public static final String JAVA_EE_5 = "1.5"; // NOI18N
    
    public static final String APP_XML = "META-INF/application.xml";
    public static final String WEB_XML = "WEB-INF/web.xml";
    public static final String WEBSERVICES_XML = "WEB-INF/webservices.xml";
    public static final String EJBJAR_XML = "META-INF/ejb-jar.xml";
    public static final String EJBSERVICES_XML = "META-INF/webservices.xml";
    public static final String CONNECTOR_XML = "META-INF/ra.xml";
    public static final String CLIENT_XML = "META-INF/application-client.xml";
    
    
    /**
     * Enterprise resorce directory property
     */
    public static final String PROP_RESOURCE_DIRECTORY = "resourceDir"; // NOI18N
    
    /**
     * Module version property
     */
    public static final String PROP_MODULE_VERSION = "moduleVersion"; // NOI18N

    static {
        J2eeModuleAccessor.setDefault(new J2eeModuleAccessor() {
            public J2eeModule createJ2eeModule(J2eeModuleImplementation impl) {
                return new J2eeModule(impl);
            }

            @Override
            public J2eeModule createJ2eeModule(J2eeModuleImplementation2 impl) {
                return new J2eeModule(impl);
            }

            public J2eeModuleProvider getJ2eeModuleProvider(J2eeModule j2eeModule) {
               return j2eeModule.getJ2eeModuleProvider();
            }

            public void setJ2eeModuleProvider(J2eeModule j2eeModule, J2eeModuleProvider j2eeModuleProvider) {
                j2eeModule.setJ2eeModuleProvider(j2eeModuleProvider);
            }

            @Override
            public ModuleType getJsrModuleType(Type type) {
                return type.getJsrType();
            }

        });
    }
    
    private J2eeModuleProvider j2eeModuleProvider;
    
    private final J2eeModuleBase impl;
    
    J2eeModule(J2eeModuleBase impl) {
        this.impl = impl;
    }

    /** 
     * Returns a Java EE module specification version, version of a web application 
     * for example.
     * <p>
     * Do not confuse with the Java EE platform specification version.
     *
     * @return module specification version.
     */
    public String getModuleVersion() {
        return impl.getModuleVersion();
    }
    
    /** 
     * Returns module type.
     * 
     * @return module type.
     * @deprecated use {@link #getType()}
     */
    @NonNull
    public Object getModuleType() {
        if (impl instanceof J2eeModuleImplementation2) {
            return ((J2eeModuleImplementation2) impl).getModuleType().getJsrType();
        } else {
            return ((J2eeModuleImplementation) impl).getModuleType();
        }
    }

    /**
     * Returns module type.
     *
     * @return module type
     * @since 1.59
     */
    @NonNull
    public Type getType() {
        if (impl instanceof J2eeModuleImplementation2) {
            return ((J2eeModuleImplementation2) impl).getModuleType();
        } else {
            Type type = Type.fromJsrType(((J2eeModuleImplementation) impl).getModuleType());
            assert type != null;
            return type;
        }
    }
    
    /** 
     * Returns the location of the module within the application archive.
     * 
     * @return location of the module within the application archive.
     */
    public String getUrl() {
        return impl.getUrl();
    }
    
    /** Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet). 
     */
    public FileObject getArchive() throws java.io.IOException {
        return impl.getArchive();
    }
    
    /** Returns the contents of the archive, in copyable form.
     *  Used for incremental deployment.
     *  Currently uses its own {@link RootedEntry} interface.
     *  If the J2eeModule instance describes a
     *  j2ee application, the result should not contain module archives.
     *  @return Iterator through {@link RootedEntry}s
     */
    public Iterator getArchiveContents() throws java.io.IOException {
        return impl.getArchiveContents();
    }

    /** This call is used in in-place deployment. 
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     *  @return FileObject for the content directory, return null if the 
     *     module doesn't have a build directory, like an binary archive project
     */
    public FileObject getContentDirectory() throws java.io.IOException {
        return impl.getContentDirectory();
    }
    
    /**
     * Returns a metadata model of a deployment descriptor specified by the 
     * <code>type</code> parameter.
     * 
     * <p>
     * As an example, passing <code>org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata.class</code>
     * as a type parameter will return a metadata model of the web module deployment 
     * descriptor - web.xml.
     * </p>
     * 
     * @param type metadata model type class for which a <code>MetadataModel</code>
     *        instance will be returned.
     * 
     * @return metadata model of a deployment descriptor specified by the <code>type</code>
     *         parameter.
     * 
     * @throws NullPointerException if the <code>type</code> parameter is <code>null</code>.
     */
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        Parameters.notNull("type", type); // NOI18N
        return impl.getMetadataModel(type);
    }
    
    /**
     * Returns the module resource directory or null if the module has no resource
     * directory.
     * 
     * @return the module resource directory or null if the module has no resource
     *         directory.
     */
    public File getResourceDirectory() {
        return impl.getResourceDirectory();
    }
    
    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml
     *        for example.
     * 
     * @return absolute path to the deployment configuration file, or null if the
     *         specified file name is not known to this J2eeModule.
     */
    public File getDeploymentConfigurationFile(String name) {
        return impl.getDeploymentConfigurationFile(name);
    }
    
    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);   
    }
    
    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }
    
    synchronized J2eeModuleProvider getJ2eeModuleProvider() {
        return j2eeModuleProvider;
    }
    
    synchronized void setJ2eeModuleProvider(J2eeModuleProvider j2eeModuleProvider) {
        this.j2eeModuleProvider = j2eeModuleProvider;
    }

    public interface RootedEntry {

        FileObject getFileObject ();

        String getRelativePath ();

    }

    public static final class Type {

        public static final Type WAR = new Type(ModuleType.WAR);

        public static final Type EJB = new Type(ModuleType.EJB);

        public static final Type EAR = new Type(ModuleType.EAR);

        public static final Type CAR = new Type(ModuleType.CAR);

        public static final Type RAR = new Type(ModuleType.RAR);

        private final ModuleType jsrType;

        private Type(ModuleType jsrType) {
            this.jsrType = jsrType;
        }

        private ModuleType getJsrType() {
            return jsrType;
        }

        public static Type fromJsrType(Object type) {
            // be defensive
            if (type instanceof Type) {
                return (Type) type;
            }
            assert type instanceof ModuleType;

            if (J2eeModule.WAR.equals(type)) {
                return WAR;
            } else if (J2eeModule.EJB.equals(type)) {
                return EJB;
            } else if (J2eeModule.EAR.equals(type)) {
                return EAR;
            } else if (J2eeModule.CLIENT.equals(type)) {
                return CAR;
            } else if (J2eeModule.CONN.equals(type)) {
                return RAR;
            }
            return null;
        }
    }

}
