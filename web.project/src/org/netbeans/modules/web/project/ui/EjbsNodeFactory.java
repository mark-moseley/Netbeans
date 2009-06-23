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

package org.netbeans.modules.web.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.Capabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Profile;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 * NodeFactory to create EJB nodes.
 *
 * @author kaktus
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project", position=100)
public class EjbsNodeFactory implements NodeFactory {

    public EjbsNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new EjbNodeList(project);
    }

    private static class EjbNodeList implements NodeList<String>, PropertyChangeListener{
        private static final String KEY_EJBS = "ejbKey"; //NOI18N
        private final WebProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        EjbNodeList(WebProject proj) {
            this.project = proj;
        }

        public List<String> keys() {
            Profile profile = Profile.fromPropertiesString(project.evaluator().getProperty(WebProjectProperties.J2EE_PLATFORM));
            if (profile == Profile.JAVA_EE_6_FULL){
                return Collections.singletonList(KEY_EJBS);
            }else{
                return Collections.EMPTY_LIST;
            }
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            if (KEY_EJBS.equals(key)) {
                EjbJar ejbModule = project.getAPIEjbJar();
                return J2eeProjectView.createEjbsView(ejbModule, project);
            }
            return null;
        }

        public void addNotify() {
            project.evaluator().addPropertyChangeListener(this);
        }

        public void removeNotify() {
            project.evaluator().removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && p.equals(WebProjectProperties.J2EE_PLATFORM)){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        changeSupport.fireChange();
                    }
                });
            }
        }
    }
}
