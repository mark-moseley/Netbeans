/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.io.File;
import java.util.Arrays;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesTest;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 * Tests {@link DataModel}.
 *
 * @author Jiri Rechtacek
 */
public class DataModelTest extends LayerTestBase {
    NbModuleProject project = null;
    
    public DataModelTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
        project = TestBase.generateStandaloneModule (getWorkDir(), "module1");
    }
    
    private void testAddUpdateCenter (String pathToSettingsFile, String[] supposedContent) throws Exception {
        WizardDescriptor wd = new WizardDescriptor (new Panel [] {});
        wd.putProperty (ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel (wd);
        
        // create declaration UC panel, sets the default values into model
        UpdateCenterRegistrationPanel p = new UpdateCenterRegistrationPanel (wd, data);
        p.updateData ();
        
        CreatedModifiedFiles cmf = data.refreshCreatedModifiedFiles ();
        assertEquals (
                Arrays.asList (new String[] {pathToSettingsFile}),
                Arrays.asList (cmf.getCreatedPaths ()));
        assertEquals(
                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/Bundle.properties", "src/org/example/module1/resources/layer.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
        
        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
    }
    
    public void testAddUpdateCenterWithDefaultValues () throws Exception {
        String[] supposedContent = new String [] {
            "<filesystem>",
                "<folder name=\"Services\">",
                    "<folder name=\"AutoupdateType\">",
                        "<file name=\"update_center.settings\" url=\"update_centerSettings.xml\">",
                            "<attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"org.example.module1.resources.Bundle\"/>",
                            "<attr name=\"enabled\" boolvalue=\"true\"/>",
                            "<attr name=\"url_key\" stringvalue=\"org_example_module1_update_center\"/>",
                        "</file>",
                    "</folder>",
                "</folder>",
            "</filesystem>"
        };

        testAddUpdateCenter ("src/org/example/module1/resources/update_centerSettings.xml", supposedContent);
    }
    
    public void testAddUpdateCenterDouble () throws Exception {
        String[] supposedContent = new String [] {
            "<filesystem>",
                "<folder name=\"Services\">",
                    "<folder name=\"AutoupdateType\">",
                        "<file name=\"update_center.settings\" url=\"update_centerSettings.xml\">",
                            "<attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"org.example.module1.resources.Bundle\"/>",
                            "<attr name=\"enabled\" boolvalue=\"true\"/>",
                            "<attr name=\"url_key\" stringvalue=\"org_example_module1_update_center\"/>",
                        "</file>",
                        "<file name=\"update_center_1.settings\" url=\"update_center_1Settings.xml\">",
                            "<attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"org.example.module1.resources.Bundle\"/>",
                            "<attr name=\"enabled\" boolvalue=\"true\"/>",
                            "<attr name=\"url_key\" stringvalue=\"org_example_module1_update_center_1\"/>",
                        "</file>",
                    "</folder>",
                "</folder>",
            "</filesystem>"
        };

        testAddUpdateCenterWithDefaultValues ();
        testAddUpdateCenter ("src/org/example/module1/resources/update_center_1Settings.xml", supposedContent);
    }
    
}

