/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Wizard for creating new Actions.
 *
 * @author Martin Krauskopf
 */
final class NewActionIterator extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
    private DataModel data;
    
    private NewActionIterator() {}
    
    public static NewActionIterator createIterator() {
        return new NewActionIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    private boolean isIcon(FileObject icon) {
        return icon.getExt().equals("gif") || icon.getExt().equals("png"); // NOI18N
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new ActionTypePanel(wiz, data),
            new GUIRegistrationPanel(wiz, data),
            new NameIconLocationPanel(wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
}
