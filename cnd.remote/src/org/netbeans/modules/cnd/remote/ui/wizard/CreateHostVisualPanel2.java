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

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSetReporter;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.ui.options.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/*package*/ final class CreateHostVisualPanel2 extends JPanel {

    private final ChangeListener wizardListener;

    public CreateHostVisualPanel2(ChangeListener listener) {
        wizardListener = listener;
        initComponents();

        textLoginName.setText(System.getProperty("user.name"));

        DocumentListener dl = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }
        };

        textLoginName.getDocument().addDocumentListener(dl);
        textPassword.getDocument().addDocumentListener(dl);
    }

    private void fireChange() {
        hostFound = null;
        wizardListener.stateChanged(null);
    }

    @Override
    public String getName() {
        return CreateHostWizardIterator.getString("CreateHostVisualPanel2.Title");
    }

    private String hostname;
    private int port;
    private ToolsCacheManager cacheManager;

    void init(String hostname, Integer port, ToolsCacheManager cacheManager) {
        this.hostname = hostname;
        this.port = (port == null) ? ExecutionEnvironmentFactory.DEFAULT_PORT : port.intValue();
        this.cacheManager = cacheManager;
    }

    private String getLoginName() {
        return textLoginName.getText();
    }

    String getPassword() {
        return new String(textPassword.getPassword());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        textLoginName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        textPassword = new javax.swing.JPasswordField();
        cbSavePassword = new javax.swing.JCheckBox();
        pbarStatusPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tpOutput = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(534, 409));
        setRequestFocusEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.jLabel1.text")); // NOI18N

        textLoginName.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.textLoginName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.jLabel2.text")); // NOI18N

        textPassword.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.textPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSavePassword, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.cbSavePassword.text")); // NOI18N

        pbarStatusPanel.setMaximumSize(new java.awt.Dimension(2147483647, 10));
        pbarStatusPanel.setMinimumSize(new java.awt.Dimension(100, 10));
        pbarStatusPanel.setLayout(new java.awt.BorderLayout());

        tpOutput.setEditable(false);
        tpOutput.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.tpOutput.text")); // NOI18N
        tpOutput.setOpaque(false);
        jScrollPane1.setViewportView(tpOutput);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.jLabel3.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, cbSavePassword)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel2)
                                    .add(jLabel1))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(textPassword)
                                    .add(textLoginName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 204, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(237, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(pbarStatusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(textLoginName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(textPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbSavePassword)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 232, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pbarStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private ProgressHandle phandle;
    
    /* package-local */ ExecutionEnvironment getHost() {
        return hostFound;
    }

    /* package-local */ Runnable getRunOnFinish() {
        return runOnFinish;
    }

    private ExecutionEnvironment hostFound = null;
    private Runnable runOnFinish = null;

    private ExecutionEnvironment getExecutionEnvironment() {
        return ExecutionEnvironmentFactory.createNew(getLoginName(), hostname, port);
    }

    public void enableControls(boolean enable) {
        textPassword.setEnabled(enable);
        textLoginName.setEnabled(enable);
        cbSavePassword.setEnabled(enable);
    }

    public boolean canValidateHost() {
        return true;
    }

    public void validateHost() {
        final String password = getPassword();
        final boolean rememberPassword = cbSavePassword.isSelected();
        final ExecutionEnvironment env = getExecutionEnvironment();
        final RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
        final boolean alreadyOnline = record.isOnline();
        if (alreadyOnline) {
            String message = NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgAlreadyConnected1");
            message = String.format(message, env.toString());
            tpOutput.setText(message);
        } else {
            record.resetOfflineState(); // this is a do-over
            tpOutput.setText("");
        }
        phandle = ProgressHandleFactory.createHandle(""); ////NOI18N
        pbarStatusPanel.removeAll();
        pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.CENTER);
        pbarStatusPanel.validate();
        phandle.start();        
        // move expensive operation out of EDT

        if (!alreadyOnline) {
            addOuputTextInUiThread(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgConnectingTo",
                    env.getHost()));
        }
        try {
            if (password == null || password.length() == 0) {
                ConnectionManager.getInstance().connectTo(env);
            } else {
                ConnectionManager.getInstance().connectTo(env, password.toCharArray(), rememberPassword);
            }
        } catch (IOException ex) {
            addOuputTextInUiThread("\n" + RemoteCommandSupport.getMessage(ex)); //NOI18N
            phandle.finish();
            return;
        } catch (CancellationException ex) {
            phandle.finish();
            return;
        }
        if (!alreadyOnline) {
            record.init(null);
        }
        if (record.isOnline()) {
            if (!alreadyOnline) {
                addOuputTextInUiThread(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgDone") + '\n');
            }
            CompilerSetReporter.setWriter(new Writer() {

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    final String value = new String(cbuf, off, len);
                    addOuputTextInUiThread(value);
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void close() throws IOException {
                }
            });
            final CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(env);
           csm.initialize(false, false);
            runOnFinish = new Runnable() {
                public void run() {
                    csm.finishInitialization();
                }
            };
            hostFound = csm.getExecutionEnvironment(); //TODO: no validations, pure cheat
            wizardListener.stateChanged(null);
        } else {
            addOuputTextInUiThread(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.ErrConn")
                    + '\n' + record.getReason()); //NOI18N
        }
        phandle.finish();
        CompilerSetReporter.setWriter(null);
        // back to EDT to work with Swing
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                pbarStatusPanel.setVisible(false);
                if (alreadyOnline) {
                    addOuputTextInUiThread('\n' + NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgAlreadyConnected2"));
                }
            }
        });
    }

    private void addOuputTextInUiThread(final String value) {
        Runnable r = new Runnable() {
                public void run() {
                    tpOutput.setText(tpOutput.getText() + value);
                }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbSavePassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTextField textLoginName;
    private javax.swing.JPasswordField textPassword;
    private javax.swing.JTextPane tpOutput;
    // End of variables declaration//GEN-END:variables
}

