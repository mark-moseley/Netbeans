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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Dimension;
import java.util.List;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.api.utils.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.PeExecutableFileFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 *
 * @author Alexander Simon
 */
public class AdditionalLibrariesListPanel extends ListEditorPanel<String> {
    
    public static JPanel wrapPanel(ListEditorPanel innerPanel) {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(innerPanel, gridBagConstraints);
        outerPanel.setPreferredSize(new Dimension(500, 250));
        return outerPanel;
    }
    
    public AdditionalLibrariesListPanel(List<String> objects) {
        super(objects);
        getDefaultButton().setVisible(false);
        getUpButton().setVisible(false);
        getDownButton().setVisible(false);
        getCopyButton().setVisible(false);
    }
    
    @Override
    public String addAction() {
        String seed = null;
        if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        }
        if (seed == null) {
            seed = System.getProperty("user.home"); // NOI18N
        }
        FileFilter[] filters;
        if (Utilities.isWindows()){
            filters = new FileFilter[] {PeExecutableFileFilter.getInstance(),
            ElfStaticLibraryFileFilter.getInstance(),
            PeDynamicLibraryFileFilter.getInstance()};
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            filters = new FileFilter[] {MacOSXExecutableFileFilter.getInstance(),
            ElfStaticLibraryFileFilter.getInstance(),
            MacOSXDynamicLibraryFileFilter.getInstance()};
        }  else {
            filters = new FileFilter[] {ElfExecutableFileFilter.getInstance(),
            ElfStaticLibraryFileFilter.getInstance(),
            ElfDynamicLibraryFileFilter.getInstance()};
        }
        FileChooser fileChooser = new FileChooser(
                getString("LIBRARY_CHOOSER_TITLE_TXT"),
                getString("LIBRARY_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                false,
                filters,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        String itemPath = fileChooser.getSelectedFile().getPath();
        itemPath = FilePathAdaptor.normalize(itemPath);
        return itemPath;
    }
    
    @Override
    public String getListLabelText() {
        return getString("LIBRARY_LIST_TXT");
    }
    @Override
    public char getListLabelMnemonic() {
        return getString("LIBRARY_LIST_MN").charAt(0);
    }
    
    @Override
    public String getAddButtonText() {
        return getString("ADD_BUTTON_TXT");
    }
    @Override
    public char getAddButtonMnemonics() {
        return getString("ADD_BUTTON_MN").charAt(0);
    }
    
    @Override
    public String getRenameButtonText() {
        return getString("EDIT_BUTTON_TXT");
    }
    @Override
    public char getRenameButtonMnemonics() {
        return getString("EDIT_BUTTON_MN").charAt(0);
    }
    
    @Override
    public String copyAction(String o) {
        return o;
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    @Override
    public void editAction(String o) {
        String s = o;
        
        InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
        notifyDescriptor.setInputText(s);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String newS = notifyDescriptor.getInputText();
        Vector vector = getListData();
        Object[] arr = getListData().toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == o) {
                vector.remove(i);
                vector.add(i, newS);
                break;
            }
        }
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(AdditionalLibrariesListPanel.class).getString(key);
    }
}