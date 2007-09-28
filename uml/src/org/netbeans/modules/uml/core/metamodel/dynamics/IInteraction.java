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


package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IInteraction extends IInteractionOperand, IBehavior
{
	/**
	 * Adds a connector to the internal collection of connectors.
	*/
	public void addConnector( IConnector connector );

	/**
	 * Removes the specified connector.
	*/
	public void removeConnector( IConnector connector );

	/**
	 * Retrieves the collection of connectors.
	*/
	public ETList<IConnector> getConnectors();

	/**
	 * Adds a formal gate to this interaction.
	*/
	public void addGate( IGate gate );

	/**
	 * Removes the specified gate from the interaction.
	*/
	public void removeGate( IGate gate );

	/**
	 * Retrieves the collection of gates owned by this interaction.
	*/
	public ETList<IGate> getGates();

	/**
	 * Adds a message to this interaction.
	*/
	public void addMessage( IMessage message );

	/**
	 * Removes the specified message from this interaction.
	*/
	public void removeMessage( IMessage message );

	/**
	 * Retrieves the collection of messages owned by this interaction.
	*/
	public ETList<IMessage> getMessages();

	/**
	 * Adds a life line to this interaction.
	*/
	public void addLifeline( ILifeline line );

	/**
	 * Removes the specified lifeline from this interaction.
	*/
	public void removeLifeline( ILifeline line );

	/**
	 * Retrieves the collection of lifelines owned by this interaction.
	*/
	public ETList<ILifeline> getLifelines();

	/**
	 * Adds an EventOccurrence to the internal collection of occurrences.
	*/
	public void addEventOccurrence( IEventOccurrence pOcc );

	/**
	 * Removes the specified EventOccurrence.
	*/
	public void removeEventOccurrence( IEventOccurrence pOcc );

	/**
	 * Retrieves the collection of EventOccurrences.
	*/
	public ETList<IEventOccurrence> getEventOccurrences();

	/**
	 * Creates a new Message, specifically oriented towards the invocation of the passed in Operation.
	*/
	public IMessage createMessage( IElement toElement, IInteractionFragment toOwner, IOperation oper, /* MessageKind */ int kind );

	/**
	 * Creates and then Inserts a new Message before the message passed in, specifically oriented towards the invocation of the passed in Operation.
	*/
	public IMessage insertMessage( IMessage fromBeforeMessage, IElement toElement, IInteractionFragment toOwner, IOperation oper, /* MessageKind */ int kind );

	/**
	 * Inserts a message into the list of messages before the specified message
	*/
	public void insertMessageBefore( IMessage message, IMessage messageBefore );

	/**
	 * Maintains the GeneralOrdering collection.
	*/
	public void handleMessageAdded( IMessage message, IMessage messageBefore );

	/**
	 * Maintains the GeneralOrdering collection.
	*/
	public void handleMessageDeleted( IMessage message );

	/**
	 * Retrieves the collection of EventOccurrences.
	*/
	public ETList<IGeneralOrdering> getGeneralOrderings();

	/**
	 * Resets all the numbers of the messages below the input message
	*/
	public void resetAutoNumbers( IMessage pMessage );
}