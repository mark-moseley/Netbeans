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

package org.netbeans.modules.uml.propertysupport;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.support.umlsupport.ICustomValidator;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

/**
 *
 * @author Sheryl
 */
public class LeafPropertyValidator implements ICustomValidator
{
    
    /** Creates a new instance of LeafPropertyValidator */
    public LeafPropertyValidator()
    {
    }
    
    public boolean validate(Object pDisp, String fieldName, String fieldValue)
    {
        
        if (pDisp instanceof IPropertyElement)
        {
            IPropertyElement pEle = (IPropertyElement)pDisp;
            if (pEle.getElement() instanceof IClassifier)
            {
                ETList<IGeneralization> list = ((IClassifier)pEle.
                        getElement()).getSpecializations();
                for (IGeneralization sub: list)
                {
                    if (sub.getSpecific() != null)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    
    
    public void whenValid(Object pDisp)
    {
        
    }
    
    
    public void whenInvalid(Object pDisp)
    {
    }
}
