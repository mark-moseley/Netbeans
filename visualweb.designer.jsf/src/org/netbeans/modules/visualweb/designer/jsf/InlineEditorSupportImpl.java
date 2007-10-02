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

package org.netbeans.modules.visualweb.designer.jsf;

import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.faces.Entities;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.openide.ErrorManager;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;

/**
 * Impl of <code>DomProvider.InlineEditorSupport</code>
 *
 * @author Peter Zavadsky
 * @author Tor Norby (old original code)
 */
class InlineEditorSupportImpl implements DomProvider.InlineEditorSupport {

    private final JsfForm jsfForm;
    // XXX TODO Remove ref to domProviderImpl.
    private final DomProviderImpl domProviderImpl;
    private final MarkupDesignBean markupDesignBean;
    private final DesignProperty   designProperty;
    
    /** Creates a new instance of InlineEditorSupportImpl */
    public InlineEditorSupportImpl(JsfForm jsfForm,DomProviderImpl domProviderImpl, MarkupDesignBean markupDesignBean, DesignProperty designProperty) {
        this.jsfForm = jsfForm;
        this.domProviderImpl = domProviderImpl;
        this.markupDesignBean = markupDesignBean;
        this.designProperty = designProperty;
    }


//    public static DomProvider.InlineEditorSupport createDummyInlineEditorSupport() {
//        return new DummyInlineEditorSupport();
//    }
//    
//    private static class DummyInlineEditorSupport implements DomProvider.InlineEditorSupport {
//    } // End of DummyInlineEditorSupport.

    public boolean isEditingAllowed() {
        return DomProviderServiceImpl.isEditingAllowed(designProperty);
    }

    public String getValueSource() {
        return designProperty.getValueSource();
    }

    public void unset() {
        designProperty.unset();
    }

    public void setValue(String value) {
        designProperty.setValue(value);
    }

    public String getName() {
        return designProperty.getPropertyDescriptor().getName();
    }

    // XXX AttributeInlineEditor only.
    public String getSpecialInitValue() {
        return DomProviderServiceImpl.getSpecialInitValue(designProperty);
    }

    public String getValue() {
        // String assumption should be checked in beandescriptor search for TEXT_NODE_PROPERTY,
        // especially if we publish this property. Or we could at least specify that the
        // property MUST be a String.
        return (String)designProperty.getValue();
    }

    public String getDisplayName() {
        return designProperty.getPropertyDescriptor().getDisplayName();
    }

//    public Method getWriteMethod() {
//        return designProperty.getPropertyDescriptor().getWriteMethod();
//    }
    public void setViaWriteMethod(String value) {
        Method m = designProperty.getPropertyDescriptor().getWriteMethod();
        try {
            m.invoke(markupDesignBean.getInstance(), new Object[] {value});
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    public Element getRenderedElement() {
        Element sourceElement = markupDesignBean.getElement();
        return MarkupService.getRenderedElementForElement(sourceElement);
    }

    public DocumentFragment createSourceFragment() {
        return domProviderImpl.createSourceFragment(markupDesignBean);
    }

    public String expandHtmlEntities(String value, boolean warn) {
        return Entities.expandHtmlEntities(value, warn, markupDesignBean.getElement());
    }

    public boolean isEscaped() {
        return DomProviderServiceImpl.isEscapedDesignBean(markupDesignBean);
    }

    public void handleEvent(Event e) {
        //        /*
        //          Node node = (org.w3c.dom.Node)e.getTarget();
        //          String type = e.getType();
        //          Node parent = node.getParentNode(); // XXX or use getRelatedNode?
        //
        //        */
        //        dispatchEvent(bean);
        Node node = (org.w3c.dom.Node)e.getTarget();
        Node parent = node.getParentNode(); // XXX or use getRelatedNode?

        // Text node or entity node changes should get translated
        // into a change event on their surrounding element...
        // XXX I could possibly handle to rebreak only
        // the LineBreakGroup.... That would save work -ESPECIALLY-
        // for text right within the <body> tag... but optimize that
        // later
        if (!(node instanceof Element) || ((Element)node).getTagName().equals(HtmlTag.BR.name)) { // text, cdata, entity, ...
            node = parent;
            parent = parent.getParentNode();

            if (node instanceof Element) {
//                MarkupDesignBean b = ((RaveElement)node).getDesignBean();
//                MarkupDesignBean b = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)node);
//                MarkupDesignBean b = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)node);
                MarkupDesignBean b = MarkupUnit.getMarkupDesignBeanForElement((Element)node);

                if (b == null) {
//                    b = bean;
                    b = markupDesignBean;
                }

//                webform.getDomSynchronizer().requestTextUpdate(b);
//                webform.requestTextUpdate(b);
                domProviderImpl.requestTextUpdate(b);
            }
        } else {
//            webform.getDomSynchronizer().requestChange(bean);
//            webform.requestChange(bean);
            domProviderImpl.requestChange(markupDesignBean);
        }
    }

    public void beanChanged() {
        domProviderImpl.beanChanged(markupDesignBean);
    }

    public void requestChange() {
        domProviderImpl.requestChange(markupDesignBean);
    }

    public void clearPrerendered() {
        domProviderImpl.setPrerenderedBean(null, null);
    }

    public boolean setPrerendered(DocumentFragment fragment) {
        return domProviderImpl.setPrerenderedBean(markupDesignBean, fragment);
    }

    public void setStyleParent(DocumentFragment fragment) {
        NodeList children = fragment.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
//                RaveElement e = (RaveElement)child;
                Element e = (Element)child;
//                CssLookup.getCssEngine(e).clearComputedStyles(e, null);
//                CssProvider.getEngineService().clearComputedStylesForElement(e);
                Element beanElement = markupDesignBean.getElement();
                // XXX #6489063 Inherit the style from the original element.
                // Maybe there should be just the size of the font inherited.
                CssProvider.getEngineService().setStyleParentForElement(e, beanElement);
                
//                e = e.getRendered();
                e = MarkupService.getRenderedElementForElement(e);
                Element beanRenderedElement = MarkupService.getRenderedElementForElement(beanElement);
                if (e != null && beanRenderedElement != null) {
//                    CssLookup.getCssEngine(e).clearComputedStyles(e, null);
//                    CssProvider.getEngineService().clearComputedStylesForElement(e);
                    // XXX #6489063 Inherit the style from the original element.
                    // Maybe there should be just the size of the font inherited.
                    CssProvider.getEngineService().setStyleParentForElement(e, beanRenderedElement);
                }
            }
        }
    }

    public DocumentFragment renderDomFragment() {
        DocumentFragment fragment = domProviderImpl.renderHtmlForMarkupDesignBean(markupDesignBean);
        // XXX To get it into source document so it can work (Positions work only against source doc!).
        // TODO Change the positions to work over the rendered document, and also attach this fragment to the rendered doc.
        fragment = (DocumentFragment)domProviderImpl.getJsfForm().getJspDom().importNode(fragment, true);
        
        // XXX Moved from designer/../AttributeInlineEditor
        jsfForm.updateErrorsInComponent();
        
        return fragment;
   }
}
