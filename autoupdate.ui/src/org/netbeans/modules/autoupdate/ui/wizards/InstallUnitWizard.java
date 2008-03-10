/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.autoupdate.ui.wizards;

import org.netbeans.modules.autoupdate.ui.*;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator.LazyUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizard {
    
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    
    /** Creates a new instance of InstallUnitWizard */
    public InstallUnitWizard () {}
    
    public boolean invokeWizard (OperationType doOperation, PluginManagerUI manager) {
        InstallUnitWizardModel model = new InstallUnitWizardModel (doOperation);
        model.setPluginManager (manager);
        return invokeWizard (model);
    }
    
    public boolean invokeWizard (InstallUnitWizardModel model) {
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new InstallUnitWizardIterator (model);
        return implInvokeWizard (iterator);
    }
    
    public boolean invokeLazyWizard (Collection<LazyUnit> units, OperationType doOperation) {
        return implInvokeWizard (new LazyInstallUnitWizardIterator (units, doOperation));
    }
    
    private boolean implInvokeWizard (WizardDescriptor.Iterator<WizardDescriptor> iterator) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor (iterator);
        wizardDescriptor.setModal (true);
        
        wizardDescriptor.setTitleFormat (new MessageFormat(NbBundle.getMessage (InstallUnitWizard.class, "InstallUnitWizard_MessageFormat")));
        wizardDescriptor.setTitle (NbBundle.getMessage (InstallUnitWizard.class, "InstallUnitWizard_Title"));
        
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible (true);
        dialog.toFront ();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        log.log (Level.FINE, "InstallUnitWizard returns with value " + wizardDescriptor.getValue ());
        return !cancelled;
    }
    
}
