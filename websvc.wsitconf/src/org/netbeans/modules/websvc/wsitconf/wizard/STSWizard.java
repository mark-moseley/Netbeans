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
package org.netbeans.modules.websvc.wsitconf.wizard;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.openide.WizardDescriptor;

import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 * Wizard to create a new STS Web service.
 * @author Martin Grebac
 */
public class STSWizard implements TemplateWizard.Iterator {

    private Project project;
    private static final Logger logger = Logger.getLogger(STSWizard.class.getName());
    private static final String SERVICENAME_TAG = "__SERVICENAME__"; //NOI18N
    private WsdlModeler wsdlModeler;
    private WsdlModel wsdlModel;
    private WsdlService service;
    private WsdlPort port;

    /** Create a new wizard iterator. */
    public STSWizard() {
    }

    public static STSWizard create() {
        return new STSWizard();
    }

    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        File tempFolder = new File(System.getProperty("netbeans.user"));     //NOI18N
        DataObject folderDO = DataObject.find(FileUtil.toFileObject(tempFolder));

        final File wsdlFile = new File(System.getProperty("netbeans.user") + File.separator + "sts.wsdl");

        FileUtil.runAtomicAction(new Runnable() {

            public void run() {
                OutputStream schemaos = null;
                try {
                    final InputStream schemaIS = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/websvc/wsitconf/resources/templates/sts_schema.template"); //NOI18N
                    File schema = new File(System.getProperty("netbeans.user") + File.separator + "sts_schema.xsd");     //NOI18N
                    schema.createNewFile();
                    schemaos = new FileOutputStream(schema);
                    FileUtil.copy(schemaIS, schemaos);
                } catch (FileNotFoundException ex) {
                    logger.log(Level.INFO, null, ex);
                } catch (IOException ex) {
                    logger.log(Level.INFO, null, ex);
                } finally {
                    if (schemaos != null) {
                        try {
                            schemaos.close();
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }
                }

                String serviceName = Templates.getTargetName(wiz) + NbBundle.getMessage(STSWizard.class, "LBL_ServiceEnding"); //NOI18N

                try {
                    OutputStream wsdlos = null;
                    try {
                        if (!wsdlFile.exists()) {
                            final InputStream wsdlIS = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/websvc/wsitconf/resources/templates/sts.template"); //NOI18N
                            wsdlFile.createNewFile();
                            wsdlos = new FileOutputStream(wsdlFile);
                            FileUtil.copy(wsdlIS, wsdlos);
                        }
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.INFO, null, ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, null, ex);
                    } finally {
                        if (wsdlos != null) {
                            try {
                                wsdlos.close();
                            } catch (IOException ex) {
                                logger.log(Level.INFO, null, ex);
                            }
                        }
                    }


                    FileObject wsdlFO = FileUtil.toFileObject(wsdlFile);
                    FileObject wsdlFolder = wsdlFO.getParent();

                    String newName = serviceName;
                    FileObject newFO = null;

                    FileInputStream fi = null;
                    OutputStream fo = null;

                    try {
                        fi = new FileInputStream(wsdlFile);
                        File f = new File(FileUtil.toFile(wsdlFolder).getAbsolutePath(), newName + ".wsdl");
                        f.createNewFile();
                        fo = new FileOutputStream(f);
                        FileUtil.copy(fi, fo);
                        newFO = FileUtil.toFileObject(f);
                    //newFO = FileUtil.copyFile(wsdlFO, wsdlFolder, newName);
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.INFO, null, ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, null, ex);
                    } finally {
                        try {
                            if (fi != null) {
                                fi.close();
                            }
                            if (fo != null) {
                                fo.close();
                            }
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }

                    File newFile = FileUtil.toFile(newFO);
                    final URL wsdlURL = newFile.toURI().toURL();

                    wiz.putProperty(WizardProperties.WSDL_FILE_PATH, newFile.getPath());

                    BufferedReader reader = null;
                    BufferedWriter writer = null;

                    try {
                        reader = new BufferedReader(new FileReader(wsdlFile));
                        writer = new BufferedWriter(new FileWriter(newFile));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if ((index = line.indexOf(SERVICENAME_TAG)) != -1) {
                                line = line.replaceAll(SERVICENAME_TAG, serviceName);
                            }
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.INFO, null, ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, null, ex);
                    } finally {
                        try {
                            if (writer != null) {
                                writer.flush();
                                writer.close();
                            }

                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }

                    wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlURL);
                    wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                        public void modelCreated(WsdlModel model) {
                            wsdlModel = model;
                            if (wsdlModel == null) {
                                try {
                                    WsdlServiceHandler.parse(wsdlURL.toExternalForm());
                                } catch (ParserConfigurationException ex) {
                                    logger.log(Level.FINE, null, ex);
                                } catch (SAXException ex) {
                                    logger.log(Level.FINE, null, ex);
                                } catch (IOException ex) {
                                    logger.log(Level.FINE, null, ex);
                                }
                            } else {
                                List services = wsdlModel.getServices();
                                if (services != null && !services.isEmpty()) {
                                    service = (WsdlService) services.get(0);
                                    List ports = service.getPorts();
                                    if (ports != null && !ports.isEmpty()) {
                                        port = (WsdlPort) ports.get(0);
                                    }
                                }
                            }
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
                } catch (MalformedURLException ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
        });

        return Collections.singleton(folderDO);
    }
    private transient int index;
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private transient TemplateWizard wiz;

    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;

        project = Templates.getProject(wiz);

        boolean wizardEnabled = Util.isJavaEE5orHigher(project);

        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        WizardDescriptor.Panel firstPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0) {
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, null), wizardEnabled);
        } else {
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, null), wizardEnabled);
        }
        JComponent comp = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(comp, NbBundle.getMessage(STSWizard.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"),
                NbBundle.getMessage(STSWizard.class, "LBL_Webservice_Name"));
        Util.hideLabelAndLabelFor(comp, NbBundle.getMessage(STSWizard.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));

        panels = new WizardDescriptor.Panel[]{
                    firstPanel,
                };

        // Creating steps.
        Object prop = this.wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
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
        String[] res = new String[(before.length - diff) + panels.length];
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
                new String[]{(Integer.valueOf(index + 1)).toString(), Integer.valueOf(panels.length).toString()}); //NOI18N
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
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

    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
}
