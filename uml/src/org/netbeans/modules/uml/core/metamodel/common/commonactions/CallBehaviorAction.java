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
 * File       : CallBehaviorAction.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.BehaviorInvocation;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class CallBehaviorAction
    extends PrimitiveAction
    implements ICallBehaviorAction
{
    private IBehaviorInvocation behaviorInvoc = new BehaviorInvocation();
    
    public CallBehaviorAction()
    {
        behaviorInvoc = new BehaviorInvocation();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        behaviorInvoc.setNode(n);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICallBehaviorAction#getIsSynchronous()
     */
    public boolean getIsSynchronous()
    {
        return getBooleanAttributeValue("isSynchronous", true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICallBehaviorAction#setIsSynchronous(boolean)
     */
    public void setIsSynchronous(boolean isSynchronous)
    {
        setBooleanAttributeValue("isSynchronous", isSynchronous);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:CallBehaviorAction", doc, node);
    }


    ///////// IBehaviorInvocation delegate methods /////////
    public IBehavior getBehavior()
    {
        return behaviorInvoc.getBehavior();
    }

    public ETList<IPin> getResults()
    {
        return behaviorInvoc.getResults();
    }

    public void addBehaviorArgument(IPin pin)
    {
        behaviorInvoc.addBehaviorArgument(pin);
    }

    public void addResult(IPin pin) 
    {
        behaviorInvoc.addResult(pin);
    }

    public void removeBehaviorArgument(IPin pin) 
    {
        behaviorInvoc.removeBehaviorArgument(pin);
    }
    
    public ETList<IPin> getBehaviorArguments()
    {
        return behaviorInvoc.getBehaviorArguments();
    }

    public void removeResult(IPin pin) 
    {
        behaviorInvoc.removeResult(pin);
    }

    public void setBehavior(IBehavior behavior) 
    {
        behaviorInvoc.setBehavior(behavior);
    }
}