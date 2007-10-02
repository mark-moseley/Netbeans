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

package org.netbeans.modules.visualweb.extension.openide.cookies;

// Copied from openide/src/org/openide/cookies, it was a wrong place for this.
import org.openide.nodes.Node;

/** Cookie used to communicate that a node has the capability
 * to export a PaletteItem to the drag clipboard (without actually
 * constructing the transferable).  Used to quickly check whether
 * a set of nodes represent a potential drop operation into the
 * designer when dnd is not actually in effect.
 *
 * @todo This cookie should have a second method which actually
 *   returns the palette item set, instead of clients having to
 *   using the dnd stuff to locate the set.  I didn't do that
 *   yet because this requires PaletteItemSet to move from toolbox
 *   into openide (or using Object as a return type with a required
 *   cast) so we'll revisit this after TP.
 *
 * @author Tor Norbye
 */
public interface RavePaletteItemSetCookie extends Node.Cookie {
    /**
     * Report whether any palette items are available, without actually
     * creating them.
     * @return true if there are palette items in the set, false
     *   if the set is empty.
     */
    public boolean hasPaletteItems();

    /**
     * If hasPaletteItems is true, this method may provide an array
     * of class names for beans included in the palette items.
     * These may be used during drag & drop operations to decide
     * if the palette items can be dropped on top of other components.
     * These are the same classes that if the PaletteItems are
     * BeanPaletteItems, getBeanClassName() would return.
     */
    public String[] getClassNames();
}
