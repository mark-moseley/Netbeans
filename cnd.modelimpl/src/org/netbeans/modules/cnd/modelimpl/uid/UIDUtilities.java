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
package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.api.model.CsmBuiltIn;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * utilities to create CsmUID for CsmObjects
 * @author Vladimir Voskresensky
 */
public class UIDUtilities {

    /** Creates a new instance of UIDUtilities */
    private UIDUtilities() {
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmProject> createProjectUID(ProjectBase prj) {
        return UIDManager.instance().getSharedUID(new ProjectUID(prj));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmFile> createFileUID(FileImpl file) {
        return UIDManager.instance().getSharedUID(new FileUID(file));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmNamespace> createNamespaceUID(CsmNamespace ns) {
        return UIDManager.instance().getSharedUID(new NamespaceUID(ns));
    }

    @SuppressWarnings("unchecked")
    public static <T extends CsmOffsetableDeclaration> CsmUID<T> createDeclarationUID(T declaration) {
        assert (!(declaration instanceof CsmBuiltIn)) : "built-in have own UIDs";
        CsmUID<T> uid;
        //if (!ProjectBase.canRegisterDeclaration(declaration)) {
        if (!namedDeclaration(declaration)) {
            uid = handleUnnamedDeclaration((CsmOffsetableDeclaration) declaration);
        } else {
            if (declaration instanceof CsmTypedef) {
                uid = new TypedefUID((CsmTypedef) declaration);
            } else if (declaration instanceof CsmClassifier) {
                uid = new ClassifierUID(declaration);
            } else {
                uid = new DeclarationUID(declaration);
            }
        }
        return UIDManager.instance().getSharedUID(uid);
    }

    private static <T extends CsmOffsetableDeclaration> boolean namedDeclaration(T declaration) {
        assert declaration != null;
        assert declaration.getName() != null;
        return declaration.getName().length() > 0;
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmMacro> createMacroUID(CsmMacro macro) {
        return UIDManager.instance().getSharedUID(new MacroUID(macro));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmInclude> createIncludeUID(CsmInclude incl) {
        return UIDManager.instance().getSharedUID(new IncludeUID(incl));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmParameterList> createParamListUID(CsmParameterList incl) {
        return UIDManager.instance().getSharedUID(new ParamListUID(incl));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmClass> createUnresolvedClassUID(String name, CsmProject project) {
        return UIDManager.instance().getSharedUID(new UnresolvedClassUID(name, project));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmFile> createUnresolvedFileUID(CsmProject project) {
        return UIDManager.instance().getSharedUID(new UnresolvedFileUID(project));
    }

    @SuppressWarnings("unchecked")
    public static CsmUID<CsmNamespace> createUnresolvedNamespaceUID(CsmProject project) {
        return UIDManager.instance().getSharedUID(new UnresolvedNamespaceUID(project));
    }

    public static int getProjectID(CsmUID<CsmFile> uid) {
        if (uid instanceof KeyBasedUID) {
            return KeyUtilities.getProjectIndex(((KeyBasedUID) uid).getKey());
        }
        return -1;
    }

    public static boolean isProjectFile(CsmUID<CsmProject> uid1, CsmUID<CsmFile> uid2) {
        if (uid1 instanceof KeyBasedUID && uid1 instanceof KeyBasedUID) {
            int i1 = KeyUtilities.getProjectIndex(((KeyBasedUID) uid1).getKey());
            int i2 = KeyUtilities.getProjectIndex(((KeyBasedUID) uid2).getKey());
            if (i1 >= 0 && i2 >=0) {
                return i1 == i2;
            }
        }
        return false;
    }

    public static boolean isSameProject(CsmUID<CsmFile> uid1, CsmUID<CsmFile> uid2) {
        if (uid1 instanceof KeyBasedUID && uid1 instanceof KeyBasedUID) {
            int i1 = KeyUtilities.getProjectIndex(((KeyBasedUID) uid1).getKey());
            int i2 = KeyUtilities.getProjectIndex(((KeyBasedUID) uid2).getKey());
            if (i1 >= 0 && i2 >=0) {
                return i1 == i2;
            }
        }
        return false;
    }

    public static boolean isSameFile(CsmUID<CsmOffsetableDeclaration> uid1, CsmUID<CsmOffsetableDeclaration> uid2) {
        if (uid1 instanceof KeyBasedUID && uid1 instanceof KeyBasedUID) {
            int i1 = KeyUtilities.getProjectFileIndex(((KeyBasedUID) uid1).getKey());
            int i2 = KeyUtilities.getProjectFileIndex(((KeyBasedUID) uid2).getKey());
            if (i1 >= 0 && i2 >=0) {
                return i1 == i2;
            }
        }
        return isSameFile(uid1.getObject(), uid2.getObject());
    }

    private static boolean isSameFile(CsmOffsetableDeclaration decl1, CsmOffsetableDeclaration decl2) {
        if (decl1 != null && decl2 != null) {
            CsmFile file1 = decl1.getContainingFile();
            CsmFile file2 = decl2.getContainingFile();
            if (file1 != null && file2 != null) {
                return file1.equals(file2);
            }
        }
        return false;
    }

    public static CsmDeclaration.Kind getKind(CsmUID<CsmOffsetableDeclaration> uid) {
        if (uid instanceof KeyBasedUID) {
            Key key = ((KeyBasedUID) uid).getKey();
            return KeyUtilities.getKeyKind(key);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            return uid.getObject().getKind();
        }
        return null;
    }

    public static CharSequence getFileName(CsmUID<CsmFile> uid) {
        if (uid instanceof KeyBasedUID) {
            Key key = ((KeyBasedUID) uid).getKey();
            return KeyUtilities.getKeyName(key);
        }
        return null;
    }

    public static CharSequence getProjectName(CsmUID<CsmProject> uid) {
        if (uid instanceof KeyBasedUID) {
            Key key = ((KeyBasedUID) uid).getKey();
            return KeyUtilities.getKeyName(key);
        }
        return null;
    }

    public static <T extends CsmOffsetableDeclaration> CharSequence getName(CsmUID<T> uid) {
        if (uid instanceof KeyBasedUID) {
            Key key = ((KeyBasedUID) uid).getKey();
            return KeyUtilities.getKeyName(key);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            return uid.getObject().getName();
        }
        return null;
    }

    public static <T extends CsmOffsetableDeclaration> int getStartOffset(CsmUID<T> uid) {
        if (uid instanceof KeyBasedUID) {
            Key key = ((KeyBasedUID) uid).getKey();
            return KeyUtilities.getKeyStartOffset(key);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            return uid.getObject().getStartOffset();
        }
        return -1;
    }

    public static <T extends CsmOffsetableDeclaration> int getEndOffset(CsmUID<T> uid) {
        if (uid instanceof KeyBasedUID) {
            Key key = ((KeyBasedUID) uid).getKey();
            return KeyUtilities.getKeyEndOffset(key);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            return uid.getObject().getEndOffset();
        }
        return -1;
    }

    /**
     * Compares UIDs of the two declarationds within the same file
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater 
     *         than the second.
     */
    public static <T extends CsmOffsetableDeclaration> int compareWithinFile(CsmUID<T> d1, CsmUID<T> d2) {

        // by start offset
        int offset1 = getStartOffset(d1);
        int offset2 = getStartOffset(d2);
        if (offset1 != offset2) {
            return offset1 - offset2;
        }
        // by end offset
        offset1 = getEndOffset(d1);
        offset2 = getEndOffset(d2);
        if (offset1 != offset2) {
            return offset1 - offset2;
        }
        // by name
        CharSequence name1 = getName(d1);
        CharSequence name2 = getName(d1);
        if (name1 instanceof Comparable) {
            @SuppressWarnings("unchecked")
            Comparable<CharSequence> o1 = (Comparable<CharSequence>) name1;
            return o1.compareTo(name2);
        }
        if (name1 != null) {
            return (name2 == null) ? 1 : 0;
        } else { // name1 == null
            return (name2 == null) ? 0 : -1;
        }
    }

    @SuppressWarnings("unchecked")
    private static CsmUID handleUnnamedDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_UNNAMED_DECLARATIONS) {
            System.err.print("\n\ndeclaration with empty name '" + decl.getUniqueName() + "'");
            new CsmTracer().dumpModel(decl);
        }
        if (decl instanceof CsmClassifier) {
            return new UnnamedClassifierUID(decl, UnnamedID.incrementAndGet());
        } else {
            return new UnnamedOffsetableDeclarationUID(decl, UnnamedID.incrementAndGet());
        }
    }
    private static AtomicInteger UnnamedID = new AtomicInteger(0);
    //////////////////////////////////////////////////////////////////////////
    // impl details

    /**
     * UID for CsmProject
     */
    /* package */ static final class ProjectUID extends KeyBasedUID<CsmProject> {

        public ProjectUID(ProjectBase project) {
            super(KeyUtilities.createProjectKey(project));
        }

        /* package */ ProjectUID(DataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmNamespace
     */
    /* package */ static final class NamespaceUID extends KeyBasedUID<CsmNamespace> {

        public NamespaceUID(CsmNamespace ns) {
            super(KeyUtilities.createNamespaceKey(ns));
        }

        /* package */ NamespaceUID(DataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmFile
     */
    /* package */ static final class FileUID extends KeyBasedUID<CsmFile> {

        public FileUID(FileImpl file) {
            super(KeyUtilities.createFileKey(file));
        }

        /* package */ FileUID(DataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * base UID for CsmDeclaration
     */
    private static abstract class OffsetableDeclarationUIDBase<T extends CsmOffsetableDeclaration> extends KeyBasedUID<T> {

        public OffsetableDeclarationUIDBase(T declaration) {
            this(KeyUtilities.createOffsetableDeclarationKey((OffsetableDeclarationBase) declaration));
        }

        protected OffsetableDeclarationUIDBase(Key key) {
            super(key);
        }

        /* package */ OffsetableDeclarationUIDBase(DataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        public String toString() {
            String retValue = getToStringPrefix() + ":" + super.toString(); // NOI18N
            return retValue;
        }

        protected String getToStringPrefix() {
            return "UID for OffsDecl"; // NOI18N
        }
    }

    /**
     * UID for CsmTypedef
     */
    /* package */ static final class TypedefUID<T extends CsmTypedef> extends OffsetableDeclarationUIDBase<T> {

        public TypedefUID(T typedef) {
            super(typedef);
//            assert typedef instanceof RegistarableDeclaration;
//            if (!((RegistarableDeclaration)typedef).isRegistered()) {
//                System.err.print("\n\nunregistered declaration'" + typedef.getUniqueName() + "'");
//                new CsmTracer().dumpModel(typedef);
//            }
//            assert ((RegistarableDeclaration)typedef).isRegistered();            
        }

        /* package */ TypedefUID(DataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "TypedefUID"; // NOI18N
        }
    }

    /**
     * UID for CsmMacro
     */
    /* package */ static final class MacroUID extends KeyBasedUID<CsmMacro> {

        public MacroUID(CsmMacro macro) {
            super(KeyUtilities.createMacroKey(macro));
        }

        /* package */ MacroUID(DataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmInclude
     */
    /* package */ static final class IncludeUID extends KeyBasedUID<CsmInclude> {

        public IncludeUID(CsmInclude incl) {
            super(KeyUtilities.createIncludeKey(incl));
        }

        /* package */ IncludeUID(DataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmParameterList
     */
    /* package */ static final class ParamListUID<T extends CsmParameterList, K extends CsmNamedElement> extends KeyBasedUID<CsmParameterList<T, K>> {

        public ParamListUID(CsmParameterList<T, K> paramList) {
            super(KeyUtilities.createParamListKey(paramList));
        }

        /* package */ ParamListUID(DataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmClassifier
     */
    /* package */ static final class DeclarationUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public DeclarationUID(T decl) {
            super(decl);
        }

        /* package */ DeclarationUID(DataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "DeclarationUID"; // NOI18N
        }
    }

    /**
     * UID for CsmClassifier
     */
    /* package */ static final class ClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public ClassifierUID(T classifier) {
            super(classifier);
        }

        /* package */ ClassifierUID(DataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "ClassifierUID"; // NOI18N
        }
    }

    /**
     * UID for CsmClassifier with empty getName()
     */
    /* package */ static final class UnnamedClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public UnnamedClassifierUID(T classifier, int index) {
            super(KeyUtilities.createUnnamedOffsetableDeclarationKey((OffsetableDeclarationBase) classifier, index));
        }

        /* package */ UnnamedClassifierUID(DataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "<UNNAMED CLASSIFIER UID>"; // NOI18N
        }
    }

    /**
     * UID for CsmDeclaration with empty getName()
     */
    /* package */ static final class UnnamedOffsetableDeclarationUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public UnnamedOffsetableDeclarationUID(T decl, int index) {
            super(KeyUtilities.createUnnamedOffsetableDeclarationKey((OffsetableDeclarationBase) decl, index));
        }

        /* package */ UnnamedOffsetableDeclarationUID(DataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "<UNNAMED OFFS-DECL UID>"; // NOI18N
        }
    }

    /**
     * Abstract base class for Unresolved* UIDs.
     */
    /* package */ static abstract class UnresolvedUIDBase<T> implements CsmUID<T>, SelfPersistent {

        private CsmUID<CsmProject> projectUID;

        public UnresolvedUIDBase(CsmProject project) {
            assert project != null : "how to create UID without project?";
            projectUID = UIDs.get(project);
        }

        protected ProjectBase getProject() {
            return (ProjectBase) projectUID.getObject();
        }

        /* package */ UnresolvedUIDBase(DataInput aStream) throws IOException {
            projectUID = UIDObjectFactory.getDefaultFactory().readUID(aStream);
        }

        public abstract T getObject();

        public void write(DataOutput output) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(projectUID, output);
        }

        protected String getToStringPrefix() {
            return "<UNRESOLVED UID>"; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UnresolvedUIDBase<?> other = (UnresolvedUIDBase<?>) obj;
            if (this.projectUID != other.projectUID && (this.projectUID == null || !this.projectUID.equals(other.projectUID))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 43 * hash + (this.projectUID != null ? this.projectUID.hashCode() : 0);
            return hash;
        }



    }

    /* package */ static final class UnresolvedClassUID<T> extends UnresolvedUIDBase<CsmClass> {

        private CharSequence name;

        public UnresolvedClassUID(String name, CsmProject project) {
            super(project);
            this.name = NameCache.getManager().getString(name);
        }

        public CsmClass getObject() {
            return getProject().getDummyForUnresolved(name);
        }

        public UnresolvedClassUID(DataInput input) throws IOException {
            super(input);
            name = PersistentUtils.readUTF(input, NameCache.getManager());
        }

        @Override
        public void write(DataOutput output) throws IOException {
            super.write(output);
            PersistentUtils.writeUTF(name, output);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final UnresolvedClassUID<?> other = (UnresolvedClassUID<?>) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

    }

    /* package */ static final class UnresolvedNamespaceUID extends UnresolvedUIDBase<CsmNamespace> {

        public UnresolvedNamespaceUID(CsmProject project) {
            super(project);
        }

        public UnresolvedNamespaceUID(DataInput input) throws IOException {
            super(input);
        }

        public CsmNamespace getObject() {
            return getProject().getUnresolvedNamespace();
        }
    }

    /* package */ static final class UnresolvedFileUID extends UnresolvedUIDBase<CsmFile> implements Disposable {
        private ProjectBase prjRef = null;
        public UnresolvedFileUID(CsmProject project) {
            super(project);
            prjRef = (ProjectBase)project;
        }

        public UnresolvedFileUID(DataInput input) throws IOException {
            super(input);
        }

        public CsmFile getObject() {
            return getProject().getUnresolvedFile();
        }

        @Override
        protected ProjectBase getProject() {
            ProjectBase prj = prjRef;
            if (prj == null) {
                prj = super.getProject();
            }
            return prj;
        }

        public void dispose() {
            prjRef = getProject();
        }
    }

    public static void disposeUnresolved(CsmUID<?> uid) {
        if (uid instanceof UnresolvedFileUID) {
            UnresolvedFileUID fileUID = (UnresolvedFileUID) uid;
            fileUID.dispose();
        }
    }

}
