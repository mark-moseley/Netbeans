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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.eventframework.IEventDispatchHelper;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

/**
 *
 * @author Trey Spiva
 */
public interface IWSProjectEventDispatcher extends IEventDispatchHelper
{
	/**
	 * method DispatchWSProjectPreCreate
	 */
	public IResultCell dispatchWSProjectPreCreate( IWorkspace space, String projName )
	throws InvalidArguments;

	/**
	 * method DispatchWSProjectCreated
	 */
	public void dispatchWSProjectCreated( IWSProject wsProject ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreOpen
	 */
	public boolean dispatchWSProjectPreOpen( IWorkspace space, String projectName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectOpened
	 */
	public void dispatchWSProjectOpened( IWSProject wsProject ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreRemove
	 */
	public boolean dispatchWSProjectPreRemove( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectRemoved
	 */
	public void dispatchWSProjectRemoved( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreInsert
	 */
	public boolean dispatchWSProjectPreInsert( IWorkspace space, String projectName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectInserted
	 */
	public void dispatchWSProjectInserted( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreRename
	 */
	public boolean dispatchWSProjectPreRename( IWSProject project, String newName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectRenamed
	 */
	public void dispatchWSProjectRenamed( IWSProject project, String oldName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreClose
	 */
	public boolean dispatchWSProjectPreClose( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectClosed
	 */
	public void dispatchWSProjectClosed( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreSave
	 */
	public boolean dispatchWSProjectPreSave( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectSaved
	 */
	public void dispatchWSProjectSaved( IWSProject project ) throws InvalidArguments;

}
