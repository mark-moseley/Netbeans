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
package org.netbeans.modules.sql.framework.ui.output.dataview;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;

/**
 * @author Ahimanikya Satapathy
 */
public class SourceTableDataPanel extends DataOutputPanel {

    private static transient final Logger mLogger = LogUtil.getLogger(SourceTableDataPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public SourceTableDataPanel(SourceTable etlObject, SQLDefinition sqlDefinition) {
        super(etlObject, sqlDefinition, false, true);
    }

    public void generateResult() {
        generateResult(this.table);
    }

    public void generateResult(SQLObject aTable) {
        this.table = aTable;
        String nbBundle1 = mLoc.t("PRSR001: Data: {0}", table.getDisplayName());
        this.setName(Localizer.parse(nbBundle1));
        String nbBundle2 = mLoc.t("PRSR001: Loading Data");
        String title = Localizer.parse(nbBundle2);
        String nbBundle3 = mLoc.t("PRSR001: Loading from database, please wait...");
        String msg = Localizer.parse(nbBundle3);
        UIUtil.startProgressDialog(title, msg);
        generateSelectAllTableData();
    }

    private void generateSelectAllTableData() {
        refreshButton.setEnabled(false);
        refreshField.setEnabled(false);
        DataViewWorkerThread queryThread = new DataViewWorkerThread(table, this);
        queryThread.start();
    }
}
