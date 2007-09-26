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

package org.openide.explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JTextField;

import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Check the behaviour of ExplorerManager's lookup by doing the same 
 * operations as in case of TopComponent's lookup. Done by providing a fake 
 * component that converts setActivatedNodes to ExplorerManager calls.
 *
 * @author Jaroslav Tulach
 */
public class ExplorerUtilCreateLookupTest extends org.openide.windows.TopComponentGetLookupTest {
    public ExplorerUtilCreateLookupTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new NbTestSuite(ExplorerUtilCreateLookupTest.class);
    }
    
    
    protected boolean runInEQ () {
        return true;
    }
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        class ExTC extends org.openide.windows.TopComponent 
        implements java.beans.PropertyChangeListener {
            ExplorerManager em = new ExplorerManager ();
            {
                addPropertyChangeListener (this);
                em.setRootContext (new AbstractNode (new Children.Array ()));
            }
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                if ("activatedNodes".equals (ev.getPropertyName())) {
                    try {
                        Node[] arr = getActivatedNodes ();
                        Children.Array ch = (Children.Array)em.getRootContext ().getChildren ();
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].getParentNode() != em.getRootContext()) {
                                assertTrue ("If this fails we are in troubles", ch.add (new Node[] { arr[i] }));
                            }
                        }
                        em.setSelectedNodes (getActivatedNodes ());
                    } catch (java.beans.PropertyVetoException ex) {
                        ex.printStackTrace();
                        fail (ex.getMessage());
                    }
                }
            }
        }
        ExTC e = new ExTC ();
        
        top = e;
        lookup = ExplorerUtils.createLookup (e.em, e.getActionMap ());
    }
    
}
