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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.nodes.OnEventNode;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractCustomNodeEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class OnEventCustomEditor extends AbstractCustomNodeEditor<OnEvent> {
    
    static final long serialVersionUID = 1L;
    
    private OnEventMainPanel mainPanel;
    private CorrelationTablePanel correlationPanel;
    private JTabbedPane tabbedPane;
    
    public OnEventCustomEditor(OnEventNode onEventNode, EditingMode mode) {
        super(onEventNode);
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
        mainPanel = new OnEventMainPanel(this);
        tabbedPane.addTab(NbBundle.getMessage(
                FormBundle.class, "LBL_Main_Tab"), mainPanel); // NOI18N
        //
        correlationPanel = new CorrelationTablePanel(this);
        tabbedPane.addTab(NbBundle.getMessage(
                FormBundle.class, "LBL_Correlations_Tab"), correlationPanel); // NOI18N
        
        String editorACSD = NbBundle.getMessage(FormBundle.class, "ACSD_LBL_OnEvent_Editor");
        tabbedPane.getAccessibleContext().setAccessibleDescription(editorACSD);
        getAccessibleContext().setAccessibleDescription(editorACSD);
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
            return super.getHelpCtx();
        }
    }
}
