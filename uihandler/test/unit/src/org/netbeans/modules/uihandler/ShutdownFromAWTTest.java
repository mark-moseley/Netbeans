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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach
 */
public class ShutdownFromAWTTest extends NbTestCase {
    Installer inst;
    
    public ShutdownFromAWTTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    

    protected void setUp() throws Exception {
        inst = Installer.findObject(Installer.class, true);
        inst.restored();

        MockServices.setServices(DD.class);
        Logger.getLogger("org.netbeans.ui").warning("ONE_LOG");
    }

    protected void tearDown() throws Exception {
    }

    public void testShutdown() throws Exception {
        assertTrue("In EQ", EventQueue.isDispatchThread());
    
        assertTrue("Ok to close", inst.closing());
        inst.close();
    }
    public static final class DD extends DialogDisplayer implements Mutex.Action<Integer> {
        private int cnt;
        
        private void assertAWT() {
            int cnt = this.cnt;
            int ret = Mutex.EVENT.readAccess(this);
            assertEquals("Incremented", cnt + 1, this.cnt);
            assertEquals("Incremented2", cnt + 1, ret);
        }
        
        public Object notify(NotifyDescriptor descriptor) {
            assertAWT();
            
            // last options allows to close usually
            return descriptor.getOptions()[descriptor.getOptions().length - 1];
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            assertAWT();
            
            return new JDialog() {
                public void setVisible(boolean v) {
                }
            };
        }
        
        public Integer run() {
            cnt++;
            assertTrue(EventQueue.isDispatchThread());
            return cnt;
        }
    }
    
}
