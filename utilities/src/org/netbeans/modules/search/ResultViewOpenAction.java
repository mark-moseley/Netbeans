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


package org.netbeans.modules.search;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * Action which opens the Search Results window.
 *
 * @see  ResultView
 * @author  Marian Petras
 */
public class ResultViewOpenAction extends AbstractAction {

    /**
     * Creates an instance of this action.
     */
    public ResultViewOpenAction() {
        String name = NbBundle.getMessage(
                ResultViewOpenAction.class,
                "TEXT_ACTION_SEARCH_RESULTS");                          //NOI18N
        putValue(NAME, name);
        putValue("iconBase",                                            //NOI18N
                 "org/netbeans/modules/search/res/find.gif");           //NOI18N
    }
    
    /**
     * Opens and activates the Search Results window.
     *
     * @param  e  event that caused this action to be called
     */
    public void actionPerformed(ActionEvent e) {
        ResultView resultView = ResultView.getInstance();
        resultView.open();
        resultView.requestActive();
    }
    
}
