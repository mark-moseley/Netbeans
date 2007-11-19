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

package org.netbeans.api.java.source;


import java.io.IOException;
import static org.netbeans.api.java.source.JavaSource.Phase.*;

/** Class for explicit invocation of compilation phases on a java source.
 *  The implementation delegates to the {@link CompilationInfo} to get the data,
 *  the access to {@link CompilationInfo} is not synchronized, so the class isn't
 *  reentrant.
 *  
 *  XXX: make toPhase automatic in getTrees(), Trees.getElement, etc....
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationController extends CompilationInfo {
    
    
    CompilationController(final CompilationInfoImpl impl) {        
        super(impl);

    }
        
    // API of the class --------------------------------------------------------
    
    /** Moves the state to required phase. If given state was already reached 
     * the state is not changed. The method will throw exception if a state is 
     * illegal required. Acceptable parameters for thid method are <BR>
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.PARSED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.UP_TO_DATE}   
     * @param phase The required phase
     * @return the reached state
     * @throws IllegalArgumentException in case that given state can not be 
     *         reached using this method
     * @throws IOException when the file cannot be red
     */    
    public JavaSource.Phase toPhase(JavaSource.Phase phase ) throws IOException {
        checkConfinement();
        return this.impl.toPhase(phase);
    }                
}
