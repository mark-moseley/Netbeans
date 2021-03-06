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
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;


/**
 * File and offset -based key
 */

/*package*/
abstract class OffsetableKey extends ProjectFileNameBasedKey implements Comparable {
    
    private final int startOffset;
    private final int endOffset;
    
    private final char kind;
    private final CharSequence name;
    
    protected OffsetableKey(CsmOffsetable obj, String kind, CharSequence name) {
	super((FileImpl) obj.getContainingFile());
	this.startOffset = obj.getStartOffset();
	this.endOffset = obj.getEndOffset();
        assert kind.length()==1;
	this.kind = kind.charAt(0);
	this.name = name;
    }
    
    @Override
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	aStream.writeInt(this.startOffset);
	aStream.writeInt(this.endOffset);
	aStream.writeChar(this.kind);
	assert this.name != null;
	aStream.writeUTF(this.name.toString());
    }
    
    protected OffsetableKey(DataInput aStream) throws IOException {
	super(aStream);
	this.startOffset = aStream.readInt();
	this.endOffset = aStream.readInt();
	this.kind = aStream.readChar();
	this.name = NameCache.getManager().getString(aStream.readUTF());
	assert this.name != null;
    }
    
    @Override
    public String toString() {
	return name + "[" + kind + " " + startOffset + "-" + endOffset + "] {" + getFileNameSafe() + "; " + getProjectName() + "}"; // NOI18N
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	OffsetableKey other = (OffsetableKey)obj;
	return  this.startOffset == other.startOffset &&
		this.endOffset == other.endOffset &&
		this.kind == other.kind &&
		this.name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
	int retValue;
	
	retValue = 17*super.hashCode() + name.hashCode();
	retValue = 17*retValue + kind;
	retValue = 17*super.hashCode() + startOffset;
	retValue = 17*retValue + endOffset;
	return retValue;
    }
    
    public int compareTo(Object o) {
	if (this == o) {
	    return 0;
	}
	OffsetableKey other = (OffsetableKey)o;
	assert (kind ==other.kind);
	//FUXUP assertion: unit and file tables should be deserialized before files deserialization.
	//instead compare indexes.
	//assert (this.getFileName().equals(other.getFileName()));
	//assert (this.getProjectName().equals(other.getProjectName()));
	assert (this.unitIndex == other.unitIndex);
	assert (this.fileNameIndex == other.fileNameIndex);
	int ofs1 = this.startOffset;
	int ofs2 = other.startOffset;
	if (ofs1 == ofs2) {
	    return 0;
	} else {
	    return (ofs1 - ofs2);
	}
    }
    
    @Override
    public int getDepth() {
	return super.getDepth() + 2;
    }
    
    @Override
    public CharSequence getAt(int level) {
	int superDepth = super.getDepth();
	if (level < superDepth) {
	    return super.getAt(level);
	} else {
	    switch(level - superDepth) {
		case 0:
		    return new String(new char[]{this.kind});
		case 1:
		    return this.name;
		default:
		    
		    throw new IllegalArgumentException("not supported level" + level); // NOI18N
	    }
	}
    }
    
    public int getSecondaryDepth() {
	return 2;
    }
    
    public int getSecondaryAt(int level) {
	switch(level) {
	    case 0:
		
		return this.startOffset;
	    case 1:
		
		return this.endOffset;
	    default:
		
		throw new IllegalArgumentException("not supported level" + level); // NOI18N
	}
    }
}
