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

package org.netbeans.modules.java.source.tasklist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class RebuildOraculum {
    
    private static final String DEPRECATED = "DEPRECATED"; //NOI18N
    
    private static Map<JavaSource, RebuildOraculum> source2Oraculum = new WeakHashMap<JavaSource, RebuildOraculum>();
    
    public static RebuildOraculum get(FileObject file) {
        JavaSource js = JavaSource.forFileObject(file);
        RebuildOraculum res = source2Oraculum.get(js);
        
        if (res == null) {
            source2Oraculum.put(js, res = new RebuildOraculum(file));
        }
        
        return res;
    }
    
    private FileObject file;
    private Map<ElementHandle, Collection<String>> members;
            
    private RebuildOraculum(FileObject file) {
        Logger.getLogger("TIMER").log(Level.FINE, "RebuildOraculum", new Object[] {file, this});
        this.file = file;
    }
    
    private Map<ElementHandle, Collection<String>> getMembers() {
        if (members != null) {
            return members;
        } else {
            return new HashMap<ElementHandle, Collection<String>>();
        }
    }
    
    private static String convertToSourceName (String binaryName) {
        binaryName = binaryName.replace ('.','/');  //NOI18N
        int index = binaryName.lastIndexOf('/');    //NOI18N
        if (index < 0) {
            index = 0;
        }
        index = binaryName.indexOf(index,'$');      //NOI18N
        if (index > 0) {
            binaryName = binaryName.substring(0, index);
        }
        return binaryName + ".java";                //NOI18N
    }
    
    private static final Pattern ANONYMOUS = Pattern.compile("\\$[0-9]"); //NOI18N
    
    public List<File> findFilesToRebuild(File root, FileObject file, ClasspathInfo cpInfo, Map<ElementHandle, Collection<String>> currentMembers, Collection<String> possiblyRemovedClasses) {
        long startTime = System.currentTimeMillis();
        long endTime   = -1;
        
        try {
        Logger.getLogger(RebuildOraculum.class.getName()).log(Level.FINE, "members={0}", getMembers());
        Logger.getLogger(RebuildOraculum.class.getName()).log(Level.FINE, "currentMembers={0}", currentMembers);
        
        Set<String> removedClasses = new HashSet<String>(possiblyRemovedClasses);
        Map<ElementHandle, Collection<String>> added = new HashMap<ElementHandle, Collection<String>>(currentMembers);
        
        for (ElementHandle h : getMembers().keySet()) {
            added.remove(h);
        }
        
        Map<ElementHandle, Collection<String>> removed = new HashMap<ElementHandle, Collection<String>>(getMembers());
        
        for (ElementHandle h : currentMembers.keySet()) {
            removed.remove(h);
            if (h.getKind().isClass() || h.getKind().isInterface()) {
                removedClasses.remove(h.getBinaryName());
            }
        }
        
        Map<ElementHandle, Collection<String>> changedElements = new HashMap<ElementHandle, Collection<String>>(getMembers());
        
        for (Iterator<ElementHandle> it = changedElements.keySet().iterator(); it.hasNext(); ) {
            ElementHandle h = it.next();
            
            Collection<String> original = changedElements.get(h);
            Collection<String> current  = currentMembers.get(h);
            
            if (original == null || current == null || original.equals(current)) {
                it.remove();
            }
        }
        
        members = currentMembers;
        
        Collection<ElementHandle<TypeElement>> classes = new ArrayList<ElementHandle<TypeElement>>();
        
        //a really simple heuristics:
        if (!added.isEmpty() || !removed.isEmpty() || !changedElements.isEmpty()) {
            for (ElementHandle h : currentMembers.keySet()) {
                if (h.getKind().isClass() || h.getKind().isInterface()) {
                    classes.add(h);
                }
            }
        }
        
        for (String s : removedClasses) {
            if (!ANONYMOUS.matcher(s).find()) {//ignore probable anonymous inner classes/local classes
                classes.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, s));
            }
        }
        
        if (classes.isEmpty()) {
            return Collections.<File>emptyList();
        }
        
        endTime = System.currentTimeMillis();
        
        ClassIndex ci = cpInfo.getClassIndex();
        
        return findAllDependent(root, file, ci, classes);
        } finally {
            if (endTime == (-1)) {
                endTime = System.currentTimeMillis();
            }
            
            if (file != null) {
                Logger.getLogger("TIMER").log(Level.FINE, "RebuildOraculum: findFilesToRebuild total",
                        new Object[] {file, System.currentTimeMillis() - startTime});
                Logger.getLogger("TIMER").log(Level.FINE, "RebuildOraculum: quick heuristics",
                        new Object[] {file, endTime - startTime});
            }
        }
    }
    
    public static List<File> findAllDependent(File root, FileObject file, ClassIndex ci, Collection<ElementHandle<TypeElement>> classes) {
        Set<ElementHandle<TypeElement>> toParse = new HashSet<ElementHandle<TypeElement>>(classes);
        
        long start = System.currentTimeMillis();
        
        boolean changed = true;
        
        while (changed) {
            Set<ElementHandle<TypeElement>> orig = new HashSet<ElementHandle<TypeElement>>(toParse);
            
            for (ElementHandle<TypeElement> e : orig) {
                toParse.addAll(ci.getElements(e, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
            }
            
            changed = !orig.equals(toParse);
        }
        
        Set<ElementHandle<TypeElement>> orig = new HashSet<ElementHandle<TypeElement>>(toParse);
        
        for (ElementHandle<TypeElement> e : orig) {
            toParse.addAll(ci.getElements(e, EnumSet.complementOf(EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS)), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        }
        
        toParse.removeAll(classes);
        
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Handles",
                new Object[] {file, System.currentTimeMillis() - start});
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Handles #",
                new Object[] {file, toParse.size()});
        }
        
        long cur = System.currentTimeMillis();
        
        Set<File> files = new HashSet<File>();
        
        for (ElementHandle<TypeElement> e : toParse) {
            String sourceName = convertToSourceName(e.getBinaryName());
            File source = new File(root, sourceName);
            
            if (source.canRead())
                files.add(source);
            
//            FileObject f = SourceUtils.getFile(e, cpInfo);
//
//            if (f == null/* || !FileUtil.isParentOf(rootFO, f)*/)
//                continue;
//
//            files.add(FileUtil.toFile(f));
        }
        
        files.remove(null);
        
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Files",
                new Object[] {file, System.currentTimeMillis() - cur});
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Files #",
                new Object[] {file, files.size()});
        }
        
        return new ArrayList<File>(files);
    }
    
    public boolean isInitialized() {
        return members != null;
    }
    
    private static Collection<String> getExtendedModifiers(Elements elements, Element el) {
        Set<String> result = new HashSet<String>();
        
        for (Modifier m : el.getModifiers()) {
            result.add(m.name());
        }
        
        if (elements.isDeprecated(el)) {
            result.add(DEPRECATED);
        }
        
        return result;
    }
    
    public static Map<ElementHandle, Collection<String>> sortOut(Elements elements, Iterable<? extends TypeElement> topLevelElements) {
        Map<ElementHandle, Collection<String>> types = new HashMap<ElementHandle, Collection<String>>();
        Queue<TypeElement> toHandle = new LinkedList<TypeElement>();
        for (TypeElement te : topLevelElements) {
            toHandle.offer(te);
        }
        
        while (!toHandle.isEmpty()) {
            TypeElement te = toHandle.poll();
            
            types.put(ElementHandle.create(te), getExtendedModifiers(elements, te));
            
            for (Element e : te.getEnclosedElements()) {
                switch (e.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                    toHandle.offer((TypeElement) e);
                    break;
                case METHOD:
                case FIELD:
                case ENUM_CONSTANT:
                    types.put(ElementHandle.create(e), getExtendedModifiers(elements, e));
                }
            }
        }
        
        return types;
    }
    
    public void initialize(Map<ElementHandle, Collection<String>> members) {
        Logger.getLogger(RebuildOraculum.class.getName()).log(Level.FINE, "initializing members={0}", members);
        if (this.members == null)
            this.members = members;
    }
}
