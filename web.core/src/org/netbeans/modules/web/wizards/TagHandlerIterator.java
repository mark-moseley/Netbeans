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
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.web.core.Util;
import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.TldAttributeType;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  mk115033
 */
public class TagHandlerIterator implements TemplateWizard.Iterator {
    private static final Logger LOG = Logger.getLogger(TagHandlerIterator.class.getName());
    private WizardDescriptor.Panel packageChooserPanel,tagHandlerSelectionPanel,tagInfoPanel;
    
    // You should define what panels you want to use here:
    protected WizardDescriptor.Panel[] createPanels (Project project,TemplateWizard wiz) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        tagHandlerSelectionPanel = new TagHandlerSelection(wiz);
        
        if (sourceGroups.length == 0)
            packageChooserPanel = Templates.createSimpleTargetChooser(project, sourceGroups, tagHandlerSelectionPanel);
        else
            packageChooserPanel = JavaTemplates.createPackageChooser(project,sourceGroups,tagHandlerSelectionPanel);
        
        sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        if (sourceGroups==null || sourceGroups.length==0)
            sourceGroups = Util.getJavaSourceGroups(project);
        if (sourceGroups==null || sourceGroups.length==0)
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);        
        tagInfoPanel = new TagInfoPanel(wiz, project, sourceGroups);
        return new WizardDescriptor.Panel[] {
            packageChooserPanel,
            tagInfoPanel
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
       
        org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        DataFolder df = DataFolder.findFolder( dir );
        
        FileObject template = Templates.getTemplate( wiz );
        
        if (((TagHandlerSelection)tagHandlerSelectionPanel).isBodyTagSupport()) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("BodyTagHandler","java"); //NOI18N
        }
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );
        // writing to TLD File
        TagInfoPanel tldPanel = (TagInfoPanel)tagInfoPanel;
        Object[][] attrs = tldPanel.getAttributes();
        boolean isBodyTag = ((TagHandlerSelection)tagHandlerSelectionPanel).isBodyTagSupport();
        
        // writing setters to tag handler
        if (attrs.length>0 || isBodyTag) {
            JavaSource clazz = JavaSource.forFileObject(dobj.getPrimaryFile());
            boolean evaluateBody = !((TagInfoPanel)tagInfoPanel).isEmpty();
            TagHandlerGenerator generator = new TagHandlerGenerator(clazz,attrs,isBodyTag, evaluateBody);
            try {
                generator.generate();
            } catch (IOException ex){
                LOG.log(Level.INFO, null, ex);
            }
        }

        //#150274
        Project project = Templates.getProject( wiz );
        ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
        if (modifier != null) {
            modifier.extendClasspath(dobj.getPrimaryFile(), new String[] {
                ContainerClassPathModifier.API_JSP
            });
        }


        
        // writing to TLD file
        if (tldPanel.writeToTLD()) {
            FileObject tldFo = tldPanel.getTLDFile();
            if (tldFo!=null) {
                if (!tldFo.canWrite()) {
                    String mes = MessageFormat.format (
                            NbBundle.getMessage (TagHandlerIterator.class, "MSG_tldRO"),
                            new Object [] {tldFo.getNameExt()});
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                } else {
                    TLDDataObject tldDO = (TLDDataObject)DataObject.find(tldFo);
                    Taglib taglib=null;
                    try {
                        taglib = tldDO.getTaglib();
                    } catch (IOException ex) {
                        String mes = MessageFormat.format (
                                NbBundle.getMessage (TagHandlerIterator.class, "MSG_tldCorrupted"),
                                new Object [] {tldFo.getNameExt()});
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                    if (taglib!=null) {
                        TagType tag = new TagType();
                        tag.setName(tldPanel.getTagName());
                        tag.setTagClass(tldPanel.getClassName());
                        if (tldPanel.isEmpty()) {
                            tag.setBodyContent("empty"); //NOI18N
                        } else if (tldPanel.isScriptless()) {
                            tag.setBodyContent(isBodyTag?"JSP":"scriptless"); //NOI18N
                        } else if (tldPanel.isTegdependent()) {
                            tag.setBodyContent("tagdependent"); //NOI18N
                        }
                        //Object[][] attrs = tldPanel.getAttributes();
                        for (int i=0;i<attrs.length;i++) {
                            TldAttributeType attr = new TldAttributeType();
                            attr.setName((String)attrs[i][0]);
                            attr.setType((String)attrs[i][1]);
                            boolean required = ((Boolean)attrs[i][2]).booleanValue();
                            if (required) attr.setRequired("true"); //NOI18N
                            boolean rtexpr = ((Boolean)attrs[i][3]).booleanValue();
                            if (rtexpr) attr.setRtexprvalue("true"); //NOI18N
                            tag.addAttribute(attr);
                        }
                        taglib.addTag(tag);
                        SaveCookie save = (SaveCookie)tldDO.getCookie(SaveCookie.class);
                        if (save!=null) save.save();
                        try {
                            tldDO.write(taglib);
                        } catch (IOException ex) {
                            LOG.log(Level.WARNING, null, ex);
                        }
                    }
                }
            }
        }
        
        return Collections.singleton(dobj);
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
        Project project = Templates.getProject( wiz );
        panels = createPanels (project,wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
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
                jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
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
        return NbBundle.getMessage(TagHandlerIterator.class, "TITLE_x_of_y",
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
    public WizardDescriptor.Panel<WizardDescriptor> current () {
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
