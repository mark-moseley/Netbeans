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

package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.core.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

/** A template wizard iterator for new servlets, filters and
 * listeners. 
 *
 * @author radim.kubacki@sun.com
 * @author ana.von.klopp@sun.com
 * @author milan.kuchtiak@sun.com
 */

public class ServletIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = -4147344271705652643L;

    private static final boolean debug = false; 

    private transient FileType fileType; 
    private transient Evaluator evaluator = null; 
    private transient DeployData deployData = null; 

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wizard;
    private transient ServletPanel servletPanel;
    private transient WizardDescriptor.Panel customPanel;

    private ServletIterator(FileType fileType) { 
	this.fileType = fileType; 
    } 

    public static ServletIterator createServletIterator() { 
	return new ServletIterator(FileType.SERVLET); 
    } 

    public static ServletIterator createFilterIterator() { 
	return new ServletIterator(FileType.FILTER); 
    }

    public void initialize (TemplateWizard wizard) {
        
        this.wizard = wizard;
        index = 0;

	if(fileType.equals(FileType.SERVLET) ||
	   fileType.equals(FileType.FILTER)) {
	    deployData = new ServletData(fileType);
	    evaluator = new TargetEvaluator(fileType, deployData); 
	}
            Project project = Templates.getProject( wizard );
            DataFolder targetFolder=null;
            try {
                targetFolder = wizard.getTargetFolder();
            } catch (IOException ex) {
                targetFolder = DataFolder.findFolder(project.getProjectDirectory());
            }
	    evaluator.setInitialFolder(targetFolder,project); 
        
	if(fileType == FileType.SERVLET) { 
	    panels = new WizardDescriptor.Panel[] {
                //wizard.targetChooser (),
                new FinishableProxyWizardPanel(
                        createPackageChooserPanel(wizard,null),
                        ServletIterator.class.getName() + "." + fileType),    // #114487
		ServletPanel.createServletPanel((TargetEvaluator)evaluator, wizard) 
	    };
	}
	else if(fileType == FileType.FILTER) {
            customPanel = new WrapperSelection(wizard);
	    panels = new WizardDescriptor.Panel[] {
                //wizard.targetChooser (),
                createPackageChooserPanel(wizard,customPanel),
		servletPanel=ServletPanel.createServletPanel((TargetEvaluator)evaluator, wizard), 
		ServletPanel.createFilterPanel((TargetEvaluator)evaluator, wizard)
	    };
	}
        
        // Creating steps.
        Object prop = wizard.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) { 
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty ("WizardPanel_contentSelectedIndex", // NOI18N
				  new Integer (i)); 
	    jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
	}	
    }

    private WizardDescriptor.Panel createPackageChooserPanel(TemplateWizard wizard, WizardDescriptor.Panel customPanel) {
        Project project = Templates.getProject(wizard);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        if (customPanel==null) {
            if (sourceGroups.length == 0)
                return Templates.createSimpleTargetChooser(project, sourceGroups);
            else
                return JavaTemplates.createPackageChooser(project, sourceGroups);
        } else {
            if (sourceGroups.length == 0)
                return Templates.createSimpleTargetChooser(project, sourceGroups, customPanel);
            else
                return JavaTemplates.createPackageChooser(project, sourceGroups, customPanel);
        }
    }
    
    public Set instantiate(TemplateWizard wizard) throws IOException {

	if(debug) log("::instantiate()"); 
	// Create the target folder. The next piece is independent of
	// the type of file we create, and it should be moved to the
	// evaluator class instead. The exact same process
	// should be used when checking if the directory is valid from
	// the wizard itself. 

	// ------------------------- FROM HERE -------------------------
        
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );

        FileObject template = Templates.getTemplate( wizard );
        if (FileType.FILTER.equals(fileType) && ((WrapperSelection)customPanel).isWrapper()) {
            template = Templates.getTemplate( wizard );
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("AdvancedFilter","java"); //NOI18N
        }
        
        HashMap <String, String> templateParameters =  new HashMap<String, String>();
        templateParameters.put("servletEditorFold", NbBundle.getMessage(ServletIterator.class, "MSG_ServletEditorFold")); //NOI18N
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard ), templateParameters  );

	if(debug) log("\topened file"); //NOI18N

	// If the user does not want to add the file to the
	// deployment descriptor, return
	if(!deployData.makeEntry()) { 
	    return Collections.singleton(dobj);
	} 

	TargetEvaluator te = (TargetEvaluator)evaluator; 

	if(debug) log("\tcreate dd entries"); //NOI18N
	
        // needed to be able to finish ServletWizard from the second panel
        if (deployData.getClassName().length()==0) {
            String targetName = wizard.getTargetName();
            FileObject targetFolder = Templates.getTargetFolder(wizard);
            String packageName = null;
            Project project = Templates.getProject( wizard );
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < groups.length && packageName == null; i++) {
                if (WebModule.getWebModule (groups [i].getRootFolder ()) != null) {
                    packageName = FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
                }
            }
            if (packageName!=null)
                packageName = packageName.replace('/','.');
            else packageName="";
            // compute (and set) the servlet-class 
            deployData.setClassName(packageName.length()==0?targetName:packageName+"."+targetName);
            // compute (and set) the servlet-name and url-pattern 
            String servletName = ((ServletData)deployData).createDDServletName(targetName);
            ((ServletData)deployData).createDDServletMapping(servletName);
        } 
        deployData.createDDEntries();

	if(debug) log("\tURI param"); //NOI18N
        return Collections.singleton(dobj);
    } 

    private void log(String s) { 
	System.out.println("ServletIterator" + s); 
    } 
    

    public void uninitialize (TemplateWizard wizard) {
        this.wizard = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    
    public String name () {
        return NbBundle.getMessage (ServletIterator.class, "TITLE_x_of_y",
        new Integer (index + 1), new Integer (panels.length));
    }
    
    // If the user has elected to place the file in a regular
    // directory (not a web module) then we don't show the DD info
    // panel. 
    public boolean hasNext () {
	if(debug) log("::hasNext()"); //NOI18N
	if(debug) log("\tindex is " + index); //NOI18N
	return index < panels.length - 1 && deployData.hasDD();
    }
    
    public boolean hasPrevious () {
        return index > 0;
    }
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // PENDING - Ann suggests updating the available panels based on
    // changes. Here is what should happen: 
    // 1. If target is directory, disable DD panels
    // 2. If target is web module but the user does not want to make a
    //    DD entry, disable second DD panel for Filters. 

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
    /*
    private transient Set listeners = new HashSet (1); // Set<ChangeListener>
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
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        listeners = new HashSet (1);
    }
     */

}
