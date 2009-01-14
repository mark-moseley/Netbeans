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

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.viewmodel.TreeModelNode;
import org.netbeans.modules.viewmodel.TreeModelRoot;
import org.netbeans.modules.viewmodel.TreeTable;
import org.netbeans.spi.viewmodel.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;



/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class YardaTest  extends NbTestCase {


    public YardaTest (String s) {
        super (s);
    }

    public void testSubsequentRequest () throws Exception {
        doColeasingSimulation (0);
    }
    public void testColeasingOfRequests () throws Exception {
        doColeasingSimulation (1);
    }
    
    private void doColeasingSimulation (int type) throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel1 cm1 = new CompoundModel1 ();
        l.add (cm1);
        TreeTable tt = (TreeTable) Models.createView 
            (Models.createCompoundModel (l));
        Node n = tt.getExplorerManager ().
            getRootContext ();
        synchronized (cm1) {
            n.getChildren ().getNodes ();
            cm1.wait ();
            assertEquals ("Model caled", 1, cm1.count);
            cm1.fire ();
            n.getChildren ().getNodes ();
            
            if (type == 1) {
                cm1.fire ();
                n.getChildren ().getNodes ();
            }
            
            cm1.notifyAll ();
        }
        tt.currentTreeModelRoot.getRootNode().getRequestProcessor().post (new Runnable () {
            public void run () {}
        }).waitFinished ();
        //System.err.println("Child = "+n.getChildren().getNodes()[0]);
        // TODO: Broken, there's a Please wait... node!
        assertEquals ("Computation has finished in RP", 3, n.getChildren ().getNodes ().length);
        assertEquals ("Model caled", 2, cm1.count);
    }
    
    public final class CompoundModel1 extends BasicTest.CompoundModel {
        
        public int count = 0; 


        // init ....................................................................

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public synchronized int getChildrenCount (Object node) throws UnknownTypeException {
            count++;
            notify ();
            /*
            try {
                wait (2000); // We must not wait here, otherwise we get a "Please wait..." node
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
             */
            return super.getChildrenCount (node);
        }
    }
}
