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

package org.netbeans.modules.editor.mimelookup.impl;

import java.util.Collection;
import javax.swing.JPanel;
import junit.framework.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vita
 */
public class SwitchLookupTest extends NbTestCase {

    /** Creates a new instance of FolderPathLookupTest */
    public SwitchLookupTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        //return new SwitchLookupTest("testSimpleWhileSomeOneElseHoldsAWTLock");
        return new NbTestSuite(SwitchLookupTest.class);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
    }
    
    protected @Override void tearDown() {
        TestUtilities.gc();
    }
    
    public void testSimple() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Creating lookup for an existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Collection instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Now create lookup over a non-existing mime path
        lookup = new SwitchLookup(MimePath.parse("text/xml"));
        instances = lookup.lookupAll(Object.class);
        
        assertEquals("Wrong number of instances", 0, instances.size());
    }
    
    public void testAddingMimePath() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        // Create lookup over a non-existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();

        result.addLookupListener(listener);
        Collection instances = result.allInstances();
        
        assertEquals("There should be no change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Lookup the instances again
        instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    public void testRemovingMimePath() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Create lookup over an existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();
        
        result.addLookupListener(listener);
        Collection instances = result.allInstances();

        assertEquals("There should be no change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Delete the mime path folder
        TestUtilities.deleteFile(getWorkDir(), "Editors/text/x-jsp/text");
        TestUtilities.sleepForWhile();

        // Lookup the instances again
        instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    // test hierarchy - instances in lower levels are not visible in higher levels,
    // but instances from higher levels are visible in lower levels
    
    public void testHierarchyInheritance() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/");
        TestUtilities.sleepForWhile();

        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, javaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, javaInstances.iterator().next().getClass());
        }
        
        {
            Lookup jspJavaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection jspJavaInstances = jspJavaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspJavaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspJavaInstances.iterator().next().getClass());
    }
    }

    public void testHierarchyRootInheritance() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/");
        TestUtilities.createFile(getWorkDir(), "Editors/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        {
            Lookup lookup = new SwitchLookup(MimePath.parse(""));
            Collection instances = lookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, instances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        }
        
        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspInstances.iterator().next().getClass());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, javaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, javaInstances.iterator().next().getClass());
        }
    }
    
    public void testHierarchyLeaks() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/");
        TestUtilities.sleepForWhile();

        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, jspInstances.size());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, javaInstances.size());
        }
        
        {
            Lookup jspJavaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection jspJavaInstances = jspJavaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspJavaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspJavaInstances.iterator().next().getClass());
        }

        {
            Lookup javaJspLookup = new SwitchLookup(MimePath.parse("text/x-java/text/x-jsp"));
            Collection javaJspInstances = javaJspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, javaJspInstances.size());
        }
    }
    
    // test that FolderPathLookups are shared and discarded when they are not needed anymore
    
    // test that instances of a class with a Class2LayerFolder provider are really read from the proper folder
    
    public void testReadFromSpecialFolders() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/DummyFolder/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();

        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Collection instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    // test that adding/removing a Class2LayerFolder provider updates the lookup for its class
    
    public void testChangeInMappers() throws Exception {
        if (Boolean.getBoolean("ignore.random.failures")) {
            return;
        }

        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/DummyFolder/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();
        
        result.addLookupListener(listener);
        Collection instances = result.allInstances();
        
        assertEquals("Wrong number of change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());

        // Add the mapper
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();

        instances = result.allInstances();
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Reset the listener
        listener.resultChangedCnt = 0;
        
        // Remove the mapper
        TestUtilities.deleteFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();

        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        instances = result.allInstances();
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    private static final class L implements LookupListener {
        public int resultChangedCnt = 0;
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    }
}
