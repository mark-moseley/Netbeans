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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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


package org.netbeans.modules.i18n;


import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.BadLocationException;
import org.jdesktop.layout.GroupLayout;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.TRAILING;
import static org.jdesktop.layout.LayoutStyle.RELATED;
import static org.jdesktop.layout.LayoutStyle.UNRELATED;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;


/**
 * Panel which is used for customizing key-value pair (and comment also)
 * encapsulated by {@code I18nString} object.
 * It's used inside {@code I18nPanel}.
 *
 * @author  Peter Zavadsky
 * @see I18nString
 * @see I18nPanel
 */
public class PropertyPanel extends JPanel {

    /** property representing the I18String. Change is fired when the i18string changes.
     * Old and new objects are not sent with the notification.
     */
    public static final String PROP_STRING = "propString";              //NOI18N

    /** Name for resource property. */
    public static final String PROP_RESOURCE = "property_resource";     //NOI18N

    /** Helper name for dummy action command. */
    private static final String DUMMY_ACTION = "dont_proceed";          //NOI18N

    /** Customized <code>I18nString</code>. */
    protected I18nString i18nString;

    /** the file for that resource should be selected **/
    private FileObject file;

    /** Internal flag to block handling of changes to the key jtextfield,
     * which didn't originate from the user but from the code. If this is >0,
     * values are just being pushed to the UI, if <=0, values are being received
     * from the ui.
     **/
    private int internalTextChange = 0;
    private String innerResourceTextContent;

    /** Creates new <code>PropertyPanel</code>. */
    public PropertyPanel() {
        initComponents();
        myInitComponents();
        initAccessibility();
    }

    @Override
    public void setEnabled(boolean ena) {
        super.setEnabled(ena);
        commentText.setEnabled(ena);
        commentLabel.setEnabled(ena);
        commentScroll.setEnabled(ena);

        keyBundleCombo.setEnabled(ena);
        keyLabel.setEnabled(ena);

        replaceFormatButton.setEnabled(ena);
        replaceFormatLabel.setEnabled(ena);
        replaceFormatTextField.setEnabled(ena);

        valueLabel.setEnabled(ena);
        valueText.setEnabled(ena);
        valueScroll.setEnabled(ena);
    }

    /** Seter for <code>i18nString</code> property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;

        updateAllValues();
        firePropertyChange(PROP_STRING, null,null);
    }

    /** Sets the file for that resource should be selected **/
    public void setFile(FileObject fo) {
        this.file = fo;
    }

    public FileObject getFile() {
        return file;
    }

    /** Initializes UI values. */
    void updateAllValues() {
        resourceText.setText(getResourceName(i18nString.getSupport().getResourceHolder().getResource()));
        innerResourceTextContent = resourceText.getText();
        updateBundleKeys();
        updateKey();
        updateValue();
        updateComment();
    }

    /** Updates selected item of <code>keyBundleCombo</code> UI.
     */
    private void updateKey() {
        String key = i18nString.getKey();

        if ((key == null) || !key.equals(keyBundleCombo.getSelectedItem())) {
            // Trick to avoid firing key selected property change.
            String oldActionCommand = keyBundleCombo.getActionCommand();
            keyBundleCombo.setActionCommand(DUMMY_ACTION);

            internalTextChange++;
            keyBundleCombo.setSelectedItem((key != null) ? key : "");   //NOI18N
            internalTextChange--;

            keyBundleCombo.setActionCommand(oldActionCommand);
        }

        updateReplaceText();
    }

    /** Updates <code>valueText</code> UI.
     */
    private void updateValue() {
        String value = i18nString.getValue();

        if (!valueText.getText().equals(value)) {
            valueText.setText((value != null) ? value : "");            //NOI18N
        }

       updateReplaceText();
    }

    /** Updates <code>commentText</code> UI. */
    private void updateComment() {
        String comment = i18nString.getComment();

        if (!commentText.getText().equals(comment)) {
            commentText.setText((comment != null) ? comment : "");      //NOI18N
        }
    }

    /** Updates <code>replaceFormatTextField</code>. */
    protected void updateReplaceText() {
        replaceFormatTextField.setText(i18nString.getReplaceString());
    }

    /** Updates <code>keyBundleCombo</code> UI. */
    void updateBundleKeys() {
        // Trick to avoid firing key selected property change.
        String oldActionCommand = keyBundleCombo.getActionCommand();
        keyBundleCombo.setActionCommand(DUMMY_ACTION);

        internalTextChange++;
        String[] keys = i18nString.getSupport().getResourceHolder().getAllKeys();
        Arrays.sort(keys);
        keyBundleCombo.setModel(new DefaultComboBoxModel(keys));
        internalTextChange--;

        keyBundleCombo.setActionCommand(oldActionCommand);

        updateKey();
    }

