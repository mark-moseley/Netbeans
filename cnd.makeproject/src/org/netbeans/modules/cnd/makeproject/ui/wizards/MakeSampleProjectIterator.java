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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.ui.wizards.NewMakeProjectWizardIterator.Name;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public class MakeSampleProjectIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 4L;
    
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new MakeSampleProjectIterator();
    }
    
    public MakeSampleProjectIterator() {
    }
    
    public void addChangeListener(ChangeListener changeListener) {
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void initialize(TemplateWizard templateWizard) {
        int i = 0;
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replaceAll(" ", ""); // NOI18N
        }
        templateWizard.putProperty("name", name); // NOI18N
	String wizardTitle = getString("SAMPLE_PROJECT") + name; // NOI18N
	String wizardTitleACSD = getString("SAMPLE_PROJECT_ACSD"); // NOI18N
        
        panels = new WizardDescriptor.Panel[1];
        panels[i] = new PanelConfigureProject(name, -1, wizardTitle, wizardTitleACSD, false);
        String[] steps = new String[panels.length];
        for (i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent) panels[i].getComponent();
            steps[i] = ((Name) panels[i]).getName();
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
        };
    }
    
    public void uninitialize(org.openide.loaders.TemplateWizard templateWizard) {
        panels = null;
        index = -1;
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
    }
    
    public Set instantiate(TemplateWizard templateWizard) throws IOException {
        File projectLocation = (File) wiz.getProperty("projdir"); // NOI18N
        String name = (String) wiz.getProperty("name"); // NOI18N
        return MakeSampleProjectGenerator.createProjectFromTemplate(templateWizard.getTemplate().getPrimaryFile(), projectLocation, name);
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
	}
	return bundle.getString(s);
    }
    
}
