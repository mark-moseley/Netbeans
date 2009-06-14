/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform;

import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Properties;
import org.netbeans.modules.javacard.constants.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tim Boudreau
 */
public class PlatformPanel extends javax.swing.JPanel implements FocusListener, DocumentListener {

    private final ChangeSupport supp = new ChangeSupport(this);
    private final PlatformValidator validator;
    private FileObject baseDir;

    /** Creates new form PlatformPanel */
    public PlatformPanel(FileObject fo) {
        this.baseDir = fo;
        validator = new PlatformValidatorImpl(fo);
        initComponents();
        displayNameField.addFocusListener(this);
        infoField.addFocusListener(this);
        displayNameField.setEnabled(false);
        displayNameField.getDocument().addDocumentListener(this);
        locationField.setText(baseDir == null ? "" : baseDir.getPath()); //NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (baseDir != null) {
            if (!validator.hasRun()) {
                validatePlatform();
            }
        } else {
            enableControls(false);
            setProblem (NbBundle.getMessage(PlatformPanel.class,
                    "MSG_NO_EMULATOR", baseDir.toString())); //NOI18N
        }
    }

    public void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }

    public void fireChange() {
        supp.fireChange();
    }

    public void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }

    boolean isProblem() {
        return problemLbl.getText().trim().length() > 0;
    }

    void setDisplayName(String nm) {
        displayNameField.setText(nm);
    }

    void setProblem(String txt) {
        boolean wasProblem = isProblem();
        txt = txt == null ? "" : txt;
        problemLbl.setText(txt);
        if (wasProblem != isProblem()) {
            fireChange();
        }
    }

    void enableControls(boolean val) {
        for (Component c : getComponents()) {
            if (c != jProgressBar1 && c != infoField && c != problemLbl) {
                c.setEnabled(val);
            }
        }
        change();
    }

    private void change() {
        String key = null;
        String name = displayNameField.getText().trim();
        if (validator.failed()) {
            key = "MSG_BAD_PLATFORM"; //NOI18N
        } else if (validator.isRunning()) {
            key = "MSG_LOADING"; //NOI18N
        } else if (name.length() == 0) {
            key = "MSG_NO_NAME"; //NOI18N
        } else if (name.contains("/") || name.contains("\\") || name.contains(":") || //NOI18N
                name.contains(";") || name.contains(File.separator) || //NOI18N
                name.contains (File.pathSeparator)) {
            //The name will be used as a filename.  Disallow known path and path separator characters
            key = "MSG_NO_SLASHES"; //NOI18N
        } else if (platformFileExists(name)) {
            key = "MSG_PLATFORM_EXISTS";
        }
        String path = baseDir == null ? "" : baseDir.getPath();
        String msg = key == null ? null : NbBundle.getMessage(PlatformPanel.class, key, path);
        setProblem(msg);
    }

    private boolean platformFileExists (String name) {
        String nm = name.replace (' ', '_') + "." + JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION;
        final FileObject platformsFolder = FileUtil.getConfigFile(
                CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER); //NOI18N
        FileObject platformFile = platformsFolder.getFileObject(nm);
        return (nm != null && platformFile != null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
        versionLabel = new javax.swing.JLabel();
        infoPane = new javax.swing.JScrollPane();
        infoField = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        problemLbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();

        displayNameLabel.setDisplayedMnemonic('D');
        displayNameLabel.setLabelFor(displayNameField);
        displayNameLabel.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.displayNameLabel.text")); // NOI18N

        displayNameField.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.displayNameField.text")); // NOI18N
        displayNameField.setToolTipText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.displayNameField.toolTipText")); // NOI18N

        versionLabel.setDisplayedMnemonic('V');
        versionLabel.setLabelFor(infoField);
        versionLabel.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.versionLabel.text")); // NOI18N

        infoField.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        infoField.setColumns(20);
        infoField.setEditable(false);
        infoField.setLineWrap(true);
        infoField.setRows(5);
        infoField.setWrapStyleWord(true);
        infoPane.setViewportView(infoField);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jProgressBar1.setIndeterminate(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jProgressBar1, gridBagConstraints);

        problemLbl.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        problemLbl.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.problemLbl.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 180;
        gridBagConstraints.ipady = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(problemLbl, gridBagConstraints);

        jLabel1.setDisplayedMnemonic('L');
        jLabel1.setLabelFor(locationField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.jLabel1.text")); // NOI18N

        locationField.setEditable(false);
        locationField.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.locationField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, infoPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(displayNameLabel)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(locationField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                            .add(displayNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, versionLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLabel)
                    .add(displayNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(locationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(versionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextArea infoField;
    private javax.swing.JScrollPane infoPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel problemLbl;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    public void focusGained(FocusEvent e) {
        if (e.getComponent() instanceof JTextComponent) {
            ((JTextComponent) e.getComponent()).selectAll();
        }
    }

    public String getDisplayName() {
        return displayNameField.getText();
    }

    public void focusLost(FocusEvent e) {
        if (displayNameField == e.getOppositeComponent()) {
            change();
        }
    }

    private void validatePlatform() {
        if (!validator.hasRun()) {
            jProgressBar1.setVisible(true);
            jProgressBar1.setIndeterminate(true);
            enableControls(false);
            validator.start();
            invalidate();
            revalidate();
            repaint();
        }
    }
    volatile boolean failed;

    public void insertUpdate(DocumentEvent e) {
        change();
        firePropertyChange("displayName", null, getDisplayName()); //NOI18N
    }

    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
    }
    PlatformInfo platformInfo;

    PlatformInfo getPlatformInfo() {
        return platformInfo;
    }

    private final class PlatformValidatorImpl extends PlatformValidator {

        PlatformValidatorImpl(FileObject baseDir) {
            super(baseDir);
        }

        @Override
        void onStart() {
            jProgressBar1.setVisible(true);
            jProgressBar1.setIndeterminate(true);
            enableControls(false);
            setProblem(NbBundle.getMessage(PlatformValidatorImpl.class,"MSG_VALIDATING")); //NOI18N
            invalidate();
            revalidate();
            repaint();
        }

        @Override
        void onFail(Exception e) {
            assert EventQueue.isDispatchThread();
            infoField.setText(getStandardOutput() + "\n" + getErrorOutput()); //NOI18N
            setProblem(e.getMessage());
            e.printStackTrace();
        }

        @Override
        void onSucceed(String stdout) {
            assert !EventQueue.isDispatchThread();
            platformInfo = getPlatformInfo(stdout, getPlatformProps());
            PlatformPanel.this.fireChange();
        }

        @Override
        void onDone() {
            assert EventQueue.isDispatchThread();
            enableControls(true);
            if (platformInfo != null) {
                displayNameField.setText(platformInfo.getName());
            }
            infoField.setText(getStandardOutput());
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setVisible(false);
            invalidate();
            revalidate();
            repaint();
            change();
        }
    }

    PlatformInfo getPlatformInfo(String out, Properties props) {
        return new PlatformInfo (props);
    }
}