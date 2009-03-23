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
package org.netbeans.modules.nativeexecution.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class EnvReader implements Callable<Map<String, String>> {

    private final InputStream is;

    public EnvReader(final InputStream is) {
        this.is = is;
    }

    public Map<String, String> call() throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        StringBuilder buffer = new StringBuilder();

        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            s = br.readLine();

            if (s == null) {
                break;
            }

            buffer.append(s.trim());

            if (s.charAt(s.length() - 1) != '\\') {
                String str = buffer.toString();
                buffer.setLength(0);

                int epos = str.indexOf('=');

                if (epos < 0) {
                    continue;
                }

                String var = str.substring(0, epos);
                var = var.substring(var.lastIndexOf(' ') + 1);
                String val = str.substring(epos + 2, str.length() - 1);

                result.put(var, val);
            }
        }

        return result;
    }
}
