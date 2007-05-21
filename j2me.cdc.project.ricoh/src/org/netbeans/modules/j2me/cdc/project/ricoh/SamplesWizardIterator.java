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

package org.netbeans.modules.j2me.cdc.project.ricoh;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.project.CDCPropertiesDescriptor;
import org.netbeans.modules.j2me.cdc.project.ricoh.dalp.DalpParser;
import org.netbeans.modules.j2me.cdc.project.ricoh.dalp.DalpParserHandlerImpl;
import org.netbeans.modules.j2me.cdc.project.ui.wizards.NewCDCProjectWizardIterator;
import org.netbeans.modules.j2me.cdc.project.ui.wizards.PanelConfigurePlatform;
import org.netbeans.modules.j2me.cdc.project.ui.wizards.PanelConfigureProject;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformInstallPanel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SamplesWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N

    private static final String MANIFEST_FILE = "manifest.mf"; // NOI18N

    private static final long serialVersionUID = 1L;
    
    private int type = NewCDCProjectWizardIterator.TYPE_SAMPLE;
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
    private String platform;
    private String preferredName;
    
    public SamplesWizardIterator() {}
    
    public static SamplesWizardIterator createIterator() {
        return new SamplesWizardIterator();
    }
        
    private WizardDescriptor.Panel[] createPanels () {
        int i = getNumberOfSuitableCdcPlatforms(platform);
        return i != 0 ?
            new WizardDescriptor.Panel[] {
                new PanelConfigurePlatform(platform),
                new PanelConfigureProject( this.type, preferredName )} :
            new WizardDescriptor.Panel[] {
                new PlatformInstallPanel.WizardPanel(platform),
                new PanelConfigurePlatform(platform),
                new PanelConfigureProject( this.type, preferredName )
            };
    }

    private String[] createSteps() {
        int i = getNumberOfSuitableCdcPlatforms(platform);
        return i != 0 ?
            new String[] {
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_SelectPlatform") ,
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_ConfigureProject") }:
            new String[] {
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_Step_AddPlatform"), //NOI18N
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_SelectPlatform") ,
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_ConfigureProject")
            };
    }
    
    private Properties parseRicohAdditionalResources(FileObject projectDir){
        FileObject[] fos = projectDir.getChildren();
        for (int i = 0; i < fos.length; i++) {
            FileObject dalp = fos[i];
            if ("dalp".compareToIgnoreCase(dalp.getExt()) == 0){
                Properties properties = new Properties();
                DalpParserHandlerImpl handler = new DalpParserHandlerImpl(projectDir, properties);
                try {
                    DalpParser.parse(new InputSource(dalp.getInputStream()), handler);
                } catch (FileNotFoundException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (SAXException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (ParserConfigurationException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                return properties;
            }
        }
        return null;
    }

    
    public Set/*<FileObject>*/ instantiate() throws IOException {
                
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirPr = (File)wiz.getProperty("projdir");        //NOI18N
        if (dirPr != null) {
            dirPr = FileUtil.normalizeFile(dirPr);
        }
    
        String name = (String)wiz.getProperty("name");        //NOI18N

        final String activePlatform = (String)wiz.getProperty("activePlatform");       //NOI18N
        final String activeDevice   = (String)wiz.getProperty("activeDevice");         //NOI18N
        final String activeProfile  = (String)wiz.getProperty("activeProfile");        //NOI18N
        Properties props = (Properties) wiz.getProperty("additionalProperties"); //NOI18N               
        final FileObject template = Templates.getTemplate(wiz);
        PlatformSelectionPanel.PlatformDescription pd=(PlatformSelectionPanel.PlatformDescription) wiz.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        AntProjectHelper h =J2MEProjectGenerator.createProject(dirPr, name, pd,new J2MEProjectGenerator.ProjectGeneratorCallback() {
            public void doPostGeneration(Project p, final AntProjectHelper h, FileObject dir, File projectLocationFile, ArrayList<String> configurations) throws IOException 
            {
                
                createManifest(dir, MANIFEST_FILE);                
                unZipFile(template.getInputStream(), dir);

                final FileObject lib = dir.getFileObject("lib");
                if (lib != null){
                    final ReferenceHelper refHelper = (ReferenceHelper) p.getLookup().lookup(ReferenceHelper.class);
                    try {
                        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
                            public Object run() throws Exception {
                                final List<String> entries = new ArrayList<String>();
                                final FileObject[] libs = lib.getChildren();
                                for (int i = 0; i < libs.length; i++) {
                                    String ref = refHelper.createForeignFileReference(FileUtil.normalizeFile(FileUtil.toFile(libs[i])), null);
                                    entries.add(ref + ((i < libs.length - 1) ? ":" : ""));
                                }

                                EditableProperties editableProps = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                editableProps.setProperty("libs.classpath", entries.toArray(new String[entries.size()]));
                                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, editableProps); // #47609
                                return null;
                            }
                        });
                    } catch (MutexException me ) {
                        ErrorManager.getDefault().notify (me);
                    }
                }

                Properties parsed =  parseRicohAdditionalResources(dir);
                if (parsed != null){
                    Iterator entries = parsed.entrySet().iterator();
                    while (entries.hasNext()) {
                        final Map.Entry elem = (Map.Entry) entries.next();
                        final Object value = elem.getValue();
                        if (value instanceof File){
                            final ReferenceHelper refHelper = (ReferenceHelper) p.getLookup().lookup(ReferenceHelper.class);
                            if (value != null && ((File)value).exists()){
                                try {
                                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
                                        public Object run() throws Exception {
                                            String ref = refHelper.createForeignFileReference(FileUtil.normalizeFile((File)value), null);
                                            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                            props.put((String)elem.getKey(), ref);
                                            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                                            return null;
                                        }
                                    });
                                } catch (MutexException me ) {
                                    ErrorManager.getDefault().notify (me);
                                }
                            }
                        } else {
                            EditableProperties editableProps = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            editableProps.setProperty((String)elem.getKey(), String.valueOf(elem.getValue()));
                            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, editableProps); // #47609
                        }
                    }
                }

                JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms (activePlatform, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
                if (platforms.length != 0){
                    CDCPlatform cdcplatform = (CDCPlatform)platforms[0];
                    final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);                
                    ep.setProperty(CDCPropertiesDescriptor.APPLICATION_NAME, p.getProjectDirectory().getNameExt());
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE, cdcplatform.getAntName()); // NOI18N        
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION, cdcplatform.getDisplayName()); // NOI18N        
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TRIGGER, "CDC"); // NOI18N        
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TYPE, cdcplatform.getType()); // NOI18N        
                    String classVersion = cdcplatform.getClassVersion();
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeDevice); // NOI18N
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_PROFILE, activeProfile); // NOI18N
                    //add bootclasspath
                    NewCDCProjectWizardIterator.generatePlatformProperties(cdcplatform, activeDevice, activeProfile, ep); // NOI18N
                    ep.setProperty(DefaultPropertiesDescriptor.JAVAC_SOURCE, classVersion != null ? classVersion : "1.2"); // NOI18N
                    ep.setProperty(DefaultPropertiesDescriptor.JAVAC_TARGET, classVersion != null ? classVersion : "1.2"); // NOI18N
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                } else {
                    throw new IllegalArgumentException("No CDC platform installed");// NOI18N
                } 
            }
        });

        resultSet.add (h.getProjectDirectory ());
        dirPr = (dirPr != null) ? dirPr.getParentFile() : null;
        if (dirPr != null && dirPr.exists()) {
            ProjectChooser.setProjectsFolder (dirPr);    
        }
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;        
        FileObject template = Templates.getTemplate(wiz);
        preferredName = template.getName();
        this.wiz.putProperty("name", preferredName);
        platform = (String) template.getAttribute("platform");
        
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
        this.wiz.putProperty("additionalProperties", new Properties());
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null);
        this.wiz.putProperty("additionalProperties", null);
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
        this.wiz.putProperty("mainClass",null);         //NOI18N
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
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
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            source.close();
        }
    }

    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @param path the relative path of the file
     * @throws IOException in case of problems
     */
    private static void createManifest(FileObject dir, String path) throws IOException {
        FileObject manifest = dir.createData(MANIFEST_FILE);
        FileLock lock = manifest.lock();
        try {
            OutputStream os = manifest.getOutputStream(lock);
            try {
                PrintWriter pw = new PrintWriter(os);
                pw.println("Manifest-Version: 1.0"); // NOI18N
                pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                pw.println(); // safest to end in \n\n due to JRE parsing bug
                pw.flush();
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    static int getNumberOfSuitableCdcPlatforms(String platformType){
        Set<String> accepted = null;
        if (platformType != null){
            accepted = new HashSet<String>();
            StringTokenizer st = new StringTokenizer(platformType, ",");
            while(st.hasMoreTokens()){
                accepted.add(st.nextToken());
            }
        }
        
            
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms (null, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        if (accepted == null)
            return platforms.length;
        
        List<JavaPlatform> plf = new ArrayList<JavaPlatform>();
        for (JavaPlatform platform : platforms) {
            if (accepted.contains( ((CDCPlatform)platform).getType()))
                plf.add(platform);
        }
        return plf.size();
    }    
}
