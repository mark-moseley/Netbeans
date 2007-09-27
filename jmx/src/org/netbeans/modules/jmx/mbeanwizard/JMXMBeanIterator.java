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
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard;

import java.awt.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.FinishableDelegatedWizardPanel;
import org.netbeans.modules.jmx.WizardPanelWithoutReadSettings;
import org.netbeans.modules.jmx.mbeanwizard.generator.GeneratorControler;

/**
 *
 * Main Wizard class : manage the panel navigation and the code generation.
 *
 */
public class JMXMBeanIterator implements TemplateWizard.Iterator {
    private static final long serialVersionUID = 1L;
    
    /** private variables */
    private transient TemplateWizard wiz;
    
    private transient StandardMBeanPanel.StandardMBeanWizardPanel mbeanTemplatePanel;
    private transient MBeanOptionsPanel.MBeanOptionsWizardPanel mbeanOpPanel;
    //private transient MBeanAttrAndMethodPanel.AttributesWizardPanel attributePanel;
    private transient MBeanAttributePanel.AttributesWizardPanel attributePanel;
    private transient MBeanWrapperAttributePanel.WrapperAttributesWizardPanel wAttributePanel;
    private transient MBeanWrapperOperationPanel.WrapperOperationsWizardPanel wOperationPanel;
    private transient MBeanOperationPanel.OperationWizardPanel operationPanel;
    private transient MBeanNotificationPanel.NotificationsWizardPanel notificationPanel;
    private transient FinishableDelegatedWizardPanel mbeanPanel;
    private transient WizardDescriptor.Panel currentPanel;
    private String[] steps;
    private transient ResourceBundle bundle;
    private Project lastSelectedProject = null;
    
    //****************************************************************
    // Called with the menu new->file->Standard MBean
    //****************************************************************
    
    /**
     * Method called with the menu new->file->Standard MBean which provides
     * an instance of an iterator
     * @return JMXMBeanIterator an iterator
     */
    public static JMXMBeanIterator createMBeanIterator() {
        return new JMXMBeanIterator();
    }
    
    //****************************************************************
    // default constructor :
    //****************************************************************
    /**
     * The default constructor
     */
    public JMXMBeanIterator() {
        bundle = NbBundle.getBundle(JMXMBeanIterator.class);
    }
    
    //*********************************************************************
    // Called to really start the wizard in
    // case of a direct call from the menu
    //*********************************************************************
    
    /**
     * Initializing method, called to really start the wizard
     * @param wiz a TemplateWizard
     */
    public void initialize(TemplateWizard wiz) {
        // kludge to work around a netbeans bug :
        
        // create step description array
        //String[] steps = initializeSteps(wiz);
        
        this.wiz = wiz;
        
        steps = new String[6];
        steps[0] = new String("Choose File Type"); // NOI18N // should be added by netbeans
        
        
        
        steps[1] = bundle.getString("LBL_Standard_Panel");// NOI18N
        steps[2] = bundle.getString("LBL_Standard_Options_Panel");// NOI18N
        
        //steps[3] = bundle.getString("LBL_Wrapper_Panel");// NOI18N
        
        steps[3] = bundle.getString("LBL_Attribute_Panel");// NOI18N
        steps[4] = bundle.getString("LBL_Operation_Panel");// NOI18N
        steps[5] = bundle.getString("LBL_Notification_Panel");// NOI18N
        
        // end of work around
        
        // Don't set the generated project as the new Netbeans "main project" !!
        //
        // cf ../projects/projectui/src/org/netbeans/modules/project/ui/actions/NewProject.java
        //
        wiz.putProperty("setAsMain", false); // NOI18N
        
        try {
            
            // setup project location for the current project
            WizardHelpers.setProjectValues(wiz);
            // initialize each panel
            initializeComponents(steps, 0);
            
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("initialize", ex);// NOI18N
        }
    }
    
    //*********************************************************************
    // WizardIntegration method :
    //
    // Called when integrating this wizard within a higher level wizard.
    //
    //*********************************************************************
    
