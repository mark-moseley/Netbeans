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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk
 */
final class TemplateChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private static String lastCategory = null;
    private static String lastTemplate = null;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private TemplateChooserPanelGUI gui;

    private Project project;
    // private String[] recommendedTypes;

    TemplateChooserPanel( Project p /*, String recommendedTypes[] */ ) {
        this.project = p;
        /* this.recommendedTypes = recommendedTypes; */
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new TemplateChooserPanelGUI();
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        // XXX
        return null;
    }

    public boolean isValid() {
        return gui != null && gui.getTemplate() != null;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void readSettings(WizardDescriptor settings) {
        TemplateChooserPanelGUI panel = (TemplateChooserPanelGUI) this.getComponent();
        panel.readValues( project, lastCategory, lastTemplate );
        settings.putProperty("WizardPanel_contentSelectedIndex", 0); // NOI18N
        settings.putProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (TemplateChooserPanel.class).getString ("LBL_TemplatesPanel_Name"), // NOI18N
                NbBundle.getBundle (TemplateChooserPanel.class).getString ("LBL_TemplatesPanel_Dots")}); // NOI18N
        // bugfix #44400: wizard title always changes
        settings.putProperty("NewFileWizard_Title", null); // NOI18N
    }

    public void storeSettings(WizardDescriptor wd) {
        Object value = wd.getValue();
        
        if ( NotifyDescriptor.CANCEL_OPTION != value &&
             NotifyDescriptor.CLOSED_OPTION != value ) {        
            try { 

                Project newProject = gui.getProject ();
                if (!project.equals (newProject)) {
                    project = newProject;
                    wd.putProperty( ProjectChooserFactory.WIZARD_KEY_PROJECT, newProject );
                }
                
                if (gui.getTemplate () == null) {
                    return ;
                }
                
                if (wd instanceof TemplateWizard) {
                    ((TemplateWizard)wd).setTemplate( DataObject.find( gui.getTemplate() ) );
                } else {
                    wd.putProperty( ProjectChooserFactory.WIZARD_KEY_TEMPLATE, gui.getTemplate () );
                }

                lastCategory = gui.getCategoryName();
                lastTemplate = gui.getTemplateName();
            }
            catch( DataObjectNotFoundException e ) {
                ErrorManager.getDefault().notify (e);
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        /*
        FileObject template = gui.getTemplate();
        p = gui.getProject();
        if (template != null) {
            setDelegate(findTemplateWizardIterator(template, p));
        } else {
            setDelegate(null);
        }
         */
        changeSupport.fireChange();
        
    }

}    
    