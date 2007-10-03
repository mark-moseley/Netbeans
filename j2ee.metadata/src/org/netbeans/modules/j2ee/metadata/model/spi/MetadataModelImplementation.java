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

package org.netbeans.modules.j2ee.metadata.model.spi;

import java.io.IOException;
import java.util.concurrent.Future;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;

/**
 * The SPI for <code>MetadataModel</code>.
 *
 * @see MetadataModelFactory
 *
 * @author Andrei Badea
 * @since 1.2
 */
public interface MetadataModelImplementation<T> {

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#runReadAction}.
     *
     * @param  action the action to be executed; never null.
     * @return the value returned by the action's {@link MetadataModelAction#run} method.
     * @throws MetadataModelException if the action's <code>run()</code> method
     *         threw a checked exception.
     * @throws IOException if an error occured while reading the model from its storage.
     */
    <R> R runReadAction(MetadataModelAction<T, R> action) throws MetadataModelException, IOException;

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#isReady}.
     *
     * @return true if the model is ready, false otherwise.
     */
    boolean isReady();

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#runReadActionWhenReady}.
     *
     * @param  action the action to be executed; never null.
     * @return a {@link Future} encapsulating the value returned by the action's {@link MetadataModelAction#run} method.
     * @throws MetadataModelException if the action's <code>run()</code> method
     *         threw a checked exception.
     * @throws IOException if an error occured while reading the model from its storage.
     */
    <R> Future<R> runReadActionWhenReady(MetadataModelAction<T, R> action) throws MetadataModelException, IOException;
}
