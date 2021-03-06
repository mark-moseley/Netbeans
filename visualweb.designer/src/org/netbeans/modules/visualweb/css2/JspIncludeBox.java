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
package org.netbeans.modules.visualweb.css2;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.net.URL;
import org.openide.ErrorManager;

import org.openide.util.NbBundle;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * XXX This shouldn't be in the desinger, there shouldn't be any notion of fragments here.
 * 
 * JspIncludeBox represents a &lt;% include file="url" %&gt; tag in the jsp markup. It will force
 * the contents to be displayed in a block box.
 *
 * @author Tor Norbye
 */
public class JspIncludeBox extends ExternalDocumentBox {
    /**
     * Element used for looking up styles for this inclusion box. That
     * way I can look up the color, width, height etc. of the page fragment root element
     * rather than the jsp.directive.include element
     */
//    private RaveElement styleElement;
    private Element styleElement;

    /** Use the "getJspIncludeBox" factory method instead */
    private JspIncludeBox(/*WebForm frameForm,*/ WebForm webform, Element element, /*URL url,*/
        BoxType boxType, boolean inline, boolean replaced) {
        super(webform.getPane(), /*frameForm,*/ webform, element, /*url,*/ boxType,
        // The jsp:directive.include directive should be block formatted.
        // However, it's not easy to include a
        // jsp:directive.include { display : block } rule in the default
        // stylesheet because the batik parser mishandles escapes
        // (the correct "jsp\:directive.include" doesn't work) so instead
        // just hardcode the block formatting knowledge here
        //inline,
        false, replaced);
    }

    /** Create a new framebox, or provide one from a cache */
    public static CssBox getJspIncludeBox(CreateContext context, WebForm webform, Element element,
        BoxType boxType, HtmlTag tag, boolean inline) {
//        URL src = getContentURL(webform, element); // TODO - check for null here!
//        WebForm frameForm = null;
//
//        if (src != null) {
////            frameForm = findForm(webform, src);
//            frameForm = webform.findExternalForm(src);
//        }

//        if (frameForm == WebForm.EXTERNAL) {
//            frameForm = null;
//        }

        JspIncludeBox box =
            new JspIncludeBox(/*frameForm,*/ webform, element, /*src,*/ boxType, inline, tag.isReplacedTag());
        
        WebForm frameForm = box.getExternalForm();
        if (frameForm != null) {
            if (context.isVisitedForm(frameForm)) {
                return new StringBox(webform, element, boxType,
                    NbBundle.getMessage(JspIncludeBox.class, "RecursiveFrame"), null, AUTO, AUTO);
            }

            // XXX Moved to designer/jsf/../JsfForm.
//            //context.visitForm(frameForm);
//            frameForm.setContextPage(webform);
            
            // XXX #110849 This is bad. The layout depends on whether the CSS was computed. Bad architecture.
            // This needs to be cleared, because otherwise when used in fragment it would yield bad result for context page.
            if (frameForm != null) {
                CssProvider.getEngineService().clearComputedStylesForElement(frameForm.getHtmlBody());
            }
        }

        
        if ((frameForm != null) && (frameForm.getHtmlBody() != null)) {
            box.styleElement = frameForm.getHtmlBody();
        }

////        if ((frameForm != null) && (frameForm.getModel() != null) &&
////                (frameForm.getModel().getFacesUnit() != null) &&
////                frameForm.getModel().getFacesUnit().isPage()) {
//        if (frameForm != null && frameForm.isPage()) {
////            InSyncService.getProvider().getRaveErrorHandler().displayError(NbBundle.getMessage(JspIncludeBox.class, "FragmentIsPage", getFile(element)));
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Form is page, and should be a fragment, form=" + frameForm + ", file=" + getFile(element, webform))); // NOI18N
//        }

        return box;
    }

    private static String getFragmentPath(Element element, WebForm webform) {
        if(element.getParentNode() instanceof Element) {
            Element parentDiv = (Element)element.getParentNode();
            CssBox parent = webform.getCssBoxForElement(parentDiv);;
            if(parent != null) {
                while((parent = parent.getParent()) != null) {
                    if(parent instanceof JspIncludeBox) {
                        String parentName = getFile(parent.getElement(), webform);
                        if(parentName == null) {
                            return null;
                        }
                        return parentName.substring(0, parentName.lastIndexOf("/") + 1);// NOI18N
                    }
                }
            }
        }
        return "";
    }

