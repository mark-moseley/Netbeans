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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midp.propertyeditors.resource.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Anton Chechel
 */
public class ImageEditorElement extends PropertyEditorResourceElement implements DocumentListener {

    private long componentID;
    private boolean doNotFireEvent;
    private Project project;
    private Image image;

    public ImageEditorElement() {
        initComponents();
        previewPanel.add(new ImagePreview(), BorderLayout.CENTER);
        pathTextField.getDocument().addDocumentListener(this);
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return ImageCD.TYPEID;
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        if (document != null) {
            project = ProjectUtils.getProject(document);
        }

        if (wrapper == null) {
            // UI stuff
            setText(null);
            setAllEnabled(false);
            return;
        }

        this.componentID = wrapper.getComponentID();
        final String[] _pathText = new String[1];

        final DesignComponent component = wrapper.getComponent();
        if (component != null) {
            // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            this.componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    _pathText[0] = MidpTypes.getString(component.readProperty(ImageCD.PROP_RESOURCE_PATH));
                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (ImageCD.PROP_RESOURCE_PATH.equals(propertyName)) {
                    _pathText[0] = MidpTypes.getString(propertyValue);
                }
            }
        }

        // UI stuff
        setAllEnabled(true);
        setText(_pathText[0]);
    }

    private synchronized void setText(String text) {
        doNotFireEvent = true;
        pathTextField.setText(text);
        doNotFireEvent = false;
    }

    private void setAllEnabled(boolean isEnabled) {
        pathLabel.setEnabled(isEnabled);
        pathTextField.setEnabled(isEnabled);
        previewLabel.setEnabled(isEnabled);
        previewPanel.setEnabled(isEnabled);
        widthLabel.setEnabled(isEnabled);
        widthTextField.setEnabled(isEnabled);
        heightLabel.setEnabled(isEnabled);
        heightTextField.setEnabled(isEnabled);
        chooserButton.setEnabled(isEnabled);
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        textChanged();
    }

    private synchronized void textChanged() {
        if (isShowing() && !doNotFireEvent) {
            fireElementChanged(componentID, ImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(pathTextField.getText()));
        }
        updatePreview();
    }

    private void updatePreview() {
        String relativePath = pathTextField.getText();
        String path = getSourceFolder().getPath() + relativePath;
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException ex) {
        }

        if (bufferedImage != null) {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            widthTextField.setText(String.valueOf(width));
            heightTextField.setText(String.valueOf(height));

            if (width > ImagePreview.IMAGE_MAX_WIDTH || height > ImagePreview.IMAGE_MAX_WIDTH) {
                if (width > height) {
                    width = ImagePreview.IMAGE_MAX_WIDTH;
                    height = -1;
                } else {
                    width = -1;
                    height = ImagePreview.IMAGE_MAX_WIDTH;
                }
                image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH | Image.SCALE_AREA_AVERAGING);
            } else {
                image = bufferedImage;
            }
        } else {
            image = null;
            widthTextField.setText(null);
            heightTextField.setText(null);
        }
        
        previewPanel.invalidate();
        previewPanel.validate();
        previewPanel.repaint();
    }

    private FileObject getSourceFolder() {
        String projectID = ProjectUtils.getProjectID(project);
        return ProjectUtils.getSourceGroups(projectID).iterator().next().getRootFolder();
    }

    private String convertFile(FileObject file) {
        String fullPath = file.getPath();
        FileObject sourceFolder = getSourceFolder();
        String sourcePath = sourceFolder.getPath();

        if (!fullPath.contains(sourcePath)) {
            try {
                file = file.copy(sourceFolder, file.getName(), file.getExt());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        fullPath = file.getPath();

        int i = fullPath.indexOf(sourcePath) + sourcePath.length();
        return fullPath.substring(i);
    }

    private static class ImageFilter extends FileFilter {

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }

            String extension = getExtension(file);
            return "png".equals(extension) || "gif".equals(extension) || "jpg".equals(extension); // NOI18N
        }

        public String getDescription() {
            return "Image Files";
        }

        private static String getExtension(File file) {
            String ext = null;
            String s = file.getName();
            int i = s.lastIndexOf('.'); // NOI18N
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }

    private class ImagePreview extends JComponent {

        static final int IMAGE_MAX_WIDTH = 90;
        private static final int BORDER_EDGE_WIDTH = 1;
        private static final int BORDER_GAP = 2;
        private static final int BORDER_EDGE_LENGTH = 10;
        private static final int IMAGE_SIZE_OFFSET = (BORDER_GAP + BORDER_EDGE_WIDTH) * 2;

        @Override
        public void paint(Graphics g) {
            // paint the border
            g.setColor(Color.BLACK);
            final int rightX = getWidth() - 1;
            final int bottomY = getHeight() - 1;
            // top left
            g.drawLine(0, 0, 0, BORDER_EDGE_LENGTH);
            g.drawLine(0, 0, BORDER_EDGE_LENGTH, 0);
            // top right
            g.drawLine(rightX, 0, rightX, BORDER_EDGE_LENGTH);
            g.drawLine(rightX, 0, rightX - BORDER_EDGE_LENGTH, 0);
            // bottom left
            g.drawLine(0, bottomY, 0, bottomY - BORDER_EDGE_LENGTH);
            g.drawLine(0, bottomY, BORDER_EDGE_LENGTH, bottomY);
            // bottom right
            g.drawLine(rightX, bottomY, rightX, bottomY - BORDER_EDGE_LENGTH);
            g.drawLine(rightX, bottomY, rightX - BORDER_EDGE_LENGTH, bottomY);
            if (image != null) {
                int xOffset = 0;
                int yOffset = 0;
                xOffset = (getWidth() - image.getWidth(this)) >> 1;
                yOffset = (getWidth() - image.getHeight(this)) >> 1;
                g.drawImage(image, xOffset, yOffset, this);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        pathLabel = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        previewLabel = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        widthTextField = new javax.swing.JTextField();
        heightLabel = new javax.swing.JLabel();
        heightTextField = new javax.swing.JTextField();
        chooserButton = new javax.swing.JButton();

        pathLabel.setText(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.pathLabel.text")); // NOI18N
        pathLabel.setEnabled(false);

        pathTextField.setEnabled(false);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.previewLabel.text")); // NOI18N
        previewLabel.setEnabled(false);

        previewPanel.setEnabled(false);
        previewPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        previewPanel.setLayout(new java.awt.BorderLayout());

        widthLabel.setText(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.widthLabel.text")); // NOI18N
        widthLabel.setEnabled(false);

        widthTextField.setEditable(false);
        widthTextField.setEnabled(false);

        heightLabel.setText(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.heightLabel.text")); // NOI18N
        heightLabel.setEnabled(false);

        heightTextField.setEditable(false);
        heightTextField.setEnabled(false);

        chooserButton.setText(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.chooserButton.text")); // NOI18N
        chooserButton.setEnabled(false);
        chooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooserButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pathLabel)
                .addContainerGap(265, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(previewLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(heightLabel)
                            .add(widthLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(widthTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                            .add(heightTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)))
                    .add(pathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooserButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chooserButton)
                    .add(pathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(widthLabel)
                            .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(heightLabel)
                            .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(previewLabel)
                    .add(previewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserButtonActionPerformed
        JFileChooser chooser = new JFileChooser(project.getProjectDirectory().getPath());
        chooser.setFileFilter(new ImageFilter());
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileObject fo = FileUtil.toFileObject(chooser.getSelectedFile());
            String relativePath = convertFile(fo);
            pathTextField.setText(relativePath);
        }
    }//GEN-LAST:event_chooserButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooserButton;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables
}
