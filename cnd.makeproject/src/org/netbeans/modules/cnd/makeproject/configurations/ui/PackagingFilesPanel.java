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
package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.loaders.CoreElfObject;
import org.netbeans.modules.cnd.loaders.ExeObject;
import org.netbeans.modules.cnd.loaders.OrphanedElfObject;
import org.netbeans.modules.cnd.loaders.ShellDataObject;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement.FileType;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

public class PackagingFilesPanel extends ListEditorPanel {

    private String baseDir;
    private JTable targetList;
    private MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();
    private JButton addButton;
    private JButton addFileOrDirectoryButton;
    private JButton addFilesButton;
    private JButton addLinkButton;
    private PackagingFilesOuterPanel packagingFilesOuterPanel;

    public PackagingFilesPanel(List<FileElement> fileList, String baseDir) {
        super(fileList.toArray(), new JButton[]{new JButton(), new JButton(), new JButton(), new JButton()});
        getAddButton().setVisible(false);
        this.baseDir = baseDir;
        this.addButton = extraButtons[0];
        this.addFileOrDirectoryButton = extraButtons[1];
        this.addFilesButton = extraButtons[2];
        this.addLinkButton = extraButtons[3];

        addButton.setText(getString("PackagingFilesPanel.addButton.text"));
	addButton.setMnemonic(getString("PackagingFilesPanel.addButton.mn").charAt(0));
        addButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addButton.ad"));
        addButton.addActionListener(new AddButtonAction());
        
        addFileOrDirectoryButton.setText(getString("PackagingFilesPanel.addFileOrDirButton.text"));
	addFileOrDirectoryButton.setMnemonic(getString("PackagingFilesPanel.addFileOrDirButton.mn").charAt(0));
        addFileOrDirectoryButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addFileOrDirButton.ad"));
        addFileOrDirectoryButton.addActionListener(new AddFileOrDirectoryButtonAction());
        
        addFilesButton.setText(getString("PackagingFilesPanel.addFilesButton.text"));
	addFilesButton.setMnemonic(getString("PackagingFilesPanel.addFilesButton.mn").charAt(0));
        addFilesButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addFilesButton.ad"));
        addFilesButton.addActionListener(new AddFilesButtonAction());
        
        addLinkButton.setText(getString("PackagingFilesPanel.addLinkButton.text"));
	addLinkButton.setMnemonic(getString("PackagingFilesPanel.addLinkButton.mn").charAt(0));
        addLinkButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addLinkButton.ad"));
        addLinkButton.addActionListener(new AddLinkButtonAction());

        getEditButton().setVisible(false);
        getDefaultButton().setVisible(false);
    }
    
    public void setOuterPanel(PackagingFilesOuterPanel packagingFilesOuterPanel) {
        this.packagingFilesOuterPanel = packagingFilesOuterPanel;
    }

