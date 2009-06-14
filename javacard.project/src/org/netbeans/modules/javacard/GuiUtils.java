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
package org.netbeans.modules.javacard;

import org.openide.awt.Mnemonics;
import org.openide.windows.WindowManager;
import org.openide.util.RequestProcessor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class GuiUtils {

    public static void prepareContainer(Container c) {
        prepareContainer (c, new FL());
    }

    private static void prepareContainer(Container c, FocusListener fl) {
        if (c instanceof AbstractButton) {
            AbstractButton ab = (AbstractButton) c;
            if (ab.getText().contains("&")) { //NOI18N
                Mnemonics.setLocalizedText(ab, ab.getText());
            }
        } else if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            if (l.getText().contains("&")) { //NOI18N
                Mnemonics.setLocalizedText(l, l.getText());
            }
        } else if (c instanceof JTextComponent) {
            JTextComponent jt = (JTextComponent) c;
            jt.addFocusListener(fl);
        } else if (c instanceof JPanel ||
                c instanceof JScrollPane ||
                c instanceof JTabbedPane ||
                c instanceof JSplitPane ||
                c instanceof JViewport ||
                c instanceof JLayeredPane ||
                c instanceof JFrame ||
                c instanceof JDialog ||
                c instanceof JRootPane) {
            for (Component cc : c.getComponents()) {
                if (cc instanceof Container) {
                    prepareContainer((Container) cc);
                }
            }
        }
    }

    public static void filterNonHexadecimalKeys(JTextComponent comp) {
        comp.addKeyListener(new HexKeyFilter());
    }

    public static void filterNonNumericKeys(JTextComponent comp) {
        comp.addKeyListener(new NumberKeyFilter());
    }

    public static void stopFilteringNonHexadecimalKeys(JTextComponent comp) {
        for (KeyListener kl : comp.getKeyListeners()) {
            if (kl instanceof HexKeyFilter) {
                comp.removeKeyListener(kl);
                break;
            }
        }
    }

    public static JDialog createModalProgressDialog(ProgressHandle handle, boolean includeDetail) {
        assert EventQueue.isDispatchThread();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(ProgressHandleFactory.createMainLabelComponent(handle), BorderLayout.SOUTH);
        panel.add(ProgressHandleFactory.createProgressComponent(handle), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setSize(new Dimension(400, 100));
        panel.setPreferredSize(new Dimension(400, 50));
        JDialog dlg = new JDialog(WindowManager.getDefault().getMainWindow(), true);
        dlg.setSize(400, 100);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(panel, BorderLayout.CENTER);
        if (includeDetail) {
            dlg.getContentPane().add(ProgressHandleFactory.createDetailLabelComponent(handle));
        }
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg.setAlwaysOnTop(true);
        dlg.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        return dlg;
    }

    public static void showProgressDialogAndRun (final ProgressHandle handle, final Runnable run, boolean includeDetail) {
        final JDialog dlg = createModalProgressDialog(handle, includeDetail);
        class WR extends WindowAdapter implements Runnable {

            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    try {
                        run.run();
                    } finally {
                        EventQueue.invokeLater(this);
                    }
                } else {
                    dlg.setVisible(false);
                    dlg.dispose();
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
                RequestProcessor.getDefault().post(this);
            }
        }
        WR wr = new WR();
        dlg.addWindowListener(wr);
        dlg.setVisible(true);
    }

    private static final class FL extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {
            JTextComponent jt = (JTextComponent) e.getComponent();
            jt.selectAll();
        }
    }

    private static final class NumberKeyFilter extends HexKeyFilter {
        @Override
        protected boolean allowed (char c) {
            return (c >= '0' && c <= '9');
        }
    }

    private static class HexKeyFilter extends KeyAdapter {

        private HexKeyFilter() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (!allowed(e.getKeyChar())) {
                Toolkit.getDefaultToolkit().beep();
                e.consume();
            } else {
                e.setKeyChar(Character.toUpperCase(e.getKeyChar()));
            }
        }

        protected boolean allowed(char c) {
            return (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') ||
                    (c >= '0' && c <= '9');
        }
    }
}
