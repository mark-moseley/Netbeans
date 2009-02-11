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

package org.netbeans.modules.refactoring.api;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.Parameters;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Context contains "environment" in which the refactoring was invoked
 * e.g. Java refactoring might put instance of ClasspathInfo here
 * @see AbstractRefactoring
 * @author Jan Becicka
 */
public final class Context extends Lookup {

    private InstanceContent instanceContent;
    private AbstractLookup delegate;

    Context(InstanceContent instanceContent) {
        super();
        delegate = new AbstractLookup(instanceContent);
        this.instanceContent = instanceContent;
    }

    /**
     * Adds value instance into this context
     * for instance Java impl. puts instance of ClasspathInfo here.
     * If there is an instance already set for this context, old
     * value is replaced by new one.
     */
    public void add(@NonNull Object value) {
        Parameters.notNull("value", value); // NOI18N
        Object old = lookup(value.getClass());
        if (old!=null) {
            instanceContent.remove(old);
        }
        instanceContent.add(value);
    }

    public <T> T lookup(Class<T> clazz) {
        return delegate.lookup(clazz);
    }

    public <T> Result<T> lookup(Template<T> template) {
        return delegate.lookup(template);
    }
}