    class AddButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String topFolder = packagingFilesOuterPanel.getTopDirectoryTextField().getText();
            if (topFolder.length() > 0 && !topFolder.endsWith("/")) { // NOI18N
                topFolder += "/"; // NOI18N
            }
            addObjectAction(new FileElement(FileType.UNKNOWN, "", topFolder)); // NOI18N
        }
    }
    
    
    class AddLinkButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
	    PackagingNewLinkPanel packagingNewEntryPanel = new PackagingNewLinkPanel(packagingFilesOuterPanel.getTopDirectoryTextField().getText());
	    DialogDescriptor dialogDescriptor = new DialogDescriptor(packagingNewEntryPanel, getString("AddNewLinkDialogTitle"));
	    packagingNewEntryPanel.setDialogDesriptor(dialogDescriptor);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
	    if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION)
		return;
            addObjectAction(new FileElement(
                    FileType.SOFTLINK,
                    packagingNewEntryPanel.getLink(),
                    packagingNewEntryPanel.getName(),
                    "", // packagingFilesOuterPanel.getFilePermTextField().getText(),
                    packagingFilesOuterPanel.getOwnerTextField().getText(),
                    packagingFilesOuterPanel.getGroupTextField().getText()
            ));
        }
    }

    class AddFileOrDirectoryButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = baseDir;
            }
            FileChooser fileChooser = new FileChooser(getString("FileChooserFileTitle"), getString("FileChooserButtonText"), FileChooser.FILES_AND_DIRECTORIES, null, seed, false);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            fileChooser.setMultiSelectionEnabled(true);
            int ret = fileChooser.showOpenDialog(null);
            if (ret == FileChooser.CANCEL_OPTION) {
                return;
            }
            File[] files = fileChooser.getSelectedFiles();
            for (int i = 0; i < files.length; i++) {
                String itemPath;
                if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                    itemPath = IpeUtils.toAbsoluteOrRelativePath(baseDir, files[i].getPath());
                } else if (PathPanel.getMode() == PathPanel.REL) {
                    itemPath = IpeUtils.toRelativePath(baseDir, files[i].getPath());
                } else {
                    itemPath = files[i].getPath();
                }
                itemPath = FilePathAdaptor.mapToRemote(itemPath);
                itemPath = FilePathAdaptor.normalize(itemPath);
                String topFolder = packagingFilesOuterPanel.getTopDirectoryTextField().getText();
                if (topFolder.length() > 0 && !topFolder.endsWith("/")) { // NOI18N
                    topFolder += "/"; // NOI18N
                }
                if (files[i].isDirectory()) {
                    addObjectAction(new FileElement(
                            FileType.DIRECTORY,
                            "", // NOI18N
                            topFolder + files[i].getName(),
                            packagingFilesOuterPanel.getDirPermTextField().getText(),
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText()
                    )); // FIXUP: softlink
                }
                else {
                    // Regular file
                    String perm;
                    if (isExecutable(files[i])) {
                        perm = packagingFilesOuterPanel.getDirPermTextField().getText();
                    }
                    else {
                        perm = packagingFilesOuterPanel.getFilePermTextField().getText();
                    }
                    addObjectAction(new FileElement(
                            FileType.FILE,
                            itemPath,
                            topFolder + files[i].getName(),
                            perm,
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText()
                    ));
                }
            }
        }
    }
    
    private boolean isExecutable(File file) {
        FileObject fo = null;
        
        if (file.getName().endsWith(".exe")) { //NOI18N
            return true;
        }
        
        try {
            fo = FileUtil.toFileObject(file.getCanonicalFile());
        } catch (IOException e) {
            return false;
        }
        
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            return false;
        }
        if (dataObject instanceof ExeObject || dataObject instanceof ShellDataObject) {
            if (dataObject instanceof OrphanedElfObject || dataObject instanceof CoreElfObject) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    class AddFilesButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = baseDir;
            }
            FileChooser fileChooser = new FileChooser(getString("FileChooserFilesTitle"), getString("FileChooserButtonText"), FileChooser.DIRECTORIES_ONLY, null, seed, false);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            fileChooser.setMultiSelectionEnabled(false);
            int ret = fileChooser.showOpenDialog(null);
            if (ret == FileChooser.CANCEL_OPTION) {
                return;
            }
            File dir = fileChooser.getSelectedFile();
            addFilesFromDirectory(dir, dir);
        }
        
        private void addFilesFromDirectory(File origDir, File dir) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    addFilesFromDirectory(origDir, files[i]);
                }
                else {
                    String path;
                    if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                        path = IpeUtils.toAbsoluteOrRelativePath(baseDir, files[i].getPath());
                    } else if (PathPanel.getMode() == PathPanel.REL) {
                        path = IpeUtils.toRelativePath(baseDir, files[i].getPath());
                    } else {
                        path = files[i].getPath();
                    }
                    path = FilePathAdaptor.mapToRemote(path);
                    path = FilePathAdaptor.normalize(path);
                    String toFile = IpeUtils.toRelativePath(origDir.getParentFile().getAbsolutePath(), files[i].getPath());
                    toFile = FilePathAdaptor.mapToRemote(toFile);
                    toFile = FilePathAdaptor.normalize(toFile);
                    String topFolder = packagingFilesOuterPanel.getTopDirectoryTextField().getText();
                    if (topFolder.length() > 0 && !topFolder.endsWith("/")) { // NOI18N
                        topFolder += "/"; // NOI18N
                    }
                    String perm;
                    if (files[i].getName().endsWith(".exe") || files[i].isDirectory() || isExecutable(files[i])) { //NOI18N
                        perm = packagingFilesOuterPanel.getDirPermTextField().getText();
                    }
                    else {
                        perm = packagingFilesOuterPanel.getFilePermTextField().getText();
                    }
                    addObjectAction(new FileElement(
                            FileType.FILE,
                            path,
                            topFolder + toFile,
                            perm,
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText()
                    )); // FIXUP: softlink
                }
            }
        }
    }

    @Override
    public Object copyAction(Object o) {
        FileElement elem = (FileElement) o;
        return new FileElement(elem.getType(), new String(elem.getFrom()), new String(elem.getTo()));
    }

    @Override
    public String getCopyButtonText() {
        return getString("PackagingFilesPanel.duplicateButton.text");
    }
    
    @Override
    public char getCopyButtonMnemonics() {
        return getString("PackagingFilesPanel.duplicateButton.mn").charAt(0);
    }
    
    @Override
    public String getCopyButtonAD() {
        return getString("PackagingFilesPanel.duplicateButton.ad");
    }

    @Override
    public String getListLabelText() {
        return getString("PackagingFilesPanel.listlabel.text");
    }
    
    @Override
    public char getListLabelMnemonic() {
        return getString("PackagingFilesPanel.listlabel.mn").charAt(0);
    }

    // Overrides ListEditorPanel
    @Override
    public int getSelectedIndex() {
        int index = getTargetList().getSelectedRow();
        if (index >= 0 && index < listData.size()) {
            return index;
        } else {
            return 0;
        }
    }

    @Override
    protected void setSelectedIndex(int i) {
        getTargetList().getSelectionModel().setSelectionInterval(i, i);
    }

    @Override
    protected void setData(Vector data) {
        getTargetList().setModel(new MyTableModel());
        // Set column sizes
        getTargetList().getColumnModel().getColumn(0).setPreferredWidth(40);
        getTargetList().getColumnModel().getColumn(0).setMaxWidth(40);
        if (getTargetList().getColumnModel().getColumnCount() >= 4) {
            getTargetList().getColumnModel().getColumn(3).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(3).setMaxWidth(50);
        }
        if (getTargetList().getColumnModel().getColumnCount() >= 6) {
            getTargetList().getColumnModel().getColumn(4).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(4).setMaxWidth(50);
            getTargetList().getColumnModel().getColumn(5).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(5).setMaxWidth(50);
        }
        //
        getTargetList().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTargetList().getSelectionModel().addListSelectionListener(new TargetSelectionListener());
        // Left align table header
        ((DefaultTableCellRenderer) getTargetList().getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private class TargetSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            checkSelection();
        }
    }

    @Override
    protected void ensureIndexIsVisible(int selectedIndex) {
        // FIXUP...
        //targetList.ensureIndexIsVisible(selectedIndex);
        //java.awt.Rectangle rect = targetList.getCellRect(selectedIndex, 0, true);
        //targetList.scrollRectToVisible(rect);
    }

    @Override
    protected Component getViewComponent() {
        return getTargetList();
    }

    private JTable getTargetList() {
        if (targetList == null) {
            targetList = new MyTable();
            setData(null);
            getListLabel().setLabelFor(targetList);
        }
        return targetList;
    }

    class MyTable extends JTable {

        public MyTable() {
//	    //setTableHeader(null); // Hides table headers
//	    if (getRowHeight() < 19)
//		setRowHeight(19);
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N
            
            putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        }

        @Override
        public Color getGridColor() {
            return new Color(225, 225, 225);
        }

//        @Override
//        public boolean getShowHorizontalLines() {
//            return false;
//        }
//
//        @Override
//        public boolean getShowVerticalLines() {
//            return false;
//        }

        
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return myTableCellRenderer;
        }
        
        @Override
	public TableCellEditor getCellEditor(int row, int col) {
	    if (col == 0) {
                FileElement elem = (FileElement) listData.elementAt(row);
                
                JComboBox comboBox = new JComboBox();
                comboBox.addItem(FileType.FILE);
                comboBox.addItem(FileType.DIRECTORY);
                comboBox.addItem(FileType.SOFTLINK);
                if (elem.getType() == FileElement.FileType.DIRECTORY) {
                    comboBox.setSelectedIndex(1);
                }
                else if (elem.getType() == FileElement.FileType.SOFTLINK) {
                    comboBox.setSelectedIndex(2);
                }
                else {
                    comboBox.setSelectedIndex(0);
                }
                return new DefaultCellEditor(comboBox);
	    }
            else {
		return super.getCellEditor(row, col);
            }
	}
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            FileElement elem = (FileElement) listData.elementAt(row);
            if (col == 0) {
                    label.setText(elem.getType().toString());
            } else if (col == 1) {
                if (elem.getType() == FileElement.FileType.SOFTLINK) {
                    String msg = getString("Softlink_tt", elem.getTo() + "->" + elem.getFrom()); // NOI18N
                    label.setToolTipText(msg);
                }
                else if (elem.getType() == FileElement.FileType.DIRECTORY) {
                    String msg = getString("Directory_tt", elem.getTo()); // NOI18N
                    label.setToolTipText(msg);
                }
                else if (elem.getType() == FileElement.FileType.FILE) {
                    String msg = getString("File_tt", (new File(IpeUtils.toAbsolutePath(baseDir, elem.getFrom())).getAbsolutePath())); // NOI18N
                    label.setToolTipText(msg);
                }
                return label;
                
            } else if (col == 2) {
                if (elem.getType() == FileElement.FileType.DIRECTORY) {
                    return label; // Already set to blank
                }
                if (elem.getType() == FileElement.FileType.SOFTLINK) {
                    return label; // OK
                }
                if (!isSelected) {
                    label = new JLabel();
                }
                File file = new File(IpeUtils.toAbsolutePath(baseDir, elem.getFrom()));
                if (!isSelected && !file.exists()) {
                    label.setForeground(Color.RED);
                }
                label.setText(elem.getFrom());
            }
