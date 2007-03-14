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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DefaultProjectCatalogSupport.java
 *
 * Created on December 18, 2006, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.catalogsupport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.xml.catalogsupport.util.ProjectReferenceUtility;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ajit
 */
public class DefaultProjectCatalogSupport extends ProjectCatalogSupport {
    
    public static final String NBURI_SCHEME = "nb-uri";
    
    public Project project;
    public AntProjectHelper helper;
    public ReferenceHelper refHelper;
    /**
     * Creates a new instance of DefaultProjectCatalogSupport
     */
    public DefaultProjectCatalogSupport(Project project) {
        this(project,null,null);
    }
    
    public DefaultProjectCatalogSupport(Project project, AntProjectHelper helper,
            ReferenceHelper refHelper) {
        this.project = project;
        this.helper = helper;
        this.refHelper = refHelper;
    }
    
    public static DefaultProjectCatalogSupport getInstance(FileObject source) {
        Project owner = FileOwnerQuery.getOwner(source);
        if(owner!=null) {
            DefaultProjectCatalogSupport support = (DefaultProjectCatalogSupport)owner.
                    getLookup().lookup(DefaultProjectCatalogSupport.class);
            if(support!=null) return support;
        }
        return new DefaultProjectCatalogSupport(owner);
    }
    
    public boolean supportsCrossProject() {
        return helper!=null;
    }
    
    public URI constructProjectProtocol(FileObject foTobeAddedInCat) {
        Project owner = FileOwnerQuery.getOwner(foTobeAddedInCat);
        if(owner!=null) {
            String ssp = getUsableProjectName(owner);
            String fragment = getRelativePath(owner.getProjectDirectory(),foTobeAddedInCat);
            try {
                return new URI(NBURI_SCHEME,ssp,fragment);
            } catch (URISyntaxException ex) {
            }
        }
        return null;
    }
    
    public boolean isProjectProtocol(URI uriStoredInCatFile) {
        return NBURI_SCHEME.equals(uriStoredInCatFile.getScheme());
    }
    
    public FileObject resolveProjectProtocol(URI uriToBeResolved) {
        if(supportsCrossProject() && isProjectProtocol(uriToBeResolved)) {
            String ssp = uriToBeResolved.getSchemeSpecificPart();
            String targetPrjRelativeRoot = helper.getProperties(
                    AntProjectHelper.PROJECT_PROPERTIES_PATH).
                    getProperty("project.".concat(ssp));
            if(targetPrjRelativeRoot!=null){
                File myPrjRoot = FileUtil.toFile(project.getProjectDirectory());
                File tgtPrjRoot = new File(myPrjRoot.toURI().resolve(targetPrjRelativeRoot));
                FileObject tgtPrjFobj = FileUtil.toFileObject(FileUtil.normalizeFile(tgtPrjRoot));
                return tgtPrjFobj.getFileObject(uriToBeResolved.getFragment());
            }
        }
        return null;
    }
    
    public boolean needsCatalogEntry(FileObject source, FileObject target) {
        assert source !=null && target !=null;
        // check if target belongs to different project or different source root
        if(project!=FileOwnerQuery.getOwner(target)) {
            return true;
        }

        FileObject folder = getSourceFolder(source);
        if (folder != null && !FileUtil.isParentOf(folder,target)) {
            return true;
        }
        
        return false;
    }
    
    public URI createCatalogEntry(FileObject source, FileObject target) throws
            IOException, CatalogModelException {
        assert source !=null && target !=null;
        CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().
                getCatalogWriteModelForProject(project.getProjectDirectory());
        assert cwm!= null;
        Project targetProject = FileOwnerQuery.getOwner(target);
        URI targetURI = null;
        URI sourceURI = null;
        try {
            sourceURI = getReferenceURI(source, target);
        } catch (URISyntaxException ex) {
            return null;
        }
        if(project!=targetProject) {
            if(!getProjectReferences().contains(targetProject) &&
                    supportsCrossProject()) {
                ProjectReferenceUtility.addProjectReference(refHelper,targetProject);
            };
            targetURI = constructProjectProtocol(target);
        } else {
            try {
                targetURI = new URI(getRelativePath(cwm.getCatalogFileObject(),target));
            } catch (URISyntaxException ex) {
                return null;
            }
        }
        cwm.addURI(sourceURI,targetURI);
        return sourceURI;
    }
    
