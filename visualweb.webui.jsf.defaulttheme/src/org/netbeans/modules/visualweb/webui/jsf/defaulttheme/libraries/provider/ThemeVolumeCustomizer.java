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

package org.netbeans.modules.visualweb.webui.jsf.defaulttheme.libraries.provider;

import java.beans.Customizer;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Arrays;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.libraries.LibraryImplementation;




import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.spi.project.libraries.LibraryCustomizerContext;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author  tom
 */
public class ThemeVolumeCustomizer extends javax.swing.JPanel implements Customizer {

    private String volumeType;
    private LibraryImplementation impl;
    private VolumeContentModel model;
    private LibraryStorageArea area;
    private Boolean allowRelativePaths = null;

    /**
     * Creates new form ThemeVolumeCustomizer
     */
    ThemeVolumeCustomizer (String volumeType) {
        this.volumeType = volumeType;
        initComponents();
        postInitComponents ();
        this.setName (NbBundle.getMessage(ThemeVolumeCustomizer.class,"TXT_"+volumeType));
    }


    public void addNotify() {
        super.addNotify();
        this.addButton.requestFocus();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.addButton.setEnabled(enabled);
        if (this.addURLButton != null) {
            this.addURLButton.setEnabled(enabled);
        }
        int[] indices = content.getSelectedIndices();
        this.removeButton.setEnabled(enabled && indices.length > 0);
        this.downButton.setEnabled(enabled && indices.length > 0 && indices[indices.length-1]<model.getSize()-1);
        this.upButton.setEnabled(enabled && indices.length>0 && indices[0]>0);
    }


