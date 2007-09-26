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
 * The Original Software is NetBeans.
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.openide.text;

import org.openide.util.Lookup;

/**
 * Allows to find another {@link CloneableEditorSupport} that all the
 * requests passed to given one should be redirected to. This is useful
 * for redirecting operation on <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
 * FileObject</a> to another one in cases when two <code>FileObject</code>s
 * represent the same physical file.
 * <p>
 * Instances should be registered to default lookup.
 * @author Jaroslav Tulach
 * @since 6.13
 */
public abstract class CloneableEditorSupportRedirector {
    /** Find a delegate for given {@link CloneableEditorSupport}'s {@link Lookup}.
     * The common code can be to extract for example a 
     * <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
     * FileObject</a> from the lookup and use its location to find another
     * <code>CloneableEditorSupport</code> to delegate to.
     * 
     * @param env the environment associated with current CloneableEditorSupport
     * @return null or another CloneableEditorSupport to use as a replacement
     */
    protected abstract CloneableEditorSupport redirect(Lookup env);
    
    static CloneableEditorSupport findRedirect(CloneableEditorSupport one) {
        for (CloneableEditorSupportRedirector r : Lookup.getDefault().lookupAll(CloneableEditorSupportRedirector.class)) {
            CloneableEditorSupport ces = r.redirect(one.getLookup());
            if (ces != null && ces != one) {
                return ces;
            }
        }
        return null;
    }
}


