/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 *
 * @author Tomas Zezula
 */
public class LMListener {

    //@GuardedBy(LMListener.class)
    private static MemoryPoolMXBean cachedPool;

    private MemoryPoolMXBean pool;

    private static final float DEFAULT_HEAP_LIMIT = 0.7f;

    private final float heapLimit;

    public LMListener () {
        this (DEFAULT_HEAP_LIMIT);
    }

    public LMListener (final float heapLimit) {
        this.heapLimit = heapLimit;
        this.pool = findPool();
        assert pool != null;
    }

    public float getHeapLimit () {
        return this.heapLimit;
    }
    
    public boolean isLowMemory () {
        if (this.pool != null) {
            final MemoryUsage usage = this.pool.getUsage();
            if (usage != null) {
                long used = usage.getUsed();
                long max = usage.getMax();
                return used > max * heapLimit;
            }
        }
        return false;
    }

    private static synchronized MemoryPoolMXBean findPool () {
        if (cachedPool == null || !cachedPool.isValid()) {
            final List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean pool : pools) {
                if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                    cachedPool = pool;
                    break;
                }
            }
            assert cachedPool != null : dumpMemoryPoolMXBean (pools);
        }
        return cachedPool;
    }

    private static String dumpMemoryPoolMXBean (List<MemoryPoolMXBean> pools) {
        StringBuilder sb = new StringBuilder ();
        for (MemoryPoolMXBean pool : pools) {
            sb.append(String.format("Pool: %s Type: %s TresholdSupported: %s\n", pool.getName(), pool.getType(), pool.isUsageThresholdSupported() ? Boolean.TRUE : Boolean.FALSE));
        }
        sb.append('\n');
        return sb.toString();
    }

}
