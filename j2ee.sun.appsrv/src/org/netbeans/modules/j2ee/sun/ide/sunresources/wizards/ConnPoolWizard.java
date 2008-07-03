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
import javax.swing.event.ChangeListener;

import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.ErrorManager;

public final class ConnPoolWizard implements WizardDescriptor.InstantiatingIterator, WizardConstants{
    
    private static Project project;
           
    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/CPWizard.xml";  //NOI18N
        
    /** An array of all wizard panels */
    private transient WizardDescriptor.Panel[] panels;
//    private transient WizardDescriptor wiz;
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
            NbBundle.getMessage(ConnPoolWizard.class, __FirstStepChoose),
            NbBundle.getMessage(ConnPoolWizard.class, "TITLE_ConnPoolWizardPanel_dbConn"), //NOI18N
            NbBundle.getMessage(ConnPoolWizard.class, "TITLE_ConnPoolWizardPanel_properties"), //NOI18N
            NbBundle.getMessage(ConnPoolWizard.class, "TITLE_ConnPoolWizardPanel_optionalProps") //NOI18N
        };
    }
    
    public Set instantiate(){
        try{
            ResourceUtils.saveConnPoolDatatoXml(this.helper.getData());
        }catch (Exception ex){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        ex);
        }
        return java.util.Collections.EMPTY_SET;
    }
    
    public void initialize(WizardDescriptor wiz){
        this.wizardInfo = getWizardInfo();
        this.helper = new ResourceConfigHelperHolder().getConnPoolHelper();
        
        //this.wiz = wiz;
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
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        ex);
        }
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }        
    }
    
    public void uninitialize(WizardDescriptor wiz){
        //this.wiz = null;
        panels = null;
    }
    
    public Wizard getWizardInfo(){
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(DATAFILE);
            this.wizardInfo = Wizard.createGraph(in);
        }catch(Exception ex){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        ex);
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
        if (index + 1 == panels.length) {
            throw new java.util.NoSuchElementException();
        }
        
        if (index == 0) {
            ((CPPropertiesPanelPanel) panels[1]).refreshFields();
        }else if (index == 1){
            ((CommonAttributePanel) panels[2]).setPropInitialFocus();
        }
        index ++;
    }
    
    public synchronized void previousPanel(){
        if (index == 0) {
            throw new java.util.NoSuchElementException();
        }
        
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
