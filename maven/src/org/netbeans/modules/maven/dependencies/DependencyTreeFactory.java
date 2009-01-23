/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.dependencies;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class DependencyTreeFactory {

    public static DependencyNode createDependencyTree(MavenProject project, MavenEmbedder embedder, String scope) {
        try {
            PlexusContainer container = embedder.getPlexusContainer();
            DependencyTreeBuilder builder = (DependencyTreeBuilder) container.lookup(DependencyTreeBuilder.ROLE);
            ArtifactFactory factory = (ArtifactFactory)container.lookup(ArtifactFactory.ROLE);
            ArtifactMetadataSource source = (ArtifactMetadataSource)container.lookup(ArtifactMetadataSource.ROLE);
            ArtifactCollector collector = (ArtifactCollector)container.lookup(ArtifactCollector.ROLE);
            return createDependencyTree(project, builder, embedder.getLocalRepository(), factory, source, collector, scope);
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }


    //copied from dependency:tree mojo
    private static DependencyNode createDependencyTree(MavenProject project,
            DependencyTreeBuilder dependencyTreeBuilder, ArtifactRepository localRepository,
            ArtifactFactory artifactFactory, ArtifactMetadataSource artifactMetadataSource,
            ArtifactCollector artifactCollector,
            String scope) {
        ArtifactFilter artifactFilter = createResolvingArtifactFilter(scope);

        try {
            // TODO: note that filter does not get applied due to MNG-3236
            return dependencyTreeBuilder.buildDependencyTree(project,
                    localRepository, artifactFactory,
                    artifactMetadataSource, artifactFilter, artifactCollector);
        } catch (DependencyTreeBuilderException exception) {
        }
        return null;
    }

    //copied from dependency:tree mojo
    /**
     * Gets the artifact filter to use when resolving the dependency tree.
     *
     * @return the artifact filter
     */
    private static ArtifactFilter createResolvingArtifactFilter(String scope) {
        ArtifactFilter filter;

        // filter scope
        if (scope != null) {

            filter = new ScopeArtifactFilter(scope);
        } else {
            filter = null;
        }

        return filter;
    }
}
