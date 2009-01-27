/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.spi.GroovyFeature;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * Temporary solution to have GSF indexing enabled on projects with only
 * Java classpath. It has 2 drawbacks:<br/>
 * - will not work in closed projects<br/>
 * - will not update GFS classpath on project source roots changes
 * 
 * @author Martin Adamek
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-java-j2seproject")
public class GsfClasspathHook extends ProjectOpenedHook {

    private final Project project;
    
    public GsfClasspathHook(Project project) {
        this.project = project;
    }
    
    @Override
    protected void projectOpened() {
        if (isGroovyEnabled()) {
            //GroovyProjectExtender.registerGsfClassPath(project);
        }
    }

    @Override
    protected void projectClosed() {
        if (isGroovyEnabled()) {
            //GroovyProjectExtender.unregisterGsfClassPath(project);
        }
    }

    private boolean isGroovyEnabled() {
        GroovyFeature groovyFeature = project.getLookup().lookup(GroovyFeature.class);
        return groovyFeature == null ? false : groovyFeature.isGroovyEnabled();
    }

}
