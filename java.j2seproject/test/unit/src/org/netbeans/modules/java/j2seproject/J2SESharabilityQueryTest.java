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

import java.io.File;
import org.netbeans.api.project.ProjectUtils;
import org.openide.modules.SpecificationVersion;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

public class J2SESharabilityQueryTest extends NbTestCase {

    public J2SESharabilityQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject dist;
    private FileObject build;
    private ProjectManager pm;
    private J2SEProject pp;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
        dist = FileUtil.createFolder(projdir,"dist");
        build = FileUtil.createFolder(projdir,"build");
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

    public void testSharability () throws Exception {
        File f = FileUtil.toFile (this.sources);
        int res = SharabilityQuery.getSharability(f);
        assertEquals("Sources must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.tests);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Tests must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.build);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Build can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        f = FileUtil.toFile (this.dist);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Dist can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        FileObject newSourceRoot = addSourceRoot(helper, projdir, "src2.dir",new File(FileUtil.toFile(scratch),"sources2"));
        ProjectUtils.getSources(pp).getSourceGroups(Sources.TYPE_GENERIC);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be sharable", SharabilityQuery.SHARABLE, res);
        FileObject newSourceRoot2 = changeSourceRoot(helper, projdir, "src2.dir", new File(FileUtil.toFile(scratch),"sources3"));
        f = FileUtil.toFile (newSourceRoot2);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources3 must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be unknown", SharabilityQuery.UNKNOWN, res);
    }

    public static FileObject addSourceRoot (AntProjectHelper helper, FileObject projdir,
                                            String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttributeNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id",propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

    public static FileObject changeSourceRoot (AntProjectHelper helper, FileObject projdir,
                                               String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

}
