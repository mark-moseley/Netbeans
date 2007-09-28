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

/**
 * @author sumitabhk
 *
 */
public class EventState {

	private IEventDispatcher m_Dispatch = null;

	/**
	 *
	 */
	private EventState() {
		super();
	}

	/**
	 * The constructor for EventState. Handles the call to the dispatcher's
	 * PushEventContext() function.
	 */
	public EventState( IEventDispatcher dispatcher, String state )
	{
	   m_Dispatch = dispatcher;
	   if( m_Dispatch != null)
	   {
		  m_Dispatch.pushEventContext(state);
	   }
	}

	/**
	 * Pushes the passed-in context onto the passed-in dispatcher.
	 *
	 * @param dispatcher[in]   The dispatcher to push the context onto
	 * @param context[in]      The context to push
	 *
	 * @return HRESULT
	 */
	public EventState( IEventDispatcher dispatcher, IEventContext context ) 
	{
		m_Dispatch =  dispatcher ;
	   if( m_Dispatch != null)
	   {
		  m_Dispatch.pushEventContext3(context );
	   }
	}

	public EventState( String dispatcherName, String state )
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
	   	m_Dispatch = (IEventDispatcher) ret.getDispatcher(dispatcherName);
		if( m_Dispatch != null)
	   	{
		   m_Dispatch.pushEventContext(state);
	    }
	}
	
	public void existState()
   {
      try
      {
      	m_Dispatch.popEventContext();
      }
      catch (NullPointerException e)
      {
         // I do not want to do anything.  If m_Dispatch
         // is NULL then there is nothing to do.
      }
   }
	

}


