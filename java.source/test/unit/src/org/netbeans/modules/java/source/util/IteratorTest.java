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

package org.netbeans.modules.java.source.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import junit.framework.*;
import org.netbeans.modules.java.source.TestUtil;

/** Basic class for testing iterators.
 *
 * @author Petr Hrebejk
 */
public class IteratorTest extends TestCase {

    public IteratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(IteratorTest.class);        
        return suite;
    }
    
    // Protected methods -------------------------------------------------------
    
    protected Iterable<IteratorDescription> createDescriptions() {
    
        List<IteratorDescription> descs = new ArrayList<IteratorDescription>(); 
        
        List<Integer> gl = IteratorTest.createSequentialList( 100 );
        
        descs.add( new IteratorDescription( "PlainListIterator",
                                            gl.iterator(), 
                                            gl.iterator(), 
                                            gl.size(),
                                            true ) );
        
        return descs;
    }
    
    // Protected innerclasses --------------------------------------------------
    
    protected static final class IteratorDescription {
        
        private String name;
        private Iterator iterator;
        private Iterator values;
        private int expectedSize;
        private boolean isModifiable;

        public IteratorDescription( String name, Iterator iterator, 
                                    Iterator values, int expectedSize, 
                                    boolean isModifiable ) {
            this.name = name;
            this.iterator = iterator;
            this.values = values;
            this.expectedSize = expectedSize;
            this.isModifiable = isModifiable;            
        }
        
        public String getName() {
            return name;
        }

        public Iterator getIterator() {
            if ( iterator == null ) {
                throw new IllegalStateException( "IteratorDescription can only be used once.");
            }
            Iterator i = iterator;
            iterator = null;
            return i;
        }

        public Iterator getValues() {
            if ( values == null ) {
                throw new IllegalStateException( "IteratorDescription can only be used once.");
            }
            Iterator i = values;
            values = null;
            return i;
        }

        public int getExpectedSize() {
            return expectedSize;
        }

        public boolean isModifiable() {
            return isModifiable;
        }
        
    }
           
    // Test methods ------------------------------------------------------------

    public void testValues() {
        
        for( IteratorDescription id : createDescriptions() ) {            
            String diff = TestUtil.collectionDiff( id.getValues(), id.getIterator());
            assertEquals( "Diff of iterator: " + id.getName() + " should be empty", "", diff );            
        }                
    }
    
    public void testIteratorSize() {
        
        for( IteratorDescription id : createDescriptions() ) {            
            assertIteratorSize( id.getName(), id.getIterator(), id.getExpectedSize());            
        }                
    }
        
    public void testModifications() {
        
        for( IteratorDescription id : createDescriptions() ) {
            if ( id.isModifiable() ) {
                assertIteratorModifiable( id.getName(), id.getIterator());
            }
            else {
                assertIteratorUnmodifiable( id.getName(), id.getIterator());
            }
        }        
    }
    
    
    public void testTranslatingIterable () throws Exception {
        List<String> keys = new ArrayList<String>(1000);
        for (int i=0; i<keys.size(); i++) {
            keys.add(String.valueOf(i));
        }
        Iterable<Integer> result = Iterators.translating(keys, new Factory<Integer,String> () {
            public Integer create(String parameter) {
                return Integer.parseInt(parameter);
            }            
        });
        Iterator<Integer> it = result.iterator();
        for (int i=0; i<keys.size(); i++) {
            assertTrue (it.hasNext());
            assertEquals(i,it.next().intValue());
        }        
        //Iterable.iterator() should be idempotent
        it = result.iterator();
        for (int i=0; i<keys.size(); i++) {
            assertTrue (it.hasNext());
            assertEquals(i,it.next().intValue());
        }
    }
        
    // Reusable static methods -------------------------------------------------
             
    public static <E> void assertIteratorSize( String name, Iterator<E> it, int expectedSize ) {
        for( int i = 0; i < expectedSize; i++ ) {
            assertTrue( "Iterator: " + name + " should have next element.", it.hasNext() );
            E o = it.next();
        }
        
        try {
            it.next();
        }
        catch ( NoSuchElementException e ) {            
            return; // That's fine
        }
        
        fail( "NoSuchElementException should have been thrown from iterator: " + name + " but was not." );
    }
    
    public static <E> void assertIteratorUnmodifiable( String name, Iterator<E> it ) {
        boolean first = true;
        while( it.hasNext() ) {
            
            if ( first ) {
                try {
                    it.remove();
                    fail( "IllegalStateException or UnsupportedOperationException should have been thrown from iterator: " + name + " but was not." );
                }
                catch( IllegalStateException e ) {
                    // That's fine                    
                }
                catch( UnsupportedOperationException e ) {
                    // That's fine too
                }                
            }
            
            E o = it.next();
            first = false;
            
            try {
                it.remove();
            }
            catch( UnsupportedOperationException e  ) {
                continue; // That's fine
            }
            fail( "UnsupportedOperationException should have been thrown from iterator: " + name + " but was not." );
        }     
    }
    
    public static <E> void assertIteratorModifiable( String name, Iterator<E> it ) {
        boolean first = true;
        while( it.hasNext() ) {
            
            if ( first ) {
                try {
                    it.remove();
                    fail( "IllegalStateException should have been thrown from iterator: " + name + " but was not." );
                }
                catch( IllegalStateException e ) {
                    // That's fine                    
                }                
            }
            
            E o = it.next();
            first = false;
            
            try {
                it.remove();
            }
            catch( UnsupportedOperationException e  ) {
                fail( "Iterator: " + name + " should be modifiable but UnsupportedOperationException should has been thrown." );
            }
            
        }     
    }
    
    public static List<Integer> createSequentialList( int size ) {        
        List<Integer> result = new ArrayList<Integer>( size );                
        for( int i = 0; i < size; i++ ) {
            result.add( new Integer(i) );
        }
        return result;
    }
        
}
