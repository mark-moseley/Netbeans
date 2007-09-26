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

package org.netbeans.core.startup;

import java.io.IOException;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Template;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;

/**
 * Test functionality of internal URLs.
 * @author Jesse Glick
 */
public class NbURLStreamHandlerFactoryDeadlockTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", MyLkp.class.getName());
    }
    
    public NbURLStreamHandlerFactoryDeadlockTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Main.initializeURLFactory();
        super.setUp();
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    
    
    public void testNbResourceStreamHandlerAndURLStreamHandlerFactoryMerging() throws Exception {
        URL url = new URL("https://www.netbeans.org");
        
        Class<?> c = getClass();
        assertClass(c);
    }
    
    private static void assertClass(Class<?> c) throws IOException {
        URL u = c.getResource(c.getSimpleName() + ".class");
        assertNotNull("Resource for " + c.getSimpleName() + " found", u);
        byte[] arr = new byte[4096];
        int r = u.openStream().read(arr);
        if (r <= 0) {
            fail("Should read something: " + r);
        }
    }
    
    public static class MyLkp extends AbstractLookup implements Runnable {
        @Override
        protected  void beforeLookup(Template<?> template) {
            RequestProcessor.getDefault().post(this).waitFinished();
        }
        
        public void run() {
            try {
                URL url = new URL("https://www.netbeans.org");
                assertClass(NbURLStreamHandlerFactoryDeadlockTest.class);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
