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

package org.netbeans.editor;

/**
* Token-id with the fixed token image. The image text is provided
* in constructor and can be retrieved by <tt>getImage()</tt>.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseImageTokenID extends BaseTokenID implements ImageTokenID {

    private final String image;

    /** Construct new imag-token-id if the name is the same as the image. */
    public BaseImageTokenID(String nameAndImage) {
        this(nameAndImage, nameAndImage);
    }

    public BaseImageTokenID(String name, String image) {
        super(name);
        this.image = image;
    }

    public BaseImageTokenID(String nameAndImage, int numericID) {
        this(nameAndImage, numericID, nameAndImage);
    }

    public BaseImageTokenID(String name, int numericID, String image) {
        super(name, numericID);
        this.image = image;
    }

    public BaseImageTokenID(String nameAndImage, TokenCategory category) {
        this(nameAndImage, category, nameAndImage);
    }

    public BaseImageTokenID(String name, TokenCategory category, String image) {
        super(name, category);
        this.image = image;
    }

    public BaseImageTokenID(String nameAndImage, int numericID, TokenCategory category) {
        this(nameAndImage, numericID, category, nameAndImage);
    }

    public BaseImageTokenID(String name, int numericID, TokenCategory category, String image) {
        super(name, numericID, category);
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String toString() {
        return super.toString() + ", image='" + getImage() + "'"; // NOI18N
    }

}
