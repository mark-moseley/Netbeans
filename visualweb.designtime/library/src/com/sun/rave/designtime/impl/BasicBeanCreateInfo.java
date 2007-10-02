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

package com.sun.rave.designtime.impl;

import java.awt.Image;
import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of BeanCreateInfo to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see BeanCreateInfo
 */
public class BasicBeanCreateInfo implements BeanCreateInfo {

    protected String beanClassName;
    protected String displayName;
    protected String description;
    protected Image smallIcon;
    protected Image largeIcon;

    public BasicBeanCreateInfo() {}

    public BasicBeanCreateInfo(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public BasicBeanCreateInfo(String beanClassName, String displayName) {
        this(beanClassName);
        this.displayName = displayName;
    }

    public BasicBeanCreateInfo(String beanClassName, String displayName, String description) {
        this(beanClassName, displayName);
        this.description = description;
    }

    public BasicBeanCreateInfo(String beanClassName, Image smallIcon) {
        this(beanClassName);
        this.smallIcon = smallIcon;
    }

    public BasicBeanCreateInfo(String beanClassName, String displayName, Image smallIcon) {
        this(beanClassName, displayName);
        this.smallIcon = smallIcon;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : beanClassName != null ? beanClassName : null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSmallIcon(Image smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Image getSmallIcon() {
        return smallIcon;
    }

    public void setLargeIcon(Image largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Image getLargeIcon() {
        return largeIcon;
    }

    public Result beanCreatedSetup(DesignBean designBean) {
        return null;
    }

    public String getHelpKey() {
        return null;
    }
}
