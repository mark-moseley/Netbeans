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
package org.netbeans.modules.editor.hints;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.ImplementationProvider;
import org.openide.util.NbBundle;

/**
 *
 *@author Jan Lahoda
 */
public class FixAction extends AbstractAction {
    
    public FixAction() {
        setEnabled(true);
        putValue(NAME, NbBundle.getMessage(FixAction.class, "NM_FixAction"));
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!HintsUI.getDefault().invokeDefaultAction()) {
            Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) e.getSource());
            
            if (actions == null)
                return ;
            
            int nextAction = 0;
            
            while (nextAction < actions.length && actions[nextAction] != this)
                nextAction++;
            
            nextAction++;
            
            if (actions.length > nextAction) {
                Action a = actions[nextAction]; //TODO - create GUI chooser
                if (a!=null && a.isEnabled()){
                    a.actionPerformed(e);
                }
            }
        }
    }
    
}

