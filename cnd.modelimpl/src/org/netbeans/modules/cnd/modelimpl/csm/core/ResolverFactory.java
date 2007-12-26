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

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * Creates an instance of appropriate resolver
 * and delegates work to it
 * @author vk155633
 */
public class ResolverFactory {

    //private static final boolean useNewResolver = Boolean.getBoolean("cnd.modelimpl.resolver2");
    //public static final int resolver = Integer.getInteger("cnd.modelimpl.resolver", 3).intValue(); // NOI18N

    /** prevents creation */
    private ResolverFactory() {
    }
    
    public static Resolver createResolver(CsmOffsetable context) {
        return createResolver(context, null);
    }
    public static Resolver createResolver(CsmOffsetable context, Resolver parent) {
//        if (useNewResolver)
//            return new Resolver2(context);
//        else
//            return new Resolver3(context);
        return new Resolver3(context, parent);
//        switch( resolver ) {
//            case 1: 
//                return new Resolver1(context, parent);
//            case 2: 
//                return new Resolver2(context, parent);
//            case 3: 
//                return new Resolver3(context, parent);
//            default:
//                return new Resolver1(context, parent);
//        }
    }

    public static Resolver createResolver(CsmFile file, int offset) {
        return createResolver(file, offset, null);
    }
    
    public static Resolver createResolver(CsmFile file, int offset, Resolver parent) {
//        if (useNewResolver)
//            return new Resolver2(file, offset);
//        else
//            return new Resolver3(file, offset);
        return new Resolver3(file, offset, parent);
//        switch( resolver ) {
//            case 1: 
//                return new Resolver1(file, offset, parent);
//            case 2: 
//                return new Resolver2(file, offset, parent);
//            case 3: 
//                return new Resolver3(file, offset, parent);
//            default:
//                return new Resolver1(file, offset, parent);
//        }
    }
    
}
