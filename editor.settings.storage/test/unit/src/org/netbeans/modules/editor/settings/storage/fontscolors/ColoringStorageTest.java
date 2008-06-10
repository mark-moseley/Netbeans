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
package org.netbeans.modules.editor.settings.storage.fontscolors;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author Vita Stejskal
 */
public class ColoringStorageTest extends NbTestCase {
    
    /** Creates a new instance of ColoringStorageTest */
    public ColoringStorageTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/fontscolors/test-layer-ColoringStorageTest.xml"),
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

    public void testAllLanguages() throws IOException {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        Map<String, AttributeSet> colorings = ess.load(MimePath.EMPTY, "MyProfileXyz", true); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 1, colorings.size());
        
        AttributeSet c = colorings.get("test-all-languages-super-default");
        assertNotNull("Should have test-all-languages-super-default coloring", c);
        assertEquals("Wrong bgColor", new Color(0xABCDEF), c.getAttribute(StyleConstants.Background));
    }

    public void testAllLanguages2() throws IOException {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        Map<String, AttributeSet> colorings = ess.load(MimePath.EMPTY, "MyProfileXyz", false); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 1, colorings.size());
        
        AttributeSet c = colorings.get("test-all-languages-super-default");
        assertNotNull("Should have test-all-languages-super-default coloring", c);
        assertEquals("Wrong bgColor", new Color(0xABCDEF), c.getAttribute(StyleConstants.Background));
    }
    
    public void testAllLanguagesHighlights() {
        ColoringStorage cs = new ColoringStorage(false);
        Map<String, AttributeSet> colorings = cs.load(MimePath.EMPTY, "MyProfileXyz", true); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 1, colorings.size());
        
        AttributeSet c = colorings.get("test-text-limit-line");
        assertNotNull("Should have test-text-limit-line coloring", c);
        assertEquals("Wrong bgColor", new Color(0x010101), c.getAttribute(StyleConstants.Foreground));
    }
    
    public void testAllLanguagesHighlights2() {
        ColoringStorage cs = new ColoringStorage(false);
        Map<String, AttributeSet> colorings = cs.load(MimePath.EMPTY, "MyProfileXyz", false); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 1, colorings.size());
        
        AttributeSet c = colorings.get("test-text-limit-line");
        assertNotNull("Should have test-text-limit-line coloring", c);
        assertEquals("Wrong bgColor", new Color(0x010101), c.getAttribute(StyleConstants.Foreground));
    }
    
    public void testMultipleFiles() throws IOException {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Map<String, AttributeSet> colorings = ess.load(mimePath, "MyProfileXyz", false); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 3, colorings.size());
        
        // Check coloring in module A only
        {
        AttributeSet c = colorings.get("module-A-coloring");
        assertNotNull("Should have module-A-coloring coloring", c);
        assertEquals("Wrong bgColor", new Color(0xAA0000), c.getAttribute(StyleConstants.Background));
        }
        {
        // Check coloring in module B only
        AttributeSet c = colorings.get("module-B-coloring");
        assertNotNull("Should have module-B-coloring coloring", c);
        assertEquals("Wrong bgColor", new Color(0xBB0000), c.getAttribute(StyleConstants.Background));
        }
        {
        // Check shared coloring
        AttributeSet c = colorings.get("both-modules-coloring");
        assertNotNull("Should have both-modules-coloring coloring", c);
        assertEquals("Wrong bgColor", new Color(0xAA0000), c.getAttribute(StyleConstants.Background));
        assertEquals("Wrong foreColor", new Color(0x00BB00), c.getAttribute(StyleConstants.Foreground));
        assertEquals("Wrong underline", new Color(0x0000BB), c.getAttribute(StyleConstants.Underline));
        }
    }
    
    public void testDeleteFiles() throws IOException {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        MimePath mimePath = MimePath.parse("text/x-type-A");
        ess.delete(mimePath, "MyProfileXyz", true);
        
        FileObject profileHome = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/x-type-A/FontsColors/MyProfileXyz/Defaults");
        assertNotNull("Can't find profileHome", profileHome);
        
        FileObject [] files = profileHome.getChildren();
        assertEquals("There should be no files", 0, files.length);
        
        Map<String, AttributeSet> colorings = ess.load(mimePath, "MyProfileXyz", true); //NOI18N
        assertNotNull("There still should be colorings map", colorings);
        assertEquals("Colorings map should be empty", 0, colorings.size());
    }
    
    public void testWriteColorings() throws IOException {
        final String name1 = "new-coloring";
        final Color color1 = new Color(0x1F2F3F);
        final String name2 = "both-modules-coloring";
        final Color color2 = new Color(0x1F2F3F);
        
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Map<String, AttributeSet> newColorings = new HashMap<String, AttributeSet>();
        newColorings.put(name1, AttributesUtilities.createImmutable(StyleConstants.Underline, color1, StyleConstants.NameAttribute, name1));
        newColorings.put(name2, AttributesUtilities.createImmutable(StyleConstants.StrikeThrough, color2, StyleConstants.NameAttribute, name2));
        
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        ess.save(mimePath, "MyProfileXyz", false, newColorings);
        
        FileObject settingFile = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/x-type-A/FontsColors/MyProfileXyz/org-netbeans-modules-editor-settings-CustomFontsColors-tokenColorings.xml");
        assertNotNull("Can't find custom settingFile", settingFile);
        assertEquals("Wrong mime type", ColoringStorage.MIME_TYPE, settingFile.getMIMEType());
        
        Map<String, AttributeSet> colorings = ess.load(mimePath, "MyProfileXyz", false); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 3, colorings.size());

        AttributeSet c = colorings.get(name1);
        assertNull("Coloring '" + name1 + "' not defined by modules should be ignored", c);
        
        AttributeSet c2 = colorings.get(name2);
        assertNotNull("Should have " + name2 + " coloring", c2);
        assertEquals("Wrong number of attributes", 3, c2.getAttributeCount());
        assertEquals("Wrong strikeThrough", color2, c2.getAttribute(StyleConstants.StrikeThrough));
        assertEquals("Wrong NameAttribute", name2, c2.getAttribute(StyleConstants.NameAttribute));
        assertEquals("Wrong DisplayName", name2, c2.getAttribute(EditorStyleConstants.DisplayName));
    }
    
    public void testLegacyFilesWithNoDTD_Issue113137() {
        ColoringStorage cs = new ColoringStorage(true);
        Map<String, AttributeSet> colorings = cs.load(MimePath.parse("text/x-legacy"), "NetBeans", false); //NOI18N
        assertNotNull("Colorings map should not be null", colorings);
        assertEquals("Wrong number of colorings", 2, colorings.size());
        {
        AttributeSet c = colorings.get("pp-active-block");
        assertNotNull("Should have pp-active-block coloring", c);
        assertEquals("Wrong bgColor", new Color(0xfffae1f0), c.getAttribute(StyleConstants.Background));
        }
        {
        AttributeSet c = colorings.get("pp-inactive-block");
        assertNotNull("Should have pp-inactive-block coloring", c);
        assertEquals("Wrong bgColor", new Color(0xffebe1fa), c.getAttribute(StyleConstants.Background));
        }
    }
}