    private void postInitComponents () {
        this.content.setCellRenderer(new ContentRenderer());
        this.upButton.setEnabled (false);
        this.downButton.setEnabled (false);
        this.removeButton.setEnabled (false);
        if (this.volumeType.equals(ThemeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {  //NOI18N
            Mnemonics.setLocalizedText(this.addButton, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_AddClassPath"));
            Mnemonics.setLocalizedText(message, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_ContentClassPath"));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_AddClassPath"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_ContentClassPath"));
        }
        else if (this.volumeType.equals(ThemeLibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {  //NOI18N
            Mnemonics.setLocalizedText(addButton, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_AddJavadoc"));
            Mnemonics.setLocalizedText(message, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_ContentJavadoc"));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_AddJavadoc"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_ContentJavadoc"));
//            this.addURLButton = new JButton ();
//            Mnemonics.setLocalizedText(addURLButton, NbBundle.getMessage (ThemeVolumeCustomizer.class,"CTL_AddJavadocURL"));
//            this.addURLButton.addActionListener (new ActionListener () {
//                public void actionPerformed(ActionEvent e) {
//                    addURLResource ();
//                }
//            });
//            GridBagConstraints c = new GridBagConstraints();
//            c.gridx = 1;
//            c.gridy = 2;
//            c.gridwidth = GridBagConstraints.REMAINDER;
//            c.gridheight = 1;
//            c.fill = GridBagConstraints.HORIZONTAL;
//            c.anchor = GridBagConstraints.NORTHWEST;
//            c.insets = new Insets (0,6,5,6);
//            ((GridBagLayout)this.getLayout()).setConstraints(this.addURLButton,c);
//            this.add (this.addURLButton);
        }
        else if (this.volumeType.equals(ThemeLibraryTypeProvider.VOLUME_TYPE_SRC)) {  //NOI18N
            Mnemonics.setLocalizedText(this.addButton, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_AddSources"));
            Mnemonics.setLocalizedText(message, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_ContentSources"));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_AddSources"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_ContentSources"));
        }
        else if (this.volumeType.equals(ThemeLibraryTypeProvider.VOLUME_TYPE_RUNTIME)) {  //NOI18N
            Mnemonics.setLocalizedText(this.addButton, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_AddSources"));
            Mnemonics.setLocalizedText(message, NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_ContentSources"));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_AddSources"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ThemeVolumeCustomizer.class,"AD_ContentSources"));
        }
        this.content.addListSelectionListener(new ListSelectionListener () {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                int[] indices = content.getSelectedIndices();
                removeButton.setEnabled(indices.length > 0);
                downButton.setEnabled(indices.length > 0 && indices[indices.length-1]<model.getSize()-1);
                upButton.setEnabled(indices.length>0 && indices[0]>0);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        message = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        content = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(null);
        message.setLabelFor(content);
        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getBundle(ThemeVolumeCustomizer.class).getString("CTL_RemoveContent"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 2, 6);
        add(message, gridBagConstraints);

        jScrollPane1.setViewportView(content);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 6);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getBundle(ThemeVolumeCustomizer.class).getString("CTL_AddContent"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addResource(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 5, 6);
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(null);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getBundle(ThemeVolumeCustomizer.class).getString("CTL_RemoveContent"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeResource(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(null);

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getBundle(ThemeVolumeCustomizer.class).getString("CTL_UpContent"));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upResource(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 6);
        add(upButton, gridBagConstraints);
        upButton.getAccessibleContext().setAccessibleDescription(null);

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getBundle(ThemeVolumeCustomizer.class).getString("CTL_DownContent"));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downResource(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(downButton, gridBagConstraints);
        downButton.getAccessibleContext().setAccessibleDescription(null);

    }
    // </editor-fold>//GEN-END:initComponents

    private void downResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downResource
        int[] indices = this.content.getSelectedIndices();
        if (indices.length == 0 || indices[0] < 0 || indices[indices.length-1]>=model.getSize()-1) {
            return;
        }
        this.model.moveDown(indices);
        for (int i=0; i< indices.length; i++) {
            indices[i] = indices[i] + 1;
        }
        this.content.setSelectedIndices (indices);
    }//GEN-LAST:event_downResource

    private void upResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upResource
        int[] indices = this.content.getSelectedIndices();
        if (indices.length == 0 || indices[0] <= 0) {
            return;
        }
        this.model.moveUp(indices);
        for (int i=0; i< indices.length; i++) {
            indices[i] = indices[i] - 1;
        }
        this.content.setSelectedIndices(indices);
    }//GEN-LAST:event_upResource

    private void removeResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeResource
        int[] indices =this.content.getSelectedIndices();
        if (indices.length == 0) {
            return;
        }
        this.model.removeResources(indices);
        if (indices[indices.length-1]-indices.length+1 < this.model.getSize()) {
            this.content.setSelectedIndex(indices[indices.length-1]-indices.length+1);
        }
        else if (indices[0]  >= 1) {
            this.content.setSelectedIndex (indices[0]-1);
        }
    }//GEN-LAST:event_removeResource

    private void addResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addResource
        File baseFolder = null;
        if (allowRelativePaths != null && allowRelativePaths.booleanValue()) {
            baseFolder = new File(URI.create(area.getLocation().toExternalForm())).getParentFile();
        }
        FileChooser chooser = new FileChooser(baseFolder, baseFolder);
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setAcceptAllFileFilterUsed(false);
        if (this.volumeType.equalsIgnoreCase(ThemeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {        //NOI18N
            chooser.setMultiSelectionEnabled (true);
            chooser.setDialogTitle(NbBundle.getMessage(ThemeVolumeCustomizer.class,"TXT_OpenClasses"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new SimpleFileFilter(NbBundle.getMessage(
                    ThemeVolumeCustomizer.class,"TXT_Classpath"),new String[] {"ZIP","JAR"}));   //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_SelectCP"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(ThemeVolumeCustomizer.class,"MNE_SelectCP").charAt(0));
        }
        else if (this.volumeType.equalsIgnoreCase(ThemeLibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {     //NOI18N
            chooser.setDialogTitle(NbBundle.getMessage(ThemeVolumeCustomizer.class,"TXT_OpenJavadoc"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new SimpleFileFilter(NbBundle.getMessage(
                    ThemeVolumeCustomizer.class,"TXT_Javadoc"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_SelectJD"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(ThemeVolumeCustomizer.class,"MNE_SelectJD").charAt(0));
        }
        else if (this.volumeType.equalsIgnoreCase(ThemeLibraryTypeProvider.VOLUME_TYPE_SRC)) {         //NOI18N
            chooser.setDialogTitle(NbBundle.getMessage(ThemeVolumeCustomizer.class,"TXT_OpenSources"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new SimpleFileFilter(NbBundle.getMessage(
                    ThemeVolumeCustomizer.class,"TXT_Sources"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_SelectSRC"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(ThemeVolumeCustomizer.class,"MNE_SelectSRC").charAt(0));
        }
        else if (this.volumeType.equalsIgnoreCase(ThemeLibraryTypeProvider.VOLUME_TYPE_RUNTIME)) {         //NOI18N
            chooser.setDialogTitle(NbBundle.getMessage(ThemeVolumeCustomizer.class,"TXT_OpenSources"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new SimpleFileFilter(NbBundle.getMessage(
                    ThemeVolumeCustomizer.class,"TXT_Sources"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_SelectSRC"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(ThemeVolumeCustomizer.class,"MNE_SelectSRC").charAt(0));
        }
        if (lastFolder != null) {
            chooser.setCurrentDirectory (lastFolder);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                lastFolder = chooser.getCurrentDirectory();
                addFiles (chooser.getSelectedPaths(), area != null ? area.getLocation() : null);
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }//GEN-LAST:event_addResource


//    private void addURLResource () {
//        DialogDescriptor.InputLine input = new DialogDescriptor.InputLine (
//                NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_AddJavadocURLMessage"),
//                NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_AddJavadocURLTitle"));
//        if (DialogDisplayer.getDefault().notify(input) == DialogDescriptor.OK_OPTION) {
//            try {
//                String value = input.getInputText();
//                URL url = new URL (value);
//                this.model.addResource(url);
//                this.content.setSelectedIndex(this.model.getSize()-1);
//            } catch (MalformedURLException mue) {
//                DialogDescriptor.Message message = new DialogDescriptor.Message (
//                        NbBundle.getMessage(ThemeVolumeCustomizer.class,"CTL_InvalidURLFormat"),
//                        DialogDescriptor.ERROR_MESSAGE
//                );
//                DialogDisplayer.getDefault().notify(message);
//            }
//        }
//    }


    private void addFiles (String[] fileNames, URL libraryLocation) throws MalformedURLException {
        int firstIndex = this.model.getSize();
        for (int i = 0; i < fileNames.length; i++) {
            File f = new File(fileNames[i]);
            URI uri = LibrariesSupport.convertFilePathToURI(fileNames[i]);
            if (allowRelativePaths != null && allowRelativePaths.booleanValue()) {
                File realFile = f;
                if (!f.isAbsolute()) {
                    assert area != null;
                    if (area != null) {
                        realFile = FileUtil.normalizeFile(new File(
                            new File(URI.create(area.getLocation().toExternalForm())).getParentFile(), f.getPath()));
                    }
                }
                if (FileUtil.isArchiveFile(realFile.toURI().toURL())) {
                    uri = LibrariesSupport.getArchiveRoot(uri);
                } else if (!uri.toString().endsWith("/")){
                    try {
                        uri = new URI(uri.toString()+"/");
                    } catch (URISyntaxException ex) {
                        throw new AssertionError(ex);
                    }
                }
                model.addResource(uri);
            } else {
                assert f.isAbsolute() : f.getPath();
                URL url = FileUtil.normalizeFile(f).toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot(url);
                } else if (!url.toExternalForm().endsWith("/")){
                    url = new URL(url.toExternalForm()+"/");
                }
                model.addResource(url);
            }
            if (this.volumeType.equals(ComponentLibraryTypeProvider.VOLUME_TYPE_JAVADOC)
                && !JavadocForBinaryQueryLibraryImpl.isValidLibraryJavadocRoot (
                    LibrariesSupport.resolveLibraryEntryURI(libraryLocation, uri).toURL())) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ComponentVolumeCustomizer.class,"TXT_InvalidJavadocRoot", f.getPath()),
                    NotifyDescriptor.ERROR_MESSAGE));
                continue;
            }
        }        
        int lastIndex = this.model.getSize()-1;
        if (firstIndex<=lastIndex) {
            int[] toSelect = new int[lastIndex-firstIndex+1];
            for (int i = 0; i < toSelect.length; i++) {
                toSelect[i] = firstIndex+i;
            }
            this.content.setSelectedIndices(toSelect);
        }
    }
    
    public void setObject(Object bean) {
        assert bean instanceof LibraryCustomizerContext : bean.getClass();
        LibraryCustomizerContext context = (LibraryCustomizerContext)bean;
        area = context.getLibraryStorageArea();
        impl = context.getLibraryImplementation();
        allowRelativePaths = Boolean.valueOf(context.getLibraryImplementation2() != null);
        model = new VolumeContentModel(impl, area, volumeType);
        content.setModel(model);
        if (model.getSize()>0) {
            content.setSelectedIndex(0);
        }
    }        
    
    
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring(index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }


    private static File lastFolder = null;


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList content;
    private javax.swing.JButton downButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel message;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
    private JButton addURLButton;

    private static class ContentRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName = null;
            Color color = null;
            String toolTip = null;
            
            URI uri = null;
            if (value instanceof URI) {
                uri = (URI)value;
            } else if (value instanceof URL) {
                try {
                    uri = ((URL) value).toURI();
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (uri != null) {
                if (uri.toString().contains("!/")) {   //NOI18N
                    uri = LibrariesSupport.getArchiveFile(uri);
                }
                boolean broken = false;
                VolumeContentModel model = (VolumeContentModel)list.getModel();
                LibraryStorageArea area = model.getArea();
                FileObject fo = LibrariesSupport.resolveLibraryEntryFileObject(area != null ? area.getLocation() : null, uri);
                if (fo == null) {
                    broken = true;
                    if ("file".equals(uri.getScheme())) { //NOI18N
                        displayName = LibrariesSupport.convertURIToFilePath(uri);
                    } else {
                        displayName = uri.toString();
                    }
                } else {
                    if (uri.isAbsolute()) {
                        displayName = FileUtil.getFileDisplayName(fo);
                    } else {
                        displayName = LibrariesSupport.convertURIToFilePath(uri);
                        toolTip = FileUtil.getFileDisplayName(fo);
                    }
                }
                if (broken) {
                    color = new Color (164,0,0);
                    toolTip = NbBundle.getMessage (ThemeVolumeCustomizer.class,"TXT_BrokenFile");
                }
            }
            Component c = super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            if (c instanceof JComponent) {
                if (color != null) {
                    ((JComponent)c).setForeground (color);
                }
                if (toolTip != null) {
                    ((JComponent)c).setToolTipText(toolTip);
                } else {
                    ((JComponent)c).setToolTipText(null);
                }
            }
            return c;
        }

    }

}
