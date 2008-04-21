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
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.web.core.Util;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.DialogDisplayer;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  mk115033
 */
public class ListenerIterator implements TemplateWizard.Iterator {

    private static final Logger LOG = Logger.getLogger(ListenerIterator.class.getName());
    
    //                                    CHANGEME vvv
    //private static final long serialVersionUID = ...L;

    // You should define what panels you want to use here:
    private ListenerPanel panel;
    protected WizardDescriptor.Panel[] createPanels(TemplateWizard wizard) {
        Project project = Templates.getProject( wiz );
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        panel = new ListenerPanel(wizard);
        
        WizardDescriptor.Panel packageChooserPanel;
        if (sourceGroups.length == 0)
            packageChooserPanel = Templates.createSimpleTargetChooser(project, sourceGroups, panel);
        else
            packageChooserPanel = JavaTemplates.createPackageChooser(project, sourceGroups, panel);

        return new WizardDescriptor.Panel[] {
            // Assuming you want to keep the default 2nd panel:
            packageChooserPanel
        };
    }

    public Set<DataObject> instantiate (TemplateWizard wiz) throws IOException/*, IllegalStateException*/ {
        // Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.
       
        FileObject folder = Templates.getTargetFolder( wiz );
        DataFolder targetFolder = DataFolder.findFolder( folder );
        
        ClassPath classPath = ClassPath.getClassPath(folder,ClassPath.SOURCE);
        String listenerName = wiz.getTargetName();
        DataObject result=null;
        
        if (classPath!=null) { //NOI18N
            DataObject template = wiz.getTemplate ();
            if (listenerName==null) {
                // Default name.
                result = template.createFromTemplate (targetFolder);
            } else {
                result = template.createFromTemplate (targetFolder, listenerName);
            }
            String className = classPath.getResourceName(result.getPrimaryFile(),'.',false);
            if (result!=null && panel.createElementInDD()){
                FileObject webAppFo=DeployData.getWebAppFor(folder);
                WebApp webApp=null;
                if (webAppFo!=null) {
                    webApp = DDProvider.getDefault().getDDRoot(webAppFo);
                }
                if (webApp!=null) {     
                    Listener[] oldListeners = webApp.getListener();
                    boolean found=false;
                    for (int i=0;i<oldListeners.length;i++) {
                        if (className.equals(oldListeners[i].getListenerClass())) {
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        try {
                            Listener listener = (Listener)webApp.createBean("Listener");//NOI18N
                            listener.setListenerClass(className);
                            StringBuffer desc= new StringBuffer();
                            int i=0;
                            if (panel.isContextListener()) {
                                desc.append("ServletContextListener"); //NOI18N
                                i++;
                            }
                            if (panel.isContextAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("ServletContextAttributeListener"); //NOI18N
                                i++;
                            }
                            if (panel.isSessionListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("HttpSessionListener"); //NOI18N
                                i++;
                            }
                            if (panel.isSessionAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("HttpSessionAttributeListener"); //NOI18N
                            }
                            if (panel.isRequestListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("RequestListener"); //NOI18N
                                i++;
                            }
                            if (panel.isRequestAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("RequestAttributeListener"); //NOI18N
                            }
                            listener.setDescription(desc.toString());
                            webApp.addListener(listener);
                            webApp.write(webAppFo);
                        }
                        catch (ClassNotFoundException ex) {
                            LOG.log(Level.FINE, "error", ex);
                            //Shouldn happen since
                        }
                    }
                }
            }
            if (result!=null) {
                JavaSource clazz = JavaSource.forFileObject(result.getPrimaryFile());
                if (clazz!=null) {
                    ListenerGenerator gen = new ListenerGenerator(
                        panel.isContextListener(),
                        panel.isContextAttrListener(),
                        panel.isSessionListener(),
                        panel.isSessionAttrListener(),
                        panel.isRequestListener(),
                        panel.isRequestAttrListener());
                    try {
                        gen.generate(clazz);
                    } catch (IOException ex){
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        } else {
            String mes = MessageFormat.format (
                    NbBundle.getMessage (ListenerIterator.class, "TXT_wrongFolderForClass"),
                    new Object [] {"Servlet Listener"}); //NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);                         
        }
        return Collections.singleton (result);
    }

    // --- The rest probably does not need to be touched. ---

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;

    private static final long serialVersionUID = -7586964579556513549L;
    
    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize (TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels (wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName ();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize (TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    // Note that this is very similar to WizardDescriptor.Iterator, but with a
    // few more options for customization. If you e.g. want to make panels appear
    // or disappear dynamically, go ahead.

    public String name () {
        return NbBundle.getMessage(ListenerIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
    }

    public boolean hasNext () {
        return index < panels.length - 1;
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
}
