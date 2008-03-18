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
package org.netbeans.modules.hibernate.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 * Provides Framework extension for Hibernate.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateWebModuleExtender extends WebModuleExtender {

    private HibernateConfigurationPanel configPanel = null;
    private final static String DEFAULT_CONFIG_FILENAME = "hibernate.cfg";
    private final String sessionName = "name";
    private final String dialect = "hibernate.dialect";
    private final String driver = "hibernate.connection.driver_class";
    private final String url = "hibernate.connection.url";
    private final String userName = "hibernate.connection.username";
    private final String password = "hibernate.connection.password";
    
    public HibernateWebModuleExtender(boolean forNewProjectWizard, 
            WebModule webModule, ExtenderController controller) {
        configPanel = new HibernateConfigurationPanel(this, controller, forNewProjectWizard);
        if(!forNewProjectWizard) {
            // Show the config panel for Proj. Customizer
            // Fill the panel with existing data.
            showConfigPanelForCustomizer(webModule);
        }
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    @Override
    public JComponent getComponent() {
        return configPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void update() {

    }

    @Override
    public boolean isValid() {
        return configPanel.isPanelValid();
    }

    @Override
    public Set<FileObject> extend(WebModule webModule) {
        Project enclosingProject = Util.getEnclosingProjectFromWebModule(webModule);
        Sources sources = ProjectUtils.getSources(enclosingProject);
        try {
            SourceGroup[] javaSourceGroup = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (javaSourceGroup != null && javaSourceGroup.length != 0) {
                FileObject targetFolder = javaSourceGroup[0].getRootFolder();
                CreateHibernateConfiguration createHibernateConfiguration =
                        new CreateHibernateConfiguration(targetFolder, enclosingProject);
                targetFolder.getFileSystem().runAtomicAction(createHibernateConfiguration);

                return createHibernateConfiguration.getCreatedFiles();
            }

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.EMPTY_SET;
    }

    private void showConfigPanelForCustomizer(WebModule webModule) {
        Project enclosingProject = Util.getEnclosingProjectFromWebModule(webModule);
        ArrayList<FileObject> configFileObjects = HibernateUtil.getAllHibernateConfigFileObjects(enclosingProject);
        for(FileObject configFile : configFileObjects) {
            if(configFile.getName().equals(DEFAULT_CONFIG_FILENAME)) {
                try {
                    HibernateCfgDataObject hibernateDO = (HibernateCfgDataObject) DataObject.find(configFile);
                    SessionFactory sessionFactory = hibernateDO.getHibernateConfiguration().getSessionFactory();
                    configPanel.setSessionName(sessionFactory.getAttributeValue(sessionName));
                    int index = 0;
                    for(String propValue : sessionFactory.getProperty2()) {
                        String propName = sessionFactory.getAttributeValue(SessionFactory.PROPERTY2, index++, "name");  //NOI18N
                        if(dialect.contains(propName)) {
                            configPanel.setDialect(propValue);
                        }
                        if(driver.contains(propName)) {
                            configPanel.setDriver(propValue);
                        }
                        if(url.contains(propName)) {
                            configPanel.setConnectionURL(propValue);
                        }
                        if(userName.contains(propName)) {
                            configPanel.setUserName(propValue);
                        }
                        if(password.contains(propName)) {
                            configPanel.setPassword(propValue);
                        }
                        
                    }
                    
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            } else {
                // There's no default hibernate configuration file.
                // Clear the fields and disable the panel.
                configPanel.setSessionName("");
                configPanel.setDialect("");
                configPanel.setDriver("");
                configPanel.setConnectionURL("");
                configPanel.setUserName("");
                configPanel.setPassword("");
            }
            configPanel.disable();
        }
    }

    private class CreateHibernateConfiguration implements FileSystem.AtomicAction {

        private FileObject targetFolder;
        private Project enclosingProject;
        private Set<FileObject> createdFilesSet = new LinkedHashSet<FileObject>();

        public CreateHibernateConfiguration(FileObject targetFolder, Project enclosingProject) {
            this.targetFolder = targetFolder;
            this.enclosingProject = enclosingProject;
        }

        public Set<FileObject> getCreatedFiles() {
            return createdFilesSet;
        }

        public void run() throws IOException {
            DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
            FileObject templateFileObject = Repository.getDefault().getDefaultFileSystem().findResource("Templates/Hibernate/Hibernate.cfg.xml");  //NOI18N
            DataObject templateDataObject = DataObject.find(templateFileObject);


            DataObject newOne = templateDataObject.createFromTemplate(
                    targetDataFolder,
                    DEFAULT_CONFIG_FILENAME);
            SessionFactory sFactory = new SessionFactory();

            sFactory.setAttributeValue(sessionName, configPanel.getSessionName());
            int row = 0;

            if (configPanel.getSelectedDialect() != null && !"".equals(configPanel.getSelectedDialect())) {
                row = sFactory.addProperty2(configPanel.getSelectedDialect());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", dialect);
            }

            if (configPanel.getSelectedDriver() != null && !"".equals(configPanel.getSelectedDriver())) {
                row = sFactory.addProperty2(configPanel.getSelectedDriver());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", driver);
            }
            if (configPanel.getSelectedURL() != null && !"".equals(configPanel.getSelectedURL())) {
                row = sFactory.addProperty2(configPanel.getSelectedURL());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", url);
            }

            row = sFactory.addProperty2(configPanel.getUserName());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", userName);

            row = sFactory.addProperty2(configPanel.getPassword());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", password);


            HibernateCfgDataObject hdo = (HibernateCfgDataObject) newOne;
            hdo.addSessionFactory(sFactory);
            hdo.save();
            // Register Hibernate Library in the project if its not already registered.
            HibernateEnvironment hibernateEnvironment = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            System.out.println("Library registered : " + hibernateEnvironment.addHibernateLibraryToProject(hdo.getPrimaryFile()));
            createdFilesSet.add(hdo.getPrimaryFile());

        }
    }
}


