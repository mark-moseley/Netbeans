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
package org.netbeans.core.startup;

import java.security.Permission;
import junit.framework.Assert;

/**
 * Counts the number of File.isDirectory() calls.
 * 
 * @author Pavel Flaska
 */
public class IsDirCntSecurityManager extends SecurityManager {

    private static int cnt;

    public static void initialize() {
        if (!(System.getSecurityManager() instanceof IsDirCntSecurityManager)) {
            System.setSecurityManager(new IsDirCntSecurityManager());
        }
        cnt = 0;
    }

    public static void assertCounts(String msg, int minCount, int maxCount) {
        StringBuilder sb = new StringBuilder(msg);
        sb.append(" limits = <").append(minCount).append(',');
        sb.append(maxCount).append('>').append("; count# = ");
        sb.append(cnt).append(".");
        Assert.assertTrue(sb.toString(), cnt >= minCount && cnt <= maxCount);
        cnt = 0;
    }

    @Override
    public void checkRead(String file) {
        super.checkRead(file);
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length - 1; i++) {
            if (stack[i].getClassName().equals(IsDirCntSecurityManager.class.getName())) {
                if (
                    "isDirectory".equals(stack[i + 1].getMethodName()) &&
                    "File.java".equals(stack[i + 1].getFileName())
                ) {
                    // File.isDirectory() has been called? If so, count it in.
                    cnt++;
                    break;
                }
            }
        }
    }

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }
}
