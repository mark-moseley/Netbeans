/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
/*
 * Util.java
 *
 * @author nn136682
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.SourceFileArtifact;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import java.io.File;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class Util {
    
    public static boolean isTypeCompatible(IElement root, boolean verbose) {
        ETList<IElement> sources = root.getSourceFiles();
        SourceFileArtifact sfa = (SourceFileArtifact) sources.get(0);
        File f = new File(sfa.getSourceFile());
        FileObject sourceFO = FileUtil.toFileObject(f);
        if (sourceFO == null) {
            return true;  // source project not available, just return default assumption
        }
        
        Project sourceProject = FileOwnerQuery.getOwner(sourceFO);
        String level = SourceLevelQuery.getSourceLevel(sourceFO);
        if (isAtLeast15(level)) {
            return true;
        }
        
        if (! (root instanceof INamespace)) {
            return true;
        }
        
        INamespace context = (INamespace) root;
        String query = "//*[name() = \'UML:Attribute\' or name() = \'UML:Parameter\' or name() = \'UML:Class\' or name() = \'UML:Interface\']"; //NOI18N
        ElementLocator locator = new ElementLocator();
        ETList<IElement> etList = locator.findElementsByDeepQuery(context, query);
        
        if(etList != null) {
            for(IElement e : etList) {
                IClassifier type = null;
                if (e instanceof ITypedElement) {
                    type = ((ITypedElement)e).getType();
                } else if (e instanceof IClassifier) {
                    type = (IClassifier) e;
                }
                
                if (type != null) {
                    if (Util.isTypeJava5Generics(type.getName())) {
                        if (verbose) {
                            String name = sourceProject.getProjectDirectory().getName();
                            String msg = NbBundle.getMessage(Util.class, "E_CMN_INCOMPATIBLE_TYPE", type, level, name);
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                        }
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    public static boolean isTypeCompatibleWithPackageSource(IPackage pack, IElement type, boolean verbose) {
        if (! (type instanceof IEnumeration )) {
            return true;
        }

        File[] sourceRoots = pack.getProject().getAssociatedProjectSourceRoots().getCompileDependencies();
        if (sourceRoots == null || sourceRoots.length < 1) {
            return true;
        }
        FileObject source = FileUtil.toFileObject(sourceRoots[0]);
        if (source == null) {
            return true;
        }
        
        String level = SourceLevelQuery.getSourceLevel(source);
        if (isAtLeast15(level)) {
            return true;
        }
        
        if (verbose) {
            Project p = FileOwnerQuery.getOwner(source);
            String name = p.getProjectDirectory().getName();
            String msg = NbBundle.getMessage(Util.class, "E_CMN_INCOMPATIBLE_TYPE", "Enumeration", level, name); //NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        }
        
        return false;
    }
    
    public static boolean isTypeCompatibleWithElementSources(String type, IElement element, boolean verbose) {
        IProject p = element.getProject();
        if (p == null || p.getAssociatedProjectSourceRoots() == null) { // some unprepared unknown element
            return true;
        }
        
        File[] roots = p.getAssociatedProjectSourceRoots().getCompileDependencies();
        if (roots == null || roots.length < 1) { // don't know
            return true;
        }
        
        return isTypeCompatible(type, roots[0], verbose);
    }
    
    public static boolean isTypeCompatibleWithElementSources(IClassifier type, IElement element, boolean verbose) {
        return isTypeCompatibleWithElementSources(type.getName(), element, verbose);
    }
    
    public static boolean isTypeCompatible(String type, File source, boolean verbose) {
        if (! isTypeJava5Generics(type)) {
            return true;
        }
        
        FileObject fo = FileUtil.toFileObject(source);
        
        // project not open
        if (fo == null) {
            return true;
        }
        
        String level = SourceLevelQuery.getSourceLevel(fo);
        if (level == null) { // don't know, default to true
            return true;
        }
        
        if (isAtLeast15(level)) { //NOI18N
            return true;
        }
        
        if (verbose) {
            Project p = FileOwnerQuery.getOwner(fo);
            String projectName = p.getProjectDirectory().getName();
            String msg = NbBundle.getMessage(Util.class, "E_CMN_INCOMPATIBLE_TYPE", type, level, projectName);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        }
        
        return false;
    }
    
    public static boolean isAtLeast15(String level) {
        try {
            int i = level.indexOf('.');
            Integer integer1 = Integer.parseInt(level.substring(0, i));
            if (integer1.intValue() < 1) {
                return false;
            } else if (integer1.intValue() > 2) {
                return true;
            }
            Integer integer2 = Integer.parseInt(level.substring(i+1));
            if (integer2.intValue() < 5) {
                return false;
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return true;
    }
    
    public static boolean isTypeJava5Generics(String type) {
        return type.indexOf('<') > 0;
    }
}


