/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.multiview;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.windows.TopComponent;



/** 
 *
 * @author Milos Kleint
 */
public class MultiViewElementTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewElementTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewElementTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testRequestVisible() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("componentOpened-componentShowing-", elem1.getLog());
        assertEquals("",elem2.getLog());

        // initilize the elements..
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        elem1.resetLog();
        elem2.resetLog();
        elem3.resetLog();
        
        elem2.doRequestVisible();
        assertEquals("componentHidden-", elem1.getLog());
        assertEquals("componentShowing-", elem2.getLog());
        assertEquals("", elem3.getLog());
        
        elem3.doRequestVisible();
        assertEquals("componentHidden-", elem1.getLog());
        assertEquals("componentShowing-componentHidden-", elem2.getLog());
        assertEquals("componentShowing-", elem3.getLog());
        
        elem1.doRequestVisible();
        assertEquals("componentShowing-componentHidden-", elem3.getLog());
        assertEquals("componentHidden-componentShowing-", elem1.getLog());
        
    }

    
    public void testRequestActive() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        tc.requestActive();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // initilize the elements..
        // test related hack, easy establishing a  connection from Desc->perspective
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc2));
        elem1.resetLog();
        elem2.resetLog();
        elem3.resetLog();
        
        elem1.doRequestActive();
        assertEquals("componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // do request active the same component, nothing should happen.
        elem1.doRequestActive();
        assertEquals("componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
    }
    
    public void testUpdateTitle() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        tc.open();
        assertEquals(null, tc.getDisplayName());
        
        
        elem2.observer.updateTitle("test1");
        assertEquals("test1", tc.getDisplayName());
        
        // switch to desc3 to initilize the element..
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);

        // test related hack, easy establishing a  connection from Desc->perspective
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        
        elem3.observer.updateTitle("test2");
        assertEquals("test2", tc.getDisplayName());
        
    }
    
}

