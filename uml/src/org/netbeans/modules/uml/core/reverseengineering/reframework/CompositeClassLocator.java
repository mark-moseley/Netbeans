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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class CompositeClassLocator implements ICompositeClassLocator
{
    private List<IClassLocator> m_Locators = new ArrayList<IClassLocator>();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.ICompositeClassLocator#addLocator(org.netbeans.modules.uml.core.reverseengineering.reframework.IClassLocator)
     */
    public void addLocator(IClassLocator newLocator)
    {
        if (newLocator != null && !m_Locators.contains(newLocator))
            m_Locators.add(newLocator);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IClassLocator#locateFileForClass(java.lang.String, java.lang.String)
     */
    public ETPairT<String,String> locateFileForClass(
            String pack, 
            String className,
            ETList<IDependencyEvent> deps)
    {
        for (int i = 0, count = m_Locators.size(); i < count; ++i)
        {
            ETPairT<String,String> file = m_Locators.get(i).locateFileForClass(pack, className, deps);

            if(file != null)
            {                    
               if (file.getParamOne() != null && file.getParamTwo() != null)
                   return file;
            }
        }
        return new ETPairT<String,String>(null, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IClassLocator#locateFile(java.lang.String)
     */
    public String locateFile(String filename)
    {
        for (int i = 0, count = m_Locators.size(); i < count; ++i)
        {
            String file = m_Locators.get(i).locateFile(filename);
            if (file != null)
                return file;
        }
        return null;
    }
}