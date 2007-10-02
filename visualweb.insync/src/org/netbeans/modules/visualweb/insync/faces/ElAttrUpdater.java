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
package org.netbeans.modules.visualweb.insync.faces;

import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.insync.markup.MarkupVisitor;


/**
 * A MarkupVisitor that updates EL reference attributes that match a pattern.
 * <code>
 *   #{<oldname><tail>} => #{<newname><tail>}
 * </code?
 *
 * @author cquinn
 */
public class ElAttrUpdater extends MarkupVisitor {
    protected String oldname;
    protected String newname;

    public ElAttrUpdater(String oldname, String newname) {
        this.oldname = oldname;
        this.newname = newname;
    }

    public static String update(String attr, String oldname, String newname) {
        if (attr.startsWith("#{") && attr.endsWith("}")) {  //NOI18N
            String expr = attr.substring(2, attr.length()-1);  // the expression from the delimiters
            if (expr.startsWith(oldname)) {
                String tail = expr.substring(oldname.length());  // everything to the right
                if (tail.length() == 0 || tail.startsWith(".") || tail.startsWith("[")) {  // make sure that was the end of a symbol
                    StringBuffer buf = new StringBuffer(expr.length() + newname.length());
                    buf.append("#{");  //NOI18N
                    buf.append(newname);
                    buf.append(tail);
                    buf.append("}");  //NOI18N
                    return buf.toString();
                }
            }
        }
        return null;
    }

    public void visit(Node node) {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            String val = node.getNodeValue();
            String newval = update(val, oldname, newname);
            if (newval != null)
                node.setNodeValue(newval);
        }
    }
}
