/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.multiview;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import junit.framework.*;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewFactoryTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewFactoryTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewFactoryTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testcreateMultiView () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createMultiView(descs, null);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createMultiView(null, null);
        assertNull(tc);
    }

    
    public void testCreateMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyClose close = new MyClose();
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1, close);
        assertNotNull(tc);
        
        tc.open();
        // just one element as shown..
        tc.close();
        // the close handler is not used, becasue all the elements are in consistent state
        assertFalse(close.wasUsed);
        
    }
    
   public void testCreateCloneableMultiView () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        CloneableTopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createCloneableMultiView(descs, null);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createCloneableMultiView(null, null);
        assertNull(tc);
    }

    
    public void testCreateCloneableMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyClose close = new MyClose();
        TopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1, close);
        assertNotNull(tc);
        
        tc.open();
        // just one element as shown..
        tc.close();
        // the close handler is not used, becasue all the elements are in consistent state
        assertFalse(close.wasUsed);
        
    }    


//    public void testCreateSafeCloseState () throws Exception {
//        CloseOperationState state = MultiViewFactory.createSafeCloseState();
//        assertNotNull(state);
//        assertTrue(state.canClose());
//        assertNotNull(state.getDiscardAction());
//        assertNotNull(state.getProceedAction());
//        assertNotNull(state.getCloseWarningID());
//        
//    }

    
    public void testCreateUnsafeCloseState () throws Exception {
        CloseOperationState state = MultiViewFactory.createUnsafeCloseState("ID_UNSAFE", 
                                            MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        assertNotNull(state);
        assertFalse(state.canClose());
        assertNotNull(state.getDiscardAction());
        assertNotNull(state.getProceedAction());
        assertEquals("ID_UNSAFE", state.getCloseWarningID());
        
        state = MultiViewFactory.createUnsafeCloseState( null, null, null);
        assertNotNull(state);
        assertFalse(state.canClose());
        assertNotNull(state.getDiscardAction());
        assertNotNull(state.getProceedAction());
        assertNotNull(state.getCloseWarningID());
        
    }
    
    
    private class MyClose implements CloseOperationHandler {
        
        public boolean wasUsed = false;
        public int supposed = 0;
        public boolean canClose = true;
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            wasUsed = true;
            return canClose;
        }
        
        
    }
    
}

