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

    public boolean getArchiveEnabled() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_ARCHIVE_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setArchiveEnabled(boolean b) {
        setAttribute(ATTR_ARCHIVE_ENABLED, FileAttribute.FILE_MESSAGE_ARCHIVE_PROPERTY, b? "true" : "false");
    }

    public boolean getProtectEnabled() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_PROTECT_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setProtectEnabled(boolean b) {
        setAttribute(ATTR_PROTECT_ENABLED, FileAttribute.FILE_MESSAGE_PROTECT_PROPERTY, b? "true" : "false");
    }

    public boolean getStagingEnabled() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_STAGE_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setStagingEnabled(boolean b) {
        setAttribute(ATTR_STAGING_ENABLED, FileAttribute.FILE_MESSAGE_STAGE_PROPERTY, b? "true" : "false");
    }

    public String getArchiveDirectory() {
        return getAttribute(FileAttribute.FILE_MESSAGE_ARCHIVE_DIR_PROPERTY);
    }

    public void setArchiveDirectory(String s) {
        setAttribute(ATTR_ARCHIVE_DIR, FileAttribute.FILE_MESSAGE_ARCHIVE_DIR_PROPERTY, s);
    }

    public String getProtectDirectory() {
        return getAttribute(FileAttribute.FILE_MESSAGE_PROTECT_DIR_PROPERTY);
    }

    public void setProtectDirectory(String s) {
        setAttribute(ATTR_PROTECT_DIR, FileAttribute.FILE_MESSAGE_PROTECT_DIR_PROPERTY, s);
    }

    public String getStagingDirectory() {
        return getAttribute(FileAttribute.FILE_MESSAGE_STAGE_DIR_PROPERTY);
    }

    public void setStagingDirectory(String s) {
        setAttribute(ATTR_STAGING_DIR, FileAttribute.FILE_MESSAGE_STAGE_DIR_PROPERTY, s);
    }

    public boolean getArchiveDirIsRelative() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_ARCHIVE_DIR_IS_RELATIVE);
        return s != null && s.equals("true");
    }

    public void setArchiveDirIsRelative(boolean b) {
        setAttribute(ATTR_ARCHIVE_DIR_IS_RELATIVE, FileAttribute.FILE_MESSAGE_ARCHIVE_DIR_IS_RELATIVE, b? "true" : "false");
    }

    public boolean getProtectDirIsRelative() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_PROTECT_DIR_IS_RELATIVE);
        return s != null && s.equals("true");
    }

    public void setProtectDirIsRelative(boolean b) {
        setAttribute(ATTR_PROTECT_DIR_IS_RELATIVE, FileAttribute.FILE_MESSAGE_PROTECT_DIR_IS_RELATIVE, b? "true" : "false");
    }

    public boolean getStagingDirIsRelative() {
        String s = getAttribute(FileAttribute.FILE_MESSAGE_STAGE_DIR_IS_RELATIVE);
        return s != null && s.equals("true");
    }

    public void setStagingDirIsRelative(boolean b) {
        setAttribute(ATTR_STAGING_DIR_IS_RELATIVE, FileAttribute.FILE_MESSAGE_STAGE_DIR_IS_RELATIVE, b? "true" : "false");
    }

}
