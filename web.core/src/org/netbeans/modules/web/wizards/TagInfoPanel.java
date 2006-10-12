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

package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  Milan Kuchtiak
 */
public class TagInfoPanel implements WizardDescriptor.Panel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private TagHandlerPanelGUI component;
    private transient TemplateWizard wizard;
    private transient Project proj;
    private transient SourceGroup[] sourceGroups;
    private String className;
    
    /** Create the wizard panel descriptor. */
    public TagInfoPanel(TemplateWizard wizard, Project proj, SourceGroup[] sourceGroups) {
        this.wizard=wizard;
        this.proj=proj;
        this.sourceGroups=sourceGroups;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new TagHandlerPanelGUI(wizard,this,proj,sourceGroups);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        //return new HelpCtx(TagInfoPanel.class); //NOI18N
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        if (writeToTLD() && getTLDFile()==null) {
            wizard.putProperty ("WizardPanel_errorMessage", org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "MSG_noTldSelected")); // NOI18N
            return false;
        } else if (!isValidTagName(getTagName())) {
            wizard.putProperty ("WizardPanel_errorMessage", org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "TXT_wrongTagName",getTagName())); // NOI18N
            return false;        
        } else if (tagNameExists(getTagName())) {
            wizard.putProperty ("WizardPanel_errorMessage", org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "TXT_tagNameExists",getTagName())); // NOI18N
            return false;        
        } else {
            wizard.putProperty ("WizardPanel_errorMessage", ""); // NOI18N
            return true;
        }
        
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
        TemplateWizard w = (TemplateWizard)settings;
        //Project project = Templates.getProject(w);
        String targetName = w.getTargetName();
        org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(w);
        Project project = Templates.getProject( w );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
        }
        if (packageName == null)
            packageName = ""; //NOI18N
        packageName = packageName.replace('/','.');
        
        if (targetName!=null) {
            if (packageName.length()>0)
                className=packageName+"."+targetName;//NOI18N
            else
                className=targetName;
            component.setClassName(className);
            if (component.getTagName().length()==0)
                component.setTagName(targetName);
        }
        Boolean bodySupport = (Boolean)w.getProperty("BODY_SUPPORT");//NOI18N
        if (bodySupport!=null && bodySupport.booleanValue()) 
            component.setBodySupport(true);
        else component.setBodySupport(false);
    }
    public void storeSettings(Object settings) {
    }
    
    public String getClassName() {
        return className;
    }
    public String getTagName() {
        return component.getTagName();
    }
    public FileObject getTLDFile() {
        return component.getTLDFile();
    }
    public boolean isEmpty() {
        return component.isEmpty();
    }
    public boolean isScriptless() {
        return component.isScriptless();
    }
    public boolean isTegdependent() {
        return component.isTegdependent();
    }
    public boolean writeToTLD() {
        return component.writeToTLD();
    }
    public Object[][] getAttributes() {
        return component.getAttributes();
    }
    
    private boolean isValidTagName(String name) {
        return org.apache.xerces.util.XMLChar.isValidNCName(name);
    }
    
    private boolean tagNameExists(String name) {
        java.util.Set tagValues = component.getTagValues();
        if (tagValues!=null && tagValues.contains(name)) return true; 
        else return false;
    }
}
