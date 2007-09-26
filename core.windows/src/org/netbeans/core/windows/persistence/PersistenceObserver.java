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


package org.netbeans.core.windows.persistence;


/**
 * Interface which defines observer of persistence changes.
 *
 * @author  Peter Zavadsky
 */
public interface PersistenceObserver {


    /** Handles adding mode to model.
     * @param modeConfig configuration data of added mode
     */
    public void modeConfigAdded(ModeConfig modeConfig);

    /** Handles removing mode from model.
     * @param modeName unique name of removed mode
     */
    public void modeConfigRemoved(String modeName);

    /** Handles adding tcRef to model. 
     * @param modeName unique name of parent mode.
     * @param tcRefConfig configuration data of added tcRef
     * @param tcRefNames array of tcIds to pass ordering of new tcRef,
     * if there is no ordering defined tcRef is appended to end of array
     */
    public void topComponentRefConfigAdded(
    String modeName, TCRefConfig tcRefConfig, String [] tcRefNames);
    
    /** Handles removing tcRef from model. 
     * @param tc_id unique id of removed tcRef
     */
    public void topComponentRefConfigRemoved(String tc_id);
    
    /** Handles adding group to model.
     * @param groupConfig configuration data of added group
     */
    public void groupConfigAdded(GroupConfig groupConfig);
    
    /** Handles removing group from model.
     * @param groupName unique name of removed group
     */
    public void groupConfigRemoved(String groupName);
    
    /** Handles adding tcGroup to model. 
     * @param groupName unique name of parent group
     * @param tcGroupConfig configuration data of added tcGroup
     */
    public void topComponentGroupConfigAdded(String groupName, TCGroupConfig tcGroupConfig);
    
    /** Handles removing tcGroup from model. 
     * @param groupName unique name of parent group.
     * @param tc_id unique id of removed tcGroup
     */
    public void topComponentGroupConfigRemoved(String groupName, String tc_id);
}

