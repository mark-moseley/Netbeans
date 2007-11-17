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

package org.netbeans.lib.editor.hyperlink.spi;

import java.util.Set;
import javax.swing.text.Document;

/**
 * This interface should be implemented by anyone who whats to provide hyperlinking
 * functionality in the source code.
 * <br>
 * Its methods are called for all the opened editors of the given mime-type
 * where the hyperlinking functionality gets requested.
 *
 * <p>
 * The providers need to be registered.
 * For NetBeans IDE, the default approach is to use System FileSystem.
 * <br>
 * The HyperlinkProvider(s) should be registered as ".instance" objects under
 * <code>Editors/&lt;mime-type&gt;/HyperlinkProviders</code> directory.
 * </p>
 * 
 * <p>
 * Please see {@link org.netbeans.lib.editor.hyperlink.HyperlinkProviderManager}
 * for more details.
 * </p>
 *
 * <p>
 * Note: there is no assurance on the order of calling of the methods in this class.
 * The callers may call the methods in any order and even do not call some of these methods
 * at all.
 * </p>
 *
 * @author Jan Lahoda
 * @since 1.18
 */
public interface HyperlinkProviderExt {
    
    /**Returns all hyperlink types that are supported by this HyperlinkProvider.
     * the resulting value should be constant over time.
     *
     * @return supported hyperlink types
     * @since 1.18
     */
    Set<HyperlinkType> getSupportedHyperlinkTypes();
    
    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given parameters.
     * <br>
     * This method is called from event dispatch thread.
     * It should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @param type the hyperlink type
     * @return true if the provided offset should be in a hyperlink
     *         false otherwise
     * @since 1.18
     */
    boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type);
    
    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread.
     * This method should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @param type the hyperlink type
     * @return a two member array which contains starting and ending offset of a hyperlink
     *         that should be on a given offset
     * @since 1.18
     */
    int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type);
    
    /**
     * The implementor should perform an action
     * corresponding to clicking on the hyperlink on the given offset. The
     * nature of the action is given by the nature of given hyperlink, but
     * generally should open some resource or move cursor
     * to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @param type the hyperlink type
     * @since 1.18
     */
    void performClickAction(Document doc, int offset, HyperlinkType type);
    
    /** Get a short description/tooltip corresponding to the given offset.
     *  Should block until the result is computed. Is called in a working thread,
     *  not in AWT Event dispatch thread. Return <code>null</code> if there should
     *  be no tooltip.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @param type the hyperlink type
     * @since 1.18
     */
    String getTooltipText(Document doc, int offset, HyperlinkType type);
    
}
