/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.netbeans.modules.subversion.client.cli.SvnCommand.Arguments;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.openide.xml.XMLUtil;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Stupka
 */
public class ListCommand extends SvnCommand {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private StringBuffer output = new StringBuffer();
    private final SVNUrl url;
    private final SVNRevision revision;
    private final boolean recursive;

    public ListCommand(SVNUrl url, SVNRevision revision, boolean recursive) {
        this.url = url;
        this.revision = revision;
        this.recursive = recursive;
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));        
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.LS;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("list");
        if (recursive) {
            arguments.add("-R");
        }        
        arguments.add("--xml");
        arguments.add(revision);
        arguments.add(url); 
    }

    @Override
    public void outputText(String lineString) {
        output.append(lineString);
    }
    
    public ISVNDirEntry[] getEntries() throws SVNClientException {
        try {
            XMLReader saxReader = XMLUtil.createXMLReader();

            XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
            saxReader.setContentHandler(xmlEntriesHandler);
            saxReader.setErrorHandler(xmlEntriesHandler);
            InputSource source = new InputSource(new ByteArrayInputStream(output.toString().getBytes()));

            saxReader.parse(source);
            return xmlEntriesHandler.getEntryAttributes();
            
        } catch (SAXException ex) {
            throw new SVNClientException(ex);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        
    }
    
    private class XmlEntriesHandler extends DefaultHandler {
        
        private static final String LIST_ELEMENT_NAME   = "list";   // NOI18N
        private static final String ENTRY_ELEMENT_NAME  = "entry";  // NOI18N
        private static final String NAME_ELEMENT_NAME   = "name";   // NOI18N
        private static final String SIZE_ELEMENT_NAME   = "size";   // NOI18N
        private static final String COMMIT_ELEMENT_NAME = "commit"; // NOI18N
        private static final String AUTHOR_ELEMENT_NAME = "author"; // NOI18N        
        private static final String DATE_ELEMENT_NAME   = "date";   // NOI18N        
        
        private static final String KIND_ATTRIBUTE      = "kind";   // NOI18N        
        private static final String PATH_ATTRIBUTE      = "path";   // NOI18N        
        private static final String REVISION_ATTRIBUTE  = "revision";   // NOI18N

        private String REVISION_ATTR                    = "revision_attr";
        private String KIND_ATTR                        = "kind_attr";
        private String PATH_ATTR                        = "path_attr";;
        
        private List<ISVNDirEntry> entries = new ArrayList<ISVNDirEntry>();        
//        <?xml version="1.0"?>
//        <lists>
//        <list
//            path="file:///foo">
//            <entry kind="file">
//                <name>Bar1.java</name>
//                <commit revision="2">
//                    <author>Hugo</author>
//                    <date>2008-02-31T16:48:08.105011Z</date>
//                </commit>
//            </entry>
//            <entry kind="file">
//                <name>Bar2.java</name>
//                <commit revision="2">
//                    <author>Hugo</author>
//                    <date>2008-02-31T16:48:08.105011Z</date>
//                </commit>
//            </entry>        
//        </list>
//        </lists>
            
        
        private Map<String, String> values;
        private String tag;               

        @Override
        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {            
            tag = qName.trim();                
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                        
                values = new HashMap<String, String>();
                values.put(KIND_ATTR, elementAttributes.getValue(KIND_ATTRIBUTE));
            } else if (COMMIT_ELEMENT_NAME.equals(qName)) {                                
                values.put(REVISION_ATTR, elementAttributes.getValue(REVISION_ATTRIBUTE));
            }
            if(values != null) {
                values.put(tag, "");
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(values == null) {
                return;
            }
            String s = toString(length, ch, start);
            values.put(tag, s);
        }                
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                                
                if(values != null) {
                    String name = values.get(NAME_ELEMENT_NAME);
                    if (name == null) throw new SAXException("'name' tag expected under 'entry'");
                                                                    
                    String commit = values.get(COMMIT_ELEMENT_NAME);
                    if (commit == null) throw new SAXException("'commit' tag expected under 'entry'");
                    
                    String author = values.get(AUTHOR_ELEMENT_NAME);
                    
                    Date date = null;
                    String dateValue = values.get(DATE_ELEMENT_NAME);
                    if(dateValue != null) {
                        try {
                            date = dateFormat.parse(dateValue);
                        } catch (ParseException ex) {
                            // ignore
                        }
                    }
                    
                    Number revision = null;
                    String revisionValue = values.get(REVISION_ATTR);
                    if(revisionValue != null && !revisionValue.trim().equals("")) {
                        try {
                            revision = new SVNRevision.Number(Long.parseLong(revisionValue));
                        } catch (NumberFormatException e) {
                            revision = new SVNRevision.Number(-1);
                        }
                    }
                
                    long size = 0;
                    String kindValue = values.get(KIND_ATTR);
                    SVNNodeKind kind = SVNNodeKind.UNKNOWN;
                    if ("file".equals(kindValue)) {
                        
                        kind = SVNNodeKind.FILE;					
                        
                        String sizeValue = values.get(SIZE_ELEMENT_NAME);
                        if (sizeValue == null) throw new SAXException("'size' tag expected under 'entry'");                        
                        try {
                            size = Long.parseLong(sizeValue);
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                        
                    } else if ("dir".equals(kindValue)) {
                        kind = SVNNodeKind.DIR;
                    }
                                        
                    entries.add(new DirEntry(name, date, revision, false, author, kind, size));
                }
                values = null;
            } 
        }
                
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public ISVNDirEntry[] getEntryAttributes() {            
            return entries != null ? entries.toArray(new ISVNDirEntry[entries.size()]) : new ISVNDirEntry[] {} ;
        }

        private String toString(int length, char[] ch, int start) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            return new String(c);
        }
    }       
    
    private class DirEntry implements ISVNDirEntry {

        private String path;
        private Date lastChangedDate;
        private Number lastChangedRevision;
        private boolean hasProps;
        private String lastCommitAuthor;
        private SVNNodeKind kind;
        private long size;

        public DirEntry(String path, Date lastChangedDate, Number lastChangedRevision, boolean hasProps, String lastCommitAuthor, SVNNodeKind kind, long size) {
            this.path = path;
            this.lastChangedDate = lastChangedDate;
            this.lastChangedRevision = lastChangedRevision;
            this.hasProps = hasProps;
            this.lastCommitAuthor = lastCommitAuthor;
            this.kind = kind;
            this.size = size;
        }
        
        public String getPath() {
            return path;
        }

        public Date getLastChangedDate() {
            return lastChangedDate;
        }

        public Number getLastChangedRevision() {
            return lastChangedRevision;
        }

        public boolean getHasProps() {
            return hasProps;
        }

        public String getLastCommitAuthor() {
            return lastCommitAuthor;
        }

        public SVNNodeKind getNodeKind() {
            return kind;
        }

        public long getSize() {
            return size;
        }
        
    }
}
