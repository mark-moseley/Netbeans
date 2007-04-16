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

package org.netbeans.modules.apisupport.project.queries;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Test AntArtifactProviderImpl.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class AntArtifactProviderImplTest extends TestBase {
    
    public AntArtifactProviderImplTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        FileObject dir = nbCVSRoot().getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        javaProjectProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        dir = nbCVSRoot().getFileObject("openide/loaders");
        assertNotNull("have openide/loaders checked out", dir);
        loadersProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
    }
    
    public void testJARFileIsProduced() throws Exception {
        AntArtifact[] arts = AntArtifactQuery.findArtifactsByType(loadersProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct project", loadersProject, arts[0].getProject());
        assertEquals("correct type", JavaProjectConstants.ARTIFACT_TYPE_JAR, arts[0].getType());
        assertEquals("correct ID", "module", arts[0].getID());
        assertEquals("correct location",
            Collections.singletonList(URI.create("../../nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
        assertEquals("correct script", nbCVSRoot().getFileObject("openide/loaders/build.xml"), arts[0].getScriptFile());
        assertEquals("correct build target", "netbeans", arts[0].getTargetName());
        assertEquals("correct clean target", "clean", arts[0].getCleanTargetName());
        assertEquals("no properties", new Properties(), arts[0].getProperties());
        arts = AntArtifactQuery.findArtifactsByType(javaProjectProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct location",
            Collections.singletonList(URI.create("../../nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
    }
    
}
