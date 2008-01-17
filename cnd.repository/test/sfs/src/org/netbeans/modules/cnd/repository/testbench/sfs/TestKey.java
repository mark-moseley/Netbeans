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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


class TestKey implements Key, SelfPersistent {
    
    private String key;
    
    public Behavior getBehavior() {
	return Behavior.Default;
    }
    
    public TestKey(String key) {
	this.key = key;
    }

    public TestKey(DataInput aStream) throws IOException {
        this(aStream.readUTF());
    }
    
    
    public String getAt(int level) {
	return key;
    }
    
    public int getDepth() {
	return 1;
    }
    
    public PersistentFactory getPersistentFactory() {
	return TestFactory.instance();
    }
    
    public int getSecondaryAt(int level) {
	return 0;
    }
    
    public int getSecondaryDepth() {
	return 0;
    }
    
    public int hashCode() {
	return key.hashCode();
    }
    
    public boolean equals(Object obj) {
	if (obj != null && TestKey.class == obj.getClass()) {
	    return this.key.equals(((TestKey) obj).key);
	}
	return false;
    }    

    public String toString() {
	return key;
    }

    public String getUnit() {
	return "Test"; // NOI18N
    }

    public void write(DataOutput output) throws IOException {
        output.writeUTF(key);
    }
   
}
