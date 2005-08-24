/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.ErrorManager;


/**
 * Represents <em>Basic branding parameters</em> panel in Suite customizer.
 *
 * @author Radek Matous
 */
public class SuiteCustomizerBasicBranding extends JPanel
        implements ComponentFactory.StoragePanel {
    
    private SuiteProperties suiteProps  = null;
    private BasicBrandingModel brandingModel;
    
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerBasicBranding(final SuiteProperties suiteProps) {
        this.suiteProps = suiteProps;
        initComponents();
        brandingModel = new BasicBrandingModel(suiteProps);
        
        DocumentListener nameListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                brandingModel.setName(nameValue.getText());
            }
        };
        
        DocumentListener titleListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                brandingModel.setTitle(titleValue.getText());
            }
        };
        
        nameValue.getDocument().addDocumentListener(nameListener);
        titleValue.getDocument().addDocumentListener(titleListener);
        
        nameValue.setText(brandingModel.getName());
        titleValue.setText(brandingModel.getTitle());
        buildWithBranding.setSelected(brandingModel.isBrandingEnabled());
        buildWithBrandingChanged();
        
        ((ImagePreview)iconPreview).setImage(new ImageIcon(brandingModel.getIconSource()));
        iconLocation.setText(brandingModel.getIconLocation());
    }
    
    public void store() {
        try {
            brandingModel.store();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void buildWithBrandingChanged() {
        nameValue.setEnabled(buildWithBranding.isSelected());
        titleValue.setEnabled(buildWithBranding.isSelected());
        browse.setEnabled(buildWithBranding.isSelected());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        title = new javax.swing.JLabel();
        titleValue = new javax.swing.JTextField();
        iconPreview = new ImagePreview(48, 48);
        name = new javax.swing.JLabel();
        nameValue = new javax.swing.JTextField();
        buildWithBranding = new javax.swing.JCheckBox();
        browse = new javax.swing.JButton();
        icon = new javax.swing.JLabel();
        iconLocation = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(title, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_AppTitle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(title, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(titleValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(iconPreview, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(name, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_AppName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(name, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(nameValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buildWithBranding, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("CTL_EnableBranding"));
        buildWithBranding.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        buildWithBranding.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buildWithBranding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildWithBrandingActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(buildWithBranding, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("CTL_Browse"));
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 0);
        add(browse, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(icon, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_AppIcon"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 12);
        add(icon, gridBagConstraints);

        iconLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 12);
        add(iconLocation, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
// TODO add your handling code here:
        JFileChooser jf = new JFileChooser();
        //TODO: write better filter (check format - not extension, 
        //also png, jpg, case sensitivity, ...)
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".gif"));//NOI18N
            }
            public String getDescription() {
                return "gif";//NOI18N
            }
        };
        jf.setFileFilter(ff);
        jf.showOpenDialog(this);
        if (jf.getSelectedFile() != null) {
            brandingModel.setIconSource(jf.getSelectedFile());
            ((ImagePreview)iconPreview).setImage(new ImageIcon(brandingModel.getIconSource()));
        }
        
    }//GEN-LAST:event_browseActionPerformed
    
    private void buildWithBrandingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildWithBrandingActionPerformed
        buildWithBrandingChanged();
        brandingModel.setBrandingEnabled(buildWithBranding.isSelected());
    }//GEN-LAST:event_buildWithBrandingActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JCheckBox buildWithBranding;
    private javax.swing.JLabel icon;
    private javax.swing.JTextField iconLocation;
    private javax.swing.JLabel iconPreview;
    private javax.swing.JLabel name;
    private javax.swing.JTextField nameValue;
    private javax.swing.JLabel title;
    private javax.swing.JTextField titleValue;
    // End of variables declaration//GEN-END:variables
    
    static class ImagePreview extends JLabel {
        private ImageIcon image = null;
        private int width;
        private int height;
        private javax.swing.border.Border border;
        ImagePreview(int width, int height){
            //this.image = im;
            this.width = width;
            this.height = height;
            String bundlePath = "org/netbeans/modules/apisupport/project/ui/customizer/Bundle";//NOI18N
            
            border = new javax.swing.border.TitledBorder(
                    ResourceBundle.getBundle(bundlePath).getString("LBL_IconPreview"));//NOI18N
            setBorder(border);
        }
        
        
        public void paint(Graphics g) {
            super.paint(g);
            
            if ((getWidth() >  width) && (getHeight() > height) && image != null) {
                if (getBorder() == null) {
                    setBorder(border);
                }
                int x = (getWidth()/2)-(width/2);
                int y = (getHeight()/2)-(height/2);
                g.drawImage(image.getImage(),x, y, width, height, this.getBackground(),null);
            } else {
                if (getBorder() != null) {
                    setBorder(null);
                }
            }
        }
        
        
        private void setImage(ImageIcon image) {
            this.image = image;
            repaint();
        }        
    }
}
