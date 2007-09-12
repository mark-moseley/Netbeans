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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.Iterator;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.api.ExNamespaceContext;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReferenceable;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;


/**
 *
 * incapuslate all attribute logic.   
 * @author Vitaly Bychkov
 * @author ads
 * @version 1.0
 */
public class AttributeAccess {

    private TMapComponentAbstract myComponent;
    
    public AttributeAccess(TMapComponentAbstract component) {
        myComponent = component;
    }

    Object getAttributeValueOf( Attribute attr, String stringValue ) {
        if (stringValue == null) {
            return null;
        }
        Class clazz = attr.getType();
        if (String.class.isAssignableFrom(clazz)) {
            return stringValue;
        } else if (ParamType.class.isAssignableFrom(clazz)) {
            return ParamType.parseParamType(stringValue);
        }
        assert false;
        return null;
    }
    
    VariableReference getTMapVarReference( Attribute attr)
    {

//        readLock();
//        try {
            return resolveTMapVarReference(attr);
//        }
//        finally {
//            readUnlock();
//        }
    }

//    <T extends BpelReferenceable> List<BpelReference<T>> getBpelReferenceList(
//            Attribute attr, Class<T> type )
//    {
//        readLock();
//        try {
//            String str = getEntity().getAttribute(attr);
//            return getBpelReferenceList(type, str, attr);
//        }
//        finally {
//            readUnlock();
//        }
//    }
//
//    private <T extends BpelReferenceable> List<BpelReference<T>> 
//            getBpelReferenceList( Class<T> type, String str, Attribute attr )
//    {
//        if (str == null) {
//            return null;
//        }
//        StringTokenizer tokenizer = new StringTokenizer(str, " ");
//        List<BpelReference<T>> list = new LinkedList<BpelReference<T>>();
//        while (tokenizer.hasMoreTokens()) {
//            String next = tokenizer.nextToken();
//            BpelReference<T> ref = BpelReferenceBuilder.getInstance().build(
//                    type, getEntity(), next);
//            BpelReferenceBuilder.getInstance().setAttribute(ref, attr);
//            list.add(ref);
//        }
//        return Collections.unmodifiableList(list);
//    }
    void setTMapVarReference( Attribute attr, 
            VariableReference ref )
    {
//        writeLock();
//        try {
            VariableReference old = getTMapVarReference(attr);

            String str = ref.getRefString();
//            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), old, 
//                    ref );
            setAttribute(attr, str);
//            getEntity().postGlobalEvent(event);
//        }
//        catch (VetoException e) {
//            assert false;
//        }
//        finally {
//            writeUnlock();
//        }
    }

    private VariableReference resolveTMapVarReference( 
            Attribute attr ) 
    {
        if ( getComponent().getAttribute( attr ) == null ){
            return null;
        }
        VariableReference ref = (VariableReference) TMapReferenceBuilder.
                getInstance().build( VariableDeclarator.class , 
                getComponent() , attr ); 
        return ref;
    }

    public <T extends ReferenceableWSDLComponent> void setWSDLReference( Attribute attr , 
            WSDLReference<T> ref )
    {

// TODO 
//////        writeLock();
//////        try {
            WSDLReference<T> old = getWSDLReference( attr , ref.getType());
            
            String str = ref.getRefString();

            boolean isQname = ((AttributesType)ref)
                    .getAttributeType() == AttributesType.AttrType.QNAME;
            if (isQname) {
                str = prepareQNameAttribute(ref.getQName());
            }
            /*
             * ref = WSDLReferenceBuilder.getInstance().build( ref.getType() , 
             *              this , str )
             * setting in event new created reference is more appropriate
             * actually.
             */
// TODO m | r            
////            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), old,
////                   ref );
            
            setAttribute( attr , str);
// TODO m | r            
////            getComponent().postGlobalEvent(event);
//////        }
//////        catch (VetoException e) {
//////            assert false;
//////        }
//////        finally {
//////            writeUnlock();
//////        }
    }    

