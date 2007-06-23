/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.ui.wizards.NewMakeProjectWizardIterator.Name;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public class MakeSampleProjectIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 4L;
    
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new MakeSampleProjectIterator();
    }
    
    public MakeSampleProjectIterator() {
    }
    
    public void addChangeListener(ChangeListener changeListener) {
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void initialize(TemplateWizard templateWizard) {
        int i = 0;
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replaceAll(" ", ""); // NOI18N
        }
        templateWizard.putProperty("name", name); // NOI18N
	String wizardTitle = getString("SAMPLE_PROJECT") + name; // NOI18N
	String wizardTitleACSD = getString("SAMPLE_PROJECT_ACSD"); // NOI18N
        
        panels = new WizardDescriptor.Panel[1];
        panels[i] = new PanelConfigureProject(name, -1, wizardTitle, wizardTitleACSD, false);
        String[] steps = new String[panels.length];
        for (i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent) panels[i].getComponent();
            steps[i] = ((Name) panels[i]).getName();
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
        };
    }
    
    public void uninitialize(org.openide.loaders.TemplateWizard templateWizard) {
        panels = null;
        index = -1;
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
    }
    
    public Set instantiate(TemplateWizard templateWizard) throws IOException {
        File projectLocation = (File) wiz.getProperty("projdir"); // NOI18N
        String name = (String) wiz.getProperty("name"); // NOI18N
        return MakeSampleProjectGenerator.createProjectFromTemplate(templateWizard.getTemplate().getPrimaryFile(), projectLocation, name);
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
	}
	return bundle.getString(s);
    }
    
}
