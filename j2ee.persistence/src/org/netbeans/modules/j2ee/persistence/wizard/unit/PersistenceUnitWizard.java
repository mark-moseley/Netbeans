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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
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
        Util.mergeSteps(wizard, panels, null);
    }
    
    public Set instantiate() throws java.io.IOException {
        PersistenceUnit punit = null;
        if (descriptor.isContainerManaged()) {
            punit = new PersistenceUnit();
            if (descriptor.isJTA()) {
                punit.setJtaDataSource(descriptor.getDatasource());
            } else {
                punit.setNonJtaDataSource(descriptor.getDatasource());
                punit.setTransactionType("RESOURCE_LOCAL");
            }
            if (descriptor.isNonDefaultProviderEnabled()) {
                punit.setProvider(descriptor.getNonDefaultProvider());
            }
        } else {
            punit = ProviderUtil.buildPersistenceUnit(descriptor.getPersistenceUnitName(),
                    descriptor.getSelectedProvider(), descriptor.getPersistenceConnection());
            punit.setTransactionType("RESOURCE_LOCAL");
            if (descriptor.getPersistenceLibrary() != null){
                Util.addLibraryToProject(project, descriptor.getPersistenceLibrary());
            }
        }
        punit.setName(descriptor.getPersistenceUnitName());
        ProviderUtil.setTableGeneration(punit, descriptor.getTableGeneration(), project);
        PUDataObject pud = ProviderUtil.getPUDataObject(project);
        pud.addPersistenceUnit(punit);
        pud.save();
        return Collections.singleton(pud.getPrimaryFile());
    }
    
    /**
     * Constructs an application managed persistence unit.
     */
    private PersistenceUnit constructApplicationManagedPU(){
        Provider provider = null;
        if (descriptor.getPersistenceLibrary() == null){
            // probably jboss (see #76738)
            provider = ProviderUtil.getProvider(descriptor.getNonDefaultProvider(), project);
        } else {
            provider = ProviderUtil.getProvider(descriptor.getPersistenceLibrary());
        }
        return ProviderUtil.buildPersistenceUnit(descriptor.getPersistenceUnitName(), provider, descriptor.getPersistenceConnection());
        
    }
    
}
