/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.model.services;

import javax.swing.text.Document;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public abstract class CsmOperatorKindResolver {
    /**
     * default instance
     */
    private static CsmOperatorKindResolver DEFAULT = new Default();
    
    protected CsmOperatorKindResolver() {
    }
    
    /**
     * Static method to obtain the CsmOperatorKindResolver implementation.
     * @return the resolver
     */
    public static CsmOperatorKindResolver getDefault() {
        return DEFAULT;
    }
    
    public enum OperatorKind {
        BINARY,
        UNARY,
        SEPARATOR,
        TYPEMODIFIER,
        UNKNOWN;
    }
    
    /**
     * Detect operator kind
     * for example:
     * Document a*b;
     * Offset point to * (start position)
     * Result is TypeModifier or Binary
     * Possible requestes about:
     * *, &, +, -, <, >.
     */
    public abstract OperatorKind getKind(Document doc, int offset);
    
    /**
     * Implementation of the default resolver
     */  
    private static final class Default extends CsmOperatorKindResolver {
        private final Lookup.Result<CsmOperatorKindResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmOperatorKindResolver.class);
        }

        public OperatorKind getKind(Document doc, int offset) {
            for (CsmOperatorKindResolver resolver : res.allInstances()) {
                OperatorKind out = resolver.getKind(doc, offset);
                if (out != OperatorKind.UNKNOWN) {
                    return out;
                }
            }
            return OperatorKind.UNKNOWN;
        }
    }
}
