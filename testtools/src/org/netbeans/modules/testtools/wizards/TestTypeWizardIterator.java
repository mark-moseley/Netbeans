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
 * TestTypeWizardIterator.java
 *
 * Created on April 11, 2002, 11:46 AM
 */

import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
import java.io.IOException;

import org.openide.WizardDescriptor;
import org.openide.src.MethodElement;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;


/** Test Type Wizard Iterator class
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestTypeWizardIterator extends WizardIterator {
    
    static final long serialVersionUID = -2511372421977509761L;
    
    private static TestTypeWizardIterator iterator;
    
    /** Creates a new instance of TestTypeWizardIterator */
    public TestTypeWizardIterator() {
    }
    
    /** singleton method
     * @return TestSuiteWizardIterator singleton instance */    
    public static synchronized TestTypeWizardIterator singleton() {
        if (iterator==null)
            iterator=new TestTypeWizardIterator();
        return iterator;
    }

    /** perform initialization of Wizard Iterator
     * @param wizard TemplateWizard instance requested Wizard Iterator */    
    public void initialize(TemplateWizard wizard) {
        this.wizard=wizard;
        WizardSettings set=new WizardSettings();
        set.typeTemplate=wizard.getTemplate();
        set.startFromType=true;
        set.readTypeSettings();
        set.store(wizard);
        panels=new WizardDescriptor.Panel[] {
            wizard.targetChooser(),
            new TestTypeSettingsPanel().panel,
            new TestTypeAdvancedSettingsPanel().panel,
            new TestBagSettingsPanel().panel,
            new TestSuiteTargetPanel().panel,
            new TestCasesPanel().panel
        };
        names = new String[panels.length];
        for (int i=0; i<panels.length; i++) {
            ((javax.swing.JComponent)panels[i].getComponent()).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            names[i]=((javax.swing.JComponent)panels[i].getComponent()).getName();
        }
        ((javax.swing.JComponent)panels[0].getComponent()).putClientProperty("WizardPanel_contentData", names);  // NOI18N
        current=0;
    }
    
    /** perform instantiation of templates
     * @param wizard TemplateWizard instance requested Wizard Iterator
     * @throws IOException when some IO problems
     * @return Set of newly created Data Objects */    
    public Set instantiate(TemplateWizard wizard) throws IOException {
        WizardSettings set=WizardSettings.get(wizard);
        set.typeTarget=wizard.getTargetFolder();
        set.typeName=wizard.getTargetName();
        set.createSuite=!hasNext();
        return instantiateTestType(set);
    }
    
}
