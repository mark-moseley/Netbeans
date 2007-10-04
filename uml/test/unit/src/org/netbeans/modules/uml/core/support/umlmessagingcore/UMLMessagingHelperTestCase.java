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


package org.netbeans.modules.uml.core.support.umlmessagingcore;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
/**
 *
 */
public class UMLMessagingHelperTestCase extends AbstractUMLTestCase
{
    private IUMLMessagingEventDispatcher m_Dispatcher = null;
    private TestUMLMessageListener m_MsgListener = new TestUMLMessageListener();
    public static int msgStr = -1;
    /**
     *
     */
    public UMLMessagingHelperTestCase()
    {
        super();
    }
    
    public void testGetMessageService()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        IMessageService service = helper.getMessageService();
        assertNotNull(service);
    }
    
    public void testSendCriticalMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendCriticalMessage("Critical Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_CRITICAL);
    }
    
    public void testSendErrorMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendErrorMessage("Error Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_ERROR);
    }
    
    public void testSendWarningMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendWarningMessage("Warning Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_WARNING);
    }
    
    public void testSendInfoMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendInfoMessage("Info Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_INFO);
    }
    
    public void testSendDebugMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendDebugMessage("Debug Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_DEBUG);
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UMLMessagingHelperTestCase.class);
    }
    
    protected void setUp() throws Exception
    {
//        ADProduct product = new ADProduct();
//        ICoreProductManager pProductManager =  CoreProductManager.instance();
//        pProductManager.setCoreProduct(product);
//        ICoreProduct pCoreProduct = pProductManager.getCoreProduct();
//        pCoreProduct.initialize();
        
        IEventDispatchController cont = product.getEventDispatchController();
        m_Dispatcher =  (IUMLMessagingEventDispatcher)
        cont.retrieveDispatcher(EventDispatchNameKeeper.EDT_MESSAGING_KIND);
        m_Dispatcher.registerMessengerEvents(m_MsgListener);
    }
    
    protected void tearDown() throws Exception
    {
        m_Dispatcher.revokeMessengerSink(m_MsgListener);
    }
}


