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

package org.openide.util.actions;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JButton;
import junit.textui.TestRunner;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Test general aspects of system actions.
 * Currently, just the icon.
 * @author Jesse Glick
 */
public class AsynchronousTest extends NbTestCase {

    private CharSequence err;
    
    public AsynchronousTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() {
        err = Log.enable("", Level.ALL);
    }
    
    public void testExecutionOfActionsThatDoesNotOverrideAsynchronousIsAsynchronousButWarningIsPrinted() throws Exception {
        DoesNotOverride action = (DoesNotOverride)DoesNotOverride.get(DoesNotOverride.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Not yet finished", action.finished);
            action.wait();
            assertTrue("The asynchronous action is finished", action.finished);
        }
        
        if (err.toString().indexOf(DoesNotOverride.class.getName() + " should override") < 0) {
            fail("There should be warning about not overriding asynchronous: " + err);
        }
    }
    
    public void testExecutionCanBeAsynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Not yet finished", action.finished);
            action.wait();
            assertTrue("The asynchronous action is finished", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeSynchronous() throws Exception {
        DoesOverrideAndReturnsFalse action = (DoesOverrideAndReturnsFalse)DoesOverrideAndReturnsFalse.get(DoesOverrideAndReturnsFalse.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeForcedToBeSynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            assertTrue("When asked for synchronous the action is finished immediatelly", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public static class DoesNotOverride extends CallableSystemAction {
        boolean finished;
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        public String getName() {
            return "Should warn action";
        }
        
        public synchronized void performAction() {
            notifyAll();
            finished = true;
        }
        
    }
    
    public static class DoesOverrideAndReturnsTrue extends DoesNotOverride {
        public boolean asynchronous() {
            return true;
        }
    }
    
    public static final class DoesOverrideAndReturnsFalse extends DoesOverrideAndReturnsTrue {
        public boolean asynchronous() {
            return false;
        }
    }
}
