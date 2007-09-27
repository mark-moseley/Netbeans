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

package org.netbeans.modules.cnd.navigation.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ class HierarchyModelImpl implements HierarchyModel {
    private Map<CsmClass,Set<CsmClass>> myMap;
    private Action[] actions;
    private Action close;
       
    /** Creates a new instance of HierarchyModel */
    public HierarchyModelImpl(CsmClass cls, Action[] actions, boolean subDirection, boolean plain, boolean recursive) {
        this.actions = actions;
        if (subDirection) {
            myMap = buildSubHierarchy(cls);
        } else {
            myMap = buildSuperHierarchy(cls);
        }
        if (!recursive) {
            Set<CsmClass> result = myMap.get(cls);
            if (result == null){
                result = new HashSet<CsmClass>();
            }
            myMap = new HashMap<CsmClass,Set<CsmClass>>();
            myMap.put(cls,result);
        }
        if (plain) {
            Set<CsmClass> result = new HashSet<CsmClass>();
            gatherList(cls, result, myMap);
            myMap = new HashMap<CsmClass,Set<CsmClass>>();
            myMap.put(cls,result);
        }
    }
    
    public Map<CsmClass,Set<CsmClass>> getModel(){
        return myMap;
    }

    private void gatherList(CsmClass cls, Set<CsmClass> result, Map<CsmClass,Set<CsmClass>> map){
        Set<CsmClass> set = map.get(cls);
        if (set == null) {
            return;
        }
        for(CsmClass c : set){
            if (!result.contains(c)) {
                result.add(c);
                gatherList(c, result, map);
            }
        }
    }

    private Map<CsmClass,Set<CsmClass>> buildSuperHierarchy(CsmClass cls){
        HashMap<CsmClass,Set<CsmClass>> aMap = new HashMap<CsmClass,Set<CsmClass>>();
        buildSuperHierarchy(cls, aMap);
        return aMap;
    }
    
    private void buildSuperHierarchy(CsmClass cls, Map<CsmClass,Set<CsmClass>> map){
        Set<CsmClass> back = map.get(cls);
        if (back != null) {
            return;
        }
        back = new HashSet<CsmClass>();
        map.put(cls, back);
        List list = cls.getBaseClasses();
        if (list != null && list.size() >0){
            for(int i = 0; i < list.size(); i++){
                CsmInheritance inh = (CsmInheritance)list.get(i);
                CsmClass c = inh.getCsmClass();
                if (c != null) {
                    back.add(c);
                    buildSuperHierarchy(c, map);
                }
            }
        }
    }
    
    private Map<CsmClass,Set<CsmClass>> buildSubHierarchy(CsmClass cls){
        HashMap<CsmClass,Set<CsmClass>> aMap = new HashMap<CsmClass,Set<CsmClass>>();
        CsmProject prj = cls.getContainingFile().getProject();
        buildSubHierarchy(prj.getGlobalNamespace(), aMap);
        return aMap;
    }
    
    private void buildSubHierarchy(CsmNamespace ns, Map<CsmClass,Set<CsmClass>> map){
        for(Iterator it = ns.getNestedNamespaces().iterator(); it.hasNext();){
            buildSubHierarchy((CsmNamespace)it.next(), map);
        }
        for(Iterator it = ns.getDeclarations().iterator(); it.hasNext();){
            CsmDeclaration decl = (CsmDeclaration)it.next();
            if (CsmKindUtilities.isClass(decl)){
                buildSubHierarchy(map, (CsmClass)decl);
            }
        }
    }

    private void buildSubHierarchy(final Map<CsmClass, Set<CsmClass>> map, final CsmClass cls) {
        List list = cls.getBaseClasses();
        if (list != null && list.size() >0){
            for(int i = 0; i < list.size(); i++){
                CsmInheritance inh = (CsmInheritance)list.get(i);
                CsmClass c = inh.getCsmClass();
                if (c != null) {
                    Set<CsmClass> back = map.get(c);
                    if (back == null){
                        back = new HashSet<CsmClass>();
                        map.put(c,back);
                    }
                    back.add(cls);
                }
            }
        }
        for(CsmMember member : cls.getMembers()){
            if (CsmKindUtilities.isClass(member)){
                buildSubHierarchy(map, (CsmClass)member);
            }
        }
    }

    public Action[] getDefaultActions() {
        return actions;
    }

    public Action getCloseWindowAction() {
        return close;
    }

    public void setCloseWindowAction(Action close) {
        this.close = close;
    }
}
