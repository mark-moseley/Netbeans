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

package org.netbeans.tests.xml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.PNGEncoder;


/**
 * Provides the basic support for XML Jemmy tests.
 * @author  ms113234
 */
public class JXTest extends XTest {
    public static final String DELIM = "|";
    protected static boolean captureScreen = true;
    protected static boolean dumpScreen = true;
    
    
    /** Creates a new instance of JXMLXtest */
    public JXTest(String name) {
        super(name);
        boolean dbgTimeouts = Boolean.getBoolean(System.getProperty("xmltest.dbgTimeouts", "true"));
        try {
            if (dbgTimeouts) {
                JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
            }
        } catch (IOException ioe) {
            log("Load Debug Timeouts fail.", ioe);
        }
    }

    protected void fail(String msg, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        if (captureScreen) {
            try {
                PNGEncoder.captureScreen(getWorkDirPath()+File.separator+"screen.png");
            } catch (Exception e1) {}
        }
        if (dumpScreen) {
            try {
                Dumper.dumpAll(getWorkDirPath()+File.separator+"screen.xml");
            } catch (Exception e2) {}
        }
        fail(msg + "\n" + sw);
    }
    
    /**
     * Finds Node in the 'data' forlder.
     * @param path relative to the 'data' folder delimited by 'DELIM'
     */
    protected Node findDataNode(String path) {
        Node node = null;
        try {
            String treePath = getFilesystemName() + DELIM + getDataPackageName(DELIM) + DELIM + path;
            JTreeOperator tree = ExplorerOperator.invoke().repositoryTab().tree();
            tree.setComparator(new Operator.DefaultStringComparator(true, true));
            node = new Node(tree, treePath);
        } catch (Exception ex) {
            log("Cannot find data node: " + path, ex);
        }
        return node;
    }
    
    /**
     * Finds Catalog's node.
     * @param path relative to the 'XML Entity Catalogs' root delimited by 'DELIM'
     */
//    protected Node findCatalogNode(String path) {
//        Node node = null;
//        try {
//            String treePath = Bundle.getStringTrimmed("org.netbeans.modules.xml.catalog.Bundle", "TEXT_catalog_root");
//            if (path != null && path.length() > 0) treePath += DELIM + path;
//            JTreeOperator tree = ExplorerOperator.invoke().runtimeTab().tree();
//            node = new Node(tree, treePath);
//        } catch (Exception ex) {
//            log("Cannot find catalog node: " + path, ex);
//        }
//        return node;
//    }
        
//    /**
//     * Returns work directory subnode or null
//     */
//    protected FolderNode getWorkDirNode(String name) throws IOException {
//        final String FILESYSTEMS = JelloBundle.getString("org.netbeans.core.Bundle", "dataSystemName");
//        String path = FILESYSTEMS + ", " + getWorkDirPath() + ", " + name;
//        
//        Explorer explorer = new Explorer();
//        explorer.switchToFilesystemsTab();
//        //TreePath treePath = explorer.getJTreeOperator().findPath(path, ", ");
//        //explorer.getJTreeOperator().expandPath(treePath);
//        
//        return FolderNode.findFolder(FILESYSTEMS + ", " + getWorkDirPath() + ", " + name);
//    }
}
