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
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import org.openide.WizardDescriptor;
import org.openide.filesystems.*;
import org.netbeans.api.project.libraries.*;
import org.netbeans.modules.form.project.ClassSource;
import org.openide.util.ChangeSupport;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a library. In this panel the user chooses a library from available libraries
 * installed in the IDE.
 *
 * @author Tomas Pavek, Jesse Glick
 */
class ChooseLibraryWizardPanel implements WizardDescriptor.Panel<AddToPaletteWizard> {

    private LibraryChooser.Panel librarySelector;
    private Component librarySelectorComponent;

//    private AddToPaletteWizard wizard;

    private final ChangeSupport cs = new ChangeSupport(this);

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (librarySelectorComponent == null) {
            librarySelector = LibraryChooser.createPanel(null, null);
            librarySelectorComponent = librarySelector.getVisualComponent();

            // wizard API: set the caption and index of this panel
            librarySelectorComponent.setName(
                PaletteUtils.getBundleString("CTL_SelectLibrary_Caption")); // NOI18N
            ((JComponent) librarySelectorComponent).putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                              new Integer(0));

            librarySelector.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    cs.fireChange();
                }
            });
                    }

        return librarySelectorComponent;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return librarySelector != null
               && !librarySelector.getSelectedLibraries().isEmpty();
    }

    public void readSettings(AddToPaletteWizard settings) {
//        wizard = settings;
    }

    public void storeSettings(AddToPaletteWizard settings) {
        if (librarySelector != null) { // create the UI component for the wizard step
            List<ClassSource.LibraryEntry> entries = new ArrayList<ClassSource.LibraryEntry>();
            for (Library lib : librarySelector.getSelectedLibraries()) {
                entries.add(new ClassSource.LibraryEntry(lib));
            }
            settings.setJARFiles(entries);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

}
