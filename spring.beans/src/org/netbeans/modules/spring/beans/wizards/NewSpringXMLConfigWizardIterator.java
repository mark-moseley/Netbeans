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
  *
 * Portions Copyrighted 2008 Craig MacKay.
*/

package org.netbeans.modules.spring.beans.wizards;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

public final class NewSpringXMLConfigWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            Project p = Templates.getProject(wizard);
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(p, groups);

            panels = new WizardDescriptor.Panel[]{
                targetChooser,
                new BeansConfigNamespacesWizardPanel(),
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }

    public Set instantiate() throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
        String targetName = Templates.getTargetName(wizard);
        FileObject templateFileObject = Templates.getTemplate(wizard);
        DataObject templateDataObject = DataObject.find(templateFileObject);

        final String extension = "xml"; // NOI18N

        if (targetName == null || "null".equals(targetName)) { // NOI18N
            targetName = "XMLDocument"; // NOI18N
        }

        String uniqueTargetName = targetName;
        int i = 2;

        while (targetFolder.getFileObject(uniqueTargetName, extension) != null) {
            uniqueTargetName = targetName + i;
            i++;
        }
        final String name = uniqueTargetName;

        DataObject newOne = templateDataObject.createFromTemplate(targetDataFolder, name);
        FileObject createdFile = newOne.getPrimaryFile();

        String[] incNamespaces = (String[]) wizard.getProperty(BeansConfigNamespacesWizardPanel.INCLUDED_NAMESPACES);

        generateFileContents(createdFile, incNamespaces);

        OpenCookie open = (OpenCookie) newOne.getCookie(OpenCookie.class);
        if (open != null) {
            open.open();
        }

        return Collections.singleton(createdFile);
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length; // NOI18N
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
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

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    private void generateFileContents(final FileObject targetFile, String[] incNamespaces) {
        StringBuilder sb = generateXML(incNamespaces);

        try {
            JEditorPane ep = new JEditorPane("text/x-springconfig+xml", ""); // NOI18N
            BaseDocument doc = new BaseDocument(ep.getEditorKit().getClass(), false);
            Formatter f = Formatter.getFormatter(ep.getEditorKit().getClass());
            
            doc.remove(0, doc.getLength());
            doc.insertString(0, sb.toString(), null);
            f.reformatLock();
            try {
                doc.atomicLock();
                try {
                    f.reformat(doc, 0, doc.getLength());
                } finally {
                    doc.atomicUnlock();
                }
            } finally {
                f.reformatUnlock();
            }
            
            sb.replace(0, sb.length(), doc.getText(0, doc.getLength()));
            final String text = sb.toString();

            FileSystem fs = targetFile.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileLock lock = targetFile.lock();
                    try {
                        BufferedWriter bw 
                                = new BufferedWriter(new OutputStreamWriter(
                                targetFile.getOutputStream(lock)));
                        bw.write(text);
                        bw.close();
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
        } catch (FileAlreadyLockedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private StringBuilder generateXML(String[] incNamespaces) {
        String sep = System.getProperty("line.separator"); // NOI18N
        StringBuilder schemaLoc = new StringBuilder();
        schemaLoc.append("       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd"); // NOI18N

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(sep); // NOI18N
        sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"").append(sep); // NOI18N
        sb.append("       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").append(sep); // NOI18N

        for (String cur : incNamespaces) {
            String prefix = cur.substring(0, cur.indexOf("-")).trim(); // NOI18N
            String schemaName = cur.substring(cur.indexOf("-") + 1).trim(); // NOI18N
            if(!schemaName.equals("http://www.springframework.org/schema/p")) { // NOI18N
                String namespace = schemaName.substring(0, schemaName.lastIndexOf("/")); // NOI18N
                sb.append("       xmlns:").append(prefix).append("=\"").append(namespace).append("\"").append(sep); // NOI18N
                schemaLoc.append(sep);
                schemaLoc.append("       ").append(namespace).append(" ").append(schemaName); // NOI18N
            } else {
                sb.append("       xmlns:").append(prefix).append("=\"").append(schemaName).append("\"").append(sep); // NOI18N
            }
        }

        sb.append(schemaLoc).append("\""); // NOI18N
        sb.append(">").append(sep).append("    ").append(sep); // NOI18N
        sb.append("</beans>"); // NOI18N

        return sb;
    }
}
