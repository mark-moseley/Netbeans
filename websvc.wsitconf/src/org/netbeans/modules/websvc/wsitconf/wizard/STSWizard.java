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

package org.netbeans.modules.websvc.wsitconf.wizard;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.openide.ErrorManager;

import org.openide.WizardDescriptor;

import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 * Wizard to create a new STS Web service.
 * @author Martin Grebac
 */
public class STSWizard implements TemplateWizard.Iterator {
    
    private Project project;
    
    private WsdlModeler wsdlModeler;
    private WsdlModel wsdlModel;
    private WsdlService service;
    private WsdlPort port;
    private WsdlServiceHandler wsdlServiceHandler;
    
    /** Create a new wizard iterator. */
    public STSWizard() {
    }
    
    public static STSWizard create() {
        return new STSWizard();
    }
    
    public Set instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate(wiz);
        DataObject dTemplate = DataObject.find(template);
        
        OutputStream schemaos = null, wsdlos = null;
        
        try {
            final InputStream schemaIS = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/websvc/wsitconf/resources/sts_schema.xsd"); //NOI18N
            File schema = new File(System.getProperty("java.io.tmpdir") + "sts_schema.xsd");     //NOI18N       
            schema.createNewFile();
            schemaos = new FileOutputStream(schema);
            FileUtil.copy(schemaIS, schemaos);
        } catch (FileNotFoundException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
        } finally {
            if (schemaos != null) schemaos.close();
        }

        File folder = null;
        try {
            final InputStream wsdlIS = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/websvc/wsitconf/resources/sts.wsdl");  //NOI18N
            folder = new File(System.getProperty("java.io.tmpdir") + "sts.wsdl");   //NOI18N
            folder.createNewFile();
            wsdlos = new FileOutputStream(folder);
            FileUtil.copy(wsdlIS, wsdlos);
        } catch (FileNotFoundException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
        } finally {
            if (wsdlos != null) wsdlos.close();
        }

        if (folder == null) return null;
        
        final URL wsdlURL = folder.toURI().toURL();

        wiz.putProperty(WizardProperties.WSDL_FILE_PATH, folder.getPath());
        
        wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlURL);
        wsdlModeler.generateWsdlModel(new WsdlModelListener() {
            public void modelCreated(WsdlModel model) {
                wsdlModel=model;
                if (wsdlModel==null) {
                    String serviceName=null;
                    String portName=null;
                    try {
                        wsdlServiceHandler = WsdlServiceHandler.parse(wsdlURL.toExternalForm());
                        serviceName = wsdlServiceHandler.getServiceName();
                        portName = wsdlServiceHandler.getPortName();
                    } catch (ParserConfigurationException ex) {
                    } catch (SAXException ex) {
                    } catch (IOException ex) {}
                } else {
                    List services = wsdlModel.getServices();
                    if (services != null && services.size() > 0) {
                        service = (WsdlService) services.get(0);
                        List ports = service.getPorts();
                        if (ports != null && ports.size() > 0)
                            port = (WsdlPort) ports.get(0);
                    }
                }

//                if (service != null && port != null) {
//                    if (wsdlHandler!=null) {
//                        String bindingType = wsdlHandler.getBindingTypeForPort(port.getName());
//                        if (bindingType!=null) port.setSOAPVersion(bindingType);
//                    }
//                }
            }
        });

        int timeout = 10000;
        while ((service == null) && (timeout > 0)) {
            try {
                Thread.sleep(200);
                timeout -= 200;
            } catch (InterruptedException ex) {
//                ex.printStackTrace();
            }
        }
        
        if (service != null) {
            wiz.putProperty(WizardProperties.WSDL_SERVICE, service);
            wiz.putProperty(WizardProperties.WSDL_PORT, port);
            wiz.putProperty(WizardProperties.WSDL_MODELER, wsdlModeler);            
            new STSWizardCreator(project, wiz).createSTS();
        }
        
        return Collections.singleton(dTemplate);
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;
    private transient WizardDescriptor.Panel bottomPanel;
    
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        
        project = Templates.getProject(wiz);
        
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
//        bottomPanel = new WebServiceFromWSDL(wiz, project);
        WizardDescriptor.Panel firstPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0)
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, bottomPanel));
        else
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, bottomPanel, true));
        
        JComponent comp = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(comp, NbBundle.getMessage(STSWizard.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"),
                NbBundle.getMessage(STSWizard.class, "LBL_Webservice_Name") );
        Util.hideLabelAndLabelFor(comp, NbBundle.getMessage(STSWizard.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));
        
        panels = new WizardDescriptor.Panel[] {
            firstPanel,
        };
        
        // Creating steps.
        Object prop = this.wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.WEB_SERVICE_TYPE, null);
        }
        panels = null;
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(STSWizard.class, "LBL_WizardStepsCount"),
                new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
