/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.WebProjectGenerator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
/**
 *
 * @author  tz97951
 */
public class LibrariesChooser extends javax.swing.JPanel {
    private Collection incompatibleLibs;

    /** Creates new form LibrariesChooser */
    public LibrariesChooser(Collection alreadySelectedLibs, String j2eePlatform) {
        initComponents();
        jList1.setPrototypeCellValue("0123456789012345678901234");      //NOI18N
        jList1.setModel(new LibrariesListModel(alreadySelectedLibs, j2eePlatform));
        incompatibleLibs =
                VisualClasspathSupport.getLibrarySet(WebProjectGenerator.getIncompatibleLibraries(j2eePlatform));
        jList1.setCellRenderer(new LibraryRenderer(alreadySelectedLibs, incompatibleLibs, j2eePlatform));
    }

    public Library[] getSelectedLibraries () {
        Object[] selected = this.jList1.getSelectedValues();
        Collection libs = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            final Library lib = (Library) selected[i];
            if(!incompatibleLibs.contains(lib)) {   // incompatible libraries are not added
                libs.add(lib);
            }
        }
        return (Library[]) libs.toArray(new Library[libs.size()]);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        edit = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 250));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("AD_LibrariesChooser"));
        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("MNE_InstalledLibraries").charAt(0));
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("CTL_InstalledLibraries"));
        jLabel1.setLabelFor(jList1);
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
        jList1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("AD_jScrollPaneLibraries"));

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

        edit.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("CTL_EditLibraries"));
        edit.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("MNE_EditLibraries").charAt(0));
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
        edit.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("AD_jButtonManageLibraries"));

    }//GEN-END:initComponents

    private void editLibraries(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLibraries
        LibrariesListModel model = (LibrariesListModel) jList1.getModel ();
        Collection oldLibraries = Arrays.asList(model.getLibraries());
        LibrariesCustomizer.showCustomizer((Library)this.jList1.getSelectedValue());
        List currentLibraries = Arrays.asList(model.getLibraries());
        Collection newLibraries = new ArrayList (currentLibraries);

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

        private Library[] cache;
        /** No of libs in LibraryManager when last refreshed */
        private int numberOfLibs;
        private Collection alreadySelectedLibs;
        private String j2eePlatform;

        private static Collection filter13 = VisualClasspathSupport.getLibrarySet(
                WebProjectGenerator.getIncompatibleLibraries(WebModule.J2EE_13_LEVEL));
        private static Collection filter14 = VisualClasspathSupport.getLibrarySet(
                WebProjectGenerator.getIncompatibleLibraries(WebModule.J2EE_14_LEVEL));

        public LibrariesListModel (Collection alreadySelectedLibs, String j2eePlatform) {
            this.j2eePlatform = j2eePlatform;
            this.alreadySelectedLibs = alreadySelectedLibs;
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
            Collection filterOut = j2eePlatform.equals("1.3") ? filter13 : filter14;
//            final Collection baseLibraries = VisualClasspathSupport.getBaseLibrarySet();
//            ArrayList asList = new ArrayList ();
//            for (int i = 0; i < libs.length; i++) {
//                final Library lib = libs[i];
//                if (alreadySelectedLibs.contains(lib)) {
//                    continue;
//                }
//                if (filterOut.contains (lib)) {
//                    continue;
//                }
//                asList.add(lib);
//            }
//            libs = (Library[]) asList.toArray(new Library [asList.size()]);
            Arrays.sort(libs, new Comparator () {
                public int compare (Object o1, Object o2) {
                    assert (o1 instanceof Library) && (o2 instanceof Library);
                    String name1 = ((Library)o1).getDisplayName();
                    String name2 = ((Library)o2).getDisplayName();
                    return name1.compareToIgnoreCase(name2);
                }
            });
            return libs;
        }
    }


    private static final class LibraryRenderer extends DefaultListCellRenderer {

        private static final String LIBRARY_ICON = "org/netbeans/modules/web/project/ui/resources/libraries.gif";  //NOI18N
        private Icon cachedIcon;
        private Collection alreadySelectedLibs;
        private Collection incompatibleLibs;
        private String j2eePlatform;

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
            final Color foreground;
            final String toolTipText;
            if(alreadySelectedLibs.contains(value)) {
                foreground = Color.LIGHT_GRAY;
                toolTipText = NbBundle.getMessage(LibrariesChooser.class, "LBL_LibraryAlreadyInProject_ToolTip");
            } else if(incompatibleLibs.contains(value)) {
                foreground = Color.RED;
                toolTipText = NbBundle.getMessage(LibrariesChooser.class, "LBL_IncompatibleLibrary_ToolTip")
                        + " (" + j2eePlatform + ")";
                isSelected = false; // selection of incompatible libraries is here only masked
            } else {
                foreground = null;
                toolTipText = null;
            }
            super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            setToolTipText(toolTipText);
            setIcon(createIcon());
            if(foreground != null) {
                setForeground(foreground);
            }
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
