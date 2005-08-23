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

package org.netbeans.modules.apisupport.project.layers;

import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Test writing changes to layers.
 * @author Jesse Glick
 */
public class LayerUtilsTest extends LayerTestBase {
    
    public LayerUtilsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    public void testLayerHandle() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileObject expectedLayerXML = project.getProjectDirectory().getFileObject("src/org/example/module/resources/layer.xml");
        assertNotNull(expectedLayerXML);
        FileObject layerXML = handle.getLayerFile();
        assertNotNull("layer.xml already exists", layerXML);
        assertEquals("right layer file", expectedLayerXML, layerXML);
        FileSystem fs = handle.layer();
        assertEquals("initially empty", 0, fs.getRoot().getChildren().length);
        long initialSize = layerXML.getSize();
        fs.getRoot().createData("foo");
        assertEquals("not saved yet", initialSize, layerXML.getSize());
        fs = handle.layer();
        assertNotNull("still have in-memory mods", fs.findResource("foo"));
        fs.getRoot().createData("bar");
        handle.save();
        assertTrue("now it is saved", layerXML.getSize() > initialSize);
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"bar\"/>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        assertEquals("right contents too", xml, TestBase.slurp(layerXML));
    }
    
    public void testLayerAutoSave() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileSystem fs = handle.layer();
        handle.setAutosave(true);
        fs.getRoot().createData("foo");
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        assertEquals("saved automatically", xml, TestBase.slurp(layerXML));
    }
    
    // XXX testInitiallyInvalidLayer
    // XXX testInitiallyMissingLayer
    // XXX testGcLayerHandle
    
    /*
    public void testSystemFilesystemStandaloneProject() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        TestBase.dump(layerXML, xml);
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        assertNotNull("have stuff from the platform", fs.findResource("Menu/File"));
        assertNotNull("have stuff from my own layer", fs.findResource("foo"));
        fs.getRoot().createData("quux");
        handle.save();
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "    <file name=\"quux\"/>\n" +
                "</filesystem>\n";
        assertEquals("new layer stored", xml, TestBase.slurp(layerXML));
    }
     */
    
    // XXX testSystemFilesystemSuiteProject
    // XXX testSystemFilesystemSuiteComponentProject
    // XXX testSystemFilesystemLocalizedNames
    
}
