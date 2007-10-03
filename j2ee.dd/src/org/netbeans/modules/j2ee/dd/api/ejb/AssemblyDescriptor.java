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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface AssemblyDescriptor extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {

        public static final String SECURITY_ROLE = "SecurityRole";	// NOI18N
	public static final String METHOD_PERMISSION = "MethodPermission";	// NOI18N
	public static final String CONTAINER_TRANSACTION = "ContainerTransaction";	// NOI18N
	public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
	public static final String EXCLUDE_LIST = "ExcludeList";	// NOI18N
        
        public ContainerTransaction[] getContainerTransaction();
        
        public ContainerTransaction getContainerTransaction(int index);
        
        public void setContainerTransaction(ContainerTransaction[] value);
        
        public void setContainerTransaction(int index, ContainerTransaction value);
        
	public int sizeContainerTransaction();

	public int addContainerTransaction(org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction value);

	public int removeContainerTransaction(org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction value);
        
        public ContainerTransaction newContainerTransaction();
        
        public MethodPermission[] getMethodPermission();
        
        public MethodPermission getMethodPermission(int index);
        
        public void setMethodPermission(MethodPermission[] value);
        
        public void setMethodPermission(int index, MethodPermission value);
        
	public int addMethodPermission(org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission value);

	public int sizeMethodPermission();

	public int removeMethodPermission(org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission value);
        
        public MethodPermission newMethodPermission();
        
        public SecurityRole[] getSecurityRole();
        
        public SecurityRole getSecurityRole(int index);
        
        public void setSecurityRole(SecurityRole[] value);
        
        public void setSecurityRole(int index, SecurityRole value);
     
	public int sizeSecurityRole();

	public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);
        
        public SecurityRole newSecurityRole();

        public void setExcludeList(ExcludeList value);

        public ExcludeList getExcludeList();
        
        public ExcludeList newExcludeList();
        
        //2.1
        public MessageDestination[] getMessageDestination() throws VersionNotSupportedException;
        
        public MessageDestination getMessageDestination(int index) throws VersionNotSupportedException;
        
        public void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException;
        
        public void setMessageDestination(int index, MessageDestination value) throws VersionNotSupportedException;
        
        public int sizeMessageDestination() throws VersionNotSupportedException;

	public int removeMessageDestination(MessageDestination value) throws VersionNotSupportedException;

	public int addMessageDestination(MessageDestination value) throws VersionNotSupportedException;
        
        public MessageDestination newMessageDestination() throws VersionNotSupportedException;
        
}
 

