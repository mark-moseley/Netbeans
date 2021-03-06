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
/*
 * CodeClipHandler.java
 *
 * Created on July 27, 2006, 10:16 AM
 *
 * Handler/SAX parser that grabs the data from the file
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 * @see layer.xml
 */

package org.netbeans.modules.visualweb.palette.codeclips;
import java.util.LinkedList;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class CodeClipHandler extends DefaultHandler {

    public static final String XML_ROOT = "codeclip_palette_item"; // NOI18N
    public static final String ATTR_VERSION = "version"; // NOI18N
    public static final String LATEST_VERSION = "1.0"; // NOI18N
    public static final String TAG_BODY = "body"; // NOI18N
    public static final String ATTR_CLASSNAME = "name"; // NOI18N
    public static final String ATTR_CUSTNAME = "name"; // NOI18N
    public static final String TAG_ICON16 = "icon16"; // NOI18N
    public static final String ATTR_URL = "urlvalue"; // NOI18N
    public static final String TAG_ICON32 = "icon32"; // NOI18N
    public static final String TAG_DESCRIPTION = "description"; // NOI18N
    public static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    public static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    public static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N

    private LinkedList<String> bodyLines;
    private boolean insideBody = false;

    //raw data read from the file
//    private HashMap attributeMap;
    private String body;
    private String icon16URL;
    private String icon32URL;
    private String bundleName;
    private String displayNameKey;
    private String tooltipKey;

    public String getBody() { return body; }
    public String getIcon16URL() { return icon16URL; }
    public String getIcon32URL() { return icon32URL; }
    public String getBundleName() { return bundleName; }
    public String getDisplayNameKey() { return displayNameKey; }
    public String getTooltipKey() { return tooltipKey; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (XML_ROOT.equals(qName)) {
            String version = attributes.getValue(ATTR_VERSION);
            if (version == null) {
                String message = NbBundle.getBundle(CodeClipHandler.class)
                    .getString("MSG_UnknownEditorPaletteItemVersion"); // NOI18N
                throw new SAXException(message);
            } else if (!version.equals(LATEST_VERSION)) { // NOI18N
                String message = NbBundle.getBundle(CodeClipHandler.class)
                    .getString("MSG_UnsupportedEditorPaletteItemVersion"); // NOI18N
                throw new SAXException(message);
            }
        } else if (TAG_BODY.equals(qName)) {
            bodyLines = new LinkedList<String>();
            insideBody = true;
            //We could ultimately parse the body here?  Then we don't need to figure out the bundle.property file.
        } else if (TAG_ICON16.equals(qName)) {
            icon16URL = attributes.getValue(ATTR_URL);
            // TODO support also class resource name for icons
        } else if (TAG_ICON32.equals(qName)) {
            icon32URL = attributes.getValue(ATTR_URL);
            // TODO support also class resource name for icons
        } else if (TAG_DESCRIPTION.equals(qName)) {
            bundleName = attributes.getValue(ATTR_BUNDLE);
            displayNameKey = attributes.getValue(ATTR_DISPLAY_NAME_KEY);
            tooltipKey = attributes.getValue(ATTR_TOOLTIP_KEY);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (TAG_BODY.equals(qName)) {
            insideBody = false;
            body = trimSurroundingLines(bodyLines);
        }
    }
    
    public void characters(char buf[], int offset, int len)
        throws SAXException
    {
        if (insideBody) {
            String chars = new String(buf, offset, len).trim();
            bodyLines.add(chars + "\n");
        }
    }

    /**
     * Trims empty lines from the beginning and the end of the line list
     */
    private String trimSurroundingLines(LinkedList/*<String>*/ lines) {
        
        int nlines = lines.size();
        
        int firstNonEmpty = nlines;

        //going from the beginning and skipping empty lines until the first nonempty line occurs
        for (int i = 0; i < firstNonEmpty; i++) {
            String line = (String)lines.get(i);
            if (line.trim().length() != 0)
                firstNonEmpty = i;
        }
            
        int lastNonEmpty = -1;
        
        //going from the end and skipping empty lines until the first nonempty line occurs
        for (int i = nlines - 1; i > lastNonEmpty; i--) {
            String line = (String)lines.get(i);
            if (line.trim().length() != 0)
                lastNonEmpty = i;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = firstNonEmpty; i <= lastNonEmpty; i++)
            sb.append((String)lines.get(i));
        
        String body = sb.toString();
        if (body.length() > 0  && body.charAt(body.length() - 1) == '\n') // cut trailing new-line
            body = body.substring(0, body.length() - 1); 
        
        return body;
    }
}

