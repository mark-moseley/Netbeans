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
import java.util.List;

/**
 * Represents a source file
 * @author Vladimir Kvashin
 */
public interface CsmFile extends CsmNamedElement, CsmScope, CsmValidable, CsmIdentifiable<CsmFile> {

    /** Gets this file absolute path */
    CharSequence getAbsolutePath();

    /** Gets the project, to which the file belongs*/
    CsmProject getProject();

    /** Gets this file text */
    CharSequence getText();

    /** Gets this file text */
    CharSequence getText(int start, int end);

    /** Sorted (by start offset) list of #include directives in the file */
    List<CsmInclude> getIncludes();
    
    /** Sorted (by start offset) list of declarations in the file */
    List<CsmOffsetableDeclaration> getDeclarations();
    
    /** Sorted (by start offset) list of #define directives in the file */
    List<CsmMacro> getMacros();
    
    /** 
     * Returns true if the file has been already parsed
     * (i.e. was parsed since last change),
     * otherwise false 
     */
    boolean isParsed();
    
    /*
     * Checks whether the file needs to be parsed,
     * if yes, scedules parsing this file.
     * If wait parameter is true, waits until this file is parsed.
     * If the file is already parsed, immediately returns.
     *
     * @param wait determines whether to wait until the file is parsed:
     * if true, waits, otherwise doesn't wait, just puts the given file
     * into parser queue
     */
    void scheduleParsing(boolean wait) throws InterruptedException;
    
    /** returns true if file is source file. */
    boolean isSourceFile();

    /** returns true if file is header file. */
    public boolean isHeaderFile();
    
}
