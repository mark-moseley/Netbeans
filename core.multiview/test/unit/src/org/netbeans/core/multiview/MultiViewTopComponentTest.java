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

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import java.awt.Image;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import junit.framework.*;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.junit.*;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.Lookups;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewTopComponentTest extends AbstractMultiViewTopComponentTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewTopComponentTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewTopComponentTest.class);
        
        return suite;
    }

    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def) {
        return MultiViewFactory.createMultiView(desc, def);
    }    
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def, CloseOperationHandler close) {
        return MultiViewFactory.createMultiView(desc, def, close);
    }
    
    protected Class getTopComponentClass() {
        return MultiViewTopComponent.class;
    }
    
    public void testPersistence() throws Exception {
        MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        SerMVElem elem2 = new SerMVElem();
        SerMVElem elem3 = new SerMVElem();
        elem2.deserializeTest = "testtesttest - 2";
        elem3.deserializeTest = "testtesttest - 3";
        
        MultiViewDescription desc1 = new SerMVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
        MultiViewDescription desc2 = new SerMVDesc("desc2", null, TopComponent.PERSISTENCE_ONLY_OPENED, elem2);
        MultiViewDescription desc3 = new SerMVDesc("desc3", null, TopComponent.PERSISTENCE_ALWAYS, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        SerCloseHandler close = new SerCloseHandler("serializedvalue");
        
        TopComponent tc = callFactory(descs, desc2, close);
        tc.open();
        tc.requestActive();
        // testing closehandler here..
        tc.close();
        
        NbMarshalledObject mars = new NbMarshalledObject(tc);
        Object obj = mars.get();
        assertNotNull(obj);
        assertEquals(getTopComponentClass(), obj.getClass());
        tc = (MultiViewTopComponent)obj;
        
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        MultiViewPerspective[] descsAfter = handler.getPerspectives();
        assertNotNull(descsAfter);
        assertEquals(2, descsAfter.length);
        MultiViewPerspective selDesc = handler.getSelectedPerspective();
        assertNotNull(selDesc);
        assertEquals("desc2", selDesc.getDisplayName());
        tc.open();
        tc.requestActive();
        MultiViewTopComponent mvtc = (MultiViewTopComponent)tc;
        Collection cold = mvtc.getModel().getCreatedElements();
        // expected number of elements is one, because the elem3 was not initialized at all..
        assertEquals(1, cold.size());
        
        // test if the deserialized instance is there..
        SerMVElem elSelecto = (SerMVElem)mvtc.getModel().getActiveElement();
        assertEquals("testtesttest - 2", elSelecto.deserializeTest);
        assertEquals("componentOpened-componentShowing-componentActivated-", elSelecto.getLog());
        
        //testing if closehandler was correctly deserialized..
        tc.close();
        
        
    }    
    
}

