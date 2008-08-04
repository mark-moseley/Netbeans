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

package org.openide.nodes;

import junit.framework.*;
import junit.textui.TestRunner;
import java.beans.*;
import java.util.*;
import org.openide.nodes.*;

import org.netbeans.junit.*;

/** Tests various aspects och chanfing nodes Children
 *
 * @author Petr Hrebejk
 */
public class SetChildrenTest extends NbTestCase {

    public SetChildrenTest(String name) {
        super(name);
    }

    /** Tests whether the nodes get Changed.
     */
    public void testChldrenEvents () throws Exception {

        Node[][] testNodes = createTestNodes();
        
        Children.Array chOld = new Children.Array ();        
        chOld.add( testNodes[0] );
        chOld.getNodes(); // To get events from the children
        
        Node root = new TestNodeHid( chOld, "rootNode" );
        TestListenerHid nlRoot = new TestListenerHid();
        root.addNodeListener( nlRoot );
        
        TestListenerHid nlOld = new TestListenerHid();
        testNodes[0][0].addNodeListener( nlOld );
        testNodes[0][1].addNodeListener( nlOld );
        
        Children.Array chNew = new Children.Array ();
        chNew.add( testNodes[1] );
        
        TestListenerHid nlNew = new TestListenerHid();
        testNodes[1][0].addNodeListener( nlNew );
        testNodes[1][1].addNodeListener( nlNew );

        root.setChildren( chNew );
     
        if ( root.getChildren() != chNew ) {
            fail( "Change to new children did not succeed" );
        }
        
        // Test events on old nodes
        
        // System.out.println("OldNodes");
        // GoldenEvent.printEvents( nlOld.getEvents() );
        
        GoldenEvent[] oldGoldenEvents = new GoldenEvent[] {
            new GoldenEvent( testNodes[0][0], Node.PROP_PARENT_NODE, root, null ),
            new GoldenEvent( testNodes[0][1], Node.PROP_PARENT_NODE, root, null )
        };
        
        GoldenEvent.assertEvents ( nlOld.getEvents(), oldGoldenEvents, null );
        
        // Test events on new nodes
        
        //System.out.println("NewNodes");
        //printEvents( nlNew.getEvents() );
        
        GoldenEvent[] newGoldenEvents = new GoldenEvent[] {
            new GoldenEvent( testNodes[1][0], Node.PROP_PARENT_NODE, null, root ),
            new GoldenEvent( testNodes[1][1], Node.PROP_PARENT_NODE, null, root )
        };
        
        GoldenEvent.assertEvents( nlNew.getEvents(), newGoldenEvents, null );
        
        // TestEvents on rootNode
        
        // System.out.println("RootNode");
        // printEvents( nlRoot.getEvents() );
        
        GoldenEvent[] rootGoldenEvents = new GoldenEvent[] {
            new GoldenEvent( root, false, 
                             testNodes[0], 
                             new int[] { 0, 1 } ),
            new GoldenEvent( root, true, 
                             testNodes[1], 
                             new int[] { 0, 1 } )
        };
        
        GoldenEvent.assertEvents ( nlRoot.getEvents(), rootGoldenEvents, null );
    }
    
    public void testFreeParent() throws Exception {
        Node root = new TestNodeHid(Children.LEAF, "rootNode" );
        Children.Array ch = new Children.Array();
        root.setChildren(ch);
        root.setChildren(Children.LEAF);
        root.setChildren(ch);
    }
    

