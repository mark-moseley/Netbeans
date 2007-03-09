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

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.SSHConnection;
import org.netbeans.modules.versioning.util.Utils;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.net.SocketFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * UI for CvsRootSettings. After initialization data
 * are taken directly from UI. These are propagated
 * into CvsRootSettings on {@link #storeValidValues()}.
 *
 * @author Petr Kuzel
 */
public final class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, ActionListener, DocumentListener {

    private static final String USE_INTERNAL_SSH = "repositoryStep.useInternalSSH";
    private static final String EXT_COMMAND = "repositoryStep.extCommand";
    private static final String RECENT_ROOTS = "repositoryStep.recentRoots";

    private RequestProcessor.Task updatePasswordTask;
    private volatile boolean passwordExpected;

    private ProgressHandle progress;
    private JComponent progressComponent;
    private JLabel progressLabel;

    private volatile boolean internalDocumentChange;
    private Thread backgroundValidationThread;
    private RepositoryPanel repositoryPanel;
    private String scrambledPassword;
    private final String initialCvsRoot;
    private String preferedCvsRoot;


    /**
     * Creates multiple roots customizer. 
     */
    public RepositoryStep() {
        initialCvsRoot = null;
    }

    /**
     * Creates single root customizer
     */
    public RepositoryStep(String root) {
        initialCvsRoot = root;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(RepositoryStep.class);
    }
    
    /**
     * Preselected cvs root (first in list).
     */
    public void initPreferedCvsRoot(String root) {
        preferedCvsRoot = root;
    }

    protected JComponent createComponent() {
        repositoryPanel = new RepositoryPanel();

        // password field, features automatic fill from ~/.cvspass

        repositoryPanel.extSshRadioButton.addActionListener(this);
        repositoryPanel.internalSshRadioButton.addActionListener(this);
        repositoryPanel.extCommandTextField.getDocument().addDocumentListener(this);
        repositoryPanel.extPasswordField.getDocument().addDocumentListener(this);
        repositoryPanel.passwordTextField.getDocument().addDocumentListener(this);
        RequestProcessor requestProcessor = new RequestProcessor();
        updatePasswordTask = requestProcessor.create(new Runnable() {
            public void run() {
                String cvsRoot = selectedCvsRoot();
                String password = PasswordsFile.findPassword(cvsRoot);
                if (password != null && passwordExpected) {
                    String fakePasswordWithProperLen = new String(password).substring(1);
                    scrambledPassword = password;
                    internalDocumentChange = true;
                    repositoryPanel.passwordTextField.setText(fakePasswordWithProperLen);
                    internalDocumentChange = false;
                    cancelPasswordUpdate();
                }
            }
        });

        // roots combo setup, keeping history

        Set recentRoots = new LinkedHashSet();
        if (preferedCvsRoot != null) {
            recentRoots.add(preferedCvsRoot);
        }
        recentRoots.addAll(Utils.getStringList(CvsModuleConfig.getDefault().getPreferences(), RECENT_ROOTS));
        if (initialCvsRoot != null) {
            // it's first => initially selected
            recentRoots.add(initialCvsRoot);
        }
        Iterator cvsPassRoots = PasswordsFile.listRoots(":pserver:").iterator();  // NOI18N
        while (cvsPassRoots.hasNext()) {
            String next = (String) cvsPassRoots.next();
            if (recentRoots.contains(next) == false) {
                recentRoots.add(next);
            }
        }
        // templates for supported connection methods
        String user = System.getProperty("user.name", ""); // NOI18N
        if (user.length() > 0) user += "@"; // NOI18N
        recentRoots.add(":pserver:" + user);  // NOI18N
        recentRoots.add(":ext:" + user); // NOI18N
        recentRoots.add(":fork:"); // NOI18N
        recentRoots.add(":local:"); // NOI18N

        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector(recentRoots));
        repositoryPanel.rootComboBox.setModel(rootsModel);
        repositoryPanel.rootComboBox.addActionListener(this);
        Component editor = repositoryPanel.rootComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        if (recentRoots.size() == 0) {
            textEditor.setText(":pserver:" + user);    // NOI18N
        } else {
            validateCvsRoot();
            CVSRoot root = getCVSRoot();
            schedulePasswordUpdate();
        }
        textEditor.selectAll();
        textEditor.getDocument().addDocumentListener(this);

        boolean useInternalSsh = CvsModuleConfig.getDefault().getPreferences().getBoolean(USE_INTERNAL_SSH, true);
        repositoryPanel.internalSshRadioButton.setSelected(useInternalSsh);
        repositoryPanel.extSshRadioButton.setSelected(!useInternalSsh);
        
        String extCommand = CvsModuleConfig.getDefault().getPreferences().get(EXT_COMMAND, "");
        repositoryPanel.extCommandTextField.setText(extCommand);

        repositoryPanel.proxyConfigurationButton.addActionListener(this);
        repositoryPanel.editButton.addActionListener(this);

        valid();
        onCvsRootChange();

        if (initialCvsRoot != null) {
            boolean chooserVisible = false;
            repositoryPanel.headerLabel.setVisible(chooserVisible);
            repositoryPanel.rootsLabel.setVisible(chooserVisible);
            repositoryPanel.rootComboBox.setVisible(chooserVisible);
            repositoryPanel.descLabel.setVisible(chooserVisible);
            repositoryPanel.editButton.setVisible(chooserVisible);
        }

        return repositoryPanel;
    }

    /**
     * Heavy validation over network.
     * Sets wizard as invalid to disable next button
     * and starts. It's invoked in background validation thread.
     */
    protected void validateBeforeNext() {

        if (validateCvsRoot() == false) {
            return;
        }
        final CVSRoot root = getCVSRoot();

        backgroundValidationThread = Thread.currentThread();

        final String invalidMsg[] = new String[1]; // ret value
        Runnable worker = new Runnable() {

            private void fail(String msg) {
                invalidMsg[0] = msg;
            }

            public void run() {

                String host = root.getHostName();
                String userName = root.getUserName();
                int port = root.getPort();
                Socket sock = null;
                Connection connection = null;

                try {
                    if (root.isLocal()) {
                        LocalConnection lconnection = new LocalConnection();
                        lconnection.setRepository(root.getRepository());
                        lconnection.verify();
                    } else {
                        invalid(null);
                        progress(NbBundle.getMessage(CheckoutWizard.class, "BK2011"));
                        SocketFactory factory = SocketFactory.getDefault();

                        // check raw network reachability

                        if (CVSRoot.METHOD_PSERVER.equals(root.getMethod())) {
                            port = port == 0 ? 2401 : port;  // default port

                            SocketAddress target = new InetSocketAddress(host, port);
                            sock = factory.createSocket();
                            sock.connect(target, 5000);
                            sock.close();

                            // try to login
                            progress(NbBundle.getMessage(CheckoutWizard.class, "BK2010"));
                            PServerConnection pconnection = new PServerConnection(root, factory);
                            String password = getScrambledPassword();
                            pconnection.setEncodedPassword(password);
                            pconnection.verify();
                        } else if (CVSRoot.METHOD_EXT.equals(root.getMethod())) {
                            if (repositoryPanel.internalSshRadioButton.isSelected()) {
                                port = port == 0 ? 22 : port;  // default port
                                String password = repositoryPanel.extPasswordField.getText();
                                SSHConnection sshConnection = new SSHConnection(factory, host, port, userName, password);
                                sshConnection.setRepository(root.getRepository());
                                sshConnection.verify();
                            } else {
                                String command = repositoryPanel.extCommandTextField.getText();
                                String userOption = ""; // NOI18N
                                if ( userName != null) {
                                    userOption = " -l " + userName;  // NOI18N
                                }
                                String cvs_server = System.getenv("CVS_SERVER") != null?
                                    System.getenv("CVS_SERVER") + " server": "cvs server";  // NOI18N
                                command += " " + host + userOption + " " + cvs_server; // NOI18N
                                ExtConnection econnection = new ExtConnection(command);
                                econnection.setRepository(root.getRepository());
                                econnection.verify();
                            }
                        } else {
                            assert false : "Login check implemented only for pserver";  // NOI18N
                        }
                    }

                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2019")); // NOi18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                    String msg = NbBundle.getMessage(CheckoutWizard.class, "BK1001", host);
                    fail(msg);
                } catch (AuthenticationException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Connection authentification verification failed.");  // NOI18N
                    err.notify(ErrorManager.INFORMATIONAL, e);

                    // enhanced contact, if getLocalizedMessage strts with "<" it contains our approved texts
                    String msg;
                    if (e.getLocalizedMessage() != null && e.getLocalizedMessage().startsWith("<")) {  // NOI18N
                        msg = e.getLocalizedMessage();
                    } else {
                        if (root.isLocal()) {
                            msg = NbBundle.getMessage(CheckoutWizard.class, "BK1004");
                        } else {
                            msg = NbBundle.getMessage(CheckoutWizard.class, "BK1002");
                        }
                    }
                    fail(msg);
                } finally {
                    if (sock != null) {
                        try {
                            sock.close();
                        } catch (IOException e) {
                            // already closed
                        }
                    }
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (IOException e) {
                            // already closed
                        }
                    }
                }
            }
        };

        Thread workerThread = new Thread(worker, "CVS I/O Probe ");  // NOI18N
        workerThread.start();
        try {
            workerThread.join();
            if (invalidMsg[0] == null) {
                valid();
                storeValidValues();
            } else {
                valid(invalidMsg[0]);
            }
        } catch (InterruptedException e) {
            invalid(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2023"));
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Passing interrupt to possibly uninterruptible nested thread: " + workerThread);  // NOI18N
            workerThread.interrupt();
            err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            backgroundValidationThread = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    validationDone();
                }
            });
        }

    }

    private void progress(String message) {
        if (progressLabel != null) {
            progressLabel.setText(message);
        }
    }

    private void validationDone() {
        progress.finish();
        repositoryPanel.jPanel1.remove(progressComponent);
        repositoryPanel.jPanel1.revalidate();
        repositoryPanel.jPanel1.repaint();
        editable(true);
    }

    private void editable(boolean editable) {
        repositoryPanel.rootComboBox.setEditable(editable);
        repositoryPanel.passwordTextField.setEditable(editable);
        repositoryPanel.extCommandTextField.setEditable(editable);
        repositoryPanel.extPasswordField.setEditable(editable);

        repositoryPanel.proxyConfigurationButton.setEnabled(editable);
        repositoryPanel.extREmemberPasswordCheckBox.setEnabled(editable);
        repositoryPanel.internalSshRadioButton.setEnabled(editable);
        repositoryPanel.extSshRadioButton.setEnabled(editable);
    }


    void storeValidValues() {
        String root = selectedCvsRoot();
        CVSRoot cvsRoot = CVSRoot.parse(root);
        if (root.startsWith(":pserver:")) { // NOI18N
            try {
                // CVSclient library reads password directly from .cvspass file
                // store it here into the file. It's potentionally necessary for
                // next step branch and module browsers

                PasswordsFile.storePassword(root, getScrambledPassword());
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2020"));
                err.notify(e);
            }
        } else if (root.startsWith(":ext:")) {  // NOI18N
            boolean internalSsh = repositoryPanel.internalSshRadioButton.isSelected();
            CvsModuleConfig.ExtSettings extSettings = new CvsModuleConfig.ExtSettings();
            extSettings.extUseInternalSsh = internalSsh;
            extSettings.extPassword = repositoryPanel.extPasswordField.getText();
            extSettings.extRememberPassword = repositoryPanel.extREmemberPasswordCheckBox.isSelected();
            extSettings.extCommand = repositoryPanel.extCommandTextField.getText();
            CvsModuleConfig.getDefault().getPreferences().putBoolean(USE_INTERNAL_SSH, internalSsh);
            CvsModuleConfig.getDefault().getPreferences().put(EXT_COMMAND, extSettings.extCommand);
            CvsModuleConfig.getDefault().setExtSettingsFor(cvsRoot, extSettings);
        }

        Utils.insert(CvsModuleConfig.getDefault().getPreferences(), RECENT_ROOTS, root, 8);
    }

    /**
     * Fast root syntax check. It can invalidate whole step
     * but neder set it as valid.
     */
    private boolean validateCvsRoot() {
        String cvsRoot = selectedCvsRoot();
        String errorMessage = null;
        boolean supportedMethod = false;
        if (cvsRoot != null) {
            supportedMethod |= cvsRoot.startsWith(":pserver:"); // NOI18N
            supportedMethod |= cvsRoot.startsWith(":local:"); // NOI18N
            supportedMethod |= cvsRoot.startsWith(":fork:"); // NOI18N
            supportedMethod |= cvsRoot.startsWith(":ext:"); // NOI18N
        }
        if (supportedMethod == false ) {
            errorMessage = NbBundle.getMessage(CheckoutWizard.class, "BK1000");
        } else {
            try {
                CVSRoot.parse(cvsRoot);
            } catch (IllegalArgumentException ex) {
                errorMessage = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2021") + ex.getLocalizedMessage();
            }
        }
        if (errorMessage != null) {
            invalid(errorMessage);
        }
        return errorMessage == null;
    }

    /**
     * On valid CVS root loads UI fields from CvsRootSettings.
     * Always updates UI fields visibility.
     */
    private void onCvsRootChange() {
        if (validateCvsRoot()) {
            valid();
            CVSRoot root = getCVSRoot();
            if (CVSRoot.METHOD_EXT.equals(root.getMethod())) {
                if (CvsModuleConfig.getDefault().hasExtSettingsFor(root)) {
                    CvsModuleConfig.ExtSettings extSettings = CvsModuleConfig.getDefault().getExtSettingsFor(root);
                    repositoryPanel.internalSshRadioButton.setSelected(extSettings.extUseInternalSsh);
                    repositoryPanel.extPasswordField.setText(extSettings.extPassword);
                    repositoryPanel.extREmemberPasswordCheckBox.setSelected(extSettings.extRememberPassword);
                    repositoryPanel.extCommandTextField.setText(extSettings.extCommand);
                }
            }
            repositoryPanel.extPasswordField.setEditable(root.getPassword() == null);
            repositoryPanel.passwordTextField.setEditable(root.getPassword() == null);
            if (root.getPassword() != null) {
                if (CVSRoot.METHOD_EXT.equals(root.getMethod())) {
                    repositoryPanel.extPasswordField.setText(root.getPassword());
                } else if (CVSRoot.METHOD_PSERVER.equals(root.getMethod())) {
                    repositoryPanel.passwordTextField.setText(root.getPassword());
                }
            } else {
                schedulePasswordUpdate();
            }
        }
        updateVisibility();
        updateLabel();
    }

    private void updateLabel() {
        String cvsRoot = selectedCvsRoot();
        if (cvsRoot.startsWith(":pserver:")) { // NOI18N
            repositoryPanel.descLabel.setText("(:pserver:username@hostname:/repository_path)");  // NOI18N
        } else if (cvsRoot.startsWith(":local:")) { // NOI18N
            repositoryPanel.descLabel.setText("(:local:/repository_path)");  // NOI18N
        } else if (cvsRoot.startsWith(":fork:")) { // NOI18N
            repositoryPanel.descLabel.setText("(:fork:/repository_path)");  // NOI18N
        } else if (cvsRoot.startsWith(":ext:")) { // NOI18N
            repositoryPanel.descLabel.setText("(:ext:username@hostname:/repository_path)");  // NOI18N
        } else {
            repositoryPanel.descLabel.setText(NbBundle.getMessage(CheckoutWizard.class, "BK1014"));
        }

    }

    /** Shows proper fields depending on CVS root connection method. */
    private void updateVisibility() {
        String root = selectedCvsRoot();
        boolean showPserverFields = root.startsWith(":pserver:");  // NOI18N
        boolean showExtFields = root.startsWith(":ext:"); // NOI18N

        repositoryPanel.passwordTextField.setVisible(showPserverFields);
        repositoryPanel.pPaswordLabel.setVisible(showPserverFields);

        repositoryPanel.internalSshRadioButton.setVisible(showExtFields);
        repositoryPanel.extSshRadioButton.setVisible(showExtFields);

        repositoryPanel.extPasswordLabel5.setVisible(showExtFields);
        repositoryPanel.extPasswordField.setVisible(showExtFields);
        repositoryPanel.extPasswordField.setEnabled(repositoryPanel.internalSshRadioButton.isSelected());
        repositoryPanel.extREmemberPasswordCheckBox.setVisible(showExtFields);
        repositoryPanel.extREmemberPasswordCheckBox.setEnabled(repositoryPanel.internalSshRadioButton.isSelected());

        repositoryPanel.extCommandLabel.setVisible(showExtFields);
        repositoryPanel.extCommandTextField.setVisible(showExtFields);
        repositoryPanel.extCommandTextField.setEnabled(repositoryPanel.extSshRadioButton.isSelected());

        repositoryPanel.proxyConfigurationButton.setVisible(showPserverFields || showExtFields);
        repositoryPanel.proxyConfigurationButton.setEnabled(!repositoryPanel.extSshRadioButton.isSelected());
        repositoryPanel.browseButton.setVisible(showExtFields);
        repositoryPanel.browseButton.setEnabled(repositoryPanel.extSshRadioButton.isSelected());
    }

    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    private String selectedCvsRoot() {
        if (initialCvsRoot != null) {
            return initialCvsRoot;
        }
        final String cvsRoot[] = new String[1];
        try {
            Runnable awt = new Runnable() {
                public void run() {
                    cvsRoot[0] = (String) repositoryPanel.rootComboBox.getEditor().getItem();
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                awt.run();
            } else {
                SwingUtilities.invokeAndWait(awt);
            }
            String root = cvsRoot[0].trim();
            try {
                return CVSRoot.parse(root).toString();
            } catch (Exception e) {
                return root;                
            }
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        } catch (InvocationTargetException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        }
        return null;
    }

    private CVSRoot getCVSRoot() {
        try {
            String root = selectedCvsRoot();
            return CVSRoot.parse(root);
        } catch (IllegalArgumentException e) {
            // expected, it means invalid root
        }
        return null;
    }

    /**
     * Visually notifies user about password length
     */
    private void schedulePasswordUpdate() {
        String root = selectedCvsRoot();
        if (root.startsWith(":pserver:")) { // NOI18N
            passwordExpected = true;
            updatePasswordTask.schedule(10);
        }
    }

    private void cancelPasswordUpdate() {
        passwordExpected = false;
    }

    private void onPasswordChange() {
        cancelPasswordUpdate();
        scrambledPassword = null;
        if (validateCvsRoot()) {
            valid();
        }
    }
    
    private void setValid() {
        valid();
    }
    
    private void onProxyConfiguration() {
        OptionsDisplayer.getDefault().open("General");
        if (validateCvsRoot()) {
            valid();
        }
    }
    
    private void editRoot() {
        String root = selectedCvsRoot();
        root = RootWizard.editCvsRoot(root);
        if (root != null) {
            repositoryPanel.rootComboBox.setSelectedItem(root);
        }
    }

    // hooks

    public void actionPerformed(ActionEvent e) {
        if (repositoryPanel.proxyConfigurationButton == e.getSource()) {
            onProxyConfiguration();
        } else if (repositoryPanel.rootComboBox == e.getSource()) {
            onCvsRootChange();
        } else if (repositoryPanel.editButton == e.getSource()) {
            editRoot();
        } else if (repositoryPanel.extSshRadioButton == e.getSource()) {
            setValid();
            validateCvsRoot();
            updateVisibility();
        } else if (repositoryPanel.internalSshRadioButton == e.getSource()) {
            setValid();
            validateCvsRoot();
            updateVisibility();
        } else {
            assert false : "Unexpected event source: " + e.getSource();  // NOI18N
        }
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selectedCvsRoot
        if (internalDocumentChange) return;
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == repositoryPanel.passwordTextField.getDocument()) {
                    onPasswordChange();
                } else if (e.getDocument() == ((JTextComponent) repositoryPanel.rootComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onCvsRootChange();
                } else if (e.getDocument() == repositoryPanel.extPasswordField.getDocument()) {
                    setValid();
                    validateCvsRoot();
                } else if (e.getDocument() == repositoryPanel.extCommandTextField.getDocument()) {
                    setValid();
                    validateCvsRoot();
                }
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    public void prepareValidation() {
        progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CheckoutWizard.class, "BK2012"));
        JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
        JButton stopButton = new JButton(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2022"));
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (backgroundValidationThread != null) {
                    backgroundValidationThread.interrupt();
                }
            }
        });
        progressComponent = new JPanel();
        progressComponent.setLayout(new BorderLayout(6, 0));
        progressLabel = new JLabel();
        progressComponent.add(progressLabel, BorderLayout.NORTH);
        progressComponent.add(bar, BorderLayout.CENTER);
        progressComponent.add(stopButton, BorderLayout.LINE_END);
        progress.start(/*2, 5*/);
        repositoryPanel.jPanel1.setLayout(new BorderLayout());
        repositoryPanel.jPanel1.add(progressComponent, BorderLayout.SOUTH);
        repositoryPanel.jPanel1.revalidate();

        editable(false);
    }

    private String getPassword() {
        return new String(repositoryPanel.passwordTextField.getPassword());
    }

    public String getCvsRoot() {
        return selectedCvsRoot();
    }

    public String getScrambledPassword() {
        if (scrambledPassword == null) {
            String plainPassword = getPassword();
            scrambledPassword = StandardScrambler.getInstance().scramble(plainPassword);
        }
        return scrambledPassword;
    }
}
