/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.progress.aggregate;

import junit.framework.TestCase;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.module.ProgressUIWorker;
import org.netbeans.progress.module.ProgressEvent;

/**
 *
 * @author mkleint
 */
public class AggregateProgressHandleTest extends TestCase {

    public AggregateProgressHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Controller.defaultInstance = new Controller(new ProgressUIWorker() {
            public void processProgressEvent(ProgressEvent event) { }
            public void processSelectedProgressEvent(ProgressEvent event) { }
        });
    }

    public void testContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1, contrib2}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib2.getRemainingParentWorkUnits());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib2.getRemainingParentWorkUnits());
        // the +1 deal is there because of the rounding, the last one gest the remainder
        assertEquals(AggregateProgressHandle.WORKUNITS /3 + 1, contrib3.getRemainingParentWorkUnits());
    }
    
    public void testDynamicContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS, contrib1.getRemainingParentWorkUnits());
    
        handle.start();
        contrib1.start(100);
        contrib1.progress(50);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, handle.getCurrentProgress());
        
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        handle.addContributor(contrib2);
        assertEquals(AggregateProgressHandle.WORKUNITS /4, contrib2.getRemainingParentWorkUnits());
        contrib1.finish();
        assertEquals(AggregateProgressHandle.WORKUNITS /4 * 3, handle.getCurrentProgress());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib2.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib3.getRemainingParentWorkUnits());
        contrib3.start(100);
        contrib3.finish();
        assertEquals((AggregateProgressHandle.WORKUNITS /4 * 3) + (AggregateProgressHandle.WORKUNITS /8), 
                     handle.getCurrentProgress());
        
        
    }
    
}
