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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectTest;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView.ModulesNode.ModuleChildren;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView.SuiteRootNode;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.Mutex;

/**
 * Test functionality of {@link SuiteLogicalView}.
 *
 * @author Martin Krauskopf
 */
public class SuiteLogicalViewTest extends TestBase {
    
    public SuiteLogicalViewTest(String name) {
        super(name);
    }
    
    public void testModulesNode() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        TestBase.generateSuiteComponent(suite1, "module1a");
        Node modulesNode = new SuiteLogicalView.ModulesNode(suite1);
        modulesNode.getChildren().getNodes(true); // "expand the node" simulation
        waitForGUIUpdate();
        assertEquals("one children", 1, modulesNode.getChildren().getNodes(true).length);
        
        final ModuleChildren children = (ModuleChildren) modulesNode.getChildren();
        TestBase.generateSuiteComponent(suite1, "module1b");
        waitForGUIUpdate();
        assertEquals("two children", 2, children.getNodes(true).length);
        TestBase.generateSuiteComponent(suite1, "module1c");
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                children.stateChanged(null); // #70914
                return null; // #70914
            }
        });
        waitForGUIUpdate();
        assertEquals("three children", 3, children.getNodes(true).length);
    }
    
    public void testNameAndDisplayName() throws Exception {
        SuiteProject p = generateSuite("Sweet Stuff");
        Node n = ((LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class)).createLogicalView();
        assertEquals("Sweet Stuff", n.getName());
        assertEquals("Sweet Stuff", n.getDisplayName());
        NL nl = new NL();
        n.addNodeListener(nl);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("app.name", "sweetness");
        ep.setProperty("app.title", "Sweetness is Now!");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertEquals(new HashSet(Arrays.asList(new String[] {Node.PROP_NAME, Node.PROP_DISPLAY_NAME})), nl.changed);
        assertEquals("Sweetness is Now!", n.getName());
        assertEquals("Sweetness is Now!", n.getDisplayName());
    }
    
    public void testProjectFiles() throws Exception {
        SuiteProject suite = generateSuite("suite");
        TestBase.generateSuiteComponent(suite, "module");
        SuiteProjectTest.openSuite(suite);
        SuiteLogicalView.SuiteRootNode rootNode = (SuiteRootNode) ((LogicalViewProvider)
        suite.getLookup().lookup(LogicalViewProvider.class)).createLogicalView();
        Set expected = new HashSet(Arrays.asList(
                new FileObject[] {
            suite.getProjectDirectory().getFileObject("nbproject"),
            suite.getProjectDirectory().getFileObject("build.xml")
        }
        ));
        assertTrue(expected.equals(rootNode.getProjectFiles()));
    }
    
    public void testImportantFiles() throws Exception {
        // so getDisplayName is taken from english bundle
        Locale.setDefault(Locale.US);
        
        SuiteProject suite = generateSuite("sweet");
        FileObject master = suite.getProjectDirectory().createData("master.jnlp");
        
        LogicalViewProvider viewProv = (LogicalViewProvider) suite.getLookup().lookup(LogicalViewProvider.class);
        Node n = viewProv.createLogicalView();
        
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Two childs are there", 2, arr.length);
        assertEquals("Named modules", "modules", arr[0].getName());
        assertEquals("Named imp files", "important.files", arr[1].getName());
        
        Node[] nodes = n.getChildren().getNodes(true);
        assertEquals("Now there are two", 2, nodes.length);
        assertEquals("Named modules", "modules", nodes[0].getName());
        assertEquals("Named imp files", "important.files", nodes[1].getName());
        
        Node[] subnodes = nodes[1].getChildren().getNodes(true);
        assertEquals("One important node", 1, subnodes.length);
        
        
        DataObject obj = (DataObject) subnodes[0].getCookie(DataObject.class);
        assertNotNull("It represents a data object", obj);
        assertEquals("And it is the master one", master, obj.getPrimaryFile());
        assertEquals("Name of node is localized", "JNLP Descriptor", subnodes[0].getDisplayName());
        
        Node nodeForObj = viewProv.findPath(n, obj);
        Node nodeForFO = viewProv.findPath(n, obj.getPrimaryFile());
        
        assertEquals("For data object we have our node", subnodes[0], nodeForObj);
        assertEquals("For file object we have our node", subnodes[0], nodeForFO);
        
        master.delete();
        
        Node[] newSubN = nodes[0].getChildren().getNodes(true);
        assertEquals("No important nodes", 0, newSubN.length);
        nodeForObj = viewProv.findPath(n, obj);
        nodeForFO = viewProv.findPath(n, obj.getPrimaryFile());
        
        assertNull("For data object null", nodeForObj);
        assertNull("For file object null", nodeForFO);
    }
    
    private static final class NL extends NodeAdapter {
        public final Set/*<String>*/ changed = new HashSet();
        public void propertyChange(PropertyChangeEvent evt) {
            changed.add(evt.getPropertyName());
        }
    }
    
    private void waitForGUIUpdate() throws Exception {
        EventQueue.invokeAndWait(new Runnable() { public void run() {} });
    }
    
}
