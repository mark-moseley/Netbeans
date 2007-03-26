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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author Vita Stejskal
 */
public class ProfilesTrackerTest extends NbTestCase {

    // This is also in Bundle.properties
    private static final String BASE = "Xyz/XoX/abc";

    // XXX: This is here just for junit to match the test names in the results properly
    private SettingsType type = (SettingsType) Suite.ENV[0][2];
    private String folder = (String) Suite.ENV[0][1];
    private String contents = (String) Suite.ENV[0][0];
    
    public static Test suite() {
        return new Suite(ProfilesTrackerTest.class);
    }
    
    /** Creates a new instance of MimeTypesTrackerTest */
    public ProfilesTrackerTest(String name) {
        super(name);
    }

    private void setEnv(SettingsType type, String folder, String contents) {
        this.type = type;
        this.folder = folder;
        this.contents = contents;
    }
    
    public String getName() {
        return super.getName() + "(" + type + ")";
    }
    
    protected void setUp() throws Exception {
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
    
    protected void tearDown() {
        assertGC("Perform GC", new WeakReference<Object>(new Object()));
    }
    
    public void testBasic() throws Exception {
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileA/abc.xml", contents);
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        Set<String> profiles = pt.getProfilesDisplayNames();
        
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "ProfileA", profiles.iterator().next());
    }

