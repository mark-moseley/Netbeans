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

package org.netbeans.modules.cnd.debugger.gdb.ui;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.gdb.GdbContext;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class MemoryViewTopComponent extends TopComponent implements PropertyChangeListener {

    private static MemoryViewTopComponent instance;
    /** path to the icon used by the component and its open action */
    // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
    static final String ICON_PATH = "org/netbeans/modules/cnd/debugger/gdb/resources/memory.png"; // NOI18N

    private static final String PREFERRED_ID = "MemoryViewTopComponent"; // NOI18N

    private MemoryViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(MemoryViewTopComponent.class, "CTL_MemoryViewTopComponent"));
        setToolTipText(NbBundle.getMessage(MemoryViewTopComponent.class, "HINT_MemoryViewTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        taResult.setFont(new Font("Monospaced", Font.PLAIN, taResult.getFont().getSize())); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        taResult = new javax.swing.JTextArea();
        fakePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfAddress = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfLength = new javax.swing.JTextField();

        taResult.setEditable(false);
        jScrollPane1.setViewportView(taResult);

        org.jdesktop.layout.GroupLayout fakePanelLayout = new org.jdesktop.layout.GroupLayout(fakePanel);
        fakePanel.setLayout(fakePanelLayout);
        fakePanelLayout.setHorizontalGroup(
            fakePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 420, Short.MAX_VALUE)
        );
        fakePanelLayout.setVerticalGroup(
            fakePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 2, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MemoryViewTopComponent.class, "MemoryViewTopComponent.jLabel1.text")); // NOI18N

        tfAddress.setText(org.openide.util.NbBundle.getMessage(MemoryViewTopComponent.class, "MemoryViewTopComponent.tfAddress.text")); // NOI18N
        tfAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfAddressActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MemoryViewTopComponent.class, "MemoryViewTopComponent.jLabel2.text")); // NOI18N

        tfLength.setText(org.openide.util.NbBundle.getMessage(MemoryViewTopComponent.class, "MemoryViewTopComponent.tfLength.text")); // NOI18N
        tfLength.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfLengthActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(fakePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(fakePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(tfLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tfAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(2, 2, 2)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfAddressActionPerformed
        update();
}//GEN-LAST:event_tfAddressActionPerformed

    private void tfLengthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLengthActionPerformed
        update();
}//GEN-LAST:event_tfLengthActionPerformed

    private void update() {
        taResult.setText("");
        GdbProxy gdb = GdbContext.getCurrentGdb();
        if (gdb == null) {
            return;
        }
        String addr = tfAddress.getText();
        if (addr == null || addr.length() == 0) {
            return;
        }
        String length = tfLength.getText();
        if (length == null || length.length() == 0) {
            return;
        }
        int len;
        try {
            len = Integer.decode(length);
        } catch (NumberFormatException nfe) {
            return;
        }
        if (len < 1) {
            return;
        }
        CommandBuffer cb = gdb.data_read_memory(addr, (len-1)/GdbProxy.MEMORY_READ_WIDTH+1);
        if (cb.isError()) {
            taResult.setText(cb.getError());
        } else {
            // parse output
            Map<String,String> res = GdbUtils.createMapFromString(cb.getResponse());
            String mem = res.get("memory"); // NOI18N
            List<String> lines = GdbUtils.createListOfValues(mem);
            StringBuilder text = new StringBuilder();
            for (String line : lines) {
                if (text.length() > 0) {
                    text.append("\n"); // NOI18N
                }
                Map<String,String> fields = GdbUtils.createMapFromString(line);
                text.append(fields.get("addr")); // NOI18N
                text.append("  "); // NOI18N
                text.append(prepareHex(fields.get("data"))); // NOI18N
                text.append(" "); // NOI18N
                text.append(fields.get("ascii")); // NOI18N
            }
            taResult.append(text.toString());
            taResult.setCaretPosition(0);
        }
    }

    /*
     * Removes quotes, colons and 0x prefixes from the hex presentation
     */
    private String prepareHex(String source) {
        StringBuilder res = new StringBuilder();
        int start = source.indexOf('"');
        while (start != -1) {
            int end = source.indexOf('"', start+1);
            if (end == -1) {
                break;
            }
            String number = source.substring(start+1, end);
            if (number.startsWith("0x")) { // NOI18N
                number = number.substring(2);
            }
            res.append(number.toUpperCase() + " "); // NOI18N
            start = source.indexOf('"', end+1);
        }
        return res.toString();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel fakePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea taResult;
    private javax.swing.JTextField tfAddress;
    private javax.swing.JTextField tfLength;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized MemoryViewTopComponent getDefault() {
        if (instance == null) {
            instance = new MemoryViewTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the MemoryViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized MemoryViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(MemoryViewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof MemoryViewTopComponent) {
            return (MemoryViewTopComponent) win;
        }
        Logger.getLogger(MemoryViewTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        GdbContext.getInstance().addPropertyChangeListener(GdbContext.PROP_STEP, this);
        GdbContext.getInstance().addPropertyChangeListener(GdbContext.PROP_EXIT, this);
        update();
    }

    @Override
    public void componentClosed() {
        GdbContext.getInstance().addPropertyChangeListener(GdbContext.PROP_STEP, this);
        GdbContext.getInstance().addPropertyChangeListener(GdbContext.PROP_EXIT, this);
    }

    @Override
    protected void componentShowing() {
        update();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                update();
            }
        });
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return MemoryViewTopComponent.getDefault();
        }
    }
}