//            else {
//                label.setText(elem.getTo());
//            }
            return label;
        }
    }
    
    /*
     * Can be overridden to show fewer colums
     */
    public int getActualColumnCount() {
        return 6;
    }

    class MyTableModel extends DefaultTableModel {

        private String[] columnNames = {
            getString("PackagingFilesOuterPanel.column.0.text"),
            getString("PackagingFilesOuterPanel.column.1.text"),
            getString("PackagingFilesOuterPanel.column.2.text"),
            getString("PackagingFilesOuterPanel.column.3.text"),
            getString("PackagingFilesOuterPanel.column.4.text"),
            getString("PackagingFilesOuterPanel.column.5.text")
        };

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getColumnCount() {
            return getActualColumnCount();
        }

        @Override
        public int getRowCount() {
            return listData.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
//            return listData.elementAt(row);
            FileElement elem = (FileElement) listData.elementAt(row);
            if (col == 0) {
                return elem.getType();
            }
            if (col == 2) {
                if (elem.getType() == FileElement.FileType.DIRECTORY) {
                    return ""; // NOI18N
                }
                else {
                    return elem.getFrom();
                }
            }
            if (col == 1) {
                return elem.getTo();
            }
            if (col == 3) {
                return elem.getPermission();
            }
            if (col == 4) {
                return elem.getOwner();
            }
            if (col == 5) {
                return elem.getGroup();
            }
            assert false;
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return true;
            } else {
                return true;
            }
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            FileElement elem = (FileElement) listData.elementAt(row);
            if (col == 0) {
                FileType fileType = (FileType)val;
                if (fileType == FileType.FILE) {
                    elem.setType(fileType);
                    elem.setPermission(packagingFilesOuterPanel.getFilePermTextField().getText());
                    elem.setOwner(packagingFilesOuterPanel.getOwnerTextField().getText());
                    elem.setGroup(packagingFilesOuterPanel.getGroupTextField().getText());
                }
                else if (fileType == FileType.DIRECTORY) {
                    elem.setType(fileType);
                    elem.setPermission(packagingFilesOuterPanel.getDirPermTextField().getText());
                    elem.setOwner(packagingFilesOuterPanel.getOwnerTextField().getText());
                    elem.setGroup(packagingFilesOuterPanel.getGroupTextField().getText());
                }
                else if (fileType == FileType.SOFTLINK) {
                    elem.setType(fileType);
                    elem.setPermission(""); // NOI18N
                    elem.setOwner(""); // NOI18N
                    elem.setGroup(""); // NOI18N
                }
                else {
                    assert false;
                }
                
                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 2) {
                elem.setFrom((String)val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 1) {
                elem.setTo((String)val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 3) {
                elem.setPermission((String)val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 4) {
                elem.setOwner((String)val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 5) {
                elem.setGroup((String)val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else {
                assert false;
            }
        }
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PackagingFilesPanel.class);
        }
        return bundle.getString(s);
    }
    
    private static String getString(String s, String a1) {
        return NbBundle.getMessage(PackagingFilesPanel.class, s, a1);
    }

}
