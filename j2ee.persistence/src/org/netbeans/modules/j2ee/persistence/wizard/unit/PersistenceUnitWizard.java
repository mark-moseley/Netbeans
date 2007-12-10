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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */

public class PersistenceUnitWizard implements WizardDescriptor.InstantiatingIterator {

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private Project project;
    private PersistenceUnitWizardDescriptor descriptor;
    private static final Logger LOG = Logger.getLogger(PersistenceUnitWizard.class.getName());
    
    public static PersistenceUnitWizard create() {
        return new PersistenceUnitWizard();
    }
    
    public String name() {
        return NbBundle.getMessage(PersistenceUnitWizard.class, "LBL_WizardTitle");
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public void previousPanel() {
        if (! hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    public void initialize(WizardDescriptor wizard) {
        project = Templates.getProject(wizard);
        descriptor = new PersistenceUnitWizardDescriptor(project);
        panels = new WizardDescriptor.Panel[] {descriptor};
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(PersistenceUnitWizard.class, "Templates/Persistence/PersistenceUnit"));
        Wizards.mergeSteps(wizard, panels, null);
    }
    
    public Set instantiate() throws java.io.IOException {
        PersistenceUnit punit = null;
        LOG.fine("Instantiating...");
            if (descriptor.isContainerManaged()) {
            LOG.fine("Creating a container managed PU");
            punit = new PersistenceUnit();
            if (descriptor.getDatasource() != null && !"".equals(descriptor.getDatasource())){
                if (descriptor.isJTA()) {
                    punit.setJtaDataSource(descriptor.getDatasource());
                } else {
                    punit.setNonJtaDataSource(descriptor.getDatasource());
                    punit.setTransactionType("RESOURCE_LOCAL");
                }
            }
            if (descriptor.isNonDefaultProviderEnabled()) {
                punit.setProvider(descriptor.getNonDefaultProvider());
            }
        } else {
            LOG.fine("Creating an application managed PU");
            punit = ProviderUtil.buildPersistenceUnit(descriptor.getPersistenceUnitName(),
                    descriptor.getSelectedProvider(), descriptor.getPersistenceConnection());
            punit.setTransactionType("RESOURCE_LOCAL");
            if (descriptor.getPersistenceLibrary() != null){
                Util.addLibraryToProject(project, descriptor.getPersistenceLibrary());
            }
        }
        punit.setName(descriptor.getPersistenceUnitName());
        ProviderUtil.setTableGeneration(punit, descriptor.getTableGeneration(), project);
        try{
            LOG.fine("Retrieving PUDataObject");
            PUDataObject pud = ProviderUtil.getPUDataObject(project);
            pud.addPersistenceUnit(punit);
            LOG.fine("Saving PUDataObject");
            pud.save();
            LOG.fine("Saved");
            return Collections.singleton(pud.getPrimaryFile());
        } catch (InvalidPersistenceXmlException ipx){
            // just log for debugging purposes, at this point the user has
            // already been warned about an invalid persistence.xml
            LOG.log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
            return Collections.emptySet();
        }
    }
    
}
