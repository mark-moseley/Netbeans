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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.VariablesFormatter;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.VariablesFilterAdapter;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;


/**
 *
 * @author   Martin Entlicher
 */
public class VariablesFormatterFilter extends VariablesFilterAdapter {

    static Map<Object, String> FORMATTED_CHILDREN_VARS = new WeakHashMap<Object, String>();

    private JPDADebugger debugger;

    public VariablesFormatterFilter(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    public String[] getSupportedTypes () {
        VariablesFormatter[] formatters = VariablesFormatter.loadFormatters();
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < formatters.length; i++) {
            if (!formatters[i].isIncludeSubTypes()) {
                String[] ts = formatters[i].getClassTypes();
                for (String t : ts) {
                    types.add(t);
                }
            }
        }
        return types.toArray(new String[] {});
    }
    
    public String[] getSupportedAncestors () {
        VariablesFormatter[] formatters = VariablesFormatter.loadFormatters();
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < formatters.length; i++) {
            if (formatters[i].isIncludeSubTypes()) {
                String[] ts = formatters[i].getClassTypes();
                for (String t : ts) {
                    types.add(t);
                }
            }
        }
        return types.toArray(new String[] {});
    }

    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    @Override
    public Object[] getChildren (
        TreeModel original, 
        Variable variable, 
        int from, 
        int to
    ) throws UnknownTypeException {

        if (variable instanceof ObjectVariable) {
            ObjectVariable ov = (ObjectVariable) variable;
            JPDAClassType ct = ov.getClassType();

            if (ct == null) {
                return original.getChildren (variable, from, to);
            }

            VariablesFormatter f = getFormatterForType(ct);
            if (f != null) {
                if (f.isUseChildrenVariables()) {
                    Map<String, String> chvs = f.getChildrenVariables();
                    Object[] ch = new Object[chvs.size()];
                    int i = 0;
                    for (String name : chvs.keySet()) {
                        try {
                            java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                            evaluateMethod.setAccessible(true);
                            Object var = evaluateMethod.invoke(ov, chvs.get(name));
                            FORMATTED_CHILDREN_VARS.put(var, name);
                            ch[i++] = var;
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                            return new Object[] {};
                        }
                    }
                    return ch;
                } else {
                    String code = f.getChildrenFormatCode();
                    if (code != null && code.length() > 0) {
                        try {
                            java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                            evaluateMethod.setAccessible(true);
                            Variable ret = (Variable) evaluateMethod.invoke(ov, code);
                            return getChildren(original, ret, from, to);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        
        return original.getChildren (variable, from, to);
    }

    private VariablesFormatter getFormatterForType(JPDAClassType ct) {
        VariablesFormatter[] formatters = VariablesFormatter.loadFormatters();
        String cname = ct.getName();
        for (VariablesFormatter f: formatters) {
            if (!f.isEnabled()) {
                continue;
            }
            String[] types = f.getClassTypes();
            boolean applies = false;
            for (String type : types) {
                if (type.equals(cname) || (f.isIncludeSubTypes() && isInstanceOf(ct, type))) {
                    applies = true;
                    break;
                }
            }
            if (applies) {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns number of filtered children for given variable.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     *
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process
     *          is time consuming, and will be performed off-line
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  number of filtered children for given variable
     */
    @Override
    public int getChildrenCount (TreeModel original, Variable variable) 
    throws UnknownTypeException {
        
        return Integer.MAX_VALUE;
    }

    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    @Override
    public boolean isLeaf (TreeModel original, Variable variable) 
    throws UnknownTypeException {
        if (variable instanceof ObjectVariable) {
            ObjectVariable ov = (ObjectVariable) variable;
            JPDAClassType ct = ov.getClassType();

            if (ct == null) {
                return original.isLeaf (variable);
            }

            /* TODO: Must not be performed in AWT!
            VariablesFormatter f = getFormatterForType(ct);
            if (f != null) {
                String expandTestCode = f.getChildrenExpandTestCode();
                if (expandTestCode != null && expandTestCode.length() > 0) {
                    try {
                        java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                        evaluateMethod.setAccessible(true);
                        Variable ret = (Variable) evaluateMethod.invoke(ov, expandTestCode);
                        return !"true".equals(ret.getValue());
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
             */
        }
        String type = variable.getType ();
        // PATCH for J2ME
        if ( isLeafType (type) 
        ) return true;
        return original.isLeaf (variable);
    }
    
    @Override
    public Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException {

        if (!(variable instanceof ObjectVariable)) {
            return original.getValueAt (variable, columnID);
        }
        String type = variable.getType ();
        ObjectVariable ov = (ObjectVariable) variable;
        JPDAClassType ct = ov.getClassType();
        if (ct == null) {
            return original.getValueAt (variable, columnID);
        }
        VariablesFormatter f = getFormatterForType(ct);
        if (f != null &&
            ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID)) {
            String code = f.getValueFormatCode();
            if (code != null && code.length() > 0) {
                try {
                    java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                    evaluateMethod.setAccessible(true);
                    Variable ret = (Variable) evaluateMethod.invoke(ov, code);
                    return getValueAt(original, ret, columnID);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
        if ( isToStringValueType (type) &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            try {
                return "\""+ov.getToStringValue ()+"\"";
            } catch (InvalidExpressionException ex) {
                // Not a supported operation (e.g. J2ME, see #45543)
                // Or missing context or any other reason
                Logger.getLogger(VariablesFormatterFilter.class.getName()).fine("getToStringValue() "+ex.getLocalizedMessage());
                if ( (ex.getTargetException () != null) &&
                     (ex.getTargetException () instanceof 
                       UnsupportedOperationException)
                ) {
                    // PATCH for J2ME. see 45543
                    return original.getValueAt (variable, columnID);
                }
                return ex.getLocalizedMessage ();
            }
        }
        return original.getValueAt (variable, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Variable variable,
                           String columnID, Object value) throws UnknownTypeException {
        String type = variable.getType();
        if (isToStringValueType(type) &&
            (columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
             columnID == Constants.WATCH_VALUE_COLUMN_ID)) {
            String expression = (String) value;
            if (expression.startsWith("\"") && expression.endsWith("\"") && expression.length() > 1) {
                // Create a new StringBuffer object with the desired content:
                expression = "new " + type + "(\"" + convertToStringInitializer(expression.substring(1, expression.length() - 1)) + "\")";
                original.setValueAt(variable, columnID, expression);
                return ;
            }
        }
        original.setValueAt(variable, columnID, value);
    }
    
    private static String convertToStringInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b");
                    break;
                case '\f':
                    sb.append ("\\f");
                    break;
                case '\\':
                    sb.append ("\\\\");
                    break;
                case '\t':
                    sb.append ("\\t");
                    break;
                case '\r':
                    sb.append ("\\r");
                    break;
                case '\n':
                    sb.append ("\\n");
                    break;
                case '\"':
                    sb.append ("\\\"");
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
    
    
    // other methods ...........................................................
    
    private static HashSet leafType;
    private static boolean isLeafType (String type) {
        if (leafType == null) {
            leafType = new HashSet ();
            leafType.add ("java.lang.String");
            leafType.add ("java.lang.Character");
            leafType.add ("java.lang.Integer");
            leafType.add ("java.lang.Float");
            leafType.add ("java.lang.Byte");
            leafType.add ("java.lang.Boolean");
            leafType.add ("java.lang.Double");
            leafType.add ("java.lang.Long");
            leafType.add ("java.lang.Short");
        }
        return leafType.contains (type);
    }
    
    private static HashSet toStringValueType;
    private static boolean isToStringValueType (String type) {
        if (toStringValueType == null) {
            toStringValueType = new HashSet ();
            toStringValueType.add ("java.lang.StringBuffer");
            toStringValueType.add ("java.lang.StringBuilder");
        }
        return toStringValueType.contains (type);
    }
    
    private static boolean isInstanceOf(JPDAClassType ct, String className) {
        if (ct == null) return false;
        try {
            java.lang.reflect.Method isInstanceOfMethod = ct.getClass().getMethod("isInstanceOf", String.class);
            return (Boolean) isInstanceOfMethod.invoke(ct, className);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

}
