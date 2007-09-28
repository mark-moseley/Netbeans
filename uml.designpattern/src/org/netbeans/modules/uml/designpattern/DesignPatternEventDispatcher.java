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


package org.netbeans.modules.uml.designpattern;

import java.util.ArrayList;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public class DesignPatternEventDispatcher extends EventDispatcher implements IDesignPatternEventDispatcher
{
	/** Handles the actual deployment of events to Preference listeners. */
	private EventManager< IDesignPatternEventsSink > m_DesignPatternEventManager = null;

	private EventFunctor m_PreApplyFunc = null;
	private EventFunctor m_PostApplyFunc = null;

	/**
	 *
	 */
	public DesignPatternEventDispatcher()
	{
		super();
		m_DesignPatternEventManager = new EventManager< IDesignPatternEventsSink >();
	}
	/**
	 * Description
	 *
	 * @param pHandler[in]
	 * @param cookie[out]
	 */
	public void registerDesignPatternEvents(IDesignPatternEventsSink pHandler)
	{
		m_DesignPatternEventManager.addListener(pHandler, null);
	}

	/**
	 * Description
	 *
	 * @param pHandler[in]
	 * @param cookie[out]
	 */
	public void revokeDesignPatternSink(IDesignPatternEventsSink pHandler)
	{
		m_DesignPatternEventManager.removeListener(pHandler);
	}
	/**
	 * Fired before a pattern is applied
	 *
	 * @param pDetails[in]	The details that apply to the pattern being applied
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public void firePreApply(IDesignPatternDetails pDetails, IEventPayload payLoad)
	{
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pDetails);

		if (validateEvent("PreApply", collection))
		{
		   IResultCell cell = prepareResultCell(payLoad);
		   if (m_PreApplyFunc == null)
		   {
				m_PreApplyFunc = new EventFunctor("org.netbeans.modules.uml.ui.products.ad.addesigncentergui.designpatternaddin.IDesignPatternEventsSink",
														"onPreApply");
		   }
		   Object[] parms = new Object[2];
		   parms[0] = pDetails;
		   parms[1] = cell;
		   m_PreApplyFunc.setParameters(parms);
		   m_DesignPatternEventManager.notifyListenersWithQualifiedProceed(m_PreApplyFunc);
		}
	}
	/**
	 * Fired after a pattern is applied
	 *
	 * @param pDetails[in]	The details that apply to the pattern was applied
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public void firePostApply(IDesignPatternDetails pDetails, IEventPayload payLoad)
	{
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pDetails);

		if (validateEvent("PostApply", collection))
		{
		   IResultCell cell = prepareResultCell(payLoad);
		   if (m_PostApplyFunc == null)
		   {
				m_PostApplyFunc = new EventFunctor("org.netbeans.modules.uml.ui.products.ad.addesigncentergui.designpatternaddin.IDesignPatternEventsSink",
														"onPostApply");
		   }
		   Object[] parms = new Object[2];
		   parms[0] = pDetails;
		   parms[1] = cell;
		   m_PostApplyFunc.setParameters(parms);
		   m_DesignPatternEventManager.notifyListenersWithQualifiedProceed(m_PostApplyFunc);
		}
	}
}
