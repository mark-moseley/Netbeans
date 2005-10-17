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

package org.openide.loaders;

import java.util.ArrayList;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;


/** Checks that behaviour of LoggingTestCaseHid is correct.
 *
 * @author  Jaroslav Tulach
 */
public class LoggingControlTest extends LoggingTestCaseHid {

    private ErrorManager err;
    
    public LoggingControlTest (String name) {
        super (name);
    }
    
    protected void setUp() throws Exception {
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
    }

    public void testCorrectThreadSwitching() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.log("A");
                events.add("B");
                err.log("B");
                events.add("C");
                err.log("C");
            }
            
            public void directly() {
                err.log("0");
                events.add(new Integer(1));
                err.log("1");
                events.add(new Integer(2));
                err.log("2");
                events.add(new Integer(3));
                err.log("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:1" +
            "THREAD:Para MSG:B" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:C" +
            "THREAD:main MSG:3";
        registerSwitches(order);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        task.waitFinished();
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order", "[A, 1, B, 2, C, 3]", res);
    }
}
