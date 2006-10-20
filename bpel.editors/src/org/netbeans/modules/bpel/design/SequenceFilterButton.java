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

package org.netbeans.modules.bpel.design;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.netbeans.modules.bpel.design.model.ViewFilters;
import org.openide.util.NbBundle;


public class SequenceFilterButton extends JToggleButton 
        implements ActionListener 
{
    
    private DesignView designView;
    

    public SequenceFilterButton(DesignView designView) {
        super(ICON);
        
        this.designView = designView;

        setSelected(getViewFilters().showImplicitSequences());
        setToolTipText(NbBundle.getMessage(DesignView.class, 
            "LBL_SequenceFilterButton")); // NOI18N
        setFocusable(false);
        
        addActionListener(this);
    }
    

    public void actionPerformed(ActionEvent e) {
        getViewFilters().setShowImplicitSequences(isSelected());
        updateView();
    }
    
    
    private void updateView(){
        designView.reloadModel();
        designView.diagramChanged();
        designView.getValidationDecorationProvider().updateDecorations();
    }
    
    
    private ViewFilters getViewFilters() {
        return designView.getModel().getFilters();
    }
    
    
    private static final Icon ICON = new ImageIcon(DesignView.class.getResource(
            "resources/sequence_filter.png")); // NOI18N
}