    public void testNotThatMuchChangesWhenSettingToLeaf () throws Exception {
        
        Node[][] testNodes = createTestNodes();
        
        Children.Array chOld = new Children.Array ();        
        chOld.add( testNodes[0] );
        chOld.getNodes(); // To get events from the children
        
        Node root = new TestNodeHid( chOld, "rootNode" );
        TestListenerHid nlRoot = new TestListenerHid();
        root.addNodeListener( nlRoot );
        
        TestListenerHid nlOld = new TestListenerHid();
        testNodes[0][0].addNodeListener( nlOld );
        testNodes[0][1].addNodeListener( nlOld );
        
        TestListenerHid nlNew = new TestListenerHid();
        testNodes[1][0].addNodeListener( nlNew );
        testNodes[1][1].addNodeListener( nlNew );

        root.setChildren(Children.LEAF);
     
        if (root.getChildren() != Children.LEAF) {
            fail( "Change to new children did not succeed" );
        }
        
        // Test events on old nodes
        
        // System.out.println("OldNodes");
        // GoldenEvent.printEvents( nlOld.getEvents() );
        
        GoldenEvent[] oldGoldenEvents = new GoldenEvent[] {
            new GoldenEvent( testNodes[0][0], Node.PROP_PARENT_NODE, root, null ),
            new GoldenEvent( testNodes[0][1], Node.PROP_PARENT_NODE, root, null )
        };
        
        GoldenEvent.assertEvents ( nlOld.getEvents(), oldGoldenEvents, null );
        
        // Test events on new nodes
        
        //System.out.println("NewNodes");
        //printEvents( nlNew.getEvents() );
        
        GoldenEvent[] newGoldenEvents = new GoldenEvent[0];
        
        GoldenEvent.assertEvents( nlNew.getEvents(), newGoldenEvents, null );
        
        // TestEvents on rootNode
        
        // System.out.println("RootNode");
        // printEvents( nlRoot.getEvents() );
        
        GoldenEvent[] rootGoldenEvents = new GoldenEvent[] {
            new GoldenEvent(root, false, testNodes[0], new int[] { 0, 1 }),
            new GoldenEvent(root, Node.PROP_LEAF, Boolean.FALSE, Boolean.TRUE),
        };
        
        GoldenEvent.assertEvents ( nlRoot.getEvents(), rootGoldenEvents, null );
    }
    
    /** Tests whether PROP_LEAF is properly changed
     */
    public void testLeafPropChange() {
        
        Node[][] testNodes = createTestNodes();
        
        Children.Array chNew = new Children.Array();
        chNew.add( testNodes[1] );
        
        Node root = new TestNodeHid( Children.LEAF, "rootNode" );
        TestListenerHid nl = new TestListenerHid();
        root.addNodeListener( nl );
        
        root.setChildren( chNew );
        if ( root.getChildren() != chNew ) {
            fail( "Children change to chNew didin't suceed" );
        }
                
        root.setChildren( Children.LEAF );
        if ( root.getChildren() != Children.LEAF )  {
            fail( "Children change to children.LEAF didin't suceed" );
        }
        
        GoldenEvent[] goldenEvents = new GoldenEvent[] {
            new GoldenEvent( root, Node.PROP_LEAF, Boolean.TRUE, Boolean.FALSE ),
            new GoldenEvent( root, Node.PROP_LEAF, Boolean.FALSE, Boolean.TRUE )
        };

        //System.out.println("LEAF TEST");
        //printEvents( nl.getEvents() );
        
        GoldenEvent.assertEvents (nl.getEvents(), goldenEvents, PropertyChangeEvent.class );
        
    }
    
    
    /** Tests whether PROP_LEAF on filter node is properly changed
     * see issue #27450
     */
    public void testLeafPropChangeFilterNode() {
        
        Node[][] testNodes = createTestNodes();
        
        Children.Array chNew = new Children.Array();
        chNew.add( testNodes[1] );
        
        Node root = new TestNodeHid( Children.LEAF, "rootNode" );
        Node filter = new FilterNode( root );
        TestListenerHid nl = new TestListenerHid();
        filter.addNodeListener( nl );
        
        root.setChildren( chNew );
        root.setChildren( Children.LEAF );
        
        GoldenEvent[] goldenEvents = new GoldenEvent[] {
            new GoldenEvent( root, Node.PROP_LEAF, Boolean.TRUE, Boolean.FALSE ),
            new GoldenEvent( root, Node.PROP_LEAF, Boolean.FALSE, Boolean.TRUE )
        };
        
        GoldenEvent.assertEvents ( nl.getEvents(), goldenEvents, PropertyChangeEvent.class );        
    }
    
    
    /** Tests property changes on old and new nodes
     */

    
    // Private methods ---------------------------------------------------------
    
