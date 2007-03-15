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

package org.netbeans.modules.sql.project.ui.wizards;

import org.openide.WizardDescriptor;

import javax.swing.JPanel;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
abstract class SettingsPanel extends JPanel {
    /**
     * DOCUMENT ME!
     *
     * @param settings DOCUMENT ME!
     */
    abstract void store(WizardDescriptor settings);

    /**
     * DOCUMENT ME!
     *
     * @param settings DOCUMENT ME!
     */
    abstract void read(WizardDescriptor settings);

    /**
     * DOCUMENT ME!
     *
     * @param settings DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    abstract boolean valid(WizardDescriptor settings);
}
