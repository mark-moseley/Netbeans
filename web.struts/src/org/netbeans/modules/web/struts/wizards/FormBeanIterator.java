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

package org.netbeans.modules.web.struts.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.struts.config.model.FormBeans;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.config.model.FormBean;
import org.netbeans.modules.web.struts.config.model.StrutsConfig;
import org.netbeans.modules.web.struts.editor.StrutsEditorUtilities;
import org.openide.cookies.OpenCookie;

/** A template wizard iterator for new struts action
 *
 * @author Petr Pisl
 * 
 */

public class FormBeanIterator implements TemplateWizard.Iterator {
    
    private int index;
    
    private transient WizardDescriptor.Panel[] panels;

    private transient boolean debug = false;
    
    public void initialize (TemplateWizard wizard) {
        if (debug) log ("initialize");                  //NOI18N
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }
        
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (debug) {
            log ("\tproject: " + project);              //NOI18N
            log ("\ttargetFolder: " + targetFolder);    //NOI18N
            log ("\tsourceGroups.length: " + sourceGroups.length);  //NOI18N
        }
        
        WizardDescriptor.Panel secondPanel = new FormBeanNewPanel(project, wizard);  
        
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0)
            javaPanel = Templates.createSimpleTargetChooser(project, sourceGroups, secondPanel);
        else
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel);

        
        panels = new WizardDescriptor.Panel[] {
            javaPanel
        };
        
        // Creating steps.
        Object prop = wizard.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) { 
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i)); // NOI18N 
	    jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
	}
    }
    
    public void uninitialize (TemplateWizard wizard) {
        panels = null;
    }
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
//how to get dynamic form bean properties
//String formBeanClassName = (String) wizard.getProperty(WizardProperties.FORMBEAN_CLASS); //NOI18N
        
        if (debug)
            log("instantiate"); //NOI18N
        
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );
        
        DataObject dTemplate = DataObject.find( template );   
        Map<String, Object> attributes = new HashMap<String,Object>();
        attributes.put("superclass", wizard.getProperty("formBeanSuperclass"));
        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wizard), attributes);        

        Project project = Templates.getProject( wizard );
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        String configFile = (String) wizard.getProperty(WizardProperties.FORMBEAN_CONFIG_FILE);

        if (wm != null && configFile != null && !"".equals(configFile)){ //NOI18N
            // write to a struts configuration file, only when it's inside wm. 
            dir = wm.getDocumentBase();

            String targetName = Templates.getTargetName(wizard);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String packageName = null;
            org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(wizard);
            for (int i = 0; i < groups.length && packageName == null; i++) {
                packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
                if (packageName!=null) break;
            }
            if (packageName!=null) packageName = packageName.replace('/','.');
            else packageName="";    //NOI18N
            String className=null;
            if (packageName.length()>0)
                className=packageName+"."+targetName;//NOI18N
            else
                className=targetName;

            
            FileObject fo = dir.getFileObject(configFile); //NOI18N
            if (fo != null){
                StrutsConfigDataObject configDO = (StrutsConfigDataObject)DataObject.find(fo);
                StrutsConfig config= configDO.getStrutsConfig();

                FormBean formBean = new FormBean();
                formBean.setAttributeValue("name", targetName); //NOI18N
                formBean.setAttributeValue("type", className); //NOI18N     
                if (config != null && config.getFormBeans()==null){
                    config.setFormBeans(new FormBeans());
                }

                config.getFormBeans().addFormBean(formBean);
                BaseDocument doc = (BaseDocument)configDO.getEditorSupport().getDocument();
                if (doc == null){
                    ((OpenCookie)configDO.getCookie(OpenCookie.class)).open();
                    doc = (BaseDocument)configDO.getEditorSupport().getDocument();
                }
                StrutsEditorUtilities.writeBean(doc, formBean, "form-bean", "form-beans"); //NOI18N
                configDO.getEditorSupport().saveDocument();
            }
        }
        return Collections.singleton(dobj);
    }
    
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    
    public boolean hasPrevious () {
        return index > 0;
    }
    
    public boolean hasNext () {
        return index < panels.length - 1;
    }
    
    public String name () {
        return NbBundle.getMessage (ActionIterator.class, "TITLE_x_of_y",   //NOI18N
            new Integer (index + 1), new Integer (panels.length));
    }
    
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    
    
    private void log (String message){
        System.out.println("ActionIterator:: \t" + message); //NOI18N
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    private void replaceInDocument(javax.swing.text.Document document, String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex){}
    }

}
