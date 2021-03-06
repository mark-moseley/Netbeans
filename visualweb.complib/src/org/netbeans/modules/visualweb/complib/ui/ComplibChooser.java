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

package org.netbeans.modules.visualweb.complib.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.complib.Complib;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider;
import org.netbeans.modules.visualweb.complib.ExtensionComplib;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Derived from NetBeans LibrariesChooser
 *
 * @author Edwin Goei
 */
class ComplibChooser extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final ComplibServiceProvider csp = ComplibServiceProvider
            .getInstance();

    /**
     * Creates new form ComplibChooser
     */
    public ComplibChooser(final JButton addComplibOption, Project project) {
        initComponents();

        jList1.setPrototypeCellValue("0123456789012345678901234"); // NOI18N
        ComplibListModel complibListModel = new ComplibListModel(project);
        jList1.setModel(complibListModel);
        jList1.setCellRenderer(new ComplibRenderer());
        jList1.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                addComplibOption
                        .setEnabled(jList1.getSelectedIndices().length != 0);
            }
        });
        
        // If there are items in the list, select the first one
        if (complibListModel.getSize() > 0) {
            jList1.setSelectedIndex(0);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ComplibChooser.class);
    }

    public ArrayList<ExtensionComplib> getSelectedComplibs() {
        ArrayList<ExtensionComplib> retVal = new ArrayList<ExtensionComplib>();
        Object[] selectedValues = jList1.getSelectedValues();
        for (Object val : selectedValues) {
            if (val instanceof ExtensionComplib) {
                retVal.add((ExtensionComplib) val);
            }
        }
        return retVal;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        edit = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ComplibChooser.class).getString("ComplibChooser.AD"));
        jLabel1.setLabelFor(jList1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(ComplibChooser.class).getString("ComplibChooser.installedLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ComplibChooser.class).getString("ComplibChooser.scrollpane.AD"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(edit, org.openide.util.NbBundle.getBundle(ComplibChooser.class).getString("ComplibChooser.edit"));
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLibraries(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(edit, gridBagConstraints);
        edit.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ComplibChooser.class).getString("ComplibChooser.button.AD"));

    }// </editor-fold>//GEN-END:initComponents

    private void editLibraries(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLibraries
        // TODO Make this nicer where new complib is selected, etc.
        new CompLibManagerPanel().showDialog();

        // ComplibListModel model = (ComplibListModel) jList1.getModel ();
        // Collection oldLibraries = Arrays.asList(model.getLibraries());
        // LibrariesCustomizer.showCustomizer((Library)this.jList1.getSelectedValue());
        // List currentLibraries = Arrays.asList(model.getLibraries());
        // Collection newLibraries = new ArrayList (currentLibraries);
        //        
        // newLibraries.removeAll(oldLibraries);
        // int indexes[] = new int [newLibraries.size()];
        //        
        // Iterator it = newLibraries.iterator();
        // for (int i=0; it.hasNext();i++) {
        // Library lib = (Library) it.next ();
        // indexes[i] = currentLibraries.indexOf (lib);
        //        }
        //        this.jList1.setSelectedIndices (indexes);
    }//GEN-LAST:event_editLibraries


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton edit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private static final class ComplibListModel extends AbstractListModel
            implements PropertyChangeListener {
        private Project project;

        private List<ExtensionComplib> addableComplibs;

        public ComplibListModel(Project project) {
            this.project = project;
            csp
                    .addPropertyChangeListener((PropertyChangeListener) WeakListeners
                            .create(PropertyChangeListener.class, this, csp));
        }

        public int getSize() {
            loadAddableComplibs();
            return addableComplibs.size();
        }

        public Object getElementAt(int index) {
            loadAddableComplibs();
            return addableComplibs.get(index);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            addableComplibs = null;
            fireContentsChanged(this, 0, getSize() - 1);
        }

        private void loadAddableComplibs() {
            if (addableComplibs == null) {
                addableComplibs = csp.getAddableComplibs(project);
            }
        }
    }

    private static class ComplibRenderer extends DefaultListCellRenderer {
        private static final Icon icon;
        static {
            String iconPath = ComplibChooser.class.getPackage().getName()
                    .replace('.', '/')
                    + "/images/library.png";
            icon = new ImageIcon(Utilities.loadImage(iconPath));
        }

        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            String displayName = null;
            if (value instanceof Complib) {
                Complib complib = (Complib) value;
                displayName = complib.getVersionedTitle();
            }
            super.getListCellRendererComponent(list, displayName, index,
                    isSelected, cellHasFocus);

            setIcon(icon);
            return this;
        }
    }
}
