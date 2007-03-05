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
package org.netbeans.modules.wsdlextensions.file.model.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

import org.netbeans.modules.wsdlextensions.file.model.FileComponent;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.model.FileQName;

/**
 * @author sweng
 */
public class FileMessageImpl extends FileComponentImpl implements FileMessage {
    
    public FileMessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FileMessageImpl(WSDLModel model){
        this(model, createPrefixedElement(FileQName.MESSAGE.getQName(), model));
    }
    
    public void accept(FileComponent.Visitor visitor) {
        visitor.visit(this);
    }
    
    public void setFileType(String val) {
        setAttribute(ATTR_FILE_TYPE, FileAttribute.FILE_MESSAGE_FILETYPE_PROPERTY, val);
    }    

    public String getFileType() {
        return getAttribute(FileAttribute.FILE_MESSAGE_FILETYPE_PROPERTY);
    }
    
    public void setFileUseType(String val) {
        setAttribute(ATTR_FILE_USE, FileAttribute.FILE_MESSAGE_USE_PROPERTY, val);
    }    

    public String getFileUseType() {
        return getAttribute(FileAttribute.FILE_MESSAGE_USE_PROPERTY);
    }
    
    public void setFileEncodingStyle(String val) {
        setAttribute(ATTR_FILE_ENCODING_STYLE, FileAttribute.FILE_MESSAGE_ENCODINGSTYLE_PROPERTY, val);
    }    

    public String getFileEncodingStyle() {
        return getAttribute(FileAttribute.FILE_MESSAGE_ENCODINGSTYLE_PROPERTY);
    }
    
    public void setFileName(String val) {
        setAttribute(ATTR_FILE_NAME, FileAttribute.FILE_MESSAGE_FILENAME_PROPERTY, val);
    }    
    
    public String getFileName() {
        return getAttribute(FileAttribute.FILE_MESSAGE_FILENAME_PROPERTY);
    }

    public void setFileNameIsPattern(boolean val) {
        setAttribute(ATTR_FILE_NAME_IS_PATTERN, FileAttribute.FILE_MESSAGE_FILENAMEISPATTERN_PROPERTY, val? "true": "false");
    }

    public boolean getFileNameIsPattern() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_FILENAMEISPATTERN_PROPERTY);
        return s != null && s.equals("true");
    }
    
    public void setPollingInterval(long val) {
    	setAttribute(ATTR_POLLING_INTERVAL, FileAttribute.FILE_MESSAGE_POLLINTERVAL_PROPERTY, "" + val);
    }
    
    public Long getPollingInterval() {
        Long longVal = null;
    	String val = getAttribute(FileAttribute.FILE_MESSAGE_POLLINTERVAL_PROPERTY);
        if (val == null) {
            return null;
        } 
        try {
            longVal = Long.parseLong(val);
        } catch (Exception e) {
            // should never get here
        }
        return longVal;
    }
    
    public void setMaxBytesPerRecord(long val) {
    	setAttribute(ATTR_MAX_BYTES_PER_RECORD, FileAttribute.FILE_MESSAGE_MAXBYTESPERRECORD_PROPERTY, "" + val);
    }
    
    public Long getMaxBytesPerRecord() {
        Long longVal = null;
        String val = getAttribute(FileAttribute.FILE_MESSAGE_MAXBYTESPERRECORD_PROPERTY);
        if (val == null) {
            return null;
        }
       try {
            longVal = new Long(val);
        } catch (Exception e) {
            // should never get here.
        }
        
        return longVal;
    }
    
    public void setRecordDelimiter(String val) {
    	setAttribute(ATTR_RECORD_DELIM, FileAttribute.FILE_MESSAGE_RECORDDELIMITER_PROPERTY, "" + val);
    }
    
    public String getRecordDelimiter() {
        return getAttribute(FileAttribute.FILE_MESSAGE_RECORDDELIMITER_PROPERTY);
    }
    
    public void setAddEOL(boolean val) {
        setAttribute(ATTR_ADD_EOL, FileAttribute.FILE_MESSAGE_ADDEOL_PROPERTY, val? "true" : "false");
    }

    public boolean getAddEOL() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_ADDEOL_PROPERTY);
        return s != null && s.equals("true");
    }
    
    public void setRemoveEOL(boolean val) {
        setAttribute(ATTR_REMOVE_EOL, FileAttribute.FILE_MESSAGE_REMOVEOL_PROPERTY, val? "true" : "false");
    }

    public boolean getRemoveEOL() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_REMOVEOL_PROPERTY);
        return s != null && s.equals("true");
    }
    
    public void setMultipleRecordsPerFile(boolean val) {
        setAttribute(ATTR_MULTIPLE_RECORDS_PER_FILE, 
            FileAttribute.FILE_MESSAGE_MULTIPLERECORDSPERFILE_MESSAGE__PROPERTY, val? "true" : "false");
    }
    
    public boolean getMultipleRecordsPerFile() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_MULTIPLERECORDSPERFILE_MESSAGE__PROPERTY);
        return s != null && s.equals("true");
    }
    
    public void setPart(String val) {
        setAttribute(ATTR_PART, FileAttribute.FILE_MESSAGE_PART_PROPERTY, val);
    }    
    
    public String getPart() {
        return getAttribute(FileAttribute.FILE_MESSAGE_PART_PROPERTY);
    }

}
