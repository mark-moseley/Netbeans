/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * WTestWorkspaceWizardIterator.java
 *
 * Created on April 11, 2002, 11:46 AM
 */

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import java.util.HashSet;
import org.w3c.dom.Document;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestWorkspaceWizardIterator extends WizardIterator {
    
    private static TestWorkspaceWizardIterator iterator;
    
    /** Creates a new instance of WorkspaceWizardIterator */
    public TestWorkspaceWizardIterator() {
    }
    
    public static synchronized TestWorkspaceWizardIterator singleton() {
        if (iterator==null)
            iterator=new TestWorkspaceWizardIterator();
        return iterator;
    }

    public void initialize(TemplateWizard wizard) {
        this.wizard=wizard;
        WizardSettings set=new WizardSettings();
        set.workspaceTemplate=wizard.getTemplate();
        Document doc=getDOM(set.workspaceTemplate);
        set.defaultType=getProperty(doc, "xtest.testtype", "value");
        set.defaultAttributes=getProperty(doc, "xtest.attribs", "value");
        set.store(wizard);
        panels=new WizardDescriptor.Panel[] {
            wizard.targetChooser(),
            new TestWorkspaceSettingsPanel(),
            new TestTypeTemplatePanel(),
            new TestTypeSettingsPanel(),
            new TestTypeAdvancedSettingsPanel(),
            new TestBagSettingsPanel(),
            new TestSuiteTargetPanel(),
            new TestCasesPanel()
        };
        names = new String[] {
            "Test Workspace "+wizard.targetChooser().getComponent().getName(),
            "Test Workspace Settings",
            "Test Type Name and Template",
            "Test Type Settings",
            "Test Type Advanced Settings",
            "Test Bag Settings",
            "Test Suite Template and Target Location",
            "Create Test Cases"
        };
        for (int i=0; i<panels.length; i++) {
            ((javax.swing.JComponent)panels[i]).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
            ((javax.swing.JComponent)panels[i]).setName(names[i]);
        }
        ((javax.swing.JComponent)panels[0]).putClientProperty("WizardPanel_contentData", names); 
        current=0;
        

    }
    
    public java.util.Set instantiate(TemplateWizard wizard) throws java.io.IOException {
        WizardSettings set=WizardSettings.get(wizard);
        set.workspaceTarget=wizard.getTargetFolder();
        set.workspaceName=wizard.getTargetName();
        set.createType=current>3;
        set.createBag=current>4;
        set.createSuite=!hasNext();
        return instantiateTestWorkspace(set);
    }
    
}
