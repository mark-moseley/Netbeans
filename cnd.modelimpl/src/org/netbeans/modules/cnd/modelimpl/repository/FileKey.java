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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/*package*/
final class FileKey extends ProjectFileNameBasedKey {
    
    public FileKey(CsmFile file) {
	super(ProjectFileNameBasedKey.getProjectName(file), file.getAbsolutePath());
    }
    
    /*package*/ FileKey(DataInput aStream) throws IOException {
	super(aStream);
    }
    
    public String toString() {
	return "FileKey (" + getProjectName() + ", " + getFileNameSafe() + ")"; // NOI18N
    }
    
    public PersistentFactory getPersistentFactory() {
	return CsmObjectFactory.instance();
    }
    
    public int getSecondaryDepth() {
	return 1;
    }
    
    public int getSecondaryAt(int level) {
	assert level == 0;
	return KeyObjectFactory.KEY_FILE_KEY;
    }
}
