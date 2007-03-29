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

package org.netbeans.modules.j2ee.deployment.common.api;

import java.io.File;
import javax.enterprise.deploy.model.DDBean;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Extra file mapping service for each j2ee module.  This service cover irregular source to
 * distribution file mapping.  Users of the mapping could update the mapping.  Provider of
 * the mapping has to ensure the mapping persistence.
 *
 * Note: the initial design is for non-static configuration file such as schema file used in
 * CMP mapping, but it could be used to expose any kind of source-to-distribution mapping.
 *
 * @author  nn136682
 */
public abstract class SourceFileMap {
    
    /**
     * Returns the concrete file for the given distribution path.
     * @param distributionPath distribution path for to find source file for.
     */
    public abstract FileObject[] findSourceFile(String distributionPath);

    /**
     * Returns the relative path in distribution of the given concrete source file.
     * @param distributionPath distribution path for to find source file for.
     */
    public abstract File getDistributionPath(FileObject sourceFile);
    
    /**
     * Return source roots this file mapping is operate on.
     */
    public abstract FileObject[] getSourceRoots();

    /**
     * Return context name, typically the J2EE module project name.
     */
    public abstract String getContextName();
    
    /**
     * Returns directory paths to repository of enterprise resource definition files.
     * If the directories pointed to by the returned path does not exists writing user
     * of the method call could attempt to create it.
     */
    public abstract File getEnterpriseResourceDir();
    
    /**
     * Returns directory paths to repository of enterprise resource definition files.
     * If the directories pointed to by the returned path does not exists writing user
     * of the method call could attempt to create it.
     * For a stand-alone J2EE module, the returned list should contain only one path as
     * returned by getEnterpriseResourceDir.
     *
     * For J2EE application module, the list contains resource directory paths of all child modules.
     */
    public abstract File[] getEnterpriseResourceDirs();
    
    /**
     * Add new mapping or update existing mapping of the given distribution path.
     * Provider of the mapping needs to extract and persist the relative path to
     * ensure the mapping is in project sharable data.  The mapping would be
     * used in ensuring that during build time the source file is put at the 
     * right relative path in the distribution content.
     *
     * @param distributionPath file path in the distribution content
     * @param sourceFile souce concrete file object.
     * @return true if added successfully; false if source file is out of this mapping scope.
     */
    public abstract boolean add(String distributionPath, FileObject sourceFile);

    /**
     * Remove mapping for the given distribution path.
     * @param distributionPath file path in the distribution content
     */
    public abstract FileObject remove(String distributionPath);

    /**
     * Returns a source file map for the module, or null if none can be identified.
     *
     * @param source A non-null source file (java, descriptor or dbschema) to establish mapping context.
     */
    public static final SourceFileMap findSourceMap(FileObject source) {
        Project owner = FileOwnerQuery.getOwner(source);
        if (owner != null) {
            Lookup l = owner.getLookup();
            J2eeModuleProvider projectModule = (J2eeModuleProvider) l.lookup(J2eeModuleProvider.class);
            if (projectModule != null) {
                return projectModule.getSourceFileMap();
            }
        }
        return null;
    }

    /**
     * Returns a source file map for the module, or null if none can be identified.
     *
     * @param ddbean An instance of ddbean to establish mapping context.
     */
    public static final SourceFileMap findSourceMap(J2eeModule j2eeModule) {
        if (j2eeModule == null) {
            throw new NullPointerException();
        }
        return J2eeModuleAccessor.DEFAULT.getJ2eeModuleProvider(j2eeModule).getSourceFileMap();
    }
}
