/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.wizard;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.io.File;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.util.Utilities;

/**
 *
 * @author  sd99038, Tomas  Zezula
 */
public class LocationChooser extends javax.swing.JFileChooser  implements PropertyChangeListener {

    private TemplateWizard.Iterator iterator;
    private LocationChooser.Panel firer;
    private InstallerRegistry regs;
    private PlatformAccessory accessory;

    public LocationChooser (LocationChooser.Panel firer) {
        super ();
        this.setFileSelectionMode(DIRECTORIES_ONLY);
        this.setMultiSelectionEnabled(false);
        this.setControlButtonsAreShown(false);
        this.accessory = new PlatformAccessory ();
        this.setFileFilter (new PlatformFileFilter());
        this.setAccessory (this.accessory);
        this.firer = firer;
        this.regs = InstallerRegistry.getDefault();
        this.setFileView( new PlatformFileView( this.getFileSystemView(), this.regs));                
        this.addPropertyChangeListener (this);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
            this.iterator = null;
            this.accessory.setType ("");    //NOI18N
            File file = this.getSelectedFile();
            if (file != null) {
                FileObject fo = FileUtil.toFileObject (file);
                if (fo != null) {
                    for (Iterator it = this.regs.getInstallers().iterator(); it.hasNext();) {
                        PlatformInstall install = (PlatformInstall) it.next ();
                        if (install.accept(fo)) {
                            this.accessory.setType (install.getDisplayName());
                            this.iterator = install.createIterator(fo);
                            break;
                        }
                    }
                }
            }
            this.firer.fireStateChanged();
        }
    }


    private boolean valid () {
        return this.getInstaller() != null;
    }

    private void read (WizardDescriptor settings) {

    }

    private void store (WizardDescriptor settings) {

    }

    private TemplateWizard.Iterator getInstaller () {
        return this.iterator;
    }

    private static class PlatformFileFilter extends FileFilter {

        public boolean accept(File f) {
            return f.isDirectory();
        }

        public String getDescription() {
            return NbBundle.getMessage (LocationChooser.class,"TXT_PlatformFolder");
        }
    }

    private static class PlatformAccessory extends JComponent {

        private JTextField tf;

        public PlatformAccessory () {
            this.initComponents ();
        }

        private void setType (String type) {
            this.tf.setText(type);
        }

        private void initComponents () {
            GridBagLayout l = new GridBagLayout();
            this.setLayout (l);
            JLabel label = new JLabel (NbBundle.getMessage(LocationChooser.class,"TXT_PlatformType"));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (0,12,3,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            l.setConstraints(label,c);
            this.add (label);
            this.tf = new JTextField();
            this.tf.setEditable(false);
            this.tf.setColumns(15);
            c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (3,12,12,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            l.setConstraints(this.tf,c);
            this.add (tf);
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
        Collection                  listeners = new ArrayList();



        public java.awt.Component getComponent() {
            if (component == null) {
                this.component = new LocationChooser (this);
            }
            return component;
        }
        
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        public boolean isValid() {
            return ((LocationChooser)this.getComponent()).valid();
        }
        
        public void readSettings(Object settings) {
            ((LocationChooser)this.getComponent()).read ((TemplateWizard) settings);
        }
        
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        public void storeSettings(Object settings) {
            ((LocationChooser)this.getComponent()).store((TemplateWizard)settings);
        }
        
        /**
         * Returns the currently selected installer.
         */
        TemplateWizard.Iterator getInstaller() {
            return ((LocationChooser)this.getComponent()).getInstaller ();
        }
        
        void fireStateChanged() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty())
                    return;
                ll = (ChangeListener[])listeners.toArray(new ChangeListener[0]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++)
                ll[i].stateChanged(ev);
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
        
        private static final Icon BADGE = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/platform/resources/platformBadge.gif")); // NOI18N
        private static final Icon EMPTY = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/platform/resources/empty.gif")); // NOI18N
        
        private FileSystemView fsv;
        private Icon lastOriginal;
        private Icon lastMerged;
        private InstallerRegistry regs;
        
        public PlatformFileView( FileSystemView fsv, InstallerRegistry regs ) {
            this.fsv = fsv;            
            this.regs = regs;
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
        
        
        private boolean isPlatformDir ( File f ) {
            if (f.isFile ()) {
                return false;
            }
            FileObject fo = FileUtil.toFileObject(f);
            if (fo == null) {
                return false;
            }
            for (Iterator it = this.regs.getInstallers().iterator(); it.hasNext();) {
                PlatformInstall install = (PlatformInstall) it.next();                
                if (install.accept(fo)) {
                    return true;
                }
            }
            return false;
        }
                
    }
}
