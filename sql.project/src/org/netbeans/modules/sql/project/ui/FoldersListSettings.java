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

package org.netbeans.modules.sql.project.ui;


import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class FoldersListSettings {
    /**
     * DOCUMENT ME!
     */
    private static final String LAST_EXTERNAL_SOURCE_ROOT = "srcRoot"; // NOI18N
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; // NOI18N
    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; // NOI18N

    private static FoldersListSettings INSTANCE = new FoldersListSettings();

    private FoldersListSettings() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String displayName() {
        return NbBundle.getMessage(FoldersListSettings.class, "TXT_WebProjectFolderList"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getLastExternalSourceRoot() {
        return NbPreferences.forModule(FoldersListSettings.class)
            .get(LAST_EXTERNAL_SOURCE_ROOT, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param path DOCUMENT ME!
     */
    public void setLastExternalSourceRoot(String path) {
        if (path != null) {
            NbPreferences.forModule(FoldersListSettings.class)
                .put(LAST_EXTERNAL_SOURCE_ROOT, path);
        } else {
            NbPreferences.forModule(FoldersListSettings.class)
                .remove(LAST_EXTERNAL_SOURCE_ROOT);
    }
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getNewProjectCount() {
        return NbPreferences.forModule(FoldersListSettings.class)
            .getInt(NEW_PROJECT_COUNT, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param count DOCUMENT ME!
     */
    public void setNewProjectCount(int count) {
        NbPreferences.forModule(FoldersListSettings.class)
            .putInt(NEW_PROJECT_COUNT, count);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isShowAgainBrokenRefAlert() {
        return NbPreferences.forModule(FoldersListSettings.class)
            .getBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param again DOCUMENT ME!
     */
    public void setShowAgainBrokenRefAlert(boolean again) {
        NbPreferences.forModule(FoldersListSettings.class)
            .putBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static FoldersListSettings getDefault() {
        return INSTANCE;
    }
}
