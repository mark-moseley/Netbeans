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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;

/**
 * Used as return type for constructor and destructor
 * @author vk155633
 */
public class NoType implements CsmType {

    private static final NoType instance = new NoType();

    private Position position = new Position() {

        public int getOffset() {
            return 0;
        }

        public int getLine() {
            return 0;
        }

        public int getColumn() {
            return 0;
        }
    };
    
    /** prevents external creation */
    private NoType() {
    }
    
    public boolean isReference() {
        return false;
    }
    
    public boolean isPointer() {
        return false;
    }
    
    public boolean isConst() {
        return false;
    }
    
    public String getText() {
        return "";
    }
    
    public String getCanonicalText() {
	return "";
    }
    
    public Position getStartPosition() {
        return null;
    }

    public int getStartOffset() {
        return 0;
    }

    public int getPointerDepth() {
        return 0;
    }

    public Position getEndPosition() {
        return null;
    }

    public int getEndOffset() {
        return 0;
    }

    public CsmFile getContainingFile() {
        return null;
    }

    public CsmClassifier getClassifier() {
        return null;
    }

    public List<CsmType> getInstantiationParams() {
        return Collections.emptyList();
    }

    public boolean isInstantiation() {
        return false;
    }

    public boolean isTemplateBased() {
        return false;
    }

    public CharSequence getClassifierText() {
        return "";
    }

    public int getArrayDepth() {
        return 0;
    }
    
    public static NoType instance() {
        return instance;
    }

    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return false;
    }
}
