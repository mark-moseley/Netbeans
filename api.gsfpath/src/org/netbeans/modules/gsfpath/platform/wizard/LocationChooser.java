/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.gsfpath.platform.wizard;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.netbeans.modules.gsfpath.platform.PlatformSettings;
import org.netbeans.spi.gsfpath.platform.PlatformInstall;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  sd99038, Tomas  Zezula
 */
public class LocationChooser extends JFileChooser implements PropertyChangeListener {
        

    private static final Dimension PREFERRED_SIZE = new Dimension (500,340);
    
    private WizardDescriptor.InstantiatingIterator iterator;
    private LocationChooser.Panel firer;
    private PlatformFileView platformFileView;
    private PlatformAccessory accessory;
    

    public LocationChooser (LocationChooser.Panel firer) {
        super ();
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.setName (NbBundle.getMessage(LocationChooser.class,"TXT_PlatformFolderTitle"));
        this.setFileSelectionMode(DIRECTORIES_ONLY);
        this.setMultiSelectionEnabled(false);
        this.setControlButtonsAreShown(false);
        this.accessory = new PlatformAccessory ();
        this.setFileFilter (new PlatformFileFilter());
        this.setAccessory (this.accessory);
        this.firer = firer;
        this.platformFileView = new PlatformFileView( this.getFileSystemView());
        this.setFileView(this.platformFileView);
        this.addPropertyChangeListener (this);
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LocationChooser.class,"AD_LocationChooser"));

        //XXX JFileChooser workaround
        getActionMap().put("cancel",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        Container parent = LocationChooser.this.getParent();
                        do {
                            parent = parent.getParent();
                        } while (parent != null && !(parent instanceof Window));
                        if (parent != null) {
                            ((Window)parent).setVisible(false);
                        }
        }});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        
    }

    
    public Dimension getPreferredSize () {
        return PREFERRED_SIZE;
    }
    


    public void propertyChange(PropertyChangeEvent evt) {
        if (SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
            this.iterator = null;
            this.accessory.setType ("");    //NOI18N
            File file = this.getSelectedFile();
            if (file != null) {
                file = FileUtil.normalizeFile(file);
                FileObject fo = FileUtil.toFileObject (FileUtil.normalizeFile(file));
                if (fo != null) {                    
                    PlatformInstall install = this.platformFileView.getPlatformInstall();                    
                    if (install != null && install.accept(fo)) {
                        this.accessory.setType (install.getDisplayName());
                        this.iterator = install.createIterator(fo);
                    }
                }
            }
            this.firer.cs.fireChange();
        }
    }


    private boolean valid () {
        return this.getInstaller() != null;
    }

    private void read (WizardDescriptor settings) {
        PlatformSettings ps = PlatformSettings.getDefault();
        if (ps !=null) {
            this.setCurrentDirectory(ps.getPlatformsFolder());
        }
    }

    private void store (WizardDescriptor settings) {
        File dir = this.getCurrentDirectory();
        if (dir != null) {
            PlatformSettings ps = PlatformSettings.getDefault();
            if (ps != null) {
                ps.setPlatformsFolder(dir);
            }
        }
    }

    private WizardDescriptor.InstantiatingIterator getInstaller () {
        return this.iterator;
    }
    
    private void setPlatformInstall (PlatformInstall platformInstall) {
        this.platformFileView.setPlatformInstall(platformInstall);
    }
    
    private PlatformInstall getPlatformInstall () {
        return this.platformFileView.getPlatformInstall ();
    }
    
    private static class PlatformFileFilter extends FileFilter {

        public boolean accept(File f) {
            return f.isDirectory();
        }

        public String getDescription() {
            return NbBundle.getMessage (LocationChooser.class,"TXT_PlatformFolder");
        }
    }

    private static class PlatformAccessory extends JPanel {

        private JTextField tf;

        public PlatformAccessory () {
            this.initComponents ();
        }

        private void setType (String type) {
            this.tf.setText(type);
        }
        

        private void initComponents () {
            this.getAccessibleContext().setAccessibleName (NbBundle.getMessage(LocationChooser.class,"AN_LocationChooserAccessiory"));
            this.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(LocationChooser.class,"AD_LocationChooserAccessiory"));
            GridBagLayout l = new GridBagLayout();
            this.setLayout (l);
            JLabel label = new JLabel (NbBundle.getMessage(LocationChooser.class,"TXT_PlatformType"));
            label.setDisplayedMnemonic (NbBundle.getMessage(LocationChooser.class,"MNE_PlatformType").charAt(0));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (0,12,3,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            l.setConstraints(label,c);
            this.add (label);
            this.tf = new JTextField();
            this.tf.setColumns(15);
            this.tf.setEditable(false);
            this.tf.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LocationChooser.class,"AD_PlatformType"));
            c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (3,12,12,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            l.setConstraints(this.tf,c);
            this.add (tf);
            label.setLabelFor (this.tf);
            JPanel fill = new JPanel ();
            c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (0,12,12,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = c.weighty = 1.0;
            l.setConstraints(fill,c);
            this.add (fill);
        }        
    }


    /**
     * Controller for the LocationChooser panel.
     */
    public static class Panel implements WizardDescriptor.Panel {
            
        LocationChooser             component;
        private final ChangeSupport cs = new ChangeSupport(this);

        public java.awt.Component getComponent() {
            if (component == null) {
                this.component = new LocationChooser (this);
            }
            return component;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx (LocationChooser.class);
        }
        
        public boolean isValid() {
            return ((LocationChooser)this.getComponent()).valid();
        }
        
        public void readSettings(Object settings) {
            ((LocationChooser)this.getComponent()).read ((WizardDescriptor) settings);
        }
        
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        public void storeSettings(Object settings) {
            ((LocationChooser)this.getComponent()).store((WizardDescriptor)settings);
        }
        
        /**
         * Returns the currently selected installer.
         */
        WizardDescriptor.InstantiatingIterator getInstallerIterator() {
            return ((LocationChooser)this.getComponent()).getInstaller ();
        }
        
        void setPlatformInstall (PlatformInstall platformInstall) {
            ((LocationChooser)this.getComponent ()).setPlatformInstall (platformInstall);
        }
        
        PlatformInstall getPlatformInstall () {
            return ((LocationChooser)this.getComponent ()).getPlatformInstall ();
        }
        
    }
    
    private static class MergedIcon implements Icon {
        
        private Icon icon1;
        private Icon icon2;
        private int xMerge;
        private int yMerge;
        
        MergedIcon( Icon icon1, Icon icon2, int xMerge, int yMerge ) {
            
            this.icon1 = icon1;
            this.icon2 = icon2;
            
            if ( xMerge == -1 ) {
                xMerge = icon1.getIconWidth() - icon2.getIconWidth();
            }
            
            if ( yMerge == -1 ) {
                yMerge = icon1.getIconHeight() - icon2.getIconHeight();
            }
            
            this.xMerge = xMerge;
            this.yMerge = yMerge;
        }
        
        public int getIconHeight() {
            return Math.max( icon1.getIconHeight(), yMerge + icon2.getIconHeight() );
        }
        
        public int getIconWidth() {
            return Math.max( icon1.getIconWidth(), yMerge + icon2.getIconWidth() );
        }
        
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            icon1.paintIcon( c, g, x, y );
            icon2.paintIcon( c, g, x + xMerge, y + yMerge );
        }
        
    }
    
    private static class PlatformFileView extends FileView {
        
        private static final Icon BADGE = new ImageIcon(Utilities.loadImage("org/netbeans/modules/gsfpath/platform/resources/platformBadge.gif")); // NOI18N
        private static final Icon EMPTY = new ImageIcon(Utilities.loadImage("org/netbeans/modules/gsfpath/platform/resources/empty.gif")); // NOI18N
        
        private FileSystemView fsv;
        private Icon lastOriginal;
        private Icon lastMerged;
        private PlatformInstall platformInstall;
        
        public PlatformFileView( FileSystemView fsv) {
            this.fsv = fsv;            
        }
                
        public Icon getIcon(File _f) {
            File f = FileUtil.normalizeFile(_f);
            Icon original = fsv.getSystemIcon(f);
            if (original == null) {
                // L&F (e.g. GTK) did not specify any icon.
                original = EMPTY;
            }
            if ( isPlatformDir( f ) ) {
                if ( original.equals( lastOriginal ) ) {
                    return lastMerged;
                }
                lastOriginal = original;
                lastMerged = new MergedIcon(original, BADGE, -1, -1);                
                return lastMerged;
            }
            else {
                return original;
            }
        }
        
        public void setPlatformInstall (PlatformInstall platformInstall) {
            this.platformInstall = platformInstall;
        }
        
        public PlatformInstall getPlatformInstall () {
            return this.platformInstall;
        }
        
        
        private boolean isPlatformDir ( File f ) {
            FileObject fo = (f != null) ? convertToValidDir(f) : null;
            if (fo != null) {
                //XXX: Workaround of /net folder on Unix, the folders in the root are not badged as platforms.
                // User can still select them.
                try {
                    if (Utilities.isUnix() && (fo.getParent() == null || fo.getFileSystem().getRoot().equals(fo.getParent()))) {
                        return false;
                    }
                } catch (FileStateInvalidException e) {
                    return false;
                }
                if (this.platformInstall.accept(fo)) {
                    return true;
                }
            }
            return false;
        }

        private static FileObject convertToValidDir(File f) {
            FileObject fo;
            File testFile = new File( f.getPath() );
            if ( testFile == null || testFile.getParent() == null ) {
                // BTW this means that roots of file systems can't be project
                // directories.
                return null;
            }
        
            /**ATTENTION: on Windows may occure dir.isDirectory () == dir.isFile () == true then
             * its used testFile instead of dir. 
            */    
            if ( !testFile.isDirectory() ) {
                return null;
            }
            
            fo =  FileUtil.toFileObject(FileUtil.normalizeFile(f));
            return fo;
        }
    }
}
