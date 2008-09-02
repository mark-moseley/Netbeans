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

package org.netbeans.modules.php.project.ui.customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik, Radek Matous
 */
public class CompositePanelProviderImpl implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String SOURCES = "Sources"; // NOI18N
    public static final String RUN = "Run"; // NOI18N
    public static final String PHP_INCLUDE_PATH = "PhpIncludePath"; // NOI18N

    private final String name;

    public CompositePanelProviderImpl(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ProjectCustomizer.Category toReturn = null;
        final ProjectCustomizer.Category[] categories = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_Sources"),
                    null,
                    categories);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_RunConfig"),
                    null,
                    categories);
        } else if (PHP_INCLUDE_PATH.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    PHP_INCLUDE_PATH,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_PhpIncludePath"),
                    null,
                    categories);
        }
        assert toReturn != null : "No category for name: " + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        PhpProjectProperties uiProps = context.lookup(PhpProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(category, uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps, category);
        } else if (PHP_INCLUDE_PATH.equals(nm)) {
            return new CustomizerPhpIncludePath(category, uiProps);
        }
        return new JPanel();
    }

    public static CompositePanelProviderImpl createSources() {
        return new CompositePanelProviderImpl(SOURCES);
    }

    public static CompositePanelProviderImpl createRunConfig() {
        return new CompositePanelProviderImpl(RUN);
    }
    
    public static CompositePanelProviderImpl createPhpIncludePath() {
        return new CompositePanelProviderImpl(PHP_INCLUDE_PATH);
    }
}
