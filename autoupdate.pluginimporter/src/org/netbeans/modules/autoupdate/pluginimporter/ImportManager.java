/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * ImportManager.java
 *
 * Created on Oct 29, 2008, 11:35:18 PM
 */

package org.netbeans.modules.autoupdate.pluginimporter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jirka Rechtacek
 */
public class ImportManager extends java.awt.Panel {
    private final File srcCluster;
    private final File dest;
    private final PluginImporter importer;
    private static ImportManager INSTANCE = null;
    private List<Boolean> checkedToInstall = Collections.emptyList ();
    private List<Boolean> checkedToImport = Collections.emptyList ();
    private List<UpdateElement> toInstall = Collections.emptyList ();
    private List<UpdateElement> toImport = Collections.emptyList ();
    private JButton bImport;
    private JButton bNo;

    /** Creates new form ImportManager */
    public ImportManager (File src, File dest, PluginImporter importer) {
        this.srcCluster = src;
        this.dest = dest;
        this.importer = importer;

        toInstall = new ArrayList<UpdateElement> (importer.getPluginsAvailableToInstall ());
        Collections.sort (toInstall, new Comparator<UpdateElement> () {
            public int compare (UpdateElement o1, UpdateElement o2) {
                return o1.getDisplayName ().compareTo (o2.getDisplayName ());
            }
        });
        checkedToInstall = new ArrayList<Boolean> (importer.getPluginsAvailableToInstall ().size ());
        checkedToInstall = new ArrayList<Boolean> (Collections.nCopies (importer.getPluginsAvailableToInstall ().size (), Boolean.TRUE));

        toImport = new ArrayList<UpdateElement> (importer.getPluginsToImport ());
        Collections.sort (toImport, new Comparator<UpdateElement> () {
            public int compare (UpdateElement o1, UpdateElement o2) {
                return o1.getDisplayName ().compareTo (o2.getDisplayName ());
            }
        });
        checkedToImport = new ArrayList<Boolean> (importer.getPluginsToImport ().size ());
        checkedToImport = new ArrayList<Boolean> (Collections.nCopies (importer.getPluginsToImport ().size (), Boolean.FALSE));

        initComponents();

        tpBroken.setEnabled (false);
        lBroken.setEnabled (importer.getBrokenPlugins ().isEmpty ());
        if (! importer.getBrokenPlugins ().isEmpty ()) {
            tpBroken.setText (importer.getBrokenPlugins ().toString ());
        }

        refreshUI ();
        INSTANCE = this;
    }

    public static ImportManager getInstance () {
        return INSTANCE;
    }

    public void notifyAvailable () {
        assert StatusLineImportIcon.icon != null : "StatusLineIcon is not null!";
        if (StatusLineImportIcon.icon == null) {
            return ;
        }
        remindLater ();
        String msg = NbBundle.getMessage (ImportManager.class,
                "ImportNotifier_PluginAvailableForImport", // NOI18N
                toImport.size () + toInstall.size ());
        String details = NbBundle.getMessage (ImportManager.class,
                "ImportNotifier_PluginAvailableForImport_Details", // NOI18N
                srcCluster);
        PluginManager.setStatusLineIconVisible (StatusLineImportIcon.icon, createBalloonContent (msg, details), true);
    }

    private static JComponent createBalloonContent (String msg, String details) {
        JPanel panel = new JPanel (new GridBagLayout ());
        panel.setOpaque (false);
        JLabel top = new JLabel (msg);
        top.setIcon (new ImageIcon (ImageUtilities.loadImage ("org/netbeans/modules/autoupdate/ui/resources/info_icon.png"))); //NOI18N
        top.setIconTextGap (10);
        panel.add (top, new GridBagConstraints (0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets (6, 0, 0, 0), 0, 0));

        panel.add (new JLabel (details),
                new GridBagConstraints (0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets (2, 0, 0, 0), 0, 0));
        return panel;
    }