    /**
     * Method which defines the different steps of our wizard;
     * Called when integrating this wizard within a higher level
     * wizard
     * @param wiz a WizardDescriptor
     * @return <CODE>String[]</CODE> step names
     */
    
    public String[] initializeSteps(WizardDescriptor wiz) {
        this.wiz = (TemplateWizard) wiz;
        
        steps = new String[5];
        steps[0] = bundle.getString("LBL_Standard_Panel");// NOI18N
        steps[1] = bundle.getString("LBL_Standard_Options_Panel");// NOI18N
        steps[2] = bundle.getString("LBL_Attribute_Panel");// NOI18N
        steps[3] = bundle.getString("LBL_Operation_Panel");// NOI18N
        steps[4] = bundle.getString("LBL_Notification_Panel");// NOI18N
        
        return steps;
    }
    
    //*********************************************************************
    // WizardIntegration method :
    //
    // Called when integrating this wizard within a higher level wizard.
    //
    // Parameters :
    //
    // Steps       : Panels list to use
    // panelOffset : number of the first panel of this wizard
    //
    //*********************************************************************
    
    /**
     * Method which initialises the different components
     * Called when integrating this wizard within a higher level wizard
     * @param steps Panels list to use
     * @param panelOffset number of the first panel of this wizard
     */
    
    public void initializeComponents(String[] steps, int panelOffset) {
        mbeanTemplatePanel = new StandardMBeanPanel.StandardMBeanWizardPanel();
        initializeComponent(steps,panelOffset + 0,
                (JComponent)mbeanTemplatePanel.getComponent());
        
        Project project = Templates.getProject(wiz);
        SourceGroup[] mbeanSrcGroups =
                WizardHelpers.getSourceGroups(project);
        WizardDescriptor.Panel delegateMBeanPanel =
                JavaTemplates.createPackageChooser(project,
                mbeanSrcGroups,
                mbeanTemplatePanel);
        mbeanPanel = new WizardPanelWithoutReadSettings(
                delegateMBeanPanel,mbeanTemplatePanel);
        mbeanPanel.getComponent().setName(
                bundle.getString("LBL_Standard_Panel"));// NOI18N
        initializeComponent(steps,panelOffset + 0,
                (JComponent)mbeanPanel.getComponent());
        ((StandardMBeanPanel.StandardMBeanWizardPanel) mbeanTemplatePanel).
                setListenerEnabled(delegateMBeanPanel,mbeanTemplatePanel,wiz);
        mbeanPanel.readAllSettings(wiz);
        
        mbeanOpPanel = new MBeanOptionsPanel.MBeanOptionsWizardPanel();
        initializeComponent(steps,panelOffset + 1,
                (JComponent)mbeanOpPanel.getComponent());
        
        //wrapperPanel = new MBeanWrapperPanel.WrapperAttributesWizardPanel();
        //initializeComponent(steps,panelOffset + 2,
        //        (JComponent)wrapperPanel.getComponent());
        
        //attributePanel = new MBeanAttrAndMethodPanel.AttributesWizardPanel();
        //initializeComponent(steps,panelOffset + 3,
        //        (JComponent)attributePanel.getComponent());
        
        JavaSource isExistingRessource = (JavaSource)wiz.getProperty(
                (WizardConstants.PROP_MBEAN_EXISTING_CLASS));
        
        attributePanel = new MBeanAttributePanel.AttributesWizardPanel();
        initializeComponent(steps,panelOffset + 2,
                (JComponent)attributePanel.getComponent());
        
        wAttributePanel = new MBeanWrapperAttributePanel.WrapperAttributesWizardPanel();
        initializeComponent(steps,panelOffset + 2,
                (JComponent)wAttributePanel.getComponent());
        
        operationPanel = new MBeanOperationPanel.OperationWizardPanel();
        initializeComponent(steps,panelOffset + 3,
                (JComponent)operationPanel.getComponent());
        
        wOperationPanel = new MBeanWrapperOperationPanel.WrapperOperationsWizardPanel();
        initializeComponent(steps,panelOffset + 3,
                (JComponent)wOperationPanel.getComponent());
        
        notificationPanel =
                new MBeanNotificationPanel.NotificationsWizardPanel();
        initializeComponent(steps,panelOffset + 4,
                (JComponent)notificationPanel.getComponent());
        
        currentPanel = mbeanPanel;
    }
    
