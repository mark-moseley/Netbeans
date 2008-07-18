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

/*
 * DiagramCreatorAction.java
 *
 * Created on January 7, 2005, 10:38 AM
 */

package org.netbeans.modules.uml.drawingarea.ui.addins.reguiaddin;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
//import org.netbeans.modules.uml.regui.*;
import org.netbeans.modules.uml.drawingarea.actions.SceneCookieAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Craig Conover
 */
public class RedefineOperationsAction extends SceneCookieAction
{
	
	/**
	 * Creates a new instance of GenerateCodeAction
	 */
	public RedefineOperationsAction()
	{
	}
	
	protected Class[] cookieClasses()
	{
		return new Class[] {IElement.class};
	}
	
	protected int mode()
	{
		return MODE_EXACTLY_ONE;
	}
	

	protected boolean enable(Node[] nodes)
	{
		boolean retVal = false;
		
		if (nodes.length == 1)
		{
			IElement element = (IElement)nodes[0].getCookie(IElement.class);
			
			if (element != null)
			{
				 if (element.getElementType().equals("Class") || // NOI18N
				     element.getElementType().equals("Interface") || // NOI18N
                                     element.getElementType().equals("Enumeration")) // NOI18N
                                 {
                                     DesignerScene scene = nodes[0].getLookup().lookup(DesignerScene.class);
                                     if((scene != null) && scene.isReadOnly() == false)
                                     {
                                         retVal = true;
                                     }
                                 }
			}
		}
		
		return retVal;
	}
	
	protected boolean asynchronous()
	{
		return false;
	}
	
	public HelpCtx getHelpCtx()
	{
		return null;
	}
	
	public String getName()
	{
		return NbBundle.getMessage(
				RedefineOperationsAction.class,
				"IDS_REDEFING_OPERATIONS_MENU"); // NOI18N
	}
	
	protected void performAction(Node[] nodes)
	{
		final ETArrayList < IElement > elements = new ETArrayList<IElement>();
		IElement element = (IElement)nodes[0].getCookie(IElement.class);
		
		if (element != null)
		{
			elements.add(element);
		
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					REGUIAddin regui = new REGUIAddin();
					regui.run(getName(), elements);
				}
			});
			
			thread.run();
		}
	}
}
