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
package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.modules.websvc.core.ProjectInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.support.SourceGroups;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.openide.filesystems.FileObject;

public class MessageHandlerWizard implements WizardDescriptor.InstantiatingIterator {

    public int currentPanel = 0;
    private WizardDescriptor.Panel[] wizardPanels;
    private WizardDescriptor.Panel<WizardDescriptor> firstPanel; //special case: use Java Chooser
    private WizardDescriptor wiz;
    private Project project;
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N

    public static MessageHandlerWizard create() {
        return new MessageHandlerWizard();
    }
    private static final String[] HANDLER_STEPS = new String[]{NbBundle.getMessage(MessageHandlerWizard.class, "LBL_SpecifyHandlerInfo")};

    public void initialize(WizardDescriptor wizard) {

        wiz = wizard;
        project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);

        //create the Java Project chooser
        if (sourceGroups.length == 0) {
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, new BottomPanel()));
        } else {
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, new BottomPanel(), true));
        }
        JComponent c = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(c, NbBundle.getMessage(MessageHandlerWizard.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(MessageHandlerWizard.class, "LBL_Handler_Name")); //NOI18N
        c.putClientProperty("WizardPanel_contentData", HANDLER_STEPS);
        c.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(0));
        c.getAccessibleContext().setAccessibleDescription(HANDLER_STEPS[0]);
        wizardPanels = new WizardDescriptor.Panel[]{firstPanel};
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public Set instantiate() throws IOException {
        //new WebServiceCreator(project, wiz).createMessageHandler();
        HandlerCreator creator = CreatorProvider.getHandlerCreator(project, wiz);
        if (creator != null) {
            creator.createMessageHandler();
        }
        return Collections.EMPTY_SET;
    }

    public WizardDescriptor.Panel current() {
        return wizardPanels[currentPanel];
    }

    public boolean hasNext() {
        return currentPanel < wizardPanels.length - 1;
    }

    public boolean hasPrevious() {
        return currentPanel > 0;
    }

    public String name() {
        return NbBundle.getMessage(MessageHandlerWizard.class, "LBL_Create_MessageHandler_Title"); //NOI18N
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentPanel++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentPanel--;
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    protected int getCurrentPanelIndex() {
        return currentPanel;
    }

    /** Dummy implementation of WizardDescriptor.Panel required in order to provide Help Button
     */
    private class BottomPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        public void storeSettings(WizardDescriptor settings) {
        }

        public void readSettings(WizardDescriptor settings) {
        }

        public java.awt.Component getComponent() {
            return new javax.swing.JPanel();
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public boolean isValid() {
            ProjectInfo creator = new ProjectInfo(project);
            int projectType = creator.getProjectType();

            //test for conditions in JSE
            if (projectType == ProjectInfo.JSE_PROJECT_TYPE) {
                return isValidInJavaProject(project, wiz);
            }

            if (!Util.isJavaEE5orHigher(project) && ((WebServicesSupport.getWebServicesSupport(project.getProjectDirectory()) == null) && (WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory()) == null))) {
                // check if jaxrpc plugin installed
                wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(MessageHandlerWizard.class, "ERR_NoJaxrpcPluginFoundHandler")); // NOI18N
                return false;
            }



            //if platform is Tomcat, source level must be jdk 1.5 and jaxws library must be in classpath
            if (!Util.isJavaEE5orHigher(project) && projectType == ProjectInfo.WEB_PROJECT_TYPE && !PlatformUtil.isJsr109Supported(project) && !PlatformUtil.isJsr109OldSupported(project)) {
                //has to be at least jdk 1.5
                if (Util.isSourceLevel14orLower(project)) {
                    wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(MessageHandlerWizard.class, "ERR_HandlerNeedProperSourceLevel")); // NOI18N
                    return false;
                }
                if (!PlatformUtil.hasJAXWSLibrary(project)) {
                    //must have jaxws library
                    wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
                    return false;
                } else {
                    return true;
                }
            }
            return true;
        }

        public HelpCtx getHelp() {
            return new HelpCtx(MessageHandlerWizard.class);
        }
    }

    public static boolean isValidInJavaProject(Project javaProject, WizardDescriptor wiz) {

        SourceGroup[] sgs = ProjectUtils.getSources(javaProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath;
        FileObject handlerFO = null;
        if (sgs.length > 0) {
            classPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.BOOT);
            if (classPath != null) {
                handlerFO = classPath.findResource("javax/xml/ws/handler/Handler.class");  //NOI18N
            }
            if(handlerFO == null){
                classPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
                if(classPath != null){
                    handlerFO = classPath.findResource("javax/xml/ws/handler/Handler.class");  //NOI18N
                }
            }
        }

        if (handlerFO == null) {
            wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(MessageHandlerWizard.class, "ERR_HandlerNeedProperLibraries")); // NOI18N
            return false;
        }
        return true;
    }
}
