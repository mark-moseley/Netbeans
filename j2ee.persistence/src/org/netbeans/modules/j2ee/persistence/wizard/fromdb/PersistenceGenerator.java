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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/** This interface allows project implementation to provide a custom
 * generator of ORM Java classes from a DB model. An instance of this
 * interface should be registered in project lookup. 
 *
 * If there is no instance the default generator will be used.
 *
 * @author Pavel Buzek
 */
public interface PersistenceGenerator {
    
    void init(WizardDescriptor wiz);
    
    void uninit();
    
    String getFQClassName(String tableName);
    
    String generateEntityName(String className);
    
    /** 
     * Generates entity beans / entity classes based on the model represented
     * by the given <code>helper</code>.
     * 
     * @param progressPanel the panel for displaying progress during the generation, 
     * or null if no panel should be displayed.
     * @param helper the helper that specifies the generation options 
     * @param dcschemafile the schema for generating.
     * @param progressContributor the progress contributor for the generation process.
     * 
     */
    void generateBeans(final ProgressPanel progressPanel,
            final RelatedCMPHelper helper,
            final FileObject dbschemaFile,
            final ProgressContributor progressContributor) throws IOException;
    
    /**
     * @return a set of <code>FileObject</code>s representing 
     * the generated classes or an empty set if no classes were generated, never null.
     */ 
    Set<FileObject> createdObjects();
}