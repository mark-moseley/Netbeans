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

package org.netbeans.modules.extexecution.api.input;

import java.io.Closeable;
import java.io.IOException;

/**
 * Processes chars read by {@link InputReader}.
 * <p>
 * When the implementation is used just by single InputReader it
 * does not have to be thread safe.
 *
 * @author Petr Hejl
 * @see InputReader
 */
public interface InputProcessor extends Closeable {

    /**
     * Processes the characters.
     *
     * @param chars characters to process
     * @throws IOException if any processing error occurs
     */
    void processInput(char[] chars) throws IOException;

    /**
     * Notifies the processor that it should reset its state.
     * <p>
     * The circumstances when this method is called must be defined
     * by the particular {@link InputReader}.
     * <p>
     * <div class="nonnormative">
     * For example reset is called by reader returned from
     * {@link InputReaders#forFileInputProvider(org.netbeans.modules.extexecution.api.input.InputReaders.FileInput.Provider)}
     * when the provided file is changed.
     * </div>
     *
     * @throws IOException if error occurs while reseting
     */
    void reset() throws IOException;

    /**
     * Closes the processor releasing the resources held by it.
     */
    void close() throws IOException;

}
