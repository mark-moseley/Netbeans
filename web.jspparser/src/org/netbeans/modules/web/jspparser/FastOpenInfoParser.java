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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.jspparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.WebModule;
import org.openide.filesystems.FileObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.netbeans.modules.xml.api.EncodingUtil;

/**
 * JSP 'open info' parser allowing to fastly determine encoding for JSPs in standart syntax
 * with DD NOT specifying JSPs encodinf or syntax (at least 95% af all JSPs)
 *
 * How the encoding is currently detected:
 * 1) find deplyment descriptor from given webmodule
 * 2) if found, parse it and find following elements
 *       <jsp-property-group>
 *           <page-encoding>
 *           ||
 *           <is-xml>
 *       </jsp-property-group>
 * 3) if any of the nested elements found, give it up and return null (and let jasper parser to determine the encoding)
 * 4) if the DD is not found or it doesn't contain the elements from #2 test if the file is JSP document (according to the extension)
 * 5) if the file is a XML document give it up (so far - we can easily implement a simple enc. parser for XMLs as well)
 * 6) the page is standard syntax - parse first 8kB of text and...
 * 7) if <%@page encoding="xxx"%> is found return the encoding value
 * 8) if <%@page encoding="xxx"%> is NOT found find <%@page contentType="mimetype; char-set=xxx"%>
 * 9) if CT found return encoding from it
 *
 * @author Marek Fukala
 */
public class FastOpenInfoParser {
    
    static final boolean debug = Boolean.getBoolean("netbeans.debug.fastopeninfo"); // NOI18N
    
    static FastOpenInfoParser get(WebModule wm) {
        return new FastOpenInfoParser(wm);
    }
    
    private WebModule wm;
    
