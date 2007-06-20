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

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
public abstract class AbstractRefactoringPlugin implements RefactoringPlugin {
    /**
     * 
     */
    protected static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.refactoring");   // NOI18N
    
    /**
     * 
     */
    protected AbstractRefactoring refactoring;
    // a regexp pattern for ordering attributes
    /**
     * 
     */
    protected Pattern orderingLayerAttrPattern = Pattern.compile("([\\S]+)/([\\S]+)"); //NOI18N
    /** Creates a new instance of AbstractRefactoringPlugin */
    public AbstractRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
        return null;
    }
    
    /**
     * returns the line number in the file if found, otherwise -1
     */
    protected final int checkContentOfFile(FileObject fo, String classToLookFor) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(fo.getInputStream(), "UTF-8")); // NOI18N
            String line = reader.readLine();
            int counter = 0;
            while (line != null) {
                if (line.indexOf(classToLookFor) != -1) {
                    return counter;
                }
                counter = counter + 1;
                line = reader.readLine();
            }
        } catch (IOException exc) {
            //TODO
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
        return -1;
    }
    
   protected static class InfoHolder {
        public String name = null;
        public String fullName = null;
        public boolean isClass = false;
        public boolean isMethod = false;
        public boolean isConstructor = false;
        public boolean isPublic = false;
        public boolean isStatic = false;
        public boolean hasFileObjectParam = false;
        public boolean hasNoParams = false;
    }    
    
    
   protected final InfoHolder examineLookup(Lookup lkp) throws IOException {
       final TreePathHandle handle = lkp.lookup(TreePathHandle.class);
       final InfoHolder infoholder = new InfoHolder();
       JavaSource source = JavaSource.forFileObject(handle.getFileObject());
       
       CancellableTask<CompilationController> info = new CancellableTask<CompilationController>() {
           public void run(CompilationController info) throws Exception {
               info.toPhase(JavaSource.Phase.RESOLVED);
               Element neco = handle.resolveElement(info);
               infoholder.name = neco.getSimpleName().toString();
               if (neco.getKind() == ElementKind.CLASS) {
                   infoholder.isClass = true;
                   TypeElement te = (TypeElement)neco;
                   infoholder.fullName = te.getQualifiedName().toString();
               } else if (neco.getKind() == ElementKind.METHOD) {
                   infoholder.isMethod = true;
                   ExecutableElement ee = (ExecutableElement)neco;
                   TypeElement te = (TypeElement)ee.getEnclosingElement();
                   infoholder.fullName = te.getQualifiedName().toString();
                   infoholder.isPublic = ee.getModifiers().contains(Modifier.PUBLIC);
                   infoholder.isStatic = ee.getModifiers().contains(Modifier.STATIC);
                   List<? extends VariableElement> lst =  ee.getParameters();
                   if (lst.size() > 1) {
                       infoholder.hasFileObjectParam = false;
                   } else {
                       if (lst.size() == 0) {
                           infoholder.hasNoParams = true;
                       }
                       for (VariableElement el : lst) {
                            TypeMirror tm = el.asType();
                            if (tm.getKind() == TypeKind.DECLARED) {
                                TypeElement vare = (TypeElement) ((DeclaredType) tm).asElement();
                                String fqn = vare.getQualifiedName().toString();
                                if ("org.openide.filesystems.FileObject".equals(fqn)) {
                                   infoholder.hasFileObjectParam = true;
                                }
                            }
                       }
                   }
                   
               } else if (neco.getKind() == ElementKind.CONSTRUCTOR) {
                   infoholder.isConstructor = true;
                   ExecutableElement ee = (ExecutableElement)neco;
                   TypeElement te = (TypeElement)ee.getEnclosingElement();
                   infoholder.fullName = te.getQualifiedName().toString();
                   infoholder.isPublic = ee.getModifiers().contains(Modifier.PUBLIC);
                   List<? extends VariableElement> lst =  ee.getParameters();
                   if (lst.size() == 0) {
                       infoholder.hasNoParams = true;
                   }
               }
           }
           public void cancel() { }
       };
       source.runUserActionTask(info, true);
       return infoholder;
   }

    /**
     * 
     * @param project 
     * @param fqClassName 
     * @param refactoringElements 
     */
    protected final void checkManifest(Project project, String fqClassName, RefactoringElementsBag refactoringElements) {
        String name = fqClassName;
        NbModuleProvider prov = project.getLookup().lookup(NbModuleProvider.class);
        if (prov == null) {
            return;
        }
        String pathName = name.replace('.', '/') + ".class"; //NOI18N
        Manifest mf = Util.getManifest(prov.getManifestFile());
        if (mf == null) {
            return;
        }
        Attributes attrs = mf.getMainAttributes();
        Iterator it = attrs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String val = (String)entry.getValue();
            if (val.indexOf(name) != -1 || val.indexOf(pathName) != -1) {
                RefactoringElementImplementation elem =
                        createManifestRefactoring(name, prov.getManifestFile(), ((Attributes.Name)entry.getKey()).toString(), val, null);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
            }
        }
        Map entries = mf.getEntries();
        if (entries != null) {
            it = entries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry secEnt = (Map.Entry)it.next();
                attrs = (Attributes)secEnt.getValue();
                String val = (String)secEnt.getKey();
                if (val.indexOf(name) != -1 || val.indexOf(pathName) != -1) {
                    String section = attrs.getValue("OpenIDE-Module-Class"); //NOI18N
                    RefactoringElementImplementation elem =
                            createManifestRefactoring(name, prov.getManifestFile(), null, val, section);
                    if (elem != null) {
                        refactoringElements.add(refactoring, elem);
                    }
                }
            }
        }
    }
    
    protected final void checkMetaInfServices(Project project, String fqname, RefactoringElementsBag refactoringElements) {
        FileObject services = Utility.findMetaInfServices(project);
        if (services == null) {
            return;
        }
        String name = fqname;
        // Easiest to check them all; otherwise would need to find all interfaces and superclasses:
        FileObject[] files = services.getChildren();
        for (int i = 0; i < files.length; i++) {
            int line = checkContentOfFile(files[i], name);
            if (line != -1) {
                RefactoringElementImplementation elem =
                        createMetaInfServicesRefactoring(fqname, files[i], line);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
            }
        }
    }
    
    protected final void checkLayer(Project project, String fqname, RefactoringElementsBag refactoringElements) {
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileSystem fs = handle.layer(false);
        if (fs != null) {
            checkFileObject(fs.getRoot(), fqname, refactoringElements, handle);
        }
    }
    
    
    private void checkFileObject(FileObject fo, String fqname, RefactoringElementsBag refactoringElements, LayerUtils.LayerHandle handle) {
        if (fo.isFolder()) {
            FileObject[] childs = fo.getChildren();
            for (int i =0; i < childs.length; i++) {
                checkFileObject(childs[i], fqname, refactoringElements, handle);
            }
            Enumeration en = fo.getAttributes();
            // check ordering attributes?
            while (en.hasMoreElements()) {
                String attrKey = (String)en.nextElement();
                Matcher match = orderingLayerAttrPattern.matcher(attrKey);
                if (match.matches()) {
                    String first = match.group(1);
                    if (first.endsWith(".instance")) { //NOI18N
                        String name = first.substring(0, first.length() - ".instance".length()).replace('-', '.'); //NOI18N
                        if (name.equals(fqname)) {
                            RefactoringElementImplementation elem = createLayerRefactoring(fqname, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                    String second = match.group(2);
                    if (second.endsWith(".instance")) { //NOI18N
                        String name = second.substring(0, second.length() - ".instance".length()).replace('-', '.'); //NOI18N
                        if (name.equals(fqname)) {
                            RefactoringElementImplementation elem = createLayerRefactoring(fqname, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                }
            }
        } else if (fo.isData()) {
            
            Enumeration en = fo.getAttributes();
            // check just a few specific attributes or iterate all?
            while (en.hasMoreElements()) {
                String attrKey = (String)en.nextElement();
                Object val = fo.getAttribute("literal:" + attrKey); //NOI18N
                if (val instanceof String) {
                    String attrValue = (String)val;
                    String value = attrValue;
                    if (attrValue.startsWith("new:")) { //NOI18N
                        value = attrValue.substring("new:".length()); //NOI18N
                    }
                    if (attrValue.startsWith("method:")) { //NOI18N
                        value = attrValue.substring("method:".length()); //NOI18N
                        int index = value.lastIndexOf('.');
                        if (index > 0) {
                            value = value.substring(0, index);
                        }
                    }
                    String pattern1 = fqname.replaceAll("\\.", "\\."); //NOI18N
                    String pattern2 = "[a-zA-Z0-9/-]*" + fqname.replaceAll("\\.", "-") + "\\.instance"; //NOI18N

                    if (value.matches(pattern1) || value.matches(pattern2)) {
                        RefactoringElementImplementation elem = createLayerRefactoring(fqname, handle, fo, attrKey);
                        if (elem != null) {
                            refactoringElements.add(refactoring, elem);
                        }
                    }
                }
            }
            // the actual fileobject is checked after the attributes, so that both can be performed.
            if ("instance".equals(fo.getExt())) { // NOI18N
                String name = fo.getName().replace('-', '.');
                if (name.equals(fqname)) {
                    RefactoringElementImplementation elem = createLayerRefactoring(fqname, handle, fo, null);
                    if (elem != null) {
                        refactoringElements.add(refactoring, elem);
                    }
                }
            }
            if ("settings".equals(fo.getExt())) { // NOI18N
                //TODO check also content of settings files for matches?
            }
            
        }
        
    }
    
    protected final Problem checkMethodLayer(InfoHolder info, FileObject fo, RefactoringElementsBag refactoringElements) {
        Problem problem = null;
        // do our check just on public static methods..
        if (!info.isPublic || !info.isStatic) {
            return problem;
        }
        // with no parameters or with parameter of type FileObject
        if (!info.hasFileObjectParam && !info.hasNoParams) {
            return problem;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
            FileSystem fs = handle.layer(false);
            if (fs != null) {
                checkFileObject(fs.getRoot(), info.name, null, info.fullName, refactoringElements, handle);
            }
        }
        return problem;
    }
    
    protected final Problem checkConstructorLayer(InfoHolder info, FileObject fo, RefactoringElementsBag refactoringElements) {
        Problem problem = null;
        // just consider public constructors with no params..
        if (!info.isPublic || !info.hasNoParams) {
            return problem;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
            FileSystem fs = handle.layer(false);
            if (fs != null) {
                checkFileObject(fs.getRoot(), null, info.name, info.fullName, refactoringElements, handle);
            }
        }
        return problem;
    }
    
    private void checkFileObject(FileObject fo, String method, String constructor, String fqname,
            RefactoringElementsBag refactoringElements, LayerUtils.LayerHandle handle) {
        if (fo.isFolder()) {
            FileObject[] childs = fo.getChildren();
            for (int i =0; i < childs.length; i++) {
                checkFileObject(childs[i], method, constructor, fqname, refactoringElements, handle);
            }
        } else if (fo.isData()) {
            if ("settings".equals(fo.getExt())) { // NOI18N
                //TODO check also content of settings files for matches?
            }
            Enumeration en = fo.getAttributes();
            // check just a few specific attributes or iterate all?
            while (en.hasMoreElements()) {
                String attrKey = (String)en.nextElement();
                Object val = fo.getAttribute("literal:" + attrKey); //NOI18N
                if (val instanceof String) {
                    String attrValue = (String)val;
                    if (method != null && attrValue.startsWith("method:") && attrValue.endsWith(method)) { //NOI18N
                        String clazz = attrValue.substring("method:".length()); //NOI18N
                        String methodString = null;
                        int index = clazz.lastIndexOf('.');
                        if (index > 0) {
                            methodString = clazz.substring(index + 1);
                            clazz = clazz.substring(0, index);
                        }
                        if (methodString != null && methodString.equals(method) &&
                                clazz.equals(fqname)) {
                            RefactoringElementImplementation elem = createLayerRefactoring(method, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                    if (constructor != null && attrValue.startsWith("new:")) { //NOI18N
                        String clazz = attrValue.substring("new:".length()); //NOI18N
                        if (clazz.equals(fqname)) {
                            RefactoringElementImplementation elem = createLayerRefactoring(constructor, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                }
            }
        }
        
    }
    
    protected abstract RefactoringElementImplementation createMetaInfServicesRefactoring(
            String fqclazz,
            FileObject serviceFile, int line);
    
    /**
     * 
     * @param manifestFile 
     * @param attributeKey 
     * @param attributeValue 
     * @param section 
     * @return 
     */
    protected abstract RefactoringElementImplementation createManifestRefactoring(
            String fqname, 
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section);
    
    protected RefactoringElementImplementation createLayerRefactoring(String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        throw new AssertionError("if you call checkLayer(), you need to implement this method");
    }
    
    protected RefactoringElementImplementation createMethodLayerRefactoring(String method, String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        throw new AssertionError("if you call checkLayer(), you need to implement this method");
    }
    
    protected RefactoringElementImplementation createConstructorLayerRefactoring(String constructor, String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        throw new AssertionError("if you call checkLayer(), you need to implement this method");
    }
    
}
