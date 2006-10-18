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

package org.netbeans.api.project.ui;

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * List of projects open in the GUI.
 * <p class="nonnormative">
 * <strong>Warning:</strong> this API is intended only for a limited set of use
 * cases where obtaining a list of all open projects is really the direct goal.
 * For example, you may wish to display a chooser letting the user select a
 * file from among the top-level source folders of any open project.
 * For many cases, however, this API is not the correct approach, so use it as
 * a last resort. Consider <a href="@JAVA/API@/org/netbeans/api/java/classpath/GlobalPathRegistry.html"><code>GlobalPathRegistry</code></a>,
 * {@link org.netbeans.spi.project.ui.ProjectOpenedHook},
 * and {@link org.netbeans.spi.project.ui.support.ProjectSensitiveActions}
 * (or {@link org.netbeans.spi.project.ui.support.MainProjectSensitiveActions})
 * first. Only certain operations should actually be aware of which projects
 * are "open"; by default, all project functionality should be available whether
 * it is open or not.
 * </p>
 * @author Jesse Glick, Petr Hrebejk
 */
public final class OpenProjects {
    
    /**
     * Property representing open projects.
     * @see #getOpenProjects
     */
    public static final String PROPERTY_OPEN_PROJECTS = "openProjects"; // NOI18N
    
    /**
     * Property representing main project.
     * @see #getMainProject
     * @since org.netbeans.modules.projectuiapi/1 1.20
     */
    public static final String PROPERTY_MAIN_PROJECT = "MainProject"; // NOI18N
    
    private static OpenProjects INSTANCE = new OpenProjects();
    
    private OpenProjectsTrampoline trampoline;
    
    private OpenProjects() {
        this.trampoline = Utilities.getOpenProjectsTrampoline();
    }

    /**
     * Get the default singleton instance of this class.
     * @return the default instance
     */
    public static OpenProjects getDefault() {                
        return INSTANCE;
    }
    
    /**
     * Gets a list of currently open projects.
     * @return list of projects currently opened in the IDE's GUI; order not specified
     */
    public Project[] getOpenProjects() {
        return trampoline.getOpenProjectsAPI();
    }

    /**
     * Opens given projects.
     * Acquires {@link org.netbeans.api.project.ProjectManager#mutex()} in the write mode.
     * @param projects to be opened. In the case when some of the projects are already opened
     * these projects are not opened again. If the projects contain duplicates, the duplicated
     * projects are opened just once.
     * @param openSubprojects if true also subprojects are opened.
     * @since org.netbeans.modules.projectuiapi/0 1.2
     * <p class="nonnormative">
     * This method is designed for use by logical view's Libraries Node to open one or more of dependent
     * projects. This method can be used also by other project GUI components which need to open certain
     * project(s), eg. code generation wizards.<br>
     * The method should not be used for opening newly created project, insted the
     * {@link org.openide.WizardDescriptor.InstantiatingIterator#instantiate()} used for creation of new project
     * should return the project directory.<br>
     * The method should not be also used  to provide a GUI to open subprojects.
     * The {@link org.netbeans.spi.project.ui.support.CommonProjectActions#openSubprojectsAction()} should be used
     * instead.
     * </p>
     */
    public void open (Project[] projects, boolean openSubprojects) {
        trampoline.openAPI (projects,openSubprojects);
    }

    /**
     * Closes given projects.
     * Acquires {@link org.netbeans.api.project.ProjectManager#mutex()} in the write mode.
     * @param projects to be closed. The non opened project contained in the projects array are ignored.
     * @since org.netbeans.modules.projectuiapi/0 1.2
     */
    public void close (Project[] projects) {
        trampoline.closeAPI (projects);
    }

    /**Retrieves the current main project set in the IDE.
     *
     * <div class="nonnormative">
     * <p><strong>Warning:</strong> the set of usecases that require invoking this method is
     * limited. {@link org.netbeans.spi.project.ui.support.MainProjectSensitiveActions} should
     * be used in favour of this method if possible. In particular, this method should <em>not</em>
     * be used to let the user choose if an action should be run on the main vs. the currently selected project.</p>
     * <p>As a rule of thumb, any code outside of the project system infrastructure which behaves differently
     * depending on the choice of main project should be reviewed critically. All functionality
     * of a project ought to be available regardless of the "main project" flag, which is intended only
     * as a convenient shortcut to permit certain actions (such as <b>Run</b>) to be invoked
     * from a global context on a preselected project.</p>
     * </div>
     *
     * @return the current main project or null if none
     * @since 1.11
     */
    public Project getMainProject() {
        return trampoline.getMainProject();
    }
    
    /**Sets the main project.
     *
     * <div class="nonnormative">
     * <p><strong>Warning:</strong> the set of usecases that require invoking this method is
     * very limited and should be generally avoided if possible. In particular, this method
     * should <em>not</em> be used to mark a project just created by the <b>New Project</b> wizard
     * as the main project.</p>
     * </div>
     *
     * @param project project to set as main project (must be open), or
     *                <code>null</code> to set no project as main.
     * @throws IllegalArgumentException if the project is not opened.
     * @since 1.11
     */
    public void setMainProject(Project project) throws IllegalArgumentException {
        trampoline.setMainProject(project);
    }
    
    /**
     * Adds a listener to changes in the set of open projects.
     * As this class is a singleton and is not subject to garbage collection,
     * it is recommended to add only weak listeners, or remove regular listeners reliably.
     * @param listener a listener to add
     * @see #PROPERTY_OPEN_PROJECTS
     */    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        trampoline.addPropertyChangeListenerAPI( listener );
    }
    
    /**
     * Removes a listener.
     * @param listener a listener to remove
     */
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        trampoline.removePropertyChangeListenerAPI( listener );
    }
    
}
