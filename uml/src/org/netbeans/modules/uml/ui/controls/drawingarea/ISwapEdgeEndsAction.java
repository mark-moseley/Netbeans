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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;

/**
 * @author KevinM
 *
 */
public interface ISwapEdgeEndsAction extends IDelayedAction {
	//The id of the new source node
	public int getNewSourceEndID();
	//The id of the new source node
	public void setNewSourceEndID(int endID);

	//The id of the new target node
	public int getNewTargetEndID();
	//The id of the new target node
	public void setNewTargetEndID(int endID);

	//This is the edge that needs to have its source and targets swapped
	public IETEdge getEdgeToSwap();
	// This is the edge that needs to have its source and targets swapped
	public void setEdgeToSwap(IETEdge pEdge);

	//Executes this simple action
	public void execute();
}
