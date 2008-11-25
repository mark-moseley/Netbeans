//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.11.07 at 12:36:44 PM PST 
//
package org.netbeans.modules.websvc.rest.wadl.design;

import java.util.Vector;

public enum MediaType {

    PLAIN("text/plain"),
    HTML("text/html"),
    TEXT_XML("text/xml"),
    XML("application/xml"),
    JSON("application/json"),
    FORM_URL_ENCODED("application/x-www-form-urlencoded"),
    MULTIPART_FORM("multipart/form-data")
    ;
    private final String value;

    MediaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MediaType fromValue(String v) {
        for (MediaType c : MediaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return PLAIN;
    }

    public static String[] values(boolean toUpper) {
        Vector<String> v = new Vector<String>();
        for (MediaType c : MediaType.values()) {
            if (toUpper) {
                v.add(c.value().toUpperCase());
            } else {
                v.add(c.value().toLowerCase());
            }
        }
        return (String[]) v.toArray(new String[0]);
    }
}