    public URI getReferenceURI(FileObject source, FileObject target) throws URISyntaxException {
        Project targetProject = FileOwnerQuery.getOwner(target);
        FileObject sourceFolder = getSourceFolder(source);
        if (sourceFolder == null) {
            throw new IllegalArgumentException(source.getPath()+" is not in project source"); //NOI18N
        }
        String relPathToSrcGroup = getRelativePath(source.getParent(), sourceFolder);
        String relPathToSrcGroupWithSlash = relPathToSrcGroup.trim().equals("")? "" : 
            relPathToSrcGroup.concat("/");
        if(project!=targetProject) {
            FileObject folder = getSourceFolder(targetProject,target);
            if (folder == null) {
                throw new IllegalArgumentException(target.getPath()+" is not in target project source"); //NOI18N
            }
            String relPathFromTgtGroup = getRelativePath(folder,target);
            return new URI(relPathToSrcGroupWithSlash.concat(
                    getUsableProjectName(targetProject)).
                    concat("/").concat(relPathFromTgtGroup));
        } else {
            FileObject targetSourceFolder = getSourceFolder(target);
            if (targetSourceFolder == null) {
                throw new IllegalArgumentException(target.getPath()+" is not in project source"); //NOI18N
            }
            String relPathFromTgtGroup =
                    getRelativePath(targetSourceFolder,target);
            return new URI(relPathToSrcGroupWithSlash.concat(relPathFromTgtGroup));
        }
    }
    
    public Set getProjectReferences() {
        SubprojectProvider provider = (SubprojectProvider)project.getLookup().
                lookup(SubprojectProvider.class);
        return provider.getSubprojects();
    }
    
    private FileObject getSourceFolder(FileObject source) {
        return getSourceFolder(project,source);
    }
    
    private static String[] sourceTypes = new String[] {
        ProjectConstants.SOURCES_TYPE_XML,
        JavaProjectConstants.SOURCES_TYPE_JAVA,
        WebProjectConstants.TYPE_DOC_ROOT,
        WebProjectConstants.TYPE_WEB_INF
    };
    
    private static FileObject getSourceFolder(Project project, FileObject source) {
        Sources sources = ProjectUtils.getSources(project);
        assert sources !=null;
        ArrayList<SourceGroup> sourceGroups = new ArrayList<SourceGroup>();
        for (String type : sourceTypes) {
            SourceGroup[] groups = sources.getSourceGroups(type);
            if (groups != null) {
                sourceGroups.addAll(Arrays.asList(groups));
            }
        }
            
        assert sourceGroups.size()>0;
        for(SourceGroup sourceGroup:sourceGroups) {
            if(FileUtil.isParentOf(sourceGroup.getRootFolder(),source))
                return sourceGroup.getRootFolder();
        }
        return null;
    }
    
    private static String getRelativePath(FileObject source, FileObject target) {
        File sourceLocationFile = FileUtil.toFile(source);
        File targetLocationFile = FileUtil.toFile(target);
        String sourceLocation = sourceLocationFile.toURI().toString();
        String targetLocation = targetLocationFile.toURI().toString();
        StringTokenizer st1 = new StringTokenizer(sourceLocation,"/");
        StringTokenizer st2 = new StringTokenizer(targetLocation,"/");
        String relativeLoc = "";
        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            relativeLoc = st2.nextToken();
            if (!st1.nextToken().equals(relativeLoc)) {
                break;
            }
            if(!st1.hasMoreTokens() || !st2.hasMoreTokens()) {
                // seems like one of the file is parent directory of other file
                if(st1.hasMoreElements()) {
                    // seems like target is parent of source
                    relativeLoc = "..";
                    st1.nextToken();
                } else if(st2.hasMoreTokens()) {
                    // seems like source is parent of target
                    relativeLoc = st2.nextToken();
                } else {
                    // both represent same file
                    relativeLoc = "";
                }
            }
        }
        while (st1.hasMoreTokens()) {
            relativeLoc = "../".concat(relativeLoc);
            st1.nextToken();
        }
        while(st2.hasMoreTokens()) {
            relativeLoc = relativeLoc.concat("/").concat(st2.nextToken());
        }
        return relativeLoc;
    }
    
    
    private static String getUsableProjectName(Project project) {
        return  PropertyUtils.getUsablePropertyName(ProjectUtils.getInformation
                (project).getName()).replace('.','_');
    }
    
    public boolean removeCatalogEntry(URI uri) throws IOException {
        CatalogWriteModel cwm  = null;
        try {
            cwm = CatalogWriteModelFactory.getInstance().
                    getCatalogWriteModelForProject(project.getProjectDirectory());
        } catch (CatalogModelException ex) {
            return false;
        }
        boolean entryFound = false;
        for(CatalogEntry ce : cwm.getCatalogEntries()){
            URI src = null;
            try {
                src = new URI(ce.getSource());
            } catch (URISyntaxException ex) {
                continue;
            }
            if(src.equals(uri)){
                entryFound = true;
                break;
            }
        }
        if(entryFound){
            cwm.removeURI(uri);
            return true;
        }
        return false;
    }
    
}
