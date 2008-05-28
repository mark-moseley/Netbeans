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

package org.netbeans.modules.db.sql.editor;
import org.netbeans.editor.LocaleSupport;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;


/**
 * Instalation class of module properties syntax.
 *
 * @author Jesse Beaumont based on code by
 * Petr Jiricka, Libor Kramolis, Jesse Glick
 */
public class RestoreColoring extends ModuleInstall {

    /**
     * <code>Localizer</code> passed to editor.
     */
    private static LocaleSupport.Localizer localizer;

    /**
     * Registers properties editor, installs options and copies settings. 
     * Overrides superclass method.  
     */
    public void restored() {
        installOptions();
    }

    /** 
     * Uninstalls properties options. 
     * And cleans up editor settings copy. 
     * Overrides superclass method. 
     */
    public void uninstalled() {
        uninstallOptions();
    }

    /** 
     * Installs properties editor and print options. 
     */
    public void installOptions() {
        // Adds localizer.
        LocaleSupport.addLocalizer(localizer = new LocaleSupport.Localizer() {
            public String getString(String key) {
                return NbBundle.getMessage(RestoreColoring.class, key);
            }
        });
    }

    /** Uninstalls properties editor and print options. */
    public void uninstallOptions() {
        // remove localizer
        LocaleSupport.removeLocalizer(localizer);
    }
    
}
