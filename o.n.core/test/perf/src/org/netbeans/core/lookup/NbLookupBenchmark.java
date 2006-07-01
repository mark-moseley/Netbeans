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


package org.netbeans.core.lookup;


import java.util.Collection;

import junit.framework.TestSuite;

import org.netbeans.performance.Benchmark;

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


public class NbLookupBenchmark extends Benchmark {
    /** how many times objects in INSTANCES should be added in */
    private static Object[] ARGS = {
        new Integer (1)
    };


    public NbLookupBenchmark(java.lang.String testName) {
        super(testName, ARGS);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new TestSuite (NbLookupBenchmark.class));
    }
    
    /** Lookup which simulates instance lookup. */
    private AbstractLookup lookup;

    /** instances that we register */
    private static Object[] INSTANCES = new Object[] {
        new Integer (10), 
        new Object (),
        "Ahoj",
        new C4 (), new C3 (), new C2 (), new C1 ()
    };

    /** Fills the lookup with instances */
    protected void setUp () {
        Integer integer = (Integer)getArgument ();
        int cnt = integer.intValue ();
        
        boolean reverse = cnt < 0;
        if (reverse) cnt = -cnt;
        
        InstanceContent iContent = new InstanceContent();
        
        lookup = new AbstractLookup(iContent);
        
        while (cnt-- > 0) {
            for (int i = 0; i < INSTANCES.length; i++) {
                if (reverse) {
                    iContent.add (INSTANCES[INSTANCES.length - i - 1]);
                } else {
                    iContent.add (INSTANCES[i]);
                }
            }
        }
    }
    
    /** Clears the lookup.
     */
    protected void tearDown () {
        lookup = null;
    }
    
    /** Test to find the first registered object.
     */
    public void testInteger () {
        enum (Integer.class);
    }
    
    /** Test object.
     */
    public void testObject () {
        enum (Object.class);
    }
    
    /** Test string.
     */
    public void testString () {
        enum (String.class);
    }
    
    public void testC1 () {
        enum (C1.class);
    }
    
    public void testC2 () {
        enum (C2.class);
    }
    
    public void testC3 () {
        enum (C3.class);
    }
    
    public void testC4 () {
        enum (C4.class);
    }
    
    public void testI1 () {
        enum (I1.class);
    }
    
    public void testI2 () {
        enum (I2.class);
    }
    
    public void testI3 () {
        enum (I3.class);
    }
    
    public void testI4 () {
        enum (I4.class);
    }
        
        
        
    /** Enumerates over instances of given class.
     * @param clazz the class to find instances of
     */
    private void enum (Class clazz) {
        int cnt = getIterationCount ();
        
        while (cnt-- > 0) {
            Lookup.Result res = lookup.lookup (new Lookup.Template (clazz));

            Collection c = res.allInstances ();
        }
    }
    
    
    private static interface I1 {}
    private static interface I2 extends I1 {}
    private static interface I3 extends I1 {}
    private static interface I4 extends I2, I3 {}
    private static class C1 extends Object implements I2 {}
    private static class C2 extends C1 {}
    private static class C3 extends C2 implements I3 {}
    private static class C4 extends C3 implements I4 {}
}
