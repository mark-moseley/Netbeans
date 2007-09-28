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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.TokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class XMLTokenDescriptor extends TokenDescriptor
        implements IXMLTokenDescriptor
{
    private Node m_TokenDescriptorsNode;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IXMLTokenDescriptor#getTokenDescriptorNode()
     */
    public Node getTokenDescriptorNode()
    {
        return m_TokenDescriptorsNode;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IXMLTokenDescriptor#setTokenDescriptorNode(org.dom4j.Node)
     */
    public void setTokenDescriptorNode(Node newVal)
    {
        m_TokenDescriptorsNode = newVal;
    }
    
    /**
     * Retrieves an int attribute from the XML node that represents the descriptor.
     * @param name [in] The attribute to retrieve.
     * @param pVal [out] The value.
     */
    protected int getIntAttribute(String name)
    {
        return XMLManip.getAttributeIntValue(m_TokenDescriptorsNode, name);
    }
    
    /**
     * Sets an int attribute to the XML node that represents the descriptor.
     * @param name [in] The attribute to set.
     * @param pVal [out] The value.
     */
    protected void setIntAttribute(String name, int val)
    {
        XMLManip.setAttributeValue(m_TokenDescriptorsNode, name, 
                String.valueOf(val));
    }
    
    protected String getAttribute(String name)
    {
        return XMLManip.getAttributeValue(m_TokenDescriptorsNode, name);
    }
    
    protected void setAttribute(String name, String value)
    {
        XMLManip.setAttributeValue(m_TokenDescriptorsNode, name, value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setLine(int)
     */
    public void setLine(int value)
    {
        setIntAttribute("line", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getLine()
     */
    public int getLine()
    {
        return getIntAttribute("line");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setColumn(int)
     */
    public void setColumn(int value)
    {
        setIntAttribute("column", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getColumn()
     */
    public int getColumn()
    {
        return getIntAttribute("column");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setPosition(int)
     */
    public void setPosition(int value)
    {
        setIntAttribute("position", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getPosition()
     */
    public long getPosition()
    {
        return getIntAttribute("position");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setType(java.lang.String)
     */
    public void setType(String value)
    {
        setAttribute("type", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getType()
     */
    public String getType()
    {
        return getAttribute("type");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        setAttribute("value", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getValue()
     */
    public String getValue()
    {
        return getAttribute("value");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setLength(int)
     */
    public void setLength(int value)
    {
        setIntAttribute("length", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getLength()
     */
    public int getLength()
    {
        return getIntAttribute("length");
    }
}