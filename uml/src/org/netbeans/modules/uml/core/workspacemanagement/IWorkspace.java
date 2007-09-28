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

import org.dom4j.Document;
import org.dom4j.Element;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IWorkspace extends IWSProject
{
	/**
	 * Sets / Gets the name of this element.
	 */
	public String getName();

	/**
	 * Sets / Gets the name of this element.
	 */
	public void setName( String value );

	/**
	 * Sets / Gets the owner of this element.
	 */
	public IWSProject getOwner();

	/**
	 * Sets / Gets the owner of this element.
	 */
	public void setOwner( IWSProject value );

	/**
	 * Sets / Gets the DOM element behind this WSElement.
	 */
	public Element getElement();

	/**
	 * Sets / Gets the DOM element behind this WSElement.
	 */
	public void setElement( Element value );

	/**
	 * property TwoPhaseCommit
	 */
	public ITwoPhaseCommit getTwoPhaseCommit();

	/**
	 * property TwoPhaseCommit
	 */
	public void setTwoPhaseCommit( ITwoPhaseCommit value );

	/**
	 * Sets / Gets the dirty flag of this element.
	 */
	public boolean isDirty();

	/**
	 * Sets / Gets the dirty flag of this element.
	 */
	public void setIsDirty( boolean value );

	/**
	 * Saves this WSElement
	 * 
	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element. 
	 */
	public void save( String location ) throws WorkspaceManagementException;

	/**
	 * Sets / Gets the data for this element.
	 */
	public String getData();

	/**
	 * Sets / Gets the data for this element.
	 */
	public void setData( String value );

	/**
	 * The documentation specific to this WSElement.
	 */
	public String getDocumentation();

	/**
	 * The documentation specific to this WSElement.
	 */
	public void setDocumentation( String value );		

	/**
	 * Creates a new WSProject.
	 */
	public IWSProject createWSProject( String BaseDirectory, String Name )
		throws WorkspaceManagementException;

	/**
	 * Retrieves the collection of WSProjects currently in this Workspace.
	 */
	public ETList<IWSProject> getWSProjects() throws WorkspaceManagementException;

	/**
	 * Retrieves a WSProject that matches the passed in name.
	 */
	public IWSProject getWSProjectByName( String projName );

	/**
	 * Sets / Gets the DOM document behind this Workspace.
	 */
	public Document getDocument();

	/**
	 * Sets / Gets the DOM document behind this Workspace.
	 */
	public void setDocument( Document value ) throws WorkspaceManagementException;

	/**
	 * Opens the WSProject that matches the passed in name.
	 */
	public IWSProject openWSProjectByName( String projName )
	  throws WorkspaceManagementException;

	/**
	 * Closes the WSProject with the matching name.
	 */
	public void closeWSProjectByName( String projName, boolean saveFirst )
		throws WorkspaceManagementException;

	/**
	 * Closes all open WSProjects.
	 */
	public void closeAllProjects( boolean saveFirst );

	/**
	 * Removes the WSProject that matches the passed in name from the Workspace.
	 */
	public void removeWSProjectByName( String projName )
		throws WorkspaceManagementException;

	/**
	 * Removes the WSProject from the Workspace.
	 */
	public void removeWSProject( IWSProject wsProject )
		throws WorkspaceManagementException;

	/**
	 * Validates that the path specified is unique within this entire Workspace. 
	 */
	public boolean verifyUniqueElementLocation( String location )
		throws WorkspaceManagementException;

	/**
	 * Removes the WSElement by location. 
	 * 
	 * @param location[in]	The absolute path to the WSElement
	 * @return <b>true> if the element was successfully removed. <false> if it
	 *         was not found.
	 */
	public boolean removeWSElementByLocation( String location )
	throws WorkspaceManagementException;

	/**
	 * Opens the WSProject that matches the passed in data string.
	 */
	public IWSProject openWSProjectByData( String dataStr )
		throws WorkspaceManagementException;

	/**
	 * Opens the WSProject by looking for a WSElement that contains the location
	 * of the project 
	 */
	public IWSProject openWSProjectByLocation( String locationStr )
		throws WorkspaceManagementException;

}

