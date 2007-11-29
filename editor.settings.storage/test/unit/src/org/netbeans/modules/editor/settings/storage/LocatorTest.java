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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.fontscolors.ColoringStorage;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class LocatorTest extends NbTestCase {
    
    private static final String FC_CONTENTS = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE fontscolors PUBLIC \"-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN\" \"http://www.netbeans.org/dtds/EditorFontsColors-1_1.dtd\">\n" +
        "<fontscolors></fontscolors>";
    
    private static final String KB_CONTENTS = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE bindings PUBLIC \"-//NetBeans//DTD Editor KeyBindings settings 1.1//EN\" \"http://www.netbeans.org/dtds/EditorKeyBindings-1_1.dtd\">\n" +
        "<bindings></bindings>";

    /** Creates a new instance of LocatorTest */
    public LocatorTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }

    public void testOsSpecificFiles() throws Exception {
        String currentOs = getCurrentOsId();
        String [] files = new String [] {
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/f.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/e.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/d.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/a.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file4.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file1.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file99.xml",
        };
        
        createOrderedFiles(files, FC_CONTENTS);
        
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/x-whatever/FontsColors/PPP/Defaults/e.xml");
        f.setAttribute("nbeditor-settings-targetOS", currentOs);

        String [] osOrderedFiles = new String [] {
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/f.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/d.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/a.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/e.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file4.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file1.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file99.xml",
        };
        
        FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        scan(ColoringStorage.ID, baseFolder, "text/x-whatever", null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("PPP");
        checkProfileFiles(osOrderedFiles, null, profileFiles, "PPP");
    }
    
    public void testFullLayout() throws Exception {
        String [] files1 = new String [] {
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file1.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file2.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file3.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file4.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/file1.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/file2.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/file3.xml",
        };
        
        String [] files2 = new String [] {
            "Editors/text/x-whatever/FontsColors/MyProfile2/Defaults/xyz.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/Defaults/abc.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/mrkev.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/okurka.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/cibule.xml",
        };
        
        createOrderedFiles(files1, FC_CONTENTS);
        TestUtilities.createFile("Editors/text/x-whatever/FontsColors/MyProfileA/org-netbeans-modules-editor-settings-CustomFontsColors.xml", FC_CONTENTS);
        createOrderedFiles(files2, FC_CONTENTS);
        TestUtilities.createFile("Editors/text/x-whatever/FontsColors/MyProfile2/org-netbeans-modules-editor-settings-CustomFontsColors.xml", FC_CONTENTS);
        
        FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        scan(ColoringStorage.ID, baseFolder, "text/x-whatever", null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 2, results.size());
        
        List<Object []> profileAFiles = results.get("MyProfileA");
        checkProfileFiles(files1, "Editors/text/x-whatever/FontsColors/MyProfileA/org-netbeans-modules-editor-settings-CustomFontsColors.xml", profileAFiles, "ProfileA");
        
        List<Object []> profile2Files = results.get("MyProfile2");
        checkProfileFiles(files2, "Editors/text/x-whatever/FontsColors/MyProfile2/org-netbeans-modules-editor-settings-CustomFontsColors.xml", profile2Files, "ProfileA");
    }
    
    public void testFullFontsColorsLegacyLayout() throws Exception {
        String [] files = new String [] {
            "Editors/NetBeans/Defaults/defaultColoring.xml",
            "Editors/NetBeans/Defaults/coloring.xml",
            "Editors/NetBeans/Defaults/editorColoring.xml",
            "Editors/NetBeans/coloring.xml",
            "Editors/NetBeans/editorColoring.xml",
        };
        
        createOrderedFiles(files, FC_CONTENTS);
        
        FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        scan(ColoringStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, null, profileFiles, "NetBeans");
    }

    public void testFullFontsColorsMixedLayout() throws Exception {
        String [] files = new String [] {
            "Editors/text/x-whatever/NetBeans/Defaults/defaultColoring.xml",
            "Editors/text/x-whatever/NetBeans/Defaults/coloring.xml",
            "Editors/text/x-whatever/NetBeans/Defaults/editorColoring.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file1.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file2.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file3.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file4.xml",
            "Editors/text/x-whatever/NetBeans/coloring.xml",
            "Editors/text/x-whatever/NetBeans/editorColoring.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/file1.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/file2.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/file3.xml",
        };
        String writableUserFile = "Editors/" + getWritableFileName(ColoringStorage.ID, "text/x-whatever", "NetBeans", "xyz", false);
        
        createOrderedFiles(files, FC_CONTENTS);
        TestUtilities.createFile(writableUserFile, FC_CONTENTS);
        orderFiles("Editors/text/x-whatever/FontsColors/NetBeans/file3.xml", writableUserFile);
//"Editors/text/x-whatever/FontsColors/NetBeans/org-netbeans-modules-editor-settings-CustomFontsColors.xml"
        
        FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        scan(ColoringStorage.ID, baseFolder, "text/x-whatever", null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, writableUserFile, profileFiles, "NetBeans");
    }
    
    public void testFullKeybindingsLegacyLayout() throws Exception {
        String [] files = new String [] {
            "Editors/text/base/Defaults/keybindings.xml",
            "Editors/Keybindings/NetBeans/Defaults/zz.xml",
            "Editors/Keybindings/NetBeans/Defaults/dd.xml",
            "Editors/Keybindings/NetBeans/Defaults/kk.xml",
            "Editors/Keybindings/NetBeans/Defaults/aa.xml",
            "Editors/text/base/keybindings.xml",
            "Editors/Keybindings/NetBeans/papap.xml",
            "Editors/Keybindings/NetBeans/kekeke.xml",
            "Editors/Keybindings/NetBeans/dhdhdddd.xml",
        };
        String writableUserFile = "Editors/" + getWritableFileName(KeyMapsStorage.ID, null, "NetBeans", null, false);
        
        createOrderedFiles(files, KB_CONTENTS);
        TestUtilities.createFile(writableUserFile, KB_CONTENTS);
        orderFiles("Editors/Keybindings/NetBeans/dhdhdddd.xml", writableUserFile);
        
        FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        scan(KeyMapsStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, "Editors/Keybindings/NetBeans/org-netbeans-modules-editor-settings-CustomKeybindings.xml", profileFiles, "NetBeans");
    }

    public void testFullKeybindingsMixedLayout() throws Exception {
        String [] files = new String [] {
            "Editors/text/base/Defaults/keybindings.xml",
            "Editors/text/base/keybindings.xml",
        };
        
        createOrderedFiles(files, KB_CONTENTS);
        
        FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        scan(KeyMapsStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, null, profileFiles, "NetBeans");
    }

    public static void checkProfileFiles(String [] paths, String writablePath, List<Object []> files, String profileId) {
        assertNotNull(profileId + ": No files", files);
        assertEquals(profileId + ": Wrong number of files", 
            writablePath != null ? paths.length + 1 : paths.length, files.size());
        
        for(int i = 0; i < paths.length; i++) {
            FileObject profileHome = (FileObject) files.get(i)[0];
            FileObject settingFile = (FileObject) files.get(i)[1];
            boolean modulesFile = ((Boolean) files.get(i)[2]).booleanValue();
            
            assertEquals(profileId + ": wrong file", paths[i], settingFile.getPath());
        }
        
        if (writablePath != null) {
            FileObject profileHome = (FileObject) files.get(paths.length)[0];
            FileObject settingFile = (FileObject) files.get(paths.length)[1];
            boolean modulesFile = ((Boolean) files.get(paths.length)[2]).booleanValue();

            assertEquals(profileId + ": wrong writable file", writablePath, settingFile.getPath());
        }
    }
    
    public static void createOrderedFiles(String [] files, String contents) throws IOException {
        for(int i = 0; i < files.length; i++) {
            FileObject f = TestUtilities.createFile(files[i], contents);
            if (i + 1 < files.length) {
                String [] thisFile = getNameParts(files[i]);
                String [] nextFile = getNameParts(files[i + 1]);

                if (thisFile[0].equals(nextFile[0])) {
                    String ordering = thisFile[1] + "/" + nextFile[1];
                    f.getParent().setAttribute(ordering, Boolean.TRUE);
                }
            }
        }
    }
    
    private static String [] getNameParts(String path) {
        int idx = path.lastIndexOf('/');
        if (idx != -1) {
            return new String [] { path.substring(0, idx), path.substring(idx + 1) };
        } else {
            return new String [] { "", path };
        }
    }
    
    public static void orderFiles(String pathA, String pathB) throws IOException {
        FileObject fileA = Repository.getDefault().getDefaultFileSystem().findResource(pathA);
        FileObject fileB = Repository.getDefault().getDefaultFileSystem().findResource(pathB);
        assertNotNull("Can't find file '" + pathA + "'", fileA);
        assertNotNull("Can't find file '" + pathB + "'", fileB);
        
        FileObject parentA = fileA.getParent();
        FileObject parentB = fileB.getParent();
        assertSame("Can't order files from different folder.", parentA, parentB);
        
        String ordering = fileA.getNameExt() + "/" + fileB.getNameExt(); //NOI18N
        parentA.setAttribute(ordering, Boolean.TRUE);
    }
    
    private String getCurrentOsId() {
        int osId = Utilities.getOperatingSystem();
        for(Field field : Utilities.class.getDeclaredFields()) {
            try {
                int value = field.getInt(null);
                if (value == osId) {
                    return field.getName();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        fail("Can't detect OS type ");
        return null; // not reachable
    }
    
    public static void scan(String settingsTypeId, FileObject baseFolder, String mimeType, String profileId, boolean fullScan, boolean scanModules, boolean scanUsers, Map<String, List<Object []>> results) {
        SettingsType.Locator l = SettingsType.getLocator(SettingsType.find(settingsTypeId));
        assertNotNull("Can't find locator for '" + settingsTypeId + "'");
        l.scan(baseFolder, mimeType, profileId, fullScan, scanModules, scanUsers, results);
    }
    
    public static String getWritableFileName(String settingsTypeId, String mimeType, String profileId, String fileId, boolean modulesFile) {
        SettingsType.Locator l = SettingsType.getLocator(SettingsType.find(settingsTypeId));
        assertNotNull("Can't find locator for '" + settingsTypeId + "'");
        return l.getWritableFileName(mimeType, profileId, fileId, modulesFile);
    }
}
