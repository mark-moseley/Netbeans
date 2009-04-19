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

package org.openide.util.lookup;

import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

/** To simulate issue 42244.
 */
public class SimpleProxyLookupIssue42244Test extends AbstractLookupBaseHid implements AbstractLookupBaseHid.Impl {
    public SimpleProxyLookupIssue42244Test (java.lang.String testName) {
        super(testName, null);
    }

    public static Test suite() {
        // return new SimpleProxyLookupIssue42244Test("testGarbageCollect");
        return new NbTestSuite(SimpleProxyLookupIssue42244Test.class);
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
        
        @Override
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
