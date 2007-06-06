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

package org.netbeans.modules.j2ee.persistence.api;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistenceapi.PersistenceScopeAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Describes a persistence scope. A persistence scope is composed of
 * a persistence.xml file and the classpath for this persistence.xml file (which
 * contains all entity classes and JAR files referenced by the persistence units
 * in the persistence.xml file).
 *
 * @author Andrei Badea
 */
public final class PersistenceScope {

    // XXX remove getClassPath(), not needed anymore

    private static final Lookup.Result<PersistenceScopeProvider> providers =
            Lookup.getDefault().lookupResult(PersistenceScopeProvider.class);

    private final PersistenceScopeImplementation impl;

    static {
        PersistenceScopeAccessor.DEFAULT = new PersistenceScopeAccessor() {
            public PersistenceScope createPersistenceScope(PersistenceScopeImplementation impl) {
                return new PersistenceScope(impl);
            }
        };
    }

    /**
     * Returns the persistence scope for the given file.
     *
     * @param  fo the file for which to find the persistence scope; cannot be null.
     *
     * @return the persistence scope for the given file or null if there is no
     *         persistence scope.
     *
     * @throws NullPointerException if the fo parameter was null.
     */
    public static PersistenceScope getPersistenceScope(FileObject fo) {
        Parameters.notNull("fo", fo); // NOI18N
        for (PersistenceScopeProvider provider : providers.allInstances()) {
            PersistenceScope persistenceScope = provider.findPersistenceScope(fo);
            if (persistenceScope != null) {
                return persistenceScope;
            }
        }
        return null;
    }

    private PersistenceScope(PersistenceScopeImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the persistence.xml file of this persistence scope.
     *
     * @return the persistence.xml file or null if it the persistence.xml file does
     * not exist.
     */
    public FileObject getPersistenceXml() {
        return impl.getPersistenceXml();
    }

    /**
     * Returns a model of entity classes for the specified persistence unit.
     * 
     * @param  persistenceUnitName the persistence unit name; cannot be null.
     * 
     * @return an entity class model or null if the given persistence scope does
     *         not exist.
     */
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
        return impl.getEntityMappingsModel(persistenceUnitName);
    }

    /**
     * Provides the classpath of this persistence scope, which covers the sources
     * of the entity classes referenced by the persistence.xml file, as well
     * as the referenced JAR files.
     *
     * @return the persistence scope classpath; never null.
     */
    public ClassPath getClassPath() {
        return impl.getClassPath();
    }
}
