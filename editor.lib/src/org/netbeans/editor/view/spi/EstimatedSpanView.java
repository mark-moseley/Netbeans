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

package org.netbeans.editor.view.spi;

/**
 * Views that implement this interface
 * signal that they work in a mode when
 * they just estimate their size instead
 * of exact measurements that are likely
 * more expensive for computation.
 * <br>
 * By default the view should be in non-estimated mode
 * i.e. the exact measurements should be used
 * unless the estimated span flag is set.
 *
 * <p>
 * If a particular view does not implement this interface
 * then it is assumed that the view uses exact measurements.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface EstimatedSpanView {
    
    /**
     * Check whether this view is currently
     * in a mode when it just estimates its span.
     *
     * @return true if the view estimates its preferred horizontal
     *   and vertical preferred, maximum and minimum spans.
     *   Returns false if the view measurements are exact.
     */
    public boolean isEstimatedSpan();
    
    /**
     * Set whether this view uses estimated span computation
     * instead of exact measurements.
     *
     * @param estimatedSpan whether view will estimate
     *  the spans or use exact measurements.
     */
    public void setEstimatedSpan(boolean estimatedSpan);
    
}
