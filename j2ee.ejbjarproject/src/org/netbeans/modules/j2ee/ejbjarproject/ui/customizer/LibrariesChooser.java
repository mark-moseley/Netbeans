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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListeners;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import org.openide.util.Utilities;
/**
 *
 * @author  tz97951
 */
public class LibrariesChooser extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 109720779197839288L;

    private Set<Library> containedLibraries;

    /** Creates new form LibrariesChooser */
    public LibrariesChooser (final JButton addLibraryOption, Set<Library> containedLibraries) {
        this.containedLibraries = containedLibraries;
        initComponents();
        jList1.setPrototypeCellValue("0123456789012345678901234");      //NOI18N
        jList1.setModel(new LibrariesListModel());
        jList1.setCellRenderer(new LibraryRenderer());
        jList1.addListSelectionListener( new ListSelectionListener () {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                addLibraryOption.setEnabled (jList1.getSelectedIndices().length != 0);
            }
        });
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( LibrariesChooser.class );
    }

    public Library[] getSelectedLibraries () {
        Object[] selected = this.jList1.getSelectedValues();
        Library[] libraries = new Library[selected.length];
        System.arraycopy(selected,0,libraries,0,selected.length);
        return libraries;
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
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(LibrariesChooser.class).getString("AD_LibrariesChooser"));
        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(LibrariesChooser.class).getString("MNE_InstalledLibraries").charAt(0));
        jLabel1.setLabelFor(jList1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(LibrariesChooser.class).getString("CTL_InstalledLibraries"));
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
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(LibrariesChooser.class).getString("AD_jScrollPaneLibraries"));

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

        org.openide.awt.Mnemonics.setLocalizedText(edit, org.openide.util.NbBundle.getBundle(LibrariesChooser.class).getString("CTL_EditLibraries"));
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
        edit.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(LibrariesChooser.class).getString("AD_jButtonManageLibraries"));

    }
    // </editor-fold>//GEN-END:initComponents

    private void editLibraries(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLibraries
        LibrariesListModel model = (LibrariesListModel) jList1.getModel ();
        Collection<Library> oldLibraries = Arrays.asList(model.getLibraries());
        LibrariesCustomizer.showCustomizer((Library)this.jList1.getSelectedValue());
        List<Library> currentLibraries = Arrays.asList(model.getLibraries());
        Collection<Library> newLibraries = new ArrayList<Library>(currentLibraries);
        
        newLibraries.removeAll(oldLibraries);                
        int indexes[] = new int [newLibraries.size()];
        
        Iterator it = newLibraries.iterator();
        for (int i=0; it.hasNext();i++) {
            Library lib = (Library) it.next ();
            indexes[i] = currentLibraries.indexOf (lib);
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
        private static final long serialVersionUID = 22946764535447141L;

        private Library[] cache;

        public LibrariesListModel () {
            LibraryManager manager = LibraryManager.getDefault();
            manager.addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, this, manager));
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
            int oldSize = this.cache == null ? 0 : this.cache.length;
            this.cache = createLibraries();            
            int newSize = this.cache.length;            
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
            Arrays.sort(libs, new Comparator<Library>() {
                public int compare (Library o1, Library o2) {
                    String name1 = (o1).getDisplayName();
                    String name2 = (o2).getDisplayName();
                    return name1.compareToIgnoreCase(name2);
                }
            });
            return libs;
        }
    }


    private final class LibraryRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 120819570259435573L;
        
        private static final String LIBRARY_ICON = "org/netbeans/modules/java/j2seproject/ui/resources/libraries.gif";  //NOI18N               
        private Icon cachedIcon;
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName = null;
            String toolTip = null;
            Color color = null;
            if (value instanceof Library) {
                Library lib = ((Library)value);
                displayName = lib.getDisplayName();
//Commented out to be compatible with Web Project
//                if (containedLibraries.contains(lib)) {
//                    color = Color.GRAY;
//                    toolTip = NbBundle.getMessage(LibrariesChooser.class,"MSG_LibraryAlreadyIncluded");
//                }
            }
            super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
//Commented out to be compatible with Web Project            
//            if (toolTip!=null) {
//                setToolTipText (toolTip);
//            }

            setIcon(createIcon());
//Commented out to be compatible with Web Project
//            if (color != null) {
//                setForeground (color);
//            }
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
