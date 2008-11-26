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

package org.netbeans.modules.project.ui;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

/**
 * Action for enabling/disabling synchronized update of Projects and Files views
 * according to file selected in editor
 *
 * @author Milan Kubec
 */
public class SyncEditorWithViewsAction extends SystemAction implements DynamicMenuContent {

    public static final String SYNC_ENABLED_PROP_NAME = "synchronizeEditorWithViews";
    
    private JCheckBoxMenuItem menuItems[];

    @Override
    public String getName() {
        return NbBundle.getMessage(SyncEditorWithViewsAction.class, "CTL_SYNC_EDITOR_WITH_VIEWS");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SyncEditorWithViewsAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Preferences prefs = NbPreferences.forModule(SyncEditorWithViewsAction.class);
        prefs.putBoolean(SYNC_ENABLED_PROP_NAME, !prefs.getBoolean(SYNC_ENABLED_PROP_NAME, false));
    }

    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return items;
    }

    private void createItems() {
        if (menuItems == null) {
            menuItems = new JCheckBoxMenuItem[1];
            menuItems[0] = new JCheckBoxMenuItem(this);
            menuItems[0].setIcon(null);
            Mnemonics.setLocalizedText(menuItems[0],
                    NbBundle.getMessage(SyncEditorWithViewsAction.class,
                    "CTL_SYNC_EDITOR_WITH_VIEWS"));
        }
    }

    private void updateState() {
        boolean sel = NbPreferences.forModule(SyncEditorWithViewsAction.class).getBoolean(SYNC_ENABLED_PROP_NAME, false);
        menuItems[0].setSelected(sel);
    }

}
