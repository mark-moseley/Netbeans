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

package gui.actions;

import gui.EPUtilities;

import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class SwitchToSchemaView  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    XMLSchemaComponentOperator schemaComponentOperator;
            
    private static String testSchemaFileName = "fields.xsd";
    
    /** Creates a new instance of SwitchSchemaView */
    public SwitchToSchemaView(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of SwitchSchemaView */
    public SwitchToSchemaView(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    protected void initialize() {
        log(":: initialize");
        Node doc = new Node(EPUtilities.getProcessFilesNode("SOATestProject"), testSchemaFileName);
        doc.select();
        new OpenAction().perform(doc);
    }
        
    public void prepare() {
        log(":: prepare");
        schemaComponentOperator = XMLSchemaComponentOperator.findXMLSchemaComponentOperator(testSchemaFileName);
        schemaComponentOperator.getDesignButton().pushNoBlock();
    }
    
    public ComponentOperator open() {
        log(":: open");
        schemaComponentOperator = XMLSchemaComponentOperator.findXMLSchemaComponentOperator(testSchemaFileName);
        schemaComponentOperator.getSchemaButton().push();
        
        return new XMLSchemaComponentOperator(testSchemaFileName);
    }
    
    public void close() {
        log("::close");
        ((XMLSchemaComponentOperator)testedComponentOperator).close();
    }
    
    @Override
    protected void shutdown() {
//        new CloseAllDocumentsAction().performAPI();
    }

    
    
    public static void main(String[] args) {
        repeat = 3;
        junit.textui.TestRunner.run(new SwitchToSchemaView("measureTime"));
    }
}