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

package org.netbeans.modules.php.project.environment;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Tomas Mysik
 */
final class MacPhpEnvironment extends PhpEnvironment {

    MacPhpEnvironment() {
    }

    @Override
    public List<DocumentRoot> getDocumentRoots(String projectName) {
        // MAMP
        File mamp = new File("/Applications/MAMP/htdocs"); // NOI18N
        if (mamp.isDirectory()) {
            String documentRoot = getFolderName(mamp, projectName);
            String url = getDefaultUrl(projectName, 8888);
            return Arrays.asList(new DocumentRoot(documentRoot, url, true));
        }
        return Collections.<DocumentRoot>emptyList();
    }

    @Override
    public List<String> getAllPhpInterpreters() {
        // MAMP
        File php = new File("/Applications/MAMP/bin/php5/bin/php"); // NOI18N
        if (!php.exists()) {
            return Collections.<String>emptyList();
        }
        return Arrays.asList(php.getAbsolutePath());
    }
}
