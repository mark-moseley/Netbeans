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

package org.netbeans.lib.collab;

import java.util.*;
import org.xml.sax.helpers.DefaultHandler;

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.util.*;
import org.jabberstudio.jso.x.xdata.*;
import org.netbeans.lib.collab.util.SAX;


/**
 * This class is used to parse the xml from the poll response
 *
 * @since version 0.1
 *
 */
public class PollResponse {
    private static final int IN_ID = 1;
    private static final int IN_POLLTYPE = 2;
    private static final int IN_ACCESS = 3;
    private static final int IN_QUESTION = 4;
    private static final int IN_ANSWER = 5;
    private static final int IN_CUSTOM = 6;

    public static final String ELEMENT_FIELD = "field";
    public static final String ELEMENT_VALUE = "value";

    public static final String ATTRIBUTE_VAR = "var";

    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_QUESTION = "question";
    public static final String ATTRIBUTE_POLLTYPE = "polltype";
    public static final String ATTRIBUTE_ACCESS = "access";
    public static final String ATTRIBUTE_CUSTOM = "custom";

    private String _pollID = null;

    private JSOImplementation _jso;
    private StreamDataFactory _sdf;
    private XDataForm _xdf;

    public PollResponse(String message) throws Exception {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
        _xdf = (XDataForm)_sdf.createElementNode(new NSI("x", XDataForm.NAMESPACE), null);
        _xdf.setType(XDataForm.SUBMIT);
        SAX.parse(message, new PollResponseParser());
    }

    public XDataForm getXDataForm() {
        return _xdf;
    }

    public String getPollID() {
        return _pollID;
    }

    // ------------------------------------------
    // Poll Response Parser
    // ------------------------------------------
    class PollResponseParser extends org.xml.sax.helpers.DefaultHandler 
    {
        private int _state;
        private StringBuffer _buf = null;

        public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
            switch (_state) {
                case IN_ANSWER :
                    _buf.append(ch, start, length);
                    XDataField a = _xdf.addField(ATTRIBUTE_QUESTION);
                    a.addValue(_buf.toString());
                    break;
                case IN_CUSTOM :
                    _buf.append(ch, start, length);
                    XDataField c = _xdf.addField(ATTRIBUTE_CUSTOM);
                    c.addValue(_buf.toString());
                    break;
                case IN_ID :
                    _buf.append(ch, start, length);
                    XDataField id = _xdf.addField(ATTRIBUTE_ID);
                    id.addValue(_buf.toString());
                    _pollID = _buf.toString();
                    break;
                case IN_ACCESS :
                    _buf.append(ch, start, length);
                    XDataField access = _xdf.addField(ATTRIBUTE_ACCESS);
                    access.addValue(_buf.toString());
                    break;
                case IN_POLLTYPE :
                    _buf.append(ch, start, length);
                    XDataField pollType = _xdf.addField(ATTRIBUTE_POLLTYPE);
                    pollType.addValue(_buf.toString());
                    break;
                default :
                    throw new org.xml.sax.SAXException("No characters at this stage");
            }
        }

        public void startDocument() throws org.xml.sax.SAXException {
        }

        public void endDocument() throws org.xml.sax.SAXException {
        }

        public void endElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName) throws org.xml.sax.SAXException {
        }

        public void startElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
            if (fqName.equals(ELEMENT_FIELD)) {
                String attr = attributes.getValue(ATTRIBUTE_VAR);
                if (attr.equals(ATTRIBUTE_QUESTION)) _state = IN_ANSWER;
                else if (attr.equals(ATTRIBUTE_CUSTOM)) _state = IN_CUSTOM;
                else if (attr.equals(ATTRIBUTE_ID)) _state = IN_ID;
                else if (attr.equals(ATTRIBUTE_POLLTYPE)) _state = IN_POLLTYPE;
                else if (attr.equals(ATTRIBUTE_ACCESS)) _state = IN_ACCESS;
            } else if (fqName.equals(ELEMENT_VALUE)) {
                _buf = new StringBuffer();
            }
        }
    }
}    
    

  
    


