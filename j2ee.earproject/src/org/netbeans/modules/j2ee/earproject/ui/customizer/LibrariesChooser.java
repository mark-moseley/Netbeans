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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * @author  tz97951
 */
public class LibrariesChooser extends javax.swing.JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 1L;

    private final Collection incompatibleLibs;

    /** Creates new form LibrariesChooser */
    public LibrariesChooser(Collection alreadySelectedLibs, String j2eePlatform) {
        initComponents();
        jList1.setPrototypeCellValue("0123456789012345678901234");      //NOI18N
        jList1.setModel(new LibrariesListModel());
        // XXX Examine this to see if we need pass this in from somewhere...
        incompatibleLibs = java.util.Collections.EMPTY_LIST;
                // VisualClasspathSupport.getLibrarySet(WebProjectGenerator.getIncompatibleLibraries(j2eePlatform));
        jList1.setCellRenderer(new LibraryRenderer(alreadySelectedLibs, incompatibleLibs, j2eePlatform));
    }

    public Library[] getSelectedLibraries () {
        Object[] selected = this.jList1.getSelectedValues();
        Collection<Library> libs = new ArrayList<Library>();
        for (int i = 0; i < selected.length; i++) {
            Library lib = (Library) selected[i];
            if(!incompatibleLibs.contains(lib)) { // incompatible libraries are not added
                libs.add(lib);
            }
        }
        return libs.toArray(new Library[libs.size()]);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(LibrariesChooser.class);
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        jList1.addListSelectionListener(listener);
    }

    public boolean isValidSelection() {
        Object[] selected = this.jList1.getSelectedValues();
        if(selected.length == 0) {
            return false;
        }
        for (int i = 0; i < selected.length; i++) {
            if(incompatibleLibs.contains(selected[i])) {
                return false;
            }
        }
        return true;
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

        setPreferredSize(new java.awt.Dimension(350, 250));
        getAccessibleContext().setAccessibleDescription(null); // NOI18N
        jLabel1.setLabelFor(jList1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(LibrariesChooser.class, "CTL_Libraries")); // NOI18N
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
        jList1.getAccessibleContext().setAccessibleDescription(null); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(edit, NbBundle.getMessage(LibrariesChooser.class, "CTL_ManageLibraries")); // NOI18N
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
        edit.getAccessibleContext().setAccessibleDescription(null); // NOI18N

    }// </editor-fold>//GEN-END:initComponents

    private void editLibraries(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLibraries
        LibrariesListModel model = (LibrariesListModel) jList1.getModel ();
        Collection<Library> oldLibraries = Arrays.asList(model.getLibraries());
        LibrariesCustomizer.showCustomizer((Library)this.jList1.getSelectedValue());
        List<Library> currentLibraries = Arrays.asList(model.getLibraries());
        Collection<Library> newLibraries = new ArrayList<Library>(currentLibraries);

        newLibraries.removeAll(oldLibraries);
        int indexes[] = new int[newLibraries.size()];

        int i = 0;
        for (Library lib : newLibraries) {
            indexes[i++] = currentLibraries.indexOf(lib);
        }
        this.jList1.setSelectedIndices (indexes);
    }//GEN-LAST:event_editLibraries


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton edit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables



    private static final class LibrariesListModel extends AbstractListModel implements PropertyChangeListener {
        private static final long serialVersionUID = 1L;

        private Library[] cache;
        /** Number of libraries in the LibraryManager when last refreshed. */
        private int numberOfLibs;
        
        // XXX more stuff that needs a look-see
        
        //VisualClasspathSupport.getLibrarySet(
        //    WebProjectGenerator.getIncompatibleLibraries(WebModule.J2EE_13_LEVEL));
        //VisualClasspathSupport.getLibrarySet(
        //    WebProjectGenerator.getIncompatibleLibraries(WebModule.J2EE_14_LEVEL));
        
        public LibrariesListModel() {
            LibraryManager manager = LibraryManager.getDefault();
            manager.addPropertyChangeListener((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,
                    this, manager));
        }

        public synchronized int getSize() {
            if (this.cache == null) {
                this.cache = this.createLibraries();
            }
            return this.cache.length;
        }

        public synchronized Object getElementAt(int index) {
            if (this.cache == null) {
                this.cache = this.createLibraries();
            }
            if (index >= 0 && index < this.cache.length) {
                return this.cache[index];
            }
            else {
                return null;
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            int oldSize = this.cache == null ? 0 : numberOfLibs;
            this.cache = createLibraries();
            int newSize = numberOfLibs;
            this.fireContentsChanged(this, 0, Math.min(oldSize-1,newSize-1));
            if (oldSize > newSize) {
                this.fireIntervalRemoved(this,newSize,oldSize-1);
            }
            else if (oldSize < newSize) {
                this.fireIntervalAdded(this,oldSize,newSize-1);
            }
        }

        public synchronized Library[] getLibraries () {
            if (this.cache == null) {
                this.cache = this.createLibraries();
            }
            return this.cache;
        }

        private Library[] createLibraries () {
            Library[] libs = LibraryManager.getDefault().getLibraries();
            numberOfLibs = libs.length;
            Arrays.sort(libs, new Comparator<Library> () {
                public int compare (Library l1, Library l2) {
                    String name1 = l1.getDisplayName();
                    String name2 = l2.getDisplayName();
                    return name1.compareToIgnoreCase(name2);
                }
            });
            return libs;
        }
    }


    private static final class LibraryRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final String LIBRARY_ICON = "org/netbeans/modules/j2ee/earproject/ui/resources/libraries.gif";  //NOI18N
        private Icon cachedIcon;
        private final Collection alreadySelectedLibs;
        private final Collection incompatibleLibs;
        private final String j2eePlatform;

        public LibraryRenderer(Collection alreadySelectedLibs, Collection incompatibleLibs, String j2eePlatform) {
            this.alreadySelectedLibs = alreadySelectedLibs;
            this.j2eePlatform = j2eePlatform;
            this.incompatibleLibs = incompatibleLibs;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName = null;
            if (value instanceof Library) {
                displayName = ((Library)value).getDisplayName();
            }
            super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            final String toolTipText;
            if (value instanceof Library) {
                final String libraryString = VisualClasspathSupport.getLibraryString((Library) value);
                if (libraryString == null) {
                    // #115897
                    toolTipText = null;
                } else if (alreadySelectedLibs.contains(value)) {
                    toolTipText = NbBundle.getMessage(LibrariesChooser.class, "LBL_LibraryAlreadyInProject_ToolTip") +
                            " !!!     (" + libraryString + ")";
                } else if (incompatibleLibs.contains(value)) {
                    toolTipText = NbBundle.getMessage(LibrariesChooser.class, "LBL_IncompatibleLibrary_ToolTip")
                            + " (" + j2eePlatform + ") !!!     (" + libraryString + ")";
                    setEnabled(false);
                } else {
                    toolTipText = libraryString;
                }
            } else {
                toolTipText = null;
            }
            setToolTipText(toolTipText);
            setIcon(createIcon());
            return this;
        }

        private synchronized Icon createIcon () {
            if (this.cachedIcon == null) {
                Image img = Utilities.loadImage(LIBRARY_ICON);
                this.cachedIcon = new ImageIcon (img);
            }
            return this.cachedIcon;
        }

    }

}
