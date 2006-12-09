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

package org.netbeans.modules.java.j2seproject;

import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

public class SourceRootsTest extends NbTestCase {

    public SourceRootsTest (String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private ProjectManager pm;
    private J2SEProject pp;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);   //NOI18N
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
        pm = ProjectManager.getDefault();
        Project p = pm.findProject(projdir);
        assertTrue("Invalid project type",p instanceof J2SEProject);
        pp = (J2SEProject) p;
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        tests = null;
        pm = null;
        pp = null;
        helper = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testSourceRoots () throws Exception {
        SourceRoots sources = pp.getSourceRoots();
        String[] srcProps = sources.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 1",1,srcProps.length);
        assertEquals("Source property should be src.dir","src.dir",srcProps[0]);
        FileObject[] srcFos = sources.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 1",1,srcFos.length);
        assertEquals("Root should be "+this.sources.getPath(),this.sources,srcFos[0]);
        URL[] srcURLs = sources.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 1",1,srcURLs.length);
        assertEquals("Root URLs should be "+this.sources.getURL(),this.sources.getURL(),srcURLs[0]);
        SourceRoots tests = pp.getTestSourceRoots();
        srcProps = tests.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 1",1,srcProps.length);
        assertEquals("Source property should be test.src.dir","test.src.dir",srcProps[0]);
        srcFos = tests.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 1",1,srcFos.length);
        assertEquals("Root should be "+this.tests.getPath(),this.tests,srcFos[0]);
        srcURLs = tests.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 1",1,srcURLs.length);
        assertEquals("Root URLs should be "+this.tests.getURL(),this.tests.getURL(),srcURLs[0]);
        //Now add new source root
        TestListener tl = new TestListener();
        sources.addPropertyChangeListener (tl);
        FileObject newRoot = addSourceRoot (helper, projdir, "src.other.dir","other");
        srcProps = sources.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 2",2,srcProps.length);
        assertEquals("The first source property should be src.dir","src.dir",srcProps[0]);
        assertEquals("The second source property should be src.other.dir","src.other.dir",srcProps[1]);
        srcFos = sources.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 2",2,srcFos.length);
        assertEquals("The first root should be "+this.sources.getPath(),this.sources,srcFos[0]);
        assertEquals("The second root should be "+newRoot.getPath(),newRoot,srcFos[1]);
        srcURLs = sources.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 2",2,srcURLs.length);
        assertEquals("The first root URLs should be "+this.sources.getURL(),this.sources.getURL(),srcURLs[0]);
        assertEquals("The second root URLs should be "+newRoot.getURL(),newRoot.getURL(),srcURLs[1]);
        Set events = tl.getEvents();
        assertTrue ("PROP_ROOT_PROPERTIES has to be fired",events.contains(SourceRoots.PROP_ROOT_PROPERTIES));
        assertTrue ("PROP_ROOTS has to be fired",events.contains(SourceRoots.PROP_ROOTS));
        tl.reset();
        newRoot = changeSourceRoot (helper, projdir, "src.other.dir","other2");
        srcProps = sources.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 2",2,srcProps.length);
        assertEquals("The first source property should be src.dir","src.dir",srcProps[0]);
        assertEquals("The second source property should be src.other.dir","src.other.dir",srcProps[1]);
        srcFos = sources.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 2",2,srcFos.length);
        assertEquals("The first root should be "+this.sources.getPath(),this.sources,srcFos[0]);
        assertEquals("The second root should be "+newRoot.getPath(),newRoot,srcFos[1]);
        srcURLs = sources.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 2",2,srcURLs.length);
        assertEquals("The first root URLs should be "+this.sources.getURL(),this.sources.getURL(),srcURLs[0]);
        assertEquals("The second root URLs should be "+newRoot.getURL(),newRoot.getURL(),srcURLs[1]);
        events = tl.getEvents();
        assertEquals(Collections.singleton(SourceRoots.PROP_ROOTS), events);
        sources.removePropertyChangeListener(tl);
    }

    public static FileObject addSourceRoot (AntProjectHelper helper, FileObject projdir,
                                            String propName, String folderName) throws Exception {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttribute("id", propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put (propName,folderName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        FileObject fo = projdir.getFileObject(folderName);
        if (fo==null) {
            fo = projdir.createFolder(folderName);
        }
        return fo;
    }

    public static FileObject changeSourceRoot (AntProjectHelper helper, FileObject projdir,
                                               String propName, String folderName) throws Exception {
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folderName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        FileObject fo = projdir.getFileObject(folderName);
        if (fo==null) {
            fo = projdir.createFolder(folderName);
        }
        return fo;
    }

    private static final class TestListener implements PropertyChangeListener {
        Set events = new HashSet ();

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (propName != null) {
                this.events.add (propName);
            }
        }

        public void reset () {
            this.events.clear();
        }

        public Set getEvents () {
            return Collections.unmodifiableSet(this.events);
        }
    }

}
