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

import org.netbeans.modules.editor.settings.storage.keybindings.KeyBindingSettingsImpl;
import org.netbeans.modules.editor.settings.storage.fontscolors.FontColorSettingsImpl;
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collection;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class SettingsProviderTest extends NbTestCase {
    
    /** Creates a new instance of SettingsProviderTest */
    public SettingsProviderTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/test-layer.xml"),
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
    
    public void testSpecialTestMimeType() throws Exception {
        final String origMimeType = "text/x-orig";
        final String specialTestMimeType = "test123456_" + origMimeType;
        Marker origMarker;
        
        {
            Lookup origLookup = MimeLookup.getLookup(MimePath.parse(origMimeType));
            Collection<? extends Marker> markers = origLookup.lookupAll(Marker.class);
            assertEquals("Wrong number of orig markers", 1, markers.size());
            
            origMarker = markers.iterator().next();
            assertNotNull("Orig marker is null", origMarker);
            assertEquals("Wrong orig marker", 
                "Editors/text/x-orig/marker.instance", 
                origMarker.getHome().getPath());
        }
        
        {
            Lookup testLookup = MimeLookup.getLookup(MimePath.parse(specialTestMimeType));
            Collection<? extends Marker> markers = testLookup.lookupAll(Marker.class);
            assertEquals("Wrong number of test markers", 1, markers.size());
            
            Marker testMarker = markers.iterator().next();
            assertNotNull("Test marker is null", testMarker);
            assertEquals("Wrong test marker", 
                "Editors/text/x-orig/marker.instance", 
                testMarker.getHome().getPath());
            
            assertSame("Test marker and orig marker should be the same", origMarker, testMarker);
        }
    }

    public void testLookupsCached() {
        FontColorSettings fcs1 = MimeLookup.getLookup(MimePath.parse("text/x-type-A")).lookup(FontColorSettings.class);
        FontColorSettings fcs2 = MimeLookup.getLookup(MimePath.parse("text/x-type-A")).lookup(FontColorSettings.class);
        
        assertSame("FontColorSettings instances should be the same", fcs1, fcs2);
    }

    public void testLookupsGCedAfterFcs() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        FontColorSettingsImpl fcsi = FontColorSettingsImpl.get(mimePath);
        Lookup lookup = MimeLookup.getLookup(mimePath);
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);

        WeakReference<FontColorSettingsImpl> fcsiRef = new WeakReference<FontColorSettingsImpl>(fcsi);
        WeakReference<MimePath> mimePathRef = new WeakReference<MimePath>(mimePath);
        WeakReference<Lookup> lookupRef = new WeakReference<Lookup>(lookup);
        WeakReference<FontColorSettings> fcsRef = new WeakReference<FontColorSettings>(fcs);
    
        fcsi = null;
        mimePath = null;
        lookup = null;
        fcs = null;
        
        // release text/x-type-A from MimePath's LRU
        for(int i = 0; i < 10; i++) {
            MimePath.parse("text/x-type-" + ('Z' + i));
        }
        
        assertGC("FCSI hasn't been GCed", fcsiRef);
        assertGC("MimePath hasn't been GCed", mimePathRef);
        assertGC("Lookup hasn't been GCed", lookupRef);
        assertGC("FCS hasn't been GCed", fcsRef);
    }

    public void testLookupsGCedAfterKbs() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        KeyBindingSettingsImpl kbsi = KeyBindingSettingsImpl.get(mimePath);
        Lookup lookup = MimeLookup.getLookup(mimePath);
        KeyBindingSettings kbs = lookup.lookup(KeyBindingSettings.class);

        WeakReference<KeyBindingSettingsImpl> kbsiRef = new WeakReference<KeyBindingSettingsImpl>(kbsi);
        WeakReference<MimePath> mimePathRef = new WeakReference<MimePath>(mimePath);
        WeakReference<Lookup> lookupRef = new WeakReference<Lookup>(lookup);
        WeakReference<KeyBindingSettings> kbsRef = new WeakReference<KeyBindingSettings>(kbs);
    
        kbsi = null;
        mimePath = null;
        lookup = null;
        kbs = null;
        
        // release text/x-type-A from MimePath's LRU
        for(int i = 0; i < 10; i++) {
            MimePath.parse("text/x-type-" + ('Z' + i));
        }
        
        assertGC("KBSI hasn't been GCed", kbsiRef);
        assertGC("MimePath hasn't been GCed", mimePathRef);
        assertGC("Lookup hasn't been GCed", lookupRef);
        assertGC("KBS hasn't been GCed", kbsRef);
    }

    public void testInheritanceForAntPlusXml() {
        MimePath mimePath = MimePath.parse("text/ant+xml");
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        
        AttributeSet antXmlAttribs = fcs.getTokenFontColors("test-inheritance-ant-xml");
        assertNotNull("Can't find coloring defined for text/ant+xml", antXmlAttribs);
        assertEquals("Wrong bgColor in coloring defined for text/ant+xml", new Color(0xAA0000), antXmlAttribs.getAttribute(StyleConstants.Background));
        
        AttributeSet xmlAttribs = fcs.getTokenFontColors("test-inheritance-xml");
        assertNotNull("Can't find coloring defined for text/xml", xmlAttribs);
        assertEquals("Wrong bgColor in coloring defined for text/xml", new Color(0x00BB00), xmlAttribs.getAttribute(StyleConstants.Background));
        
        AttributeSet attribs = fcs.getTokenFontColors("test-all-languages-super-default");
        assertNotNull("Can't find coloring defined for root", attribs);
        assertEquals("Wrong bgColor in coloring defined for root", new Color(0xABCDEF), attribs.getAttribute(StyleConstants.Background));
    }

    public void testInheritanceForEmbedded() {
        MimePath mimePath = MimePath.parse("text/x-type-B/text/x-type-A");
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        
        AttributeSet attribsTypeA = fcs.getTokenFontColors("test-inheritance-typeA-specific");
        assertNotNull("Can't find coloring defined for text/x-type-A", attribsTypeA);
        assertEquals("Wrong bgColor in coloring defined for text/x-type-A", new Color(0xAA0000), attribsTypeA.getAttribute(StyleConstants.Background));
        
        AttributeSet attribsTypeB = fcs.getTokenFontColors("test-inheritance-typeB-specific");
        assertNull("Shouldn't find coloring defined for text/x-type-B", attribsTypeB);
//        assertEquals("Wrong bgColor in coloring defined for text/x-type-B", new Color(0xBB0000), attribsTypeB.getAttribute(StyleConstants.Background));
        
        AttributeSet attribsBoth = fcs.getTokenFontColors("test-inheritance-typeA-typeB");
        assertNotNull("Can't find coloring defined for both typeA and typeB", attribsBoth);
        assertEquals("Wrong bgColor in coloring defined for both typeA typeB", new Color(0xAAAA00), attribsBoth.getAttribute(StyleConstants.Background));

        AttributeSet attribsEmbedded = fcs.getTokenFontColors("test-inheritance-typeA-embedded-in-typeB");
        assertNotNull("Can't find coloring defined for typeA embedded in typeB", attribsEmbedded);
        assertEquals("Wrong bgColor in coloring defined for typeA embedded in typeB", new Color(0xAABB00), attribsEmbedded.getAttribute(StyleConstants.Background));
    }
    
    public static Marker createMarker(FileObject f) {
        return new Marker(f);
    }
    
    public static final class Marker {
        
        private FileObject home;
        
        public Marker(FileObject home) {
            this.home = home;
        }
        
        public FileObject getHome() {
            return home;
        }
    } // End of Marker class
    
}
