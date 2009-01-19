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

import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;


/**
 * Container for all unresolved stuff in the project
 * 
 * @author Vladimir Kvasihn
 */
public final class Unresolved implements Disposable {
    
    private static final CharSequence UNRESOLVED = CharSequenceKey.create("$unresolved file$"); // NOI18N)
    private static class IllegalCallException extends RuntimeException {
	IllegalCallException() {
	    super("This method should never be called for Unresolved"); // NOI18N
	}
    }
    
    public final class UnresolvedClass extends ClassEnumBase<CsmClass> implements CsmClass {
        public UnresolvedClass(String name) {
            super(name, unresolvedFile, null);
            initScope(unresolvedNamespace, null);
            initQualifiedName(unresolvedNamespace, null);
        }
	
	public void register() {
	    // we don't need registering in project here.
	    // so we just register in namespace and in repository
            if (unresolvedNamespace != null) {
                unresolvedNamespace.addDeclaration(this);
            }
	}
	
        public boolean isTemplate() {
            return false;
        }
        public Collection<CsmScopeElement> getScopeElements() {
            return Collections.<CsmScopeElement>emptyList();
        }
        
        public Collection<CsmMember> getMembers() {
            return Collections.<CsmMember>emptyList();
        }

        public List<CsmFriend> getFriends() {
            return Collections.<CsmFriend>emptyList();
        }
        
        public int getLeftBracketOffset() {
            return 0;
        }
        
        public List<CsmInheritance> getBaseClasses() {
            return Collections.<CsmInheritance>emptyList();
        }
        
        @Override
        public boolean isValid() {
            return false; // false for dummy class, to allow reconstruct in usage place
        }
        
        public CsmDeclaration.Kind getKind() {
            return CsmClass.Kind.CLASS;
        }

	@Override
	protected CsmUID createUID() {
	    return UIDUtilities.createUnresolvedClassUID(getName().toString(), getProject());
	}
	
        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(DataOutput output) throws IOException {
            throw new IllegalCallException();
        }        
    }
    
    private final static class UnresolvedNamespace extends NamespaceImpl {
	
        private UnresolvedNamespace(ProjectBase project) {
            super(project, null, "$unresolved$","$unresolved$"); // NOI18N
        }

        @Override
        protected void notifyCreation() {
            // skip registration
        }

	@Override
	protected CsmUID<CsmNamespace> createUID() {
	    return UIDUtilities.createUnresolvedNamespaceUID(getProject());
	}

	
	@Override
	public void write(DataOutput output) throws IOException {
	    throw new IllegalCallException();
	}
    }
    
    public final class UnresolvedFile implements CsmFile, CsmIdentifiable {
	
        private UnresolvedFile() {
        }
        
        public String getText(int start, int end) {
            return "";
        }
        public String getText() {
            return "";
        }
        public List<CsmScopeElement> getScopeElements() {
            return Collections.<CsmScopeElement>emptyList();
        }
        
        public CsmProject getProject() {
            return _getProject();
        }

        private synchronized CsmProject _getProject() {
            if( projectRef == null ) {
                assert projectUID != null;
                return (ProjectBase)UIDCsmConverter.UIDtoProject(projectUID);
            }
            else {
                return projectRef;
            }
        }
        
        public CharSequence getName() {
            return UNRESOLVED; // NOI18N
        }
        public List<CsmInclude> getIncludes() {
            return Collections.<CsmInclude>emptyList();
        }
        public List<CsmOffsetableDeclaration> getDeclarations() {
            return Collections.<CsmOffsetableDeclaration>emptyList();
        }
        public String getAbsolutePath() {
            return "$unresolved file$"; // NOI18N
        }
        public boolean isValid() {
            return getProject().isValid();
        }
        public void scheduleParsing(boolean wait) {
        }
        public boolean isParsed() {
            return true;
        }
        public List<CsmMacro> getMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Iterator<CsmMacro> getMacros(CsmFilter filter) {
            return getMacros().iterator();
        }
        
        public CsmUID<CsmFile> getUID() {
            if (uid == null) {
                uid = UIDUtilities.createUnresolvedFileUID(this.getProject());
            }
            return uid;
        }
	
        private CsmUID<CsmFile> uid = null;

        public boolean isSourceFile() {
            return false;
        }

        public boolean isHeaderFile() {
            return true;
        }

        public Collection<CsmErrorDirective> getErrors() {
            return Collections.<CsmErrorDirective>emptyList();
        }
    };
    
    // only one of projectRef/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    
    // doesn't need Repository Keys
    private final CsmFile unresolvedFile;
    // doesn't need Repository Keys
    private final NamespaceImpl unresolvedNamespace;
    // doesn't need Repository Keys
    private Map<String, Reference<UnresolvedClass>> dummiesForUnresolved = new HashMap<String, Reference<UnresolvedClass>>();
    
    public Unresolved(ProjectBase project) {
        this.projectUID = UIDCsmConverter.projectToUID(project);
        this.projectRef = null;
        unresolvedFile = new UnresolvedFile();
        unresolvedNamespace = new UnresolvedNamespace(project);
    }
    
    public void dispose() {
        onDispose();
    }
    
    private synchronized void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.projectRef = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
            assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
        }
    }    
    
    public CsmClass getDummyForUnresolved(CharSequence[] nameTokens) {
        return getDummyForUnresolved(getName(nameTokens));
    }
    
    @SuppressWarnings("unchecked")
    public CsmClass getDummyForUnresolved(String name) {
        Reference<UnresolvedClass> ref = dummiesForUnresolved.get(name);
        UnresolvedClass cls = ref == null ? null : ref.get();
        if( cls == null ) {
            cls = new UnresolvedClass(name);
            dummiesForUnresolved.put(name, new SoftReference(cls));
	    cls.register();
        }
        return cls;
    }
    
    public CsmNamespace getUnresolvedNamespace() {
        return unresolvedNamespace;
    }
    
    public CsmFile getUnresolvedFile() {
	return unresolvedFile;
    }
    
    private String getName(CharSequence[] nameTokens) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < nameTokens.length; i++ ) {
            if( i > 0 ) {
                sb.append("::"); // NOI18N
            }
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }
}
