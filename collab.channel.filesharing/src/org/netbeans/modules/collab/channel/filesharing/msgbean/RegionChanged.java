/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 *        This generated bean class RegionChanged
 *        matches the schema element '_region-changed'.
 *
 *        Generated on Mon Sep 27 16:53:13 PDT 2004
 */
package org.netbeans.modules.collab.channel.filesharing.msgbean;

public class RegionChanged {
    private TextRegionChanged _TextRegionChanged;
    private JavaRegionChanged _JavaRegionChanged;
    private LineRegionChanged _LineRegionChanged;

    public RegionChanged() {
    }

    // Deep copy
    public RegionChanged(org.netbeans.modules.collab.channel.filesharing.msgbean.RegionChanged source) {
        _TextRegionChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.TextRegionChanged(
                source._TextRegionChanged
            );
        _JavaRegionChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.JavaRegionChanged(
                source._JavaRegionChanged
            );
        _LineRegionChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.LineRegionChanged(
                source._LineRegionChanged
            );
    }

    // This attribute is mandatory
    public void setTextRegionChanged(org.netbeans.modules.collab.channel.filesharing.msgbean.TextRegionChanged value) {
        _TextRegionChanged = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.TextRegionChanged getTextRegionChanged() {
        return _TextRegionChanged;
    }

    // This attribute is mandatory
    public void setJavaRegionChanged(org.netbeans.modules.collab.channel.filesharing.msgbean.JavaRegionChanged value) {
        _JavaRegionChanged = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.JavaRegionChanged getJavaRegionChanged() {
        return _JavaRegionChanged;
    }

    // This attribute is mandatory
    public void setLineRegionChanged(org.netbeans.modules.collab.channel.filesharing.msgbean.LineRegionChanged value) {
        _LineRegionChanged = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.LineRegionChanged getLineRegionChanged() {
        return _LineRegionChanged;
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent)
    throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        out.write(">\n");

        String nextIndent = indent + "	";

        if (_TextRegionChanged != null) {
            _TextRegionChanged.writeNode(out, "text-region-changed", nextIndent);
        }

        if (_JavaRegionChanged != null) {
            _JavaRegionChanged.writeNode(out, "java-region-changed", nextIndent);
        }

        if (_LineRegionChanged != null) {
            _LineRegionChanged.writeNode(out, "line-region-changed", nextIndent);
        }

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

            if (childNodeName == "text-region-changed") {
                _TextRegionChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.TextRegionChanged();
                _TextRegionChanged.readNode(childNode);
            } else if (childNodeName == "java-region-changed") {
                _JavaRegionChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.JavaRegionChanged();
                _JavaRegionChanged.readNode(childNode);
            } else if (childNodeName == "line-region-changed") {
                _LineRegionChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.LineRegionChanged();
                _LineRegionChanged.readNode(childNode);
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

        if (name == "textRegionChanged") {
            setTextRegionChanged((TextRegionChanged) value);
        } else if (name == "javaRegionChanged") {
            setJavaRegionChanged((JavaRegionChanged) value);
        } else if (name == "lineRegionChanged") {
            setLineRegionChanged((LineRegionChanged) value);
        } else {
            throw new IllegalArgumentException(name + " is not a valid property name for RegionChanged");
        }
    }

    public Object fetchPropertyByName(String name) {
        if (name == "textRegionChanged") {
            return getTextRegionChanged();
        }

        if (name == "javaRegionChanged") {
            return getJavaRegionChanged();
        }

        if (name == "lineRegionChanged") {
            return getLineRegionChanged();
        }

        throw new IllegalArgumentException(name + " is not a valid property name for RegionChanged");
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
        if (_TextRegionChanged != null) {
            if (recursive) {
                _TextRegionChanged.childBeans(true, beans);
            }

            beans.add(_TextRegionChanged);
        }

        if (_JavaRegionChanged != null) {
            if (recursive) {
                _JavaRegionChanged.childBeans(true, beans);
            }

            beans.add(_JavaRegionChanged);
        }

        if (_LineRegionChanged != null) {
            if (recursive) {
                _LineRegionChanged.childBeans(true, beans);
            }

            beans.add(_LineRegionChanged);
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof org.netbeans.modules.collab.channel.filesharing.msgbean.RegionChanged)) {
            return false;
        }

        org.netbeans.modules.collab.channel.filesharing.msgbean.RegionChanged inst = (org.netbeans.modules.collab.channel.filesharing.msgbean.RegionChanged) o;

        if (
            !((_TextRegionChanged == null) ? (inst._TextRegionChanged == null)
                                               : _TextRegionChanged.equals(inst._TextRegionChanged))
        ) {
            return false;
        }

        if (
            !((_JavaRegionChanged == null) ? (inst._JavaRegionChanged == null)
                                               : _JavaRegionChanged.equals(inst._JavaRegionChanged))
        ) {
            return false;
        }

        if (
            !((_LineRegionChanged == null) ? (inst._LineRegionChanged == null)
                                               : _LineRegionChanged.equals(inst._LineRegionChanged))
        ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 17;
        result = (37 * result) + ((_TextRegionChanged == null) ? 0 : _TextRegionChanged.hashCode());
        result = (37 * result) + ((_JavaRegionChanged == null) ? 0 : _JavaRegionChanged.hashCode());
        result = (37 * result) + ((_LineRegionChanged == null) ? 0 : _LineRegionChanged.hashCode());

        return result;
    }
}

/*
                The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : collab.xsd
    Created on : May 21, 2004, 7:45 PM
    Author     : Ayub Khan
    Description:
        Purpose of the document follows.
-->
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://sun.com/ns/collab/dev/1_0/filesharing"
            xmlns:c="http://sun.com/ns/collab/dev/1_0"
            xmlns:ch="http://sun.com/ns/collab/dev/1_0/filesharing"
            elementFormDefault="qaulified">

    <!-- collab element -->
    <xsd:element name="c:collab" type="_collab">
    </xsd:element>

    <xsd:complexType name="_collab">
        <xsd:sequence>
            <xsd:element name="version" type="xsd:string"
                        minOccurs="1" maxOccurs="1"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="ch:send-file" type="_send-file"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:file-changed" type="_file-changed"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:lock-region" type="_lock-region"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:unlock-region" type="_unlock-region"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:join-filesharing" type="_join-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:pause-filesharing" type="_pause-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:resume-filesharing" type="_resume-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:leave-filesharing" type="_leave-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:commands" type="_commands"
                        minOccurs="0" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for send-file -->
    <xsd:complexType name="_send-file">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="send-file-data" type="_send-file-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

   <!-- Schema for file-changed -->
    <xsd:complexType name="_file-changed">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-changed-data" type="_file-changed-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for lock-region -->
    <xsd:complexType name="_lock-region">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="lock-region-data" type="_lock-region-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for unlock-region -->
    <xsd:complexType name="_unlock-region">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="unlock-region-data" type="_unlock-region-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for join filesharing -->
    <xsd:complexType name="_join-filesharing">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="begin-join"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="end-join"
                        minOccurs="0" maxOccurs="1"/>
            </xsd:choice>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for pause filesharing -->
    <xsd:complexType name="_pause-filesharing">
        <xsd:sequence>
            <xsd:element name="join-user" type="_join-user"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="moderator" type="_moderator"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-owners" type="_file-owners"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for resume filesharing -->
    <xsd:complexType name="_resume-filesharing">
        <xsd:sequence>
            <xsd:element name="moderator" type="_moderator"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for pause filesharing -->
    <xsd:complexType name="_leave-filesharing">
        <xsd:sequence>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="new-moderator" type="_moderator"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="new-file-owner" type="_new-file-owner"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for commands -->
    <xsd:complexType name="_commands">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="filesystem-command" type="_filesystem-command"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <!-- ===================================================== -->

    <xsd:complexType name="_file-groups">
        <xsd:sequence>
            <xsd:element name="file-group" type="_file-group"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_file-group">
        <xsd:sequence>
            <xsd:element name="file-group-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_send-file-data">
        <xsd:sequence>
            <xsd:element name="file-data" type="_file-data"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="choose-line-region-function" type="xsd:boolean"
                        minOccurs="1" maxOccurs="1"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="line-region-function" type="_line-region-function"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="line-region" type="_line-region"
                        minOccurs="1" maxOccurs="unbounded"/>
            </xsd:choice>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_line-region-function">
        <xsd:sequence>
            <xsd:element name="funtion-name" type="xsd:string"
                        minOccurs="1" maxOccurs="1"/>
            <xsd:element name="arguments"  type="xsd:string"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_file-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="content-type" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="description" type="xsd:string"
                    minOccurs="0" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

   <!-- Schema for file-changed -->
    <xsd:complexType name="_file-changed-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="digest" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="region-changed" type="_region-changed"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for lock-region -->
    <xsd:complexType name="_lock-region-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="line-region" type="_line-region"
                    minOccurs="1" maxOccurs="unbounded"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="text-region" type="_text-region"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="java-region" type="_java-region"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for unlock-region -->
    <xsd:complexType name="_unlock-region-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="line-region" type="_line-region"
                    minOccurs="1" maxOccurs="unbounded"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="text-region" type="_text-region"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="java-region" type="_java-region"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- user elements -->
    <xsd:complexType name="_moderator">
        <xsd:sequence>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_join-user">
        <xsd:sequence>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_file-owners">
        <xsd:sequence>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_new-file-owner">
        <xsd:sequence>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_users">
        <xsd:sequence>
            <xsd:element name="user" type="_user"
                    minOccurs="0" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_user">
        <xsd:sequence>
            <xsd:element name="id" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="name" type="xsd:string"
                    minOccurs="0" maxOccurs="1"/>
            <xsd:element name="description" type="xsd:string"
                    minOccurs="0" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- filesystem command schema -->
    <xsd:complexType name="_filesystem-command">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
            <!-- file commands -->
                <xsd:element name="delete-file" type="_delete-file"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_delete-file">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- ===================================================== -->

    <xsd:complexType name="_region-changed">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="text-region-changed" type="_text-region-changed"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="java-region-changed" type="_java-region-changed"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="line-region-changed" type="_line-region-changed"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_text-region-changed">
        <xsd:sequence>
            <xsd:element name="text-region" type="_text-region"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="text-change" type="_text-change"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_java-region-changed">
        <xsd:sequence>
            <xsd:element name="java-region" type="_java-region"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="java-change" type="_java-change"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_line-region-changed">
        <xsd:sequence>
            <xsd:element name="line-region" type="_line-region"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="line-change" type="_line-change"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_region" abstract="true">
        <xsd:sequence>
            <xsd:element name="region-name" type="xsd:string"
                        minOccurs="1" maxOccurs="1">
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_change" abstract="true"/>

    <xsd:complexType name="_content">
        <xsd:sequence>
            <xsd:element name="encoding" type="xsd:string"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
            <xsd:element name="digest" type="xsd:string"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
            <xsd:element name="data" type="xsd:string"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================   Text Region    =================== -->
    <!-- ===================================================== -->

    <xsd:complexType name="_text-region">
        <xsd:complexContent>
            <xsd:extension base="_region">
                <xsd:sequence>
                    <xsd:element name="begin-offset" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                    <xsd:element name="length" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="_line-range">
        <xsd:sequence>
            <xsd:element name="from-line" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="to-line" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_offset-range">
        <xsd:sequence>
            <xsd:element name="begin-offset" type="xsd:integer"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
            <xsd:element name="length" type="xsd:integer"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_text-change">
        <xsd:complexContent>
            <xsd:extension base="_change">
                    <xsd:choice maxOccurs="1">
                        <xsd:element name="change-texts" type="_change-texts"
                                minOccurs="1" maxOccurs="1"/>
                        <xsd:element name="content" type="_content"
                                minOccurs="1" maxOccurs="1"/>
                    </xsd:choice>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================   Java Region    =================== -->
    <!-- ===================================================== -->
    <xsd:complexType name="_java-region">
        <xsd:complexContent>
            <xsd:extension base="_region">
                <xsd:sequence>
                    <xsd:element name="begin-offset" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                    <xsd:element name="length" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="_java-change">
        <xsd:complexContent>
            <xsd:extension base="_change">
                <xsd:sequence>
                    <xsd:choice maxOccurs="1">
                        <xsd:element name="change-texts" type="_change-texts"
                                minOccurs="1" maxOccurs="1"/>
                        <xsd:element name="content" type="_content"
                                minOccurs="1" maxOccurs="1"/>
                    </xsd:choice>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================   Line Region    =================== -->
    <!-- ===================================================== -->
    <xsd:complexType name="_line-region">
        <xsd:complexContent>
            <xsd:extension base="_region">
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="_line-change">
        <xsd:complexContent>
            <xsd:extension base="_change">
                <xsd:sequence>
                    <xsd:choice maxOccurs="1">
                        <xsd:element name="content" type="_content"
                                minOccurs="1" maxOccurs="1"/>
                    </xsd:choice>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================Common Region Type=================== -->
    <!-- ===================================================== -->

    <xsd:complexType name="_change-texts">
        <xsd:sequence>
            <xsd:element name="remove-texts" type="_remove-texts"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="insert-texts" type="_insert-texts"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_remove-texts">
        <xsd:sequence>
            <xsd:element name="remove-text" type="_remove-text"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_insert-texts">
        <xsd:sequence>
            <xsd:element name="insert-text" type="_insert-text"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_remove-text">
        <xsd:sequence>
            <xsd:element name="offset" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_insert-text">
        <xsd:sequence>
            <xsd:element name="offset" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

</xsd:schema>

*/
