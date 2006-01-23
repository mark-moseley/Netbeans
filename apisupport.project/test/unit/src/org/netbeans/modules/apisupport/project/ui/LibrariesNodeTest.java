package org.netbeans.modules.apisupport.project.ui;

import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;

/**
 * @author Martin Krauskopf
 */
public class LibrariesNodeTest extends TestBase {

    public LibrariesNodeTest(String testName) {
        super(testName);
    }
    
    public void testLibrariesNodeListening() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        assertNotNull("have the Libraries node", libraries);
        assertEquals("no libraries yet", 0, libraries.getChildren().getNodesCount());
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(p);
        cmf.add(cmf.addModuleDependency("org.netbeans.modules.java.project"));
        cmf.run();

        assertEquals("dependency noticed", 1, libraries.getChildren().getNodesCount());
    }
    
    // XXX Much more needed
    
}
