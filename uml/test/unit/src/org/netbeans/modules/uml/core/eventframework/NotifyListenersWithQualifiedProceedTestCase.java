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


package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;

import junit.framework.TestCase;

public class NotifyListenersWithQualifiedProceedTestCase extends TestCase
{
	EventManager m_Manager = null;
	EventFunctor eventFunctor = null;
	EventFrameworkTestListener1 eveFWListener1 = null;
	EventFrameworkTestListener2 eveFWListener2 = null;
	int listnrNo = 0;
	static int chkVal;
	
	protected void setUp()
	{
		m_Manager = new EventManager();
		eveFWListener1 = new EventFrameworkTestListener1();
		eveFWListener2 = new EventFrameworkTestListener2();
	}
	
	public void testNotifyListenersWithQualifiedProceed()
	{
		// Adds the listeners to EventManager
		m_Manager.addListener(eveFWListener1,null);
		m_Manager.addListener(eveFWListener2,null);
		
		//	Make sure that the variable in the listener class is set false
		assertFalse(EventFrameworkTestListener1.isCalled);
		
		// Tests the whether the function gets executed correctly by calling the first listener
		eventFunctor = new EventFunctor("org.netbeans.modules.uml.core.eventframework.EventFrameworkTestListener1","testMethod1");
		m_Manager.notifyListenersWithQualifiedProceed(eventFunctor);
		assertTrue(EventFrameworkTestListener1.isCalled);
		
		//	Make sure that the variable in the listener class is set false
		assertFalse(EventFrameworkTestListener2.isCalled);
		
		// Tests the whether the function gets executed correctly by calling the second listener
		eventFunctor = new EventFunctor("org.netbeans.modules.uml.core.eventframework.EventFrameworkTestListener2","testMethod2");
		m_Manager.notifyListenersWithQualifiedProceed(eventFunctor);
		assertTrue(EventFrameworkTestListener2.isCalled);
		
	}


}
