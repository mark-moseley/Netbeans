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

package org.netbeans.lib.profiler.ui.memory;

import org.netbeans.lib.profiler.results.ResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.ui.UIUtils;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;


/**
 * This class implements presentation frames for Object Allocation Profiling.
 *
 * @author Ian Formanek
 */
public class SnapshotAllocResultsPanel extends AllocResultsPanel implements ActionListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    private static final String GO_SOURCE_POPUP_ITEM_NAME = messages.getString("AllocResultsPanel_GoSourcePopupItemName"); // NOI18N
    private static final String SHOW_STACK_TRACES_POPUP_ITEM_NAME = messages.getString("AllocResultsPanel_ShowStackTracesPopupItemName"); // NOI18N
                                                                                                                                          // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AllocMemoryResultsSnapshot snapshot;
    private JMenuItem popupShowSource;
    private JMenuItem popupShowStacks;
    private JPopupMenu memoryResPopupMenu;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SnapshotAllocResultsPanel(AllocMemoryResultsSnapshot snapshot, MemoryResUserActionsHandler actionsHandler) {
        super(actionsHandler);
        this.snapshot = snapshot;

        fetchResultsFromSnapshot();

        //prepareResults();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public ResultsSnapshot getSnapshot() {
        return snapshot;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == popupShowStacks) {
            actionsHandler.showStacksForClass(selectedClassId, getSortingColumn(), getSortingOrder());
        } else if (e.getSource() == popupShowSource) {
            showSourceForClass(selectedClassId);
        }
    }

    protected String getClassName(int classId) {
        return snapshot.getClassName(classId);
    }

    protected String[] getClassNames() {
        return snapshot.getClassNames();
    }

    protected JPopupMenu getPopupMenu() {
        if (memoryResPopupMenu == null) {
            memoryResPopupMenu = new JPopupMenu();

            Font boldfont = memoryResPopupMenu.getFont().deriveFont(Font.BOLD);

            popupShowSource = new JMenuItem();
            popupShowSource.setFont(boldfont);
            popupShowSource.setText(GO_SOURCE_POPUP_ITEM_NAME);
            memoryResPopupMenu.add(popupShowSource);
            popupShowSource.addActionListener(this);

            if (snapshot.containsStacks()) {
                memoryResPopupMenu.addSeparator();
                popupShowStacks = new JMenuItem();
                popupShowStacks.setText(SHOW_STACK_TRACES_POPUP_ITEM_NAME);
                memoryResPopupMenu.add(popupShowStacks);
                popupShowStacks.addActionListener(this);
            }
        }

        return memoryResPopupMenu;
    }

    private void fetchResultsFromSnapshot() {
        totalAllocObjectsSize = UIUtils.copyArray(snapshot.getObjectsSizePerClass());
        nTotalAllocObjects = UIUtils.copyArray(snapshot.getObjectsCounts());

        // In some situations nInstrClasses can be already updated, but nTotalAllocObjects.length and/ort totalAllocObjectsSize - not yet.
        // Take measures to avoid ArrayIndexOutOfBoundsException.
        nTrackedItems = snapshot.getNProfiledClasses();

        if (nTrackedItems > nTotalAllocObjects.length) {
            nTrackedItems = nTotalAllocObjects.length;
        }

        if (nTrackedItems > totalAllocObjectsSize.length) {
            nTrackedItems = totalAllocObjectsSize.length;
        }

        // Operations necessary for correct bar representation of results
        maxValue = 0;
        nTotalBytes = 0;
        nTotalClasses = 0;

        for (int i = 0; i < nTrackedItems; i++) {
            if (maxValue < totalAllocObjectsSize[i]) {
                maxValue = totalAllocObjectsSize[i];
            }

            nTotalBytes += totalAllocObjectsSize[i];
            nTotalClasses += nTotalAllocObjects[i];
        }

        initDataUponResultsFetch();
    }
}
