/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.spi.model.TypesProvider;
import org.openide.util.Lookup;

/**
 * service for working with types
 * @author Vladimir Voskresensky
 */
public final class CsmTypes {
    private static TypesProvider provider;

    // private service constructor
    private CsmTypes() {
    }


    public static CsmType createType(CsmClassifier cls, TypeDescriptor td, OffsetDescriptor offs) {
        return getProvider().createType(cls, td, offs);
    }

    public static CsmType createType(CsmType orig, TypeDescriptor newTypeDescriptor) {
        return getProvider().createType(orig, newTypeDescriptor);
    }

    public static CsmType createSimpleType(CsmClassifier cls, OffsetDescriptor offs) {
        return getProvider().createType(cls, new TypeDescriptor(false, false, 0, 0), offs);
    }

    /**
     * creates const type of original type
     * @param type dereferenced type
     * @return new type
     */
    public static CsmType createConstType(CsmType orig) {
        return getProvider().createType(orig, new TypeDescriptor(true, orig.isReference(), orig.getPointerDepth(), orig.getArrayDepth()));
    }

    /**
     * creates type dereferenced as *var or var[]
     * @param type dereferenced type
     * @return new type
     */
    public static CsmType createDereferencedType(CsmType type) {
        int arrDepth = type.getArrayDepth();
        int ptrDepth = type.getPointerDepth();
        // pointer type could be dereferenced with [] as well
        if (ptrDepth > 0) {
            ptrDepth--;
        } else {
            arrDepth = Math.max(arrDepth - 1, 0);
        }
        return getProvider().createType(type, new TypeDescriptor(type.isConst(), type.isReference(), ptrDepth, arrDepth));
    }

    //@Immutable
    public static final class OffsetDescriptor {
        private final CsmFile container;
        private final int start;
        private final int end;

        public OffsetDescriptor(CsmFile container, int start, int end) {
            this.container = container;
            this.start = start;
            this.end = end;
        }

        public CsmFile getContainer() {
            return container;
        }

        public int getEndOffset() {
            return end;
        }

        public int getStartOffset() {
            return start;
        }
    }

    //@Immutable
    public static final class TypeDescriptor {
        private final boolean _const;
        private final boolean _reference;
        private final int _ptrDepth;
        private final int _arrDepth;

        public TypeDescriptor(boolean _const, boolean _reference, int _ptrDepth, int _arrDepth) {
            this._const = _const;
            this._reference = _reference;
            this._ptrDepth = _ptrDepth;
            this._arrDepth = _arrDepth;
        }

        public int getArrDepth() {
            return _arrDepth;
        }

        public boolean isConst() {
            return _const;
        }

        public int getPtrDepth() {
            return _ptrDepth;
        }

        public boolean isReference() {
            return _reference;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl details
    private static TypesProvider getProvider() {
        if (ProviderInstanceHolder.provider == null) {
            throw new IllegalStateException("no any avaiable org.netbeans.modules.cnd.spi.model.TypesProvider instances");
        }
        return ProviderInstanceHolder.provider;
    }

    private static final class ProviderInstanceHolder {
        private static final TypesProvider provider = Lookup.getDefault().lookup(TypesProvider.class);
    }
}
