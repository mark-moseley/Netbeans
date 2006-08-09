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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class NewJ2SEFreeformProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    public static final String PROP_PROJECT_MODEL = "projectModel"; // <List> NOI18N
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public NewJ2SEFreeformProjectWizardIterator() {
    }
    
    private WizardDescriptor.Panel[] createPanels () {
        return new WizardDescriptor.Panel[] {
                NewFreeformProjectSupport.createBasicProjectInfoWizardPanel(),
                NewFreeformProjectSupport.createTargetMappingWizardPanel(new ArrayList()), // NOI18N
                new SourceFoldersWizardPanel(),
                new ClasspathWizardPanel(),
            };
    }
    
    public Set/*<FileObject>*/ instantiate () throws IOException {
        throw new AssertionError();
    }

    public Set/*<FileObject>*/ instantiate(final ProgressHandle handle) throws IOException {
        handle.start(6);
        final WizardDescriptor wiz = this.wiz;
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    AntProjectHelper helper = NewFreeformProjectSupport.instantiateBasicProjectInfoWizardPanel(wiz);
                    handle.progress(1);
                    NewFreeformProjectSupport.instantiateTargetMappingWizardPanel(helper, wiz);
                    handle.progress(2);
                    ProjectModel pm = (ProjectModel)wiz.getProperty(PROP_PROJECT_MODEL);
                    ProjectModel.instantiateJavaProject(helper, pm);
                    handle.progress(3);
                    Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    handle.progress(4);
                    ProjectManager.getDefault().saveProject(p);
                    handle.progress(5);
                } catch (IOException e) {
                    ioe[0] = e;
                    return;
                }
            }});
        if (ioe[0] != null) {
            throw ioe[0];
        }
        ProjectModel pm = (ProjectModel)wiz.getProperty(PROP_PROJECT_MODEL);
        File nbProjectFolder = pm.getNBProjectFolder();
        Set resultSet = new HashSet();
        resultSet.add(FileUtil.toFileObject(nbProjectFolder));
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(nbProjectFolder));
        if (p != null) {
            Sources srcs = ProjectUtils.getSources(p);
            if (srcs != null) {
                SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (grps != null && grps.length > 0) {
                    resultSet.add(grps[0].getRootFolder());
                }
            }
        }
        
        File f = nbProjectFolder.getParentFile();
        if (f != null) {
            ProjectChooser.setProjectsFolder(f);
        }
        handle.finish();
        return resultSet;
    }
    
        
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        
        List l = new ArrayList();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            l.add(jc.getName());
        }
        String[] steps = (String[])l.toArray(new String[l.size()]);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            // set title
            jc.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_NewProjectWizardTitle")); // NOI18N
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        NewFreeformProjectSupport.uninitializeBasicProjectInfoWizardPanel(wiz);
        NewFreeformProjectSupport.uninitializeTargetMappingWizardPanel(wiz);
        NewJavaFreeformProjectSupport.uninitializeJavaPanels(wiz);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format (NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_TitleFormat"), // NOI18N
            new Object[] {new Integer (index + 1), new Integer (panels.length) });
    }
    
    public boolean hasNext() {
        if (current() instanceof SourceFoldersWizardPanel) {
            assert current().getComponent() instanceof SourceFoldersPanel;
            SourceFoldersPanel sfp = (SourceFoldersPanel)current().getComponent();
            if (!sfp.hasSomeSourceFolder()) {
                return false;
            }
        }
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
