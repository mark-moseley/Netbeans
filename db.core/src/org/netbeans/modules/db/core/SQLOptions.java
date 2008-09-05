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

package org.netbeans.modules.db.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea
 */
public class SQLOptions  {
    private static SQLOptions INSTANCE = new SQLOptions();
    private static final String PROP_FETCH_STEP = "fetchStep"; // NOI18N
    private static final int DEFAULT_FETCH_STEP = 200;
    private static final String PROP_MAX_ROWS = "maxRows";
    private static final int DEFAULT_MAX_ROWS = 200000;

    public static final String PROP_KEEP_OLD_TABS = "keepOldResultTabs"; // NOI18N

    public static SQLOptions getDefault() {
        return INSTANCE;
    }
    
    public String displayName() {
        return NbBundle.getMessage(SQLOptions.class, "LBL_SQLOptions");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(SQLOptions.class);
    }
        
    public int getFetchStep() {
        return getPreferences().getInt(PROP_FETCH_STEP, DEFAULT_FETCH_STEP);
    }
    
    public void setFetchStep(int value) {
        getPreferences().putInt(PROP_FETCH_STEP, value);
    }   
    
    public int getMaxRows() {
        return getPreferences().getInt(PROP_MAX_ROWS, DEFAULT_MAX_ROWS);
    }
    
    public void setMaxRows(int rows) {
        getPreferences().putInt(PROP_MAX_ROWS, rows);
    }
    public boolean isKeepOldResultTabs() {
        return getPreferences().getBoolean(PROP_KEEP_OLD_TABS, false);
    }

    public void setKeepOldResultTabs(boolean keepOldTabs) {
        getPreferences().putBoolean(PROP_KEEP_OLD_TABS, keepOldTabs);
    }

}
