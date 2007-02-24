/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.requirements.doorsprovider;

import java.awt.Frame;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  Thuy
 */
public class DoorsProjectDialog extends javax.swing.JPanel
{
    //private boolean wasAccepted = false;
    private String selectedItem = "";
    private boolean errorFound = false;
    
    /** Creates new form DoorsProjectDialog */
    public DoorsProjectDialog()
    {
        initComponents();
        addDOORSProjects();
    }
    
    protected void addDOORSProjects()
   {
      try
      {
         String result = DoorUtility.sendRequestToDoors("#include \"GetProjectInfo.dxl\";GetProjectNames()");
         StringTokenizer tokenizer = new StringTokenizer(result, "|");

         DefaultListModel model = new DefaultListModel();
         while(tokenizer.hasMoreTokens() == true)
         {
            model.addElement(tokenizer.nextToken());
         }
         projectList.setModel(model);
         
         if (model != null  && model.size() > 0) {
            projectList.setSelectedIndex(0);
         }
      }
      catch(RequirementsException e)
      {
         Frame hwnd = null;
         IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
         
         if( cpProxyUserInterface != null)
         {
            hwnd = cpProxyUserInterface.getWindowHandle();
         }
         
         String msgText = NbBundle.getMessage(DoorsProjectDialog.class, "IDS_DOORSNOTAVAILABLEMESSAGE");
         String msgTitle = NbBundle.getMessage(DoorsProjectDialog.class, "IDS_DOORSNOTAVAILABLETITLE");
         
         IQuestionDialog cpQuestionDialog = UIFactory.createQuestionDialog();
         
         cpQuestionDialog.displaySimpleQuestionDialog( MessageDialogKindEnum.SQDK_OK,
                                                       MessageIconKindEnum.EDIK_ICONWARNING,
                                                       msgText ,
                                                       MessageResultKindEnum.SQDRK_RESULT_YES,
                                                       hwnd,
                                                       msgTitle  );
         errorFound = true;
      }
   }
    
    public boolean hasError () {
        return errorFound;
    }
    
    public String  performAction (Object action) {
        String selectedItem = null;
        if (action == DialogDescriptor.OK_OPTION) {
            selectedItem = (String)projectList.getSelectedValue();
        }
        return selectedItem;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        projectListLabel = new javax.swing.JLabel();

        jScrollPane.setFocusable(false);
        jScrollPane.setViewportView(projectList);
        projectList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "ACSN_PROJECT_LIST"));
        projectList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "ACSD_PROJECT_LIST"));

        projectListLabel.setLabelFor(projectList);
        org.openide.awt.Mnemonics.setLocalizedText(projectListLabel, org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "LBL_DoorsProjects"));
        projectListLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "ACSD_PROJECT_LIST"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(projectListLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(projectListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JList projectList;
    private javax.swing.JLabel projectListLabel;
    // End of variables declaration//GEN-END:variables
    
}
