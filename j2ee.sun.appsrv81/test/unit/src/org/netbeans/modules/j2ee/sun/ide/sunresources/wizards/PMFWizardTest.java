/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import junit.framework.TestCase;
import org.openide.WizardDescriptor;

/**
 *
 * @author vkraemer
 */
public class PMFWizardTest extends TestCase {
    private WizardDescriptor wiz;
    private WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[3];
       
    public void testCreate() {
        PMFWizard s1 = new PMFWizard().create();
        wiz = new WizardDescriptor(panels);
        s1.initialize(wiz);
        
        assertNotNull("ResourceConfigHelper was created ", s1.getResourceConfigHelper());
        //PMFWizard.PMFWizardIterator s1 = (PMFWizard.PMFWizardIterator) PMFWizard.singleton();
        //wiz.getPanel(0);
        //wiz.getResourceConfigHelper();
        //s1.stateChange(new ChangeEvent() {
            
        //});
        //}
        s1.getResourceConfigHelper().getData().addProperty("foo", "bar");
        s1.getResourceConfigHelper().getData().setTargetFile("PMFWizardTestFile");
        s1.getResourceConfigHelper().getData().addProperty("foo", "bar");
        s1.getWizardInfo("foo");
        s1.hasNext();
        s1.hasPrevious();
        s1.name();
        s1.nextPanel();
        s1.previousPanel();
        s1.setResourceConfigHelper(s1.getResourceConfigHelper());
        s1.instantiate();
         
    }
    
    public PMFWizardTest(String testName) {
        super(testName);
    }
    
}
