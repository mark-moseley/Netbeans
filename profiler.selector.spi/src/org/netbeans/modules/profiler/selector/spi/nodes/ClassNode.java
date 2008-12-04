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

package org.netbeans.modules.profiler.selector.spi.nodes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;


/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class ClassNode extends ContainerNode {
    private boolean anonymous;

    /**
     * A {@linkplain Comparator} able to compare {@linkplain ClassNode} instances
     */
    public static final Comparator COMPARATOR = new Comparator<ClassNode>() {
        public int compare(ClassNode o1, ClassNode o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    private static class ClassChildren extends SelectorChildren<ClassNode> {
        protected List<SelectorNode> prepareChildren(final ClassNode parent) {
            List<SelectorNode> contents = new ArrayList<SelectorNode>();
            SelectorNode content = null;

            if (!parent.isAnonymous()) { // no constructors for anonymous inner classes
                content = parent.getConstructorsNode();

                if (content != null && !content.isLeaf()) {
                    contents.add(content);
                }
            }

            content = parent.getMethodsNode();

            if (content != null && !content.isLeaf()) {
                contents.add(content);
            }

            content = parent.getInnerClassesNode();

            if (content != null && !content.isLeaf()) {
                contents.add(content);
            }

            return contents;
        }
    }


    /** Creates a new instance of ClassNode */
    public ClassNode(String className, String displayName, Icon icon, boolean isAnonymous, final ContainerNode parent) {
        super(className, displayName, icon, parent);
        this.anonymous = isAnonymous;
    }

    public ClassNode(String className, String displayName, boolean isAnonymous, final ContainerNode parent) {
        super(className, displayName, IconResource.CLASS_ICON, parent);
        this.anonymous = isAnonymous;
    }

    final protected SelectorChildren getChildren() {
        return new ClassChildren();
    }

    /**
     * Is class an anonymous one
     * @return Returns true if the {@linkplain ClassNode} represents an anonymous class; false otherwise
     *
     */
    final public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * The implementation will take care of generating the appropriate {@linkplain ConstructorsNode} instance
     * @return Returns a specific {@linkplain ConstructorsNode} instance or NULL
     */
    abstract protected ConstructorsNode getConstructorsNode();

    /**
     * The implementation will take care of generating the appropriate {@linkplain MethodsNode} instance
     * @return Returns a specific {@linkplain MethodsNode} instance or NULL
     */
    abstract protected MethodsNode getMethodsNode();

    /**
     * The implementation will take care of generating the appropriate {@linkplain InnerClassesNode} instance
     * @return Returns a specific {@linkplain InnerClassesNode} instance or NULL
     */
    abstract protected InnerClassesNode getInnerClassesNode();
}
