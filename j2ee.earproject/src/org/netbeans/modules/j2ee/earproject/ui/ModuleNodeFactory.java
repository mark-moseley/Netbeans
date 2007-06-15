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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
/**
 *
 * @author Lukas Jungmann
 */
public class ModuleNodeFactory implements NodeFactory {

    public ModuleNodeFactory() {
    }

    public NodeList<String> createNodes(Project p) {
        EarProject project = p.getLookup().lookup(EarProject.class);
        assert project != null;
        return new ModuleNodeList(project);
    }

    private static final class ModuleNodeList implements NodeList<String> {

        private static final String JAVAEE_MODULES = "javaeeModules"; //NOI18N

        private final EarProject project;

        ModuleNodeList(EarProject proj) {
            project = proj;
        }

        public List<String> keys() {
            return Collections.singletonList(JAVAEE_MODULES);
        }

        public void addChangeListener(ChangeListener l) {
            // Ignore, not generating change event.
        }

        public void removeChangeListener(ChangeListener l) {
            // Ignore, not generating change event.
        }

        public Node node(String key) {
            if (JAVAEE_MODULES.equals(key)) {
                return new LogicalViewNode(project.getAntProjectHelper());
            }
            assert false : "No node for key: " + key; // NOI18N
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }
    }
}

