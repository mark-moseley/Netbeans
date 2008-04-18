/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.diff;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Mode;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.*;
import java.io.File;
import java.awt.event.MouseEvent;

/**
 *
 * @author Maros Sandor
 */
class EditorBufferSelectorPanel extends JPanel implements ListSelectionListener {
    
    private final JFileChooser fileChooser;
    private final FileObject peer;
    private JList elementsList;

    /** Creates new form EditorBufferSelectorPanel 
     * @param fileChooser*/
    public EditorBufferSelectorPanel(JFileChooser fileChooser, FileObject peer) {
        this.fileChooser = fileChooser;
        this.peer = peer;
        initComponents();
        initEditorDocuments();
    }

    private void initEditorDocuments() {
        elementsList = new JList() {
            public String getToolTipText(MouseEvent event) {
                int index = locationToIndex(event.getPoint());
                if (index != -1) {
                    EditorListElement element = (EditorListElement) elementsList.getModel().getElementAt(index);
                    return element.fileObject.getPath();
                }
                return null;
            }
        };
        
        List<EditorListElement> elements = new ArrayList<EditorListElement>();

        WindowManager wm = WindowManager.getDefault();
        Set<? extends Mode> modes = wm.getModes();
        for (Mode mode : modes) {
            if (wm.isEditorMode(mode)) {
                TopComponent[] tcs = mode.getTopComponents();
                for (TopComponent tc : tcs) {
                    Lookup lukap = tc.getLookup();
                    FileObject fo = lukap.lookup(FileObject.class);
                    if (fo == null) {
                        DataObject dobj = lukap.lookup(DataObject.class);
                        if (dobj != null) {
                            fo = dobj.getPrimaryFile();
                        }
                    }
                    if (fo != null && fo != peer) {
                        if (tc.getHtmlDisplayName() != null) {
                            elements.add(new EditorListElement(fo, tc.getHtmlDisplayName()));
                        } else {
                            elements.add(new EditorListElement(fo, tc.getName()));
                        }   
                    }
                }
            }
        }

        elementsList.setListData(elements.toArray(new EditorListElement[elements.size()]));
        elementsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementsList.addListSelectionListener(this);
        jPanel1.add(elementsList);
    }

    public void valueChanged(ListSelectionEvent e) {
        EditorListElement element = (EditorListElement) elementsList.getSelectedValue();
        if (element != null) {
            File file = FileUtil.toFile(element.fileObject);
            fileChooser.setSelectedFile(file);
        } else {
            File file = new File("");
            fileChooser.setSelectedFile(file);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorBufferSelectorPanel.class, "EditorBufferSelectorPanel.jLabel1.text")); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .add(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private class EditorListElement {
        FileObject      fileObject;
        String          displayName;

        EditorListElement(FileObject tc, String displayName) {
            this.fileObject = tc;
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
    }
}
