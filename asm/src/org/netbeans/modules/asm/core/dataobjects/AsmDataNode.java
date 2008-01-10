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

package org.netbeans.modules.asm.core.dataobjects;

import java.lang.reflect.InvocationTargetException;
import java.util.Formatter;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;


public class AsmDataNode extends DataNode {

    private static final String IMAGE_ICON_BASE = 
                "org/netbeans/modules/asm/core/resources/file_asm_16.png";
    
    private static String INITAL_COMMAND = "/set/mars/dist/intel-S2/bin/cc";
    private static String INITAL_ARGS = "/%s";

    private String command;
    private String args;      

    public AsmDataNode(AsmDataObject obj) {
        super(obj, Children.LEAF);
        args = (new Formatter()).format(INITAL_ARGS, obj.getPrimaryFile().getPath()).
                                          toString();

        command = INITAL_COMMAND;

        setIconBaseWithExtension(IMAGE_ICON_BASE);

    }

    

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);

        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
       }



       PropertySupport<String> result = new PropertySupport.ReadWrite<String> (
                    "Compile command",
                    String.class,
                    "Compile command",
                    "Compile command"
                ) {

                    public String getValue() {
                        return command; 
                        //return Utilities.escapeParameters(args);
                    }

                    public void setValue (String val) throws InvocationTargetException {
                       command = val;                        
                    }                 
                };

       result.setValue("oneline", Boolean.TRUE);
       ss.put(result);

       
       result = new PropertySupport.ReadWrite<String> (
                    "Compile arguments",
                    String.class,
                    "Compile arguments",
                    "Compile arguments"
                ) {
                    public String getValue() {                        
                        return args;
                    }

                    public void setValue (String val) throws InvocationTargetException {                      
                        args = val;                                            
                    }                 
                };

       result.setValue("oneline", Boolean.TRUE);

       ss.put(result);
       return s;
    }   
}

