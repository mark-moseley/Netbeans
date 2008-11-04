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

package org.netbeans.modules.compapp.projects.jbi;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.openide.util.NbBundle;

import java.io.IOException;


/**
 * Factory for EJB Module projects
 *
 * @author Chris Webster
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.support.ant.AntBasedProjectType.class)
public final class JbiProjectType implements AntBasedProjectType {
    /**
     * DOCUMENT ME!
     */
    public static final String TYPE = "org.netbeans.modules.compapp.projects.jbi"; // NOI18N
    private static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-jbi/1"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-jbi-private/1"; // NOI18N

    /**
     * Creates a new JbiProjectType object.
     */
    public JbiProjectType() {
        int i = 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getType() {
        return TYPE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param helper DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public Project createProject(AntProjectHelper helper)
        throws IOException {
        if (null == helper) {
            throw new IllegalArgumentException(NbBundle.getMessage(JbiProjectType.class, "MSG_helper")); // NOI18N
        }

        return new JbiProject(helper, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param shared DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }

    /**
     * DOCUMENT ME!
     *
     * @param shared DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }
}
