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

package org.netbeans.modules.php.editor.sql;

import org.netbeans.modules.php.editor.sql.PHPSQLCompletion;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.editor.codegen.ui.ConnectionGeneratorPanel;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SelectConnectionItem implements CompletionItem {

    private static final ImageIcon CONNECTION_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/editor/resources/connection.gif")); // NOI18N

    private Project project;
    private DatabaseConnection dbconn;

    public SelectConnectionItem(Project project) {
        this.project = project;
        dbconn = PHPSQLCompletion.getDatabaseConnection(project);
    }

    public void defaultAction(JTextComponent component) {
        DatabaseConnection newDBConn = ConnectionGeneratorPanel.selectConnection(dbconn, false, false);
        PHPSQLCompletion.setDatabaseConnection(project, newDBConn);
        if (newDBConn != null) {
            Completion.get().showCompletion();
        }
    }

    public void processKeyEvent(KeyEvent evt) {
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), null, g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getImageIcon(), getLeftHtmlText(), null, g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    public int getSortPriority() {
        return 0;
    }

    public CharSequence getSortText() {
        return "";
    }

    public CharSequence getInsertPrefix() {
        return "";
    }

    private ImageIcon getImageIcon() {
        return CONNECTION_ICON;
    }

    private String getLeftHtmlText() {
        return NbBundle.getMessage(SelectConnectionItem.class, "SelectDatabaseConnection");
    }
}
