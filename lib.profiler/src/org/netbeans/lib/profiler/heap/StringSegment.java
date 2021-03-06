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

package org.netbeans.lib.profiler.heap;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Tomas Hurka
 */
class StringSegment extends TagBounds {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final int UTF8CharsOffset;
    final int lengthOffset;
    final int stringIDOffset;
    final int timeOffset;
    Map stringIDMap;
    private HprofHeap hprofHeap;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    StringSegment(HprofHeap heap, long start, long end) {
        super(HprofHeap.STRING, start, end);

        int idSize = heap.dumpBuffer.getIDSize();
        hprofHeap = heap;
        timeOffset = 1;
        lengthOffset = timeOffset + 4;
        stringIDOffset = lengthOffset + 4;
        UTF8CharsOffset = stringIDOffset + idSize;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    String getString(long start) {
        HprofByteBuffer dumpBuffer = getDumpBuffer();

        if (start == -1) {
            return "<unknown string>"; // NOI18N
        }

        int len = dumpBuffer.getInt(start + lengthOffset);
        byte[] chars = new byte[len - dumpBuffer.getIDSize()];
        dumpBuffer.get(start + UTF8CharsOffset, chars);

        String s = "Error"; // NOI18N

        try {
            s = new String(chars, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        return s;
    }

    String getStringByID(long stringID) {
        return getString(getStringOffsetByID(stringID));
    }

    long getStringOffsetByID(long stringID) {
        Long startLong;

        if (stringIDMap == null) {
            stringIDMap = new HashMap(8000);

            long[] offset = new long[] { startOffset };

            while (offset[0] < endOffset) {
                long start = offset[0];
                long sID = readStringTag(offset);
                stringIDMap.put(new Long(sID), new Long(start));
            }
        }

        startLong = (Long) stringIDMap.get(new Long(stringID));

        if (startLong == null) {
            return -1;
        }

        return startLong.longValue();
    }

    private HprofByteBuffer getDumpBuffer() {
        HprofByteBuffer dumpBuffer = hprofHeap.dumpBuffer;

        return dumpBuffer;
    }

    private long readStringTag(long[] offset) {
        long start = offset[0];

        if (hprofHeap.readTag(offset) != HprofHeap.STRING) {
            return 0;
        }

        return getDumpBuffer().getID(start + stringIDOffset);
    }
}
