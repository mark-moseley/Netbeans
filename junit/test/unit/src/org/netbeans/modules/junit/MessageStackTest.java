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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
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

package org.netbeans.modules.junit;

import junit.framework.TestCase;

/**
 * Test of class <code>MessageStack</code>.
 *
 * @author Marian Petras
 */
public class MessageStackTest extends TestCase {

    public MessageStackTest(String testName) {
        super(testName);
    }

    private MessageStack stack;

    private boolean useVolatile = false;
    private boolean useClear = false;

    protected void setUp() throws Exception {
        stack = new MessageStack(2);
    }

    public void testSetMessage() {
        System.out.println("testSetMessage");
        
        useVolatile = false;
        useClear = false;
        runStackTest();
    }
    
    public void testClearMessage() {
        System.out.println("testClearMessage");
        
        useVolatile = false;
        useClear = true;
        runStackTest();
    }

    public void testSetVolatileMessage() {
        System.out.println("testSetVolatileMessage");
        
        useVolatile = true;
        useClear = false;
        runStackTest();
    }

    public void testClearVolatileMessage() {
        System.out.println("testClearVolatileMessage");
        
        useVolatile = true;
        useClear = true;
        runStackTest();
    }
    
    private void runStackTest() {

        String msg0, msg1, msg;
        String toDisplay;
        
        msg0 = "message0";
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, str) on an empty stack",
                     msg0, toDisplay);
        
        msg0 = "message00";
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, str) to change item 0",
                     msg0, toDisplay);
        
        msg1 = "message1";
        toDisplay = setMessage(1, msg1);
        assertEquals("setMessage(1, str) to add message below message 0",
                     null, toDisplay);
        
        toDisplay = setMessage(0, null);
        assertEquals("setMessage(0, null) to uncover lower message",
                     msg1, toDisplay);
        
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, str) to cover lower message",
                     msg0, toDisplay);
        
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, str) repeated (no change on the stack)",
                     (String) null, toDisplay);
        
        msg1 = "message11";
        toDisplay = setMessage(1, msg1);
        assertEquals("setMessage(1, str) to change covered lower message",
                     (String) null, toDisplay);
        
        toDisplay = setMessage(1, null);
        assertEquals("setMessage(1, null) to clear covered lower message",
                     (String) null, toDisplay);
        
        toDisplay = setMessage(0, null);
        assertEquals("setMessage(0, null) to clear the only remaining message",
                     "", toDisplay);
        
        toDisplay = setMessage(1, null);
        assertEquals("setMessage(1, null) to \"clear\" already clean message",
                     (String) null, toDisplay);
        
        msg0 = msg1 = "MESSAGE";
        stack.setMessage(0, null);
        stack.setMessage(1, null);
        stack.setMessage(1, msg1);
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, msg) to cover the lower message "
                     + "with the same upper message",
                     (String) null, toDisplay);
        
        msg0 = "message0";
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, msg) to change the upper message, "
                     + "leaving the lower message intact",
                     msg0, toDisplay);
        
        toDisplay = setMessage(0, msg0);
        assertEquals("setMessage(0, msg) again to \"change\" the upper message",
                     (String) null, toDisplay);
        
        msg1 = msg0;
        toDisplay = setMessage(1, msg1);
        assertEquals("setMessage(1, msg) to change the lower message",
                     (String) null, toDisplay);
        
        toDisplay = setMessage(0, null);
        assertEquals("setMessage(0, null) to uncover the lower message"
                     + "which is the same as was the upper message",
                     (String) null, toDisplay);
        
        /* volatile messages: */
        
        stack = new MessageStack(2);
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg = "volatile msg");
        assertEquals("cover an empty stack with a volatile message",
                     msg, toDisplay);
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg = "another volatile message");
        assertEquals("replace the volatile message with another one",
                     msg, toDisplay);
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg);
        assertEquals("\"replace\" the volatile message with the same message",
                     (String) null, toDisplay);
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     null);
        assertEquals("clear the volatile message, "
                     + "thus uncovering an empty stack",
                     "", toDisplay);
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     null);
        assertEquals("clear the already clean volatile message (stack empty)",
                     (String) null, toDisplay);
        
        stack.setMessage(0, msg0 = "message0");
        stack.setMessage(1, msg1 = "message1");
        
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg = "volatile message");
        assertEquals("cover a non-empty stack with a volatile message",
                     msg, toDisplay);
        
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg = "another volatile message");
        assertEquals("replace the volatile message with another one",
                     msg, toDisplay);
        
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     null);
        assertEquals("clear the volatile message, "
                     + "thus uncovering the (non-empty) stack",
                     msg0, toDisplay);
        
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg0);
        assertEquals("cover a non-empty stack with a volatile message "
                     + "that is the same as the topmost stack message",
                     (String) null, toDisplay);
        
        toDisplay = setMessage(MessageStack.LAYER_VOLATILE,
                                     msg0);
        assertEquals("clear the volatile message that is the same as"
                     + " the topmost stack message",
                     (String) null, toDisplay);
        
        stack.setMessage(MessageStack.LAYER_VOLATILE, msg = "volatile");
        toDisplay = setMessage(0, msg0 = "message0");
        assertEquals("setting a stack message when a volatile message is set",
                     msg0, toDisplay);
        
        stack.setMessage(MessageStack.LAYER_VOLATILE, msg = "volatile");
        toDisplay = setMessage(0, msg);
        assertEquals("setting a stack message when a volatile message is set"
                     + " if the stack message is the same as was the volatile"
                     + " message",
                     (String) null, toDisplay);
        
        stack.setMessage(0, msg0 = "message0");
        stack.setMessage(1, msg1 = "message1");
        stack.setMessage(MessageStack.LAYER_VOLATILE, msg = "volatile");
        toDisplay = setMessage(0, null);
        assertEquals("clearing a stack message when a volatile message is set",
                     msg1, toDisplay);
        
        msg = "volatile message";
        stack.setMessage(0, msg0 = "message0");
        stack.setMessage(1, msg);
        stack.setMessage(MessageStack.LAYER_VOLATILE, msg);
        toDisplay = setMessage(0, null);
        assertEquals("clearing a stack message when a volatile message is set"
                     + " if the uncovered stack message is the same as was"
                     + " the volatile message",
                     (String) null, toDisplay);
        
        stack.setMessage(0, null);
        stack.setMessage(1, msg1 = "message1");
        stack.setMessage(MessageStack.LAYER_VOLATILE, msg);
        toDisplay = setMessage(1, null);
        assertEquals("clearing the last stack message when a volatile message"
                     + " is set",
                     "", toDisplay);
        
        stack.setMessage(0, null);
        stack.setMessage(1, null);
        stack.setMessage(MessageStack.LAYER_VOLATILE, msg);
        toDisplay = setMessage(1, null);
        assertEquals("clearing the already clean stack message when"
                     + "a volatile message is set",
                     "", toDisplay);
        
        /* expected exceptions: */
        
        try {
            stack.setMessage(2, "msg");
            fail("setMessage(2, str) should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //expected
        } catch (Exception ex) {
            fail("setMessage(2, str) thrown an unexpected exception");
        }
        
        try {
            stack.setMessage(2, null);
           fail("setMessage(2, null) should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //expected
        } catch (Exception ex) {
            fail("setMessage(2, null) thrown an unexpected exception");
        }
        
        try {
            stack.clearMessage(2);
            fail("clearMessage(2) should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //expected
        } catch (Exception ex) {
            fail("clearMessage(2) thrown an unexpected exception");
        }
    }

    private String setMessage(final int layer, final String msg) {
        boolean volat = (useVolatile && (layer == MessageStack.LAYER_VOLATILE));
        boolean clear = (useClear && (msg == null));
        
        final int callType = (volat ? 0x01 : 0x00) | (clear ? 0x10 : 0x00);
        switch (callType) {
            case 0x00:
                return stack.setMessage(layer, msg);
            case 0x01:
                return stack.setVolatileMessage(msg);
            case 0x10:
                return stack.clearMessage(layer);
            case 0x11:
                return stack.clearVolatileMessage();
            default:
                assert false;
                return "ERROR IN TEST CODE";
        }
    }
    
}
