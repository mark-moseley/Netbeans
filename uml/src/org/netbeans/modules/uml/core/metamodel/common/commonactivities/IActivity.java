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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IActivity extends IBehavior
{
	/**
	 * property Kind
	*/
	public int getKind();

	/**
	 * property Kind
	*/
	public void setKind( /* ActivityKind */ int value );

	/**
	 * property IsSingleCopy
	*/
	public boolean getIsSingleCopy();

	/**
	 * property IsSingleCopy
	*/
	public void setIsSingleCopy( boolean value );

	/**
	 * method AddNode
	*/
	public void addNode( IActivityNode pNode );

	/**
	 * method RemoveNode
	*/
	public void removeNode( IActivityNode pNode );

	/**
	 * property Nodes
	*/
	public ETList<IActivityNode> getNodes();

	/**
	 * method AddEdge
	*/
	public void addEdge( IActivityEdge pEdge );

	/**
	 * method RemoveEdge
	*/
	public void removeEdge( IActivityEdge pEdge );

	/**
	 * property Edges
	*/
	public ETList<IActivityEdge> getEdges();

	/**
	 * method AddGroup
	*/
	public void addGroup( IActivityGroup pGroup );

	/**
	 * method RemoveGroup
	*/
	public void removeGroup( IActivityGroup pGroup );

	/**
	 * property Groups
	*/
	public ETList<IActivityGroup> getGroups();

	/**
	 * method AddPartition
	*/
	public void addPartition( IActivityPartition pPartition );

	/**
	 * method RemovePartition
	*/
	public void removePartition( IActivityPartition pPartition );

	/**
	 * property Partitions
	*/
	public ETList<IActivityPartition> getPartitions();

}
