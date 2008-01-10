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

package org.netbeans.modules.cnd.modelimpl.uid;

import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.utils.cache.WeakSharedSet;

/**
 *
 * @author Vladimir Voskresensky
 */
public class UIDManager {
    private final UIDStorage storage;
    private static final int UID_MANAGER_DEFAULT_CAPACITY=1024;
    private static final int UID_MANAGER_DEFAULT_SLICED_NUMBER = 29;

    
    private static final UIDManager instance = new UIDManager();
    
    /** Creates a new instance of UIDManager */
    private UIDManager() {
        storage = new UIDStorage(UID_MANAGER_DEFAULT_SLICED_NUMBER, UID_MANAGER_DEFAULT_CAPACITY);
    }
    
    public static UIDManager instance() {
        return instance;
    }
    
    // we need exclusive copy of string => use "new String(String)" constructor
    private final String lock = new String("lock in UIDManager"); // NOI18N
    
    /**
     * returns shared uid instance equal to input one.
     *
     * @param uid - interested shared uid
     * @return the shared instance of uid
     * @exception NullPointerException If the <code>uid</code> parameter
     *                                 is <code>null</code>.
     */
    public final CsmUID getSharedUID(CsmUID uid) {
        if (uid == null) {
            throw new NullPointerException("null string is illegal to share"); // NOI18N
        }
        CsmUID outUID = null;
        synchronized (lock) {
            outUID = storage.getSharedUID(uid);
        }
        assert (outUID != null);
        assert (outUID.equals(uid));
        return outUID;
    }
    
    public final void dispose() {
        storage.dispose();
    }
    
    private static final class UIDStorage {
        private final WeakSharedSet<CsmUID>[] instances;
        private final int sliceNumber; // primary number for better distribution
        private final int initialCapacity;
        private UIDStorage(int sliceNumber, int initialCapacity) {
            this.sliceNumber = sliceNumber;
            this.initialCapacity = initialCapacity;
            instances = new WeakSharedSet[sliceNumber];
            for (int i = 0; i < instances.length; i++) {
                instances[i] = new WeakSharedSet<CsmUID>(initialCapacity);
            }
        }
        
        private WeakSharedSet<CsmUID> getDelegate(CsmUID uid) {
            int index = uid.hashCode() % sliceNumber;
            if (index < 0) {
                index += sliceNumber;
            }
            return instances[index];
        }
        
        public final CsmUID getSharedUID(CsmUID uid) {
            return getDelegate(uid).addOrGet(uid);
        }

        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size()>0) {
                    instances[i].clear();
                    instances[i].resize(initialCapacity);
                }
            }            
        }        
    }    
}
