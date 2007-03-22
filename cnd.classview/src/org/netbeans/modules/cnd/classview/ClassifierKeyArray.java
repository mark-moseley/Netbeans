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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.ClassNode;
import org.netbeans.modules.cnd.classview.model.EnumNode;
import org.netbeans.modules.cnd.classview.model.EnumeratorNode;
import org.netbeans.modules.cnd.classview.model.GlobalFuncNode;
import org.netbeans.modules.cnd.classview.model.MemberNode;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class ClassifierKeyArray extends HostKeyArray implements UpdatebleHost {
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.key-events"); // NOI18N
    
    public ClassifierKeyArray(ChildrenUpdater childrenUpdater, CsmCompoundClassifier classifier){
        super(childrenUpdater, classifier.getContainingNamespace().getProject(),PersistentKey.createKey(classifier));
    }
    
    public boolean newNamespsce(CsmNamespace ns) {
        return false;
    }
    
    public boolean removeNamespsce(CsmNamespace ns) {
        return false;
    }
    
    protected boolean canCreateNode(CsmDeclaration d) {
        return true;
    }
    
    protected java.util.Map<PersistentKey,SortedName> getMembers() {
        CsmCompoundClassifier classifier = getClassifier();
        java.util.Map<PersistentKey,SortedName> res = new HashMap<PersistentKey,SortedName>();
        if (classifier != null){
            if (CsmKindUtilities.isClass(classifier)){
                initClass((CsmClass)classifier, res);
            } else if (CsmKindUtilities.isEnum(classifier)){
                initEnum((CsmEnum)classifier, res);
            }
        }
        return res;
    }
    
    private void initClass(CsmClass cls, java.util.Map<PersistentKey, SortedName> res){
        for( Iterator/*<CsmClass>*/ iter = cls.getMembers().iterator(); iter.hasNext(); ) {
            CsmMember member = (CsmMember) iter.next();
            res.put(PersistentKey.createKey(member), getSortedName(member));
        }
    }
    
    private void initEnum(CsmEnum en, java.util.Map<PersistentKey, SortedName> res){
        for (Iterator iter = en.getEnumerators().iterator(); iter.hasNext();) {
            CsmEnumerator val = (CsmEnumerator) iter.next();
            res.put(PersistentKey.createKey(val), new SortedName(0,val.getName(),0));
        }
    }
    
    private CsmCompoundClassifier getClassifier(){
        return (CsmCompoundClassifier) getHostId().getObject();
    }
    
    protected CsmDeclaration findDeclaration(PersistentKey declId){
        CsmDeclaration res = (CsmDeclaration) declId.getObject();
        return res;
    }
    
    private CsmNamespace findNamespace(String nsId){
        return getProject().findNamespace(nsId);
    }
    
    protected Node createNode(PersistentKey key){
        ChildrenUpdater updater = getUpdater();
        Node node = null;
        if (updater != null) {
            CsmDeclaration member = findDeclaration(key);
            if (member != null){
                if( CsmKindUtilities.isClass(member) ) {
                    node = new ClassNode((CsmClass) member,
                            new ClassifierKeyArray(updater, (CsmClass) member));
                } else if( CsmKindUtilities.isEnum(member) ) {
                    node = new EnumNode((CsmEnum) member,
                            new ClassifierKeyArray(updater, (CsmEnum) member));
                } else if( CsmKindUtilities.isEnumerator(member) ) {
                    node = new EnumeratorNode((CsmEnumerator) member);
                } else if( CsmKindUtilities.isClassMember(member) ) {
                    node = new MemberNode((CsmMember) member);
                } else if( CsmKindUtilities.isFunction(member) ) {
                    if (traceEvents){
                        System.out.println("It should be member:"+member.getUniqueName());
                    }
                    node = new GlobalFuncNode((CsmFunction) member);
                } else {
                    if (traceEvents){
                        System.out.println("It should be member:"+member.getUniqueName());
                    }
                }
            }
        }
        return node;
    }
}