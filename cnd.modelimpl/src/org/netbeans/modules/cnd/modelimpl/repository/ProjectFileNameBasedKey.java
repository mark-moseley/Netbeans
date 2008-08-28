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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 * A common ancestor for keys 
 * that are based on (project, file) pair
 */

/*package*/
abstract class ProjectFileNameBasedKey extends ProjectNameBasedKey {
    
    protected final int fileNameIndex;
    
    protected ProjectFileNameBasedKey(String prjName, String fileName) {
	super(prjName);
	assert fileName != null;
	this.fileNameIndex = KeyUtilities.getFileIdByName(unitIndex, fileName);
    }
    
    protected ProjectFileNameBasedKey(FileImpl file) {
	this(getProjectName(file), file.getAbsolutePath());
    }
    
    protected static String getProjectName(FileImpl file) {
	assert (file != null);
	ProjectBase prj = file.getProjectImpl(true);
	assert (prj != null);
	return prj == null ? "<No Project Name>" : prj.getUniqueName().toString();  // NOI18N
    }
    
    @Override
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	aStream.writeInt(fileNameIndex);
    }
    
    protected ProjectFileNameBasedKey(DataInput aStream) throws IOException {
	super(aStream);
	this.fileNameIndex = aStream.readInt();
    }
    
    @Override
    public int hashCode() {
	int key = super.hashCode();
	key = 17*key + fileNameIndex;
	return key;
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	ProjectFileNameBasedKey other = (ProjectFileNameBasedKey)obj;
	
	return this.fileNameIndex==other.fileNameIndex;
    }
    
    protected String getFileName() {
	return KeyUtilities.getFileNameById(unitIndex, this.fileNameIndex);
    }
    
    /** A special safe method, mainly for toString / tracing */
    protected String getFileNameSafe() {
	return KeyUtilities.getFileNameByIdSafe(unitIndex, this.fileNameIndex);
    }
    
    @Override
    public int getDepth() {
	assert super.getDepth() == 0;
	return 1;
    }
    
    @Override
    public CharSequence getAt(int level) {
	assert super.getDepth() == 0 && level < getDepth();
	return getFileName();
    }
}
