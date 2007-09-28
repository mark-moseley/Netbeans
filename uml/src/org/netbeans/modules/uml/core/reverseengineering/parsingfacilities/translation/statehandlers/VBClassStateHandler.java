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
 * File       : VBClassStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class VBClassStateHandler extends ClassStateHandler
{

    public VBClassStateHandler(
        String language,
        String packageName,
        boolean isInner)
    {
        super(language, packageName, isInner);
    }
    
    public String cleanseComment(String origComment) 
    {
        String retVal = ""; 


        String test = origComment.substring(0, 3);
        if("rem".equalsIgnoreCase(test))
        {
            retVal += origComment.substring(3);
        }   
        else
        {
            // The only other comment style is to start the line with a '
            retVal += origComment.substring(1);
        }
        return retVal;   
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        
        if("Variable Definition".equals(stateName))
        {
            retVal = new VBAttributeStateHandler(language);
        }
        else if(("Method Definition".equals(stateName)) ||           
                ("Method Declaration".equals(stateName)) )
        {
            retVal = new VBOperationStateHandler(language, 
                                                    stateName, 
                                                    OperationStateHandler.OPERATION, 
                                                    isForceAbstractMethods());
        } 
        else if("Structure Declaration".equals(stateName))
        {

            retVal = new StructureStateHandler(language, "", true);
        }
        else if("Enumeration Declaration".equals(stateName))
        {

            retVal= new EnumStateHandler(language, "", true);
        }
        else
        {
            retVal = super.createSubStateHandler(stateName, language);
        }

        if((retVal != null) && (retVal != this))
        {
            Node pClassNode = getDOMNode();

            if(pClassNode != null)
            {
                retVal.setDOMNode(pClassNode);
            }
        }
        return retVal;
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        super.processToken(pToken, language);
    }
    
    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        long line = pToken.getLine();
        long col = pToken.getColumn();
        long pos = pToken.getPosition();
        long length = pToken.getLength();
        
        String nameString = pToken.getValue();

        if(nameString.indexOf("\"") != -1)
        {
           nameString = nameString.substring(1, (int)length - 2);         
        }

        setNodeAttribute("name", nameString);

        createTokenDescriptor("Name", 
                                line, 
                                col + 1, 
                                pos + 1, 
                                nameString, 
                                nameString.length());
    }

}
