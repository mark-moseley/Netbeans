/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
package org.netbeans.modules.bpel.properties.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractCustomNodeEditor;
import org.netbeans.modules.bpel.nodes.ReceiveNode;
import org.netbeans.modules.bpel.properties.Util;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class ReceiveCustomEditor extends AbstractCustomNodeEditor<Receive> {
    
    static final long serialVersionUID = 1L;
    
    private ReceiveMainPanel mainPanel;
    private CorrelationTablePanel correlationPanel;
    private JTabbedPane tabbedPane;
    
    public ReceiveCustomEditor(ReceiveNode receiveNode, EditingMode mode) {
        super(receiveNode);
        if (mode != null) {
            setEditingMode(mode);
        }
        createContent();
        initControls();
        subscribeListeners();
    }
    
    public void createContent() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        //
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);
        //
        mainPanel = new ReceiveMainPanel(this);
        tabbedPane.addTab(NbBundle.getMessage(
                FormBundle.class, "LBL_Main_Tab"), mainPanel); // NOI18N
        //
        correlationPanel = new CorrelationTablePanel(this);
        tabbedPane.addTab(NbBundle.getMessage(
                FormBundle.class, "LBL_Correlations_Tab"), correlationPanel); // NOI18N
        //
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fireHelpContextChange();
            }
        });
        //
        Util.activateInlineMnemonics(this);
    }
    
    public HelpCtx getHelpCtx() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp != null && comp instanceof HelpCtx.Provider) {
            return ((HelpCtx.Provider)comp).getHelpCtx();
        } else {
            return new HelpCtx("org.netbeans.modules.bpel.design.nodes.NodeType.RECEIVE"); // NOI18N
            // return super.getHelpCtx();
        }
    }
}
