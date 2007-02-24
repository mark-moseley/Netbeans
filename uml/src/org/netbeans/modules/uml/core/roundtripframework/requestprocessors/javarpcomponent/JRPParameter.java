/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File       : JRPParameter.java
 * Created on : Oct 29, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IParameterDirectionKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;

/**
 * @author Aztec
 */
public class JRPParameter implements IJRPParameter
{
    private String m_Name;
    private String m_Type;
    private IClassifier m_TypeClass = null;
    private IStrings    m_Ranges = null;
    private int m_Direction;
    
    
    public JRPParameter()
    {
        m_Name = null;
        m_Type = null;
        m_Direction = IParameterDirectionKind.PDK_IN;
    }

    public JRPParameter(String name, String type, int dir )
    {
        setName(name);
        setType(type);
        setDirection(dir);
    }

    public JRPParameter(String name, IClassifier type, int dir )
    {
        setName(name);
        setTypeClass(type);
        setDirection(dir);
    }

    public JRPParameter(IJRPParameter copy)
    {
        setName(copy.getName());
        setType(copy.getType());
        setTypeClass(copy.getTypeClass());        
        setRanges(copy.getRanges());
    }
        
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#addRange(java.lang.String, java.lang.String)
     */
    public void addRange(String lower, String upper)
    {
        if(m_Ranges == null)
        {
            m_Ranges = new Strings();
        }
        m_Ranges.add(lower);
        m_Ranges.add(upper);
    }

    public IStrings getRanges()
    {
        return m_Ranges;
    }
    
    public void setRanges(IStrings ranges)
    {
        m_Ranges = ranges;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#createParameter(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public IParameter createParameter(IOperation pOp)
    {
        if(pOp == null) return null;

        IParameter pRetVal = null;
        // Since we can have either a classifier as a type, or
        // just a string, we have to choose. We choose to use the
        // classifier when we have it.

        if (getDirection() == IParameterDirectionKind.PDK_RESULT )
        {
            pRetVal = pOp.getReturnType();
        }
        else
        {
            if (m_TypeClass != null)
            {
                pRetVal = pOp.createParameter2(m_TypeClass, m_Name);
            }
            else
            {
                pRetVal = pOp.createParameter(m_Type, m_Name);
            }
        }

        if (m_Ranges != null && pRetVal != null)
        {
            int count = m_Ranges.getCount();
            int i = 0;
            IMultiplicity pMult = pRetVal.getMultiplicity();
            if (pMult != null)
            {
                while(i < count)
                {
                    IMultiplicityRange pRange = pMult.createRange();
                    if(pRange != null)
                    {
                        String lower = m_Ranges.item(i);
                        String upper = null;
                        i++;
                        if(i < count)
                        {                            
                            upper = m_Ranges.item(i);
                        }
                        pRange.setRange(lower, upper);
                        pMult.addRange(pRange);
                        i++;
                    }
                }
                pRetVal.setMultiplicity(pMult);
            }
        }
        return pRetVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#getDirection()
     */
    public int getDirection()
    {
        return m_Direction;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#getName()
     */
    public String getName()
    {
        return m_Name;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#getType()
     */
    public String getType()
    {
        return m_Type;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#getTypeClass()
     */
    public IClassifier getTypeClass()
    {
        return m_TypeClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#setDirection(int)
     */
    public void setDirection(int paramDirKind)
    {
        m_Direction = paramDirKind;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#setName(java.lang.String)
     */
    public void setName(String name)
    {
        m_Name = name;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#setType(java.lang.String)
     */
    public void setType(String type)
    {
        m_Type = type;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJRPParameter#setTypeClass(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setTypeClass(IClassifier pType)
    {
        m_TypeClass = pType;
    }
}
