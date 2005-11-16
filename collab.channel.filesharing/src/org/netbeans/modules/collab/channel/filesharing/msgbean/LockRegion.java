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
 *        This generated bean class LockRegion
 *        matches the schema element '_lock-region'.
 *
 *        Generated on Mon Sep 27 16:53:12 PDT 2004
 */
package org.netbeans.modules.collab.channel.filesharing.msgbean;

public class LockRegion {
    private Use2phase _Use2phase;
    private FileGroups _FileGroups;
    private java.util.List _LockRegionData = new java.util.ArrayList(); // List<LockRegionData>

    public LockRegion() {
        _FileGroups = new FileGroups();
    }

    // Deep copy
    public LockRegion(org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion source) {
        _Use2phase = new org.netbeans.modules.collab.channel.filesharing.msgbean.Use2phase(source._Use2phase);
        _FileGroups = new org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups(source._FileGroups);

        for (java.util.Iterator it = source._LockRegionData.iterator(); it.hasNext();) {
            _LockRegionData.add(
                new org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData(
                    (org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData) it.next()
                )
            );
        }
    }
 
    // This attribute is optional
    public void setUse2phase(org.netbeans.modules.collab.channel.filesharing.msgbean.Use2phase value) {
        _Use2phase = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.Use2phase getUse2phase() {
        return _Use2phase;
    }

    // This attribute is mandatory
    public void setFileGroups(org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups value) {
        _FileGroups = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups getFileGroups() {
        return _FileGroups;
    }

    // This attribute is an array containing at least one element
    public void setLockRegionData(org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData[] value) {
        if (value == null) {
            value = new LockRegionData[0];
        }

        _LockRegionData.clear();

        for (int i = 0; i < value.length; ++i) {
            _LockRegionData.add(value[i]);
        }
    }

    public void setLockRegionData(
        int index, org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData value
    ) {
        _LockRegionData.set(index, value);
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData[] getLockRegionData() {
        LockRegionData[] arr = new LockRegionData[_LockRegionData.size()];

        return (LockRegionData[]) _LockRegionData.toArray(arr);
    }

    public java.util.List fetchLockRegionDataList() {
        return _LockRegionData;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData getLockRegionData(int index) {
        return (LockRegionData) _LockRegionData.get(index);
    }

    // Return the number of lockRegionData
    public int sizeLockRegionData() {
        return _LockRegionData.size();
    }

    public int addLockRegionData(org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData value) {
        _LockRegionData.add(value);

        return _LockRegionData.size() - 1;
    }

    // Search from the end looking for @param value, and then remove it.
    public int removeLockRegionData(org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData value) {
        int pos = _LockRegionData.indexOf(value);

        if (pos >= 0) {
            _LockRegionData.remove(pos);
        }

        return pos;
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent)
    throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        out.write(">\n");

        String nextIndent = indent + "	";

        if (_Use2phase != null) {
            _Use2phase.writeNode(out, "use2phase", nextIndent);
        }

        if (_FileGroups != null) {
            _FileGroups.writeNode(out, "file-groups", nextIndent);
        }

        for (java.util.Iterator it = _LockRegionData.iterator(); it.hasNext();) {
            org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData element = (org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData) it.next();

            if (element != null) {
                element.writeNode(out, "lock-region-data", nextIndent);
            }
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

            if (childNodeName == "use2phase") {
                _Use2phase = new org.netbeans.modules.collab.channel.filesharing.msgbean.Use2phase();
                _Use2phase.readNode(childNode);
            } else if (childNodeName == "file-groups") {
                _FileGroups = new org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups();
                _FileGroups.readNode(childNode);
            } else if (childNodeName == "lock-region-data") {
                LockRegionData aLockRegionData = new org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData();
                aLockRegionData.readNode(childNode);
                _LockRegionData.add(aLockRegionData);
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

        if (name == "use2phase") {
            setUse2phase((Use2phase)value);
        } else if (name == "fileGroups") {
            setFileGroups((FileGroups) value);
        } else if (name == "lockRegionData") {
            addLockRegionData((LockRegionData) value);
        } else if (name == "lockRegionData[]") {
            setLockRegionData((LockRegionData[]) value);
        } else {
            throw new IllegalArgumentException(name + " is not a valid property name for LockRegion");
        }
    }

    public Object fetchPropertyByName(String name) {
        if (name == "use2phase") {
            return getUse2phase();
        }
        
        if (name == "fileGroups") {
            return getFileGroups();
        }

        if (name == "lockRegionData[]") {
            return getLockRegionData();
        }

        throw new IllegalArgumentException(name + " is not a valid property name for LockRegion");
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
        if (_Use2phase != null) {
            if (recursive) {
                _Use2phase.childBeans(true, beans);
            }
            beans.add(_Use2phase);
        }
        
        if (_FileGroups != null) {
            if (recursive) {
                _FileGroups.childBeans(true, beans);
            }

            beans.add(_FileGroups);
        }

        for (java.util.Iterator it = _LockRegionData.iterator(); it.hasNext();) {
            org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData element = (org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData) it.next();

            if (element != null) {
                if (recursive) {
                    element.childBeans(true, beans);
                }

                beans.add(element);
            }
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion)) {
            return false;
        }

        org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion inst = (org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion) o;

        if (!(_Use2phase == null ? inst._Use2phase == null : _Use2phase.equals(inst._Use2phase))) {
            return false; 
        }
        
        if (!((_FileGroups == null) ? (inst._FileGroups == null) : _FileGroups.equals(inst._FileGroups))) {
            return false;
        }

        if (sizeLockRegionData() != inst.sizeLockRegionData()) {
            return false;
        }

        // Compare every element.
        for (
            java.util.Iterator it = _LockRegionData.iterator(), it2 = inst._LockRegionData.iterator();
                it.hasNext() && it2.hasNext();
        ) {
            org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData element = (org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData) it.next();
            org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData element2 = (org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData) it2.next();

            if (!((element == null) ? (element2 == null) : element.equals(element2))) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + (_Use2phase == null ? 0 : _Use2phase.hashCode());
        result = (37 * result) + ((_FileGroups == null) ? 0 : _FileGroups.hashCode());
        result = (37 * result) + ((_LockRegionData == null) ? 0 : _LockRegionData.hashCode());

        return result;
    }
}
