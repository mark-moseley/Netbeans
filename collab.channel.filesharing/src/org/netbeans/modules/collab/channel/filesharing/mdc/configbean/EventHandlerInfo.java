/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 *        This generated bean class EventHandlerInfo
 *        matches the schema element '_event-handler-info'.
 *
 *        Generated on Thu Aug 19 15:45:47 PDT 2004
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.configbean;

public class EventHandlerInfo {
    private java.lang.String _HandlerClass;
    private boolean _Stateful;

    public EventHandlerInfo() {
        _HandlerClass = "";
    }

    // Deep copy
    public EventHandlerInfo(org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventHandlerInfo source) {
        _HandlerClass = source._HandlerClass;
        _Stateful = source._Stateful;
    }

    // This attribute is mandatory
    public void setHandlerClass(java.lang.String value) {
        _HandlerClass = value;
    }

    public java.lang.String getHandlerClass() {
        return _HandlerClass;
    }

    // This attribute is mandatory
    public void setStateful(boolean value) {
        _Stateful = value;
    }

    public boolean isStateful() {
        return _Stateful;
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent)
    throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        out.write(">\n");

        String nextIndent = indent + "	";

        if (_HandlerClass != null) {
            out.write(nextIndent);
            out.write("<handler-class"); // NOI18N
            out.write(">"); // NOI18N
            org.netbeans.modules.collab.channel.filesharing.mdc.configbean.CCollab.writeXML(out, _HandlerClass, false);
            out.write("</handler-class>\n"); // NOI18N
        }

        out.write(nextIndent);
        out.write("<stateful"); // NOI18N
        out.write(">"); // NOI18N
        out.write(_Stateful ? "true" : "false");
        out.write("</stateful>\n"); // NOI18N
        out.write(indent);
        out.write("</" + nodeName + ">\n");
    }

    public void readNode(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList children = node.getChildNodes();

        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = ((childNode.getLocalName() == null) ? childNode.getNodeName().intern()
                                                                       : childNode.getLocalName().intern());
            String childNodeValue = "";

            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }

            if (childNodeName == "handler-class") {
                _HandlerClass = childNodeValue;
            } else if (childNodeName == "stateful") {
                if (childNode.getFirstChild() == null) {
                    _Stateful = true;
                } else {
                    _Stateful = java.lang.Boolean.valueOf(childNodeValue).booleanValue();
                }
            } else {
                // Found extra unrecognized childNode
            }
        }
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) {
            return;
        }

        name = name.intern();

        if (name == "handlerClass") {
            setHandlerClass((java.lang.String) value);
        } else if (name == "stateful") {
            setStateful(((java.lang.Boolean) value).booleanValue());
        } else {
            throw new IllegalArgumentException(name + " is not a valid property name for EventHandlerInfo");
        }
    }

    public Object fetchPropertyByName(String name) {
        if (name == "handlerClass") {
            return getHandlerClass();
        }

        if (name == "stateful") {
            return (isStateful() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
        }

        throw new IllegalArgumentException(name + " is not a valid property name for EventHandlerInfo");
    }

    // Return an array of all of the properties that are beans and are set.
    public java.lang.Object[] childBeans(boolean recursive) {
        java.util.List children = new java.util.LinkedList();
        childBeans(recursive, children);

        java.lang.Object[] result = new java.lang.Object[children.size()];

        return (java.lang.Object[]) children.toArray(result);
    }

    // Put all child beans into the beans list.
    public void childBeans(boolean recursive, java.util.List beans) {
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventHandlerInfo)) {
            return false;
        }

        org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventHandlerInfo inst = (org.netbeans.modules.collab.channel.filesharing.mdc.configbean.EventHandlerInfo) o;

        if (!((_HandlerClass == null) ? (inst._HandlerClass == null) : _HandlerClass.equals(inst._HandlerClass))) {
            return false;
        }

        if (!(_Stateful == inst._Stateful)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 17;
        result = (37 * result) + ((_HandlerClass == null) ? 0 : _HandlerClass.hashCode());
        result = (37 * result) + (_Stateful ? 0 : 1);

        return result;
    }
}

/*
                The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : collab_config.xsd
    Created on : Aug 19, 2004, 7:45 AM
    Author     : Ayub Khan
    Description:
        Purpose of the document follows.
-->
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://sun.com/ns/collab/dev/1_0/mdc"
            xmlns:c="http://sun.com/ns/collab/dev/1_0"
            xmlns:mdc="http://sun.com/ns/collab/dev/1_0/mdc"
            elementFormDefault="qaulified">

    <!-- collab element -->
    <xsd:element name="c:collab" type="_collab"/>

    <xsd:complexType name="_collab">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="mdc:config" type="_config"
                        minOccurs="1" maxOccurs="unbounded"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for config -->
    <xsd:complexType name="_config">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="mdc:event-notifier-config" type="_event-notifier-config"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="mdc:event-processor-config" type="_event-processor-config"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
        <xsd:attribute name="version" type="xsd:string"/>
    </xsd:complexType>

   <!-- Schema for event-notifier-config -->
    <xsd:complexType name="_event-notifier-config">
        <xsd:sequence>
            <xsd:element name="register-event" type="_register-event"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

   <!-- Schema for event-processor-config -->
    <xsd:complexType name="_event-processor-config">
        <xsd:sequence>
            <xsd:element name="register-event-handler" type="_register-event-handler"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for register-event -->
    <xsd:complexType name="_register-event">
        <xsd:sequence>
            <xsd:element name="event-class" type="_class-name"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="event-name" type="_event-name"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for register-event-handler -->
    <xsd:complexType name="_register-event-handler">
        <xsd:sequence>
            <xsd:element name="event-name" type="_event-name"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="event-handler-info" type="_event-handler-info"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for _event-handler -->
    <xsd:complexType name="_event-handler-info">
        <xsd:sequence>
            <xsd:element name="handler-class" type="_class-name"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="stateful" type="xsd:boolean"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for _event-name -->
    <xsd:complexType name="_event-name">
        <xsd:simpleContent>
            <xsd:extension base="xsd:string"/>
        </xsd:simpleContent>
    </xsd:complexType>

    <!-- Schema for _class -->
    <xsd:complexType name="_class-name">
        <xsd:simpleContent>
            <xsd:extension base="xsd:string"/>
        </xsd:simpleContent>
    </xsd:complexType>

</xsd:schema>

*/
