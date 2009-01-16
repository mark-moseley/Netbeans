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

import java.util.Iterator;
import java.util.Properties;


/**
 *
 * @author Tomas Hurka
 */
class HprofProxy {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofProxy() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    static Properties getProperties(Instance propertiesInstance) {
        Instance defaultsObj = (Instance) propertiesInstance.getValueOfField("defaults"); // NOI18N
        ObjectArrayDump entriesObj = (ObjectArrayDump) propertiesInstance.getValueOfField("table"); // NOI18N
        Iterator enIt = entriesObj.getValues().iterator();
        Properties props;

        if (defaultsObj != null) {
            props = new Properties(getProperties(defaultsObj));
        } else {
            props = new Properties();
        }
        while (enIt.hasNext()) {
            Instance entry = (Instance) enIt.next();

            for (; entry != null; entry = (Instance) entry.getValueOfField("next")) { // NOI18N
                Instance key = (Instance) entry.getValueOfField("key"); // NOI18N
                Instance val = (Instance) entry.getValueOfField("value"); // NOI18N

                if (key != null) {
                    props.setProperty(getString(key), getString(val));
                }
            }
        }

        return props;
    }

    static String getString(Instance stringInstance) {
        if (stringInstance == null) {
            return "*null*"; // NOI18N
        }
        assert stringInstance.getJavaClass().getName().equals(String.class.getName());

        PrimitiveArrayDump chars = (PrimitiveArrayDump) stringInstance.getValueOfField("value"); // NOI18N
        Integer offset = (Integer) stringInstance.getValueOfField("offset"); // NOI18N
        Integer len = (Integer) stringInstance.getValueOfField("count"); // NOI18N
        char[] charArr = ((PrimitiveArrayDump) chars).getChars(offset.intValue(), len.intValue());

        return new String(charArr).intern();
    }
}
