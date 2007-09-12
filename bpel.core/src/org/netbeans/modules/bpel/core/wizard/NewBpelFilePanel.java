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



package org.netbeans.modules.bpel.core.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

/**
 *
 * from nb webservice module
 */
final class NewBpelFilePanel implements WizardDescriptor.Panel<WizardDescriptor> {

    NewBpelFilePanel(Project project, SourceGroup[] folders) {
        this.folders = folders;
        this.project = project;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    }
    
    void setNameTF(JTextField nameTF) {
        gui.attachFileNameListener(nameTF);
    }

    public Component getComponent() {
        if (gui == null) {
            gui=new BpelOptionsPanel(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(NewBpelFilePanel.class);
    }

    public boolean isValid() {
        boolean valid = true;
        
        if(gui.getNamespaceTextField().contains(" "))
            valid = false;
        
        return valid;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( WizardDescriptor settings ) {
        templateWizard = (TemplateWizard)settings;
    }

    public void storeSettings(WizardDescriptor settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( settings.getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( settings.getValue() ) ) {
            return;
        }
        
        settings.putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    String getNS() {
        return gui.getNamespaceTextField();
    }
    
    String getWsName() {
        return gui.getWsName();
    }
    
    Project getProject() {
        return project;
    }

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private BpelOptionsPanel gui;

    private Project project;
    private SourceGroup[] folders;
    private TemplateWizard templateWizard;
   
}
