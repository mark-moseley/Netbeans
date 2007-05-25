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
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "Versioning|CVS|Import into Repository" main menu item or
 * "Versioning|Import into CVS Repository..." popup.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class ImportAction extends ActionNoBlock {
    /** "Versioning" menu item. */
    public static final String VERSIONING_ITEM = Bundle.getStringTrimmed(
           "org.netbeans.modules.versioning.Bundle", "Menu/Window/Versioning");
    /** "CVS" menu item. */
    public static final String CVS_ITEM = Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle",
                "CTL_MenuItem_CVSCommands_Label");
    // "Import into Repository"
    private static final String IMPORT_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", "BK0006");
    // Import into CVS Repository...
    private static final String IMPORT_CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "CTL_PopupMenuItem_Import");
    
    /** Creates new ImportAction instance. */
    public ImportAction() {
        super(VERSIONING_ITEM+"|"+CVS_ITEM+"|"+IMPORT_ITEM, VERSIONING_ITEM+"|"+IMPORT_CVS_ITEM);
    }
}

