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

package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.api.webmodule.WebModule;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  Milan Kuchtiak
 */
public class TagHandlerSelection implements WizardDescriptor.Panel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private transient TagHandlerPanel component;
    private transient TemplateWizard wizard;
    private transient String j2eeVersion;
    
    /** Create the wizard panel descriptor. */
    public TagHandlerSelection(TemplateWizard wizard) {
        this.wizard=wizard;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        Project project = Templates.getProject( wizard );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        WebModule wm=null;
        j2eeVersion = WebModule.J2EE_14_LEVEL;
        if (groups!=null && groups.length>0) {
            wm = WebModule.getWebModule(groups[0].getRootFolder());;
        }
        if (wm!=null) {
            j2eeVersion=wm.getJ2eePlatformVersion();
        }
        if (component == null) {
            component = new TagHandlerPanel(this,j2eeVersion);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx(TagHandlerSelection.class); //NOI18N
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        if (!isBodyTagSupport() && WebModule.J2EE_13_LEVEL.equals(j2eeVersion)) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                org.openide.util.NbBundle.getMessage(TagHandlerSelection.class, "NOTE_simpleTag"));
        } else {
            wizard.putProperty("WizardPanel_errorMessage", ""); // NOI18N
        }
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition ();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent ();
        // and uncomment the complicated stuff below.
    }
 
    //public final void addChangeListener(ChangeListener l) {}
    //public final void removeChangeListener(ChangeListener l) {}

    private final Set listeners = new HashSet (1); // Set<ChangeListener>
    public final void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    public final void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    protected final void fireChangeEvent () {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener) it.next ()).stateChanged (ev);
        }
    }

    
    // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
    }
    public void storeSettings(Object settings) {
        WizardDescriptor w = (WizardDescriptor)settings;
        if (isBodyTagSupport())
            w.putProperty("BODY_SUPPORT",Boolean.TRUE);//NOI18N
        else 
            w.putProperty("BODY_SUPPORT",Boolean.FALSE);//NOI18N
    }
    
    boolean isBodyTagSupport() {return component.isBodyTagSupport();}
    
}
