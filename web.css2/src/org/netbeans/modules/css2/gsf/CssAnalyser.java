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
package org.netbeans.modules.css2.gsf;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.fpi.gsf.Error;
import org.netbeans.fpi.gsf.Severity;
import org.netbeans.modules.css2.editor.Property;
import org.netbeans.modules.css2.editor.PropertyModel;
import org.netbeans.modules.css2.parser.CSSParserTreeConstants;
import org.netbeans.modules.css2.parser.NodeVisitor;
import org.netbeans.modules.css2.parser.SimpleNode;
import org.netbeans.modules.css2.parser.SimpleNodeUtil;
import org.netbeans.sfpi.gsf.DefaultError;
import org.openide.util.NbBundle;

/**
 * @todo Add some support for CSS versions
 * @todo Localize !!!!!!!
 *
 * @author marek
 */
public class CssAnalyser {

    private static final String UNKNOWN_PROPERTY = "unknown_property";
    private CSSParserResult result;
    
    public CssAnalyser(CSSParserResult result) {
        this.result = result;
    }

    public List<Error> checkForErrors(final SimpleNode node) {
        final ArrayList<Error> errors = new ArrayList();
        final PropertyModel model = PropertyModel.instance();
        NodeVisitor visitor = new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (node.kind() == CSSParserTreeConstants.JJTPROPERTY) {
                    String propertyName = node.image().trim();
                    //check for vendor specific properies - ignore them
                    if (!isVendorSpecificProperty(propertyName) && model.getProperty(propertyName) == null) {
                        //unknown property - report
                        Error error =
                                new DefaultError(UNKNOWN_PROPERTY, 
                                NbBundle.getMessage(CssAnalyser.class, UNKNOWN_PROPERTY, propertyName),
                                null, result.getFile().getFileObject(),
                                node.startOffset(), node.endOffset(), Severity.WARNING);
                        errors.add(error);
                    }

                }
            }
        };
        SimpleNodeUtil.visitChildren(node, visitor);
        return errors;
    }

    public static boolean isVendorSpecificProperty(String propertyName) {
        return propertyName.startsWith("_") || propertyName.startsWith("-");
    }
}
