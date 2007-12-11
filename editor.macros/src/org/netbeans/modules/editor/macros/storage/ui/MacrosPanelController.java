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
package org.netbeans.modules.editor.macros.storage.ui;

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Jan Jancura
 */
public final class MacrosPanelController extends OptionsPanelController {

    public MacrosPanelController() {
        // no-op
    }

    public void update() {
        MacrosModel model = lastPanel.getModel();
        if (!model.isLoaded()) {
            model.load();
        }
    }

    public void applyChanges() {
        lastPanel.getModel().save();
    }

    public void cancel() {
        lastPanel.getModel().load();
    }

    public boolean isValid() {
        return true;
    }

    public boolean isChanged() {
        return lastPanel.getModel().isChanged();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.editor.macros"); //NOI18N
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getMacrosPanel(masterLookup);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        lastPanel.getModel().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        lastPanel.getModel().removePropertyChangeListener(l);
    }
    private static final Map<Lookup, Reference<MacrosPanel>> PANELS = new WeakHashMap<Lookup, Reference<MacrosPanel>>();
    private MacrosPanel lastPanel = null;

    private MacrosPanel getMacrosPanel(Lookup masterLookup) {
        Reference<MacrosPanel> ref = PANELS.get(masterLookup);
        MacrosPanel panel = ref == null ? null : ref.get();

        if (panel == null) {
            panel = new MacrosPanel(masterLookup);
            PANELS.put(masterLookup, new WeakReference<MacrosPanel>(panel));
        }

        this.lastPanel = panel;
        return panel;
    }
}
