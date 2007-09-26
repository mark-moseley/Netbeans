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

package org.netbeans.spi.navigator;

import org.netbeans.modules.navigator.NavigatorTC;

/**
 * Set of methods for driving navigator behaviour.
 *
 * @author Dafe Simonek
 */
public final class NavigatorHandler {
    
    /** No external instantiation allowed.
     */
    private NavigatorHandler () {
    }
    
    /** 
     * Activates and shows given panel in navigator view. Panel must be one of  
     * available panels at the time this method is called, which means that 
     * panel must be registered (either through mime type in xml layer or NavigatorLookupHint)
     * for currently activated node in the system.  
     * Previously activated panel is deactivated and hidden.
     * <p>
     * Typical use case is to set preferred navigator panel in a situation 
     * when multiple panels are registered for multiple data types.   
     * <p>
     * This method must be called from EventQueue thread.
     * 
     * @param panel Navigator panel to be activated
     * @throws IllegalArgumentException if given panel is not available 
     */ 
    public static void activatePanel (NavigatorPanel panel) {
        NavigatorTC.getInstance().getController().activatePanel(panel);
    }
    
}
