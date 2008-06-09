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
package org.netbeans.modules.hibernate.service;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.HibernateException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.spi.hibernate.HibernateFileLocationProvider;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.modules.hibernate.wizards.Util;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This class provides the service for NetBeans projects.
 * This class abstracts the services provided by Hibernate and
 * wrapps them around to provide a more meaningful and high level services.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateEnvironment implements HibernateFileLocationProvider {

    /** Handle to the current project to which this HibernateEnvironment is bound*/
    private Project project;

    /**
     * Creates a new hibernate environment for this NetBeans project.
     *
     * @param project NB project.
     */
    public HibernateEnvironment(Project project) {
        this.project = project;
    }

    public FileObject getLocation() {
        return Util.getSourceRoot(project);
    }

    /**
     * Returns all tables found in the configurations present in this project.
     * 
     * @return list of table names.
     */
    public List<String> getAllDatabaseTablesForProject() {
        return getAllDatabaseTables(
                (HibernateUtil.getAllHibernateConfigurations(project)).toArray(new HibernateConfiguration[]{}));
    }

    public List<String> getDatabaseTables(FileObject mappingFile) {
        List<String> databaseTables = new ArrayList<String>();
        try {
            List<HibernateConfiguration> configurations = getHibernateConfigurationForMappingFile(mappingFile);
            if (configurations.size() == 0) {
                //This mapping file does not belong to any configuration file.
                return databaseTables;
            }
            databaseTables.addAll(HibernateUtil.getAllDatabaseTables(configurations.toArray(new HibernateConfiguration[]{})));
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (HibernateException ex) {
            Exceptions.printStackTrace(ex);
        }
        return databaseTables;
    }

    /**
     * Returns the list of 'HibernateConfiguration' (schema2beans bean) for 
     * the current project.
     * 
     * @return list of HibernateConfiguration(s).
     */
    public List<HibernateConfiguration> getAllHibernateConfigurationsFromProject() {
        return HibernateUtil.getAllHibernateConfigurations(project);
    }

    /**
     * Returns configuration fileobjects if any contained in this project.
     * @return list of FileObjects for configuration files if found in this project, otherwise empty list.
     */
    public List<FileObject> getAllHibernateConfigFileObjects() {
        return HibernateUtil.getAllHibernateConfigFileObjects(project);
    }

    /**
     * Returns all mapping files defined under this project.
     * 
     * @return List of FileObjects for mapping files.
     */
    public List<FileObject> getAllHibernateMappingFileObjects() {
        return HibernateUtil.getAllHibernateMappingFileObjects(project);
    }

    /**
     * Returns relaive source paths of all mapping files present in this project.
     * 
     * @return List of FileObjects for mapping files.
     */
    public List<String> getAllHibernateMappings() {
        return HibernateUtil.getAllHibernateMappingsRelativeToSourcePath(project);
    }

    /**
     * Returns all reverse engineering files defined under this project.
     * 
     * @return List of FileObjects for reverse engg. files.
     */
    public List<FileObject> getAllHibernateReverseEnggFileObjects() {
        return HibernateUtil.getAllHibernateReverseEnggFileObjects(project);
    }
    
    /**
     * Connects to the DB using supplied HibernateConfigurations and gets the list of
     * all table names.
     *
     * @param configurations vararg of Hibernate Configurations.
     * @return array list of strings of table names.
     */
    public List<String> getAllDatabaseTables(HibernateConfiguration... configurations) {
        try {
            return HibernateUtil.getAllDatabaseTables(configurations);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (HibernateException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    /**
     * Returns the table column names for the given table.
     * 
     * @param tableName the table whose column names are needed.
     * @return the list of column names.
     */
    public List<TableColumn> getColumnsForTable(String tableName, FileObject mappingFileObject) {
        List<TableColumn> columnNames = new ArrayList<TableColumn>();
        List<HibernateConfiguration> hibernateConfigurations =
                getHibernateConfigurationForMappingFile(mappingFileObject);
        if (hibernateConfigurations.size() == 0) {
            // This mapping fileis not (yet) mapped to any config file.
            return columnNames;
        } else {
            for (HibernateConfiguration hibernateConfiguration : hibernateConfigurations) {
                columnNames.addAll(HibernateUtil.getColumnsForTable(
                        tableName,
                        hibernateConfiguration));
            }
        }

        return columnNames;
    }

    /**
     * Registers Hibernate Library in this project.
     * 
     * @return true if the library is registered, false if the library is already registered or 
     * registration fails for some reason.
     */
    public boolean addHibernateLibraryToProject(FileObject fileInProject) {
        boolean addLibraryResult = false;
        try {
            LibraryManager libraryManager = LibraryManager.getDefault();
            Library hibernateLibrary = libraryManager.getLibrary("hibernate-support");  //NOI18N
            ProjectClassPathModifier projectClassPathModifier = project.getLookup().lookup(ProjectClassPathModifier.class);
            addLibraryResult = ProjectClassPathModifier.addLibraries(new Library[]{hibernateLibrary}, fileInProject, ClassPath.COMPILE);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            addLibraryResult = false;
        } catch (UnsupportedOperationException ex) {
            //TODO handle this exception gracefully.
            // For now just report it.
            Exceptions.printStackTrace(ex);
        }
        return addLibraryResult;
    }

    /**
     * Returns all mappings registered with this HibernateConfiguration.
     *
     * @param hibernateConfiguration hibernate configuration.
     * @return list of mapping files.
     */
    public List<String> getAllHibernateMappingsFromConfiguration(HibernateConfiguration hibernateConfiguration) {
        List<String> mappingsFromConfiguration = new ArrayList<String>();
        SessionFactory fact = hibernateConfiguration.getSessionFactory();
        int count = 0;
        for (boolean val : fact.getMapping()) {
            String propName = fact.getAttributeValue(SessionFactory.MAPPING,
                    count++, "resource"); //NOI18N
            mappingsFromConfiguration.add(propName);
        }
        return mappingsFromConfiguration;
    }

    private List<HibernateConfiguration> getHibernateConfigurationForMappingFile(FileObject mappingFileObject) {
        List<HibernateConfiguration> hibernateConfigurations = new ArrayList<HibernateConfiguration>();
        for (HibernateConfiguration config : getAllHibernateConfigurationsFromProject()) {
            for (String mappingFile : getAllHibernateMappingsFromConfiguration(config)) {
                if (!mappingFile.trim().equals("") && mappingFileObject.getPath().contains(mappingFile)) {
                    hibernateConfigurations.add(config);
                }
            }
        }
        return hibernateConfigurations;
    }
    
    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @param projectFile file in current project.
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    public List<URL> getProjectClassPath(FileObject projectFile) {
        return HibernateUtil.getProjectClassPathEntries(projectFile);
    }

    /**
     * Returns the NetBeans project to which this HibernateEnvironment instance is bound.
     *
     * @return NetBeans project.
     */
    public Project getProject() {
        return project;
    }
}