    /** Creates a new instance of FastOpenInfoParser */
    private FastOpenInfoParser(WebModule wm) {
        this.wm = wm;
    }
    
    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject fo, boolean useEditor) {
        long a = System.currentTimeMillis();
        try {
            if(wm != null && wm.getDocumentBase() != null && useEditor) return null; //better let the parser do it
            
            //if there isn't a webmodule detect the encoding from the file only
            if(wm != null) {
                //find deployment descriptor
                FileObject documentBase = wm.getDocumentBase();
                if(documentBase != null) {
                    org.netbeans.modules.web.api.webmodule.WebModule wmod = org.netbeans.modules.web.api.webmodule.WebModule.getWebModule(documentBase);
                    if (wmod != null) { //can be null if the document out of any webmodule
                        FileObject dd = wmod.getDeploymentDescriptor();
                        //test whether the DD exists, if not parse the JSP file
                        if (dd != null) {
                            //parse the DD and try to find <jsp-property-group> element with <page-encoding> and <is-xml> elements
                            DDParseInfo ddParseInfo = parse(new InputSource(dd.getInputStream())); //parse with default encoding
                            //if the DD defines encoding or marks jsps as xml documents return null
                            if (ddParseInfo.definesEncoding || ddParseInfo.marksXMLDocuments) {
                                return null;
                            }
                        }
                    }
                }
            }

            String enc = null;
            
            //get encoding from the disk file if webmodule is null and useEditor is true (during file save)
            //XXX may be fixed better - to get the editor document instance from the fileobject (but I need to add some deps)
            
            //#64418 - create a ByteArrayInputStream - we need a an inputstream with marks supported
            byte[] buffer = new byte[8192*4];
            InputStream _is = fo.getInputStream();
            int readed = _is.read(buffer);
            InputStream is = new ByteArrayInputStream(buffer,0,readed);
            _is.close();
            
            if(isXMLSyntax(fo)) {
                //XML document - detect encoding acc. to fisrt 4 bytes or xml prolog
                enc = EncodingUtil.detectEncoding(is);
            } else {
                //JSP in standart syntax
                //find <%@page encoding or contentType attributes
                enc = parseEncodingFromFile(is);
            }
            
            if(debug) System.out.println("[fast open parser] detected " + enc + " encoding.");
            return enc == null ? null : new JspParserAPI.JspOpenInfo(isXMLSyntax(fo), enc);
            
        } catch(IOException e) {
            //do not handle
        } catch(SAXException se) {
            //do not handle
        } catch(ParserConfigurationException pce) {
            //do not handle
        } finally {
            if(debug) System.out.println("[fast open parser] taken " + (System.currentTimeMillis() - a) + "ms.");
        }
        return null;
    }
    
    private static String parseEncodingFromFile(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is); //read with default encoding
        //read only first 8kB of text
        char[] buffer = new char[8192];
        int readed = isr.read(buffer);
        isr.close();
        
        return parseJspText(buffer, readed);
    }
    
    private static boolean isXMLSyntax(FileObject fo) {
        String ext = fo.getExt();
        if(ext != null && ("jspx".equalsIgnoreCase(ext) || "tagx".equalsIgnoreCase(ext))) return true;
        else return false;
    }
    
    //JSP encoding parser
    private static final String PAGE = "page";
    private static final String ENCODING = "pageEncoding";
    private static final String CONTENTYPE = "contentType";
    private static final String CHARSET = "charset=";
    
    private static final int P_INIT = 0;
    private static final int P_LT = 1; //after <
    private static final int P_LT_PER = 2; //after <%
    private static final int P_LT_PER_ATS = 3; //after <%@
    private static final int P_PD = 4; //in page directive
    private static final int P_APER = 5; //after closing %
    
    private static final int P_ENC = 7; //after 'encoding' attribute
    private static final int P_ENC_EQ = 8; //after encoding=
    private static final int P_ENC_EQ_VAL = 9; //after encoding="
    
    private static final int P_CT = 11; //after 'contentType' attribute
    private static final int P_CT_EQ = 12; //after contentType=
    private static final int P_CT_EQ_VAL = 13; //after contentType="
    private static final int P_CT_VAL_CHS = 14; //after contentType="TYPE; char-set=
    
    private static String parseJspText(char[] buffer, int len) {
        String contentType = null;
        
        int state = P_INIT;
        int i = 0;
        int pos = -1;
        while(i < len) {
            char c = buffer[i];
            
            switch(state) {
                case P_INIT:
                    if(c == '<') state = P_LT;
                    i++;
                    break;
                case P_LT:
                    switch(c) {
                        case '%' :
                            state = P_LT_PER;
                            break;
                        default: state = P_INIT;
                    }
                    i++;
                    break;
                    
                case P_LT_PER:
                    switch(c) {
                        case '@':
                            state = P_LT_PER_ATS;
                            break;
                        default: state = P_INIT;
                    }
                    i++;
                    break;
                case P_LT_PER_ATS:
                    if(c == ' ' || c == '\t') {
                        i++;
                        break;
                    } else if(prescanFor(buffer, i, PAGE)) {
                        state = P_PD;
                        i = i + PAGE.length();
                        break;
                    }
                    state = P_INIT;
                    i++;
                    break;
                case P_PD:
                    if(prescanFor(buffer, i, ENCODING)) {
                        state = P_ENC;
                        i = i + ENCODING.length();
                        break;
                    } else if(prescanFor(buffer, i, CONTENTYPE)) {
                        state = P_CT;
                        i = i + CONTENTYPE.length();
                        break;
                    } else if(c == '%') state = P_APER;
                    i++;
                    break;
                case P_APER:
                    if(c == '>') state = P_INIT;
                    else state = P_PD;
                    i++;
                    break;
                case P_ENC:
                    switch(c) {
                        case ' ':
                        case '\t':
                            ;
                            break;
                        case '=':
                            state = P_ENC_EQ;
                            break;
                        case '%':
                            state = P_APER;
                            break;
                        default:
                            state = P_PD;
                    }
                    i++;
                    break;
                case P_ENC_EQ:
                    switch(c) {
                        case ' ':
                        case '\t':
                            break;
                        case '"':
                            state = P_ENC_EQ_VAL;
                            pos = i + 1;
                            break;
                        case '%':
                            state = P_APER;
                            break;
                        default:
                            state = P_PD;
                    }
                    i++;
                    break;
                case P_ENC_EQ_VAL:
                    switch(c) {
                        case '"': return new String(buffer, pos, i - pos); //return the encoding attr value
                        default:
                    }
                    i++;
                    break;
                    
                case P_CT:
                    switch(c) {
                        case ' ':
                        case '\t':
                            break;
                        case '=':
                            state = P_CT_EQ;
                            break;
                        case '%':
                            state = P_APER;
                            break;
                        default:
                            state = P_PD;
                    }
                    i++;
                    break;
                case P_CT_EQ:
                    switch(c) {
                        case ' ':
                        case '\t':
                            break;
                        case '"':
                            state = P_CT_EQ_VAL;
                            break;
                        case '%':
                            state = P_APER;
                            break;
                        default:
                            state = P_PD;
                    }
                    i++;
                    break;
                case P_CT_EQ_VAL:
                    if(prescanFor(buffer, i, CHARSET)) {
                        state = P_CT_VAL_CHS;
                        i = i + CHARSET.length();
                        pos = i;
                        break;
                    } else if(c == '"') {
                        state = P_PD;
                        break;
                    }
                    i++;
                    break;
                case P_CT_VAL_CHS:
                    switch(c) {
                        case '"':
                            contentType = new String(buffer, pos, i - pos); //return the encoding attr value
                            state = P_PD;
                            break;
                        default:
                    }
                    i++;
                    break;
                    
            } //eof state switch
        }
        
        //returns either contentType value or null; encoding is returned directly from the parser (has priority over CT)
        return contentType;
    }
    
    
    private static boolean prescanFor(char[] buffer, int position, String text) {
        if((buffer.length - position) < text.length()) return false; //too short buffer - the text cannot be there
        for(int i = 0; i < text.length(); i++) {
            if(buffer[position+i] != text.charAt(i)) return false;
        }
        return true;
    }
    

    static final String JSP_PROPERTY_GROUP = "jsp-property-group";
    static final String PAGE_ENCODING = "page-encoding";
    static final String IS_XML = "is-xml";
    
    /** returns an array of booleans - the first states whether the dd contains a <jsp-property-group> element
     * with defined encoding resp. marks a set of JSPs to be xml documents. */
    private static DDParseInfo parse(InputSource src) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        final DDParseInfo ddParseInfo = new DDParseInfo();
        
        class Handler extends DefaultHandler {
            private boolean inJspPropertyGroup = false;
            public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                String tagName = qname.toLowerCase();
                if(JSP_PROPERTY_GROUP.equals(tagName)) inJspPropertyGroup = true;
                if(inJspPropertyGroup) {
                    if(PAGE_ENCODING.equals(tagName)) ddParseInfo.definesEncoding = true;
                    if(IS_XML.equals(tagName)) ddParseInfo.marksXMLDocuments = true;
                }
            }
            public void endElement(String uri, String localname, String qname) throws SAXException {
                String tagName = qname.toLowerCase();
                if(JSP_PROPERTY_GROUP.equals(tagName)) inJspPropertyGroup = false;
            }
            public InputSource resolveEntity (String publicId, String systemId) {
                return new InputSource(new StringReader("")); //prevent the parser to use catalog entity resolver
            }
        }
        parser.parse(src, new Handler());
        return ddParseInfo;
    }
    
    private static final class DDParseInfo {
        public boolean definesEncoding, marksXMLDocuments;
        public DDParseInfo() {}
    }
    
}
