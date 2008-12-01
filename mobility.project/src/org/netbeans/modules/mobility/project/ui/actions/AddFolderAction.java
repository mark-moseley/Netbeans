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
package org.netbeans.modules.mobility.project.ui.actions;

import java.io.File;
import java.util.List;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class AddFolderAction extends NodeAction<File> {

    private static File lastFile = null;

    private AddFolderAction() {
        super(NbBundle.getMessage(AddFolderAction.class, "LBL_CustLibs_Add_Folder")); //NO18N
    }

    public static Action getStaticInstance() {
        return new AddFolderAction();
    }

    protected File[] getItems() {
        File[] files = null;
        // Let user search for the Jar file
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle(NbBundle.getMessage(AddFolderAction.class, "LBL_Classpath_AddFolder")); //NO18N
        // NOI18N
        if (defaultDir != null) {
            chooser.setSelectedFile(FileUtil.toFile(defaultDir.getChildren()[0]));
        } else if (lastFile != null) {
            chooser.setSelectedFile(lastFile);
        }
        final int option = chooser.showOpenDialog(null);
        // Sow the chooser
        if (option == JFileChooser.APPROVE_OPTION) {
            files = chooser.getSelectedFiles();
            if (files.length > 0) {
                lastFile = files[0];
            }
        }
        return files;
    }

    protected List<VisualClassPathItem> addItems(File[] files, final List<VisualClassPathItem> set, final Node node) {
        for (File file : files) {
            file = FileUtil.normalizeFile(file);
            set.add(new VisualClassPathItem(file, VisualClassPathItem.TYPE_FOLDER, null, file.getPath()));
        }
        return set;
    }
}
