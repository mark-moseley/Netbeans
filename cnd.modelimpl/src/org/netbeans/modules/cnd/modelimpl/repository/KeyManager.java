/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.cache.WeakSharedSet;

/**
 *
 * @author Vladimir Voskresensky
 * @author Alexander Simon
 */
public class KeyManager {

    private final KeyStorage storage;
    private static final int KEY_MANAGER_DEFAULT_CAPACITY;
    private static final int KEY_MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = Runtime.getRuntime().availableProcessors();
        if (nrProc <= 4) {
            KEY_MANAGER_DEFAULT_SLICED_NUMBER = 32;
            KEY_MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            KEY_MANAGER_DEFAULT_SLICED_NUMBER = 128;
            KEY_MANAGER_DEFAULT_CAPACITY = 128;
        }
    }
    private static final KeyManager instance = new KeyManager();

    /** Creates a new instance of KeyManager */
    private KeyManager() {
        storage = new KeyStorage(KEY_MANAGER_DEFAULT_SLICED_NUMBER, KEY_MANAGER_DEFAULT_CAPACITY);
    }

    public static KeyManager instance() {
        return instance;
    }
    private static final class Lock {}
    private final Object lock = new Lock();

    /**
     * returns shared uid instance equal to input one.
     *
     * @param uid - interested shared uid
     * @return the shared instance of uid
     * @exception NullPointerException If the <code>uid</code> parameter
     *                                 is <code>null</code>.
     */
    public final Key getSharedUID(Key key) {
        if (key == null) {
            throw new NullPointerException("null string is illegal to share"); // NOI18N
        }
        Key outKey = null;
        synchronized (lock) {
            outKey = storage.getSharedUID(key);
        }
        assert (outKey != null);
        assert (outKey.equals(key));
        return outKey;
    }

    public final void dispose() {
        storage.dispose();
    }

    private static final class KeyStorage {

        private final WeakSharedSet<Key>[] instances;
        private final int segmentMask; // mask
        private final int initialCapacity;

        private KeyStorage(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            this.initialCapacity = initialCapacity;
            @SuppressWarnings("unchecked")
            WeakSharedSet<Key>[] ar = new WeakSharedSet[ssize];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = new WeakSharedSet<Key>(initialCapacity);
            }
            instances = ar;
        }

        private WeakSharedSet<Key> getDelegate(Key key) {
            int index = key.hashCode() & segmentMask;
            return instances[index];
        }

        public final Key getSharedUID(Key key) {
            return getDelegate(key).addOrGet(key);
        }

        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size() > 0) {
                    instances[i].clear();
                    instances[i].resize(initialCapacity);
                }
            }
        }
    }
}
