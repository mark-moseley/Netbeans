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

package com.sun.rave.designtime;

/**
 * <P>A BeanCreateInfoSet is a group version of the BeanCreateInfo interface.  It describes a
 * single item on a Palette that will create a set of beans in a visual designer.  This includes a
 * display name, description, icon, etc.  There is also (most importantly) a hook to
 * programmatically manipulate the newly created beans immediately after they have been created.
 * This is useful for setting the default state for the newly created set of beans.</P>
 *
 * <P>If the any of the specified JavaBeans have an associated DesignInfo, the DesignInfo's
 * 'beanCreatedSetup' method will be called before the BeanCreateInfoSet's 'beansCreatedSetup' method
 * will be called.  This gives the DesignInfo the "first crack", but it ultimately gives the
 * BeanCreateInfoSet the "last word".</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  The BasicBeanCreateInfoSet class can be used for convenience.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.impl.BasicBeanCreateInfoSet
 */
public interface BeanCreateInfoSet extends DisplayItem {

    /**
     * Returns an array of class names of the new JavaBeans to create when this BeanCreateInfoSet
     * is invoked in a visual designer.
     *
     * @return A String[] of fully qualified class names for the JavaBeans to create.
     */
    public String[] getBeanClassNames();

    /**
     * <p>A hook that gets called after the full set of JavaBean gets created.  This is useful for
     * a component author to setup an initial state for a set of JavaBeans when they are first
     * created.  Note that this method is only called one time after the JavaBeans are initially
     * created from the palette.  This is *not* a hook that is called each time the project is
     * reopened.</p>
     *
     * <P>If the any of the specified JavaBeans have an associated DesignInfo, the DesignInfo's
     * 'beanCreated' method will be called before each of the BeanCreateInfo's 'beanCreated' methods
     * are called.  Once all of the beans have been created, and the individual 'beanCreated' methods
     * have been called, this 'beansCreated' method will be called.  This gives the DesignInfo the
     * "first crack", but it ultimately gives the BeanCreateInfoSet the "last word".</P>
     *
     * @param designBeans The array of DesignBean objects representing the JavaBeans that have just been
     *        created.
     * @return A standard Result object, indicating success or failure - and optionally including
     *         messages for the user.
     */
    public Result beansCreatedSetup(DesignBean[] designBeans);
}
