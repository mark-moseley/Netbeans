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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;

/**
 * Source model
 *
 * @author Vladimir Kvashin
 */
public interface CsmModel {

    // TODO: write full description
    /** @param id Netbeans project */
    CsmProject getProject(Object id);

    Collection<CsmProject> projects();

    void addModelListener(CsmModelListener listener);

    void removeModelListener(CsmModelListener listener);
    
    void addProgressListener(CsmProgressListener listener);
    
    void removeProgressListener(CsmProgressListener listener);
    
    void addModelStateListener(CsmModelStateListener listener);
    
    void removeModelStateListener(CsmModelStateListener listener);
    
    /**
     * Code model calls can be very expensive.
     * Therefore one can never call code model from event dispatching thread.
     * Moreover, to make code model able to effectively solve synchronization issues,
     * all callers shall use not their own threads but call enqueue method instead.
     *
     * The method creates a thread and runs the given task in this thread.
     *
     * Whether or not the thread be created immediately or the task
     * will be just enqueued and runned later on, depends on implementation.
     *
     * @param task task to run
     */
    void enqueue(Runnable task);

    /**
     * Code model calls can be very expensive. 
     * Therefore one can never call code model from event dispatching thread.
     * Moreover, to make code model able to effectively solve synchronization issues,
     * all callers shall use not their own threads but call enqueue method instead.
     *
     * The method creates a thread and runs the given task in this thread.
     *
     * Whether or not the thread be created immediately or the task
     * will be just enqueued and runned later on, depends on implementation.
     *
     * We recommend using this method rather than one without <code>name</code> parameter.
     *
     * @param task task to run
     * @param name name that would be added to the thread name
     */
    void enqueue(Runnable task, String name);
    
    /**
     * Find project that contains file.
     * Returns CsmFile if project is found.
     *
     * This function might be costly (this depends on the model state).
     *
     * CAUTION: this method should never be called directly from the thread, 
     * in which model notificatios (either CsmModelListener or CsmProgressListener) come.
     * These notifications come directly in parser thread or project initialization thread.
     * Calling findFile from these threads may cause deadlock.
     *
     * TODO: add CsmFile[] findFiles(String absPath) since there might be multiple CsmFile instances,
     * associated with the same path, in the case the same file is included into different projects
     *
     * @param absPath absolute file path
     */
    CsmFile findFile(String absPath);
    
    /**
     * Returns the state of the model
     */
    CsmModelState getState();
}
