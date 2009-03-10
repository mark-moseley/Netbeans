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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.ClassifierContainerKey;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * Storage for project classifiers. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ class ClassifierContainer extends ProjectComponent implements Persistent, SelfPersistent {

    private Map<CharSequence, CsmUID<CsmClassifier>> classifiers = new HashMap<CharSequence, CsmUID<CsmClassifier>>();
    private Map<CharSequence, CsmUID<CsmClassifier>> typedefs = new HashMap<CharSequence, CsmUID<CsmClassifier>>();
    private ReadWriteLock declarationsLock = new ReentrantReadWriteLock();

    // empty stub
    private static final ClassifierContainer EMPTY = new ClassifierContainer() {
        @Override
        public boolean putClassifier(CsmClassifier decl) {
            return false;
        }

        @Override
        public void put() {
        }
    };

    public static ClassifierContainer empty() {
        return EMPTY;
    }
    
    /** Creates a new instance of ClassifierContainer */
    public ClassifierContainer(ProjectBase project) {
        super(new ClassifierContainerKey(project.getUniqueName().toString()), true);
        put();
    }

    public ClassifierContainer(DataInput input) throws IOException {
        super(input);
        read(input);
    }

    // only for EMPTY static field
    private ClassifierContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null, true);
    }
    
    public CsmClassifier getClassifier(CharSequence qualifiedName) {
        CsmClassifier result;
        CsmUID<CsmClassifier> uid;
        qualifiedName = CharSequenceKey.create(qualifiedName);
        try {
            declarationsLock.readLock().lock();
            uid = classifiers.get(qualifiedName);
            if (uid == null) {
                uid = typedefs.get(qualifiedName);
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        result = UIDCsmConverter.UIDtoDeclaration(uid);
        return result;
    }
    
    public boolean putClassifier(CsmClassifier decl) {
        CharSequence qn = decl.getQualifiedName();
        Map<CharSequence, CsmUID<CsmClassifier>> map;
        try {
            declarationsLock.writeLock().lock();
            if (isTypedef(decl)) {
                map = typedefs;
            } else {
                map = classifiers;
            }
            if (!map.containsKey(qn)) {
                CsmUID<CsmClassifier> uid = UIDCsmConverter.declarationToUID(decl);
                assert uid != null;
                map.put(qn, uid);
                assert (UIDCsmConverter.UIDtoDeclaration(uid) != null);
                return true;
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        return false;
    }

    public void removeClassifier(CsmDeclaration decl) {
        Map<CharSequence, CsmUID<CsmClassifier>> map;
        CsmUID<CsmClassifier> uid;
        try {
            declarationsLock.writeLock().lock();
            if (isTypedef(decl)) {
                map = typedefs;
            } else {
                map = classifiers;
            }
            uid = map.remove(decl.getQualifiedName());
        } finally {
            declarationsLock.writeLock().unlock();
        }
        assert (uid == null) || (UIDCsmConverter.UIDtoCsmObject(uid) != null) : " no object for UID " + uid;
    }

    //public void clearClassifiers() {
    //    classifiers.clear();
    //    typedefs.clear();
    //}

    private boolean isTypedef(CsmDeclaration decl){
        return CsmKindUtilities.isTypedef(decl);
    }
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        try {
            declarationsLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.classifiers, output, false);
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.typedefs, output, false);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }
    
    private void read(DataInput input) throws IOException {
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.classifiers, input, QualifiedNameCache.getManager());
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.typedefs, input, QualifiedNameCache.getManager());
    }
}