    private void initializeComponent(String[] steps, int panelOffset,JComponent jc) {
        jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        jc.putClientProperty("WizardPanel_contentSelectedIndex", panelOffset);// NOI18N
    }
    
    /**
     * Method which releases the wizard
     * @param wiz a TemplateWizard
     */
    public void uninitialize(TemplateWizard wiz) {
        this.wiz = null;
    }
    
    //*********************************************************************
    // real code / file generation
    //*********************************************************************
    
    /**
     * Method which recalls the stored data and generates the mbean
     * @return <CODE>Set</CODE> set of generated files to open
     * @param wizard the wizard which contains all the data
     * @throws java.io.IOException <CODE>IOException</CODE>
     */
    public java.util.Set/*<FileObject>*/ instantiate(TemplateWizard wizard)
    throws java.io.IOException {
        mbeanPanel.storeAllSettings(wizard);
        
        // mbean generation
        try {
            
            return GeneratorControler.generate(wizard);
            
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("MBean generation ", ex);// NOI18N
            return Collections.EMPTY_SET;
        }
    }
    
    /**
     * Method returning the name of a component contained in the current panel
     * @return name the name of the component
     */
    public String name() {
        Component c = currentPanel.getComponent();
        
        if (c != null)
            return c.getName();
        
        return null;
    }
    
    /**
     * Method returning the current panel
     * @return currentPanel the current panel
     */
    public org.openide.WizardDescriptor.Panel current() {
        return currentPanel;
    }
    
    /**
     * Method returning if the current panel has a next panel or not
     * Enables the next button
     * @return next true if the current panel has a next one
     */
    public boolean hasNext() {
        
        if (currentPanel == notificationPanel) {
            return false;
        } else return true;
        
    }
    
    /**
     * Method returning if the current panel has a previous panel or not
     * Enables the back button
     * @return next true if the current panel has a previous one
     */
    public boolean hasPrevious() {
        if (currentPanel != mbeanPanel)
            return true;
        else
            return false;
    }
    
    /**
     * Method reaffecting the current panel variable to the next panel
     */
    public void nextPanel() {
        
        JavaSource isExistingRessource = (JavaSource)wiz.getProperty(
                (WizardConstants.PROP_MBEAN_EXISTING_CLASS));
        
        if (currentPanel == mbeanPanel)
            currentPanel = mbeanOpPanel;
        else {
            if (currentPanel == mbeanOpPanel) {
                if (isExistingRessource == null)
                    currentPanel = attributePanel;
                else
                    currentPanel = wAttributePanel;
            } else {
                if (currentPanel == attributePanel)
                    currentPanel = operationPanel;
                else if (currentPanel == wAttributePanel) {
                    currentPanel = wOperationPanel;
                } else if ((currentPanel == operationPanel) || (currentPanel == wOperationPanel)) {
                    currentPanel = notificationPanel;
                }
            }
        }
    }
    
    
    /**
     * Method reaffecting the current panel variable to the previous panel
     */
    public void previousPanel() {
        JavaSource isExistingRessource = (JavaSource)wiz.getProperty(
                (WizardConstants.PROP_MBEAN_EXISTING_CLASS));
        
        if (currentPanel == notificationPanel) {
            if (isExistingRessource != null)
                currentPanel = wOperationPanel;
            else
                currentPanel = operationPanel;
        } else {
            if (currentPanel == operationPanel)
                currentPanel = attributePanel;
            else if (currentPanel == wOperationPanel)
                currentPanel = wAttributePanel;
            else {
                if ((currentPanel == attributePanel) || (currentPanel == wAttributePanel))
                    currentPanel = mbeanOpPanel;
                else {
                    if (currentPanel == mbeanOpPanel)
                        currentPanel = mbeanPanel;
                }
            }
        }
    }
    
    private transient Set listeners = new HashSet(1); // Set<ChangeListener>
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /**
     * Fire a change event.
     */
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
}
