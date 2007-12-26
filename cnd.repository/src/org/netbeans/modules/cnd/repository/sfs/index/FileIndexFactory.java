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

package org.netbeans.modules.cnd.repository.sfs.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Nickolay Dalmatov
 */
public class FileIndexFactory extends AbstractObjectFactory {
    private static FileIndexFactory theFactory;
    private static final Object lock = new Object();
    
    /** Creates a new instance of FileIndexFactory */
    protected FileIndexFactory() {
    }
    
    public static FileIndexFactory getDefaultFactory() {
        synchronized (lock) {
            if (theFactory == null) {
                theFactory = new FileIndexFactory();
            }
        }
        return theFactory;
    }
    
   public void writeIndex(final FileIndex anIndex, final DataOutput aStream) throws IOException {
        assert anIndex instanceof SelfPersistent;
        super.writeSelfPersistent((SelfPersistent)anIndex, aStream);
    }
    
    public FileIndex readIndex(final DataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out instanceof FileIndex;
        return (FileIndex)out;
    }    

    protected int getHandler(final Object object) {
        final int aHandle;
        
        if (object instanceof SimpleFileIndex) {
            aHandle = FILE_INDEX_SIMPLE;
        } else if (object instanceof CompactFileIndex) {
            aHandle = FILE_INDEX_COMPACT;
        } else {
            throw new IllegalArgumentException("The Index is an instance of the unknown final class " + object.getClass().getName());  // NOI18N
        }
        return aHandle;
    }

    protected SelfPersistent createObject(final int handler, final DataInput stream) throws IOException {
        final SelfPersistent anIndex;
        
        switch (handler) {
            case FILE_INDEX_SIMPLE:
                anIndex = new SimpleFileIndex(stream);
                break;
            case FILE_INDEX_COMPACT:
                anIndex = new CompactFileIndex(stream);
                break;
            default:
                throw new IllegalArgumentException("Unknown hander for index was provided: " + handler);  // NOI18N
        }
        
        return anIndex;
    }
    
    private static final int FIRST_INDEX        = 0;
    private static final int FILE_INDEX_SIMPLE  = FIRST_INDEX;
    private static final int FILE_INDEX_COMPACT = FILE_INDEX_SIMPLE + 1;
    public static final int LAST_INDEX          = FILE_INDEX_COMPACT;
    
}