    private static String getFile(Element element, WebForm webform) {
        String src;
        // XXX #94248 Hack for jsp:include, get page attribute.
        if (HtmlTag.JSPINCLUDEX.name.equals(element.getTagName())) {
            src = element.getAttribute("page"); // NOI18N
        } else { // Falling for the rest.
            // See http://java.sun.com/products/jsp/syntax/1.2/syntaxref129.html
            // TODO: make attribute in HtmlAttribute
            src = element.getAttribute("file"); // NOI18N
        }

        if ((src == null) || (src.length() == 0)) {
            return null;
        }

        return getFragmentPath(element, webform) + src;
    }

    protected String getUrlString() {
        return getFile(getElement(), getWebForm());
    }

    /**
     * Return a URL for the included content, or null if it could not be determined.
     */
    @Override
    protected URL getContentURL(WebForm webform, Element element) {
        String src = getFile(element, webform);

        if ((src == null) || (src.length() == 0)) {
            return null;
        }

//        return InSyncService.getProvider().resolveUrl(webform.getMarkup().getBase(), webform.getJspDom(), src);
        return webform.resolveUrl(src);
    }

//    public String toString() {
//        return "JspIncludeBox[" + paramString() + "]";
//    }

    /*
    protected String paramString() {
        return super.paramString() + ", " + markup;
    }
    */

    // Override standard methods to give frames special treatment, since
    // they are "black boxes" as far as the box hierarchy is concerned

    /**
     * What should the default intrinsic width be? Mozilla 1.6 seems to use 300x150.
     */
    public int getIntrinsicWidth() {
        return 300;
    }

    /**
     * What should the default intrinsic height be? Mozilla 1.6 seems to use 300x150.
     */
    public int getIntrinsicHeight() {
        return 150;
    }

    public void relayout(FormatContext context) {
        // Note - we don't pass in context.initialCB since 
        // fixed boxes should not be relative to the outer viewport
        // by default
        initializeContentSize(); // XXX I shouldn't do this here - I've already set it in compute horiz

        int cw = contentWidth;
        int ch = contentHeight;
        int w = (contentWidth != AUTO) ? contentWidth : containingBlockWidth;
        int h = (contentHeight != AUTO) ? contentHeight : containingBlockHeight;
        relayout(null, w, h, -1);

        // Box page layout overrides contentHeight and contentWidth
        // XXX Do I need to reset the "width" and "height" properties too?
        if (cw != AUTO) {
            contentWidth = cw;
            width = leftBorderWidth + leftPadding + contentWidth + rightPadding + rightBorderWidth;
        }

        if (ch != AUTO) {
            contentHeight = ch;
            height =
                topBorderWidth + topPadding + contentHeight + bottomPadding + bottomBorderWidth;
        }

        //super.relayout(context);
    }

    /** No grids in included page fragment visualizations - the grid only applies when
     * users are manipulating individual components in a surface, which is not allowed
     * in a page fragment
     */
    protected void initializeGrid() {
    }

    protected void initializeContentSize() {
        if (styleElement != null) {
//            contentWidth = CssLookup.getLength(styleElement, XhtmlCss.WIDTH_INDEX);
//            contentHeight = CssLookup.getLength(styleElement, XhtmlCss.HEIGHT_INDEX);
            contentWidth = CssUtilities.getCssLength(styleElement, XhtmlCss.WIDTH_INDEX);
            contentHeight = CssUtilities.getCssLength(styleElement, XhtmlCss.HEIGHT_INDEX);
        } else {
            super.initializeContentSize();
        }
    }

//    protected void initializeHorizontalWidths(FormatContext context) {
//        Element oldElement = element;
//        try {
//            if (styleElement != null) {
//                // XXX Bad hack, cheating by replacing temporarily the element.
//                // FIXME Find a better solution.
//                element = styleElement;
//            }
//            
//            super.initializeHorizontalWidths(context);
//        } finally {
//            element = oldElement;
//        }
//        
//    }
    // XXX FIXME Overriding to fake diff element.
    protected CssValue computeWidthCssValue() {
        if (styleElement != null) {
            return CssProvider.getEngineService().getComputedValueForElement(styleElement, XhtmlCss.WIDTH_INDEX);
        } else {
            return super.computeWidthCssValue();
        }
    }
    // XXX FIXME Overriding to fake diff element.
    protected void uncomputeWidthCssValue() {
        if (styleElement != null) {
            CssProvider.getEngineService().uncomputeValueForElement(styleElement, XhtmlCss.WIDTH_INDEX);
        } else {
            super.uncomputeWidthCssValue();
        }
    }


    protected void createChildren(CreateContext context) {
        super.createChildren(context);

        if (getBoxCount() == 0) {
            String desc =
                NbBundle.getMessage(JspIncludeBox.class,
                    (getExternalForm() != null) ? "EmptyFragment" : "NoFragment"); // NOI18N
            addGrayItalicText(context, (styleElement != null) ? styleElement : getElement(), desc);

            return;
        }
    }
}
