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

package org.netbeans.modules.groovy.grailsproject;

import org.netbeans.modules.groovy.grails.api.GrailsServerState;
import org.netbeans.modules.groovy.grailsproject.ui.GrailsLogicalViewProvider;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public final class GrailsProject implements Project {

    private final FileObject projectDir;
    private final ProjectState projectState;
    private final LogicalViewProvider logicalView;

    private Lookup lookup;

    public GrailsProject(FileObject projectDir, ProjectState projectState) {
        this.projectDir = projectDir;
        this.projectState = projectState;
        this.logicalView = new GrailsLogicalViewProvider(this);
    }

    public FileObject getProjectDirectory() {
        return projectDir;
    }

    public Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(
                this,  //project spec requires a project be in its own lookup
                projectState, //allow outside code to mark the project as needing saving
                new Info(), //Project information implementation
                new GrailsSources(projectDir),
                new GrailsServerState(),
                logicalView //Logical view of project implementation
            );
        }
        return lookup;
    }

    private final class Info implements ProjectInformation {

        public Icon getIcon() {
            Image image = Utilities.loadImage("org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png");
            return new ImageIcon(image);
        }

        public String getName() {
            return getProjectDirectory().getName();
        }

        public String getDisplayName() {
            return getName();
        }

        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        public Project getProject() {
            return GrailsProject.this;
        }
    }

}
