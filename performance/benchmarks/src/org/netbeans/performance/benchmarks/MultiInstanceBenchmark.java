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
package org.netbeans.performance.benchmarks;

/**
 * Base class for benchmarks that need a separate instance for every
 * iteration and need to have them prepared before the actual test.
 *
 * The benchmark based on this class needs only to implement method
 * createInstance, and implement test methods that will use array
 * of created instances, named instances. It can also override
 * preSetUp method to do some per-run initialization. It is called
 * at the very beginning of setUp().
 *
 * Example:
 * <PRE>
 * class ListTest extends MultiInstanceBenchmark {
 *
 *     public ListTest( String name ) {
 *         super( name, new Object[] {
 *             new Integer(10), new Integer(100), new Integer(1000)
 *         }
 *     }
 *
 *     protected Object createInstance() {
 *         return new ArrayList();
 *     }
 *
 *     public void testAppend() {
 *         int count = getIterationsCount();
 *         int arg = ((Integer)getArgument()).intValue();
 *
 *         while( count-- > 0 ) {
 *             for( int i=0; i<arg; i++ ) {
 *                 ((List)instances[count]).add( null );
 *             }
 *         }
 *     }
 * }
 *
 * @author  Petr Nejedly
 * @version 0.9
 */
public abstract class MultiInstanceBenchmark extends Benchmark {
    
    public MultiInstanceBenchmark( String name ) {
	super( name );
    }

    public MultiInstanceBenchmark( String name, Object[] args ) {
	super( name, args );
    }
    

    protected Object[] instances;

    protected void setUp() throws Exception {
	preSetUp();
	
	int iters = getIterationCount();
	
	instances = new Object[iters];
	for( int i=0; i<iters; i++ ) {
	    instances[i] = createInstance();
	}
    }

    protected void preSetUp() {
    }
    
    protected abstract Object createInstance();

    protected void tearDown() throws Exception {
	instances = null;
    }
    
}
