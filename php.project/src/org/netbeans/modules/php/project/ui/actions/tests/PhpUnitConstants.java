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

package org.netbeans.modules.php.project.ui.actions.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PHP Unit 3.x constants.
 * @author Tomas Mysik
 */
public final class PhpUnitConstants {
    // minimum supported version
    public static final int[] MINIMAL_VERSION = new int[] {3, 3, 0};

    // test files
    public static final String TEST_FILE_SUFFIX = "Test.php"; // NOI18N

    // cli options
    public static final String PARAM_VERSION = "--version"; // NOI18N
    public static final String PARAM_XML_LOG = "--log-xml"; // NOI18N
    public static final String PARAM_XML_CONFIG = "--configuration"; // NOI18N
    public static final String PARAM_SKELETON = "--skeleton-test"; // NOI18N
    // for older PHP Unit versions
    public static final String PARAM_SKELETON_OLD = "--skeleton"; // NOI18N

    // output files
    public static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-phpunit-log.xml"); // NOI18N

    private PhpUnitConstants() {
    }

    /**
     * Get an array with actual and minimal PHPUnit versions.
     */
    public static String[] getPhpUnitVersions(int[] actualVersion) {
        List<String> params = new ArrayList<String>(6);
        if (actualVersion == null) {
            params.add("?"); params.add("?"); params.add("?"); // NOI18N
        } else {
            for (Integer i : actualVersion) {
                params.add(String.valueOf(i));
            }
        }
        for (Integer i : MINIMAL_VERSION) {
            params.add(String.valueOf(i));
        }
        return params.toArray(new String[params.size()]);
    }
}