     /** Helper method. Changes resource. */
    private void changeResource(DataObject resource) {
        if (resource == null) {
            throw new IllegalArgumentException();
        }

        DataObject oldValue = i18nString.getSupport().getResourceHolder().getResource();

        if ((oldValue != null) && oldValue.equals(resource)) {
            return;
        }

        i18nString.getSupport().getResourceHolder().setResource(resource);
        String newResourceValue = i18nString.getSupport().getResourceHolder()
                                  .getValueForKey(i18nString.getKey());
        if (newResourceValue != null) {
            i18nString.setValue(newResourceValue);
        }
        updateAllValues();

        firePropertyChange(PROP_RESOURCE, oldValue, resource);

        I18nUtil.getOptions().setLastResource2(resource);
    }

    public void setResource(DataObject resource) {
        if (isResourceClass(resource.getClass())) {
            changeResource(resource);
        }
    }

    private boolean isResourceClass(Class clazz) {
        return Arrays.asList(
                i18nString.getSupport().getResourceHolder().getResourceClasses()).contains(clazz);
    }

    private String getResourceName(DataObject resource) {
        if (resource == null) {
            return "";                                                  //NOI18N
        } else {
            String name = Util.getResourceName(file, resource.getPrimaryFile(), '.', false);
            return (name != null) ? name : "";                          //NOI18N
        }
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_PropertyPanel"));   //NOI18N
        valueText.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_valueText"));       //NOI18N
        commentText.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_commentText"));     //NOI18N
        replaceFormatButton.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_CTL_Format"));      //NOI18N
        replaceFormatTextField.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_replaceFormatTextField"));//NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_CTL_BrowseButton"));//NOI18N
        resourceText.getAccessibleContext().setAccessibleDescription(
                I18nUtil.getBundle().getString("ACS_ResourceText"));    //NOI18N
    }

    private void myInitComponents() {
        argumentsButton.setVisible(false);
        // hook the Key combobox edit-field for changes
        ((JTextField) keyBundleCombo.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) { keyBundleTextChanged();}
                    public void insertUpdate(DocumentEvent e) {keyBundleTextChanged();}
                    public void removeUpdate(DocumentEvent e) {keyBundleTextChanged();}
                }
               );
        valueText.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) { valueTextChanged();}
                    public void insertUpdate(DocumentEvent e) {valueTextChanged();}
                    public void removeUpdate(DocumentEvent e) {valueTextChanged();}
                }
               );

    }

    private void keyBundleTextChanged() {
        if (internalTextChange == 0) {
            String key = ((JTextField) keyBundleCombo.getEditor().getEditorComponent()).getText();

            if (!key.equals(i18nString.getKey())) {
                i18nString.setKey(key);
                firePropertyChange(PROP_STRING, null, null);
            }
        }
    }

    private void valueTextChanged() {
        i18nString.setValue(valueText.getText());
//        updateValue();
        firePropertyChange(PROP_STRING, null, null);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" UI initialization code ">
    private void initComponents() {

        commentLabel = new JLabel();
        commentScroll = new JScrollPane();
        commentText = new JTextArea();
        keyLabel = new JLabel();
        valueLabel = new JLabel();
        valueScroll = new JScrollPane();
        valueText = new JTextArea();
        keyBundleCombo = new JComboBox();
        replaceFormatTextField = new JTextField();
        replaceFormatLabel = new JLabel();
        replaceFormatButton = new JButton();
        bundleNameLabel = new JLabel();
        resourceText = new JTextField();
        argumentsButton = new JButton();
        browseButton = new JButton();
        warningLabel = new JLabel();

        warningLabel.setForeground(java.awt.Color.RED);

        commentLabel.setLabelFor(commentText);
        Mnemonics.setLocalizedText(commentLabel, I18nUtil.getBundle().getString("LBL_Comment")); // NOI18N

        commentText.setRows(2);
        commentText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                commentTextFocusLost(evt);
            }
        });
        commentScroll.setViewportView(commentText);

        keyLabel.setLabelFor(keyBundleCombo);
        Mnemonics.setLocalizedText(keyLabel, I18nUtil.getBundle().getString("LBL_Key")); // NOI18N

        valueLabel.setLabelFor(valueText);
        Mnemonics.setLocalizedText(valueLabel, I18nUtil.getBundle().getString("LBL_Value")); // NOI18N

        valueText.setRows(2);
        valueText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                valueTextFocusLost(evt);
            }
        });
        valueScroll.setViewportView(valueText);

        keyBundleCombo.setEditable(true);
        keyBundleCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                keyBundleComboActionPerformed(evt);
            }
        });

        replaceFormatTextField.setColumns(35);
        replaceFormatTextField.setEditable(false);
        replaceFormatTextField.selectAll();
        replaceFormatTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                replaceFormatTextFieldFocusGained(evt);
            }
        });

        replaceFormatLabel.setLabelFor(replaceFormatTextField);
        Mnemonics.setLocalizedText(replaceFormatLabel, I18nUtil.getBundle().getString("LBL_ReplaceFormat")); // NOI18N

        Mnemonics.setLocalizedText(replaceFormatButton, I18nUtil.getBundle().getString("CTL_Format")); // NOI18N
        replaceFormatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                replaceFormatButtonActionPerformed(evt);
            }
        });

        bundleNameLabel.setLabelFor(resourceText);
        Mnemonics.setLocalizedText(bundleNameLabel, I18nUtil.getBundle().getString("LBL_BundleName")); // NOI18N

        resourceText.setColumns(20);

        Mnemonics.setLocalizedText(argumentsButton, I18nUtil.getBundle().getString("CTL_Arguments")); // NOI18N

        Mnemonics.setLocalizedText(browseButton, I18nUtil.getBundle().getString("CTL_BrowseButton")); // NOI18N
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        resourceText.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resourceTextKeyReleased(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup()
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup()
                            .add(bundleNameLabel)
                            .add(valueLabel)
                            .add(commentLabel)
                            .add(keyLabel)
                            .add(replaceFormatLabel))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup()
                            .add(layout.createSequentialGroup()
                                .add(resourceText)
                                .addPreferredGap(RELATED)
                                .add(browseButton))
                            .add(warningLabel)
                            .add(keyBundleCombo)
                            .add(valueScroll)
                            .add(commentScroll)
                            .add(replaceFormatTextField)))
                    .add(TRAILING, layout.createSequentialGroup()
                        .add(argumentsButton)
                        .addPreferredGap(RELATED)
                        .add(replaceFormatButton)))
                .addContainerGap()
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(BASELINE)
                    .add(bundleNameLabel)
                    .add(resourceText)
                    .add(browseButton))
                .addPreferredGap(RELATED)
                .add(layout.createParallelGroup()
                    .add(warningLabel))
                .addPreferredGap(RELATED)
                .add(layout.createParallelGroup(BASELINE)
                    .add(keyLabel)
                    .add(keyBundleCombo))
                .addPreferredGap(RELATED)
                .add(layout.createParallelGroup()
                    .add(valueLabel)
                    .add(valueScroll))
                .addPreferredGap(RELATED)
                .add(layout.createParallelGroup()
                    .add(commentLabel)
                    .add(commentScroll))
                .addPreferredGap(UNRELATED)
                .add(layout.createParallelGroup(BASELINE)
                    .add(replaceFormatTextField)
                    .add(replaceFormatLabel))
                .addPreferredGap(RELATED)
                .add(layout.createParallelGroup(BASELINE)
                    .add(replaceFormatButton)
                    .add(argumentsButton))
                .addContainerGap()
        );
    }// </editor-fold>

    private void resourceTextKeyReleased(java.awt.event.KeyEvent evt) {
        if (!innerResourceTextContent.equals(resourceText.getText())
                && resourceText.getText().trim().length() != 0) {
            String bundlePath = resourceText.getText()
                                    .replaceAll("[.]", "/")  //NOI18N
                                    .concat(".properties");  //NOI18N
            FileObject resourceFO = Util.getResource(file, bundlePath);

            if ((resourceFO != null) && resourceFO.isValid() && !resourceFO.isVirtual()) {
                try {
                    setResource(DataObject.find(resourceFO));
                    warningLabel.setText("");  //NOI18N
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
               warningLabel.setText(
                       I18nUtil.getBundle().getString("LBL_InvalidBundle")  //NOI18N
                       + bundlePath);  //NOI18N
            }
        } else {
            warningLabel.setText("");  //NOI18N
        }
    }

    private void browseButtonActionPerformed(ActionEvent evt) {
        ResourceHolder rh = i18nString.getSupport().getResourceHolder();
        DataObject template;
        try {
            template = rh.getTemplate(rh.getResourceClasses()[0]);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        DataObject resource = SelectorUtils.selectOrCreateBundle(
                                file,
                                template,
                                i18nString.getSupport().getResourceHolder().getResource());

        //DataObject resource = SelectorUtils.selectBundle(this.project, file);
        if (resource != null) {
	    changeResource(resource);
            warningLabel.setText("");  //NOI18N
        }

    }

    private void replaceFormatTextFieldFocusGained(FocusEvent evt) {
        // Accessibility
        replaceFormatTextField.selectAll();
    }

    private boolean isReplaceFormatValid(String replaceFormat) {
        I18nString i18nReplaceCheck = (I18nString) i18nString.clone();
        i18nReplaceCheck.setReplaceFormat(replaceFormat);
        return i18nReplaceCheck.getReplaceString() != null;
    }

    private void replaceFormatButtonActionPerformed(ActionEvent evt) {
        final Dialog[] dialogs = new Dialog[1];
        final HelpStringCustomEditor customPanel = new HelpStringCustomEditor(
                                                        i18nString.getReplaceFormat(),
                                                        I18nUtil.getReplaceFormatItems(),
                                                        I18nUtil.getReplaceHelpItems(),
                                                        I18nUtil.getBundle().getString("LBL_ReplaceCodeFormat"),
                                                        I18nUtil.PE_REPLACE_CODE_HELP_ID);
        final DialogDescriptor dd = new DialogDescriptor(
            customPanel,
            I18nUtil.getBundle().getString("LBL_ReplaceStringFormatEditor"),//NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    final Object source = ev.getSource();
                    if (source == DialogDescriptor.OK_OPTION) {
                        String newText = (String) customPanel.getPropertyValue();

                        if (!newText.equals(replaceFormatTextField.getText())) {
                            i18nString.setReplaceFormat(newText);
                            updateReplaceText();
                            firePropertyChange(PROP_STRING, null, null);

                            // Reset option as well.
                            I18nUtil.getOptions().setReplaceJavaCode(newText);
                        }

                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else if (source == DialogDescriptor.CANCEL_OPTION) {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }
                       });
        dd.setValid(isReplaceFormatValid(i18nString.getReplaceFormat()));
        // look for ComboBox to create a hook, which would disable/enable
        // the OK button based on validity of replace format string
        for (Component c : customPanel.getComponents()) {
            if (c instanceof JComboBox) {
                try {
                ((JTextField) ((JComboBox) c).getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {

                        public void insertUpdate(DocumentEvent e) {
                            try {
                                dd.setValid(isReplaceFormatValid(e.getDocument().getText(0, e.getDocument().getLength())));
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }

                        public void removeUpdate(DocumentEvent e) {
                            try {
                                dd.setValid(isReplaceFormatValid(e.getDocument().getText(0, e.getDocument().getLength())));
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }

                        public void changedUpdate(DocumentEvent e) {
                            try {
                                dd.setValid(isReplaceFormatValid(e.getDocument().getText(0, e.getDocument().getLength())));
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
                break;
            }
        }
        dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
    }

    private void keyBundleComboActionPerformed(ActionEvent evt) {
        if (DUMMY_ACTION.equals(evt.getActionCommand())) {
            return;
        }

        String key = (String)keyBundleCombo.getSelectedItem();
        i18nString.setKey(key);
        updateKey();

        String value = i18nString.getSupport().getResourceHolder().getValueForKey(key);
        if (value != null) {
            i18nString.setValue(value);
            updateValue();
        }

        String comment = i18nString.getSupport().getResourceHolder().getCommentForKey(key);
        if (comment != null) {
            i18nString.setComment(comment);
            updateComment();
        }
        firePropertyChange(PROP_STRING, null, null);
    }

    private void commentTextFocusLost(FocusEvent evt) {
        i18nString.setComment(commentText.getText());
        updateComment();
        firePropertyChange(PROP_STRING, null, null);
    }

    private void valueTextFocusLost(FocusEvent evt) {
        valueTextChanged();
    }

    protected JButton argumentsButton;
    private JButton browseButton;
    private JLabel commentLabel;
    private JScrollPane commentScroll;
    private JTextArea commentText;
    private JLabel bundleNameLabel;
    private JComboBox keyBundleCombo;
    private JLabel keyLabel;
    private JButton replaceFormatButton;
    private JLabel replaceFormatLabel;
    private JTextField replaceFormatTextField;
    private JTextField resourceText;
    private JLabel valueLabel;
    private JScrollPane valueScroll;
    private JTextArea valueText;
    private JLabel warningLabel;
}