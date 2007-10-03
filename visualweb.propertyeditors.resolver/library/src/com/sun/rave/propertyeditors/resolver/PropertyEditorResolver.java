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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
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
package com.sun.rave.propertyeditors.resolver;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

/**
 * Defines a resolver responsible for determining a property editor suitable for
 * editing a property, as described by a property descriptor. Implementing classes
 * may use any information in the property descriptor to determine an appropriate
 * editor.
 *
 * <p>Typically, the IDE will provide at least one implementation. Implementations
 * are made available using the service provider interface. Within the module or
 * library JAR, the file
 * {@code META-INF/services/com.sun.rave.propertyeditors.resolver.PropertyEditorResolver}
 * contains the name of the Java class that implements {@link PropertyEditorResolver}.
 * Each implementation discovered during the service provider lookup will be asked,
 * in turn, to provide an editor. The first editor returned is used. If an 
 * implementation is provided via a module, it may be given a numbered
 * priority, which will guarantee that it is invoked before any resolvers supplied by
 * the IDE.
 */
public interface PropertyEditorResolver {
    
    /**
     * Returns a property editor suitable for editing the property described by
     * the property descriptor specified. If the resolver does not know of a suitable
     * editor, returns null.
     */
    public PropertyEditor getEditor(PropertyDescriptor propertyDescriptor);
    
}
