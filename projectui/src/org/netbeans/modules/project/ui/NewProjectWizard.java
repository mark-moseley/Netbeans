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

import java.text.MessageFormat;
import javax.swing.JComponent;

import org.openide.WizardDescriptor;
//import org.netbeans.spi.project.ui.templates.support.InstantiatingIterator;
import org.openide.filesystems.FileObject;

import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public final class NewProjectWizard extends TemplateWizard {

    private FileObject templatesFO;
    private MessageFormat format;

    public NewProjectWizard (FileObject fo) {
        this.templatesFO = fo;
        putProperty (TemplatesPanelGUI.TEMPLATES_FOLDER, templatesFO);
        format = new MessageFormat (NbBundle.getBundle (NewFileWizard.class).getString ("LBL_NewProjectWizard_MessageFormat"));
        //setTitleFormat( new MessageFormat( "{0}") );
    }
        
    public void updateState () {
        super.updateState ();
        String substitute = (String)getProperty ("NewProjectWizard_Title"); // NOI18N
        String title;
        if (substitute == null) {
            title = NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Title"); // NOI18N
        } else {
            Object[] args = new Object[] {
                    NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Subtitle"), // NOI18N
                    substitute};
            title = format.format (args);
        }
        super.setTitle (title);
    }
    
    public void setTitle (String ignore) {}
    
    protected WizardDescriptor.Panel<WizardDescriptor> createTemplateChooser() {
        WizardDescriptor.Panel<WizardDescriptor> panel = new TemplatesPanel();
        JComponent jc = (JComponent)panel.getComponent ();
        jc.setPreferredSize( new java.awt.Dimension (500, 340) );
        jc.setName (NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Name")); // NOI18N
        jc.getAccessibleContext ().setAccessibleName (NbBundle.getBundle (NewProjectWizard.class).getString ("ACSN_NewProjectWizard")); // NOI18N
        jc.getAccessibleContext ().setAccessibleDescription (NbBundle.getBundle (NewProjectWizard.class).getString ("ACSD_NewProjectWizard")); // NOI18N
        jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (0)); // NOI18N
        jc.putClientProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Name"), // NOI18N
                NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Dots")}); // NOI18N
                
        return panel;
    }          
    
}
