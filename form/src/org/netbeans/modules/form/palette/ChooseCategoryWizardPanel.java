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

package org.netbeans.modules.form.palette;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

import org.openide.WizardDescriptor;
import org.openide.explorer.*;

/**
 * The third panel in the wizard for adding new components to the palette.
 * Lets the user choose the palette category where to add the selected
 * components.
 *
 * @author Tomas Pavek
 */

class ChooseCategoryWizardPanel implements WizardDescriptor.FinishablePanel {

    private CategorySelector categorySelector;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (categorySelector == null) { // create the UI component for the wizard step
            categorySelector = new CategorySelector();

            // wizard API: set the caption and index of this panel
            categorySelector.setName(PaletteUtils.getBundleString("CTL_SelectCategory_Caption")); // NOI18N
            categorySelector.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                               new Integer(2));

            categorySelector.getExplorerManager().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName()))
                            fireStateChanged();
                    }
                });
        }

        return categorySelector;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return categorySelector != null
               && categorySelector.getSelectedCategory() != null;
    }

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
        if (categorySelector != null)
            ((AddToPaletteWizard)settings).setSelectedCategory(categorySelector.getSelectedCategory());
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // WizardDescriptor.FinishablePanel implementation
    public boolean isFinishPanel() {
        return true;
    }

    // -----

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i >= 0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
