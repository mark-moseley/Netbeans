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
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "CVS|Search History..." popup or "Versioning|Search History..." main menu item.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class SearchHistoryAction extends Action {

    /** "Versioning" menu item. */
    private static final String VERSIONING_ITEM = Bundle.getStringTrimmed(
           "org.netbeans.modules.versioning.Bundle", "Menu/Window/Versioning");
    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle",
            "CTL_MenuItem_CVSCommands_Label");
    // "Search History..."
    private static final String SEARCH_POPUP_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle",
            "CTL_PopupMenuItem_SearchHistory");
    // "Search "filename" History..."
    private static final String SEARCH_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.log.Bundle",
            "CTL_MenuItem_SearchHistory_Context");
    
    /** Creates new SearchHistory instance. */
    public SearchHistoryAction() {
        super(VERSIONING_ITEM+"|"+SEARCH_ITEM, CVS_ITEM+"|"+SEARCH_POPUP_ITEM);
    }
    
    /** Performs main menu with exact file name.
     * @param filename file name
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Search "filename" History...
        this.menuPath = VERSIONING_ITEM+"|"+
            Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.log.Bundle",
                "CTL_MenuItem_SearchHistory_Context",
                 new String[] {filename});
        try {
            super.performMenu();
        } finally {
            this.menuPath = oldMenuPath;
        }
    }
}

