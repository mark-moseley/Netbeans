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

import javax.swing.text.View;

/**
 * Interface describing a view being fragment of some original view.
 * <br>
 * It provides information that the fragment usually has to know anyway
 * but that it's not available through
 * the {@link javax.swing.text.View} class that the fragment is instance of.
 *
 * <p>
 * The instances of this interface are created once fragments
 * of a view have to created e.g. for line-wrapping purposes.
 * <br>
 * If the returned fragment does not implement this interface
 * a default wrapper gets created.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface ViewFragment {

    /**
     * Return original view for which this wrapper is being created.
     */
    public View getOriginalView();
    
    /**
     * Return relative starting offset against the starting offset
     * of the original view.
     */
    public int getRelativeStartOffset();
    
    /**
     * Return relative ending offset against the starting offset
     * of the original view.
     */
    public int getRelativeEndOffset();

}
