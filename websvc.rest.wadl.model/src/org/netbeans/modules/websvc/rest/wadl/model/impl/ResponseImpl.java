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

public class ResponseImpl extends NamedImpl implements Response {

    /** Creates a new instance of ResponseImpl */
    public ResponseImpl(WadlModel model, Element e) {
        super(model, e);
    }

    public ResponseImpl(WadlModel model){
        this(model, createNewElement(WadlQNames.RESPONSE.getQName(), model));
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

    public Collection<Representation> getRepresentation() {
        return getChildren(Representation.class);
    }

    public void addRepresentation(Representation rep) {
        addAfter(REPRESENTATION_PROPERTY, rep, TypeCollection.FOR_REPRESENTATION.types());
    }

    public void removeRepresentation(Representation rep) {
        removeChild(REPRESENTATION_PROPERTY, rep);
    }

    public Collection<Fault> getFault() {
        return getChildren(Fault.class);
    }

    public void addFault(Fault fault) {
        addAfter(FAULT_PROPERTY, fault, TypeCollection.FOR_FAULT.types());
    }

    public void removeFault(Fault fault) {
        removeChild(FAULT_PROPERTY, fault);
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
