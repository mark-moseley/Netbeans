/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.editor.suites.keybindings;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.jellytools.modules.editor.KeyBindings;
import org.netbeans.test.editor.LineDiff;

/**This is test in very development stage. I put it into the CVS mainly because
 * it simplifies testing on different platforms. This test may or may not
 * be reliable and may or may not work at all.
 *
 * @author  Jan Lahoda
 */
public class CheckActionsListPerformer extends JellyTestCase {
    
    public static String[] TESTED_EDITORS={"Java Editor","Plain Editor","HTML Editor"};
    
    
    public CheckActionsListPerformer(String name) {
        super(name);
    }
    
    /**
     * @param args the command line arguments
     */
    public void doTest(String editorName) throws Exception {
        log("doTest start");
        
        try {
            List list = KeyBindings.listActions(editorName);
            File f=new File(getWorkDir(),editorName+" actions.ref");
            PrintWriter pw=new PrintWriter(new FileWriter(f));
            for (int i=0;i < list.size();i++) {
                pw.println(list.get(i));
            }
            pw.close();
            assertFile("Output does not match golden file.", getGoldenFile(editorName+" actions.pass"), f, null, new LineDiff(false));
        } finally {
            log("doTest finished");
        }
    }
    
    public void setUp() {
        log("Starting check Key Bindings actions test.");
        log("Test name=" + getName());
    }
    
    public void tearDown() throws Exception {
        log("Starting check Key Bindings actions test.");
        //assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
    }
    
    public void testCheckActions() throws Exception {
        for (int i=0;i < TESTED_EDITORS.length;i++) {
            doTest(TESTED_EDITORS[i]);
        }
    }
    
    public static void main(String[] args) throws Exception {
        new CheckActionsListPerformer("testCheckActions").testCheckActions();
    }
    
}
