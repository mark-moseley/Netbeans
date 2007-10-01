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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.refactoring.api.impl;

import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.util.Lookup;

/**
 * @author Jan Becicka
 */
public final class ActionsImplementationFactory {
    
    private ActionsImplementationFactory(){}
    
    private static final Lookup.Result<ActionsImplementationProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template(ActionsImplementationProvider.class));

    public static boolean canRename(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canRename(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doRename(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canRename(lookup)) {
                rafi.doRename(lookup);
                return;
            }
        }
    }

    public static boolean canFindUsages(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canFindUsages(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doFindUsages(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canFindUsages(lookup)) {
                rafi.doFindUsages(lookup);
                return;
            }
        }
    }
    public static boolean canDelete(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canDelete(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doDelete(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canDelete(lookup)) {
                rafi.doDelete(lookup);
                return;
            }
        }
    }
    
    public static void doMove(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canMove(lookup)) {
                rafi.doMove(lookup);
                return;
            }
        }
    }
    
    public static boolean canMove(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canMove(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doCopy(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canCopy(lookup)) {
                rafi.doCopy(lookup);
                return;
            }
        }
    }
    
    public static boolean canCopy(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canCopy(lookup)) {
                return true;
            }
        }
        return false;
    }
    
}
