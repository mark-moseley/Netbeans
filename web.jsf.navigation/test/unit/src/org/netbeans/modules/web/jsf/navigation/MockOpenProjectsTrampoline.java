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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

/**
 *
 * @author joelle
 */
public class MockOpenProjectsTrampoline implements OpenProjectsTrampoline {

    private final Collection<Project> openProjects = new ArrayList<Project>();

        public MockOpenProjectsTrampoline() {
        }



        public Project[] getOpenProjectsAPI() {
            Project[] projects = new Project[openProjects.size()];
            openProjects.toArray(projects);
            return projects;
        }

        public void openAPI(Project[] projects, boolean openRequiredProjects) {
            for (Project project : projects) {
                openProjects.add(project);
                mainProject = project;
            }
        }

        public void closeAPI(Project[] projects) {
            for (Project project : projects) {
                openProjects.remove(project);
            }
        }

        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private Project mainProject;

        public Project getMainProject() {
            return mainProject;
        }

        public void setMainProject(Project project) {
            if (project != null && !openProjects.contains(project)) {
                throw new IllegalArgumentException("Project " + ProjectUtils.getInformation(project).getDisplayName() + " is not open and cannot be set as main.");
            }
            this.mainProject = project;
        }

}
