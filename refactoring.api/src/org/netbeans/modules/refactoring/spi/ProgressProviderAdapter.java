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

package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 * Simple implementation of ProgressProvider
 * @see ProgressProvider
 * @author Jan Becicka
 */
public class ProgressProviderAdapter implements ProgressProvider {

    private ProgressSupport progressSupport;

    /**
     * Default constructor
     */
    protected ProgressProviderAdapter() {
    }
    
    /** Registers ProgressListener to receive events.
     * @param listener The listener to register.
     *
     */
    public synchronized void addProgressListener(ProgressListener listener) {
        if (progressSupport == null ) {
            progressSupport = new ProgressSupport();
        }
        progressSupport.addProgressListener(listener);
    }
    
    /** Removes ProgressListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        if (progressSupport != null ) {
            progressSupport.removeProgressListener(listener); 
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    protected final void fireProgressListenerStart(int type, int count) {
        if (progressSupport != null) 
            progressSupport.fireProgressListenerStart(this, type, count);
    }
    
    /** Notifies all registered listeners about the event.
     */
    protected final void fireProgressListenerStep() {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStep(this);
    }
    
    /**
     * Notifies all registered listeners about the event.
     * @param count 
     */
    protected final void fireProgressListenerStep(int count) {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStep(this, count);
    }
    
    /** Notifies all registered listeners about the event.
     */
    protected final void fireProgressListenerStop() {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStop(this);
    }
}
