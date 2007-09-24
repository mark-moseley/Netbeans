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
package org.netbeans.modules.visualweb.xhtml;


/**
 * <b>IFramesetFramesetChoiceChoice</b> is generated from xhtml.rng by Relaxer.
 * Concrete classes of the interface are Frame, FramesetFrameset and Body.
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public interface IFramesetFramesetChoiceChoice {
    /**
     * @return String
     */
    String getId();

    /**
     * @param id
     */
    void setId(String id);

    /**
     * @return String[]
     */
    String[] getClassValue();

    /**
     * @param classValue
     */
    void setClassValue(String[] classValue);

    /**
     * @param classValue
     */
    void setClassValue(String classValue);

    /**
     * @param classValue
     */
    void addClassValue(String classValue);

    /**
     * @param classValue
     */
    void addClassValue(String[] classValue);

    /**
     * @return int
     */
    int sizeClassValue();

    /**
     * @param index
     * @return String
     */
    String getClassValue(int index);

    /**
     * @param index
     * @param classValue
     */
    void setClassValue(int index, String classValue);

    /**
     * @param index
     * @param classValue
     */
    void addClassValue(int index, String classValue);

    /**
     * @param index
     */
    void removeClassValue(int index);

    /**
     * @param classValue
     */
    void removeClassValue(String classValue);

    /**
     */
    void clearClassValue();

    /**
     * @return String
     */
    String getTitle();

    /**
     * @param title
     */
    void setTitle(String title);

    /**
     * @return String
     */
    String getStyle();

    /**
     * @param style
     */
    void setStyle(String style);
}
