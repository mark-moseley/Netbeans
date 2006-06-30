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
/*
 * ConnPoolWizard.java
 *
 * Created on September 30, 2003, 10:05 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

/**
 *
 * @author  nityad
 */

import java.awt.Component;
import java.util.Set;
import javax.swing.JComponent;
import java.io.InputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

public final class ConnPoolWizard implements WizardDescriptor.InstantiatingIterator, WizardConstants{
    
    private static Project project;
           
    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/CPWizard.xml";  //NOI18N
        
    /** An array of all wizard panels */
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private transient String[] steps;
    private transient int index;
    private ResourceConfigHelper helper;
    private Wizard wizardInfo;
        
    /** Creates a new instance of ConnPoolWizard */
    public static ConnPoolWizard create () {
        return new ConnPoolWizard ();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new CPVendorPanel(this.helper, this.wizardInfo),
            new CPPropertiesPanelPanel(this.helper, this.wizardInfo),
            new CommonAttributePanel(this.helper, this.wizardInfo,  new String[] {"pool-setting", "pool-setting-2", "pool-setting-3"}), //NOI18N
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            __FirstStepChoose,
            NbBundle.getMessage(ConnPoolWizard.class, "TITLE_ConnPoolWizardPanel_dbConn"), //NOI18N
            NbBundle.getMessage(ConnPoolWizard.class, "TITLE_ConnPoolWizardPanel_properties"), //NOI18N
            NbBundle.getMessage(ConnPoolWizard.class, "TITLE_ConnPoolWizardPanel_optionalProps") //NOI18N
        };
    }
    
    public Set instantiate(){
        try{
            ResourceUtils.saveConnPoolDatatoXml(this.helper.getData());
        }catch (Exception ex){
            //System.out.println("Error in instantiate ");
        }
        return java.util.Collections.EMPTY_SET;
    }
    
    public void initialize(WizardDescriptor wiz){
        this.wizardInfo = getWizardInfo();
        this.helper = new ResourceConfigHelperHolder().getConnPoolHelper();
        
        this.wiz = wiz;
        wiz.putProperty("NewFileWizard_Title", NbBundle.getMessage(ConnPoolWizard.class, "Templates/SunResources/JDBC_Connection_Pool")); //NOI18N
        index = 0;
                
        project = Templates.getProject(wiz);
        
        panels = createPanels();
        // Make sure list of steps is accurate.
        steps = createSteps();
        
        try{
            FileObject pkgLocation = project.getProjectDirectory();
            if (pkgLocation != null) {
                this.helper.getData().setTargetFileObject(pkgLocation);
            }
        }catch (Exception ex){
            //Unable to get project location
        }
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }        
    }
    
    public void uninitialize(WizardDescriptor wiz){
        this.wiz = null;
        panels = null;
    }
    
    public Wizard getWizardInfo(){
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(DATAFILE);
            this.wizardInfo = Wizard.createGraph(in);
        }catch(Exception ex){
            //System.out.println("Unable to get Wiz Info");
        }
        return this.wizardInfo;
    }
    
    public String name(){
        return NbBundle.getMessage(ConnPoolWizard.class, "Templates/SunResources/JDBC_Connection_Pool"); //NOI18N
    }
    
    public boolean hasNext(){
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious(){
        return index > 0;
    }
    
    public synchronized void nextPanel(){
        if (index + 1 == panels.length)
            throw new java.util.NoSuchElementException();
        
        if (index == 0) {
            ((CPPropertiesPanelPanel) panels[1]).refreshFields();
        }else if (index == 1){
            ((CommonAttributePanel) panels[2]).setPropInitialFocus();
        }
        index ++;
    }
    
    public synchronized void previousPanel(){
        if (index == 0)
            throw new java.util.NoSuchElementException();
        
        index--;
    }
    
    public WizardDescriptor.Panel current(){
        return (WizardDescriptor.Panel)panels[index];
    }
    
    public void addChangeListener(ChangeListener l){
    }
    
    public void removeChangeListener(ChangeListener l){
    }

     
    public void setResourceConfigHelper(ResourceConfigHelper helper){
        this.helper = helper;
    }
    
    public ResourceConfigHelper getResourceConfigHelper(){
        return this.helper;
    }
    
}
