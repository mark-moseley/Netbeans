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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerInterruptedTest extends PersistenceTestCase {
    
    public PersistentObjectManagerInterruptedTest(String name) {
        super(name);
    }
    
    public void testInterrupted() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                // first checking that the manager does not (for any reason) initialize temporarily
                ObjectProviderImpl provider = new ObjectProviderImpl(false);
                PersistentObjectManager<PersistentObject> manager = helper.createPersistentObjectManager(provider);
                manager.getObjects();
                assertFalse(manager.temporary);
                // now checking that the manager initializes temporarily when ObjectProvider.createInitialObjects throws InterruptedException
                provider = new ObjectProviderImpl(true);
                manager = helper.createPersistentObjectManager(provider);
                manager.getObjects();
                assertTrue(manager.temporary);
            }
        });
    }
    
    private static final class ObjectProviderImpl implements ObjectProvider<PersistentObject> {
        
        private final boolean interruptible;

        public ObjectProviderImpl(boolean interruptible) {
            this.interruptible = interruptible;
        }

        public List<PersistentObject> createInitialObjects() throws InterruptedException {
            if (interruptible) {
                throw new InterruptedException();
            } else {
                return Collections.emptyList();
            }
        }

        public List<PersistentObject> createObjects(TypeElement type) {
            throw new UnsupportedOperationException();
        }

        public boolean modifyObjects(TypeElement type, List<PersistentObject> objects) {
            throw new UnsupportedOperationException();
        }
    }
}
