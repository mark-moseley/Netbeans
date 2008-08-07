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

package org.netbeans.modules.php.project.ui.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Heavily inspired by Andrei's {@link org.netbeans.modules.spring.beans.ui.customizer.SelectConfigFilesPanel}.
 * @author Tomas Mysik
 */
public class SelectPhpInterpreterPanel extends JPanel {
    private static final long serialVersionUID = 2632292202688721433L;

    private final RequestProcessor rp = new RequestProcessor("PHP interpreter detection thread", 1, true); // NOI18N

    private List<String> availablePhpInterpreters;
    private DialogDescriptor descriptor;
    private Task detectTask;

    public SelectPhpInterpreterPanel() {
        initComponents();
    }

    public boolean open() {
        String title = NbBundle.getMessage(SelectPhpInterpreterPanel.class, "LBL_PhpInterpretersTitle");
        descriptor = new DialogDescriptor(this, title, true, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelDetection();
            }
        });
        if (availablePhpInterpreters == null) {
            // no available files, will run the detection task
            descriptor.setValid(false);
            phpInterpretersList.setEnabled(true);
            progressBar.setIndeterminate(true);
            detectTask = rp.create(new PhpInterpreterDetector());
            detectTask.schedule(0);
        } else {
            updateAvailablePhpInterpreters(availablePhpInterpreters);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == DialogDescriptor.OK_OPTION;
    }

    public List<String> getAvailablePhpInterpreters() {
        return availablePhpInterpreters;
    }

    public String getSelectedPhpInterpreter() {
        return (String) phpInterpretersList.getSelectedValue();
    }

    private void cancelDetection() {
        if (detectTask != null) {
            detectTask.cancel();
        }
    }

    private void updateAvailablePhpInterpreters(List<String> availablePhpInterpreters) {
        this.availablePhpInterpreters = availablePhpInterpreters;
        phpInterpretersList.setEnabled(true);
        phpInterpretersList.setListData(availablePhpInterpreters.toArray(new String[availablePhpInterpreters.size()]));
        // In an attempt to hide the progress bar and label, but force the occupy the same space.
        String message = null;
        if (availablePhpInterpreters.size() == 0) {
            message = NbBundle.getMessage(SelectPhpInterpreterPanel.class, "LBL_NoPhpInterpretersFound");
        } else {
            message = " "; // NOI18N
            // preselect the 1st item
            phpInterpretersList.setSelectedIndex(0);
        }
        messageLabel.setText(message);
        progressBar.setIndeterminate(false);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(getBackground());
        descriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detectedFilesLabel = new JLabel();
        phpInterpretersScrollPane = new JScrollPane();
        phpInterpretersList = new JList();
        messageLabel = new JLabel();
        progressBar = new JProgressBar();

        detectedFilesLabel.setLabelFor(phpInterpretersList);

        detectedFilesLabel.setText(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "LBL_PhpInterpreters")); // NOI18N
        phpInterpretersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phpInterpretersList.setEnabled(false);
        phpInterpretersScrollPane.setViewportView(phpInterpretersList);

        phpInterpretersList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.phpInterpretersList.AccessibleContext.accessibleName")); // NOI18N
        phpInterpretersList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.phpInterpretersList.AccessibleContext.accessibleDescription")); // NOI18N
        messageLabel.setLabelFor(progressBar);

        messageLabel.setText(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "LBL_PleaseWait")); // NOI18N
        progressBar.setString(" "); // NOI18N
        progressBar.setStringPainted(true);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, phpInterpretersScrollPane, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, progressBar, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, detectedFilesLabel)
                    .add(GroupLayout.LEADING, messageLabel))
                .addContainerGap())
        
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(detectedFilesLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(phpInterpretersScrollPane, GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(messageLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        
        );

        detectedFilesLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.detectedFilesLabel.AccessibleContext.accessibleName")); // NOI18N
        detectedFilesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.detectedFilesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpInterpretersScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.phpInterpretersScrollPane.AccessibleContext.accessibleName")); // NOI18N
        phpInterpretersScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.phpInterpretersScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        messageLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.messageLabel.AccessibleContext.accessibleName")); // NOI18N
        messageLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.messageLabel.AccessibleContext.accessibleDescription")); // NOI18N
        progressBar.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.progressBar.AccessibleContext.accessibleName")); // NOI18N
        progressBar.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.progressBar.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPhpInterpreterPanel.class, "SelectPhpInterpreterPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel detectedFilesLabel;
    private JLabel messageLabel;
    private JList phpInterpretersList;
    private JScrollPane phpInterpretersScrollPane;
    private JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    private final class PhpInterpreterDetector implements Runnable {

        public void run() {
            // just to be sure that the progress bar is displayed at least for a while
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                return;
            }
            final List<String> allPhps = PhpEnvironment.get().getAllPhpInterpreters();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateAvailablePhpInterpreters(allPhps);
                }
            });
        }
    }
}
