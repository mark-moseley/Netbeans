//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.11.07 at 12:36:44 PM PST 
//


package org.netbeans.modules.websvc.rest.wadl.model.impl;

import java.util.Collection;
import java.util.Vector;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.visitor.WadlVisitor;
import org.w3c.dom.Element;

public class ResourceImpl extends NamedImpl implements Resource {

    /** Creates a new instance of OperationImpl */
    public ResourceImpl(WadlModel model, Element e) {
        super(model, e);
    }

    public ResourceImpl(WadlModel model){
        this(model, createNewElement(WadlQNames.RESOURCE.getQName(), model));
    }

    public Collection<Param> getParam() {
        return getChildren(Param.class);
    }

    public void addParam(Param param) {
        addAfter(PARAM_PROPERTY, param, TypeCollection.FOR_PARAM.types());
    }

    public void removeParam(Param param) {
        removeChild(PARAM_PROPERTY, param);
    }

    public Collection<Method> getMethod() {
        return getChildren(Method.class);
    }

    public void addMethod(Method method) {
        addAfter(METHOD_PROPERTY, method, TypeCollection.FOR_METHOD.types());
    }

    public void removeMethod(Method method) {
        removeChild(METHOD_PROPERTY, method);
    }
    
    public Collection<Resource> getResource() {
        return getChildren(Resource.class);
    }

    public void addResource(Resource resource) {
        addAfter(RESOURCE_PROPERTY, resource, TypeCollection.FOR_RESOURCE.types());
    }

    public void removeResource(Resource resource) {
        removeChild(RESOURCE_PROPERTY, resource);
    }

    public String getId() {
        return getAttribute(WadlAttribute.ID);
    }

    public void setId(String base) {
        setAttribute(ID_PROPERTY, WadlAttribute.ID, base);
    }

    public String getType() {
        return getAttribute(WadlAttribute.TYPE);
    }

    public void setType(String base) {
        setAttribute(TYPE_PROPERTY, WadlAttribute.TYPE, base);
    }

    public String getQueryType() {
        return getAttribute(WadlAttribute.QUERY_TYPE);
    }

    public void setQueryType(String base) {
        setAttribute(QUERY_TYPE_PROPERTY, WadlAttribute.QUERY_TYPE, base);
    }

    public String getPath() {
        return getAttribute(WadlAttribute.PATH);
    }

    public void setPath(String base) {
        setAttribute(PATH_PROPERTY, WadlAttribute.PATH, base);
    }

    public void accept(WadlVisitor visitor) {
        visitor.visit(this);
    }
    
    public ParamStyle[] getValidParamStyles() {
        Vector<ParamStyle> v = new Vector<ParamStyle>();
        for(ParamStyle s:VALID_PARAM_STYLES) {
            v.add(s);
        }
        return (ParamStyle[]) v.toArray(new ParamStyle[0]);
    }

    public String[] getValidParamStyles(boolean toUpper) {
        Vector<String> v = new Vector<String>();
        for(ParamStyle s:VALID_PARAM_STYLES) {
            v.add(s.value());
        }
        return (String[]) v.toArray(new String[0]);
    }

}
