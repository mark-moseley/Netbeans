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
package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.ui.commit.CommitTable;
import org.netbeans.modules.subversion.ui.commit.CommitTableModel;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.util.Context;
import org.openide.util.HelpCtx;

/**
 * @author Tomas Stupka
 */
public class ImportPreviewStep extends AbstractStep {
    
    private PreviewPanel previewPanel;
    private Context context;
    private CommitTable table;    
    
    public ImportPreviewStep(Context context) {
        this.context = context;
    }
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportPreviewStep.class);
    }    

    protected JComponent createComponent() {
        if (previewPanel == null) {
            previewPanel = new PreviewPanel();

            //TableSorter sorter = SvnModuleConfig.getDefault().getImportTableSorter();
            //if(sorter==null) {
                table = new CommitTable(previewPanel.tableLabel, CommitTable.IMPORT_COLUMNS, new String[] { CommitTableModel.COLUMN_NAME_PATH });    
            //} else {
            //    table = new CommitTable(previewPanel.tableLabel, CommitTable.IMPORT_COLUMNS, sorter);
            //}                                    
            
            JComponent component = table.getComponent();
            previewPanel.tablePanel.setLayout(new BorderLayout());
            previewPanel.tablePanel.add(component, BorderLayout.CENTER);
        }
        return previewPanel;              
    }

    protected void validateBeforeNext() {
        validateUserInput();
    }       

    public void validateUserInput() {
        if(table != null && table.getCommitFiles().size() > 0) {
            valid();
        } else {
            invalid(org.openide.util.NbBundle.getMessage(ImportPreviewStep.class, "CTL_Import_NothingToImport")); // NOI18N
        }        
    }    

    public void setup(String repositoryPath, String rootLocalPath) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(context, FileInformation.STATUS_LOCAL_CHANGE);

        if (files.length == 0) {
            return;
        }

        if(repositoryPath != null) {
            table.setRootFile(repositoryPath, rootLocalPath);
        }

        SvnFileNode[] nodes;        
        ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(files.length);

        for (int i = 0; i<files.length; i++) {
            File file = files[i];
            SvnFileNode node = new SvnFileNode(file);
            nodesList.add(node);
        }
        nodes = nodesList.toArray(new SvnFileNode[files.length]);
        table.setNodes(nodes);

        validateUserInput();
    }

    public Map getCommitFiles() {
        return table.getCommitFiles();
    }
    
    public void storeTableSorter() {
        //SvnModuleConfig.getDefault().setImportTableSorter(table.getSorter());        
    }

}

