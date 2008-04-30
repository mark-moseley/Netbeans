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

package org.netbeans.modules.spring.beans;

import java.io.File;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.LockedDocument;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelManager;

/**
 *
 * @author Andrei Badea
 */
public abstract class SpringConfigModelAccessor {

    private static volatile SpringConfigModelAccessor accessor;

    public static void setDefault(SpringConfigModelAccessor accessor) {
        if (SpringConfigModelAccessor.accessor != null) {
            throw new IllegalStateException();
        }
        SpringConfigModelAccessor.accessor = accessor;
    }

    public static SpringConfigModelAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        try {
            Class.forName(SpringConfigModel.class.getName(), true, SpringConfigModel.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        return accessor;
    }

    public abstract SpringConfigModel createSpringConfigModel(SpringConfigFileModelManager fileModelManager, ConfigFileGroup configFileGroup);
    
    public abstract ConfigFileGroup getConfigFileGroup(SpringConfigModel model);

    public abstract DocumentAccess createDocumentAccess(SpringBeans springBeans, File file, LockedDocument lockedDoc);
}