    public void attachButtons (JButton bImport, JButton bNo) {
        this.bImport = bImport;
        this.bNo = bNo;
        bImport.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                if (doImport ()) {
                    doClose ();
                }
            }
        });
        refreshUI ();
    }

    public void remindLater () {
        Preferences p = NbPreferences.forModule (Installer.class);
        p.put (Installer.KEY_IMPORT_FROM, srcCluster.toString ());
    }

    public void dontRemind () {
        Preferences p = NbPreferences.forModule (Installer.class);
        try {
            p.clear ();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace (ex);
        }
    }

    private void doClose () {
        bNo.doClick (); // XXX
    }

    private boolean doImport () {
        boolean res = true;
        boolean wizardFinished = false;
        try {
            if (checkedToImport.indexOf (Boolean.TRUE) != -1) {
                Collection<UpdateElement> reallyToImport = new HashSet<UpdateElement> ();
                for (UpdateElement el : toImport) {
                    if (checkedToImport.get (toImport.indexOf (el))) {
                        reallyToImport.add (el);
                    }
                }
                importer.importPlugins (reallyToImport, srcCluster, dest);
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        toImport.clear ();
                        checkedToImport.clear ();
                        tToImport.setModel (getModel (toImport, checkedToImport));
                        refreshUI ();
                    }
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace (ex);
        }
        try {
            dontRemind ();
            if (checkedToInstall.indexOf (Boolean.TRUE) != -1) {
                OperationContainer<InstallSupport> oc = OperationContainer.createForInstall ();
                for (UpdateElement el : toInstall) {
                    if (checkedToInstall.get (toInstall.indexOf (el))) {
                        OperationContainer.OperationInfo<InstallSupport> info = oc.add (el);
                        if (info != null) {
                            oc.add (info.getRequiredElements ());
                        }
                    }
                }
                wizardFinished = PluginManager.openInstallWizard (oc);
                if (wizardFinished) {
                    toInstall.clear ();
                    checkedToInstall.clear ();
                 } else {
                    res = false;
                 }
            }
        } finally {
            tToInstall.setModel (getModel (toInstall, checkedToInstall));
            refreshUI ();
        }
        return res;

    }

    private TableModel getModel (final List<UpdateElement> plugins, final List<Boolean> checked) {
        return new AbstractTableModel () {
            public int getRowCount () {
                return plugins.size ();
            }

            public int getColumnCount () {
                return 2;
            }

            public Object getValueAt (int rowIndex, int columnIndex) {
                switch (columnIndex) {
                    case 0 :
                        return checked.get (rowIndex);
                    case 1 :
                        return plugins.get (rowIndex).getDisplayName ();
                }
                return null;
            }

            @Override
            public Class<?> getColumnClass (int columnIndex) {
                switch (columnIndex) {
                    case 0 :
                        return Boolean.class;
                    case 1 :
                        return String.class;
                }
                return null;
            }

            @Override
            public String getColumnName (int column) {
                switch (column) {
                    case 0 :
                        return NbBundle.getMessage (ImportManager.class, "ImportNotifier_Install");
                    case 1 :
                        return NbBundle.getMessage (ImportManager.class, "ImportNotifier_Plugin");
                }
                return null;
            }

            @Override
            public boolean isCellEditable (int rowIndex, int columnIndex) {
                switch (columnIndex) {
                    case 0 :
                        return true;
                    case 1 :
                        return false;
                }
                return false;
            }

            @Override
            public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
                switch (columnIndex) {
                    case 0 :
                        checked.set (rowIndex, ((Boolean) aValue));
                        refreshUI ();
                        break;
                    case 1 :
                        assert false : "Name is not editable.";
                        break;
                }
                return;
            }

        };
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        lToInstall = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tToInstall = new javax.swing.JTable();
        lToImport = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tToImport = new javax.swing.JTable();
        lBroken = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tpBroken = new javax.swing.JTextPane();
        lDesc = new javax.swing.JLabel();

        jScrollPane3.setViewportView(jTextPane1);

        lToInstall.setLabelFor(lToInstall);
        org.openide.awt.Mnemonics.setLocalizedText(lToInstall, org.openide.util.NbBundle.getMessage(ImportManager.class, "ImportManager.lToInstall.text")); // NOI18N

        tToInstall.setModel(getModel (toInstall, checkedToInstall));
        tToInstall.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tToInstall.setOpaque(false);
        jScrollPane1.setViewportView(tToInstall);

        lToImport.setLabelFor(tToImport);
        org.openide.awt.Mnemonics.setLocalizedText(lToImport, org.openide.util.NbBundle.getMessage(ImportManager.class, "ImportManager.lToImport.text")); // NOI18N

        tToImport.setModel(getModel (toImport, checkedToImport));
        tToImport.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tToImport.setOpaque(false);
        jScrollPane2.setViewportView(tToImport);

        lBroken.setLabelFor(tpBroken);
        org.openide.awt.Mnemonics.setLocalizedText(lBroken, org.openide.util.NbBundle.getMessage(ImportManager.class, "ImportManager.lBroken.text")); // NOI18N

        tpBroken.setEnabled(false);
        tpBroken.setOpaque(false);
        jScrollPane4.setViewportView(tpBroken);

        lDesc.setFont(lDesc.getFont().deriveFont(lDesc.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(lDesc, org.openide.util.NbBundle.getMessage(ImportManager.class, "ImportManager.lDesc.text", new Object[] {srcCluster})); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .add(lToInstall)
                    .add(lDesc)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .add(lToImport, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(lBroken, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 565, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lDesc)
                .add(7, 7, 7)
                .add(lToInstall)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lToImport)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lBroken)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel lBroken;
    private javax.swing.JLabel lDesc;
    private javax.swing.JLabel lToImport;
    private javax.swing.JLabel lToInstall;
    private javax.swing.JTable tToImport;
    private javax.swing.JTable tToInstall;
    private javax.swing.JTextPane tpBroken;
    // End of variables declaration//GEN-END:variables

    private void refreshUI () {
        lToImport.setEnabled (importer.getPluginsToImport ().size () > 0);
        tToImport.setEnabled (importer.getPluginsToImport ().size () > 0);

        lToImport.setEnabled (importer.getPluginsAvailableToInstall ().size () > 0);
        tToInstall.setEnabled (importer.getPluginsAvailableToInstall ().size () > 0);

        TableColumn activeColumn = tToImport.getColumnModel ().getColumn (0);
        activeColumn.setMaxWidth (tToImport.getTableHeader ().getHeaderRect (0).width);
        activeColumn = tToInstall.getColumnModel ().getColumn (0);
        activeColumn.setMaxWidth (tToInstall.getTableHeader ().getHeaderRect (0).width);

        if (bImport != null) {
            bImport.setEnabled (checkedToInstall.indexOf (Boolean.TRUE) != -1 || checkedToImport.indexOf (Boolean.TRUE) != -1);
        }
    }

}
