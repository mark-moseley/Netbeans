/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.websvc.api.webservices.WebServicesClientSupport;
import org.netbeans.modules.websvc.core.Utilities;

/** Wizard for adding web service clients to an application
 */
public class WebServiceClientWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index = 0;
    private WizardDescriptor.Panel [] panels;
	
    private WizardDescriptor wiz;
	// !PW FIXME How to handle freeform???
    private Project project;
    
	/** Entry point specified in layer
	 */
    public static WebServiceClientWizardIterator create() {
	 	return new WebServiceClientWizardIterator();
    }
	
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new WebServiceClientWizardDescriptor()
        };
    }

	public void initialize(WizardDescriptor wizard) {
		wiz = wizard;
		project = Templates.getProject(wiz);

		index = 0;
		panels = createPanels();
		
		Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
		String[] beforeSteps = null;
		if (prop != null && prop instanceof String[]) {
			beforeSteps = (String[])prop;
		}
		String[] steps = Utilities.createSteps (beforeSteps, panels);

		for (int i = 0; i < panels.length; i++) {
			Component c = panels[i].getComponent();
			if (steps[i] == null) {
				// Default step name to component name of panel.
				// Mainly useful for getting the name of the target
				// chooser to appear in the list of steps.
				steps[i] = c.getName();
			}

			assert c instanceof JComponent;
			JComponent jc = (JComponent)c;
			// Step #.
			jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
			// Step name (actually the whole list for reference).
			jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
		}
	}
	
	public void uninitialize(WizardDescriptor wizard) {
        wiz.putProperty(WizardProperties.WSDL_FILE_PATH,null);
        wiz.putProperty(WizardProperties.WSDL_PACKAGE_NAME,null);
		wiz = null;
		panels = null;
	}
	
	public Set/*FileObject*/ instantiate() throws IOException {
		
		Set result = new HashSet();
		
		// Steps:
		// 1. invoke wizard to select which service to add a reference to.
		//    How to interpret node input set --
		//    + empty: wizard forces project selection, then service selection
		//    + client node: determine project and start on service page
		//    + wsdl node: would select project, but not service.  would also
		//      have to verify that WSDL is fully formed.
		
		WebServicesClientSupport clientSupport = null;
		project = Templates.getProject(wiz);
		
		// !PW Get client support from project (from first page of wizard)
		if(project != null) {
			clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
		}
		
		if(clientSupport == null) {
			// notify no client support
//			String mes = MessageFormat.format (
//				NbBundle.getMessage (WebServiceClientWizardIterator.class, "ERR_WebServiceClientSupportNotFound"),
//				new Object [] {"Servlet Listener"}); //NOI18N
			String mes = "No web service client support found in selected project";
			NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
			DialogDisplayer.getDefault().notify(desc);
			return result;
		}
		
		String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
		String packageName = (String) wiz.getProperty(WizardProperties.WSDL_PACKAGE_NAME);
		
		File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
		FileObject sourceWsdlFile = FileUtil.toFileObject(normalizedWsdlFilePath);
		if(sourceWsdlFile == null) {
			String mes = "Cannot get FileObject for wsdl file: '" + normalizedWsdlFilePath + "'";
			NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
			DialogDisplayer.getDefault().notify(desc);
			return result;
		}

		// 2. add the service to the project.
		ClientBuilder builder = new ClientBuilder(project, clientSupport, sourceWsdlFile, packageName);
		result = builder.generate();
		
		return result;
	}
	
	public String name() {
	 	return "Web Service Client";
	}
	
	public WizardDescriptor.Panel current() {
       return panels[index];
	}
	
	public boolean hasNext() {
        return index < panels.length - 1;  
	}
	
	public void nextPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
		
        index++;
	}
	
	public boolean hasPrevious() {
		return index > 0;
	}
	
	public void previousPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
		
        index--;
	}
	
	public void addChangeListener(ChangeListener l) {
		// nothing to do yet
	}
	
	public void removeChangeListener(ChangeListener l) {
		// nothing to do yet
	}
}
