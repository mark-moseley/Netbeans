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
 * File       : SubExpressionProxy.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class SubExpressionProxy implements IExpressionProxy
{
	private ExpressionStateHandler mSubExpression = null;

    public SubExpressionProxy(ExpressionStateHandler expression)
    {
		mSubExpression = expression;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#clear()
     */
    public void clear()
    {
		if(mSubExpression != null)
			mSubExpression= null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getEndPosition()
     */
    public long getEndPosition()
    {
		long retVal = -1;
		if(mSubExpression != null)
		{
			try
			{
				retVal = mSubExpression.getEndPosition();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}   
      
		}
		return retVal;
    }

   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getStartLine()
     */
    public long getStartLine()
    {
		long retVal = -1;
		if(mSubExpression != null)
		{
			try
			{
				retVal = mSubExpression.getStartLine();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}   
		}
		return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getStartPosition()
     */
    public long getStartPosition()
    {
		long retVal = -1;
		if(mSubExpression != null)
		{
			try
			{
				retVal = mSubExpression.getStartPosition();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}   
		}
		return retVal;
    }
    
    
	public String toString()
	{
	   String retVal = "";
	   if(mSubExpression != null)
	   {
		  retVal = mSubExpression.toString();
	   }
	   return retVal;
	}


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#sendOperationEvents(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
     */
    public InstanceInformation sendOperationEvents(InstanceInformation pInstance,
												   IREClass pThisPtr,
												   SymbolTable symbolTable,
												   IREClassLoader pClassLoader,
												   IUMLParserEventDispatcher pDispatcher,
												   Node pParentNode)
    {
		InstanceInformation retVal = null;
		if(mSubExpression != null)
		{
			retVal = mSubExpression.sendOperationEvents(pInstance, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
		}
		return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
     */
    public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo,
    													 Node pParentNode,
    													 SymbolTable symbolTable,
    													 IREClass pThisPtr,
    													 IREClassLoader pClassLoader)
    {
		ETPairT<InstanceInformation, Node> retVal = new ETPairT<InstanceInformation,Node>(null,null);
		if(mSubExpression != null)
		{
			retVal = mSubExpression.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
		}
		return retVal;
    }
    
    public void processToken(ITokenDescriptor  pToken, String language)
    {
       if(mSubExpression != null)
       {
          mSubExpression.processToken(pToken, language);
       }
    }
}
