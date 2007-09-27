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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.classview;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 * @author vk155633
 */
public class SmartChangeEvent {
    protected Map<CsmProject,Storage> changedProjects = new HashMap<CsmProject,Storage>();
    
    // to trace only
    private int count = 1;
    
    public SmartChangeEvent(CsmChangeEvent e){
        //super(e.getSource());
        doAdd(e);
    }
    
    public boolean addChangeEvent(CsmChangeEvent e){
        if (e.getRemovedDeclarations().size() == 0){
            doAdd(e);
            count++;
            return true;
        }
        return false;
    }
    
    public boolean addChangeEvent(SmartChangeEvent e){
        for(Storage storage : getChangedProjects().values()){
            if (storage.getRemovedDeclarations().size() > 0 ||
                storage.getRemovedNamespaces().size() > 0){
                return false;
            }
        }
        doAdd(e);
        count++;
        return true;
    }
    
    int getCount(){
        return count;
    }
    
    private void doAdd(SmartChangeEvent e){
        for(Map.Entry<CsmProject,Storage> entry : e.getChangedProjects().entrySet()){
            CsmProject project = entry.getKey();
            Storage storage = changedProjects.get(project);
            if (storage == null) {
                changedProjects.put(project, entry.getValue());
            } else {
                storage.getNewNamespaces().addAll(entry.getValue().getNewNamespaces());
                storage.getRemovedNamespaces().addAll(entry.getValue().getRemovedNamespaces());
                storage.getNewDeclarations().addAll(entry.getValue().getNewDeclarations());
                storage.getRemovedDeclarations().addAll(entry.getValue().getRemovedDeclarations());
                storage.getChangedDeclarations().putAll(entry.getValue().getChangedDeclarations());
            }
        }
    }
    
    private void doAdd(CsmChangeEvent e){
        for (CsmNamespace ns : e.getNewNamespaces()){
            Storage storage = getStorage(ns);
            if (storage != null){
                storage.addNewNamespaces(ns);
            }
        }
        for (CsmNamespace ns : e.getRemovedNamespaces()){
            Storage storage = getStorage(ns);
            if (storage != null){
                storage.addRemovedNamespaces(ns);
            }
        }
        for (CsmOffsetableDeclaration decl : e.getNewDeclarations()){
            Storage storage = getStorage(decl);
            if (storage != null){
                storage.addNewDeclaration(decl);
            }
        }
        for (CsmOffsetableDeclaration decl : e.getRemovedDeclarations()){
            Storage storage = getStorage(decl);
            if (storage != null){
                storage.addRemovedDeclarations(decl);
            }
        }
        for (Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration> decl : e.getChangedDeclarations().entrySet()){
            Storage storage = getStorage(decl.getValue());
            if (storage != null){
                storage.addChangedDeclarations(decl.getKey(),decl.getValue());
            }
        }
    }
    
    private Storage getStorage(CsmNamespace ns){
        CsmProject project = ns.getProject();
        if (project != null && project.isValid()){
            Storage storage = changedProjects.get(project);
            if (storage == null) {
                storage = new Storage(project);
                changedProjects.put(project, storage);
            }
            return storage;
        }
        return null;
    }
    
    private Storage getStorage(CsmOffsetableDeclaration decl){
        CsmProject project = findProject(decl);
        if (project != null && project.isValid()){
            Storage storage = changedProjects.get(project);
            if (storage == null) {
                storage = new Storage(project);
                changedProjects.put(project, storage);
            }
            return storage;
        }
        return null;
    }
    
    private static CsmProject findProject(CsmOffsetableDeclaration decl){
        CsmFile file = decl.getContainingFile();
        if (file != null){
            if (file.isValid()) {
                return file.getProject();
            }
            return null;
        }
        System.err.println("Cannot fing project for declaration "+decl.getUniqueName());
        return null;
    }
    
    public Map<CsmProject,Storage> getChangedProjects(){
        return changedProjects;
    }
    
    public static class Storage {
        private CsmProject changedProject;
        private Set<CsmNamespace>  newNamespaces = new HashSet<CsmNamespace>();
        private Set<CsmNamespace>  removedNamespaces = new HashSet<CsmNamespace>();
        private Set<CsmOffsetableDeclaration> newDeclarations = new HashSet<CsmOffsetableDeclaration>();
        private Set<CsmOffsetableDeclaration> removedDeclarations = new HashSet<CsmOffsetableDeclaration>();
        private Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> changedDeclarations = new HashMap<CsmOffsetableDeclaration,CsmOffsetableDeclaration>();
        
        public Storage(CsmProject project){
            changedProject = project;
        }
        
        public Collection<CsmOffsetableDeclaration> getNewDeclarations() {
            return newDeclarations;
        }
        
        private void addNewDeclaration(CsmOffsetableDeclaration declaration) {
            newDeclarations.add(declaration);
        }
        
        public Collection<CsmOffsetableDeclaration> getRemovedDeclarations() {
            return removedDeclarations;
        }
        
        private void addRemovedDeclarations(CsmOffsetableDeclaration declaration) {
            removedDeclarations.add(declaration);
        }
        
        public Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> getChangedDeclarations() {
            return changedDeclarations;
        }
        
        private void addChangedDeclarations(CsmOffsetableDeclaration oldDecl, CsmOffsetableDeclaration newDecl) {
            changedDeclarations.put(oldDecl, newDecl);
        }
        
        public CsmProject getProject() {
            return changedProject;
        }
        
        public Collection<CsmNamespace> getNewNamespaces() {
            return newNamespaces;
        }
        
        private void addNewNamespaces(CsmNamespace ns) {
            newNamespaces.add(ns);
        }
        
        public Collection<CsmNamespace> getRemovedNamespaces() {
            return removedNamespaces;
        }
        
        private void addRemovedNamespaces(CsmNamespace ns) {
            removedNamespaces.add(ns);
        }
    }
}
