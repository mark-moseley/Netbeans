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
package org.netbeans.spi.java.project.support.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;


public final class SharableLibrariesUtils {

    static final String PROP_LOCATION = "location"; //NOI18N
    static final String PROP_ACTIONS = "actions"; //NOI18N
    static final String PROP_HELPER = "helper"; //NOI18N
    static final String PROP_REFERENCE_HELPER = "refhelper"; //NOI18N
    static final String PROP_LIBRARIES = "libraries"; //NOI18N
    static final String PROP_JAR_REFS = "jars"; //NOI18N
    
    /**
     * The default filename for sharable library definition file.
     */
    public static final String DEFAULT_LIBRARIES_FILENAME = "nblibraries.properties";
    
    
    
    /**
     * File chooser implementation for browsing for shared library location.
     * @param current
     * @param comp
     * @param projectLocation
     * @return relative or absolute path to project libraries folder.
     */
    public static String browseForLibraryLocation(String current, Component comp, File projectLocation) {
        File lib = PropertyUtils.resolveFile(projectLocation, current);
        if (!lib.exists()) {
            lib = lib.getParentFile();
        }
        lib = FileUtil.normalizeFile(lib);
        FileChooser chooser = new FileChooser(projectLocation, null);
        chooser.setCurrentDirectory(lib);
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        chooser.setDialogTitle(NbBundle.getMessage(SharableLibrariesUtils.class,"LBL_Browse_Libraries_Title"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(comp)) {
            String[] files;
            try {
                files = chooser.getSelectedPaths();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            if (files.length == 1) {
                String currentLibrariesLocation = files[0];
                return currentLibrariesLocation;
            }
        }
        return null;
    }    

    public static boolean showMakeSharableWizard(final AntProjectHelper helper, ReferenceHelper ref, List<String> libraryNames, List<String> jarReferences) {

        final WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Make project sharable and self-contained.");
        wizardDescriptor.putProperty(PROP_HELPER, helper);
        wizardDescriptor.putProperty(PROP_REFERENCE_HELPER, ref);
        wizardDescriptor.putProperty(PROP_LIBRARIES, libraryNames);
        wizardDescriptor.putProperty(PROP_JAR_REFS, jarReferences);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final String loc = (String) wizardDescriptor.getProperty(PROP_LOCATION);
            assert loc != null;
            try {
                // create libraries property file if it does not exist:
                File f = new File(loc);
                if (!f.isAbsolute()) {
                    f = new File(FileUtil.toFile(helper.getProjectDirectory()), loc);
                }
                f = FileUtil.normalizeFile(f);
                if (!f.exists()) {
                    FileUtil.createData(f);
                }

                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {

                        public Object run() throws IOException {
                            try {
                                helper.setLibrariesLocation(loc);
                                ProjectManager.getDefault().saveProject(FileOwnerQuery.getOwner(helper.getProjectDirectory()));

                                // TODO or make just runnables?
                                List<Action> actions = (List<Action>) wizardDescriptor.getProperty(PROP_ACTIONS);
                                for (Action act : actions) {
                                    act.actionPerformed(null);
                                }
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    throw (IOException) ex.getException();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }


        }
        return !cancelled;
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private static WizardDescriptor.Panel[] getPanels() {
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[]{
            new MakeSharableWizardPanel1(),
            new MakeSharableWizardPanel2()
        };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }
        }
        return panels;
    }
    
    static class KeepLibraryAtLocation extends AbstractAction {
        private boolean keepRelativeLocations;
        private Library library;
        private AntProjectHelper helper;

        KeepLibraryAtLocation(Library l , boolean relative, AntProjectHelper h) {
            library = l;
            keepRelativeLocations = relative;
            helper = h;
        }
        public void actionPerformed(ActionEvent e) {
            String loc = helper.getLibrariesLocation();
            assert loc != null;
            File mainPropertiesFile = helper.resolveFile(loc);
            try {
                LibraryManager man = LibraryManager.forLocation(mainPropertiesFile.toURI().toURL());
                Map<String, List<URL>> volumes = new HashMap<String, List<URL>>();
                LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(library.getType());
                assert provider != null;
                for (String volume : provider.getSupportedVolumeTypes()) {
                    List<URL> urls = library.getContent(volume);
                    List<URL> newurls = new ArrayList<URL>();
                    for (URL url : urls) {
                        String jarFolder = null;
                        boolean isArchive = false;
                        if ("jar".equals(url.getProtocol())) { // NOI18N
                            jarFolder = getJarFolder(url);
                            url = FileUtil.getArchiveFile(url);
                            isArchive = true;
                        }
                        FileObject fo = URLMapper.findFileObject(url);

                        if (fo != null) {
                            if (keepRelativeLocations) {
                                File path = FileUtil.toFile(fo);
                                String str = PropertyUtils.relativizeFile(mainPropertiesFile.getParentFile(), path);
                                url = LibrariesSupport.convertFilePathToURL(str);
                            } else {
                                url = fo.getURL();
                            }
                            if (isArchive) {
                                url = FileUtil.getArchiveRoot(url);
                            }
                            if (jarFolder != null) {
                                 url = appendJarFolder(url, jarFolder);
                            }
                            
                        }
                        

                        newurls.add(url);
                    }
                    volumes.put(volume, newurls);
                }
                
                man.createLibrary(library.getType(), library.getName(), volumes);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
    static class CopyLibraryJars extends AbstractAction {
        private Library library;
        private ReferenceHelper refHelper;
 
        CopyLibraryJars(ReferenceHelper h, Library l) {
            refHelper = h;
            library = l;
        }
         
        public void actionPerformed(ActionEvent e) {
            assert library.getManager() == LibraryManager.getDefault() : "Only converting from non-sharable to sharable is supported."; //NOi18N
            try {
                refHelper.copyLibrary(library);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    

    
    
    /** for jar url this method returns path wihtin jar or null*/
    private static String getJarFolder(URL url) {
        assert "jar".equals(url.getProtocol()) : url;
        String u = url.toExternalForm();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1 && index + 2 < u.length()) {
            return u.substring(index + 2);
        }
        return null;
    }

    /** append path to given jar root url */
    private static URL appendJarFolder(URL u, String jarFolder) {
        assert "jar".equals(u.getProtocol()) && u.toExternalForm().endsWith("!/") : u;
        try {
            return new URL(u + jarFolder.replace('\\', '/')); //NOI18N
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }         
}
