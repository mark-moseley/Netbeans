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

package org.netbeans.modules.profiler.heapwalk.ui;

import org.netbeans.modules.profiler.heapwalk.HeapWalker;
import org.netbeans.modules.profiler.heapwalk.HeapWalkerManager;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;


/**
 * Opens the Heap Walker
 *
 * @author Jiri Sedlacek
 */
public class OpenHeapWalkerAction extends SystemAction {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String ACTION_NAME = NbBundle.getMessage(OpenHeapWalkerAction.class, "OpenHeapWalkerAction_ActionName"); // NOI18N
    private static final String DIALOG_CAPTION = NbBundle.getMessage(OpenHeapWalkerAction.class,
                                                                     "OpenHeapWalkerAction_DialogCaption"); // NOI18N
                                                                                                            // -----
    private static File importDir;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public OpenHeapWalkerAction() {
        putValue("noIconInMenu", null); // NOI18N        
        setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/profiler/resources/memory.png", true))); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenHeapWalkerAction.class);
    }

    public String getName() {
        return ACTION_NAME;
    }

    public void actionPerformed(ActionEvent e) {
        final File heapDumpFile = getHeapDumpFile();
        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    if (heapDumpFile != null) {
                        HeapWalkerManager.getDefault().openHeapWalker(heapDumpFile);
                    }
                }
            });
    }

    private static File getHeapDumpFile() {
        JFileChooser chooser = new JFileChooser();

        if (importDir != null) {
            chooser.setCurrentDirectory(importDir);
        }

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle(DIALOG_CAPTION);

        if (chooser.showOpenDialog(IDEUtils.getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            importDir = chooser.getCurrentDirectory();

            return chooser.getSelectedFile();
        }

        return null;
    }
}
