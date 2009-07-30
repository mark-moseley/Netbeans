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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.web.core.Util;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.TagFileType;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  Milan Kuchtiak
 */
public class PageIterator implements TemplateWizard.Iterator {

    private static final Logger LOG = Logger.getLogger(PageIterator.class.getName());
    private static final long serialVersionUID = -7586964579556513549L;
    private transient FileType fileType;
    private WizardDescriptor.Panel folderPanel;
    private transient SourceGroup[] sourceGroups;

    public static PageIterator createJspIterator() {
        return new PageIterator(FileType.JSP);
    }

    public static PageIterator createTagIterator() {
        return new PageIterator(FileType.TAG);
    }

    public static PageIterator createTagLibraryIterator() {
        return new PageIterator(FileType.TAGLIBRARY);
    }

    public static PageIterator createHtmlIterator() {
        return new PageIterator(FileType.HTML);
    }

    public static PageIterator createXHtmlIterator() {
        return new PageIterator(FileType.XHTML);
    }

    public static PageIterator createXCssIterator() {
        return new PageIterator(FileType.CSS);
    }

    protected PageIterator(FileType fileType) {
        this.fileType = fileType;
    }

    // You should define what panels you want to use here:
    protected WizardDescriptor.Panel[] createPanels(Project project) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        if (fileType.equals(FileType.JSP)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel(project, sourceGroups, fileType);

            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        } else if (fileType.equals(FileType.HTML) || fileType.equals(FileType.XHTML) || fileType.equals(FileType.CSS)) {
            SourceGroup[] docRoot = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            SourceGroup[] srcRoots = Util.getJavaSourceGroups(project);
            if (docRoot != null && srcRoots != null) {
                sourceGroups = new SourceGroup[docRoot.length + srcRoots.length];
                System.arraycopy(docRoot, 0, sourceGroups, 0, docRoot.length);
                System.arraycopy(srcRoots, 0, sourceGroups, docRoot.length, srcRoots.length);
            }
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel(project, sourceGroups, fileType);
            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        } else if (fileType.equals(FileType.TAG)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            }
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel(project, sourceGroups, fileType);
            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        } else if (fileType.equals(FileType.TAGLIBRARY)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            }
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel(project, sourceGroups, fileType);
            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        }
        return new WizardDescriptor.Panel[]{
                    Templates.createSimpleTargetChooser(project, sourceGroups)
                };
    }

    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        // Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.

        org.openide.filesystems.FileObject dir = Templates.getTargetFolder(wiz);
        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wiz);
        FileObject templateParent = template.getParent();
        TargetChooserPanel panel = (TargetChooserPanel) folderPanel;
        
        Map<String, Object> wizardProps = new HashMap<String, Object>();

        if (FileType.JSP.equals(fileType)) {
            if (panel.isSegment()) {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("JSPFX", "jspf"); //NOI18N
                } else {
                    template = templateParent.getFileObject("JSPF", "jspf"); //NOI18N
                }
            } else {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("JSPX", "jspx"); //NOI18N
                }
                if (panel.isFacelets()) {
                    template = templateParent.getFileObject("JSP", "xhtml"); //NOI18N
                }
            }
        } else if (FileType.TAG.equals(fileType)) {
            if (panel.isSegment()) {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("TagFileFX", "tagf"); //NOI18N
                } else {
                    template = templateParent.getFileObject("TagFileF", "tagf"); //NOI18N
                }
            } else {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("TagFileX", "tagx"); //NOI18N
                }
            }
        } else if (FileType.TAGLIBRARY.equals(fileType)) {
            WebModule wm = WebModule.getWebModule(dir);
            if (wm != null) {
                Profile j2eeVersion = wm.getJ2eeProfile();
                if (Profile.J2EE_13.equals(j2eeVersion)) {
                    template = templateParent.getFileObject("TagLibrary_1_2", "tld"); //NOI18N
                }
            }
        }
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wiz), wizardProps);
        if (dobj != null) {
            if (FileType.TAGLIBRARY.equals(fileType)) { //TLD file 
                TLDDataObject tldDO = (TLDDataObject) dobj;
                Taglib taglib = tldDO.getTaglib();
                taglib.setUri(panel.getUri());
                taglib.setShortName(panel.getPrefix());
                tldDO.write(taglib);
            } else if (FileType.TAG.equals(fileType) && panel.isTldCheckBoxSelected()) { //Write Tag File to TLD 
                FileObject tldFo = panel.getTldFileObject();
                if (tldFo != null) {
                    if (!tldFo.canWrite()) {
                        String mes = java.text.MessageFormat.format(
                                NbBundle.getMessage(PageIterator.class, "MSG_tldRO"),
                                new Object[]{tldFo.getNameExt()});
                        org.openide.NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(mes,
                                org.openide.NotifyDescriptor.Message.ERROR_MESSAGE);
                        org.openide.DialogDisplayer.getDefault().notify(desc);
                    } else {
                        TLDDataObject tldDO = (TLDDataObject) DataObject.find(tldFo);
                        Taglib taglib = null;
                        try {
                            taglib = tldDO.getTaglib();
                        } catch (IOException ex) {
                            String mes = java.text.MessageFormat.format(
                                    NbBundle.getMessage(PageIterator.class, "MSG_tldCorrupted"),
                                    new Object[]{tldFo.getNameExt()});
                            org.openide.NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(mes,
                                    org.openide.NotifyDescriptor.Message.ERROR_MESSAGE);
                            org.openide.DialogDisplayer.getDefault().notify(desc);
                        }
                        if (taglib != null) {
                            TagFileType tag = new TagFileType();
                            tag.setName(panel.getTagName());
                            String packageName = null;
                            for (int i = 0; i < sourceGroups.length && packageName == null; i++) {
                                packageName = org.openide.filesystems.FileUtil.getRelativePath(sourceGroups[i].getRootFolder(), dobj.getPrimaryFile());
                            }
                            tag.setPath("/" + packageName); //NOI18N
                            taglib.addTagFile(tag);
                            SaveCookie save = (SaveCookie) tldDO.getCookie(SaveCookie.class);
                            if (save != null) {
                                save.save();
                            }
                            try {
                                tldDO.write(taglib);
                            } catch (IOException ex) {
                                LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    }
                }
            }
        }
        return Collections.singleton(dobj);
    }

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;

    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject(wiz);
        panels = createPanels(project);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    public void uninitialize(TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    // Note that this is very similar to WizardDescriptor.Iterator, but with a
    // few more options for customization. If you e.g. want to make panels appear
    // or disappear dynamically, go ahead.
    public String name() {
        return NbBundle.getMessage(PageIterator.class, "TITLE_x_of_y",
                index + 1, panels.length);
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

    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
}
