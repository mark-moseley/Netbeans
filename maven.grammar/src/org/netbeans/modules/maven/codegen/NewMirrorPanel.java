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

package org.netbeans.modules.maven.codegen;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class NewMirrorPanel extends javax.swing.JPanel {
    private SettingsModel model;
    private NotificationLineSupport nls;

    private final static String CENTRAL = "central"; //NOI18N
    private final static String ALL = "*"; //2.0.5+ //NOI18N
    private final static String ALL_NON_LOCAL = "external:*"; //2.0.9+ //NOI18N
    private final static String ALL_BUT_FOO = "*,!foo"; //2.0.9+ //NOI18N
    private final static String LIST = "foo,bar"; //2.0.9+ //NOI18N

    private final String[] MIRROROFS = new String[] {
        CENTRAL,
        ALL,
        ALL_NON_LOCAL,
        ALL_BUT_FOO,
        LIST
    };
    private DefaultComboBoxModel urlmodel;


    public NewMirrorPanel(SettingsModel model) {
        initComponents();
        this.model = model;
        txtId.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkId();
            }
            public void removeUpdate(DocumentEvent e) {
                checkId();
            }
            public void changedUpdate(DocumentEvent e) {
                checkId();
            }
        });
        DefaultComboBoxModel mirrormodel = new DefaultComboBoxModel(MIRROROFS);
        comMirrorOf.setModel(mirrormodel);
        comMirrorOf.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toRet = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (toRet instanceof JLabel) {
                    JLabel label = (JLabel)toRet;
                    if (CENTRAL.equals(value)) {
                        label.setText(org.openide.util.NbBundle.getMessage(NewMirrorPanel.class, "LBL_Central"));
                    } else if (ALL.equals(value)) {
                        label.setText(org.openide.util.NbBundle.getMessage(NewMirrorPanel.class, "LBL_All"));
                    } else if (ALL_NON_LOCAL.equals(value)) {
                        label.setText(org.openide.util.NbBundle.getMessage(NewMirrorPanel.class, "LBL_NonLocal"));
                    } else if (ALL_BUT_FOO.equals(value)) {
                        label.setText(org.openide.util.NbBundle.getMessage(NewMirrorPanel.class, "LBL_AllButFoo"));
                    } else if (LIST.equals(value)) {
                        label.setText(org.openide.util.NbBundle.getMessage(NewMirrorPanel.class, "LBL_List"));
                    }
                }
                return toRet;
            }
        });
        Component cmp = comMirrorOf.getEditor().getEditorComponent();
        if (cmp instanceof JTextField) {
            JTextField fld = (JTextField)cmp;
            fld.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    checkCentral();
                }
                public void removeUpdate(DocumentEvent e) {
                    checkCentral();
                }
                public void changedUpdate(DocumentEvent e) {
                    checkCentral();
                }
            });
        } else {
            //TODO do something or just ignore..
        }
        urlmodel = new DefaultComboBoxModel();
        comUrl.setModel(urlmodel);

        btnLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URL link = new URL("http://maven.apache.org/guides/mini/guide-mirror-settings.html"); //NOI18N
                    HtmlBrowser.URLDisplayer.getDefault().showURL(link);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        checkCentral();
    }

    /** For gaining access to DialogDisplayer instance to manage
     * warning messages
     */
    public void attachDialogDisplayer(DialogDescriptor dd) {
        nls = dd.getNotificationLineSupport();
        if (nls == null) {
            nls = dd.createNotificationLineSupport();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        assert nls != null : " The notificationLineSupport was not attached to the panel."; //NOI18N
    }

    private void checkId() {
        String id = txtId.getText().trim();
        Mirror existing = model.getSettings().findMirrorById(id);
        if (existing != null) {
            nls.setErrorMessage(NbBundle.getMessage(NewProfilePanel.class, "ERR_SameMirrorId"));
        } else {
            nls.clearMessages();
        }
    }

    private void checkCentral() {
        String sel = (String)comMirrorOf.getSelectedItem();
        urlmodel.removeAllElements();
        if (CENTRAL.equals(sel)) {
            //see http://docs.codehaus.org/display/MAVENUSER/Mirrors+Repositories
            // for a list of central mirrors.
            //TODO might be worth to externalize somehow.
            urlmodel.addElement("http://mirrors.ibiblio.org/pub/mirrors/maven2"); //NOI18N
            urlmodel.addElement("http://www.ibiblio.net/pub/packages/maven2");//NOI18N
            urlmodel.addElement("http://ftp.cica.es/mirrors/maven2");//NOI18N
            urlmodel.addElement("http://repo1.sonatype.net/maven2");//NOI18N
            urlmodel.addElement("http://repo.exist.com/maven2");//NOI18N
            urlmodel.addElement("http://mirrors.redv.com/maven2");//NOI18N
            urlmodel.addElement("http://mirrors.dotsrc.org/maven2");//NOI18N
        }
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblId = new JLabel();
        txtId = new JTextField();
        lblMirrorOf = new JLabel();
        comMirrorOf = new JComboBox();
        lblUrl = new JLabel();
        comUrl = new JComboBox();
        btnLink = new JButton();

        lblId.setLabelFor(txtId);

        Mnemonics.setLocalizedText(lblId, NbBundle.getMessage(NewMirrorPanel.class, "NewMirrorPanel.lblId.text")); // NOI18N
        lblMirrorOf.setLabelFor(comMirrorOf);

        Mnemonics.setLocalizedText(lblMirrorOf, NbBundle.getMessage(NewMirrorPanel.class, "NewMirrorPanel.lblMirrorOf.text")); // NOI18N
        comMirrorOf.setEditable(true);
        comMirrorOf.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblUrl.setLabelFor(comUrl);

        Mnemonics.setLocalizedText(lblUrl, NbBundle.getMessage(NewMirrorPanel.class, "NewMirrorPanel.lblUrl.text")); // NOI18N
        comUrl.setEditable(true);
        comUrl.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        Mnemonics.setLocalizedText(btnLink, NbBundle.getMessage(NewMirrorPanel.class, "NewMirrorPanel.btnLink.text")); // NOI18N
        btnLink.setBorder(null);
        btnLink.setBorderPainted(false);
        btnLink.setContentAreaFilled(false);
        btnLink.setHorizontalAlignment(SwingConstants.LEFT);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(lblMirrorOf)
                            .add(lblId))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(txtId, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                .add(145, 145, 145))
                            .add(comMirrorOf, 0, 303, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(lblUrl)
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(comUrl, 0, 303, Short.MAX_VALUE))
                    .add(btnLink, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(lblId)
                    .add(txtId, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(lblMirrorOf)
                    .add(comMirrorOf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(lblUrl)
                    .add(comUrl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(btnLink)
                .addContainerGap(36, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnLink;
    private JComboBox comMirrorOf;
    private JComboBox comUrl;
    private JLabel lblId;
    private JLabel lblMirrorOf;
    private JLabel lblUrl;
    private JTextField txtId;
    // End of variables declaration//GEN-END:variables


    String getMirrorId() {
        return txtId.getText().trim();
    }

    String getMirrorOf() {
        return (String)comMirrorOf.getSelectedItem();
    }

    String getMirrorUrl() {
        return (String)comUrl.getSelectedItem();
    }
}
