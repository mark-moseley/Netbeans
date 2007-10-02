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
package org.netbeans.modules.visualweb.insync.beans;


/**
 * Common superclass for all beans package nodes.
 *
 * @author cquinn
 */
public abstract class BeansNode {

    protected final BeansUnit unit;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a populated root node
     *
     * @param unit The BeansUnit that this node will belong to.
     */
    protected BeansNode(BeansUnit unit) {
        this.unit = unit;
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @return This node's Unit.
     */
    public BeansUnit getUnit() {
        return unit;
    }

    //--------------------------------------------------------------------------------------- Output

    /**
     * Utility function for debugging--return the name-part of this object's class name
     *
     * @return The name-part of this object's class name.
     */
    protected String clzName() {
        String cls = getClass().getName();
        int pre = cls.lastIndexOf('.');
        return cls.substring(pre + 1);
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(30);
        sb.append("[");
        sb.append(clzName());
        toString(sb);
        sb.append("]");
        return sb.toString();
    }

    /**
     * Helper method for this class hierarchy to simplify and speed up toString() implementation.
     *
     * @param sb  StringBuffer to toString() this object into.
     */
    public void toString(StringBuffer sb) {
    }

}
