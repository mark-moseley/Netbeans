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
package org.netbeans.modules.visualweb.faces.dt.component.html;

import com.sun.rave.designtime.markup.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.html.HtmlPanelGroup;
import org.w3c.dom.*;

public class HtmlPanelGroupDesignInfo extends HtmlDesignInfoBase implements MarkupDesignInfo {
    public Class getBeanClass() { return HtmlPanelGroup.class; }

    public void customizeRender(MarkupDesignBean bean, MarkupRenderContext renderContext) {
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();

        if (bean.isContainer() && bean.getChildBeanCount() > 0) {
            return;
        }
        if (begin == end) { // table didn't render anything.... that's not right! Insert one? TODO
            return;
        }
        assert begin.getUnderParent() == end.getUnderParent();
        Node child = begin.getBeforeSibling();
        Node stop = end.getBeforeSibling();
        for (child = begin.getBeforeSibling(); child != stop; child = child.getNextSibling()) {
            if (child instanceof Element) {
                Element e = (Element)child;
                String style = e.getAttribute("style"); //NOI18N
                if (style == null) {
                    style = ""; //NOI18N
                }
                // This is wrong because "font-height" will be a match for
                // example
                if (e.getChildNodes().getLength() == 0 && style.indexOf("position:") == -1) { // NOI18N
                    // An empty group panel in flow formatting context -- it will render
                    // to absolutely nothing visible, so put its bean name in here as a help
                    e.appendChild(e.getOwnerDocument().createTextNode(bean.getInstanceName()));
                    e.setAttribute("class", "rave-uninitialized-text"); // NOI18N
                    break;
                } else if (style.indexOf("width") < 0 || style.indexOf("height") < 0) { //NOI18N
                    StringBuffer sb = new StringBuffer(style.length()+30);
                    sb.append(style);
                    if (!style.endsWith(";")) { //NOI18N
                        sb.append(';');
                    }
                    // Set display: block  - work around the fact that
                    // the group panel uses a <span> which of course
                    // is an inline tag and therefore ignores our width
                    // and height settings.
                    sb.append("display:block;"); // NOI18N
                    if (style.indexOf("width") < 0) { //NOI18N
                        sb.append(" width: 96px;"); //NOI18N
                    }
                    if (style.indexOf("height") < 0) { //NOI18N
                        sb.append(" height: 96px;"); //NOI18N
                    }
                    style = sb.toString();
                    e.setAttribute("style", style); //NOI18N
                }
                // Ensure that we show a border
                e.setAttribute("class", "rave-design-border"); // NOI18N
                break;
            }
        }
    }

}
