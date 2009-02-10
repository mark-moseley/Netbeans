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

package org.netbeans.spi.project;

import org.netbeans.api.project.SourceGroup;

/**
 * The SPI side of {@link org.netbeans.api.project.SourceGroupModifier}.
 * Expected to be present in project lookup of project types supporting automated creation
 * of {@link org.netbeans.api.project.SourceGroup} root folders.
 * @since org.netbeans.modules.projectapi 1.24
 * @author mkleint
 */
public interface SourceGroupModifierImplementation {
    /**
     * Creates a {@link org.netbeans.api.project.SourceGroup} of the given type and hint.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     * If the SourceGroup's type/hint is not supported, the implementation shall silently return null and not throw any exceptions.
     * If the SourceGroup of given type/hint already exists it shall be returned as well.
     *
     * @param project
     * @param type constant for type of sources
     * @param hint
     * @return the created or existing SourceGroup or null
     */

    public SourceGroup createSourceGroup(String type, String hint);

}
