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
package org.netbeans.api.project.libraries;


import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import java.awt.Dialog;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.modules.project.libraries.ui.LibrariesModel;
import org.netbeans.modules.project.libraries.ui.NewLibraryPanel;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;

/** Provides method for opening Libraries customizer
 *
 */
public final class LibrariesCustomizer {

    private LibrariesCustomizer () {
    }

    /**
     * Shows libraries customizer for given library manager.
     * @param activeLibrary if not null the activeLibrary is selected in the opened customizer
     * @return true if user pressed OK and libraries were sucessfully modified
     */
    public static boolean showCustomizer (Library activeLibrary, LibraryManager libraryManager) {
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer (libraryManager.getArea());
        customizer.setBorder(new EmptyBorder(12, 12, 0, 12));
        if (activeLibrary != null)
            customizer.setSelectedLibrary (activeLibrary.getLibraryImplementation ());
        DialogDescriptor descriptor = new DialogDescriptor (customizer,NbBundle.getMessage(LibrariesCustomizer.class,
                "TXT_LibrariesManager"));
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                return customizer.apply();
            } else {
                return false;
            }
        } finally {
            dlg.dispose();
        }
    }

    /**
     * Shows libraries customizer for global libraries.
     * @param activeLibrary if not null the activeLibrary is selected in the opened customizer
     * @return true if user pressed OK and libraries were sucessfully modified
     */
    public static boolean showCustomizer (Library activeLibrary) {
        return showCustomizer(activeLibrary, LibraryManager.getDefault());
    }
    
    /**
     * Show customizer for creating new library in the given library manager.
     * @param manager manager
     * @return created persisted library or null if user cancelled operation
     * @since org.netbeans.modules.project.libraries/1 1.16
     */
    public static Library showCreateNewLibraryCustomizer(LibraryManager manager) {                                             
        if (manager == null) {
            manager = LibraryManager.getDefault();
        }
        LibraryStorageArea area = manager.getArea();
        if (area == null) {
            area = LibrariesModel.GLOBAL_AREA;
        }
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer (area);
        NewLibraryPanel p = new NewLibraryPanel(customizer.getModel(), null, area);
        DialogDescriptor dd = new DialogDescriptor (p, 
                NbBundle.getMessage(LibrariesCustomizer.class,"LibrariesCustomizer.createLibrary.title"),
                true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        p.setDialogDescriptor(dd);
        Dialog dlg = DialogDisplayer.getDefault().createDialog (dd);
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            LibraryImplementation impl;
            if (area != LibrariesModel.GLOBAL_AREA) {
                impl = customizer.getModel().createArealLibrary(p.getLibraryType(), p.getLibraryName(), manager.getArea());
            } else {
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider(p.getLibraryType());
                if (provider == null) {
                    return null;
                }
                impl = provider.createLibrary();
                impl.setName(p.getLibraryName());
            }
            customizer.getModel().addLibrary(impl);
            customizer.forceTreeRecreation();
            if (customizeLibrary(customizer, impl)) {
                return manager.getLibrary(impl.getName());
            }
        }
        return null;
    }                                            

    /**
     * Show library customizer for the given library.
     * @param library library
     * @return true if library was modified or not
     * @since org.netbeans.modules.project.libraries/1 1.16
     */
    public static boolean showSingleLibraryCustomizer(Library library) {                                             
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer (library.getManager().getArea());
        return customizeLibrary(customizer, library.getLibraryImplementation());
    }
    
    private static boolean customizeLibrary(org.netbeans.modules.project.libraries.ui.LibrariesCustomizer customizer, 
            LibraryImplementation activeLibrary) {
        customizer.hideLibrariesList();
        customizer.setBorder(new EmptyBorder(12, 8, 0, 10));
        customizer.setSelectedLibrary (activeLibrary);
        DialogDescriptor descriptor = new DialogDescriptor (customizer,NbBundle.getMessage(LibrariesCustomizer.class,
                "LibrariesCustomizer.customizeLibrary.title"));
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                customizer.apply();
                return true;
            } else {
                return false;
            }
        } finally {
            dlg.dispose();
        }
    }
    
}

