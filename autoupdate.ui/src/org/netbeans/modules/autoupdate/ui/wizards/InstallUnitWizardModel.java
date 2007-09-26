/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizardModel extends OperationWizardModel {
    private Installer installer = null;
    private OperationType doOperation;
    private static Set<String> approvedLicences = new HashSet<String> ();
    private InstallSupport support;
    private InstallSupport additionallySupport = null;
    
    /** Creates a new instance of InstallUnitWizardModel */
    public InstallUnitWizardModel (OperationType doOperation) {
        this.doOperation = doOperation;
        assert getBaseContainer () != null : "The base container for operation " + doOperation + " must exist!";
    }
    
    public OperationType getOperation () {
        return doOperation;
    }
    
    public OperationContainer getBaseContainer () {
        OperationContainer<InstallSupport> c = null;
        switch (getOperation ()) {
        case INSTALL :
            c = Containers.forAvailable ();
            support = Containers.forAvailable ().getSupport ();
            break;
        case UPDATE :
            c = Containers.forUpdate ();
            support = Containers.forUpdate ().getSupport ();
            break;
        case LOCAL_DOWNLOAD :
            OperationContainer<InstallSupport> additionallyContainer;
            if (Containers.forUpdateNbms ().listAll ().isEmpty ()) {
                c = Containers.forAvailableNbms ();
                additionallyContainer = Containers.forUpdateNbms ();
            } else {
                c = Containers.forUpdateNbms ();
                additionallyContainer = Containers.forAvailableNbms ();
            }
            additionallySupport = additionallyContainer.getSupport ();
            support = c.getSupport ();
            break;
        }
        return c;
    }
    
    public OperationContainer<OperationSupport> getCustomHandledContainer () {
        return Containers.forCustomInstall ();
    }
    
    public boolean allLicensesApproved () {
        for (UpdateElement el : getVisibleUpdateElements (getAllUpdateElements (), false, getOperation ())) {
            if (el.getLicence () != null && ! approvedLicences.contains (el.getLicence ())) {
                return false;
            }
        }
        return true;
    }
    
    public void addApprovedLicenses (Collection<String> licences) {
        approvedLicences.addAll (licences);
    }
    
    public InstallSupport getInstallSupport () {
        return support;
    }
    
    public InstallSupport getAdditionallyInstallSupport () {
        return additionallySupport;
    }
    
    public void setInstaller (Installer i) {
        installer = i;
    }
    
    public Installer getInstaller () {
        return installer;
    }
    
    @Override
    public void doCleanup (boolean cancel) throws OperationException {
        try {
            if (cancel) {
                if (getBaseContainer ().getSupport () instanceof InstallSupport) {
                    if (OperationType.LOCAL_DOWNLOAD == getOperation ()) {
                        InstallSupport asupp = Containers.forAvailableNbms ().getSupport ();
                        if (asupp != null) {
                            asupp.doCancel ();
                        }
                        InstallSupport usupp = Containers.forUpdateNbms ().getSupport ();
                        if (usupp != null) {
                            usupp.doCancel ();
                        }
                        Containers.forAvailableNbms ().removeAll ();
                        Containers.forUpdateNbms ().removeAll ();
                    } else {
                        InstallSupport isupp = (InstallSupport) getBaseContainer ().getSupport ();
                        if (isupp != null) {
                            isupp.doCancel ();
                        }
                    }
                } else {
                    OperationSupport osupp = (OperationSupport) getBaseContainer ().getSupport ();
                    if (osupp != null) {
                        osupp.doCancel ();
                    }
                }
                OperationSupport osupp = getCustomHandledContainer ().getSupport ();
                if (osupp != null) {
                    osupp.doCancel ();
                }
            }
        } catch (Exception x) {
            Exceptions.printStackTrace (x);
        } finally {
            super.doCleanup (false);
        }
    }

}
