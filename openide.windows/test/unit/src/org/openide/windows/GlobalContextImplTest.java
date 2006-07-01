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

package org.openide.windows;

import java.util.ArrayList;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes.
 *
 * @author Jaroslav Tulach
 */
public class GlobalContextImplTest extends NbTestCase
implements org.openide.util.LookupListener {

    private javax.swing.Action sampleAction = new javax.swing.AbstractAction () {
        public void actionPerformed (java.awt.event.ActionEvent ev) {
        }
    };
    private TopComponent tc;
    private Lookup lookup;
    private Lookup.Result result;
    private int cnt;
    
    
    public GlobalContextImplTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp () throws Exception {
        tc = new TopComponent ();
        tc.getActionMap ().put (this, sampleAction);
        tc.requestActive();
        
        
        Lookup global = Lookup.getDefault();
        
        Object p = global.lookup (org.openide.util.ContextGlobalProvider.class);
        assertNotNull ("There is one", p);
        assertEquals ("Action context provider is our as well", org.netbeans.modules.openide.windows.GlobalActionContextImpl.class, p.getClass ());
        
        
        lookup = org.openide.util.Utilities.actionsGlobalContext();
        result = lookup.lookup (new Lookup.Template (Node.class));
        result.addLookupListener (this);
        result.allItems();
    }
    
    private void assertActionMap () {
        ActionMap map = (ActionMap)lookup.lookup (ActionMap.class);
        assertNotNull ("Map has to be there", map);
        
        javax.swing.Action action = map.get (this);
        assertEquals ("It is really our action", sampleAction, action);
    }
        
    
    public void testCurrentNodes () throws Exception {
        tc.setActivatedNodes(new Node[] {Node.EMPTY});

        assertEquals ("This fires change", 1, cnt);

        assertEquals ("One item in result", 1, result.allItems ().size ());
        Lookup.Item item = (Lookup.Item)result.allItems ().iterator ().next ();
        assertEquals ("Item should return Node.EMPTY", Node.EMPTY, item.getInstance());
        assertActionMap ();
        
        tc.setActivatedNodes (null);
        assertEquals ("One change", 2, cnt);
        
        assertEquals ("One empty item in result", 1, result.allItems ().size ());
        item = (Lookup.Item)result.allItems ().iterator ().next ();
        assertEquals ("Item should return null", null, item.getInstance());
        assertEquals ("Name is null", "none", item.getId ());
        assertActionMap ();
        
        tc.setActivatedNodes (new Node[0]);
        assertEquals ("No change", 3, cnt);
        
        assertEquals ("No items in lookup", 0, result.allItems ().size ());
        assertActionMap ();
    }
    
    public void testRequestVisibleBlinksTheActionMapForAWhile () throws Exception {
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(new Node[] { n });
        
        assertActionMap ();
        final Lookup.Result res = lookup.lookup (new Lookup.Template (ActionMap.class));
        assertEquals ("One action map", 1, res.allItems ().size ());
        
        class L implements org.openide.util.LookupListener {
            ArrayList maps = new ArrayList ();
            
            public void resultChanged (org.openide.util.LookupEvent ev) {
                assertEquals ("Still only one", 1, res.allItems ().size ());
                Lookup.Item i = (Lookup.Item)res.allItems ().iterator ().next ();
                assertNotNull (i);
                
                maps.add (i.getInstance ());
                
                assertNode ();
            }
            
            public void assertNode () {
                assertEquals ("The node is available", n, lookup.lookup (Node.class));
            }
        }
        L myListener = new L ();
        myListener.assertNode ();
        
        res.addLookupListener (myListener);
                
        TopComponent my = new TopComponent ();
        my.requestVisible ();
        
        if (myListener.maps.size () != 2) {
            fail ("Expected two changes in the ActionMaps: " + myListener.maps);
        }

        myListener.assertNode ();

        ActionMap m1 = (ActionMap)myListener.maps.get (0);
        ActionMap m2 = (ActionMap)myListener.maps.get (1);
        
        assertNull ("Our action is not in first map", m1.get (this));
        assertEquals ("Our action is in second map", sampleAction, m2.get (this));

        assertActionMap ();
    }
    
    public void resultChanged(org.openide.util.LookupEvent ev) {
        cnt++;
    }

}
