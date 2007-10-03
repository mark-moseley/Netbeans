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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.wizard.entity.WrapperPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelDS;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelJdbc;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Copy of j2ee/utilities Util class
 */
public class Util {
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the simple class for the passed fully-qualified class name.
     */
    public static String getClassName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(dot + 1);
        } else {
            return fqClassName;
        }
    }
    
    /**
     * Returns the package name of the passed fully-qualified class name.
     */
    public static String getPackageName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(0, dot);
        } else {
            return ""; // NOI18N
        }
    }
    
    /**
     * Returns the SourceGroup of the passesd project which contains the
     * fully-qualified class name.
     */
    public static SourceGroup getClassSourceGroup(Project project, String fqClassName) {
        String classFile = fqClassName.replace('.', '/') + ".java"; // NOI18N
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject classFO = sourceGroup.getRootFolder().getFileObject(classFile);
            if (classFO != null) {
                return sourceGroup;
            }
        }
        return null;
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(project, sourceGroups);
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }
    
    private static Set/*<SourceGroup>*/ getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set testGroups = new HashSet();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }
    
    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else if (Logger.getLogger("global").isLoggable(Level.FINE)) {
                Logger.getLogger("global").log(Level.FINE, null, new IllegalStateException("No FileObject found for the following URL: " + urls[i]));
            }
        }
        return result;
    }
    
    private static List/*<SourceGroup>*/ getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList();
        }
        List result = new ArrayList();
        List sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = (FileObject) sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    public static ClassPath getFullClasspath(FileObject fo) {
        FileObject[] sourceRoots = ClassPath.getClassPath(fo, ClassPath.SOURCE).getRoots();
        FileObject[] bootRoots = ClassPath.getClassPath(fo, ClassPath.BOOT).getRoots();
        FileObject[] compileRoots = ClassPath.getClassPath(fo, ClassPath.COMPILE).getRoots();
        FileObject[] roots = new FileObject[sourceRoots.length + bootRoots.length + compileRoots.length];
        for (int i = 0; i < sourceRoots.length; i++) {
            roots[i] = sourceRoots[i];
        }
        for (int i = 0; i < bootRoots.length; i++) {
            roots[sourceRoots.length + i] = bootRoots[i];
        }
        for (int i = 0; i < compileRoots.length; i++) {
            roots[sourceRoots.length + bootRoots.length + i] = compileRoots[i];
        }
        return ClassPathSupport.createClassPath(roots);
    }
    
    // from ejbcore utils
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;
    
    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }
    
    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, new Integer(i));
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }
    
    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }
    
    public static boolean isSupportedJavaEEVersion(Project project) {
        JPAModuleInfo moduleInfo = project.getLookup().lookup(JPAModuleInfo.class);
        if (moduleInfo == null){
            return false;
        }
        if (JPAModuleInfo.ModuleType.EJB == moduleInfo.getType()
                && "3.0".equals(moduleInfo.getVersion())){
            return true;
        }
        if (JPAModuleInfo.ModuleType.WEB == moduleInfo.getType()
                && "2.5".equals(moduleInfo.getVersion())){
            return true;
        }
        return false;
    }
    
    public static boolean isEjbModule(Project project) {
        JPAModuleInfo moduleInfo = project.getLookup().lookup(JPAModuleInfo.class);
        if (moduleInfo == null){
            return false;
        }
        return JPAModuleInfo.ModuleType.EJB == moduleInfo.getType();
    }
    
    
    public static boolean isEjb21Module(Project project) {
        JPAModuleInfo moduleInfo = project.getLookup().lookup(JPAModuleInfo.class);
        if (moduleInfo == null){
            return false;
        }
        
        return JPAModuleInfo.ModuleType.EJB == moduleInfo.getType()
                && "2.1".equals(moduleInfo.getVersion());
    }
    
    /**
     * @return true if given this data object's project's enviroment is Java SE, false otherwise.
     */
    public static boolean isJavaSE(Project project){
        return project.getLookup().lookup(JPAModuleInfo.class) == null;
    }
    
    /**
     * Builds a persistence unit using wizard. Does not save the created persistence unit
     * nor create the persistence.xml file if it  does not exist.
     * @param project the current project
     * @param preselectedDB the name of the database connection that should be preselected in the wizard.
     * @tableGeneration the table generation strategy that should be preselected in the wizard.
     * @return the created PersistenceUnit or null if nothing was created, for example
     * if wizard was cancelled.
     */
    public static PersistenceUnit buildPersistenceUnitUsingWizard(Project project,
            String preselectedDB, TableGeneration tableGeneration){
        
        boolean isContainer = Util.isSupportedJavaEEVersion(project);
        PersistenceUnitWizardPanel panel;
        if (isContainer) {
            panel = new PersistenceUnitWizardPanelDS(project, null, true, tableGeneration);
        } else {
            panel = new PersistenceUnitWizardPanelJdbc(project, null, true, tableGeneration);
        }
        if (preselectedDB != null) {
            panel.setPreselectedDB(preselectedDB);
        }
        
        final JButton createPUButton = new JButton(NbBundle.getMessage(Util.class,"LBL_CreatePersistenceUnitButton"));
        createPUButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Util.class,"ACSD_CreatePersistenceUnitButton"));
        Object[] buttons = new Object[] { createPUButton, DialogDescriptor.CANCEL_OPTION };
        
        final DialogDescriptor nd = new DialogDescriptor(
                new WrapperPanel(panel),
                NbBundle.getMessage(Util.class, "LBL_CreatePersistenceUnit"),
                true,
                buttons,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null
                );
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        nd.setValid(((Boolean)newvalue).booleanValue());
                        createPUButton.setEnabled(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        if (!panel.isValidPanel()) {
            nd.setValid(false);
            createPUButton.setEnabled(false);
        }
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (result == createPUButton) {
            PersistenceUnit punit = new PersistenceUnit();
            if (isContainer) {
                PersistenceUnitWizardPanelDS puPanel = (PersistenceUnitWizardPanelDS) panel;
                if (puPanel.getDatasource() != null && !"".equals(puPanel.getDatasource().trim())){
                    if (puPanel.isJTA()) {
                        punit.setJtaDataSource(puPanel.getDatasource());
                    } else {
                        if (puPanel.isNonDefaultProviderEnabled()) {
                            punit.setNonJtaDataSource(puPanel.getDatasource());
                        }
                        punit.setTransactionType("RESOURCE_LOCAL");
                    }
                }
                if (puPanel.isNonDefaultProviderEnabled()) {
                    punit.setProvider(puPanel.getNonDefaultProvider());
                }
            } else {
                PersistenceUnitWizardPanelJdbc puJdbc = (PersistenceUnitWizardPanelJdbc) panel;
                punit = ProviderUtil.buildPersistenceUnit(puJdbc.getPersistenceUnitName(), puJdbc.getSelectedProvider(), puJdbc.getPersistenceConnection());
                punit.setTransactionType("RESOURCE_LOCAL"); //NO18N
                if (puJdbc.getPersistenceLibrary() != null){
                    addLibraryToProject(project, puJdbc.getPersistenceLibrary());
                }
            }
            punit.setName(panel.getPersistenceUnitName());
            ProviderUtil.setTableGeneration(punit, panel.getTableGeneration(), project);
            return punit;
        }
        return null;
        
    }

    /**
     * Creates a persistence unit using the PU wizard and adds the created
     * persistence unit to the given project's <code>PUDataObject</code> and saves it.
     *
     * @param project the project to which the created persistence unit is to be created.
     * @param preselectedDB the name of the db connection that should be preselected, or null if none needs
     * to be preselected.
     * @param tableGeneration the table generation strategy for the persistence unit.
     *
     * @return true if the creation of the persistence unit was successful, false otherwise.
     *
     * @throws InvalidPersistenceXmlException if the persistence.xml file in the given
     * project is not valid.
     *
     */
    public static boolean createPersistenceUnitUsingWizard(Project project,
            String preselectedDB, TableGeneration tableGeneration) throws InvalidPersistenceXmlException {
        
        PersistenceUnit punit = buildPersistenceUnitUsingWizard(project, preselectedDB, tableGeneration);
        if (punit == null){
            return false;
        }
        PUDataObject pud = ProviderUtil.getPUDataObject(project);
        pud.addPersistenceUnit(punit);
        pud.save();
        return true;
    }
    
    /**
     * Creates a persistence unit with the default table generation strategy using the PU wizard and adds the created
     * persistence unit to the given project's <code>PUDataObject</code> and saves it.
     *
     * @param project the project to which the created persistence unit is to be created.
     * @param preselectedDB the name of the db connection that should be preselected, or null if none needs
     * to be preselected.
     *
     * @return true if the creation of the persistence unit was successful, false otherwise.
     *
     * @throws InvalidPersistenceXmlException if the persistence.xml file in the given
     * project is not valid.
     *
     */
    public static boolean createPersistenceUnitUsingWizard(Project project, String preselectedDB) throws InvalidPersistenceXmlException {
        return createPersistenceUnitUsingWizard(project, preselectedDB, TableGeneration.CREATE);
    }
    
    public static void addLibraryToProject(Project project, Library library) {
        ProjectClassPathExtender pcpe = (ProjectClassPathExtender) project.getLookup().lookup(ProjectClassPathExtender.class);
        if (pcpe != null) {
            try {
                pcpe.addLibrary(library);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static String simpleClassName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot > 0 ? fqn.substring(lastDot + 1) : fqn;
    }
    
}
