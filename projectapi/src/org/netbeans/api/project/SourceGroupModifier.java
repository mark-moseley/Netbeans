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

package org.netbeans.api.project;

import org.netbeans.spi.project.SourceGroupModifierImplementation;

/**
 * <code>SourceGroupModifier</code> provides ways of create specific folders ({@link org.netbeans.api.project.SourceGroup} root folders)
 * in case they don't exist, eg. cannot be retrieved from {@link org.netbeans.api.project.Sources}
 * The project type supporting automated creation of {@link org.netbeans.api.project.SourceGroup} root folders needs to
 * provide {@link org.netbeans.spi.project.SourceGroupModifierImplementation} in the project's lookup.
 *
 * @since org.netbeans.modules.projectapi 1.24
 * @author mkleint
 */
public final class SourceGroupModifier {

    private SourceGroupModifier() {
    }

    /**
     * Creates a {@link org.netbeans.api.project.SourceGroup} in the given {@link org.netbeans.api.project.Project} of the given type and hint.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     *
     * @param project
     * @param type constant for type of sources
     * @param hint
     * @return the created SourceGroup or null
     */
    public static final SourceGroup createSourceGroup(Project project, String type, String hint) {
        assert project != null;
        SourceGroupModifierImplementation impl = project.getLookup().lookup(SourceGroupModifierImplementation.class);
        if (impl == null) {
            return null;
        }
        return impl.createSourceGroup(type, hint);
    }

    /**
     * Creates a {@link org.netbeans.api.project.SourceGroupModifier.Future} object
     * that is capable of lazily creating {@link org.netbeans.api.project.SourceGroup} in the given {@link org.netbeans.api.project.Project} of the given type and hint.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     * @param project
     * @param type constant for type of sources
     * @param hint
     * @return Future instance that is capable of creating a SourceGroup or null
     */
    public static final SourceGroupModifier.Future createSourceGroupFuture(Project project, String type, String hint) {
        assert project != null;
        SourceGroupModifierImplementation impl = project.getLookup().lookup(SourceGroupModifierImplementation.class);
        if (impl == null) {
            return null;
        }
        return new Future(impl, type, hint);
    }

    /**
     * A wrapper class that is capable of lazily creating a {@link org.netbeans.api.project.SourceGroup} instance.
     */
    public static final class Future {

        SourceGroupModifierImplementation impl;
        private String hint;
        private String type;

        private Future(SourceGroupModifierImplementation im, String type, String hint) {
            this.impl = im;
            this.hint = hint;
            this.type = type;
        }

        /**
         * Create the instance of {@link org.netbeans.api.project.SourceGroup} wrapped by
         * this object.
         * @return
         */
        public final SourceGroup createSourceGroup() {
            return impl.createSourceGroup(type, hint);
        }


        /**
         * The type of sources associated with the current instance.
         * @return type constant for type of sources
         */
        public String getType() {
            return type;
        }

        /**
         * The source hint associated with the current instance.
         * @return hint constant for type of sources
         */
        public String getHint() {
            return hint;
        }
    }
}
