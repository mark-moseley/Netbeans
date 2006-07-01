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

package org.openide.util.lookup;

import org.openide.util.*;

import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

/** To simulate issue 42244.
 */
public class SimpleProxyLookupIssue42244Test extends AbstractLookupBaseHid implements AbstractLookupBaseHid.Impl {
    public SimpleProxyLookupIssue42244Test (java.lang.String testName) {
        super(testName, null);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite (SimpleProxyLookupIssue42244Test.class));
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (final Lookup lookup) {
        class C implements Lookup.Provider {
            public Lookup getLookup () {
                return lookup;
            }
        }
        return Lookups.proxy (new C ());
    }
    
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new KeepResultsProxyLookup (new AbstractLookup (ic));
    }
    
    public void clearCaches () {
        KeepResultsProxyLookup k = (KeepResultsProxyLookup)this.instanceLookup;
        
        ArrayList toGC = new ArrayList ();
        Iterator it = k.allQueries.iterator ();
        while (it.hasNext ()) {
            Lookup.Result r = (Lookup.Result)it.next ();
            toGC.add (new WeakReference (r));
        }
        
        k.allQueries = null;
        
        it = toGC.iterator ();
        while (it.hasNext ()) {
            WeakReference r = (WeakReference)it.next ();
            assertGC ("Trying to release all results from memory", r);
        }
    }
    
    class KeepResultsProxyLookup extends ProxyLookup {
        private ArrayList allQueries = new ArrayList ();
        private ThreadLocal in = new ThreadLocal ();
        
        public KeepResultsProxyLookup (Lookup delegate) {
            super (new Lookup[] { delegate });
        }
        
        protected void beforeLookup (org.openide.util.Lookup.Template template) {
            super.beforeLookup (template);
            if (allQueries != null && in.get () == null) {
                in.set (this);
                Lookup.Result res = lookup (template);
                allQueries.add (res);
                in.set (null);
            }
        }
        
    }
}
