/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.api.ejbjar;

import java.util.Collections;
import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjar.EjbJarAccessor;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.j2ee.spi.ejbjar.*;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * EjbJar should be used to access properties of an ejb jar module.
 * <p>
 * A client may obtain an EjbJar instance using {@link EjbJar#getEjbJar} method
 * for any FileObject in the ejb jar module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for ejb jar module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class EjbJar {
    
    private EjbJarImplementation impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(EjbJarProvider.class));
    
    static  {
        EjbJarAccessor.DEFAULT = new EjbJarAccessor() {
            public EjbJar createEjbJar(EjbJarImplementation spiEjbJar) {
                return new EjbJar(spiEjbJar);
            }

            public EjbJarImplementation getEjbJarImplementation(EjbJar wm) {
                return wm == null ? null : wm.impl;
            }
        };
    }
    
    private EjbJar (EjbJarImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the EjbJar for given file or null if the file does not belong
     * to any web module.
     */
    public static EjbJar getEjbJar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to EjbJar.getEjbJar(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            EjbJarProvider impl = (EjbJarProvider)it.next();
            EjbJar wm = impl.findEjbJar (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Find EjbJar(s) for all ejb modules within a given project.
     * @return an array of EjbJar instance (empty array if no instance are found).
     */
    public static EjbJar [] getEjbJars (Project project) {
        EjbJarsInProject providers = (EjbJarsInProject) project.getLookup().lookup(EjbJarsInProject.class);
        if (providers != null) {
            EjbJar jars [] = providers.getEjbJars();
            if (jars != null) {
                return jars;
            }
        }
        return new EjbJar[] {};
    }
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion();
    }
    
    /**
     * Deployment descriptor (ejb-jar.xml file) of an ejb module.
     *
     * @return descriptor FileObject or <code>null</code> if not available.
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor();
    }

    /** Source roots associated with the EJB module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the EJB module.
     * </div>
     */
    public FileObject[] getJavaSources() {
        return impl.getJavaSources();
    }
    
    /** Meta-inf
     */
    public FileObject getMetaInf() {
        return impl.getMetaInf();
    }

    /**
     * Coupling of deployment desrciptor and classpath containing annotated classes
     * describing metadata of the EJB module
     * 
     * @return non-null value
     */
    public MetadataUnit getMetadataUnit() {
        return impl.getMetadataUnit();
    }
    
}