    public <T extends ReferenceableWSDLComponent> WSDLReference<T> getWSDLReference( 
            Attribute attr , Class<T> clazz )
    {

// TODO m 
////        readLock();
////        try {
            return resolveWSDLReference( attr, clazz );
//        }
//        finally {
//            readUnlock();
//        }
    }

    private <T extends ReferenceableWSDLComponent> WSDLReference<T> 
        resolveWSDLReference( Attribute attr , Class<T> clazz ) 
    {
        
        TMapComponentAbstract opImpl = getComponent();
        
        if ( getComponent().getAttribute( attr ) == null ){
            return null;
        }
        
        WSDLReference<T> ref = WSDLReferenceBuilder.getInstance().build( clazz , 
                getComponent() , attr );
        return ref;
    }

    
    
    
    /**
     * Prepare XML for setting qName as value. 1) Corresponded namespace
     * possibly should be added. 2) attribute value will be returned as String (
     * prefix will be determined and value will be formed ).
     */
    private String prepareQNameAttribute( QName qName ) {
        String attributeValue;
        String prefix = qName.getPrefix();
        String localPart = qName.getLocalPart();
        String uri = qName.getNamespaceURI();

        ExNamespaceContext context = getComponent().getNamespaceContext();

        String existedPrefix = context.getPrefix(uri);
        if (existedPrefix != null) {
            attributeValue = existedPrefix + ":" + localPart;
        }
        else {
            boolean isGoodPrefix = true;

            /*
             * check for presence prefix already in context. If It exists then
             * it have different uri ( becuase we don't have this uri here at
             * all - cathed previous case in "if" ).
             */
            if ((prefix != null) && (prefix.length() > 0)) {
                Iterator<String> iterator = context.getPrefixes();
                while (iterator.hasNext()) {
                    String pref = iterator.next();
                    if (pref.equals(prefix)) {
                        isGoodPrefix = false;
                        break;
                    }
                }
            }
            else {
                isGoodPrefix = false;
            }

            try {
                if (isGoodPrefix) {
                    context.addNamespace(prefix, uri);
                }
                else {
                    prefix = context.addNamespace(uri);
                }
            }
            catch (InvalidNamespaceException e) {
                /*
                 * we should not appear here - becuase we check presence of
                 * prefix..... ???? wrong prefix ( not NCName) ??? ( Qname
                 * doens't check correctness of prefix ).
                 */
                assert false;
            }

            // we have added namespace to context ,
            // at least we set value of attribute
            attributeValue = prefix + ":" + localPart;
        }
        return attributeValue;
    }
    
    //==========================================================================
    
    private void setAttribute( Attribute attr, String str ) {
        getComponent().setAttribute( attr.getName() , attr, str);
    }

    //==========================================================================
    //
    // Event framework.
    //
    //==========================================================================
////    private PropertyUpdateEvent preUpdateAttribute( String attrName,
////            Object oldValue, Object value ) throws VetoException
////    {
////        return preUpdateAttribute( getComponent() , attrName , oldValue , value );
////    }
////    
////    private PropertyUpdateEvent preUpdateAttribute( TMapComponentAbstract component,
////            String attrName, Object oldValue, Object value ) throws VetoException
////    {
////        component.checkDeleted();
////        PropertyUpdateEvent event = new PropertyUpdateEvent(component.getModel().
////                getSource(), component, attrName, oldValue, value);
////        component.getModel().preInnerEventNotify(event);
////        return event;
////    }
////    
////    private PropertyRemoveEvent preRemoveAttribute( String name, Object obj ) {
////        getComponent().checkDeleted();
////        PropertyRemoveEvent event = new PropertyRemoveEvent(getComponent().getModel()
////                .getSource(), getComponent(), name, obj);
////
////        try {
////            getComponent().getModel().preInnerEventNotify(event);
////            return event;
////        }
////        catch (VetoException e) {
////            assert false;
////        }
////        return null;
////    }
////
    //==========================================================================
    
    private TMapComponentAbstract getComponent(){
        return myComponent;
    }
    
}

