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
package org.netbeans.modules.cnd.makeproject.configurations;

import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.xml.sax.Attributes;

public class ItemXMLCodec extends XMLDecoder implements XMLEncoder {

    private ItemConfiguration item;
    public final static String ITEM_ELEMENT = "item"; // NOI18N
    public final static String PATH_ATTR = "path"; // NOI18N
    public final static String EXCLUDED_ELEMENT = "excluded"; // FIXUP: < 7 // NOI18N
    public final static String TOOL_ELEMENT = "tool"; // FIXUP: < 7 // NOI18N
    public final static String ITEM_EXCLUDED_ELEMENT = "itemExcluded"; // NOI18N
    public final static String ITEM_TOOL_ELEMENT = "itemTool"; // NOI18N
    public final static String DEBUGGING_ELEMENT = "justfordebugging"; // NOI18N
    public final static String TRUE_VALUE = "true"; // NOI18N
    public final static String FALSE_VALUE = "false"; // NOI18N

    public ItemXMLCodec(ItemConfiguration item) {
        this.item = item;
    }

    // interface XMLDecoder
    public String tag() {
        return item.getId();
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
        String what = "item"; // NOI18N
        int maxVersion = 1;
        checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    public void end() {
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
    }

    // interface XMLDecoder
    public void endElement(String element, String currentText) {
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
        if (item.isDefaultConfiguration()) {
            return;
        }

        xes.elementOpen(ITEM_ELEMENT, new AttrValuePair[]{new AttrValuePair(PATH_ATTR, item.getItem().getPath())});
        if (item.getExcluded().getModified()) {
            xes.element(ITEM_EXCLUDED_ELEMENT, "" + item.getExcluded().getValue()); // NOI18N
        }
        xes.element(ITEM_TOOL_ELEMENT, "" + item.getTool()); // NOI18N
        if (item.getTool() == Tool.CCompiler) {
            CommonConfigurationXMLCodec.writeCCompilerConfiguration(xes, item.getCCompilerConfiguration());
        } else if (item.getTool() == Tool.CCCompiler) {
            CommonConfigurationXMLCodec.writeCCCompilerConfiguration(xes, item.getCCCompilerConfiguration());
        } else if (item.getTool() == Tool.FortranCompiler) {
            CommonConfigurationXMLCodec.writeFortranCompilerConfiguration(xes, item.getFortranCompilerConfiguration());
        } else if (item.getTool() == Tool.CustomTool) {
            CommonConfigurationXMLCodec.writeCustomToolConfiguration(xes, item.getCustomToolConfiguration());
        }
        xes.elementClose(ITEM_ELEMENT);
    }
}