    public void testBasicRoot() throws Exception {
        TestUtilities.createFile(BASE + "/" + folder + "/ProfileA/abc.xml", contents);
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        Set<String> profiles = pt.getProfilesDisplayNames();
        
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "ProfileA", profiles.iterator().next());
    }

    public void testSeveralMimes() throws Exception {
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileA/abc.xml", contents);
        TestUtilities.createFile(BASE + "/text/x-java/" + folder + "/ProfileA/abc.xml", contents);
        TestUtilities.createFile(BASE + "/text/xml/" + folder + "/ProfileA/abc.xml", contents);
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileB/abc.xml", contents);
        TestUtilities.createFile(BASE + "/text/xml/" + folder + "/ProfileB/abc.xml", contents);
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        Set<String> profiles = pt.getProfilesDisplayNames();
        
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 2, profiles.size());
        assertTrue("No 'ProfileA'", profiles.contains("ProfileA"));
        assertTrue("No 'ProfileB'", profiles.contains("ProfileB"));
    }
    
    public void testDisplayName() throws Exception {
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileA/abc.xml", contents);
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource(BASE + "/text/plain/" + folder + "/ProfileA");
        f.setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.editor.settings.storage.Bundle");
        
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        Set<String> profiles = pt.getProfilesDisplayNames();
        
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "Nice Display Name Of Profile A", profiles.iterator().next());
    }

    public void testRollbackIndicator() throws Exception {
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));

        {
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileA/abc.xml", contents);
        ProfilesTracker.ProfileDescription pd = pt.getProfileByDisplayName("ProfileA");
        assertNotNull("ProfileDescription should not be null", pd);
        assertFalse("Wrong isRollbackAllowed value", pd.isRollbackAllowed());
        }
        
        {
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileA/Defaults/abc.xml", contents);
        ProfilesTracker.ProfileDescription pd = pt.getProfileByDisplayName("ProfileA");
        assertNotNull("ProfileDescription should not be null", pd);
        assertTrue("Wrong isRollbackAllowed value", pd.isRollbackAllowed());
        }
    }
    
    public void testGC() throws Exception {
        TestUtilities.createFile(BASE + "/text/plain/" + folder + "/ProfileA/abc.xml", contents);
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        Set<String> profiles = pt.getProfilesDisplayNames();
        
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "ProfileA", profiles.iterator().next());
        
        WeakReference<ProfilesTracker> ref = new WeakReference<ProfilesTracker>(pt);
        pt = null;
        assertGC("Can't GC the tracker", ref);
    }

    public void testRecognition() throws Exception {
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        
        {
        TestUtilities.createFolder(BASE);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        }
        {
        TestUtilities.createFolder(BASE + "/text");
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        }
        {
        TestUtilities.createFolder(BASE + "/text/x-jsp");
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        }
        {
        TestUtilities.createFolder(BASE + "/text/x-jsp/FontsColors");
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        }
        {
        TestUtilities.createFile(BASE + "/text/x-jsp/" + folder + "/MyProfile/abc.xml", contents);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "MyProfile", profiles.iterator().next());
        }
    }

    public void testRecognition2() throws Exception {
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        
        {
        TestUtilities.createFile(BASE + "/text/x-jsp/" + folder + "/MyProfile/abc.xml", contents);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "MyProfile", profiles.iterator().next());
        }
        {
        TestUtilities.delete(BASE + "/text/x-jsp/" + folder + "/MyProfile/abc.xml");
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        }
        {
        TestUtilities.createFile(BASE + "/text/x-jsp/" + folder + "/MyProfile/abc.xml", contents);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "MyProfile", profiles.iterator().next());
        }
        {
        TestUtilities.delete(BASE);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        }
    }
    
    public void testEvents() throws Exception {
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        L listener = new L();
        
        pt.addPropertyChangeListener(listener);
        {
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        assertEquals("Wrong # of events", 0, listener.events);
        }
        
        {
        TestUtilities.createFile(BASE + "/text/x-jsp/" + folder + "/MyProfile/abc.xml", contents);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "MyProfile", profiles.iterator().next());
        assertEquals("Wrong # of events", 1, listener.events);
        assertEquals("Wrong change event name", ProfilesTracker.PROP_PROFILES, listener.lastEventName);
        assertTrue("Wrong change event old value", listener.lastEventOldValue instanceof Map);
        assertEquals("Wrong change event old value contents", 0, ((Map) listener.lastEventOldValue).size());
        assertTrue("Wrong change event new value", listener.lastEventNewValue instanceof Map);
        assertEquals("Wrong change event new value contents", 1, ((Map) listener.lastEventNewValue).size());
        assertEquals("Wrong change event new value profile", "MyProfile", ((Map) listener.lastEventNewValue).keySet().iterator().next());
        }
        
        {
        listener.reset();
        TestUtilities.delete(BASE);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        assertEquals("Wrong # of events", 1, listener.events);
        assertEquals("Wrong change event name", ProfilesTracker.PROP_PROFILES, listener.lastEventName);
        assertTrue("Wrong change event old value", listener.lastEventOldValue instanceof Map);
        assertEquals("Wrong change event old value contents", 1, ((Map) listener.lastEventOldValue).size());
        assertEquals("Wrong change event new value profile", "MyProfile", ((Map) listener.lastEventOldValue).keySet().iterator().next());
        assertTrue("Wrong change event new value", listener.lastEventNewValue instanceof Map);
        assertEquals("Wrong change event new value contents", 0, ((Map) listener.lastEventNewValue).size());
        }
    }
    
    public void testEventsRoot() throws Exception {
        ProfilesTracker pt = new ProfilesTracker(type, new MimeTypesTracker(BASE, null));
        L listener = new L();
        
        pt.addPropertyChangeListener(listener);
        {
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        assertEquals("Wrong # of events", 0, listener.events);
        }
        
        {
        TestUtilities.createFile(BASE + "/" + folder + "/MyProfile/abc.xml", contents);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 1, profiles.size());
        assertEquals("Wrong profile display name", "MyProfile", profiles.iterator().next());
        assertEquals("Wrong # of events", 1, listener.events);
        assertEquals("Wrong change event name", ProfilesTracker.PROP_PROFILES, listener.lastEventName);
        assertTrue("Wrong change event old value", listener.lastEventOldValue instanceof Map);
        assertEquals("Wrong change event old value contents", 0, ((Map) listener.lastEventOldValue).size());
        assertTrue("Wrong change event new value", listener.lastEventNewValue instanceof Map);
        assertEquals("Wrong change event new value contents", 1, ((Map) listener.lastEventNewValue).size());
        assertEquals("Wrong change event new value profile", "MyProfile", ((Map) listener.lastEventNewValue).keySet().iterator().next());
        }
        
        {
        listener.reset();
        TestUtilities.delete(BASE);
        Set<String> profiles = pt.getProfilesDisplayNames();
        assertNotNull("Profiles should not be null", profiles);
        assertEquals("Wrong # of recognized profiles", 0, profiles.size());
        assertEquals("Wrong # of events", 1, listener.events);
        assertEquals("Wrong change event name", ProfilesTracker.PROP_PROFILES, listener.lastEventName);
        assertTrue("Wrong change event old value", listener.lastEventOldValue instanceof Map);
        assertEquals("Wrong change event old value contents", 1, ((Map) listener.lastEventOldValue).size());
        assertEquals("Wrong change event new value profile", "MyProfile", ((Map) listener.lastEventOldValue).keySet().iterator().next());
        assertTrue("Wrong change event new value", listener.lastEventNewValue instanceof Map);
        assertEquals("Wrong change event new value contents", 0, ((Map) listener.lastEventNewValue).size());
        }
    }
    
    private static final class L implements PropertyChangeListener {
        public int events;
        public String lastEventName;
        public Object lastEventOldValue;
        public Object lastEventNewValue;
        
        public void propertyChange(PropertyChangeEvent evt) {
            events++;
            lastEventName = evt.getPropertyName();
            lastEventOldValue = evt.getOldValue();
            lastEventNewValue = evt.getNewValue();
        }
        
        public void reset() {
            events = 0;
            lastEventName = null;
            lastEventOldValue = null;
            lastEventNewValue = null;
        }
    }
    
    private static final class Suite extends NbTestSuite {
        
        public static final Object [][] ENV = {
            new Object [] {
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE fontscolors PUBLIC \"-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN\" \"http://www.netbeans.org/dtds/EditorFontsColors-1_1.dtd\">\n" +
                "<fontscolors></fontscolors>",
                "FontsColors",
                SettingsType.FONTSCOLORS
            },
            new Object [] {
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE bindings PUBLIC \"-//NetBeans//DTD Editor KeyBindings settings 1.1//EN\" \"http://www.netbeans.org/dtds/EditorKeyBindings-1_1.dtd\">\n" +
                "<bindings></bindings>",
                "Keybindings",
                SettingsType.KEYBINDINGS
            },
        };
        
        public Suite(Class klass) {
            super(klass);
        }
        
        @Override
        public void run(TestResult result) {
            for(int i = 0; i < ENV.length; i++) {
                String contents = (String) ENV[i][0];
                String folder = (String) ENV[i][1];
                SettingsType type = (SettingsType) ENV[i][2];
                
                for(int j = 0; j < testCount(); j++) {
                    Test test = testAt(j);
                    if (test instanceof ProfilesTrackerTest) {
                        ((ProfilesTrackerTest) test).setEnv(type, folder, contents);
                    }
                }

                System.out.println("Running tests for: " + type);
                super.run(result);
            }
        }
    }
}
