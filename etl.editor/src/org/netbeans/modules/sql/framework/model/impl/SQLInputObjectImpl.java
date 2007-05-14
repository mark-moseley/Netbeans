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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * UI wrapper class for SQLObjects which serve as inputs to SQLConnectableObjects.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SQLInputObjectImpl implements SQLInputObject {

    /* Argument name */
    private String argName;

    /* Display name */
    private String dispName;

    /* SQLObject representing input value */
    private SQLObject input;

    /**
     * Creates a new instance of SQLInputObject with the given argument name and input
     * object.
     * 
     * @param argumentName argument name to associate with the given SQLObject
     * @param displayName display name for this instance
     * @param inputObject SQLObject providing input value for the given argument name
     */
    public SQLInputObjectImpl(String argumentName, String displayName, SQLObject inputObject) {
        if (StringUtil.isNullString(argumentName)) {
            throw new IllegalArgumentException("Must supply non-empty String ref for argumentName.");
        }

        argName = argumentName;
        dispName = displayName;
        input = inputObject;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * SQLInputObjects as well as values of non-transient member variables.
     * 
     * @param o Object to compare against this
     * @return hashcode for this instance
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof SQLInputObjectImpl)) {
            return false;
        }

        SQLInputObjectImpl impl = (SQLInputObjectImpl) o;
        boolean response = (argName != null) ? argName.equals(impl.argName) : (impl.argName == null);
        response &= (input != null) ? (input.equals(impl.input)) : (impl.input == null);

        return response;
    }

    /**
     * Gets argument name associated with this input.
     * 
     * @return argument name
     */
    public String getArgName() {
        return argName;
    }

    /**
     * Gets display name of this input.
     * 
     * @return current display name
     */
    public String getDisplayName() {
        return (dispName != null) ? dispName : argName;
    }

    /**
     * Gets reference to SQLObject holding value of this input
     * 
     * @return input object
     */
    public SQLObject getSQLObject() {
        return input;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hashCode = (argName != null) ? argName.hashCode() : 0;
        hashCode += (input != null) ? input.hashCode() : 0;

        return hashCode;
    }

    /**
     * Sets display name of this input.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        dispName = newName;
    }

    /**
     * Sets reference to SQLObject holding value of this input
     * 
     * @param newInput reference to new input object
     */
    public void setSQLObject(SQLObject newInput) {
        input = newInput;
    }

    /**
     * @see SQLInputObject
     */
    public String toXMLString(String prefix) {
        StringBuilder buf = new StringBuilder();

        if (prefix == null) {
            prefix = "";
        }

        buf.append(prefix).append("<" + TAG_INPUT + " ");
        buf.append(ATTR_ARGNAME + "=\"").append(argName).append("\" ");
        buf.append(ATTR_DISPLAY_NAME + "=\"").append(getDisplayName()).append("\">\n");

        try {
            // TODO: make Source and target columns as canvas objects
            // if input is a canvas object then it is refered object
            if (input instanceof SQLCanvasObject || input instanceof DBColumn) {
                buf.append(TagParserUtility.toXMLObjectRefTag(input, prefix + "\t"));
            } else {
                // if input is not canvas object then it is part of object
                buf.append(input.toXMLString(prefix + "\t"));
            }
        } catch (BaseException e) {
            // @TODO log this exception
        }

        buf.append(prefix).append("</" + TAG_INPUT + ">\n");

        return buf.toString();
    }
}

