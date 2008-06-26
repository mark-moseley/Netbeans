package org.netbeans.modules.apisupport.project.ui.wizard.glf;

import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;


public final class GLFTemplateVisualPanel2 extends JPanel {

    private GLFTemplateWizardPanel2 wizardPanel;
    
    /** Creates new form GLFTemplateVisualPanel2 */
    public GLFTemplateVisualPanel2 (GLFTemplateWizardPanel2 wizardPanel) {
        this.wizardPanel = wizardPanel;
        initComponents ();
        wizardPanel.getIterator().getWizardDescriptor().putProperty("NewFileWizard_Title",  // NOI18N
                NbBundle.getMessage(GLFTemplateVisualPanel2.class, "LBL_GLFWizardTitle"));
        DocumentListener documentListener = new DocumentListener () {
            public void insertUpdate (DocumentEvent e) {
                update ();
            }

            public void removeUpdate (DocumentEvent e) {
                update ();
            }

            public void changedUpdate (DocumentEvent e) {
                update ();
            }
            
        };
        tfExtensions.getDocument ().addDocumentListener (documentListener);
        tfMimeType.getDocument ().addDocumentListener (documentListener);
        update ();
    }
    
    private static final Pattern MIME_PATTERN = Pattern.compile("[\\w+-.]+/[\\w+-.]+");  // NOI18N
    private static final Pattern EXT_PATTERN = Pattern.compile("(\\w+\\s*)+");  // NOI18N
    
    private void update () {
        final WizardDescriptor wd = wizardPanel.getIterator().getWizardDescriptor();
        // reasonable mime type check
        if (! MIME_PATTERN.matcher(getMimeType().trim()).matches()) {
            wd.putProperty (
                "WizardPanel_errorMessage",  // NOI18N
                NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Invalid_Mime_Type"));
            wizardPanel.setValid (false);
            return;
        }
        if (! EXT_PATTERN.matcher(getExtensions ().trim ()).matches()) {
            wd.putProperty (
                "WizardPanel_errorMessage",  // NOI18N
                NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Invalid_Extensions"));
            wizardPanel.setValid (false);
            return;
        }
        wd.putProperty (
            "WizardPanel_errorMessage",  // NOI18N
            null
        );
        wizardPanel.setValid (true);
    }
    
    public @Override String getName () {
        return NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Step2");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modifiedFiles = new javax.swing.JTextArea();
        lMimeType = new javax.swing.JLabel();
        tfMimeType = new javax.swing.JTextField();
        lExtensions = new javax.swing.JLabel();
        tfExtensions = new javax.swing.JTextField();
        createdFiles = new javax.swing.JTextArea();
        extensionsHint = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setBorder(null);

        lMimeType.setLabelFor(tfMimeType);
        org.openide.awt.Mnemonics.setLocalizedText(lMimeType, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Mime_Type")); // NOI18N

        tfMimeType.setNextFocusableComponent(tfExtensions);

        lExtensions.setLabelFor(tfExtensions);
        org.openide.awt.Mnemonics.setLocalizedText(lExtensions, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Extensions")); // NOI18N

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(extensionsHint, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Extensions_Comment")); // NOI18N

        jLabel1.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lMimeType)
                    .add(lExtensions)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(modifiedFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, extensionsHint)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, createdFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tfMimeType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tfExtensions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lMimeType)
                    .add(tfMimeType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lExtensions)
                    .add(tfExtensions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(extensionsHint)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(createdFiles))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modifiedFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .add(jLabel2)))
        );

        modifiedFiles.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.modifiedFiles.AccessibleContext.accessibleDescription")); // NOI18N
        tfMimeType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.tfMimeType.AccessibleContext.accessibleDescription")); // NOI18N
        tfExtensions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.tfExtensions.AccessibleContext.accessibleDescription")); // NOI18N
        createdFiles.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.createdFiles.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel extensionsHint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lExtensions;
    private javax.swing.JLabel lMimeType;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JTextField tfExtensions;
    private javax.swing.JTextField tfMimeType;
    // End of variables declaration//GEN-END:variables
    
    String getMimeType () {
        return tfMimeType.getText ();
    }
    
    String getExtensions () {
        return tfExtensions.getText ();
    }
}