    private Node[][] createTestNodes() {
        return new Node[][] {
            { new TestNodeHid( Children.LEAF, "Old1" ),
              new TestNodeHid( Children.LEAF, "Old2" ) },
            { new TestNodeHid( Children.LEAF, "New1" ),
              new TestNodeHid( Children.LEAF, "New2" ) }
        };
    }
    
    
    
    // Private innerclasses ----------------------------------------------------
    
    private static class TestNodeHid extends AbstractNode {
                
        public TestNodeHid( Children ch, String name ) {
            super( ch );
            setName( name );
        }        
    }
    
    /** Useful class for testing events.
     */
    
    static class GoldenEvent {
        
        private String name;
        private Object oldValue, newValue;
        private Node source;
        private boolean isAdd;
        private Node[] delta;
        private int[] indices;
        
        public GoldenEvent( Node source, String name, Object oldValue, Object newValue ) {
            this.source = source;
            this.name = name;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        
        public GoldenEvent( Node source, boolean isAdd, Node[] delta, int[] indices ) {
            this.source = source;
            this.isAdd = isAdd;
            this.delta = delta;
            this.indices = indices;
        }
        
        public Class getRepresentedClass() {
            if ( name == null ) {
                return NodeMemberEvent.class;
            }
            else {
                return PropertyChangeEvent.class;
            }
        }
        
        public String toString () {
            StringBuffer sb = new StringBuffer (getRepresentedClass ().getName ());
            sb.append ('[');
            
            if ( getRepresentedClass() == PropertyChangeEvent.class ) {
                sb.append ("source=");
                sb.append (source);
                /*
                return source.equals( pe.getSource() ) &&
                       name.equals( pe.getPropertyName() ) &&
                       oldValue == null ? pe.getOldValue() == null : oldValue.equals( pe.getOldValue() ) &&
                       newValue == null ? pe.getNewValue() == null : newValue.equals( pe.getNewValue() );
                 */
            }
            
            else if ( getRepresentedClass() == NodeMemberEvent.class  ) {
                /*
                NodeMemberEvent nme = (NodeMemberEvent) ev;                
                
                return source.equals( nme.getNode() ) &&
                       isAdd == nme.isAddEvent() &&
                       Arrays.equals( delta, nme.getDelta() ) &&
                       Arrays.equals( indices, nme.getDeltaIndices() );
                 */
            }
            sb.append (']');
            
            return sb.toString();
        }
        
        /* Compares the GoldenEvent against another event
         */
        
        public boolean compareTo( Object ev ) {
            if ( getRepresentedClass() != ev.getClass() ) {
                return false;
            }
            
            if ( getRepresentedClass() == PropertyChangeEvent.class ) {
                PropertyChangeEvent pe = (PropertyChangeEvent)ev;
                return source.equals( pe.getSource() ) &&
                       name.equals( pe.getPropertyName() ) &&
                       oldValue == null ? pe.getOldValue() == null : oldValue.equals( pe.getOldValue() ) &&
                       newValue == null ? pe.getNewValue() == null : newValue.equals( pe.getNewValue() );
            }
            
            else if ( getRepresentedClass() == NodeMemberEvent.class  ) {
                NodeMemberEvent nme = (NodeMemberEvent) ev;                
                
                return source.equals( nme.getNode() ) &&
                       isAdd == nme.isAddEvent() &&
                       Arrays.equals( delta, nme.getDelta() ) &&
                       Arrays.equals( indices, nme.getDeltaIndices() );
            }
            else {
                return false;
            }
        }
       
        /** Compares list of event with array of GoldenEvents. If the 
         * parameter. If the eventClass param is not null only events of 
         * given class are compared.
         */
        public static void assertEvents ( List events, GoldenEvent[] goldenEvents, Class eventClass ) {
            
            List filteredEvents = new ArrayList();
            if ( eventClass != null ) {
                for ( Iterator it = events.iterator(); it.hasNext(); ) {
                    Object e = it.next();
                    if ( e.getClass() == eventClass ) {
                        filteredEvents.add( e );
                    }
                }
            }
            else { 
                filteredEvents = events;
            }
            
            if ( filteredEvents.size() != goldenEvents.length ) {
                fail (
                    "Events have different sizes: expected: <" + goldenEvents.length + "> but was: <" + filteredEvents.size () + ">\n" +
                    "the expected events are " + Arrays.asList (goldenEvents) + "\n" +
                    "the fired events are    " + filteredEvents
                );
            }
                        
            for ( int i = 0; i < filteredEvents.size(); i++ ) {
                if ( !goldenEvents[i].compareTo( filteredEvents.get( i ) ) ) {
                    fail (
                        i + "th events are different: expected: " + goldenEvents[i] + " but was: " + filteredEvents.get (i) + "\n" +
                        "the expected events are " + Arrays.asList (goldenEvents) + "\n" +
                        "the fired events are    " + filteredEvents
                    );
                }
            }
        }
        
        private static void printEvents( List events ) {
        
            for ( Iterator it = events.iterator(); it.hasNext(); ) {
                Object e = it.next();

                if ( e instanceof PropertyChangeEvent ) {
                    System.out.println("PCHG : " + ((PropertyChangeEvent)e).getPropertyName() + " : " + ((PropertyChangeEvent)e).getSource() );
                    System.out.println(" new : " + ((PropertyChangeEvent)e).getOldValue() );
                    System.out.println(" old : " + ((PropertyChangeEvent)e).getNewValue() );
                }

                if ( e instanceof NodeMemberEvent ) {
                    NodeMemberEvent ne = (NodeMemberEvent) e;
                    System.out.println( ( ne.isAddEvent() ? "cADD : " : "cRMV : " ) + ne.getNode().getName() );

                    Node[] delta = ne.getDelta();                
                    if ( delta == null ) {
                        System.out.println("d    : " + null );
                    }
                    else {
                        System.out.println("d    : "  );
                        for( int i = 0; i < delta.length; i++ ) {
                            System.out.println("      " + delta[i].getName() );
                        }
                    }

                    int[] deltaIdx = ne.getDeltaIndices();                
                    if ( deltaIdx == null ) {
                        System.out.println("di   : " + null );
                    }
                    else {
                        System.out.println("di   : " );
                        for( int i = 0; i < deltaIdx.length; i++ ) {
                            System.out.println("      " + deltaIdx[i] );
                        }
                    }

                }

            }    
        }
        
    }
        
    
    private static class TestListenerHid implements NodeListener {
        
        private List events = new ArrayList();
        
        
        public void propertyChange(PropertyChangeEvent evt) {
            events.add( evt );
        }        
                
        public void nodeDestroyed(NodeEvent evt) {
            ChildFactoryTest.assertNodeAndEvent(evt, Collections.<Node>emptyList());
            events.add( evt );
        }        
                
        public void childrenReordered(NodeReorderEvent evt) {
            ChildFactoryTest.assertNodeAndEvent(evt, evt.getSnapshot());
            events.add( evt );
        }
                
        public void childrenRemoved(NodeMemberEvent evt) {
            ChildFactoryTest.assertNodeAndEvent(evt, evt.getSnapshot());
            events.add( evt );
        }
                
        public void childrenAdded(NodeMemberEvent evt) {
            ChildFactoryTest.assertNodeAndEvent(evt, evt.getSnapshot());
            events.add( evt );            
        }
        
        public List getEvents() {
            return events;
        }
        
    }
    
}
