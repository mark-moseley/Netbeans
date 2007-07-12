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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectSettingsValidator;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Key for ProjectSettingsValidator data
 * @author Vladimir Kvashin
 */
public class ProjectSettingsValidatorKey extends ProjectNameBasedKey {
    
    public ProjectSettingsValidatorKey(String project) {
	 super(project);
    }
    
    public ProjectSettingsValidatorKey(DataInput in) throws IOException {
	super(in);
    }
    
    public int getSecondaryDepth() {
	return 1;
    }
    
    public String toString() {
	return "ProjectSettingsValidatorKey " + getProjectName(); // NOI18N
    }
    
    public int getSecondaryAt(int level) {
	assert (level == 0);
	return KeyObjectFactory.KEY_PRJ_VALIDATOR_KEY;
    }
    
    public PersistentFactory getPersistentFactory() {
	return ProjectSettingsValidator.getPersistentFactory();
    }
    
}
