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

package org.netbeans.modules.apisupport.project.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
public class LibrariesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public LibrariesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        NbModuleProject proj =  p.getLookup().lookup(NbModuleProject.class);
        assert proj != null;
        return new LibraryNL(proj);
    }
    
    private static class LibraryNL implements NodeList<String> {
        
        private NbModuleProject project;
        
        LibraryNL(NbModuleProject prj) {
            project = prj;
        }
    
        public List<String> keys() {
            List<String> toRet = new ArrayList<String>();
            toRet.add(LibrariesNode.LIBRARIES_NAME);
            for (String testType : project.supportedTestTypes()) {
                toRet.add(testType);
            }
            return toRet;
        }
        
        private FileObject resolveFileObjectFromProperty(String property){
            String filename = project.evaluator().getProperty(property);
            if (filename == null) {
                return null;
            }
            return project.getHelper().resolveFileObject(filename);
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public Node node(String key) {
            if (key == LibrariesNode.LIBRARIES_NAME) {
                return  new LibrariesNode(project);
            } else {
                return new UnitTestLibrariesNode(key, project);
            }
        }

        public void addNotify() {
            //TODO shall we somehow listen on project and ech for the 
            // test.unit.src.dir prop appearance/disappearance ??
        }

        public void removeNotify() {
        }
}

}
