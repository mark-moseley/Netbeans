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

package org.netbeans.modules.ant.freeform;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test property evaluation.
 * @author Jesse Glick
 */
public class FreeformEvaluatorTest extends TestBase {
    
    public FreeformEvaluatorTest(String name) {
        super(name);
    }
    
    public void testPropertyEvaluation() throws Exception {
        PropertyEvaluator eval = simple.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
    }
    
    public void testPropertyEvaluationChanges() throws Exception {
        FreeformProject simple2 = copyProject(simple);
        PropertyEvaluator eval = simple2.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
        EditableProperties p = new EditableProperties();
        FileObject buildProperties = simple2.getProjectDirectory().getFileObject("build.properties");
        assertNotNull("have build.properties", buildProperties);
        InputStream is = buildProperties.getInputStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("right original value", "src", p.getProperty("src.dir"));
        p.setProperty("src.dir", "somethingnew");
        TestPCL l = new TestPCL();
        eval.addPropertyChangeListener(l);
        FileLock lock = buildProperties.lock();
        try {
            final OutputStream os = buildProperties.getOutputStream(lock);
            try {
                p.store(os);
            } finally {
                // close file under ProjectManager.readAccess so that events are fired synchronously
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                    public Void run() throws Exception {
                        os.close();
                        return null;
                    }
                });
            }
        } finally {
            lock.releaseLock();
        }
        assertEquals("got a change from properties file in src.dir", Collections.singleton("src.dir"), l.changed);
        l.reset();
        assertEquals("new value of src.dir", "somethingnew", eval.getProperty("src.dir"));
    }
    
    public void testChangesInPropertyFileLocation() throws Exception {
        // #48230: if some change in earlier properties causes location of a property file to change, reread it
        FreeformProject simple2 = copyProject(simple);
        // Replace <property name="build.properties">build.properties</property>
        // with <property-file>loc.properties</property-file> so that we can change it
        // without triggering a project.xml change (which always fires changes, so it is cheating).
        EditableProperties p = new EditableProperties();
        p.setProperty("build.properties", "build.properties");
        FileObject locProperties = simple2.getProjectDirectory().createData("loc.properties");
        FileLock lock = locProperties.lock();
        try {
            OutputStream os = locProperties.getOutputStream(lock);
            try {
                p.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
        Element data = simple2.helper().getPrimaryConfigurationData(true);
        NodeList propertiesNL = data.getElementsByTagNameNS(FreeformProjectType.NS_GENERAL, "properties");
        assertEquals("one <properties>", 1, propertiesNL.getLength());
        Element properties = (Element) propertiesNL.item(0);
        properties.removeChild(properties.getFirstChild());
        Element propertyFile = properties.getOwnerDocument().createElementNS(FreeformProjectType.NS_GENERAL, "property-file");
        propertyFile.appendChild(properties.getOwnerDocument().createTextNode("loc.properties"));
        properties.insertBefore(propertyFile, properties.getFirstChild());
        simple2.helper().putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(simple2);
        // Now check baseline evaluation.
        PropertyEvaluator eval = simple2.evaluator();
        TestPCL l = new TestPCL();
        eval.addPropertyChangeListener(l);
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
        // Make a build2.properties with a slight change.
        p = new EditableProperties();
        FileObject buildProperties = simple2.getProjectDirectory().getFileObject("build.properties");
        assertNotNull("have build.properties", buildProperties);
        InputStream is = buildProperties.getInputStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("right original value", "src", p.getProperty("src.dir"));
        p.setProperty("src.dir", "somethingnew");
        FileObject buildProperties2 = simple2.getProjectDirectory().createData("build2.properties");
        lock = buildProperties2.lock();
        try {
            OutputStream os = buildProperties2.getOutputStream(lock);
            try {
                p.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
        assertEquals("No changes fired yet", Collections.EMPTY_SET, l.changed);
        // Tell loc.properties to point to it.
        p = new EditableProperties();
        p.setProperty("build.properties", "build2.properties");
        locProperties = simple2.getProjectDirectory().getFileObject("loc.properties");
        lock = locProperties.lock();
        try {
            final OutputStream os = locProperties.getOutputStream(lock);
            try {
                p.store(os);
            } finally {
                // close file under ProjectManager.readAccess so that events are fired synchronously
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                    public Void run() throws Exception {
                        os.close();
                        return null;
                    }
                });
            }
        } finally {
            lock.releaseLock();
        }
        // Check that the change took.
        Set<String> exact = new HashSet<String>(Arrays.asList("src.dir", "build.properties"));
        // OK to just return null for the property name instead.
        assertTrue("got a change from properties file in src.dir: " + l.changed, l.changed.contains(null) || l.changed.equals(exact));
        l.reset();
        assertEquals("new value of src.dir", "somethingnew", eval.getProperty("src.dir"));
    }
    
}
